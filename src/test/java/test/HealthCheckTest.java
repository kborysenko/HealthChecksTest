package test;

import org.junit.jupiter.api.Tag;

import test.UIPages.AIChatBotPage;
import test.UIPages.BrandsPage;
import test.ui.SeleniumSetup;
import org.junit.jupiter.api.Test;

import static test.UIPages.BasePage.open;

public class HealthCheckTest extends SeleniumSetup {

    @Tag("healthcheck")
    @Test
    void verifyAdIntelligenceBrandsPageIsOperational() throws InterruptedException {
        Thread.sleep(10000);
        open(BrandsPage.class)
                .checkSideMenuIsAvailable()
                .checkReportCardsAreDisplayed()
                .checkPageHasNoBlockingErrors()
                .openFirstReport();
    }

    @Tag("healthcheck")
    @Test
    void verifyChatbotIsOperational() throws InterruptedException {
        Thread.sleep(10000);
        open(AIChatBotPage.class)
                .checkChatbotIsAvailable()
                .sendMessage("test")
                .waitForAssistantResponse()
                .shouldHaveAssistantResponseReady()
                .checkPageHasNoBlockingErrors();
    }
}
