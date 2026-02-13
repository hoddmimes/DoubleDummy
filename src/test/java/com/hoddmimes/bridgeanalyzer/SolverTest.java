package com.hoddmimes.bridgeanalyzer;

import com.hoddmimes.bridgeanalyzer.game.Deal;
import com.hoddmimes.bridgeanalyzer.game.Hand;
import com.hoddmimes.bridgeanalyzer.model.*;
import com.hoddmimes.bridgeanalyzer.solver.AlphaBetaSolver;
import com.hoddmimes.bridgeanalyzer.solver.BruteForceSolver;
import com.hoddmimes.bridgeanalyzer.solver.Solver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SolverTest {

    /** Create a mini deal with a few cards per hand for fast testing. */
    private Deal miniDeal() {
        // 2 cards each, NT, N declares
        // N: SA SK   E: HA HK   S: SQ SJ   W: HQ HJ
        Deal deal = new Deal();
        Hand n = new Hand();
        n.add(Card.of(Suit.SPADES, Rank.ACE));
        n.add(Card.of(Suit.SPADES, Rank.KING));
        deal.setHand(Direction.NORTH, n);

        Hand e = new Hand();
        e.add(Card.of(Suit.HEARTS, Rank.ACE));
        e.add(Card.of(Suit.HEARTS, Rank.KING));
        deal.setHand(Direction.EAST, e);

        Hand s = new Hand();
        s.add(Card.of(Suit.SPADES, Rank.QUEEN));
        s.add(Card.of(Suit.SPADES, Rank.JACK));
        deal.setHand(Direction.SOUTH, s);

        Hand w = new Hand();
        w.add(Card.of(Suit.HEARTS, Rank.QUEEN));
        w.add(Card.of(Suit.HEARTS, Rank.JACK));
        deal.setHand(Direction.WEST, w);

        return deal;
    }

    @Test
    void bruteForceMiniDeal() {
        Deal deal = miniDeal();
        Solver solver = new BruteForceSolver();
        // N declares NT, E leads. E leads HA, S discards SJ, W plays HJ, N discards...
        // Actually: E leads, E has HA HK. NS have all spades, EW have all hearts.
        // NT: whoever leads wins their 2 tricks in their suit.
        // N declares -> E leads -> E takes 2 heart tricks -> NS=0
        int nsTricks = solver.solve(deal, Trump.NO_TRUMP, Direction.NORTH);
        assertEquals(0, nsTricks);
    }

    @Test
    void bruteForceMiniDealWithTrump() {
        Deal deal = miniDeal();
        Solver solver = new BruteForceSolver();
        // Spades trump, N declares -> E leads
        // E leads HA, S can trump with SQ! -> NS win
        // Then N leads SA, E must play HK (void in spades), W plays HQ, S plays SJ -> N wins
        // NS = 2
        int nsTricks = solver.solve(deal, Trump.SPADES, Direction.NORTH);
        assertEquals(2, nsTricks);
    }

    @Test
    void alphaBetaMiniDeal() {
        Deal deal = miniDeal();
        Solver solver = new AlphaBetaSolver();
        int nsTricks = solver.solve(deal, Trump.NO_TRUMP, Direction.NORTH);
        assertEquals(0, nsTricks);
    }

    @Test
    void alphaBetaMiniDealWithTrump() {
        Deal deal = miniDeal();
        Solver solver = new AlphaBetaSolver();
        int nsTricks = solver.solve(deal, Trump.SPADES, Direction.NORTH);
        assertEquals(2, nsTricks);
    }

    @Test
    void solversAgreeOnThreeCardDeal() {
        // 3 cards each
        // N: SA SK SQ   E: HA HK HQ   S: DA DK DQ   W: CA CK CQ
        Deal deal = new Deal();
        Hand n = new Hand();
        n.add(Card.of(Suit.SPADES, Rank.ACE));
        n.add(Card.of(Suit.SPADES, Rank.KING));
        n.add(Card.of(Suit.SPADES, Rank.QUEEN));
        deal.setHand(Direction.NORTH, n);

        Hand e = new Hand();
        e.add(Card.of(Suit.HEARTS, Rank.ACE));
        e.add(Card.of(Suit.HEARTS, Rank.KING));
        e.add(Card.of(Suit.HEARTS, Rank.QUEEN));
        deal.setHand(Direction.EAST, e);

        Hand s = new Hand();
        s.add(Card.of(Suit.DIAMONDS, Rank.ACE));
        s.add(Card.of(Suit.DIAMONDS, Rank.KING));
        s.add(Card.of(Suit.DIAMONDS, Rank.QUEEN));
        deal.setHand(Direction.SOUTH, s);

        Hand w = new Hand();
        w.add(Card.of(Suit.CLUBS, Rank.ACE));
        w.add(Card.of(Suit.CLUBS, Rank.KING));
        w.add(Card.of(Suit.CLUBS, Rank.QUEEN));
        deal.setHand(Direction.WEST, w);

        Solver bf = new BruteForceSolver();
        Solver ab = new AlphaBetaSolver();

        for (Trump trump : Trump.values()) {
            for (Direction declarer : Direction.values()) {
                int bfResult = bf.solve(deal, trump, declarer);
                int abResult = ab.solve(deal, trump, declarer);
                assertEquals(bfResult, abResult,
                        String.format("Mismatch for trump=%s, declarer=%s: bf=%d, ab=%d",
                                trump, declarer, bfResult, abResult));
            }
        }
    }
}
