package frc.robot.util.Music;

import com.ctre.phoenix6.Orchestra;
import com.ctre.phoenix6.hardware.TalonFX;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;

public class OrchestraTesting extends TimedRobot {

    // Single motor for Orchestra
    private TalonFX motor = new TalonFX(1);

    // Orchestra object
    private Orchestra orchestra;

    // Controller
    private Joystick joystick = new Joystick(0);

    // Song filenames
    private final String PAC_VICTORY = "Pac_Man_Victory.chrp";
    private final String PAC_DIES    = "Pac_Man_Dies.chrp";
    private final String PAC_WAKAWAKA = "Pac_Man_WakaWaka.chrp";
    private final String PAC_SIREN   = "Pac_Man_Siren.chrp";
    private final String PAC_LEVELUP = "Pac_Man_LevelUp.chrp";
    private final String PAC_INTRO   = "Pac_Man_Intro.chrp";

    @Override
    public void robotInit() {
        orchestra = new Orchestra();
        orchestra.addInstrument(motor);
    }

    @Override
    public void teleopPeriodic() {
        // Each button mapped individually
        if (joystick.getRawButtonPressed(1)) {
            orchestra.stop();
            orchestra.loadMusic(PAC_VICTORY);
            orchestra.play();
        }
        if (joystick.getRawButtonPressed(2)) {
            orchestra.stop();
            orchestra.loadMusic(PAC_DIES);
            orchestra.play();
        }
        if (joystick.getRawButtonPressed(3)) {
            orchestra.stop();
            orchestra.loadMusic(PAC_WAKAWAKA);
            orchestra.play();
        }
        if (joystick.getRawButtonPressed(4)) {
            orchestra.stop();
            orchestra.loadMusic(PAC_SIREN);
            orchestra.play();
        }
        if (joystick.getRawButtonPressed(5)) {
            orchestra.stop();
            orchestra.loadMusic(PAC_LEVELUP);
            orchestra.play();
        }
        if (joystick.getRawButtonPressed(6)) {
            orchestra.stop();
            orchestra.loadMusic(PAC_INTRO);
            orchestra.play();
        }
    }
}
