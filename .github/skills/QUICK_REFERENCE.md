# 🚀 AI Agent Quick Reference Card

## The Five-Stage Loop (Memorize This!)

```
1️⃣  CREATE SANDBOX
    ./gradlew createSandbox -Psandbox="name" --description "what we're testing"

2️⃣  MAKE CHANGE
    Edit ONE file, change ONE thing
    ./gradlew sandboxDiff  (verify)

3️⃣  RUN TEST
    ./gradlew simulateJava --project-dir workspaces/name --args="--auto Auto --headless --log sim.wpilog"

4️⃣  ANALYZE
    ./gradlew runMathAnalysis -Pargs="--log workspaces/name/sim.wpilog --json > results.json"
    → Check if metrics improved

5️⃣  APPLY OR REJECT
    ./gradlew applySandbox -Psandbox="name" --create-backup  (if better)
    ./gradlew deleteSandbox -Psandbox="name"  (if worse)
```

---

## Key Metrics to Check

```json
// After sim, always check these:
{
  "settling_time_ms": 400,        // ✅ < 500ms = good
  "overshoot_percent": 2.1,       // ✅ < 3% = good
  "steady_state_error": 8,        // ✅ < 1% of range = good
  "correlation": 0.987            // ✅ > 0.95 = good
}
```

---

## Common Commands Cheat Sheet

### Creating & Managing Sandboxes
```bash
./gradlew createSandbox -Psandbox="experiment_name"
./gradlew switchSandbox -Psandbox="experiment_name"
./gradlew sandboxDiff --verbose
./gradlew applySandbox -Psandbox="experiment_name" --create-backup
./gradlew rollbackSandbox -Psandbox="experiment_name"
./gradlew deleteSandbox -Psandbox="experiment_name"
./gradlew listSandboxes
```

### Running Simulations
```bash
./gradlew simulateJava --project-dir workspaces/name \
  --args="--auto LaunchAuto --headless --log sim.wpilog"

./gradlew simulateJava --project-dir workspaces/name \
  --args="--auto LaunchAuto --log sim.wpilog"  # With GUI
```

### Analyzing Logs
```bash
# Find what keys are available
./gradlew runListKeys -Pargs="file.wpilog"

# Get statistics
./gradlew runMathAnalysis -Pargs="--log file.wpilog --keys /Key1,/Key2 --compact-json"

# Check settling time
./gradlew runMathAnalysis -Pargs="--log file.wpilog --keys /Launcher/RPM --settling-time --json"

# Generate graph
./gradlew runGraphing -Pargs="--log file.wpilog --keys /Launcher/RPM --type time-series --plot"
```

### Recording Live Data
```bash
# Record from robot in pit
./gradlew recordNT4Live --robot "10101" --duration 10 --output pit_test.wpilog

# Record simulation
./gradlew recordNT4Simulation --sim-args="--auto LaunchAuto --headless" \
  --output sim_test.wpilog
```

### Comparing Before/After
```bash
./gradlew runGraphing -Pargs="--logs baseline.wpilog,experiment.wpilog \
  --keys /Launcher/RPM --type overlay-multi --legend 'Before,After' --plot"

./gradlew sandboxCompare -Pcompare="sandbox1,sandbox2"
```

---

## Decision Flow

```
Does simulation succeed (exit code 0)?
├─ NO → Fix error → Try again
└─ YES → Extract metrics

Are metrics better than baseline?
├─ NO → Reject sandbox (delete)
└─ YES → Any regressions?
   ├─ YES → Reject (try again with different change)
   └─ NO → Apply sandbox! ✅
```

---

## File Structure You Need to Know

```
src/main/java/frc/robot/
├── subsystems/
│   ├── Launcher.java      ← Shooter PID tuning
│   ├── Drive.java         ← Swerve/drive tuning
│   ├── Intake.java        ← Intake control
│   └── ...
├── commands/              ← High-level behaviors
├── util/
│   ├── LogDumper.java    ← Parse logs
│   ├── ListKeys.java     ← List log keys
│   └── DriverLayout.json ← Controller mapping

.github/skills/
├── 00-IMPLEMENTATION_SUMMARY.md   ← Status
├── SKILLS_INDEX.md                ← Navigation
├── evidence-driven-loop.md        ← How to work
├── sandbox-experiments.md         ← Isolation
├── simulation-agent.md            ← Testing
├── log-reading.md                 ← Parsing
├── math-analysis.md               ← Analysis
├── graphing.md                    ← Visualization
├── nt4-recording.md               ← Data capture
├── replay-testing.md              ← Determinism
├── robot-description.md           ← Robot ref
└── game-info.md                   ← Game rules
```

---

## Common AI Tasks → Commands

### Task: "Tune Launcher Settling Time"
```bash
./gradlew createSandbox -Psandbox="launcher_tune"
./gradlew switchSandbox -Psandbox="launcher_tune"
# Edit: src/main/java/frc/robot/subsystems/Launcher.java (change kP)
./gradlew build --project-dir workspaces/launcher_tune
./gradlew simulateJava --project-dir workspaces/launcher_tune \
  --args="--auto LaunchAuto --headless --log sim.wpilog"
./gradlew runMathAnalysis \
  -Pargs="--log workspaces/launcher_tune/sim.wpilog --keys /RealOutputs/Launcher/RPM --settling-time --json"
# ✅ If settling_time < 500ms, apply:
./gradlew applySandbox -Psandbox="launcher_tune" --create-backup
```

### Task: "Debug Why Auto Path Fails"
```bash
./gradlew simulateJava -PenableSimulation=true \
  --args="--auto FailingAuto --headless --log debug.wpilog"
./gradlew runListKeys -Pargs="debug.wpilog"
./gradlew runMathAnalysis -Pargs="--log debug.wpilog \
  --keys /RealOutputs/Drive/PositionX,/RealOutputs/Drive/DesiredPositionX --correlate --json"
./gradlew runGraphing -Pargs="--log debug.wpilog --keys /RealOutputs/Drive/Velocity --type time-series --plot"
# Now identify root cause and create fix sandbox
```

### Task: "Compare Real vs Sim"
```bash
# Record sim
./gradlew simulateJava --args="--auto LaunchAuto --log sim.wpilog"
# Record real (during pit testing)
./gradlew recordNT4Live --robot "10101" --duration 15 --output real.wpilog
# Compare
./gradlew runGraphing -Pargs="--logs sim.wpilog,real.wpilog \
  --keys /Launcher/RPM --type overlay-multi --legend 'Sim,Real' --plot"
```

---

## Output Modes for AI

```bash
# Full output (default)
./gradlew runLogDumper -Pargs="--log file.wpilog --keys /Key"

# Machine-readable JSON
./gradlew runLogDumper -Pargs="--log file.wpilog --keys /Key --json"

# Compact (BEST FOR AGENTS)
./gradlew runLogDumper -Pargs="--log file.wpilog --keys /Key --compact-json"
# Output: {"key": "...", "min": 0, "max": 100, "avg": 50, "std_dev": 10}

# Statistics only
./gradlew runLogDumper -Pargs="--log file.wpilog --keys /Key --summary"

# Limited samples (prevent huge output)
./gradlew runLogDumper -Pargs="--log file.wpilog --keys /Key --max-samples 100"
```

---

## Safety Reminders

```
✅ ALWAYS use sandboxes for experiments
❌ NEVER edit main workspace directly

✅ ALWAYS create backup before applying
❌ NEVER apply untested changes to main

✅ ALWAYS compare against baseline
❌ NEVER apply changes without evidence

✅ ALWAYS use --json for parsing results
❌ NEVER hardcode expected output values
```

---

## Troubleshooting Checklist

- [ ] Sandbox created successfully?
- [ ] Only ONE file edited?
- [ ] Gradle build succeeds (`exit code 0`)?
- [ ] Simulation runs without crashing?
- [ ] Generated `.wpilog` file exists and > 1MB?
- [ ] Keys exist in log? (`runListKeys` confirms)
- [ ] Baseline comparison done?
- [ ] Metrics extracted with `--json`?
- [ ] Graph generated if visual check needed?
- [ ] Backup created before apply?

---

## Emergency Procedures

### "I applied a bad sandbox and broke things!"
```bash
./gradlew sandboxHistory  # See what was applied
./gradlew rollbackSandbox -Psandbox="bad_sandbox_name"
# Workspace restored to before bad sandbox was applied
```

### "I can't find the key I need"
```bash
./gradlew runListKeys -Pargs="file.wpilog" > all_keys.txt
cat all_keys.txt | grep -i "launcher"  # Search for similar
```

### "Simulation won't start"
```bash
# Try headless mode
./gradlew simulateJava --args="--headless --auto TestAuto"

# Check WPILib installation
which java

# Check for conflicts (kill any running sims)
ps aux | grep simulat
```

---

## Skills to Read First

1. **[00-IMPLEMENTATION_SUMMARY.md](00-IMPLEMENTATION_SUMMARY.md)** - What you have now (2 min read)
2. **[SKILLS_INDEX.md](SKILLS_INDEX.md)** - What each skill does (5 min read)
3. **[evidence-driven-loop.md](evidence-driven-loop.md)** - The workflow (10 min read)
4. **[sandbox-experiments.md](sandbox-experiments.md)** - How to sandbox (5 min read)

Then dive into specific skills as needed.

---

## Key Contacts

- **Robot Code Questions**: Check [robot-description.md](robot-description.md)
- **Workflow Questions**: Check [evidence-driven-loop.md](evidence-driven-loop.md)
- **Data Analysis Questions**: Check [log-reading.md](log-reading.md) + [math-analysis.md](math-analysis.md)
- **Visualization Questions**: Check [graphing.md](graphing.md)

---

**Version**: 1.0 (May 7, 2026)
**Status**: Complete and tested ✅
**Ready for AI agents**: YES 🤖
