package services;

import models.AdaptiveAssessmentSession;
import models.Question;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.*;

public class AdaptiveQuestionService {

    private final GeminiService geminiService;
    private final Gson gson = new Gson();

    public AdaptiveQuestionService() {
        this.geminiService = new GeminiService();
    }

    public Question getNextAdaptiveQuestion(AdaptiveAssessmentSession session, List<Question> availableQuestions) {
        if (availableQuestions == null || availableQuestions.isEmpty()) {
            return null;
        }

        // If we've asked less than 3 questions, just return the next in sequence
        if (session.getQuestionCount() < 3) {
            return availableQuestions.get(0);
        }

        // Determine next focus based on scores
        String nextFocus = session.determineNextFocus();
        session.setCurrentFocus(nextFocus);

        // Try to find a matching question
        Question nextQuestion = findQuestionByFocus(session, availableQuestions, nextFocus);

        // If no matching question or we should use AI for dynamic generation
        if (nextQuestion == null && session.isUseAIAdaptive()) {
            nextQuestion = generateDynamicQuestion(session, nextFocus);
        }

        // Fallback to first available question
        if (nextQuestion == null && !availableQuestions.isEmpty()) {
            nextQuestion = availableQuestions.get(0);
        }

        return nextQuestion;
    }

    private Question findQuestionByFocus(AdaptiveAssessmentSession session, List<Question> questions, String focus) {
        // First, check if we should skip this category
        if (focus.startsWith("skip_")) {
            String category = focus.substring(5);
            if (session.shouldSkipCategory(category)) {
                return null; // Signal to move to next category
            }
        }

        // For deep dive, look for more specific questions
        if (focus.startsWith("deep_")) {
            String category = focus.substring(5);
            return findDeepQuestion(questions, category);
        }

        // For normal focus, find general questions in that category
        return findGeneralQuestion(questions, focus);
    }

    private Question findDeepQuestion(List<Question> questions, String category) {
        List<Question> candidates = new ArrayList<>();

        for (Question q : questions) {
            String text = q.getText().toLowerCase();
            switch (category) {
                case "anxiety":
                    if (text.contains("panic") || text.contains("attack") ||
                            text.contains("heart") || text.contains("sweat")) {
                        candidates.add(q);
                    }
                    break;
                case "depression":
                    if (text.contains("suicidal") || text.contains("worthless") ||
                            text.contains("empty") || text.contains("guilt")) {
                        candidates.add(q);
                    }
                    break;
                case "sleep":
                    if (text.contains("insomnia") || text.contains("nightmare") ||
                            text.contains("restless") || text.contains("early")) {
                        candidates.add(q);
                    }
                    break;
                case "social":
                    if (text.contains("avoid") || text.contains("crowd") ||
                            text.contains("public") || text.contains("talk")) {
                        candidates.add(q);
                    }
                    break;
            }
        }

        return candidates.isEmpty() ? null : candidates.get(new Random().nextInt(candidates.size()));
    }

    private Question findGeneralQuestion(List<Question> questions, String category) {
        List<Question> candidates = new ArrayList<>();

        for (Question q : questions) {
            String text = q.getText().toLowerCase();
            switch (category) {
                case "anxiety":
                    if (text.contains("anxious") || text.contains("worry") || text.contains("nervous")) {
                        candidates.add(q);
                    }
                    break;
                case "depression":
                    if (text.contains("depress") || text.contains("sad") || text.contains("hopeless")) {
                        candidates.add(q);
                    }
                    break;
                case "sleep":
                    if (text.contains("sleep") || text.contains("tired") || text.contains("energy")) {
                        candidates.add(q);
                    }
                    break;
                case "social":
                    if (text.contains("social") || text.contains("alone") || text.contains("isolat")) {
                        candidates.add(q);
                    }
                    break;
                default:
                    candidates.add(q);
            }
        }

        return candidates.isEmpty() ? null : candidates.get(new Random().nextInt(candidates.size()));
    }

    private Question generateDynamicQuestion(AdaptiveAssessmentSession session, String focus) {
        try {
            // Build prompt for AI to generate a dynamic follow-up question
            StringBuilder prompt = new StringBuilder();
            prompt.append("Based on a mental health assessment where the patient has shown ");

            // Add context from previous answers
            Map<String, Double> scores = session.getCategoryScores();
            if (!scores.isEmpty()) {
                prompt.append("the following patterns: ");
                for (Map.Entry<String, Double> entry : scores.entrySet()) {
                    double avgScore = entry.getValue() / session.getQuestionCount();
                    String level = avgScore > 0.7 ? "high" : (avgScore > 0.4 ? "moderate" : "low");
                    prompt.append(String.format("%s (%s levels), ", entry.getKey(), level));
                }
            }

            // Ask for specific question based on focus
            if (focus.startsWith("deep_")) {
                String category = focus.substring(5);
                prompt.append(String.format(
                        "\n\nGenerate ONE deep, specific follow-up question about %s. " +
                                "The question should probe deeper into this area. " +
                                "Format: Just the question text, nothing else.", category));
            } else {
                prompt.append(String.format(
                        "\n\nGenerate ONE general question about %s to explore this area further. " +
                                "Format: Just the question text, nothing else.", focus));
            }

            String response = GeminiService.generateContent(prompt.toString());

            // Create a new dynamic question
            Question dynamicQuestion = new Question();
            dynamicQuestion.setAssessmentId(session.getAssessmentId());
            dynamicQuestion.setText(cleanQuestionText(response));
            dynamicQuestion.setScale("Never/Rarely/Sometimes/Often/Always"); // Default scale

            return dynamicQuestion;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String cleanQuestionText(String text) {
        // Remove any numbering, quotes, or extra formatting
        return text.replaceAll("^\\d+\\.\\s*", "")
                .replaceAll("^\"|\"$", "")
                .replaceAll("^\\*|\\*$", "")
                .trim();
    }
}