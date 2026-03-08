package frc.robot.subsystems;

import static edu.wpi.first.units.Units.Degrees;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.wpilibj2.command.Command;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IntakeTests {
  private Intake mIntake;

  @BeforeEach
  void setup() {
    assert HAL.initialize(500, 0);
    mIntake = new Intake();
  }

  @AfterEach
  void teardown() {
    mIntake.close();
  }

  @Test
  void canCreateIntakeTest() {
    assertNotNull(mIntake);
  }

  // @Test
  void setAngleTest() {
    Angle simAngle = Degrees.of(45);
    Command cmd = mIntake.setAngle(simAngle);
    assertNotNull(cmd);
    System.out.print(mIntake.getPivotAngle());
    assertEquals(simAngle, mIntake.getPivotAngle());
  }
}
