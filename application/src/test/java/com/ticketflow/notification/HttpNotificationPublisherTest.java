package com.ticketflow.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

class HttpNotificationPublisherTest {

    private WireMockServer wireMock;
    private HttpNotificationPublisher publisher;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(0);
        wireMock.start();
        configureFor("localhost", wireMock.port());
        publisher = new HttpNotificationPublisher(
                HttpClient.newHttpClient(),
                new ObjectMapper(),
                wireMock.baseUrl() + "/notifications");
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void publishesSuccessfulNotification() {
        stubFor(postRequest().willReturn(aResponse().withStatus(200).withBody("{\"accepted\":true}")));

        publisher.publish(notification());

        verify(postRequestedFor(urlEqualTo("/notifications"))
                .withRequestBody(equalToJson("{\"event\":\"TICKET_CREATED\",\"incidentNumber\":\"INC000001\",\"priority\":\"P2_HIGH\",\"status\":\"NEW\"}")));
    }

    @Test
    void toleratesClientAndServerFailures() {
        stubFor(postRequest().willReturn(aResponse().withStatus(400)));
        publisher.publish(notification());
        verify(postRequestedFor(urlEqualTo("/notifications")));

        wireMock.resetAll();
        stubFor(postRequest().willReturn(aResponse().withStatus(500)));
        publisher.publish(notification());
        verify(postRequestedFor(urlEqualTo("/notifications")));
    }

    @Test
    void toleratesDelayedResponseWithinFailureBoundary() {
        stubFor(postRequest().willReturn(aResponse().withStatus(200).withFixedDelay(2500)));

        publisher.publish(notification());

        verify(postRequestedFor(urlEqualTo("/notifications")));
    }

    @Test
    void toleratesMalformedResponse() {
        stubFor(postRequest().willReturn(aResponse().withStatus(200).withBody("not-json")));

        publisher.publish(notification());

        verify(postRequestedFor(urlEqualTo("/notifications")));
    }

    private TicketNotification notification() {
        return new TicketNotification("TICKET_CREATED", "INC000001", "P2_HIGH", "NEW");
    }

    private static com.github.tomakehurst.wiremock.client.MappingBuilder postRequest() {
        return com.github.tomakehurst.wiremock.client.WireMock.post(urlEqualTo("/notifications"));
    }
}
