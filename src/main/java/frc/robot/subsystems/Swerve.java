package frc.robot.subsystems;

import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.lib.swerve.SecondOrderSwerveDriveKinematics;
import frc.robot.Constants;
import frc.robot.SwerveModule;

/**
 * Creates swerve drive and commands for drive.
 */
public class Swerve extends SubsystemBase {
    public AHRS gyro = new AHRS(Constants.Swerve.navXID);
    public SwerveDrivePoseEstimator swerveOdometry;
    public SwerveModule[] swerveMods;
    private double fieldOffset = gyro.getYaw();
    ChassisSpeeds chassisSpeeds;
    private final Field2d field;

    /**
     * Initializes swerve modules.
     */
    public Swerve() {
        field = new Field2d();
        SmartDashboard.putData("Field Pos", field);


        swerveMods = new SwerveModule[] {new SwerveModule(0, Constants.Swerve.Mod0.constants),
            new SwerveModule(1, Constants.Swerve.Mod1.constants),
            new SwerveModule(2, Constants.Swerve.Mod2.constants),
            new SwerveModule(3, Constants.Swerve.Mod3.constants)};

        swerveOdometry = new SwerveDrivePoseEstimator(Constants.Swerve.swerveKinematics, getYaw(),
            getPositions(), new Pose2d());
    }

    /**
     * New command to set wheels inward.
     */
    public void wheelsIn() {
        swerveMods[0].setDesiredState(new SwerveModuleState(2, Rotation2d.fromDegrees(45)), false);
        swerveMods[1].setDesiredState(new SwerveModuleState(2, Rotation2d.fromDegrees(135)), false);
        swerveMods[2].setDesiredState(new SwerveModuleState(2, Rotation2d.fromDegrees(-45)), false);
        swerveMods[3].setDesiredState(new SwerveModuleState(2, Rotation2d.fromDegrees(-135)),
            false);
        this.setMotorsZero(Constants.Swerve.isOpenLoop, Constants.Swerve.isFieldRelative);
    }

    /**
     * Moves the swerve drive train
     *
     * @param translation The 2d translation in the X-Y plane
     * @param rotation The amount of rotation in the Z axis
     * @param fieldRelative Whether the movement is relative to the field or absolute
     * @param isOpenLoop Open or closed loop system
     */
    public void drive(Translation2d translation, double rotation, boolean fieldRelative,
        boolean isOpenLoop) {

        SwerveModuleState[] swerveModuleStates =
            Constants.Swerve.swerveKinematics.toSwerveModuleStates(fieldRelative
                ? ChassisSpeeds.fromFieldRelativeSpeeds(translation.getX(), translation.getY(),
                    rotation, Rotation2d.fromDegrees(getYaw().getDegrees() - fieldOffset))
                : new ChassisSpeeds(translation.getX(), translation.getY(), rotation));
        SecondOrderSwerveDriveKinematics.desaturateWheelSpeeds(swerveModuleStates,
            Constants.Swerve.maxSpeed);

        for (SwerveModule mod : swerveMods) {
            mod.setDesiredState(swerveModuleStates[mod.moduleNumber], isOpenLoop);
        }
    }

    /**
     * Sets motors to 0 or inactive.
     *
     * @param isOpenLoop Open or closed loop system
     * @param fieldRelative Whether the movement is relative to the field or absolute
     */
    public void setMotorsZero(boolean isOpenLoop, boolean fieldRelative) {
        SwerveModuleState[] swerveModuleStates =
            Constants.Swerve.swerveKinematics.toSwerveModuleStates(
                fieldRelative ? ChassisSpeeds.fromFieldRelativeSpeeds(0, 0, 0, getYaw())
                    : new ChassisSpeeds(0, 0, 0));

        for (SwerveModule mod : swerveMods) {
            mod.setDesiredState(swerveModuleStates[mod.moduleNumber], isOpenLoop);
        }
        System.out.println("Setting Zero!!!!!!");
    }

    /**
     * Used by SwerveControllerCommand in Auto
     *
     * @param desiredStates The desired states of the swerve modules
     */
    public void setModuleStates(SwerveModuleState[] desiredStates) {
        SecondOrderSwerveDriveKinematics.desaturateWheelSpeeds(desiredStates,
            Constants.Swerve.maxSpeed);

        for (SwerveModule mod : swerveMods) {
            mod.setDesiredState(desiredStates[mod.moduleNumber], false);
        }
    }

    /**
     * Returns the position of the robot on the field.
     *
     * @return The pose of the robot (x and y are in meters).
     */
    public Pose2d getPose() {
        return swerveOdometry.getEstimatedPosition();
    }

    /**
     * Resets the robot's position on the field.
     *
     * @param pose The position on the field that your robot is at.
     */
    public void resetOdometry(Pose2d pose) {
        swerveOdometry.resetPosition(getYaw(), getPositions(), pose);
    }

    /**
     * Gets the states of each swerve module.
     *
     * @return Swerve module state
     */
    public SwerveModuleState[] getStates() {
        SwerveModuleState[] states = new SwerveModuleState[4];
        for (SwerveModule mod : swerveMods) {
            states[mod.moduleNumber] = mod.getState();
        }
        return states;
    }

    /**
     * Resets the gryo to 0 offset
     */
    public void zeroGyro() {
        gyro.zeroYaw();
    }

    /**
     * Resets the gyro field relative driving offset
     */
    public void resetFieldRelativeOffset() {
        fieldOffset = getYaw().getDegrees();
    }

    /**
     * Gets the rotation degree from swerve modules.
     */
    public Rotation2d getYaw() {
        float yaw = gyro.getYaw();
        return (Constants.Swerve.invertGyro) ? Rotation2d.fromDegrees(-yaw)
            : Rotation2d.fromDegrees(yaw);
    }

    public String getStringYaw() {
        float yaw = gyro.getYaw();
        return (Constants.Swerve.invertGyro) ? "Yaw: " + (360 - yaw) : "Yaw: " + yaw;
    }

    @Override
    public void periodic() {
        swerveOdometry.update(getYaw(), getPositions());
        // send robot pos to dashboard
        field.setRobotPose(swerveOdometry.getEstimatedPosition());
        SmartDashboard.putNumber("Robot X", swerveOdometry.getEstimatedPosition().getX());
        SmartDashboard.putNumber("Robot Y", swerveOdometry.getEstimatedPosition().getY());
        SmartDashboard.putNumber("Robot Rotation",
            swerveOdometry.getEstimatedPosition().getRotation().getDegrees());
        SmartDashboard.putNumber("Gyro Yaw", getYaw().getDegrees());
        SmartDashboard.putNumber("Field Offset", fieldOffset);
        SmartDashboard.putNumber("Gyro Yaw - Offset", getYaw().getDegrees() - fieldOffset);


        for (SwerveModule mod : swerveMods) {
            SmartDashboard.putNumber("Mod " + mod.moduleNumber + " Cancoder",
                mod.getCanCoder().getDegrees());
            SmartDashboard.putNumber("Mod " + mod.moduleNumber + " Integrated",
                mod.getState().angle.getDegrees());
            SmartDashboard.putNumber("Mod " + mod.moduleNumber + " Velocity",
                mod.getState().speedMetersPerSecond);
            SmartDashboard.putNumber("Mod " + mod.moduleNumber + " Position",
                mod.getPosition().distanceMeters);
        }
    }

    /**
     * Get rotation in Degrees of Gyro
     *
     * @return Rotation of gyro in Degrees
     */
    public double getRotation() {
        return getYaw().getDegrees();
    }

    /**
     * Get position of all swerve modules
     *
     * @return Array of Swerve Module Positions
     */
    public SwerveModulePosition[] getPositions() {
        SwerveModulePosition[] positions = new SwerveModulePosition[4];

        for (SwerveModule mod : swerveMods) {
            positions[mod.moduleNumber] = mod.getPosition();

        }
        return positions;
    }

}
