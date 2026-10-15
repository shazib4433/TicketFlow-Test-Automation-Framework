package ui;

import com.microsoft.playwright.Page;

public class TicketListPage {

    private final Page page;

    public TicketListPage(Page page) {
        this.page = page;
    }

    public TicketListPage search(String query) {
        page.getByTestId("search-input").fill(query);
        page.getByTestId("search-submit").click();
        return this;
    }

    public TicketFormPage startCreate() {
        page.getByTestId("create-ticket").click();
        return new TicketFormPage(page);
    }

    public TicketDetailsPage openTicket(String incidentNumber) {
        page.locator("[data-test-id='ticket-row'][data-number='" + incidentNumber + "']").click();
        return new TicketDetailsPage(page);
    }

    public int ticketCount() {
        return page.getByTestId("ticket-row").count();
    }
}
