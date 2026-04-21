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
import static edu.wpi.first.units.Units.RPM;
import static edu.wpi.first.units.Units.Second;
import static edu.wpi.first.units.Units.Seconds;
import static edu.wpi.first.units.Units.Volts;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation2d;
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
import frc.robot.util.Launcher.ProjectileSimulator;

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

  public static class LauncherConstants {

    public static final int MOTOR_ID_LEAD = 20;
    public static final int MOTOR_ID_FOLLOW0 = 21;
    public static final int MOTOR_ID_FOLLOW1 = 22;

    // Closed-loop gains (real robot)
    public static final double REAL_kP = 0.1; // 0.18216 or 0.45
    public static final double REAL_kI = 0.0; // 0.1
    public static final double REAL_kD = 0.0;

    // Closed-loop gains (sim)
    public static final double SIM_kP = 0.91;
    public static final double SIM_kI = 0.1;
    public static final double SIM_kD = 0.0;

    // Feedforward (ks, kv, ka) used by SimpleMotorFeedforward
    public static final double FFW_kS = 0.2196;
    public static final double FFW_kV = 0.11953;
    public static final double FFW_kA = 0.013135;

    // Motion constraints used in controllers (RPM, RPM/sec)
    public static final double MAX_VELOCITY_RPM = 6000.0;
    public static final double MAX_ACCEL_RPMPerS = 1800.0;

    // Telemetry names
    public static final String MOTOR_TELEMETRY_NAME = "LauncherMotor";
    public static final String MECH_TELEMETRY_NAME = "LauncherMech";

    // Mechanical / motor configuration
    public static final int GEARING = 1;
    public static final boolean MOTOR_INVERTED = true;
    public static final boolean FOLLOWER0_INVERTED = true;
    public static final boolean FOLLOWER1_INVERTED = true;
    
    public static final int MOTOR_COUNT = 3; // motors per side used for DCMotor factory

    // Electrical limits
    public static final double STATOR_CURRENT_LIMIT_AMPS = 40.0;

    // Geometry / mass properties
    public static final double DIAMETER_INCH = 3.965;
    public static final double MASS_GRAMS = 2314.288;
    // Moment of inertia about spin axis (kg*m^2) - from CAD Lzz converted
    public static final double MOI_KG_M2 = 0.04180177;

    // Soft limits (RPM)
    public static final double SOFT_LIMIT_RPM = 5000.0;

    public static final double ChangeNudgeFactor = -50;
    public static final double TRENCH_RPM = -4600.0;
    public static final double HUB_RPM = -3300.0;
    public static final double TOWER_RPM = -3500.0;
    public static final double FLYWHEEL_IDLE_RPM = 0.0; // 800
    public static final double READY_TOLERANCE_RPM = 150.0;
    public static final double SHOT_CONFIDENCE_THRESHOLD = 50.0;
    public static final double PASS_TARGET_X_METERS = 1.5;

    // Shoot-on-the-move & simulation parameters
    public static final double LAUNCHER_OFFSET_X = -0.223; // meters forward from robot center
    public static final double LAUNCHER_OFFSET_Y = 0.0; // meters left from center (0 if centered)
    public static final double LAUNCHER_TUBE_SPACING =
        0.155575; // meters between left/center/right tubes in parallel launcher

    public static final ProjectileSimulator.SimParameters params =
        new ProjectileSimulator.SimParameters(
            0.215, // ball mass kg
            0.1501, // ball diameter m
            0.47, // drag coeff (smooth sphere)
            0.2, // Magnus coeff
            1.225, // air density
            0.539, // exit height (m), floor to where the ball leaves the shooter
            0.1016, // flywheel diameter (m), measure with calipers
            1.83, // target height (m), from game manual
            0.42, // slip factor (0=no grip, 1=perfect), tune this on the real robot
            60.0, // launch angle from horizontal, measure from CAD
            0.001, // sim timestep
            1200,
            6000,
            25,
            5.0 // RPM search range, iterations, max sim time
            );
    public static final Translation2d hubCenter = new Translation2d(4.623, 4.0); // your target
    public static final Translation2d blueHubForward =
        new Translation2d(1, 0); // which way the hub faces
    public static final Translation2d redHubForward =
        new Translation2d(-1, 0); // which way the hub faces
  }

  public static final class IntakeConstants {
    public final class Pivot {
      public final class Real {
        public static final double kp = 100.0;
        public static final double ki = 0.0;
        public static final double kd = 0.0;
        public static final AngularVelocity maxVelocity = DegreesPerSecond.of(180);
        public static final AngularAcceleration maxAcceleration = DegreesPerSecondPerSecond.of(360);

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
        public static final double kg = 0.0; // Gravity compensation for arm
        public static final double kv = 0.0;
      }

      public static final int intakePivotID = 22;

      // Gears
      // Write down gear stages from motor to output
      // Example: 3:1 stage followed by 4:1 stage -> gearStages = {3, 4}
      // If you gear up instead, use fractions like 1/3.0
      public static final double[] gearStages = {60.0 / 8.0, 60.0 / 18.0, 30 / 15.0};
      public static final double totalGear =
          java.util.Arrays.stream(gearStages).reduce(1, (a, b) -> a * b);

      // Motor properties from tutorial to prevent over currenting
      public static final Current currentLimit = Amps.of(40);
      public static final Time closedLoopRampRate = Seconds.of(0.01);
      public static final Time openLoopRampRate = Seconds.of(0.25);

      // Pivot mechanism constraints
      public static final Angle softLimitOne = Degrees.of(-3);
      public static final Angle softLimitTwo = Degrees.of(105);
      public static final Angle hardLimitOne = Degrees.of(-5);
      public static final Angle hardLimitTwo = Degrees.of(105);
      public static final Angle startingPosition = Degrees.of(105);
      public static final Angle stowedPosition = Degrees.of(105);
      public static final Angle intakePosition = Degrees.of(0);
      public static final Angle jitterPosition = Degrees.of(45);
      public static final Distance armLength = Feet.of(1);
      public static final Mass mass = Pounds.of(8);

      // sys Id stuff
      public static final Voltage maxVoltage = Volts.of(7);
      public static final Velocity<VoltageUnit> stepVoltage = Volts.of(2).per(Second);
      public static final Time sysIdDuration = Seconds.of(4);
    }

    public final class Roller {
      public static final int rollerMotorID = 13;
      public static final double rollerRatio = 1; // feet per second
      public static final double smartCurrentLimit = 50;
      public static final double kp = 0;
      public static final double ki = 0;
      public static final double kd = 0;
      public static final double kS = 0.26651;
      public static final double kV = 0.12215;
      public static final double kA = 0.003861;
      public static final AngularVelocity maxVelocity = DegreesPerSecond.of(90);
      public static final AngularAcceleration maxAcceleration = DegreesPerSecondPerSecond.of(45);
      public static final double[] gearStages = {1};
      public static final double totalGear =
          java.util.Arrays.stream(gearStages).reduce(1, (a, b) -> a * b);

      public static final Current currentLimit = Amps.of(40);
      public static final Time closedLoopRampRate = Seconds.of(0.1);
      public static final Time openLoopRampRate = Seconds.of(0.25);

      // Roller speeds (duty cycle -1 to 1)
      // public static final double intakeSpeed = 0.65;
      // public static final double outtakeSpeed = 0.3;
      public static final AngularVelocity intakeSpeed = RPM.of(-4000);
      public static final AngularVelocity outtakeSpeed = RPM.of(500);
    }
  }

  public static final class SparkMaxCanIDs {
    /** Column Can ID */
    public static final int ColumnMotor = 4;

    /** Climb Can IDs */
    public static final int ClimbLeftMotor = 18;

    public static final int ClimbRightMotor = 8;

    /** BeltDexter Can ID */
    public static final int BeltDexterMotor = 15;
  }

  public static final class ClimbConstants {
    /** Column Gear Ratio */
    public static final int gearRatio = 25;
    /** Column Stall Current Limit */
    public static final Current currentLimit = Amps.of(60);

    public static final Time ClosedLoppRampRate = Seconds.of(0.25);
    public static final Time OpenLoppRampRate = Seconds.of(0.25);

    public static final Distance PreHangExtension = Inches.of(9.5);
    public static final Distance HangDistance = Inches.of(0);
    public static final Distance ReleaseDistance = Inches.of(9.5);
    public static final Distance RestDistance = Inches.of(0);

    public static final Distance MechanismCircumference =
        Meters.of(Inches.of(0.25).in(Meters) * 22);

    public static final Distance hardMinimum = Inches.of(0);
    public static final Distance hardMaximum = Inches.of(9.5);
    public static final Mass Weight = Pounds.of(1.02757);

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
      public static final double ki = 0.0;
      public static final double kd = 0.0;
      public static final AngularVelocity maxVelocity = DegreesPerSecond.of(90);
      public static final AngularAcceleration maxAcceleration = DegreesPerSecondPerSecond.of(45);

      public static final double ks = 0.5;
      public static final double kg = 0.5;
      public static final double kv = 0.5;
    }
  }

  public static final class BeltDexterConstants {
    /** Beltdexter Gear Ratio */
    public static final int gearRatio = 2;
    /** Beltdexter Stall Current Limit */
    public static final Current currentLimit = Amps.of(40);

    public static final AngularVelocity IntakeSpeed = RPM.of(1000);
    public static final AngularVelocity OuttakeSpeed = RPM.of(-75);
    public static final AngularVelocity IdleSpinSpeed = RPM.of(50);
    public static final AngularVelocity LaunchSpeed = RPM.of(75);

    public final class Real {
      public static final double kp = 0;
      public static final double ki = 0.0;
      public static final double kd = 0.0;
      public static final AngularVelocity maxVelocity = DegreesPerSecond.of(90);
      public static final AngularAcceleration maxAcceleration = DegreesPerSecondPerSecond.of(45);

      // feedforward
      public static final double ks = 2.8752;
      public static final double ka = 0.0;
      public static final double kv = 0.46964;
    }

    public final class Sim {
      public static final double kp = 0.0;
      public static final double ki = 0.0;
      public static final double kd = 0.0;
      public static final AngularVelocity maxVelocity = DegreesPerSecond.of(90);
      public static final AngularAcceleration maxAcceleration = DegreesPerSecondPerSecond.of(45);

      public static final double ks = 0.0;
      public static final double ka = 0.0;
      public static final double kv = 0.0;
    }
  }

  public static final class ColumnConstants {
    /** Column Gear Ratio */
    public static final int gearRatio = 2;
    /** Column Stall Current Limit */
    public static final Current currentLimit = Amps.of(40);

    public static final Voltage IntakeSpeed = Volts.of(6);
    public static final Voltage OuttakeSpeed = Volts.of(-3);
    public static final Voltage IdleReverseSpeed = Volts.of(-0.5);
    public static final Voltage FirstLaunchSpeed = Volts.of(10);
    public static final double LaunchRampSeconds = 5;

    public final class Real {
      public static final double kp = 0.0;
      public static final double ki = 0.0;
      public static final double kd = 0.0;
      public static final AngularVelocity maxVelocity = DegreesPerSecond.of(90);
      public static final AngularAcceleration maxAcceleration = DegreesPerSecondPerSecond.of(45);

      // Feedforward
      public static final double ks = 0.0;
      public static final double ka = 0.0;
      public static final double kv = 0.0;
    }

    public final class Sim {
      public static final double kp = 0.0;
      public static final double ki = 0.0;
      public static final double kd = 0.0;
      public static final AngularVelocity maxVelocity = DegreesPerSecond.of(90);
      public static final AngularAcceleration maxAcceleration = DegreesPerSecondPerSecond.of(45);

      public static final double ks = 0.0;
      public static final double ka = 0.0;
      public static final double kv = 0.0;
    }
  }

  public static final class VisionConstants {
    // AprilTag layout
    public static final AprilTagFieldLayout aprilTagLayout =
        AprilTagFieldLayout.loadField(AprilTagFields.kDefaultField);

    // Camera names, must match names configured on coprocessor
    public static final String camera0Name = "Cherry";
    public static final String camera1Name = "Orange";
    public static final String camera2Name = "Grape";
    public static final String camera3Name = "Strawberry";

    // Robot to camera transforms
    // (Not used by Limelight, configure in web UI instead)
    public static final Transform3d robotToCamera0 =
        new Transform3d(
            Inches.of(12.25),
            Inches.of(12.25),
            Inches.of(13.375),
            new Rotation3d(Degrees.of(0), Degrees.of(0), Degrees.of(0)));
    public static final Transform3d robotToCamera1 =
        new Transform3d(
            Inches.of(12.25),
            Inches.of(-12.25),
            Inches.of(13.375),
            new Rotation3d(Degrees.of(0), Degrees.of(0), Degrees.of(0)));
    public static final Transform3d robotToCamera2 =
        new Transform3d(
            Inches.of(-(9.25)),
            Inches.of(11.25),
            Inches.of(8.375),
            new Rotation3d(Degrees.of(0), Degrees.of(-45), Degrees.of(180)));
    public static final Transform3d robotToCamera3 =
        new Transform3d(
            Inches.of(-(9.25)),
            Inches.of(-11.25),
            Inches.of(8.375),
            new Rotation3d(Degrees.of(0), Degrees.of(-45), Degrees.of(180)));

    // SIM Camera Constants
    public static final int resWidth = 1280;
    public static final int resHeight = 800;
    public static final Rotation2d fovDiag = Rotation2d.fromDegrees(92.4);

    // Basic filtering thresholds
    public static final double maxAmbiguity = 0.3;
    public static final double maxZError = 0.75;

    // Standard deviation baselines, for 1 meter distance and 1 tag
    // (Adjusted automatically based on distance and # of tags)
    public static final double linearStdDevBaseline = 0.02; // Meters
    public static final double angularStdDevBaseline = 0.06; // Radians

    // Standard deviation multipliers for each camera
    // (Adjust to trust some cameras more than others)
    public static final double[] cameraStdDevFactors =
        new double[] {
          1.0, // Camera 0
          1.0, // Camera 1
          1.0, // Camera 2
          1.0 // Camera 3
        };

    // Multipliers to apply for MegaTag 2 observations
    public static final double linearStdDevMegatag2Factor = 0.5; // More stable than full 3D solve
    public static final double angularStdDevMegatag2Factor =
        Double.POSITIVE_INFINITY; // No rotation data available
  }

  public static final class RobotConstants {
    public static final double BumperWidth = 0.818; // Meters
    public static final double BumperLength = 0.818; // Meters
    public static final double BumperHeight = 0.127; // Meters
  }

  public static final class LEDConstants {
    public static final int CANDLE_CAN_ID = 31;
    public static final double BRIGHTNESS_SCALAR = 0.5;

    // Slot 0 LED indices
    public static final int SLOT0_START_IDX = 0;
    public static final int SLOT0_END_IDX = 3;

    // Slot 1 LED indices
    public static final int SLOT1_START_IDX = 4;
    public static final int SLOT1_END_IDX = 7;
  }
}
