package test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import test.UIPages.BrandsPage;
import test.ui.SeleniumSetup;

import static test.UIPages.BasePage.open;

/**
 * Smoke healthchecks for the two product areas. Each check goes past "page loaded" and exercises
 * the feature the way a user would, so a green run means the feature is actually usable.
 */
@DisplayName("UI system healthcheck")
public class BrandsModuleTest extends SeleniumSetup {

    @Tag("healthcheck")
    @Test
    @DisplayName("Brands module is operational")
    void verifyAdIntelligenceBrandsPageIsOperational() {
        open(BrandsPage.class)
                .checkSideMenuIsAvailable()
                .checkReportCardsAreDisplayed()
                .checkPageHasNoBlockingErrors();
    }

    @Tag("healthcheck")
    @Test
    @DisplayName("Brand Report module is operational")
    void verifyAdIntelligenceBrandsReportPageIsOperational() {
        open(BrandsPage.class)
                .checkSideMenuIsAvailable()
                .checkReportCardsAreDisplayed()
                .checkPageHasNoBlockingErrors()
                .searchBrand("The Farmer’s Dog")
                .checkFiltersAreDisplayed()
                .checkWidgetIsDisplayed("Media coverage")
                .clearFilters()
                .checkBrandSearchDropdownIsDisplayed()
                .clickBackToBrandsButton()
                .checkReportCardsAreDisplayed();
    }

    @Tag("healthcheck")
    @Test
    @DisplayName("Brand Page - Brand cards contain required data")
    void verifyAdIntelligenceBrandsCardContainRequiredData() {
        open(BrandsPage.class)
                .checkSideMenuIsAvailable()
                .checkReportCardsAreDisplayed()
                .checkAllReportCardsContainRequiredData();
    }

}
