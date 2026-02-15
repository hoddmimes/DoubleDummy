package com.hoddmimes.bridgeanalyzer.solver;

import com.hoddmimes.bridgeanalyzer.game.Deal;
import com.hoddmimes.bridgeanalyzer.game.GameState;
import com.hoddmimes.bridgeanalyzer.model.Card;
import com.hoddmimes.bridgeanalyzer.model.Direction;
import com.hoddmimes.bridgeanalyzer.model.Trump;

public class BruteForceSolver implements Solver {

    // Pre-allocated move buffers per recursion depth
    private final Card[][] moveBuffers = new Card[52][13];

    @Override
    public int solve(Deal deal, Trump trump, Direction declarer) {
        GameState state = new GameState(deal, trump, declarer);
        return minimax(state, 0);
    }

    private int minimax(GameState state, int depth) {
        if (state.isTerminal()) {
            return state.nsTricks();
        }

        // Early termination bounds
        int tricksPlayed = state.nsTricks() + state.ewTricks();
        int tricksRemaining = state.totalTricks() - tricksPlayed;

        // Last trick optimization
        if (state.currentTrick().count() == 0 && tricksRemaining == 1) {
            return state.solveLastTrick();
        }

        int nsMax = state.nsTricks() + tricksRemaining;
        int nsMin = state.nsTricks();

        Card[] moves = moveBuffers[depth];
        int moveCount = state.fillLegalMovesReduced(moves);
        boolean nsToPlay = state.nextPlayer().isNS();
        int best = nsToPlay ? -1 : Integer.MAX_VALUE;

        for (int i = 0; i < moveCount; i++) {
            Card card = moves[i];
            int undo = state.playCardFast(card);
            int result = minimax(state, depth + 1);
            state.undoCardFast(card, undo);

            if (nsToPlay) {
                best = Math.max(best, result);
                if (best == nsMax) break;
            } else {
                best = Math.min(best, result);
                if (best == nsMin) break;
            }
        }
        return best;
    }
}
