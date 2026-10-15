package com.ticketflow.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;
import java.time.Duration;

@Component
public class HttpNotificationPublisher implements NotificationPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpNotificationPublisher.class);
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String notificationUrl;

    public HttpNotificationPublisher(
            ObjectMapper objectMapper,
            @Value("${notification.url:http://localhost:8089/notifications}") String notificationUrl) {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build(), objectMapper, notificationUrl);
    }

    HttpNotificationPublisher(HttpClient httpClient, ObjectMapper objectMapper, String notificationUrl) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.notificationUrl = notificationUrl;
    }

    @Override
    public void publish(TicketNotification notification) {
        try {
            String payload = objectMapper.writeValueAsString(notification);
            HttpRequest request = HttpRequest.newBuilder(URI.create(notificationUrl))
                    .timeout(Duration.ofSeconds(2))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                LOGGER.warn("Notification service returned status {} for event {} and ticket {}",
                        response.statusCode(), notification.event(), notification.incidentNumber());
            }
        } catch (IOException | InterruptedException | RuntimeException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.warn("Notification service unavailable for event {} and ticket {}",
                    notification.event(), notification.incidentNumber(), exception);
        }
    }
}
