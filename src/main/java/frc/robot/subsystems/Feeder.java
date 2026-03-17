// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import static edu.wpi.first.units.Units.Rotations;
import static edu.wpi.first.units.Units.RotationsPerSecond;
import static edu.wpi.first.units.Units.Seconds;
import static edu.wpi.first.units.Units.Volts;

import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.units.measure.MutAngle;
import edu.wpi.first.units.measure.MutAngularVelocity;
import edu.wpi.first.units.measure.MutVoltage;
import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import frc.robot.Constants.ColumnConstants;
import frc.robot.Constants.SparkMaxCanIDs;
import yams.motorcontrollers.SmartMotorController;
import yams.motorcontrollers.SmartMotorControllerConfig;
import yams.motorcontrollers.SmartMotorControllerConfig.ControlMode;
import yams.motorcontrollers.SmartMotorControllerConfig.MotorMode;
import yams.motorcontrollers.SmartMotorControllerConfig.TelemetryVerbosity;
import yams.motorcontrollers.local.SparkWrapper;

/**
 * This is the subsystem for moving fuel between the {@link Indexer} and the {@link Launcher}.
 *
 * <p>It may also be referred to as <i>Network Switch in between RoboRio and Radio</i>.
 */
public class Feeder extends SubsystemBase {
  private Voltage m_motorspeed = Volts.zero();

  private final MutVoltage m_appliedVoltage = new MutVoltage(0, 0, Volts);
  private final MutAngle m_position = new MutAngle(0, 0, Rotations);
  private final MutAngularVelocity m_velocity = new MutAngularVelocity(0, 0, RotationsPerSecond);

  private SmartMotorControllerConfig MotorConfig =
      new SmartMotorControllerConfig(this)
          .withControlMode(ControlMode.CLOSED_LOOP)
          .withClosedLoopController(
              ColumnConstants.Real.kp,
              ColumnConstants.Real.ki,
              ColumnConstants.Real.kd,
              ColumnConstants.Real.maxVelocity,
              ColumnConstants.Real.maxAcceleration)
          .withSimClosedLoopController(
              ColumnConstants.Sim.kp,
              ColumnConstants.Sim.ki,
              ColumnConstants.Sim.kd,
              ColumnConstants.Sim.maxVelocity,
              ColumnConstants.Sim.maxAcceleration)
          .withTelemetry("ColumnMotor", TelemetryVerbosity.HIGH)
          .withGearing(ColumnConstants.gearRatio)
          .withMotorInverted(false)
          .withIdleMode(MotorMode.BRAKE)
          .withSupplyCurrentLimit(ColumnConstants.currentLimit);

  private SparkMax m_motor = new SparkMax(SparkMaxCanIDs.ColumnMotor, MotorType.kBrushless);

  // create the smartMotorController
  private SmartMotorController motorController =
      new SparkWrapper(m_motor, DCMotor.getNEO(1), MotorConfig);

  /** Creates a new Feeder. */
  public Feeder() {}

  public Command IntakeFuel() {
    return runOnce(() -> m_motorspeed = ColumnConstants.IntakeSpeed);
  }

  public Command OuttakeFuel() {
    return runOnce(() -> m_motorspeed = ColumnConstants.OuttakeSpeed);
  }

  public Command NoFuel() {
    return runOnce(() -> m_motorspeed = Volts.zero());
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    motorController.setVoltage(m_motorspeed);
    SmartDashboard.putNumber("Column Setpoint", m_motorspeed.baseUnitMagnitude());
    SmartDashboard.putNumber(
        "Column Actual", motorController.getMechanismVelocity().baseUnitMagnitude());

    motorController.updateTelemetry();
  }

  @Override
  public void simulationPeriodic() {
    motorController.simIterate();
  }
  // Create the SysIdRoutine
  private final SysIdRoutine sysIdRoutine =
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
              "MyMechanism" // Name for logging
              ));

  /** Returns the quasistatic test command. */
  public Command sysIdQuasistatic(SysIdRoutine.Direction direction) {
    return sysIdRoutine.quasistatic(direction);
  }

  /** Returns the dynamic test command. */
  public Command sysIdDynamic(SysIdRoutine.Direction direction) {
    return sysIdRoutine.dynamic(direction);
  }
}
