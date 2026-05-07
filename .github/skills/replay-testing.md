---
name: replay-testing
description: >
  Use AdvantageKit replay logs (.wpilog files) to validate robot code changes through
  deterministic log replay. AdvantageKit records ALL robot inputs (sensors, joystick commands,
  timestamps) and outputs robot commands deterministically, enabling fast regression testing.
  Trigger this skill whenever the user wants to replay a match log, validate a code change
  against recorded match data, run AdvantageKit replay, compare robot behavior before and after
  a change, mentions "replay", "log replay", "AdvantageKit replay", "Replay Watch", or wants
  regression testing using real match data.
---

# AdvantageKit Replay Testing

Use AdvantageKit replay logs to **deterministically** validate code changes against real robot data.

---

## 🔒 What is Deterministic Replay?

AdvantageKit's replay is **deterministic**: the robot code produces **identical outputs every time** when replaying the same log, regardless of replay speed. This is fundamentally different from other replay tools that can suffer from "butterfly effects" where timing differences cause divergent behavior over time.

### Why It Matters

- **Accurate debugging**: Replayed behavior exactly matches real robot behavior
- **Fast iteration**: Replay at 50x speed without sacrificing accuracy
- **Trustworthy outputs**: Computed values in replay can be trusted for validation
- **Practical regression testing**: Run 10-minute match logs in ~12 seconds

### The Butterfly Effect Problem

Non-deterministic replay tools (e.g., Hoot Replay at 5x speed) suffer from timing drift:
- Small timing differences compound over time
- State machines diverge from real robot behavior within seconds
- Replay outputs cannot be trusted for debugging
- Running faster than ~5x speed causes major accuracy loss

AdvantageKit's determinism eliminates this: a 10-minute match replays in 12 seconds with **perfect accuracy**.

---

## ⏰ Deterministic Timestamps (Critical!)

### The Problem

WPILib's `Timer.getTimestamp()` returns a new value each time it's called. During replay, these varying timestamps are not logged, making replay non-deterministic.

### AdvantageKit's Solution

AdvantageKit **injects a synchronized timestamp** at the start of each loop cycle:

```
Real Robot:         Loop Start → getTimestamp() → getTimestamp() → getTimestamp() → Loop End
                    (different values each time)

AdvantageKit Replay: Loop Start → inject timestamp → all getTimestamp() calls return same value → Loop End
                    (all control logic is deterministic)
```

### Using Timestamps Correctly

1. **For most control logic**: Use default synchronized timestamps (automatic)
   ```java
   // This is deterministic during replay (uses AdvantageKit's injected timestamp)
   double dt = Timer.getTimestamp() - lastTimestamp;
   ```

2. **For sensor measurement timestamps**: Record timestamps as part of input data
   ```java
   // Example: Phoenix 6 signals include measurement timestamps
   var signal = motor.getVelocity();
   double measurementTimestamp = signal.getTimestamp(); // Precise sensor timestamp
   ```

3. **For FPGA timing (performance analysis)**: Use `Timer.getFPGATimestamp()`
   ```java
   // This is NOT affected by replay (always returns actual FPGA time)
   double startTime = Timer.getFPGATimestamp();
   doExpensiveOperation();
   double executionTime = Timer.getFPGATimestamp() - startTime;
   ```

4. **To disable deterministic timestamps globally** (rarely needed):
   ```java
   // In Robot constructor after Logger.start():
   if (!Logger.hasReplaySource()) {
       RobotController.setTimeSource(RobotController::getFPGATime);
   }
   ```

---

## 🔄 Two Replay Methods

### 1. Traditional Replay (Manual)

For one-time validation or single experiments.

#### Setup

```java
// In Constants.java
public static final Mode simMode = Mode.REPLAY;
```

```java
// In Robot constructor
public Robot() {
    // Set the replay log file
    String logPath = LogFileUtil.findReplayLog();
    // Priority: AKIT_LOG_PATH env var → AdvantageScope open file → user prompt

    Logger.setReplaySource(new WPILOGReader(logPath));
    Logger.addDataReceiver(
        new WPILOGWriter(LogFileUtil.addPathSuffix(logPath, "_sim"))
    );

    // Optional: Run as fast as possible (doesn't affect accuracy!)
    setUseTiming(false);

    Logger.start();
}
```

#### Usage

```bash
# Start simulation (standard WPILib simulation)
./gradlew simulateJava

# Or run in headless mode with no GUI
./gradlew simulateJava -x enableSimGui
```

Replay output automatically opens in AdvantageScope with original inputs/outputs alongside new replay outputs.

#### Output Structure

```
New log file contains three tables:
├─ RealOutputs/    ← Original robot outputs (from real match)
├─ ReplayOutputs/  ← New outputs from replaying log through updated code
└─ <original inputs> ← Unchanged from original log
```

---

### 2. Replay Watch (Automatic, Rapid Iteration) ⭐

For continuous development: automatically re-runs replay when code changes.

#### Setup

The AdvantageKit template includes a Gradle task. Verify `build.gradle` has:

```gradle
task(replayWatch, type: JavaExec) {
    mainClass = "org.littletonrobotics.junction.ReplayWatch"
    classpath = sourceSets.main.runtimeClasspath
}
```

Configure `Constants.java`:

```java
public static final Mode simMode = Mode.REPLAY;
```

Configure `Robot.java` as above (use `setUseTiming(false)` recommended).

#### Usage

1. Open a log file in AdvantageScope or set the `AKIT_LOG_PATH` environment variable:
   ```bash
   export AKIT_LOG_PATH="/path/to/match_log.wpilog"
   ```

2. Start Replay Watch:
   ```bash
   ./gradlew replayWatch
   ```

3. **Every time you modify code**, replay automatically runs and results open in AdvantageScope
   - Time range and visualizations are preserved
   - Previous replay output is overwritten (original log unchanged)
   - Fast iteration: typical replays take seconds

#### Perfect For

- Tuning pose estimation algorithms
- Validating path following logic
- Testing auto sequence timing
- Debugging state machine transitions

---

## 🔍 Comparing Replays (Before & After)

### Using AdvantageScope (Easiest)

1. Replay with **old code** → generates `oldcode_replay.wpilog`
2. Open it in AdvantageScope
3. Replay with **new code** → generates `newcode_replay.wpilog`
4. Open new log in same AdvantageScope window
5. Compare side-by-side using AdvantageScope's visualizations

### Using Command Line

```bash
# Export both logs to text
wpilog-dump old_replay.wpilog > old.txt
wpilog-dump new_replay.wpilog > new.txt

# Find differences
diff old.txt new.txt

# Or focus on a specific subsystem
diff <(grep "Launcher/" old.txt) <(grep "Launcher/" new.txt)
```

### Programmatically (Unit Test)

```java
@Test
void testLauncherRPMImprovement() {
    // Replay generates outputs in ReplayOutputs table
    double[] measuredRPM = Logger.getLoggedDoubleArray("ReplayOutputs/Launcher/MeasuredRPM");
    double[] setpointRPM = Logger.getLoggedDoubleArray("ReplayOutputs/Launcher/SetpointRPM");

    // Check that error is within tolerance
    for (int i = 0; i < measuredRPM.length; i++) {
        double error = Math.abs(measuredRPM[i] - setpointRPM[i]);
        assertTrue(error < 100, "RPM error too high at index " + i);
    }
}
```

---

## 📋 Common Validation Patterns

| Code Change | What to Check in Replay | Log Fields |
|---|---|---|
| **Launcher tuning** | Measured RPM convergence to setpoint | `ReplayOutputs/Launcher/MeasuredRPM` vs `SetpointRPM` |
| **Path following gains** | Robot pose following trajectory | `ReplayOutputs/Drive/PoseX/Y` vs `PathPlanner/TargetPose` |
| **Intake pivot logic** | Pivot angle transitions | `ReplayOutputs/Intake/PivotAngleDeg` state changes |
| **Feeder ramp timing** | Voltage ramp profile | `ReplayOutputs/Feeder/AppliedVolts` over time |
| **Vision pose estimation** | Odometry accuracy with/without vision | `ReplayOutputs/Drive/Pose` deviation from real |
| **State machine transitions** | Goal state changes at correct times | `ReplayOutputs/<Subsystem>/Goal` changes |

---

## 🚀 Rapid Iteration Workflow Example

### Scenario: Tuning Launcher PID Gains

1. **Capture match log** with original PID gains
   ```bash
   # Robot logs automatically during match
   # Download match_log.wpilog from logs/ directory
   ```

2. **Adjust PID gains** in `LauncherConstants.REAL_kP`
   ```java
   public static final double REAL_kP = 0.25; // Changed from 0.1879
   ```

3. **Start Replay Watch**
   ```bash
   export AKIT_LOG_PATH="path/to/match_log.wpilog"
   ./gradlew replayWatch
   ```

4. **Watch replay results** automatically update in AdvantageScope
   - Compare `ReplayOutputs/Launcher/MeasuredRPM` to setpoint
   - If overshoot improves, great!
   - If worse, undo the change and try again

5. **Repeat**: Change constants → save → replay auto-runs (~10 seconds)

All changes iterate without touching the original log file.

---

## 🎯 Replay Bubble Limitation

When code is modified during replay, **modified outputs cannot affect replayed inputs**. This is called the "replay bubble":

```
┌─────────────────────────────────────┐
│  Replayed Inputs (frozen)           │
│  ├─ Sensor readings (fixed)         │
│  ├─ Joystick commands (fixed)       │
│  └─ Timestamps (fixed)              │
└──────────────────────────────────────┘
           ↓ (read only)
┌──────────────────────────────────────┐
│  Modified Robot Code                 │
│  ├─ PID gains (can change)           │
│  ├─ State machine logic (can change) │
│  └─ Output calculations (can change) │
└──────────────────────────────────────┘
           ↓ (generates outputs)
┌──────────────────────────────────────┐
│  New ReplayOutputs                   │
│  ├─ Motor commands                   │
│  ├─ State values                     │
│  └─ Calculated values                │
└──────────────────────────────────────┘
```

**Implication**: If code depends on feedback loops (e.g., "measure actual velocity, adjust gains, measure again"), replay can only validate the first iteration. For true feedback validation, real robot testing is still needed.

---

## 🛠️ Troubleshooting

| Issue | Solution |
|---|---|
| **Replay ends immediately** | Check `LogFileUtil.findReplayLog()` finds the file; verify file is not empty |
| **File not found with `AKIT_LOG_PATH`** | Verify path exists: `ls $AKIT_LOG_PATH` |
| **Inputs missing in replay output** | Ensure fields are logged with `@AutoLog` annotation |
| **Divergence between real and replay** | Check for `Timer.getTimestamp()` calls outside logged inputs; use Phoenix 6 signal timestamps instead |
| **Replay too slow** | Enable headless mode: `./gradlew simulateJava -x enableSimGui` |
| **AdvantageScope doesn't auto-open** | Check `WPILOGWriter` is configured with `AUTO` open behavior (default) |
| **Non-deterministic outputs** | Ensure replay is running deterministically; check `Logger.hasReplaySource()` |

---

## 📚 Key Classes (API Reference)

```java
// Replay setup
org.littletonrobotics.junction.Logger
org.littletonrobotics.junction.wpilog.WPILOGReader     // Input log
org.littletonrobotics.junction.wpilog.WPILOGWriter     // Output log
org.littletonrobotics.junction.wpilog.WPILOGWriter.AdvantageScopeOpenBehavior
org.littletonrobotics.junction.LogFileUtil             // findReplayLog(), addPathSuffix()

// Timestamp management
edu.wpi.first.wpilibj.Timer.getTimestamp()             // Synchronized (deterministic)
edu.wpi.first.wpilibj.Timer.getFPGATimestamp()        // Real FPGA time (non-deterministic)
edu.wpi.first.wpilibj.RobotController.setTimeSource()
```

---

## ✅ Checklist: Before Deploying Code Changes

- [ ] Replay matches original robot behavior (check key outputs in ReplayOutputs)
- [ ] No divergence in state machines or tuned parameters
- [ ] Vision/odometry pose estimates align between real and replay
- [ ] Auto sequences execute at correct times
- [ ] Motor commands reasonable (no max-voltage spikes)
- [ ] Tested on multiple match logs if available
- [ ] Real robot testing validates feedback-dependent logic

---

## References

- **AdvantageKit Docs**: https://docs.advantagekit.org/
- **Log Replay Comparison**: https://docs.advantagekit.org/theory/log-replay-comparison
- **Deterministic Timestamps**: https://docs.advantagekit.org/theory/deterministic-timestamps
- **Traditional Replay**: https://docs.advantagekit.org/getting-started/traditional-replay
- **Replay Watch**: https://docs.advantagekit.org/getting-started/replay-watch
- **AdvantageScope**: https://docs.advantagescope.org/
- **WPILib Simulation**: https://docs.wpilib.org/en/stable/docs/software/wpilib-tools/robot-simulation/introduction.html
