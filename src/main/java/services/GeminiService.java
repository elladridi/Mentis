package services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GeminiService {
    // Use Groq instead of Gemini since Gemini model is not found
    private static final String GROQ_API_KEY = "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";
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    public static String getGoalAdvice(String goal) throws Exception {
        String jsonRequest = "{"
                + "\"model\": \"llama-3.3-70b-versatile\","
                + "\"messages\": [{\"role\": \"user\", \"content\": \"Donne un conseil court pour : " + goal + "\"}],"
                + "\"max_tokens\": 100"
                + "}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GROQ_URL))
                .header("Authorization", "Bearer " + GROQ_API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("--- DEBUG GROQ ---");
        System.out.println("STATUS: " + response.statusCode());

        if (response.statusCode() == 200) {
            String body = response.body();
            int start = body.indexOf("\"content\":\"") + 11;
            int end = body.indexOf("\"},\"logprobs\"");
            String advice = body.substring(start, end);
            return advice.replace("\\n", "\n");
        }
        return "Erreur du serveur (Status " + response.statusCode() + ")";
    }

    public static String generateContent(String prompt) throws Exception {
        // Build a more specific prompt for question generation
        String systemPrompt = "You are an expert at creating mental health assessment questions. " +
                "Generate questions in the exact format specified. " +
                "Each question must be numbered and followed by SCALE: on the next line.";

        String fullPrompt = systemPrompt + "\n\n" + prompt;

        String jsonRequest = "{"
                + "\"model\": \"llama-3.3-70b-versatile\","
                + "\"messages\": ["
                + "{\"role\": \"system\", \"content\": \"" + escapeJson(systemPrompt) + "\"},"
                + "{\"role\": \"user\", \"content\": \"" + escapeJson(prompt) + "\"}"
                + "],"
                + "\"max_tokens\": 2000,"
                + "\"temperature\": 0.7"
                + "}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GROQ_URL))
                .header("Authorization", "Bearer " + GROQ_API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

        System.out.println("--- Sending request to Groq API ---");
        System.out.println("Prompt: " + prompt.substring(0, Math.min(100, prompt.length())) + "...");

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("STATUS: " + response.statusCode());

        if (response.statusCode() == 200) {
            String body = response.body();

            // Extract content from Groq response
            int start = body.indexOf("\"content\":\"") + 11;
            if (start == 10) { // Try alternative format
                start = body.indexOf("\"content\": \"") + 12;
            }

            int end = body.indexOf("\"", start + 1);
            // Find the actual end of content (looking for the closing quote of the message)
            while (end < body.length()) {
                if (body.charAt(end) == '"' && body.charAt(end - 1) != '\\') {
                    // Check if this is the end of the content field
                    if (end + 1 < body.length() && (body.charAt(end + 1) == ',' || body.charAt(end + 1) == '}')) {
                        break;
                    }
                }
                end++;
                if (end >= body.length()) {
                    end = body.indexOf("\"},\"logprobs\"");
                    break;
                }
            }

            if (start > 10 && end > start) {
                String content = body.substring(start, end);
                // Clean up the response
                content = content.replace("\\n", "\n")
                        .replace("\\\"", "\"")
                        .replace("\\'", "'")
                        .replace("\\\\", "\\");

                // Ensure the format matches what the parser expects
                if (!content.contains("SCALE:")) {
                    // If no SCALE lines, add default scale to each question
                    String[] lines = content.split("\n");
                    StringBuilder formatted = new StringBuilder();
                    for (String line : lines) {
                        if (line.matches("^\\d+\\..*")) {
                            formatted.append(line).append("\n");
                            formatted.append("SCALE: Never/Rarely/Sometimes/Often/Always\n");
                        } else {
                            formatted.append(line).append("\n");
                        }
                    }
                    content = formatted.toString();
                }

                return content;
            }
        }

        throw new Exception("Groq API error: HTTP " + response.statusCode() + " - " + response.body());
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static String parseResponse(String rawJson) {
        try {
            if (rawJson.contains("\"content\": \"")) {
                int start = rawJson.indexOf("\"content\": \"") + 12;
                int end = rawJson.indexOf("\"", start);
                return rawJson.substring(start, end).replace("\\n", "\n").replace("\\\"", "\"");
            }
        } catch (Exception e) {
            return "Réponse brute : " + rawJson;
        }
        return rawJson;
    }
}