// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.vision;

import static frc.robot.subsystems.vision.VisionConstants.aprilTagLayout;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.wpilibj.Timer;
import java.util.function.Supplier;
import org.photonvision.simulation.PhotonCameraSim;
import org.photonvision.simulation.SimCameraProperties;
import org.photonvision.simulation.VisionSystemSim;

/** Add your docs here. */
public class VisionIOPhotonVisionSim extends VisionIOPhotonVision {
  private static final double simUpdatePeriodSecs = 0.02;

  private final VisionSystemSim visionSim;
  private long lastUpdateCycle = Long.MIN_VALUE;

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
    this.visionSim = new VisionSystemSim("fruit");
    this.visionSim.addAprilTags(aprilTagLayout);
    createCameraSim(robotToCamera);
  }

  /** Convenience constructor using one of the predefined cameras (index 0-3). */
  public VisionIOPhotonVisionSim(int cameraIndex, Supplier<Pose2d> poseSupplier) {
    super(cameraIndex);
    this.poseSupplier = poseSupplier;
    this.visionSim = new VisionSystemSim("fruit");
    this.visionSim.addAprilTags(aprilTagLayout);
    createCameraSim(this.robotToCamera);
  }

  private PhotonCameraSim createCameraSim(Transform3d robotToCamera) {
    // Add sim camera
    var cameraProperties =
        new SimCameraProperties()
            .setCalibration(1280, 800, Rotation2d.fromDegrees(92.4))
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
    double now = Timer.getFPGATimestamp();
    long cycle = (long) Math.floor(now / simUpdatePeriodSecs);
    if (cycle != lastUpdateCycle) {
      visionSim.update(poseSupplier.get());
      lastUpdateCycle = cycle;
    }
  }
}
