// package frc.robot.subsystems;

// import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
// import static org.junit.jupiter.api.Assertions.assertNotNull;

// import edu.wpi.first.hal.HAL;
// import edu.wpi.first.wpilibj2.command.Command;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;

// public class LauncherTests {
//   private Launcher mLauncher;

//   @BeforeEach
//   void setup() {
//     assert HAL.initialize(500, 0);
//     mLauncher = new Launcher();
//   }

//   @Test
//   void canCreateLauncherTest() {
//     assertNotNull(mLauncher);
//   }

//   @Test
//   void getVelocityNotNullTest() {
//     // Ensure the launcher reports a velocity object (even if zero)
//     assertNotNull(mLauncher.getVelocity());
//   }

//   @Test
//   void setDutyCycleReturnsCommandTest() {
//     // set(dutyCycle) should return a Command that can be scheduled
//     Command cmd = mLauncher.set(0.5);
//     assertNotNull(cmd);
//   }

//   @Test
//   void periodicAndSimulationDontThrow() {
//     // Calling lifecycle methods should not throw exceptions
//     assertDoesNotThrow(
//         () -> {
//           mLauncher.periodic();
//           mLauncher.simulationPeriodic();
//         });
//   }
// }
