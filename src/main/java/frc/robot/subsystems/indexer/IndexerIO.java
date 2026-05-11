// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.indexer;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Celsius;
import static edu.wpi.first.units.Units.RPM;
import static edu.wpi.first.units.Units.Volts;

import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Temperature;
import edu.wpi.first.units.measure.Voltage;
import org.littletonrobotics.junction.AutoLog;

/** Add your docs here. */
public interface IndexerIO {
  @AutoLog
  public static class IndexerIOInputs {
    public boolean connected = false;
    public AngularVelocity velocity = RPM.of(0);
    public Voltage appliedVoltage = Volts.of(0);
    public Current supplyCurrent = Amps.of(0);
    public Current statorCurrent = Amps.of(0);
    public Temperature temperature = Celsius.of(0);
  }

  default void updateInputs(IndexerIOInputs inputs) {}

  default void setTargetVelocity(AngularVelocity velocity) {}

  default void setVoltage(Voltage volts) {}

  default void stop() {}
}
