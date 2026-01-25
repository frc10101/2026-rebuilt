package frc.robot.subsystems;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import edu.wpi.first.hal.HAL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FeederTests {
  private Feeder mFeeder;

  @BeforeEach
  void setup() {
    assert HAL.initialize(500, 0);
    mFeeder = new Feeder();
  }

  @Test
  void canCreateFeederTest() {
    assertNotNull(mFeeder);
  }
}
