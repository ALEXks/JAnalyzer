package analyzer;

import analyzer.stat.DVMHStatMetrics;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import analyzer.json.GPUMetricJson;
import analyzer.json.GPUTimesJson;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

public class GPUPane extends VBox {
    private final Label titleLabel = new Label();
    private final GridPane gridPane = new GridPane();

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    private static final int DVMHStatMetricCount =
            DVMHStatMetrics.DVMH_STAT_METRIC_FORCE_INT.ordinal();
    private static final String[] dvmhStatMetricsTitles = {
        "Выполение ядер",
        "Копирование с GPU на CPU",
        "Копирование с CPU на GPU",
        "Копирование с GPU на GPU",
        "[Теневые грани] Копирование с GPU на CPU",
        "[Теневые грани] Копирование с CPU на GPU",
        "[Теневые грани] Копирование с GPU на GPU",
        "[Remote] Копирование с GPU на CPU",
        "[Remote] Копирование с CPU на GPU",
        "[Remote] Копирование с GPU на GPU",
        "[Перераспределение] Копирование с GPU на CPU",
        "[Перераспределение] Копирование с CPU на GPU",
        "[Перераспределение] Копирование с GPU на GPU",
        "[Region IN] Копирование с GPU на CPU",
        "[Region IN] Копирование с CPU на GPU",
        "[Region IN] Копирование с GPU на GPU",
        "GET_ACTUAL",
        "Выполнение циклов",
        "Реорганизация данных",
        "Редукция",
        "GPU Runtime compilation",
        "Page lock host memory"
    };

    NumberFormat f4 = new DecimalFormat("#0.####");

    private String prepareValue(double value, boolean isPositive, boolean isSize,
                                boolean dashOnZero, boolean isTime)
    {
        if (isPositive && value < 0)
            return "⏤";
        if (value == 0 && dashOnZero)
            return "⏤";
        if (isSize)
        {
            if (value - 0.001 < 0) return "⏤";
            if (value >= ((long)1 << 30))
                return f4.format(value / ((long)1 << 30)) +"G";
            if (value >= ((long)1 << 20))
                return f4.format(value / ((long)1 << 20)) +"M";
            if (value >= ((long)1 << 10))
                return f4.format(value / ((long)1 << 10)) +"K";
            if (value >= 0)
                return f4.format(value) + "B";
            return "?";
        }
        if (isTime)
            return f4.format(value) + "s";
        return f4.format(value);
    }

    public GPUPane() {
        super();

        gridPane.setHgap(20);
        gridPane.setVgap(3);
        gridPane.setPadding(new Insets(5, 10, 10, 10));
        titleLabel.setPadding(new Insets(0, 0, 2, 10));
        setMinSize(VBox.USE_PREF_SIZE, VBox.USE_PREF_SIZE);
        setMaxSize(VBox.USE_PREF_SIZE, VBox.USE_PREF_SIZE);

        //-----  Style  -----//
        titleLabel.setStyle(
                "-fx-font-size: 11;"
        );
        gridPane.setStyle(
                "-fx-background-color: #f4f4f4;" +
                "-fx-background-radius: 10;" +
                "-fx-font-size: 11;"
        );

        //-----  Init labels  -----//
        gridPane.add(new Label("#"), 1, 0);
        gridPane.add(new Label("Минимум"), 2, 0);
        gridPane.add(new Label("Максимум"), 3, 0);
        gridPane.add(new Label("Сумма"), 4, 0);
        gridPane.add(new Label("Среднее"), 5, 0);
        gridPane.add(new Label("Полезное"), 6, 0);
        gridPane.add(new Label("Потерянное"), 7, 0);

        getChildren().add(titleLabel);
        getChildren().add(gridPane);
    }

    public void Init(GPUTimesJson gpuTimes, int gpuNum){
        String title = "GPU #" + gpuNum + " (" + gpuTimes.gpu_name + ")";
        setTitle(title);
        int rowNum = 1;
        ArrayList<Label> metricLabels = new ArrayList<>();
        for (int i = 0; i < DVMHStatMetricCount; ++i)
        {
            GPUMetricJson metric = gpuTimes.metrics.get(i);
            if (metric.countMeasures <= 0)
                continue;
            boolean isSize = i >= DVMHStatMetrics.DVMH_STAT_METRIC_CPY_DTOH.ordinal() &&
                i <= DVMHStatMetrics.DVMH_STAT_METRIC_CPY_GET_ACTUAL.ordinal() ||
                i == DVMHStatMetrics.DVMH_STAT_METRIC_UTIL_ARRAY_TRANSFORMATION.ordinal();
            Label metricLabel = new Label(dvmhStatMetricsTitles[i]);
            metricLabels.add(metricLabel);

            gridPane.addRow(rowNum++,
                    metricLabel,
                    new Label(String.valueOf(metric.countMeasures)),
                    new Label(prepareValue(metric.min, isSize, isSize, false, false)),
                    new Label(prepareValue(metric.max, isSize, isSize, false, false)),
                    new Label(prepareValue(metric.sum, isSize, isSize, false, false)),
                    new Label(prepareValue(metric.mean, isSize, isSize, false, false)),
                    new Label(prepareValue(metric.timeProductive, true, false, true, true)),
                    new Label(prepareValue(metric.timeLost, true, false, true, true))
                );
        }
        for (Node node: gridPane.getChildren()) {
            GridPane.setHalignment(node, HPos.RIGHT);
            GridPane.setValignment(node, VPos.CENTER);
        }
        for (Node node: metricLabels) {
            GridPane.setHalignment(node, HPos.LEFT);
        }

        //-----  Separator  -----//
        Separator separator = new Separator();
        separator.setOrientation(Orientation.HORIZONTAL);
        gridPane.add(separator, 0, rowNum++, 8, 1);

        //-----  Productive and Lost times  -----//
        GridPane prodLostPane = new GridPane();
        prodLostPane.setHgap(15);
        prodLostPane.setVgap(3);

        Label prodLabel = new Label(prepareValue(gpuTimes.prod_time, true, false, true, true));
        Label lostLabel = new Label(prepareValue(gpuTimes.lost_time, true, false, true, true));
        prodLostPane.addRow(0, new Label("Полезное время"), prodLabel);
        prodLostPane.addRow(1, new Label("Потерянное время"), lostLabel);
        GridPane.setHalignment(prodLabel, HPos.RIGHT);
        GridPane.setHalignment(lostLabel, HPos.RIGHT);

        gridPane.add(prodLostPane, 0, rowNum, 2, 1);
    }
}
