package utils;

import api.TicketApiModels;

import java.util.UUID;

public final class TicketTestData {

    private TicketTestData() {
    }

    public static TicketApiModels.CreateTicketRequest createRequest() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return new TicketApiModels.CreateTicketRequest(
                "API incident " + suffix,
                "Created by REST Assured " + suffix,
                "Network",
                "P3_MEDIUM");
    }

    public static TicketApiModels.UpdateTicketRequest updateRequest() {
        return new TicketApiModels.UpdateTicketRequest(
                "Updated API incident",
                "Updated through the TicketFlow API",
                "Hardware",
                "P2_HIGH",
                null,
                null);
    }
}
