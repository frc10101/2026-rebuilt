// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.ColumnConstants;
import frc.robot.Constants.SparkMaxCanIDs;
import yams.motorcontrollers.SmartMotorController;
import yams.motorcontrollers.SmartMotorControllerConfig;
import yams.motorcontrollers.SmartMotorControllerConfig.MotorMode;
import yams.motorcontrollers.SmartMotorControllerConfig.TelemetryVerbosity;
import yams.motorcontrollers.local.SparkWrapper;

public class Feeder extends SubsystemBase {
  private double m_motorspeed = 0.0;
  private SmartMotorControllerConfig MotorConfig =
      new SmartMotorControllerConfig(this)
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

  public Command IntakeFuel(double speed) {
    return runOnce(() -> m_motorspeed = speed);
  }

  public Command OuttakeFuel(double speed) {
    return runOnce(() -> m_motorspeed = -speed);
  }

  public Command NoFuel() {
    return runOnce(() -> m_motorspeed = 0);
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    m_motor.set(m_motorspeed);

    motorController.updateTelemetry();
  }

  @Override
  public void simulationPeriodic() {
    motorController.simIterate();
  }
}
