package com.hoddmimes.bridgeanalyzer.cli;

import com.hoddmimes.bridgeanalyzer.game.Deal;
import com.hoddmimes.bridgeanalyzer.model.Direction;
import com.hoddmimes.bridgeanalyzer.model.Trump;
import com.hoddmimes.bridgeanalyzer.solver.Solver;
import com.hoddmimes.bridgeanalyzer.solver.SolverFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws Exception {
        Map<String, String> params = parseArgs(args);

        String solverName = params.getOrDefault("solver", "alphabeta");
        Solver solver = SolverFactory.create(solverName);

        List<Deal> deals;
        if (params.containsKey("file")) {
            deals = LinParser.parseFile(params.get("file"));
            if (params.containsKey("board")) {
                int boardNum = Integer.parseInt(params.get("board"));
                deals = deals.stream()
                        .filter(d -> d.boardNumber() == boardNum)
                        .toList();
                if (deals.isEmpty()) {
                    System.err.println("Board " + boardNum + " not found in file.");
                    System.exit(1);
                }
            }
        } else {
            deals = List.of(DealGenerator.generate());
        }

        Trump[] trumps;
        if (params.containsKey("trump")) {
            trumps = new Trump[]{Trump.fromString(params.get("trump"))};
        } else {
            trumps = Trump.values();
        }

        Direction[] declarers;
        if (params.containsKey("declarer")) {
            declarers = new Direction[]{Direction.fromChar(params.get("declarer").charAt(0))};
        } else {
            declarers = Direction.values();
        }

        for (Deal deal : deals) {
            printDeal(deal, solver, trumps, declarers);
        }
    }

    private static void printDeal(Deal deal, Solver solver, Trump[] trumps, Direction[] declarers) {
        System.out.printf("Board %d: %s%n", deal.boardNumber(),
                deal.boardName() != null ? deal.boardName() : "");
        System.out.print(deal.displayHands());

        // Header
        System.out.printf("%n       ");
        for (Trump t : trumps) {
            System.out.printf("%-4s", t.label());
        }
        System.out.println();

        for (Direction declarer : declarers) {
            System.out.printf("  %s:   ", declarer.name().charAt(0));
            for (Trump trump : trumps) {
                int nsTricks = solver.solve(deal, trump, declarer);
                int tricks;
                if (declarer.isNS()) {
                    tricks = nsTricks;
                } else {
                    tricks = deal.hand(Direction.NORTH).cardCount() - nsTricks;
                }
                System.out.printf("%-4d", tricks);
            }
            System.out.println();
        }
        System.out.println();
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> params = new HashMap<>();
        for (String arg : args) {
            if (arg.startsWith("--")) {
                String stripped = arg.substring(2);
                int eq = stripped.indexOf('=');
                if (eq >= 0) {
                    params.put(stripped.substring(0, eq), stripped.substring(eq + 1));
                } else {
                    params.put(stripped, "true");
                }
            }
        }
        return params;
    }
}
