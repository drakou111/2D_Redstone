package dev.drakou111.blocks;

import dev.drakou111.blocks.templates.Block;
import dev.drakou111.blocks.templates.DetectedByComparator;
import dev.drakou111.blocks.templates.PoweredBlock;
import dev.drakou111.logic.BlockType;

public class Chest extends PoweredBlock implements DetectedByComparator {
    public Chest(int power) {
        super(null, null, false, false, power, false, BlockType.CHEST);
    }

    @Override
    public String getSpriteKey() {
        return "chest_s" + getPower();
    }

    @Override
    public Block clone() {
        return new Chest(getPower());
    }

    @Override
    public int getSignalStrength() {
        return getPower();
    }
}
