package test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import test.UIPages.AIChatBotComponent;
import test.UIPages.BrandsPage;
import test.UIPages.LoginPage;
import test.annotations.SkipLogin;
import test.config.TestConfig;
import test.config.TimeoutConfig;
import test.ui.SeleniumSetup;
import test.utils.UIPerformanceTimer;

import static test.UIPages.BasePage.open;

/**
 * Smoke healthchecks for the two product areas. Each check goes past "page loaded" and exercises
 * the feature the way a user would, so a green run means the feature is actually usable.
 */
@DisplayName("UI system healthcheck")
public class UIPerformanceTest extends SeleniumSetup {

    @Tag("healthcheck")
    @Test
    @DisplayName("AI Chatbot starts generating an answer within 10 seconds")
    void verifyChatbotStartsGeneratingResponse() {

        UIPerformanceTimer timer = UIPerformanceTimer.start();

        open(AIChatBotComponent.class)
                .checkChatbotIsAvailable()
                .sendMessage("test")
                .waitForAssistantResponse();

        timer.shouldFinishWithin(TimeoutConfig.AI_CHAT_FIRST_RESPONSE, "AI Chatbot first response");
    }

    @Tag("healthcheck")
    @Test
    @SkipLogin
    @DisplayName("Login page redirects to application within 10 seconds")
    void verifyLoginRedirect() {
        UIPerformanceTimer timer = UIPerformanceTimer.start();

        open(LoginPage.class)
                .login(TestConfig.getUsername(), TestConfig.getPassword())
                .checkSideMenuIsAvailable();

        timer.shouldFinishWithin(TimeoutConfig.LOGIN_REDIRECT, "Login redirect to application");
    }

    @Tag("healthcheck")
    @Test
    @DisplayName("Brands page loads within 15 seconds")
    void verifyBrandsPageLoadsWithinTimeout() {

        UIPerformanceTimer timer = UIPerformanceTimer.start();

        open(BrandsPage.class)
                .checkSideMenuIsAvailable();

        timer.shouldFinishWithin(TimeoutConfig.BRANDS_PAGE_LOAD, "Brands page loading");
    }

    @Tag("healthcheck")
    @Test
    @DisplayName("Brands page - brands list loads within 15 seconds")
    void verifyBrandsListLoadsWithinTimeout() {
        UIPerformanceTimer timer = UIPerformanceTimer.start();

        open(BrandsPage.class)
                .checkBrandsListLoadedWithin(TimeoutConfig.BRANDS_LIST_LOAD);

        timer.shouldFinishWithin(TimeoutConfig.BRANDS_LIST_LOAD, "Brands list loading");
    }

    @Tag("healthcheck")
    @Test
    @DisplayName("AI Chatbot - full response is received within 60 seconds")
    void verifyChatbotFullResponseWithinTimeout() {
        UIPerformanceTimer timer = UIPerformanceTimer.start();

        open(AIChatBotComponent.class)
                .checkChatbotIsAvailable()
                .sendMessage("test")
                .waitForAssistantResponse()
                .checkPageHasNoBlockingErrors();

        timer.shouldFinishWithin(
                TimeoutConfig.AI_CHAT_FULL_RESPONSE,
                "AI chatbot full response"
        );
    }
}
