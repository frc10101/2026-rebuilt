// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.util;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import frc.robot.Constants;
import java.util.Optional;
import org.littletonrobotics.junction.Logger;

/** Add your docs here. */
public class Helpers {
  public static boolean isAllianceHubActive() {
    final double matchTime =
        DriverStation.getMatchTime(); // Time remaing in either an Auto period, or a Tele-Op period

    if (matchTime == 0.0) { // If match time is 0, then there is no way your hub can be scored in
      return false;
    }
    if (matchTime <= 30.0 || matchTime > 130) { // If in Endgame, Auto, or transversal period
      return true;
    }

    final String gameData =
        DriverStation.getGameSpecificMessage(); // Either an "R" or "B", based on who won Auto
    if (gameData == null || gameData.isEmpty()) { // Check for valid data
      return false;
    }

    final Optional<Alliance> myAlliance = DriverStation.getAlliance(); // Our alliance
    if (myAlliance.isEmpty()) return false; // Do it be empty?

    boolean isAllianceShiftEven = getIsAllianceShiftEven(matchTime); // Grab the alliance shift
    final Alliance gameDataAlliance =
        gameData.equals("R") ? Alliance.Red : Alliance.Blue; // Is the game data R?

    return !(isAllianceShiftEven
        ^ (myAlliance.get()
            == gameDataAlliance)); // If the shift is even and you won Auto or it's odd and you lost
    // Auto
  }

  /**
   * Returns the number of seconds until the next alliance shift during teleop.
   *
   * <p>Alliance shifts occur every 25 seconds during teleop, ending at the start of endgame. If the
   * match time is outside of the shift window (auto or endgame), this returns 0.
   */
  public static double getTimeToNextAllianceShift() {
    return getTimeToNextAllianceShift(DriverStation.getMatchTime());
  }

  /**
   * Returns the number of seconds until the next alliance shift during teleop.
   *
   * @param matchTime time remaining in the current period, in seconds
   * @return seconds until the next shift, or 0 if outside the shift window
   */
  public static double getTimeToNextAllianceShift(double matchTime) {
    if (matchTime <= 30.0 || matchTime > 130.0) {
      return 0.0;
    }

    double shiftClock = matchTime - 30.0;
    if (shiftClock > 50.0) {
      shiftClock -= 50.0;
    }

    if (shiftClock > 25.0) {
      return shiftClock - 25.0;
    }

    return shiftClock;
  }

  public static String getShift() {
    return getShift(DriverStation.getMatchTime());
  }

  /**
   * Returns the current shift label based on match time.
   *
   * <p>Auto is "A" (first 20 seconds), transition shift is "TS" (next 10 seconds), shifts 1-4 are
   * "1"-"4" (25 seconds each), and endgame is "EG" (last 30 seconds).
   *
   * @param matchTime time remaining in the current period, in seconds
   * @return shift label for the current time window
   */
  public static String getShift(double matchTime) {
    if (matchTime <= 0.0) {
      return "EG";
    }
    if (matchTime <= 30.0) {
      return "EG";
    }
    if (matchTime > 130.0) {
      return "A";
    }
    if (matchTime >= 120.0) {
      return "TS";
    }

    double shiftClock = matchTime - 30.0;
    if (shiftClock > 75.0) {
      return "1";
    }
    if (shiftClock > 50.0) {
      return "2";
    }
    if (shiftClock > 25.0) {
      return "3";
    }
    return "4";
  }

  /**
   * Returns true when the given pose is in the rebuilt alliance zone rectangle.
   *
   * <p>Blue zone bounds: (0, 0) to (4.6, 8.0) meters. Red side mirrors across field length.
   */
  public static boolean isPoseInAllianceZone(Pose2d pose) {
    if (pose == null) {
      return false;
    }

    double fieldLength = Constants.VisionConstants.aprilTagLayout.getFieldLength();
    boolean isRed = DriverStation.getAlliance().orElse(Alliance.Blue) == Alliance.Red;
    Logger.recordOutput("isRed", isRed);
    double minY = 0.0;
    double maxY = 8.0;
    double minXBlue = 0.0;
    double maxXBlue = 4.6;

    double minX = isRed ? fieldLength - maxXBlue : minXBlue;
    double maxX = isRed ? fieldLength - minXBlue : maxXBlue;

    Logger.recordOutput("minX", minX);
    Logger.recordOutput("maxX", maxX);
    Logger.recordOutput("minY", minY);
    Logger.recordOutput("maxY", maxY);
    boolean isInZone =
        pose.getX() >= minX && pose.getX() <= maxX && pose.getY() >= minY && pose.getY() <= maxY;
    Logger.recordOutput("isAutoRev", isInZone);
    return isInZone;
  }

  private static boolean getIsAllianceShiftEven(double matchTime) {
    matchTime -= 30.0; // Take off EndGame
    if (matchTime > 50.0) {
      matchTime -=
          50.0; // Only checking if it's even, so only need to consider on each half of "the 100"
      // seconds
    }

    return matchTime <= 25.0; // is it the First shift on each half of "the 100"?
  }
  /**
   * @param pose
   * @return
   */
  public static double getYCoordinate(Pose2d pose) {
    double ans = pose.getTranslation().getY();
    Logger.recordOutput("Y Coordinate", ans);
    return ans;
  }
}
