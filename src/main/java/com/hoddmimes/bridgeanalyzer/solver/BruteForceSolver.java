package com.hoddmimes.bridgeanalyzer.solver;

import com.hoddmimes.bridgeanalyzer.game.Deal;
import com.hoddmimes.bridgeanalyzer.game.GameState;
import com.hoddmimes.bridgeanalyzer.model.Card;
import com.hoddmimes.bridgeanalyzer.model.Direction;
import com.hoddmimes.bridgeanalyzer.model.Trump;

import java.util.List;

public class BruteForceSolver implements Solver {

    @Override
    public int solve(Deal deal, Trump trump, Direction declarer) {
        GameState state = new GameState(deal, trump, declarer);
        return minimax(state);
    }

    private int minimax(GameState state) {
        if (state.isTerminal()) {
            return state.nsTricks();
        }

        List<Card> moves = state.legalMoves();
        boolean nsToPlay = state.nextPlayer().isNS();
        int best = nsToPlay ? -1 : Integer.MAX_VALUE;

        for (Card card : moves) {
            GameState.UndoInfo undo = state.playCard(card);
            int result = minimax(state);
            state.undoCard(card, undo);

            if (nsToPlay) {
                best = Math.max(best, result);
            } else {
                best = Math.min(best, result);
            }
        }
        return best;
    }
}
