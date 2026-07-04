package test.ui;

import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.LifecycleMethodExecutionExceptionHandler;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Captures a screenshot and page source on failure — whether the failure happens inside the test
 * body ({@link AfterTestExecutionCallback}) or during {@code @BeforeEach} setup such as login
 * ({@link LifecycleMethodExecutionExceptionHandler}). "Can't even log in" is a first-class
 * healthcheck failure, so it must produce an artifact too.
 *
 * <p>Both hooks run while the browser is still open, before teardown. Artifacts are attached to the
 * Allure report and written to {@code target/screenshots} so CI can upload them.
 */
public class ScreenshotOnFailure
        implements AfterTestExecutionCallback, LifecycleMethodExecutionExceptionHandler {

    private static final String SCREENSHOT_DIR = "target/screenshots";

    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getExecutionException().isPresent()) {
            capture(context);
        }
    }

    @Override
    public void handleBeforeEachMethodExecutionException(ExtensionContext context, Throwable ex) throws Throwable {
        capture(context);
        throw ex; // preserve the original failure
    }

    private void capture(ExtensionContext context) {
        if (!WebDriverRunner.hasWebDriverStarted()) {
            return;
        }
        String name = context.getDisplayName().replaceAll("[^a-zA-Z0-9-_]", "_");
        try {
            byte[] png = ((TakesScreenshot) WebDriverRunner.getWebDriver()).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment("screenshot-" + name, "image/png", new ByteArrayInputStream(png), "png");
            writeFile(SCREENSHOT_DIR + "/" + name + ".png", png);

            byte[] html = WebDriverRunner.getWebDriver().getPageSource().getBytes(StandardCharsets.UTF_8);
            Allure.addAttachment("page-source-" + name, "text/html", new ByteArrayInputStream(html), "html");
        } catch (Exception e) {
            // Never let artifact capture mask the original failure.
            System.err.println("Could not capture failure artifacts: " + e.getMessage());
        }
    }

    private void writeFile(String path, byte[] bytes) throws IOException {
        Path target = Path.of(path);
        Files.createDirectories(target.getParent());
        Files.write(target, bytes);
    }
}
