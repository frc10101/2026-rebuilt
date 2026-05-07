---
name: NT4 Recording
description: Record NetworkTables 4.0 data from live robots or simulations to WPILOG files for later analysis, diagnostic capture, and post-match review.
---

# NT4 Recording

This skill enables capturing robot telemetry and diagnostic data via NetworkTables 4.0 (NT4) and saving it to `.wpilog` format for offline analysis. This bridges the gap between real-time robot operation and detailed log analysis.

## Core Use Cases

- **Live Robot Diagnostics**: Record data from competition robot without modifying deployed code
- **Simulation Recording**: Capture sim runs to validate behavior without AdvantageKit logging
- **Pit Debug**: Quick diagnostics during competitions without USB connection
- **Continuous Monitoring**: Record team events, scrimmages, or practice to build diagnostic baselines
- **Machine Learning**: Collect training data from match recordings

## Recording from Simulation

### Basic Sim Recording

Record NT4 output from WPILib simulation:

```bash
# Start NT4 recording, run simulation, save to log
./gradlew recordNT4Simulation -Psimulation="simulateJava" \
  --sim-args="--auto LaunchAuto --headless" \
  --output sim_launch_auto.wpilog
```

This:
1. Starts NT4 server
2. Launches simulation with `--auto LaunchAuto --headless`
3. Records all NT4 traffic to `sim_launch_auto.wpilog`
4. Automatically stops when simulation ends

### With Explicit Duration

```bash
# Record for exactly 135 seconds (full match)
./gradlew recordNT4Simulation \
  --sim-args="--headless --teleop 135" \
  --duration 135 \
  --output match_sim.wpilog
```

### Filtering NT4 Keys

Record only specific keys to reduce file size:

```bash
# Record only drive and launcher data
./gradlew recordNT4Simulation \
  --sim-args="--auto FullAuto --headless" \
  --keys "/Drive/*,/Launcher/*" \
  --output drive_launcher_sim.wpilog
```

## Recording from Live Robot

### Setup: Enable NT4 Recording on Robot

Your robot code must publish to NetworkTables:

```java
// Example: In your subsystem or Main class
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StructPublisher;

public class Launcher {
  private StructPublisher<LauncherState> launcherStatePublisher;

  public Launcher() {
    var inst = NetworkTableInstance.getDefault();
    launcherStatePublisher =
      inst.getStructTopic("/Launcher/State", LauncherState.struct)
          .publish();
  }

  public void periodic() {
    // ... control logic ...
    launcherStatePublisher.set(new LauncherState(...));
  }
}
```

### Record from Live Robot in Pit

```bash
# Connect to robot via mDNS
./gradlew recordNT4Live --robot "10101" \
  --output pit_diagnostic.wpilog
```

This connects to `10101.local` and starts recording.

### Record During Match

```bash
# Record for exactly 150 seconds (auto + teleop + buffer)
./gradlew recordNT4Live --robot "10101" \
  --duration 150 \
  --keys "/Drive/*,/Shooter/*" \
  --output match_data.wpilog
```

### Record from Specific NT4 Server

```bash
# Connect to custom NT4 server (e.g., running on laptop)
./gradlew recordNT4Live --server localhost:5800 \
  --output local_server_data.wpilog
```

## Comparing Sim vs Real

### Workflow: Record Both and Compare

```bash
# 1. Record simulation
./gradlew recordNT4Simulation \
  --sim-args="--auto LaunchAuto --headless" \
  --output auto_sim.wpilog

# 2. Deploy to robot and record real match
./gradlew recordNT4Live --robot "10101" \
  --duration 15 \
  --output auto_real.wpilog

# 3. Compare
./gradlew runGraphing -Pargs="--logs auto_sim.wpilog,auto_real.wpilog \
  --keys /Launcher/RPM \
  --type overlay-multi \
  --title 'Launcher: Sim vs Real' \
  --legend 'Sim,Real' \
  --output sim_vs_real.png"
```

**Analysis points:**
- ✅ Curves should overlay closely
- ❌ Lag or delay = sim config issue
- ❌ Different settling behavior = tuning issue

## Advanced Recording Scenarios

### Scenario 1: Pit Scouting System

Record standardized tests from all robots at an event:

```bash
#!/bin/bash
# record_pit_tests.sh

for team_num in 10101 10102 10103; do
  echo "Recording $team_num..."

  # Run spin-up test
  ./gradlew recordNT4Live --robot "$team_num" \
    --duration 5 \
    --output "pit_spin_test_${team_num}.wpilog"

  sleep 2

  # Run drive test
  ./gradlew recordNT4Live --robot "$team_num" \
    --duration 10 \
    --output "pit_drive_test_${team_num}.wpilog"
done
```

Then analyze all recordings:
```bash
./gradlew runMathAnalysis -Pargs="--logs pit_spin_test_*.wpilog \
  --keys /Shooter/RPM \
  --settling-time \
  --compact"
```

### Scenario 2: Thermal Characterization

Record motor temperatures under known loads:

```bash
# Record intake spinning for 60 seconds
./gradlew recordNT4Live --robot "10101" \
  --duration 60 \
  --keys "/Intake/MotorCurrent,/Intake/MotorTemp,/Intake/Velocity" \
  --output thermal_test_intake.wpilog

# Analyze temperature rise
./gradlew runMathAnalysis -Pargs="--log thermal_test_intake.wpilog \
  --keys /Intake/MotorTemp \
  --derivative \
  --summary"
```

### Scenario 3: Match Review Workflow

After each match, automatically record and analyze:

```bash
# 1. Record match data (done during match)
# Data stored: match_data.wpilog

# 2. Immediately analyze
./gradlew runMathAnalysis -Pargs="--log match_data.wpilog \
  --keys /Drive/Velocity,/Shooter/RPM,/Indexer/BeamBreak \
  --settling-time \
  --threshold-check \
  --json > match_report.json"

# 3. Generate visual report
./gradlew runGraphing -Pargs="--log match_data.wpilog \
  --keys /Drive/Velocity,/Shooter/RPM,/UserCodeMS \
  --type multi-axis \
  --output match_report.png"

# 4. Upload for team review
# scp match_report.json match_report.png coach@team-server:/match_data/
```

### Scenario 4: Continuous Logging During Practice

Record background data while testing:

```bash
# Start long-running recording session
./gradlew recordNT4Live --robot "10101" \
  --duration 3600 \
  --keys "/Shooter/*,/Drive/Velocity,/UserCodeMS" \
  --output practice_session_1hr.wpilog &

# ... run practice, autos, drills ...

# Recording happens in background, process log after practice ends
```

## NT4 Key Filtering

Control which data gets recorded to reduce file size:

### Filter Patterns

```bash
# Record everything from Launcher subsystem
--keys "/Launcher/*"

# Record specific subsystems
--keys "/Drive/*,/Shooter/*,/Intake/*"

# Record everything except high-frequency data
--keys "/*" --exclude "/DriverStation/RawButtons*,/DriverStation/RawAxis*"

# Record only setpoints (not raw values)
--keys "/*Desired*,/*Command*,/*Setpoint*"
```

### File Size Optimization

```bash
# Default: record everything at full resolution
# Result: ~50MB per match

# Optimization 1: Filter keys
./gradlew recordNT4Live --robot "10101" \
  --keys "/Drive/Velocity,/Shooter/RPM,/Indexer/HasNote" \
  --output optimized_match.wpilog
# Result: ~5MB per match (90% reduction)

# Optimization 2: Reduce sample rate
./gradlew recordNT4Live --robot "10101" \
  --hz 10 \
  --output low_freq_match.wpilog
# Records at 10Hz instead of 100Hz
# Result: ~2MB per match (95% reduction)

# Optimization 3: Combine
./gradlew recordNT4Live --robot "10101" \
  --keys "/Drive/Velocity,/Shooter/RPM" \
  --hz 20 \
  --output compact_match.wpilog
# Result: ~1MB per match
```

## CLI Reference

### Simulation Recording
- `--recordNT4Simulation` : Record from simulation
- `--sim-args <args>` : Arguments to pass to simulation (e.g., `--auto AutoName --headless`)
- `--duration <seconds>` : Recording duration (default: sim decides)
- `--output <path>` : Output `.wpilog` file path (default: `nt4_recording.wpilog`)

### Live Robot Recording
- `--recordNT4Live` : Record from live robot
- `--robot <team_num>` : Team number or `<team>.local` format (e.g., `10101` or `localhost:5800`)
- `--server <host:port>` : Custom NT4 server (default: `<team>.local:5810`)
- `--duration <seconds>` : Recording duration in seconds
- `--keys <filter>` : Comma-separated key patterns (default: all keys)
- `--exclude <filter>` : Keys to exclude (comma-separated patterns)
- `--hz <frequency>` : Sample rate in Hz (default: 100)
- `--output <path>` : Output file path

### Advanced Options
- `--timeout <seconds>` : Stop recording if no data for N seconds (default: 60)
- `--prefix` : Add prefix to all keys (e.g., `--prefix /Match1`)
- `--compress` : Compress output WPILOG (saves ~30% space)
- `--validate` : Verify output WPILOG integrity

## Troubleshooting

### "Connection refused" when recording live robot
- Robot might not be on network
- Check: `ping 10101.local`
- Ensure laptop and robot are on same network
- Try explicit server: `--server <robot-ip>:5810`

### Recording stops unexpectedly
- Network timeout (default: 60s without data)
- Use `--timeout 300` for longer timeout
- Check robot console for NT4 errors

### WPILOG file is corrupted
- Ensure recording completed: check file size > 1MB
- Validate: `./gradlew validateWPILOG -Plog="file.wpilog"`
- Try recording again

### File size is too large
- Use `--keys` to filter to important data only
- Reduce sample rate: `--hz 20` instead of 100
- Enable compression: `--compress`

### Can't find robot via mDNS
- Use explicit IP: `--server 10.101.51.1:5810`
- Check robot's IP from Driver Station
- Ensure IT allows mDNS in network

## Recording Best Practices

1. **Filter to Relevant Keys**: Don't record everything; use `--keys` to focus
2. **Use Consistent Names**: Name recordings with date, test type, team number
3. **Record Baseline**: Early practice sessions = diagnostic baseline
4. **Post-Match Review**: Always record and analyze in-competition matches
5. **Compress Large Recordings**: Use `--compress` for files > 100MB
6. **Backup Important Data**: Copy key recordings before deleting from robot
7. **Cross-Check**: Compare sim vs real when behavior differs

## Example Recording Schedule

### Practice Session
```bash
# Start of practice
./gradlew recordNT4Live --robot "10101" \
  --duration 14400 \  # 4 hours
  --keys "/Shooter/RPM,/Drive/Velocity,/UserCodeMS" \
  --output practice_full_session.wpilog &
```

### Competition Day
```bash
# Before each match
./gradlew recordNT4Live --robot "10101" \
  --duration 150 \  # Auto + Teleop + buffer
  --keys "*" \
  --output match_${MATCH_NUMBER}.wpilog
```

## Related Skills
- [Log Reading](log-reading.md) - Analyze recorded NT4 data
- [Math Analysis](math-analysis.md) - Process recorded sensor data
- [Graphing](graphing.md) - Visualize recorded matches
- [Simulation Agent](simulation-agent.md) - Generate sim data to record
