package com.hoddmimes.bridgeanalyzer.cli;

import com.hoddmimes.bridgeanalyzer.game.Deal;
import com.hoddmimes.bridgeanalyzer.game.Hand;
import com.hoddmimes.bridgeanalyzer.model.Card;
import com.hoddmimes.bridgeanalyzer.model.Direction;
import com.hoddmimes.bridgeanalyzer.model.Rank;
import com.hoddmimes.bridgeanalyzer.model.Suit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LinParser {

    public static List<Deal> parseFile(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(filePath));
        List<Deal> deals = new ArrayList<>();
        int boardNum = 0;
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            Deal deal = parseLine(line);
            if (deal != null) {
                boardNum++;
                if (deal.boardNumber() == 0) {
                    deal.setBoardNumber(boardNum);
                }
                deals.add(deal);
            }
        }
        return deals;
    }

    public static Deal parseLine(String line) {
        // Parse pipe-separated tag|value pairs
        String[] tokens = line.split("\\|", -1);
        Deal deal = new Deal();
        String mdField = null;

        for (int i = 0; i < tokens.length - 1; i += 2) {
            String tag = tokens[i];
            String value = tokens[i + 1];
            switch (tag) {
                case "qx" -> {
                    // Extract board number from e.g. "o1" or "c3"
                    String num = value.replaceAll("[^0-9]", "");
                    if (!num.isEmpty()) {
                        deal.setBoardNumber(Integer.parseInt(num));
                    }
                }
                case "md" -> mdField = value;
                case "ah" -> deal.setBoardName(value);
                case "sv" -> deal.setVulnerability(value);
            }
        }

        if (mdField == null) return null;
        parseMd(deal, mdField);
        return deal;
    }

    public static void parseMd(Deal deal, String md) {
        // First char is dealer: 1=S, 2=W, 3=N, 4=E
        char dealerChar = md.charAt(0);
        deal.setDealer(switch (dealerChar) {
            case '1' -> Direction.SOUTH;
            case '2' -> Direction.WEST;
            case '3' -> Direction.NORTH;
            case '4' -> Direction.EAST;
            default -> Direction.NORTH;
        });

        // Rest is 3 hands separated by commas: South, West, North
        String handsPart = md.substring(1);
        String[] handStrs = handsPart.split(",");

        Direction[] order = {Direction.SOUTH, Direction.WEST, Direction.NORTH};
        long allBits = 0;
        for (int i = 0; i < 3 && i < handStrs.length; i++) {
            Hand hand = parseHand(handStrs[i]);
            deal.setHand(order[i], hand);
            allBits |= hand.bits();
        }

        // East = remaining cards
        long fullDeck = (1L << 52) - 1;
        Hand east = new Hand(fullDeck & ~allBits);
        deal.setHand(Direction.EAST, east);
    }

    public static Hand parseHand(String s) {
        Hand hand = new Hand();
        Suit currentSuit = null;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case 'S' -> currentSuit = Suit.SPADES;
                case 'H' -> currentSuit = Suit.HEARTS;
                case 'D' -> currentSuit = Suit.DIAMONDS;
                case 'C' -> currentSuit = Suit.CLUBS;
                default -> {
                    if (currentSuit != null) {
                        Rank rank = Rank.fromChar(c);
                        hand.add(Card.of(currentSuit, rank));
                    }
                }
            }
        }
        return hand;
    }
}
