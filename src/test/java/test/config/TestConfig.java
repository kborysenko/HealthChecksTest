package test.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestConfig {
    private static final Properties properties = new Properties();

    static {
        try (InputStream is = TestConfig.class.getClassLoader()
                .getResourceAsStream("config.properties")) {

            properties.load(is);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getUrl() {
        return properties.getProperty("app.url");
    }

    public static String getUsername() {
        return properties.getProperty("app.username");
    }

    public static String getPassword() {
        return properties.getProperty("app.password");
    }
}
