// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.intake;

import static edu.wpi.first.units.Units.Amps;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.Robot;
import frc.robot.subsystems.intake.IntakePivot.IntakePivotIO;
import frc.robot.subsystems.intake.IntakePivot.IntakePivotIOInputsAutoLogged;
import frc.robot.subsystems.intake.IntakeRoller.IntakeRollerIO;
import frc.robot.subsystems.intake.IntakeRoller.IntakeRollerIOInputsAutoLogged;
import org.littletonrobotics.junction.Logger;

public class IntakeSubsystem extends SubsystemBase {
  private final IntakePivotIO pivotIO;
  private final IntakePivotIOInputsAutoLogged pivotInputs = new IntakePivotIOInputsAutoLogged();

  private final IntakeRollerIO rollerIO;
  private final IntakeRollerIOInputsAutoLogged rollerInputs = new IntakeRollerIOInputsAutoLogged();

  /** Creates a new IntakeSubsystem. */
  public IntakeSubsystem(IntakePivotIO pivotIO, IntakeRollerIO rollerIO) {
    this.pivotIO = pivotIO;
    this.rollerIO = rollerIO;
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    pivotIO.updateInputs(pivotInputs);
    rollerIO.updateInputs(rollerInputs);
    Logger.processInputs("pivot", pivotInputs);
    Logger.processInputs("roller", rollerInputs);

    // Log battery usage
    if (Robot.batteryLogger != null) {
      double pivotCurrent = pivotInputs.connected ? pivotInputs.supplyCurrent.in(Amps) : 0.0;
      double rollerCurrent = rollerInputs.connected ? rollerInputs.supplyCurrent.in(Amps) : 0.0;

      Robot.batteryLogger.reportCurrentUsage("Intake/Pivot", false, pivotCurrent);
      Robot.batteryLogger.reportCurrentUsage("Intake/Roller", false, rollerCurrent);
      Robot.batteryLogger.reportCurrentUsage("Intake", false, pivotCurrent + rollerCurrent);
    }
  }

  /*-----------------------------------------------------*/
  /*--------------------Pivot Commands-------------------*/
  /*-----------------------------------------------------*/

  public Command pivotDown() {
    return runOnce(() -> pivotIO.setTargetAngle(Constants.IntakeConstants.Pivot.intakePosition))
        .withName("Pivot.Down");
  }

  public Command pivotStowed() {
    return runOnce(() -> pivotIO.setTargetAngle(Constants.IntakeConstants.Pivot.stowedPosition))
        .withName("Pivot.Stowed");
  }

  public Command pivotJitter() {
    return runOnce(() -> pivotIO.setTargetAngle(Constants.IntakeConstants.Pivot.jitterPosition))
        .withTimeout(0.5)
        .andThen(
            runOnce(() -> pivotIO.setTargetAngle(Constants.IntakeConstants.Pivot.intakePosition))
                .withTimeout(0.1))
        .withTimeout(.25)
        .withName("Pivot.Jitter");
  }

  public Command stopPivot() {
    return runOnce(() -> pivotIO.stop()).withName("Pivot.Stop");
  }

  /*-----------------------------------------------------*/
  /*--------------------Intake Commands------------------*/
  /*-----------------------------------------------------*/

  public Command rollerIntake() {
    return runOnce(() -> rollerIO.setTargetVelocity(Constants.IntakeConstants.Roller.intakeSpeed))
        .withName("Roller.Intake");
  }

  public Command rollerOuttake() {
    return runOnce(() -> rollerIO.setTargetVelocity(Constants.IntakeConstants.Roller.outtakeSpeed))
        .withName("Roller.Outtake");
  }

  public Command stopRoller() {
    return runOnce(() -> rollerIO.stop()).withName("Roller.Stop");
  }
}
