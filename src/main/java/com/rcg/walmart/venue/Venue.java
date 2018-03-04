package com.rcg.walmart.venue;

import com.rcg.walmart.seating.SeatingFragment;

import java.util.List;

public interface Venue {
    int numSeatsAvailable();
    List<SeatingFragment> getSeatingFragments();
}
