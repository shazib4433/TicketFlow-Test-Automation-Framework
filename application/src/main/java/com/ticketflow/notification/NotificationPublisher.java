package com.ticketflow.notification;

public interface NotificationPublisher {

    void publish(TicketNotification notification);
}
