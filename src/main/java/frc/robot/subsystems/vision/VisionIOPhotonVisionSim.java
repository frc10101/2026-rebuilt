// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.vision;

import static frc.robot.subsystems.vision.VisionConstants.aprilTagLayout;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform3d;
import java.util.function.Supplier;
import org.photonvision.PhotonCamera;
import org.photonvision.simulation.PhotonCameraSim;
import org.photonvision.simulation.SimCameraProperties;
import org.photonvision.simulation.VisionSystemSim;

/** Add your docs here. */
public class VisionIOPhotonVisionSim extends VisionIOPhotonVision {
  private static VisionSystemSim visionSim;

  private final Supplier<Pose2d> poseSupplier;
  private final PhotonCameraSim cameraSim;

  /**
   * Creates a new VisionIOPhotonVisionSim.
   *
   * @param name The name of the camera.
   * @param poseSupplier Supplier for the robot pose to use in simulation.
   */
  public VisionIOPhotonVisionSim(
      String name, Transform3d robotToCamera, Supplier<Pose2d> poseSupplier) {
    super(name, robotToCamera);
    this.poseSupplier = poseSupplier;

    // Initialize vision sim
    if (visionSim == null) {
      visionSim = new VisionSystemSim("fruit");
      visionSim.addAprilTags(aprilTagLayout);
    }

    // Add sim camera
    var cameraProperties =
        new SimCameraProperties()
            .setCalibration(1280, 800, Rotation2d.fromDegrees(92.4))
            .setFPS(100);
    cameraSim = new PhotonCameraSim(camera, cameraProperties);
    cameraSim.enableDrawWireframe(true);
    visionSim.addCamera(cameraSim, robotToCamera);
  }

  @Override
  public void updateInputs(VisionIOInputs inputs) {
    visionSim.update(poseSupplier.get());
    super.updateInputs(inputs);
  }

  /**
   * Register all four predefined cameras from {@link VisionConstants} with the vision simulator.
   * This creates PhotonCamera/PhotonCameraSim instances for each configured camera name and adds
   * them to the shared VisionSystemSim using the transforms defined in {@link VisionConstants}.
   *
   * <p>Call this once during simulation setup (for example in Robot.simulationInit) to enable sim
   * cameras for all four physical cameras.
   *
   * @param poseSupplier supplier for the robot pose used by the sim when updating cameras
   */
  public static void registerAllSimCameras(Supplier<Pose2d> poseSupplier) {
    // Ensure vision sim exists
    if (visionSim == null) {
      visionSim = new VisionSystemSim("fruit");
      visionSim.addAprilTags(aprilTagLayout);
    }

    // Common camera properties
    var cameraProperties =
        new SimCameraProperties()
            .setCalibration(1280, 800, Rotation2d.fromDegrees(92.4))
            .setFPS(100);

    // Camera 0
    PhotonCamera cam0 = new PhotonCamera(VisionConstants.camera0Name);
    PhotonCameraSim camSim0 = new PhotonCameraSim(cam0, cameraProperties);
    camSim0.enableDrawWireframe(true);
    visionSim.addCamera(camSim0, VisionConstants.robotToCamera0);

    // Camera 1
    PhotonCamera cam1 = new PhotonCamera(VisionConstants.camera1Name);
    PhotonCameraSim camSim1 = new PhotonCameraSim(cam1, cameraProperties);
    camSim1.enableDrawWireframe(true);
    visionSim.addCamera(camSim1, VisionConstants.robotToCamera1);

    // Camera 2
    PhotonCamera cam2 = new PhotonCamera(VisionConstants.camera2Name);
    PhotonCameraSim camSim2 = new PhotonCameraSim(cam2, cameraProperties);
    camSim2.enableDrawWireframe(true);
    visionSim.addCamera(camSim2, VisionConstants.robotToCamera2);

    // Camera 3
    PhotonCamera cam3 = new PhotonCamera(VisionConstants.camera3Name);
    PhotonCameraSim camSim3 = new PhotonCameraSim(cam3, cameraProperties);
    camSim3.enableDrawWireframe(true);
    visionSim.addCamera(camSim3, VisionConstants.robotToCamera3);

    // Optional: schedule visionSim updates elsewhere using the supplied poseSupplier. We keep
    // the poseSupplier parameter to mirror the instance update path, but this static method only
    // registers cameras. The caller may still call visionSim.update(pose) as needed.
  }
}
