package com.rcg.walmart.seating;

import org.junit.*;

import static org.junit.Assert.assertEquals;

public class SeatTest {
    private static double EPSILON = 0.0000001;
    @Test
    public void basicBeanTest() {
        Seat unit = new Seat(1,2,3.0);
        assertEquals(1, (int)unit.getBlockNumber());
        assertEquals(2, (int) unit.getSeatNumber());
        assertEquals(3.0, unit.getSeatValue(), EPSILON);
    }
}
