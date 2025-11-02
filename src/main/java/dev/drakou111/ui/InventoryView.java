package dev.drakou111.ui;

import dev.drakou111.blocks.*;
import dev.drakou111.blocks.templates.Block;
import dev.drakou111.logic.Direction;
import dev.drakou111.util.SpriteManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

public class InventoryView {
    private final VBox root = new VBox();
    private final GridPane grid = new GridPane();
    // Map of prototype instance -> sprite key
    private final List<Block> blocks = List.of(
            new SolidBlock(),
            new Dust(false),
            new Repeater(Direction.DOWN, 1),
            new Comparator(Direction.DOWN, false),
            new Torch(Direction.DOWN),
            new RedstoneBlock(),
            new Barrel(0),
            new Chest(0),
            new TargetBlock(),
            new CopperBulb(),
            new RedstoneLamp(),
            new StoneButton(null),
            new WoodenButton(null),
            new Lever(null),
            new StoneButton(Direction.DOWN),
            new WoodenButton(Direction.DOWN),
            new Lever(Direction.DOWN)
                                              );
    // Now the callback passes a Block instance (prototype)
    private Consumer<Block> onSelect = b -> {
    };

    public InventoryView(SpriteManager sprites) {
        root.setPadding(new Insets(5));
        root.setAlignment(Pos.TOP_CENTER);
        grid.setHgap(8);
        grid.setVgap(8);

        int cols = 3;
        int i = 0;
        for (Block block : blocks) {

            Image img = sprites.get(block.getSpriteKey());
            Button b = new Button();

            if (img != null) {
                ImageView iv = new ImageView(img);
                iv.setFitWidth(48);
                iv.setFitHeight(48);
                iv.setPreserveRatio(true);
                b.setGraphic(iv);
                b.setText(null);
            }

            // pass the prototype instance to the callback
            b.setOnAction(e -> onSelect.accept(block));
            grid.add(b, i % cols, i / cols);
            i++;
        }

        root.getChildren().add(grid);
    }

    public Node getNode() {
        return root;
    }

    public void setOnSelect(Consumer<Block> c) {
        this.onSelect = c;
    }

    public void selectPrototype(Block prototype) {
        onSelect.accept(prototype);
    }
}
