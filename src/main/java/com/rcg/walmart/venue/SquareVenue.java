package com.rcg.walmart.venue;

import com.rcg.walmart.seating.Seat;
import com.rcg.walmart.seating.SeatingFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class SquareVenue implements Venue {
    //These fragments are the empty venue available fragments of the empty venue capturing the relative cost of each
    //seat
    private List<SeatingFragment> seatingFragments;

    @Override
    public int numSeatsAvailable() {
        int numSeats = seatingFragments.stream().map(l -> l.getSize()).reduce(0, (a, b) -> a + b);
        return numSeats;
    }

    @Override
    public List<SeatingFragment> getSeatingFragments() {
        return this.seatingFragments;
    }

    private Double linearDistanceFromIdealizedSeat(int seatRow, int seatIndex, int idealRow, int idealSeatIndex) {
        return Math.sqrt(Math.pow((seatRow - idealRow), 2) + Math.pow((seatIndex - idealSeatIndex), 2));
    }

    public SquareVenue(int rows, int seatsPerRow, int idealRow, int idealSeatIndex) {
        this.seatingFragments = new ArrayList<SeatingFragment>(rows);
        for (int seatRow = 0; seatRow < rows; seatRow++) {
            ArrayList<Seat> rowOfSeats = new ArrayList<Seat>(seatsPerRow);
            for (int seatIndex = 0; seatIndex < seatsPerRow; seatIndex++) {
                Double seatValue = linearDistanceFromIdealizedSeat(seatRow, seatIndex, idealRow, idealSeatIndex);
                Seat aSeat = new Seat(seatRow, seatIndex, seatValue);
                rowOfSeats.add(aSeat);
            }
            this.seatingFragments.add(new SeatingFragment(rowOfSeats));
        }
    }

    public Optional<Double> getSeatValue(int blockIndex, int seatIndex) {
        //getting rid of empty optionals from the list is clumsy in java 8, compared to scala
        Optional<Double> optionalSeatValue = this.seatingFragments.stream()
                .map(sf -> sf.getSeatValue(blockIndex, seatIndex))
                .flatMap(possibleAnswer -> possibleAnswer.isPresent() ? Stream.of(possibleAnswer.get()): Stream.empty()).findFirst();
        return optionalSeatValue;
    }

}
