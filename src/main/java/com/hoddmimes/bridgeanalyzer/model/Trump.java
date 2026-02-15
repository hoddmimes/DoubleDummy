package com.hoddmimes.bridgeanalyzer.model;

public enum Trump {
    SPADES(Suit.SPADES),
    HEARTS(Suit.HEARTS),
    DIAMONDS(Suit.DIAMONDS),
    CLUBS(Suit.CLUBS),
    NO_TRUMP(null);

    private final Suit suit;
    private final int trumpSuitIndex; // -1 for NT

    Trump(Suit suit) {
        this.suit = suit;
        this.trumpSuitIndex = suit != null ? suit.index() : -1;
    }

    public Suit suit() { return suit; }

    public boolean isTrump(Suit s) {
        return s.index() == trumpSuitIndex;
    }

    public static Trump fromString(String s) {
        return switch (s.toUpperCase()) {
            case "S" -> SPADES;
            case "H" -> HEARTS;
            case "D" -> DIAMONDS;
            case "C" -> CLUBS;
            case "NT" -> NO_TRUMP;
            default -> throw new IllegalArgumentException("Unknown trump: " + s);
        };
    }

    public String label() {
        return this == NO_TRUMP ? "NT" : "" + suit.ch();
    }
}
