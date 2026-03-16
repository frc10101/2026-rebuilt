// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.climb;

import static edu.wpi.first.units.Units.Inches;
import static edu.wpi.first.units.Units.Rotations;

import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import edu.wpi.first.math.controller.ElevatorFeedforward;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.ClimbConstants;
import yams.mechanisms.config.ElevatorConfig;
import yams.mechanisms.positional.Elevator;
import yams.motorcontrollers.SmartMotorController;
import yams.motorcontrollers.SmartMotorControllerConfig;
import yams.motorcontrollers.SmartMotorControllerConfig.ControlMode;
import yams.motorcontrollers.SmartMotorControllerConfig.MotorMode;
import yams.motorcontrollers.SmartMotorControllerConfig.TelemetryVerbosity;
import yams.motorcontrollers.local.SparkWrapper;

/**
 * This is the subsystem for completing climbs.
 *
 * <p>It may also be referred to as <i>Level Up</i>.
 */
public class Climb extends SubsystemBase {
  private Distance distance = ClimbConstants.RestDistance;
  private boolean working = false;
  private final String name;

  private final SmartMotorControllerConfig motorConfig;
  private final SparkMax motor;
  private final SmartMotorController motorController;
  private final ElevatorConfig elevatorConfig;

  // Elevator Mechanism
  private final Elevator elevator;

  /** Creates a new Climb. */
  public Climb(String telemetryName, int canID) {
    name = telemetryName;

    motorConfig =
        new SmartMotorControllerConfig(this)
            .withControlMode(ControlMode.CLOSED_LOOP)
            .withMechanismCircumference(ClimbConstants.MechanismCircumference)
            .withClosedLoopController(
                ClimbConstants.Real.kp,
                ClimbConstants.Real.ki,
                ClimbConstants.Real.kd,
                ClimbConstants.Real.maxVelocity,
                ClimbConstants.Real.maxAcceleration)
            .withSimClosedLoopController(
                ClimbConstants.Sim.kp,
                ClimbConstants.Sim.ki,
                ClimbConstants.Sim.kd,
                ClimbConstants.Sim.maxVelocity,
                ClimbConstants.Sim.maxAcceleration)
            // Feedforward Constants
            .withFeedforward(
                new ElevatorFeedforward(
                    ClimbConstants.Real.ks, ClimbConstants.Real.kg, ClimbConstants.Real.kv))
            .withSimFeedforward(
                new ElevatorFeedforward(
                    ClimbConstants.Sim.ks, ClimbConstants.Sim.kg, ClimbConstants.Sim.kv))
            // Telemetry name and verbosity level
            .withTelemetry(telemetryName + " telemetry", TelemetryVerbosity.HIGH)
            .withGearing(ClimbConstants.gearRatio)
            .withMotorInverted(false)
            .withIdleMode(MotorMode.BRAKE)
            .withStatorCurrentLimit(ClimbConstants.currentLimit)
            .withClosedLoopRampRate(ClimbConstants.ClosedLoppRampRate)
            .withOpenLoopRampRate(ClimbConstants.OpenLoppRampRate);

    motor = new SparkMax(canID, MotorType.kBrushless);

    motorController = new SparkWrapper(motor, DCMotor.getNEO(1), motorConfig);

    elevatorConfig =
        new ElevatorConfig(motorController)
            .withStartingHeight(ClimbConstants.RestDistance)
            .withHardLimits(ClimbConstants.hardMinimum, ClimbConstants.hardMaximum)
            .withTelemetry(telemetryName + " elevator", TelemetryVerbosity.HIGH)
            .withMass(ClimbConstants.Weight);

    elevator = new Elevator(elevatorConfig);
  }

  /** Brings climb arm(s) to height specified for prepping hang */
  public Command GoToPreHangHeight() {
    working = true;
    distance = ClimbConstants.PreHangExtension;
    return runOnce(() -> elevator.setHeight(ClimbConstants.PreHangExtension));
  }

  /** Brings climb arm(s) to height specified for clamping */
  public Command GoToHangHeight() {
    working = true;
    distance = ClimbConstants.HangDistance;
    return runOnce(() -> elevator.setHeight(ClimbConstants.HangDistance));
  }

  /** Brings climb arm(s) to height specified for releasing from hang */
  public Command GoToReleaseHeight() {
    working = true;
    distance = ClimbConstants.ReleaseDistance;
    return runOnce(() -> elevator.setHeight(ClimbConstants.ReleaseDistance));
  }

  /** Brings climb arm(s) to height specified for resting */
  public Command GoToRestHeight() {
    working = true;
    distance = ClimbConstants.RestDistance;
    return runOnce(() -> elevator.setHeight(ClimbConstants.RestDistance));
  }

  public Command GoToHeight(Distance height) {
    working = true;
    distance = height;
    return run(() -> elevator.setHeight(height));
  }

  /**
   * Get the current height of the climb arm
   *
   * @return The current height of the climb arm
   */
  public Distance getHeight() {
    return motorController.getMeasurementPosition();
  }

  public Command zeroClimb() {
    return runOnce(
        () -> {
          motorController.setEncoderPosition(Rotations.of(0));
        });
  }

  public void close() {
    motor.close();
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    SmartDashboard.putNumber(" Climb Distance Setpoint", distance.in(Inches));
    SmartDashboard.putNumber(" Climb Distance Actual", getHeight().in(Inches));

    motorController.updateTelemetry();
  }

  @Override
  public void simulationPeriodic() {
    SmartDashboard.putBoolean("Is it working?", working);
    motorController.simIterate();
  }
}
