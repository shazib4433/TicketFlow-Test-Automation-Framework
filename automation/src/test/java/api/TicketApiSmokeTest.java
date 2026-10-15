package api;

import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import utils.TicketTestData;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.Matchers.notNullValue;

public class TicketApiSmokeTest {

    private TicketApiClient client;

    @BeforeClass
    public void setUp() {
        client = new TicketApiClient();
    }

    @Test(groups = "smoke")
    public void createsAndRetrievesTicket() {
        TicketApiModels.CreateTicketRequest request = TicketTestData.createRequest();
        Response createResponse = client.create(request);

        createResponse.then()
                .statusCode(201)
                .header("Content-Type", matchesRegex("application/json.*"))
                .body("incidentNumber", matchesRegex("INC\\d{6}"))
                .body("shortDescription", equalTo(request.shortDescription()))
                .body("description", equalTo(request.description()))
                .body("category", equalTo(request.category()))
                .body("priority", equalTo(request.priority()))
                .body("status", equalTo("NEW"))
                .body("createdDate", notNullValue())
                .body("updatedDate", notNullValue());

        String incidentNumber = createResponse.jsonPath().getString("incidentNumber");
        client.get(incidentNumber).then()
                .statusCode(200)
                .body("incidentNumber", equalTo(incidentNumber));
    }
}
