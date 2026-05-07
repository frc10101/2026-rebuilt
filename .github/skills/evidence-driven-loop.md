---
name: Evidence-Driven Loop
description: Automated iteration workflow for AI agents - sandbox creation, simulation testing, log analysis, and conditional application of verified changes.
---

# Evidence-Driven Loop

This skill describes the core AI workflow for safely iterating on robot code: **Create a sandbox → Make changes → Run simulation → Analyze evidence → Apply only verified changes**.

This is the integration point that ties together all other agent skills.

## Core Philosophy

**Don't edit code blindly. Verify with evidence first.**

```
Traditional Development:
  Agent: "I'll change the PID constants"
  → [Code directly edited]
  → [Deploy and hope]
  → [Breaks on field?]

Evidence-Driven Loop:
  Agent: "I'll test PID changes in a sandbox"
  → [Create isolated workspace]
  → [Make changes]
  → [Run simulation]
  → [Analyze logs]
  → [Verify improvement]
  → [Apply only if better]
```

## The Five-Stage Loop

### Stage 1: Bounded Change in Sandbox

**Goal:** Make ONE focused change, test it in isolation.

```bash
# Create sandbox for the experiment
./gradlew createSandbox -Psandbox="launcher_kp_increase" \
  --description "Test increasing launcher kP from 0.1 to 0.15"

# Switch to sandbox
./gradlew switchSandbox -Psandbox="launcher_kp_increase"

# Make the bounded change (ONLY edit Launcher.java, ONLY change kP)
# File: workspaces/launcher_kp_increase/src/main/java/frc/robot/subsystems/Launcher.java
# Change:   kP = 0.1;  →  kP = 0.15;

# Verify what you changed
./gradlew sandboxDiff
# Output: Modified 1 file, 1 line changed
```

**Guardrails:**
- ✅ ONE concept per sandbox (not "launcher AND intake")
- ✅ Minimal files touched (not refactoring entire subsystem)
- ✅ No cascading effects (change is isolated)

### Stage 2: Run Simulation

**Goal:** Test the change in a controlled environment.

```bash
# Build the sandbox
./gradlew build --project-dir workspaces/launcher_kp_increase

# Run simulation with the new code
./gradlew simulateJava \
  --project-dir workspaces/launcher_kp_increase \
  -PenableSimulation=true \
  --args="--auto LaunchAuto --headless --log sandbox_sim.wpilog"

# Check exit code
# 0 = success, 1 = exception, 2 = watchdog timeout, 3 = assertion failure
```

**Decision Gate:**
- ✅ Exit code 0 → Proceed to analysis
- ❌ Exit code != 0 → Investigate error, may need to abort experiment

### Stage 3: Inspect Concrete Evidence

**Goal:** Analyze the log to see if the change improved things.

```bash
# Extract metrics to compare
./gradlew runMathAnalysis \
  --project-dir workspaces/launcher_kp_increase \
  -Pargs="--log sandbox_sim.wpilog \
    --keys /RealOutputs/Launcher/RPM,/RealOutputs/Launcher/DesiredRPM \
    --settling-time --threshold 50 \
    --json" > new_results.json

# Also get baseline (original code, no changes)
./gradlew simulateJava \
  -PenableSimulation=true \
  --args="--auto LaunchAuto --headless --log baseline_sim.wpilog"

./gradlew runMathAnalysis \
  -Pargs="--log baseline_sim.wpilog \
    --keys /RealOutputs/Launcher/RPM,/RealOutputs/Launcher/DesiredRPM \
    --settling-time --threshold 50 \
    --json" > baseline_results.json
```

**Compare Results:**
```json
// baseline_results.json
{
  "settling_time_ms": 850,
  "overshoot_percent": 2.1,
  "steady_state_error": 15
}

// new_results.json
{
  "settling_time_ms": 420,    // ✅ 50% better!
  "overshoot_percent": 1.8,   // ✅ Improved
  "steady_state_error": 8     // ✅ Lower error
}
```

### Stage 4: Review Results

**Goal:** Decide: is this change worth applying?

```bash
# Generate comparison graphs
./gradlew runGraphing -Pargs="--logs baseline_sim.wpilog,sandbox_sim.wpilog \
  --keys /RealOutputs/Launcher/RPM \
  --type overlay-multi \
  --legend 'Before,After' \
  --title 'Launcher Response Comparison' \
  --output comparison.png"
```

**Acceptance Criteria (Agent Decisions):**
- ✅ **Settling time improved** by >20%? → APPLY
- ✅ **No regressions** in other metrics? → APPLY
- ❌ **Overshoot increased** significantly? → REJECT
- ❌ **Steady state error worse**? → REJECT
- ⚠️ **Marginal improvement** (<5%)? → Keep experimenting

### Stage 5: Apply Verified Changes

**Goal:** If evidence supports the change, merge it to main workspace.

```bash
# Create backup before applying
./gradlew applySandbox -Psandbox="launcher_kp_increase" \
  --create-backup

# Backup created at: backups/pre_launcher_kp_increase.zip
# Changes applied to: src/main/java/frc/robot/subsystems/Launcher.java
```

**After Apply:**
```bash
# Verify it's in main workspace now
grep "kP = 0" src/main/java/frc/robot/subsystems/Launcher.java
# Output: kP = 0.15;  ✅ (verified)

# Delete the sandbox (experiment complete)
./gradlew deleteSandbox -Psandbox="launcher_kp_increase"
```

---

## Full Workflow Example: Iterative PID Tuning

### Iteration 1: Initial Experiment

```bash
# Step 1: Create sandbox
./gradlew createSandbox -Psandbox="pid_v1" --description "Try kP=0.15"
./gradlew switchSandbox -Psandbox="pid_v1"
# [Edit: kP = 0.15]

# Step 2: Simulate
./gradlew build --project-dir workspaces/pid_v1
./gradlew simulateJava --project-dir workspaces/pid_v1 \
  --args="--auto LaunchAuto --headless --log pid_v1.wpilog"

# Step 3: Analyze
./gradlew runMathAnalysis -Pargs="--log pid_v1.wpilog \
  --keys /RealOutputs/Launcher/RPM --settling-time --json" > pid_v1_metrics.json

# Result: settling_time=420ms (good improvement from 850ms baseline)

# Step 4: Apply
./gradlew applySandbox -Psandbox="pid_v1" --create-backup
```

### Iteration 2: Further Refinement

```bash
# Step 1: Create new sandbox (starting from updated baseline)
./gradlew createSandbox -Psandbox="pid_v2" --description "Try kP=0.18, kI=0.01"
./gradlew switchSandbox -Psandbox="pid_v2"
# [Edit: kP = 0.18, kI = 0.01]

# Step 2: Simulate
./gradlew build --project-dir workspaces/pid_v2
./gradlew simulateJava --project-dir workspaces/pid_v2 \
  --args="--auto LaunchAuto --headless --log pid_v2.wpilog"

# Step 3: Analyze
./gradlew runMathAnalysis -Pargs="--log pid_v2.wpilog \
  --keys /RealOutputs/Launcher/RPM --settling-time --json" > pid_v2_metrics.json

# Result: settling_time=380ms, but overshoot now 4.2% (too high)

# Step 4: REJECT (overshoot is worse)
./gradlew deleteSandbox -Psandbox="pid_v2" --reason "Overshoot regressed"
```

### Iteration 3: Fine-Tune Without Breaking

```bash
# Step 1: Try more conservative gains
./gradlew createSandbox -Psandbox="pid_v3" --description "Try kP=0.16, kI=0.005"
./gradlew switchSandbox -Psandbox="pid_v3"
# [Edit: kP = 0.16, kI = 0.005]

# Step 2: Simulate
./gradlew build --project-dir workspaces/pid_v3
./gradlew simulateJava --project-dir workspaces/pid_v3 \
  --args="--auto LaunchAuto --headless --log pid_v3.wpilog"

# Step 3: Analyze
./gradlew runMathAnalysis -Pargs="--log pid_v3.wpilog \
  --keys /RealOutputs/Launcher/RPM --settling-time --json" > pid_v3_metrics.json

# Result: settling_time=395ms, overshoot=1.9%, steady_state_error=±6 (all good!)

# Step 4: Apply
./gradlew applySandbox -Psandbox="pid_v3" --create-backup
```

### Final State
- ✅ Iteration 1 applied (kP=0.15)
- ✅ Iteration 3 applied (kP=0.16, kI=0.005)
- ❌ Iteration 2 rejected (overshoot too high)
- Result: Settling time improved 50% with no regressions

---

## Parallel Experiments (Expert Agents)

Multiple agents can test different approaches simultaneously:

```bash
# Agent A: Tests approach with increased kP
./gradlew createSandbox -Psandbox="approach_a_high_gains"
# [Test high gains, settling time improved but overshoot high]
# Result: REJECTED

# Agent B: Tests approach with more integral
./gradlew createSandbox -Psandbox="approach_b_more_integral"
# [Test more I term, more stable but slower]
# Result: REJECTED

# Agent C: Tests approach with derivative
./gradlew createSandbox -Psandbox="approach_c_with_derivative"
# [Test D term for damping, best settling time + low overshoot]
# Result: ACCEPTED

# Compare all approaches
./gradlew sandboxCompareMatrix \
  --sandboxes "approach_a_high_gains,approach_b_more_integral,approach_c_with_derivative" \
  --metrics "settling_time,overshoot,steady_state_error"

# Apply winning approach
./gradlew applySandbox -Psandbox="approach_c_with_derivative"
```

---

## Automated Decision Gate

For agents to apply changes automatically, use exit codes:

```bash
# Pseudo-code for agent decision logic

result=$(./gradlew runMathAnalysis \
  --log sandbox_sim.wpilog \
  --keys /RealOutputs/Launcher/RPM \
  --settling-time \
  --json)

settling_time=$(echo $result | jq '.settling_time_ms')
overshoot=$(echo $result | jq '.overshoot_percent')

# Decision logic
if [ $settling_time -lt 500 ] && [ $overshoot -lt 3.0 ]; then
  # Criteria met, apply the sandbox
  ./gradlew applySandbox -Psandbox="$SANDBOX_NAME"
  exit 0  # Success
else
  # Criteria not met, reject
  ./gradlew deleteSandbox -Psandbox="$SANDBOX_NAME"
  exit 1  # Failure
fi
```

---

## Safety Features

### Automatic Backups

Every `applySandbox` creates a backup:
```bash
./gradlew applySandbox -Psandbox="my_change" --create-backup
# Created: backups/pre_my_change_backup.zip
```

### Rollback Capability

If an applied change causes issues:
```bash
./gradlew rollbackSandbox -Psandbox="launcher_kp_increase"
# Reverted to state before launcher_kp_increase was applied
```

### Change Audit Trail

Every applied sandbox is tracked:
```bash
./gradlew sandboxHistory
# Output:
#   2026-05-07 14:23:00 | launcher_kp_increase | Applied  | kP 0.1→0.15
#   2026-05-07 14:28:15 | launcher_kp_increase | Applied  | kI 0→0.005
#   2026-05-07 14:35:42 | tuning_v2           | Applied  | ...
```

### Bounded Experimentation

Sandboxes enforce limits:
```bash
# Prevent experiments from touching too much code
./gradlew validateSandboxDiff --max-files 5 --max-lines 100
# Warning: Sandbox modifies 12 files
# Use --force to override
```

---

## Integration with Other Skills

```
Evidence-Driven Loop
├── Sandbox Experiments
│   ├── Create bounded changes
│   ├── Build and test in isolation
│   └── Rollback if needed
├── Simulation Agent
│   ├── Run autonomous routines
│   ├── Run teleop sequences
│   └── Generate simulation logs
├── Log Reading
│   ├── Filter log data
│   ├── Extract specific keys
│   └── Compute statistics
├── Math Analysis
│   ├── Calculate settling time
│   ├── Detect correlations
│   └── Generate metrics for decisions
├── Graphing
│   ├── Visualize changes
│   ├── Compare before/after
│   └── Generate evidence reports
└── NT4 Recording
    ├── Record real robot data
    ├── Compare sim vs real
    └── Build diagnostic baselines
```

---

## Troubleshooting

### "Change didn't improve things"
- Increase sandbox iterations
- Try larger increments in parameter values
- Compare against multiple test scenarios (not just one auto)

### "Simulation passes but robot fails"
- Sim-to-real gap; check simulator physics models
- Consider recording real robot data for comparison
- May need tuning adjustment for real hardware

### "Can't decide whether to apply"
- Define explicit acceptance criteria before experimenting
- Use multiple metrics (settling time AND overshoot, not just one)
- Compare against baseline and competing approaches

### "Experiments running forever"
- Set `--duration` limits on simulations
- Use `--headless` to avoid GUI slowdown
- Run multiple experiments in parallel on different agents

---

## Best Practices

1. **Document Experiments**: Always use `--description` when creating sandboxes
2. **Establish Baselines**: Run baseline measurements before experimenting
3. **One Change Per Sandbox**: Don't combine launcher + intake tuning
4. **Compare Against Baseline**: Always run baseline sim for comparison
5. **Automate Decisions**: Define thresholds upfront, use `--json` output
6. **Keep History**: Don't delete sandboxes immediately; they're evidence
7. **Backup Before Apply**: Always create backup when applying to main
8. **Cross-Validate**: If major change, test on real robot if possible

---

## Related Skills
- [Sandbox Experiments](sandbox-experiments.md) - Isolation and workspace management
- [Simulation Agent](simulation-agent.md) - Run tests to generate evidence
- [Log Reading](log-reading.md) - Parse simulation/robot logs
- [Math Analysis](math-analysis.md) - Analyze and validate evidence
- [Graphing](graphing.md) - Visualize evidence and comparisons
- [NT4 Recording](nt4-recording.md) - Record real robot data for validation
