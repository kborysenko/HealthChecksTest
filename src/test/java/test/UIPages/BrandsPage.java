package test.UIPages;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;
import test.config.TestConfig;

import java.time.Duration;

import static com.codeborne.selenide.CollectionCondition.allMatch;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;

/**
 * Ad Intelligence — Brands module. The healthcheck confirms the feature is genuinely usable:
 * navigation is present, reports are offered as working entry points, there are no blocking
 * errors, and drilling into a report actually navigates.
 */
public class BrandsPage extends AuthenticatedPage {

    private final ElementsCollection reportCards = $$("report-card");
    private final ElementsCollection menuItems = $$("[data-userflow='sidebar-menu-item-title']");

    private static final By BRAND_SEARCH_INPUT = By.cssSelector("[data-unit='search-input']");
    private static final By BRAND_DROPDOWN_OPTIONS = By.cssSelector("mat-option");

    private static final By CHANNEL_FILTER = By.cssSelector("channel-selector [data-unit='root-button']");
    private static final By DEVICE_FILTER = By.cssSelector("device-selector button");
    private static final By PERIOD_FILTER = By.cssSelector("[data-unit='period-button']");
    private static final By COUNTRY_FILTER = By.cssSelector("[data-unit='country-button']");
    private static final By BRAND_HIERARCHY_FILTER = By.cssSelector("brand-hierarchy-toggle [data-unit='menu-trigger-button']");

    private static final String REPORT_TITLE = ".title";
    private static final String REPORT_LINK = "a.card-link";

    private static final By CLEAR_FILTER_BUTTON = By.cssSelector("[data-unit='clear-all-button']");
    private static final By SEARCH_DROPDOWN_ITEMS = By.cssSelector("[data-unit='search-popup-item']");
    private static final SelenideElement BACK_TO_BRANDS_BUTTON = $("[data-unit='report-empty-page-back-button']");

    private static final ElementsCollection REPORT_CARDS =
            $$("report-card");

    private static final String CARD_TITLE = ".title-and-date .title";
    private static final String CARD_DATE = ".title-and-date .title-info";
    private static final String CARD_LOGO = ".header .icon fav-icon";
    private static final String CARD_COUNTRY_FLAG = "[data-unit='country-flag-image']";
    private static final String CARD_DURATION = ".duration span";
    private static final String CARD_CHANNEL = ".channel .title";

    public BrandsPage checkSideMenuIsAvailable() {
        sideMenu.shouldBe(visible);

        Assertions.assertThat(menuItems.texts())
                .contains(
                        "Categories",
                        "Brands",
                        "Advertisers",
                        "Publishers",
                        "Campaigns",
                        "Keywords");

        return this;
    }

    public BrandsPage checkReportCardsAreDisplayed() {
        // At least one report must be offered
        reportCards.shouldHave(CollectionCondition.sizeGreaterThan(0));
        // and each must be a usable entry point: a titled, clickable report link.
        // (Intentionally not asserting every decorative control so incidental
        // UI changes don't false-fail the healthcheck.)
        reportCards.shouldHave(allMatch(
                "every report card is a titled, clickable report",
                card -> card.findElement(By.cssSelector(REPORT_LINK)).isDisplayed()
                        && !card.findElement(By.cssSelector(REPORT_TITLE)).getText().isBlank()));

        return this;
    }

    public BrandsPage checkPageHasNoBlockingErrors() {
        assertNoBlockingErrors();
        return this;
    }

    public BrandsPage searchBrand(String brandName) {
        $(BRAND_SEARCH_INPUT)
                .shouldBe(Condition.visible)
                .setValue(brandName);

        $$(BRAND_DROPDOWN_OPTIONS)
                .findBy(Condition.text(brandName))
                .shouldBe(Condition.visible)
                .click();

        webdriver().shouldHave(urlContaining("/ad-intelligence/brand"));

        return this;
    }

    public BrandsPage checkFiltersAreDisplayed() {
        $(CHANNEL_FILTER).shouldBe(Condition.visible);
        $(DEVICE_FILTER).shouldBe(Condition.visible);
        $(PERIOD_FILTER).shouldBe(Condition.visible);
        $(COUNTRY_FILTER).shouldBe(Condition.visible);
        $(BRAND_HIERARCHY_FILTER).shouldBe(Condition.visible);

        return this;
    }

    //TODO: add filter and validate it works

    public BrandsPage checkWidgetIsDisplayed(String widgetName) {
        $("widget-card")
                .shouldBe(Condition.visible);

        $$("widget-card .widget-title .crop-text")
                .findBy(Condition.matchText(".*" + widgetName + ".*"))
                .shouldBe(Condition.visible);

        return this;
    }

    public BrandsPage checkWidgetsAreDisplayed(String... widgetNames) {
        for (String widgetName : widgetNames) {
            $$("[data-unit='widget-title']")
                    .findBy(Condition.matchText(".*" + widgetName + ".*"))
                    .shouldBe(Condition.visible);
        }

        return this;
    }

    public BrandsPage checkBrandsListLoadedWithin(Long timeoutMillis) {
        reportCards
                .shouldHave(
                        CollectionCondition.sizeGreaterThan(0),
                        Duration.ofMillis(timeoutMillis)
                );

        return this;
    }

    public BrandsPage clearFilters() {
        $(CLEAR_FILTER_BUTTON)
                .shouldBe(Condition.visible)
                .click();

        return this;
    }

    public BrandsPage checkBrandSearchDropdownIsDisplayed() {
        $$(SEARCH_DROPDOWN_ITEMS)
                .shouldHave(CollectionCondition.sizeGreaterThan(0));

        return this;
    }

    public BrandsPage clickBackToBrandsButton() {
        BACK_TO_BRANDS_BUTTON
                .shouldBe(Condition.visible)
                .click();

        return this;
    }

    public BrandsPage checkAllReportCardsContainRequiredData() {
        REPORT_CARDS.shouldHave(CollectionCondition.sizeGreaterThan(0));

        REPORT_CARDS.forEach(card -> {
            card.$(CARD_TITLE)
                    .shouldBe(Condition.visible)
                    .shouldHave(Condition.matchText(".+"));

            card.$(CARD_DATE)
                    .shouldBe(Condition.visible)
                    .shouldHave(Condition.matchText(".+"));

            card.$(CARD_LOGO)
                    .shouldBe(Condition.visible);

            card.$(CARD_COUNTRY_FLAG)
                    .shouldBe(Condition.visible)
                    .shouldHave(Condition.attribute("src"));

            card.$(CARD_DURATION)
                    .shouldBe(Condition.visible)
                    .shouldHave(Condition.matchText(".+"));

            card.$(CARD_CHANNEL)
                    .shouldBe(Condition.visible)
                    .shouldHave(Condition.matchText(".+"));
        });

        return this;
    }

    @Override
    protected String createUrl() {
        return TestConfig.getUrl() + "/brand";
    }
}
