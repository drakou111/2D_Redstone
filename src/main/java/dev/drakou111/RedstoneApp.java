package dev.drakou111;


import dev.drakou111.ui.MainWindow;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class RedstoneApp extends Application {
    @Override
    public void start(Stage stage) {
        MainWindow window = new MainWindow();
        Scene scene = new Scene((Parent) window.getRoot(), 1200, 800);
        stage.setScene(scene);
        stage.setTitle("Redstone Simulator");
        stage.show();
        scene.getRoot().requestFocus();
    }
}