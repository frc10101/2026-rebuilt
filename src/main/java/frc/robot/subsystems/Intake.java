// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import yams.motorcontrollers.SmartMotorControllerConfig;
import yams.motorcontrollers.SmartMotorControllerConfig.ControlMode;
import yams.motorcontrollers.SmartMotorControllerConfig.MotorMode;
import yams.motorcontrollers.SmartMotorControllerConfig.TelemetryVerbosity;

// Pacman
public class Intake extends SubsystemBase {

  private SmartMotorControllerConfig smcConfig =
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
          .withTelemetry("Intake", TelemetryVerbosity.HIGH)
          // Gearing from motor rotor to final shaft
          // Uses the pre-calculated totalGear from Constants (product of all gear stages)
          // If you need to gear up instead, change totalGear calculation in Constants
          .withGearing(Constants.Intake.totalGear)
          //Motor properties from tutorial to prevent over currenting
          .withMotorInverted(false)
          .withIdleMode(MotorMode.BRAKE)
          .withStatorCurrentLimit(Constants.Intake.currentLimit)
          .withClosedLoopRampRate(Constants.Intake.closedLoopRampRate)
          .withOpenLoopRampRate(Constants.Intake.openLoopRampRate);


  /** Creates a new Intake. */
  public Intake() {}

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }
}
