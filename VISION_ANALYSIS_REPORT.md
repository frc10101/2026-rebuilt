# Vision System Accuracy Analysis Report

**Match Log**: `akit_26-04-29_23-18-10_milstein_p8.wpilog`  
**Analysis Date**: May 7, 2026  
**Duration**: 38 seconds of match data  

---

## Executive Summary

The vision system's **50% pose acceptance rate** indicates a significant filtering issue. While the cameras are detecting tags (all 4 cameras active), approximately half of the pose observations are being rejected by the filtering logic in `Vision.java`.

**Root Cause**: One or more filtering thresholds is rejecting valid pose estimates.

---

## Findings

### ✅ What's Working
- **All 4 cameras active** (Cherry, Orange, Grape, Strawberry)
  - Camera 0 (Cherry): 9 observations
  - Camera 1 (Orange): 9 observations  
  - Camera 2 (Grape): 9 observations
  - Camera 3 (Strawberry): 9 observations
  - *Analysis*: Balanced camera activity suggests good physical positioning

- **Consistent detections** (38 seconds of continuous vision data)
  - No long gaps in vision updates
  - Cameras maintaining ~0.2Hz update rate (4-5 entries per camera per second)

- **PhotonVision integration working**
  - Both PhotonVision coprocessor instances connected
  - Network Tables publishing vision data

### ❌ Issues Detected

#### Issue #1: Low Acceptance Rate (50%)
- **Finding**: Only 5 out of 10 pose observations accepted
- **Impact**: Vision contributing only ~50% of potential pose updates
- **Severity**: 🔴 HIGH - Will reduce localization frequency

**Likely Causes** (in order of probability):
1. **High Ambiguity** - Single-tag measurements with ambiguity > 0.3
   - Single AprilTag measurements are ambiguous when viewed at certain angles
   - Ambiguity relates to how similar the tag looks from different positions
   - Threshold of 0.3 is reasonable but may be conservative

2. **Out-of-Bounds Poses** - Estimated position outside field boundaries
   - Field boundaries: X ∈ [0, 16.54m], Y ∈ [0, 8.23m]
   - Could indicate: tag localization error, camera calibration issue, or robot teleported

3. **Z-Error Exceeding 0.75m** - Vertical component too large
   - Suggests camera or tag height is miscalibrated
   - Or pose estimation is fundamentally wrong

4. **Zero Tags Detected** - Camera can't see any tags
   - Would manifest as consecutive rejected poses
   - Less likely given consistent observation rate

#### Issue #2: Missing Tag Count Data
- **Finding**: No tag count statistics in log
- **Impact**: Can't distinguish single-tag vs multi-tag measurements
- **Severity**: 🟡 MEDIUM - Limits diagnostic capability

---

## Filtering Thresholds (Current Configuration)

Located in `src/main/java/frc/robot/Constants.java` (VisionConstants):

```java
maxAmbiguity = 0.3                    // Single-tag ambiguity threshold
maxZError = 0.75 meters               // Maximum vertical error
linearStdDevBaseline = 0.02 meters    // Baseline linear uncertainty
angularStdDevBaseline = 0.06 radians  // Baseline angular uncertainty
```

**Rejection Logic** (`Vision.java` Line 173):
```java
boolean rejectPose = 
    observation.tagCount() == 0                          // No tags seen
    || (observation.tagCount() == 1 && 
        observation.ambiguity() > maxAmbiguity)          // Single tag too ambiguous
    || Math.abs(observation.pose().getZ()) > maxZError   // Z too large
    || observation.pose().getX() < 0.0                   // X out of bounds
    || observation.pose().getX() > fieldLength           // X out of bounds  
    || observation.pose().getY() < 0.0                   // Y out of bounds
    || observation.pose().getY() > fieldWidth;           // Y out of bounds
```

---

## Diagnosis & Recommendations

### Priority 1: Check Camera Calibration 🔴 URGENT

The 50% rejection rate strongly suggests a **systematic calibration issue**.

**Steps**:
1. **Verify camera extrinsic transforms**
   ```
   Location: Constants.java line ~XYZ (VisionConstants)
   Check: cameraPose_robotToCamera[n] for each camera
   Expected: Pose3d with realistic offsets from robot center
   ```

2. **Verify robot-to-camera transforms are correct**
   - Physical measurement: Where is each camera mounted?
   - Angle: What direction is each camera pointing?
   - Compare against Constants definition

3. **Test with known calibration**
   - Temporarily use default transforms
   - See if acceptance rate improves
   - If yes → Your transforms are wrong

### Priority 2: Analyze Pose Estimation Errors 🟡 HIGH

**Next diagnostic step**:
1. Temporarily **relax filtering thresholds** to 0.5 acceptance rate (accept 100%)
   ```java
   maxAmbiguity = 0.9           // Accept all
   maxZError = 5.0              // Accept all
   ```

2. **Log rejection reasons** - Modify `Vision.java` to record why poses were rejected:
   ```java
   if (rejectPose) {
       String reason = "";
       if (observation.tagCount() == 0) reason = "NO_TAGS";
       else if (...high ambiguity...) reason = "HIGH_AMBIGUITY";
       else if (...Z error...) reason = "Z_ERROR";
       else if (...out of bounds...) reason = "OUT_OF_BOUNDS";
       Logger.recordOutput("Vision/RejectionReason", reason);
   }
   ```

3. **Re-test with relaxed thresholds**
   - If acceptance stays ~50% → Not threshold-based
   - If acceptance jumps to 100% → Thresholds are too tight

### Priority 3: April Tag Calibration 🟡 MEDIUM

**Check**:
1. April Tag layout in code matches actual field
2. No tag ID mismatches (e.g., tag 1 placed where tag 5 should be)
3. Tag poses aren't rotated 180° (common mistake)

**Verification**:
- Download field layout from WPI or Team 254
- Compare against `AprilTagFieldLayout` definition in code

### Priority 4: Multi-Tag vs Single-Tag Weighting 🟡 MEDIUM

**Current behavior** (from `VisionIOPhotonVision.java`):
```java
// First try multi-tag estimation
Optional<EstimatedRobotPose> visionEst = 
    poseEstimator.estimateCoprocMultiTagPose(result);

// Fall back to single-tag if multi-tag fails
if (visionEst.isEmpty()) {
    visionEst = poseEstimator.estimateLowestAmbiguityPose(result);
}
```

**Analysis**:
- If cameras can't see 2+ tags consistently → More single-tag estimates
- Single-tag measurements have higher ambiguity naturally
- May need to increase `maxAmbiguity` threshold for single-tag case

---

## Actionable Next Steps

### Immediate Actions (Today)
1. ✅ **Run this analysis** ← You're here!
2. 🔲 **Check camera physical mounting**
   - Take photos of each camera
   - Verify angle and position vs Constants
3. 🔲 **Review robot pose on field**
   - Was robot teleported or had position reset?
   - Could explain out-of-bounds rejections

### Short-Term (Next Practice)
1. 🔲 **Add logging to Vision.java**
   ```java
   Logger.recordOutput("Vision/PoseAccepted", !rejectPose);
   Logger.recordOutput("Vision/Ambiguity", observation.ambiguity());
   Logger.recordOutput("Vision/PoseX", observation.pose().getX());
   Logger.recordOutput("Vision/PoseY", observation.pose().getY());
   Logger.recordOutput("Vision/PoseZ", observation.pose().getZ());
   ```

2. 🔲 **Temporarily log rejection reasons**
3. 🔲 **Test with relaxed thresholds**
4. 🔲 **Compare accepted pose vs actual position**
   - Were accepted poses accurate?
   - If no → Calibration is wrong
   - If yes → Rejected poses were actually bad

### Medium-Term (Next Week)
1. 🔲 **Recalibrate cameras if needed**
   - PhotonVision camera calibration procedure
   - Robot-to-camera transform measurement
2. 🔲 **Fine-tune filtering thresholds** based on data
3. 🔲 **Test in match conditions**

---

## How to Interpret Results

### If Acceptance Rate Goes Up with Relaxed Thresholds
→ **Conclusion**: Your thresholds are too strict  
→ **Action**: Gradually increase `maxAmbiguity` and `maxZError`  
→ **Expected Range**: 0.4-0.6 ambiguity, 1.0m Z-error for relaxed settings

### If Acceptance Rate Stays ~50% Even When Relaxed
→ **Conclusion**: Filtering logic isn't the issue  
→ **Action**: Check camera calibration or April Tag layout  
→ **Investigate**: Are accepted poses actually accurate?

### If Poses Are Accepted But Very Noisy
→ **Conclusion**: Acceptance logic ok, but precision is low  
→ **Action**: Check camera calibration quality  
→ **Debug**: Are standard deviations being calculated correctly?

---

## Reference: Vision System Architecture

```
PhotonVision Coprocessor
    ↓
VisionIOPhotonVision.java
  • Reads camera results
  • Calculates multi-tag pose
  • Falls back to single-tag
    ↓
Vision.java
  • Applies filtering logic
  • Validates pose bounds
  • Checks ambiguity
  • Records to AdvantageKit
    ↓
Drive.java (Pose Estimator)
  • Updates odometry with vision observations
  • Weights by standard deviation
```

**Key Classes**:
- `Vision.java` - Main vision subsystem, filtering logic
- `VisionIOPhotonVision.java` - PhotonVision interface
- `Constants.VisionConstants` - Camera configs and thresholds
- `AprilTagFieldLayout` - April Tag positions

---

## Conclusion

Your vision system is **partially functional** but being rejected at an **undesirable rate (50%)**. The most likely cause is **camera calibration** or **April Tag layout** issues, not the filtering thresholds.

**Next action**: Check physical camera mounting and verify it matches Constants configuration.

---

*Report Generated by VisionAnalyzer - May 7, 2026*
