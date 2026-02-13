package com.hoddmimes.bridgeanalyzer.model;

public enum Direction {
    NORTH(0),
    EAST(1),
    SOUTH(2),
    WEST(3);

    private final int index;

    Direction(int index) {
        this.index = index;
    }

    public int index() { return index; }

    public Direction next() {
        return values()[(index + 1) % 4];
    }

    public Direction partner() {
        return values()[(index + 2) % 4];
    }

    public boolean isNS() {
        return this == NORTH || this == SOUTH;
    }

    public static Direction fromChar(char c) {
        return switch (Character.toUpperCase(c)) {
            case 'N' -> NORTH;
            case 'E' -> EAST;
            case 'S' -> SOUTH;
            case 'W' -> WEST;
            default -> throw new IllegalArgumentException("Unknown direction: " + c);
        };
    }

    public static Direction fromIndex(int index) {
        return values()[index];
    }
}
