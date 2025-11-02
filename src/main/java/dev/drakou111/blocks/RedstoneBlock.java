package dev.drakou111.blocks;

import dev.drakou111.blocks.templates.Block;
import dev.drakou111.blocks.templates.PoweredBlock;
import dev.drakou111.logic.BlockType;
import dev.drakou111.logic.Constants;

public class RedstoneBlock extends PoweredBlock {
    public RedstoneBlock() {
        super(null, null, false, true, Constants.MAX_POWER, true, BlockType.REDSTONE_BLOCK);
    }

    @Override
    public String getSpriteKey() {
        return "redstoneblock";
    }

    @Override
    public Block clone() {
        return new RedstoneBlock();
    }

    @Override
    public void setPower(int power) {
        super.setPower(Constants.MAX_POWER);
    }
}
