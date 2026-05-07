---
name: robot-description
description: >
  High-level explanation of FRC robot subsystems and behavior for Team 10101's 2026
  "REBUILT" robot, WakaWaka. Use this skill whenever the user asks about how their robot
  works, wants to document subsystems, needs a subsystem overview, asks about robot
  architecture, mechanism behavior, command structure, or sensor integration. Also trigger
  when asked to "explain the robot", "describe subsystem X", "how does the launcher/intake
  work", or when writing or reviewing subsystem documentation.
---

# Robot Description — WakaWaka (Team 10101, 2026)

Team 10101's 2026 REBUILT robot is a fuel-launching platform with a modular intake-indexer-feeder-launcher pipeline. The robot uses swerve drive with advanced odometry and vision-based localization for precise autonomous play.

## Robot Architecture Overview

```
Intake (Pacman) ← Fuel pieces
    ↓ (roller captures)
    ↓
Indexer (BeltDexter/Clyde) ← Conveys fuel forward
    ↓ (belt mechanism)
    ↓
Feeder (Column/Network Switch) ← Controls fuel flow
    ↓ (throttle valve)
    ↓
Launcher (Blinky) → Launches fuel into HUB
    ↓ (flywheel)
    ↓
FIELD
```

---

## Subsystem Details

### Drive (Swerve Drivetrain)

**Purpose**: Enables omnidirectional movement and positioning for fuel scoring and autonomous navigation.

**Mechanism**:
- 4 swerve modules (front-left, front-right, back-left, back-right)
- Each module: TalonFX drive motor + steer motor (inverted pair for steer)
- Gyro: NavX2 or Pigeon 2.0 for heading feedback
- Wheel encoders on drive motors for odometry

**Motion Control**:
- Field-relative drive commanded via `DriveCommands`
- Swerve kinematics translate velocity commands to individual module vectors
- Pose estimation via `PoseEstimator` (odometry + vision fusion)

**Key Classes**:
- `Drive`: Main subsystem for swerve control
- `Module`: Individual module state/control
- `ModuleIO`: Interface for TalonFX, sim, or replay I/O
- `GyroIO`: Interface for gyro I/O (NavX, Pigeon2, Sim)

**Sensor Feedback**:
- Wheel encoders: velocity and position (odometry integration)
- Gyro: heading for field-relative control
- Vision cameras: AprilTag pose estimates

**Command Patterns**:
- `driverOneController.getAxis()` → velocity/rotation setpoints
- Path planner auto routines for autonomous scoring

---

### Intake (Pacman)

**Purpose**: Captures FUEL pieces from the floor or passed from the HUB and directs them into the Indexer belt.

**Mechanism**:
- Intake Pivot: Actuated arm that rotates to intake or stowed position
  - Motor: TalonFX with closed-loop position control
  - Gearing: Provides mechanical advantage
  - State: `IntakePivot` subsystem (position-based control)
- Intake Roller: Spinning wheel that grabs FUEL
  - Motor: Likely brushless or brushed motor
  - Control: Velocity-based or simple on/off

**States / Modes**:
- `STOWED`: Arm folded up, roller off (safe for movement)
- `INTAKING`: Arm deployed low, roller spinning inward
- `IDLE`: Arm at intake position, roller holding

**Key Commands**:
- `intakePivot.setAngle(Angle angle)`: Position the pivot
- `m_intake.intake()`: Start intaking fuel
- `m_intake.stopRoller()`: Stop the roller
- `m_intake.outtake()`: Reverse roller (eject fuel)

**Sensor Feedback**:
- Pivot encoder: position (closed-loop control)
- Roller current: stall detection if fuel is stuck

**Control Strategy**:
- Closed-loop position control on pivot (trapezoidal motion profiling)
- Simple voltage control on roller (fast spin-up)
- Named auto commands: `IntakeDown`, `IntakeRollerOut`, `IntakeRollerStop`

---

### Indexer (BeltDexter / Clyde)

**Purpose**: Conveys captured FUEL from the Intake toward the Feeder, buffering multiple pieces.

**Mechanism**:
- Belt/Chain Conveyor: Moves fuel axially along the robot
  - Motor: SparkMax (brushless NEO)
  - Control: Voltage-based (simple on/off with speed levels)
  - Belt geometry: Holds roughly 1-2 fuel pieces

**States / Modes**:
- `INTAKE`: Belt pulling fuel inward (from Intake)
- `IDLE`: Belt stationary (holding fuel)
- `LAUNCH`: Belt pushing fuel toward Feeder
- `OUTTAKE`: Belt reversing (eject fuel)
- `STOP`: Coast or brake (no command)

**Key Commands**:
- `BeltDexter.IntakeFuel()`: Start pulling fuel
- `BeltDexter.NoFuel()`: Stop the belt
- `BeltDexter.LaunchFuel()`: Push fuel toward Feeder
- `BeltDexter.OuttakeFuel()`: Reverse belt

**Sensor Feedback**:
- Motor current: stall detection (fuel jam)
- Motor velocity: optional feedback if closed-loop

**Telemetry**:
- Applied voltage, mechanism velocity, current draw

**Named Auto Commands**:
- `IndexerIn` → `IntakeFuel()`
- `IndexerStop` → `NoFuel()`

---

### Feeder (Column / Network Switch)

**Purpose**: Acts as a "valve" or throttle between the Indexer and Launcher, controlling fuel flow rate and timing.

**Mechanism**:
- Vertical Shaft Motor: Spins to pull fuel from Indexer into Launcher hopper
  - Motor: SparkMax (brushless NEO)
  - Control: Voltage ramping for smooth fuel transfer
  - Gearing: Converts RPM to volumetric flow

**States / Modes**:
- `IDLE_REVERSE`: Light reverse spin (prevents jamming)
- `RAMP_TO_LAUNCH`: Gradual voltage increase to launcher speed
- `LAUNCH`: Steady fuel feed into launcher
- `OUTTAKE`: Reverse spin (eject fuel back to Indexer)
- `STOP`: No motion

**Voltage Profile** (from code):
- `IdleReverseSpeed`: ~2V (keep fuel moving gently)
- `FirstLaunchSpeed`: Higher initial voltage
- `IntakeSpeed`: Target voltage for steady feeding
- `OuttakeSpeed`: Negative voltage

**Key Commands**:
- `Column.IntakeFuel()`: Feed fuel to launcher (`LAUNCH` state)
- `Column.OuttakeFuel()`: Reverse feed
- `Column.NoFuel()`: Stop
- `Column.IdleReverse()`: Gentle reverse holding
- `Column.VoltageRampDownLaunch()`: Smooth transition with ramp timer

**Ramp Timer Logic**:
```
If RAMP_TO_LAUNCH:
  progress = min(1.0, time / ramp_seconds)
  voltage = start_voltage + (end_voltage - start_voltage) * progress
  if progress >= 1.0:
    state = LAUNCH
```

**Sensor Feedback**:
- Motor velocity (optional closed-loop)
- Motor current (jam detection)
- Applied voltage telemetry

**Named Auto Commands**:
- `FeedIn` → `IntakeFuel()`
- `FeedOut` → `OuttakeFuel()`

---

### Launcher (Blinky)

**Purpose**: Accelerates FUEL to high RPM and launches it into the HUB for scoring.

**Mechanism**:
- Flywheel Stack: Multiple motors driving a common wheel
  - Motor Lead: TalonFX (CAN, primary control)
  - Motor Follow0 & Follow1: TalonFX (follow lead)
  - Gearing: High-ratio (fast spinup, high RPM)
  - Material: Likely rubber-coated wheel
- Shot Calculation: Uses vision and range-to-RPM lookup table (LUT) for distance-based aiming

**Modes** (`LauncherMode`):
- `IDLE`: Low RPM holding (~1000-2000 RPM, no scoring)
- `ALLIANCE_AUTO`: Autonomous launching with preset RPM
- `PASS`: Lower RPM for short-range passes
- `TARGET_LOCK`: Vision-tracked distance-to-RPM based on AprilTag
- `OVERRIDE_LAUNCH`: Manual RPM override (testing)
- `WORLDS_AUTO_REV`: Custom world-competition tuning
- `NOT_IDLE`: Spinning up, not ready to launch

**Velocity Control**:
- Closed-loop PID on flywheel motor (TalonFX velocity mode)
- Feedforward: `SimpleMotorFeedforward(kS, kV, kA)` for linearization
- **PID Gains** (tuned for real + sim):
  - Real: `kP`, `kI`, `kD` (loaded from constants)
  - Sim: Separate gains for simulator fidelity

**Launch Ready Condition**:
- `launcher.isLaunchReady()`: Returns true when velocity is within tolerance of setpoint

**Key Commands**:
- `launcher.setVelocity(RPM rpm)`: Set target flywheel RPM
- `launcher.shotCalculator.calculateLaunchRPM(...)`: Compute RPM from distance/angle
- Sequence: Spin-up → Wait for ready → Feeder feeds → Launch

**Shot Physics**:
- `ProjectileSimulator`: Models fuel ballistics (gravity, air resistance)
- `ShotCalculator.Config`: Contains projectile parameters
- `ShotLUT`: Pre-computed lookup table (distance → RPM) for fast targeting
- Integration with vision: Uses AprilTag distance estimates

**Sensor Feedback**:
- Flywheel encoder velocity (velocity feedback)
- Motor current & voltage (diagnostics)
- Target RPM setpoint (open-loop reference)

**Telemetry**:
- Current velocity vs. setpoint
- Applied voltage and current draw
- Launch readiness indicator

**SysId Integration**:
- Quasistatic and dynamic characterization routines for feedforward tuning

---

### Vision (Localization)

**Purpose**: Provides AprilTag-based pose estimates for accurate autonomous navigation and targeting.

**Hardware**:
- PhotonVision cameras (likely mounted on turret or fixed positions)
- Detects AprilTags 1-32 on field elements
- Runs on coprocessor (Jetson Nano, Raspberry Pi, etc.)

**Integration**:
- `Vision` subsystem receives camera inputs
- Feeds pose estimates into `PoseEstimator` via `drive.addVisionMeasurement()`
- Trust gating: Only accepts estimates within tolerance of odometry
- Multi-camera support for redundancy

**Key Classes**:
- `Vision`: Main subsystem aggregating camera inputs
- `VisionIO`: Interface for PhotonVision or replay
- `VisionIOPhotonVision`: Real camera I/O
- `VisionIOPhotonVisionSim`: Simulation support

**Field Element Targeting**:
- HUB: Tags 2-27 (for positioning near HUB)
- TOWER: Tags 15-16, 31-32 (for climbing assistance)
- OUTPOST: Tags 13-14, 29-30 (for coordinating with human players)
- TRENCH: Tags 1, 6-7, 12, 17, 22-23, 28 (for zone crossing)

---

### Candle (LED Control)

**Purpose**: Visual feedback to drivers and field spectators.

**Mechanism**:
- CANdle LED controller (addressable LED strip)
- Color patterns indicate robot state

**States** (example):
- IDLE: Dim or off-color
- INTAKING: One color (e.g., green)
- READY_TO_LAUNCH: Another color (e.g., cyan)
- LAUNCHED: Flash pattern
- ERROR: Red

---

## Command Structure Patterns

### Intake to Launch Pipeline Example:
```java
// Sequential command group for full cycle
new SequentialCommandGroup(
    m_intake.goToIntakePosition(),           // Deploy arm
    BeltDexter.IntakeFuel(),                 // Start conveyor
    WaitCommand(0.5),                        // Let fuel settle
    Column.VoltageRampDownLaunch(),          // Ramp feeder
    Commands.waitUntil(launcher::isLaunchReady), // Spin up shooter
    BeltDexter.LaunchFuel()                  // Feed fuel
)
```

### Vision-Based Aiming:
```java
launcher.setVelocity(
    shotCalculator.calculateLaunchRPM(
        robotPose,
        targetAprilTag,
        launchAngle
    )
)
```

### State Machines:
- Feeder has internal enum (`FeederState`) tracking current mode
- Launcher has `LauncherMode` for operation state
- Periodic methods update state machine logic

---

## Key Tuning Parameters

Located in `Constants.java` and sub-constant classes:

### Launcher:
- `LauncherConstants.REAL_kP/kI/kD`: PID gains for velocity control
- `LauncherConstants.FFW_kS/kV/kA`: Feedforward coefficients
- `LauncherConstants.FLYWHEEL_IDLE_RPM`: Low-power holding speed
- `LauncherConstants.GEARING`: Gear ratio for motor→flywheel

### Feeder (Column):
- `ColumnConstants.IntakeSpeed`: Feed voltage to launcher
- `ColumnConstants.IdleReverseSpeed`: Holding reverse voltage
- `ColumnConstants.LaunchRampSeconds`: Duration of voltage ramp
- `ColumnConstants.Real.maxVelocity/maxAcceleration`: Trapezoidal profile

### Intake:
- `IntakeConstants.Pivot.Real.kp/ki/kd`: Pivot position control
- `IntakeConstants.Pivot.Real.maxVelocity/maxAcceleration`: Pivot motion limits

### Drive (Swerve):
- Supplied by CTR `TunerConstants.java` (generated by Tuner X)
- Wheel radius, module offsets, drive/steer gear ratios

---

## I/O Architecture (AdvantageKit Pattern)

Each subsystem follows this pattern:

1. **Inputs Class** (e.g., `LauncherInputs`):
   - All sensor readings (velocity, voltage, current, etc.)
   - Marked with `@AutoLog` for automatic logging

2. **IO Interface** (e.g., `LauncherIO`):
   - Abstract methods: `updateInputs()`, `setVoltage()`, `getVelocity()`
   - Decouples subsystem from hardware

3. **Concrete IO Implementations**:
   - `LauncherIOReal`: Actual TalonFX hardware
   - `LauncherIOSim`: Simulation (physics-based)
   - `LauncherIOReplay`: Log replay for testing

4. **Subsystem** (e.g., `Launcher`):
   - Holds `LauncherIO io` instance
   - Calls `io.updateInputs()` in `periodic()`
   - Logs via AdvantageKit `Logger`

---

## Autonomous Routines

Set up in `RobotContainer` using PathPlanner:

```java
NamedCommands.registerCommand("IntakeDown", m_intake.goToIntakePosition());
NamedCommands.registerCommand("Launch",
    Commands.waitUntil(launcher::isLaunchReady)
        .andThen(BeltDexter.IntakeFuel())
        .andThen(...)
);
```

Routines are loaded from `.pathplanner` files and can mix trajectory following with named commands.

---

## Common Issues & Mitigations

### Fuel Jams
- **Symptom**: Motor current spikes, velocity drops
- **Mitigation**: Feeder has `toggleColumn()` to reverse flow; regular idle-reverse prevents congestion

### Launch Inconsistency
- **Cause**: Flywheel RPM not stable before feed
- **Mitigation**: `isLaunchReady()` ensures velocity tolerance; SysId tuning improves stability

### Vision Drift
- **Cause**: AprilTag detection fails or odometry accumulates error
- **Mitigation**: Multi-camera setup, trust gating in pose estimator, periodic re-localization

### Pivot Jamming
- **Cause**: Intake arm binds at extreme angles
- **Mitigation**: Soft limits in code; closed-loop control with current monitoring

---

## Testing & Diagnostics

### System Identification (SysId)
- Both Feeder and Launcher have SysId routines:
  - `sysIdQuasistatic()`: Slow ramping to find static friction (kS)
  - `sysIdDynamic()`: Fast ramping to characterize acceleration (kV, kA)
- Results auto-update feedforward constants

### Telemetry Dashboard
- SmartDashboard updates each periodic:
  - Column setpoint and actual velocity
  - Launcher RPM vs. target
  - Intake pivot angle
  - Drive pose estimate
- AdvantageKit logging captures all data for offline analysis

### Simulator Support
- `ModuleIOSim`, `GyroIOSim`: Physics-based swerve simulation
- `LauncherIOSim`: Projectile physics for debugging ballistics
- `VisionIOPhotonVisionSim`: Synthetic AprilTag detections
- Enables testing without robot hardware

---

## Commands Quick Reference

| Subsystem | Command | Effect |
|-----------|---------|--------|
| Intake | `m_intake.intake()` | Roller spins inward |
| Intake | `m_intake.stopRoller()` | Roller stops |
| Intake | `m_intake.outtake()` | Roller reverses (eject) |
| Indexer | `BeltDexter.IntakeFuel()` | Belt pulls fuel forward |
| Indexer | `BeltDexter.NoFuel()` | Belt stops |
| Feeder | `Column.IntakeFuel()` | Feeder feeds into launcher |
| Feeder | `Column.VoltageRampDownLaunch()` | Ramps voltage for smooth feed |
| Feeder | `Column.OuttakeFuel()` | Feeder reverses |
| Launcher | `launcher.setVelocity(rpm)` | Spin flywheel to target RPM |
| Launcher | `launcher.isLaunchReady()` | Check if velocity on target |
| Drive | `DriveCommands.drive(...)` | Field-relative swerve drive |

---

## References

- **AdvantageKit Logging**: `Logger.processInputs()`
- **PathPlanner**: Auto routine composition
- **Phoenix 6 (TalonFX)**: CAN motor control & feedback
- **REV Robotics (SparkMax)**: PWM motor control
- **PhotonVision**: AprilTag detection & pose estimation
- **WPILib**: Geometry2D, odometry, PID
