package com.hoddmimes.bridgeanalyzer;

import com.hoddmimes.bridgeanalyzer.game.Trick;
import com.hoddmimes.bridgeanalyzer.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrickTest {

    @Test
    void highestOfLedSuitWins() {
        Trick trick = new Trick();
        trick.play(Card.of(Suit.SPADES, Rank.KING), Direction.NORTH);
        trick.play(Card.of(Suit.SPADES, Rank.TWO), Direction.EAST);
        trick.play(Card.of(Suit.SPADES, Rank.ACE), Direction.SOUTH);
        trick.play(Card.of(Suit.SPADES, Rank.THREE), Direction.WEST);

        assertEquals(Direction.SOUTH, trick.winner(Trump.NO_TRUMP));
    }

    @Test
    void trumpBeatsHighCard() {
        Trick trick = new Trick();
        trick.play(Card.of(Suit.SPADES, Rank.ACE), Direction.NORTH);
        trick.play(Card.of(Suit.HEARTS, Rank.TWO), Direction.EAST); // trump
        trick.play(Card.of(Suit.SPADES, Rank.KING), Direction.SOUTH);
        trick.play(Card.of(Suit.SPADES, Rank.QUEEN), Direction.WEST);

        assertEquals(Direction.EAST, trick.winner(Trump.HEARTS));
    }

    @Test
    void highestTrumpWins() {
        Trick trick = new Trick();
        trick.play(Card.of(Suit.SPADES, Rank.ACE), Direction.NORTH);
        trick.play(Card.of(Suit.HEARTS, Rank.TWO), Direction.EAST); // trump
        trick.play(Card.of(Suit.HEARTS, Rank.KING), Direction.SOUTH); // higher trump
        trick.play(Card.of(Suit.DIAMONDS, Rank.ACE), Direction.WEST);

        assertEquals(Direction.SOUTH, trick.winner(Trump.HEARTS));
    }

    @Test
    void offSuitDoesNotWin() {
        Trick trick = new Trick();
        trick.play(Card.of(Suit.SPADES, Rank.TWO), Direction.NORTH);
        trick.play(Card.of(Suit.DIAMONDS, Rank.ACE), Direction.EAST); // off suit
        trick.play(Card.of(Suit.SPADES, Rank.THREE), Direction.SOUTH);
        trick.play(Card.of(Suit.CLUBS, Rank.ACE), Direction.WEST); // off suit

        assertEquals(Direction.SOUTH, trick.winner(Trump.NO_TRUMP));
    }
}
