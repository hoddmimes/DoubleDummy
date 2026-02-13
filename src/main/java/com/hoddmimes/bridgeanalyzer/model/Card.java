package com.hoddmimes.bridgeanalyzer.model;

public final class Card {
    private static final Card[] ALL = new Card[52];

    static {
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                int idx = suit.index() * 13 + rank.value();
                ALL[idx] = new Card(suit, rank);
            }
        }
    }

    private final Suit suit;
    private final Rank rank;
    private final int bitIndex;
    private final long bitMask;

    private Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
        this.bitIndex = suit.index() * 13 + rank.value();
        this.bitMask = 1L << bitIndex;
    }

    public Suit suit() { return suit; }
    public Rank rank() { return rank; }
    public int bitIndex() { return bitIndex; }
    public long bitMask() { return bitMask; }

    public static Card of(Suit suit, Rank rank) {
        return ALL[suit.index() * 13 + rank.value()];
    }

    public static Card fromBitIndex(int bitIndex) {
        return ALL[bitIndex];
    }

    public static Card[] allCards() {
        return ALL.clone();
    }

    @Override
    public String toString() {
        return "" + suit.ch() + rank.ch();
    }
}
