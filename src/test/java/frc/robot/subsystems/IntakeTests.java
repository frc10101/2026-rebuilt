package frc.robot.subsystems;

import static edu.wpi.first.units.Units.Degrees;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import edu.wpi.first.hal.HAL;
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
  void tearDown() {
    mIntake.close();
  }

  @Test
  void canCreateIntakeTest() {
    assertNotNull(mIntake);
  }

  // Pivot Tests

  @Test
  void getPivotAngleNotNullTest() {
    // getPivotAngle should return an Angle (even if zero)
    assertNotNull(mIntake.getPivotAngle());
  }

  @Test
  void setAngleReturnsCommandTest() {
    // setAngle should return a Command that can be scheduled
    Command cmd = mIntake.setAngle(Degrees.of(0));
    assertNotNull(cmd);
  }

  @Test
  void setDutyCycleReturnsCommandTest() {
    // set(dutyCycle) should return a Command that can be scheduled
    Command cmd = mIntake.set(0.5);
    assertNotNull(cmd);
  }

  @Test
  void runSysIdReturnsCommandTest() {
    // runSysId should return a Command for system identification
    Command cmd = mIntake.runSysId();
    assertNotNull(cmd);
  }

  // Roller Tests

  @Test
  void getRollerVelocityNotNullTest() {
    // getRollerVelocity should return an AngularVelocity (even if zero)
    assertNotNull(mIntake.getRollerVelocity());
  }

  @Test
  void setRollerReturnsCommandTest() {
    // setRoller should return a Command that can be scheduled
    Command cmd = mIntake.setRoller(0.5);
    assertNotNull(cmd);
  }

  @Test
  void intakeReturnsCommandTest() {
    // intake() should return a Command for intaking game pieces
    Command cmd = mIntake.intake();
    assertNotNull(cmd);
  }

  @Test
  void outtakeReturnsCommandTest() {
    // outtake() should return a Command for outtaking game pieces
    Command cmd = mIntake.outtake();
    assertNotNull(cmd);
  }

  @Test
  void stopRollerReturnsCommandTest() {
    // stopRoller() should return a Command to stop the roller
    Command cmd = mIntake.stopRoller();
    assertNotNull(cmd);
  }

  // Lifecycle Tests

  @Test
  void periodicDoesNotThrowTest() {
    // Calling periodic should not throw exceptions
    assertDoesNotThrow(() -> mIntake.periodic());
  }

  @Test
  void simulationPeriodicDoesNotThrowTest() {
    // Calling simulationPeriodic should not throw exceptions
    assertDoesNotThrow(() -> mIntake.simulationPeriodic());
  }

  @Test
  void periodicAndSimulationPeriodicDontThrowTest() {
    // Calling both lifecycle methods together should not throw exceptions
    assertDoesNotThrow(
        () -> {
          mIntake.periodic();
          mIntake.simulationPeriodic();
        });
  }
}
