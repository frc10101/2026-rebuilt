package frc.robot.subsystems;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClimbTests {
  private Climb mClimb;

  @BeforeEach
  void setup() {
    assert HAL.initialize(500, 0);
    mClimb = new Climb();
  }

  @Test
  void canCreateClimbTest() {
    assertNotNull(mClimb);
  }
}
