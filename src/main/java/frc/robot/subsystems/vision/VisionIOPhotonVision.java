// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.vision;

import static frc.robot.Constants.VisionConstants.*;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform3d;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonCamera;
import org.photonvision.PhotonPoseEstimator;

/** Add your docs here. */
public class VisionIOPhotonVision implements VisionIO {
  protected final PhotonCamera camera;
  protected final Transform3d robotToCamera;
  protected final PhotonPoseEstimator poseEstimator;

  /** Creates IO instances for all predefined cameras (indices 0-3). */
  public static VisionIOPhotonVision[] createAllCameras() {
    return createCameras(0, 1, 2, 3);
  }

  /** Creates IO instances for the provided camera indices. */
  public static VisionIOPhotonVision[] createCameras(int... cameraIndices) {
    VisionIOPhotonVision[] cameras = new VisionIOPhotonVision[cameraIndices.length];
    for (int i = 0; i < cameraIndices.length; i++) {
      cameras[i] = new VisionIOPhotonVision(cameraIndices[i]);
    }
    return cameras;
  }

  /**
   * Creates a new VisionIOPhotonVision.
   *
   * @param name The configured name of the camera.
   * @param robotToCamera The 3D position of the camera relative to the robot.
   */
  public VisionIOPhotonVision(String name, Transform3d robotToCamera) {
    camera = new PhotonCamera(name);
    this.robotToCamera = robotToCamera;
    poseEstimator = new PhotonPoseEstimator(aprilTagLayout, robotToCamera);
  }

  /**
   * Convenience constructor that selects one of the predefined cameras and transforms from {@link
   * VisionConstants} using an index 0-3.
   *
   * @param cameraIndex index of the camera (0..3)
   */
  public VisionIOPhotonVision(int cameraIndex) {
    String name;
    Transform3d transform;
    switch (cameraIndex) {
      case 0:
        name = camera0Name;
        transform = robotToCamera0;
        break;
      case 1:
        name = camera1Name;
        transform = robotToCamera1;
        break;
      case 2:
        name = camera2Name;
        transform = robotToCamera2;
        break;
      case 3:
        name = camera3Name;
        transform = robotToCamera3;
        break;
      default:
        throw new IllegalArgumentException("cameraIndex must be between 0 and 3 inclusive");
    }

    camera = new PhotonCamera(name);
    this.robotToCamera = transform;
    poseEstimator = new PhotonPoseEstimator(aprilTagLayout, robotToCamera);
  }

  @Override
  public void updateInputs(VisionIOInputs inputs) {
    inputs.connected = camera.isConnected();

    // Read new camera observations
    Set<Short> tagIds = new HashSet<>();
    List<PoseObservation> poseObservations = new LinkedList<>();
    for (var result : camera.getAllUnreadResults()) {
      // Update latest target observation
      if (result.hasTargets()) {
        inputs.latestTargetObservation =
            Optional.of(
                new TargetObservation(
                    Rotation2d.fromDegrees(result.getBestTarget().getYaw()),
                    Rotation2d.fromDegrees(result.getBestTarget().getPitch())));
      } else {
        inputs.latestTargetObservation = Optional.empty();
      }

      for (var target : result.targets) {
        tagIds.add((short) target.fiducialId);
      }

      Optional<EstimatedRobotPose> visionEst = poseEstimator.estimateCoprocMultiTagPose(result);
      if (visionEst.isEmpty()) {
        visionEst = poseEstimator.estimateLowestAmbiguityPose(result);
      }

      if (visionEst.isPresent()) {
        // Calculate average tag distance
        double totalTagDistance = 0.0;
        for (var target : result.targets) {
          totalTagDistance += target.bestCameraToTarget.getTranslation().getNorm();
        }
        double averageTagDistance =
            result.targets.isEmpty() ? 0.0 : totalTagDistance / result.targets.size();
        double ambiguity = result.hasTargets() ? result.getBestTarget().getPoseAmbiguity() : 0.0;

        poseObservations.add(
            new PoseObservation(
                visionEst.get().timestampSeconds,
                visionEst.get().estimatedPose,
                ambiguity,
                result.targets.size(),
                averageTagDistance,
                PoseObservationType.PHOTONVISION));
      }
    }

    // Save pose observations to inputs object (clones the array)
    inputs.poseObservations = poseObservations.toArray(new PoseObservation[0]);

    // Save tag IDs to inputs objects
    inputs.tagIds = new int[tagIds.size()];
    int i = 0;
    for (int id : tagIds) {
      inputs.tagIds[i++] = id;
    }
  }
}
