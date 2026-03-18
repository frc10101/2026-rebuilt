// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.vision;

import static frc.robot.Constants.VisionConstants.*;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.wpilibj.Alert;
import edu.wpi.first.wpilibj.Alert.AlertType;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.subsystems.vision.VisionIO.PoseObservationType;
import frc.robot.subsystems.vision.VisionIO.VisionIOInputs;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.littletonrobotics.junction.Logger;

public class Vision extends SubsystemBase {
  private final VisionConsumer consumer;
  private final VisionIO[] io;
  private final VisionIOInputs[] inputs;
  private final Alert[] disconnectedAlerts;

  public Vision(VisionConsumer consumer, VisionIO... io) {
    this.consumer = consumer;
    this.io = io;

    // Initialize inputs
    this.inputs = new VisionIOInputs[io.length];
    for (int i = 0; i < inputs.length; i++) {
      inputs[i] = new VisionIOInputs();
    }

    // Initialize disconnected alerts
    this.disconnectedAlerts = new Alert[io.length];
    for (int i = 0; i < inputs.length; i++) {
      disconnectedAlerts[i] =
          new Alert(
              "Vision camera " + Integer.toString(i) + " is disconnected.", AlertType.kWarning);
    }
  }

  /**
   * Returns the X angle to the best target, which can be used for simple servoing with vision.
   *
   * @param cameraIndex The index of the camera to use.
   */
  public Optional<Rotation2d> getTargetX(int cameraIndex) {
    if (cameraIndex > 3 || cameraIndex < 0) {
      return Optional.empty();
    }
    var observation = inputs[cameraIndex].latestTargetObservation;
    if (observation.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(observation.get().tx());
  }

  public double getFiducialID(int cameraIndex) {
    if (cameraIndex > 3 || cameraIndex < 0) {
      System.out.println("No Camera");
      return -1;
    }
    // Check if there are any pose observations available
    if (inputs[cameraIndex].poseObservations.length == 0) {
      return -1; // Return -1 if no tags are detected
    }

    // Find the closest tag based on averageTagDistance
    VisionIO.PoseObservation closestObservation = inputs[cameraIndex].poseObservations[0];
    for (VisionIO.PoseObservation observation : inputs[cameraIndex].poseObservations) {
      if (observation.averageTagDistance() < closestObservation.averageTagDistance()) {
        closestObservation = observation;
      }
    }

    // Return the ID of the closest tag
    if (closestObservation.tagCount() > 0) {
      return inputs[cameraIndex].tagIds[0]; // Assuming tagIds correspond to poseObservations
    }

    return -1; // Return -1 if no valid tag is found
  }

  public boolean getTV(int cameraIndex) {
    return inputs[cameraIndex].tagIds.length > 0;
  }
  /**
   * Returns the 3D transform of the robot in the coordinate system of the primary in-view AprilTag.
   * The result is an array of six elements: [tx, ty, tz, pitch, yaw, roll].
   *
   * @param cameraIndex The index of the camera to use.
   * @return An array representing the robot's pose in the target's coordinate system: [tx, ty, tz,
   *     pitch, yaw, roll] (meters, degrees).
   */
  public Optional<double[]> getBotPose_TargetSpace(int cameraIndex) {
    Optional<double[]> WrongTag = Optional.empty();
    if (cameraIndex > 3 || cameraIndex < 0) {
      return null;
    }

    // Check if there are any tag IDs available
    if (inputs[cameraIndex].tagIds.length == 0) {
      // return new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0}; // Return zero array if no tag is
      // detected
      return WrongTag;
    }

    // Get the primary tag ID and its pose
    int primaryTagId = inputs[cameraIndex].tagIds[0];
    var tagPoseOptional = aprilTagLayout.getTagPose(primaryTagId);

    if (tagPoseOptional.isEmpty()) {
      return WrongTag; // Return zero array if tag pose is unavailable
    }

    Pose3d tagPose = tagPoseOptional.get();

    // Get the robot's pose relative to the camera
    if (inputs[cameraIndex].poseObservations.length == 0) {
      return WrongTag; // Return zero array if no pose observations
    }

    Pose3d robotPoseRelativeToCamera = inputs[cameraIndex].poseObservations[0].pose();

    // Transform the robot's pose into the tag's coordinate system
    Pose3d robotPoseInTagSpace = tagPose.relativeTo(robotPoseRelativeToCamera);

    // Extract translation and rotation components
    double tx = robotPoseInTagSpace.getX(); // Translation X (meters)
    double ty = robotPoseInTagSpace.getY(); // Translation Y (meters)
    double tz = robotPoseInTagSpace.getZ(); // Translation Z (meters)

    var rotation = robotPoseInTagSpace.getRotation();

    double pitch = Math.toDegrees(rotation.getX()); // Pitch (degrees)
    double yaw = Math.toDegrees(rotation.getY()); // Yaw (degrees)
    double roll = Math.toDegrees(rotation.getZ()); // Roll (degrees)

    Optional<double[]> RightTag = Optional.of(new double[] {tx, ty, tz, pitch, yaw, roll});

    // Return the result as an array
    // return new double[] {tx, ty, tz, pitch, yaw, roll};
    return RightTag;
  }

  @Override
  public void periodic() {
    for (int i = 0; i < io.length; i++) {
      io[i].updateInputs(inputs[i]);
      inputs[i].log();
    }

    // Initialize logging values
    List<Pose3d> allTagPoses = new LinkedList<>();
    List<Pose3d> allRobotPoses = new LinkedList<>();
    List<Pose3d> allRobotPosesAccepted = new LinkedList<>();
    List<Pose3d> allRobotPosesRejected = new LinkedList<>();

    // Loop over cameras
    for (int cameraIndex = 0; cameraIndex < io.length; cameraIndex++) {
      // Update disconnected alert
      disconnectedAlerts[cameraIndex].set(!inputs[cameraIndex].connected);

      // Initialize logging values
      List<Pose3d> tagPoses = new LinkedList<>();
      List<Pose3d> robotPoses = new LinkedList<>();
      List<Pose3d> robotPosesAccepted = new LinkedList<>();
      List<Pose3d> robotPosesRejected = new LinkedList<>();

      // Add tag poses
      for (int tagId : inputs[cameraIndex].tagIds) {
        var tagPose = aprilTagLayout.getTagPose(tagId);
        if (tagPose.isPresent()) {
          tagPoses.add(tagPose.get());
        }
      }

      // Loop over pose observations
      for (var observation : inputs[cameraIndex].poseObservations) {
        // Check whether to reject pose
        boolean rejectPose =
            observation.tagCount() == 0 // Must have at least one tag
                || (observation.tagCount() == 1
                    && observation.ambiguity() > maxAmbiguity) // Cannot be high ambiguity
                || Math.abs(observation.pose().getZ())
                    > maxZError // Must have realistic Z coordinate

                // Must be within the field boundaries
                || observation.pose().getX() < 0.0
                || observation.pose().getX() > aprilTagLayout.getFieldLength()
                || observation.pose().getY() < 0.0
                || observation.pose().getY() > aprilTagLayout.getFieldWidth();

        // Add pose to log
        robotPoses.add(observation.pose());
        if (rejectPose) {
          robotPosesRejected.add(observation.pose());
        } else {
          robotPosesAccepted.add(observation.pose());
        }

        // Skip if rejected
        if (rejectPose) {
          continue;
        }

        // Calculate standard deviations (PhotonVision heuristic)
        Matrix<N3, N1> stdDevs = updateEstimationStdDevs(cameraIndex, observation);
        double linearStdDev = stdDevs.get(0, 0);
        double angularStdDev = stdDevs.get(2, 0);

        // Send vision observation
        consumer.accept(
            observation.pose().toPose2d(),
            observation.timestamp(),
            VecBuilder.fill(linearStdDev, linearStdDev, angularStdDev));
      }

      // Log camera datadata
      Logger.recordOutput(
          "Vision/Camera" + Integer.toString(cameraIndex) + "/TagPoses",
          tagPoses.toArray(new Pose3d[tagPoses.size()]));
      Logger.recordOutput(
          "Vision/Camera" + Integer.toString(cameraIndex) + "/RobotPoses",
          robotPoses.toArray(new Pose3d[robotPoses.size()]));
      Logger.recordOutput(
          "Vision/Camera" + Integer.toString(cameraIndex) + "/RobotPosesAccepted",
          robotPosesAccepted.toArray(new Pose3d[robotPosesAccepted.size()]));
      Logger.recordOutput(
          "Vision/Camera" + Integer.toString(cameraIndex) + "/RobotPosesRejected",
          robotPosesRejected.toArray(new Pose3d[robotPosesRejected.size()]));
      allTagPoses.addAll(tagPoses);
      allRobotPoses.addAll(robotPoses);
      allRobotPosesAccepted.addAll(robotPosesAccepted);
      allRobotPosesRejected.addAll(robotPosesRejected);
    }

    // Log summary data
    Logger.recordOutput(
        "Vision/Summary/TagPoses", allTagPoses.toArray(new Pose3d[allTagPoses.size()]));
    Logger.recordOutput(
        "Vision/Summary/RobotPoses", allRobotPoses.toArray(new Pose3d[allRobotPoses.size()]));
    Logger.recordOutput(
        "Vision/Summary/RobotPosesAccepted",
        allRobotPosesAccepted.toArray(new Pose3d[allRobotPosesAccepted.size()]));
    Logger.recordOutput(
        "Vision/Summary/RobotPosesRejected",
        allRobotPosesRejected.toArray(new Pose3d[allRobotPosesRejected.size()]));
  }

  private Matrix<N3, N1> updateEstimationStdDevs(
      int cameraIndex, VisionIO.PoseObservation observation) {
    Matrix<N3, N1> singleTagStdDevs =
        VecBuilder.fill(linearStdDevBaseline, linearStdDevBaseline, angularStdDevBaseline);
    Matrix<N3, N1> multiTagStdDevs =
        VecBuilder.fill(
            linearStdDevBaseline * 0.5, linearStdDevBaseline * 0.5, angularStdDevBaseline * 0.5);

    Matrix<N3, N1> estStdDevs = singleTagStdDevs;
    int numTags = observation.tagCount();
    double avgDist = observation.averageTagDistance();

    if (numTags == 0) {
      estStdDevs = singleTagStdDevs;
    } else {
      if (numTags > 1) {
        estStdDevs = multiTagStdDevs;
      }
      if (numTags == 1 && avgDist > 2.0) {
        estStdDevs = VecBuilder.fill(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
      } else {
        estStdDevs = estStdDevs.times(1.0 + (avgDist * avgDist / 30.0));
      }
    }

    if (observation.type() == PoseObservationType.MEGATAG_2) {
      estStdDevs =
          VecBuilder.fill(
              estStdDevs.get(0, 0) * linearStdDevMegatag2Factor,
              estStdDevs.get(1, 0) * linearStdDevMegatag2Factor,
              estStdDevs.get(2, 0) * angularStdDevMegatag2Factor);
    }

    if (cameraIndex < cameraStdDevFactors.length) {
      estStdDevs = estStdDevs.times(cameraStdDevFactors[cameraIndex]);
    }

    return estStdDevs;
  }

  @FunctionalInterface
  public interface VisionConsumer {
    void accept(
        Pose2d visionRobotPoseMeters,
        double timestampSeconds,
        Matrix<N3, N1> visionMeasurementStdDevs);
  }
}
