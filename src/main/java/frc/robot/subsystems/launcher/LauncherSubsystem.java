// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.launcher;

import static edu.wpi.first.units.Units.Amps;

import edu.wpi.first.math.filter.Debouncer;
import edu.wpi.first.wpilibj.Alert;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Robot;
import org.littletonrobotics.junction.Logger;

public class LauncherSubsystem extends SubsystemBase {
  private final LauncherIO io;
  private final LauncherIOInputsAutoLogged inputs = new LauncherIOInputsAutoLogged();

  private final Debouncer motorLeadConnectedDebouncer =
      new Debouncer(0.5, Debouncer.DebounceType.kFalling);
  private final Debouncer motorFollower0ConnectedDebouncer =
      new Debouncer(0.5, Debouncer.DebounceType.kFalling);
  private final Debouncer motorFollower1ConnectedDebouncer =
      new Debouncer(0.5, Debouncer.DebounceType.kFalling);

  private final Alert motorLeadDisconnected;
  private final Alert motorFollower0Disconnected;
  private final Alert motorFollower1Disconnected;

  /** Creates a new LauncherSubsystem. */
  public LauncherSubsystem(LauncherIO io) {
    this.io = io;

    motorLeadDisconnected = new Alert("Flywheel Lead Motor Disconnected!", Alert.AlertType.kError);
    motorFollower0Disconnected =
        new Alert("Flywheel Follower 0 Motor Disconnected!", Alert.AlertType.kError);
    motorFollower1Disconnected =
        new Alert("Flywheel Follower 1 Motor Disconnected!", Alert.AlertType.kError);
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    io.updateInputs(inputs);
    Logger.processInputs("Launcher", inputs);

    motorLeadDisconnected.set(
        Robot.showHardwareAlerts() && !motorLeadConnectedDebouncer.calculate(inputs.connected0));
    motorFollower0Disconnected.set(
        Robot.showHardwareAlerts()
            && !motorFollower0ConnectedDebouncer.calculate(inputs.connected1));
    motorFollower1Disconnected.set(
        Robot.showHardwareAlerts()
            && !motorFollower1ConnectedDebouncer.calculate(inputs.connected2));

    // Log battery usage
    if (Robot.batteryLogger != null) {
      double motor0Current = inputs.connected0 ? inputs.supplyCurrentMotor0.in(Amps) : 0.0;
      double motor1Current = inputs.connected1 ? inputs.supplyCurrentMotor1.in(Amps) : 0.0;
      double motor2Current = inputs.connected2 ? inputs.supplyCurrentMotor2.in(Amps) : 0.0;

      Robot.batteryLogger.reportCurrentUsage("Launcher/Motor0", false, motor0Current);
      Robot.batteryLogger.reportCurrentUsage("Launcher/Motor1", false, motor1Current);
      Robot.batteryLogger.reportCurrentUsage("Launcher/Motor2", false, motor2Current);
      Robot.batteryLogger.reportCurrentUsage(
          "Launcher", false, motor0Current + motor1Current + motor2Current);
    }
  }
}
