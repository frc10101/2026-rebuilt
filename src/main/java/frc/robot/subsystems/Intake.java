// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.ctre.phoenix6.hardware.TalonFX;
import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.wpilibj2.command.Command;
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

/**
 * This is the subsystem for moving fuel from outside the robot to the {@link Indexer}.
 *
 * <p>It may also be referred to as <i>Pacman</i>.
 */
public class Intake extends SubsystemBase {

  private SmartMotorControllerConfig SmartPivotMotorConfig =
      new SmartMotorControllerConfig(this)
          .withControlMode(ControlMode.CLOSED_LOOP)
          .withClosedLoopController(
              Constants.IntakeConstants.Pivot.Real.kp,
              Constants.IntakeConstants.Pivot.Real.ki,
              Constants.IntakeConstants.Pivot.Real.kd,
              Constants.IntakeConstants.Pivot.Real.maxVelocity,
              Constants.IntakeConstants.Pivot.Real.maxAcceleration)
          .withSimClosedLoopController(
              Constants.IntakeConstants.Pivot.Sim.kp,
              Constants.IntakeConstants.Pivot.Sim.ki,
              Constants.IntakeConstants.Pivot.Sim.kd,
              Constants.IntakeConstants.Pivot.Sim.maxVelocity,
              Constants.IntakeConstants.Pivot.Sim.maxAcceleration)
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
          .withStatorCurrentLimit(Constants.IntakeConstants.Pivot.currentLimit)
          .withClosedLoopRampRate(Constants.IntakeConstants.Pivot.closedLoopRampRate)
          .withOpenLoopRampRate(Constants.IntakeConstants.Pivot.openLoopRampRate);

  private SmartMotorControllerConfig SmartRollerMotorConfig =
      new SmartMotorControllerConfig(this)
          .withControlMode(ControlMode.OPEN_LOOP)
          .withTelemetry("IntakeRollerMotor", TelemetryVerbosity.LOW)
          .withGearing(
              new MechanismGearing(
                  GearBox.fromReductionStages(Constants.IntakeConstants.Roller.totalGear)))
          .withMotorInverted(false)
          .withIdleMode(MotorMode.BRAKE)
          .withStatorCurrentLimit(Constants.IntakeConstants.Roller.currentLimit)
          .withClosedLoopRampRate(Constants.IntakeConstants.Roller.closedLoopRampRate)
          .withOpenLoopRampRate(Constants.IntakeConstants.Roller.openLoopRampRate);

  private TalonFX pivot = new TalonFX(Constants.IntakeConstants.Pivot.intakePivotID);
  private TalonFX roller = new TalonFX(Constants.IntakeConstants.Roller.rollerMotorID);

  // create the smartMotorController
  private SmartMotorController pivotController =
      new TalonFXWrapper(pivot, DCMotor.getKrakenX60(1), SmartPivotMotorConfig);

  private SmartMotorController rollerController =
      new TalonFXWrapper(roller, DCMotor.getKrakenX60(1), SmartRollerMotorConfig);

  private ArmConfig pivotConfig =
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

  private Arm intakePivot = new Arm(pivotConfig);

  // Commands
  /**
   * Set the angle of arm
   *
   * @param angle Angle to go to
   */
  /** public Command setAngle(Angle angle) { return runOnce(() -> setAngle = angle); } */
  public Command setAngle(Angle angle) {
    return intakePivot.setAngle(angle);
  }

  /**
   * Move the arm up and down
   *
   * @param dutyCycle Duty cycle to set (-1 to 1)
   */
  public Command set(double dutyCycle) {
    return intakePivot.set(dutyCycle);
  }

  public Command goToIntakePosition() {
    return setAngle(Constants.IntakeConstants.Pivot.intakePosition).andThen(intakePivot.set(0.0));
  }

  // Roller Commands

  /**
   * Set the roller to a specific duty cycle
   *
   * @param dutyCycle Duty cycle to set (-1 to 1)
   */
  public Command setRollerSpeed(AngularVelocity vel) {
    return runEnd(() -> rollerController.setVelocity(vel), () -> rollerController.setDutyCycle(0));
  }

  /** Run the roller to intake game pieces */
  public Command intake() {
    return setRollerSpeed(Constants.IntakeConstants.Roller.intakeSpeed);
  }

  /** Run the roller to outtake game pieces */
  public Command outtake() {
    return setRollerSpeed(Constants.IntakeConstants.Roller.outtakeSpeed);
  }

  /** Stop the roller */
  public Command stopRoller() {
    return runOnce(() -> rollerController.setDutyCycle(0));
  }

  /**
   * Get the current pivot angle
   *
   * @return The current angle of the pivot
   */
  public Angle getPivotAngle() {
    return intakePivot.getAngle();
  }

  /**
   * Get the current roller velocity
   *
   * @return The current velocity of the roller
   */
  public AngularVelocity getRollerVelocity() {
    return rollerController.getMechanismVelocity();
  }

  public void close() {
    pivot.close();
    roller.close();
  }

  /** Creates a new Intake. */
  public Intake() {}

  @Override
  public void periodic() {
    intakePivot.updateTelemetry();
    rollerController.updateTelemetry();
  }

  @Override
  public void simulationPeriodic() {
    intakePivot.simIterate();
    rollerController.simIterate();
  }
}
