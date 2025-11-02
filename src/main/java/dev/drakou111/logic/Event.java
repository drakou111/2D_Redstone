package dev.drakou111.logic;

import dev.drakou111.blocks.templates.PoweredBlock;

public class Event {
    private final PoweredBlock blockToUpdate;
    private int delay;
    private int priority;

    public Event(PoweredBlock blockToUpdate, int delay) {
        this(blockToUpdate, delay, 0);
    }

    public Event(PoweredBlock blockToUpdate, int delay, int priority) {
        this.blockToUpdate = blockToUpdate;
        this.delay = delay;
        this.priority = priority;
    }

    public void applyEvent(Board board) {
        blockToUpdate.setSchedule(false);
        blockToUpdate.doScheduledTick(board);
    }

    public int getDelay() {
        return delay;
    }

    public void decrementDelay() {
        if (delay <= 0) throw new IllegalArgumentException("Cannot decrement delay <= 0");
        delay--;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public PoweredBlock getBlockToUpdate() {
        return blockToUpdate;
    }
}
