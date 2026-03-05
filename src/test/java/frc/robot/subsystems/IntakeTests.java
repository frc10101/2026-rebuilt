package frc.robot.subsystems;

import static edu.wpi.first.units.Units.Degrees;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntakeTests {
  private static Intake mIntake;
  private static CommandScheduler scheduler = null;

  @BeforeAll
  static void setup() {
    assert HAL.initialize(500, 0);
    mIntake = new Intake();

    scheduler = CommandScheduler.getInstance();
  }

  @AfterAll
  static void teardown() {
    // mIntake.close();
    CommandScheduler.getInstance().cancelAll();
    // CommandScheduler.getInstance().run();
  }

  @Test
  void canCreateIntakeTest() {
    assertNotNull(mIntake);
  }

  //@Test
  void setAngleTest() {
    Angle simAngle = Degrees.of(5);
    Command cmd = mIntake.setAngle(simAngle);
    assertNotNull(cmd);
    mIntake.setDefaultCommand(cmd);
    for (int i = 0; i < 50; i++) {
      // CommandScheduler.getInstance().schedule(cmd);
      // CommandScheduler.getInstance().run();
      // mIntake.simulationPeriodic();
      // mIntake.setAngle(simAngle);
      scheduler.schedule(cmd);
      scheduler.run();
      System.out.println("god hlep us " + mIntake.getPivotAngle().in(Degrees));
    }
    assertEquals(simAngle.in(Degrees), mIntake.getPivotAngle().in(Degrees), 0.5);
  }
}
