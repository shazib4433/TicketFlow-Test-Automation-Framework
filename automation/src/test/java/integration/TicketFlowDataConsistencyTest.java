package integration;

import api.TicketApiClient;
import api.TicketApiModels;
import config.TestConfig;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ui.BaseUiTest;
import ui.LoginPage;
import ui.TicketDetailsPage;
import ui.TicketListPage;
import utils.TicketDatabaseClient;
import utils.TicketTestData;

public class TicketFlowDataConsistencyTest extends BaseUiTest {

    private TicketApiClient apiClient;~
        private TicketDatabaseClient databaseClient;

        @BeforeClass
        public void setUpClients() {
                apiClient = new TicketApiClient();
                databaseClient = new TicketDatabaseClient();
        }

        @Test(groups = "integration")
        public void uiCreateApiReadDatabaseVerifyApiUpdateUiRefresh() {
                new LoginPage(page).login(TestConfig.appUsername(), TestConfig.appPassword());
                TicketDetailsPage created = new TicketListPage(page)
                                .startCreate()
                                .fill("Integration laptop issue", "Integration test description", "Hardware", "P2_HIGH")
                                .save();
                String incidentNumber = created.incidentNumber();

                TicketApiModels.TicketResponse apiTicket = apiClient.get(incidentNumber)
                                .then().statusCode(200).extract().as(TicketApiModels.TicketResponse.class);
                Assert.assertEquals(apiTicket.incidentNumber(), incidentNumber);
                Assert.assertEquals(apiTicket.description(), "Integration test description");
                Assert.assertEquals(apiTicket.category(), "Hardware");
                Assert.assertEquals(apiTicket.priority(), "P2_HIGH");
                Assert.assertEquals(apiTicket.status(), "NEW");

                TicketDatabaseClient.TicketDatabaseRecord databaseTicket = databaseClient
                                .findByIncidentNumber(incidentNumber);
                Assert.assertEquals(databaseTicket.incidentNumber(), incidentNumber);
                Assert.assertEquals(databaseTicket.description(), apiTicket.description());
                Assert.assertEquals(databaseTicket.category(), apiTicket.category());
                Assert.assertEquals(databaseTicket.priority(), apiTicket.priority());
                Assert.assertEquals(databaseTicket.status(), apiTicket.status());

                apiClient.update(incidentNumber, new TicketApiModels.UpdateTicketRequest(
                                null, "Updated through API integration test", null, null, null, null))
                                .then().statusCode(200);

                page.reload();
                TicketDetailsPage refreshed = new TicketListPage(page)
                                .search(incidentNumber)
                                .openTicket(incidentNumber);
                Assert.assertEquals(refreshed.description(), "Updated through API integration test");
        }

        @Test(groups = "integration")
        public void apiCreateIsVisibleInUi() {
                TicketApiModels.CreateTicketRequest request = TicketTestData.createRequest();
                String incidentNumber = apiClient.create(request)
                                .then().statusCode(201).extract().path("incidentNumber");

                new LoginPage(page).login(TestConfig.appUsername(), TestConfig.appPassword());
                TicketDetailsPage details = new TicketListPage(page)
                                .search(incidentNumber)
                                .openTicket(incidentNumber);

                Assert.assertEquals(details.incidentNumber(), incidentNumber);
                Assert.assertEquals(details.description(), request.description());
                Assert.assertEquals(details.priority(), request.priority());
                Assert.assertEquals(details.status(), "NEW");
        }
}
