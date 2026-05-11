// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot;

import com.pathplanner.lib.auto.NamedCommands;
import com.pathplanner.lib.commands.PathPlannerAuto;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.commands.DriveCommands;
import frc.robot.generated.TunerConstants;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.drive.GyroIO;
import frc.robot.subsystems.drive.GyroIOPigeon2;
import frc.robot.subsystems.drive.ModuleIO;
import frc.robot.subsystems.drive.ModuleIOSim;
import frc.robot.subsystems.drive.ModuleIOTalonFX;
import frc.robot.util.Helpers;
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

  // Controller
  private final CommandXboxController driverOneController = new CommandXboxController(0);

  // Buttons tehe
  // Driver One A Button
  private final Trigger alignToGoalButton = driverOneController.button(1);

  // Driver One Options Button
  private final Trigger resetIMUButton = driverOneController.button(8);

  // Driver One X Button
  private final Trigger xOutButton = driverOneController.button(3);

  // Driver One Right Trigger
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

        // NamedCommands.registerCommand(
        //     "LauncherSpinUp",
        //     // launcher
        //         // .changeDistanceType(LauncherState.AUTO)
        //         // .andThen(new InstantCommand(launcher::setVelocity)));

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
    // Default command, normal field-relative drive
    drive.setDefaultCommand(
        DriveCommands.joystickDrive(
            drive,
            () -> -driverOneController.getRawAxis(1),
            () -> -driverOneController.getRawAxis(0),
            () -> -driverOneController.getRawAxis(4) / 1.5));

    // Lock to 0 degrees when A button is held
    alignToGoalButton.whileTrue(
        DriveCommands.joystickDrive(
            drive,
            () -> -driverOneController.getLeftY(),
            () -> -driverOneController.getLeftX(),
            () -> 0));

    // Switch to X pattern when X button is pressed
    xOutButton.onTrue(Commands.runOnce(drive::stopWithX, drive));

    // Reset gyro to 0° when B button is pressed
    resetIMUButton.onTrue(Commands.runOnce(drive::stop, drive).ignoringDisable(true));
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
