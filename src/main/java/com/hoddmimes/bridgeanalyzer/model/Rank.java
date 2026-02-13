package com.hoddmimes.bridgeanalyzer.model;

public enum Rank {
    TWO(0, '2'),
    THREE(1, '3'),
    FOUR(2, '4'),
    FIVE(3, '5'),
    SIX(4, '6'),
    SEVEN(5, '7'),
    EIGHT(6, '8'),
    NINE(7, '9'),
    TEN(8, 'T'),
    JACK(9, 'J'),
    QUEEN(10, 'Q'),
    KING(11, 'K'),
    ACE(12, 'A');

    private final int value;
    private final char ch;

    Rank(int value, char ch) {
        this.value = value;
        this.ch = ch;
    }

    public int value() { return value; }
    public char ch() { return ch; }

    public static Rank fromChar(char c) {
        return switch (Character.toUpperCase(c)) {
            case '2' -> TWO;
            case '3' -> THREE;
            case '4' -> FOUR;
            case '5' -> FIVE;
            case '6' -> SIX;
            case '7' -> SEVEN;
            case '8' -> EIGHT;
            case '9' -> NINE;
            case 'T' -> TEN;
            case 'J' -> JACK;
            case 'Q' -> QUEEN;
            case 'K' -> KING;
            case 'A' -> ACE;
            default -> throw new IllegalArgumentException("Unknown rank: " + c);
        };
    }
}
