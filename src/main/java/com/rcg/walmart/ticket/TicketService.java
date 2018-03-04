package com.rcg.walmart.ticket;

import com.rcg.walmart.event.EventService;
import com.rcg.walmart.locking.Lock;
import com.rcg.walmart.locking.LockService;
import com.rcg.walmart.seathold.SeatHold;
import com.rcg.walmart.seating.SeatingFragment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TicketService {

    //Because of the signature of the TicketService and the scope of the assignment, a simple private instance of
    //the eventService is being used here without parameters as there is not a good way to look up the event from the
    // signatures used to create the holds and reservations.
    private static final int HOLD_DURATION_MINUTES = 2;

    @Autowired
    private EventService eventService;
    @Autowired
    private LockService lockService;

    public TicketService(EventService eventService, LockService lockService) {
        this.lockService = lockService;
        this.eventService = eventService;
    }

    /**
     * The number of seats in the venue that are neither held nor reserved
     *
     * @return the number of tickets available in the venue
     */
    public int numSeatsAvailable() {
        return this.eventService.totalSeatsAvailable();
    }

    /**
     * Find and hold the best available seats for a customer
     *
     * @param numSeats      the number of seats to find and hold
     * @param customerEmail unique identifier for the customer
     * @return a SeatHold object identifying the specific seats and related
     * information
     */
    public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
        Optional<Lock> eventLock = lockService.acquireLock(eventService.getEventId());
        if (!eventLock.isPresent()) {
            return null;
            //at the controller level we would convert this null to something more meaningful
        }

        Optional<List<SeatingFragment>> seatsRequested = findBestSeatingFragments(numSeats);
        if (!seatsRequested.isPresent()) {
            //unable to build seating fragments to accommodate request
            return null;
        }
        //have a desired collection of seats based on business logic contained here, handing off the request to the
        //eventService for final acceptance and management of the available seats.
        Optional<SeatHold> reservationOptional = this.eventService.createSeatHold(seatsRequested.get(), customerEmail, HOLD_DURATION_MINUTES);
        lockService.releaseLock(eventLock.get());
        return reservationOptional.orElse(null);
    }

    /**
     * Commit seats held for a specific customer
     *
     * @param seatHoldId    the seat hold identifier
     * @param customerEmail the email address of the customer to which the
     *                      seat hold is assigned
     * @return a reservation confirmation code
     */
    public String reserveSeats(int seatHoldId, String customerEmail) {
        return this.eventService.confirmSeatHold(seatHoldId, customerEmail).orElse(null);
    }

    public List<String> getEvents() {
        return Arrays.asList(eventService.getEventId());
    }

    public List<SeatHold> getReservations() {
        return new ArrayList<>(eventService.getAllSeatHolds());
    }

    public SeatHold getReservation(int seatHoldId, String emailAddress) {
        return eventService.getSeatHold(seatHoldId, emailAddress).orElse(null);
    }

    /* as the ticket service evolves we want the service to be responsible for the business logic
       of determining "best" and leave the eventService to manage the seat availability, and reservation logic.
     */
    protected Optional<List<SeatingFragment>> findBestSeatingFragments(int numSeats) {
        ArrayList<SeatingFragment> blocksDesired = new ArrayList<SeatingFragment>();
        if (numSeatsAvailable() < numSeats) {
            return Optional.empty();
        }
        List<SeatingFragment> availableFragments = new ArrayList<SeatingFragment>(eventService.getAvailableSeatingFragments()); //going to work on a copy, and remove seat blocks as necesssary
        int seatsStillNeeded = numSeats;

        Comparator<SeatingFragment> seatingFragmentCostComparator = (sf1, sf2) -> Double.compare(sf1.getTotalSeatCost(), sf2.getTotalSeatCost());
        while (seatsStillNeeded > 0) {
            Stream<Integer> fragmentSizes = availableFragments.stream().map(sf -> sf.getSize());
            int largestBlockSize = fragmentSizes.max(Integer::compare).orElse(0);
            if (largestBlockSize <= seatsStillNeeded) {
                List<SeatingFragment> fragmentsUnderConsideration = availableFragments.stream().filter(sf -> sf.getSize() == largestBlockSize).collect(Collectors.toList());
                Optional<SeatingFragment> bestOption = fragmentsUnderConsideration.stream().min(seatingFragmentCostComparator);
                //we can be confident that while the seats might go away, they haven't gone away from our copy of the available fragments.
                blocksDesired.add(bestOption.get());
                seatsStillNeeded -= bestOption.get().getSize();
                availableFragments.remove(bestOption.get());
            } else {
                final int lastBlockSize = seatsStillNeeded;
                Stream<SeatingFragment> fragmentsUnderConsideration = availableFragments.stream().filter(sf -> sf.getSize() >= lastBlockSize);
                SeatingFragment finalFragment = fragmentsUnderConsideration.map(seatingFragment -> seatingFragment.bestBlockFromFragment(lastBlockSize)).map(opt -> opt.get()).min(seatingFragmentCostComparator).get();
                blocksDesired.add(finalFragment);
                seatsStillNeeded -= seatsStillNeeded;
            }

        }

        return Optional.of(blocksDesired);
    }

}
