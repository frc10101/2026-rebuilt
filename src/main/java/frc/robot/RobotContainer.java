// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot;

import static edu.wpi.first.units.Units.RPM;

import com.pathplanner.lib.auto.AutoBuilder;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import frc.robot.Constants.ClimbConstants;
import frc.robot.commands.DriveCommands;
import frc.robot.generated.TunerConstants;
import frc.robot.subsystems.Feeder;
import frc.robot.subsystems.Indexer;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Launcher;
import frc.robot.subsystems.climb.Climb;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.drive.GyroIO;
import frc.robot.subsystems.drive.GyroIOPigeon2;
import frc.robot.subsystems.drive.ModuleIO;
import frc.robot.subsystems.drive.ModuleIOSim;
import frc.robot.subsystems.drive.ModuleIOTalonFX;
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
  // private final Vision vision;
  private final Launcher launcher;
  private final Intake m_intake;
  private final Feeder Column;
  private final Indexer BeltDexter;
  private final Climb leftClimb;
  private final Climb rightClimb;

  // Controller
  private final CommandXboxController driverOneController = new CommandXboxController(0);
  private final CommandXboxController driverTwoController = new CommandXboxController(1);
  private final CommandXboxController testController = new CommandXboxController(2);

  // Buttons tehe
  // Driver One A Button
  private final Trigger alignToZeroButton = driverOneController.button(1);

  // Driver One Options Button
  private final Trigger resetIMUButton = driverOneController.button(8);

  // Driver One X Button
  private final Trigger xOutButton = driverOneController.button(3);

  // Driver Two Right Trigger Button
  private final Trigger launcherVelocityButton = driverTwoController.axisGreaterThan(3, 0.3);

  // Driver Twp Left Bumper
  private final Trigger beltIntakeButton = driverTwoController.button(5);

  // Driver Two Right Bumper
  private final Trigger beltOuttakeButton = driverTwoController.button(6);

  // Driver Two B Button
  private final Trigger lowerIntakeButton = driverTwoController.button(2);

  // Driver Two X Button
  private final Trigger stowIntakeButton = driverTwoController.button(3);

  // Driver Two Y Button
  private final Trigger jitterIntakeButton = driverTwoController.button(4);

  // Driver Two Left Trigger
  private final Trigger intakeFuelButton = driverTwoController.axisGreaterThan(2, 0.3);

  // Driver Two Dpad Up
  private final Trigger climbUp = driverTwoController.pov(0);

  // Driver Two Dpad Down
  private final Trigger climbDown = driverTwoController.pov(180);

  // Dashboard inputs
  private final LoggedDashboardChooser<Command> autoChooser;

  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {
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
        leftClimb = new Climb("Left", Constants.SparkMaxCanIDs.ClimbLeftMotor);
        rightClimb = new Climb("Right", Constants.SparkMaxCanIDs.ClimbRightMotor);
        launcher = new Launcher();
        // vision = new Vision(drive::addVisionMeasurement,
        // VisionIOPhotonVision.createAllCameras());
        m_intake = new Intake();
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
        leftClimb = new Climb("Left", Constants.SparkMaxCanIDs.ClimbLeftMotor);
        rightClimb = new Climb("Right", Constants.SparkMaxCanIDs.ClimbRightMotor);
        launcher = new Launcher();
        /*vision =
        new Vision(
            drive::addVisionMeasurement,
            VisionIOPhotonVisionSim.createAllSimCameras(drive::getPose));
        */
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
        leftClimb = new Climb("Left", Constants.SparkMaxCanIDs.ClimbLeftMotor);
        rightClimb = new Climb("Right", Constants.SparkMaxCanIDs.ClimbRightMotor);
        launcher = new Launcher();
        // vision = new Vision(drive::addVisionMeasurement);
        m_intake = new Intake();
        break;
    }

    // Set up auto routines
    autoChooser = new LoggedDashboardChooser<>("Auto Choices", AutoBuilder.buildAutoChooser());

    // Set up SysId routines
    autoChooser.addOption(
        "Drive Wheel Radius Characterization", DriveCommands.wheelRadiusCharacterization(drive));
    autoChooser.addOption(
        "Drive Simple FF Characterization", DriveCommands.feedforwardCharacterization(drive));
    autoChooser.addOption(
        "Drive SysId (Quasistatic Forward)",
        drive.sysIdQuasistatic(SysIdRoutine.Direction.kForward));
    autoChooser.addOption(
        "Drive SysId (Quasistatic Reverse)",
        drive.sysIdQuasistatic(SysIdRoutine.Direction.kReverse));
    autoChooser.addOption(
        "Drive SysId (Dynamic Forward)", drive.sysIdDynamic(SysIdRoutine.Direction.kForward));
    autoChooser.addOption(
        "Drive SysId (Dynamic Reverse)", drive.sysIdDynamic(SysIdRoutine.Direction.kReverse));
    // autoChooser.addOption("AUTOTEST", );

    // Configure the button bindings

    configureButtonBindings();
  }

  /**
   * Use this method to define your button->command mappings. Buttons can be created by
   * instantiating a {@link GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a {@link
   * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  private void configureButtonBindings() {
    // m_intake.setDefaultCommand(m_intake.setAngle(Degrees.of(90)));

    // Default command, normal field-relative drive
    drive.setDefaultCommand(
        DriveCommands.joystickDrive(
            drive,
            () -> -driverOneController.getRawAxis(1),
            () -> -driverOneController.getRawAxis(0),
            () -> -driverOneController.getRawAxis(4)));

    // Lock to 0 degrees when A button is held
    alignToZeroButton.whileTrue(
        DriveCommands.joystickDriveAtAngle(
            drive,
            () -> -driverOneController.getLeftY(),
            () -> -driverOneController.getLeftX(),
            () -> Rotation2d.kZero));

    // Switch to X pattern when X button is pressed
    xOutButton.onTrue(Commands.runOnce(drive::stopWithX, drive));

    // Reset gyro to 0° when B button is pressed
    resetIMUButton.onTrue(
        Commands.runOnce(
                () -> drive.setPose(new Pose2d(drive.getPose().getTranslation(), Rotation2d.kZero)),
                drive)
            .ignoringDisable(true));

    stowIntakeButton.onTrue(((m_intake.setAngle(Constants.IntakeConstants.Pivot.stowedPosition))));
    lowerIntakeButton.onTrue(((m_intake.goToIntakePosition())));

    intakeFuelButton.onTrue(m_intake.toggleIntake());
    jitterIntakeButton.onTrue(m_intake.jitterIntake());

    beltIntakeButton.whileTrue(Column.IntakeFuel().alongWith(BeltDexter.IntakeFuel()));
    beltOuttakeButton.whileTrue(Column.OuttakeFuel());
    (beltIntakeButton.or(beltOuttakeButton))
        .whileFalse(Column.NoFuel().alongWith(BeltDexter.NoFuel()));

    launcherVelocityButton.whileTrue(launcher.setVelocity(RPM.of(-4000)));
    launcherVelocityButton.whileFalse(launcher.set(0));

    climbUp.onTrue(
        leftClimb
            .GoToHeight(ClimbConstants.PreHangExtension)
            .alongWith(rightClimb.GoToHeight(ClimbConstants.PreHangExtension)));
    climbDown.onTrue(
        leftClimb
            .GoToHeight(ClimbConstants.RestDistance)
            .alongWith(rightClimb.GoToHeight(ClimbConstants.RestDistance)));

    var speedTrigger = launcher.isAtSpeed();
    speedTrigger.whileTrue(
        Commands.run(() -> driverOneController.setRumble(RumbleType.kBothRumble, 0.5)));
    speedTrigger.whileFalse(
        Commands.run(() -> driverOneController.setRumble(RumbleType.kBothRumble, 0)));

    // Quasistatic tests
    // testController.button(1).whileTrue(drive.sysIdQuasistatic(SysIdRoutine.Direction.kForward));
    // testController.button(2).whileTrue(drive.sysIdQuasistatic(SysIdRoutine.Direction.kReverse));

    // Dynamic tests
    // testController.button(3).whileTrue(drive.sysIdDynamic(SysIdRoutine.Direction.kForward));
    // testController.button(4).whileTrue(drive.sysIdDynamic(SysIdRoutine.Direction.kReverse));

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
}
