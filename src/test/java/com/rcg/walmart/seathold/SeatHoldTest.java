package com.rcg.walmart.seathold;

import com.rcg.walmart.seathold.SeatHold;
import org.junit.Before;
import org.junit.Test;

import java.time.*;
import java.util.Optional;
import java.util.UUID;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class SeatHoldTest {

    private static final String EMAIL = "someone@domain.com";

    @Test
    public void testIsValid() {
        Clock clock = mock(Clock.class);
        Instant fauxNow = Instant.now();
        Instant hourAgo = fauxNow.minus(Duration.ofHours(1));

        doReturn(hourAgo).doReturn(fauxNow).doReturn(fauxNow).when(clock).instant();
        doReturn(ZoneOffset.UTC).when(clock).getZone();

        SeatHold unit = new SeatHold(null, EMAIL, ZonedDateTime.now(clock), 10);
        assertFalse(unit.isValid(clock));
        assertEquals(EMAIL, unit.getEmail());
    }

    @Test
    public void testConfirmReservation() {

        Clock clock = mock(Clock.class);
        Instant fauxNow = Instant.now();
        Instant hourAgo = fauxNow.minus(Duration.ofHours(1));

        doReturn(fauxNow).when(clock).instant();
        doReturn(ZoneOffset.UTC).when(clock).getZone();

        SeatHold unit = new SeatHold(null, EMAIL, ZonedDateTime.now(clock), 10);
        assertTrue(unit.isValid(clock));

        assertTrue(unit.isHold());
        assertFalse(unit.isReservation());

        assertNull(unit.getConfirmationTime());
        Optional<String> confirmationCode = unit.confirmReservation(clock);
        assertTrue(unit.isReservation());
        assertFalse(unit.isHold());
        assertTrue(confirmationCode.isPresent());
        assertNotNull(unit.getConfirmationCode());

        try {
            UUID test = UUID.fromString(confirmationCode.get());
            //the id of the reservation is a valid uuid
        } catch (IllegalArgumentException e) {
            fail("The confirmation code was not a valid uuid");
        }

        ZonedDateTime confirmationTime = unit.getConfirmationTime();
        assertNotNull(confirmationTime);

        Optional<String> secondErroneousConfirmation = unit.confirmReservation(clock);
        assertFalse(secondErroneousConfirmation.isPresent());
    }

    @Test
    public void testConfirmingExpiredHold() {
        Clock clock = mock(Clock.class);
        Instant fauxNow = Instant.now();
        Instant hourAgo = fauxNow.minus(Duration.ofHours(1));
        doReturn(hourAgo).doReturn(fauxNow).when(clock).instant();
        doReturn(ZoneOffset.UTC).when(clock).getZone();

        SeatHold unit = new SeatHold(null, EMAIL, ZonedDateTime.now(clock), 10);

        Optional<String> failedStaleConfirmation = unit.confirmReservation(clock);
        assertFalse(failedStaleConfirmation.isPresent());


    }
}
