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
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import frc.robot.Constants.ColumnConstants;
import frc.robot.Constants.SparkMaxCanIDs;
import org.littletonrobotics.junction.AutoLog;
import org.littletonrobotics.junction.Logger;
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
  @AutoLog
  public static class FeederInputs {
    public double setpointVolts = 0.0;
    public double appliedVolts = 0.0;
    public double mechanismVelocity = 0.0;
    public double currentAmps = 0.0;
    public String state = FeederState.IDLE_REVERSE.name();
  }

  private final FeederInputsAutoLogged feederInputs = new FeederInputsAutoLogged();
  private Voltage m_motorspeed = Volts.zero();

  private enum FeederState {
    IDLE_REVERSE,
    RAMP_TO_LAUNCH,
    LAUNCH,
    STOP,
    OUTTAKE
  }

  private final MutVoltage m_appliedVoltage = new MutVoltage(0, 0, Volts);
  private final MutAngle m_position = new MutAngle(0, 0, Rotations);
  private final MutAngularVelocity m_velocity = new MutAngularVelocity(0, 0, RotationsPerSecond);
  private final Timer voltageRampTimer = new Timer();
  private FeederState state = FeederState.IDLE_REVERSE;

  private SmartMotorControllerConfig MotorConfig =
      new SmartMotorControllerConfig(this)
          .withControlMode(ControlMode.CLOSED_LOOP)
          .withClosedLoopController(
              ColumnConstants.Real.kp, ColumnConstants.Real.ki, ColumnConstants.Real.kd)
          .withSimClosedLoopController(
              ColumnConstants.Sim.kp, ColumnConstants.Sim.ki, ColumnConstants.Sim.kd)
          .withTrapezoidalProfile(
              ColumnConstants.Real.maxVelocity, ColumnConstants.Real.maxAcceleration)
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
    return runOnce(() -> state = FeederState.LAUNCH);
  }

  public Command OuttakeFuel() {
    return runOnce(() -> state = FeederState.OUTTAKE);
  }

  public Command NoFuel() {
    return runOnce(() -> state = FeederState.STOP);
  }

  public Command IdleReverse() {
    return runOnce(
        () -> {
          resetLaunchRampTimer();
          state = FeederState.IDLE_REVERSE;
        });
  }

  public Command HoldIdleReverse() {
    return run(
        () -> {
          state = FeederState.IDLE_REVERSE;
        });
  }

  public Command VoltageRampDownLaunch() {
    return runOnce(
        () -> {
          resetLaunchRampTimer();
          voltageRampTimer.start();
          state = FeederState.RAMP_TO_LAUNCH;
        });
  }

  public Command HoldLaunchWithRamp() {
    return run(
        () -> {
          if (state != FeederState.RAMP_TO_LAUNCH && state != FeederState.LAUNCH) {
            resetLaunchRampTimer();
            voltageRampTimer.start();
            state = FeederState.RAMP_TO_LAUNCH;
          }
        });
  }

  public Command ResetVoltageRampDownLaunch() {
    return IdleReverse();
  }

  private void resetLaunchRampTimer() {
    voltageRampTimer.stop();
    voltageRampTimer.reset();
  }

  private void updateInputs() {
    feederInputs.setpointVolts = m_motorspeed.in(Volts);
    feederInputs.appliedVolts = motorController.getVoltage().in(Volts);
    feederInputs.mechanismVelocity = motorController.getMechanismVelocity().baseUnitMagnitude();
    feederInputs.currentAmps = m_motor.getOutputCurrent();
    feederInputs.state = state.name();
  }

  @Override
  public void periodic() {
    if (state == FeederState.RAMP_TO_LAUNCH) {
      double progress = Math.min(1.0, voltageRampTimer.get() / ColumnConstants.LaunchRampSeconds);
      double startVoltage = ColumnConstants.FirstLaunchSpeed.in(Volts);
      double endVoltage = ColumnConstants.IntakeSpeed.in(Volts);
      m_motorspeed = Volts.of(startVoltage + (endVoltage - startVoltage) * progress);
      if (progress >= 1.0) {
        state = FeederState.LAUNCH;
      }
    } else if (state == FeederState.IDLE_REVERSE) {
      m_motorspeed = ColumnConstants.IdleReverseSpeed;
    } else if (state == FeederState.LAUNCH) {
      m_motorspeed = ColumnConstants.IntakeSpeed;
    } else if (state == FeederState.OUTTAKE) {
      m_motorspeed = ColumnConstants.OuttakeSpeed;
    } else {
      m_motorspeed = Volts.zero();
    }

    motorController.setVoltage(m_motorspeed);
    updateInputs();
    Logger.processInputs("Feeder", feederInputs);
    SmartDashboard.putNumber("Column Setpoint", m_motorspeed.baseUnitMagnitude());
    SmartDashboard.putNumber(
        "Column Actual", motorController.getMechanismVelocity().baseUnitMagnitude());
    SmartDashboard.putString("Column State", state.name());

    motorController.updateTelemetry();
  }

  public Command toggleColumn() {
    return runOnce(
        () -> {
          if (state == FeederState.IDLE_REVERSE) {
            state = FeederState.STOP;
          } else {
            state = FeederState.IDLE_REVERSE;
          }
        });
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
