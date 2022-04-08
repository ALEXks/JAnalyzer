package analyzer;

import analyzer.stat.Interval;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class InsufPromt extends VBox {

    public InsufPromt(Interval inter){
        super();

        setPadding(new Insets(5, 5, 5, 5));
        setSpacing(5);
        setMaxWidth(270);

        Label promtLabel = new Label();
        promtLabel.setWrapText(true);
        promtLabel.setStyle("-fx-font-size: 12;");

        switch (inter.info.id.t) {
            case Interval.PAR:
                promtLabel.setText("Интервал соответствует параллельному циклу. " +
                        "Вероятнее всего, потери происходят из-за неверного задания " +
                        "матрицы процессоров при запуске программы или неверного " +
                        "распределения данных и вычислений.");
                break;
            case Interval.SEQ:
                promtLabel.setText("Интервал соответствует последовательному участку. " +
                        "Скорее всего, причиной потерь является наличие " +
                        "последовательного цикла, выполняющего большой " +
                        "объем вычислений.");
                break;
            case Interval.USER:
                promtLabel.setText("Подсказка по недостаточному параллелизму доступна " +
                        "только для автоматически размеченных интервалов при " +
                        "компиляции программы с флагами -e1, -e3 или -e4.");
        }

        getChildren().add(promtLabel);

        if (inter.hasChildLoopInterval()){
            Label deepLabel = new Label("Рассмотрите более глубокие интервалы для уточнения оценки "
                    + "недостаточного параллелизма.");
            deepLabel.setWrapText(true);
            deepLabel.setStyle(
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 11;"
            );
            getChildren().add(deepLabel);
        }
    }

}
