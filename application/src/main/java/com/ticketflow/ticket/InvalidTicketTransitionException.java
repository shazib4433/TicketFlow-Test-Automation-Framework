package com.ticketflow.ticket;

public class InvalidTicketTransitionException extends RuntimeException {

    public InvalidTicketTransitionException(TicketStatus current, TicketStatus requested) {
        super("Invalid ticket transition from " + current + " to " + requested);
    }
}
