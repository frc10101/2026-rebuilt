// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

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
  private Distance distance = ClimbConstants.RestDistance;

  private SmartMotorControllerConfig MotorConfig =
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

  private SparkMax m_motor = new SparkMax(SparkMaxCanIDs.ClimbMotor, MotorType.kBrushless);

  private SmartMotorController motorController =
      new SparkWrapper(m_motor, DCMotor.getNEO(1), MotorConfig);

  /** Creates a new Climb. */
  public Climb() {}

  public Command GoToPreHangHeight() {
    return runOnce(() -> distance = ClimbConstants.PreHangExtension);
  }

  public Command GoToHangHeight() {
    return runOnce(() -> distance = ClimbConstants.HangDistance);
  }

  public Command GoToReleaseHeight() {
    return runOnce(() -> distance = ClimbConstants.ReleaseDistance);
  }

  public Command GoToRestHeight() {
    return runOnce(() -> distance = ClimbConstants.RestDistance);
  }

  public Distance getHeight() {
    return motorController.getMeasurementPosition();
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    motorController.setPosition(distance);

    // SmartDashboard.putNumber("Left Climb Distance Setpoint", leftDistance.in(Inches));
    // SmartDashboard.putNumber("Left Climb Distance Actual", getLeftHeight().in(Inches));

    motorController.updateTelemetry();
  }

  public void close() {
    m_motor.close();
  }

  @Override
  public void simulationPeriodic() {
    motorController.simIterate();
  }
}
