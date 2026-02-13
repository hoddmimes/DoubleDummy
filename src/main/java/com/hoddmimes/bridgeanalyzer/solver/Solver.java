package com.hoddmimes.bridgeanalyzer.solver;

import com.hoddmimes.bridgeanalyzer.game.Deal;
import com.hoddmimes.bridgeanalyzer.model.Direction;
import com.hoddmimes.bridgeanalyzer.model.Trump;

public interface Solver {
    /** Returns the number of tricks NS can take with optimal play from both sides. */
    int solve(Deal deal, Trump trump, Direction declarer);
}
