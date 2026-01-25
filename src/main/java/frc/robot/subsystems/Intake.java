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
                Constants.Intake.Real.kp,
                Constants.Intake.Real.ki,
                Constants.Intake.Real.kd,
                Constants.Intake.Real.maxVelocity,
                Constants.Intake.Real.maxAcceleration)
            .withSimClosedLoopController(
                Constants.Intake.Sim.kp,
                Constants.Intake.Sim.ki,
                Constants.Intake.Sim.kd,
                Constants.Intake.Sim.maxVelocity,
                Constants.Intake.Sim.maxAcceleration)
            .withFeedforward(
                new ArmFeedforward(
                    Constants.Intake.Real.ks, Constants.Intake.Real.kg, Constants.Intake.Real.kv))
            .withSimFeedforward(
                new ArmFeedforward(
                    Constants.Intake.Sim.ks, Constants.Intake.Sim.kg, Constants.Intake.Sim.kv))
            .withTelemetry("IntakePivotMotor", TelemetryVerbosity.HIGH)
            // Gearing from motor rotor to final shaft
            // Uses the pre-calculated totalGear from Constants (product of all gear stages)
            // If you need to gear up instead, change totalGear calculation in Constants
            .withGearing(Constants.Intake.totalGear)
            // Motor properties from tutorial to prevent over currenting
            .withMotorInverted(false)
            .withIdleMode(MotorMode.BRAKE)
            .withStatorCurrentLimit(Constants.Intake.currentLimit)
            .withClosedLoopRampRate(Constants.Intake.closedLoopRampRate)
            .withOpenLoopRampRate(Constants.Intake.openLoopRampRate);

    private SparkMax pivot = new SparkMax(Constants.IDs.intakePivotMotor, MotorType.kBrushless);

    //create the smartMotorController
    private SmartMotorController pivotController = new SparkWrapper(pivot, DCMotor.getKrakenX44(1), pivotMotorConfig);

    private ArmConfig pivotConfig = new ArmConfig(pivotController)
    .withSoftLimits(Constants.Intake.softLimitOne, Constants.Intake.softLimitTwo)
    .withHardLimit(Constants.Intake.hardLimitOne, Constants.Intake.hardLimitTwo)
    .withStartingPosition(Constants.Intake.startingPostion)
    .withLength(Constants.Intake.armLength)
    .withMass(Constants.Intake.mass)
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
        return intakePivot.sysId(Constants.Intake.maxVoltage, Constants.Intake.stepVoltage, Constants.Intake.sysIdDuration);
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
