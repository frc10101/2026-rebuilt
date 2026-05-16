/*
 * ShotCalculator.java - LUT-backed launcher shot lookup
 *
 * MIT License
 *
 * Copyright (c) 2026 FRC Team 5962 perSEVERE
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND.
 */

package frc.robot.util.Launcher;

import static edu.wpi.first.units.Units.RPM;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.interpolation.InterpolatingDoubleTreeMap;
import edu.wpi.first.units.measure.AngularVelocity;

/**
 * Distance-keyed launcher shot lookup backed by a physics-generated LUT.
 *
 * <p>The physics simulator generates the base table. This class only does direct lookup,
 * interpolation, and light output shaping for the launcher subsystem.
 */
public class ShotCalculator {

  /**
   * The result of calculate(). RPM to spin up, time of flight, heading to aim at, and a 0-100
   * confidence score.
   */
  public record LaunchParameters(
      double rpm,
      double timeOfFlightSec,
      Rotation2d driveAngle,
      double driveAngularVelocityRadPerSec,
      boolean isValid,
      double confidence,
      double solvedDistanceM,
      int iterationsUsed,
      boolean warmStartUsed) {

    public static final LaunchParameters INVALID =
        new LaunchParameters(0, 0, new Rotation2d(), 0, false, 0, 0, 0, false);
  }

  /**
   * Minimal shot inputs for a distance lookup. The extra fields remain so callers do not need to
   * change yet, but the calculator no longer uses iterative motion solving.
   */
  public record ShotInputs(
      Pose2d robotPose,
      edu.wpi.first.math.kinematics.ChassisSpeeds fieldVelocity,
      edu.wpi.first.math.kinematics.ChassisSpeeds robotVelocity,
      Translation2d hubCenter,
      Translation2d hubForward,
      double visionConfidence,
      double pitchDeg,
      double rollDeg) {

    /** Convenience constructor for callers that do not have pitch/roll data. */
    public ShotInputs(
        Pose2d robotPose,
        edu.wpi.first.math.kinematics.ChassisSpeeds fieldVelocity,
        edu.wpi.first.math.kinematics.ChassisSpeeds robotVelocity,
        Translation2d hubCenter,
        Translation2d hubForward,
        double visionConfidence) {
      this(
          robotPose,
          fieldVelocity,
          robotVelocity,
          hubCenter,
          hubForward,
          visionConfidence,
          0.0,
          0.0);
    }
  }

  /** Tuning parameters for the lookup layer. */
  public static class Config {
    public double launcherOffsetX = 0.20; // meters forward of robot center
    public double launcherOffsetY = 0.0; // meters left of robot center
    public double shooterAngleOffsetRad = 0.0;
  }

  private final Config config;
  private ShotLUT shotLUT = new ShotLUT();
  private final InterpolatingDoubleTreeMap correctionRpmMap = new InterpolatingDoubleTreeMap();
  private final InterpolatingDoubleTreeMap correctionTofMap = new InterpolatingDoubleTreeMap();
  private double rpmOffset = 0;

  public ShotCalculator(Config config) {
    this.config = config;
  }

  /** Default config. */
  public ShotCalculator() {
    this(new Config());
  }

  /**
   * Add a distance/RPM/TOF point to the lookup table. If no angle is supplied, the entry defaults
   * to zero degrees.
   */
  public void loadLUTEntry(double distanceM, double rpm, double tof) {
    if (shotLUT == null) {
      shotLUT = new ShotLUT();
    }
    shotLUT.put(distanceM, new ShotParameters(rpm, 0.0, tof));
  }

  /** Direct access to the interpolated shot parameters at a distance. */
  public ShotParameters getShotParameters(double distanceM) {
    return shotLUT != null ? shotLUT.get(distanceM) : ShotParameters.ZERO;
  }

  private double effectiveRPM(double distanceM) {
    double base = getShotParameters(distanceM).rpm();
    Double correction = correctionRpmMap.get(distanceM);
    return base + (correction != null ? correction : 0.0) + rpmOffset;
  }

  private double effectiveTOF(double distanceM) {
    double base = getShotParameters(distanceM).tofSec();
    Double correction = correctionTofMap.get(distanceM);
    return base + (correction != null ? correction : 0.0);
  }

  private boolean hasShotData() {
    return shotLUT != null && shotLUT.size() > 0;
  }

  /** Resolve the current shot using a direct LUT lookup. */
  public LaunchParameters calculate(ShotInputs inputs) {
    if (inputs == null || inputs.robotPose() == null || inputs.hubCenter() == null) {
      return LaunchParameters.INVALID;
    }
    if (!hasShotData()) {
      return LaunchParameters.INVALID;
    }

    Pose2d robotPose = inputs.robotPose();
    Translation2d hubCenter = inputs.hubCenter();

    double heading = robotPose.getRotation().getRadians();
    double cosH = Math.cos(heading);
    double sinH = Math.sin(heading);

    double launcherX =
        robotPose.getX() + config.launcherOffsetX * cosH - config.launcherOffsetY * sinH;
    double launcherY =
        robotPose.getY() + config.launcherOffsetX * sinH + config.launcherOffsetY * cosH;

    double hubX = hubCenter.getX();
    double hubY = hubCenter.getY();
    double dx = hubX - launcherX;
    double dy = hubY - launcherY;
    double distance = Math.hypot(dx, dy);

    double rpm = effectiveRPM(distance);
    double tof = effectiveTOF(distance);

    Rotation2d driveAngle = new Rotation2d(dx, dy);
    if (config.shooterAngleOffsetRad != 0.0) {
      driveAngle = driveAngle.plus(new Rotation2d(config.shooterAngleOffsetRad));
    }

    return new LaunchParameters(rpm, tof, driveAngle, 0.0, true, 100.0, distance, 0, false);
  }

  /** Layer a per-distance RPM adjustment on top of the base LUT. */
  public void addRpmCorrection(double distance, double deltaRpm) {
    correctionRpmMap.put(distance, deltaRpm);
  }

  /** Layer a per-distance TOF adjustment on top of the base LUT. */
  public void addTofCorrection(double distance, double deltaTof) {
    correctionTofMap.put(distance, deltaTof);
  }

  /** Clear all corrections, back to the raw LUT. */
  public void clearCorrections() {
    correctionRpmMap.clear();
    correctionTofMap.clear();
  }

  /** Bump the RPM offset by delta. Clamped to +/- 200. Bind this to copilot D-pad. */
  public void adjustOffset(double delta) {
    rpmOffset = MathUtil.clamp(rpmOffset + delta, -200, 200);
  }

  /** Reset the RPM offset to zero. */
  public void resetOffset() {
    rpmOffset = 0;
  }

  public double getOffset() {
    return rpmOffset;
  }

  /** Raw time-of-flight from the LUT at this distance. */
  public double getTimeOfFlight(double distanceM) {
    return effectiveTOF(distanceM);
  }

  /** Base RPM at this distance, before any corrections or offset. */
  public double getBaseRPM(double distance) {
    return getShotParameters(distance).rpm();
  }

  /** Reset the lookup state. */
  public void resetWarmStart() {}

  /** Load a ShotLUT generated from the physics simulator or a hand-tuned table. */
  public void loadShotLUT(ShotLUT lut) {
    this.shotLUT = lut != null ? lut : new ShotLUT();
  }

  /** Hood angle at this distance from the ShotLUT. Returns 0 when no angle data is loaded. */
  public double getHoodAngle(double distance) {
    return getShotParameters(distance).angleDeg();
  }

  public double getTargetRPM(double distanceM) {
    return effectiveRPM(distanceM);
  }

  public AngularVelocity getTargetVelocity(double distanceM) {
    return RPM.of(effectiveRPM(distanceM));
  }
}
