package com.hoddmimes.bridgeanalyzer;

import com.hoddmimes.bridgeanalyzer.game.Hand;
import com.hoddmimes.bridgeanalyzer.model.Card;
import com.hoddmimes.bridgeanalyzer.model.Rank;
import com.hoddmimes.bridgeanalyzer.model.Suit;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HandTest {

    @Test
    void addAndContains() {
        Hand hand = new Hand();
        Card sa = Card.of(Suit.SPADES, Rank.ACE);
        hand.add(sa);
        assertTrue(hand.contains(sa));
        assertEquals(1, hand.cardCount());
    }

    @Test
    void removeCard() {
        Hand hand = new Hand();
        Card sa = Card.of(Suit.SPADES, Rank.ACE);
        hand.add(sa);
        hand.remove(sa);
        assertFalse(hand.contains(sa));
        assertEquals(0, hand.cardCount());
    }

    @Test
    void legalPlaysFollowSuit() {
        Hand hand = new Hand();
        hand.add(Card.of(Suit.SPADES, Rank.ACE));
        hand.add(Card.of(Suit.SPADES, Rank.KING));
        hand.add(Card.of(Suit.HEARTS, Rank.TWO));

        List<Card> plays = hand.legalPlays(Suit.SPADES);
        assertEquals(2, plays.size());
        assertTrue(plays.stream().allMatch(c -> c.suit() == Suit.SPADES));
    }

    @Test
    void legalPlaysAnyWhenVoid() {
        Hand hand = new Hand();
        hand.add(Card.of(Suit.HEARTS, Rank.ACE));
        hand.add(Card.of(Suit.CLUBS, Rank.TWO));

        List<Card> plays = hand.legalPlays(Suit.SPADES);
        assertEquals(2, plays.size());
    }

    @Test
    void displayString() {
        Hand hand = new Hand();
        hand.add(Card.of(Suit.SPADES, Rank.ACE));
        hand.add(Card.of(Suit.SPADES, Rank.KING));
        hand.add(Card.of(Suit.HEARTS, Rank.TWO));
        String display = hand.toDisplayString();
        assertTrue(display.contains("AK"));
    }
}
