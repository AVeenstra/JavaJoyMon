package nl.utwente.aveenstra;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Event;
import org.apache.commons.cli.*;

import java.util.Arrays;

/**
 * Created by Antoine on 18-7-2015.
 * <p>
 * Contains the main and handles the commandline arguments.
 */
public class Main {


    private static int componentStartButton = 6;
    private static int componentChildNotVisible = 0;
    private static int componentStress = 1;

    private static boolean running = true;

    /**
     * The options of the commandline interface.
     */
    public static Options options = new Options();

    static {
        options.addOption("h", "help", false, "Prints this help message.");
    }

    /**
     * The main method of this program.
     *
     * @param args The commandline arguments given to the program.
     */
    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);
            JoystickWrapper joystick = new JoystickWrapper();
            Controller controller = joystick.getController();
            Component[] components = controller.getComponents();
            for (int i = 0; i < components.length; i++) {
                Component component = components[i];
                System.out.println(i+"\t"+component.getName());
            }
            for (int i = 0; i < 100; i++) {
                controller.poll();
                Event event = new Event();
                while (controller.getEventQueue().getNextEvent(event)) {
                    Component component = event.getComponent();
                    System.out.println(component.getName()+component.getPollData());
                }

                Thread.sleep(500);
            }
        } catch (ParseException e) {
            System.err.println(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NoControllerFoundException e) {
            System.err.println("No joystick was found.");
        } finally {
            stopRunning();
        }
    }

    public static void printHelp() {
        HelpFormatter help = new HelpFormatter();
        help.printHelp("JoyMon [Options] [Path]", " Options:", options, "");
    }

    public static boolean isRunning() {
        return running;
    }

    public static void stopRunning() {
        Main.running = false;
    }
}
