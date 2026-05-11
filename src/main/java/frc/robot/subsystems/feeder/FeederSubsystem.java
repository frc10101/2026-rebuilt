// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.feeder;

import static edu.wpi.first.units.Units.Amps;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.Robot;
import org.littletonrobotics.junction.Logger;

public class FeederSubsystem extends SubsystemBase {
  private final FeederIO IO;
  private final FeederIOInputsAutoLogged inputs = new FeederIOInputsAutoLogged();

  /** Creates a new FeederSubsystem. */
  public FeederSubsystem(FeederIO IO) {
    this.IO = IO;
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    IO.updateInputs(inputs);
    Logger.processInputs("Feeder", inputs);

    // Log battery usage
    if (Robot.batteryLogger != null) {
      Robot.batteryLogger.reportCurrentUsage(
          "Feeder", false, inputs.connected ? inputs.supplyCurrent.in(Amps) : 0.0);
    }
  }

  public Command Launch() {
    return runOnce(() -> IO.setVoltage(Constants.ColumnConstants.FirstLaunchSpeed))
        .withName("Feeder.Outtake");
  }

  public Command IdleReverse() {
    return runOnce(() -> IO.setVoltage(Constants.ColumnConstants.IdleReverseSpeed))
        .withName("Feeder.IdleReverse");
  }

  public Command stopRoller() {
    return runOnce(() -> IO.stop()).withName("Feeder.Stop");
  }
}
