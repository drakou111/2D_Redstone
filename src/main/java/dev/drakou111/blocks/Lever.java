package dev.drakou111.blocks;

import dev.drakou111.blocks.templates.Block;
import dev.drakou111.blocks.templates.PoweredBlock;
import dev.drakou111.logic.BlockType;
import dev.drakou111.logic.Board;
import dev.drakou111.logic.Constants;
import dev.drakou111.logic.Direction;

import java.util.EnumSet;

public class Lever extends PoweredBlock {
    public Lever(Direction attachedDirection) {
        super(EnumSet.allOf(Direction.class), attachedDirection, false, false, Constants.MIN_POWER, true, BlockType.LEVER);
    }

    public void toggle(Board board) {
        setPower(isPowered() ? Constants.MIN_POWER : Constants.MAX_POWER);
        updateNeighbors(board);
    }

    @Override
    public boolean hook(Board board, int x, int y) {
        super.hook(board, x, y);
        if (getDirection() != null) {
            return getNeighbors().get(getDirection()) != null && getNeighbors().get(getDirection()).isFullFace();
        }
        return true;
    }

    @Override
    public String getSpriteKey() {
        return "lever_s" + getPower() + "_d" + (getDirection() == null ? "FLOOR" : getDirection().name());
    }

    @Override
    public Block clone() {
        Lever c = new Lever(getDirection());
        c.setOutputs(getOutputs());
        c.setPower(getPower());
        return c;
    }

    @Override
    public boolean canPlaceAt(Board board, int x, int y) {
        if (getDirection() != null) {
            Block block = board.getBlockAt(x + getDirection().toXOffset(), y + getDirection().toYOffset());
            return block != null && block.isFullFace();
        }
        return true;
    }

    private void updateNeighbors(Board board) {
        for (Block block : getNeighbors().values()) {
            if (block != null) block.update(board);
        }

        if (getDirection() != null) {
            Block block = getNeighbors().get(getDirection());
            if (block != null) {
                for (Block neighbor : block.getNeighbors().values()) {
                    if (neighbor == null || neighbor == this) continue;
                    neighbor.update(board);
                }
            }
        }
    }
}
