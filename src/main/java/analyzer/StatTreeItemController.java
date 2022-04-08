package analyzer;

import javafx.fxml.FXML;
import javafx.scene.control.Label;


public class StatTreeItemController {
    @FXML private Label execLabel;
    @FXML private Label coefLabel;
    @FXML private Label typeLabel;

    public void init(double exec, double coef, String type){
        execLabel.setText(String.format("%.3f", exec));
        coefLabel.setText(String.format("%.3f", coef));
        typeLabel.setText(type);
    }
}
