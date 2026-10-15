package ui;

import config.TestConfig;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TicketFlowUiTest extends BaseUiTest {

    @Test(groups = "smoke")
    public void validLoginShowsTicketList() {
        new LoginPage(page).login(TestConfig.appUsername(), TestConfig.appPassword());

        Assert.assertTrue(page.getByTestId("ticket-list-view").isVisible());
    }

    @Test(groups = "regression")
    public void invalidLoginShowsValidationMessage() {
        LoginPage loginPage = new LoginPage(page).login("", "");

        Assert.assertTrue(loginPage.error().contains("required"));
    }

    @Test(groups = "smoke")
    public void createsTicketAndDisplaysDetails() {
        new LoginPage(page).login(TestConfig.appUsername(), TestConfig.appPassword());
        TicketDetailsPage details = new TicketListPage(page)
                .startCreate()
                .fill("Laptop access issue", "User cannot access laptop resources", "Hardware", "P2_HIGH")
                .save();

        Assert.assertTrue(details.incidentNumber().matches("INC\\d{6}"));
        Assert.assertEquals(details.priority(), "P2_HIGH");
        Assert.assertEquals(details.status(), "NEW");
    }

    @Test(groups = "regression")
    public void mandatoryFieldsPreventSubmission() {
        new LoginPage(page).login(TestConfig.appUsername(), TestConfig.appPassword());
        TicketFormPage form = new TicketListPage(page).startCreate();
        form.save();

        Assert.assertTrue(form.error().isEmpty());
        Assert.assertTrue(page.getByTestId("ticket-form-view").isVisible());
    }

    @Test(groups = "regression")
    public void updatesAssignsResolvesAndClosesTicket() {
        new LoginPage(page).login(TestConfig.appUsername(), TestConfig.appPassword());
        TicketDetailsPage details = new TicketListPage(page)
                .startCreate()
                .fill("Printer issue", "Printer is offline", "Hardware", "P3_MEDIUM")
                .save();

        details.assign();
        details.updateDescription("Printer has been replaced");
        Assert.assertEquals(details.description(), "Printer has been replaced");
        details.resolve();
        Assert.assertEquals(details.status(), "RESOLVED");
        details.close();
        Assert.assertEquals(details.status(), "CLOSED");
    }
}
