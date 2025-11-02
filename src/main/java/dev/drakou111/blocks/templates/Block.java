package dev.drakou111.blocks.templates;

import dev.drakou111.logic.BlockType;
import dev.drakou111.logic.Board;
import dev.drakou111.logic.Direction;

import java.util.EnumMap;
import java.util.EnumSet;

public abstract class Block {
    private final boolean isSolid;      // Whether this block can send power through it
    private final boolean fullFace;
    private final BlockType blockType;
    private Direction direction;
    private EnumSet<Direction> outputs;  // Which direction the output is at. null if no output.
    private EnumMap<Direction, Block> neighbors;
    private boolean isScheduled;

    public Block(EnumSet<Direction> outputs, Direction direction, boolean isSolid, boolean fullFace, BlockType type) {
        this.outputs = outputs;
        this.isSolid = isSolid;
        this.direction = direction;
        this.fullFace = fullFace;
        this.blockType = type;
        this.isScheduled = false;
    }

    public boolean hook(Board board, int x, int y) {
        this.neighbors = new EnumMap<>(Direction.class);
        this.neighbors.put(Direction.LEFT, board.getBlockAt(x + Direction.LEFT.toXOffset(), y + Direction.LEFT.toYOffset()));
        this.neighbors.put(Direction.RIGHT, board.getBlockAt(x + Direction.RIGHT.toXOffset(), y + Direction.RIGHT.toYOffset()));
        this.neighbors.put(Direction.UP, board.getBlockAt(x + Direction.UP.toXOffset(), y + Direction.UP.toYOffset()));
        this.neighbors.put(Direction.DOWN, board.getBlockAt(x + Direction.DOWN.toXOffset(), y + Direction.DOWN.toYOffset()));
        return true;
    }

    public void setSchedule(boolean isScheduled) {
        this.isScheduled = isScheduled;
    }

    public boolean isScheduled() {
        return isScheduled;
    }

    public void update(Board board) {
    }

    public boolean isSolid() {
        return this.isSolid;
    }

    public boolean isFullFace() {
        return this.fullFace;
    }

    public EnumSet<Direction> getOutputs() {
        return this.outputs;
    }

    protected void setOutputs(EnumSet<Direction> outputs) {
        this.outputs = outputs;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public BlockType getBlockType() {
        return this.blockType;
    }

    public boolean hasDirection(Direction direction) {
        return this.outputs.contains(direction);
    }

    public EnumMap<Direction, Block> getNeighbors() {
        return this.neighbors;
    }

    public boolean canPlaceAt(Board board, int x, int y) {
        return true;
    }

    public void rotateClockwise() {
        if (getOutputs() != null) {
            EnumSet<Direction> newOutputs = EnumSet.noneOf(Direction.class);
            for (Direction direction : this.outputs) {
                newOutputs.add(direction.clockwise());
            }
            this.outputs = newOutputs;
        }

        if (getDirection() != null) {
            this.direction = getDirection().clockwise();
        }
    }

    public void rotateCounterClockwise() {
        if (getOutputs() != null) {
            EnumSet<Direction> newOutputs = EnumSet.noneOf(Direction.class);
            for (Direction direction : this.outputs) {
                newOutputs.add(direction.counterClockwise());
            }
            this.outputs = newOutputs;
        }

        if (getDirection() != null) {
            this.direction = getDirection().counterClockwise();
        }
    }

    public abstract String getSpriteKey();

    public abstract Block clone();
}
