package com.ticketflow.ticket;

public class TicketNotFoundException extends RuntimeException {

    public TicketNotFoundException(String incidentNumber) {
        super("Ticket not found: " + incidentNumber);
    }
}
