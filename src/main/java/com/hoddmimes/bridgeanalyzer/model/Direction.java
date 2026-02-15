package com.hoddmimes.bridgeanalyzer.model;

public enum Direction {
    NORTH(0),
    EAST(1),
    SOUTH(2),
    WEST(3);

    private static final Direction[] VALS = values();
    private static final Direction[] NEXT = {EAST, SOUTH, WEST, NORTH};
    private static final Direction[] PARTNER = {SOUTH, WEST, NORTH, EAST};
    private static final boolean[] NS = {true, false, true, false};

    private final int index;

    Direction(int index) {
        this.index = index;
    }

    public int index() { return index; }

    public Direction next() {
        return NEXT[index];
    }

    public Direction partner() {
        return PARTNER[index];
    }

    public boolean isNS() {
        return NS[index];
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
        return VALS[index];
    }
}
