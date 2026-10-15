package concurrency;

import api.TicketApiClient;
import api.TicketApiModels;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.TicketTestData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ConcurrentTicketTest {

    @Test(groups = "concurrency")
    public void concurrentUsersReceiveUniqueTicketNumbers() throws Exception {
        int userCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(userCount);
        try {
            List<Callable<String>> tasks = new ArrayList<>();
            for (int index = 0; index < userCount; index++) {
                tasks.add(() -> new TicketApiClient().create(TicketTestData.createRequest())
                        .then().statusCode(201).extract().path("incidentNumber"));
            }
            Set<String> incidentNumbers = ConcurrentHashMap.newKeySet();
            for (Future<String> result : executor.invokeAll(tasks)) {
                incidentNumbers.add(result.get());
            }
            Assert.assertEquals(incidentNumbers.size(), userCount, "Concurrent creates must be unique");
        } finally {
            executor.shutdownNow();
        }
    }

    @Test(groups = "concurrency")
    public void conflictingUpdatesLeaveTicketInAValidState() throws Exception {
        TicketApiClient setupClient = new TicketApiClient();
        String incidentNumber = setupClient.create(TicketTestData.createRequest())
                .then().statusCode(201).extract().path("incidentNumber");
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Callable<Integer> firstUpdate = () -> update(incidentNumber, "First concurrent update");
            Callable<Integer> secondUpdate = () -> update(incidentNumber, "Second concurrent update");
            List<Future<Integer>> results = executor.invokeAll(List.of(firstUpdate, secondUpdate));

                Set<Integer> statuses = Set.of(results.get(0).get(), results.get(1).get());
                Assert.assertTrue(statuses.contains(200), "One update must succeed");
                Assert.assertTrue(statuses.stream().allMatch(status -> status == 200 || status == 409),
                    "Conflicting updates must succeed or return 409");
            TicketApiModels.TicketResponse finalTicket = setupClient.get(incidentNumber)
                    .then().statusCode(200).extract().as(TicketApiModels.TicketResponse.class);
            Assert.assertTrue(finalTicket.description().equals("First concurrent update")
                    || finalTicket.description().equals("Second concurrent update"));
        } finally {
            executor.shutdownNow();
        }
    }

    private int update(String incidentNumber, String description) {
        return new TicketApiClient().update(incidentNumber, new TicketApiModels.UpdateTicketRequest(
                null, description, null, null, null, null)).then().extract().statusCode();
    }
}
