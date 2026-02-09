package models;

import java.util.Date;

public class AssessmentResult {

    private int resultId;
    private int userId;
    private int assessmentId;
    private int totalScore;
    private String riskLevel;           // low / moderate / high
    private String interpretation;
    private String recommendedContent;  // articles/videos/books
    private boolean suggestSession;
    private Date takenAt;

    public AssessmentResult() {}

    public AssessmentResult(int userId, int assessmentId, int totalScore, String riskLevel,
                            String interpretation, String recommendedContent, boolean suggestSession, Date takenAt) {
        this.userId = userId;
        this.assessmentId = assessmentId;
        this.totalScore = totalScore;
        this.riskLevel = riskLevel;
        this.interpretation = interpretation;
        this.recommendedContent = recommendedContent;
        this.suggestSession = suggestSession;
        this.takenAt = takenAt;
    }

    // Getters and setters
    public int getResultId() { return resultId; }
    public void setResultId(int resultId) { this.resultId = resultId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getAssessmentId() { return assessmentId; }
    public void setAssessmentId(int assessmentId) { this.assessmentId = assessmentId; }

    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getInterpretation() { return interpretation; }
    public void setInterpretation(String interpretation) { this.interpretation = interpretation; }

    public String getRecommendedContent() { return recommendedContent; }
    public void setRecommendedContent(String recommendedContent) { this.recommendedContent = recommendedContent; }

    public boolean isSuggestSession() { return suggestSession; }
    public void setSuggestSession(boolean suggestSession) { this.suggestSession = suggestSession; }

    public Date getTakenAt() { return takenAt; }
    public void setTakenAt(Date takenAt) { this.takenAt = takenAt; }
}
