package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdaptiveAssessmentSession {
    private int assessmentId;
    private int userId;
    private List<Question> allQuestions;
    private List<Question> remainingQuestions;
    private List<Question> askedQuestions;
    private Map<Integer, String> answers;
    private Map<Integer, Integer> answerScores;
    private Map<String, Double> categoryScores;
    private String currentFocus;
    private int questionCount;
    private boolean useAIAdaptive;

    // Thresholds for adaptive branching
    private static final double HIGH_THRESHOLD = 0.7; // 70% of max score
    private static final double MEDIUM_THRESHOLD = 0.4; // 40% of max score

    public AdaptiveAssessmentSession(int assessmentId, int userId, List<Question> questions) {
        this.assessmentId = assessmentId;
        this.userId = userId;
        this.allQuestions = new ArrayList<>(questions);
        this.remainingQuestions = new ArrayList<>(questions);
        this.askedQuestions = new ArrayList<>();
        this.answers = new HashMap<>();
        this.answerScores = new HashMap<>();
        this.categoryScores = new HashMap<>();
        this.currentFocus = "general";
        this.questionCount = 0;
        this.useAIAdaptive = true;
    }

    // Getters and setters
    public int getAssessmentId() { return assessmentId; }
    public int getUserId() { return userId; }
    public List<Question> getAllQuestions() { return allQuestions; }
    public List<Question> getRemainingQuestions() { return remainingQuestions; }
    public List<Question> getAskedQuestions() { return askedQuestions; }
    public Map<Integer, String> getAnswers() { return answers; }
    public Map<Integer, Integer> getAnswerScores() { return answerScores; }
    public Map<String, Double> getCategoryScores() { return categoryScores; }
    public String getCurrentFocus() { return currentFocus; }
    public int getQuestionCount() { return questionCount; }
    public boolean isUseAIAdaptive() { return useAIAdaptive; }

    public void setCurrentFocus(String currentFocus) { this.currentFocus = currentFocus; }
    public void setUseAIAdaptive(boolean useAIAdaptive) { this.useAIAdaptive = useAIAdaptive; }

    public void addAnsweredQuestion(Question question, String answer, int score) {
        askedQuestions.add(question);
        remainingQuestions.remove(question);
        answers.put(question.getQuestionId(), answer);
        answerScores.put(question.getQuestionId(), score);
        questionCount++;

        // Update category scores based on question type/content
        updateCategoryScores(question, score);
    }

    private void updateCategoryScores(Question question, int score) {
        // Categorize question based on keywords in the text
        String questionText = question.getText().toLowerCase();
        double normalizedScore = normalizeScore(score, question.getScale());

        if (questionText.contains("anxious") || questionText.contains("worry") ||
                questionText.contains("nervous") || questionText.contains("fear")) {
            categoryScores.put("anxiety", categoryScores.getOrDefault("anxiety", 0.0) + normalizedScore);
        }
        else if (questionText.contains("depress") || questionText.contains("sad") ||
                questionText.contains("hopeless") || questionText.contains("interest")) {
            categoryScores.put("depression", categoryScores.getOrDefault("depression", 0.0) + normalizedScore);
        }
        else if (questionText.contains("sleep") || questionText.contains("tired") ||
                questionText.contains("energy") || questionText.contains("fatigue")) {
            categoryScores.put("sleep", categoryScores.getOrDefault("sleep", 0.0) + normalizedScore);
        }
        else if (questionText.contains("social") || questionText.contains("alone") ||
                questionText.contains("isolat") || questionText.contains("connect")) {
            categoryScores.put("social", categoryScores.getOrDefault("social", 0.0) + normalizedScore);
        }
        else {
            categoryScores.put("general", categoryScores.getOrDefault("general", 0.0) + normalizedScore);
        }
    }

    private double normalizeScore(int score, String scale) {
        // Normalize score to 0-1 range based on scale type
        if (scale.contains("5") || scale.contains("Never/Rarely/Sometimes/Often/Always")) {
            return (score - 1) / 4.0; // 1-5 scale
        } else if (scale.contains("Yes/No")) {
            return score == 1 ? 1.0 : 0.0; // Binary
        } else if (scale.contains("3") || scale.contains("Not at all/Moderately/Very")) {
            return (score - 1) / 2.0; // 1-3 scale
        }
        return score / 5.0; // Default
    }

    public String determineNextFocus() {
        if (categoryScores.isEmpty()) return "general";

        // Find the category with highest average score
        String maxCategory = "general";
        double maxScore = 0;

        for (Map.Entry<String, Double> entry : categoryScores.entrySet()) {
            double avgScore = entry.getValue() / getQuestionsInCategory(entry.getKey());
            if (avgScore > maxScore) {
                maxScore = avgScore;
                maxCategory = entry.getKey();
            }
        }

        // If high score in a category, dive deeper
        if (maxScore > HIGH_THRESHOLD) {
            return "deep_" + maxCategory;
        }
        // If medium score, explore more
        else if (maxScore > MEDIUM_THRESHOLD) {
            return maxCategory;
        }
        // If low scores, continue general or skip
        else {
            return "skip_" + maxCategory;
        }
    }

    private int getQuestionsInCategory(String category) {
        int count = 0;
        for (Question q : askedQuestions) {
            String text = q.getText().toLowerCase();
            if (category.equals("anxiety") && (text.contains("anxious") || text.contains("worry"))) count++;
            else if (category.equals("depression") && (text.contains("depress") || text.contains("sad"))) count++;
            else if (category.equals("sleep") && (text.contains("sleep") || text.contains("tired"))) count++;
            else if (category.equals("social") && (text.contains("social") || text.contains("alone"))) count++;
            else if (category.equals("general")) count++;
        }
        return Math.max(1, count); // Avoid division by zero
    }

    public boolean shouldSkipCategory(String category) {
        Double score = categoryScores.get(category);
        return score != null && (score / getQuestionsInCategory(category)) < MEDIUM_THRESHOLD;
    }
}