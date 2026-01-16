import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import frc.robot.util.Helpers;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class HelpersTests {
  private MockedStatic<DriverStation> driveStationMock;

  @BeforeEach
  public void setUp() {
    driveStationMock = mockStatic(DriverStation.class);
  }

  @AfterEach
  public void tearDown() {
    driveStationMock.close();
  }

  @Test
  public void isAllianceHubActive_Test_OnRed_RedWins() {
    driveStationMock.when(DriverStation::getGameSpecificMessage).thenReturn("R");
    driveStationMock.when(DriverStation::getAlliance).thenReturn(Optional.of(Alliance.Red));

    boolean isAllianceHubActive = Helpers.isAllianceHubActive();
    assertTrue(isAllianceHubActive);
  }

  @Test
  public void isAllianceHubActive_Test_OnBlue_BlueWins() {
    driveStationMock.when(DriverStation::getGameSpecificMessage).thenReturn("B");
    driveStationMock.when(DriverStation::getAlliance).thenReturn(Optional.of(Alliance.Blue));

    boolean isAllianceHubActive = Helpers.isAllianceHubActive();
    assertTrue(isAllianceHubActive);
  }
}
