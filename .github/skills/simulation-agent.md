---
name: simulation-agent
description: >
  Run FRC robot simulations using simulateJavaAgent for automated autonomous (auto) and
  teleoperated (teleop) testing. Use this skill whenever the user wants to simulate robot
  behavior, run automated simulation tests, validate auto routines, test teleop sequences,
  spawn a simulation agent, or mentions simulateJavaAgent in any context. Also trigger when
  the user asks to "test without a robot", "run sim", "simulate auto", or "validate a path
  in simulation".
---

# Simulation Agent

Runs robot simulations through `simulateJavaAgent` for automated auto/teleop testing.

## When to use
- Validating autonomous routines before competition
- Testing teleop sequences without physical hardware
- Regression testing after code changes
- Verifying path following and motion profiles
## Prerequisites
- WPILib installed and on PATH
- `ROBOT_PROJECT` env var set to the robot project root, or passed explicitly
- Java 17+
## Workflow

### 1. Build the robot project
```bash
cd $ROBOT_PROJECT
./gradlew assemble
```

### 2. Launch simulateJavaAgent
```bash
./gradlew simulateJava \
  -PenableSimulation=true \
  -PagentMode=true \
  --args="--auto <AUTO_ROUTINE_NAME> --teleop <TELEOP_DURATION_SECONDS>"
```

**Common flags:**
| Flag | Description |
|---|---|
| `--auto <name>` | Named auto routine to run (matches `getAutoChooser()` key) |
| `--teleop <seconds>` | Teleop duration in seconds (default: 135) |
| `--alliance <red\|blue>` | Alliance color (default: blue) |
| `--headless` | Suppress GUI; useful for CI |
| `--log <path>` | Write a .wpilog to this path for later replay |

### 3. Interpret results
The agent exits with:
- `0` — simulation completed without faults
- `1` — robot code threw an uncaught exception
- `2` — watchdog timeout (loop overrun)
- `3` — assertion failure (if using SimAssert)
Stdout includes timestamped HAL state changes. Stderr captures Java exceptions.

### 4. Automated assertions (optional)
Use `SimAssert` from the `wpilibj-simulation` extras to assert robot state at time T:
```java
SimAssert.atTime(5.0).assertPose(new Pose2d(3.0, 2.0, Rotation2d.fromDegrees(90)), 0.1);
```

## CI integration
```yaml
# GitHub Actions example
- name: Run simulation tests
  run: ./gradlew simulateJava --args="--auto TestAuto --headless --log sim_output.wpilog"
- uses: actions/upload-artifact@v4
  with:
    name: sim-log
    path: sim_output.wpilog
```

## Troubleshooting
- **HAL initialization failed**: Make sure no other simulation or Driver Station is running.
- **ClassNotFoundException for agent**: Run `./gradlew assemble` before `simulateJava`.
- **Watchdog timeout in sim**: Increase `setExpiration()` or profile the loop with `Tracer`.
