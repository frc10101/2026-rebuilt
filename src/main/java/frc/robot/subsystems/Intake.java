// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.RPM;
import static edu.wpi.first.units.Units.Rotations;
import static edu.wpi.first.units.Units.RotationsPerSecond;
import static edu.wpi.first.units.Units.Seconds;
import static edu.wpi.first.units.Units.Volts;

import com.ctre.phoenix6.hardware.TalonFX;
import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.MutAngle;
import edu.wpi.first.units.measure.MutAngularVelocity;
import edu.wpi.first.units.measure.MutVoltage;
import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
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
          .withFeedforward(
              new SimpleMotorFeedforward(
                  Constants.IntakeConstants.Roller.kS,
                  Constants.IntakeConstants.Roller.kV,
                  Constants.IntakeConstants.Roller.kA))
          .withMotorInverted(false)
          .withIdleMode(MotorMode.BRAKE)
          .withSupplyCurrentLimit(Constants.IntakeConstants.Roller.currentLimit)
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

  private final MutVoltage m_appliedVoltage = new MutVoltage(0, 0, Volts);
  private final MutAngle m_position = new MutAngle(0, 0, Rotations);
  private final MutAngularVelocity m_velocity = new MutAngularVelocity(0, 0, RotationsPerSecond);

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
    return setAngle(Constants.IntakeConstants.Pivot.intakePosition);
  }

  public Command jitterIntake() {
    return setAngle(Constants.IntakeConstants.Pivot.jitterPosition)
        .withTimeout(0.5)
        .andThen(setAngle(Constants.IntakeConstants.Pivot.intakePosition).withTimeout(0.1));
  }

  public Command zeroPivot() {
    return runOnce(() -> pivotController.setEncoderPosition(Degrees.of(0)));
  }

  // Roller Commands

  /**
   * Set the roller to a specific Velocity
   *
   * @param vel Velocity to set
   */
  public Command setRollerSpeed(AngularVelocity vel) {
    return runOnce(() -> rollerController.setVelocity(vel));
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
    return runOnce(() -> rollerController.setVelocity(RPM.of(0)));
  }

  public Command setIntakeRollerDutyCycle(double speed) {
    return runOnce(() -> rollerController.setDutyCycle(speed));
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

  public Command toggleIntake() {
    return runOnce(
        () -> {
          if (getRollerVelocity().abs(RPM) > 0) {
            rollerController.setVelocity(RPM.of(0));
          } else {
            rollerController.setVelocity(Constants.IntakeConstants.Roller.intakeSpeed);
          }
        });
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
    SmartDashboard.putNumber("Pivot Voltage", pivot.getMotorVoltage().getValueAsDouble());
    SmartDashboard.putNumber("Pivot Position", pivot.getPosition().getValueAsDouble());

    SmartDashboard.putNumber("Roller Speed", getRollerVelocity().baseUnitMagnitude());
  }

  @Override
  public void simulationPeriodic() {
    intakePivot.simIterate();
    rollerController.simIterate();
  }

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
                  rollerController.setDutyCycle(
                      voltage.in(Volts) / RobotController.getBatteryVoltage()),
              // Log callback - records position, velocity, and voltage
              // updateTelemetry() and simIterate() ensure sensor data is fresh at logging time
              log -> {
                rollerController.updateTelemetry();
                rollerController.simIterate();
                log.motor("motor")
                    .voltage(
                        m_appliedVoltage.mut_replace(
                            rollerController.getDutyCycle() * RobotController.getBatteryVoltage(),
                            Volts))
                    .angularPosition(
                        m_position.mut_replace(rollerController.getMechanismPosition()))
                    .angularVelocity(
                        m_velocity.mut_replace(rollerController.getMechanismVelocity()));
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
