package ui;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import config.TestConfig;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.nio.file.Files;
import java.nio.file.Path;

public abstract class BaseUiTest {

    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;

    @BeforeMethod(alwaysRun = true)
    public void openBrowser() {
        playwright = Playwright.create();
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setHeadless(TestConfig.headless());
        browser = playwright.chromium().launch(options);
        context = browser.newContext();
        page = context.newPage();
        page.navigate(TestConfig.baseUrl());
    }

    @AfterMethod(alwaysRun = true)
    public void closeBrowser(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE && page != null) {
            try {
                Path directory = Path.of("target", "screenshots");
                Files.createDirectories(directory);
                page.screenshot(new Page.ScreenshotOptions().setPath(directory.resolve(result.getMethod().getMethodName() + "-" + System.currentTimeMillis() + ".png")).setFullPage(true));
            } catch (Exception screenshotFailure) {
                System.err.println("Unable to capture UI failure screenshot: " + screenshotFailure.getMessage());
            }
        }
        if (context != null) context.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}
