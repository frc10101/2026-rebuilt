// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.indexer;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Volts;

import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.BeltDexterConstants;
import frc.robot.Constants.SparkMaxCanIDs;
import yams.motorcontrollers.SmartMotorController;
import yams.motorcontrollers.SmartMotorControllerConfig;
import yams.motorcontrollers.SmartMotorControllerConfig.ControlMode;
import yams.motorcontrollers.SmartMotorControllerConfig.MotorMode;
import yams.motorcontrollers.SmartMotorControllerConfig.TelemetryVerbosity;
import yams.motorcontrollers.local.SparkWrapper;

/** Add your docs here. */
public class IndexerIOSparkMax implements IndexerIO {
  private SmartMotorController Indexer;
  private SparkMax IndexerMotor;

  public IndexerIOSparkMax(SubsystemBase subsystem) {
    SmartMotorControllerConfig MotorConfig =
        new SmartMotorControllerConfig(subsystem)
            .withControlMode(ControlMode.CLOSED_LOOP)
            .withClosedLoopController(
                BeltDexterConstants.Real.kp,
                BeltDexterConstants.Real.ki,
                BeltDexterConstants.Real.kd)
            .withSimClosedLoopController(
                BeltDexterConstants.Sim.kp, BeltDexterConstants.Sim.ki, BeltDexterConstants.Sim.kd)
            .withTrapezoidalProfile(
                BeltDexterConstants.Real.maxVelocity, BeltDexterConstants.Real.maxAcceleration)
            .withFeedforward(
                new SimpleMotorFeedforward(
                    BeltDexterConstants.Real.ks,
                    BeltDexterConstants.Real.kv,
                    BeltDexterConstants.Real.ka))
            .withTelemetry("IndexerMotor", TelemetryVerbosity.HIGH)
            .withGearing(BeltDexterConstants.gearRatio)
            .withMotorInverted(false)
            .withIdleMode(MotorMode.BRAKE)
            .withSupplyCurrentLimit(BeltDexterConstants.currentLimit);

    IndexerMotor = new SparkMax(SparkMaxCanIDs.BeltDexterMotor, MotorType.kBrushless);

    Indexer = new SparkWrapper(IndexerMotor, DCMotor.getNEO(1), MotorConfig);
  }

  @Override
  public void updateInputs(IndexerIOInputs inputs) {
    inputs.connected = IndexerMotor.getFirmwareVersion() != 0;
    inputs.velocity = Indexer.getMechanismVelocity();
    inputs.appliedVoltage = Indexer.getVoltage();
    inputs.supplyCurrent = Indexer.getSupplyCurrent().map(c -> c).orElse(Amps.of(0.0));
    inputs.statorCurrent = Indexer.getStatorCurrent();
    inputs.temperature = Indexer.getTemperature();
  }

  @Override
  public void setTargetVelocity(AngularVelocity velocity) {
    Indexer.setVelocity(velocity);
  }

  @Override
  public void setVoltage(Voltage volts) {
    Indexer.setVoltage(volts);
  }

  @Override
  public void stop() {
    Indexer.setVoltage(Volts.zero());
  }
}
