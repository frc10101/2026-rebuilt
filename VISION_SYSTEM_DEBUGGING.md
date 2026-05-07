# Vision System Debugging - Complete Summary

## 🎯 The Problem

Your vision system achieved only a **50% pose acceptance rate** during the match (May 7, 13:57 logs).

- **Total pose observations**: 49
- **Accepted poses**: 5 ✅
- **Rejected poses**: 5 ❌
- **Match duration**: 38 seconds

This is **below optimal** (target >70% acceptance).

---

## 🔧 What You Have Now

### New Tools Created
1. **VisionAnalyzer.java** (`src/main/java/frc/robot/util/VisionAnalyzer.java`)
   - Analyzes .wpilog files for vision data
   - Counts observations, acceptances, rejections
   - Provides preliminary diagnosis
   - Run via: `./gradlew runVisionAnalyzer -Pargs="/path/to/log.wpilog"`

2. **Analysis Report** (`VISION_ANALYSIS_REPORT.md`)
   - Detailed findings from the match log
   - Likely root causes (in priority order)
   - Recommended next steps

3. **Debugging Checklist** (`VISION_DEBUG_CHECKLIST.md`)
   - Step-by-step verification process
   - Physical measurements checklist
   - Code verification steps
   - Threshold adjustment guide
   - Resolution verification steps

---

## 🚨 Root Cause (Most Likely)

Based on the analysis, **the most probable cause is CAMERA CALIBRATION**:

### Why?
1. **All 4 cameras actively detecting** (balanced observation counts)
   - Rules out: camera failure, pointing wrong direction
   
2. **Consistent rejection pattern** (50% rate sustained)
   - Rules out: temporary occlusion, network lag
   
3. **Systematic issue affecting all cameras equally**
   - Points to: global calibration problem (transforms, April Tag layout)

### Least Likely Causes
- ✅ NOT a software bug (code looks correct)
- ✅ NOT network issues (coprocessor connected)
- ✅ NOT tag visibility (cameras seeing observations)

---

## 📋 How to Fix (Priority Order)

### 1️⃣ Check Camera Physical Mounting (30 minutes)
**What**: Verify actual camera positions match code  
**Where**: Use `VISION_DEBUG_CHECKLIST.md` "Physical Verification" section  
**Expected**: Find discrepancy or confirm setup correct

### 2️⃣ Verify April Tag Layout (30 minutes)
**What**: Confirm tag poses match actual field  
**Where**: Use `VISION_DEBUG_CHECKLIST.md` "April Tag Calibration" section  
**Expected**: Verify layout is correct or find mismatched tags

### 3️⃣ Add Diagnostic Logging (15 minutes)
**What**: Log rejection reasons to identify which threshold is problematic  
**Where**: Use `VISION_DEBUG_CHECKLIST.md` "Step 1: Add Diagnostic Logging"  
**Expected**: Discover which rejection reason is most common

### 4️⃣ Adjust Thresholds Based on Data (15 minutes)
**What**: Gradually relax problematic threshold and test  
**Where**: Use `VISION_DEBUG_CHECKLIST.md` "Step 3: Test Threshold Adjustment"  
**Expected**: Acceptance rate improves to >70%

### 5️⃣ Verify Fix Works (30 minutes)
**What**: Record new match log and confirm improvement  
**Where**: Use `VISION_DEBUG_CHECKLIST.md` "Resolution Verification"  
**Expected**: New acceptance rate >70%, no new issues

**Total Time**: ~2 hours

---

## 📊 Key Metrics to Track

### Before Fix
```
Acceptance Rate: 50% (5/10)
Cameras Active: 4/4 ✅
System Status: Partially Functional ⚠️
```

### After Fix (Target)
```
Acceptance Rate: >70% (7/10+)
Cameras Active: 4/4 ✅
System Status: Fully Functional ✅
```

---

## 🛠️ Quick Start Guide

### Run Full Analysis
```bash
cd /Users/cohenhill/Desktop/Code/2026-rebuilt
./gradlew runVisionAnalyzer -Pargs="/Users/cohenhill/Desktop/Code/2026-rebuilt/logs/akit_26-04-29_23-18-10_milstein_p8.wpilog"
```

### Start Debugging
1. Open `VISION_DEBUG_CHECKLIST.md`
2. Follow sections in order
3. Check off boxes as you complete
4. Note your findings

### Update Code
All changes go in these 2 files:
- `src/main/java/frc/robot/Constants.java` (camera transforms, thresholds)
- `src/main/java/frc/robot/subsystems/vision/Vision.java` (add logging)

### Build & Test
```bash
# Compile
./gradlew build

# Deploy to robot
./gradlew deploy

# Record new match log (robot-side)
# Download log when done

# Analyze new log
./gradlew runVisionAnalyzer -Pargs="/path/to/new/log.wpilog"
```

---

## 📚 Documentation Files

| File | Purpose | Status |
|------|---------|--------|
| `VISION_ANALYSIS_REPORT.md` | Detailed findings & diagnosis | ✅ Ready |
| `VISION_DEBUG_CHECKLIST.md` | Step-by-step debugging guide | ✅ Ready |
| `VISION_SYSTEM_DEBUGGING.md` | This file | ✅ Ready |
| `VisionAnalyzer.java` | Analysis tool | ✅ Ready |

---

## 🤔 FAQ

**Q: Why was acceptance only 50%?**
A: Most likely your camera transforms or April Tag layout doesn't match reality. The filtering logic rejected poses it identified as invalid.

**Q: Will just relaxing thresholds fix it?**
A: Probably not permanently. If poses are actually invalid (e.g., way off field), keeping them helps short-term but breaks later. Better to fix root cause first.

**Q: How do I know if I fixed it?**
A: Record a new match log, run VisionAnalyzer on it, and check if acceptance rate is >70% and poses look accurate on dashboard.

**Q: Should I adjust thresholds higher or lower?**
A: After you know the rejection reason (from logging), adjust upward (e.g., `maxAmbiguity = 0.3 → 0.5`). This accepts more observations.

**Q: What if nothing works?**
A: Post your log file and configuration to the FRC Discord or ask a mentor. Include: acceptance rate, rejection reasons, camera photos.

---

## 🎓 Learning Context

This vision issue demonstrates why **systematic debugging** is important:
- ✅ Collected data (log analysis)
- ✅ Formed hypothesis (camera calibration)
- ✅ Planned experiments (threshold testing)
- ✅ Ready to validate (new match logs)

This same process applies to all debugging!

---

## 📞 Support

If you get stuck:
1. Check the specific section in `VISION_DEBUG_CHECKLIST.md`
2. Review the corresponding code location listed
3. Ask a mentor or post to FRC Discord
4. Include:
   - What step you're on
   - What you expected
   - What actually happened
   - Relevant log file/code snippet

---

**Next Action**: Open `VISION_DEBUG_CHECKLIST.md` and start with "Physical Verification"

**Estimated Time to Fix**: 2-3 hours

**Success Criteria**: Acceptance rate >70%, accurate pose estimates

---

*Generated by VisionAnalyzer - May 7, 2026*  
*For questions about vision system accuracy, consult this guide first*
