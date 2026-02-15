package com.hoddmimes.bridgeanalyzer.solver;

import com.hoddmimes.bridgeanalyzer.game.Deal;
import com.hoddmimes.bridgeanalyzer.game.GameState;
import com.hoddmimes.bridgeanalyzer.model.Card;
import com.hoddmimes.bridgeanalyzer.model.Direction;
import com.hoddmimes.bridgeanalyzer.model.Suit;
import com.hoddmimes.bridgeanalyzer.model.Trump;

public class AlphaBetaSolver implements Solver {

    // Pre-allocated move buffers per recursion depth (max 52 cards = 13 tricks * 4)
    private final Card[][] moveBuffers = new Card[52][13];
    private final TranspositionTable tt = new TranspositionTable();

    @Override
    public int solve(Deal deal, Trump trump, Direction declarer) {
        GameState state = new GameState(deal, trump, declarer);
        tt.clear();
        return alphaBeta(state, 0, state.totalTricks(), 0);
    }

    private long stateKey(GameState state) {
        long h = state.handBits(Direction.NORTH);
        h = h * 0x9E3779B97F4A7C15L + state.handBits(Direction.EAST);
        h = h * 0x9E3779B97F4A7C15L + state.handBits(Direction.SOUTH);
        h = h * 0x9E3779B97F4A7C15L + state.handBits(Direction.WEST);
        h = h * 31 + state.nextPlayer().index();
        h = h * 31 + state.currentTrick().count();
        return h;
    }

    private int moveScore(Card card, Suit ledSuit, Trump trump) {
        int score = card.rank().value();
        if (trump.isTrump(card.suit())) {
            score += 26;
        } else if (ledSuit != null && card.suit() == ledSuit) {
            score += 13;
        }
        return score;
    }

    private void orderMoves(Card[] moves, int count, GameState state) {
        if (count <= 1) return;

        boolean nsToPlay = state.nextPlayer().isNS();
        Trump trump = state.trump();
        Suit ledSuit = state.currentTrick().count() > 0 ? state.currentTrick().ledSuit() : null;

        // Simple insertion sort (small arrays, avoids allocation)
        for (int i = 1; i < count; i++) {
            Card key = moves[i];
            int keyScore = moveScore(key, ledSuit, trump);
            int j = i - 1;
            while (j >= 0) {
                int cmpScore = moveScore(moves[j], ledSuit, trump);
                boolean shouldSwap = nsToPlay ? cmpScore < keyScore : cmpScore > keyScore;
                if (!shouldSwap) break;
                moves[j + 1] = moves[j];
                j--;
            }
            moves[j + 1] = key;
        }
    }

    private int alphaBeta(GameState state, int alpha, int beta, int depth) {
        if (state.isTerminal()) {
            return state.nsTricks();
        }

        int tricksPlayed = state.nsTricks() + state.ewTricks();
        int tricksRemaining = state.totalTricks() - tricksPlayed;

        if (state.nsTricks() + tricksRemaining <= alpha) {
            return alpha;
        }
        if (state.nsTricks() >= beta) {
            return beta;
        }

        boolean atTrickBoundary = state.currentTrick().count() == 0;

        // Last trick optimization: compute directly without recursion
        if (atTrickBoundary && tricksRemaining == 1) {
            return state.solveLastTrick();
        }

        // Quick tricks tightening — only for the side that has the lead
        if (atTrickBoundary && tricksRemaining >= 3) {
            boolean nsLeads = state.nextPlayer().isNS();
            if (nsLeads) {
                int nsQuick = Math.min(state.countNSQuickTricks(), tricksRemaining);
                int nsFloor = state.nsTricks() + nsQuick;
                if (nsFloor >= beta) return nsFloor;
                alpha = Math.max(alpha, nsFloor);
            } else {
                int ewQuick = Math.min(state.countEWQuickTricks(), tricksRemaining);
                int nsCeiling = state.nsTricks() + tricksRemaining - ewQuick;
                if (nsCeiling <= alpha) return nsCeiling;
                beta = Math.min(beta, nsCeiling);
            }
        }

        // Transposition table lookup (only at trick boundaries for cleaner semantics)
        long key = 0;
        boolean useTT = atTrickBoundary;
        if (useTT) {
            key = stateKey(state);
            int slot = tt.lookup(key);
            if (slot >= 0) {
                int lower = tt.lower(slot), upper = tt.upper(slot);
                if (lower >= beta) return lower;
                if (upper <= alpha) return upper;
                alpha = Math.max(alpha, lower);
                beta = Math.min(beta, upper);
            }
        }

        int origAlpha = alpha;
        int origBeta = beta;

        Card[] moves = moveBuffers[depth];
        int moveCount = state.fillLegalMovesReduced(moves);
        orderMoves(moves, moveCount, state);
        boolean nsToPlay = state.nextPlayer().isNS();

        int value;
        if (nsToPlay) {
            value = 0;
            for (int i = 0; i < moveCount; i++) {
                Card card = moves[i];
                int undo = state.playCardFast(card);
                value = Math.max(value, alphaBeta(state, alpha, beta, depth + 1));
                state.undoCardFast(card, undo);
                alpha = Math.max(alpha, value);
                if (alpha >= beta) break;
            }
        } else {
            value = state.totalTricks();
            for (int i = 0; i < moveCount; i++) {
                Card card = moves[i];
                int undo = state.playCardFast(card);
                value = Math.min(value, alphaBeta(state, alpha, beta, depth + 1));
                state.undoCardFast(card, undo);
                beta = Math.min(beta, value);
                if (alpha >= beta) break;
            }
        }

        // Store in transposition table
        if (useTT) {
            int slot = tt.store(key);
            if (value <= origAlpha) {
                int cur = tt.upper(slot);
                if (value < cur) tt.setUpper(slot, value);
            } else if (value >= origBeta) {
                int cur = tt.lower(slot);
                if (value > cur) tt.setLower(slot, value);
            } else {
                tt.setLower(slot, value);
                tt.setUpper(slot, value);
            }
        }

        return value;
    }
}
