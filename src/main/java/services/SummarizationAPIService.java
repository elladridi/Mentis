package services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;

/**
 * Text Summarization using RapidAPI
 * Uses your specific API endpoint
 */
public class SummarizationAPIService {

    private static final String API_KEY = "5b67be9d7bmsha22965121d32d7bp1234cajsn8702cb533f63";
    private static final String API_HOST = "textanalysis-text-summarization.p.rapidapi.com";
    private static final String API_URL = "https://textanalysis-text-summarization.p.rapidapi.com/text-summarizer";

    private static int requestCount = 0;
    private static final int MAX_REQUESTS = 1000; // Free tier limit
    private static boolean rateLimitWarning = false;

    /**
     * Summarize text using the API
     * @param text Text to summarize (will be truncated to 5000 chars for safety)
     * @param sentences Number of sentences in summary (1-10)
     * @return Summarized text or fallback if API fails
     */
    public static String summarize(String text, int sentences) {
        // Check rate limit
        if (requestCount >= MAX_REQUESTS) {
            if (!rateLimitWarning) {
                System.err.println("⚠️ API rate limit reached (1000/month). Using fallback.");
                rateLimitWarning = true;
            }
            return fallbackSummary(text);
        }

        // Truncate text to avoid API limits
        if (text.length() > 5000) {
            text = text.substring(0, 4997) + "...";
        }

        try {
            HttpClient client = HttpClient.newHttpClient();

            String jsonRequest = String.format(
                    "{\"text\":\"%s\",\"sentnum\":%d}",
                    escapeJson(text),
                    Math.min(sentences, 8) // API supports up to 8 sentences
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("x-rapidapi-host", API_HOST)
                    .header("x-rapidapi-key", API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();

            System.out.println("📡 Calling summarization API...");
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            requestCount++;
            System.out.println("✅ API called. Remaining: " + (MAX_REQUESTS - requestCount));

            if (response.statusCode() == 200) {
                return parseSummary(response.body());
            } else if (response.statusCode() == 429) {
                System.err.println("❌ Rate limit exceeded (429)");
                return fallbackSummary(text);
            } else {
                System.err.println("❌ API error: " + response.statusCode());
                System.err.println("Response: " + response.body());
                return fallbackSummary(text);
            }

        } catch (Exception e) {
            System.err.println("❌ API call failed: " + e.getMessage());
            return fallbackSummary(text);
        }
    }

    /**
     * Summarize text with default 3 sentences
     */
    public static String summarize(String text) {
        return summarize(text, 3);
    }

    private static String parseSummary(String json) {
        try {
            // Try to parse as JSON
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

            // Check different possible response formats
            if (jsonObject.has("summary")) {
                return jsonObject.get("summary").getAsString();
            } else if (jsonObject.has("text")) {
                return jsonObject.get("text").getAsString();
            } else if (jsonObject.has("result")) {
                return jsonObject.get("result").getAsString();
            }

            // If no known field, return the whole response
            return json;

        } catch (Exception e) {
            // If JSON parsing fails, maybe it's plain text
            if (json.length() < 1000) {
                return json;
            }
            return fallbackSummary(json);
        }
    }

    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("/", "\\/");
    }

    private static String fallbackSummary(String text) {
        // Simple fallback: take first 200 characters
        if (text.length() > 200) {
            return text.substring(0, 197) + "...";
        }
        return text;
    }

    /**
     * Get remaining API calls this month
     */
    public static int getRemainingCalls() {
        return MAX_REQUESTS - requestCount;
    }

    /**
     * Check if service is available
     */
    public static boolean isAvailable() {
        return requestCount < MAX_REQUESTS;
    }
}