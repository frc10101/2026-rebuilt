# Vision System Debugging Checklist

**Match Log**: `akit_26-04-29_23-18-10_milstein_p8.wpilog`  
**Issue**: Only 50% of vision pose observations being accepted  

---

## 🔍 Pre-Debugging Investigation

- [ ] Review the full analysis report: `VISION_ANALYSIS_REPORT.md`
- [ ] Understand current filtering thresholds in `Constants.java`
- [ ] Review rejection logic in `Vision.java` lines 173-193

---

## 📸 Camera Calibration Check

### Physical Verification
- [ ] Take photos of all 4 cameras on robot
- [ ] Measure physical offsets from robot center:
  - [ ] Camera 0 (Cherry): X=___, Y=___, Z=___ (meters)
  - [ ] Camera 1 (Orange): X=___, Y=___, Z=___ (meters)
  - [ ] Camera 2 (Grape): X=___, Y=___, Z=___ (meters)
  - [ ] Camera 3 (Strawberry): X=___, Y=___, Z=___ (meters)

- [ ] Measure camera angles (roll, pitch, yaw):
  - [ ] Camera 0: Roll=___, Pitch=___, Yaw=___ (degrees)
  - [ ] Camera 1: Roll=___, Pitch=___, Yaw=___ (degrees)
  - [ ] Camera 2: Roll=___, Pitch=___, Yaw=___ (degrees)
  - [ ] Camera 3: Roll=___, Pitch=___, Yaw=___ (degrees)

### Code Verification
- [ ] Open `src/main/java/frc/robot/Constants.java`
- [ ] Find `VisionConstants` section
- [ ] Check camera transforms match physical measurements:
  ```java
  public static final Transform3d cameraPose_robotToCamera0 = /* check this */
  public static final Transform3d cameraPose_robotToCamera1 = /* check this */
  public static final Transform3d cameraPose_robotToCamera2 = /* check this */
  public static final Transform3d cameraPose_robotToCamera3 = /* check this */
  ```

- [ ] If mismatch found:
  - [ ] Note the discrepancies
  - [ ] Update Constants with correct values
  - [ ] Rebuild and re-test

---

## 🏷️ April Tag Calibration Check

- [ ] Verify April Tag layout file is correct
  - Location: `path/to/AprilTagFieldLayout.json` (or hardcoded constants)
  - Check: Are tag IDs and poses correct?
  - Common issues:
    - [ ] Tag rotated 180° (Pose3d rotation wrong)
    - [ ] Tag ID mismatch (actual tag ≠ expected tag)
    - [ ] Duplicate tag numbers
    - [ ] Missing tags

- [ ] Compare against official WPI field layout
  - [ ] Download from: https://github.com/wpilibsuite/allwpilib/tree/main/apriltag/src/main/resources
  - [ ] Select correct year and competition

- [ ] If layout differs:
  - [ ] Update Constants or JSON file
  - [ ] Rebuild and re-test

---

## 🛠️ Filtering Threshold Debugging

### Step 1: Add Diagnostic Logging
Open `src/main/java/frc/robot/subsystems/vision/Vision.java` around line 170-190

Add this code before rejection check:
```java
Logger.recordOutput("Vision/RawObservation/Ambiguity", observation.ambiguity());
Logger.recordOutput("Vision/RawObservation/TagCount", observation.tagCount());
Logger.recordOutput("Vision/RawObservation/PoseX", observation.pose().getX());
Logger.recordOutput("Vision/RawObservation/PoseY", observation.pose().getY());
Logger.recordOutput("Vision/RawObservation/PoseZ", observation.pose().getZ());
```

Add this code after rejection check:
```java
if (rejectPose) {
    String reason = "UNKNOWN";
    if (observation.tagCount() == 0) reason = "NO_TAGS";
    else if (observation.tagCount() == 1 && observation.ambiguity() > maxAmbiguity) 
        reason = "HIGH_AMBIGUITY";
    else if (Math.abs(observation.pose().getZ()) > maxZError) 
        reason = "Z_ERROR";
    else if (observation.pose().getX() < 0.0 || 
             observation.pose().getX() > aprilTagLayout.getFieldLength())
        reason = "X_OUT_OF_BOUNDS";
    else if (observation.pose().getY() < 0.0 || 
             observation.pose().getY() > aprilTagLayout.getFieldWidth())
        reason = "Y_OUT_OF_BOUNDS";
    
    Logger.recordOutput("Vision/RejectionReason", reason);
} else {
    Logger.recordOutput("Vision/RejectionReason", "ACCEPTED");
}
```

- [ ] Rebuild code: `./gradlew build`
- [ ] Deploy to robot: `./gradlew deploy`
- [ ] Record a new match
- [ ] Download log and analyze which rejection reason is most common

### Step 2: Identify Problem Threshold
Based on logging results, identify which threshold is rejecting most poses:

- [ ] Rejection reason was: _____________
  
### Step 3: Test Threshold Adjustment
In `Constants.java`, try adjusting the problematic threshold:

**If rejections due to HIGH_AMBIGUITY:**
```java
// Current: maxAmbiguity = 0.3
// Try: maxAmbiguity = 0.5
```
- [ ] Update value
- [ ] Rebuild and test
- [ ] Note if acceptance rate improves

**If rejections due to Z_ERROR:**
```java
// Current: maxZError = 0.75
// Try: maxZError = 1.0
```
- [ ] Update value
- [ ] Rebuild and test
- [ ] Note if acceptance rate improves

**If rejections due to OUT_OF_BOUNDS:**
- [ ] This suggests severe pose estimation error
- [ ] Likely NOT threshold issue - probably calibration
- [ ] Go back to camera calibration section above

---

## 📊 Pose Quality Verification

### Compare with Odometry
- [ ] Use Dashboard to visualize both:
  - [ ] Vision estimated pose
  - [ ] Odometry estimated pose
  - [ ] Actual robot position

- [ ] Do accepted vision poses match actual position?
  - [ ] Yes → Rejection logic is working; maybe thresholds too strict
  - [ ] No → Vision measurements are fundamentally wrong; check calibration

### Analyze Accuracy
- [ ] Were rejected poses actually bad?
  - [ ] Run test with `maxAmbiguity = 1.0` (accept all ambiguous)
  - [ ] Log whether accepted vs rejected poses are accurate
  - [ ] If rejected poses are consistently bad → Thresholds are good
  - [ ] If rejected poses are actually good → Thresholds too strict

---

## ✅ Resolution Verification

After making changes, verify they fixed the problem:

- [ ] **Build successful**: `./gradlew build`
- [ ] **Deploy successful**: `./gradlew deploy`
- [ ] **Record new match log**
- [ ] **Run analysis**: `./gradlew runVisionAnalyzer -Pargs="path/to/new/log.wpilog"`
- [ ] **Acceptance rate improved**: ___% → ___% (target: >70%)
- [ ] **Pose estimates accurate**: Confirmed via Dashboard
- [ ] **No new issues appeared**: All cameras still active

---

## 🐛 If Problem Persists

If acceptance rate is still ~50% after all checks above:

1. [ ] **Review PhotonLib documentation**
   - Maybe pose estimation algorithm has known issues
   - Check if using latest PhotonLib version

2. [ ] **Test with different April Tag layouts**
   - Try official layout vs custom
   - Verify tag poses match reality

3. [ ] **Check network latency**
   - Is PhotonVision coprocessor slow?
   - Are images arriving on time?

4. [ ] **Consider hardware issue**
   - Are cameras actually good quality?
   - Try swapping cameras to test

5. [ ] **Ask mentors or team forum**
   - Share log file and configuration
   - Post to First Discord or Team 254 Discord

---

## 📋 Quick Reference

**Key Thresholds** (`Constants.java`):
```
maxAmbiguity = 0.3              ← Increase if HIGH_AMBIGUITY rejections
maxZError = 0.75 meters         ← Increase if Z_ERROR rejections  
linearStdDevBaseline = 0.02m    ← Affects covariance weighting
angularStdDevBaseline = 0.06rad ← Affects covariance weighting
```

**Key Code Locations**:
- Vision filtering: `src/main/java/frc/robot/subsystems/vision/Vision.java` (line 173)
- Camera config: `src/main/java/frc/robot/Constants.java` (VisionConstants)
- Data collection: `src/main/java/frc/robot/subsystems/vision/VisionIOPhotonVision.java`

**Useful Commands**:
```bash
# Compile
./gradlew compileJava

# Build fully
./gradlew build

# Deploy to robot
./gradlew deploy

# Run analysis on log
./gradlew runVisionAnalyzer -Pargs="/path/to/log.wpilog"

# List all keys in log
./gradlew runListKeys -Pargs="/path/to/log.wpilog" | grep -i vision
```

---

**Last Updated**: May 7, 2026  
**Analysis Tool**: VisionAnalyzer.java  
**Status**: Ready for debugging
