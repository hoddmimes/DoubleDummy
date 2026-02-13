package com.hoddmimes.bridgeanalyzer;

import com.hoddmimes.bridgeanalyzer.model.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DirectionTest {

    @Test
    void nextCyclesCorrectly() {
        assertEquals(Direction.EAST, Direction.NORTH.next());
        assertEquals(Direction.SOUTH, Direction.EAST.next());
        assertEquals(Direction.WEST, Direction.SOUTH.next());
        assertEquals(Direction.NORTH, Direction.WEST.next());
    }

    @Test
    void partnerIsCorrect() {
        assertEquals(Direction.SOUTH, Direction.NORTH.partner());
        assertEquals(Direction.NORTH, Direction.SOUTH.partner());
        assertEquals(Direction.WEST, Direction.EAST.partner());
        assertEquals(Direction.EAST, Direction.WEST.partner());
    }

    @Test
    void isNS() {
        assertTrue(Direction.NORTH.isNS());
        assertTrue(Direction.SOUTH.isNS());
        assertFalse(Direction.EAST.isNS());
        assertFalse(Direction.WEST.isNS());
    }
}
