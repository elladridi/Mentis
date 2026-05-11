package com.mentalhealth.app.services;

import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;
import utils.z.AppConfig;

public class HuggingFaceService {
    private static final String HF_TOKEN = AppConfig.hfApiKey();
    private static final String HF_ENDPOINT =
            "https://api-inference.huggingface.co/models/runwayml/stable-diffusion-v1-5";
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(120, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .build();

    public String generateImagePrompt(String emotion, String note) {
        String basePrompt = "Digital art representation of " + emotion + ". " + (note != null ? note : "") + ", ";
        switch (emotion.toLowerCase()) {
            case "heureux": return basePrompt + "bright colors, bursting with joy, vibrant, masterpiece";
            case "triste": return basePrompt + "melancholic blues and greys, rain, emotional, cinematic lighting";
            case "énervé": return basePrompt + "aggressive red and black colors, sharp shapes, intensity, dramatic";
            case "neutral": return basePrompt + "harmony of soft tones, balanced shapes, serenity, minimal";
            case "motivé": return basePrompt + "ascending lines, golden tones, ambition, success, heroic";
            default: return basePrompt + "artistic, aesthetic, detailed";
        }
    }

    public byte[] generateImage(String prompt) throws IOException {
        if (HF_TOKEN.isBlank()) {
            throw new IOException("Configuration manquante: définis MENTIS_HF_API_KEY.");
        }
        JSONObject json = new JSONObject();
        json.put("inputs", prompt);
        String jsonBody = json.toString();
        
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody, mediaType);
        
        Request request = new Request.Builder()
                .url(HF_ENDPOINT)
                .addHeader("Authorization", "Bearer " + HF_TOKEN)
                .addHeader("x-wait-for-model", "true")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                if (response.code() == 401) {
                    throw new IOException("Erreur d'authentification (401) : Ta clé MENTIS_HF_API_KEY est invalide ou expirée. Vérifie tes variables d'environnement.");
                }
                throw new IOException("Erreur API (" + response.code() + ") : " + errorBody);
            }
            if (response.body() == null) throw new IOException("Réponse de l'API vide");
            return response.body().bytes();
        }
    }
}
