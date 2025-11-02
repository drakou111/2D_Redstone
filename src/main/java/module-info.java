module Redstone2D {
    requires javafx.controls;
    requires javafx.fxml;

    opens dev.drakou111 to javafx.fxml;
    exports dev.drakou111;
}