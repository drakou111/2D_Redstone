package dev.drakou111.blocks;

import dev.drakou111.blocks.templates.Block;
import dev.drakou111.logic.BlockType;

public class TargetBlock extends Block {

    public TargetBlock() {
        super(null, null, true, true, BlockType.TARGET_BLOCK);
    }

    @Override
    public String getSpriteKey() {
        return "targetblock";
    }

    @Override
    public Block clone() {
        return new TargetBlock();
    }
}
