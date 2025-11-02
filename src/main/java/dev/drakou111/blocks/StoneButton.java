package dev.drakou111.blocks;

import dev.drakou111.blocks.templates.Block;
import dev.drakou111.blocks.templates.Button;
import dev.drakou111.logic.BlockType;
import dev.drakou111.logic.Constants;
import dev.drakou111.logic.Direction;

public class StoneButton extends Button {
    public StoneButton(Direction attachedDirection) {
        super(attachedDirection, 20, BlockType.STONE_BUTTON);
    }

    @Override
    public String getSpriteKey() {
        return "stonebutton_s" + getPower() + "_d" + (getDirection() == null ? "FLOOR" : getDirection().name());
    }

    @Override
    public Block clone() {
        StoneButton c = new StoneButton(getDirection());
        c.setOutputs(getOutputs());
        c.setPower(Constants.MIN_POWER);
        return c;
    }
}
