package dev.drakou111.blocks;

import dev.drakou111.blocks.templates.Block;
import dev.drakou111.blocks.templates.Button;
import dev.drakou111.logic.BlockType;
import dev.drakou111.logic.Constants;
import dev.drakou111.logic.Direction;

public class WoodenButton extends Button {
    public WoodenButton(Direction attachedDirection) {
        super(attachedDirection, 30, BlockType.WOODEN_BUTTON);
    }

    @Override
    public String getSpriteKey() {
        return "woodenbutton_s" + getPower() + "_d" + (getDirection() == null ? "FLOOR" : getDirection().name());
    }

    @Override
    public Block clone() {
        WoodenButton c = new WoodenButton(getDirection());
        c.setOutputs(getOutputs());
        c.setPower(Constants.MIN_POWER);
        return c;
    }
}
