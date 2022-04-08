package analyzer.characteristics;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class CharacteristicPane<T> extends GridPane {
    private Characteristic characteristic;

    public Characteristic getCharacteristic(){return characteristic;}
    public void setCharacteristic(Characteristic characteristic){
        this.characteristic = characteristic;
    }

    private static final NumberFormat f4 = new DecimalFormat("#0.####");

    private void initPane(Paint color){
        Label nameLabel = new Label(characteristic.getName());
        String strVal = characteristic.getVal().toString();
        if (characteristic.getVal().getClass() == Double.class || characteristic.getVal().getClass() == Float.class)
            strVal = f4.format(characteristic.getVal());
        Label valueLabel = new Label(strVal);
        valueLabel.setTextFill(color);

        nameLabel.setAlignment(Pos.CENTER_LEFT);
        valueLabel.setAlignment(Pos.CENTER_RIGHT);
        addRow(0, nameLabel, valueLabel);
        setHalignment(nameLabel, HPos.LEFT);
        setValignment(nameLabel, VPos.CENTER);
        setHalignment(valueLabel, HPos.RIGHT);
        setValignment(valueLabel, VPos.CENTER);

        setVgap(3);
        setPadding(new Insets(2, 5, 5, 5));

        ColumnConstraints column1 = new ColumnConstraints(80,100,Double.MAX_VALUE);
        ColumnConstraints column2 = new ColumnConstraints(0,100,Double.MAX_VALUE);
        column2.setHgrow(Priority.SOMETIMES);
        column1.setHgrow(Priority.SOMETIMES);
        getColumnConstraints().addAll(column1, column2);

    }

    private void initPane(){
        initPane(Color.BLACK);
    }

    public CharacteristicPane(Characteristic<T> characteristic, Paint color){
        super();
        this.characteristic = characteristic;
        initPane(color);
    }

    public CharacteristicPane(String name, T value, Paint color){
        super();
        this.characteristic = new Characteristic<>(name, value);
        initPane(color);
    }

    public CharacteristicPane(String name, T value){
        super();
        this.characteristic = new Characteristic<>(name, value);
        initPane();
    }
}
