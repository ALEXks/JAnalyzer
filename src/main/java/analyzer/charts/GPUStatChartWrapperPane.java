package analyzer.charts;

import analyzer.stat.Interval;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class GPUStatChartWrapperPane extends BorderPane {
    GPUStatChart gpuStatChart;
    AnchorPane anchorPane;
    ToggleButton copyButton;

    public GPUStatChartWrapperPane() {
        super();
        AnchorPane.setTopAnchor(this, .0);
        AnchorPane.setBottomAnchor(this, .0);
        AnchorPane.setLeftAnchor(this, .0);
        AnchorPane.setRightAnchor(this, .0);

        anchorPane = new AnchorPane();
        anchorPane.setMinSize(AnchorPane.USE_PREF_SIZE, AnchorPane.USE_PREF_SIZE);
        anchorPane.setPrefSize(AnchorPane.USE_COMPUTED_SIZE, AnchorPane.USE_COMPUTED_SIZE);
        anchorPane.setMaxSize(AnchorPane.USE_COMPUTED_SIZE, AnchorPane.USE_COMPUTED_SIZE);
        setCenter(anchorPane);

        gpuStatChart = new GPUStatChart();
        anchorPane.getChildren().add(gpuStatChart);

        copyButton = new ToggleButton("Копирование");

    }

    public void displayData(Interval interval) {
        gpuStatChart.displayData(interval);
    }

}
