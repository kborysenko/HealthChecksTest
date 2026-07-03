package test.UIPages;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;

import java.time.Duration;

import static com.codeborne.selenide.CollectionCondition.allMatch;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;

public class BrandsPage extends BasePage {

    final String PAGE_URL = "https://stg-ui.adcint.com/ad-intelligence/brand";

    private final ElementsCollection REPORT_CARDS = $$("report-card");
    final SelenideElement SIDE_MENU = $("app-sidebar");
    private final ElementsCollection MENU_ITEMS = $$("[data-userflow='sidebar-menu-item-title']");

    private static final String REPORT_TITLE = ".title";
    private static final String REPORT_LINK = "a.card-link";
    private static final String SHARE_BUTTON = ".btn-share";
    private static final String SAVE_BUTTON = ".btn-save";
    private static final String COUNTRY_FLAG = "img.country";

    final SelenideElement ERROR_PAGE = $("app-error-page");
    final SelenideElement SPINNER = $(".loading-spinner");

    public BrandsPage checkReportCardsAreDisplayed() {
        REPORT_CARDS.shouldHave(CollectionCondition.sizeGreaterThan(0));

        REPORT_CARDS.shouldHave(allMatch(
                "Every report card is complete",
                card ->
                        card.findElement(By.cssSelector(REPORT_LINK)).isDisplayed()
                                && !card.findElement(By.cssSelector(REPORT_TITLE)).getText().isBlank()
                                && card.findElement(By.cssSelector(SHARE_BUTTON)).isDisplayed()
                                && card.findElement(By.cssSelector(SAVE_BUTTON)).isDisplayed()
                                && card.findElement(By.cssSelector(COUNTRY_FLAG)).isDisplayed()
        ));

        return this;
    }

    public BrandsPage checkSideMenuIsAvailable() {
        SIDE_MENU.shouldBe(visible);

        Assertions.assertThat(
                        MENU_ITEMS.texts())
                .contains(
                        "Categories",
                        "Brands",
                        "Advertisers",
                        "Publishers",
                        "Campaigns",
                        "Keywords");

        return this;
    }

    public BrandsPage checkPageHasNoBlockingErrors() {

        SPINNER.should(disappear);

        ERROR_PAGE.shouldNot(exist);

        $("body").shouldNotHave(text("Something went wrong"));
        $("body").shouldNotHave(text("Unexpected error"));
        $("body").shouldNotHave(text("Access denied"));

        return this;
    }

    public BrandsPage openFirstReport() {

        REPORT_CARDS.first()
                .$("a.card-link")
                .click();

        webdriver().shouldHave(urlContaining("/ad-intelligence/"));

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
