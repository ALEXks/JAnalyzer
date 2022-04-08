package analyzer;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ErrorDialog {

    private ErrorController errorController;
    private Stage stage;

    public ErrorDialog(String message){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("errorForm.fxml"));

        Pane p;
        try {
            p = fxmlLoader.load();
        } catch (Exception e){
            System.out.println("Error opening analyzer.ErrorDialog: " + e);
            return;
        }
        errorController = fxmlLoader.getController();
        errorController.setMessage(message);
        Scene scene = new Scene(p);
        stage = new Stage();
        scene.getStylesheets().add("analyzer/bootstrap3.css");
        stage.setScene(scene);
        stage.getIcons().add(new Image("analyzer/warr.png"));
        stage.setTitle("Ошибка");
        stage.initModality(Modality.APPLICATION_MODAL);
    }

    public void showDialog(){
        if (stage != null)
            stage.showAndWait();
    }

}
