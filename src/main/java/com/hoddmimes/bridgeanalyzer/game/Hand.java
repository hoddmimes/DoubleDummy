package com.hoddmimes.bridgeanalyzer.game;

import com.hoddmimes.bridgeanalyzer.model.Card;
import com.hoddmimes.bridgeanalyzer.model.Rank;
import com.hoddmimes.bridgeanalyzer.model.Suit;

import java.util.ArrayList;
import java.util.List;

public class Hand {
    private static final long SUIT_MASK = 0x1FFFL; // 13 bits

    private long bits;

    public Hand(long bits) {
        this.bits = bits;
    }

    public Hand() {
        this(0L);
    }

    public long bits() { return bits; }

    public void add(Card card) {
        bits |= card.bitMask();
    }

    public void remove(Card card) {
        bits &= ~card.bitMask();
    }

    public boolean contains(Card card) {
        return (bits & card.bitMask()) != 0;
    }

    public boolean hasSuit(Suit suit) {
        return suitBits(suit) != 0;
    }

    public long suitBits(Suit suit) {
        return (bits >> (suit.index() * 13)) & SUIT_MASK;
    }

    public int cardCount() {
        return Long.bitCount(bits);
    }

    public int suitCount(Suit suit) {
        return Long.bitCount(suitBits(suit));
    }

    public List<Card> cardsInSuit(Suit suit) {
        List<Card> cards = new ArrayList<>();
        long sb = suitBits(suit);
        int base = suit.index() * 13;
        while (sb != 0) {
            int bit = Long.numberOfTrailingZeros(sb);
            cards.add(Card.fromBitIndex(base + bit));
            sb &= sb - 1;
        }
        return cards;
    }

    public List<Card> allCards() {
        List<Card> cards = new ArrayList<>();
        long b = bits;
        while (b != 0) {
            int bit = Long.numberOfTrailingZeros(b);
            cards.add(Card.fromBitIndex(bit));
            b &= b - 1;
        }
        return cards;
    }

    public List<Card> legalPlays(Suit ledSuit) {
        if (ledSuit != null && hasSuit(ledSuit)) {
            return cardsInSuit(ledSuit);
        }
        return allCards();
    }

    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        for (Suit suit : Suit.values()) {
            if (sb.length() > 0) sb.append('-');
            long s = suitBits(suit);
            if (s == 0) {
                sb.append('-');
            } else {
                int base = suit.index() * 13;
                // Print from high rank to low
                for (int r = 12; r >= 0; r--) {
                    if ((s & (1L << r)) != 0) {
                        sb.append(Rank.values()[r].ch());
                    }
                }
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toDisplayString();
    }
}
