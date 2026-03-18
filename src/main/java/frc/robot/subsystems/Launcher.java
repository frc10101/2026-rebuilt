// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Grams;
import static edu.wpi.first.units.Units.Inches;
import static edu.wpi.first.units.Units.KilogramSquareMeters;
import static edu.wpi.first.units.Units.RPM;
import static edu.wpi.first.units.Units.Rotations;
import static edu.wpi.first.units.Units.RotationsPerSecond;
import static edu.wpi.first.units.Units.Second;
import static edu.wpi.first.units.Units.Seconds;
import static edu.wpi.first.units.Units.Volts;

import com.ctre.phoenix6.hardware.TalonFX;
import edu.wpi.first.math.Pair;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.interpolation.InterpolatingDoubleTreeMap;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.MomentOfInertia;
import edu.wpi.first.units.measure.MutAngle;
import edu.wpi.first.units.measure.MutAngularVelocity;
import edu.wpi.first.units.measure.MutVoltage;
import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import frc.robot.Constants.LauncherConstants;
import yams.gearing.MechanismGearing;
import yams.mechanisms.config.FlyWheelConfig;
import yams.mechanisms.velocity.FlyWheel;
import yams.motorcontrollers.SmartMotorController;
import yams.motorcontrollers.SmartMotorControllerConfig;
import yams.motorcontrollers.SmartMotorControllerConfig.ControlMode;
import yams.motorcontrollers.SmartMotorControllerConfig.MotorMode;
import yams.motorcontrollers.SmartMotorControllerConfig.TelemetryVerbosity;
import yams.motorcontrollers.remote.TalonFXWrapper;

/**
 * This is the subsystem for delivering fuel by launching it from the robot.
 *
 * <p>It may also be referred to as <i>Blinky</i>.
 */
public class Launcher extends SubsystemBase {

  private AngularVelocity TargetSpeed = RPM.zero();

  /** Creates a new Launcher. */
  private TalonFX FlywheelLead = new TalonFX(LauncherConstants.MOTOR_ID_LEAD);
  private InterpolatingDoubleTreeMap LauncherLUT = new InterpolatingDoubleTreeMap();

  private TalonFX FlywheelFollow = new TalonFX(LauncherConstants.MOTOR_ID_FOLLOW);
  private SmartMotorControllerConfig smcConfig =
      new SmartMotorControllerConfig(this)
          .withControlMode(ControlMode.CLOSED_LOOP)
          // Feedback Constants (PID Constants)
          .withClosedLoopController(
              LauncherConstants.REAL_kP,
              LauncherConstants.REAL_kI,
              LauncherConstants.REAL_kD,
              RPM.of(LauncherConstants.MAX_VELOCITY_RPM),
              RPM.per(Second).of(LauncherConstants.MAX_ACCEL_RPMPerS))
          .withSimClosedLoopController(
              LauncherConstants.SIM_kP,
              LauncherConstants.SIM_kI,
              LauncherConstants.SIM_kD,
              RPM.of(LauncherConstants.MAX_VELOCITY_RPM),
              RPM.per(Second).of(LauncherConstants.MAX_ACCEL_RPMPerS))
          // Feedforward Constants
          .withFeedforward(
              new SimpleMotorFeedforward(
                  LauncherConstants.FFW_kS, LauncherConstants.FFW_kV, LauncherConstants.FFW_kA))
          .withSimFeedforward(
              new SimpleMotorFeedforward(
                  LauncherConstants.FFW_kS, LauncherConstants.FFW_kV, LauncherConstants.FFW_kA))
          // Telemetry name and verbosity level
          .withTelemetry(LauncherConstants.MOTOR_TELEMETRY_NAME, TelemetryVerbosity.HIGH)
          .withGearing(new MechanismGearing(LauncherConstants.GEARING))
          // Motor properties to prevent over currenting.
          .withMotorInverted(LauncherConstants.MOTOR_INVERTED)
          .withIdleMode(MotorMode.COAST)
          .withFollowers(Pair.of(FlywheelFollow, LauncherConstants.FOLLOWER_INVERTED))
          .withSupplyCurrentLimit(Amps.of(LauncherConstants.STATOR_CURRENT_LIMIT_AMPS));

  private SmartMotorController shooterMotors =
      new TalonFXWrapper(
          FlywheelLead, DCMotor.getKrakenX60Foc(LauncherConstants.MOTOR_COUNT), smcConfig);

  private final FlyWheelConfig LauncherConfig =
      new FlyWheelConfig(shooterMotors)
          .withDiameter(Inches.of(LauncherConstants.DIAMETER_INCH))
          .withMass(Grams.of(LauncherConstants.MASS_GRAMS))
          .withMOI(MomentOfInertia.ofBaseUnits(LauncherConstants.MOI_KG_M2, KilogramSquareMeters))
          .withTelemetry(LauncherConstants.MECH_TELEMETRY_NAME, TelemetryVerbosity.HIGH)
          .withSoftLimit(
              RPM.of(-LauncherConstants.SOFT_LIMIT_RPM), RPM.of(LauncherConstants.SOFT_LIMIT_RPM));

  private FlyWheel Launcher = new FlyWheel(LauncherConfig);

  private final MutVoltage m_appliedVoltage = new MutVoltage(0, 0, Volts);
  private final MutAngle m_position = new MutAngle(0, 0, Rotations);
  private final MutAngularVelocity m_velocity = new MutAngularVelocity(0, 0, RotationsPerSecond);



  /**
   * Gets the current velocity of the shooter.
   *
   * @return Shooter velocity.
   */
  public AngularVelocity getVelocity() {
    return Launcher.getSpeed();
  }

  /**
   * Set the shooter velocity.
   *
   * @param speed Speed to set.
   * @return {@link edu.wpi.first.wpilibj2.command.RunCommand}
   */
  public Command setVelocity(AngularVelocity speed) {
    TargetSpeed = speed;
    return Launcher.setSpeed(speed);
  }

  /**
   * Set the dutycycle of the shooter.
   *
   * @param dutyCycle DutyCycle to set.
   * @return {@link edu.wpi.first.wpilibj2.command.RunCommand}
   */
  public Command set(double dutyCycle) {
    return Launcher.set(dutyCycle);
  }

  public Trigger isAtSpeed() {
    return new Trigger(() -> TargetSpeed == Launcher.getSpeed());
  }

  public Launcher() {
    LauncherLUT.put(79.75, -3300.0);
    LauncherLUT.put(126.18248429134, -3500.0);
    LauncherLUT.put(200.0, -5000.0);
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    // Launcher.updateTelemetry();
    SmartDashboard.putNumber("Flywheel Target Speed", TargetSpeed.baseUnitMagnitude());
    SmartDashboard.putNumber("Flywheel Actual Speed", Launcher.getSpeed().baseUnitMagnitude());
    SmartDashboard.putBoolean("Flywheel at Speed", TargetSpeed == Launcher.getSpeed());
  }

  @Override
  public void simulationPeriodic() {
    // This method will be called once per scheduler run during simulation
    // Launcher.simIterate();
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
                  shooterMotors.setDutyCycle(
                      voltage.in(Volts) / RobotController.getBatteryVoltage()),
              // Log callback - records position, velocity, and voltage
              // updateTelemetry() and simIterate() ensure sensor data is fresh at logging time
              log -> {
                shooterMotors.updateTelemetry();
                shooterMotors.simIterate();
                log.motor("motor")
                    .voltage(
                        m_appliedVoltage.mut_replace(
                            shooterMotors.getDutyCycle() * RobotController.getBatteryVoltage(),
                            Volts))
                    .angularPosition(m_position.mut_replace(shooterMotors.getMechanismPosition()))
                    .angularVelocity(m_velocity.mut_replace(shooterMotors.getMechanismVelocity()));
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

  public Command launchDistance(double distance){
    return runOnce(()->{launchDistance(distance);});
  }

  private void LaunchDistance(double distance){
    setVelocity(RPM.of(LauncherLUT.get(distance)));
  }
}
