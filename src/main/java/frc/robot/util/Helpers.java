// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.util;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import java.util.Optional;

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

  private static boolean getIsAllianceShiftEven(double matchTime) {
    matchTime -= 30.0; // Take off EndGame
    if (matchTime > 50.0) {
      matchTime -=
          50.0; // Only checking if it's even, so only need to consider on each half of "the 100"
      // seconds
    }

    return matchTime <= 25.0; // is it the First shift on each half of "the 100"?
  }
}
