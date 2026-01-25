// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkLowLevel.MotorType;

import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
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

    private SmartMotorControllerConfig pivotMotorConfig =
        new SmartMotorControllerConfig(this)
            .withControlMode(ControlMode.CLOSED_LOOP)
            .withClosedLoopController(
                Constants.IntakeConstants.Real.kp,
                Constants.IntakeConstants.Real.ki,
                Constants.IntakeConstants.Real.kd,
                Constants.IntakeConstants.Real.maxVelocity,
                Constants.IntakeConstants.Real.maxAcceleration)
            .withSimClosedLoopController(
                Constants.IntakeConstants.Sim.kp,
                Constants.IntakeConstants.Sim.ki,
                Constants.IntakeConstants.Sim.kd,
                Constants.IntakeConstants.Sim.maxVelocity,
                Constants.IntakeConstants.Sim.maxAcceleration)
            .withFeedforward(
                new ArmFeedforward(
                    Constants.IntakeConstants.Real.ks, Constants.IntakeConstants.Real.kg, Constants.IntakeConstants.Real.kv))
            .withSimFeedforward(
                new ArmFeedforward(
                    Constants.IntakeConstants.Sim.ks, Constants.IntakeConstants.Sim.kg, Constants.IntakeConstants.Sim.kv))
            .withTelemetry("IntakePivotMotor", TelemetryVerbosity.HIGH)
            // Gearing from motor rotor to final shaft
            // Uses the pre-calculated totalGear from Constants (product of all gear stages)
            // If you need to gear up instead, change totalGear calculation in Constants
            .withGearing(Constants.IntakeConstants.totalGear)
            // Motor properties from tutorial to prevent over currenting
            .withMotorInverted(false)
            .withIdleMode(MotorMode.BRAKE)
            .withStatorCurrentLimit(Constants.IntakeConstants.currentLimit)
            .withClosedLoopRampRate(Constants.IntakeConstants.closedLoopRampRate)
            .withOpenLoopRampRate(Constants.IntakeConstants.openLoopRampRate);

    private SparkMax pivot = new SparkMax(Constants.IDs.intakePivotMotor, MotorType.kBrushless);

    //create the smartMotorController
    private SmartMotorController pivotController = new SparkWrapper(pivot, DCMotor.getKrakenX44(1), pivotMotorConfig);

    private ArmConfig pivotConfig = new ArmConfig(pivotController)
    .withSoftLimits(Constants.IntakeConstants.softLimitOne, Constants.IntakeConstants.softLimitTwo)
    .withHardLimit(Constants.IntakeConstants.hardLimitOne, Constants.IntakeConstants.hardLimitTwo)
    .withStartingPosition(Constants.IntakeConstants.startingPostion)
    .withLength(Constants.IntakeConstants.armLength)
    .withMass(Constants.IntakeConstants.mass)
    .withTelemetry("IntakePivot", TelemetryVerbosity.HIGH);

    private Arm intakePivot = new Arm(pivotConfig);
    
    //Commands
    /**
     * Set the angle of arm
     * @param angle Angle to go to
     */
    public Command setAngle(Angle angle) {
        return intakePivot.setAngle(angle);
    }

    /**
     * Move the arm up and down
     * @param dutyCycle Duty cycle to set (-1 to 1)
     */
    public Command set(double dutyCycle) {
        return intakePivot.set(dutyCycle);
    }

    /**
     * Run sysId on the {&link intakePivot}
     */
    public Command runSysId() {
        return intakePivot.sysId(Constants.IntakeConstants.maxVoltage, Constants.IntakeConstants.stepVoltage, Constants.IntakeConstants.sysIdDuration);
    }

  /** Creates a new Intake. */
  public Intake() {}

  @Override
  public void periodic() {
    intakePivot.updateTelemetry();
  }

  @Override
  public void simulationPeriodic() {
    intakePivot.simIterate();
  }
}
