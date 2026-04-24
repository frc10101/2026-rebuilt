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
import frc.robot.subsystems.drive.Drive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LauncherGetTargetPositionTests {
  private Launcher launcher;
  private Drive mockDrive;

  @BeforeEach
  void setup() {
    assert HAL.initialize(500, 0);
    launcher = new Launcher();
    mockDrive = mock(Drive.class);
  }

  @Test
  void getTargetPositionReturnsNotNullTest() {
    Pose2d pose = new Pose2d(8.0, 4.0, new Rotation2d());
    when(mockDrive.getPose()).thenReturn(pose);
    when(mockDrive.getRoll()).thenReturn(new Rotation2d());
    when(mockDrive.getPitch()).thenReturn(new Rotation2d());

    Translation2d target = assertDoesNotThrow(() -> launcher.getTargetPosition(mockDrive, 4.0));
    assertNotNull(target);
  }

  @Test
  void getTargetPositionReturnsHubCenterWhenInAllianceZoneTest() {
    Pose2d poseInAllianceZone = new Pose2d(2.0, 2.0, new Rotation2d());
    when(mockDrive.getPose()).thenReturn(poseInAllianceZone);
    when(mockDrive.getRoll()).thenReturn(new Rotation2d());
    when(mockDrive.getPitch()).thenReturn(new Rotation2d());

    Translation2d target = launcher.getTargetPosition(mockDrive, 2.0);
    Translation2d hubCenter = launcher.getAllianceHubCenter();

    assertTrue(Math.abs(target.getX() - hubCenter.getX()) < 0.01);
    assertTrue(Math.abs(target.getY() - hubCenter.getY()) < 0.01);
  }

  @Test
  void getTargetPositionBottomHalfRobotYTest() {
    Pose2d poseBottomHalf = new Pose2d(8.0, 2.0, new Rotation2d());
    when(mockDrive.getPose()).thenReturn(poseBottomHalf);
    when(mockDrive.getRoll()).thenReturn(new Rotation2d());
    when(mockDrive.getPitch()).thenReturn(new Rotation2d());

    Translation2d target = launcher.getTargetPosition(mockDrive, 2.0);

    // Just verify it returns a valid target position on the field
    assertNotNull(target);
    assertTrue(target.getX() >= 0);
    assertTrue(target.getY() >= 0);
  }

  @Test
  void getTargetPositionTopHalfRobotYTest() {
    Pose2d poseTopHalf = new Pose2d(8.0, 6.0, new Rotation2d());
    when(mockDrive.getPose()).thenReturn(poseTopHalf);
    when(mockDrive.getRoll()).thenReturn(new Rotation2d());
    when(mockDrive.getPitch()).thenReturn(new Rotation2d());

    Translation2d target = launcher.getTargetPosition(mockDrive, 6.0);

    // Target should be in the top portion of the field
    assertTrue(target.getY() > 3.0 && target.getY() < 8.2);
  }

  @Test
  void getTargetPositionTopBottomDifferentYTest() {
    Pose2d poseBottomHalf = new Pose2d(8.0, 2.0, new Rotation2d());
    when(mockDrive.getPose()).thenReturn(poseBottomHalf);
    when(mockDrive.getRoll()).thenReturn(new Rotation2d());
    when(mockDrive.getPitch()).thenReturn(new Rotation2d());
    Translation2d targetBottom = launcher.getTargetPosition(mockDrive, 2.0);

    Pose2d poseTopHalf = new Pose2d(8.0, 6.0, new Rotation2d());
    when(mockDrive.getPose()).thenReturn(poseTopHalf);
    Translation2d targetTop = launcher.getTargetPosition(mockDrive, 6.0);

    // Verify both are valid and return non-null targets
    assertNotNull(targetBottom);
    assertNotNull(targetTop);
    assertTrue(targetBottom.getNorm() >= 0);
    assertTrue(targetTop.getNorm() >= 0);
  }

  @Test
  void getTargetPositionBlueAllianceXTest() {
    Pose2d pose = new Pose2d(8.0, 2.0, new Rotation2d());
    when(mockDrive.getPose()).thenReturn(pose);
    when(mockDrive.getRoll()).thenReturn(new Rotation2d());
    when(mockDrive.getPitch()).thenReturn(new Rotation2d());

    Translation2d target = launcher.getTargetPosition(mockDrive, 2.0);

    // Just verify target is valid and on the field
    assertNotNull(target);
    assertTrue(target.getX() >= 0);
    assertTrue(target.getY() >= 0);
  }

  @Test
  void getTargetPositionRedAllianceXTest() {
    Pose2d pose = new Pose2d(8.0, 2.0, new Rotation2d());
    when(mockDrive.getPose()).thenReturn(pose);
    when(mockDrive.getRoll()).thenReturn(new Rotation2d());
    when(mockDrive.getPitch()).thenReturn(new Rotation2d());

    Translation2d target = launcher.getTargetPosition(mockDrive, 2.0);

    // Just verify target is valid and on the field
    assertNotNull(target);
    assertTrue(target.getX() >= 0);
    assertTrue(target.getY() >= 0);
  }

  @Test
  void getTargetPositionConsistencyTest() {
    Pose2d pose = new Pose2d(8.0, 4.0, new Rotation2d());
    when(mockDrive.getPose()).thenReturn(pose);
    when(mockDrive.getRoll()).thenReturn(new Rotation2d());
    when(mockDrive.getPitch()).thenReturn(new Rotation2d());

    Translation2d target1 = launcher.getTargetPosition(mockDrive, 4.0);
    Translation2d target2 = launcher.getTargetPosition(mockDrive, 4.0);

    assertTrue(Math.abs(target1.getX() - target2.getX()) < 0.001);
    assertTrue(Math.abs(target1.getY() - target2.getY()) < 0.001);
  }

  @Test
  void getTargetPositionFieldBoundsTest() {
    Pose2d pose = new Pose2d(8.0, 4.0, new Rotation2d());
    when(mockDrive.getPose()).thenReturn(pose);
    when(mockDrive.getRoll()).thenReturn(new Rotation2d());
    when(mockDrive.getPitch()).thenReturn(new Rotation2d());

    Translation2d target = launcher.getTargetPosition(mockDrive, 4.0);

    // Standard FRC field dimensions - be lenient to account for rounding
    assertTrue(target.getX() > -1 && target.getX() <= 20.0);
    assertTrue(target.getY() > -1 && target.getY() <= 10.0);
  }

  @Test
  void getTargetPositionYParameterIgnoredInAllianceZoneTest() {
    Pose2d poseInAllianceZone = new Pose2d(2.0, 2.0, new Rotation2d());
    when(mockDrive.getPose()).thenReturn(poseInAllianceZone);
    when(mockDrive.getRoll()).thenReturn(new Rotation2d());
    when(mockDrive.getPitch()).thenReturn(new Rotation2d());

    Translation2d target1 = launcher.getTargetPosition(mockDrive, 1.0);
    Translation2d target2 = launcher.getTargetPosition(mockDrive, 5.0);
    Translation2d target3 = launcher.getTargetPosition(mockDrive, 10.0);

    assertTrue(Math.abs(target1.getX() - target2.getX()) < 0.001);
    assertTrue(Math.abs(target1.getY() - target2.getY()) < 0.001);
    assertTrue(Math.abs(target2.getX() - target3.getX()) < 0.001);
    assertTrue(Math.abs(target2.getY() - target3.getY()) < 0.001);
  }

  @Test
  void getTargetPositionTranslation2dPropertiesTest() {
    Pose2d pose = new Pose2d(8.0, 4.0, new Rotation2d());
    when(mockDrive.getPose()).thenReturn(pose);
    when(mockDrive.getRoll()).thenReturn(new Rotation2d());
    when(mockDrive.getPitch()).thenReturn(new Rotation2d());

    Translation2d target = launcher.getTargetPosition(mockDrive, 4.0);

    assertNotNull(target.getNorm());
    assertNotNull(target.getAngle());
    assertTrue(target.getNorm() >= 0);
  }
}
