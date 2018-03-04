package com.rcg.walmart.venue;

import com.rcg.walmart.venue.SquareVenue;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


public class SquareVenueTest {
    static final int IDEAL_ROW = 4;
    static final int IDEAL_SEAT_INDEX = 4;
    static final double EPSILON = 0.00001;
    private SquareVenue unit;

    @Before
    public void setup() {
        unit = new SquareVenue(10, 10, IDEAL_ROW, IDEAL_SEAT_INDEX);
    }

    @Test
    public void testIdealizedSeatValue() {
        Optional<Double> result =unit.getSeatValue(4, 4);
        assertTrue(result.isPresent());
        result.ifPresent(d -> assertEquals(0.0, d, EPSILON));
    }

    @Test
    public void testSeatValuesAwayFromIdealizedSeat() {
        Optional<Double> result = unit.getSeatValue(IDEAL_ROW, IDEAL_SEAT_INDEX - 1);
        assertTrue(result.isPresent());
        result.ifPresent(v -> assertEquals(1.0, v, EPSILON));

        int row = IDEAL_ROW + 2;
        int seatIndex = IDEAL_SEAT_INDEX + 2;
        Double linearDistance = Math.sqrt(Math.pow(2, 2) + Math.pow(2, 2));
        assertEquals(0.0, Math.abs(linearDistance - unit.getSeatValue(row, seatIndex).get()), EPSILON);
    }

    @Test
    public void testSeatCount() {
        assertEquals(100, unit.numSeatsAvailable());
    }

    @Test
    public void testSeatingFragmentCount() {
        assertEquals(10, unit.getSeatingFragments().size());
    }

}
