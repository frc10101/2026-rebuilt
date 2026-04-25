// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot;

import com.pathplanner.lib.auto.NamedCommands;
import com.pathplanner.lib.commands.PathPlannerAuto;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.button.CommandJoystick;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import frc.robot.Constants.Mode;
import frc.robot.commands.DriveCommands;
import frc.robot.generated.TunerConstants;
import frc.robot.subsystems.Feeder;
import frc.robot.subsystems.Indexer;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Launcher;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.drive.GyroIO;
import frc.robot.subsystems.drive.GyroIOPigeon2;
import frc.robot.subsystems.drive.ModuleIO;
import frc.robot.subsystems.drive.ModuleIOSim;
import frc.robot.subsystems.drive.ModuleIOTalonFX;
import frc.robot.subsystems.vision.Vision;
import frc.robot.subsystems.vision.VisionIOPhotonVision;
import frc.robot.subsystems.vision.VisionIOPhotonVisionSim;
import frc.robot.util.Helpers;
import frc.robot.util.Launcher.FuelPhysicsSim;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.LoggedDashboardChooser;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  // Subsystems
  private final Drive drive;

  @SuppressWarnings("unused")
  private final Vision vision;

  private final Launcher launcher;
  private final Intake m_intake;
  private final Feeder Column;
  private final Indexer BeltDexter;
  private FuelPhysicsSim ballSim;

  // Controller
  private final CommandXboxController driverOneController = new CommandXboxController(0);
  private final CommandXboxController driverTwoController = new CommandXboxController(1);
  private final CommandJoystick testController = new CommandJoystick(2);

  // Buttons tehe
  // Driver One A Button
  private final Trigger alignToGoalButton = driverOneController.button(1);

  // Driver One Options Button
  private final Trigger resetIMUButton = driverOneController.button(8);

  // Driver One X Button
  private final Trigger xOutButton = driverOneController.button(3);

  // Driver One Right Trigger
  private final Trigger LaunchFuel = driverOneController.axisGreaterThan(3, 0.3);

  // Driver One Dpad Down Button
  private final Trigger IntakeStowed = driverOneController.povDown();

  // Driver Two B  Button
  private final Trigger JitterIntake = driverTwoController.button(2);

  // Driver Twp Right Bumper
  private final Trigger IntakeDownButton = driverTwoController.button(6);

  // Driver Two Left Bumper
  private final Trigger IntakeUp = driverTwoController.button(5);

  // Driver Two Left Button
  private final Trigger ToggleIntake = driverTwoController.axisGreaterThan(2, 0.3);
  // Driver Two Right Trigger
  private final Trigger LaunchFuelOveride = driverTwoController.axisGreaterThan(3, 0.3);
  // Driver Two Dpad Up
  private final Trigger LaunchNudgeUp = driverTwoController.povUp();
  // Driver Two Dpad Down
  private final Trigger LaunchNudgeDown = driverTwoController.povDown();

  // Dashboard inputs
  private final LoggedDashboardChooser<Command> autoChooser;

  /** The container for the robot. Contains subsystems, IO devices, and commands. */
  public RobotContainer() {
    DriverStation.silenceJoystickConnectionWarning(true);
    autoChooser = new LoggedDashboardChooser<>("Auto Choices");
    switch (Constants.currentMode) {
      case REAL:
        // Real robot, instantiate hardware IO implementations
        // ModuleIOTalonFX is intended for modules with TalonFX drive, TalonFX turn, and
        // a CANcoder
        drive =
            new Drive(
                new GyroIOPigeon2(),
                new ModuleIOTalonFX(TunerConstants.FrontLeft),
                new ModuleIOTalonFX(TunerConstants.FrontRight),
                new ModuleIOTalonFX(TunerConstants.BackLeft),
                new ModuleIOTalonFX(TunerConstants.BackRight));

        // The ModuleIOTalonFXS implementation provides an example implementation for
        // TalonFXS controller connected to a CANdi with a PWM encoder. The
        // implementations
        // of ModuleIOTalonFX, ModuleIOTalonFXS, and ModuleIOSpark (from the Spark
        // swerve
        // template) can be freely intermixed to support alternative hardware
        // arrangements.
        // Please see the AdvantageKit template documentation for more information:
        // https://docs.advantagekit.org/getting-started/template-projects/talonfx-swerve-template#custom-module-implementations
        //
        // drive =
        // new Drive(
        // new GyroIOPigeon2(),
        // new ModuleIOTalonFXS(TunerConstants.FrontLeft),
        // new ModuleIOTalonFXS(TunerConstants.FrontRight),
        // new ModuleIOTalonFXS(TunerConstants.BackLeft),
        // new ModuleIOTalonFXS(TunerConstants.BackRight));
        Column = new Feeder();
        BeltDexter = new Indexer();
        launcher = new Launcher();
        vision = new Vision(drive::addVisionMeasurement, VisionIOPhotonVision.createAllCameras());
        m_intake = new Intake();
        NamedCommands.registerCommand("IntakeDown", m_intake.goToIntakePosition());
        // NamedCommands.registerCommand(
        //     "LauncherSpinUp",
        //     // launcher
        //         // .changeDistanceType(LauncherState.AUTO)
        //         // .andThen(new InstantCommand(launcher::setVelocity)));
        NamedCommands.registerCommand(
            "Launch",
            Commands.waitUntil(launcher::isLaunchReady)
                .andThen(
                    BeltDexter.IntakeFuel()
                        .alongWith(Column.VoltageRampDownLaunch())
                        .alongWith(m_intake.jitterIntakeAuto().repeatedly())));
        NamedCommands.registerCommand(
            "LauncherIdle",
            launcher.worldsAutoRev(drive, LaunchFuelOveride.getAsBoolean()).repeatedly());
        NamedCommands.registerCommand("ColumnIdle", Column.IdleReverse());

        // Intake Roller
        NamedCommands.registerCommand("IntakeRollerIn", m_intake.intake());
        NamedCommands.registerCommand("IntakeRollerOut", m_intake.outtake());
        NamedCommands.registerCommand("IntakeRollerStop", m_intake.stopRoller());

        // Indexer (BeltDexter)
        NamedCommands.registerCommand("IndexerIn", BeltDexter.IntakeFuel());
        NamedCommands.registerCommand("IndexerStop", BeltDexter.NoFuel());

        // Feeder
        NamedCommands.registerCommand("FeedIn", Column.IntakeFuel());
        NamedCommands.registerCommand("FeedOut", Column.OuttakeFuel());
        NamedCommands.registerCommand("FeedStop", Column.NoFuel());

        // Wait
        NamedCommands.registerCommand("Wait", Commands.waitSeconds(4.0));
        // Set up SysId routines
        // autoChooser.addOption(
        //     "Drive Wheel Radius Characterization",
        // DriveCommands.wheelRadiusCharacterization(drive));
        // autoChooser.addOption(
        //     "Drive Simple FF Characterization",
        // DriveCommands.feedforwardCharacterization(drive));
        // autoChooser.addOption(
        //     "Drive SysId (Quasistatic Forward)",
        //     drive.sysIdQuasistatic(SysIdRoutine.Direction.kForward));
        // autoChooser.addOption(
        //     "Drive SysId (Quasistatic Reverse)",
        //     drive.sysIdQuasistatic(SysIdRoutine.Direction.kReverse));
        // autoChooser.addOption(
        //     "Drive SysId (Dynamic Forward)",
        // drive.sysIdDynamic(SysIdRoutine.Direction.kForward));
        // autoChooser.addOption(
        //     "Drive SysId (Dynamic Reverse)",
        // drive.sysIdDynamic(SysIdRoutine.Direction.kReverse));

        // most important autos
        // === NEUTRAL TOP ===
        autoChooser.addOption(
            "NT > Launch > Launch T", new PathPlannerAuto("Start-Neutral-Top-Launch-T"));
        autoChooser.addOption(
            "NT > Depot > Launch T", new PathPlannerAuto("Start-Neutral-Top-Depot-Launch-T"));
        autoChooser.addOption(
            "NT > Chute > Launch T", new PathPlannerAuto("Start-Neutral-Top-Chute-Launch-T"));

        // === NEUTRAL HALF TOP ===
        autoChooser.addOption(
            "NHT > Launch > Launch T", new PathPlannerAuto("Start-Neutral-Half-Top-Launch-T"));
        autoChooser.addOption(
            "NHT > Depot > Launch T",
            new PathPlannerAuto("Start-Neutral-Half-Top-Launch-Depot-Launch-T"));
        autoChooser.addOption(
            "NHT > Chute > Launch T",
            new PathPlannerAuto("Start-Neutral-Half-Top-Launch-Chute-Launch-T"));
        autoChooser.addOption(
            "NHT > Top > Launch T",
            new PathPlannerAuto("Start-Neutral-Half-Top-Launch-Top-Launch-T"));

        // === NEUTRAL HALF BOTTOM ===
        autoChooser.addOption(
            "NHB > Launch B", new PathPlannerAuto("Start-Neutral-Half-Bottom-Launch-B"));
        autoChooser.addOption(
            "NHB > Depot B",
            new PathPlannerAuto("Start-Neutral-Half-Bottom-Launch-Depot-Launch-B"));
        autoChooser.addOption(
            "NHB > Chute B",
            new PathPlannerAuto("Start-Neutral-Half-Bottom-Launch-Chute-Launch-B"));
        autoChooser.addOption(
            "NHB > Bottom > Launch B",
            new PathPlannerAuto("Start-Neutral-Half-Bottom-Launch-Bottom-Launch-B"));

        // === NEUTRAL BOTTOM ===
        autoChooser.addOption(
            "NB > Launch > Launch B", new PathPlannerAuto("Start-Neutral_Bottom-Launch-B"));
        autoChooser.addOption(
            "NB > Depot > Launch B", new PathPlannerAuto("Start-Neutral_Bottom-Depot-Launch-B"));
        autoChooser.addOption(
            "NB > Chute > Launch B", new PathPlannerAuto("Start-Neutral-B-Launch-Chute-Launch-B"));

        // === LAUNCH ===
        autoChooser.addOption("Launch T", new PathPlannerAuto("Start-Launch-T"));
        autoChooser.addOption("Launch M", new PathPlannerAuto("Start-Launch-M"));
        autoChooser.addOption("Launch B", new PathPlannerAuto("Start-Launch-B"));

        // === DEPOT ===
        autoChooser.addOption("Depot T", new PathPlannerAuto("Start-Depot_Launch-T"));

        // === CHUTE ===
        autoChooser.addOption("Chute B", new PathPlannerAuto("Start-Chute-Launch-B"));

        // === STUPID SIMPLE ===
        autoChooser.addOption("StupidSimple", new PathPlannerAuto("StupidSimple"));

        // NamedCommands.registerCommand(
        //     "StopAll",
        //     launcher
        //         .changeDistanceType(LauncherState.OFF)
        //         .alongWith(BeltDexter.NoFuel())
        //         .alongWith(Column.NoFuel())
        //         .alongWith(m_intake.goToIntakePosition())
        //         .andThen(Column.ResetVoltageRampDownLaunch())
        //         // .alongWith(new InstantCommand(launcher::setVelocity)));
        break;

      case SIM:
        // Sim robot, instantiate physics sim IO implementations
        drive =
            new Drive(
                new GyroIO() {},
                new ModuleIOSim(TunerConstants.FrontLeft),
                new ModuleIOSim(TunerConstants.FrontRight),
                new ModuleIOSim(TunerConstants.BackLeft),
                new ModuleIOSim(TunerConstants.BackRight));
        Column = new Feeder();
        BeltDexter = new Indexer();
        launcher = new Launcher();
        vision =
            new Vision(
                drive::addVisionMeasurement,
                VisionIOPhotonVisionSim.createAllSimCameras(drive::getPose));

        m_intake = new Intake();
        break;

      default:
        // Replayed robot, disable IO implementations
        drive =
            new Drive(
                new GyroIO() {},
                new ModuleIO() {},
                new ModuleIO() {},
                new ModuleIO() {},
                new ModuleIO() {});
        Column = new Feeder();
        BeltDexter = new Indexer();
        launcher = new Launcher();
        vision = new Vision(drive::addVisionMeasurement);
        m_intake = new Intake();
        break;
    }

    // Set up auto routines
    // Set up SysId routines
    // autoChooser.addOption(
    //     "Drive Wheel Radius Characterization", DriveCommands.wheelRadiusCharacterization(drive));
    // autoChooser.addOption(
    //     "Drive Simple FF Characterization", DriveCommands.feedforwardCharacterization(drive));
    // autoChooser.addOption(
    //     "Drive SysId (Quasistatic Forward)",
    //     drive.sysIdQuasistatic(SysIdRoutine.Direction.kForward));
    // autoChooser.addOption(
    //     "Drive SysId (Quasistatic Reverse)",
    //     drive.sysIdQuasistatic(SysIdRoutine.Direction.kReverse));
    // autoChooser.addOption(
    //     "Drive SysId (Dynamic Forward)", drive.sysIdDynamic(SysIdRoutine.Direction.kForward));
    // autoChooser.addOption(
    //     "Drive SysId (Dynamic Reverse)", drive.sysIdDynamic(SysIdRoutine.Direction.kReverse));
    autoChooser.addOption("AutoTest", drive.getAuto("MiddleStartLoadingStation"));
    autoChooser.addOption("AutoNoRev", drive.getAuto("MiddleStartLoadingStationNoRev"));
    // autoChooser.addOption("AUTOTEST", );

    // Configure the button bindings

    configureButtonBindings();
    Logger.recordOutput("isHubActive", Helpers.isAllianceHubActive());
    Logger.recordOutput("timeToNextShift", Helpers.getTimeToNextAllianceShift());
    Logger.recordOutput("currentShift", Helpers.getShift());
  }

  /**
   * Use this method to define your button->command mappings. Buttons can be created by
   * instantiating a {@link GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a {@link
   * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  private void configureButtonBindings() {
    launcher.setDefaultCommand(launcher.runIdleControl());
    Column.setDefaultCommand(Column.HoldIdleReverse());
    BeltDexter.setDefaultCommand(BeltDexter.HoldIdleSpin());
    // m_intake.setDefaultCommand(m_intake.setAngle(Degrees.of(90)));

    // Default command, normal field-relative drive
    drive.setDefaultCommand(
        DriveCommands.joystickDrive(
            drive,
            () -> -driverOneController.getRawAxis(1),
            () -> -driverOneController.getRawAxis(0),
            () -> -driverOneController.getRawAxis(4) / 1.5));

    // Lock to 0 degrees when A button is held
    alignToGoalButton
        .and(LaunchFuelOveride.negate())
        .whileTrue(
            DriveCommands.joystickDriveAtAngle(
                drive,
                () -> -driverOneController.getLeftY(),
                () -> -driverOneController.getLeftX(),
                () -> {
                  try {
                    var az = launcher.Launch(drive, LaunchFuelOveride.getAsBoolean());
                    return az != null ? az : Rotation2d.kZero;
                  } catch (Exception e) {
                    Logger.recordOutput("LaunchSupplier/Error", e.toString());
                    return Rotation2d.kZero;
                  }
                }));

    alignToGoalButton
        .and(LaunchFuelOveride)
        .whileTrue(
            DriveCommands.joystickDriveAtAngle(
                drive,
                () -> -driverOneController.getLeftY(),
                () -> -driverOneController.getLeftX(),
                () -> {
                  try {
                    var az = launcher.Launch(drive, LaunchFuelOveride.getAsBoolean());
                    return az != null ? az : Rotation2d.kZero;
                  } catch (Exception e) {
                    Logger.recordOutput("LaunchSupplier/Error", e.toString());
                    return Rotation2d.kZero;
                  }
                }));
    // Switch to X pattern when X button is pressed
    xOutButton.onTrue(Commands.runOnce(drive::stopWithX, drive));
    // xOutButton.onTrue(DriveCommands.DriveToNeutralZone());

    // Reset gyro to 0° when B button is pressed
    resetIMUButton.onTrue(
        Commands.runOnce(
                () -> drive.setPose(new Pose2d(drive.getPose().getTranslation(), Rotation2d.kZero)),
                drive)
            .ignoringDisable(true));

    IntakeStowed.onTrue(((m_intake.setAngle(Constants.IntakeConstants.Pivot.stowedPosition))));
    IntakeDownButton.onTrue(((m_intake.goToIntakePosition())));

    ToggleIntake.onTrue(m_intake.intake());
    ToggleIntake.onFalse(m_intake.stopRoller());
    JitterIntake.onTrue(m_intake.jitterIntake());

    Trigger allianceAutoRev =
        new Trigger(
            () ->
                DriverStation.isTeleopEnabled() && (Helpers.isPoseInAllianceZone(drive.getPose())));

    // allianceAutoRev.and(LaunchFuel2.negate()).whileTrue(launcher.runAllianceAutoControl(drive));
    allianceAutoRev.and(LaunchFuelOveride).whileTrue(launcher.worldsAutoRev(drive, true));
    allianceAutoRev.and(LaunchFuelOveride.negate()).whileTrue(launcher.worldsAutoRev(drive, false));
    Logger.recordOutput(
        "Passing Target Position",
        launcher.getTargetPostition(drive, Helpers.getYCoordinate(drive.getPose())));
    Logger.recordOutput(
        "we are on top fr this time",
        (Helpers.getYCoordinate(drive.getPose())
            > AprilTagFieldLayout.loadField(AprilTagFields.k2026RebuiltWelded).getFieldWidth()
                / 2));
    allianceAutoRev.negate().and(LaunchFuelOveride).whileTrue(launcher.worldsAutoRev(drive, true));
    allianceAutoRev
        .negate()
        .and(LaunchFuelOveride.negate())
        .whileTrue(launcher.worldsAutoRev(drive, false));

   

    Trigger launchRequest = LaunchFuel.or(LaunchFuel.and(LaunchFuelOveride));
    Logger.recordOutput("Launch Requested", launchRequest.getAsBoolean());
    Logger.recordOutput("is Launch Ready", launcher.isLaunchReady());
    if (Constants.currentMode == Mode.SIM) {
      Trigger simLaunchTrigger =
          new Trigger(
              () ->
                  Constants.currentMode == Mode.SIM
                          && launchRequest.getAsBoolean()
                          && (launcher.isLaunchReady())
                      || LaunchFuelOveride.getAsBoolean());
      simLaunchTrigger.onTrue(Commands.runOnce(this::LaunchFuelSim));
    }

    launchRequest.whileTrue(
        Commands.waitUntil(() -> launcher.isLaunchReady())
            .andThen(Column.HoldLaunchWithRamp().alongWith(BeltDexter.LaunchFuel())));

    launchRequest.whileFalse(
        Column.IdleReverse()
            .alongWith(BeltDexter.HoldIdleSpin())
            .andThen(Column.ResetVoltageRampDownLaunch()));

    // (beltIntakeButton.or(beltOuttakeButton))
    //     .whileFalse(Column.NoFuel().alongWith(BeltDexter.NoFuel()));

    // launcherVelocityButton.whileTrue(launcher.setVelocity(RPM.of(-5000)));

    IntakeUp.onTrue(m_intake.setAngle(Constants.IntakeConstants.Pivot.stowedPosition));

    LaunchNudgeUp.onTrue(
        new InstantCommand(
            () -> {
              launcher.shotCalc.adjustOffset(25);
            }));
    LaunchNudgeDown.onTrue(
        new InstantCommand(
            () -> {
              launcher.shotCalc.adjustOffset(-25);
            }));

    // ClimbUp.whileTrue(leftClimb.goUp().alongWith(rightClimb.goUp()));
    // ClimbDown.onTrue(leftClimb.goDown().alongWith(rightClimb.goDown()));
    // ClimbDown.or(ClimbUp).whileFalse(leftClimb.No().alongWith(rightClimb.No()));

    // RClimbUp.whileTrue(rightClimb.goUp());
    // RClimbDown.onTrue(rightClimb.goDown());
    // RClimbDown.or(RClimbUp).whileFalse(rightClimb.No());

    // var speedTrigger = launcher.isAtSpeed();
    // speedTrigger.whileTrue(
    //     Commands.run(() -> driverOneController.setRumble(RumbleType.kBothRumble, 0.5)));
    // speedTrigger.whileFalse(
    //     Commands.run(() -> driverOneController.setRumble(RumbleType.kBothRumble, 0)));

    // // Quasistatic Turn Tests
    // testController.button(1).whileTrue(drive.sysIdTurnQuasistatic(SysIdRoutine.Direction.kForward));
    // testController.button(2).whileTrue(drive.sysIdTurnQuasistatic(SysIdRoutine.Direction.kReverse));
    // // Dynamic Turn Tests
    // testController.button(3).whileTrue(drive.sysIdTurnDynamic(SysIdRoutine.Direction.kForward));
    // testController.button(4).whileTrue(drive.sysIdTurnDynamic(SysIdRoutine.Direction.kReverse));

    // Toggle which SysId test is bound to buttons 1-4
    // Quasistatic Drive tests
    // testController.button(1).whileTrue(drive.sysIdQuasistatic(SysIdRoutine.Direction.kForward));
    // testController.button(2).whileTrue(drive.sysIdQuasistatic(SysIdRoutine.Direction.kReverse));

    // Dynamic Drivetests
    // testController.button(3).whileTrue(drive.sysIdDynamic(SysIdRoutine.Direction.kForward));
    // testController.button(4).whileTrue(drive.sysIdDynamic(SysIdRoutine.Direction.kReverse));

    // Quasistatic Rotation tests
    // testController
    //     .button(1)
    //     .whileTrue(drive.sysIdRotationQuasistatic(SysIdRoutine.Direction.kForward));
    // testController
    //     .button(2)
    //     .whileTrue(drive.sysIdRotationQuasistatic(SysIdRoutine.Direction.kReverse));

    // Dynamic Rotation tests
    // testController.button(3).whileTrue(drive.sysIdRotationDynamic(SysIdRoutine.Direction.kForward));
    // testController.button(4).whileTrue(drive.sysIdRotationDynamic(SysIdRoutine.Direction.kReverse));

    // Quasistatic Rotation tests
    testController
        .button(1)
        .whileTrue(BeltDexter.sysIdQuasistatic(SysIdRoutine.Direction.kForward));
    testController
        .button(2)
        .whileTrue(BeltDexter.sysIdQuasistatic(SysIdRoutine.Direction.kReverse));

    // Dynamic Rotation tests
    testController.button(3).whileTrue(BeltDexter.sysIdDynamic(SysIdRoutine.Direction.kForward));
    testController.button(4).whileTrue(BeltDexter.sysIdDynamic(SysIdRoutine.Direction.kReverse));

    // testController.button(7).whileTrue(DriveCommands.wheelRadiusCharacterization(drive));
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    return autoChooser.get();
  }

  public void SimulationInit() {
    ballSim = new FuelPhysicsSim("Sim/Fuel");
    ballSim.enable();
    ballSim.placeFieldBalls();
    ballSim.configureRobot(
        Constants.RobotConstants.BumperWidth,
        Constants.RobotConstants.BumperLength,
        Constants.RobotConstants.BumperHeight,
        () -> drive.getPose(),
        () -> drive.getChassisSpeeds());
  }

  public void SimulationPeriodic() {
    Logger.recordOutput(
        "Target RPM",
        launcher.calculateShotToTarget(
            drive, launcher.getTargetPostition(drive, drive.getPose().getY())));
    Logger.recordOutput("Launch Override?", LaunchFuelOveride.getAsBoolean());
    ballSim.tick();
    Helpers.getYCoordinate(drive.getPose());
    Logger.recordOutput(
        "Target Position",
        launcher.getTargetPostition(drive, Helpers.getYCoordinate(drive.getPose())));
  }

  public void LaunchFuelSim() {
    // Get launcher parameters
    double slipFactor = Constants.LauncherConstants.params.slipFactor();
    double wheelDiameterM = Constants.LauncherConstants.params.wheelDiameterM();
    double launchAngleDeg = Constants.LauncherConstants.params.fixedLaunchAngleDeg();
    double exitHeightM = Constants.LauncherConstants.params.exitHeightM();

    // Get target launch RPM from ShotCalculator (not current velocity)
    Rotation2d azimuth;
    try {
      azimuth = launcher.Launch(drive, LaunchFuelOveride.getAsBoolean());
      if (azimuth == null) azimuth = Rotation2d.kZero;
    } catch (Exception e) {
      Logger.recordOutput("LaunchSim/Error", e.toString());
      azimuth = Rotation2d.kZero;
    }
    double targetLauncherRPM = launcher.getTargetLaunchRPM();

    Logger.recordOutput("Launch/TargetRPM", targetLauncherRPM);
    Logger.recordOutput("Launch/Azimuth", azimuth.getDegrees());

    double exitSpeed = slipFactor * targetLauncherRPM * Math.PI * wheelDiameterM / 60.0;
    double launchRad = Math.toRadians(launchAngleDeg);

    double vHorizontal = exitSpeed * Math.cos(launchRad);
    double vVertical = exitSpeed * Math.sin(launchRad);

    Logger.recordOutput("Launch/ExitSpeed", exitSpeed);
    Logger.recordOutput("Launch/VHorizontal", vHorizontal);
    Logger.recordOutput("Launch/VVertical", vVertical);

    // Apply azimuth rotation to horizontal velocity
    double vx = vHorizontal * azimuth.getCos();
    double vy = vHorizontal * azimuth.getSin();

    // Calculate launcher position in field frame
    Pose2d robotPose = drive.getPose();
    Translation2d launcherOffset =
        new Translation2d(
            Constants.LauncherConstants.LAUNCHER_OFFSET_X,
            Constants.LauncherConstants.LAUNCHER_OFFSET_Y);
    Translation2d launcherPos2d =
        robotPose.getTranslation().plus(launcherOffset.rotateBy(robotPose.getRotation()));
    Translation3d launchPos =
        new Translation3d(launcherPos2d.getX(), launcherPos2d.getY(), exitHeightM);
    Translation3d launchVel = new Translation3d(vx, vy, vVertical);

    Logger.recordOutput("Launch/Position", launchPos);
    Logger.recordOutput("Launch/Velocity", launchVel);
    ballSim.launchBall(launchPos, launchVel, targetLauncherRPM);
    ballSim.launchBall(
        launchPos.plus(new Translation3d(0, Constants.LauncherConstants.LAUNCHER_TUBE_SPACING, 0)),
        launchVel,
        launchVel);
    ballSim.launchBall(
        launchPos.minus(new Translation3d(0, Constants.LauncherConstants.LAUNCHER_TUBE_SPACING, 0)),
        launchVel,
        launchVel);
  }

  /**
   * Helper method to launch 3 fuel in simulation continuously. Called by launch commands to eject
   * fuel as long as fire button is held. Launches 3 fuel every ~100ms to simulate continuous
   * feeding from hopper.
   */
}
