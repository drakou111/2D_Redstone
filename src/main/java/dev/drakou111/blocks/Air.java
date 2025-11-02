package dev.drakou111.blocks;

import dev.drakou111.blocks.templates.Block;
import dev.drakou111.blocks.templates.DetectedByComparator;
import dev.drakou111.blocks.templates.PoweredBlock;
import dev.drakou111.logic.BlockType;

public class Air extends Block {
    public Air() {
        super(null, null, false, false, BlockType.AIR);
    }

    @Override
    public String getSpriteKey() {
        return null;
    }

    @Override
    public Block clone() {
        return new Air();
    }
}
