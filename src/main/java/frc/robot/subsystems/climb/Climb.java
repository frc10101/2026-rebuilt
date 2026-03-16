// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.climb;

import static edu.wpi.first.units.Units.Inches;
import static edu.wpi.first.units.Units.Rotations;
import static edu.wpi.first.units.Units.RotationsPerSecond;
import static edu.wpi.first.units.Units.Seconds;
import static edu.wpi.first.units.Units.Volts;

import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import edu.wpi.first.math.controller.ElevatorFeedforward;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.units.measure.MutAngle;
import edu.wpi.first.units.measure.MutAngularVelocity;
import edu.wpi.first.units.measure.MutVoltage;
import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
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
  private boolean isWorking = false;
  private final String name;

  private final MutVoltage m_appliedVoltage = new MutVoltage(0, 0, Volts);
  private final MutAngle m_position = new MutAngle(0, 0, Rotations);
  private final MutAngularVelocity m_velocity = new MutAngularVelocity(0, 0, RotationsPerSecond);

  private final SmartMotorControllerConfig motorConfig;
  private final SparkMax motor;
  private final SmartMotorController motorController;
  private final ElevatorConfig elevatorConfig;

  // Elevator Mechanism
  private final Elevator elevator;

  private final SysIdRoutine sysIdRoutine;

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

     // Create the SysIdRoutine
  sysIdRoutine =
      new SysIdRoutine(
          // Config: ramp rate, step voltage, timeout
          new SysIdRoutine.Config(
              Volts.of(1).per(Seconds), // Quasistatic ramp rate (1 V/s)
              Volts.of(4), // Dynamic step voltage
              Seconds.of(10) // Timeout
              ),
          new SysIdRoutine.Mechanism(
              // Drive callback - convert voltage to duty cycle
              // Using duty cycle instead of the motor controller's voltage control
              // bypasses the internal closed-loop controller, resulting in cleaner data
              (Voltage voltage) ->
                  motorController.setDutyCycle(
                      voltage.in(Volts) / RobotController.getBatteryVoltage()),
              // Log callback - records position, velocity, and voltage
              // updateTelemetry() and simIterate() ensure sensor data is fresh at logging time
              log -> {
                motorController.updateTelemetry();
                motorController.simIterate();
                log.motor("motor")
                    .voltage(
                        m_appliedVoltage.mut_replace(
                            motorController.getDutyCycle() * RobotController.getBatteryVoltage(),
                            Volts))
                    .angularPosition(m_position.mut_replace(motorController.getMechanismPosition()))
                    .angularVelocity(
                        m_velocity.mut_replace(motorController.getMechanismVelocity()));
              },
              this, // Subsystem for requirements
              "Climb" // Name for logging
              ));

  }

  /** Brings climb arm(s) to height specified for prepping hang */
  public Command GoToPreHangHeight() {
    return runOnce(
            () -> {
              distance = ClimbConstants.PreHangExtension;
            })
        .andThen(elevator.setHeight(distance));
  }

  /** Brings climb arm(s) to height specified for clamping */
  public Command GoToHangHeight() {
    return runOnce(
            () -> {
              distance = ClimbConstants.HangDistance;
            })
        .andThen(elevator.setHeight(distance));
  }

  /** Brings climb arm(s) to height specified for releasing from hang */
  public Command GoToReleaseHeight() {
    return runOnce(
            () -> {
              distance = ClimbConstants.ReleaseDistance;
            })
        .andThen(elevator.setHeight(distance));
  }

  /** Brings climb arm(s) to height specified for resting */
  public Command GoToRestHeight() {
    return runOnce(
            () -> {
              distance = ClimbConstants.RestDistance;
            })
        .andThen(elevator.setHeight(distance));
  }

  public Command GoToHeight(Distance height) {
    return runOnce(
            () -> {
              distance = height;
              isWorking = true;
            })
        .andThen(elevator.setHeight(distance));
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
    SmartDashboard.putNumber(name + " Climb Distance Setpoint", distance.in(Inches));
    SmartDashboard.putNumber(name + " Climb Distance Actual", getHeight().in(Inches));

    motorController.updateTelemetry();
  }

  @Override
  public void simulationPeriodic() {
    SmartDashboard.putBoolean("Is it working?", isWorking);
    motorController.simIterate();
  }

  /** Returns the quasistatic test command. */
  public Command sysIdQuasistatic(SysIdRoutine.Direction direction) {
    return sysIdRoutine.quasistatic(direction);
  }

  /** Returns the dynamic test command. */
  public Command sysIdDynamic(SysIdRoutine.Direction direction) {
    return sysIdRoutine.dynamic(direction);
  }
}
