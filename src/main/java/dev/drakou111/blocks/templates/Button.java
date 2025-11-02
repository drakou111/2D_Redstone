package dev.drakou111.blocks.templates;

import dev.drakou111.logic.*;

import java.util.EnumSet;

public abstract class Button extends PoweredBlock {
    int cooldown;

    public Button(Direction attachedDirection, int cooldown, BlockType type) {
        super(EnumSet.allOf(Direction.class), attachedDirection, false, false, Constants.MIN_POWER, true, type);
        this.cooldown = cooldown;
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
    public void doScheduledTick(Board board) {
        setPower(Constants.MIN_POWER);
        updateNeighbors(board);
    }

    @Override
    public boolean canPlaceAt(Board board, int x, int y) {
        if (getDirection() != null) {
            Block block = board.getBlockAt(x + getDirection().toXOffset(), y + getDirection().toYOffset());
            return block != null && block.isFullFace();
        }
        return true;
    }

    public void press(Board board) {
        if (isPowered()) return;
        setPower(Constants.MAX_POWER);
        updateNeighbors(board);
        Event event = new Event(this, cooldown);
        board.tryPushEvent(event);
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

    public int getCooldown() {
        return cooldown;
    }

    private void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }
}
