package dev.drakou111.blocks;

import dev.drakou111.blocks.templates.Block;
import dev.drakou111.blocks.templates.PoweredBlock;
import dev.drakou111.logic.*;

import java.util.EnumSet;
import java.util.Map;

public class Comparator extends PoweredBlock {
    private static final EnumSet<BlockType> INSTANT_POWER_SOURCES = EnumSet.of(BlockType.DUST, BlockType.REPEATER, BlockType.REDSTONE_BLOCK, BlockType.COMPARATOR, BlockType.TORCH, BlockType.CHEST, BlockType.BARREL, BlockType.COPPER_BULB, BlockType.LEVER, BlockType.STONE_BUTTON, BlockType.WOODEN_BUTTON);
    private static final EnumSet<BlockType> BLOCK_DIRECT_POWER_SOURCES = EnumSet.of(BlockType.CHEST, BlockType.BARREL, BlockType.COPPER_BULB);
    private static final EnumSet<BlockType> BLOCK_NEIGHBOR_POWER_SOURCES = EnumSet.of(BlockType.DUST, BlockType.REPEATER, BlockType.COMPARATOR, BlockType.LEVER, BlockType.STONE_BUTTON, BlockType.WOODEN_BUTTON);

    private static final EnumSet<BlockType> SIDE_INSTANT_POWER_SOURCES = EnumSet.of(BlockType.DUST, BlockType.REPEATER, BlockType.REDSTONE_BLOCK, BlockType.COMPARATOR, BlockType.LEVER, BlockType.STONE_BUTTON, BlockType.WOODEN_BUTTON);

    private boolean isSubtractMode;

    public Comparator(Direction direction, boolean isSubtractMode) {
        super(EnumSet.of(direction), direction, false, false, Constants.MIN_POWER, false, BlockType.COMPARATOR);
        this.isSubtractMode = isSubtractMode;
    }

    @Override
    public void doScheduledTick(Board board) {
        int newPower;

        newPower = getNewPower();
        setPower(newPower);

        Direction direction = getDirection();
        Direction opposite = direction.opposite();

        Block frontNeighbor = getNeighbors().get(direction);
        if (frontNeighbor != null) {
            board.pushUpdate(frontNeighbor);
            for (Map.Entry<Direction, Block> blockNeighbor : frontNeighbor.getNeighbors().entrySet()) {
                Block neighborBlock = blockNeighbor.getValue();
                Direction neighborDirection = blockNeighbor.getKey();
                if (neighborDirection != opposite && neighborBlock != null) {
                    board.pushUpdate(neighborBlock);
                }
            }
        }
    }

    @Override
    public void update(Board board) {
        int oldPower = getPower();
        int newPower = getNewPower();

        if (newPower != oldPower) {
            Event event = new Event(this, 2);

            Block frontNeighbor = getNeighbors().get(getDirection());
            if (frontNeighbor != null) {
                if (frontNeighbor instanceof Repeater || frontNeighbor instanceof Comparator) event.setPriority(-1);
                else event.setPriority(0);
            }

            board.tryPushEvent(event);
        }
    }

    private int getNewPower() {
        int newPower;
        int input = getMaxPowerFromSide(INSTANT_POWER_SOURCES, BLOCK_DIRECT_POWER_SOURCES, BLOCK_NEIGHBOR_POWER_SOURCES, getDirection().opposite());

        // No matter the side input of the comparator, if at 0, stay 0.
        if (input == Constants.MIN_POWER) return Constants.MIN_POWER;

        int leftPower = getMaxPowerFromSide(SIDE_INSTANT_POWER_SOURCES, EnumSet.noneOf(BlockType.class), EnumSet.noneOf(BlockType.class), getDirection().clockwise());
        int rightPower = getMaxPowerFromSide(SIDE_INSTANT_POWER_SOURCES, EnumSet.noneOf(BlockType.class), EnumSet.noneOf(BlockType.class), getDirection().counterClockwise());
        int sidePower = Math.max(leftPower, rightPower);

        if (isSubtractMode) {
            newPower = Math.max(input - sidePower, 0);
        } else {
            newPower = input >= sidePower ? input : 0;
        }
        return newPower;
    }

    public boolean isSubtractMode() {
        return this.isSubtractMode;
    }

    public void setSubtractMode(boolean subtractMode) {
        this.isSubtractMode = subtractMode;
    }

    @Override
    public String getSpriteKey() {
        return "comparator_s" + (isPowered() ? 1 : 0) + "_d" + getDirection().name() + "_m" + (isSubtractMode ? "1" : "0");
    }

    @Override
    public Block clone() {
        Comparator c = new Comparator(getDirection(), isSubtractMode);
        c.setPower(getPower());
        return c;
    }
}
