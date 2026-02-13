package com.hoddmimes.bridgeanalyzer.model;

public enum Suit {
    SPADES(0, 'S'),
    HEARTS(1, 'H'),
    DIAMONDS(2, 'D'),
    CLUBS(3, 'C');

    private final int index;
    private final char ch;

    Suit(int index, char ch) {
        this.index = index;
        this.ch = ch;
    }

    public int index() { return index; }
    public char ch() { return ch; }

    public static Suit fromChar(char c) {
        return switch (Character.toUpperCase(c)) {
            case 'S' -> SPADES;
            case 'H' -> HEARTS;
            case 'D' -> DIAMONDS;
            case 'C' -> CLUBS;
            default -> throw new IllegalArgumentException("Unknown suit: " + c);
        };
    }

    public static Suit fromIndex(int index) {
        return values()[index];
    }
}
