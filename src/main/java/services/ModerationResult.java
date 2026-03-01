package services;

public class ModerationResult {
    private boolean isAppropriate;
    private double confidence;
    private String reason;
    private String filteredVersion;
    private String originalText;
    private boolean containsProfanity;
    private boolean containsHateSpeech;
    private boolean containsHarassment;

    // Constructors
    public ModerationResult() {}

    public ModerationResult(boolean isAppropriate, String filteredVersion, String reason) {
        this.isAppropriate = isAppropriate;
        this.filteredVersion = filteredVersion;
        this.reason = reason;
    }

    // Getters and Setters
    public boolean isAppropriate() {
        return isAppropriate;
    }

    public void setAppropriate(boolean appropriate) {
        isAppropriate = appropriate;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getFilteredVersion() {
        return filteredVersion;
    }

    public void setFilteredVersion(String filteredVersion) {
        this.filteredVersion = filteredVersion;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public boolean isContainsProfanity() {
        return containsProfanity;
    }

    public void setContainsProfanity(boolean containsProfanity) {
        this.containsProfanity = containsProfanity;
    }

    public boolean isContainsHateSpeech() {
        return containsHateSpeech;
    }

    public void setContainsHateSpeech(boolean containsHateSpeech) {
        this.containsHateSpeech = containsHateSpeech;
    }

    public boolean isContainsHarassment() {
        return containsHarassment;
    }

    public void setContainsHarassment(boolean containsHarassment) {
        this.containsHarassment = containsHarassment;
    }

    @Override
    public String toString() {
        return "ModerationResult{" +
                "isAppropriate=" + isAppropriate +
                ", reason='" + reason + '\'' +
                ", containsProfanity=" + containsProfanity +
                ", containsHateSpeech=" + containsHateSpeech +
                ", containsHarassment=" + containsHarassment +
                '}';
    }
}