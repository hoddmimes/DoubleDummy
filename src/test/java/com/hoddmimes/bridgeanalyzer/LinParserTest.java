package com.hoddmimes.bridgeanalyzer;

import com.hoddmimes.bridgeanalyzer.cli.LinParser;
import com.hoddmimes.bridgeanalyzer.game.Deal;
import com.hoddmimes.bridgeanalyzer.model.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LinParserTest {

    @Test
    void parseExampleLine() {
        String line = "qx|o1|md|3SA74HKJ975DT8C842,ST853HT4DJ9CAKQJ5,SKQJ9HQ862DQ54C97|rh||ah|Bricka 1|sv|0|pg||";
        Deal deal = LinParser.parseLine(line);

        assertNotNull(deal);
        assertEquals(1, deal.boardNumber());
        assertEquals("Bricka 1", deal.boardName());
        assertEquals(Direction.NORTH, deal.dealer());

        // South: SA74 HKJ975 DT8 C842
        assertEquals(13, deal.hand(Direction.SOUTH).cardCount());
        // West: ST853 HT4 DJ9 CAKQJ5
        assertEquals(13, deal.hand(Direction.WEST).cardCount());
        // North: SKQJ9 HQ862 DQ54 C97
        assertEquals(13, deal.hand(Direction.NORTH).cardCount());
        // East: derived, should also be 13
        assertEquals(13, deal.hand(Direction.EAST).cardCount());
    }

    @Test
    void allCardsAccountedFor() {
        String line = "qx|o1|md|3SA74HKJ975DT8C842,ST853HT4DJ9CAKQJ5,SKQJ9HQ862DQ54C97|rh||ah|Bricka 1|sv|0|pg||";
        Deal deal = LinParser.parseLine(line);

        long allBits = deal.hand(Direction.SOUTH).bits()
                | deal.hand(Direction.WEST).bits()
                | deal.hand(Direction.NORTH).bits()
                | deal.hand(Direction.EAST).bits();
        assertEquals(52, Long.bitCount(allBits));
    }

    @Test
    void noOverlap() {
        String line = "qx|o1|md|3SA74HKJ975DT8C842,ST853HT4DJ9CAKQJ5,SKQJ9HQ862DQ54C97|rh||ah|Bricka 1|sv|0|pg||";
        Deal deal = LinParser.parseLine(line);

        Direction[] dirs = Direction.values();
        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < 4; j++) {
                long overlap = deal.hand(dirs[i]).bits() & deal.hand(dirs[j]).bits();
                assertEquals(0, overlap,
                        "Overlap between " + dirs[i] + " and " + dirs[j]);
            }
        }
    }
}
