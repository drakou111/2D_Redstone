package dev.drakou111.blocks;

import dev.drakou111.blocks.templates.Block;
import dev.drakou111.blocks.templates.DetectedByComparator;
import dev.drakou111.blocks.templates.PoweredBlock;
import dev.drakou111.logic.BlockType;

public class Barrel extends PoweredBlock implements DetectedByComparator {
    public Barrel(int power) {
        super(null, null, true, true, power, false, BlockType.BARREL);
    }

    @Override
    public String getSpriteKey() {
        return "barrel_s" + getPower();
    }

    @Override
    public Block clone() {
        return new Barrel(getPower());
    }

    @Override
    public int getSignalStrength() {
        return getPower();
    }
}
