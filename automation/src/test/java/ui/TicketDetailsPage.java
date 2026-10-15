package ui;

import com.microsoft.playwright.Page;

public class TicketDetailsPage {

    private final Page page;

    public TicketDetailsPage(Page page) {
        this.page = page;
    }

    public String incidentNumber() { return page.getByTestId("incident-number").textContent(); }
    public String shortDescription() { return page.getByTestId("details-short-description").inputValue(); }
    public String description() { return page.getByTestId("details-description").inputValue(); }
    public String priority() { return page.getByTestId("details-priority").inputValue(); }
    public String status() { return page.getByTestId("details-status").inputValue(); }

    public TicketDetailsPage updateDescription(String description) {
        page.getByTestId("details-description").fill(description);
        page.getByTestId("update-ticket").click();
        return this;
    }

    public TicketDetailsPage assign() { page.getByTestId("assign-ticket").click(); return this; }
    public TicketDetailsPage resolve() { page.getByTestId("resolve-ticket").click(); return this; }
    public TicketDetailsPage close() { page.getByTestId("close-ticket").click(); return this; }
}
