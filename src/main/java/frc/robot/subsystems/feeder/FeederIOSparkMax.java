// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.feeder;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.RPM;
import static edu.wpi.first.units.Units.Volts;

import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.ColumnConstants;
import frc.robot.Constants.SparkMaxCanIDs;
import yams.motorcontrollers.SmartMotorController;
import yams.motorcontrollers.SmartMotorControllerConfig;
import yams.motorcontrollers.SmartMotorControllerConfig.ControlMode;
import yams.motorcontrollers.SmartMotorControllerConfig.MotorMode;
import yams.motorcontrollers.SmartMotorControllerConfig.TelemetryVerbosity;
import yams.motorcontrollers.local.SparkWrapper;

/** Add your docs here. */
public class FeederIOSparkMax implements FeederIO {
  private SmartMotorController Feeder;
  private SparkMax FeederMotor;

  public FeederIOSparkMax(SubsystemBase subsystem) {
    SmartMotorControllerConfig MotorConfig =
        new SmartMotorControllerConfig(subsystem)
            .withControlMode(ControlMode.CLOSED_LOOP)
            .withClosedLoopController(
                ColumnConstants.Real.kp, ColumnConstants.Real.ki, ColumnConstants.Real.kd)
            .withSimClosedLoopController(
                ColumnConstants.Sim.kp, ColumnConstants.Sim.ki, ColumnConstants.Sim.kd)
            .withTrapezoidalProfile(
                ColumnConstants.Real.maxVelocity, ColumnConstants.Real.maxAcceleration)
            .withTelemetry("FeederMotor", TelemetryVerbosity.HIGH)
            .withGearing(ColumnConstants.gearRatio)
            .withMotorInverted(false)
            .withIdleMode(MotorMode.BRAKE)
            .withSupplyCurrentLimit(ColumnConstants.currentLimit);

    FeederMotor = new SparkMax(SparkMaxCanIDs.ColumnMotor, MotorType.kBrushless);

    Feeder = new SparkWrapper(FeederMotor, DCMotor.getNEO(1), MotorConfig);
  }

  @Override
  public void updateInputs(FeederIOInputs inputs) {
    inputs.connected = FeederMotor.getFirmwareVersion() != 0;
    inputs.velocity = Feeder.getMechanismVelocity();
    inputs.appliedVoltage = Feeder.getVoltage();
    inputs.supplyCurrent = Feeder.getSupplyCurrent().map(c -> c).orElse(Amps.of(0.0));
    inputs.statorCurrent = Feeder.getStatorCurrent();
    inputs.temperature = Feeder.getTemperature();
    inputs.targetVelocity = Feeder.getMechanismSetpointVelocity().map(a -> a).orElse(RPM.of(0.0));
  }

  @Override
  public void setTargetVelocity(AngularVelocity velocity) {
    Feeder.setVelocity(velocity);
  }

  @Override
  public void setVoltage(Voltage volts) {
    Feeder.setVoltage(volts);
  }

  @Override
  public void stop() {
    Feeder.setVoltage(Volts.zero());
  }
}
