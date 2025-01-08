package frc.robot;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;


public class RobotState {
    public record OdometryObservation(SwerveModulePosition[] wheelPositions, Rotation2d gyroAngle,
        double timestamp) {
    }

    public record VisionObservation(Pose2d visionPose, double timestamp, Matrix<N3, N1> stdDevs) {
    }

    private static final double poseBufferSizeSeconds = 2.0;
    private Pose2d odometryPose = new Pose2d();
    public SwerveDrivePoseEstimator swerveOdometry;
    // Odometry
    private final SwerveDriveKinematics kinematics = Constants.Swerve.swerveKinematics;
    private static RobotState instance;

    public static RobotState getInstance() {
        if (instance == null)
            instance = new RobotState();
        return instance;
    }

    private RobotState() {
        // for (int i = 0; i < 3; ++i) {
        // qStdDevs.set(i, 0, Math.pow(DriveConstants.odometryStateStdDevs.get(i, 0), 2));
        // }
        // kinematics = DriveConstants.kinematics;

        // Setup NoteVisualizer
        // NoteVisualizer.setRobotPoseSupplier(this::getEstimatedPose);
    }

    public void resetPoseEstimator(Rotation2d yaw, SwerveModulePosition[] modulePositions,
        Pose2d pose2d) {
        swerveOdometry = new SwerveDrivePoseEstimator(kinematics, yaw, modulePositions, pose2d);
    }

    public Pose2d getPose2d() {
        return swerveOdometry.getEstimatedPosition();
    }

    /** Add odometry observation */
    public void addOdometryObservation(OdometryObservation observation) {
        swerveOdometry.update(observation.gyroAngle(), observation.wheelPositions());
    }

    // public void addVisionObservation(AprilTagCameraResult observation) {
    // swerveOdometry.addVisionMeasurement(observation.estimatedRobotPose.estimatedPose.toPose2d(),
    // observation.timestamp, observation.visionMeasurementStdDevs);
    // System.out.println("Adding Vision Observation for " + observation);
    // }
}