// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot.util;

import java.util.HashMap;
import java.util.Map;
import org.littletonrobotics.junction.Logger;

/** Class for logging current, power, and energy usage. */
public class BatteryLogger {
  private double totalCurrent = 0.0;
  private double driveCurrent = 0.0;
  private double totalPower = 0.0;
  private double totalEnergy = 0.0;

  private double batteryVoltage = 12.6;
  private double rioCurrent = 0.0;

  private Map<String, Double> subsystemCurrents = new HashMap<>();
  private Map<String, Double> subsystemPowers = new HashMap<>();
  private Map<String, Double> subsystemEnergies = new HashMap<>();

  public void reportCurrentUsage(String key, boolean drive, double... amps) {
    double totalAmps = 0.0;
    for (double amp : amps) {
      totalAmps += Math.abs(amp);
    }
    if (drive) {
      driveCurrent += totalAmps;
    }

    double power = totalAmps * batteryVoltage;
    double energy = power * 0.02; // Assuming 50Hz loop

    totalCurrent += totalAmps;
    totalPower += power;
    totalEnergy += energy;

    subsystemCurrents.put(key, totalAmps);
    subsystemPowers.put(key, power);
    subsystemEnergies.merge(key, energy, Double::sum);

    // Aggregate by hierarchy
    String[] keys = key.split("/|-");
    if (keys.length < 2) {
      return;
    }

    String subkey = "";
    for (int i = 0; i < keys.length - 1; i++) {
      subkey += keys[i];
      if (i < keys.length - 2) {
        subkey += "/";
      }
      subsystemCurrents.merge(subkey, totalAmps, Double::sum);
      subsystemPowers.merge(subkey, power, Double::sum);
      subsystemEnergies.merge(subkey, energy, Double::sum);
    }
  }

  public void periodicAfterScheduler() {
    reportCurrentUsage("Controls/RoboRIO", false, rioCurrent);

    // Log total and subsystem energy usage
    Logger.recordOutput("BatteryLogger/TotalCurrent", totalCurrent);
    Logger.recordOutput("BatteryLogger/TotalPower", totalPower);
    Logger.recordOutput("BatteryLogger/TotalEnergy", joulesToWattHours(totalEnergy));
    Logger.recordOutput("BatteryLogger/DriveCurrent", driveCurrent);

    for (var entry : subsystemCurrents.entrySet()) {
      Logger.recordOutput("BatteryLogger/Current/" + entry.getKey(), entry.getValue());
      subsystemCurrents.put(entry.getKey(), 0.0);
    }
    for (var entry : subsystemPowers.entrySet()) {
      Logger.recordOutput("BatteryLogger/Power/" + entry.getKey(), entry.getValue());
      subsystemPowers.put(entry.getKey(), 0.0);
    }
    for (var entry : subsystemEnergies.entrySet()) {
      Logger.recordOutput(
          "BatteryLogger/Energy/" + entry.getKey(), joulesToWattHours(entry.getValue()));
    }

    resetTotals();
  }

  public void resetTotals() {
    totalPower = 0.0;
    totalCurrent = 0.0;
    driveCurrent = 0.0;
  }

  public double getTotalCurrent() {
    return totalCurrent;
  }

  public double getTotalPower() {
    return totalPower;
  }

  public double getTotalEnergy() {
    return totalEnergy;
  }

  public double getDriveCurrent() {
    return driveCurrent;
  }

  public void setBatteryVoltage(double voltage) {
    batteryVoltage = voltage;
  }

  public void setRioCurrent(double current) {
    rioCurrent = current;
  }

  private double joulesToWattHours(double joules) {
    return joules / 3600.0;
  }
}
