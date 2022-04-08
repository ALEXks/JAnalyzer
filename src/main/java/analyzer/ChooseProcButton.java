package analyzer;

import javafx.scene.control.*;
import javafx.scene.layout.Pane;

import java.util.function.IntPredicate;
import java.util.function.Predicate;

public class ChooseProcButton extends MenuButton {
    private final TextField textField;
    private IntPredicate chosenProcsPred = p -> true;
    Runnable selectProcs, resetSelectProcs;

    public ChooseProcButton(Runnable selectProcs, Runnable resetSelectProcs) {
        super();

        this.selectProcs = selectProcs;
        this.resetSelectProcs = resetSelectProcs;

        setText("Выбор процесса(-ов)");

        Pane pane = new Pane();
        textField = new TextField();
        textField.setPromptText("1, 3-6"); // TODO: add 2k+1
        Button chooseButton = new Button("Выбрать");

        chooseButton.setOnAction(e -> {
            parseAndSetPred(textField.getText());
            selectProcs.run();
        });

        pane.setPrefSize(188, 29);
        textField.setPrefSize(114, 29);
        chooseButton.setPrefSize(70, 29);

        chooseButton.setLayoutX(118);
        chooseButton.setLayoutY(1);

        pane.getChildren().addAll(textField, chooseButton);

        CustomMenuItem chooseProcMenuItem = new CustomMenuItem();
        chooseProcMenuItem.setContent(pane);

        MenuItem resetMenuItem = new MenuItem("Сброс");
        resetMenuItem.setOnAction(e -> reset());

        this.getItems().addAll(chooseProcMenuItem, new SeparatorMenuItem(), resetMenuItem);
    }

    private boolean isNumeric(String str) {
        return str != null && str.matches("[0-9.]+");
    }

    private boolean isRange(String str) {
        return str != null && str.matches("[0-9.]+-[0-9.]+");
    }

    private void parseAndSetPred(String text) {
        chosenProcsPred = p -> false;
        text = text.replaceAll(" ", "");
        String[] strings = text.split(",");
        for (String s : strings) {
            if (isNumeric(s)) {
                chosenProcsPred = chosenProcsPred.or(p -> p == Integer.parseInt(s));
                continue;
            }

            if (isRange(s)) {
                String[] range = s.split("-");
                if (range.length != 2) {
                    System.out.println("[WARN] invalid string in chooseProcs: " + s + "; length = " + range.length);
                    continue;
                }
                chosenProcsPred = chosenProcsPred.or(
                        p -> Integer.parseInt(range[0]) <= p && p <= Integer.parseInt(range[1])
                );
                continue;
            }

            // TODO: parse func
            System.out.println("[WARN] invalid string in chooseProcs: " + s);
        }

    }

    public IntPredicate getChosenProcsPred(){
        return chosenProcsPred;
    }

    public void reset(){
        textField.clear();
        chosenProcsPred = p -> true;
        resetSelectProcs.run();
    }

}
