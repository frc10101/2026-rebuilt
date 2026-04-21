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
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.Pair;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.MomentOfInertia;
import edu.wpi.first.units.measure.MutAngle;
import edu.wpi.first.units.measure.MutAngularVelocity;
import edu.wpi.first.units.measure.MutVoltage;
import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import frc.robot.Constants.LauncherConstants;
import frc.robot.subsystems.drive.Drive;
import frc.robot.util.Helpers;
import frc.robot.util.Launcher.ProjectileSimulator;
import frc.robot.util.Launcher.ShotCalculator;
import java.util.function.Supplier;
import org.littletonrobotics.junction.AutoLog;
import org.littletonrobotics.junction.Logger;
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
  public enum LauncherMode {
    IDLE,
    ALLIANCE_AUTO,
    PASS
  }

  @AutoLog
  public static class ShooterInputs {
    public AngularVelocity velocity = RPM.of(0);
    public AngularVelocity setpoint = RPM.of(0);
    public Voltage volts = Volts.of(0);
    public Current current = Amps.of(0);
  }

  private final ShooterInputsAutoLogged shooterInputs = new ShooterInputsAutoLogged();
  private ProjectileSimulator sim = new ProjectileSimulator(LauncherConstants.params);
  private ProjectileSimulator.GeneratedLUT lut = sim.generateLUT();
  private double lastLaunchRPM = 0.0; // Store the last calculated launch RPM
  private LauncherMode currentMode = LauncherMode.IDLE;
  private double requestedRpm = LauncherConstants.FLYWHEEL_IDLE_RPM;

  // in RobotContainer or wherever you set stuff up
  private ShotCalculator.Config config = new ShotCalculator.Config();

  public ShotCalculator shotCalc;

  /** Creates a new Launcher. */
  private TalonFX FlywheelLead = new TalonFX(LauncherConstants.MOTOR_ID_LEAD);

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

  private void updateInputs() {
    shooterInputs.velocity = Launcher.getSpeed();
    shooterInputs.setpoint = shooterMotors.getMechanismSetpointVelocity().orElse(RPM.of(0));
    shooterInputs.volts = shooterMotors.getVoltage();
    shooterInputs.current = shooterMotors.getStatorCurrent();
  }

  public Launcher() {
    // print it out
    for (var entry : lut.entries()) {
      if (entry.reachable()) {
        System.out.printf(
            "%.2fm -> %.0f RPM, %.3fs TOF%n", entry.distanceM(), entry.rpm(), entry.tof());
      }
    }
    config.launcherOffsetX = -0.223; // how far forward the launcher is from robot center (m)
    config.launcherOffsetY = 0.0; // how far left, 0 if centered
    config.phaseDelayMs = 30.0; // your vision pipeline latency
    config.mechLatencyMs = 20.0; // how long the mechanism takes to respond
    config.maxTiltDeg = 5.0; // suppress firing when chassis tilts past this (bumps/ramps)
    config.headingSpeedScalar = 1.0; // heading tolerance tightens with robot speed (0 to disable)
    config.headingReferenceDistance = 2.5; // heading tolerance scales with distance from hub
    shotCalc = new ShotCalculator(config);

    // load the LUT you generated
    for (var entry : lut.entries()) {
      if (entry.reachable()) {
        shotCalc.loadLUTEntry(entry.distanceM(), entry.rpm(), entry.tof());
      }
    }
  }
  /**
   * Gets the current velocity of the shooter.
   *
   * @return FlyWheel velocity.
   */
  public AngularVelocity getVelocity() {
    return shooterInputs.velocity;
  }

  /**
   * Set the shooter velocity.
   *
   * @param speed Speed to set.
   * @return {@link edu.wpi.first.wpilibj2.command.RunCommand}
   */
  public Command setVelocity(AngularVelocity speed) {
    Logger.recordOutput("Shooter/Setpoint", speed);
    return Launcher.setSpeed(speed);
  }

  /**
   * Set the dutycycle of the shooter.
   *
   * @param dutyCycle DutyCycle to set.
   * @return {@link edu.wpi.first.wpilibj2.command.RunCommand}
   */
  public Command set(double dutyCycle) {
    Logger.recordOutput("Shooter/DutyCycle", dutyCycle);
    return Launcher.set(dutyCycle);
  }

  public Command setVelocity(Supplier<AngularVelocity> speed) {
    return Launcher.setSpeed(
        () -> {
          Logger.recordOutput("Shooter/Setpoint", speed.get());
          return speed.get();
        });
  }

  public Command setDutyCycle(Supplier<Double> dutyCycle) {
    return Launcher.set(
        () -> {
          Logger.recordOutput("Shooter/DutyCycle", dutyCycle.get());
          return dutyCycle.get();
        });
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    updateInputs();
    Logger.processInputs("Shooter", shooterInputs);
    Logger.recordOutput("LaunchRPM", Launcher.getSpeed().in(RPM));
    Logger.recordOutput("Shooter/Mode", currentMode.name());
    Logger.recordOutput("Shooter/RequestedRPM", requestedRpm);
    Logger.recordOutput("Shooter/Ready", isLaunchReady());
    Logger.recordOutput("LauncherOffset", shotCalc.getOffset());
    Launcher.updateTelemetry();
  }

  @Override
  public void simulationPeriodic() {
    // This method will be called once per scheduler run during simulation
    Launcher.simIterate();
  }

  // Create the SysIdRoutine
  private final SysIdRoutine sysIdRoutine =
      new SysIdRoutine(
          // Config: ramp rate, step voltage, timeout
          new SysIdRoutine.Config(
              Volts.of(1).per(Seconds), // Quasistatic ramp rate (1 V/s)
              Volts.of(4), // Dynamic step voltage
              Seconds.of(10), // Timeout
              (state) -> Logger.recordOutput("SysIdLauncher_State", state.toString())),
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

  public Command runIdleControl() {
    return run(this::applyIdleTarget);
  }

  public Command runAllianceAutoControl(Drive swerve, boolean allianceFlip, boolean override) {
    return run(
        () -> {
          if (Helpers.isPoseInAllianceZone(swerve.getPose())) {
            currentMode = LauncherMode.ALLIANCE_AUTO;
            var hubCenter = getAllianceHubCenter();
            if (allianceFlip) {
              hubCenter = getNotAllianceHubCenter();
            }

            Logger.recordOutput("Alliance Hub Center", hubCenter);
            Logger.recordOutput("Overide Run Alliance Auto Control", override);
            var shot = calculateShotToTarget(swerve, hubCenter);
            if (shot == null) {
              applyIdleTarget();
              return;
            }

            if (override || shot.confidence() > 0.49) {
              applyTargetRpm(shot.rpm());
              lastLaunchRPM = shot.rpm();
            } else {
              applyIdleTarget();
            }
          } else {
            if (!override) {
              applyIdleTarget();
              return;
            }
            currentMode = LauncherMode.ALLIANCE_AUTO;
            var hubCenter = getAllianceHubCenter();
            if (allianceFlip) {
              hubCenter = getNotAllianceHubCenter();
            }

            Logger.recordOutput("Alliance Hub Center", hubCenter);
            Logger.recordOutput("Overide Run Alliance Auto Control", override);
            var shot = calculateShotToTarget(swerve, hubCenter);
            if (shot == null) {
              applyIdleTarget();
              return;
            }
            applyTargetRpm(shot.rpm());
            lastLaunchRPM = shot.rpm();
            return;
          }
        });
  }

  public Command runPassControl(Drive swerve, boolean override) {
    return run(
        () -> {
          currentMode = LauncherMode.PASS;
          Translation2d passTarget = getAlliancePassTarget(swerve);
          var shot = calculateShotToTarget(swerve, passTarget);
          if (shot != null || override) {
            applyTargetRpm(shot.rpm());
            lastLaunchRPM = shot.rpm();
            Logger.recordOutput("Shooter/PassTargetX", passTarget.getX());
            Logger.recordOutput("Shooter/PassTargetY", passTarget.getY());
            Logger.recordOutput("Overide Run Pass Control", override);
          } else {
            applyIdleTarget();
          }
        });
  }

  public boolean isLaunchReady() {
    if (currentMode == LauncherMode.IDLE) {
      return false;
    }

    return Math.abs(getLauncherSpeedRPM() - requestedRpm) <= LauncherConstants.READY_TOLERANCE_RPM;
  }

  public Rotation2d Launch(Drive swerve, boolean override) {
    Logger.recordOutput("Override Launch Method", override);
    ShotCalculator.LaunchParameters shot = calculateShotToTarget(swerve, getAllianceHubCenter());
    /*if (shot != null) {
      lastLaunchRPM = shot.rpm();
      return shot.driveAngle();
    }
    */

    if (shot.confidence() > 50) {}

    // Fallback: If ShotCalculator fails, just aim at hub
    Translation2d hubCenter = getAllianceHubCenter();
    Translation2d robotPos = swerve.getPose().getTranslation();
    Translation2d toHub = hubCenter.minus(robotPos);
    return toHub.getAngle();
  }

  private void applyIdleTarget() {
    currentMode = LauncherMode.IDLE;
    applyTargetRpm(LauncherConstants.FLYWHEEL_IDLE_RPM);
  }

  private void applyTargetRpm(double rpm) {
    requestedRpm = rpm;
    Launcher.setMechanismVelocitySetpoint(RPM.of(rpm));
  }

  public ShotCalculator.LaunchParameters calculateShotToTarget(Drive swerve, Translation2d target) {
    ShotCalculator.ShotInputs inputs =
        new ShotCalculator.ShotInputs(
            swerve.getPose(),
            swerve.getFieldVelocity(),
            swerve.getRobotVelocity(),
            target,
            DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue)
                    == DriverStation.Alliance.Red
                ? LauncherConstants.redHubForward
                : LauncherConstants.blueHubForward,
            0.9,
            swerve.getPitch().getDegrees(),
            swerve.getRoll().getDegrees());

    ShotCalculator.LaunchParameters shot = shotCalc.calculate(inputs);
    Logger.recordOutput("isValid", shot.isValid());
    Logger.recordOutput("Confidence", shot.confidence());
    return shot;
  }

  private Translation2d getAlliancePassTarget(Drive swerve) {
    double fieldLength = frc.robot.Constants.VisionConstants.aprilTagLayout.getFieldLength();
    boolean isRed =
        DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue)
            == DriverStation.Alliance.Red;
    double targetX =
        isRed
            ? fieldLength - LauncherConstants.PASS_TARGET_X_METERS
            : LauncherConstants.PASS_TARGET_X_METERS;
    return new Translation2d(targetX, swerve.getPose().getY());
  }

  public Translation2d getAllianceHubCenter() {
    double fieldLength =
        AprilTagFieldLayout.loadField(AprilTagFields.k2026RebuiltWelded)
            .getFieldLength(); // 2024-2025 field length in meters
    boolean isRed =
        edu.wpi.first.wpilibj.DriverStation.getAlliance()
                .orElse(edu.wpi.first.wpilibj.DriverStation.Alliance.Blue)
            == edu.wpi.first.wpilibj.DriverStation.Alliance.Red;

    if (isRed) {
      // Red hub is mirrored across field center
      return new Translation2d(fieldLength - 4.6, 4.0);
    } else {
      // Blue hub
      return new Translation2d(4.6, 4.0);
    }
  }

  private Translation2d getNotAllianceHubCenter() {
    double fieldLength =
        AprilTagFieldLayout.loadField(AprilTagFields.k2026RebuiltWelded)
            .getFieldLength(); // 2024-2025 field length in meters
    boolean isRed =
        edu.wpi.first.wpilibj.DriverStation.getAlliance()
                .orElse(edu.wpi.first.wpilibj.DriverStation.Alliance.Blue)
            == edu.wpi.first.wpilibj.DriverStation.Alliance.Red;

    if (!isRed) {
      // Red hub is mirrored across field center
      return new Translation2d(fieldLength - 4.6, 4.0);
    } else {
      // Blue hub
      return new Translation2d(4.6, 4.0);
    }
  }

  public double getLauncherSpeedRPM() {
    return shooterInputs.velocity.in(RPM);
  }

  public double getTargetLaunchRPM() {
    return lastLaunchRPM;
  }
}
