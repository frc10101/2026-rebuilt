// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.indexer;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Volts;

import edu.wpi.first.units.measure.Voltage;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Robot;
import org.littletonrobotics.junction.Logger;

public class IndexerSubsystem extends SubsystemBase {
  private final IndexerIO IO;
  private final IndexerIOInputsAutoLogged inputs = new IndexerIOInputsAutoLogged();
  private Voltage lastCommandedVoltage = Volts.of(0);

  /** Creates a new IndexerSubsystem. */
  public IndexerSubsystem(IndexerIO IO) {
    this.IO = IO;
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    IO.updateInputs(inputs);
    Logger.processInputs("Indexer", inputs);
    Logger.recordOutput("Indexer/TargetVoltage", lastCommandedVoltage);

    // Log battery usage
    if (Robot.batteryLogger != null) {
      Robot.batteryLogger.reportCurrentUsage(
          "Indexer", false, inputs.connected ? inputs.supplyCurrent.in(Amps) : 0.0);
    }
  }

  private void setTargetVoltage(Voltage voltage) {
    lastCommandedVoltage = voltage;
    IO.setVoltage(voltage);
    ;
  }

  public Command Intake() {
    return runOnce(() -> setTargetVoltage(Volts.of(6))).withName("Indexer.Intake");
  }

  public Command Idle() {
    return runOnce(() -> setTargetVoltage(Volts.of(4))).withName("Indexer.Idle");
  }

  public Command Launch() {
    return runOnce(() -> setTargetVoltage(Volts.of(8))).withName("Indexer.Launch");
  }

  public Command Outtake() {
    return runOnce(() -> setTargetVoltage(Volts.of(-6))).withName("Indexer.Outtake");
  }
}
