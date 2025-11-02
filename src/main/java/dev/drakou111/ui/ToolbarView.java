package dev.drakou111.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.util.function.Consumer;

public class ToolbarView {
    private final HBox root = new HBox(8);
    private final Timeline timeline = new Timeline();
    private Runnable onStep = () -> {
    };
    private Consumer<Boolean> onSendUpdates = b -> {
    }; // callback for checkbox
    private Consumer<Boolean> onRunningChanged = b -> {
    };
    private boolean isRunning = false;

    public ToolbarView() {
        root.setPadding(new Insets(8));
        root.setAlignment(Pos.CENTER_LEFT);

        CheckBox sendUpdates = new CheckBox("SEND UPDATES ON PLACE/BREAK");
        sendUpdates.setSelected(true);
        Button start = new Button("START");
        Button stop = new Button("STOP");
        Button step = new Button("STEP");
        TextField tpsField = new TextField();
        tpsField.setPromptText("TPS");
        tpsField.setPromptText("TPS");
        tpsField.setPrefWidth(64);
        tpsField.setText("20");
        Button quickSave = new Button("QUICK SAVE");
        Button quickLoad = new Button("QUICK LOAD");
        Button saveAs = new Button("SAVE AS");
        Button loadFrom = new Button("LOAD FROM");

        stop.setDisable(true);

        sendUpdates.setOnAction(e -> onSendUpdates.accept(sendUpdates.isSelected()));

        start.setOnAction(e -> {
            int tps = parseTPS(tpsField.getText());
            if (tps <= 0) return;

            timeline.stop();
            timeline.getKeyFrames().clear();
            KeyFrame kf = new KeyFrame(Duration.seconds(1.0 / tps), ev -> onStep.run());
            timeline.getKeyFrames().add(kf);
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();

            isRunning = true;
            start.setDisable(true);
            stop.setDisable(false);
            step.setDisable(true);
            onRunningChanged.accept(true);
        });

        step.setOnAction(e -> onStep.run());

        stop.setOnAction(e -> {
            timeline.stop();
            isRunning = false;
            start.setDisable(false);
            stop.setDisable(true);
            step.setDisable(false);
            onRunningChanged.accept(false);
        });

        tpsField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                tpsField.setText(oldVal);
            } else {
                int val = parseTPS(newVal);
                if (val < 1) tpsField.setText("1");
                else if (val > 99999) tpsField.setText("99999");
            }
        });

        quickSave.setOnAction(e -> {
        });
        quickLoad.setOnAction(e -> {
        });
        saveAs.setOnAction(e -> {
        });
        loadFrom.setOnAction(e -> {
        });

        root.getChildren().addAll(sendUpdates, start, stop, step, tpsField/*, quickSave, quickLoad, saveAs, loadFrom*/);
    }

    private int parseTPS(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public Node getNode() {
        return root;
    }

    public void setOnStep(Runnable r) {
        this.onStep = r;
    }

    public void setOnCheck(Consumer<Boolean> c) {
        this.onSendUpdates = c;
    }

    public void setOnRunningChanged(Consumer<Boolean> c) {
        this.onRunningChanged = c;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
