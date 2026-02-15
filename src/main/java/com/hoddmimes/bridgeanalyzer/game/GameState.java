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

    // Pre-allocated undo stack to avoid per-call allocations
    private final UndoSlot[] undoStack;
    private int undoDepth;

    public GameState(Deal deal, Trump trump, Direction declarer) {
        for (Direction dir : Direction.values()) {
            hands[dir.index()] = deal.hand(dir).bits();
        }
        this.trump = trump;
        this.currentTrick = new Trick();
        this.nextPlayer = declarer.next();
        this.totalTricks = deal.hand(Direction.NORTH).cardCount();

        // Max depth = totalTricks * 4 cards per trick
        this.undoStack = new UndoSlot[totalTricks * 4];
        for (int i = 0; i < undoStack.length; i++) {
            undoStack[i] = new UndoSlot();
        }
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
     * Fill buffer with legal moves. Returns the number of moves written.
     */
    public int fillLegalMoves(Card[] buffer) {
        long myHand = hands[nextPlayer.index()];
        Suit ledSuit = currentTrick.count() > 0 ? currentTrick.ledSuit() : null;

        if (ledSuit != null) {
            long suitBits = (myHand >> (ledSuit.index() * 13)) & SUIT_MASK;
            if (suitBits != 0) {
                // Must follow suit
                int base = ledSuit.index() * 13;
                int count = 0;
                while (suitBits != 0) {
                    int bit = Long.numberOfTrailingZeros(suitBits);
                    buffer[count++] = Card.fromBitIndex(base + bit);
                    suitBits &= suitBits - 1;
                }
                return count;
            }
        }

        // Play any card
        int count = 0;
        long b = myHand;
        while (b != 0) {
            int bit = Long.numberOfTrailingZeros(b);
            buffer[count++] = Card.fromBitIndex(bit);
            b &= b - 1;
        }
        return count;
    }

    /**
     * Fill buffer with reduced legal moves (equivalent cards pruned).
     * Returns the number of moves written.
     */
    public int fillLegalMovesReduced(Card[] buffer) {
        int moveCount = fillLegalMoves(buffer);
        if (moveCount <= 1) return moveCount;

        long allRemaining = hands[0] | hands[1] | hands[2] | hands[3];
        long myHand = hands[nextPlayer.index()];

        int writeIdx = 0;
        for (int i = 0; i < moveCount; i++) {
            Card card = buffer[i];
            int suitBase = card.suit().index() * 13;
            int rank = card.rank().value();

            boolean isEquivalentToHigher = false;
            if (rank < 12) {
                long suitRemaining = (allRemaining >> suitBase) & SUIT_MASK;
                long mySuitCards = (myHand >> suitBase) & SUIT_MASK;

                long aboveMask = suitRemaining >> (rank + 1);
                if (aboveMask != 0) {
                    int nextRemainingRank = rank + 1 + Long.numberOfTrailingZeros(aboveMask);
                    if ((mySuitCards & (1L << nextRemainingRank)) != 0) {
                        isEquivalentToHigher = true;
                    }
                }
            }

            if (!isEquivalentToHigher) {
                buffer[writeIdx++] = card;
            }
        }
        return writeIdx;
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

    /** Pre-allocated undo slot to avoid allocations in the hot path. */
    static class UndoSlot {
        Direction prevPlayer;
        boolean trickCompleted;
        Direction trickWinner;
        final Card[] trickCards = new Card[4];
        final Direction[] trickPlayers = new Direction[4];
    }

    /**
     * Play a card using the pre-allocated undo stack. Returns the undo depth
     * (to be passed to undoCardFast).
     */
    public int playCardFast(Card card) {
        UndoSlot slot = undoStack[undoDepth];
        slot.prevPlayer = nextPlayer;
        hands[nextPlayer.index()] &= ~card.bitMask();
        currentTrick.play(card, nextPlayer);

        if (currentTrick.isComplete()) {
            Direction winner = currentTrick.winner(trump);
            slot.trickCompleted = true;
            slot.trickWinner = winner;
            for (int i = 0; i < 4; i++) {
                slot.trickCards[i] = currentTrick.card(i);
                slot.trickPlayers[i] = currentTrick.player(i);
            }
            if (winner.isNS()) {
                tricksWon[0]++;
            } else {
                tricksWon[1]++;
            }
            currentTrick.reset();
            nextPlayer = winner;
        } else {
            slot.trickCompleted = false;
            nextPlayer = nextPlayer.next();
        }
        return undoDepth++;
    }

    /**
     * Undo a card using the pre-allocated undo stack.
     */
    public void undoCardFast(Card card, int depth) {
        undoDepth = depth;
        UndoSlot slot = undoStack[depth];
        if (slot.trickCompleted) {
            if (slot.trickWinner.isNS()) {
                tricksWon[0]--;
            } else {
                tricksWon[1]--;
            }
            currentTrick.reset();
            for (int i = 0; i < 3; i++) {
                currentTrick.play(slot.trickCards[i], slot.trickPlayers[i]);
            }
        } else {
            currentTrick.undoLast();
        }
        hands[slot.prevPlayer.index()] |= card.bitMask();
        nextPlayer = slot.prevPlayer;
    }

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
