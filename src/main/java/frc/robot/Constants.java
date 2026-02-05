// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.DegreesPerSecond;
import static edu.wpi.first.units.Units.DegreesPerSecondPerSecond;
import static edu.wpi.first.units.Units.Feet;
import static edu.wpi.first.units.Units.Inches;
import static edu.wpi.first.units.Units.Meters;
import static edu.wpi.first.units.Units.Pounds;
import static edu.wpi.first.units.Units.Second;
import static edu.wpi.first.units.Units.Seconds;
import static edu.wpi.first.units.Units.Volts;

import edu.wpi.first.units.VoltageUnit;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularAcceleration;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.units.measure.Mass;
import edu.wpi.first.units.measure.Time;
import edu.wpi.first.units.measure.Velocity;
import edu.wpi.first.units.measure.Voltage;
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

  public final class IDs {
    public static final int intakePivotMotor = 100;
    public static final int intakeRollerMotor = 200;
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

  public final class IntakeConstants {
    public final class Pivot {
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
        public static final double kp = 1.0;
        public static final double ki = 0.75;
        public static final double kd = 0.25;
        public static final AngularVelocity maxVelocity = DegreesPerSecond.of(90);
        public static final AngularAcceleration maxAcceleration = DegreesPerSecondPerSecond.of(45);

        public static final double ks = 0.01;
        public static final double kg = 0.5; // Gravity compensation for arm
        public static final double kv = 0.1;
      }

      // Gears
      // Write down gear stages from motor to output
      // Example: 3:1 stage followed by 4:1 stage -> gearStages = {3, 4}
      // If you gear up instead, use fractions like 1/3.0
      public static final double[] gearStages = {3, 4};
      public static final double totalGear =
          java.util.Arrays.stream(gearStages).reduce(1, (a, b) -> a * b);

      // Motor properties from tutorial to prevent over currenting
      public static final Current currentLimit = Amps.of(40);
      public static final Time closedLoopRampRate = Seconds.of(0);
      public static final Time openLoopRampRate = Seconds.of(0.25);

      // Pivot mechanism constraints
      public static final Angle softLimitOne = Degrees.of(-20);
      public static final Angle softLimitTwo = Degrees.of(10);
      public static final Angle hardLimitOne = Degrees.of(-30);
      public static final Angle hardLimitTwo = Degrees.of(40);
      public static final Angle startingPosition = Degrees.of(-5);
      public static final Distance armLength = Feet.of(3);
      public static final Mass mass = Pounds.of(1);

      // sys Id stuff
      public static final Voltage maxVoltage = Volts.of(7);
      public static final Velocity<VoltageUnit> stepVoltage = Volts.of(2).per(Second);
      public static final Time sysIdDuration = Seconds.of(4);
    }

    public final class Roller {
      public static final double rollerRatio = 1; // feet per second
      public static final double smartCurrentLimit = 50;
      public static final double kp = 0;
      public static final double ki = 0;
      public static final double kd = 0;
      public static final double FF = 1.0 / 5767;
      public static final AngularVelocity maxVelocity = DegreesPerSecond.of(90);
      public static final AngularAcceleration maxAcceleration = DegreesPerSecondPerSecond.of(45);
      public static final double[] gearStages = {1};
      public static final double totalGear =
          java.util.Arrays.stream(gearStages).reduce(1, (a, b) -> a * b);

      public static final Current currentLimit = Amps.of(40);
      public static final Time closedLoopRampRate = Seconds.of(0);
      public static final Time openLoopRampRate = Seconds.of(0.25);

      // Roller speeds (duty cycle -1 to 1)
      public static final double intakeSpeed = 1.0;
      public static final double outtakeSpeed = -1.0;
    }
  }

  public static final class SparkMaxCanIDs {
    /** Column Can ID */
    public static final int ColumnMotor = 10;

    /** Climb Can IDs */
    public static final int ClimbMotor = 11;
  }

  public static final class ColumnConstants {
    /** Column Gear Ratio */
    public static final int gearRatio = 1;
    /** Column Stall Current Limit */
    public static final Current currentLimit = Amps.of(60);

    public static final double IntakeSpeed = 1.0;
    public static final double OuttakeSpeed = -0.5;

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

  public static final class ClimbConstants {
    /** Column Gear Ratio */
    public static final int gearRatio = 1;
    /** Column Stall Current Limit */
    public static final Current currentLimit = Amps.of(60);

    public static final Distance PreHangExtension = Inches.of(0);
    public static final Distance HangDistance = Inches.of(0);
    public static final Distance ReleaseDistance = Inches.of(0);
    public static final Distance RestDistance = Inches.of(0);

    public static final Distance MechanismCircumference =
        Meters.of(Inches.of(0.25).in(Meters) * 22);

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
