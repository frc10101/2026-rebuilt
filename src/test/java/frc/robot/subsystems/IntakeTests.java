package frc.robot.subsystems;

import static edu.wpi.first.units.Units.Degrees;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj2.command.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IntakeTests {
  private Intake mIntake;

  @BeforeEach
  void setup() {
    assert HAL.initialize(500, 0);
    mIntake = new Intake();
  }

  @Test
  void canCreateIntakeTest() {
    assertNotNull(mIntake);
  }

  @Test
  void getPivotAngleNotNullTest() {
    assertNotNull(mIntake.getPivotAngle());
  }

  @Test
  void getRollerVelocityNotNullTest() {
    assertNotNull(mIntake.getRollerVelocity());
  }

  @Test
  void setAngleReturnsCommandTest() {
    Command cmd = mIntake.setAngle(Degrees.of(0));
    assertNotNull(cmd);
  }

  @Test
  void setDutyCycleReturnsCommandTest() {
    Command cmd = mIntake.set(0.5);
    assertNotNull(cmd);
  }

  @Test
  void setRollerReturnsCommandTest() {
    Command cmd = mIntake.setRoller(0.5);
    assertNotNull(cmd);
  }

  @Test
  void periodicAndSimulationDontThrow() {
    assertDoesNotThrow(
        () -> {
          mIntake.periodic();
          mIntake.simulationPeriodic();
        });
  }
}
