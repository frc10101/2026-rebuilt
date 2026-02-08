package frc.robot.subsystems;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj2.command.Command;
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
    Command cmd = mClimb.GoToPreHangHeight();
    assertNotNull(cmd);
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
