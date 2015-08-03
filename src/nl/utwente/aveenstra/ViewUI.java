package nl.utwente.aveenstra;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Observable;

/**
 * Created by antoine on 02/08/15.
 */
public class ViewUI extends Application implements View {
    public ArrayList<UpdatingScatterChart> charts = new ArrayList<>();
    private TextField author;
    private TextField directory;
    private TextField rNumber;

    public ViewUI() {
        super();
        initView();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("test");
        primaryStage.setScene(createWindowTwoAxes(0, 1, true));
        primaryStage.show();

        int i = 2;
        for (; i < ComponentWrapper.axes.size() - 1; i += 2) {
            Stage temp = new Stage();
            temp.setTitle("test");
            temp.setScene(createWindowTwoAxes(i, i + 1, false));
            temp.show();
        }

        if (i < ComponentWrapper.axes.size()) {
            Stage temp = new Stage();
            temp.setTitle("test");
            temp.setScene(createWindowOneAxes(i));
            temp.show();
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg != null && arg instanceof Boolean) {
            for (UpdatingScatterChart chart : charts) {
                chart.setChanged();
            }
        }
    }

    @Override
    public void run() {
        launch();
    }

    private Scene createWindowTwoAxes(int axes1, int axes2, boolean main) {
        ScatterChart.Data<Number, Number> data = new ScatterChart.Data<>(0, 0);

        UpdatingScatterChart chart = new UpdatingScatterChart(new NumberAxis(0, 1000, 200), new NumberAxis(0, 1000, 200), data);
        ComponentWrapper.axes.get(axes1).setUpdateFunction(chart::setXValue);
        ComponentWrapper.axes.get(axes2).setUpdateFunction(chart::setYValue);

        chart.setTitle("pudding");
        if (main) {

            GridPane gridPane = new GridPane();
            gridPane.setHgap(5);
            gridPane.setVgap(5);
            gridPane.add(new Label("Author:"), 0, 0);
            gridPane.add(author = new TextField(), 1, 0);
            gridPane.add(new Label("Directory:"), 0, 1);
            gridPane.add(author = new TextField(), 1, 1);
            gridPane.add(new Label("R number:"), 0, 2);
            gridPane.add(author = new TextField(), 1, 2);
            gridPane.add(new Button("OK"), 0, 3, 2, 1);

            Tab configTab = new Tab("Configuration");
            configTab.setClosable(false);
            configTab.setContent(gridPane);

            Tab chartTab = new Tab("Record");
            chartTab.setClosable(false);
            chartTab.setContent(chart);

            TabPane pane = new TabPane();
            pane.getTabs().add(configTab);
            pane.getTabs().add(chartTab);
            return new Scene(pane, 500, 600);
        } else {
            return new Scene(chart, 500, 500);
        }
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

}
