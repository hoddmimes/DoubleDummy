package com.hoddmimes.bridgeanalyzer.game;

import com.hoddmimes.bridgeanalyzer.model.Card;
import com.hoddmimes.bridgeanalyzer.model.Direction;
import com.hoddmimes.bridgeanalyzer.model.Suit;
import com.hoddmimes.bridgeanalyzer.model.Trump;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    private static final long SUIT_MASK = 0x1FFFL;

    private final long[] hands = new long[4];
    private final int[] tricksWon = new int[2]; // 0=NS, 1=EW
    private final Trick currentTrick;
    private final Trump trump;
    private Direction nextPlayer;
    private final int totalTricks;

    public GameState(Deal deal, Trump trump, Direction declarer) {
        for (Direction dir : Direction.values()) {
            hands[dir.index()] = deal.hand(dir).bits();
        }
        this.trump = trump;
        this.currentTrick = new Trick();
        this.nextPlayer = declarer.next();
        this.totalTricks = deal.hand(Direction.NORTH).cardCount();
    }

    public Trump trump() { return trump; }
    public Direction nextPlayer() { return nextPlayer; }
    public int nsTricks() { return tricksWon[0]; }
    public int ewTricks() { return tricksWon[1]; }
    public int totalTricks() { return totalTricks; }
    public Trick currentTrick() { return currentTrick; }

    public boolean isTerminal() {
        return tricksWon[0] + tricksWon[1] == totalTricks;
    }

    public List<Card> legalMoves() {
        Hand h = new Hand(hands[nextPlayer.index()]);
        Suit ledSuit = currentTrick.count() > 0 ? currentTrick.ledSuit() : null;
        return h.legalPlays(ledSuit);
    }

    /**
     * Like legalMoves() but with equivalent cards pruned.
     * Two cards in the same suit are equivalent if they are adjacent in the
     * remaining-card ordering (no card between them exists in any hand).
     * Only the highest of each equivalence group is returned.
     */
    public List<Card> legalMovesReduced() {
        List<Card> moves = legalMoves();
        if (moves.size() <= 1) return moves;

        // Compute remaining cards across all hands for each suit
        long allRemaining = hands[0] | hands[1] | hands[2] | hands[3];
        long myHand = hands[nextPlayer.index()];

        List<Card> reduced = new ArrayList<>();
        for (Card card : moves) {
            int suitBase = card.suit().index() * 13;
            int rank = card.rank().value();

            // Check if there's a higher card in the same suit in our legal moves
            // that is adjacent (no intervening remaining cards from other hands)
            boolean isEquivalentToHigher = false;
            if (rank < 12) {
                int nextRank = rank + 1;
                long suitRemaining = (allRemaining >> suitBase) & SUIT_MASK;
                long mySuitCards = (myHand >> suitBase) & SUIT_MASK;

                // Check if the next higher remaining card is also in our hand
                // Find the next set bit above 'rank' in suitRemaining
                long aboveMask = suitRemaining >> (rank + 1);
                if (aboveMask != 0) {
                    int nextRemainingRank = rank + 1 + Long.numberOfTrailingZeros(aboveMask);
                    // If that next remaining card is also in our hand, they're equivalent
                    if ((mySuitCards & (1L << nextRemainingRank)) != 0) {
                        isEquivalentToHigher = true;
                    }
                }
            }

            if (!isEquivalentToHigher) {
                reduced.add(card);
            }
        }
        return reduced;
    }

    public record UndoInfo(Direction prevPlayer, boolean trickCompleted, Direction trickWinner,
                           Card[] trickCards, Direction[] trickPlayers) {}

    public UndoInfo playCard(Card card) {
        Direction prevPlayer = nextPlayer;
        hands[nextPlayer.index()] &= ~card.bitMask();
        currentTrick.play(card, nextPlayer);

        if (currentTrick.isComplete()) {
            Direction winner = currentTrick.winner(trump);
            Card[] tc = new Card[4];
            Direction[] tp = new Direction[4];
            for (int i = 0; i < 4; i++) {
                tc[i] = currentTrick.card(i);
                tp[i] = currentTrick.player(i);
            }
            if (winner.isNS()) {
                tricksWon[0]++;
            } else {
                tricksWon[1]++;
            }
            currentTrick.reset();
            nextPlayer = winner;
            return new UndoInfo(prevPlayer, true, winner, tc, tp);
        } else {
            nextPlayer = nextPlayer.next();
            return new UndoInfo(prevPlayer, false, null, null, null);
        }
    }

    public void undoCard(Card card, UndoInfo undo) {
        if (undo.trickCompleted()) {
            if (undo.trickWinner().isNS()) {
                tricksWon[0]--;
            } else {
                tricksWon[1]--;
            }
            currentTrick.reset();
            for (int i = 0; i < 3; i++) {
                currentTrick.play(undo.trickCards()[i], undo.trickPlayers()[i]);
            }
        } else {
            currentTrick.undoLast();
        }
        hands[undo.prevPlayer().index()] |= card.bitMask();
        nextPlayer = undo.prevPlayer();
    }

    public long handBits(Direction dir) {
        return hands[dir.index()];
    }
}
