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

import edu.wpi.first.units.measure.AngularAcceleration;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
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

  public static final class SparkMaxCanIDs {
    /** Column Can ID */
    public static final int ColumnMotor = 10;
  }

  public static final class ColumnConstants {
    /** Column Gear Ratio */
    public static final int gearRatio = 1;
    /** Column Stall Current Limit */
    public static final Current currentLimit = Amps.of(60);

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
  }
}
