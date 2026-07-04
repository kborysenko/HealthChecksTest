package test.UIPages;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import test.config.TestConfig;

import java.time.Duration;

import static com.codeborne.selenide.Condition.empty;
import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

/**
 * AI Chatbot — the "Ask anything…" assistant, which is surfaced on the Brands view. The
 * healthcheck confirms end-to-end functionality: the input is usable, a prompt can be sent,
 * and a real (non-placeholder) assistant answer renders back.
 */
public class AIChatBotComponent extends AuthenticatedPage {

    private final SelenideElement chatbotInput = $("textarea[data-unit='textarea']");
    private final SelenideElement sendButton = $("button[data-unit='submit-btn']");
    private final ElementsCollection assistantMessages = $$("chatbot-card[data-unit='assistant-message']");
    private final SelenideElement lastAssistantMessage = $("chatbot-card[data-unit='assistant-message']:last-of-type");
    private final SelenideElement assistantMarkdown = lastAssistantMessage.$("[data-unit='markdown-content']");

    public AIChatBotComponent checkChatbotIsAvailable() {
        chatbotInput.shouldBe(visible, Duration.ofSeconds(30)).click();
        return this;
    }

    public AIChatBotComponent sendMessage(String message) {
        chatbotInput.setValue(message);
        sendButton.shouldBe(enabled).click();
        return this;
    }

    public AIChatBotComponent waitForAssistantResponse() {
        assistantMessages.shouldHave(CollectionCondition.sizeGreaterThan(0), Duration.ofSeconds(30));
        lastAssistantMessage.shouldBe(visible, Duration.ofSeconds(30));
        assistantMarkdown.shouldBe(visible, Duration.ofSeconds(30))
                .shouldNotBe(empty)
                .shouldNotHave(text("Thinking"))
                .shouldNotHave(text("..."));
        return this;
    }

    public AIChatBotComponent checkPageHasNoBlockingErrors() {
        assertNoBlockingErrors();
        return this;
    }

    @Override
    protected String createUrl() {
        return TestConfig.getUrl() + "/brand";
    }
}
