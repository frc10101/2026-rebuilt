// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.DegreesPerSecond;
import static edu.wpi.first.units.Units.DegreesPerSecondPerSecond;
import static edu.wpi.first.units.Units.Seconds;

import edu.wpi.first.units.measure.AngularAcceleration;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Time;
import edu.wpi.first.wpilibj.RobotBase;

/**
 * This class defines the runtime mode used by AdvantageKit. The mode is always "real" when running
 * on a roboRIO. Change the value of "simMode" to switch between "sim" (physics sim) and "replay"
 * (log replay from a file).
 */
public final class Constants {
  public static final Mode simMode = Mode.SIM;
  public static final Mode currentMode = RobotBase.isReal() ? Mode.REAL : simMode;

  public static enum Mode {
    /** Running on a real robot. */
    REAL,

    /** Running a physics simulator. */
    SIM,

    /** Replaying from a log file. */
    REPLAY
  }

  public final class Intake {
    public final class Real {
      public static final double kp = 50.0;
      public static final double ki = 0.0;
      public static final double kd = 0.0;
      public static final AngularVelocity maxVelocity = DegreesPerSecond.of(90);
      public static final AngularAcceleration maxAcceleration = DegreesPerSecondPerSecond.of(45);

      public static final double ks = 0.0;
      public static final double kg = 0.0;
      public static final double kv = 0.0;
    }

    public final class Sim {
      public static final double kp = 50.0;
      public static final double ki = 0.0;
      public static final double kd = 0.0;
      public static final AngularVelocity maxVelocity = DegreesPerSecond.of(90);
      public static final AngularAcceleration maxAcceleration = DegreesPerSecondPerSecond.of(45);

      public static final double ks = 0.0;
      public static final double kg = 0.0;
      public static final double kv = 0.0;
    }   

    // Gears
    // Write down gear stages from motor to output
    // Example: 3:1 stage followed by 4:1 stage -> gearStages = {3, 4}
    // If you gear up instead, use fractions like 1/3.0
    public static final int[] gearStages = {3, 4};
    public static final int totalGear = java.util.Arrays.stream(gearStages).reduce(1, (a, b) -> a * b);

    // Motor properties from tutorial to prevent over currenting
    public static final Current currentLimit = Amps.of(40);
    public static final Time closedLoopRampRate = Seconds.of(0.);
    public static final Time openLoopRampRate = Seconds.of(0.25);
  }

  public final class IDs {}
}
