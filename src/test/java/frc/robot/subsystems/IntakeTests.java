package frc.robot.subsystems;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import edu.wpi.first.hal.HAL;
import frc.robot.subsystems.Intake;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IntakeTests {
  private Intake mIntake;

  @BeforeEach
  void setup() {
    assert HAL.initialize(500, 0);
    mIntake = new Intake(); //ToDo Pass in Talon and Sparkmax
  }

  //Test
  void canCreateIntakeTest() {
    assertNotNull(mIntake);
  }
}
