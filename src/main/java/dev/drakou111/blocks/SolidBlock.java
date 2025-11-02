package dev.drakou111.blocks;

import dev.drakou111.blocks.templates.Block;
import dev.drakou111.logic.BlockType;

public class SolidBlock extends Block {
    public SolidBlock() {
        super(null, null, true, true, BlockType.SOLID_BLOCK);
    }

    @Override
    public String getSpriteKey() {
        return "solidblock";
    }

    @Override
    public Block clone() {
        return new SolidBlock();
    }
}
