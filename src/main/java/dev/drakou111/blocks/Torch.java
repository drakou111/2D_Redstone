package dev.drakou111.blocks;

import dev.drakou111.blocks.templates.Block;
import dev.drakou111.blocks.templates.PoweredBlock;
import dev.drakou111.logic.*;

import java.util.EnumSet;
import java.util.Map;

public class Torch extends PoweredBlock {
    private static final EnumSet<BlockType> BLOCK_NEIGHBOR_POWER_SOURCES = EnumSet.of(BlockType.DUST, BlockType.REPEATER, BlockType.COMPARATOR, BlockType.LEVER, BlockType.STONE_BUTTON, BlockType.WOODEN_BUTTON);

    public Torch(Direction direction) {
        super(EnumSet.of(direction, direction.clockwise(), direction.counterClockwise()), direction, false, false, Constants.MAX_POWER, true, BlockType.TORCH);
    }

    @Override
    public void doScheduledTick(Board board) {
        int newPower;

        int input = getMaxPowerFromSide(EnumSet.noneOf(BlockType.class), EnumSet.noneOf(BlockType.class), BLOCK_NEIGHBOR_POWER_SOURCES, getDirection().opposite());
        newPower = input > Constants.MIN_POWER ? Constants.MIN_POWER : Constants.MAX_POWER;

        // Handle the case where the torch is attached on a redstone block
        if (getNeighbors().get(getDirection().opposite()) instanceof RedstoneBlock) newPower = 0;

        setPower(newPower);

        for (Block neighbor : getNeighbors().values()) {
            if (neighbor == null) continue;
            board.pushUpdate(neighbor);
        }
        for (Direction direction : getOutputs()) {
            Block neighbor = getNeighbors().get(direction);
            Direction opposite = direction.opposite();
            if (neighbor != null) {
                for (Map.Entry<Direction, Block> blockNeighbor : neighbor.getNeighbors().entrySet()) {
                    Block neighborBlock = blockNeighbor.getValue();
                    Direction neighborDirection = blockNeighbor.getKey();
                    if (neighborDirection != opposite && neighborBlock != null) {
                        board.pushUpdate(neighborBlock);
                    }
                }
            }
        }
    }

    @Override
    public boolean hook(Board board, int x, int y) {
        super.hook(board, x, y);
        return getNeighbors().get(getDirection().opposite()) != null && getNeighbors().get(getDirection().opposite()).isFullFace();
    }

    @Override
    public void update(Board board) {
        // TODO: torch burnout
        int oldPower = getPower();
        int newPower = 0;

        int input = getMaxPowerFromSide(EnumSet.noneOf(BlockType.class), EnumSet.noneOf(BlockType.class), BLOCK_NEIGHBOR_POWER_SOURCES, getDirection().opposite());
        newPower = input > Constants.MIN_POWER ? Constants.MIN_POWER : Constants.MAX_POWER;

        // Handle the case where the torch is attached on a redstone block
        if (getNeighbors().get(getDirection().opposite()) instanceof RedstoneBlock) newPower = 0;

        if (newPower != oldPower) {
            Event event = new Event(this, 2);
            board.tryPushEvent(event);
        }
    }

    @Override
    public String getSpriteKey() {
        return "torch_s" + getPower() + "_d" + (getDirection().name());
    }

    @Override
    public Block clone() {
        Torch c = new Torch(getDirection());
        c.setPower(getPower());
        return c;
    }

    @Override
    public boolean canPlaceAt(Board board, int x, int y) {
        Direction dir = getDirection().opposite();
        Block block = board.getBlockAt(x + dir.toXOffset(), y + dir.toYOffset());
        return block != null && block.isFullFace();
    }
}
