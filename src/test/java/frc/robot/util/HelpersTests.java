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
    "R,Red,true",
    "B,Blue,true",
    "R,Blue,false",
    "B,Red,false",
  })
  public void isAllianceHubActive_Test_OnRed_RedWins(
      String gameData, Alliance alliance, boolean isActiveExpected) {
    MockedStatic<DriverStation> driverStationMock = mockStatic(DriverStation.class);
    driverStationMock.when(DriverStation::getGameSpecificMessage).thenReturn(gameData);
    driverStationMock.when(DriverStation::getAlliance).thenReturn(Optional.of(alliance));

    boolean isAllianceHubActive = Helpers.isAllianceHubActive();
    assertEquals(isActiveExpected, isAllianceHubActive);
    driverStationMock.close();
  }
}
