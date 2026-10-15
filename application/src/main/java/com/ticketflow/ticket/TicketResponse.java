package com.ticketflow.ticket;

import java.time.Instant;

public record TicketResponse(
        String incidentNumber,
        String shortDescription,
        String description,
        String category,
        TicketPriority priority,
        TicketStatus status,
        String assignedUser,
        String assignedGroup,
        Instant createdDate,
        Instant updatedDate,
        String resolutionNotes) {

    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
                ticket.getIncidentNumber(),
                ticket.getShortDescription(),
                ticket.getDescription(),
                ticket.getCategory(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getAssignedUser(),
                ticket.getAssignedGroup(),
                ticket.getCreatedDate(),
                ticket.getUpdatedDate(),
                ticket.getResolutionNotes());
    }
}
