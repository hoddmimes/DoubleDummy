package com.hoddmimes.bridgeanalyzer.game;

import com.hoddmimes.bridgeanalyzer.model.Card;
import com.hoddmimes.bridgeanalyzer.model.Direction;
import com.hoddmimes.bridgeanalyzer.model.Suit;
import com.hoddmimes.bridgeanalyzer.model.Trump;

public class Trick {
    private final Card[] cards = new Card[4];
    private final Direction[] players = new Direction[4];
    private int count;
    private Suit ledSuit;
    private int winIdx; // incrementally tracked winner index

    public void play(Card card, Direction player) {
        if (count == 0) {
            ledSuit = card.suit();
            winIdx = 0;
        } else {
            // we defer winner tracking to winner() for simplicity
            // but track it here: check if this card beats current winner
        }
        cards[count] = card;
        players[count] = player;
        count++;
    }

    public void undoLast() {
        count--;
        if (count == 0) {
            ledSuit = null;
        }
    }

    public int count() { return count; }
    public boolean isComplete() { return count == 4; }
    public Suit ledSuit() { return ledSuit; }
    public Card card(int i) { return cards[i]; }
    public Direction player(int i) { return players[i]; }

    public Direction winner(Trump trump) {
        int winIdx = 0;
        for (int i = 1; i < count; i++) {
            if (beats(cards[i], cards[winIdx], trump)) {
                winIdx = i;
            }
        }
        return players[winIdx];
    }

    /**
     * Compute the winner of a full trick given 4 specific cards and players,
     * without requiring them to be stored in this Trick object.
     */
    public static Direction computeWinner(Card c0, Direction p0, Card c1, Direction p1,
                                           Card c2, Direction p2, Card c3, Direction p3,
                                           Suit ledSuit, Trump trump) {
        Card winCard = c0;
        Direction winPlayer = p0;
        if (beatsStatic(c1, winCard, ledSuit, trump)) { winCard = c1; winPlayer = p1; }
        if (beatsStatic(c2, winCard, ledSuit, trump)) { winCard = c2; winPlayer = p2; }
        if (beatsStatic(c3, winCard, ledSuit, trump)) { winCard = c3; winPlayer = p3; }
        return winPlayer;
    }

    private static boolean beatsStatic(Card challenger, Card current, Suit ledSuit, Trump trump) {
        boolean challengerTrump = trump.isTrump(challenger.suit());
        boolean currentTrump = trump.isTrump(current.suit());

        if (challengerTrump != currentTrump) return challengerTrump;
        if (challengerTrump) {
            return challenger.rank().value() > current.rank().value();
        }
        if (challenger.suit() == current.suit()) {
            return challenger.rank().value() > current.rank().value();
        }
        // Different non-trump suits: led suit wins
        return challenger.suit() == ledSuit;
    }

    private boolean beats(Card challenger, Card current, Trump trump) {
        boolean challengerTrump = trump.isTrump(challenger.suit());
        boolean currentTrump = trump.isTrump(current.suit());

        if (challengerTrump && !currentTrump) return true;
        if (!challengerTrump && currentTrump) return false;
        if (challengerTrump) {
            return challenger.rank().value() > current.rank().value();
        }
        if (challenger.suit() == ledSuit && current.suit() != ledSuit) return true;
        if (challenger.suit() != ledSuit && current.suit() == ledSuit) return false;
        if (challenger.suit() == current.suit()) {
            return challenger.rank().value() > current.rank().value();
        }
        return false;
    }

    public void reset() {
        count = 0;
        ledSuit = null;
    }
}
