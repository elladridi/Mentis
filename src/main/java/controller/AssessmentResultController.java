package controller;

import models.AssessmentResult;
import models.Question;
import services.AssessmentResultService;
import services.GeminiService;
import services.QuestionService;
import utils.MyDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AssessmentResultController {

    private AssessmentResultService resultService;
    private QuestionService questionService;

    public AssessmentResultController() {
        this.resultService = new AssessmentResultService();
        this.questionService = new QuestionService();
    }

    // ═══════════════════════════════════════════════════════════════
    //  SUBMIT ASSESSMENT
    // ═══════════════════════════════════════════════════════════════

    public Map<String, Object> submitAssessment(int userId, int assessmentId,
                                                Map<Integer, Integer> scores,
                                                Map<Integer, String> originalAnswers) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Question> questions = getQuestionsByAssessment(assessmentId);

            int totalScore = 0;
            for (Integer score : scores.values()) {
                totalScore += score;
            }

            String riskLevel = determineRiskLevel(totalScore, assessmentId);

            // Calls Gemini via GeminiService, falls back to rule-based if API fails
            String aiAnalysis = generateAIAnalysis(questions, scores, originalAnswers, totalScore, riskLevel);

            String recommendedContent = generateRecommendedContent(riskLevel, assessmentId, aiAnalysis);
            String interpretation = generateInterpretation(riskLevel, aiAnalysis);
            boolean suggestSession = shouldSuggestSession(riskLevel, aiAnalysis);

            AssessmentResult result = new AssessmentResult(
                    userId, assessmentId, totalScore, riskLevel,
                    interpretation, recommendedContent, suggestSession, new Date()
            );

            resultService.addResult(result);

            response.put("success", true);
            response.put("result", Map.of(
                    "totalScore", totalScore,
                    "riskLevel", riskLevel,
                    "interpretation", interpretation,
                    "recommendedContent", recommendedContent,
                    "aiAnalysis", aiAnalysis,
                    "suggestSession", suggestSession,
                    "resultId", getLatestResultId(userId)
            ));

        } catch (SQLException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Error processing assessment: " + e.getMessage());
        }

        return response;
    }

    // Backward compatibility
    public Map<String, Object> submitAssessment(int userId, int assessmentId, Map<Integer, Integer> scores) {
        return submitAssessment(userId, assessmentId, scores, new HashMap<>());
    }

    // ═══════════════════════════════════════════════════════════════
    //  GEMINI AI ANALYSIS — uses your existing GeminiService
    // ═══════════════════════════════════════════════════════════════

    /**
     * Tries Gemini first. If the API call fails for any reason,
     * automatically falls back to the rule-based analysis so the
     * app never crashes.
     */
    private String generateAIAnalysis(List<Question> questions,
                                      Map<Integer, Integer> scores,
                                      Map<Integer, String> originalAnswers,
                                      int totalScore, String riskLevel) {
        try {
            System.out.println("Calling Gemini API for assessment analysis...");
            String prompt = buildGeminiPrompt(questions, scores, originalAnswers, totalScore, riskLevel);
            String result = GeminiService.generateContent(prompt);
            System.out.println("Gemini response received successfully.");
            return result;
        } catch (Exception e) {
            System.err.println("Gemini API failed (" + e.getMessage() + ") — using rule-based fallback.");
            return generateRuleBasedAnalysis(questions, scores, originalAnswers, totalScore, riskLevel);
        }
    }

    /**
     * Builds the rich prompt sent to Gemini.
     */
    private String buildGeminiPrompt(List<Question> questions,
                                     Map<Integer, Integer> scores,
                                     Map<Integer, String> originalAnswers,
                                     int totalScore, String riskLevel) {

        int maxPossible = questions.size() * 4;

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a compassionate, professional mental health assessment analyst working for the Mentis wellness platform. ")
                .append("A user has just completed a mental wellness self-assessment. ")
                .append("Your job is to provide a thorough, empathetic, and highly personalized analysis of their specific responses. ")
                .append("Write in plain English paragraphs only. ")
                .append("Do NOT use markdown symbols such as **, ##, *, or bullet points with dashes. ")
                .append("Do NOT be generic — reference the user's actual answers directly in your analysis. ")
                .append("Be warm, non-judgmental, and constructive throughout.\n\n");

        prompt.append("=== ASSESSMENT DATA ===\n");
        prompt.append("Total Score: ").append(totalScore).append(" out of ").append(maxPossible).append("\n");
        prompt.append("Risk Level Determined: ").append(riskLevel).append("\n\n");

        prompt.append("=== USER RESPONSES (question, their answer, score out of 4) ===\n");
        for (Question q : questions) {
            Integer score = scores.get(q.getQuestionId());
            String answer = originalAnswers.get(q.getQuestionId());
            if (score != null && answer != null) {
                prompt.append("Question: ").append(q.getText()).append("\n");
                prompt.append("Answer: ").append(answer).append(" | Score: ").append(score).append("/4\n\n");
            }
        }

        prompt.append("=== YOUR TASK ===\n");
        prompt.append("Write a detailed, personalized mental health analysis report with the following sections. ")
                .append("Label each section clearly on its own line, then write the content underneath.\n\n");

        prompt.append("OVERALL SUMMARY\n");
        prompt.append("Write 3 to 4 warm paragraphs summarizing the user's overall mental state based on their score of ")
                .append(totalScore).append(" out of ").append(maxPossible)
                .append(". Reference their risk level of ").append(riskLevel)
                .append(" and what it means for them personally.\n\n");

        prompt.append("DETAILED RESPONSE ANALYSIS\n");
        prompt.append("For each question, write 2 to 3 sentences explaining what their specific answer reveals ")
                .append("about their mental wellness and what it might mean for their daily life. ")
                .append("Be specific — use their actual answer in your explanation.\n\n");

        prompt.append("PATTERNS DETECTED\n");
        prompt.append("Identify meaningful patterns, symptom clusters, or connections between their answers. ")
                .append("For example, note if sleep and anxiety issues appear together, or if there is a consistent ")
                .append("theme of low energy and low mood.\n\n");

        prompt.append("PERSONALIZED RECOMMENDATIONS\n");
        prompt.append("Give 4 to 6 specific, actionable recommendations tailored to THIS user's exact responses. ")
                .append("Do not give generic advice — connect each recommendation directly to something they answered.\n\n");

        prompt.append("CLOSING NOTE\n");
        prompt.append("End with an encouraging, supportive 2-paragraph closing message that acknowledges the courage ")
                .append("it takes to reflect on one's mental health, and reminds them that support is always available.\n\n");

        prompt.append("Write a minimum of 500 words total. The user is reading this directly after completing their assessment.");

        return prompt.toString();
    }

    // ═══════════════════════════════════════════════════════════════
    //  RULE-BASED FALLBACK (used when Gemini is unavailable)
    // ═══════════════════════════════════════════════════════════════

    private String generateRuleBasedAnalysis(List<Question> questions,
                                             Map<Integer, Integer> scores,
                                             Map<Integer, String> originalAnswers,
                                             int totalScore, String riskLevel) {

        StringBuilder analysis = new StringBuilder();
        double averageScore = questions.isEmpty() ? 0 : totalScore / (double) questions.size();
        int maxPossible = questions.size() * 4;
        double percentage = maxPossible > 0 ? (totalScore * 100.0 / maxPossible) : 0;

        int concerningCount = 0;
        int severeCount = 0;
        List<String> concernAreas = new ArrayList<>();
        List<String> positiveAreas = new ArrayList<>();

        for (Question q : questions) {
            Integer score = scores.get(q.getQuestionId());
            if (score == null) continue;
            if (score >= 3) {
                concerningCount++;
                concernAreas.add(q.getText());
                if (score == 4) severeCount++;
            } else if (score <= 1) {
                positiveAreas.add(q.getText());
            }
        }

        // ── OVERALL SUMMARY ──
        analysis.append("OVERALL SUMMARY\n");
        analysis.append("═══════════════════════════════════════════════════\n\n");
        analysis.append("You completed ").append(questions.size())
                .append(" questions with a total score of ").append(totalScore)
                .append(" out of a maximum of ").append(maxPossible)
                .append(" points (").append(String.format("%.0f", percentage)).append("%). ")
                .append("Your overall risk level has been assessed as: ").append(riskLevel.toUpperCase()).append(".\n\n");

        if (averageScore <= 1.0) {
            analysis.append("Your responses indicate that you are currently managing well across most areas assessed. ")
                    .append("The low scores across your answers suggest that you are not experiencing significant distress ")
                    .append("at this time, and your coping mechanisms appear to be functioning effectively. ")
                    .append("This is a positive sign that your mental wellness is in a good place. ")
                    .append("Maintaining this balance through consistent healthy habits is key to sustaining your wellbeing.\n\n");
        } else if (averageScore <= 2.0) {
            analysis.append("Your responses suggest mild fluctuations in certain areas of your mental wellness. ")
                    .append("While most of your scores fall within a manageable range, there are a few areas ")
                    .append("that may benefit from additional attention and self-care. ")
                    .append("This is quite common and does not indicate a serious concern, but paying ")
                    .append("attention to these signals early can prevent them from escalating over time.\n\n");
        } else if (averageScore <= 3.0) {
            analysis.append("Your responses indicate moderate levels of distress or difficulty in several areas. ")
                    .append("A score at this level suggests that certain aspects of your daily functioning ")
                    .append("and emotional wellbeing may be affected. It is important to acknowledge these ")
                    .append("feelings and take proactive steps to address them. You are not alone in experiencing ")
                    .append("these challenges, and there are effective strategies and support systems available to help you.\n\n");
        } else {
            analysis.append("Your responses reflect significant levels of distress across multiple areas. ")
                    .append("High scores such as these suggest that you may be experiencing considerable difficulty ")
                    .append("in your day-to-day life, and it is important to take these signals seriously. ")
                    .append("Reaching out to a mental health professional would be a meaningful and important step ")
                    .append("toward getting the support you deserve. You do not need to navigate this alone.\n\n");
        }

        // ── DETAILED RESPONSE ANALYSIS ──
        analysis.append("DETAILED RESPONSE ANALYSIS\n");
        analysis.append("═══════════════════════════════════════════════════\n\n");

        for (Question question : questions) {
            Integer score = scores.get(question.getQuestionId());
            String answer = originalAnswers.get(question.getQuestionId());
            if (score == null || answer == null) continue;

            analysis.append("Question: ").append(question.getText()).append("\n");
            analysis.append("Your Answer: ").append(answer).append("  |  Score: ").append(score).append("/4\n");
            analysis.append("Analysis: ").append(generateDetailedInsight(question.getText(), score, answer)).append("\n\n");
        }

        // ── PATTERNS DETECTED ──
        analysis.append("PATTERNS DETECTED\n");
        analysis.append("═══════════════════════════════════════════════════\n\n");

        if (concerningCount == 0) {
            analysis.append("No significant concerning patterns were detected in your responses. ")
                    .append("Your answers are consistent with someone who is maintaining good mental health ")
                    .append("and effectively managing life's everyday stressors.\n\n");
        } else {
            analysis.append("Out of ").append(questions.size()).append(" questions assessed, ")
                    .append(concerningCount).append(" responses scored at a concerning level (3 or above), ")
                    .append("and ").append(severeCount).append(" responses were at the highest severity level (4/4). ");

            if (concerningCount >= questions.size() / 2) {
                analysis.append("The breadth of elevated responses across more than half of the assessment ")
                        .append("suggests a pattern of widespread impact rather than isolated difficulties. ")
                        .append("This pattern warrants careful attention and support.\n\n");
            } else {
                analysis.append("These concerns appear to be concentrated in specific areas rather than widespread, ")
                        .append("which suggests targeted support strategies may be particularly effective.\n\n");
            }

            if (!concernAreas.isEmpty()) {
                analysis.append("Areas that showed elevated responses:\n");
                for (String area : concernAreas) {
                    analysis.append("  - ").append(area).append("\n");
                }
                analysis.append("\n");
            }
        }

        if (!positiveAreas.isEmpty()) {
            analysis.append("On a positive note, you scored low (indicating fewer difficulties) on:\n");
            for (String area : positiveAreas) {
                analysis.append("  - ").append(area).append("\n");
            }
            analysis.append("\nThese are genuine strengths to build upon.\n\n");
        }

        // ── PERSONALIZED RECOMMENDATIONS ──
        analysis.append("PERSONALIZED RECOMMENDATIONS\n");
        analysis.append("═══════════════════════════════════════════════════\n\n");

        if (riskLevel.equalsIgnoreCase("low") || riskLevel.equalsIgnoreCase("minimal")) {
            analysis.append("Given your low risk assessment, the focus should be on maintenance and prevention. ")
                    .append("Regular physical activity, consistent sleep schedules, and meaningful social connections ")
                    .append("are the pillars of sustained mental wellness. Consider journaling as a way to stay ")
                    .append("in touch with your emotional state over time. Periodic self-check-ins like this assessment ")
                    .append("are a great habit to continue so that you can catch any changes early.\n\n");
        } else if (riskLevel.equalsIgnoreCase("mild") || riskLevel.equalsIgnoreCase("moderate")) {
            analysis.append("At this level, proactive self-care becomes especially important. Begin by identifying ")
                    .append("the specific stressors or triggers that may be contributing to your elevated scores. ")
                    .append("Mindfulness practices such as deep breathing, meditation, or yoga have strong evidence ")
                    .append("behind them for reducing moderate stress and anxiety. Establishing a consistent daily ")
                    .append("routine can also provide a sense of stability and control. If you notice your scores ")
                    .append("staying elevated or worsening over time, speaking with a counselor or therapist would ")
                    .append("be a valuable next step — and it is a sign of strength, not weakness, to seek help.\n\n");
        } else {
            analysis.append("Your scores suggest that you would benefit significantly from professional support. ")
                    .append("A qualified mental health professional can work with you to develop a personalized ")
                    .append("plan that addresses your specific challenges. In the meantime, try to maintain basic ")
                    .append("self-care routines: regular meals, adequate sleep, and gentle movement. ")
                    .append("Reach out to someone you trust and share how you are feeling. ")
                    .append("Crisis support is also available 24/7 if you ever feel overwhelmed.\n\n");
        }

        // ── CLOSING NOTE ──
        analysis.append("CLOSING NOTE\n");
        analysis.append("═══════════════════════════════════════════════════\n\n");
        analysis.append("Taking the time to reflect on your mental health is an act of genuine courage and self-awareness. ")
                .append("Regardless of your score, the fact that you completed this assessment shows that you care ")
                .append("about your wellbeing — and that is always a meaningful first step.\n\n");
        analysis.append("This analysis is generated for informational and reflective purposes only. ")
                .append("It is not a clinical diagnosis and should not replace professional medical or psychological evaluation. ")
                .append("If you are concerned about your mental health, please reach out to a qualified professional. ")
                .append("You deserve support, and help is always available.\n");

        return analysis.toString();
    }

    /**
     * Per-question insight used by the rule-based fallback.
     */
    private String generateDetailedInsight(String questionText, int score, String answer) {
        String text = questionText.toLowerCase();

        if (text.contains("sleep") || text.contains("insomnia")) {
            switch (score) {
                case 0: case 1: return "Your sleep patterns appear to be healthy and restorative. Good sleep is foundational to emotional regulation and cognitive function, and maintaining this is a real asset to your overall wellbeing.";
                case 2: return "You are experiencing some sleep disruptions. Occasional sleep difficulties are common, but if this persists, consider establishing a consistent bedtime routine and limiting screen time before sleep.";
                case 3: return "Your sleep is being noticeably affected. Poor sleep can significantly worsen mood, concentration, and stress tolerance. Sleep hygiene improvements such as a dark, quiet environment and fixed wake times may help.";
                case 4: return "Severe sleep difficulties are present. Chronic sleep deprivation has serious effects on both mental and physical health. This is an area that would benefit from professional attention, potentially including a sleep assessment.";
            }
        } else if (text.contains("anxi") || text.contains("worry") || text.contains("nervous")) {
            switch (score) {
                case 0: case 1: return "You appear to be managing anxiety and worry effectively. This suggests strong coping skills and a relatively stable sense of security in your daily life.";
                case 2: return "Moderate anxiety is present. Some degree of worry is normal, but if it is interfering with your focus or enjoyment, grounding techniques and regular relaxation practices can provide meaningful relief.";
                case 3: return "Anxiety is occurring frequently and may be affecting your daily functioning. Cognitive-behavioral strategies, regular physical exercise, and limiting caffeine can help manage persistent anxiety.";
                case 4: return "Severe anxiety is indicated. At this level, anxiety can be debilitating and significantly impact quality of life. Professional support through therapy or medical evaluation is strongly recommended.";
            }
        } else if (text.contains("sad") || text.contains("depress") || text.contains("hopeless") || text.contains("mood")) {
            switch (score) {
                case 0: case 1: return "Your mood appears stable and you are not reporting significant low mood or sadness. This is a positive indicator of emotional resilience.";
                case 2: return "You are experiencing some low mood. This can often be linked to situational stressors. Engaging in activities you enjoy and maintaining social connections can help lift your mood.";
                case 3: return "Persistent low mood is present. When sadness or hopelessness is frequent, it can drain motivation and affect relationships. Speaking with a counselor can help you explore and address the underlying causes.";
                case 4: return "Severe low mood or feelings of hopelessness are reported. These are significant symptoms that deserve professional attention. Please consider reaching out to a mental health provider or a trusted person in your life.";
            }
        } else if (text.contains("stress") || text.contains("overwhelm") || text.contains("pressure")) {
            switch (score) {
                case 0: case 1: return "Your stress levels appear well-managed. Effective stress management is a key protective factor for both mental and physical health.";
                case 2: return "Moderate stress is present. This is common in daily life, but it is worth identifying your key stressors and building in regular recovery time such as breaks, hobbies, and social time.";
                case 3: return "High stress is reported. Chronic stress takes a toll on the body and mind. Time management strategies, delegation where possible, and regular relaxation are important tools to implement now.";
                case 4: return "You are experiencing overwhelming stress. This level of stress can lead to burnout and health complications. Prioritizing stress reduction and seeking support are both urgent and important.";
            }
        } else if (text.contains("energy") || text.contains("tired") || text.contains("fatigue")) {
            switch (score) {
                case 0: case 1: return "Your energy levels appear adequate. Maintaining good nutrition, sleep, and exercise will help sustain this.";
                case 2: return "Some fatigue is present. This could be related to sleep quality, nutrition, or emotional load. Paying attention to rest and recovery is worthwhile.";
                case 3: return "Low energy is significantly impacting you. Persistent fatigue can be a symptom of depression, poor sleep, or medical issues. It is worth discussing with a healthcare provider.";
                case 4: return "Severe fatigue or energy depletion is reported. This level of exhaustion warrants medical and psychological evaluation to identify and address the root causes.";
            }
        } else if (text.contains("interest") || text.contains("pleasure") || text.contains("enjoy")) {
            switch (score) {
                case 0: case 1: return "You are maintaining interest and pleasure in activities, which is an important marker of positive mental health and engagement with life.";
                case 2: return "Some reduction in interest or pleasure is noted. This can happen during stressful periods. Deliberately scheduling enjoyable activities can help restore motivation.";
                case 3: return "Loss of interest in activities you used to enjoy is a notable concern and is one of the key indicators of depression. Connecting with others and gentle re-engagement with hobbies may help.";
                case 4: return "Significant loss of interest or pleasure is reported. This is a core symptom of depression and should be discussed with a mental health professional as soon as possible.";
            }
        }

        // Generic fallback by score
        switch (score) {
            case 0: case 1: return "Your response here indicates a low level of difficulty, which is a positive sign. Continue to monitor this area as part of your regular self-care routine.";
            case 2: return "A moderate response was recorded for this area. While not alarming, it is worth being mindful of and considering whether any lifestyle adjustments might help.";
            case 3: return "An elevated response was recorded here, suggesting this area is having a notable impact on your wellbeing. Targeted attention and support in this area would be beneficial.";
            case 4: return "A maximum score was recorded for this question, indicating this is a significant area of difficulty for you. This specific concern deserves focused attention and professional support.";
            default: return "Response recorded and considered in your overall assessment.";
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  DATA ACCESS METHODS
    // ═══════════════════════════════════════════════════════════════

    public List<AssessmentResult> getUserResults(int userId) throws SQLException {
        return resultService.getResultsByUser(userId);
    }

    public List<AssessmentResult> getAllResults() throws SQLException {
        List<AssessmentResult> results = new ArrayList<>();
        try {
            Connection conn = MyDB.getInstance().getConnection();
            String query = "SELECT * FROM assessmentresult ORDER BY taken_at DESC";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                AssessmentResult result = new AssessmentResult();
                result.setResultId(rs.getInt("result_id"));
                result.setUserId(rs.getInt("user_id"));
                result.setAssessmentId(rs.getInt("assessment_id"));
                result.setTotalScore(rs.getInt("total_score"));
                result.setRiskLevel(rs.getString("risk_level"));
                result.setInterpretation(rs.getString("interpretation"));
                result.setRecommendedContent(rs.getString("recommended_content"));
                result.setSuggestSession(rs.getBoolean("suggest_session"));
                result.setTakenAt(rs.getTimestamp("taken_at"));
                results.add(result);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all results: " + e.getMessage());
            throw e;
        }
        return results;
    }

    public void deleteResult(int resultId) throws SQLException {
        resultService.deleteResult(resultId);
    }

    public List<Question> getQuestionsByAssessment(int assessmentId) {
        try {
            return questionService.getQuestionsByAssessment(assessmentId);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Map<String, Object> getUserStatistics(int userId) throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        List<AssessmentResult> results = resultService.getResultsByUser(userId);

        stats.put("totalAssessments", results.size());

        if (!results.isEmpty()) {
            int totalScore = 0;
            int highRiskCount = 0;
            Date latestDate = results.get(0).getTakenAt();

            for (AssessmentResult result : results) {
                totalScore += result.getTotalScore();
                if ("High".equalsIgnoreCase(result.getRiskLevel()) ||
                        "Severe".equalsIgnoreCase(result.getRiskLevel())) {
                    highRiskCount++;
                }
                if (result.getTakenAt().after(latestDate)) {
                    latestDate = result.getTakenAt();
                }
            }

            stats.put("averageScore", results.size() > 0 ? totalScore / results.size() : 0);
            stats.put("highRiskPercentage", results.size() > 0 ? (highRiskCount * 100) / results.size() : 0);
            stats.put("latestAssessment", latestDate);
        }

        return stats;
    }

    // ═══════════════════════════════════════════════════════════════
    //  ANSWER / SCALE PARSING
    // ═══════════════════════════════════════════════════════════════

    public int parseAnswerToScore(String answer, String scale) {
        if (answer == null || answer.trim().isEmpty()) return 0;

        String answerLower = answer.toLowerCase().trim();
        System.out.println("Parsing answer: '" + answer + "' from scale: '" + scale + "'");

        if (answer.contains(" - ")) {
            try {
                return Integer.parseInt(answer.split(" - ")[0].trim());
            } catch (NumberFormatException e) { }
        }

        try {
            return Integer.parseInt(answer.trim());
        } catch (NumberFormatException e) { }

        if (answerLower.contains("always") || answerLower.contains("very often") ||
                answerLower.contains("nearly every day") || answerLower.contains("constantly")) {
            return 4;
        } else if (answerLower.contains("often") || answerLower.contains("frequently") ||
                answerLower.contains("more than half") || answerLower.contains("high")) {
            return 3;
        } else if (answerLower.contains("sometimes") || answerLower.contains("occasionally") ||
                answerLower.contains("several days") || answerLower.contains("moderate") ||
                answerLower.contains("medium")) {
            return 2;
        } else if (answerLower.contains("rarely") || answerLower.contains("seldom") ||
                answerLower.contains("a little") || answerLower.contains("low")) {
            return 1;
        } else if (answerLower.contains("never") || answerLower.contains("not at all") ||
                answerLower.contains("none")) {
            return 0;
        } else if (answerLower.contains("yes")) {
            return 1;
        } else if (answerLower.contains("no")) {
            return 0;
        }

        return 2;
    }

    public String[] parseScaleToOptions(String scale) {
        if (scale == null || scale.trim().isEmpty()) {
            return new String[]{"1", "2", "3", "4", "5"};
        }

        scale = scale.trim();

        if (scale.contains("=")) {
            String[] items = scale.split(",");
            List<String> options = new ArrayList<>();
            for (String item : items) {
                if (item.contains("=")) {
                    String[] parts = item.split("=");
                    if (parts.length >= 2) {
                        options.add(parts[0].trim() + " - " + parts[1].trim());
                    }
                }
            }
            if (!options.isEmpty()) return options.toArray(new String[0]);
        }

        if (scale.contains("/")) {
            return scale.split("/");
        }

        if (scale.contains("-")) {
            try {
                String[] range = scale.split("-");
                int start = Integer.parseInt(range[0].trim());
                int end = Integer.parseInt(range[1].trim());
                List<String> options = new ArrayList<>();
                for (int i = start; i <= end; i++) options.add(String.valueOf(i));
                return options.toArray(new String[0]);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) { }
        }

        return new String[]{"1", "2", "3", "4", "5"};
    }

    // ═══════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════

    private String generateInterpretation(String riskLevel, String aiAnalysis) {
        String base;
        switch (riskLevel.toLowerCase()) {
            case "low":
            case "minimal":
                base = "Your scores indicate minimal concerns in this area."; break;
            case "moderate":
            case "mild":
                base = "Your scores suggest some areas that may need attention."; break;
            case "high":
            case "severe":
                base = "Your scores indicate significant concerns that should be addressed."; break;
            default:
                base = "Assessment completed.";
        }
        return base + " Please review your AI analysis for personalized insights.";
    }

    private String generateRecommendedContent(String riskLevel, int assessmentId, String aiAnalysis) {
        StringBuilder content = new StringBuilder();
        content.append("Based on your assessment results:\n");

        switch (riskLevel.toLowerCase()) {
            case "low":
            case "minimal":
                content.append("- Continue with healthy habits\n");
                content.append("- Mindfulness practices for maintenance\n");
                content.append("- Regular exercise routine\n");
                break;
            case "moderate":
            case "mild":
                content.append("- Stress management techniques\n");
                content.append("- Self-help resources and books\n");
                content.append("- Consider talking to a counselor\n");
                if (aiAnalysis.toLowerCase().contains("sleep")) {
                    content.append("- Sleep hygiene improvement strategies\n");
                }
                if (aiAnalysis.toLowerCase().contains("anxi")) {
                    content.append("- Anxiety reduction exercises\n");
                }
                break;
            case "high":
            case "severe":
                content.append("- Professional consultation recommended\n");
                content.append("- Support groups available\n");
                content.append("- Crisis hotline: 1-800-273-8255\n");
                content.append("- Comprehensive evaluation suggested\n");
                break;
        }

        return content.toString();
    }

    private boolean shouldSuggestSession(String riskLevel, String aiAnalysis) {
        if ("high".equalsIgnoreCase(riskLevel) || "severe".equalsIgnoreCase(riskLevel)) return true;
        if (aiAnalysis.toLowerCase().contains("professional") &&
                aiAnalysis.toLowerCase().contains("significant")) return true;
        return false;
    }

    private String determineRiskLevel(int totalScore, int assessmentId) {
        if (assessmentId == 2) {
            if (totalScore <= 3) return "Minimal";
            else if (totalScore <= 6) return "Mild";
            else if (totalScore <= 9) return "Moderate";
            else return "Severe";
        } else if (assessmentId == 1) {
            if (totalScore <= 4) return "Low";
            else if (totalScore <= 8) return "Moderate";
            else return "High";
        } else {
            int maxScore = 12;
            double percentage = (double) totalScore / maxScore * 100;
            if (percentage <= 25) return "Low";
            else if (percentage <= 50) return "Mild";
            else if (percentage <= 75) return "Moderate";
            else return "High";
        }
    }

    private int getLatestResultId(int userId) throws SQLException {
        List<AssessmentResult> results = resultService.getResultsByUser(userId);
        if (!results.isEmpty()) return results.get(0).getResultId();
        return -1;
    }

    // ═══════════════════════════════════════════════════════════════
    //  EXPORT METHODS
    // ═══════════════════════════════════════════════════════════════

    public String exportResultToText(AssessmentResult result, String aiAnalysis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return  "========================================\n" +
                "          MENTIS ASSESSMENT REPORT      \n" +
                "========================================\n\n" +
                "Report Generated: " + new Date() + "\n" +
                "Result ID: " + result.getResultId() + "\n" +
                "User ID: " + result.getUserId() + "\n" +
                "Assessment ID: " + result.getAssessmentId() + "\n" +
                "Date Taken: " + sdf.format(result.getTakenAt()) + "\n\n" +
                "========================================\n" +
                "               SCORE SUMMARY            \n" +
                "========================================\n" +
                "Total Score: " + result.getTotalScore() + "\n" +
                "Risk Level: " + result.getRiskLevel() + "\n" +
                "Session Recommended: " + (result.isSuggestSession() ? "Yes" : "No") + "\n\n" +
                "========================================\n" +
                "            AI ANALYSIS REPORT          \n" +
                "========================================\n" +
                aiAnalysis + "\n\n" +
                "========================================\n" +
                "            INTERPRETATION              \n" +
                "========================================\n" +
                result.getInterpretation() + "\n\n" +
                "========================================\n" +
                "           RECOMMENDATIONS              \n" +
                "========================================\n" +
                result.getRecommendedContent() + "\n\n" +
                "========================================\n" +
                "        DISCLAIMER & IMPORTANT INFO     \n" +
                "========================================\n" +
                "This report is generated by an AI system for informational purposes only.\n" +
                "It is not a substitute for professional medical advice, diagnosis, or treatment.\n" +
                "If you are experiencing a mental health emergency, please call your local\n" +
                "emergency number or the National Suicide Prevention Lifeline at 1-800-273-8255.\n\n" +
                "Confidentiality: This report contains sensitive information.\n" +
                "Please store it securely and share only with trusted healthcare providers.";
    }

    public String exportResultToHTML(AssessmentResult result, String aiAnalysis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return "<!DOCTYPE html>\n<html>\n<head>\n" +
                "    <title>Mentis Assessment Report</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; margin: 40px; line-height: 1.6; }\n" +
                "        .header { background-color: #6c9e83; color: white; padding: 20px; text-align: center; }\n" +
                "        .section { margin: 30px 0; border-left: 4px solid #6c9e83; padding-left: 15px; }\n" +
                "        .risk-low { color: green; font-weight: bold; }\n" +
                "        .risk-moderate { color: orange; font-weight: bold; }\n" +
                "        .risk-high { color: red; font-weight: bold; }\n" +
                "        .disclaimer { background-color: #f8f8f8; padding: 15px; border: 1px solid #ddd; margin-top: 30px; }\n" +
                "        table { width: 100%; border-collapse: collapse; margin: 20px 0; }\n" +
                "        th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }\n" +
                "        th { background-color: #f2f2f2; }\n" +
                "    </style>\n</head>\n<body>\n" +
                "    <div class=\"header\"><h1>MENTIS ASSESSMENT REPORT</h1>" +
                "<p>AI-Powered Mental Wellness Analysis</p></div>\n" +
                "    <div class=\"section\"><h2>Report Information</h2><table>\n" +
                "        <tr><th>Report Generated</th><td>" + new Date() + "</td></tr>\n" +
                "        <tr><th>Result ID</th><td>" + result.getResultId() + "</td></tr>\n" +
                "        <tr><th>User ID</th><td>" + result.getUserId() + "</td></tr>\n" +
                "        <tr><th>Assessment ID</th><td>" + result.getAssessmentId() + "</td></tr>\n" +
                "        <tr><th>Date Taken</th><td>" + sdf.format(result.getTakenAt()) + "</td></tr>\n" +
                "    </table></div>\n" +
                "    <div class=\"section\"><h2>Score Summary</h2><table>\n" +
                "        <tr><th>Total Score</th><td>" + result.getTotalScore() + "</td></tr>\n" +
                "        <tr><th>Risk Level</th><td class=\"risk-" +
                result.getRiskLevel().toLowerCase() + "\">" + result.getRiskLevel() + "</td></tr>\n" +
                "        <tr><th>Session Recommended</th><td>" +
                (result.isSuggestSession() ? "Yes" : "No") + "</td></tr>\n" +
                "    </table></div>\n" +
                "    <div class=\"section\"><h2>AI Analysis Report</h2>\n" +
                "        <pre style=\"white-space: pre-wrap; background-color: #f8f8f8; padding: 15px;\">" +
                aiAnalysis.replace("\n", "<br>") + "</pre></div>\n" +
                "    <div class=\"section\"><h2>Interpretation</h2><p>" +
                result.getInterpretation() + "</p></div>\n" +
                "    <div class=\"section\"><h2>Recommendations</h2><p>" +
                result.getRecommendedContent().replace("\n", "<br>") + "</p></div>\n" +
                "    <div class=\"disclaimer\">\n" +
                "        <h3>Disclaimer & Important Information</h3>\n" +
                "        <p>This report is generated by an AI system for informational purposes only.</p>\n" +
                "        <p>It is not a substitute for professional medical advice, diagnosis, or treatment.</p>\n" +
                "        <p>Crisis line: 1-800-273-8255</p>\n" +
                "        <p><strong>Confidentiality:</strong> Please store securely and share only with trusted healthcare providers.</p>\n" +
                "    </div>\n</body>\n</html>";
    }
}