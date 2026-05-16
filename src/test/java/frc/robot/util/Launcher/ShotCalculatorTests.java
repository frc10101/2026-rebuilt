package frc.robot.util.Launcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import org.junit.jupiter.api.Test;

public class ShotCalculatorTests {
  @Test
  void shotLutInterpolatesAllShotFields() {
    ShotLUT lut = new ShotLUT();
    lut.put(1.0, new ShotParameters(2000.0, 15.0, 0.45));
    lut.put(3.0, new ShotParameters(3000.0, 25.0, 0.75));

    ShotParameters interpolated = lut.get(2.0);

    assertEquals(2500.0, interpolated.rpm(), 1e-9);
    assertEquals(20.0, interpolated.angleDeg(), 1e-9);
    assertEquals(0.60, interpolated.tofSec(), 1e-9);
  }

  @Test
  void calculatorUsesLutDirectlyAndReportsValidShot() {
    ShotLUT lut = new ShotLUT();
    lut.put(2.0, new ShotParameters(2500.0, 20.0, 0.60));

    ShotCalculator calculator = new ShotCalculator(new ShotCalculator.Config());
    calculator.loadShotLUT(lut);

    ShotCalculator.ShotInputs inputs =
        new ShotCalculator.ShotInputs(
            new Pose2d(0.0, 0.0, new Rotation2d()),
            new ChassisSpeeds(),
            new ChassisSpeeds(),
            new Translation2d(2.0, 0.0),
            new Translation2d(1.0, 0.0),
            1.0);

    ShotCalculator.LaunchParameters result = calculator.calculate(inputs);

    assertTrue(result.isValid());
    assertEquals(2500.0, result.rpm(), 1e-9);
    assertEquals(0.60, result.timeOfFlightSec(), 1e-9);
    assertEquals(2.0, result.solvedDistanceM(), 1e-9);
    assertEquals(0.0, result.driveAngle().getRadians(), 1e-9);
    assertEquals(100.0, result.confidence(), 1e-9);
    assertFalse(result.warmStartUsed());
    assertEquals(0, result.iterationsUsed());
  }

  @Test
  void calculatorReturnsInvalidWithoutShotData() {
    ShotCalculator calculator = new ShotCalculator();

    ShotCalculator.ShotInputs inputs =
        new ShotCalculator.ShotInputs(
            new Pose2d(),
            new ChassisSpeeds(),
            new ChassisSpeeds(),
            new Translation2d(1.0, 0.0),
            new Translation2d(1.0, 0.0),
            1.0);

    ShotCalculator.LaunchParameters result = calculator.calculate(inputs);

    assertFalse(result.isValid());
    assertEquals(0.0, result.rpm(), 1e-9);
    assertEquals(0.0, result.timeOfFlightSec(), 1e-9);
  }
}
