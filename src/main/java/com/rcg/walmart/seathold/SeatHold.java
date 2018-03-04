package com.rcg.walmart.seathold;

import com.rcg.walmart.seating.SeatingFragment;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SeatHold {

    private static int counter;

    private int seatHoldId;
    private List<SeatingFragment> seatingFragments;
    private String confirmationCode;
    private String emailAddress;
    private ZonedDateTime holdTime;
    private ZonedDateTime confirmationTime;
    private int holdDurationMinutes;


    public int getSeatHoldId() {
        return this.seatHoldId;
    }

    public boolean isReservation() {
        return this.confirmationCode != null;
    }

    public boolean isHold() {
        return this.confirmationCode == null;
    }

    public List<SeatingFragment> getSeatingFragments() {
        return this.seatingFragments;
    }

    public String getEmail() {
        return this.emailAddress;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public ZonedDateTime getConfirmationTime() {
        return confirmationTime;
    }

    public Optional<String> confirmReservation(Clock timeprovider) {
        if (!isHold() || !isValid(timeprovider)){
            return Optional.empty();
        }
        this.confirmationTime = ZonedDateTime.now(timeprovider);
        this.confirmationCode = UUID.randomUUID().toString();
        return Optional.of(this.confirmationCode);
    }

    private boolean isConfirmedReservation() {
        return this.confirmationCode != null;
    }

    public boolean isValid(Clock timeprovider) {
        ZonedDateTime now = ZonedDateTime.now(timeprovider);
        boolean result = isConfirmedReservation() || this.holdTime.plusMinutes(this.holdDurationMinutes)
                .isAfter(now);
        return result;

    }

    public SeatHold(List<SeatingFragment> seatingFragments, String emailAddress, ZonedDateTime holdTime, int holdDurationMinutes) {
        this.seatHoldId = ++SeatHold.counter;
        this.seatingFragments = seatingFragments;
        this.confirmationCode = null;
        this.emailAddress = emailAddress;
        this.holdTime = holdTime;
        this.holdDurationMinutes = holdDurationMinutes;
    }
}
