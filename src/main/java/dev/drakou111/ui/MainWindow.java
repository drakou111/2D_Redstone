package dev.drakou111.ui;


import dev.drakou111.logic.Board;
import dev.drakou111.util.SpriteManager;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;


public class MainWindow {
    private final BorderPane root = new BorderPane();
    private final Board board = new Board(100, 100);
    private final SpriteManager spriteManager = new SpriteManager();


    public MainWindow() {
        spriteManager.loadSprites();

        BoardView boardView = new BoardView(board, spriteManager);
        InventoryView inventory = new InventoryView(spriteManager);
        ToolbarView toolbar = new ToolbarView();

        inventory.setOnSelect(boardView::setHeldBlockPrototype);
        boardView.setOnPickedUp(inventory::selectPrototype);
        toolbar.setOnStep(board::tick);
        toolbar.setOnCheck(board::setSendUpdates);

        VBox right = new VBox(10, new Label("INVENTORY"), inventory.getNode());
        right.setPadding(new Insets(10));
        right.setPrefWidth(300);


        root.setCenter(boardView.getNode());
        root.setRight(right);
        root.setBottom(toolbar.getNode());

        BorderPane.setMargin(boardView.getNode(), new Insets(10));
    }


    public Node getRoot() {
        return root;
    }
}