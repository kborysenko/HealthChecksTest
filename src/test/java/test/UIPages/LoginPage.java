package test.UIPages;

import com.codeborne.selenide.SelenideElement;
import test.config.TestConfig;

import java.time.Duration;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;

public class LoginPage extends BasePage {

    private final SelenideElement EMAIL_INPUT = $("#email");
    private final SelenideElement PASSWORD_INPUT = $("#password");
    private final SelenideElement LOGIN_BUTTON = $("button[type='submit']");

    public BrandsPage login(String email, String password) {
        EMAIL_INPUT.shouldBe(visible).setValue(email);
        PASSWORD_INPUT.shouldBe(visible).setValue(password);
        LOGIN_BUTTON.shouldBe(visible).click();

        $("app-sidebar").shouldBe(visible, Duration.ofSeconds(30));

        return page(BrandsPage.class); //TODO: create common page with side menu etc items that are present for all pages
    }

    @Override
    protected String createUrl() {
        return TestConfig.getUrl() + "/login";
    }

    @Override
    protected boolean isValid() {
        return true;
    }
}