package dev.drakou111.blocks;

import dev.drakou111.blocks.templates.Block;
import dev.drakou111.blocks.templates.PoweredBlock;
import dev.drakou111.logic.BlockType;
import dev.drakou111.logic.Board;
import dev.drakou111.logic.Constants;
import dev.drakou111.logic.Direction;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

public class Dust extends PoweredBlock {
    private static final EnumSet<BlockType> INSTANT_POWER_SOURCES = EnumSet.of(BlockType.DUST, BlockType.REPEATER, BlockType.REDSTONE_BLOCK, BlockType.COMPARATOR, BlockType.TORCH, BlockType.LEVER, BlockType.STONE_BUTTON, BlockType.WOODEN_BUTTON);
    private static final EnumSet<BlockType> BLOCK_NEIGHBOR_POWER_SOURCES = EnumSet.of(BlockType.REPEATER, BlockType.COMPARATOR, BlockType.LEVER, BlockType.STONE_BUTTON, BlockType.WOODEN_BUTTON);

    private static final EnumSet<BlockType> CONNECT_TO_ANY_SIDE = EnumSet.of(BlockType.DUST, BlockType.TORCH, BlockType.COMPARATOR, BlockType.TARGET_BLOCK, BlockType.REDSTONE_BLOCK, BlockType.LEVER, BlockType.STONE_BUTTON, BlockType.WOODEN_BUTTON);

    private boolean shouldBeDot;

    public Dust(boolean shouldBeDot) {
        super(null, null, false, false, Constants.MIN_POWER, false, BlockType.DUST);
        this.shouldBeDot = shouldBeDot;
        if (!shouldBeDot)
            setOutputs(EnumSet.allOf(Direction.class));
    }

    @Override
    public boolean hook(Board board, int x, int y) {
        super.hook(board, x, y);

        EnumSet<Direction> directions = EnumSet.noneOf(Direction.class);
        Direction aDirection = null;

        for (Map.Entry<Direction, Block> entry : getNeighbors().entrySet()) {
            Block neighbor = entry.getValue();
            Direction direction = entry.getKey();

            if (neighbor == null) continue;

            if (CONNECT_TO_ANY_SIDE.contains(neighbor.getBlockType())) {
                directions.add(direction);
                aDirection = direction;
            }

            // Connect to front and back of repeater
            if (neighbor instanceof Repeater repeater) {
                if (repeater.getDirection() == direction || repeater.getDirection() == direction.opposite()) {
                    directions.add(direction);
                    aDirection = direction;
                }
            }
        }

        // If dust connected on only one side, opposite side must be connected too.
        if (directions.size() == 1 && aDirection != null)
            directions.add(aDirection.opposite());

        if (!shouldBeDot && directions.isEmpty()) {
            directions = EnumSet.allOf(Direction.class);
        }

        setOutputs(directions);
        return true;
    }

    @Override
    public void update(Board board) {
        int oldPower = getPower();

        setPower(Constants.MIN_POWER);
        for (Map.Entry<Direction, Block> entry : getNeighbors().entrySet()) {
            Direction dir = entry.getKey();

            int input = getMaxPowerFromSide(INSTANT_POWER_SOURCES, EnumSet.noneOf(BlockType.class), BLOCK_NEIGHBOR_POWER_SOURCES, dir);

            if (getNeighbors().get(dir) instanceof Dust)
                input = Math.max(Constants.MIN_POWER, input - 1);

            addPower(input);
        }

        int newPower = getPower();

        if (newPower != oldPower) {
            for (Block neighbor : getNeighbors().values()) {
                if (neighbor == null) continue;
                neighbor.update(board);
            }
            for (Direction direction : getOutputs()) {
                Block neighbor = getNeighbors().get(direction);
                Direction opposite = direction.opposite();
                if (neighbor != null) {
                    for (EnumMap.Entry<Direction, Block> blockNeighbor : neighbor.getNeighbors().entrySet()) {
                        Block neighborBlock = blockNeighbor.getValue();
                        Direction neighborDirection = blockNeighbor.getKey();
                        if (neighborDirection != opposite && neighborBlock != null) {
                            neighborBlock.update(board);
                        }
                    }
                }
            }
        }
    }

    public String getSpriteKey() {
        String connectionsBinary;
        if (getOutputs() == null || getOutputs().isEmpty())
            connectionsBinary = "0000";
        else connectionsBinary =
                (getOutputs().contains(Direction.DOWN) ? "1" : "0") +
                (getOutputs().contains(Direction.RIGHT) ? "1" : "0") +
                (getOutputs().contains(Direction.LEFT) ? "1" : "0") +
                (getOutputs().contains(Direction.UP) ? "1" : "0");

        return "dust_s" + getPower() + "_c" + connectionsBinary;
    }

    public boolean getShouldBeDot() {
        return shouldBeDot;
    }

    private void setShouldBeDot(boolean shouldBeDot) {
        this.shouldBeDot = shouldBeDot;
    }

    @Override
    public Block clone() {
        Dust c = new Dust(shouldBeDot);
        c.setPower(getPower());
        c.setOutputs(getOutputs());
        return c;
    }
}
