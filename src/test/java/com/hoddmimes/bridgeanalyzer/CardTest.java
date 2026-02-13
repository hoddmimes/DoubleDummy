package com.hoddmimes.bridgeanalyzer;

import com.hoddmimes.bridgeanalyzer.model.Card;
import com.hoddmimes.bridgeanalyzer.model.Rank;
import com.hoddmimes.bridgeanalyzer.model.Suit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {

    @Test
    void bitIndexIsUnique() {
        Card[] all = Card.allCards();
        long combined = 0;
        for (Card c : all) {
            assertEquals(0, combined & c.bitMask(), "Duplicate bit for " + c);
            combined |= c.bitMask();
        }
        assertEquals(52, Long.bitCount(combined));
    }

    @Test
    void ofAndFromBitIndex() {
        Card aceOfSpades = Card.of(Suit.SPADES, Rank.ACE);
        assertEquals(Suit.SPADES, aceOfSpades.suit());
        assertEquals(Rank.ACE, aceOfSpades.rank());
        assertEquals(aceOfSpades, Card.fromBitIndex(aceOfSpades.bitIndex()));
    }

    @Test
    void bitIndexLayout() {
        // Spades index=0, so SA should be at bit 12
        Card sa = Card.of(Suit.SPADES, Rank.ACE);
        assertEquals(12, sa.bitIndex());

        // Hearts index=1, HA at bit 13+12=25
        Card ha = Card.of(Suit.HEARTS, Rank.ACE);
        assertEquals(25, ha.bitIndex());

        // Clubs index=3, C2 at bit 39
        Card c2 = Card.of(Suit.CLUBS, Rank.TWO);
        assertEquals(39, c2.bitIndex());
    }
}
