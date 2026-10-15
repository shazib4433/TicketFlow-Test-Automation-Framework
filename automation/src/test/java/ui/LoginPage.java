package ui;

import com.microsoft.playwright.Page;

public class LoginPage {

    private final Page page;

    public LoginPage(Page page) {
        this.page = page;
    }

    public LoginPage login(String username, String password) {
        page.getByTestId("username").fill(username);
        page.getByTestId("password").fill(password);
        page.getByTestId("login-submit").click();
        return this;
    }

    public String error() {
        return page.getByTestId("login-error").textContent();
    }
}
