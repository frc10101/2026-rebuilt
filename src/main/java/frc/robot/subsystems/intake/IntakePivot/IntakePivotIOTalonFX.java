// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.intake.IntakePivot;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Volts;

import com.ctre.phoenix6.hardware.TalonFX;
import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import yams.gearing.GearBox;
import yams.gearing.MechanismGearing;
import yams.mechanisms.config.ArmConfig;
import yams.mechanisms.positional.Arm;
import yams.motorcontrollers.SmartMotorController;
import yams.motorcontrollers.SmartMotorControllerConfig;
import yams.motorcontrollers.SmartMotorControllerConfig.ControlMode;
import yams.motorcontrollers.SmartMotorControllerConfig.MotorMode;
import yams.motorcontrollers.SmartMotorControllerConfig.TelemetryVerbosity;
import yams.motorcontrollers.remote.TalonFXWrapper;

/** Add your docs here. */
public class IntakePivotIOTalonFX implements IntakePivotIO {
  private final Arm intakePivot;
  private final SmartMotorController pivotController;
  private final TalonFX PivotMotor;

  public IntakePivotIOTalonFX(SubsystemBase subsystem) {
    PivotMotor = new TalonFX(Constants.IntakeConstants.Pivot.intakePivotID);

    SmartMotorControllerConfig SmartPivotMotorConfig =
        new SmartMotorControllerConfig(subsystem)
            .withControlMode(ControlMode.CLOSED_LOOP)
            .withClosedLoopController(
                Constants.IntakeConstants.Pivot.Real.kp,
                Constants.IntakeConstants.Pivot.Real.ki,
                Constants.IntakeConstants.Pivot.Real.kd)
            .withSimClosedLoopController(
                Constants.IntakeConstants.Pivot.Sim.kp,
                Constants.IntakeConstants.Pivot.Sim.ki,
                Constants.IntakeConstants.Pivot.Sim.kd)
            .withTrapezoidalProfile(
                Constants.IntakeConstants.Pivot.Real.maxVelocity,
                Constants.IntakeConstants.Pivot.Real.maxAcceleration)
            .withFeedforward(
                new ArmFeedforward(
                    Constants.IntakeConstants.Pivot.Real.ks,
                    Constants.IntakeConstants.Pivot.Real.kg,
                    Constants.IntakeConstants.Pivot.Real.kv))
            .withSimFeedforward(
                new ArmFeedforward(
                    Constants.IntakeConstants.Pivot.Sim.ks,
                    Constants.IntakeConstants.Pivot.Sim.kg,
                    Constants.IntakeConstants.Pivot.Sim.kv))
            .withTelemetry("IntakePivotMotor", TelemetryVerbosity.HIGH)
            // Gearing from motor rotor to final shaft
            .withGearing(
                new MechanismGearing(
                    GearBox.fromReductionStages(Constants.IntakeConstants.Pivot.totalGear)))
            // Motor properties from tutorial to prevent over currenting
            .withMotorInverted(false)
            .withIdleMode(MotorMode.COAST)
            .withSupplyCurrentLimit(Constants.IntakeConstants.Pivot.currentLimit)
            .withClosedLoopRampRate(Constants.IntakeConstants.Pivot.closedLoopRampRate)
            .withOpenLoopRampRate(Constants.IntakeConstants.Pivot.openLoopRampRate);

    pivotController =
        new TalonFXWrapper(PivotMotor, DCMotor.getKrakenX60Foc(1), SmartPivotMotorConfig);
    ArmConfig pivotConfig =
        new ArmConfig(pivotController)
            .withSoftLimits(
                Constants.IntakeConstants.Pivot.softLimitOne,
                Constants.IntakeConstants.Pivot.softLimitTwo)
            .withHardLimit(
                Constants.IntakeConstants.Pivot.hardLimitOne,
                Constants.IntakeConstants.Pivot.hardLimitTwo)
            .withStartingPosition(Constants.IntakeConstants.Pivot.startingPosition)
            .withLength(Constants.IntakeConstants.Pivot.armLength)
            .withMass(Constants.IntakeConstants.Pivot.mass)
            .withTelemetry("IntakePivot", TelemetryVerbosity.HIGH);

    this.intakePivot = new Arm(pivotConfig);
  }

  @Override
  public void updateInputs(IntakePivotIOInputs inputs) {
    inputs.connected = PivotMotor.isConnected();
    inputs.angle = pivotController.getMechanismPosition();
    inputs.velocity = pivotController.getMechanismVelocity();
    inputs.appliedVoltage = pivotController.getVoltage();
    inputs.supplyCurrent = pivotController.getSupplyCurrent().map(c -> c).orElse(Amps.of(0.0));
    inputs.statorCurrent = pivotController.getStatorCurrent();
    inputs.temperature = pivotController.getTemperature();
    inputs.targetAngle =
        pivotController.getMechanismPositionSetpoint().map(a -> a).orElse(Degrees.of(0.0));
  }

  @Override
  public void setTargetAngle(Angle angle) {
    // Use SmartMotorController's setPosition method
    pivotController.setPosition(angle);
  }

  @Override
  public void stop() {
    pivotController.setVoltage(Volts.of(0));
  }

  /** Access the IntakePivot mechanism for command helpers like run() and runTo() */
  public Arm getIntakePivot() {
    return intakePivot;
  }
}
