package api;

import config.TestConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class TicketApiClient {

    private static final ThreadLocal<String> LAST_REQUEST = new ThreadLocal<>();
    private static final ThreadLocal<String> LAST_RESPONSE = new ThreadLocal<>();

    public TicketApiClient() {
        RestAssured.baseURI = TestConfig.baseUrl();
    }

    public Response create(TicketApiModels.CreateTicketRequest request) {
        return record("POST /api/tickets", givenRequest()
                .body(request)
            .post("/api/tickets"));
    }

    public Response get(String incidentNumber) {
        return record("GET /api/tickets/" + incidentNumber, givenRequest().get("/api/tickets/{number}", incidentNumber));
    }

    public Response search(String query) {
        return record("GET /api/tickets?query=" + query, givenRequest().queryParam("query", query).get("/api/tickets"));
    }

    public Response searchAll() {
        return record("GET /api/tickets", givenRequest().get("/api/tickets"));
    }

    public Response update(String incidentNumber, TicketApiModels.UpdateTicketRequest request) {
        return record("PATCH /api/tickets/" + incidentNumber, givenRequest()
                .body(request)
            .patch("/api/tickets/{number}", incidentNumber));
    }

    public Response assign(String incidentNumber, TicketApiModels.AssignmentRequest request) {
        return record("POST /api/tickets/" + incidentNumber + "/assign", givenRequest()
                .body(request)
            .post("/api/tickets/{number}/assign", incidentNumber));
    }

    public Response resolve(String incidentNumber, TicketApiModels.ResolveRequest request) {
        return record("POST /api/tickets/" + incidentNumber + "/resolve", givenRequest()
                .body(request)
            .post("/api/tickets/{number}/resolve", incidentNumber));
    }

    public Response close(String incidentNumber) {
        return record("POST /api/tickets/" + incidentNumber + "/close", givenRequest().post("/api/tickets/{number}/close", incidentNumber));
    }

    public static String lastRequestContext() {
        String request = LAST_REQUEST.get();
        String response = LAST_RESPONSE.get();
        return request == null ? "" : "API request: " + request + "; response: " + response;
    }

    private Response record(String request, Response response) {
        LAST_REQUEST.set(request);
        LAST_RESPONSE.set(response.statusCode() + " " + response.statusLine());
        return response;
    }

    private io.restassured.specification.RequestSpecification givenRequest() {
        return given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }
}
