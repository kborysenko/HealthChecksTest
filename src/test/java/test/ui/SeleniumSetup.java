package test.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.chrome.ChromeOptions;
import test.annotations.SkipLogin;
import test.api.AuthApi;
import test.api.AuthSession;
import test.config.TestConfig;
import org.junit.jupiter.api.TestInfo;

/**
 * Shared lifecycle for every healthcheck.
 *
 * <p>Selenide owns the WebDriver (Selenium Manager resolves the matching driver binary), so there
 * is no manual driver construction here. Each test runs in a fresh, freshly-authenticated browser
 * for isolation and determinism — a healthcheck must not carry state between checks.
 */
@ExtendWith(ScreenshotOnFailure.class)
public abstract class SeleniumSetup {

    protected AuthSession authSession;

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

    protected String jwtToken;

    @BeforeEach
    void logIn(TestInfo testInfo) {

        if (testInfo.getTestMethod()
                .map(method -> method.isAnnotationPresent(SkipLogin.class))
                .orElse(false)) {
            return;
        }

        authSession = new AuthApi().authenticate();
        openApplicationWithAuth();
    }

    private void openApplicationWithAuth() {
        Selenide.open(TestConfig.getUrl());

        Selenide.webdriver()
                .driver()
                .getWebDriver()
                .manage()
                .addCookie(
                        new Cookie(
                                "sigma_authorization",
                                authSession.getSigmaAuthorizationCookie(),
                                "stg-ui.adcint.com",
                                "/",
                                null
                        )
                );

        Selenide.refresh();
    }

    @AfterEach
    void closeBrowser() {
        Selenide.closeWebDriver();
    }
}
