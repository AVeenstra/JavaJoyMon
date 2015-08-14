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
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Observable;
import java.util.regex.Pattern;

/**
 * Created by antoine on 02/08/15.
 */
public class ViewUI extends Application implements View {
    public static final Pattern rNumberPattern = Pattern.compile("^r?[0-9]+$");
    public ArrayList<UpdatingScatterChart> charts = new ArrayList<>();
    private TextField author;
    private TextField directory;
    private TextField rNumber;
    private Button okButton;
    private TabPane tabPane;
    private Tab configurationTab;
    private Tab recordingTab;

    public ViewUI() {
        super();
        initView();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ApplicationKiller killer = new ApplicationKiller();
        primaryStage.setOnCloseRequest(killer);
        primaryStage.setTitle("test");
        primaryStage.setScene(createWindowTwoAxes(0, 1, true));
        primaryStage.show();

        int i = 2;
        for (; i < ComponentWrapper.axes.size() - 1; i += 2) {
            Stage temp = new Stage();
            temp.setOnCloseRequest(killer);
            temp.setTitle("test");
            temp.setScene(createWindowTwoAxes(i, i + 1, false));
            temp.show();
        }

        if (i < ComponentWrapper.axes.size()) {
            Stage temp = new Stage();
            temp.setOnCloseRequest(killer);
            temp.setTitle("test");
            temp.setScene(createWindowOneAxes(i));
            temp.show();
        }

        checkConfiguration();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg != null && arg instanceof Boolean) {
            for (UpdatingScatterChart chart : charts) {
                chart.setChanged();
            }
        } else if (tabPane != null) {
            if (JoystickWrapper.getInstance().getCurrentState() == JoystickWrapper.State.Configuration) {
                Platform.runLater(new Thread() {
                    @Override
                    public void run() {
                        configurationTab.setDisable(false);
                        tabPane.getSelectionModel().select(configurationTab);
                    }
                });
            } else if (JoystickWrapper.getInstance().getCurrentState() == JoystickWrapper.State.ReadyToRecord) {
                Platform.runLater(new Thread() {
                    @Override
                    public void run() {
                        configurationTab.setDisable(false);
                        tabPane.getSelectionModel().select(recordingTab);
                    }
                });
            } else {
                Platform.runLater(new Thread() {
                    @Override
                    public void run() {
                        configurationTab.setDisable(true);
                        tabPane.getSelectionModel().select(recordingTab);
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

        UpdatingScatterChart chart = new UpdatingScatterChart(new NumberAxis(0, 1000, 200), new NumberAxis(0, 1000, 200), data);
        wrapper1.setUpdateFunction(chart::setXValue);
        wrapper2.setUpdateFunction(chart::setYValue);

        chart.setTitle(wrapper1.getName() + " - " + wrapper2.getName());
        if (main) {
            okButton = new Button("OK");
            okButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    setReadyToRecord();
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
                    file = chooser.showDialog(null);
                    if (file != null) {
                        directory.setText(file.getPath());
                    }
                }
            });

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
            gridPane.add(okButton, 0, 3, 3, 1);

            ConfigChangeHandler changeHandler = new ConfigChangeHandler();
            author.setOnKeyReleased(changeHandler);
            directory.setOnKeyReleased(changeHandler);
            rNumber.setOnKeyReleased(changeHandler);

            configurationTab = new Tab("Configuration");
            configurationTab.setClosable(false);
            configurationTab.setContent(gridPane);

            recordingTab = new Tab("Record");
            recordingTab.setClosable(false);
            recordingTab.setContent(chart);

            tabPane = new TabPane();
            tabPane.getTabs().add(configurationTab);
            tabPane.getTabs().add(recordingTab);

            tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
                    if (newValue == configurationTab) {
                        setConfiguration();
                    } else {
                        setReadyToRecord();
                    }
                }
            });

            return new Scene(tabPane, 500, 600);
        } else {
            return new Scene(chart, 500, 500);
        }
    }

    private void setReadyToRecord() {
        Main.PREFERENCES.put(Main.AUTHOR, author.getText());
        Main.PREFERENCES.put(Main.DIRECTORY, directory.getText());
        JoystickWrapper.getInstance().setCurrentState(JoystickWrapper.State.ReadyToRecord);
    }

    private void setConfiguration() {
        JoystickWrapper.getInstance().setCurrentState(JoystickWrapper.State.Configuration);
    }

    private Scene createWindowOneAxes(int axes) {
        ScatterChart.Data<Number, Number> data = new ScatterChart.Data<>(0.5, 0);

        UpdatingScatterChart chart = new UpdatingScatterChart(new NumberAxis(0, 1, 1), new NumberAxis(0, 1000, 200), data);
        ComponentWrapper.axes.get(axes).setUpdateFunction(chart::setYValue);
        chart.setTitle("pudding");
        return new Scene(chart, 100, 500);
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

    public void checkConfiguration() {
        File file = new File(directory.getText());
        boolean temp = author.getText().isEmpty() || !(file.isDirectory() && file.canWrite() && file.canExecute() && file.canRead() && rNumberPattern.matcher(rNumber.getText()).find());
        Platform.runLater(new Thread() {
            @Override
            public void run() {
                okButton.setDisable(temp);
                recordingTab.setDisable(temp);
            }
        });
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
}
