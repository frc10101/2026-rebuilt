package frc.robot.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;

public class HelpersTests {
  @DisplayName("Active Hub Test")
  @ParameterizedTest(name = "On {1} Alliance with Game Data {0}")
  @CsvSource({
    // Auto
    ",Red,20.0,true",
    ",Blue,20.0,true",
    ",Red,14.0,true",
    ",Blue,14.0,true",
    ",Red,0.0,false",
    ",Blue,0.0,false",

    // T1
    ",Red,140.0,true",
    ",Blue,140.0,true",
    ",Red,139.0,true",
    ",Blue,139.0,true",
    ",Red,137.0,true",
    ",Blue,137.0,true",

    // T2
    "R,Red,137.0,true",
    "B,Red,137.0,true",
    "R,Blue,137.0,true",
    "B,Blue,137.0,true",
    "R,Red,134.0,true",
    "B,Red,134.0,true",
    "R,Blue,134.0,true",
    "B,Blue,134.0,true",
    "R,Red,131.0,true",
    "B,Red,131.0,true",
    "R,Blue,131.0,true",
    "B,Blue,131.0,true",

    // S1
    "R,Red,130.0,false",
    "B,Red,130.0,true",
    "R,Blue,130.0,true",
    "B,Blue,130.0,false",
    "R,Red,111.0,false",
    "B,Red,111.0,true",
    "R,Blue,111.0,true",
    "B,Blue,111.0,false",
    "R,Red,106.0,false",
    "B,Red,106.0,true",
    "R,Blue,106.0,true",
    "B,Blue,106.0,false",

    // S2
    "R,Red,105.0,true",
    "B,Red,105.0,false",
    "R,Blue,105.0,false",
    "B,Blue,105.0,true",
    "R,Red,92.0,true",
    "B,Red,92.0,false",
    "R,Blue,92.0,false",
    "B,Blue,92.0,true",
    "R,Red,81.0,true",
    "B,Red,81.0,false",
    "R,Blue,81.0,false",
    "B,Blue,81.0,true",

    // S3
    "R,Red,80.0,false",
    "B,Red,80.0,true",
    "R,Blue,80.0,true",
    "B,Blue,80.0,false",
    "R,Red,74.0,false",
    "B,Red,74.0,true",
    "R,Blue,74.0,true",
    "B,Blue,74.0,false",
    "R,Red,56.0,false",
    "B,Red,56.0,true",
    "R,Blue,56.0,true",
    "B,Blue,56.0,false",

    // S4
    "R,Red,55.0,true",
    "B,Red,55.0,false",
    "R,Blue,55.0,false",
    "B,Blue,55.0,true",
    "R,Red,42.0,true",
    "B,Red,42.0,false",
    "R,Blue,42.0,false",
    "B,Blue,42.0,true",
    "R,Red,31.0,true",
    "B,Red,31.0,false",
    "R,Blue,31.0,false",
    "B,Blue,31.0,true",

    // Endgame
    "R,Red,30.0,true",
    "B,Red,30.0,true",
    "R,Blue,30.0,true",
    "B,Blue,30.0,true",
    "R,Red,17.0,true",
    "B,Red,17.0,true",
    "R,Blue,17.0,true",
    "B,Blue,17.0,true",
    "R,Red,0.0,false",
    "B,Red,0.0,false",
    "R,Blue,0.0,false",
    "B,Blue,0.0,false",
  })
  public void isAllianceHubActive_Test_OnRed_RedWins(
      String gameData, Alliance alliance, double matchTime, boolean isActiveExpected) {
    MockedStatic<DriverStation> driverStationMock = mockStatic(DriverStation.class);
    driverStationMock.when(DriverStation::getGameSpecificMessage).thenReturn(gameData);
    driverStationMock.when(DriverStation::getAlliance).thenReturn(Optional.of(alliance));
    driverStationMock.when(DriverStation::getMatchTime).thenReturn(matchTime);

    boolean isAllianceHubActive = Helpers.isAllianceHubActive();
    assertEquals(isActiveExpected, isAllianceHubActive);
    driverStationMock.close();
  }
}
