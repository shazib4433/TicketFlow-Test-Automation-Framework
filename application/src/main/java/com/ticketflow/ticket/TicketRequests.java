package com.ticketflow.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class TicketRequests {

    private TicketRequests() {
    }

    public record CreateTicketRequest(
            @NotBlank String shortDescription,
            @NotBlank String description,
            @NotBlank String category,
            @NotNull TicketPriority priority) {
    }

    public record UpdateTicketRequest(
            String shortDescription,
            String description,
            String category,
            TicketPriority priority,
            TicketStatus status,
            String resolutionNotes) {
    }

    public record AssignmentRequest(
            String assignedUser,
            @NotBlank String assignedGroup) {
    }

    public record ResolveRequest(@NotBlank String resolutionNotes) {
    }
}
