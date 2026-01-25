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

  public static class Launcher {

    public static final int MOTOR_ID_LEAD = 10;
    public static final int MOTOR_ID_FOLLOW = 11;

    // Closed-loop gains (real robot)
    public static final double REAL_kP = 0.0;
    public static final double REAL_kI = 0.0;
    public static final double REAL_kD = 0.0;

    // Closed-loop gains (sim)
    public static final double SIM_kP = 0.91;
    public static final double SIM_kI = 0.1;
    public static final double SIM_kD = 0.0;

    // Feedforward (ks, kv, ka) used by SimpleMotorFeedforward
    public static final double FFW_kS = 0.0;
    public static final double FFW_kV = 0.39;
    public static final double FFW_kA = 0.58;

    // Motion constraints used in controllers (degrees/sec, degrees/sec^2)
    public static final double MAX_VELOCITY_DPS = 90.0;
    public static final double MAX_ACCEL_DPS2 = 45.0;

    // Telemetry names
    public static final String MOTOR_TELEMETRY_NAME = "LauncherMotor";
    public static final String MECH_TELEMETRY_NAME = "LauncherMech";

    // Mechanical / motor configuration
    public static final int GEARING = 1;
    public static final boolean MOTOR_INVERTED = false;
    public static final boolean FOLLOWER_INVERTED = true;
    public static final int MOTOR_COUNT = 2; // motors per side used for DCMotor factory

    // Electrical limits
    public static final double STATOR_CURRENT_LIMIT_AMPS = 40.0;

    // Geometry / mass properties
    public static final double DIAMETER_INCH = 3.965;
    public static final double MASS_GRAMS = 2314.288;
    // Moment of inertia about spin axis (kg*m^2) - from CAD Lzz converted
    public static final double MOI_KG_M2 = 0.04180177;

    // Soft limits (RPM)
    public static final double SOFT_LIMIT_RPM = 5000.0;
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
