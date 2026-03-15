// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.vision;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import java.util.Optional;
import org.littletonrobotics.junction.Logger;

/** Add your docs here. */
public interface VisionIO {

  class VisionIOInputs {
    public boolean connected = false;
    public Optional<TargetObservation> latestTargetObservation = Optional.empty();
    public PoseObservation[] poseObservations = new PoseObservation[0];
    public int[] tagIds = new int[0];

    void log() {
      Logger.recordOutput("connected", connected);
      Logger.recordOutput("poseObservations", poseObservations);
      Logger.recordOutput("tagIds", tagIds);

      Logger.recordOutput("latestTargetObservation", latestTargetObservation.isPresent() ? latestTargetObservation.get() : new TargetObservation(new Rotation2d(Double.MAX_VALUE),new Rotation2d(Double.MAX_VALUE)));
    }
  }

  /** Represents the angle to a simple target, not used for pose estimation. */
  record TargetObservation(Rotation2d tx, Rotation2d ty) {}

  /** Represents a robot pose sample used for pose estimation. */
  record PoseObservation(
      double timestamp,
      Pose3d pose,
      double ambiguity,
      int tagCount,
      double averageTagDistance,
      PoseObservationType type) {}

  enum PoseObservationType {
    MEGATAG_1,
    MEGATAG_2,
    PHOTONVISION
  }

  default void updateInputs(VisionIOInputs inputs) {}
}
