package dev.drakou111.logic;

public enum Direction {
    LEFT, RIGHT, UP, DOWN;

    public Direction opposite() {
        return switch (this) {
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
            case UP -> DOWN;
            case DOWN -> UP;
        };
    }

    public Direction clockwise() {
        return switch (this) {
            case LEFT -> UP;
            case RIGHT -> DOWN;
            case UP -> RIGHT;
            case DOWN -> LEFT;
        };
    }

    public Direction counterClockwise() {
        return switch (this) {
            case LEFT -> DOWN;
            case RIGHT -> UP;
            case UP -> LEFT;
            case DOWN -> RIGHT;
        };
    }

    public int toXOffset() {
        return switch (this) {
            case LEFT -> -1;
            case RIGHT -> 1;
            default -> 0;
        };
    }

    public int toYOffset() {
        return switch (this) {
            case UP -> -1;
            case DOWN -> 1;
            default -> 0;
        };
    }
}
