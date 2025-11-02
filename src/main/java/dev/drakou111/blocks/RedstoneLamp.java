package dev.drakou111.blocks;

import dev.drakou111.blocks.templates.Block;
import dev.drakou111.blocks.templates.PoweredBlock;
import dev.drakou111.logic.*;

import java.util.EnumSet;
import java.util.Map;

public class RedstoneLamp extends PoweredBlock {
    private static final EnumSet<BlockType> INSTANT_POWER_SOURCES = EnumSet.of(BlockType.DUST, BlockType.REPEATER, BlockType.REDSTONE_BLOCK, BlockType.COMPARATOR, BlockType.TORCH, BlockType.LEVER, BlockType.STONE_BUTTON, BlockType.WOODEN_BUTTON);
    private static final EnumSet<BlockType> BLOCK_NEIGHBOR_POWER_SOURCES = EnumSet.of(BlockType.DUST, BlockType.REPEATER, BlockType.COMPARATOR, BlockType.LEVER, BlockType.STONE_BUTTON, BlockType.WOODEN_BUTTON);

    private boolean isLit;

    public RedstoneLamp() {
        super(null, null, true, true, 0, true, BlockType.REDSTONE_LAMP);
        this.isLit = false;
    }

    @Override
    public String getSpriteKey() {
        return "redstonelamp_m" + (isLit ? "1" : "0");
    }

    @Override
    public void doScheduledTick(Board board) {
        if (getPower() < Constants.MAX_POWER)
            setLit(false);
    }

    @Override
    public void update(Board board) {
        int oldPower = getPower();
        int newPower = 0;

        for (Map.Entry<Direction, Block> entry : getNeighbors().entrySet()) {
            Direction dir = entry.getKey();

            int input = getMaxPowerFromSide(INSTANT_POWER_SOURCES, EnumSet.noneOf(BlockType.class), BLOCK_NEIGHBOR_POWER_SOURCES, dir);
            newPower = Math.max(newPower, input > Constants.MIN_POWER ? Constants.MAX_POWER : Constants.MIN_POWER);
        }

        setPower(newPower);

        if (newPower != oldPower) {
            if (newPower > Constants.MIN_POWER) {
                setLit(true);
            } else {
                Event event = new Event(this, 4);
                board.tryPushEvent(event);
            }
        }
    }

    @Override
    public Block clone() {
        RedstoneLamp bulb = new RedstoneLamp();
        bulb.setPower(getPower());
        bulb.setLit(isLit());
        return bulb;
    }

    public boolean isLit() {
        return isLit;
    }

    public void setLit(boolean isLit) {
        this.isLit = isLit;
    }
}
