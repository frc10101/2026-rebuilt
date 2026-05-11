// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.intake.IntakeRoller;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.RPM;
import static edu.wpi.first.units.Units.Volts;

import com.ctre.phoenix6.hardware.TalonFX;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import yams.gearing.GearBox;
import yams.gearing.MechanismGearing;
import yams.motorcontrollers.SmartMotorController;
import yams.motorcontrollers.SmartMotorControllerConfig;
import yams.motorcontrollers.SmartMotorControllerConfig.ControlMode;
import yams.motorcontrollers.SmartMotorControllerConfig.MotorMode;
import yams.motorcontrollers.SmartMotorControllerConfig.TelemetryVerbosity;
import yams.motorcontrollers.remote.TalonFXWrapper;

/** Add your docs here. */
public class IntakeRollerIOTalonFX implements IntakeRollerIO {
  private SmartMotorController IntakeRoller;
  private TalonFX RollerMotor;

  public IntakeRollerIOTalonFX(SubsystemBase subsystem) {
    RollerMotor = new TalonFX(Constants.IntakeConstants.Roller.rollerMotorID);
    SmartMotorControllerConfig SmartRollerMotorConfig =
        new SmartMotorControllerConfig(subsystem)
            .withControlMode(ControlMode.OPEN_LOOP)
            .withTelemetry("IntakeRollerMotor", TelemetryVerbosity.LOW)
            .withGearing(
                new MechanismGearing(
                    GearBox.fromReductionStages(Constants.IntakeConstants.Roller.totalGear)))
            .withFeedforward(
                new SimpleMotorFeedforward(
                    Constants.IntakeConstants.Roller.kS,
                    Constants.IntakeConstants.Roller.kV,
                    Constants.IntakeConstants.Roller.kA))
            .withMotorInverted(false)
            .withIdleMode(MotorMode.BRAKE)
            .withSupplyCurrentLimit(Constants.IntakeConstants.Roller.currentLimit)
            .withClosedLoopRampRate(Constants.IntakeConstants.Roller.closedLoopRampRate)
            .withOpenLoopRampRate(Constants.IntakeConstants.Roller.openLoopRampRate);
    IntakeRoller =
        new TalonFXWrapper(RollerMotor, DCMotor.getKrakenX60Foc(1), SmartRollerMotorConfig);
  }

  @Override
  public void updateInputs(IntakeRollerIOInputs inputs) {
    inputs.connected = RollerMotor.isConnected();
    inputs.velocity = IntakeRoller.getMechanismVelocity();
    inputs.velocity = IntakeRoller.getMechanismVelocity();
    inputs.appliedVoltage = IntakeRoller.getVoltage();
    inputs.supplyCurrent = IntakeRoller.getSupplyCurrent().map(c -> c).orElse(Amps.of(0.0));
    inputs.statorCurrent = IntakeRoller.getStatorCurrent();
    inputs.temperature = IntakeRoller.getTemperature();
    inputs.targetVelocity =
        IntakeRoller.getMechanismSetpointVelocity().map(a -> a).orElse(RPM.of(0.0));
  }

  @Override
  public void setTargetVelocity(AngularVelocity velocity) {
    // Use SmartMotorController's setVelocity method
    IntakeRoller.setVelocity(velocity);
  }

  @Override
  public void stop() {
    IntakeRoller.setVoltage(Volts.of(0));
  }
}
