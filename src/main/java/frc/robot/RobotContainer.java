package frc.robot;

import edu.wpi.first.wpilibj.AddressableLEDBufferView;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.Robot.RobotRunType;
import frc.robot.commands.TeleopSwerve;
import frc.robot.subsystems.LEDs;
import frc.robot.subsystems.swerve.Swerve;
import frc.robot.subsystems.swerve.SwerveIO;
import frc.robot.subsystems.swerve.SwerveReal;
import frc.robot.subsystems.swerve.SwerveSim;


/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
    AddressableLEDBufferView m_left = Robot.m_ledBuffer.createView(0, 59);
    AddressableLEDBufferView m_right = Robot.m_ledBuffer.createView(60, 119).reversed();


    /* Shuffleboard */
    private final SendableChooser<Command> autoChooser = new SendableChooser<>();

    /* Controllers */
    public final CommandXboxController driver = new CommandXboxController(Constants.DRIVER_ID);

    /* Subsystems */
    private Swerve s_Swerve;
    // private Vision s_Vision = new Vision();
    private LEDs s_LEDs1 = new LEDs(m_left);
    private LEDs s_LEDs2 = new LEDs(m_right);

    /**
     */
    public RobotContainer(RobotRunType runtimeType) {

        switch (runtimeType) {
            case kReal:
                s_Swerve = new Swerve(new SwerveReal());
                break;
            case kSimulation:
                s_Swerve = new Swerve(new SwerveSim());
                break;
            default:
                s_Swerve = new Swerve(new SwerveIO() {});
        }

        // autoChooser.addOption("P32", new P32(s_Swerve, elevatorWrist, intake, shooter));
        // autoChooser.addOption("P675", new P675(s_Swerve, elevatorWrist, intake, shooter));
        // autoChooser.addOption("P3675", new P3675(s_Swerve, elevatorWrist, intake, shooter));

        s_Swerve.setDefaultCommand(new TeleopSwerve(s_Swerve, driver,
            Constants.Swerve.isFieldRelative, Constants.Swerve.isOpenLoop));
        s_LEDs1.setDefaultCommand(s_LEDs1.runDefault(Color.kRed));
        s_LEDs2.setDefaultCommand(s_LEDs2.runDefault2(Color.kBlue));
        // Configure the button bindings
        // CanandEventLoop.getInstance();
        configureButtonBindings();
    }

    /**
     * Use this method to vol your button->command mappings. Buttons can be created by instantiating
     * a {@link GenericHID} or one of its subclasses ({@link edu.wpi.first.wpilibj.Joystick} or
     * {@link XboxController}), and then passing it to a
     * {@link edu.wpi.first.wpilibj2.command.button.JoystickButton}.
     */
    private void configureButtonBindings() {


        /* Driver Buttons */
        driver.y().onTrue(new InstantCommand(() -> s_Swerve.resetFieldRelativeOffset()));

        driver.x().whileTrue(s_Swerve.runNeo(1));

        // driver.a()
        // .whileTrue(CommandFactory.rotateToGamePiece(s_Swerve, s_Vision::getObjectHeading));
    }

    /**
     * Gets the user's selected autonomous command.
     *
     * @return Returns autonomous command selected.
     */
    public Command getAutonomousCommand() {
        Command autocommand = autoChooser.getSelected();
        return autocommand;
    }
}
