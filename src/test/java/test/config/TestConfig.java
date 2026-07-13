package test.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Central configuration resolver.
 *
 * <p>Values are resolved from the <b>environment first</b>, then fall back to an optional
 * classpath {@code config.properties} for local development. This single mechanism satisfies
 * both "multiple environments" (each env is a different set of env vars / --env-file) and
 * "no secrets in the repo" (credentials are injected at runtime, never committed).
 *
 * <pre>
 *   Environment variable   config.properties key   meaning
 *   APP_URL                app.url                 base URL under test
 *   APP_USERNAME           app.username            login email
 *   APP_PASSWORD           app.password            login password
 *   HEADLESS               selenide.headless       run Chrome headless (default true)
 * </pre>
 */
public final class TestConfig {

    private static final Properties FILE = new Properties();

    static {
        try (InputStream is = TestConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (is != null) {
                FILE.load(is);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read config.properties", e);
        }
    }

    private TestConfig() {
    }

    public static String getUrl() {
        return require("APP_URL", "app.url");
    }

    public static String getUsername() {
        return require("APP_USERNAME", "app.username");
    }

    public static String getPassword() {
        return require("APP_PASSWORD", "app.password");
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(resolveOrDefault("HEADLESS", "selenide.headless", "false"));
    }

    private static String require(String envKey, String fileKey) {
        String value = resolve(envKey, fileKey);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing required config '" + envKey + "'. Set the " + envKey
                            + " environment variable or the '" + fileKey + "' key in config.properties.");
        }
        return value.trim();
    }

    private static String resolve(String envKey, String fileKey) {
        String env = System.getenv(envKey);
        if (env != null && !env.isBlank()) {
            return env;
        }
        return FILE.getProperty(fileKey);
    }

    private static String resolveOrDefault(String envKey, String fileKey, String defaultValue) {
        String value = resolve(envKey, fileKey);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }
}
