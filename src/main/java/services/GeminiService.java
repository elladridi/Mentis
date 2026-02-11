package services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GeminiService {
    // 1. TA CLÉ SANS ESPACES
    private static final String API_KEY = "AIzaSyAlayyS3f-2eGqw7-_n3CCtbwhCdQDBehI";
    // 2. URL AVEC LE MODÈLE VERSION LATEST (PLUS ROBUSTE)
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;
    public static String getGoalAdvice(String goal) throws Exception {
        // 1. METS TA CLÉ GROQ ICI (elle commence par gsk_...)
        String GROQ_API_KEY = "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";
        String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

        // 2. Format de requête standard OpenAI/Groq
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
            // On extrait uniquement le contenu du message de l'IA
            int start = body.indexOf("\"content\":\"") + 11;
            int end = body.indexOf("\"},\"logprobs\"");
            String advice = body.substring(start, end);

            // On remplace les \n par des vrais retours à la ligne
            return advice.replace("\\n", "\n");

        }return "Erreur du serveur (Status " + response.statusCode() + ")";
    }

    public static String parseResponse(String rawJson) {
        try {
            if (rawJson.contains("\"text\": \"")) {
                int start = rawJson.indexOf("\"text\": \"") + 9;
                int end = rawJson.indexOf("\"", start);
                return rawJson.substring(start, end).replace("\\n", "\n").replace("\\\"", "\"");
            }
        } catch (Exception e) {
            return "Réponse brute : " + rawJson;
        }
        return rawJson;
    }
}