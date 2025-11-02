package dev.drakou111.blocks;

import dev.drakou111.blocks.templates.Block;
import dev.drakou111.blocks.templates.DetectedByComparator;
import dev.drakou111.blocks.templates.PoweredBlock;
import dev.drakou111.logic.BlockType;
import dev.drakou111.logic.Board;
import dev.drakou111.logic.Constants;
import dev.drakou111.logic.Direction;

import java.util.EnumSet;
import java.util.Map;

public class CopperBulb extends PoweredBlock implements DetectedByComparator {
    private static final EnumSet<BlockType> INSTANT_POWER_SOURCES = EnumSet.of(BlockType.DUST, BlockType.REPEATER, BlockType.REDSTONE_BLOCK, BlockType.COMPARATOR, BlockType.TORCH, BlockType.LEVER, BlockType.STONE_BUTTON, BlockType.WOODEN_BUTTON);
    private static final EnumSet<BlockType> BLOCK_NEIGHBOR_POWER_SOURCES = EnumSet.of(BlockType.DUST, BlockType.REPEATER, BlockType.COMPARATOR, BlockType.LEVER, BlockType.STONE_BUTTON, BlockType.WOODEN_BUTTON);

    private boolean isLit;

    public CopperBulb() {
        super(null, null, false, true, 0, true, BlockType.COPPER_BULB);
        this.isLit = false;
    }

    @Override
    public String getSpriteKey() {
        return "copperbulb_s" + getPower() + "_m" + (isLit ? "1" : "0");
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
                setLit(!isLit());
            }

            for (Block neighbor : getNeighbors().values()) {
                if (neighbor == null) continue;
                neighbor.update(board);
            }
        }
    }

    public boolean isLit() {
        return isLit;
    }

    public void setLit(boolean isLit) {
        this.isLit = isLit;
    }


    @Override
    public Block clone() {
        CopperBulb bulb = new CopperBulb();
        bulb.setPower(getPower());
        bulb.setLit(isLit());
        return bulb;
    }

    @Override
    public int getSignalStrength() {
        return isLit() ? Constants.MAX_POWER : Constants.MIN_POWER;
    }
}
