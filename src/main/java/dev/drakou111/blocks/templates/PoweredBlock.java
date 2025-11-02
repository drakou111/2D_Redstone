package dev.drakou111.blocks.templates;

import dev.drakou111.blocks.Comparator;
import dev.drakou111.blocks.Lever;
import dev.drakou111.logic.BlockType;
import dev.drakou111.logic.Board;
import dev.drakou111.logic.Constants;
import dev.drakou111.logic.Direction;

import java.util.EnumSet;
import java.util.Map;

public abstract class PoweredBlock extends Block {
    private boolean isBoolPower;
    private int power;

    public PoweredBlock(EnumSet<Direction> directions, Direction direction, boolean isSolid, boolean isFullFace, int power, boolean isBoolPower, BlockType type) {
        super(directions, direction, isSolid, isFullFace, type);
        this.isBoolPower = isBoolPower;
        setPower(power);
    }

    public void doScheduledTick(Board board) {}

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        if (power < Constants.MIN_POWER)
            throw new IllegalArgumentException("Power must be greater than or equal to " + Constants.MIN_POWER);
        if (power > Constants.MAX_POWER)
            throw new IllegalArgumentException("Power must be less than or equal to " + Constants.MAX_POWER);
        if (isBoolPower)
            this.power = power == Constants.MIN_POWER ? Constants.MIN_POWER : Constants.MAX_POWER;
        else
            this.power = power;
    }

    public boolean isPowered() {
        return power > Constants.MIN_POWER;
    }

    public void addPower(int power) {
        setPower(Math.max(getPower(), power));
    }

    private int updatePower(int power, PoweredBlock source, Direction direction) {
        if (source.isPowered() && (source.getOutputs() == null || source.getOutputs().isEmpty() || source.hasDirection(direction.opposite()))) {
            return Math.max(source.getPower(), power);
        }
        return power;
    }

    private boolean isComparing(Block neighbor, EnumSet<BlockType> instantPowerSources) {
        if (neighbor instanceof PoweredBlock source)
            if (instantPowerSources.contains(source.getBlockType()))
                return this instanceof Comparator && source instanceof DetectedByComparator;
        return false;
    }

    public int getMaxPowerFromSide(
            EnumSet<BlockType> instantPowerSources,
            EnumSet<BlockType> blockDirectPowerSources,
            EnumSet<BlockType> blockNeighborPowerSources,
            Direction direction) {
        int out = Constants.MIN_POWER;

        Block neighbor = getNeighbors().get(direction);

        if (neighbor == null) return out;

        // Direct Power
        out = handleDirectPower(out, neighbor, direction, instantPowerSources);

        // If we are comparing something, instantly exit (we don't care about block neighbors)
        // TODO: somehow avoid rechecking?
        if (isComparing(neighbor, instantPowerSources)) return out;

        if (neighbor.isSolid()) {
            // Block Neighbors Power
            out = handleNeighborPower(out, neighbor, direction, blockNeighborPowerSources);

            // Direct Block Neighbor Power
            out = handleDirectBlockPower(out, neighbor, direction, blockDirectPowerSources);
        }

        return out;
    }

    private int handleDirectPower(int out, Block neighbor, Direction direction, EnumSet<BlockType> instantPowerSources) {
        if (instantPowerSources.isEmpty()) return out;

        if (neighbor instanceof PoweredBlock source) {

            if (instantPowerSources.contains(source.getBlockType())) {
                if (this instanceof Comparator && source instanceof DetectedByComparator ss) {
                    out = updatePower(ss.getSignalStrength(), source, direction);
                } else {
                    out = updatePower(out, source, direction);
                }
            }
        }
        return out;
    }

    private int handleNeighborPower(int out, Block neighbor, Direction direction, EnumSet<BlockType> blockNeighborPowerSources) {
        if (blockNeighborPowerSources.isEmpty()) return out;

        Direction opposite = direction.opposite();

        for (Map.Entry<Direction, Block> solidBlockNeighbor : neighbor.getNeighbors().entrySet()) {
            Block blockNeighbor = solidBlockNeighbor.getValue();
            Direction blockDirection = solidBlockNeighbor.getKey();

            if (blockNeighbor == null) continue;
            if (blockDirection == opposite) continue;

            if (blockNeighbor instanceof PoweredBlock source) {
                if (blockNeighborPowerSources.contains(source.getBlockType())) {
                    // Power through with levers/buttons only if they are attached to the wall
                    if (source instanceof Lever || source instanceof Button) {
                        if (source.getDirection() != null)
                            out = updatePower(out, source, blockDirection);
                    } else
                        out = updatePower(out, source, blockDirection);
                }
            }
        }
        return out;
    }

    private int handleDirectBlockPower(int out, Block neighbor, Direction direction, EnumSet<BlockType> blockDirectPowerSources) {
        if (blockDirectPowerSources.isEmpty()) return out;

        boolean isReadingContainer = false;
        int containerValue = 0;
        Block oppositeNeighbor = neighbor.getNeighbors().get(direction);
        if (oppositeNeighbor instanceof PoweredBlock source) {
            if (blockDirectPowerSources.contains(source.getBlockType())) {
                if (this instanceof Comparator && source instanceof DetectedByComparator ss) {
                    isReadingContainer = true;
                    containerValue = ss.getSignalStrength();
                } else {
                    out = updatePower(out, source, direction);
                }
            }
        }
        if (isReadingContainer)
            if (out < Constants.MAX_POWER)
                return containerValue;
        return out;
    }

    private void setBoolPower(boolean boolPower) {
        this.isBoolPower = boolPower;
    }
}
