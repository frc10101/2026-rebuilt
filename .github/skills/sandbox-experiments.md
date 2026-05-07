---
name: Sandbox Experiments
description: Create isolated workspaces to test robot code changes without mutating the main repository. Build, test, verify, and safely apply only the changes that work.
---

# Sandbox Experiments

This skill enables AI agents to safely experiment with robot code changes in isolated sandboxes before applying them to the main workspace. This is critical for maintaining code safety and enabling high-confidence iteration.

## Core Concept: Evidence-First Development

Instead of directly editing your robot code:
```
❌ Agent reads code → makes changes → hopes it works
```

Use sandboxes for verification:
```
✅ Agent creates sandbox → makes changes → runs sim → analyzes logs → applies ONLY verified changes
```

## Creating a Sandbox

A sandbox is an isolated copy of your robot project where experiments can run safely.

### Quick Start: Create a Bounded Experiment

```bash
# Create a new sandbox for testing launcher tuning
./gradlew createSandbox -Psandbox="launcher_tuning_attempt_1" \
  --description "Test new PID constants for launcher"
```

This creates:
```
workspaces/
  └── launcher_tuning_attempt_1/
      ├── src/
      ├── build.gradle
      └── .sandbox-meta.json  (tracks changes)
```

### Template-Based Sandboxes

Start with a known-good template:

```bash
# Create sandbox from specific git commit (known working version)
./gradlew createSandbox -Psandbox="launcher_tuning" \
  --from-commit abc123def456

# Create sandbox from a branch
./gradlew createSandbox -Psandbox="vision_rewrite" \
  --from-branch feature/vision-refactor

# Create sandbox from a tagged release
./gradlew createSandbox -Psandbox="tuning_experiment" \
  --from-tag v1.2.0
```

## Working in a Sandbox

### Switch to Sandbox Context

```bash
# All subsequent commands operate in this sandbox
./gradlew switchSandbox -Psandbox="launcher_tuning_attempt_1"
```

Verification:
```bash
# Confirm which workspace you're in
./gradlew getSandboxContext
# Output: launcher_tuning_attempt_1 (workspace: workspaces/launcher_tuning_attempt_1)
```

### Make Bounded Changes

Edit files normally, but changes are isolated:

```bash
# Edit a single file in the sandbox
# Example: workspaces/launcher_tuning_attempt_1/src/main/java/frc/robot/subsystems/Launcher.java
# Change PID constants: kP, kI, kD

# Verify what you changed
./gradlew sandboxDiff
# Output: Shows only your changes (compared to base)
```

### Build in Sandbox

```bash
# Build only this sandbox (doesn't affect main workspace)
./gradlew build --project-dir workspaces/launcher_tuning_attempt_1
```

### Run Simulation in Sandbox

```bash
# Simulate launcher with new PID constants
./gradlew simulateJava -PenableSimulation=true \
  --project-dir workspaces/launcher_tuning_attempt_1 \
  --args="--auto LaunchAuto --log sandbox_sim.wpilog"
```

### Analyze Results

```bash
# Analyze sandbox simulation log
./gradlew runMathAnalysis \
  --project-dir workspaces/launcher_tuning_attempt_1 \
  -Pargs="--log sandbox_sim.wpilog \
    --keys /RealOutputs/Launcher/RPM \
    --settling-time --threshold 50 \
    --json"
```

**Decision Gate:**
- ✅ Settling time < 0.5s? → Changes are good
- ❌ Settling time > 1.0s? → Tuning made it worse

## Sandbox Workflows

### Workflow 1: Iterative Tuning

```bash
# Iteration 1
./gradlew createSandbox -Psandbox="pid_tune_v1"
./gradlew switchSandbox -Psandbox="pid_tune_v1"
# Edit: Change kP from 0.1 to 0.15
./gradlew build
./gradlew simulateJava --args="--auto LaunchAuto --log sim.wpilog"
./gradlew runMathAnalysis -Pargs="--log sim.wpilog --keys /RealOutputs/Launcher/RPM --settling-time --json"
# Result: Settling time 0.8s (too slow)

# Iteration 2
./gradlew createSandbox -Psandbox="pid_tune_v2"
./gradlew switchSandbox -Psandbox="pid_tune_v2"
# Edit: Change kP from 0.1 to 0.20
./gradlew build
./gradlew simulateJava --args="--auto LaunchAuto --log sim.wpilog"
./gradlew runMathAnalysis -Pargs="--log sim.wpilog --keys /RealOutputs/Launcher/RPM --settling-time --json"
# Result: Settling time 0.35s ✅ (good!)

# Compare results
./gradlew sandboxCompare -Pcompare="pid_tune_v1,pid_tune_v2"
# Output: Shows which version is better

# Apply winning version to main workspace
./gradlew applySandbox -Psandbox="pid_tune_v2"
```

### Workflow 2: Feature Branch Testing

Test a complete new feature before merging:

```bash
# Create sandbox for new vision code
./gradlew createSandbox -Psandbox="vision_feature_test"
./gradlew switchSandbox -Psandbox="vision_feature_test"

# Make changes (add new vision class, update robot.java, etc.)
# ... edit multiple files ...

# Verify all changes are reasonable
./gradlew sandboxDiff --verbose
# Output: Shows all modified files and changes

# Run comprehensive tests
./gradlew test --project-dir workspaces/vision_feature_test
./gradlew build --project-dir workspaces/vision_feature_test
./gradlew simulateJava --project-dir workspaces/vision_feature_test \
  --args="--auto TargetAuto --headless --log vision_test.wpilog"

# Analyze results
./gradlew runGraphing -Pargs="--log workspaces/vision_feature_test/vision_test.wpilog \
  --keys /RealOutputs/Vision/ErrorX,/RealOutputs/Vision/ErrorY \
  --type overlay"

# If satisfied, apply
./gradlew applySandbox -Psandbox="vision_feature_test"
```

### Workflow 3: Parallel Experiments

Run multiple experiments simultaneously to compare approaches:

```bash
# Expert 1: Tests approach A
./gradlew createSandbox -Psandbox="shooter_approach_a"
# ... test approach A ...

# Expert 2: Tests approach B (in parallel)
./gradlew createSandbox -Psandbox="shooter_approach_b"
# ... test approach B ...

# Compare results
./gradlew sandboxCompare -Pcompare="shooter_approach_a,shooter_approach_b" \
  --metrics settling-time,overshoot,robustness
```

### Workflow 4: Regression Testing

Create a sandbox to verify nothing broke:

```bash
# After making unrelated changes, verify critical systems still work
./gradlew createSandbox -Psandbox="regression_check"
./gradlew switchSandbox -Psandbox="regression_check"

# Run comprehensive simulation suite
./gradlew simulateJava --args="--auto AllAutoRoutines --headless --log regression.wpilog"

# Run automated checks
./gradlew runMathAnalysis -Pargs="--log regression.wpilog \
  --keys /RealOutputs/Drive/Velocity,/RealOutputs/Launcher/RPM,/RealOutputs/Intake/Velocity \
  --settling-time \
  --threshold-check"

# If all checks pass, proceed; otherwise investigate
```

## Advanced Sandbox Operations

### Sandbox Comparison Matrix

```bash
# Compare multiple sandboxes across multiple metrics
./gradlew sandboxCompareMatrix \
  --sandboxes "launcher_v1,launcher_v2,launcher_v3" \
  --metrics "settling_time,overshoot,steady_state_error,responsiveness"
```

Output:
```
┌─────────────┬──────────────┬──────────┬─────────────┬─────────────┐
│ Sandbox     │ Settling(s)  │ Overshoot│ Error ±     │ Response    │
├─────────────┼──────────────┼──────────┼─────────────┼─────────────┤
│ launcher_v1 │ 0.85         │ 2.1%     │ ±15 RPM     │ Slow        │
│ launcher_v2 │ 0.42 ✓       │ 1.8%     │ ±8 RPM  ✓   │ Good ✓      │
│ launcher_v3 │ 0.38 ✓       │ 4.2%     │ ±12 RPM     │ Excellent ✓ │
└─────────────┴──────────────┴──────────┴─────────────┴─────────────┘
```

### Sandbox Merging

Combine validated changes from multiple sandboxes:

```bash
# Apply changes from v2 (launcher) AND v2 (intake) to main
./gradlew mergeSandboxes \
  --from "launcher_v2,intake_v2" \
  --to-main
```

### Sandbox Rollback

If an applied sandbox caused issues, rollback:

```bash
# Show last 5 applied sandboxes
./gradlew sandboxHistory --count 5

# Rollback to before launcher_v2 was applied
./gradlew rollbackSandbox -Psandbox="launcher_v2"
```

### Sandbox Cleanup

Remove old sandboxes to save space:

```bash
# List all sandboxes with their status
./gradlew listSandboxes

# Delete a specific sandbox (if not applied to main)
./gradlew deleteSandbox -Psandbox="launcher_tuning_attempt_1"

# Cleanup all applied sandboxes (keeps unapplied ones)
./gradlew cleanupSandboxes --keep-unapplied
```

## Sandbox Metadata

Each sandbox tracks:

```json
{
  "name": "launcher_tuning_v2",
  "created": "2026-05-07T14:23:00Z",
  "base_commit": "abc123def456",
  "description": "PID tuning for launcher; testing kP=0.20",
  "status": "testing",
  "changes": {
    "modified": ["src/main/java/frc/robot/subsystems/Launcher.java"],
    "added": [],
    "deleted": []
  },
  "sim_results": {
    "test": "LaunchAuto",
    "settling_time_ms": 350,
    "overshoot_percent": 1.8,
    "passed": true
  },
  "applied": false,
  "applied_at": null
}
```

## Safety Guardrails

Sandboxes enforce best practices:

### Bounded Change Detection

```bash
# Prevent accidentally editing too many files at once
./gradlew validateSandboxDiff --max-files 10
# Warning: Sandbox modifies 15 files (limit: 10)
# Use --force-large-change to override
```

### Automatic Backup Before Apply

```bash
# Before applying any sandbox, create a backup
./gradlew applySandbox -Psandbox="launcher_v2" --create-backup
# Backup created: backups/pre_launcher_v2_backup.zip
```

### Code Review Checklist

```bash
# Generate review checklist before applying
./gradlew sandboxReviewChecklist -Psandbox="launcher_v2"

Output:
  ☐ All tests pass
  ☐ Simulation validates
  ☐ No deprecated API usage
  ☐ No syntax errors
  ☐ Performance impact < 5%
  ☐ Code style consistent
```

## CLI Reference

### Sandbox Creation
- `--createSandbox -Psandbox="name"` : Create new sandbox
- `--from-commit <hash>` : Base sandbox on specific commit
- `--from-branch <name>` : Base sandbox on branch
- `--from-tag <tag>` : Base sandbox on release tag
- `--description <text>` : Document the experiment

### Sandbox Management
- `--switchSandbox -Psandbox="name"` : Switch to sandbox
- `--getSandboxContext` : Show current sandbox
- `--sandboxDiff` : Show changes in current sandbox
- `--sandboxDiff --verbose` : Show detailed diff

### Testing in Sandbox
- `--build` (with `--project-dir workspaces/...`) : Build sandbox
- `--test` (with `--project-dir workspaces/...`) : Run tests in sandbox
- `--simulateJava` (with `--project-dir workspaces/...`) : Simulate in sandbox

### Analysis & Comparison
- `--sandboxCompare -Pcompare="s1,s2"` : Compare two sandboxes
- `--sandboxCompareMatrix` : Compare multiple sandboxes
- `--sandboxHistory` : Show sandbox application history

### Apply & Cleanup
- `--applySandbox -Psandbox="name"` : Apply sandbox to main
- `--applySandbox --create-backup` : Create backup before applying
- `--rollbackSandbox -Psandbox="name"` : Undo sandbox application
- `--deleteSandbox -Psandbox="name"` : Delete sandbox
- `--cleanupSandboxes --keep-unapplied` : Clean old applied sandboxes

## Best Practices

1. **Use Descriptive Names**: `launcher_kp_0p2_attempt` > `test1`
2. **Document Changes**: Use `--description` to explain the experiment
3. **One Concept Per Sandbox**: Tune launcher OR intake, not both at once
4. **Always Compare**: Run the same test in original + sandbox
5. **Automate Decisions**: Use `--threshold` checks to decide automatically
6. **Create Backups**: Before applying to main, create backup via `--create-backup`
7. **Keep Sandbox History**: Don't delete sandboxes immediately; they're evidence

## Related Skills
- [Log Reading](log-reading.md) - Analyze sandbox test results
- [Math Analysis](math-analysis.md) - Numerical validation in sandboxes
- [Graphing](graphing.md) - Visualize sandbox simulation results
- [Evidence-Driven Loop](evidence-driven-loop.md) - Full workflow integration
- [Simulation Agent](simulation-agent.md) - Run tests in sandbox
