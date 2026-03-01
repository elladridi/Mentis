package services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;

public class TranslationService {

    private boolean useRealAPI = true;
    private HttpClient httpClient;

    public TranslationService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public String translate(String text, String targetLanguage) {
        if (targetLanguage.equals("English") || targetLanguage.equals("en")) {
            return text;
        }

        String langCode = getLanguageCode(targetLanguage);

        try {
            // Using free LibreTranslate API (no key needed!)
            String url = "https://libretranslate.com/translate";

            // Create JSON request body
            String jsonBody = String.format(
                    "{\"q\":\"%s\",\"source\":\"en\",\"target\":\"%s\",\"format\":\"text\"}",
                    escapeJson(text), langCode
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Parse JSON response
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                String translated = json.get("translatedText").getAsString();
                return translated;
            } else {
                System.out.println("Translation API returned: " + response.statusCode());
                return simulateTranslation(text, targetLanguage);
            }

        } catch (Exception e) {
            System.out.println("Translation error: " + e.getMessage());
            return simulateTranslation(text, targetLanguage);
        }
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private String simulateTranslation(String text, String language) {
        // Simple word replacement for common languages
        if (language.equals("French")) {
            return text.replace("Session:", "Séance:")
                    .replace("Date:", "Date:")
                    .replace("Time:", "Heure:")
                    .replace("Location:", "Emplacement:")
                    .replace("Type:", "Type:");
        }
        if (language.equals("Spanish")) {
            return text.replace("Session:", "Sesión:")
                    .replace("Date:", "Fecha:")
                    .replace("Time:", "Hora:")
                    .replace("Location:", "Ubicación:")
                    .replace("Type:", "Tipo:");
        }
        if (language.equals("German")) {
            return text.replace("Session:", "Sitzung:")
                    .replace("Date:", "Datum:")
                    .replace("Time:", "Zeit:")
                    .replace("Location:", "Ort:")
                    .replace("Type:", "Art:");
        }
        if (language.equals("Italian")) {
            return text.replace("Session:", "Sessione:")
                    .replace("Date:", "Data:")
                    .replace("Time:", "Ora:")
                    .replace("Location:", "Luogo:")
                    .replace("Type:", "Tipo:");
        }
        return "[" + language + "] " + text;
    }

    private String getLanguageCode(String language) {
        switch (language) {
            case "French": return "fr";
            case "Spanish": return "es";
            case "German": return "de";
            case "Italian": return "it";
            case "Portuguese": return "pt";
            case "Russian": return "ru";
            case "Japanese": return "ja";
            case "Chinese": return "zh";
            case "Arabic": return "ar";
            case "Hindi": return "hi";
            default: return "en";
        }
    }

    public List<String> getSupportedLanguages() {
        List<String> languages = new ArrayList<>();
        languages.add("French");
        languages.add("Spanish");
        languages.add("German");
        languages.add("Italian");
        languages.add("Portuguese");
        languages.add("Russian");
        languages.add("Japanese");
        languages.add("Chinese");
        languages.add("Arabic");
        languages.add("Hindi");
        return languages;
    }
}