package nl.utwente.aveenstra;

import java.util.Observer;

/**
 * Created by antoine on 02/08/15.
 */
public interface View extends Observer {
    default public void initView() {
        JoystickWrapper.getInstance().addObserver(this);
    }

    default void run() {
        try {
            Main.joystickThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    default int getPressingButton() {
        return 1;
    }
    default int getUnderstood() {
        return 1;
    }

    default int getTic() {
        return 1;
    }
}
