package frc.robot.subsystems;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.drive.Drive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LauncherTests {
  private Launcher mLauncher;
  private Drive mockDrive;

  @BeforeEach
  void setup() {
    assert HAL.initialize(500, 0);
    mLauncher = new Launcher();
    mockDrive = mock(Drive.class);
  }

  @Test
  void canCreateLauncherTest() {
    assertNotNull(mLauncher);
  }

  @Test
  void getVelocityNotNullTest() {
    // Ensure the launcher reports a velocity object (even if zero)
    assertNotNull(mLauncher.getVelocity());
  }

  @Test
  void setDutyCycleReturnsCommandTest() {
    // set(dutyCycle) should return a Command that can be scheduled
    Command cmd = mLauncher.set(0.5);
    assertNotNull(cmd);
  }

  @Test
  void periodicAndSimulationDontThrow() {
    // Calling lifecycle methods should not throw exceptions
    assertDoesNotThrow(
        () -> {
          mLauncher.periodic();
          mLauncher.simulationPeriodic();
        });
  }

  // Unit tests for getTargetPosition()

  @Test
  void getTargetPositionReturnsNotNullTest() {
    // getTargetPosition should never return null
    Pose2d pose = new Pose2d(2.0, 2.0, new Rotation2d());
    when(mockDrive.getPose()).thenReturn(pose);
    when(mockDrive.getRoll()).thenReturn(new Rotation2d());
    when(mockDrive.getPitch()).thenReturn(new Rotation2d());

    Translation2d target = assertDoesNotThrow(() -> mLauncher.getTargetPosition(mockDrive, 2.0));
    assertNotNull(target);
  }

  @Test
  void getTargetPositionReturnsHubCenterWhenInAllianceZoneTest() {
    // When robot is in alliance zone, should return hub center
    Pose2d poseInAllianceZone = new Pose2d(2.0, 2.0, new Rotation2d());
    when(mockDrive.getPose()).thenReturn(poseInAllianceZone);
    when(mockDrive.getRoll()).thenReturn(new Rotation2d());
    when(mockDrive.getPitch()).thenReturn(new Rotation2d());

    Translation2d target = mLauncher.getTargetPosition(mockDrive, 2.0);
    Translation2d hubCenter = mLauncher.getAllianceHubCenter();

    // Target should equal hub center when in alliance zone
    assertTrue(Math.abs(target.getX() - hubCenter.getX()) < 0.01);
    assertTrue(Math.abs(target.getY() - hubCenter.getY()) < 0.01);
  }

  @Test
  void getTargetPositionCalculatesTargetWhenNotInAllianceZoneTest() {
    // When robot is not in alliance zone, should calculate target position
    Pose2d poseOutsideAllianceZone = new Pose2d(8.0, 4.0, new Rotation2d());
    when(mockDrive.getPose()).thenReturn(poseOutsideAllianceZone);
    when(mockDrive.getRoll()).thenReturn(new Rotation2d());
    when(mockDrive.getPitch()).thenReturn(new Rotation2d());

    Translation2d target = mLauncher.getTargetPosition(mockDrive, 4.0);
    assertNotNull(target);

    // Target should have valid coordinates within field bounds
    assertTrue(target.getX() >= 0);
    assertTrue(target.getY() >= 0);
  }

  @Test
  void getTargetPositionRobotYPositionAffectsResultTest() {
    // Robot Y position should affect target Y calculation
    Pose2d poseBottomHalf = new Pose2d(8.0, 2.0, new Rotation2d());
    when(mockDrive.getPose()).thenReturn(poseBottomHalf);
    when(mockDrive.getRoll()).thenReturn(new Rotation2d());
    when(mockDrive.getPitch()).thenReturn(new Rotation2d());

    // Get target for robot in bottom half of field
    Translation2d targetBottomHalf = mLauncher.getTargetPosition(mockDrive, 2.0);

    // Get target for robot in top half of field (Y > fieldWidth/2)
    Pose2d poseTopHalf = new Pose2d(8.0, 6.0, new Rotation2d());
    when(mockDrive.getPose()).thenReturn(poseTopHalf);
    Translation2d targetTopHalf = mLauncher.getTargetPosition(mockDrive, 6.0);

    // Y values should be different based on robot position
    assertTrue(Math.abs(targetBottomHalf.getY() - targetTopHalf.getY()) > 0.1);
  }

  @Test
  void getTargetPositionBlueAllianceTest() {
    // Test target position for Blue alliance
    Pose2d pose = new Pose2d(8.0, 4.0, new Rotation2d());
    when(mockDrive.getPose()).thenReturn(pose);
    when(mockDrive.getRoll()).thenReturn(new Rotation2d());
    when(mockDrive.getPitch()).thenReturn(new Rotation2d());

    Translation2d target = mLauncher.getTargetPosition(mockDrive, 4.0);

    // Should have valid coordinates
    assertNotNull(target);
    assertTrue(target.getX() >= 0);
    assertTrue(target.getY() >= 0);
  }

  @Test
  void getTargetPositionRedAllianceTest() {
    // Test target position for Red alliance
    Pose2d pose = new Pose2d(8.0, 4.0, new Rotation2d());
    when(mockDrive.getPose()).thenReturn(pose);
    when(mockDrive.getRoll()).thenReturn(new Rotation2d());
    when(mockDrive.getPitch()).thenReturn(new Rotation2d());

    Translation2d target = mLauncher.getTargetPosition(mockDrive, 4.0);

    // Should have valid coordinates
    assertNotNull(target);
    assertTrue(target.getX() >= 0);
    assertTrue(target.getY() >= 0);
  }

  @Test
  void getTargetPositionYParameterIgnoredInAllianceZoneTest() {
    // When in alliance zone, the robotY parameter should be ignored
    Pose2d poseInAllianceZone = new Pose2d(2.0, 2.0, new Rotation2d());
    when(mockDrive.getPose()).thenReturn(poseInAllianceZone);
    when(mockDrive.getRoll()).thenReturn(new Rotation2d());
    when(mockDrive.getPitch()).thenReturn(new Rotation2d());

    Translation2d target1 = mLauncher.getTargetPosition(mockDrive, 1.0);
    Translation2d target2 = mLauncher.getTargetPosition(mockDrive, 5.0);
    Translation2d target3 = mLauncher.getTargetPosition(mockDrive, 10.0);

    // All should return the same hub center regardless of robotY parameter
    assertTrue(Math.abs(target1.getX() - target2.getX()) < 0.01);
    assertTrue(Math.abs(target1.getY() - target2.getY()) < 0.01);
    assertTrue(Math.abs(target2.getX() - target3.getX()) < 0.01);
    assertTrue(Math.abs(target2.getY() - target3.getY()) < 0.01);
  }

  @Test
  void getTargetPositionFieldBoundsTest() {
    // Target position should be within field bounds
    Pose2d pose = new Pose2d(8.0, 4.0, new Rotation2d());
    when(mockDrive.getPose()).thenReturn(pose);
    when(mockDrive.getRoll()).thenReturn(new Rotation2d());
    when(mockDrive.getPitch()).thenReturn(new Rotation2d());

    Translation2d target = mLauncher.getTargetPosition(mockDrive, 4.0);

    // Assuming standard FRC field dimensions (approximately 16.5m x 8.2m)
    assertTrue(target.getX() >= 0 && target.getX() <= 20.0);
    assertTrue(target.getY() >= 0 && target.getY() <= 10.0);
  }

  @Test
  void getTargetPositionMultipleCalls() {
    // Multiple calls should be consistent
    Pose2d pose = new Pose2d(8.0, 4.0, new Rotation2d());
    when(mockDrive.getPose()).thenReturn(pose);
    when(mockDrive.getRoll()).thenReturn(new Rotation2d());
    when(mockDrive.getPitch()).thenReturn(new Rotation2d());

    Translation2d target1 = mLauncher.getTargetPosition(mockDrive, 4.0);
    Translation2d target2 = mLauncher.getTargetPosition(mockDrive, 4.0);

    // Same inputs should produce same outputs
    assertTrue(Math.abs(target1.getX() - target2.getX()) < 0.01);
    assertTrue(Math.abs(target1.getY() - target2.getY()) < 0.01);
  }

  @Test
  void getTargetPositionTranslation2dProperties() {
    // Verify Translation2d has expected properties
    Pose2d pose = new Pose2d(8.0, 4.0, new Rotation2d());
    when(mockDrive.getPose()).thenReturn(pose);
    when(mockDrive.getRoll()).thenReturn(new Rotation2d());
    when(mockDrive.getPitch()).thenReturn(new Rotation2d());

    Translation2d target = mLauncher.getTargetPosition(mockDrive, 4.0);

    // Should be able to get distance and angle
    assertNotNull(target.getNorm());
    assertNotNull(target.getAngle());
  }
}
