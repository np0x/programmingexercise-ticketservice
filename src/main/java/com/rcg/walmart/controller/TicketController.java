package com.rcg.walmart.controller;

import com.rcg.walmart.seathold.SeatHold;
import com.rcg.walmart.ticket.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TicketController {
    @Autowired
    TicketService ticketService;

    @RequestMapping("/")
    public @ResponseBody
    String readTheReadme() {
        return "All information about the project is found in the README.md in the root of the project.\n";
    }

    @RequestMapping("/event/list")
    public List<String> getEvents() {
        return ticketService.getEvents();
    }

    @RequestMapping("/event/{eventId}/seatsAvailable")
    public int getSeatsAvailable(@PathVariable String eventId) {
        return ticketService.numSeatsAvailable();
    }

    @RequestMapping(method = RequestMethod.POST, path = "/event/{eventId}/hold/{numSeats}")
    public SeatHold createSeatHold(@PathVariable String eventId, @PathVariable int numSeats, @RequestBody String emailAddress) {
        SeatHold seatHold = ticketService.findAndHoldSeats(numSeats, emailAddress);
        if (seatHold != null) {
            return seatHold;
        } else {
            return null;
        }
    }

    @RequestMapping("/event/{eventId}/hold/listAll")
    public List<SeatHold> getAllHolds(@PathVariable String eventId) {
        return ticketService.getReservations();
    }

    @RequestMapping("/event/{eventId}/hold/{seatHoldId}")
    public SeatHold getSeatHoldDetails(@PathVariable String eventId, @PathVariable int seatHoldId, @RequestBody String emailAddress) {
        return ticketService.getReservation(seatHoldId, emailAddress);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/event/{eventId}/hold/confirm/{seatHoldId}")
    public String confirmSeatHold(@PathVariable int seatHoldId, @RequestBody String emailAddress) {
        return ticketService.reserveSeats(seatHoldId, emailAddress);
    }
}
