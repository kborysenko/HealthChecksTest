package test.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import test.UIPages.LoginPage;

import static test.UIPages.BasePage.open;

public abstract class SeleniumSetup {
    public static WebDriver driver;

    @BeforeTest
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
        Configuration.timeout = 10000;
    }

    @BeforeMethod
    @BeforeEach
    public void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        WebDriverRunner.setWebDriver(driver);

        open(LoginPage.class)
                .login("fahosev830@nriza.com", ":3I{*SK0Le.6");
    }

    @AfterMethod
    @AfterEach
    public void tearDown() {
        driver.quit();
    }
}

