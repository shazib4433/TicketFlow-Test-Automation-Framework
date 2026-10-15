package config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.Properties;

/**
 * Resolution order: JVM system property, environment variable
 * (UPPER_SNAKE_CASE), test.properties.
 */
public final class TestConfig {

    private static final Properties DEFAULTS = loadDefaults();

    private TestConfig() {
    }

    public static String get(String key) {
        String value = System.getProperty(key);
        if (isBlank(value)) {
            value = System.getenv(toEnvName(key));
        }
        if (isBlank(value)) {
            value = DEFAULTS.getProperty(key);
        }
        if (isBlank(value)) {
            throw new IllegalStateException("Missing required configuration: " + key + " (env " + toEnvName(key) + ")");
        }
        return value.trim();
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    public static String baseUrl() {
        return get("base.url");
    }

    public static String dbUrl() {
        return get("db.url");
    }

    public static String dbUsername() {
        return get("db.username");
    }

    public static String dbPassword() {
        return get("db.password");
    }

    public static String appUsername() {
        return get("app.username");
    }

    public static String appPassword() {
        return get("app.password");
    }

    public static String wiremockUrl() {
        return get("wiremock.url");
    }

    public static boolean headless() {
        return getBoolean("headless");
    }

    private static String toEnvName(String key) {
        return key.replace('.', '_').toUpperCase(Locale.ROOT);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static Properties loadDefaults() {
        Properties properties = new Properties();
        try (InputStream in = TestConfig.class.getClassLoader().getResourceAsStream("test.properties")) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to load test.properties", e);
        }
        return properties;
    }
}
