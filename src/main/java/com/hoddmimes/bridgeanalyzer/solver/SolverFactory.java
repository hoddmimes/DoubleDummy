package com.hoddmimes.bridgeanalyzer.solver;

public class SolverFactory {
    public static Solver create(String name) {
        return switch (name.toLowerCase()) {
            case "bruteforce" -> new BruteForceSolver();
            case "alphabeta" -> new AlphaBetaSolver();
            default -> throw new IllegalArgumentException("Unknown solver: " + name);
        };
    }
}
