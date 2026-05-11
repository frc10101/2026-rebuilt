// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.intake.IntakePivot;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Celsius;
import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.RPM;
import static edu.wpi.first.units.Units.Volts;

import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Temperature;
import edu.wpi.first.units.measure.Voltage;
import org.littletonrobotics.junction.AutoLog;

/** Add your docs here. */
public interface IntakePivotIO {
  @AutoLog
  public static class IntakePivotIOInputs {
    public boolean connected = false;
    public AngularVelocity velocity = RPM.zero();
    public Angle angle = Degrees.of(0);
    public Voltage appliedVoltage = Volts.of(0);
    public Current supplyCurrent = Amps.of(0);
    public Current statorCurrent = Amps.of(0);
    public Temperature temperature = Celsius.of(0);
    public Angle targetAngle = Degrees.of(0);
  }

  default void updateInputs(IntakePivotIOInputs inputs) {}

  default void setTargetAngle(Angle angle) {}

  default void stop() {}
}
