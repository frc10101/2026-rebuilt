package frc.robot.subsystems;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants.ClimbConstants;
import frc.robot.subsystems.climb.Climb;
import frc.robot.subsystems.climb.ClimbType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClimbTests {
  private Climb mClimb;

  @BeforeEach
  void setup() {
    assert HAL.initialize(500, 0);
    mClimb = new Climb();
  }

  @AfterEach
  void teardown() {
    mClimb.close();
  }

  @Test
  void canCreateClimbTest() {
    assertNotNull(mClimb);
  }

  @Test
  void GoToPreHangHeightReturnsCommandTest() {
    Command cmd = mClimb.GoToPreHangHeight(ClimbType.LEFT);
    assertNotNull(cmd);
    cmd = mClimb.GoToPreHangHeight(ClimbType.RIGHT);
    assertNotNull(cmd);
    cmd = mClimb.GoToPreHangHeight(ClimbType.BOTH);
    assertNotNull(cmd);
  }

  @Test
  void GoToHangHeightReturnsCommandTest() {
    Command cmd = mClimb.GoToHangHeight(ClimbType.LEFT);
    assertNotNull(cmd);
    cmd = mClimb.GoToHangHeight(ClimbType.RIGHT);
    assertNotNull(cmd);
    cmd = mClimb.GoToHangHeight(ClimbType.BOTH);
    assertNotNull(cmd);
  }

  @Test
  void GoToReleaseHeightReturnsCommandTest() {
    Command cmd = mClimb.GoToReleaseHeight(ClimbType.LEFT);
    assertNotNull(cmd);
    cmd = mClimb.GoToReleaseHeight(ClimbType.RIGHT);
    assertNotNull(cmd);
    cmd = mClimb.GoToReleaseHeight(ClimbType.BOTH);
    assertNotNull(cmd);
  }

  @Test
  void GoToRestHeightReturnsCommandTest() {
    Command cmd = mClimb.GoToRestHeight(ClimbType.LEFT);
    assertNotNull(cmd);
    cmd = mClimb.GoToRestHeight(ClimbType.RIGHT);
    assertNotNull(cmd);
    cmd = mClimb.GoToRestHeight(ClimbType.BOTH);
    assertNotNull(cmd);
  }

  @Test
  void getLeftHeightReturnsPositionTest() {
    Distance LeftHeightTestVariable = mClimb.getLeftHeight();
    assertEquals(ClimbConstants.RestDistance, LeftHeightTestVariable);
  }

  @Test
  void getRightHeightReturnsPositionTest() {
    Distance RightHeightTestVariable = mClimb.getRightHeight();
    assertEquals(ClimbConstants.RestDistance, RightHeightTestVariable);
  }

  @Test
  void periodicAndSimulationDontThrow() {
    // Calling lifecycle methods should not throw exceptions
    assertDoesNotThrow(
        () -> {
          mClimb.periodic();
          mClimb.simulationPeriodic();
        });
  }
}
