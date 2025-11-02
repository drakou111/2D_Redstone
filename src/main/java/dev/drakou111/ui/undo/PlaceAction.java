package dev.drakou111.ui.undo;

import dev.drakou111.blocks.templates.Block;
import dev.drakou111.logic.Board;

public class PlaceAction extends BoardAction {
    final Block previousBlock, placedBlock;

    public PlaceAction(int x, int y, Block previousBlock, Block placedBlock) {
        super(x, y);
        this.previousBlock = previousBlock;
        this.placedBlock = placedBlock;
    }

    @Override
    public void undo(Board board) {
        board.setBlockAt(previousBlock, x, y);
    }

    @Override
    public void redo(Board board) {
        board.setBlockAt(placedBlock, x, y);
    }
}
