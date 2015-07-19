package nl.utwente.aveenstra;

import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

import java.util.Arrays;

/**
 * Created by Johan on 18-7-2015.
 */
public class JoystickWrapper extends Thread {
    private Controller controller;

    private CyberballResults results;

    private boolean isRecording = false;

    private float x, y, z;

    public JoystickWrapper() throws NoControllerFoundException {
        setController();
    }

    public void run() {
        try {
            while (Main.isRunning()) {


                if (isRecording) {

                }
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            Main.stopRunning();
            e.printStackTrace();
        }

    }

    public Controller getController() {
        return controller;
    }

    private void setController() throws NoControllerFoundException {
        Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
        for (int i = 0; i < controllers.length && controller == null; i++) {
            if (controllers[i].getType() == Controller.Type.STICK) {
                this.controller = controllers[i];
            }
        }
        if (controller == null) {
            throw new NoControllerFoundException();
        }
    }

    public boolean isRecording() {
        return isRecording;
    }
}
