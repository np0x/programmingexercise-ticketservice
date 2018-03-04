package com.rcg.walmart.seating;

import com.rcg.walmart.seating.Seat;
import com.rcg.walmart.seating.SeatingFragment;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.*;
import static org.junit.Assert.assertFalse;

public class SeatingFragmentTest {
    static final double EPSILON = 0.00001;

    private SeatingFragment unit1;
    private SeatingFragment middleOfUnit1;
    private SeatingFragment unit1_1;
    private SeatingFragment unit2;
    private SeatingFragment unit3;
    private SeatingFragment fragmentFromBlock2;
    Seat seat1 = new Seat(1, 1, 2.0);
    Seat seat2 = new Seat(1, 2, 0.0);
    Seat seat3 = new Seat(1, 3, 1.0);
    Seat seat4 = new Seat(1, 4, 1.0);
    Seat seat5 = new Seat(1, 5, 0.0);
    Seat seat6 = new Seat(1, 6, 1.0);
    Seat seat7 = new Seat(1, 7, 0.0);
    Seat seat1FromAnotherBlock = new Seat(2, 1,4);

    @Before
    public void setup() {

        List<Seat> seats1 = Arrays.asList(seat1, seat2, seat3);
        unit1 = new SeatingFragment(seats1);
        List<Seat> seats_1_1 = Arrays.asList(seat2, seat3);
        unit1_1 = new SeatingFragment(seats_1_1);
        middleOfUnit1 = new SeatingFragment(Arrays.asList(seat2));

        List<Seat> seats2 = Arrays.asList(seat4,
                seat5);
        unit2 = new SeatingFragment(seats2);

        List<Seat> seats3 = Arrays.asList(seat6, seat7);
        unit3 = new SeatingFragment(seats3);

        fragmentFromBlock2 = new SeatingFragment(Arrays.asList(seat1FromAnotherBlock));
    }

    @Test
    public void testSeatingFragmentTotalCost() {
        assertEquals(3, unit1.getTotalSeatCost(), EPSILON);
    }

    @Test
    public void testEmptySeatingBlock() {
        //empty list no good
        List<Seat> seats = Arrays.asList();
        try {
            SeatingFragment unit = new SeatingFragment(seats);
            fail("exception thrown");
        } catch (IllegalArgumentException e) {
            //test passed
        }
    }

    @Test
    public void testNonContiguousSeatingBlock() {
        //skipped seatIndex #2
        List<Seat> seats = Arrays.asList(new Seat(1, 1, 1.0),
                new Seat(1, 3, 1.0));
        try {
            SeatingFragment unit = new SeatingFragment(seats);
            fail("exception thrown");
        } catch (IllegalArgumentException e) {
            //test passed
        }
    }

    @Test
    public void testSeatsFromDifferentBlocks() {
        //the blockIndexes should match
        List<Seat> seats = Arrays.asList(new Seat(1, 1, 1.0),
                new Seat(2, 2, 1.0));
        try {
            SeatingFragment unit = new SeatingFragment(seats);
            fail("exception thrown");
        } catch (IllegalArgumentException e) {
            //test passed
        }
    }

    @Test
    public void testSeatsIncorrectlyOrdered() {
        //this first seat should not be first
        List<Seat> seats = Arrays.asList(new Seat(1, 2, 1.0),
                new Seat(1, 1, 1.0));
        try {
            SeatingFragment unit = new SeatingFragment(seats);
        } catch (IllegalArgumentException e) {
            //test passed
        }
    }

    @Test
    public void contiguousBlockTests() {

        assertEquals(true, unit1.contiguousWith(unit2));
        assertEquals(true, unit2.contiguousWith(unit1));
        assertEquals(false, unit1.contiguousWith(unit3));
        assertEquals(false, unit3.contiguousWith(unit1));
    }

    @Test
    public void mergeTest() {
        SeatingFragment block1and2 = unit1.mergeWithBlock(unit2);
        assertEquals(5, block1and2.getSize());
        //opposite order
        SeatingFragment block1and2and3 = unit3.mergeWithBlock(block1and2);
        assertEquals(7, block1and2and3.getSize());
    }

    @Test
    public void containsTest() {
        assertTrue(unit1.containsAnother(unit1));
        assertTrue(unit1.containsAnother(unit1_1));
        assertFalse(unit1_1.containsAnother(unit1));
    }

    @Test
    public void minusTest() {
        assertFalse(unit1.minusAnother(unit3).isPresent());
        Optional<List<SeatingFragment>> result = unit1.minusAnother(unit1_1);
        assertTrue(result.isPresent());
        SeatingFragment firstFragment = result.get().get(0);
        assertEquals(unit1.getSize() - unit1_1.getSize(), firstFragment.getSize());
        assertEquals(1, firstFragment.getStartIndex());
        assertEquals(1, firstFragment.getEndIndex());
        assertEquals(1, firstFragment.getBlockIndex());

        result = unit1.minusAnother(unit1);
        assertTrue(result.isPresent());
        assertEquals(0, result.get().size());
    }

    @Test
    public void voidMinusFromMiddleOfFragmentTest() {
        Optional<List<SeatingFragment>> result = unit1.minusAnother(middleOfUnit1);
        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        assertEquals(1, result.get().get(0).getSize());
        assertEquals(1, result.get().get(1).getSize());
    }

    @Test
    public void findBestSubFragmentExtremes() {
        Optional<SeatingFragment> result = unit1.bestBlockFromFragment(3);
        assertTrue(result.isPresent());

        assertEquals(3, result.get().getSize());

        result = unit1.bestBlockFromFragment(4);
        assertFalse(result.isPresent());
    }

    @Test
    public void findBestSubFragment() {
        Optional<SeatingFragment> result = unit1.bestBlockFromFragment(2);
        assertTrue(result.isPresent());
        assertTrue("seat3 should be in there", result.get().getSeats().contains(seat3));

        result = unit1.bestBlockFromFragment(1);
        assertTrue(result.isPresent());
        assertTrue(result.get().getSeats().contains(seat2));
    }

    @Test
    public void testSeatFromAnotherBlock() {
        assertFalse(unit1.contiguousWith(fragmentFromBlock2));
    }

    @Test
    public void testTotalSeatCost() {
        assertEquals(3, unit1.getTotalSeatCost(), EPSILON);
    }

    @Test
    public void mergeFailsIfNotContiguous() {
        try {
            unit1.mergeWithBlock(unit3);
            fail();
        } catch (IllegalArgumentException e) {
            //error thrown is desired behavior
        }
    }

}
