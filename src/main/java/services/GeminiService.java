package services;

import utils.z.AppConfig;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GeminiService {

    private static final String GROQ_API_KEY = utils.z.AppConfig.groqApiKey();
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    // ── Main question generation ──────────────────────────
    public static String generateContent(String prompt) throws Exception {
        String systemPrompt = "You are an expert at creating mental health assessment questions. "
                + "Generate questions in the exact format specified. "
                + "Each question must be numbered and followed by SCALE: on the next line. "
                + "Questions MUST be answerable with a single scale selection only. "
                + "Never ask for descriptions or paragraphs.";

        String jsonRequest = buildJsonRequest(
                "llama-3.1-8b-instant",
                systemPrompt,
                prompt,
                2000,
                0.7
        );

        String response = sendRequest(jsonRequest);
        String content  = extractContent(response);

        if (content == null || content.isEmpty()) {
            throw new Exception("Empty response from Groq API");
        }

        // Inject SCALE: lines if missing
        if (!content.contains("SCALE:")) {
            String[] lines   = content.split("\n");
            StringBuilder sb = new StringBuilder();
            for (String line : lines) {
                sb.append(line).append("\n");
                if (line.trim().matches("^\\d+\\..*")) {
                    sb.append("SCALE: Never/Rarely/Sometimes/Often/Always\n");
                }
            }
            content = sb.toString();
        }

        return content;
    }

    // ── Goal advice ───────────────────────────────────────
    public static String getGoalAdvice(String goal) throws Exception {
        String jsonRequest = buildJsonRequest(
                "llama-3.3-70b-versatile",
                "You are a compassionate mental health advisor. Give brief, practical advice.",
                "Give a short helpful tip for this goal: " + goal,
                150,
                0.7
        );

        String response = sendRequest(jsonRequest);
        String content  = extractContent(response);
        return content != null ? content : "Could not generate advice at this time.";
    }

    // ── Adaptive question generation ──────────────────────
    // Generates a question that properly fits the given scale
    public static String generateAdaptiveQuestion(
            String context,
            String focus,
            String scale
    ) throws Exception {

        // Parse the scale to understand what options are available
        String scaleDescription = describeScale(scale);
        String[] options        = parseScaleOptions(scale);
        String optionsList      = String.join(", ", options);

        String systemPrompt = "You are a clinical psychologist creating mental health assessment questions. "
                + "You MUST generate questions that make perfect sense with the given answer scale. "
                + "The question must be naturally answerable using exactly the provided options. "
                + "Use first-person format (I feel..., I have..., I notice...). "
                + "Keep questions under 20 words. "
                + "Return ONLY the question text. Nothing else.";

        String userPrompt = String.format(
                "Create ONE mental health question about: %s\n\n"
                        + "Context from patient's previous answers: %s\n\n"
                        + "ANSWER SCALE: %s\n"
                        + "SCALE TYPE: %s\n"
                        + "AVAILABLE OPTIONS: %s\n\n"
                        + "The question MUST make sense with this scale. "
                        + "For example:\n"
                        + "- If scale is Never/Rarely/.../Always → ask about FREQUENCY ('How often do I...')\n"
                        + "- If scale is 1-5 → ask for a rating ('On a scale of 1-5, how much do I...')\n"
                        + "- If scale is Yes/No → ask a yes/no question ('Do I experience...')\n"
                        + "- If scale is Agree/Disagree → make a statement to agree/disagree with ('I feel that...')\n\n"
                        + "Return ONLY the question text. No numbering, no explanation.",
                focus,
                context.isEmpty() ? "no context available" : context,
                scale,
                scaleDescription,
                optionsList
        );

        String jsonRequest = buildJsonRequest(
                "llama-3.1-8b-instant",
                systemPrompt,
                userPrompt,
                100,
                0.6
        );

        String response = sendRequest(jsonRequest);
        String content  = extractContent(response);

        if (content == null || content.isEmpty()) {
            return getFallbackAdaptiveQuestion(focus, scale);
        }

        // Clean up the response
        String cleaned = content
                .replaceAll("^\\d+\\.\\s*", "")
                .replaceAll("^\"|\"$", "")
                .replaceAll("^\\*+|\\*+$", "")
                .replaceAll("SCALE:.*", "")
                .trim();

        // Validate the question makes sense with scale
        if (!questionFitsScale(cleaned, scale)) {
            return getFallbackAdaptiveQuestion(focus, scale);
        }

        return cleaned.isEmpty() ? getFallbackAdaptiveQuestion(focus, scale) : cleaned;
    }

    // ── Overload for backward compatibility ──────────────
    // If no scale provided, default to Never/Always
    public static String generateAdaptiveQuestion(String context, String focus) throws Exception {
        return generateAdaptiveQuestion(context, focus, "Never/Rarely/Sometimes/Often/Always");
    }

    // ── Content moderation ────────────────────────────────
    public static String moderateReview(String reviewText) throws Exception {
        String prompt = "Analyze this review for inappropriate content:\n\n"
                + "Review: \"" + escapeJson(reviewText) + "\"\n\n"
                + "Return ONLY this JSON:\n"
                + "{\"isAppropriate\": true, \"confidence\": 0.0, \"reason\": \"\", "
                + "\"filteredVersion\": \"\", \"containsProfanity\": false, "
                + "\"containsHateSpeech\": false, \"containsHarassment\": false}";

        String jsonRequest = buildJsonRequest(
                "llama-3.3-70b-versatile",
                "You are a content moderator. Return ONLY valid JSON.",
                prompt,
                500,
                0.1
        );

        try {
            String response = sendRequest(jsonRequest);
            String content  = extractContent(response);
            return content != null ? content.replace("\\n", "\n").replace("\\\"", "\"")
                    : defaultModerationJson(reviewText);
        } catch (Exception e) {
            return defaultModerationJson(reviewText);
        }
    }

    // ═══════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═══════════════════════════════════════════════════

    // ── Parse scale string into array of options ─────────
    private static String[] parseScaleOptions(String scale) {
        if (scale == null || scale.isEmpty()) {
            return new String[]{"Never", "Rarely", "Sometimes", "Often", "Always"};
        }

        scale = scale.trim();

        // Slash format: Never/Rarely/Sometimes/Often/Always
        if (scale.contains("/")) {
            return scale.split("/");
        }

        // Comma format: Never,Rarely,Sometimes,Often,Always
        if (scale.contains(",") && !scale.contains("=")) {
            return scale.split(",");
        }

        // Key=value format: 0=Never,1=Rarely,...
        if (scale.contains("=")) {
            String[] pairs = scale.split(",");
            String[] values = new String[pairs.length];
            for (int i = 0; i < pairs.length; i++) {
                String[] kv = pairs[i].split("=");
                values[i] = kv.length > 1 ? kv[1].trim() : kv[0].trim();
            }
            return values;
        }

        // Numeric range: 1-5 or 1-10
        if (scale.matches("\\d+-\\d+")) {
            String[] parts = scale.split("-");
            int start = Integer.parseInt(parts[0]);
            int end   = Integer.parseInt(parts[1]);
            String[] nums = new String[end - start + 1];
            for (int i = 0; i <= end - start; i++) {
                nums[i] = String.valueOf(start + i);
            }
            return nums;
        }

        // Yes/No
        if (scale.equalsIgnoreCase("Yes/No") || scale.equalsIgnoreCase("Yes,No")) {
            return new String[]{"Yes", "No"};
        }

        return new String[]{"Never", "Rarely", "Sometimes", "Often", "Always"};
    }

    // ── Describe scale type for the AI prompt ────────────
    private static String describeScale(String scale) {
        if (scale == null || scale.isEmpty()) {
            return "frequency (Never to Always)";
        }

        scale = scale.trim().toLowerCase();

        if (scale.contains("never") && scale.contains("always")) {
            return "frequency scale (Never/Rarely/Sometimes/Often/Always) — ask HOW OFTEN";
        }
        if (scale.contains("agree") || scale.contains("disagree")) {
            return "agreement scale — make a STATEMENT the person agrees or disagrees with";
        }
        if (scale.matches("\\d+-\\d+")) {
            return "numeric rating scale — ask to RATE on a scale of numbers";
        }
        if (scale.contains("yes") && scale.contains("no")) {
            return "yes/no scale — ask a question answerable with YES or NO";
        }
        if (scale.contains("not at all") || scale.contains("extremely")) {
            return "intensity scale — ask about INTENSITY or SEVERITY";
        }
        if (scale.contains("poor") || scale.contains("excellent")) {
            return "quality scale — ask about QUALITY or HOW GOOD something is";
        }

        return "custom scale with options: " + scale;
    }

    // ── Validate question fits scale ──────────────────────
    private static boolean questionFitsScale(String question, String scale) {
        if (question == null || question.isEmpty()) return false;

        String q = question.toLowerCase();
        String s = scale.toLowerCase();

        // Frequency scales need frequency questions
        if (s.contains("never") && s.contains("always")) {
            boolean hasFrequencyWords = q.contains("how often") || q.contains("often")
                    || q.contains("frequently") || q.contains("do i") || q.contains("have i")
                    || q.contains("feel") || q.contains("experience") || q.contains("notice");
            return hasFrequencyWords;
        }

        // Agreement scale needs a statement not a question
        if (s.contains("agree") || s.contains("disagree")) {
            // Should NOT be a question (no question mark at start logic)
            // Should be a statement like "I feel..." or "I have..."
            return q.startsWith("i ") || q.startsWith("my ");
        }

        // Yes/No needs a direct question
        if (s.contains("yes") && s.contains("no")) {
            return q.contains("do i") || q.contains("have i")
                    || q.contains("am i") || q.contains("can i")
                    || q.contains("did i") || q.contains("is my");
        }

        // Numeric scales need rating questions
        if (scale.matches("\\d+-\\d+")) {
            return q.contains("rate") || q.contains("scale") || q.contains("how much")
                    || q.contains("how") || q.contains("level");
        }

        return true; // Accept by default for unknown scales
    }

    // ── Fallback questions that fit common scales ─────────
    private static String getFallbackAdaptiveQuestion(String focus, String scale) {
        String[] options = parseScaleOptions(scale);
        String s = scale.toLowerCase();

        // Frequency scale fallbacks
        if (s.contains("never") && s.contains("always")) {
            switch (focus.toLowerCase()) {
                case "anxiety":
                    return "How often do I feel anxious or worried about everyday situations?";
                case "depression":
                    return "How often do I feel sad, empty, or hopeless throughout the day?";
                case "sleep":
                    return "How often do I have difficulty falling or staying asleep?";
                case "social":
                    return "How often do I avoid social situations due to discomfort?";
                default:
                    return "How often do I feel overwhelmed by my emotions?";
            }
        }

        // Agreement scale fallbacks
        if (s.contains("agree") || s.contains("disagree")) {
            switch (focus.toLowerCase()) {
                case "anxiety":
                    return "I find it difficult to control my worrying thoughts.";
                case "depression":
                    return "I feel a persistent sense of sadness that affects my daily life.";
                case "sleep":
                    return "My sleep problems significantly impact my daily functioning.";
                case "social":
                    return "I feel uncomfortable in most social situations.";
                default:
                    return "I struggle to manage my emotions on a daily basis.";
            }
        }

        // Numeric scale fallbacks
        if (scale.matches("\\d+-\\d+")) {
            String[] parts = scale.split("-");
            String max = parts[1];
            switch (focus.toLowerCase()) {
                case "anxiety":
                    return "On a scale of 1-" + max + ", how intense is my anxiety today?";
                case "depression":
                    return "On a scale of 1-" + max + ", how low is my mood right now?";
                case "sleep":
                    return "On a scale of 1-" + max + ", how poor has my sleep quality been?";
                case "social":
                    return "On a scale of 1-" + max + ", how much do I avoid social interactions?";
                default:
                    return "On a scale of 1-" + max + ", how much does this affect my daily life?";
            }
        }

        // Yes/No fallbacks
        if (s.contains("yes") && s.contains("no")) {
            switch (focus.toLowerCase()) {
                case "anxiety":
                    return "Do I experience physical symptoms like racing heart when anxious?";
                case "depression":
                    return "Have I lost interest in activities I used to enjoy?";
                case "sleep":
                    return "Do I regularly wake up feeling unrefreshed or tired?";
                case "social":
                    return "Do I avoid social situations because of fear or discomfort?";
                default:
                    return "Do I feel that my mental health significantly impacts my daily life?";
            }
        }

        // Generic fallback
        return "How would you describe your experience with " + focus + " recently?";
    }

    // ── Build JSON request payload ────────────────────────
    private static String buildJsonRequest(
            String model,
            String systemPrompt,
            String userPrompt,
            int maxTokens,
            double temperature
    ) {
        return "{"
                + "\"model\": \"" + model + "\","
                + "\"messages\": ["
                + "{\"role\": \"system\", \"content\": \"" + escapeJson(systemPrompt) + "\"},"
                + "{\"role\": \"user\", \"content\": \"" + escapeJson(userPrompt) + "\"}"
                + "],"
                + "\"max_tokens\": " + maxTokens + ","
                + "\"temperature\": " + temperature
                + "}";
    }

    // ── Send HTTP request to Groq ─────────────────────────
    private static String sendRequest(String jsonPayload) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GROQ_URL))
                .header("Authorization", "Bearer " + GROQ_API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() == 401) {
            throw new Exception("Invalid API key (401). Check your GROQ_API_KEY in AppConfig.");
        }
        if (response.statusCode() != 200) {
            throw new Exception("Groq API error: HTTP " + response.statusCode()
                    + " - " + response.body());
        }

        return response.body();
    }

    // ── Extract content from Groq JSON response ───────────
    private static String extractContent(String responseBody) {
        if (responseBody == null || responseBody.isEmpty()) return null;

        try {
            // Primary extraction — standard Groq response format
            String marker = "\"content\":\"";
            int start = responseBody.indexOf(marker);
            if (start == -1) {
                marker = "\"content\": \"";
                start  = responseBody.indexOf(marker);
            }
            if (start == -1) return null;

            start += marker.length();

            // Find closing quote accounting for escaped quotes
            StringBuilder content = new StringBuilder();
            int i = start;
            while (i < responseBody.length()) {
                char c = responseBody.charAt(i);
                if (c == '\\' && i + 1 < responseBody.length()) {
                    char next = responseBody.charAt(i + 1);
                    switch (next) {
                        case 'n':  content.append('\n'); i += 2; break;
                        case 't':  content.append('\t'); i += 2; break;
                        case '"':  content.append('"');  i += 2; break;
                        case '\\': content.append('\\'); i += 2; break;
                        default:   content.append(c);    i++;    break;
                    }
                } else if (c == '"') {
                    break; // End of content
                } else {
                    content.append(c);
                    i++;
                }
            }

            return content.toString().trim();

        } catch (Exception e) {
            return null;
        }
    }

    // ── Parse response helper (public for backward compat) ─
    public static String parseResponse(String rawJson) {
        String extracted = extractContent(rawJson);
        return extracted != null ? extracted : rawJson;
    }

    // ── Default moderation JSON ───────────────────────────
    private static String defaultModerationJson(String reviewText) {
        return "{\"isAppropriate\": true, \"confidence\": 1.0, "
                + "\"reason\": \"API unavailable, auto-approved\", "
                + "\"filteredVersion\": \"" + escapeJson(reviewText) + "\", "
                + "\"containsProfanity\": false, "
                + "\"containsHateSpeech\": false, "
                + "\"containsHarassment\": false}";
    }

    // ── JSON escaping ─────────────────────────────────────
    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}