package services;

public class ReviewModeratorService {

    // No need for geminiService instance variable

    public ReviewModeratorService() {
        // Empty constructor
    }

    /**
     * Check if a review contains offensive content using AI
     * @param reviewText The review text to check
     * @return ModerationResult with analysis
     */
    public ModerationResult moderateReview(String reviewText) {
        try {
            // ⭐ FIXED: Call the static method directly
            String aiResponse = GeminiService.moderateReview(reviewText);

            System.out.println("🔍 AI Response: " + aiResponse); // Debug log

            // If AI response is null or contains null, APPROVE (don't block users)
            if (aiResponse == null || aiResponse.contains("null") || aiResponse.isEmpty()) {
                return createApprovedResult(reviewText, "AI service unavailable, review approved");
            }

            return parseAIResponse(aiResponse, reviewText);

        } catch (Exception e) {
            e.printStackTrace();
            // If AI fails, APPROVE (don't block users)
            return createApprovedResult(reviewText, "AI service error, review approved");
        }
    }

    private ModerationResult createApprovedResult(String reviewText, String reason) {
        ModerationResult result = new ModerationResult();
        result.setOriginalText(reviewText);
        result.setAppropriate(true);
        result.setFilteredVersion(reviewText);
        result.setReason(reason);
        result.setContainsProfanity(false);
        result.setContainsHateSpeech(false);
        result.setContainsHarassment(false);
        return result;
    }

    private ModerationResult parseAIResponse(String aiResponse, String originalText) {
        ModerationResult result = new ModerationResult();
        result.setOriginalText(originalText);

        try {
            // Check if the review is appropriate
            if (aiResponse.contains("\"isAppropriate\": true")) {
                result.setAppropriate(true);
                result.setFilteredVersion(originalText);

                // Try to extract reason if available
                if (aiResponse.contains("\"reason\": \"")) {
                    int start = aiResponse.indexOf("\"reason\": \"") + 11;
                    int end = aiResponse.indexOf("\"", start);
                    if (start > 11 && end > start) {
                        result.setReason(aiResponse.substring(start, end));
                    } else {
                        result.setReason("Review is respectful and appropriate");
                    }
                } else {
                    result.setReason("Review is respectful and appropriate");
                }

                result.setContainsProfanity(false);
                result.setContainsHateSpeech(false);
                result.setContainsHarassment(false);

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
                    if (start > 11 && end > start) {
                        result.setReason(aiResponse.substring(start, end));
                    } else {
                        result.setReason("Review contains inappropriate content");
                    }
                } else {
                    result.setReason("Review contains inappropriate content");
                }

                // Check specific flags
                result.setContainsProfanity(aiResponse.contains("\"containsProfanity\": true"));
                result.setContainsHateSpeech(aiResponse.contains("\"containsHateSpeech\": true"));
                result.setContainsHarassment(aiResponse.contains("\"containsHarassment\": true"));
            } else {
                // If we can't determine, default to APPROVED
                return createApprovedResult(originalText, "Could not determine content, review approved");
            }

        } catch (Exception e) {
            e.printStackTrace();
            // If ANY error occurs, APPROVE the review
            return createApprovedResult(originalText, "Error parsing AI response, review approved");
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