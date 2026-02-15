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

    public void play(Card card, Direction player) {
        if (count == 0) {
            ledSuit = card.suit();
        }
        cards[count] = card;
        players[count] = player;
        count++;
    }

    public void undoLast() {
        count--;
        cards[count] = null;
        players[count] = null;
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

    private boolean beats(Card challenger, Card current, Trump trump) {
        boolean challengerTrump = trump.isTrump(challenger.suit());
        boolean currentTrump = trump.isTrump(current.suit());

        if (challengerTrump && !currentTrump) return true;
        if (!challengerTrump && currentTrump) return false;
        if (challengerTrump) {
            // both trump
            return challenger.rank().value() > current.rank().value();
        }
        // neither is trump
        if (challenger.suit() == ledSuit && current.suit() != ledSuit) return true;
        if (challenger.suit() != ledSuit && current.suit() == ledSuit) return false;
        if (challenger.suit() == current.suit()) {
            return challenger.rank().value() > current.rank().value();
        }
        return false; // different non-led suits, current wins
    }

    public void reset() {
        count = 0;
        ledSuit = null;
    }
}
