// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.climb;

import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.ClimbConstants;
import frc.robot.Constants.SparkMaxCanIDs;
import yams.motorcontrollers.SmartMotorController;
import yams.motorcontrollers.SmartMotorControllerConfig;
import yams.motorcontrollers.SmartMotorControllerConfig.ControlMode;
import yams.motorcontrollers.SmartMotorControllerConfig.MotorMode;
import yams.motorcontrollers.SmartMotorControllerConfig.TelemetryVerbosity;
import yams.motorcontrollers.local.SparkWrapper;

/**
 * This is the subsystem for completing climbs.
 *
 * <p>It may also be referred to as <i>Level Up</i>.
 */
public class Climb extends SubsystemBase {
  private Distance leftDistance = ClimbConstants.RestDistance;
  private Distance rightDistance = ClimbConstants.RestDistance;

  private SmartMotorControllerConfig LeftMotorConfig =
      new SmartMotorControllerConfig(this)
          .withControlMode(ControlMode.CLOSED_LOOP)
          .withMechanismCircumference(ClimbConstants.MechanismCircumference)
          .withClosedLoopController(
              ClimbConstants.Real.kp,
              ClimbConstants.Real.ki,
              ClimbConstants.Real.kd,
              ClimbConstants.Real.maxVelocity,
              ClimbConstants.Real.maxAcceleration)
          .withSimClosedLoopController(
              ClimbConstants.Sim.kp,
              ClimbConstants.Sim.ki,
              ClimbConstants.Sim.kd,
              ClimbConstants.Sim.maxVelocity,
              ClimbConstants.Sim.maxAcceleration)
          .withTelemetry("ClimbLeftMotor", TelemetryVerbosity.HIGH)
          .withGearing(ClimbConstants.gearRatio)
          .withMotorInverted(false)
          .withIdleMode(MotorMode.BRAKE)
          .withStatorCurrentLimit(ClimbConstants.currentLimit);

  private SmartMotorControllerConfig RightMotorConfig =
      new SmartMotorControllerConfig(this)
          .withControlMode(ControlMode.CLOSED_LOOP)
          .withMechanismCircumference(ClimbConstants.MechanismCircumference)
          .withClosedLoopController(
              ClimbConstants.Real.kp,
              ClimbConstants.Real.ki,
              ClimbConstants.Real.kd,
              ClimbConstants.Real.maxVelocity,
              ClimbConstants.Real.maxAcceleration)
          .withSimClosedLoopController(
              ClimbConstants.Sim.kp,
              ClimbConstants.Sim.ki,
              ClimbConstants.Sim.kd,
              ClimbConstants.Sim.maxVelocity,
              ClimbConstants.Sim.maxAcceleration)
          .withTelemetry("ClimbRightMotor", TelemetryVerbosity.HIGH)
          .withGearing(ClimbConstants.gearRatio)
          .withMotorInverted(false)
          .withIdleMode(MotorMode.BRAKE)
          .withStatorCurrentLimit(ClimbConstants.currentLimit);

  private SparkMax m_left_motor = new SparkMax(SparkMaxCanIDs.ClimbLeftMotor, MotorType.kBrushless);
  private SparkMax m_right_motor =
      new SparkMax(SparkMaxCanIDs.ClimbRightMotor, MotorType.kBrushless);

  private SmartMotorController leftMotorController =
      new SparkWrapper(m_left_motor, DCMotor.getNEO(1), LeftMotorConfig);

  private SmartMotorController rightMotorController =
      new SparkWrapper(m_right_motor, DCMotor.getNEO(1), RightMotorConfig);

  /** Creates a new Climb. */
  public Climb() {}

  /**
   * Brings climb arm(s) to height specified for prepping hang
   *
   * @param climbType Arms to move (LEFT, RIGHT, BOTH)
   */
  public Command GoToPreHangHeight(ClimbType climbType) {
    return runOnce(
        () -> {
          if (climbType == ClimbType.LEFT || climbType == ClimbType.BOTH)
            leftDistance = ClimbConstants.PreHangExtension;
          if (climbType == ClimbType.RIGHT || climbType == ClimbType.BOTH)
            rightDistance = ClimbConstants.PreHangExtension;
        });
  }

  /**
   * Brings climb arm(s) to height specified for clamping
   *
   * @param climbType Arms to move (LEFT, RIGHT, BOTH)
   */
  public Command GoToHangHeight(ClimbType climbType) {
    return runOnce(
        () -> {
          if (climbType == ClimbType.LEFT || climbType == ClimbType.BOTH)
            leftDistance = ClimbConstants.HangDistance;
          if (climbType == ClimbType.RIGHT || climbType == ClimbType.BOTH)
            rightDistance = ClimbConstants.HangDistance;
        });
  }

  /**
   * Brings climb arm(s) to height specified for releasing from hang
   *
   * @param climbType Arms to move (LEFT, RIGHT, BOTH)
   */
  public Command GoToReleaseHeight(ClimbType climbType) {
    return runOnce(
        () -> {
          if (climbType == ClimbType.LEFT || climbType == ClimbType.BOTH)
            leftDistance = ClimbConstants.ReleaseDistance;
          if (climbType == ClimbType.RIGHT || climbType == ClimbType.BOTH)
            rightDistance = ClimbConstants.ReleaseDistance;
        });
  }

  /**
   * Brings climb arm(s) to height specified for resting
   *
   * @param climbType Arms to move (LEFT, RIGHT, BOTH)
   */
  public Command GoToRestHeight(ClimbType climbType) {
    return runOnce(
        () -> {
          if (climbType == ClimbType.LEFT || climbType == ClimbType.BOTH)
            leftDistance = ClimbConstants.RestDistance;
          if (climbType == ClimbType.RIGHT || climbType == ClimbType.BOTH)
            rightDistance = ClimbConstants.RestDistance;
        });
  }

  /** 
   * Get the current height of left climb arm 
   * 
   * @return The current height of left climb arm
   */
  public Distance getLeftHeight() {
    return leftMotorController.getMeasurementPosition();
  }

  /** 
   * Get the current height of right climb arm 
   * 
   * @return The current height of right climb arm
   */
  public Distance getRightHeight() {
    return rightMotorController.getMeasurementPosition();
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    leftMotorController.setPosition(leftDistance);
    rightMotorController.setPosition(rightDistance);

    // SmartDashboard.putNumber("Left Climb Distance Setpoint", leftDistance.in(Inches));
    // SmartDashboard.putNumber("Left Climb Distance Actual", getLeftHeight().in(Inches));

    leftMotorController.updateTelemetry();
    rightMotorController.updateTelemetry();
  }

  public void close() {
    m_left_motor.close();
    m_right_motor.close();
  }

  @Override
  public void simulationPeriodic() {
    leftMotorController.simIterate();
    rightMotorController.simIterate();
  }
}
