// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.DegreesPerSecond;
import static edu.wpi.first.units.Units.DegreesPerSecondPerSecond;
import static edu.wpi.first.units.Units.Grams;
import static edu.wpi.first.units.Units.Inches;
import static edu.wpi.first.units.Units.KilogramSquareMeters;
import static edu.wpi.first.units.Units.RPM;

import com.ctre.phoenix6.hardware.TalonFX;
import edu.wpi.first.math.Pair;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.MomentOfInertia;
import edu.wpi.first.wpilibj2.command.Command;
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

/**
 * This is the subsystem for delivering fuel by launching it from the robot.
 *
 * <p>It may also be referred to as <i>Blinky</i>.
 */
public class Launcher extends SubsystemBase {
  /** Creates a new Launcher. */
  private TalonFX FlywheelLead = new TalonFX(LauncherConstants.MOTOR_ID_LEAD);

  private TalonFX FlywheelFollow = new TalonFX(LauncherConstants.MOTOR_ID_FOLLOW);
  private SmartMotorControllerConfig smcConfig =
      new SmartMotorControllerConfig(this)
          .withControlMode(ControlMode.CLOSED_LOOP)
          // Feedback Constants (PID Constants)
          .withClosedLoopController(
              LauncherConstants.REAL_kP,
              LauncherConstants.REAL_kI,
              LauncherConstants.REAL_kD,
              DegreesPerSecond.of(LauncherConstants.MAX_VELOCITY_DPS),
              DegreesPerSecondPerSecond.of(LauncherConstants.MAX_ACCEL_DPS2))
          .withSimClosedLoopController(
              LauncherConstants.SIM_kP,
              LauncherConstants.SIM_kI,
              LauncherConstants.SIM_kD,
              DegreesPerSecond.of(LauncherConstants.MAX_VELOCITY_DPS),
              DegreesPerSecondPerSecond.of(LauncherConstants.MAX_ACCEL_DPS2))
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
          .withFollowers(Pair.of(FlywheelFollow, LauncherConstants.FOLLOWER_INVERTED))
          .withStatorCurrentLimit(Amps.of(LauncherConstants.STATOR_CURRENT_LIMIT_AMPS));

  private SmartMotorController shooterMotors =
      new TalonFXWrapper(
          FlywheelLead, DCMotor.getKrakenX60Foc(LauncherConstants.MOTOR_COUNT), smcConfig);

  private final FlyWheelConfig LauncherConfig =
      new FlyWheelConfig(shooterMotors)
          .withDiameter(Inches.of(LauncherConstants.DIAMETER_INCH))
          .withMass(Grams.of(LauncherConstants.MASS_GRAMS))
          .withMOI(MomentOfInertia.ofBaseUnits(LauncherConstants.MOI_KG_M2, KilogramSquareMeters))
          .withTelemetry(LauncherConstants.MECH_TELEMETRY_NAME, TelemetryVerbosity.HIGH)
          .withSoftLimit(
              RPM.of(-LauncherConstants.SOFT_LIMIT_RPM),
              RPM.of(LauncherConstants.SOFT_LIMIT_RPM));

  private FlyWheel Launcher = new FlyWheel(LauncherConfig);

  /**
   * Gets the current velocity of the shooter.
   *
   * @return Shooter velocity.
   */
  public AngularVelocity getVelocity() {
    return Launcher.getSpeed();
  }

  /**
   * Set the shooter velocity.
   *
   * @param speed Speed to set.
   * @return {@link edu.wpi.first.wpilibj2.command.RunCommand}
   */
  public Command setVelocity(AngularVelocity speed) {
    return Launcher.setSpeed(speed);
  }

  /**
   * Set the dutycycle of the shooter.
   *
   * @param dutyCycle DutyCycle to set.
   * @return {@link edu.wpi.first.wpilibj2.command.RunCommand}
   */
  public Command set(double dutyCycle) {
    return Launcher.set(dutyCycle);
  }

  public Launcher() {}

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    Launcher.updateTelemetry();
  }

  @Override
  public void simulationPeriodic() {
    // This method will be called once per scheduler run during simulation
    Launcher.simIterate();
  }
}
