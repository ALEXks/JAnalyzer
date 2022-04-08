package analyzer.utils;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;

public class LabeledProgressBar extends AnchorPane {
    private String getColorClassString(double progress){
        if (progress > 0.7)
            return "danger";
        if (progress > 0.4)
            return "warning";
        return "success";
    }

    public LabeledProgressBar(Node node, double value, double max){
        super();
        setPadding(new Insets(2, 5, 2, 2));

        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress(value / max);

        progressBar.getStyleClass().add(getColorClassString(value / max));

        this.getChildren().addAll(progressBar, node);

        for (Node p : getChildren()) {
            setTopAnchor(p, 3.);
            setBottomAnchor(p, 3.);
            setLeftAnchor(p, 6.);
            setRightAnchor(p, 3.);
        }
    }


}
