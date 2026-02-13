package com.hoddmimes.bridgeanalyzer.solver;

import com.hoddmimes.bridgeanalyzer.game.Deal;
import com.hoddmimes.bridgeanalyzer.game.GameState;
import com.hoddmimes.bridgeanalyzer.model.Card;
import com.hoddmimes.bridgeanalyzer.model.Direction;
import com.hoddmimes.bridgeanalyzer.model.Trump;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlphaBetaSolver implements Solver {

    @Override
    public int solve(Deal deal, Trump trump, Direction declarer) {
        GameState state = new GameState(deal, trump, declarer);
        Map<Long, int[]> transTable = new HashMap<>();
        return alphaBeta(state, 0, state.totalTricks(), transTable);
    }

    /**
     * Build a hash key from the 4 hands + who is next to play + trick position.
     * We encode: hands (52 bits each but combined), nextPlayer (2 bits), trick count in progress (2 bits).
     * Since hands are unique game states together with whose turn it is, we use a compact key.
     */
    private long stateKey(GameState state) {
        // Combine the 4 hand bitmasks. Since each card is in exactly one hand,
        // we need to know which hand has which card. Use a compact encoding:
        // For each of the 52 card positions, 2 bits tell us which player holds it.
        // But that's 104 bits. Instead, use a simpler approach:
        // hash = hand[N] XOR (hand[E] * prime) XOR (hand[S] * prime2) XOR ...
        // Plus next player and trick position.
        long h = state.handBits(Direction.NORTH);
        h = h * 0x9E3779B97F4A7C15L + state.handBits(Direction.EAST);
        h = h * 0x9E3779B97F4A7C15L + state.handBits(Direction.SOUTH);
        h = h * 0x9E3779B97F4A7C15L + state.handBits(Direction.WEST);
        h = h * 31 + state.nextPlayer().index();
        h = h * 31 + state.currentTrick().count();
        return h;
    }

    private int alphaBeta(GameState state, int alpha, int beta, Map<Long, int[]> tt) {
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

        // Transposition table lookup (only at trick boundaries for cleaner semantics)
        Long key = null;
        if (state.currentTrick().count() == 0) {
            key = stateKey(state);
            int[] cached = tt.get(key);
            if (cached != null) {
                int lower = cached[0], upper = cached[1];
                if (lower >= beta) return lower;
                if (upper <= alpha) return upper;
                alpha = Math.max(alpha, lower);
                beta = Math.min(beta, upper);
            }
        }

        int origAlpha = alpha;
        int origBeta = beta;

        List<Card> moves = state.legalMovesReduced();
        boolean nsToPlay = state.nextPlayer().isNS();

        int value;
        if (nsToPlay) {
            value = 0;
            for (Card card : moves) {
                GameState.UndoInfo undo = state.playCard(card);
                value = Math.max(value, alphaBeta(state, alpha, beta, tt));
                state.undoCard(card, undo);
                alpha = Math.max(alpha, value);
                if (alpha >= beta) break;
            }
        } else {
            value = state.totalTricks();
            for (Card card : moves) {
                GameState.UndoInfo undo = state.playCard(card);
                value = Math.min(value, alphaBeta(state, alpha, beta, tt));
                state.undoCard(card, undo);
                beta = Math.min(beta, value);
                if (alpha >= beta) break;
            }
        }

        // Store in transposition table
        if (key != null) {
            int[] entry = tt.computeIfAbsent(key, k -> new int[]{0, 13});
            if (value <= origAlpha) {
                entry[1] = Math.min(entry[1], value); // upper bound
            } else if (value >= origBeta) {
                entry[0] = Math.max(entry[0], value); // lower bound
            } else {
                entry[0] = value;
                entry[1] = value; // exact
            }
        }

        return value;
    }
}
