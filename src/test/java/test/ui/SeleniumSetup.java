package test.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.chrome.ChromeOptions;
import test.UIPages.LoginPage;
import test.config.TestConfig;

import static test.UIPages.BasePage.open;

/**
 * Shared lifecycle for every healthcheck.
 *
 * <p>Selenide owns the WebDriver (Selenium Manager resolves the matching driver binary), so there
 * is no manual driver construction here. Each test runs in a fresh, freshly-authenticated browser
 * for isolation and determinism — a healthcheck must not carry state between checks.
 */
@ExtendWith(ScreenshotOnFailure.class)
public abstract class SeleniumSetup {

    @BeforeAll
    static void configureSelenide() {
        Configuration.browser = "chrome";
        Configuration.headless = TestConfig.isHeadless();
        Configuration.timeout = 10_000;            // global wait for conditions without an explicit Duration
        Configuration.pageLoadTimeout = 30_000;
        Configuration.browserSize = "1920x1080";
        Configuration.reportsFolder = "target/screenshots";

        ChromeOptions options = new ChromeOptions();
        // Flags required to run headless Chrome inside a container / as root.
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu");
        Configuration.browserCapabilities = options;
    }

    @BeforeEach
    void logIn() {
        open(LoginPage.class)
                .login(TestConfig.getUsername(), TestConfig.getPassword());
    }

    @AfterEach
    void closeBrowser() {
        Selenide.closeWebDriver();
    }
}
