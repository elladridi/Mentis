package utils.z;

public final class AppConfig {
    private AppConfig() {}

    public static String get(String key, String defaultValue) {
        String env = System.getenv(key);
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        String prop = System.getProperty(key);
        if (prop != null && !prop.isBlank()) {
            return prop.trim();
        }
        return defaultValue;
    }

    public static String dbUrl() {
        return get("MENTIS_DB_URL", "jdbc:mysql://localhost:3306/Mentis");
    }

    public static String dbUser() {
        return get("MENTIS_DB_USER", "root");
    }

    public static String dbPassword() {
        return get("MENTIS_DB_PASSWORD", "");
    }

    public static String groqApiKey() {
        return get("MENTIS_GROQ_API_KEY", "");
    }

    public static String hfApiKey() {
        return get("MENTIS_HF_API_KEY", "");
    }
}
