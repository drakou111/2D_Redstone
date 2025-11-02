package dev.drakou111.ui.undo;

import dev.drakou111.logic.Board;

public abstract class BoardAction {
    final int x, y;

    BoardAction(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public abstract void undo(Board board);

    public abstract void redo(Board board);
}