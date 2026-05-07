---
name: game-info
description: >
  Reference information for the 2026 FRC game "REBUILT" including field layout, zones, scoring
  objectives, game pieces, and rules relevant to robot strategy and programming. Trigger
  this skill whenever the user asks about the 2026 game, field zones, scoring, game
  pieces, auto vs teleop objectives, alliance wall positions, field coordinates, or
  anything related to "what does the robot need to do this year". Also use when
  programming autonomous routines and the user needs to know field dimensions or
  zone boundaries.
---

# Game Info — FRC 2026: REBUILT

## Field Overview

| Dimension | Value |
|---|---|
| Field length | 16.541 m (54 ft 3.25 in) |
| Field width | 8.020 m (26 ft 3.5 in) |
| Fuel diameter | 0.150 m (5.91 in) |
| Trench bar width | 0.075 m (2.95 in) |
| AprilTag width | 0.165 m (6.5 in) |
| Origin (WPILib) | Blue alliance left corner |

**Coordinate system**: WPILib field-relative coordinates. X = toward red alliance wall, Y = toward blue driver station left side.

---

## Alliance Stations

| Station | X (m) | Y (m) | Heading |
|---|---|---|---|
| Blue 1 | 0.0 | 7.35 | 0° |
| Blue 2 | 0.0 | 5.55 | 0° |
| Blue 3 | 0.0 | 3.69 | 0° |
| Red 1 | 16.54 | 1.20 | 180° |
| Red 2 | 16.54 | 3.00 | 180° |
| Red 3 | 16.54 | 4.80 | 180° |

---

## Game Pieces

- **Fuel** (Mechanism Build Elements) — Spherical game pieces used for scoring
  - Diameter: 5.91 in (15.0 cm)
  - Weight: 0.448-0.500 lb (~0.203-0.227 kg)
  - Material: High density foam ball
  - Supplier: AndyMark (part #am-5801)
  - **Control**: A ROBOT may CONTROL any number of FUEL after the start of the MATCH
  - **Damage tolerance**: FUEL that appears approximately like a FUEL is valid for scoring, even if damaged (small chunks are NOT considered FUEL)

### FUEL Staging for Each Match

**Total FUEL per match: 504** (may increase to 600 at District Championships and FIRST Championships)

**Staging Distribution:**
- **24 FUEL** in each DEPOT (2 depots = 48 FUEL total; non-uniform layout)
- **24 FUEL** in each OUTPOST CHUTE (2 chutes = 48 FUEL total)
- **0-48 FUEL** preloaded in ROBOTS (up to 8 per robot, fully supported by robot)
- **360-408 FUEL** in NEUTRAL ZONE (remaining after preloads)

**NEUTRAL ZONE Arrangement:**
- Bounding box: ~206.0 in wide (5.23 m) × 72.0 in deep (1.83 m)
- Divider in middle: 2.0 in (5.08 cm) wide (removed before match start)
- Expected distribution: Roughly equal split on both sides of CENTER LINE
- **NOTE**: Placement is NOT in a perfect grid; expect variances match-to-match
- **NOTE**: Field staff may have ±24 FUEL variance in NEUTRAL ZONE due to high element count

---

## Field Zones & Elements

### HUB (Scoring Structure)
| Component | Dimension |
|---|---|
| Width | 47.0 in (1.194 m) |
| Depth | 47.0 in (1.194 m) |
| Hexagonal opening size | 41.7 in (~1.06 m) |
| Opening height from carpet | 72.0 in (1.83 m) |
| Distance from ALLIANCE WALL | 158.6 in (4.03 m) |
| **Position between BUMPs** | Centered |

**HUB Features:**
- Hexagonal opening at top surface for FUEL delivery
- Series of 4 exits at base distributing FUEL into NEUTRAL ZONE
- Net structure in back prevents FUEL from prohibited areas
- FUEL exits randomly distribute game pieces

### BUMP (Drive-Over Structure)
| Component | Dimension |
|---|---|
| Width | 73.0 in (1.854 m) |
| Depth | 44.4 in (1.128 m) |
| Height | 6.513 in (16.54 cm) |
| Ramp angle | 15 degrees |
| Ramp thickness | 0.5 in (1.27 cm) |
| Material | ALLIANCE-colored Orange Peel textured HDPE |

**BUMP Features:**
- One ramp slopes down toward NEUTRAL ZONE
- One ramp slopes down toward ALLIANCE ZONE
- Located on both sides of HUB

### TRENCH (Drive-Under Structure)
| Component | Dimension |
|---|---|
| Width | 65.65 in (1.668 m) |
| Depth | 47.0 in (1.194 m) |
| Height | 40.25 in (1.023 m) |
| Opening width (underneath) | 50.34 in (1.279 m) |
| Opening height (underneath) | 22.25 in (56.52 cm) |

**TRENCH Features:**
- Located on both sides of field (guardrail to BUMP)
- Scoring table side: contains electronics to reach HUB
- Non-scoring table side: has pivot arm for ROBOT retrieval and field reset
- Pivot arm locked horizontal during MATCH

### TOWER (Climbing Structure)
| Component | Dimension |
|---|---|
| Width | 49.25 in (1.251 m) |
| Depth | 45.0 in (1.143 m) |
| Height | 78.25 in (1.988 m) |
| TOWER BASE width | 39.0 in (0.991 m) |
| TOWER BASE depth | 45.18 in (1.148 m) |
| TOWER BASE edge height | 0.2-0.3 in (0.5-0.8 cm) |

**UPRIGHTS:**
- Two uprights: 72.1 in (1.831 m) tall
- Uprights: 1.5 in (3.81 cm) thick, 3.5 in (8.89 cm) deep
- Distance between uprights: 32.25 in (81.92 cm)
- Material: Sheet metal box frames, powder-coated red or blue

**RUNGS (1-1/4" Sch 40 pipe):**
- Pipe OD: 1.66 in (4.216 cm)
- Each rung extends 5.875 in (14.92 cm) from outer face of upright on either side
- Rung spacing: 18.0 in (45.72 cm) center-to-center

**Rung Heights from Floor:**
- LOW RUNG center: 27.0 in (68.58 cm)
- MID RUNG center: 45.0 in (114.3 cm)
- HIGH RUNG center: 63.0 in (160 cm)

**Supporting Structures:**
- Additional supports extend from uprights to tower wall
- Support height range: 28.40 in (72.14 cm) to 43.38 in (1.102 m) off floor

### DEPOT (FUEL Storage)
| Component | Dimension |
|---|---|
| Width | 42.0 in (1.067 m) |
| Depth | 27.0 in (68.6 cm) |
| Height (with fastener) | ~1.125 in (2.86 cm) |
| Material | 3.0 in (7.62 cm) wide, 1.0 in (2.54 cm) tall steel barriers |
| Position | Along ALLIANCE WALL (1 per alliance) |

**DEPOT Features:**
- Secured to carpet using hook fastener
- Staging location for FUEL (24 FUEL per DEPOT)

### OUTPOST (Human Player Interface)
| Component | Dimension |
|---|---|
| Position | 1 on each end of field (guardrail to ALLIANCE WALL) |
| CHUTE opening width | 31.8 in (80.8 cm) |
| CHUTE opening height | 7.0 in (17.8 cm) |
| CHUTE opening height from floor | 28.1 in (71.4 cm) |
| CHUTE slope angle | 15.0 degrees |
| CHUTE capacity | ~25 FUEL at a time |
| CHUTE tape line distance | 12.9 in (32.8 cm) from field-facing wall |

**CORRAL (ROBOT delivery area):**
- Opening width: 32.0 in (81.3 cm)
- Opening height: 7.0 in (17.8 cm)
- Opening height from ground: 1.88 in (4.77 cm)
- Opening divided by center pipe (1-1/4" Sch 40)
- CORRAL interior: 35.8 in (90.8 cm) wide × 37.6 in (95.5 cm) deep
- CORRAL walls: 8.13 in (20.6 cm) tall polycarbonate panels
- CORRAL tape line distance: 12.7 in (32.3 cm) from field-facing wall

**OUTPOST Features:**
- CHUTE DOOR: HDPE arm on pivot (~90 degrees rotation) operated by HUMAN PLAYER
- Holds 24 FUEL for staging
- ROBOTS can push FUEL into CORRAL for HUMAN PLAYER retrieval

---

## Vertical Reference Lines (X-axis offsets)

| Line | X Position (m) | Description |
|---|---|---|
| Field center | 8.27 | Center of field |
| Starting line | ~1.105 | Alliance zone / hub near face |
| Hub center | ~1.652 | Center of hub |
| Neutral zone near | ~6.128 | 120 in from center toward alliance |
| Neutral zone far | ~10.414 | 120 in from center toward opponent |
| Opponent hub center | ~14.889 | Opponent hub center |
| Opponent alliance zone | ~15.436 | Opponent zone / hub near face |

---

## Horizontal Reference Lines (Y-axis offsets)

| Line | Y Position (m) | Description |
|---|---|---|
| Field center | 4.010 | Center of field |
| Right bump start | Hub right corner Y | Right bump edge |
| Right trench middle | Calculated | Right trench center |
| Left bump middle | Calculated | Left bump center |
| Left trench middle | Calculated | Left trench center |

---

## Auto Period

- Duration: **20 seconds** (0:20 – 0:00)
- Starting positions: 3 legal starting positions per alliance
- Preloaded pieces: 1 fuel piece per robot (on robot at match start)
- HUB Status: **Both HUBs ACTIVE**
- ROBOTS operate autonomously without DRIVE TEAM control
- Scoring: FUEL scored here counts, can retrieve additional FUEL, can climb TOWER
- Result: AUTO outcome determines HUB status for subsequent ALLIANCE SHIFTS

## Teleop Period

- Total Duration: **2 minutes 20 seconds** (2:20 – 0:00)
- DRIVERS remotely operate ROBOTS
- Game piece staging: Fuel Pool (center field)
- Continuous scoring: Robots can score fuel and climb TOWER

### TELEOP Segments:

| Segment | Duration | Timer | HUB Status | Notes |
|---|---|---|---|---|
| TRANSITION SHIFT | 10 sec | 2:20 – 2:10 | Both ACTIVE | Buffer period between AUTO and SHIFTS |
| SHIFT 1 | 25 sec | 2:10 – 1:45 | Alternates* | Based on AUTO winner |
| SHIFT 2 | 25 sec | 1:45 – 1:20 | Alternates* | Opposite of SHIFT 1 |
| SHIFT 3 | 25 sec | 1:20 – 0:55 | Alternates* | Same as SHIFT 1 |
| SHIFT 4 | 25 sec | 0:55 – 0:30 | Alternates* | Same as SHIFT 2 |
| END GAME | 30 sec | 0:30 – 0:00 | Both ACTIVE | Both HUBs return to active |

*HUB Status During ALLIANCE SHIFTS:
- **ALLIANCE that scored more FUEL in AUTO**: HUB inactive in SHIFT 1, active in SHIFT 2, inactive in SHIFT 3, active in SHIFT 4
- **ALLIANCE that scored less FUEL in AUTO**: HUB active in SHIFT 1, inactive in SHIFT 2, active in SHIFT 3, inactive in SHIFT 4
- **If tied**: FMS randomly selects which ALLIANCE's HUB status order applies

## Endgame

- Begins at 0:30 (30 seconds remaining)
- Duration: **30 seconds** (0:30 – 0:00)
- HUB Status: **Both HUBs return to ACTIVE**
- Climb objectives: Robots climb TOWER structure to reach different LEVELS
- LEVEL 1 (Low): 10 TELEOP pts (scored during TELEOP, not just ENDGAME)
- LEVEL 2 (Mid): 20 TELEOP pts
- LEVEL 3 (High): 30 TELEOP pts
- TRAVERSAL RP threshold: 50+ TOWER points in match

---

## Scoring Summary

| Action | Auto Points | Teleop Points | Notes |
|---|---|---|---|
| FUEL scored in an active HUB | 1 | 1 | Passes through top opening and sensor array |
| FUEL scored in an inactive HUB | — | — | No points awarded |
| TOWER - Each ROBOT at LEVEL 1 | 15 | 10 | Max 2 robots can score LEVEL 1 in AUTO |
| TOWER - Each ROBOT at LEVEL 2 | — | 20 | Not available in AUTO |
| TOWER - Each ROBOT at LEVEL 3 | — | 30 | Not available in AUTO |

**Scoring Timing Notes:**
- FUEL scored continues to be assessed for **3 seconds after the match timer displays 0:00** following AUTO
- FUEL scored continues to be assessed for **3 seconds after the match timer displays 0:00** following TELEOP
- AUTO TOWER points assessed after timer displays 0:00 following AUTO
- TELEOP TOWER points assessed 3 seconds after timer displays 0:00 following TELEOP (or when all ROBOTS come to rest, whichever is first)

---

## Ranking Points (RP)

| RP | Condition | Points |
|---|---|---|
| Win | Match win | 3 |
| Tie | Match tie | 1 |
| ENERGIZED RP | FUEL scored in active HUB ≥ threshold | 1 |
| SUPERCHARGED RP | FUEL scored in active HUB ≥ higher threshold | 1 |
| TRAVERSAL RP | TOWER points in match ≥ threshold | 1 |

### RP Thresholds by Event Type

| RP Type | Regional/District | District Championships | FIRST Championship |
|---|---|---|---|
| ENERGIZED RP | 100 FUEL | 240 FUEL | 360 FUEL |
| SUPERCHARGED RP | 360 FUEL | 360 FUEL | 500 FUEL |
| TRAVERSAL RP | 50 points | 50 points | 50 points |

---

## Robot Constraints Relevant to Programming

### Starting Configuration (R104)
- **Max ROBOT PERIMETER**: 110.0 in (2.794 m) — measured with string wrapped taut around outermost parts (excluding BUMPERS)
- **Max height**: 30.0 in (76.2 cm)
- **ROBOT PERIMETER**: Fixed, non-articulated structural elements (excludes BUMPERS)
  - Minor protrusions ≤0.25 in (0.64 cm) excluded: bolt heads, fastener ends, weld beads, rivets

### Horizontal Extension Limits (R105, R106)
- **Max extension beyond ROBOT PERIMETER**: 12.0 in (30.48 cm)
- **Extension constraint**: May extend in **only ONE direction** (one side) at a time
- **Height limit**: Total height must not exceed 30.0 in (76.2 cm)
- **Extension floor interaction** (R108): ROBOT extensions may NOT interact with carpet, BUMPS, or TOWER BASE such that BUMPERS lift out of BUMPER ZONE

### Weight Limit (R103)
- **Max weight**: 115.0 lb (52.16 kg)
- **Exclusions from weight** (not counted):
  - ROBOT BUMPERS
  - ROBOT battery and Anderson cable quick connect/disconnect pair
  - AprilTags for localization

### Overhang Rule (R102)
- In STARTING CONFIGURATION, no part of ROBOT shall extend outside vertical projection of ROBOT PERIMETER
- **Exception**: BUMPERS and minor protrusions (bolt heads, fastener ends, rivets, cable ties, etc.)
- **Test**: If each side pushed against vertical wall (BUMPERS removed), only ROBOT PERIMETER should contact wall

### Interchangeable Mechanisms (I103)
- If ROBOT uses swappable mechanisms, teams must demonstrate compliance with size/weight rules in ALL configurations

**Key Programming Implications:**
- Autonomous must account for 12 in max extension
- Software constraints may be needed to limit extension in specific directions
- Vision tracking must account for variable ROBOT footprint during extension
- Path planning should consider 110 in max PERIMETER + 12 in extension = max 122 in effective width

---

## PathPlanner / Choreo Named Waypoints

Add your team's named positions here for reuse across auto routines:

```java
// Hub-related positions (from FieldConstants)
public static final Pose2d HUB_TOP_CENTER = new Pose2d(1.652, 4.010, Rotation2d.fromDegrees(0));
public static final Pose2d HUB_NEAR_LEFT = new Pose2d(1.105, 4.635, Rotation2d.fromDegrees(0));
public static final Pose2d HUB_NEAR_RIGHT = new Pose2d(1.105, 3.385, Rotation2d.fromDegrees(0));
public static final Pose2d HUB_FAR_LEFT = new Pose2d(1.975, 4.635, Rotation2d.fromDegrees(180));
public static final Pose2d HUB_FAR_RIGHT = new Pose2d(1.975, 3.385, Rotation2d.fromDegrees(180));

// Tower-related positions
public static final Pose2d TOWER_CENTER = new Pose2d(1.105, 4.010, Rotation2d.fromDegrees(0));

// Fuel Pool positions (center field)
public static final Pose2d FUEL_POOL_CENTER = new Pose2d(8.27, 4.010, Rotation2d.fromDegrees(0));
public static final Pose2d FUEL_POOL_LEFT = new Pose2d(8.27, 6.320, Rotation2d.fromDegrees(0));
public static final Pose2d FUEL_POOL_RIGHT = new Pose2d(8.27, 1.700, Rotation2d.fromDegrees(0));

// Trench positions
public static final Pose2d LEFT_TRENCH_CENTER = new Pose2d(1.652, 6.565, Rotation2d.fromDegrees(90));
public static final Pose2d RIGHT_TRENCH_CENTER = new Pose2d(1.652, 1.455, Rotation2d.fromDegrees(270));

// Alliance station / staging
public static final Pose2d BLUE_STARTING_1 = new Pose2d(0.5, 7.35, Rotation2d.fromDegrees(0));
public static final Pose2d BLUE_STARTING_2 = new Pose2d(0.5, 5.55, Rotation2d.fromDegrees(0));
public static final Pose2d BLUE_STARTING_3 = new Pose2d(0.5, 3.69, Rotation2d.fromDegrees(0));
```

---

## AprilTag Information

**Overview:**
- **Total tags**: 32 unique markers on field (IDs 1-32)
- **Tag family**: 36h11
- **Marker size**: 8.125 in (20.64 cm) square
- **Mount panel**: 10.5 in (26.67 cm) square polycarbonate panel
- **Repair**: Damaged tags repaired with gaffers tape during competition
- **Locations**: HUB, TOWER WALL, OUTPOST, and TRENCHES

### HUB AprilTags
| IDs | Count | Location | Height | Arrangement |
|---|---|---|---|---|
| 2, 3, 4, 5, 8, 9, 10, 11, 18, 19, 20, 21, 24, 25, 26, 27 | 16 tags | All four faces of HUB (4 faces × 4 tags per face) | 44.25 in (1.124 m) off floor | 2 tags per face: 1 centered, 1 horizontally offset |

**HUB Face Layout:**
- Each HUB face has 4 AprilTags with centers at 44.25 in height
- One tag centered on face, one tag horizontally offset
- Optimal for vision targeting from multiple approach angles

### TOWER AprilTags
| IDs | Count | Location | Height | Arrangement |
|---|---|---|---|---|
| 15, 16, 31, 32 | 4 tags | TOWER WALLs (2 tags per tower, 2 towers) | 21.75 in (55.25 cm) off floor | 2 tags per tower: 1 centered, 1 horizontally offset |

**TOWER Layout:**
- Red TOWER: Tags 31, 32
- Blue TOWER: Tags 15, 16
- Each tower has centered tag and horizontally offset tag
- Positioned for vision-based climbing assistance

### OUTPOST AprilTags
| IDs | Count | Location | Height | Arrangement |
|---|---|---|---|---|
| 13, 14, 29, 30 | 4 tags | OUTPOSTs (2 tags per outpost, 2 outposts) | 21.75 in (55.25 cm) off floor | 2 tags per outpost: 1 centered, 1 horizontally offset |

**OUTPOST Layout:**
- Red OUTPOST: Tags 29, 30
- Blue OUTPOST: Tags 13, 14
- Centered tag aligns with CHUTE and CORRAL openings
- One tag horizontally offset
- Aids HUMAN PLAYER and ROBOT positioning

### TRENCH AprilTags
| IDs | Count | Location | Height | Arrangement |
|---|---|---|---|---|
| 1, 6, 7, 12, 17, 22, 23, 28 | 8 tags | TRENCH mounting brackets (2 tags per trench, 4 trenches) | 35 in (88.9 cm) off floor | 2 tags per trench: 1 facing ALLIANCE ZONE, 1 facing NEUTRAL ZONE |

**TRENCH Layout:**
- Mounted on top surface of horizontal arm
- Approximately centered on opening under TRENCH arm
- One tag faces ALLIANCE ZONE, one faces NEUTRAL ZONE
- Provides localization crossing between zones

### AprilTag ID Reference Table

| ID | Location | Alliance | Notes |
|---|---|---|---|
| 1 | TRENCH | — | Neutral zone trench |
| 2 | HUB | Blue | Face tag |
| 3 | HUB | Blue | Face tag |
| 4 | HUB | Red | Face tag |
| 5 | HUB | Red | Face tag |
| 6 | TRENCH | — | Alliance zone trench |
| 7 | TRENCH | — | Neutral zone trench |
| 8 | HUB | Blue | Face tag |
| 9 | HUB | Blue | Face tag |
| 10 | HUB | Red | Face tag |
| 11 | HUB | Red | Face tag |
| 12 | TRENCH | — | Alliance zone trench |
| 13 | OUTPOST | Blue | Chute/Corral centered |
| 14 | OUTPOST | Blue | Horizontally offset |
| 15 | TOWER | Blue | Centered |
| 16 | TOWER | Blue | Horizontally offset |
| 17 | TRENCH | — | Neutral zone trench |
| 18 | HUB | Blue | Face tag |
| 19 | HUB | Blue | Face tag |
| 20 | HUB | Red | Face tag |
| 21 | HUB | Red | Face tag |
| 22 | TRENCH | — | Alliance zone trench |
| 23 | TRENCH | — | Neutral zone trench |
| 24 | HUB | Blue | Face tag |
| 25 | HUB | Blue | Face tag |
| 26 | HUB | Red | Face tag |
| 27 | HUB | Red | Face tag |
| 28 | TRENCH | — | Alliance zone trench |
| 29 | OUTPOST | Red | Chute/Corral centered |
| 30 | OUTPOST | Red | Horizontally offset |
| 31 | TOWER | Red | Centered |
| 32 | TOWER | Red | Horizontally offset |

**Vision Strategy Notes:**
- HUB tags at 44.25 in: Optimal for scoring approach shots
- TOWER tags at 21.75 in: Lower mount for climbing visual feedback
- OUTPOST tags at 21.75 in: Assist human player coordination
- TRENCH tags at 35 in: Mid-height for zone crossing localization
- Multiple tags per structure enable multi-angle vision targeting

---

## Resources

- [FRC Game Manual 2026](https://www.firstinspires.org/resource-library/frc/competition-manual-qa-system)
- [WPILib Field Coordinate System](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/geometry/coordinate-systems.html)
- [PathPlanner Field Images](https://github.com/mjansen4857/pathplanner)
- [FRC Field CAD / drawings](https://www.firstinspires.org/robotics/frc/playing-field)
- **Team FieldConstants class**: Reference `org.littletonrobotics.frc2026.FieldConstants` for programmatic access to all field element positions
