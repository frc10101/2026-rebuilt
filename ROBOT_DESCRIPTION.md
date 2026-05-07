# WakaWaka (Team 10101) — 2026 REBUILT Robot Architecture & Libraries

## Overview

**WakaWaka** is Team 10101's 2026 REBUILT fuel-launching platform with modular intake-indexer-feeder-launcher subsystems. The robot employs a swerve drivetrain for omnidirectional movement, advanced vision-based localization with AprilTag detection, and precision fuel launching capabilities.

---

## Core Robot Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    ROBOT PIPELINE                       │
├─────────────────────────────────────────────────────────┤
│  INTAKE (Pacman)                                        │
│  └─ Pivot Arm (TalonFX) + Roller Motor                 │
│     ↓                                                   │
│  INDEXER (BeltDexter/Clyde)                            │
│  └─ Belt Conveyor (SparkMax NEO)                       │
│     ↓                                                   │
│  FEEDER (Network Switch)                               │
│  └─ Vertical Shaft (SparkMax NEO)                      │
│     ↓                                                   │
│  LAUNCHER (Blinky)                                     │
│  └─ Flywheel Stack (3× TalonFX)                        │
│     ↓                                                   │
│  FIELD (HUB, TOWER, TRENCH)                            │
└─────────────────────────────────────────────────────────┘

DRIVE: Swerve Drivetrain (4× Modules, NavX2/Pigeon2 Gyro)
VISION: PhotonVision Camera + AprilTag Localization
LEDS: CANdle Controller for Driver Feedback
```

---

## Subsystems Details

### 1. **Drive (Swerve Drivetrain)**
- **Purpose**: Omnidirectional field-relative movement and autonomous navigation
- **Hardware**:
  - 4 swerve modules (FL, FR, BL, BR)
  - Drive motors: TalonFX (Phoenix 6)
  - Steer motors: TalonFX (Phoenix 6) inverted pairs
  - Gyro: NavX2 or Pigeon 2.0
  - Odometry via drive motor encoders
- **Control**:
  - Field-relative velocity commands
  - Swerve kinematics with Phoenix 6 API
  - Pose estimation via odometry + vision fusion
- **Key Classes**:
  - `Drive.java` - Main subsystem
  - `Module.java` - Individual swerve module
  - `ModuleIOTalonFX.java` - Real hardware I/O (CAN)
  - `ModuleIOSim.java` - Physics-based simulation
  - `GyroIOPigeon2.java` / `GyroIONavX.java` - Gyro implementations
  - `PhoenixOdometryThread.java` - Odometry sampling at 200Hz

---

### 2. **Intake (Pacman)**
- **Purpose**: Captures fuel from the floor and directs it to the Indexer
- **Hardware**:
  - **Pivot Arm**: TalonFX with closed-loop position control
    - Geared for mechanical advantage
    - Uses ArmFeedforward for gravity compensation
  - **Roller Motor**: Brushless motor for grabbing fuel
- **Control**:
  - Closed-loop position control (0° stowed → ~45° intake)
  - Voltage-based roller control
  - State tracking: STOWED, INTAKING, IDLE, OUTTAKE
- **Configuration**:
  - Uses `SmartMotorController` wrapper (YAMS library)
  - Trapezoidal motion profiling
  - Sim model includes arm physics
- **Key Methods**:
  - `goToIntakePosition()`, `goToStowPosition()`
  - `intake()`, `stopRoller()`, `outtake()`

---

### 3. **Indexer (BeltDexter / Clyde)**
- **Purpose**: Conveys fuel from Intake toward Feeder via belt mechanism
- **Hardware**:
  - SparkMax (REV NEO brushless motor)
  - Belt/chain conveyor holding 1-2 fuel pieces
- **Control**:
  - Voltage-based (simple on/off with speed levels)
  - State machine: INTAKE, IDLE, LAUNCH, OUTTAKE, STOP
- **States**:
  - `IntakeFuel()` - Belt pulls inward
  - `NoFuel()` / `IdleFuel()` - Belt stops
  - `LaunchFuel()` - Belt pushes toward Feeder
  - `OuttakeFuel()` - Belt reverses (eject)
- **Telemetry**:
  - Applied voltage, motor velocity, current draw (jam detection)

---

### 4. **Feeder (Network Switch / Column)**
- **Purpose**: Throttle valve controlling fuel flow rate to Launcher
- **Hardware**:
  - SparkMax (REV NEO brushless motor)
  - Vertical shaft with gearing
- **Control**:
  - Voltage ramping for smooth fuel transfer
  - State machine with ramp timer logic
- **States**:
  - `IDLE_REVERSE` - Light reverse (~2V, jam prevention)
  - `RAMP_TO_LAUNCH` - Gradual voltage increase over configurable time
  - `LAUNCH` - Steady feed voltage to Launcher
  - `OUTTAKE` - Reverse feed
  - `STOP` - No motion
- **Voltage Profile**:
  - Ramp start: ~2V
  - Ramp end: Target launch voltage (~8V typical)
  - Duration: 0.2-0.5 seconds (tunable)
- **Key Features**:
  - Non-blocking async ramp (uses Timer)
  - SysId integration for feedforward tuning
  - Closed-loop optional (current mode)

---

### 5. **Launcher (Blinky)**
- **Purpose**: Accelerates fuel to high RPM and launches into scoring zone
- **Hardware**:
  - **Flywheel**: 3 TalonFX motors (1 lead + 2 followers)
    - Diameter: 3.965 inches
    - Mass: 2314g
    - Moment of inertia: 0.0418 kg·m²
  - Geared for high RPM (6000+ max)
- **Control**:
  - Closed-loop velocity control (PID + Feedforward)
  - **Real Robot PID**: kP=0.1879, kI=0.0, kD=0.0
  - **Sim PID**: kP=0.91, kI=0.1, kD=0.0
  - **Feedforward** (SimpleMotorFeedforward):
    - kS = 0.24566 (static friction)
    - kV = 0.12251 (velocity gain)
    - kA = 0.014885 (acceleration gain)
- **LauncherMode Enum**:
  - `IDLE` - Low RPM holding (~1000-2000)
  - `ALLIANCE_AUTO` - Autonomous with preset RPM
  - `PASS` - Lower RPM for short passes (~3000)
  - `TARGET_LOCK` - Vision-guided distance-to-RPM
  - `WORLDS_AUTO_REV` - Competition tuning
  - `OVERRIDE_LAUNCH` - Manual RPM override (testing)
  - `NOT_IDLE` - Spinning up, not ready
- **Shot Physics**:
  - `ProjectileSimulator`: Ballistic modeling (gravity, drag, Magnus effect)
  - `ShotCalculator`: Distance-based RPM lookup
  - `ShotLUT`: Pre-computed lookup table (distance → RPM)
  - Integration with AprilTag distance estimates
- **Launch Ready**:
  - `isLaunchReady()` checks if velocity within 150 RPM tolerance
  - Prevents premature or inconsistent launches
- **Simulation**:
  - Exit height: 0.539m
  - Launch angle: 60°
  - Slip factor: 0.43 (grip/friction)
  - Air density: 1.225 kg/m³
- **SysId Support**:
  - Quasistatic and dynamic characterization
  - Auto-updates feedforward constants

---

### 6. **Vision (Localization)**
- **Purpose**: AprilTag-based pose estimation for autonomous and targeting
- **Hardware**:
  - PhotonVision camera on coprocessor
  - Detects AprilTags 1-32 on field
- **Implementation**:
  - `VisionIOPhotonVision.java` - Real hardware
  - `VisionIOPhotonVisionSim.java` - Simulation
- **Integration**:
  - Feeds pose estimates to Drive's PoseEstimator
  - Trust gating: Only accepts estimates within tolerance
  - Multi-camera capable for redundancy
- **Field Element Mappings**:
  - HUB: Tags 2-27
  - TOWER: Tags 15-16, 31-32
  - OUTPOST: Tags 13-14, 29-30
  - TRENCH: Tags 1, 6-7, 12, 17, 22-23, 28
- **Telemetry**:
  - Camera pose detections, latency, tag reliability

---

### 7. **Candle (LED Control)**
- **Purpose**: Visual feedback to drivers and spectators
- **Hardware**: CANdle addressable LED controller
- **States**:
  - IDLE, INTAKING, READY_TO_LAUNCH, LAUNCHED, ERROR
- **Implementation**: `Candle.java` subsystem

---

## Libraries & Dependencies

### **WPILib Ecosystem (2026)**
- **Core**: `edu.wpi.first.wpilibj`, `edu.wpi.first.wpilibj2`
- **Commands Framework**: WPILib-New-Commands v1.0.0
- **Math & Units**: `edu.wpi.first.math`, `edu.wpi.first.units`
- **AprilTag**: Built-in field layouts and detection utilities
- **RobotController**: Hardware I/O access (CAN, PWM, DIO)

| Library | Purpose |
|---------|---------|
| `edu.wpi.first.wpilibj` | Core robot framework (TimingLoop, DriverStation, etc.) |
| `edu.wpi.first.wpilibj2` | Command-based framework, SubsystemBase, Commands |
| `edu.wpi.first.math.geometry` | Rotation2d, Translation2d, Translation3d, Pose2d, Pose3d |
| `edu.wpi.first.math.controller` | PID, SimpleMotorFeedforward, ArmFeedforward |
| `edu.wpi.first.math.kinematics` | SwerveDriveKinematics, SwerveModuleState |
| `edu.wpi.first.math.estimator` | SwerveDrivePoseEstimator for odometry + vision fusion |
| `edu.wpi.first.units` | Type-safe units system (Distance, Velocity, Voltage, etc.) |
| `edu.wpi.first.apriltag` | AprilTagFieldLayout, AprilTagFields |

### **Motor & Hardware Control**

#### **CTRE Phoenix 6 v26.2.0**
- **TalonFX Drive/Steer Motors**: CAN-based swerve control
- **CANifier/CANdle**: LED control
- **Status Frame Management**: Odometry thread at 200Hz
- **Features**:
  - Closed-loop velocity control with onboard PID
  - SimpleMotorFeedforward integration
  - Status signals streaming
- **Modules**:
  - `com.ctre.phoenix6.hardware.TalonFX`
  - `com.ctre.phoenix6.hardware.CANdle`
  - `com.ctre.phoenix6.controls.*`

#### **REV Robotics REVLib v2026.0.5**
- **SparkMax Motors** (NEO brushless):
  - Feeder (Column) motor
  - Indexer (BeltDexter) motor
  - Potential secondary Intake roller
- **Features**:
  - PWM/CAN control options
  - Current monitoring (jam detection)
  - REV encoder integration
- **Modules**:
  - `com.revrobotics.spark.SparkMax`
  - `com.revrobotics.spark.SparkLowLevel`
  - `com.revrobotics.RelativeEncoder`

### **Vendor Libraries**

#### **AdvantageKit v26.0.2** (Mechanical Advantage)
- **Purpose**: Advanced logging, log replay, and data analysis
- **Features**:
  - `@AutoLog` annotation for input classes (auto-generates logging)
  - `Logger` singleton for centralized data capture
  - Log replay mode for testing without hardware
  - Binary log format with compression
- **Usage**:
  ```java
  @AutoLog
  public static class ShooterInputs {
    public AngularVelocity velocity = RPM.of(0);
  }
  private final ShooterInputsAutoLogged inputs = new ShooterInputsAutoLogged();
  // In periodic:
  Logger.processInputs("Shooter", inputs);
  ```
- **Modules**:
  - `org.littletonrobotics.junction.Logger`
  - `org.littletonrobotics.junction.AutoLog`
  - `org.littletonrobotics.junction.networktables.LoggedDashboardChooser`

#### **PathPlannerLib v2026.1.2** (3015 Ranger Robotics)
- **Purpose**: Visual path planning and autonomous routines
- **Features**:
  - Trajectory generation with constraints
  - Named command integration
  - Field visualization support
  - `.pathplanner` file format
- **Usage**:
  ```java
  NamedCommands.registerCommand("IntakeDown", m_intake.goToIntakePosition());
  new PathPlannerAuto("MyAuto").schedule();
  ```
- **Modules**:
  - `com.pathplanner.lib.auto.NamedCommands`
  - `com.pathplanner.lib.commands.PathPlannerAuto`

#### **PhotonLib v2026.3.4** (Photon Vision)
- **Purpose**: AprilTag detection and pose estimation
- **Features**:
  - Runs on coprocessor (Jetson Nano, Raspberry Pi, etc.)
  - Multi-target tracking
  - Latency-compensated pose estimates
  - Calibration support for camera parameters
- **Usage**:
  ```java
  PhotonCamera camera = new PhotonCamera("frontCamera");
  PhotonPipelineResult result = camera.getLatestResult();
  for (PhotonTrackedTarget target : result.getTargets()) {
    AprilTagFieldLayout layout = AprilTagFieldLayout.loadField(...);
  }
  ```
- **Modules**:
  - `org.photonvision.PhotonCamera`
  - `org.photonvision.targeting.PhotonTrackedTarget`

#### **Studica v2026.0.0** (Studica Robotics)
- **Purpose**: Additional FRC utilities and sensors
- **Likely Components**: CANdle extensions, servo control, etc.
- **Modules**:
  - `com.studica.frc.*`

#### **URCL v2026.0.0** (Mechanical Advantage)
- **Purpose**: Universal Relay Control Library
- **Features**: Relay and pneumatic solenoid abstractions
- **Modules**:
  - `org.littletonrobotics.urcl.*`

#### **YAMS v2026.4.1** (Yet Another Mechanism System)
- **Purpose**: Abstraction layer for mechanism control (motors, arms, flywheels)
- **Features**:
  - `SmartMotorController`: Unified interface for TalonFX and SparkMax
  - `SmartMotorControllerConfig`: Builder for motor configuration
  - Mechanism types: `Arm`, `FlyWheel`, `LinearArm`, etc.
  - Gearing abstractions: `MechanismGearing`, `GearBox`
  - Automatic simulation model generation
  - Configurable control modes: Voltage, Velocity, Position
  - Telemetry verbosity levels
- **Usage** (Launcher example):
  ```java
  SmartMotorControllerConfig config =
    new SmartMotorControllerConfig(this)
      .withControlMode(ControlMode.CLOSED_LOOP)
      .withClosedLoopController(kP, kI, kD)
      .withFeedforward(kS, kV, kA)
      .withGearing(gearRatio)
      .withTelemetry("LauncherMotor", TelemetryVerbosity.HIGH);
  FlyWheel launcher = new FlyWheel(config, motor);
  ```
- **Modules**:
  - `yams.motorcontrollers.SmartMotorController`
  - `yams.mechanisms.velocity.FlyWheel`
  - `yams.mechanisms.positional.Arm`
  - `yams.gearing.MechanismGearing`

---

## Build & Deployment Configuration

### **Gradle Build System**
- **GradleRIO v2026.2.1**: WPILib gradle plugin for FRC
- **Build Tool**: `gradlew` (Gradle wrapper)
- **Target**: Java 17 (source and target compatibility)

### **Key Gradle Tasks**
```bash
./gradlew build               # Compile and test
./gradlew deploy             # Deploy to RoboRIO
./gradlew simulateJava       # Run physics simulator
./gradlew spotlessApply      # Auto-format code (Google Java Format)
./gradlew test               # Run unit tests (JUnit 5)
./gradlew replayWatch        # Watch AdvantageKit logs
```

### **Code Quality**
- **Spotless v6.12.0**: Google Java Format, unused import removal
- **JUnit 5.10.1 + Mockito 5.x**: Testing framework

---

## Robot Operating Modes

### **Mode Enum** (Constants.java)
```java
public enum Mode {
  REAL,    // Running on actual RoboRIO hardware
  SIM,     // Physics simulation (desktop)
  REPLAY   // Replaying AdvantageKit logs
}
```

### **I/O Pattern (AdvantageKit + YAMS)**
Each subsystem follows a standard I/O abstraction:
1. **Inputs Class**: Annotated with `@AutoLog` for automatic logging
2. **IO Interface**: Abstract methods for hardware operations
3. **Concrete Implementations**:
   - Real: Hardware CAN/PWM I/O
   - Sim: Physics-based simulation
   - Replay: Dummy I/O during log replay

---

## Command Architecture

### **Teleoperated Control**
- **Driver One Controller (XboxController 0)**:
  - Left/Right triggers → Drive forward/strafe
  - Right trigger (axis 3) > 0.3 → Launch fuel
  - A button → Align to goal
  - Options button → Reset IMU
  - X button → X-out (panic stop)

- **Driver Two Controller (XboxController 1)**:
  - Left Bumper → Intake up
  - Right Bumper → Intake down
  - B Button → Jitter intake (test)
  - Left trigger > 0.3 → Toggle intake roller
  - Right trigger > 0.3 → Secondary control

- **Test Controller (CommandJoystick 2)**:
  - SysId routine buttons (quasistatic/dynamic)
  - Mechanism test commands

### **Autonomous Routines**
- Registered via `NamedCommands.registerCommand()`
- Loaded from PathPlanner `.pathplanner` files
- Mix trajectory following with named commands

### **Sequential Fuel Cycle Example**
```
1. Deploy intake arm → goToIntakePosition()
2. Start belt conveyor → BeltDexter.IntakeFuel()
3. Wait for fuel to settle → WaitCommand(0.5)
4. Ramp feeder voltage → Column.VoltageRampDownLaunch()
5. Spin up launcher → launcher.setVelocity(RPM)
6. Wait for ready → waitUntil(launcher::isLaunchReady)
7. Feed fuel → Column.IntakeFuel()
8. Launcher launches fuel automatically
```

---

## Tuning & Calibration

### **Launcher (Blinky)**
- **Feedforward**: SysId quasistatic/dynamic tests
- **Closed-loop**: Real kP=0.1879, Sim kP=0.91
- **Soft limit**: 5000 RPM
- **Launch tolerance**: 150 RPM

### **Feeder (Column)**
- **Ramp time**: Tunable (typically 0.2-0.5s)
- **Voltage profile**: Idle reverse → Launch
- **SysId tuning**: Automatic feedforward updates

### **Intake (Pacman)**
- **Pivot gains**: Real kP=20, Sim varies
- **Motion limits**: Trapezoidal profile with max velocity/accel
- **Soft limits**: 0° (stow) to ~45° (intake)

### **Drive (Swerve)**
- **Module offsets**: Calibrated in TunerX (CTR tool)
- **Wheel radius**: Measured from CAD/testing
- **Drive ratio**: ~7.36:1 (typical for swerve)
- **Steer ratio**: ~12.8:1

### **Vision**
- **AprilTag spacing**: Field-specific (2026 fields)
- **Camera calibration**: Intrinsics/extrinsics in PhotonVision
- **Pose trust gates**: Confidence thresholds

---

## Key Files & Structure

```
src/main/java/frc/robot/
├── Main.java                    # Entry point
├── Robot.java                   # Periodic loops (teleoperated, autonomous, disabled)
├── RobotContainer.java          # Subsystem instantiation & button bindings
├── Constants.java               # All tuning parameters
├── BuildConstants.java          # Auto-generated build info
│
├── subsystems/
│   ├── Launcher.java            # Blinky - Flywheel launcher
│   ├── Feeder.java              # Network Switch - Fuel throttle valve
│   ├── Indexer.java             # BeltDexter - Fuel conveyor
│   ├── Intake.java              # Pacman - Fuel intake mechanism
│   ├── Candle.java              # LED strip control
│   │
│   ├── drive/
│   │   ├── Drive.java           # Main swerve subsystem
│   │   ├── Module.java          # Single swerve module
│   │   ├── GyroIO*.java         # Gyro abstractions
│   │   ├── ModuleIO*.java       # Module I/O (Real, Sim, TalonFX, etc.)
│   │   └── PhoenixOdometryThread.java
│   │
│   └── vision/
│       ├── Vision.java          # Main vision subsystem
│       └── VisionIO*.java       # Vision I/O (PhotonVision, Sim)
│
├── commands/
│   └── DriveCommands.java       # Drive control routines
│
├── util/
│   ├── Helpers.java             # Utility functions
│   ├── Launcher/                # Projectile simulator, shot calculator
│   ├── DriverLayout.json        # Controller mapping config
│   ├── Music/                   # Audio files (if used)
│   └── Launcher.json            # Launcher tuning data
│
└── generated/
    └── TunerConstants.java      # Auto-generated by CTR Tuner X

src/main/deploy/
├── pathplanner/
│   ├── settings.json            # PathPlanner config
│   ├── navgrid.json             # Navigation grid for obstacles
│   ├── autos/                   # Autonomous routines (.pathplanner files)
│   └── paths/                   # Pre-built trajectories

vendordeps/
├── AdvantageKit.json            # Logging framework
├── PathplannerLib.json          # Path planning
├── Phoenix6-26.2.0.json         # CTRE motor control
├── photonlib.json               # Vision AprilTag
├── REVLib.json                  # REV SparkMax control
├── Studica.json                 # Additional utilities
├── URCL.json                    # Pneumatic/relay abstractions
├── WPILibNewCommands.json       # Commands framework
└── yams.json                    # Mechanism abstraction layer
```

---

## Testing

### **Unit Tests**
- Located in `src/test/java/frc/robot/`
- Framework: JUnit 5 + Mockito
- Test reports: `build/reports/tests/test/index.html`

### **Simulation**
- **Desktop physics sim**: Run `./gradlew simulateJava`
- **Replay logs**: `./gradlew replayWatch` (AdvantageKit)
- **Subsystem sim support**:
  - `ModuleIOSim` - Swerve module physics
  - `GyroIOSim` - Gyro simulation
  - `LauncherIOSim` - Flywheel physics (optional)
  - `VisionIOPhotonVisionSim` - Synthetic AprilTag detections

---

## Deployment Checklist

1. **Code**:
   - [ ] Compile: `./gradlew build`
   - [ ] Tests pass: `./gradlew test`
   - [ ] Format: `./gradlew spotlessApply`

2. **Constants Tuning**:
   - [ ] Launcher PID/Feedforward finalized
   - [ ] Feeder ramp times tuned
   - [ ] Intake pivot angles calibrated
   - [ ] Drive constants from TunerX

3. **Hardware**:
   - [ ] All motor CAN IDs configured
   - [ ] Gyro orientation correct
   - [ ] Vision camera calibrated
   - [ ] LED strip wired correctly

4. **Autonomous**:
   - [ ] `.pathplanner` files created
   - [ ] Named commands registered
   - [ ] Routes tested in simulation

5. **Deploy**:
   - `./gradlew deploy`
   - Verify RoboRIO boots correctly
   - Run diagnostic test in RoboRIO web dashboard

---

## References & Documentation

- **WPILib**: https://docs.wpilib.org/
- **CTRE Phoenix 6**: https://pro.docs.ctr-electronics.com/
- **REV Robotics**: https://docs.revrobotics.com/
- **AdvantageKit**: https://github.com/Mechanical-Advantage/AdvantageKit
- **PathPlanner**: https://pathplanner.dev/
- **PhotonVision**: https://docs.photonvision.org/
- **YAMS**: https://yet-another-software-suite.github.io/YAMS/
- **Studica**: https://www.studica.com/
- **URCL**: https://github.com/Mechanical-Advantage/URCL

---

## Summary Table: All Libraries

| Library | Version | Vendor | Purpose | Key Classes |
|---------|---------|--------|---------|------------|
| WPILib | 2026 | FIRST | Core robot framework | SubsystemBase, Commands, RobotBase |
| WPILib-New-Commands | 1.0.0 | FIRST | Command-based programming | Command, Subsystem, Trigger |
| Phoenix 6 | 26.2.0 | CTRE | TalonFX motor control | TalonFX, CANdle, SwerveRequest |
| REVLib | 2026.0.5 | REV | SparkMax motor control | SparkMax, RelativeEncoder |
| AdvantageKit | 26.0.2 | Mech Advantage | Data logging & replay | Logger, @AutoLog |
| PathPlannerLib | 2026.1.2 | 3015 Rangers | Path planning & autos | PathPlannerAuto, NamedCommands |
| PhotonLib | v2026.3.4 | Photon Vision | AprilTag pose estimation | PhotonCamera, PhotonTrackedTarget |
| Studica | 2026.0.0 | Studica | Additional FRC utilities | (Various) |
| URCL | 2026.0.0 | Mech Advantage | Pneumatics abstraction | (Various) |
| YAMS | 2026.4.1 | YASS Team | Mechanism abstraction | SmartMotorController, FlyWheel, Arm |
| JUnit | 5.10.1 | JUnit | Unit testing framework | @Test, Assertions |
| Mockito | 5.x | Mockito | Mocking library | @Mock, when(), verify() |

---

**Last Updated**: May 7, 2026
**Robot Name**: WakaWaka
**Team**: FRC 10101
**Season**: 2026 - REBUILT
