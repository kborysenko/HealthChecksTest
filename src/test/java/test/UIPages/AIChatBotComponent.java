package test.UIPages;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class AIChatBotComponent extends BrandsPage {

    private final SelenideElement CHATBOT_INPUT = $("textarea[data-unit='textarea']");
    private final SelenideElement SEND_BUTTON = $("button[data-unit='submit-btn']");
    private final ElementsCollection ASSISTANT_MESSAGES = $$("chatbot-card[data-unit='assistant-message']");
    private final SelenideElement LAST_ASSISTANT_MESSAGE = $("chatbot-card[data-unit='assistant-message']:last-of-type");
    private final SelenideElement ASSISTANT_MARKDOWN = LAST_ASSISTANT_MESSAGE.$("[data-unit='markdown-content']");

    public AIChatBotComponent checkPageHasNoBlockingErrors() {
        SPINNER.should(disappear);
        ERROR_PAGE.shouldNot(exist);

        $("body").shouldNotHave(text("Something went wrong"));
        $("body").shouldNotHave(text("Unexpected error"));
        $("body").shouldNotHave(text("Access denied"));

        return this;
    }

    public AIChatBotComponent shouldHaveAssistantResponseReady() {

        ASSISTANT_MESSAGES.shouldHave(CollectionCondition.sizeGreaterThan(0), Duration.ofSeconds(20));
        LAST_ASSISTANT_MESSAGE.shouldBe(visible);
        ASSISTANT_MARKDOWN.shouldBe(visible)
                .shouldNotBe(empty)
                .shouldNotHave(text("Thinking"))
                .shouldNotHave(text("null"));

        return this;
    }

    public AIChatBotComponent checkChatbotIsAvailable() {

        CHATBOT_INPUT.shouldBe(visible, Duration.ofSeconds(30))
                .click();
        return this;
    }

    public AIChatBotComponent sendMessage(String message) {

        CHATBOT_INPUT.setValue(message);
        SEND_BUTTON.click();

        return this;
    }

    public AIChatBotComponent waitForAssistantResponse() {
        ASSISTANT_MESSAGES.shouldHave(
                CollectionCondition.sizeGreaterThan(0),
                Duration.ofSeconds(30)
        );

        LAST_ASSISTANT_MESSAGE.shouldBe(visible, Duration.ofSeconds(30));

        ASSISTANT_MARKDOWN.shouldBe(visible, Duration.ofSeconds(30))
                .shouldNotBe(empty)
                .shouldNotHave(text("Thinking"))
                .shouldNotHave(text("..."));

        return this;
    }

    @Override
    protected String createUrl() {
        return PAGE_URL;
    }

    @Override
    protected boolean isValid() {
        return SIDE_MENU.shouldBe(visible, Duration.ofSeconds(20)).exists();
    }
}
