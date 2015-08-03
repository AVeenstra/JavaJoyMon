package nl.utwente.aveenstra;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Johan on 18-7-2015.
 */
public class CyberballRecordingCSV implements Observer {

    private long startTime;
    private String pathname = "test.csv";
    private PrintWriter writer;

    public CyberballRecordingCSV() throws FileNotFoundException {
        JoystickWrapper.getInstance().addObserver(this);
        writer = new PrintWriter(pathname);
        startTime = System.currentTimeMillis();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg == null) {
            writer.format("%.2f", (System.currentTimeMillis()-startTime)/1000.0);
            for (int i = 1; i < ComponentWrapper.componentWrappers.length; i++) {
                writer.format(", %.3f",Math.max(0,ComponentWrapper.componentWrappers[i].getAverage()));
            }
            writer.println();
        }
    }

    public void close() {
        if (writer != null) {
            writer.close();
        }
        JoystickWrapper.getInstance().deleteObserver(this);
    }
}
