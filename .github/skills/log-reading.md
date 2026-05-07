---

name: Log Reading

description: Instructions on how to read wpilog files and analyze them.

---

# Log Reading

This skill covers how to use the `LogDumper` utility to extract and analyze data from WPILOG files. It can be run from the command line using the `runLogDumper` Gradle task. Additional instructions for using this utility in simulation can be found in the Replay Testing skill.

## Using LogDumper

`LogDumper` is a utility class that parses `.wpilog` files and outputs the data in various formats, either to the console or to a CSV file. It also supports calculating statistics and correlations when the `--stats` flag is used.

### CLI Arguments

When running `LogDumper`, the following options are available:

- `--log <path>` : **(Required)** Path to the `.wpilog` file you wish to analyze.

- `--keys <k1,k2>` : (Optional) A comma-separated list of keys to filter the dump. If specified, the output will only contain these keys, usually output in a tabular format.

- `--start <seconds>` : (Optional) The start time in seconds for the data dump/analysis.

- `--end <seconds>` : (Optional) The end time in seconds for the data dump/analysis.

- `--out <csv_file>` : (Optional) Path to output the resulting data (or tabular format) as a CSV file.

- `--stats` : (Optional) Runs in statistics mode. This mode computes the mean, max, min, and standard deviation for `UserCodeMS` values and calculates the correlations between `UserCodeMS` and subsystem latencies (e.g., `/latencyPeriodicSec`).

### Running via Gradle

We use the `runLogDumper` Gradle task to make executing the utility straightforward. You pass the CLI arguments to the application using the `-Pargs` Gradle property.

### Useful Keys

When analyzing logs, here are some commonly used keys. Note that the `/RealOutputs` prefix should be replaced with `/ReplayOutputs` when running in replay mode:

- `/RealOutputs/Drive/Viz/Pose` - The robot’s estimated pose on the field, calculated by the drive subsystem using odometry and vision updates. Useful for tracking where the robot thinks it is during a match or simulation.

- `/RealOutputs/FieldSimulation/SimPose3d` - The ground-truth 3D pose of the robot on the field (available when running in simulation). Useful for comparing the robot’s estimated pose against its actual simulated position.

- `/DriverStation/Enabled` and `/DriverStation/Autonomous` - Useful for finding the precise start times of autonomous and teleop modes:

- **Start of Auto:** Look for when both `/DriverStation/Enabled` and `/DriverStation/Autonomous` are `true`.

- **Start of Teleop:** Look for when `/DriverStation/Enabled` is `true` but `/DriverStation/Autonomous` is `false`.

#### Basic Usage

To dump all log entries from a log file:

```bash
./gradlew runLogDumper -Pargs="--log /path/to/logfile.wpilog"
```

#### Finding Keys with ListKeys

It is highly recommended to use the `ListKeys` utility (`src/main/java/com/team254/lib/util/ListKeys.java`) to view all available keys in a log file before analysis, since keys often have specific prefixes (like `/RealOutputs` or `/ReplayOutputs`) that can be easy to mistype. You can run the `main` method of `ListKeys.java` passing the log file path as an argument. Or, you can use the built-in gradle task from your terminal:

```bash

./gradlew runListKeys -Pargs=“/path/to/logfile.wpilog”

```

#### Comparing Real vs. Replay Outputs

If you want to view specific keys, such as comparing real outputs against replay outputs, you can provide the `--keys` argument. Make sure to check out the Replay Testing skill for instructions on generating the `_sim.wpilog` files needed for comparison.  By default, keys have a prefix of `/RealOutputs` in sim or real mode and `/ReplayOutputs` in replay mode.

```bash
./gradlew runLogDumper -Pargs="--log match_sim.wpilog --keys /RealOutputs/Drive/Velocity,/ReplayOutputs/Drive/Velocity"
```

#### Running Statistics Analysis

If you want to analyze `UserCodeMS` and hardware latency correlation over a specific timeframe:

```bash
./gradlew runLogDumper -Pargs="--log /path/to/logfile.wpilog --stats --start 10.5 --end 40.0"
```

#### Saving to a CSV File

Output the parsed log entries directly to a CSV file for closer examination in spreadsheet software or plotting tools:

```bash
./gradlew runLogDumper -Pargs="--log /path/to/logfile.wpilog --keys /RealOutputs/Drive/Velocity --out drive_velocity.csv"
```

## Output Gating & Filtering

For AI agents working with large log files, reduce context by filtering output intelligently.

### JSON Output (Machine-Readable)

```bash
./gradlew runLogDumper -Pargs="--log match.wpilog --keys /RealOutputs/Launcher/RPM --json"
```

Output:
```json
{
  "key": "/RealOutputs/Launcher/RPM",
  "samples": 1500,
  "data_type": "double",
  "values": [0, 150, 450, 3000, 3010, 2995, ...],
  "timestamps": [0.0, 0.1, 0.2, 0.5, 0.6, 0.7, ...]
}
```

Use with agents:
```bash
result=$(./gradlew runLogDumper -Pargs="--log match.wpilog --keys /Shooter/RPM --json")
echo $result | jq '.values | max'  # Extract max RPM
```

### Compact JSON (Minimal Context)

```bash
./gradlew runLogDumper -Pargs="--log match.wpilog --keys /RealOutputs/Launcher/RPM --compact-json"
```

Output:
```json
{
  "key": "/RealOutputs/Launcher/RPM",
  "count": 1500,
  "min": 0,
  "max": 3015,
  "avg": 2850,
  "median": 3000,
  "std_dev": 125
}
```

**Perfect for agents:** Drastically reduces tokens while keeping decision-relevant info.

### Summary Mode (Statistics Only)

```bash
./gradlew runLogDumper -Pargs="--log match.wpilog --keys /RealOutputs/Drive/Velocity,/RealOutputs/Launcher/RPM --summary"
```

Output:
```
Statistics Summary
==================
/RealOutputs/Drive/Velocity:
  Samples: 2000 | Min: 0 m/s | Max: 4.5 m/s | Avg: 2.1 m/s | Std Dev: 1.3

/RealOutputs/Launcher/RPM:
  Samples: 1500 | Min: 0 RPM | Max: 3015 RPM | Avg: 2850 RPM | Std Dev: 125
```

### Threshold-Based Filtering

Only output data when values exceed thresholds:

```bash
./gradlew runLogDumper -Pargs="--log match.wpilog \
  --keys /RealOutputs/Indexer/MotorCurrent \
  --threshold 15.0 \
  --above"
```

Output: Only shows timepoints where current > 15A
```
Time    Current
12.5s   28.3A
12.6s   31.2A
12.7s   18.9A
12.8s   16.1A
35.2s   25.5A
...
```

Use case: Find when indexer jams (high current spike)

### Output Gating with Limits

Prevent huge outputs by capping data:

```bash
./gradlew runLogDumper -Pargs="--log match.wpilog \
  --keys /RealOutputs/Drive/Velocity \
  --max-samples 100"
```

Outputs only 100 evenly-spaced samples instead of full 10,000 samples.

### Combined Filtering

Mix and match for powerful analysis:

```bash
# Find anomalies: high current in launcher during auto
./gradlew runLogDumper -Pargs="--log match.wpilog \
  --keys /RealOutputs/Launcher/MotorCurrent,/DriverStation/Autonomous \
  --start 0 --end 15 \
  --threshold 12.0 \
  --above \
  --summary"

# Output only when condition met:
#   Time window: 0-15s (autonomous only)
#   Current: > 12A
#   Show: statistical summary (not raw values)
```

## Advanced CLI Arguments

### Output Control
- `--json` : JSON output (one object per key)
- `--compact-json` : Compact JSON with statistics only
- `--summary` : Show only statistics, no raw data
- `--csv <file>` : Export to CSV file
- `--max-samples <n>` : Limit output to N evenly-spaced samples
- `--table` : Tabular console output (default)

### Filtering & Gating
- `--threshold <value>` : Filter by value threshold
- `--above` : Keep only values > threshold
- `--below` : Keep only values < threshold
- `--within <min,max>` : Keep only values between min and max
- `--window <seconds>` : Smooth/average over time window

## Use Cases for Agents

### Use Case 1: Automated Anomaly Detection

Agent detects when something went wrong:

```bash
# Find all current spikes > 30A (indicates jam)
./gradlew runLogDumper -Pargs="--log match.wpilog \
  --keys /RealOutputs/*/MotorCurrent \
  --threshold 30 \
  --above \
  --compact-json" > anomalies.json

# If any results, alert: "Possible jam detected at [time]"
```

### Use Case 2: Context-Efficient Reporting

Agent generates compact report for human review:

```bash
./gradlew runLogDumper -Pargs="--log match.wpilog \
  --keys /RealOutputs/Drive/Velocity,/RealOutputs/Launcher/RPM,/UserCodeMS \
  --compact-json > match_summary.json"

# Result: < 1KB of JSON with essential stats
# Instead of 100KB+ of raw data
```

### Use Case 3: Conditional Verification

Agent verifies specific conditions were met:

```bash
# Was launcher ever above 2900 RPM?
./gradlew runLogDumper -Pargs="--log match.wpilog \
  --keys /RealOutputs/Launcher/RPM \
  --threshold 2900 \
  --above \
  --max-samples 1" | grep -q "2900"

if [ $? -eq 0 ]; then
  echo "Launcher reached target RPM ✅"
else
  echo "Launcher never reached target RPM ❌"
fi
```

## Related Skills
- [Math Analysis](math-analysis.md) - Numerical analysis and calculations
- [Graphing](graphing.md) - Visualize log data
- [Replay Testing](replay-testing.md) - Deterministic validation
- [Evidence-Driven Loop](evidence-driven-loop.md) - Use log reading in verification workflow
