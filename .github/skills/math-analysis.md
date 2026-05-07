---
name: Math Analysis
description: Signal processing and mathematical analysis utilities for control logic validation, including derivatives, integrals, RMS, settling time, and statistical checks.
---

# Math Analysis

This skill provides advanced mathematical tools to analyze robot sensor data, validate control system behavior, and detect anomalies in log files. Use these utilities to verify that motors are reaching setpoints, detecting overshoot, checking for instability, and validating motion profiles.

## Core Concepts

### Derivatives (Rate of Change)
Derivatives measure how fast a signal is changing. Use this to detect:
- **Acceleration changes**: Is the motor ramping smoothly or abruptly?
- **Control loop oscillation**: Is the controller hunting for the setpoint?
- **Noise**: High-frequency noise in derivatives indicates sensor/measurement issues

**Gradle task:**
```bash
./gradlew runMathAnalysis -Pargs="--log match.wpilog --keys /RealOutputs/Drive/Velocity --derivative --plot"
```

**Expected output:** A new column `velocity_derivative` showing rate of change (m/s²)

### Integrals (Accumulated Motion)
Integrals sum up changes over time. Use this to:
- **Verify distance traveled**: Integrate velocity to get position
- **Check for drift**: If integral doesn't match expected distance, there's slip or calibration error
- **Energy calculation**: Integrate power over time to get energy consumed

**Example:**
```bash
./gradlew runMathAnalysis -Pargs="--log match.wpilog --keys /RealOutputs/Drive/VelocityY --integral --compare /RealOutputs/Drive/PositionY"
```

**Output:** Shows integrated velocity vs actual position (should match within tolerance)

### RMS (Root Mean Square)
RMS measures the typical magnitude of a signal, accounting for oscillation. Use this to:
- **Quantify noise**: Compare RMS to average to see noise level
- **Check stability**: Low RMS around setpoint = good control
- **Motor vibration detection**: High RMS in acceleration = bearing wear or mechanical issues

**Example:**
```bash
./gradlew runMathAnalysis -Pargs="--log match.wpilog --keys /RealOutputs/Launcher/Current --rms --window 0.5"
```

**Output:** `RMS Current: 12.5A (avg: 11.2A, noise: ±1.3A)`

### Settling Time
Settling time measures how quickly a system reaches steady state after a command change. Use this to:
- **Validate PID tuning**: Is settling time acceptable? < 0.5 seconds is good
- **Detect instability**: Oscillation that doesn't settle = tuning issue
- **Compare controller versions**: Which gains settle faster?

**Example:**
```bash
./gradlew runMathAnalysis -Pargs="--log match.wpilog --keys /RealOutputs/Launcher/RPM --settling-time --threshold 50 --start 15.0 --end 20.0"
```

**Output:**
```
Settling Analysis (15.0s - 20.0s):
  Command step detected at 15.2s (3000 RPM)
  Reached within ±50 RPM at 15.8s
  Settling time: 0.6s
  Overshoot: 3.2% (3096 RPM)
  Final steady-state error: ±12 RPM
```

### Thresholds & Limits
Detect when signals exceed safe operating ranges. Use this to:
- **Find anomalies**: Motor current spike = jam or stall?
- **Validate safety checks**: Were thermal limits respected?
- **Trigger analysis**: "Show me all times when motor temp > 60°C"

**Example:**
```bash
./gradlew runMathAnalysis -Pargs="--log match.wpilog --keys /RealOutputs/Indexer/MotorCurrent --threshold 20.0 --above --report"
```

**Output:**
```
Threshold Violations: /RealOutputs/Indexer/MotorCurrent > 20.0A
  Event 1: 12.5s - 12.8s (peak: 28.3A, duration: 0.3s)
  Event 2: 35.2s - 36.1s (peak: 31.5A, duration: 0.9s)
  Total events: 2 | Total duration: 1.2s
```

### Cross-Correlation & Relationships
Find how two signals relate to each other. Use this to:
- **Lag detection**: Motor command vs actual velocity (is there delay?)
- **Coupling**: Does closing the launcher affect drive stability?
- **Validation**: Does setpoint match commanded value?

**Example:**
```bash
./gradlew runMathAnalysis -Pargs="--log match.wpilog --keys /RealOutputs/Drive/Velocity --correlate /RealOutputs/Drive/DesiredVelocity"
```

**Output:**
```
Cross-Correlation Analysis:
  Pearson coefficient: 0.987 (excellent correlation)
  Time lag: 0.015s (command leads by 15ms)
  Coherence: 0.94 (good coherence at 5Hz)
```

## Common Analysis Workflows

### 1. Validate Motor Tuning
```bash
# Check if launcher is settling properly
./gradlew runMathAnalysis -Pargs="--log match.wpilog \
  --keys /RealOutputs/Launcher/RPM \
  --settling-time --threshold 50 \
  --derivative --rms"
```

Look for:
- ✅ Settling time < 0.5s
- ✅ RMS near steady state < 5% of commanded value
- ✅ Derivative smooth (no chatter)

### 2. Detect Mechanical Issues
```bash
# High current draw = binding, friction, or jam
./gradlew runMathAnalysis -Pargs="--log match.wpilog \
  --keys /RealOutputs/Intake/MotorCurrent,/RealOutputs/Intake/Velocity \
  --correlate \
  --threshold 15.0"
```

Look for:
- ⚠️ High current but low velocity = stalled/jammed
- ✅ Current/velocity correlation > 0.9

### 3. Check For Loop Overruns
```bash
# UserCodeMS spiking = periodic loop running over
./gradlew runMathAnalysis -Pargs="--log match.wpilog \
  --keys /UserCodeMS \
  --derivative --rms --threshold 20.0"
```

Look for:
- ✅ UserCodeMS stays < 20ms
- ✅ Derivative smooth (no sudden jumps)
- ⚠️ RMS > 2ms indicates inconsistent loop timing

### 4. Validate Autonomous Motion
```bash
# Compare planned vs actual trajectory
./gradlew runMathAnalysis -Pargs="--log match_auto.wpilog \
  --keys /RealOutputs/Drive/PositionX,/RealOutputs/Drive/DesiredPositionX \
  --correlate --integral \
  --plot --compare"
```

Look for:
- ✅ Position correlation > 0.98
- ✅ Integrated velocity matches position
- ⚠️ Lag > 100ms indicates controller tuning needed

## CLI Reference

### Common Arguments
- `--log <path>` : **(Required)** Path to `.wpilog` file
- `--keys <k1,k2,...>` : Keys to analyze (comma-separated)
- `--start <seconds>` : Start time for analysis window
- `--end <seconds>` : End time for analysis window
- `--plot` : Generate Matplotlib graphs (saves to `output.png`)
- `--compare` : Compare multiple keys (for validation)
- `--report` : Generate detailed HTML report

### Math Operations
- `--derivative` : Calculate rate of change (first derivative)
- `--integral` : Calculate accumulated sum (integration)
- `--rms` : Root mean square magnitude
- `--settling-time` : Time to reach steady state
- `--threshold <value>` : Detect crossings of value
- `--above` : Find values above threshold
- `--below` : Find values below threshold
- `--correlate` : Cross-correlation between keys

### Output Control
- `--json` : JSON output (machine-readable)
- `--compact` : Compact JSON (minimal output)
- `--summary` : Statistical summary only
- `--csv <file>` : Export to CSV file

## Advanced Examples

### Finding Performance Regressions
Compare two match logs to detect changes:

```bash
# Log from last week (baseline)
./gradlew runMathAnalysis -Pargs="--log baseline_match.wpilog \
  --keys /RealOutputs/Launcher/RPM \
  --settling-time --json > baseline_metrics.json"

# Current log
./gradlew runMathAnalysis -Pargs="--log current_match.wpilog \
  --keys /RealOutputs/Launcher/RPM \
  --settling-time --json > current_metrics.json"

# Compare (agent can run this automatically)
# If current_settling_time > baseline_settling_time * 1.2:
#   Alert: "Launcher tuning has degraded"
```

### Noise Analysis
Detect sensor noise or electrical interference:

```bash
./gradlew runMathAnalysis -Pargs="--log match.wpilog \
  --keys /RealOutputs/Drive/PositionX \
  --derivative --rms \
  --summary"
```

**Good sensor data:** Low derivative RMS (< 1mm/s)
**Noisy sensor:** High derivative RMS (> 10mm/s)

### Power Consumption Tracking
```bash
./gradlew runMathAnalysis -Pargs="--log match.wpilog \
  --keys /RealOutputs/PDH/Voltage,/RealOutputs/PDH/Current \
  --multiply-keys \
  --integral \
  --report"
```

Output: Total energy (Volt-Ampere-seconds), peak power, average power

## Troubleshooting

### "Graph not generated"
- Ensure Matplotlib is installed: `pip install matplotlib`
- Make sure `--plot` flag is included
- Check that key exists in log file

### "Correlation returned NaN"
- Keys may not have overlapping time windows
- Try specifying explicit `--start` and `--end` times
- Ensure both keys have data in the specified window

### "Derivative is noisy"
- Increase window size for smoothing: `--window 0.1` (100ms)
- Raw sensor data is typically noisy; use filtered log keys
- Consider using RMS instead of raw derivative

## Related Skills
- [Log Reading](log-reading.md) - Parse and filter log files
- [Graphing](graphing.md) - Visualize data with plots
- [Replay Testing](replay-testing.md) - Deterministic validation
- [Evidence-Driven Loop](evidence-driven-loop.md) - Automated iteration workflow
