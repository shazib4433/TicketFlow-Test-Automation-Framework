package api;

import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import utils.TicketTestData;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

public class TicketApiRegressionTest {

    private TicketApiClient client;

    @BeforeClass
    public void setUp() {
        client = new TicketApiClient();
    }

    @Test(groups = "regression")
    public void updatesAssignsResolvesAndClosesTicket() {
        String incidentNumber = createTicket().jsonPath().getString("incidentNumber");

        client.update(incidentNumber, TicketTestData.updateRequest()).then()
                .statusCode(200)
                .body("shortDescription", equalTo("Updated API incident"))
                .body("priority", equalTo("P2_HIGH"));

        client.assign(incidentNumber, new TicketApiModels.AssignmentRequest("alex", "network-support")).then()
                .statusCode(200)
                .body("assignedUser", equalTo("alex"))
                .body("assignedGroup", equalTo("network-support"))
                .body("status", equalTo("IN_PROGRESS"));

        client.resolve(incidentNumber, new TicketApiModels.ResolveRequest("Service restored")).then()
                .statusCode(200)
                .body("status", equalTo("RESOLVED"))
                .body("resolutionNotes", equalTo("Service restored"));

        client.close(incidentNumber).then()
                .statusCode(200)
                .body("status", equalTo("CLOSED"));
    }

    @Test(groups = "regression")
    public void searchesByShortDescription() {
        TicketApiModels.CreateTicketRequest request = TicketTestData.createRequest();
        String incidentNumber = createTicket(request).jsonPath().getString("incidentNumber");

        client.search(request.shortDescription()).then()
                .statusCode(200)
                .body("incidentNumber", not(empty()))
                .body("incidentNumber", hasItem(incidentNumber));
    }

    @Test(groups = "regression")
    public void rejectsInvalidPayload() {
        client.create(new TicketApiModels.CreateTicketRequest("", "", "", null)).then()
                .statusCode(400);
    }

    @Test(groups = "regression")
    public void rejectsUnknownTicketNumber() {
        client.get("INC999999").then().statusCode(404);
    }

    @Test(groups = "regression")
    public void rejectsInvalidTransition() {
        String incidentNumber = createTicket().jsonPath().getString("incidentNumber");

        client.resolve(incidentNumber, new TicketApiModels.ResolveRequest("Not ready")).then()
                .statusCode(409)
                .body("error", equalTo("Invalid ticket transition from NEW to RESOLVED"));
    }

    @Test(groups = "regression")
    public void assignsUniqueIncidentNumbers() {
        String first = createTicket().jsonPath().getString("incidentNumber");
        String second = createTicket().jsonPath().getString("incidentNumber");

        org.testng.Assert.assertNotEquals(first, second, "Concurrent-safe persistence must generate unique incident numbers");
    }

    private Response createTicket() {
        return createTicket(TicketTestData.createRequest());
    }

    private Response createTicket(TicketApiModels.CreateTicketRequest request) {
        return client.create(request).then().statusCode(201).extract().response();
    }
}
