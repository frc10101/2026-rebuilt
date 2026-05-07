---
name: Graphing
description: Generate Matplotlib visualizations of robot log data for analysis, debugging, and verification. Create trajectory plots, time-series graphs, scatter plots, and comparative analyses.
---

# Graphing

This skill enables AI agents to generate publication-quality graphs from robot log files for analysis, debugging, and verification. Visualization makes it easy to spot patterns, anomalies, and performance issues that are invisible in raw data.

## When to Use Graphing

- **Trajectory Analysis**: Plot drive position to verify autonomous path execution
- **Closed-Loop Verification**: Graph commanded vs actual setpoints (e.g., motor RPM)
- **Performance Debugging**: Visualize settling time, overshoot, oscillation
- **Anomaly Detection**: Spot data spikes, dropouts, or unexpected behavior
- **Report Generation**: Create evidence for design reviews and post-match analysis
- **Comparative Analysis**: Overlay multiple runs or compare real vs replay

## Core Graph Types

### 1. Time-Series Graph (Single Key Over Time)
Plot a single sensor value across the match timeline.

```bash
./gradlew runGraphing -Pargs="--log match.wpilog \
  --keys /RealOutputs/Drive/Velocity \
  --type time-series \
  --title 'Drive Velocity Over Match' \
  --ylabel 'Velocity (m/s)' \
  --output drive_velocity.png"
```

**Use cases:**
- Verify acceleration profiles smooth and steady
- Detect sudden stops or reversals
- Check for communication dropouts (flat lines)

**Output:** PNG with time on X-axis, signal value on Y-axis

### 2. Commanded vs Actual (Closed-Loop Validation)
Compare setpoint command against actual sensor reading.

```bash
./gradlew runGraphing -Pargs="--log match.wpilog \
  --keys /RealOutputs/Launcher/RPM,/RealOutputs/Launcher/DesiredRPM \
  --type overlay \
  --title 'Launcher Control Performance' \
  --ylabel 'RPM' \
  --legend 'Actual,Desired' \
  --output launcher_control.png"
```

**What to look for:**
- ✅ Actual tracks desired (overlay paths)
- ✅ Settling time < 0.5s (quick response)
- ❌ Lag or delay (commanded leads actual)
- ❌ Oscillation or ringing (tuning issue)

### 3. Scatter Plot (Correlation Analysis)
Plot one signal against another to find relationships.

```bash
./gradlew runGraphing -Pargs="--log match.wpilog \
  --keys /RealOutputs/Indexer/MotorCurrent,/RealOutputs/Indexer/Velocity \
  --type scatter \
  --title 'Indexer Current vs Velocity' \
  --xlabel 'Velocity (m/s)' \
  --ylabel 'Current (A)' \
  --output indexer_correlation.png"
```

**Interpretation:**
- Linear cloud = healthy motor (current proportional to load)
- Outliers = jam, stall, or spike events
- Horizontal spread at high current = slipping/binding

### 4. Multi-Axis Plot (Compare Different Units)
Plot signals with different units using separate Y-axes.

```bash
./gradlew runGraphing -Pargs="--log match.wpilog \
  --keys /RealOutputs/Intake/MotorCurrent,/RealOutputs/Intake/Velocity \
  --type multi-axis \
  --title 'Intake Motor Analysis' \
  --ylabel1 'Current (A)' \
  --ylabel2 'Velocity (m/s)' \
  --output intake_analysis.png"
```

### 5. Histogram (Distribution Analysis)
Analyze the distribution of a value to detect anomalies.

```bash
./gradlew runGraphing -Pargs="--log match.wpilog \
  --keys /UserCodeMS \
  --type histogram \
  --title 'Loop Execution Time Distribution' \
  --xlabel 'Time (ms)' \
  --ylabel 'Frequency' \
  --bins 50 \
  --output loop_timing.png"
```

**Use to detect:**
- Loop overruns (spikes)
- Inconsistent timing (multi-modal distribution)
- Performance regressions (compare against baseline)

### 6. Heatmap (2D Sensor Arrays or Time Windows)
Visualize data over time with color intensity.

```bash
./gradlew runGraphing -Pargs="--log match.wpilog \
  --keys /RealOutputs/Drive/ModuleStates \
  --type heatmap \
  --title 'Swerve Module Angles Over Match' \
  --output swerve_heatmap.png"
```

### 7. 2D Trajectory Plot (XY Path)
Plot the robot's estimated pose as a 2D path on the field.

```bash
./gradlew runGraphing -Pargs="--log match_auto.wpilog \
  --keys /RealOutputs/Drive/Viz/Pose \
  --type trajectory-2d \
  --title 'Autonomous Path Execution' \
  --field-image field_2026.png \
  --output auto_path.png"
```

**Elements plotted:**
- Actual trajectory (blue line)
- Desired trajectory (green line for comparison)
- Start/end points
- Field markings (if provided)

### 8. Comparative Multi-Log (Overlay Multiple Runs)
Compare the same metric across different matches or code versions.

```bash
./gradlew runGraphing -Pargs="--logs match1.wpilog,match2.wpilog,match3.wpilog \
  --keys /RealOutputs/Drive/Velocity \
  --type overlay-multi \
  --title 'Drive Velocity Consistency Across Matches' \
  --ylabel 'Velocity (m/s)' \
  --legend 'Match1,Match2,Match3' \
  --output velocity_comparison.png"
```

## Practical Debugging Workflows

### Workflow 1: Autonomous Validation
Verify that auto routine executed correctly:

```bash
./gradlew runGraphing -Pargs="--log match_auto.wpilog \
  --keys /RealOutputs/Drive/PositionX,/RealOutputs/Drive/PositionY,/RealOutputs/Drive/Velocity \
  --type trajectory-2d \
  --title 'Auto Route' \
  --output auto_validation.png"

./gradlew runGraphing -Pargs="--log match_auto.wpilog \
  --keys /RealOutputs/Drive/DesiredVelocity,/RealOutputs/Drive/Velocity \
  --type overlay \
  --title 'Velocity Tracking' \
  --output velocity_tracking.png"
```

**Check:**
- Robot reached all waypoints
- Smooth acceleration/deceleration
- No overshoot at endpoints
- Velocity matches desired profile

### Workflow 2: Shooter Tuning
Debug launcher RPM control:

```bash
# Generate comparison of launcher performance before/after tuning
./gradlew runGraphing -Pargs="--logs launcher_before.wpilog,launcher_after.wpilog \
  --keys /RealOutputs/Launcher/RPM \
  --type overlay-multi \
  --start 10.0 --end 20.0 \
  --title 'Launcher Tuning Improvement' \
  --legend 'Before,After' \
  --output launcher_tuning.png"
```

### Workflow 3: Mechanical Issue Detection
Find the exact moment a mechanism fails:

```bash
./gradlew runGraphing -Pargs="--log match_issue.wpilog \
  --keys /RealOutputs/Indexer/Velocity,/RealOutputs/Indexer/MotorCurrent,/RealOutputs/Indexer/Temperature \
  --type multi-axis \
  --title 'Indexer Failure Analysis' \
  --output indexer_failure.png"
```

**Look for:**
- Velocity drops to zero suddenly = jam
- Current spikes before failure = mechanical load
- Temperature spike = overload

### Workflow 4: Loop Performance Profiling
Identify which subsystems are consuming time:

```bash
./gradlew runGraphing -Pargs="--log match.wpilog \
  --keys /UserCodeMS,/Drive/LogMS,/Vision/LogMS,/Shooter/LogMS \
  --type stacked-area \
  --title 'Loop Time Budget Usage' \
  --ylabel 'Time (ms)' \
  --output loop_profiling.png"
```

## CLI Reference

### Graph Selection
- `--type <type>` : Graph type: `time-series`, `overlay`, `scatter`, `multi-axis`, `histogram`, `heatmap`, `trajectory-2d`, `overlay-multi`
- `--keys <k1,k2>` : Keys to plot (comma-separated)
- `--logs <l1,l2>` : Multiple log files for comparison (comma-separated)

### Customization
- `--title <text>` : Graph title
- `--xlabel <text>` : X-axis label
- `--ylabel <text>` : Y-axis label
- `--ylabel1 <text>` : First Y-axis label (multi-axis)
- `--ylabel2 <text>` : Second Y-axis label (multi-axis)
- `--legend <l1,l2>` : Custom legend labels
- `--start <seconds>` : Start time window
- `--end <seconds>` : End time window

### Output
- `--output <path>` : Output file path (default: `graph.png`)
- `--format <fmt>` : Output format: `png`, `pdf`, `svg` (default: png)
- `--dpi <number>` : Resolution in DPI (default: 150)
- `--style <style>` : Matplotlib style: `seaborn`, `dark_background`, `ggplot` (default: seaborn)
- `--size <width,height>` : Figure size in inches (default: 12,6)

### Advanced Options
- `--field-image <path>` : Overlay FRC field image (trajectory plots)
- `--bins <number>` : Number of histogram bins (default: 30)
- `--smooth` : Apply smoothing filter to reduce noise
- `--window <seconds>` : Smoothing window size (default: 0.05s)
- `--grid` : Show grid lines
- `--log-scale` : Use logarithmic scale for Y-axis

## Example Gallery

### Example 1: Elevator Position During Match
```bash
./gradlew runGraphing -Pargs="--log match.wpilog \
  --keys /RealOutputs/Elevator/Position,/RealOutputs/Elevator/DesiredPosition \
  --type overlay \
  --title 'Elevator Height Control' \
  --ylabel 'Height (inches)' \
  --grid \
  --output elevator.png"
```

### Example 2: Real vs Replay Comparison
```bash
./gradlew runGraphing -Pargs="--log match_sim.wpilog \
  --keys /RealOutputs/Drive/Velocity,/ReplayOutputs/Drive/Velocity \
  --type overlay \
  --title 'Sim Validation: Real vs Replay' \
  --ylabel 'Velocity (m/s)' \
  --legend 'Real,Replay' \
  --output real_vs_replay.png"
```

### Example 3: Motor Current Under Load
```bash
./gradlew runGraphing -Pargs="--log match.wpilog \
  --keys /RealOutputs/Climber/MotorCurrent \
  --type histogram \
  --title 'Climber Current Distribution' \
  --xlabel 'Current (A)' \
  --output climber_current_dist.png"
```

## Troubleshooting

### Graph is empty or shows no data
- Verify key exists: `./gradlew runListKeys -Pargs="log.wpilog" | grep -i velocity`
- Check time window: `--start` and `--end` may be outside data range
- Try with `--start 0 --end 999` to include full match

### Graph output looks pixelated
- Increase DPI: `--dpi 300`
- Increase size: `--size 16,8`

### Multi-axis plot Y-axes are misaligned
- This is normal; each axis auto-scales to its key
- Use `--scale-to <value>` to manually set range

### Trajectory plot not showing field
- Ensure field image file exists at path
- Use `--field-image path/to/2026_field.png`
- Field image should be 540×270 pixels (FRC standard)

## Related Skills
- [Math Analysis](math-analysis.md) - Numerical analysis and calculations
- [Log Reading](log-reading.md) - Parse and filter log files
- [Evidence-Driven Loop](evidence-driven-loop.md) - Use graphs in verification workflow
