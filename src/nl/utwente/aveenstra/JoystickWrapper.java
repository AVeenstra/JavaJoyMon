package nl.utwente.aveenstra;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;

import java.io.IOException;
import java.util.Observable;

/**
 * Created by Johan on 18-7-2015.
 */
public class JoystickWrapper extends Observable implements Runnable {
    public enum State {
        Configuration, ReadyToRecord, Recording
    }
    private State currentState = State.Configuration;
    private static JoystickWrapper INSTANCE;
    private Controller controller;

    private CyberballRecording cyberballRecording;

    private boolean startIsPressed = false;

    private JoystickWrapper() {
        setController();
    }

    public static JoystickWrapper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new JoystickWrapper();
        }
        return INSTANCE;
    }

    public static void stop() {
        if (INSTANCE != null) {
            if (INSTANCE.cyberballRecording != null) {
                INSTANCE.cyberballRecording.close();
            }
        }
    }

    public void run() {
        try {
            setChanged();
            Event event = new Event();
            net.java.games.input.EventQueue queue = controller.getEventQueue();
            while (Main.isRunning()) {
                for (int count = 0; count < 5; count++) {
                    Thread.sleep(10);
                    controller.poll();
                    for (ComponentWrapper aComponentsToSave : ComponentWrapper.componentWrappers) {
                        if (currentState == State.Recording) {
                            if (aComponentsToSave.setDataAverage()) {
                                setChanged();
                            }
                        } else {
                            if (aComponentsToSave.setData()) {
                                setChanged();
                            }
                        }
                    }
                    while (queue.getNextEvent(event)) {
                        Component component = event.getComponent();
                        ComponentWrapper c = ComponentWrapper.getComponentWrapper(component);
                        if (c != null && c.isButton()) {
                            c.isPressed();
                        }
                    }
                    notifyObservers(currentState == State.Recording);
                }
                setChanged();
                notifyObservers();
            }
        } catch (InterruptedException e) {
            Main.stopRunning();
            e.printStackTrace();
        }

    }

    public Controller getController() {
        return controller;
    }

    private void setController() {
        Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
        for (int i = 0; i < controllers.length && controller == null; i++) {
            if (controllers[i].getType() == Controller.Type.STICK || controllers[i].getType() == Controller.Type.GAMEPAD) {
                this.controller = controllers[i];
            }
        }
        if (controller == null) {
            throw new NoControllerFoundException();
        } else {
            for (int i = 0; i < ComponentWrapper.componentWrappers.length; i++) {
                ComponentWrapper component = ComponentWrapper.componentWrappers[i];
                if (component.getComponentNumber() < controller.getComponents().length) {
                    component.setComponent(controller.getComponents()[component.getComponentNumber()]);
                } else {
                    component.setComponent(controller.getComponents()[0]);
                    System.err.println("Component number out of range please reconfigure your joystick!");
                }
            }
        }
    }

    private boolean contains(Object[] array, Object object) {
        boolean result = false;
        for (int i = 0; i < array.length && !result; i++) {
            result = array[i] == object;
        }
        return result;
    }

    public void startPressed(boolean isPressed) {
        if (isPressed != startIsPressed) {
            if (isPressed) {
                if (currentState == State.Recording) {
                    cyberballRecording.close();
                    cyberballRecording = null;
                    setCurrentState(State.Configuration, true);
                } else if (currentState == State.ReadyToRecord) {
                    for (ComponentWrapper component : ComponentWrapper.componentWrappers) {
                        component.getAverage();
                    }
                    try {
                        cyberballRecording = new CyberballRecording();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setCurrentState(State.Recording, true);
                }
            }
            startIsPressed = isPressed;
        }
    }

    public State getCurrentState() {
        return currentState;
    }

    public void setCurrentState(State currentState, boolean updateView) {
        if (this.currentState != currentState && updateView) {
            this.currentState = currentState;
            setChanged();
            notifyObservers();
        }
    }
}
