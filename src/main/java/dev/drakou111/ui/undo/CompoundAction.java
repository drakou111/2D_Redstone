package dev.drakou111.ui.undo;

import dev.drakou111.logic.Board;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CompoundAction extends BoardAction {
    public final List<BoardAction> actions = new ArrayList<>();
    private final Set<Long> visited = new HashSet<>();

    public CompoundAction() {
        super(-1, -1);
    }

    public void add(BoardAction action) {
        actions.add(action);
    }

    @Override
    public void undo(Board board) {
        for (int i = actions.size() - 1; i >= 0; i--) actions.get(i).undo(board);
    }

    @Override
    public void redo(Board board) {
        for (BoardAction action : actions) action.redo(board);
    }

    public boolean isEmpty() {
        return actions.isEmpty();
    }

    public int size() {
        return actions.size();
    }

    public Set<Long> getVisitedSet() {
        return visited;
    }

    public BoardAction getOnlyAction() {
        return actions.size() == 1 ? actions.get(0) : null;
    }
}
