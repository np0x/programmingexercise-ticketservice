package com.rcg.walmart.event;

import com.rcg.walmart.seathold.SeatHold;
import com.rcg.walmart.seating.Seat;
import com.rcg.walmart.seating.SeatingFragment;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EventTest {
    private EventService unit;

    private static final int DEFAULT_BLOCK_INDEX = 1;
    private static final String EVENT_ID = "VENUE_1_DATE_20180231";

    @Before
    public void setup() {
        List<Seat> seats = Arrays.asList(
                new Seat(1, 1, 1.0),
                new Seat(1, 2, 0.0),
                new Seat(1, 3, 1.0)
        );
        unit = new EventService(EVENT_ID, Arrays.asList(new SeatingFragment(seats)), new ArrayList<SeatHold>());
    }

    @Test
    public void testReservationsRemoveSeatsFromAvailabillity() {
        List<Seat> seats = unit.getAvailableSeats();
        int reserveTheseSeats = 2;
        assertEquals(3, seats.size());
        List<Seat> goingToReserveTheseSeats = seats.stream().filter(s -> s.getBlockNumber().equals(DEFAULT_BLOCK_INDEX) && s.getSeatNumber() == reserveTheseSeats).collect(Collectors.toList());
        Optional<SeatHold> hold = unit.createSeatHold(Arrays.asList(new SeatingFragment(goingToReserveTheseSeats)), "", 10);
        assertTrue(hold.isPresent());
        assertEquals(1, hold.get().getSeatingFragments().size());
        assertTrue(hold.get().getSeatingFragments().get(0).getSeats().containsAll(goingToReserveTheseSeats));
        assertEquals("seat was not removed from available pool", 2, unit.totalSeatsAvailable());
        assertEquals("there are too many seating fragments, this will make seating large groups not go smoothly", 2, unit.getAvailableSeatingFragments().size());
    }

    @Test
    public void testZeroDurationJustGoesAway() {
        //simply by using 0 minute expiration reservation,

        Clock clock = mock(Clock.class);
        Instant fauxNow = Instant.now();
        Instant hourAgo = fauxNow.minus(Duration.ofHours(1));

        doReturn(hourAgo).doReturn(hourAgo).doReturn(hourAgo).when(clock).instant();
        doReturn(ZoneOffset.UTC).doReturn(ZoneOffset.UTC).when(clock).getZone();
        unit.setClock(clock);

        List<Seat> seats = unit.getAvailableSeats();
        List<Integer> reserveTheseSeats = Arrays.asList(2, 3);
        List<Seat> goingToReserveTheseSeats = seats.stream().filter(s -> s.getBlockNumber().equals(DEFAULT_BLOCK_INDEX) &&
                reserveTheseSeats.contains(s.getSeatNumber())).collect(Collectors.toList());
        assertEquals(2, goingToReserveTheseSeats.size());
        assertEquals(1, unit.getAvailableSeatingFragments().size());
        assertEquals(3, unit.totalSeatsAvailable());
        unit.createSeatHold(Arrays.asList(new SeatingFragment(goingToReserveTheseSeats)), "", 0);
        assertEquals("totalSeatsAvailable error", 3, unit.totalSeatsAvailable());
        assertEquals("number of available fragment count error", 1, unit.getAvailableSeatingFragments().size());
        assertEquals("there should be ZERO reservations on book", 0, unit.getAllSeatHolds().size());
    }

    @Test
    public void testEventCannotReserveUnavailableSeats() {
        List<Seat> oneSeat = unit.getAvailableSeats().subList(0, 1);
        assertEquals(1, oneSeat.size());

        Optional<SeatHold> seatHold = unit.createSeatHold(Arrays.asList(new SeatingFragment(oneSeat)), "", 10);
        assertTrue(seatHold.isPresent());

        Optional<SeatHold> cannotReserveSameSeatTwice = unit.createSeatHold(Arrays.asList(new SeatingFragment(oneSeat)), "", 10);
        assertFalse(cannotReserveSameSeatTwice.isPresent());
    }

    @Test
    public void middleBlockReservationExpirationAndRejoin() {
        //simply by using 0 minute expiration reservation,

        Clock clock = mock(Clock.class);
        Instant fauxNow = Instant.now();
        Instant hourAgo = fauxNow.minus(Duration.ofHours(1));

//        doReturn(hourAgo).doReturn(fauxNow).when(clock).instant();
        when(clock.instant()).thenReturn(hourAgo).thenReturn(hourAgo).thenReturn(hourAgo).thenReturn(hourAgo).thenReturn(fauxNow);
        doReturn(ZoneOffset.UTC).when(clock).getZone();
        unit.setClock(clock);

        List<Seat> seats = unit.getAvailableSeats();
        List<Integer> reserveTheseSeats = Arrays.asList(2);
        List<Seat> goingToReserveTheseSeats = seats.stream().filter(s -> s.getBlockNumber().equals(DEFAULT_BLOCK_INDEX) &&
                reserveTheseSeats.contains(s.getSeatNumber())).collect(Collectors.toList());
        assertEquals(1, goingToReserveTheseSeats.size());
        assertEquals(1, unit.getAvailableSeatingFragments().size());
        assertEquals(3, unit.totalSeatsAvailable());


        unit.createSeatHold(Arrays.asList(new SeatingFragment(goingToReserveTheseSeats)), "", 10);
        assertEquals("totalSeatsAvailable error", 2, unit.totalSeatsAvailable());
        assertEquals("number of available fragment count error", 2, unit.getAvailableSeatingFragments().size());
        assertEquals("there should be one reservations on book", 1, unit.getAllSeatHolds().size());

        when(clock.instant()).thenReturn(fauxNow);
        unit.removeStaleSeatHolds();

        assertEquals("totalSeatsAvailable error", 3, unit.totalSeatsAvailable());
        assertEquals("number of available fragment count error", 1, unit.getAvailableSeatingFragments().size());
        assertEquals("there should be ZERO reservations on book", 0, unit.getAllSeatHolds().size());
    }

    @Test
    public void testConfirmSeatHold() {
        String EMAIL = "someone@domain.com";

        List<Seat> seats = unit.getAvailableSeats();
        int reserveTheseSeats = 2;
        List<Seat> goingToReserveTheseSeats = seats.stream().filter(s -> s.getBlockNumber().equals(DEFAULT_BLOCK_INDEX) && s.getSeatNumber() == reserveTheseSeats).collect(Collectors.toList());
        Optional<SeatHold> hold = unit.createSeatHold(Arrays.asList(new SeatingFragment(goingToReserveTheseSeats)), EMAIL, 10);
        assertTrue(hold.isPresent());
        Optional<String> confirmation = unit.confirmSeatHold(hold.get().getSeatHoldId(), EMAIL);
        assertTrue(confirmation.isPresent());
    }

    @Test
    public void testFailConfirmWithWrongEmail() {
        String EMAIL = "someone@domain.com";
        String EMAIL2 = "someoneelse@domain.com";

        List<Seat> seats = unit.getAvailableSeats();
        int reserveTheseSeats = 2;
        List<Seat> goingToReserveTheseSeats = seats.stream().filter(s -> s.getBlockNumber().equals(DEFAULT_BLOCK_INDEX) && s.getSeatNumber() == reserveTheseSeats).collect(Collectors.toList());
        Optional<SeatHold> hold = unit.createSeatHold(Arrays.asList(new SeatingFragment(goingToReserveTheseSeats)), EMAIL, 10);
        assertTrue(hold.isPresent());
        Optional<String> confirmation = unit.confirmSeatHold(hold.get().getSeatHoldId(), EMAIL2);
        assertTrue(!confirmation.isPresent());
    }

}
