import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EnvLoader {


    public static String getFromEnv(String key) {
        try (InputStream is = ClassLoader.getSystemResourceAsStream("secrets/.env")) {
            if (is == null) {
                throw new FileNotFoundException(".env file not found in resources/secrets/.env");
            }
            Properties props = new Properties();
            props.load(is);
            String value = props.getProperty(key);
            if (value == null) {
                throw new IllegalArgumentException("Missing key in .env: " + key);
            }
            return value;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load .env: " + e.getMessage(), e);
        }
    }

    public static String getSecretKey() {
        return getFromEnv("SMTP_SECRET_KEY");
    }

    public static String getConfigPath() {
        return getFromEnv("CONFIG_PATH");
    }
}
