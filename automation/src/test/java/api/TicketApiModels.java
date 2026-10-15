package api;

import java.time.Instant;

public final class TicketApiModels {

    private TicketApiModels() {
    }

    public record CreateTicketRequest(String shortDescription, String description, String category, String priority) {
    }

    public record UpdateTicketRequest(
            String shortDescription,
            String description,
            String category,
            String priority,
            String status,
            String resolutionNotes) {
    }

    public record AssignmentRequest(String assignedUser, String assignedGroup) {
    }

    public record ResolveRequest(String resolutionNotes) {
    }

    public record TicketResponse(
            String incidentNumber,
            String shortDescription,
            String description,
            String category,
            String priority,
            String status,
            String assignedUser,
            String assignedGroup,
            Instant createdDate,
            Instant updatedDate,
            String resolutionNotes) {
    }
}
