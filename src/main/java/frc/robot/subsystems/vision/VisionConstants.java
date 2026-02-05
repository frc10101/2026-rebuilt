// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.vision;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Inches;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;

/** Add your docs here. */
public class VisionConstants {
  // AprilTag layout
  public static AprilTagFieldLayout aprilTagLayout =
      AprilTagFieldLayout.loadField(AprilTagFields.kDefaultField);

  // Camera names, must match names configured on coprocessor
  public static String camera0Name = "cherry";
  public static String camera1Name = "orange";
  public static String camera2Name = "grape";
  public static String camera3Name = "strawberry";

  // Robot to camera transforms
  // (Not used by Limelight, configure in web UI instead)
  public static Transform3d robotToCamera0 =
      new Transform3d(
          Inches.of(0.123),
          Inches.of(4.230),
          Inches.of(11.007),
          new Rotation3d(Degrees.of(0), Degrees.of(0), Degrees.of(180)));
  public static Transform3d robotToCamera1 =
      new Transform3d(
          Inches.of(0.123),
          Inches.of(-4.230),
          Inches.of(11.007),
          new Rotation3d(Degrees.of(0), Degrees.of(0), Degrees.of(180)));
  public static Transform3d robotToCamera2 =
      new Transform3d(
          Inches.of(0.123),
          Inches.of(8.230),
          Inches.of(11.007),
          new Rotation3d(Degrees.of(0), Degrees.of(0), Degrees.of(180)));
  public static Transform3d robotToCamera3 =
      new Transform3d(
          Inches.of(0.123),
          Inches.of(-8.230),
          Inches.of(11.007),
          new Rotation3d(Degrees.of(0), Degrees.of(0), Degrees.of(180)));

  // Basic filtering thresholds
  public static double maxAmbiguity = 0.3;
  public static double maxZError = 0.75;

  // Standard deviation baselines, for 1 meter distance and 1 tag
  // (Adjusted automatically based on distance and # of tags)
  public static double linearStdDevBaseline = 0.02; // Meters
  public static double angularStdDevBaseline = 0.06; // Radians

  // Standard deviation multipliers for each camera
  // (Adjust to trust some cameras more than others)
  public static double[] cameraStdDevFactors =
      new double[] {
        1.0, // Camera 0
        1.0 // Camera 1
      };

  // Multipliers to apply for MegaTag 2 observations
  public static double linearStdDevMegatag2Factor = 0.5; // More stable than full 3D solve
  public static double angularStdDevMegatag2Factor =
      Double.POSITIVE_INFINITY; // No rotation data available
}
