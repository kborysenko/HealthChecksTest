package test.UIPages;

import com.codeborne.selenide.SelenideElement;

import java.time.Duration;
import java.util.List;

import static com.codeborne.selenide.Condition.disappear;
import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

/**
 * Base for any page behind login. Holds the shared app-shell elements (sidebar, loading spinner,
 * error page) and the blocking-error check that every authenticated view needs, so individual
 * pages describe only what is unique to them.
 *
 * <p>A visible sidebar is treated as the signal that the authenticated shell has rendered.
 */
public abstract class AuthenticatedPage extends BasePage {

    protected final SelenideElement sideMenu = $("app-sidebar");
    protected final SelenideElement spinner = $(".loading-spinner");
    protected final SelenideElement errorPage = $("app-error-page");

    private static final List<String> BLOCKING_ERROR_TEXTS = List.of(
            "Something went wrong",
            "Unexpected error",
            "Access denied");


    protected void assertNoBlockingErrors() {
        spinner.should(disappear);
        errorPage.shouldNot(exist);
        for (String errorText : BLOCKING_ERROR_TEXTS) {
            $("body").shouldNotHave(text(errorText));
        }
    }

    @Override
    protected boolean isValid() {
        try {
            sideMenu.shouldBe(visible, Duration.ofSeconds(20));
            return true;
        } catch (Throwable notLoaded) {
            return false;
        }
    }
}
