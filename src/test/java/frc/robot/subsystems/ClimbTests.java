package frc.robot.subsystems;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants;
import frc.robot.Constants.ClimbConstants;
import frc.robot.subsystems.climb.Climb;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClimbTests {
  private Climb leftClimb;
  private Climb rightClimb;

  @BeforeEach
  void setup() {
    assert HAL.initialize(500, 0);
    leftClimb = new Climb("Left", Constants.SparkMaxCanIDs.ClimbLeftMotor);
    rightClimb = new Climb("Right", Constants.SparkMaxCanIDs.ClimbRightMotor);
  }

  @AfterEach
  void teardown() {
    leftClimb.close();
    rightClimb.close();
  }

  @Test
  void canCreateClimbTest() {
    assertNotNull(leftClimb);
    assertNotNull(rightClimb);
  }

  @Test
  void GoToPreHangHeightReturnsCommandTest() {
    Command cmd = leftClimb.GoToPreHangHeight();
    assertNotNull(cmd);
    cmd = rightClimb.GoToPreHangHeight();
    assertNotNull(cmd);
  }

  @Test
  void GoToHangHeightReturnsCommandTest() {
    Command cmd = leftClimb.GoToHangHeight();
    assertNotNull(cmd);
    cmd = rightClimb.GoToHangHeight();
    assertNotNull(cmd);
  }

  @Test
  void GoToReleaseHeightReturnsCommandTest() {
    Command cmd = rightClimb.GoToReleaseHeight();
    assertNotNull(cmd);
    cmd = rightClimb.GoToReleaseHeight();
    assertNotNull(cmd);
  }

  @Test
  void GoToRestHeightReturnsCommandTest() {
    Command cmd = leftClimb.GoToRestHeight();
    assertNotNull(cmd);
    cmd = rightClimb.GoToRestHeight();
    assertNotNull(cmd);
  }

  @Test
  void getLeftHeightReturnsPositionTest() {
    Distance LeftHeightTestVariable = leftClimb.getHeight();
    assertEquals(ClimbConstants.RestDistance, LeftHeightTestVariable);
  }

  @Test
  void getRightHeightReturnsPositionTest() {
    Distance RightHeightTestVariable = rightClimb.getHeight();
    assertEquals(ClimbConstants.RestDistance, RightHeightTestVariable);
  }

  @Test
  void periodicAndSimulationDontThrow() {
    // Calling lifecycle methods should not throw exceptions
    assertDoesNotThrow(
        () -> {
          leftClimb.periodic();
          leftClimb.simulationPeriodic();
          rightClimb.periodic();
          rightClimb.simulationPeriodic();
        });
  }
}
