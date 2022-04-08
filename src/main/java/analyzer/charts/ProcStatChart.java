package analyzer.charts;

import analyzer.stat.Interval;
import analyzer.json.ProcTimesJson;

import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class ProcStatChart extends StackedBarChart {
    Pattern pData = Pattern.compile("data.");
    BiConsumer<Boolean, Boolean> resetProc;
    Consumer<Integer> selectProc;

    public ProcStatChart(
            BiConsumer<Boolean, Boolean> resetProc,
            Consumer<Integer> selectProc
    ) {
        super(new CategoryAxis(), new NumberAxis());
        this.resetProc = resetProc;
        this.selectProc = selectProc;
        setTitle("Потерянное время");
        setAnimated(false);
        AnchorPane.setTopAnchor(this, .0);
        AnchorPane.setBottomAnchor(this, .0);
        AnchorPane.setLeftAnchor(this, .0);
        AnchorPane.setRightAnchor(this, .0);
    }

    //-----  Displays Labels for Data in Chart  -----//
    private void displayLabelForData(XYChart.Data<String, Double> data) {
        StackPane bar = (StackPane) data.getNode();
        Text dataText = new Text(String.format("%.2f", data.getYValue()));
        dataText.setMouseTransparent(true);
        dataText.setPickOnBounds(false);
        dataText.setStyle("-fx-font-size: 10px;");
        bar.getChildren().add(dataText);
    }

    //-----  Find data number from css list  -----//
    private Integer findDataNum(List<String> styleList) {
        // TODO: find also for axes
        for (String s : styleList) {
            if (pData.matcher(s).matches()) {
                return Integer.decode(s.replaceAll("[^0-9]", ""));
            }
        }
        return null;
    }

    public void displayLostTime(Interval interval) {
        XYChart.Series<String, Double> series1 = new XYChart.Series<>();
        XYChart.Series<String, Double> series2 = new XYChart.Series<>();
        XYChart.Series<String, Double> series3 = new XYChart.Series<>();
        XYChart.Series<String, Double> series4 = new XYChart.Series<>();
        series1.setName("Недостаточный параллелизм (sys)");
        series2.setName("Недостаточный параллелизм (user)");
        series3.setName("Простои");
        series4.setName("Коммуникации");

        List<ProcTimesJson> procTimes = interval.info.proc_times;

        //-----  Init proc data  ------//
        for (int i = 0; i < procTimes.size(); ++i) {
            series1.getData().add(new XYChart.Data<>(Integer.toString(i + 1), procTimes.get(i).insuf_sys));
            series2.getData().add(new XYChart.Data<>(Integer.toString(i + 1), procTimes.get(i).insuf_user));
            series3.getData().add(new XYChart.Data<>(Integer.toString(i + 1), procTimes.get(i).idle));
            series4.getData().add(new XYChart.Data<>(Integer.toString(i + 1), procTimes.get(i).comm));
        }

        getYAxis().setAutoRanging(interval.info.id.nlev == 0);
        getYAxis().setTickMarkVisible(true);
        getYAxis().setTickLabelsVisible(true);

        getData().clear();
        getData().addAll(series1, series2, series3, series4);

        //-----  Display labels  -----//
        for (int i = 0; i < procTimes.size(); ++i) {
            displayLabelForData(series1.getData().get(i));
            displayLabelForData(series2.getData().get(i));
            displayLabelForData(series3.getData().get(i));
            displayLabelForData(series4.getData().get(i));
        }
        setCategoryGap(20);

        setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if (mouseEvent.getClickCount() == 2) {
                    Node node = mouseEvent.getPickResult().getIntersectedNode();
                    Integer dataNum = findDataNum(node.getStyleClass());
                    if (dataNum == null) {
                        resetProc.accept(true, true);
                        return;
                    }
                    selectProc.accept(dataNum);
                }
            }
        });
    }

}
