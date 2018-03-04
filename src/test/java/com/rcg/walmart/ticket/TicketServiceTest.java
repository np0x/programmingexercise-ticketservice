package com.rcg.walmart.ticket;


import com.rcg.walmart.event.EventService;
import com.rcg.walmart.locking.LockService;
import com.rcg.walmart.locking.SimpleLockService;
import com.rcg.walmart.seathold.SeatHold;
import com.rcg.walmart.seating.SeatingFragment;
import com.rcg.walmart.venue.SquareVenue;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class TicketServiceTest {
    private static final String EMAIL = "name@domain.com";
    private static final String EVENT_ID = "VENUE_1_DATE_20180231";
    private TicketService unit;
    private EventService event;

    @Before
    public void setup() {
        SquareVenue hundredSquareVenueBestSeatFrontCenter = new SquareVenue(100, 100, 0, 50);
        event = new EventService(EVENT_ID, hundredSquareVenueBestSeatFrontCenter.getSeatingFragments(), new ArrayList<SeatHold>());
        SimpleLockService lockService = new SimpleLockService();
        unit = new TicketService(event, lockService);
    }

    @Test
    public void testGetEvents() {
        assertEquals(1, unit.getEvents().size());
    }

    @Test
    public void testSeatCount() {
        assertEquals(10000, unit.numSeatsAvailable());
    }

    @Test
    public void testSeatCountWithReservations() {
        SeatHold seatHold = unit.findAndHoldSeats(200, EMAIL);
        System.out.println(seatHold);
        assertEquals(9800, unit.numSeatsAvailable());
        assertNotNull(seatHold.getSeatHoldId());
        assertEquals(1, unit.getReservations().size());
        assertEquals(seatHold, unit.getReservation(seatHold.getSeatHoldId(), EMAIL));
    }

    @Test
    public void testSeatHoldToConfirmedTransition() {
        SeatHold seatHold = unit.findAndHoldSeats(10, EMAIL);
        assertEquals(9990, unit.numSeatsAvailable());
        assertNotNull(seatHold.getSeatHoldId());
        assertTrue(seatHold.isHold());
        assertFalse(seatHold.isReservation());
    }

    @Test
    public void testMultipleHolds() {
        SeatHold seatHold = unit.findAndHoldSeats(10, EMAIL);
        assertNotNull(seatHold);
        assertNotNull(seatHold.getSeatHoldId());
        assertTrue(seatHold.isHold());
        assertEquals(9990, unit.numSeatsAvailable());

        seatHold = unit.findAndHoldSeats(10, EMAIL);
        assertNotNull(seatHold);
        assertNotNull(seatHold.getSeatHoldId());
        assertTrue(seatHold.isHold());
        assertEquals(9980, unit.numSeatsAvailable());
    }

    @Test
    public void testFindBestSeatingFragments() {
        Optional<List<SeatingFragment>> optionalSeatingFragmentList = unit.findBestSeatingFragments(1);  //this should find the front row, middle seat
        assertTrue(optionalSeatingFragmentList.isPresent());
        System.out.println(optionalSeatingFragmentList.get());
        assertEquals(1, optionalSeatingFragmentList.get().size());
    }


    @Test
    public void findBestSeatingFragmentsTest() {
        Optional<List<SeatingFragment>> result = unit.findBestSeatingFragments(200);
        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());

        result = unit.findBestSeatingFragments(1);
        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
    }

    @Test
    public void testRequestMoreThanTotalSeats() {
        SeatHold result = unit.findAndHoldSeats(10001, "");
        assertNull(result);
    }

    @Test
    public void testConfirmSeatHold() {
        String email = "someone@domain.com";
        SeatHold hold = unit.findAndHoldSeats(101, email);
        assertNotNull(hold);
        String confirmationCode = unit.reserveSeats(hold.getSeatHoldId(), email);
        assertNotNull(confirmationCode);
    }

    @Test
    public void testEventLockAlreadyTaken() {
        String lockId = event.getEventId();
        LockService mockLockService = mock(LockService.class);
        doReturn(Optional.empty()).when(mockLockService).acquireLock(lockId);
        TicketService localUnit = new TicketService(event, mockLockService);

        SeatHold someoneElseIsInFlight = localUnit.findAndHoldSeats(1, "");
        assertNull(someoneElseIsInFlight);
    }
}
