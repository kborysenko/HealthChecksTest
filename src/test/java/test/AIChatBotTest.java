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
public class AIChatBotTest extends SeleniumSetup {

    @Tag("healthcheck")
    @Test
    @DisplayName("AI Chatbot · \"Ask anything…\" answers a prompt")
    void verifyChatbotIsOperational() {
        open(AIChatBotComponent.class)
                .checkChatbotIsAvailable()
                .sendMessage("test")
                .waitForAssistantResponse()
                .waitForResponseNotValidRequest()
                .checkPageHasNoBlockingErrors();
    }

    @Tag("healthcheck")
    @Test
    @DisplayName("AI Chatbot asks for clarification for ambiguous brand query")
    void verifyChatbotAsksForClarificationForAmbiguousBrand() {
        open(AIChatBotComponent.class)
                .checkChatbotIsAvailable()
                .sendMessage("Coca Cola")
                .waitForAssistantResponse()
                .checkAssistantResponseContains("clarifications")
                .checkAssistantResponseContains("country")
                .checkPageHasNoBlockingErrors();
    }

    @Tag("healthcheck")
    @Test
    @DisplayName("AI Chatbot does not create incorrect info")
    void verifyChatbotDoesNotCreateIncorrectInfo() {
        open(AIChatBotComponent.class)
                .checkChatbotIsAvailable()
                .sendMessage("What is the CEO's favorite food?")
                .waitForAssistantResponse()
                .checkAssistantResponseContains("answer questions based on advertising data available in AdClarity")
                .checkPageHasNoBlockingErrors();
    }

}
