package com.hoddmimes.bridgeanalyzer.cli;

import com.hoddmimes.bridgeanalyzer.game.Deal;
import com.hoddmimes.bridgeanalyzer.game.Hand;
import com.hoddmimes.bridgeanalyzer.model.Card;
import com.hoddmimes.bridgeanalyzer.model.Direction;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class DealGenerator {

    public static Deal generate() {
        Card[] allCards = Card.allCards();
        List<Card> deck = new ArrayList<>(List.of(allCards));
        Collections.shuffle(deck);

        Deal deal = new Deal();
        deal.setBoardName("Random");
        deal.setBoardNumber(1);
        deal.setDealer(Direction.NORTH);

        Direction[] dirs = Direction.values();
        for (int i = 0; i < 52; i++) {
            Hand hand = deal.hand(dirs[i / 13]);
            hand.add(deck.get(i));
        }
        return deal;
    }
}
