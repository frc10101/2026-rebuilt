# Vision System Integration Guide

## Quick Summary

The Vision system is **95% complete** but **not connected** to the Drive subsystem. Here's what needs to be done:

---

## 1. RobotContainer - Add Vision Integration

### Current Code (RobotContainer.java)
```java
public class RobotContainer {
    private final Drive drive;
    
    public RobotContainer() {
        // ...drive initialization...
    }
}
```

### Required Changes

**Add imports:**
```java
import frc.robot.subsystems.vision.Vision;
import frc.robot.subsystems.vision.VisionIO;
import frc.robot.subsystems.vision.VisionIOPhotonVision;
```

**Add Vision field:**
```java
public class RobotContainer {
    private final Drive drive;
    private final Vision vision;  // ADD THIS
    
    public RobotContainer() {
        // ... existing drive code ...
        
        // Add vision based on mode
        switch (Constants.currentMode) {
            case REAL:
                vision = new Vision(
                    drive::addVisionObservation,  // Pass observation consumer to drive
                    VisionIOPhotonVision.createAllCameras()
                );
                break;
                
            case SIM:
                vision = new Vision(
                    drive::addVisionObservation,
                    VisionIOPhotonVision.createAllCameras()  // Sim cameras registered elsewhere
                );
                break;
                
            default:
                vision = null;
        }
    }
    
    // Add getter if needed
    public Vision getVision() {
        return vision;
    }
}
```

---

## 2. Robot.java - Add Simulation Registration

### Current Code (Robot.java)
```java
@Override
public void simulationInit() {
    // Currently empty or minimal
}
```

### Required Changes

**Add import:**
```java
import frc.robot.subsystems.vision.VisionIOPhotonVisionSim;
```

**Add simulation setup:**
```java
@Override
public void simulationInit() {
    // Register vision cameras for simulation
    VisionIOPhotonVisionSim.registerAllSimCameras(
        () -> robotContainer.getDrive().getPose()
    );
}
```

---

## 3. Verify Drive Has Pose Estimator Integration

### Check Drive.java
Ensure the Drive class has a method like:
```java
public void addVisionObservation(
    Pose2d visionRobotPose,
    double timestampSeconds,
    Matrix<N3, N1> visionMeasurementStdDevs) {
    poseEstimator.addVisionMeasurement(
        visionRobotPose,
        timestampSeconds,
        visionMeasurementStdDevs);
}
```

If this method doesn't exist, it needs to be added.

---

## 4. Testing Steps

### Real Robot
1. Deploy code to robot
2. Open PhotonVision dashboard
3. Verify all 4 cameras appear:
   - Cherry (0)
   - Orange (1)
   - Grape (2)
   - Strawberry (3)
4. Enable AprilTag tracking
5. Check AdvantageScope for Vision/Camera* inputs
6. Position robot near tags and verify pose updates

### Simulation
1. Run robot in simulator mode
2. Verify PhotonVision simulation window shows 4 cameras
3. Check AdvantageScope shows Vision/Camera* data
4. Monitor drive pose estimates (should include vision)

---

## 5. Troubleshooting

### Vision cameras don't appear
- Check PhotonVision coprocessor IP/connection
- Verify camera names match configuration
- Restart PhotonVision dashboard

### No pose observations
- Verify AprilTag detection is enabled
- Check tag IDs are in 2026 field layout
- Increase lighting on field

### Drive pose not improving
- Check vision observations are being logged
- Verify `addVisionObservation()` is being called
- Check standard deviations aren't too high

---

## 6. Files Needing Modifications

| File | Changes |
|------|---------|
| `RobotContainer.java` | Add Vision field and initialization |
| `Robot.java` | Add `simulationInit()` vision setup |
| `Drive.java` (if needed) | Verify `addVisionObservation()` method exists |

---

## Status After Integration

Once these changes are made, the vision system will be:
- ✅ Collecting observations from 4 cameras
- ✅ Filtering observations by ambiguity/distance/field bounds
- ✅ Calculating proper standard deviations
- ✅ Sending observations to drive pose estimator
- ✅ Improving drive localization with vision
- ✅ Logging all data for analysis in AdvantageScope
- ✅ Ready for competition

