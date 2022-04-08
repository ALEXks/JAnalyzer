package analyzer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ErrorController {
    @FXML private Button okButton;
    @FXML private Label errorLabel;

    public void setMessage(String message) {
        errorLabel.setText(message);
    }

    @FXML public void okButtonPressed(){
        Stage stage = (Stage) okButton.getScene().getWindow();
        stage.close();
    }
}
