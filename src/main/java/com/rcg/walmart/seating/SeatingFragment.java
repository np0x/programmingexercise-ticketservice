package com.rcg.walmart.seating;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Optional;

public class SeatingFragment {
    private List<Seat> contiguousSeats;
    private int blockIndex;
    private int startIndex;
    private int endIndex;
    private double totalSeatCost;

    public SeatingFragment(List<Seat> contiguousSeats) throws IllegalArgumentException {
        if (contiguousSeats.size() == 0) {
            throw new IllegalArgumentException("empty seat list");
        }
        //sanity check block identifiers
        List<Integer> blockIdentifiers = contiguousSeats.stream().map(Seat::getBlockNumber).distinct().collect(Collectors.toList());
        if (blockIdentifiers.size() > 1) {
            throw new IllegalArgumentException("more than one block identifier found");
        }

        int currIndex = contiguousSeats.get(0).getSeatNumber();
        this.totalSeatCost = contiguousSeats.get(0).getSeatValue();
        for (int i = 1; i < contiguousSeats.size(); i++) {
            this.totalSeatCost += contiguousSeats.get(i).getSeatValue();
            int nextIndex = contiguousSeats.get(i).getSeatNumber();
            if (nextIndex - currIndex != 1) {
                throw new IllegalArgumentException("indexes are not contiguous, went from: " + currIndex + " -> " + nextIndex);
            }
            currIndex = nextIndex;
        }
        this.contiguousSeats = contiguousSeats;
        this.startIndex = contiguousSeats.get(0).getSeatNumber();
        this.endIndex = contiguousSeats.get(contiguousSeats.size() - 1).getSeatNumber();
        this.blockIndex = contiguousSeats.get(0).getBlockNumber();
        //a little work up front makes comparison/containment/contiguous tests much easier to reason about and saves on traversals.
    }

    public boolean contiguousWith(SeatingFragment anotherFragment) {
        if (this.blockIndex == anotherFragment.getBlockIndex()) {
            return (this.endIndex + 1 == anotherFragment.getStartIndex()) ||
                    (anotherFragment.getEndIndex() + 1 == this.startIndex);
        } else {
            return false;
        }
    }

    public boolean containsAnother(SeatingFragment anotherFragment) {
        return this.blockIndex == anotherFragment.getBlockIndex() &&
                this.startIndex <= anotherFragment.getStartIndex() &&
                this.endIndex >= anotherFragment.getEndIndex();
    }

    public Optional<List<SeatingFragment>> minusAnother(SeatingFragment anotherFragment) {
        if (this.containsAnother(anotherFragment)) {
            ArrayList<SeatingFragment> result = new ArrayList<SeatingFragment>();
            if (this.startIndex == anotherFragment.startIndex && this.endIndex == anotherFragment.endIndex) {
                //full block == no seating fragment after removal of the sub fragment
                return Optional.of(Collections.emptyList());
            }
            List<List<Seat>> blocksToCreateFragmentsFrom = new ArrayList<List<Seat>>();
            List<Seat> anotherFragmentsSeats = anotherFragment.getSeats();
            int startIndexSubFragment = this.getSeats().indexOf(anotherFragmentsSeats.get(0));
            int endIndexSubFragment = this.getSeats().indexOf(anotherFragmentsSeats.get(anotherFragmentsSeats.size()-1));
            if (startIndexSubFragment != 0) {
                blocksToCreateFragmentsFrom.add(new ArrayList<Seat>(this.getSeats().subList(0, startIndexSubFragment)));
            }
            if (endIndexSubFragment != this.getSeats().size()-1) {
                blocksToCreateFragmentsFrom.add(new ArrayList<Seat>(this.getSeats().subList(endIndexSubFragment+1, this.getSeats().size())));
            }
            for (List<Seat> seatBlock : blocksToCreateFragmentsFrom) {
                result.add(new SeatingFragment(seatBlock));
            }

//            List<Seat> remainingSeats = new ArrayList(this.getSeats());
//            remainingSeats.removeAll(anotherFragment.getSeats());
//            result.add(new SeatingFragment(remainingSeats));
            return Optional.of(result);
        } else {
            return Optional.empty();
        }
    }

    public SeatingFragment mergeWithBlock(SeatingFragment anotherFragment) {
        if (this.contiguousWith(anotherFragment) == false) {
            throw new IllegalArgumentException("blocks are not contiguous");
        }
        Stream<Seat> s1 = this.contiguousSeats.stream();
        Stream<Seat> s2 = anotherFragment.contiguousSeats.stream();
        Stream<Seat> combined = Stream.concat(s1, s2);
        List<Seat> combinedSeats = combined.collect(Collectors.toList());
        Collections.sort(combinedSeats, new Comparator<Seat>() {
            @Override
            public int compare(Seat s1, Seat s2) {
                return s1.getSeatNumber().compareTo(s2.getSeatNumber());
            }
        });
        return new SeatingFragment(combinedSeats);
    }

    private double seatValue(List<Seat> seats) {
        return seats.stream().map(Seat::getSeatValue).reduce(0.0, (v1, v2) -> v1 + v2);
    }

    public Optional<SeatingFragment> bestBlockFromFragment(int numSeats) {
        if (getSize() < numSeats) {
            //impossible
            return Optional.empty();
        } else if (getSize() == numSeats) {
            //one option, full fragment
            return Optional.of(new SeatingFragment(this.getSeats()));
        } else {
            int s = 0;
            double bestValue = seatValue(getSeats().subList(s,s+numSeats));
            for(int i = 0 ; i <= getSize() - numSeats ; i++) {
                double currValue = seatValue(getSeats().subList(i, i+numSeats));
                if (currValue <= bestValue) {
                    bestValue = currValue;
                    s = i;
                }
            }
            return Optional.of(new SeatingFragment(getSeats().subList(s,s+numSeats)));
        }
    }

    public double getTotalSeatCost() {
        return this.totalSeatCost;
    }

    public List<Seat> getSeats() {
        return this.contiguousSeats;
    }

    public int getBlockIndex() {
        return this.blockIndex;
    }

    public int getStartIndex() {
        return this.startIndex;
    }

    public int getEndIndex() {
        return this.endIndex;
    }

    public int getSize() {
        return this.contiguousSeats.size();
    }

    public Optional<Double> getSeatValue(int blockIndex, int seatIndex) {
        if (this.blockIndex != blockIndex) { return Optional.empty(); }
        else {
            return this.contiguousSeats.stream().filter( s -> s.getSeatNumber() == seatIndex).findFirst().map(s -> s.getSeatValue());
        }
    }

    @Override
    public String toString() {
        return "SeatingFragment{" +
                "blockIndex=" + blockIndex +
                ", startIndex=" + startIndex +
                ", endIndex=" + endIndex +
                '}';
    }
}
