// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.vision;

import static frc.robot.Constants.VisionConstants.*;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.wpilibj.Timer;
import java.util.function.Supplier;
import org.photonvision.simulation.PhotonCameraSim;
import org.photonvision.simulation.SimCameraProperties;
import org.photonvision.simulation.VisionSystemSim;

/** Add your docs here. */
public class VisionIOPhotonVisionSim extends VisionIOPhotonVision {
  private static VisionSystemSim visionSim;
  private static long lastUpdateCycle = Long.MIN_VALUE;
  private static final double simUpdatePeriodSecs = 0.02;

  private final Supplier<Pose3d> poseSupplier;

  /** Creates sim IO instances for all predefined cameras (indices 0-3). */
  public static VisionIOPhotonVisionSim[] createAllSimCameras(Supplier<Pose3d> poseSupplier) {
    return createSimCameras(poseSupplier, 0, 1, 2, 3);
  }

  /** Creates sim IO instances for the provided camera indices. */
  public static VisionIOPhotonVisionSim[] createSimCameras(
      Supplier<Pose3d> poseSupplier, int... cameraIndices) {
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
      String name, Transform3d robotToCamera, Supplier<Pose3d> poseSupplier) {
    super(name, robotToCamera);
    this.poseSupplier = poseSupplier;

    createCameraSim(robotToCamera);
  }

  /** Convenience constructor using one of the predefined cameras (index 0-3). */
  public VisionIOPhotonVisionSim(int cameraIndex, Supplier<Pose3d> poseSupplier) {
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
        new SimCameraProperties().setCalibration(resWidth, resHeight, fovDiag).setFPS(30);
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
}
