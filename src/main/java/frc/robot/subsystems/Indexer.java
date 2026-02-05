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
import frc.robot.Constants.BeltDexterConstants;
import frc.robot.Constants.SparkMaxCanIDs;
import yams.motorcontrollers.SmartMotorController;
import yams.motorcontrollers.SmartMotorControllerConfig;
import yams.motorcontrollers.SmartMotorControllerConfig.ControlMode;
import yams.motorcontrollers.SmartMotorControllerConfig.MotorMode;
import yams.motorcontrollers.SmartMotorControllerConfig.TelemetryVerbosity;
import yams.motorcontrollers.local.SparkWrapper;

/**
 * This is the subsystem for moving fuel between the {@link Intake} and the {@link Feeder}.
 *
 * <p>It may also be referred to as <i>Clyde</i>.
 */
public class Indexer extends SubsystemBase {
  private double m_motorspeed = 0.0;

  private SmartMotorControllerConfig MotorConfig =
      new SmartMotorControllerConfig(this)
          .withControlMode(ControlMode.CLOSED_LOOP)
          .withClosedLoopController(
              BeltDexterConstants.Real.kp,
              BeltDexterConstants.Real.ki,
              BeltDexterConstants.Real.kd,
              BeltDexterConstants.Real.maxVelocity,
              BeltDexterConstants.Real.maxAcceleration)
          .withSimClosedLoopController(
              BeltDexterConstants.Sim.kp,
              BeltDexterConstants.Sim.ki,
              BeltDexterConstants.Sim.kd,
              BeltDexterConstants.Sim.maxVelocity,
              BeltDexterConstants.Sim.maxAcceleration)
          .withTelemetry("BeltDexterMotor", TelemetryVerbosity.HIGH)
          .withGearing(BeltDexterConstants.gearRatio)
          .withMotorInverted(false)
          .withIdleMode(MotorMode.BRAKE)
          .withStatorCurrentLimit(BeltDexterConstants.currentLimit);

  private SparkMax m_motor = new SparkMax(SparkMaxCanIDs.BeltDexterMotor, MotorType.kBrushless);

  // create the smartMotorController
  private SmartMotorController motorController =
      new SparkWrapper(m_motor, DCMotor.getNEO(1), MotorConfig);

  /** Creates a new Feeder. */
  public Indexer() {}

  public Command IntakeFuel() {
    return runOnce(() -> m_motorspeed = BeltDexterConstants.IntakeSpeed);
  }

  public Command OuttakeFuel() {
    return runOnce(() -> m_motorspeed = BeltDexterConstants.OuttakeSpeed);
  }

  public Command NoFuel() {
    return runOnce(() -> m_motorspeed = 0);
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    m_motor.set(m_motorspeed);
    SmartDashboard.putNumber("BeltDexter Speed", m_motorspeed);

    motorController.updateTelemetry();
  }

  @Override
  public void simulationPeriodic() {
    motorController.simIterate();
  }
}
