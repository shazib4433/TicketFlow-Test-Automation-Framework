package concurrency;

import api.TicketApiClient;
import api.TicketApiModels;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.BoundedPoller;
import utils.TicketTestData;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AsyncAssignmentTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(100);

    @Test(groups = "concurrency")
    public void eventuallyConsistentAssignmentIsObservedByPolling() throws Exception {
        TicketApiClient client = new TicketApiClient();
        String incidentNumber = client.create(TicketTestData.createRequest())
                .then().statusCode(201).extract().path("incidentNumber");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<?> assignment = executor.submit(() -> client.assign(
                    incidentNumber, new TicketApiModels.AssignmentRequest("async-user", "service-desk"))
                    .then().statusCode(200));

            BoundedPoller.await("assignment for " + incidentNumber, TIMEOUT, POLL_INTERVAL, () ->
                    "service-desk".equals(client.get(incidentNumber).jsonPath().getString("assignedGroup")));

            assignment.get();
            Assert.assertEquals(client.get(incidentNumber).jsonPath().getString("status"), "IN_PROGRESS");
        } finally {
            executor.shutdownNow();
        }
    }

    @Test(groups = "concurrency")
    public void failedAssignmentDoesNotCreateOrCorruptATicket() {
        TicketApiClient client = new TicketApiClient();

        client.assign("INC999999", new TicketApiModels.AssignmentRequest("async-user", "service-desk"))
                .then().statusCode(404);
    }
}
