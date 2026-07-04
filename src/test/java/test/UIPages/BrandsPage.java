package test.UIPages;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.ElementsCollection;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;
import test.config.TestConfig;

import static com.codeborne.selenide.CollectionCondition.allMatch;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;

/**
 * Ad Intelligence — Brands module. The healthcheck confirms the feature is genuinely usable:
 * navigation is present, reports are offered as working entry points, there are no blocking
 * errors, and drilling into a report actually navigates.
 */
public class BrandsPage extends AuthenticatedPage {

    private final ElementsCollection reportCards = $$("report-card");
    private final ElementsCollection menuItems = $$("[data-userflow='sidebar-menu-item-title']");

    private static final String REPORT_TITLE = ".title";
    private static final String REPORT_LINK = "a.card-link";

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

    public BrandsPage openFirstReport() {
        reportCards.first()
                .$(REPORT_LINK)
                .click();

        webdriver().shouldHave(urlContaining("/ad-intelligence/"));

        return this;
    }

    @Override
    protected String createUrl() {
        return TestConfig.getUrl() + "/brand";
    }
}
