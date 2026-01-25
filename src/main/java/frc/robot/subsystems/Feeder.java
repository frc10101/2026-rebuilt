// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.ColumnConstants;
import frc.robot.Constants.SparkMaxCanIDs;
import yams.motorcontrollers.SmartMotorController;
import yams.motorcontrollers.SmartMotorControllerConfig;
import yams.motorcontrollers.SmartMotorControllerConfig.ControlMode;
import yams.motorcontrollers.SmartMotorControllerConfig.MotorMode;
import yams.motorcontrollers.SmartMotorControllerConfig.TelemetryVerbosity;
import yams.motorcontrollers.local.SparkWrapper;

/**
 * This is the subsystem for moving fuel between the {@link Indexer} and the {@link Launcher}.
 *
 * <p>It may also be referred to as <i>Network Switch in between RoboRio and Radio</i>.
 */
public class Feeder extends SubsystemBase {
  private double m_motorspeed = 0.0;

  private SmartMotorControllerConfig MotorConfig =
      new SmartMotorControllerConfig(this)
          .withControlMode(ControlMode.CLOSED_LOOP)
          .withClosedLoopController(
              ColumnConstants.Real.kp,
              ColumnConstants.Real.ki,
              ColumnConstants.Real.kd,
              ColumnConstants.Real.maxVelocity,
              ColumnConstants.Real.maxAcceleration)
          .withSimClosedLoopController(
              ColumnConstants.Sim.kp,
              ColumnConstants.Sim.ki,
              ColumnConstants.Sim.kd,
              ColumnConstants.Sim.maxVelocity,
              ColumnConstants.Sim.maxAcceleration)
          .withTelemetry("ColumnMotor", TelemetryVerbosity.HIGH)
          .withGearing(ColumnConstants.gearRatio)
          .withMotorInverted(false)
          .withIdleMode(MotorMode.BRAKE)
          .withStatorCurrentLimit(ColumnConstants.currentLimit);

  private SparkMax m_motor = new SparkMax(SparkMaxCanIDs.ColumnMotor, MotorType.kBrushless);

  // create the smartMotorController
  private SmartMotorController motorController =
      new SparkWrapper(m_motor, DCMotor.getNEO(1), MotorConfig);

  /** Creates a new Feeder. */
  public Feeder() {}

  public Command IntakeFuel() {
    return runOnce(() -> m_motorspeed = ColumnConstants.IntakeSpeed);
  }

  public Command OuttakeFuel() {
    return runOnce(() -> m_motorspeed = ColumnConstants.OuttakeSpeed);
  }

  public Command NoFuel() {
    return runOnce(() -> m_motorspeed = 0);
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    m_motor.set(m_motorspeed);
    SmartDashboard.putNumber("Column Speed", m_motorspeed);

    motorController.updateTelemetry();
  }

  @Override
  public void simulationPeriodic() {
    motorController.simIterate();
  }
}
