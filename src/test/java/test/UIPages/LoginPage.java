package test.UIPages;

import com.codeborne.selenide.SelenideElement;
import test.config.TestConfig;

import java.time.Duration;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class LoginPage extends BasePage {

    private final SelenideElement EMAIL_INPUT = $("#email");
    private final SelenideElement PASSWORD_INPUT = $("#password");
    private final SelenideElement LOGIN_BUTTON = $("button[type='submit']");

    public void login(String email, String password) {
        EMAIL_INPUT.shouldBe(visible).setValue(email);
        PASSWORD_INPUT.shouldBe(visible).setValue(password);
        LOGIN_BUTTON.shouldBe(visible).click();

        $("app-sidebar").shouldBe(visible, Duration.ofSeconds(30));
    }

    @Override
    protected String createUrl() {
        return TestConfig.getUrl();
    }

    @Override
    protected boolean isValid() {
        return true;
    }
}