// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.vision;

import static frc.robot.Constants.VisionConstants.*;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.wpilibj.Timer;
import frc.robot.Constants;

import java.util.function.Supplier;
import org.photonvision.PhotonCamera;
import org.photonvision.simulation.PhotonCameraSim;
import org.photonvision.simulation.SimCameraProperties;
import org.photonvision.simulation.VisionSystemSim;

/** Add your docs here. */
public class VisionIOPhotonVisionSim extends VisionIOPhotonVision {
  private static VisionSystemSim visionSim;
  private static long lastUpdateCycle = Long.MIN_VALUE;
  private static final double simUpdatePeriodSecs = 0.02;

  private final Supplier<Pose2d> poseSupplier;

  /** Creates sim IO instances for all predefined cameras (indices 0-3). */
  public static VisionIOPhotonVisionSim[] createAllSimCameras(Supplier<Pose2d> poseSupplier) {
    return createSimCameras(poseSupplier, 0, 1, 2, 3);
  }

  /** Creates sim IO instances for the provided camera indices. */
  public static VisionIOPhotonVisionSim[] createSimCameras(
      Supplier<Pose2d> poseSupplier, int... cameraIndices) {
    VisionIOPhotonVisionSim[] cameras = new VisionIOPhotonVisionSim[cameraIndices.length];
    for (int i = 0; i < cameraIndices.length; i++) {
      cameras[i] = new VisionIOPhotonVisionSim(cameraIndices[i], poseSupplier);
    }
    return cameras;
  }

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

    createCameraSim(robotToCamera);
  }

  /** Convenience constructor using one of the predefined cameras (index 0-3). */
  public VisionIOPhotonVisionSim(int cameraIndex, Supplier<Pose2d> poseSupplier) {
    super(cameraIndex);
    this.poseSupplier = poseSupplier;

    createCameraSim(this.robotToCamera);
  }

  private PhotonCameraSim createCameraSim(Transform3d robotToCamera) {
    // Initialize vision sim
    if (visionSim == null) {
      visionSim = new VisionSystemSim("fruit");
      visionSim.addAprilTags(aprilTagLayout);
    }

    // Add sim camera
    var cameraProperties =
        new SimCameraProperties()
            .setCalibration(resWidth, resHeight, fovDiag)
            .setFPS(100);
    var sim = new PhotonCameraSim(camera, cameraProperties);
    sim.enableDrawWireframe(true);
    visionSim.addCamera(sim, robotToCamera);
    return sim;
  }

  @Override
  public void updateInputs(VisionIOInputs inputs) {
    updateVisionSim();
    super.updateInputs(inputs);
  }

  private void updateVisionSim() {
    if (visionSim == null) {
      return;
    }

    double now = Timer.getFPGATimestamp();
    long cycle = (long) Math.floor(now / simUpdatePeriodSecs);
    if (cycle != lastUpdateCycle) {
      visionSim.update(poseSupplier.get());
      lastUpdateCycle = cycle;
    }
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
            .setCalibration(resWidth, resHeight, fovDiag)
            .setFPS(100);

    // Camera 0
    PhotonCamera cam0 = new PhotonCamera(Constants.VisionConstants.camera0Name);
    PhotonCameraSim camSim0 = new PhotonCameraSim(cam0, cameraProperties);
    camSim0.enableDrawWireframe(true);
    visionSim.addCamera(camSim0, Constants.VisionConstants.robotToCamera0);

    // Camera 1
    PhotonCamera cam1 = new PhotonCamera(Constants.VisionConstants.camera1Name);
    PhotonCameraSim camSim1 = new PhotonCameraSim(cam1, cameraProperties);
    camSim1.enableDrawWireframe(true);
    visionSim.addCamera(camSim1, Constants.VisionConstants.robotToCamera1);

    // Camera 2
    PhotonCamera cam2 = new PhotonCamera(Constants.VisionConstants.camera2Name);
    PhotonCameraSim camSim2 = new PhotonCameraSim(cam2, cameraProperties);
    camSim2.enableDrawWireframe(true);
    visionSim.addCamera(camSim2, Constants.VisionConstants.robotToCamera2);

    // Camera 3
    PhotonCamera cam3 = new PhotonCamera(Constants.VisionConstants.camera3Name);
    PhotonCameraSim camSim3 = new PhotonCameraSim(cam3, cameraProperties);
    camSim3.enableDrawWireframe(true);
    visionSim.addCamera(camSim3, Constants.VisionConstants.robotToCamera3);

    // Optional: schedule visionSim updates elsewhere using the supplied poseSupplier. We keep
    // the poseSupplier parameter to mirror the instance update path, but this static method only
    // registers cameras. The caller may still call visionSim.update(pose) as needed.
  }
}
