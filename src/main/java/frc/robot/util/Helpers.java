// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.util;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;

/** Add your docs here. */
public class Helpers {
    public static boolean isAllianceHubActive(){
        String gameData;
        Alliance alliance;
        gameData = DriverStation.getGameSpecificMessage();
        alliance = DriverStation.getAlliance().get();
        if(gameData.length() > 0){
            if(alliance == Alliance.Red){
                return gameData.charAt(0) == 'R';
            } else if (alliance == Alliance.Blue){
                return gameData.charAt(0) == 'B';
            }
        }
        return false;
    }
}
