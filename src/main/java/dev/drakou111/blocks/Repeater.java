package dev.drakou111.blocks;

import dev.drakou111.blocks.templates.Block;
import dev.drakou111.blocks.templates.PoweredBlock;
import dev.drakou111.logic.*;

import java.util.EnumSet;
import java.util.Map;

public class Repeater extends PoweredBlock {
    private static final EnumSet<BlockType> INSTANT_POWER_SOURCES = EnumSet.of(BlockType.DUST, BlockType.REPEATER, BlockType.REDSTONE_BLOCK, BlockType.COMPARATOR, BlockType.TORCH, BlockType.LEVER, BlockType.STONE_BUTTON, BlockType.WOODEN_BUTTON);
    private static final EnumSet<BlockType> BLOCK_NEIGHBOR_POWER_SOURCES = EnumSet.of(BlockType.DUST, BlockType.REPEATER, BlockType.COMPARATOR, BlockType.LEVER, BlockType.STONE_BUTTON, BlockType.WOODEN_BUTTON);

    private static final EnumSet<BlockType> SIDE_INSTANT_POWER_SOURCES = EnumSet.of(BlockType.REPEATER, BlockType.COMPARATOR);

    private int delay;
    private boolean locked;

    public Repeater(Direction direction, int delay) {
        super(EnumSet.of(direction), direction, false, false, Constants.MIN_POWER, true, BlockType.REPEATER);
        if (delay < Constants.REPEATER_MIN_DELAY || delay > Constants.REPEATER_MAX_DELAY)
            throw new IllegalArgumentException("Repeater delay can only be between 1 and 4!");
        this.delay = delay;
        this.locked = false;
    }

    @Override
    public void doScheduledTick(Board board) {
        int oldPower = getPower();
        int newPower = 0;

        if (!this.locked) {
            int input = getMaxPowerFromSide(INSTANT_POWER_SOURCES, EnumSet.noneOf(BlockType.class), BLOCK_NEIGHBOR_POWER_SOURCES, getDirection().opposite());
            newPower = input > Constants.MIN_POWER ? Constants.MAX_POWER : Constants.MIN_POWER;
        }

        if (oldPower > Constants.MIN_POWER && newPower == Constants.MIN_POWER) {
            setPower(Constants.MIN_POWER);
        } else if (oldPower == Constants.MIN_POWER) {
            setPower(Constants.MAX_POWER);

            // Send extra event
            if (newPower == Constants.MIN_POWER) {
                Event event = new Event(this, 2 * delay);
                Block frontNeighbor = getNeighbors().get(getDirection());
                if (frontNeighbor != null) {
                    if (frontNeighbor instanceof Repeater || frontNeighbor instanceof Comparator) event.setPriority(-3);
                    else if (getPower() == 0) event.setPriority(-2);
                }

                board.tryPushEvent(event);
            }
        }

        Block frontNeighbor = getNeighbors().get(getDirection());
        if (frontNeighbor != null) {
            board.pushUpdate(frontNeighbor);
            Direction opposite = getDirection().opposite();
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
        int newPower;

        int leftPower = getMaxPowerFromSide(SIDE_INSTANT_POWER_SOURCES, EnumSet.noneOf(BlockType.class), EnumSet.noneOf(BlockType.class), getDirection().clockwise());
        int rightPower = getMaxPowerFromSide(SIDE_INSTANT_POWER_SOURCES, EnumSet.noneOf(BlockType.class), EnumSet.noneOf(BlockType.class), getDirection().counterClockwise());
        int sidePower = Math.max(leftPower, rightPower);
        setLocked(sidePower > Constants.MIN_POWER);

        if (!this.locked) {
            int input = getMaxPowerFromSide(INSTANT_POWER_SOURCES, EnumSet.noneOf(BlockType.class), BLOCK_NEIGHBOR_POWER_SOURCES, getDirection().opposite());
            newPower = input > Constants.MIN_POWER ? Constants.MAX_POWER : Constants.MIN_POWER;

            boolean isPowering = oldPower == Constants.MIN_POWER && newPower > Constants.MIN_POWER;
            boolean isDepowering = oldPower > Constants.MIN_POWER && newPower == Constants.MIN_POWER;

            if (newPower != oldPower) {
                Event event = new Event(this, 2 * delay);

                Block frontNeighbor = getNeighbors().get(getDirection());
                if (frontNeighbor != null) {
                    if (frontNeighbor instanceof Repeater || frontNeighbor instanceof Comparator) event.setPriority(-3);
                    else if (isPowering) event.setPriority(-1);
                    else if (isDepowering) event.setPriority(-2);
                    else if (getPower() == 0) event.setPriority(-2);
                }

                board.tryPushEvent(event);
            }
        }
    }

    public boolean isLocked() {
        return locked;
    }

    private void setLocked(boolean locked) {
        this.locked = locked;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    @Override
    public String getSpriteKey() {
        return "repeater_s" + getPower() + "_dir" + getDirection().name() + "_del" + delay + "_m" + (isLocked() ? "1" : "0");
    }

    @Override
    public Block clone() {
        Repeater c = new Repeater(getDirection(), delay);
        c.setPower(getPower());
        c.locked = isLocked();
        return c;
    }
}
