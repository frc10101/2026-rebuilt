// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.launcher;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Grams;
import static edu.wpi.first.units.Units.Inches;
import static edu.wpi.first.units.Units.KilogramSquareMeters;
import static edu.wpi.first.units.Units.RPM;
import static edu.wpi.first.units.Units.Volts;

import com.ctre.phoenix6.hardware.TalonFX;
import edu.wpi.first.math.Pair;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.MomentOfInertia;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.LauncherConstants;
import yams.gearing.MechanismGearing;
import yams.mechanisms.config.FlyWheelConfig;
import yams.mechanisms.velocity.FlyWheel;
import yams.motorcontrollers.SmartMotorController;
import yams.motorcontrollers.SmartMotorControllerConfig;
import yams.motorcontrollers.SmartMotorControllerConfig.ControlMode;
import yams.motorcontrollers.SmartMotorControllerConfig.MotorMode;
import yams.motorcontrollers.SmartMotorControllerConfig.TelemetryVerbosity;
import yams.motorcontrollers.remote.TalonFXWrapper;

/** Add your docs here. */
public class LauncherIOTalonFX implements LauncherIO {
  private FlyWheel Launcher;
  private SmartMotorController shooterMotors;

  private TalonFX FlywheelLead;
  private TalonFX FlywheelFollow0;
  private TalonFX FlywheelFollow1;

  public LauncherIOTalonFX(SubsystemBase subsystem, int canId) {
    FlywheelLead = new TalonFX(LauncherConstants.MOTOR_ID_LEAD);
    FlywheelFollow0 = new TalonFX(LauncherConstants.MOTOR_ID_FOLLOW0);
    FlywheelFollow1 = new TalonFX(LauncherConstants.MOTOR_ID_FOLLOW1);

    // Step 1: Create SmartMotorControllerConfig
    SmartMotorControllerConfig smcConfig =
        new SmartMotorControllerConfig(subsystem)
            .withControlMode(ControlMode.CLOSED_LOOP)
            // Feedback Constants (PID Constants)
            .withClosedLoopController(
                LauncherConstants.REAL_kP, LauncherConstants.REAL_kI, LauncherConstants.REAL_kD)
            .withSimClosedLoopController(
                LauncherConstants.SIM_kP, LauncherConstants.SIM_kI, LauncherConstants.SIM_kD)
            // Feedforward Constants
            .withFeedforward(
                new SimpleMotorFeedforward(
                    LauncherConstants.FFW_kS, LauncherConstants.FFW_kV, LauncherConstants.FFW_kA))
            .withSimFeedforward(
                new SimpleMotorFeedforward(
                    LauncherConstants.FFW_kS, LauncherConstants.FFW_kV, LauncherConstants.FFW_kA))
            // Telemetry name and verbosity level
            .withTelemetry(LauncherConstants.MOTOR_TELEMETRY_NAME, TelemetryVerbosity.HIGH)
            .withGearing(new MechanismGearing(LauncherConstants.GEARING))
            // Motor properties to prevent over currenting.
            .withMotorInverted(LauncherConstants.MOTOR_INVERTED)
            .withIdleMode(MotorMode.COAST)
            .withFollowers(
                Pair.of(FlywheelFollow0, LauncherConstants.FOLLOWER0_INVERTED),
                Pair.of(FlywheelFollow1, LauncherConstants.FOLLOWER1_INVERTED))
            .withSupplyCurrentLimit(Amps.of(LauncherConstants.STATOR_CURRENT_LIMIT_AMPS));

    // Step 2: Create SmartMotorController (TalonFXWrapper)
    shooterMotors =
        new TalonFXWrapper(
            FlywheelLead, DCMotor.getKrakenX60Foc(LauncherConstants.MOTOR_COUNT), smcConfig);

    // Step 3: Create FlyWheelConfig with the SmartMotorController
    FlyWheelConfig LauncherConfig =
        new FlyWheelConfig(shooterMotors)
            .withDiameter(Inches.of(LauncherConstants.DIAMETER_INCH))
            .withMass(Grams.of(LauncherConstants.MASS_GRAMS))
            .withMOI(MomentOfInertia.ofBaseUnits(LauncherConstants.MOI_KG_M2, KilogramSquareMeters))
            .withTelemetry(LauncherConstants.MECH_TELEMETRY_NAME, TelemetryVerbosity.HIGH)
            .withSoftLimit(
                RPM.of(-LauncherConstants.SOFT_LIMIT_RPM),
                RPM.of(LauncherConstants.SOFT_LIMIT_RPM));

    // Step 4: Create FlyWheel mechanism - handles simulation automatically!
    Launcher = new FlyWheel(LauncherConfig);
  }

  @Override
  public void updateInputs(LauncherIOInputs inputs) {
    // Pull telemetry data from the underlying SmartMotorController
    inputs.connected0 = FlywheelLead.isConnected();
    inputs.connected1 = FlywheelFollow0.isConnected();
    inputs.connected2 = FlywheelFollow1.isConnected();
    inputs.velocity = shooterMotors.getMechanismVelocity();
    inputs.appliedVoltage = shooterMotors.getVoltage();
    inputs.supplyCurrentMotor0 = FlywheelLead.getSupplyCurrent().getValue();
    inputs.supplyCurrentMotor1 = FlywheelFollow0.getSupplyCurrent().getValue();
    inputs.supplyCurrentMotor2 = FlywheelFollow1.getSupplyCurrent().getValue();
    inputs.statorCurrent = shooterMotors.getStatorCurrent();
    inputs.temperature0 = FlywheelLead.getDeviceTemp().getValue();
    inputs.temperature1 = FlywheelFollow0.getDeviceTemp().getValue();
    inputs.temperature2 = FlywheelFollow1.getDeviceTemp().getValue();
    inputs.targetVelocity =
        shooterMotors.getMechanismSetpointVelocity().map(v -> v).orElse(RPM.of(0.0));
  }

  @Override
  public void setTargetVelocity(AngularVelocity velocity) {
    // Use SmartMotorController's setVelocity method
    shooterMotors.setVelocity(velocity);
  }

  @Override
  public void stop() {
    shooterMotors.setVoltage(Volts.of(0));
  }

  /** Access the FlyWheel mechanism for command helpers like run() and runTo() */
  public FlyWheel getFlyWheel() {
    return Launcher;
  }
}
