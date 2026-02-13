package com.hoddmimes.bridgeanalyzer.model;

public enum Trump {
    SPADES(Suit.SPADES),
    HEARTS(Suit.HEARTS),
    DIAMONDS(Suit.DIAMONDS),
    CLUBS(Suit.CLUBS),
    NO_TRUMP(null);

    private final Suit suit;

    Trump(Suit suit) {
        this.suit = suit;
    }

    public Suit suit() { return suit; }

    public boolean isTrump(Suit s) {
        return suit != null && suit == s;
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
