package nl.utwente.aveenstra;

import javafx.application.Application;
import javafx.stage.Stage;
import jfx.messagebox.MessageBox;
import net.java.games.input.Component;
import org.apache.commons.cli.*;

import java.util.Arrays;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Created by Antoine on 18-7-2015.
 * <p>
 * Contains the main and handles the commandline arguments.
 */
public class Main {

    public static final String DEBUGGING = "blurp";
    public static final String AUTHOR = "author";
    public static final String CLI = "cli";
    public static final String DIRECTORY = "dir";
    public static final String[] CONFIG_KEYS_STRING = new String[]{AUTHOR, DIRECTORY};
    public static final String[] CONFIG_KEYS_BOOLEAN = new String[]{CLI};

    public static final String CONFIG_NODE_NAME = "JavaJoyMon";
    public static final Preferences PREFERENCES = Preferences.userRoot().node(CONFIG_NODE_NAME);
    public static Thread joystickThread;

    private static boolean running = true;

    /**
     * The options of the commandline interface.
     */
    public static Options options = new Options();

    static {
        options.addOption(AUTHOR.charAt(0)+"", AUTHOR, true, "Set the name of the author of the results");
        options.addOption(CLI.charAt(0)+"", CLI, false, "Do not open a UI, but instead start a CLI.");
        options.addOption(DEBUGGING.charAt(0)+"", DEBUGGING, false, "Start the debugging mode.");
        options.addOption(DIRECTORY.charAt(0)+"", DIRECTORY, true, "Set the root directory for the results.");
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
            if (line.hasOption('h')) {
                printHelp();
            } else if (line.hasOption(DEBUGGING)) {
                JoystickWrapper joystick = JoystickWrapper.getInstance();
                joystickThread = new Thread(joystick, "Joystick Thread");
                joystick.addObserver((o, arg) -> {
                    int i = 0;
                    for (Component c : JoystickWrapper.getInstance().getController().getComponents()) {

                        System.out.printf("%d:%1.2f  ", i++, c.getPollData());
                    }
                    System.out.print("\r");
                });
                joystickThread.run();
                joystickThread.join();
            }else {
                try {
                    System.out.println(Arrays.toString(Preferences.userRoot().node("JavaJoyMon").keys()));
                } catch (BackingStoreException e) {
                    e.printStackTrace();
                }

                for (String property : CONFIG_KEYS_STRING) {
                    if (line.hasOption(property.charAt(0))) {
                        PREFERENCES.put(property, line.getOptionValue(property.charAt(0)));
                    }
                }
                for (String property : CONFIG_KEYS_BOOLEAN) {
                    if (line.hasOption(property.charAt(0))) {
                        PREFERENCES.putBoolean(property, line.getOptionValue(property.charAt(0)).isEmpty());
                    }
                }

                JoystickWrapper joystick = JoystickWrapper.getInstance();
                joystickThread = new Thread(joystick, "Joystick Thread");
                View view;
                if (PREFERENCES.getBoolean(CLI, false)) {
                    view = new ViewCLI();
                } else {
                    view = new ViewUI();
                }
                joystickThread.start();
                view.run();
                stopRunning();
                joystickThread.join();
            }
        } catch (ParseException e) {
            System.err.println(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NoControllerFoundException e) {
            if (PREFERENCES.getBoolean(CLI, false)) {
                System.err.println("No joystick was found.");
            } else {
                new NoJoystickFound().run();
            }
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
        JoystickWrapper.stop();
    }

    public static class NoJoystickFound extends Application{

        public void run(){
            launch();
        }

        @Override
        public void start(Stage primaryStage) throws Exception {
            MessageBox.show(null, "No joystick was found.", "Error starting the application", MessageBox.OK);
        }
    }
}
