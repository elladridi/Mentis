package services;

public class ReviewModeratorService {

    private GeminiService geminiService;

    public ReviewModeratorService() {
        this.geminiService = new GeminiService();
    }

    /**
     * Check if a review contains offensive content using AI
     * @param reviewText The review text to check
     * @return ModerationResult with analysis
     */
    public ModerationResult moderateReview(String reviewText) {
        try {
            String prompt = String.format(
                    "You are a content moderator for a mental health app called Mentis. " +
                            "Analyze this review and determine if it contains ANY offensive, insulting, " +
                            "harmful, inappropriate, or disrespectful language.\n\n" +
                            "Review: \"%s\"\n\n" +
                            "Respond with a JSON object containing these fields:\n" +
                            "1. 'isAppropriate': true/false (true if review is respectful and acceptable)\n" +
                            "2. 'confidence': number between 0 and 1\n" +
                            "3. 'reason': short explanation (max 50 words)\n" +
                            "4. 'filteredVersion': the same review but with offensive words replaced by [removed]\n" +
                            "5. 'containsProfanity': true/false\n" +
                            "6. 'containsHateSpeech': true/false\n" +
                            "7. 'containsHarassment': true/false\n\n" +
                            "Return ONLY the JSON object, no other text.",
                    reviewText.replace("\"", "\\\"")
            );

            String aiResponse = geminiService.getGoalAdvice(prompt);
            return parseAIResponse(aiResponse, reviewText);

        } catch (Exception e) {
            e.printStackTrace();
            // If AI fails, do basic checks
            return fallbackModeration(reviewText);
        }
    }

    private ModerationResult parseAIResponse(String aiResponse, String originalText) {
        ModerationResult result = new ModerationResult();
        result.setOriginalText(originalText);

        try {
            // Simple JSON parsing (you might want to use a proper JSON parser)
            if (aiResponse.contains("\"isAppropriate\": true")) {
                result.setAppropriate(true);
                result.setFilteredVersion(originalText);
                result.setReason("Review is respectful and appropriate");
            } else if (aiResponse.contains("\"isAppropriate\": false")) {
                result.setAppropriate(false);

                // Try to extract filtered version
                if (aiResponse.contains("\"filteredVersion\": \"")) {
                    int start = aiResponse.indexOf("\"filteredVersion\": \"") + 20;
                    int end = aiResponse.indexOf("\"", start);
                    if (start > 20 && end > start) {
                        result.setFilteredVersion(aiResponse.substring(start, end));
                    } else {
                        result.setFilteredVersion("[Content moderated due to inappropriate language]");
                    }
                } else {
                    result.setFilteredVersion("[Content moderated due to inappropriate language]");
                }

                // Extract reason
                if (aiResponse.contains("\"reason\": \"")) {
                    int start = aiResponse.indexOf("\"reason\": \"") + 11;
                    int end = aiResponse.indexOf("\"", start);
                    result.setReason(aiResponse.substring(start, end));
                } else {
                    result.setReason("Review contains inappropriate content");
                }

                // Check specific flags
                result.setContainsProfanity(aiResponse.contains("\"containsProfanity\": true"));
                result.setContainsHateSpeech(aiResponse.contains("\"containsHateSpeech\": true"));
                result.setContainsHarassment(aiResponse.contains("\"containsHarassment\": true"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.setAppropriate(false);
            result.setFilteredVersion("[Review could not be processed]");
            result.setReason("Error analyzing review content");
        }

        return result;
    }

    private ModerationResult fallbackModeration(String text) {
        ModerationResult result = new ModerationResult();
        result.setOriginalText(text);
        result.setAppropriate(true); // Default to true
        result.setFilteredVersion(text);
        result.setReason("Processed with basic filtering");
        result.setContainsProfanity(false);
        result.setContainsHateSpeech(false);
        result.setContainsHarassment(false);

        // Basic check for common offensive words (just as fallback)
        String lowerText = text.toLowerCase();
        String[] commonOffensive = {"stupid", "idiot", "dumb", "hate", "terrible", "awful"};

        for (String word : commonOffensive) {
            if (lowerText.contains(word)) {
                result.setAppropriate(false);
                result.setFilteredVersion("[Review contains inappropriate language]");
                result.setReason("Review contains potentially offensive language");
                result.setContainsProfanity(true);
                break;
            }
        }

        return result;
    }

    /**
     * Simple validation before sending to AI (optional)
     */
    public boolean hasMinimumLength(String text) {
        return text != null && text.trim().length() >= 5;
    }
}