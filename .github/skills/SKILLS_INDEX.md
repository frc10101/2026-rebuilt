---
name: Skills Index
description: Master index and guide for all AI agent capabilities available in this workspace.
---

# AI Agent Skills Index

This is the complete guide to what AI agents can do with this 2026 WakaWaka robot project. These skills enable evidence-driven iteration, safe experimentation, and rapid debugging.

## Quick Navigation

### 🎯 For First-Time Users
Start here to understand the AI workflow:
1. **[Evidence-Driven Loop](evidence-driven-loop.md)** - The core workflow: sandbox → simulate → analyze → apply
2. **[Robot Description](robot-description.md)** - Understand what WakaWaka can do
3. **[Simulation Agent](simulation-agent.md)** - Run tests without hardware

### 🔧 For Code Changes & Tuning
Use these skills when modifying robot code:
1. **[Sandbox Experiments](sandbox-experiments.md)** - Test changes safely in isolation
2. **[Simulation Agent](simulation-agent.md)** - Verify changes work in simulation
3. **[Log Reading](log-reading.md)** - Analyze simulation results
4. **[Math Analysis](math-analysis.md)** - Validate performance metrics
5. **[Graphing](graphing.md)** - Visualize before/after comparison

### 📊 For Analysis & Debugging
Use these when investigating problems:
1. **[Log Reading](log-reading.md)** - Parse and filter log files
2. **[Math Analysis](math-analysis.md)** - Calculate statistics and trends
3. **[Graphing](graphing.md)** - Visualize data patterns
4. **[NT4 Recording](nt4-recording.md)** - Capture live robot data

### 🤖 Complete Skill Catalog

| Skill | Purpose | When to Use |
|-------|---------|------------|
| **[Evidence-Driven Loop](evidence-driven-loop.md)** | Core AI workflow for safe iteration | Always: start here for AI tasks |
| **[Robot Description](robot-description.md)** | WakaWaka architecture and capabilities | Understanding the robot |
| **[Simulation Agent](simulation-agent.md)** | Run autonomous and teleop tests | Testing code without hardware |
| **[Sandbox Experiments](sandbox-experiments.md)** | Isolated code change testing | Before applying any changes |
| **[Log Reading](log-reading.md)** | Parse and analyze `.wpilog` files | After every simulation run |
| **[Math Analysis](math-analysis.md)** | Statistical and signal processing | Validating control system performance |
| **[Graphing](graphing.md)** | Matplotlib visualizations | Understanding complex data |
| **[Replay Testing](replay-testing.md)** | Deterministic validation | Comparing real vs replay behavior |
| **[NT4 Recording](nt4-recording.md)** | Capture robot telemetry | Recording match data and diagnostics |
| **[Game Info](game-info.md)** | 2026 game rules and field layout | Understanding match strategy |

---

## The AI Agent Workflow

### For Code Changes (The Five-Stage Loop)

```
1. 📦 CREATE SANDBOX
   ./gradlew createSandbox -Psandbox="experiment_name"

2. ✏️ MAKE BOUNDED CHANGE
   Edit ONE file, change ONE thing
   ./gradlew sandboxDiff  (verify changes)

3. ▶️ RUN SIMULATION
   ./gradlew simulateJava --project-dir workspaces/experiment_name ...
   (Generates: sandbox_sim.wpilog)

4. 🔍 ANALYZE EVIDENCE
   ./gradlew runMathAnalysis --log sandbox_sim.wpilog ...
   ./gradlew runGraphing --log sandbox_sim.wpilog ...
   (Compare against baseline)

5. ✅ APPLY IF BETTER
   ./gradlew applySandbox -Psandbox="experiment_name" --create-backup
   (Only if metrics improved)
```

**Resources:**
- [Evidence-Driven Loop](evidence-driven-loop.md) - Full workflow guide
- [Sandbox Experiments](sandbox-experiments.md) - Sandbox operations
- [Simulation Agent](simulation-agent.md) - Running tests

---

## Common Agent Tasks

### Task: "Tune the Launcher PID Constants"

**Skill Sequence:**
```
1. Create sandbox for experiment
   → Sandbox Experiments
2. Edit Launcher.java with new constants
   → Read Robot Description to find subsystem
3. Build and simulate
   → Simulation Agent
4. Run math analysis on settling time
   → Math Analysis
5. Generate comparison graph
   → Graphing
6. If metrics improved, apply changes
   → Evidence-Driven Loop
```

**Example Commands:**
```bash
./gradlew createSandbox -Psandbox="launcher_pid_v1"
./gradlew switchSandbox -Psandbox="launcher_pid_v1"
# [Edit: Launcher.java, change kP]
./gradlew build --project-dir workspaces/launcher_pid_v1
./gradlew simulateJava --project-dir workspaces/launcher_pid_v1 --args="--auto LaunchAuto --headless --log sim.wpilog"
./gradlew runMathAnalysis -Pargs="--log workspaces/launcher_pid_v1/sim.wpilog --keys /RealOutputs/Launcher/RPM --settling-time --json"
./gradlew runGraphing -Pargs="--log workspaces/launcher_pid_v1/sim.wpilog --keys /RealOutputs/Launcher/RPM --type time-series --plot"
./gradlew applySandbox -Psandbox="launcher_pid_v1" --create-backup
```

### Task: "Diagnose Why Auto Path Fails"

**Skill Sequence:**
```
1. Run the autonomous in simulation
   → Simulation Agent
2. Analyze the generated log
   → Log Reading + Math Analysis
3. Plot the trajectory
   → Graphing
4. Check for velocity/position lag
   → Math Analysis (correlation)
5. Identify root cause
6. Create sandbox to test fix
   → Evidence-Driven Loop
```

**Example Commands:**
```bash
./gradlew simulateJava -PenableSimulation=true --args="--auto FailingAuto --headless --log debug_auto.wpilog"
./gradlew runListKeys -Pargs="debug_auto.wpilog"  # Find relevant keys
./gradlew runMathAnalysis -Pargs="--log debug_auto.wpilog --keys /RealOutputs/Drive/PositionX,/RealOutputs/Drive/DesiredPositionX --correlate"
./gradlew runGraphing -Pargs="--log debug_auto.wpilog --keys /RealOutputs/Drive/Velocity,/RealOutputs/Drive/PositionX --type trajectory-2d --plot"
```

### Task: "Record Live Robot Data and Analyze"

**Skill Sequence:**
```
1. Connect to robot at event
   → NT4 Recording
2. Record match or pit test
   → NT4 Recording (live robot)
3. Analyze recorded data
   → Log Reading + Math Analysis
4. Generate diagnostic report
   → Graphing
5. Compare against simulation baseline
   → Log Reading (multi-log comparison)
```

**Example Commands:**
```bash
./gradlew recordNT4Live --robot "10101" --duration 150 --output match_10101.wpilog
./gradlew runMathAnalysis -Pargs="--log match_10101.wpilog --keys /Launcher/RPM,/Drive/Velocity --settling-time --compact-json"
./gradlew runGraphing -Pargs="--logs auto_sim.wpilog,match_10101.wpilog --keys /Launcher/RPM --type overlay-multi --legend 'Sim,Real' --plot"
```

---

## Key Concepts

### Evidence-Driven Development
Don't edit blindly. Generate evidence first:
- ✅ Make change in sandbox
- ✅ Run simulation
- ✅ Analyze logs
- ✅ Compare metrics
- ✅ Apply only if better

### Bounded Experimentation
Keep changes small and isolated:
- ONE sandbox per concept
- ONE file modified per experiment
- Easy to rollback if wrong
- Parallel experiments possible

### Safety First
Protect the main workspace:
- Sandboxes are isolated copies
- Automatic backups before apply
- Full rollback capability
- Audit trail of all changes

### Context Efficiency
Help AI agents work better:
- Use `--json` for machine-readable output
- Use `--compact-json` for summaries
- Use `--summary` to reduce tokens
- Use `--max-samples` to cap output

---

## Integration Diagram

```
┌─ Evidence-Driven Loop (Master Workflow) ──────────────────┐
│                                                             │
│  ┌─ Sandbox Experiments ──────┐   ┌─ Analysis Pipeline ─┐ │
│  │ • Create sandboxes         │   │ • Log Reading       │ │
│  │ • Isolated testing         │──→│ • Math Analysis     │ │
│  │ • Easy rollback            │   │ • Graphing          │ │
│  └────────────────────────────┘   └─────────────────────┘ │
│           │                              ▲                  │
│           v                              │                  │
│  ┌─────────────────────────┐   ┌────────────────────┐      │
│  │ Simulation Agent        │   │ NT4 Recording      │      │
│  │ • Auto/Teleop tests     │───│ • Live robot data  │      │
│  │ • Generate .wpilog      │   │ • Sim validation   │      │
│  └─────────────────────────┘   └────────────────────┘      │
│                                                             │
│                    ✅ Apply if Better                       │
└─────────────────────────────────────────────────────────────┘
```

---

## Quick Reference: CLI Patterns

### Common Patterns for Agents

```bash
# Pattern 1: Run a complete iteration
./gradlew createSandbox -Psandbox="$NAME"
./gradlew switchSandbox -Psandbox="$NAME"
# [edit files]
./gradlew build --project-dir workspaces/$NAME
./gradlew simulateJava --project-dir workspaces/$NAME --args="..." --log sim.wpilog
./gradlew runMathAnalysis -Pargs="--log workspaces/$NAME/sim.wpilog --json > results.json"
./gradlew applySandbox -Psandbox="$NAME" --create-backup

# Pattern 2: Analyze a log file
./gradlew runListKeys -Pargs="log.wpilog" | head -20
./gradlew runMathAnalysis -Pargs="--log log.wpilog --keys KEY1,KEY2 --settling-time --compact-json"
./gradlew runGraphing -Pargs="--log log.wpilog --keys KEY1 --type time-series --plot"

# Pattern 3: Compare two runs
./gradlew runGraphing -Pargs="--logs run1.wpilog,run2.wpilog --keys KEY --type overlay-multi --plot"
./gradlew sandboxCompare -Pcompare="sandbox1,sandbox2" --metrics settling_time,overshoot

# Pattern 4: Record and analyze live data
./gradlew recordNT4Live --robot "10101" --duration 150 --output match.wpilog
./gradlew runMathAnalysis -Pargs="--log match.wpilog --json > metrics.json"
```

---

## Troubleshooting Guide

### Skill Not Working?

1. **"Command not found"**
   - Ensure you're in the project root directory
   - Run `./gradlew tasks` to list all available tasks

2. **"Simulation won't start"**
   - Check WPILib installation
   - Ensure no other simulation is running
   - Try `--headless` flag

3. **"Log file is empty or corrupt"**
   - Verify simulation completed successfully (exit code 0)
   - Check file size: `ls -lh logfile.wpilog`
   - Try validating: `./gradlew validateWPILOG -Plog="logfile.wpilog"`

4. **"Can't find keys in log"**
   - Use `./gradlew runListKeys` to see available keys
   - Check for typos in key names (case-sensitive)
   - Verify key exists in expected time window with `--start` and `--end`

### Performance Issues?

- Reduce log file size: `--max-samples 100` in LogDumper
- Use `--compact-json` instead of full JSON
- Filter keys: `--keys "/Drive/*,/Shooter/*"` instead of all keys
- Increase Gradle heap: `export GRADLE_OPTS="-Xmx2g"`

---

## Best Practices for AI Agents

1. **Always Create Baseline**: Run original code in simulation before experimenting
2. **Use Sandboxes**: Never edit main workspace directly
3. **Compare Evidence**: Always compare against baseline, not just absolute values
4. **Document Decisions**: Use `--description` to explain reasoning
5. **Automate Decisions**: Define thresholds upfront, use JSON output for parsing
6. **Create Backups**: Always `--create-backup` before applying changes
7. **Keep Experiment History**: Don't delete sandboxes; they're evidence

---

## Related Resources

- **[2026 Game Info](game-info.md)** - Game rules for strategy context
- **[Robot Description](robot-description.md)** - Detailed system documentation
- **[WPILib Docs](https://docs.wpilib.org)** - Official WPILib reference
- **[AdvantageKit Docs](https://docs.advantagekit.org)** - Logging and replay system

---

## Getting Started: Three-Step Guide

### Step 1: Understand the Robot
Read: **[Robot Description](robot-description.md)**
- Learn subsystems: Drive, Launcher, Intake, etc.
- Find key files and classes
- Understand hardware and vendor libraries

### Step 2: Learn the Workflow
Read: **[Evidence-Driven Loop](evidence-driven-loop.md)**
- Understand five-stage iteration process
- See full worked examples
- Learn decision gates and safety features

### Step 3: Try Your First Experiment
1. Create sandbox: `./gradlew createSandbox -Psandbox="my_first_test"`
2. Make a small change (e.g., edit a constant)
3. Run simulation: `./gradlew simulateJava --project-dir workspaces/my_first_test ...`
4. Analyze: `./gradlew runMathAnalysis --log workspaces/my_first_test/sim.wpilog ...`
5. Apply or reject

You're now ready to help the team iterate safely! 🚀

---

## FAQ

**Q: Can I edit the main workspace directly, or must I use sandboxes?**
A: Always use sandboxes for experiments. Main workspace should only be updated through verified sandbox applications. This prevents accidental breakage.

**Q: How do I know if a change is actually better?**
A: Define metrics first (settling time, overshoot, error, etc.). Use `--json` output to extract numbers. Compare against baseline. Use graphing to visualize.

**Q: What if I make a mistake?**
A: Sandboxes are isolated, so mistakes don't affect main workspace. If you apply a bad sandbox to main, use `./gradlew rollbackSandbox` to undo.

**Q: How do I speed up iteration?**
A: Use multiple sandboxes in parallel. Run simulations headless with `--headless`. Use compact output modes to reduce context.

**Q: Can I record data from the live robot?**
A: Yes, use NT4 Recording skill. Robot must publish to NetworkTables. Works for pit diagnostics and match analysis.
