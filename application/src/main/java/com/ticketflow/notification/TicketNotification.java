package com.ticketflow.notification;

public record TicketNotification(String event, String incidentNumber, String priority, String status) {
}
