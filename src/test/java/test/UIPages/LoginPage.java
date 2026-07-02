package test.UIPages;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class LoginPage extends BasePage {

    private static final String PAGE_URL = "https://stg-ui.adcint.com/login";

    private final SelenideElement EMAIL_INPUT = $("#email");
    private final SelenideElement PASSWORD_INPUT = $("#password");
    private final SelenideElement LOGIN_BUTTON = $("button[type='submit']");

    public void login(String email, String password) {
        EMAIL_INPUT.shouldBe(visible).setValue(email);
        PASSWORD_INPUT.shouldBe(visible).setValue(password);
        LOGIN_BUTTON.shouldBe(visible).click();

    }

    @Override
    protected String createUrl() {
        return PAGE_URL;
    }

    @Override
    protected boolean isValid() {
        return true;
    }
}