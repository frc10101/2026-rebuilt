// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.units.measure.Angle;
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
import yams.motorcontrollers.local.SparkWrapper;

// Pacman
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
          // GearBox.fromReductionStages(3, 4) is the same as "3:1" then "4:1" = 12:1 reduction
          .withGearing(new MechanismGearing(GearBox.fromReductionStages(3, 4)))
          // Motor properties from tutorial to prevent over currenting
          .withMotorInverted(false)
          .withIdleMode(MotorMode.BRAKE)
          .withStatorCurrentLimit(Constants.IntakeConstants.Pivot.currentLimit)
          .withClosedLoopRampRate(Constants.IntakeConstants.Pivot.closedLoopRampRate)
          .withOpenLoopRampRate(Constants.IntakeConstants.Pivot.openLoopRampRate);

  private SmartMotorControllerConfig SmartRollerMotorConfig =
      new SmartMotorControllerConfig(this)
          .withControlMode(ControlMode.OPEN_LOOP)
          .withTelemetry("IntakeRollerMotor", TelemetryVerbosity.LOW)
          .withGearing(new MechanismGearing(GearBox.fromReductionStages(1)))
          .withMotorInverted(false)
          .withIdleMode(MotorMode.COAST)
          .withStatorCurrentLimit(Constants.IntakeConstants.Roller.currentLimit)
          .withClosedLoopRampRate(Constants.IntakeConstants.Roller.closedLoopRampRate)
          .withOpenLoopRampRate(Constants.IntakeConstants.Roller.openLoopRampRate);

  private SparkMax pivot = new SparkMax(Constants.IDs.intakePivotMotor, MotorType.kBrushless);
  private SparkMax roller = new SparkMax(Constants.IDs.intakeRollerMotor, MotorType.kBrushless);

  // create the smartMotorController
  private SmartMotorController pivotController =
      new SparkWrapper(pivot, DCMotor.getKrakenX44(1), SmartPivotMotorConfig);

  private SmartMotorController rollerController =
      new SparkWrapper(roller, DCMotor.getNEO(1), SmartRollerMotorConfig);

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

  /** Run sysId on the {&link intakePivot} */
  public Command runSysId() {
    return intakePivot.sysId(
        Constants.IntakeConstants.Pivot.maxVoltage,
        Constants.IntakeConstants.Pivot.stepVoltage,
        Constants.IntakeConstants.Pivot.sysIdDuration);
  }

  // Roller Commands

  /**
   * Set the roller to a specific duty cycle
   *
   * @param dutyCycle Duty cycle to set (-1 to 1)
   */
  public Command setRoller(double dutyCycle) {
    return runEnd(
        () -> rollerController.setDutyCycle(dutyCycle), () -> rollerController.setDutyCycle(0));
  }

  /** Run the roller to intake game pieces */
  public Command intake() {
    return setRoller(Constants.IntakeConstants.Roller.intakeSpeed);
  }

  /** Run the roller to outtake game pieces */
  public Command outtake() {
    return setRoller(Constants.IntakeConstants.Roller.outtakeSpeed);
  }

  /** Stop the roller */
  public Command stopRoller() {
    return runOnce(() -> rollerController.setDutyCycle(0));
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
