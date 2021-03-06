package nl.utwente.aveenstra;

import javafx.application.Application;
import javafx.stage.Stage;
import jfx.messagebox.MessageBox;
import org.apache.commons.cli.*;

import java.io.File;
import java.util.Arrays;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Created by Antoine on 18-7-2015.
 * <p>
 * Contains the main and handles the commandline arguments.
 */
public class Main {

    /**
     * Key used for author name.
     */
    public static final String AUTHOR = "author";
    public static final String CLI = "cli";

    /**
     * Key used for preferences
     */
    public static final String OUTPUT_DIR = "outputdir";
    public static final String INPUT_DIRECTORY = "inputdir";
    public static final String[] CONFIG_KEYS_STRING = new String[]{AUTHOR, OUTPUT_DIR, INPUT_DIRECTORY};
    public static final String[] CONFIG_KEYS_BOOLEAN = new String[]{CLI};
    public static final String CONFIG_NODE_NAME = "JavaJoyMon";
    public static final Preferences PREFERENCES = Preferences.userRoot().node(CONFIG_NODE_NAME);
    public static Thread joystickThread;
    /**
     * The options of the commandline interface.
     */
    public static Options options = new Options();
    private static View view;
    private static String rNumber = "";
    private static String filmDate = "";
    private static boolean running = true;

    static {
        options.addOption(AUTHOR.charAt(0)+"", AUTHOR, true, "Set the name of the author of the results");
        options.addOption(CLI.charAt(0)+"", CLI, false, "Do not open a UI, but instead start a CLI.");
        options.addOption(OUTPUT_DIR.charAt(0) + "", OUTPUT_DIR, true, "Set the root directory for the results.");
        options.addOption(INPUT_DIRECTORY.charAt(0) + "", INPUT_DIRECTORY, true, "Set the root directory for the results.");
        options.addOption("n", "n", false, "Do not search for a controller.");
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
            } else {
                System.setProperty("net.java.games.input.librarypath", new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getFile()).toPath().resolveSibling("lib").toString());
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

                if (!line.hasOption('n')) {
                    JoystickWrapper joystick = JoystickWrapper.getInstance();
                    joystickThread = new Thread(joystick, "Joystick Thread");
                    if (PREFERENCES.getBoolean(CLI, false)) {
                        view = new ViewCLI();
                    } else {
                        view = new ViewUI();
                    }
                    joystickThread.start();
                    view.run();
                    stopRunning();
                    joystickThread.join();
                } else {
                    if (PREFERENCES.getBoolean(CLI, false)) {
                        view = new ViewCLI();
                    } else {
                        view = new ViewUI();
                    }
                }
            }
        } catch (ParseException e) {
            System.err.println(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NoControllerFoundException e) {
            System.err.println("No joystick was found. Exiting application!");
            if (!PREFERENCES.getBoolean(CLI, false)) {
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

    public static String getrNumber() {
        return rNumber;
    }

    public static void setrNumber(String rNumber) {
        Main.rNumber = trimRNumber(rNumber);
    }

    public static String getFilmDate() {
        return filmDate;
    }

    public static void setFilmDate(String date) {
        filmDate = date;
    }

    public static String trimRNumber(String rNumber){
        char first = rNumber.charAt(0);
        if (first < '0' || '9' < first) {
            rNumber = rNumber.substring(1);
        }
        return rNumber;
    }

    public static View getView() {
        return view;
    }

    public static class NoJoystickFound extends Application {

        public void run() {
            launch();
        }

        @Override
        public void start(Stage primaryStage) throws Exception {
            MessageBox.show(null, "No joystick was found.", "Error starting the application", MessageBox.OK);
        }
    }
}
