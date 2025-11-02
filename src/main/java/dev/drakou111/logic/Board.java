package dev.drakou111.logic;

import dev.drakou111.blocks.Air;
import dev.drakou111.blocks.templates.Block;
import dev.drakou111.ui.undo.BoardAction;
import dev.drakou111.ui.undo.CompoundAction;
import dev.drakou111.ui.undo.PlaceAction;

import java.util.*;

public class Board {
    private final Block[][] grid;
    private final List<Block> updateQueue;
    private final List<Event> eventQueue;
    private boolean sendUpdates;

    public Board(int width, int height) {
        this.updateQueue = new LinkedList<>();
        this.eventQueue = new LinkedList<>();
        this.sendUpdates = true;

        // Slow, but called once and will be helpful for sending updates through air.
        this.grid = new Block[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = new Air();
            }
        }
        hookAll();
    }

    private static long keyFor(int x, int y) {
        return ((long) x << 32) | (y & 0xffffffffL);
    }

    public void setSendUpdates(boolean sendUpdates) {
        this.sendUpdates = sendUpdates;
    }

    public int getHeight() {
        return grid.length;
    }

    public int getWidth() {
        return grid[0].length;
    }

    public void setBlockAt(Block block, int x, int y) {
        if (x < 0 || x >= grid[0].length || y < 0 || y >= grid.length) return;
        if (block == null) return;

        boolean success;
        success = block.hook(this, x, y);
        if (success) {
            grid[y][x] = block;
            if (sendUpdates) pushUpdate(block);

            // hook neighboring blocks
            for (Direction dir : Direction.values()) {
                int nx = x + dir.toXOffset();
                int ny = y + dir.toYOffset();
                Block neighbor = getBlockAt(nx, ny);
                if (neighbor != null) {
                    success = neighbor.hook(this, nx, ny);
                    if (!success) setBlockAt(null, nx, ny);
                    if (sendUpdates) pushUpdate(neighbor);
                }
            }
        }
    }

    public void setBlockAtWithUndo(Block newBlock, int x, int y, CompoundAction compoundAction, Deque<BoardAction> undoStack, Deque<BoardAction> redoStack) {
        boolean createdLocalCompound = false;
        if (compoundAction == null) {
            compoundAction = new CompoundAction();
            createdLocalCompound = true;
        }

        Set<Long> visited = compoundAction.getVisitedSet();

        setBlockAtWithUndoInternal(newBlock, x, y, compoundAction, visited);

        if (createdLocalCompound && !compoundAction.isEmpty()) {
            if (compoundAction.size() == 1) {
                BoardAction single = compoundAction.getOnlyAction();
                pushUndo(single, undoStack, redoStack);
            } else {
                pushUndo(compoundAction, undoStack, redoStack);
            }
        }
    }

    private void setBlockAtWithUndoInternal(Block newBlock, int x, int y, CompoundAction compoundAction, Set<Long> visited) {
        if (x < 0 || x >= grid[0].length || y < 0 || y >= grid.length) return;

        long key = keyFor(x, y);
        if (visited.contains(key)) return;
        visited.add(key);

        Block oldBlock = grid[y][x];

        if (Objects.equals(oldBlock, newBlock)) return;

        boolean placed;
        placed = newBlock.hook(this, x, y);
        if (placed) {
            grid[y][x] = newBlock;
            if (sendUpdates) pushUpdate(newBlock);

            for (Direction dir : Direction.values()) {
                int nx = x + dir.toXOffset();
                int ny = y + dir.toYOffset();
                if (nx < 0 || nx >= grid[0].length || ny < 0 || ny >= grid.length) continue;

                Block neighbor = getBlockAt(nx, ny);
                if (neighbor != null) {
                    boolean sideSuccess;
                    try {
                        sideSuccess = neighbor.hook(this, nx, ny);
                    } catch (Throwable t) {
                        sideSuccess = false;
                    }
                    if (!sideSuccess) {
                        setBlockAtWithUndoInternal(new Air(), nx, ny, compoundAction, visited);
                    } else {
                        if (sendUpdates) pushUpdate(neighbor);
                    }
                }
            }
        }

        BoardAction action;
        action = new PlaceAction(x, y, oldBlock, newBlock);
        compoundAction.add(action);
    }

    private void pushUndo(BoardAction action, Deque<BoardAction> undoStack, Deque<BoardAction> redoStack) {
        undoStack.push(action);
        redoStack.clear();
    }

    public Block getBlockAt(int x, int y) {
        if (x < 0 || x >= grid[0].length || y < 0 || y >= grid.length) return null;
        return grid[y][x];
    }

    // Should be avoided to reduce lag, fine to use when loading.
    public void hookAll() {
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[0].length; x++) {
                if (grid[y][x] == null) continue;
                grid[y][x].hook(this, x, y);
            }
        }
    }

    public void tryPushEvent(Event event) {
        if (!event.getBlockToUpdate().isScheduled()) {
            event.getBlockToUpdate().setSchedule(true);
            eventQueue.add(event);
        }
    }

    public void pushUpdate(Block update) {
        updateQueue.add(update);
    }

    public void tick() {
        List<Event> eventsToRun = new ArrayList<>();

        for (Event event : eventQueue) {
            event.decrementDelay();
            if (event.getDelay() == 0) {
                eventsToRun.add(event);
            }
        }

        eventQueue.removeAll(eventsToRun);
        eventsToRun.sort(Comparator.comparingInt(Event::getPriority)); // Priority -1 before 0

        for (Event event : eventsToRun) {
            event.applyEvent(this);
        }

        for (Block update : updateQueue) {
            update.update(this);
        }

        updateQueue.clear();
    }
}
