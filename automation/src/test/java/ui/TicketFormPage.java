package ui;

import com.microsoft.playwright.Page;

public class TicketFormPage {

    private final Page page;

    public TicketFormPage(Page page) {
        this.page = page;
    }

    public TicketFormPage fill(String shortDescription, String description, String category, String priority) {
        page.getByTestId("short-description").fill(shortDescription);
        page.getByTestId("description").fill(description);
        page.getByTestId("category").fill(category);
        page.getByTestId("priority").selectOption(priority);
        return this;
    }

    public TicketDetailsPage save() {
        page.getByTestId("save-ticket").click();
        page.getByTestId("ticket-details-view").waitFor();
        return new TicketDetailsPage(page);
    }

    public String error() {
        return page.getByTestId("form-error").textContent();
    }
}
