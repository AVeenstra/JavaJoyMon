package nl.utwente.aveenstra;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jfx.messagebox.MessageBox;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;

/**
 * Created by antoine on 02/08/15.
 */
public class ViewUI extends Application implements View {
    public static final String readyToRecordString = "Ready to record";
    public static final String recordingString = "Recording";
    public static final Pattern rNumberPattern = Pattern.compile("^[rR]?[0-9]+$");
    //   String datePattern = "\\d{1,2}-\\d{1,2}-\\d{4}";

    public static final Pattern filmdatePattern = Pattern.compile("^\\d{2}-\\d{2}-\\d{4}$");
    public ArrayList<UpdatingScatterChart> charts = new ArrayList<>();
    private TextField author;
    private TextField directory;
    private TextField rNumber;
    private TextField filmdate;
    private Button okButton;
    private TabPane tabPane;
    private Tab configurationTab;
    private Tab recordingTab;
    private Stage primaryStage;
    private Label state;
    private Label LabelrNumber;

    public ViewUI() {
        super();
        initView();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        ApplicationKiller killer = new ApplicationKiller();
        primaryStage.setOnCloseRequest(killer);
        primaryStage.setTitle("JavaJoyMon");
        primaryStage.setScene(createWindowTwoAxes(0, 1, true));
        primaryStage.show();

        int i = 2;
        for (; i < ComponentWrapper.axes.size() - 1; i += 2) {
            Stage temp = new Stage();
            temp.setOnCloseRequest(killer);
            temp.setTitle("JavaJoyMon");
            temp.setScene(createWindowTwoAxes(i, i + 1, false));
            temp.show();
        }

        if (i < ComponentWrapper.axes.size()) {
            Stage temp = new Stage();
            temp.setOnCloseRequest(killer);
            temp.setTitle("JavaJoyMon");
            temp.setScene(createWindowOneAxes(i));
            temp.show();
        }

        checkConfiguration();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg != null && arg instanceof Boolean) {
            charts.forEach(ViewUI.UpdatingScatterChart::setChanged);
        } else if (tabPane != null) {
            if (JoystickWrapper.getInstance().getCurrentState() == JoystickWrapper.State.Configuration) {
                Platform.runLater(new Thread() {
                    @Override
                    public void run() {
                        configurationTab.setDisable(false);
                        tabPane.getSelectionModel().select(configurationTab);
                        checkConfiguration();
                        state.setText("");
                    }
                });
            } else if (JoystickWrapper.getInstance().getCurrentState() == JoystickWrapper.State.ReadyToRecord) {
                Platform.runLater(new Thread() {
                    @Override
                    public void run() {
                        configurationTab.setDisable(false);
                        tabPane.getSelectionModel().select(recordingTab);
                        state.setText(readyToRecordString);
                    }
                });
            } else {
                Platform.runLater(new Thread() {
                    @Override
                    public void run() {
                        configurationTab.setDisable(true);
                        tabPane.getSelectionModel().select(recordingTab);
                        state.setText(recordingString);
                    }
                });
            }
        }
    }

    @Override
    public void run() {
        launch();
    }

    private Scene createWindowTwoAxes(int axes1, int axes2, boolean main) {
        ScatterChart.Data<Number, Number> data = new ScatterChart.Data<>(0, 0);

        ComponentWrapper wrapper1 = ComponentWrapper.axes.get(axes1);
        ComponentWrapper wrapper2 = ComponentWrapper.axes.get(axes2);

// Axes Sad and Anger
        UpdatingScatterChart chart = new UpdatingScatterChart(new NumberAxis(0, 500, 100), new NumberAxis(0, 500, 100), data);
        wrapper1.addUpdateFunction(chart::setXValue);
        wrapper2.addUpdateFunction(chart::setYValue);

        //      chart.set(wrapper1.getName());
        BorderPane chartPane = new BorderPane(chart);
        if (main) {
            okButton = new Button("OK");
            okButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    setReadyToRecord(true);
                }
            });

            Button directoryButton = new Button("Choose directory");
            directoryButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    DirectoryChooser chooser = new DirectoryChooser();
                    chooser.setTitle("Directory to save the files to");
                    File file = new File(directory.getText());
                    if (file.isDirectory()) {
                        chooser.setInitialDirectory(file);
                    }
                    file = chooser.showDialog(primaryStage);
                    if (file != null) {
                        directory.setText(file.getPath());
                    }
                }
            });

// Start menu.
            GridPane gridPane = new GridPane();
            gridPane.setHgap(5);
            gridPane.setVgap(5);
            gridPane.add(new Label("Author:"), 0, 0);
            gridPane.add(author = new TextField(Main.PREFERENCES.get(Main.AUTHOR, "")), 1, 0, 2, 1);
            gridPane.add(new Label("Directory:"), 0, 1);
            gridPane.add(directory = new TextField(Main.PREFERENCES.get(Main.DIRECTORY, "")), 1, 1);
            gridPane.add(directoryButton, 2, 1);
            gridPane.add(new Label("R number:"), 0, 2);
            gridPane.add(rNumber = new TextField(), 1, 2, 2, 1);
            gridPane.add(new Label("Film date:"), 0, 3, 3, 1);
            gridPane.add(filmdate = new TextField(), 1, 3, 2, 1);
            gridPane.add(okButton, 0, 4, 3, 1);

            ConfigChangeHandler changeHandler = new ConfigChangeHandler();
            author.setOnKeyReleased(changeHandler);
            directory.setOnKeyReleased(changeHandler);
            rNumber.setOnKeyReleased(changeHandler);
            filmdate.setOnKeyReleased(changeHandler);

            configurationTab = new Tab("Configuration");
            configurationTab.setClosable(false);
            configurationTab.setContent(gridPane);

// Chart Sad and Anger
            GridPane recordingGrid = new GridPane();
            recordingGrid.add(state = new Label("Ready to record"), 0, 0, 4, 1);
            recordingGrid.add(chartPane, 2, 4, 4, 1);
            // Location name axes.
            recordingGrid.add(new Label(wrapper1.getName()), 6, 5, 1, 1);
            recordingGrid.add(new Label(wrapper2.getName()), 0, 4, 1, 1);
            // Location scores Sad and Anger.
            recordingGrid.add(new UpdatingLabel(wrapper1), 0, 5, 1, 1);
            recordingGrid.add(new UpdatingLabel(wrapper2), 0, 3, 4, 1);

            recordingGrid.add(LabelrNumber = new Label(), 6, 0, 4, 1 );

            Iterator<ComponentWrapper> iterator = ComponentWrapper.buttons.iterator();
            for (int row = 2; iterator.hasNext(); row++) {
                for (int column = 0; column < 4 && iterator.hasNext(); column += 2) {
                    ComponentWrapper tempWrapper = iterator.next();
                    recordingGrid.add(new Label(tempWrapper.getName()), column, row);
                    recordingGrid.add(new UpdatingLabel(tempWrapper), column + 1, row);
                }
            }

            recordingTab = new Tab("Record");
            recordingTab.setClosable(false);
            recordingTab.setContent(recordingGrid);

            tabPane = new TabPane();
            tabPane.getTabs().add(configurationTab);
            tabPane.getTabs().add(recordingTab);

            tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
                    if (newValue == configurationTab) {
                        setConfiguration();
                    } else {
                        setReadyToRecord(false);
                    }
                }
            });

            return new Scene(tabPane, 500, 600);
        } else {
            return new Scene(chartPane, 500, 500);
        }
    }

    private void setReadyToRecord(boolean updateView) {
        Main.PREFERENCES.put(Main.AUTHOR, author.getText());
        Main.PREFERENCES.put(Main.DIRECTORY, directory.getText());
        Main.setrNumber(rNumber.getText());
        LabelrNumber.setText('R' + Main.getrNumber());
        Main.PREFERENCES.put(Main.FILMDATE, filmdate.getText());
        JoystickWrapper.getInstance().setCurrentState(JoystickWrapper.State.ReadyToRecord, updateView);
    }

    private void setConfiguration() {
        JoystickWrapper.getInstance().setCurrentState(JoystickWrapper.State.Configuration, false);
    }

    private Scene createWindowOneAxes(int axes) {
        ComponentWrapper component = ComponentWrapper.axes.get(axes);
        ScatterChart.Data<Number, Number> data = new ScatterChart.Data<>(0.1, 0);

// Chart contempt
        UpdatingScatterChart chart = new UpdatingScatterChart(new NumberAxis(0, 0, 0), new NumberAxis(0, 500, 100), data);
        component.addUpdateFunction(chart::setYValue);
        chart.setTitle(component.getName());
        return new Scene(new BorderPane(chart, null, null, new UpdatingLabel(component), null), 150, 500);
    }

    // Checks if date is the right pattern.
    public boolean checkDate(String filmdate) {
        boolean isDate = false;
        String datePattern = "\\d{1,2}-\\d{1,2}-\\d{4}";
        isDate = filmdate.matches(datePattern);
        return isDate;
    }

    // Checks if length Rnumber is correct.
    public boolean checklengthRnumber(String rNumber) {
        boolean isLength = false;
        if (rNumber.length() == 6) {
            isLength = true;
        }
        return isLength;
    }

    // Checks if every field from the start menu is filled correctly.
    public void checkConfiguration() {
        File folder = new File(directory.getText());
        boolean temp = author.getText().isEmpty() || (filmdate.getText().isEmpty() || checkDate(filmdate.getText()) == false) || !(folder.isDirectory() && folder.canWrite() && folder.canExecute()
                && folder.canRead() && rNumberPattern.matcher(rNumber.getText()).find() && !CyberballRecording.buildPath(folder, Main.trimRNumber(rNumber.getText())).exists() && checklengthRnumber(Main.trimRNumber(rNumber.getText())) == true);
        Platform.runLater(new Thread() {
            @Override
            public void run() {
                okButton.setDisable(temp);
                recordingTab.setDisable(temp);
            }
        });
    }

    public int getUnderstood() {
        while (true) {
            BlockingQueue<Integer> queue = new LinkedBlockingQueue<>(1);
            Platform.runLater(() -> queue.add(MessageBox.show(primaryStage, "Did the child understand the test?", "Finalising the test", MessageBox.YES | MessageBox.NO) == MessageBox.YES ? 2 : 1));

            try {
                return queue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public int getPressingButton() {
        while (true) {
            BlockingQueue<Integer> queue = new LinkedBlockingQueue<>(1);
            Platform.runLater(() -> queue.add(MessageBox.show(primaryStage, "Did the child press buttons until the end of the test?", "Finalising the test", MessageBox.YES | MessageBox.NO) == MessageBox.YES ? 2 : 1));
            try {
                return queue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public int getTic() {
        while (true) {
            BlockingQueue<Integer> queue = new LinkedBlockingQueue<>(1);
            Platform.runLater(() -> queue.add(MessageBox.show(primaryStage, "Did the child have a tic?", "Finalising the test", MessageBox.YES | MessageBox.NO) == MessageBox.YES ? 2 : 1));
            try {
                return queue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public class UpdatingScatterChart extends ScatterChart<Number, Number> {
        private ScatterChart.Data<Number, Number> point;

        public UpdatingScatterChart(Axis<Number> numberAxis, Axis<Number> numberAxis2, ScatterChart.Data<Number, Number> point) {
            super(numberAxis, numberAxis2);
            this.point = point;
            ScatterChart.Series<Number, Number> series = new ScatterChart.Series<>();
            series.getData().add(point);
            getData().add(series);
            setLegendVisible(false);
            setAnimated(false);
        }

        private void setChanged() {
            dataItemChanged(point);
        }

        public void setXValue(Number data) {
            point.setXValue(data);
            setChanged();
        }

        public void setYValue(Number data) {
            point.setYValue(data);
            setChanged();
        }
    }

    private class ConfigChangeHandler implements EventHandler<KeyEvent> {

        @Override
        public void handle(KeyEvent event) {
            checkConfiguration();
        }
    }

    private class ApplicationKiller implements EventHandler<WindowEvent> {

        @Override
        public void handle(WindowEvent event) {
            Main.stopRunning();
            try {
                stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.exit(0);
        }
    }

    private class UpdatingLabel extends Label {
        UpdatingLabel(ComponentWrapper component) {
            super("0");
            component.addUpdateFunction(this::setValue);
        }

        void setValue(Number value) {
            Platform.runLater(() -> setText(value.intValue() + ""));
        }
    }
}
