package com.rcg.walmart.event;

import com.rcg.walmart.seating.Seat;
import com.rcg.walmart.seating.SeatingFragment;
import com.rcg.walmart.seathold.SeatHold;
import com.rcg.walmart.venue.SquareVenue;
import com.rcg.walmart.venue.Venue;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/* simplistic service, that only knows about a single event */
@Service
public class EventService {
    private static final String DEFAULT_EVENT_ID = "main_event";
        private static final Venue DEFAULT_VENUE = new SquareVenue(100, 100, 0, 50);
    private String eventId;
    private List<SeatingFragment> availableSeats;
    private List<SeatHold> reservations;
    private Clock clock;

    public EventService() {
        this(DEFAULT_EVENT_ID, DEFAULT_VENUE.getSeatingFragments(), new ArrayList<>());
    }

    public EventService(String eventId, List<SeatingFragment> availableSeatingFragments, List<SeatHold> reservations) {
        this.eventId = eventId;
        this.availableSeats = availableSeatingFragments.stream().collect(Collectors.toCollection(ArrayList::new));
        this.reservations = reservations.stream().collect(Collectors.toCollection(ArrayList::new));
        this.clock = Clock.system(ZoneOffset.UTC);
    }


    public String getEventId() {
        return eventId;
    }


    public Optional<String> confirmSeatHold(int seatHoldId, String emailAddressOnFile) {
        Optional<SeatHold> hold = getSeatHold(seatHoldId, emailAddressOnFile);
        if (hold.isPresent() && hold.isPresent()) {
            return hold.get().confirmReservation(this.clock);
        } else {
            return Optional.empty();
        }
    }

    public Optional<SeatHold> getSeatHold(int seatHoldId, String emailAddressOnFile) {
        Optional<SeatHold> hold = reservations.stream()
                .filter(sh -> sh.getSeatHoldId() == seatHoldId && sh.getEmail().equals(emailAddressOnFile))
                .findFirst();
        return hold;
    }

    private void addReservation(SeatHold seatHold) {
        synchronized (this.reservations) {
            this.reservations.add(seatHold);
            removeStaleSeatHolds();
        }
    }

    public Optional<SeatHold> createSeatHold(List<SeatingFragment> requestedSeats, String email, int holdDurationMinutes) {
        boolean seatsAvailable = requestedSeats.stream().allMatch(requestedFragment ->
                this.availableSeats.stream().anyMatch(availableFragment -> availableFragment.containsAnother(requestedFragment))
        );
        if (seatsAvailable) {
            ArrayList<SeatingFragment> reservedBlocks = new ArrayList<SeatingFragment>();
            for (SeatingFragment r : requestedSeats) {
                //before we got here we confirmed up at the top that the fragments are all represented, proceed sunny day
                Optional<SeatingFragment> sourceBlock = this.availableSeats.stream().filter(sf -> sf.containsAnother(r)).findFirst();

                boolean removalResult = this.availableSeats.remove(sourceBlock.get());
                reservedBlocks.add(r);
                Optional<List<SeatingFragment>> remainingAvailableSpace = sourceBlock.get().minusAnother(r);

                this.availableSeats.addAll(remainingAvailableSpace.get());
            }
            SeatHold seatHold = new SeatHold(reservedBlocks, email, ZonedDateTime.now(this.clock), holdDurationMinutes);
            addReservation(seatHold);
            return Optional.of(seatHold);
        }
        return Optional.empty();

    }

    private void returnSeatingFragmentToAvailablePool(SeatingFragment staleReservationFragment) {
        synchronized (this.availableSeats) {
            SeatingFragment addThisBackToAvailable = new SeatingFragment(staleReservationFragment.getSeats());
            Optional<SeatingFragment> before = this.availableSeats.stream()
                    .filter(sf -> sf.getBlockIndex() == staleReservationFragment.getBlockIndex()
                            && sf.getEndIndex() + 1 == staleReservationFragment.getStartIndex()).findFirst();

            Optional<SeatingFragment> after = this.availableSeats.stream()
                    .filter(sf -> sf.getBlockIndex() == staleReservationFragment.getBlockIndex()
                            && sf.getStartIndex() - 1 == staleReservationFragment.getEndIndex()).findFirst();
            if (before.isPresent()) {
                this.availableSeats.remove(before.get());
                addThisBackToAvailable = addThisBackToAvailable.mergeWithBlock(before.get());
            }
            if (after.isPresent()) {
                this.availableSeats.remove(after.get());
                addThisBackToAvailable = addThisBackToAvailable.mergeWithBlock(after.get());
            }
            this.availableSeats.add(addThisBackToAvailable);
        }
    }

    public void removeStaleSeatHolds() {
        synchronized (this.reservations) {
            List<SeatHold> staleSeatHolds = this.reservations.stream()
                    .filter(r -> !r.isValid(this.clock)).collect(Collectors.toList());
            for (SeatHold r : staleSeatHolds) {
                for (final SeatingFragment seatingFragmentFromStaleHold : r.getSeatingFragments()) {
                    returnSeatingFragmentToAvailablePool(seatingFragmentFromStaleHold);
                }
                this.reservations.remove(r);
            }
        }
    }

    public int totalSeatsAvailable() {
        removeStaleSeatHolds();
        int numSeatsAvailable = availableSeats.stream().map(SeatingFragment::getSize).reduce(0, (a, b) -> a + b);
        return numSeatsAvailable;
    }

    public List<SeatHold> getAllSeatHolds() {
        removeStaleSeatHolds();
        return reservations;
    }

    public List<Seat> getAvailableSeats() {
        List<Seat> availableSeats = this.availableSeats.stream()
                .map(SeatingFragment::getSeats)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        return availableSeats;
    }

    public List<SeatingFragment> getAvailableSeatingFragments() {
        return this.availableSeats;
    }


    //for testing purposes
    public void setClock(Clock clock) {
        this.clock = clock;
    }
}
