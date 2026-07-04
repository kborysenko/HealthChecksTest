package test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import test.UIPages.AIChatBotComponent;
import test.UIPages.BrandsPage;
import test.ui.SeleniumSetup;

import static test.UIPages.BasePage.open;

/**
 * Smoke healthchecks for the two product areas. Each check goes past "page loaded" and exercises
 * the feature the way a user would, so a green run means the feature is actually usable.
 */
@DisplayName("UI system healthcheck")
public class HealthCheckTest extends SeleniumSetup {

    @Tag("healthcheck")
    @Test
    @DisplayName("Ad Intelligence · Brands module is operational")
    void verifyAdIntelligenceBrandsPageIsOperational() {
        open(BrandsPage.class)
                .checkSideMenuIsAvailable()
                .checkReportCardsAreDisplayed()
                .checkPageHasNoBlockingErrors()
                .openFirstReport();
    }

    @Tag("healthcheck")
    @Test
    @DisplayName("AI Chatbot · \"Ask anything…\" answers a prompt")
    void verifyChatbotIsOperational() {
        open(AIChatBotComponent.class)
                .checkChatbotIsAvailable()
                .sendMessage("test")
                .waitForAssistantResponse()
                .checkPageHasNoBlockingErrors();
    }
}
