package controller;

import models.AssessmentResult;
import models.Question;
import services.AssessmentResultService;
import services.QuestionService;
import utils.MyDB;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

// Add these imports
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AssessmentResultController {

    private AssessmentResultService resultService;
    private QuestionService questionService;

    public AssessmentResultController() {
        this.resultService = new AssessmentResultService();
        this.questionService = new QuestionService();
    }

    // ========== EXISTING METHODS (Keep these) ==========

    // Submit assessment answers - UPDATED VERSION with AI analysis
    public Map<String, Object> submitAssessment(int userId, int assessmentId,
                                                Map<Integer, Integer> scores,
                                                Map<Integer, String> originalAnswers) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Get questions for context
            List<Question> questions = getQuestionsByAssessment(assessmentId);

            // Calculate total score
            int totalScore = 0;
            for (Integer score : scores.values()) {
                totalScore += score;
            }

            // Determine risk level
            String riskLevel = determineRiskLevel(totalScore, assessmentId);

            // Generate AI-powered analysis
            String aiAnalysis = generateAIAnalysis(questions, scores, originalAnswers, totalScore, riskLevel);

            // Generate recommendations based on AI analysis
            String recommendedContent = generateRecommendedContent(riskLevel, assessmentId, aiAnalysis);
            String interpretation = generateInterpretation(riskLevel, aiAnalysis);
            boolean suggestSession = shouldSuggestSession(riskLevel, aiAnalysis);

            // Save result
            AssessmentResult result = new AssessmentResult(
                    userId, assessmentId, totalScore, riskLevel,
                    interpretation, recommendedContent, suggestSession, new Date()
            );

            resultService.addResult(result);

            // Prepare response
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

    // Old method for backward compatibility (keep this)
    public Map<String, Object> submitAssessment(int userId, int assessmentId, Map<Integer, Integer> scores) {
        // Create empty original answers map for backward compatibility
        Map<Integer, String> originalAnswers = new HashMap<>();
        return submitAssessment(userId, assessmentId, scores, originalAnswers);
    }

    // Get results for a specific user
    public List<AssessmentResult> getUserResults(int userId) throws SQLException {
        return resultService.getResultsByUser(userId);
    }

    // Get all results (for admin/psychologist) - FIXED IMPLEMENTATION
    public List<AssessmentResult> getAllResults() throws SQLException {
        List<AssessmentResult> results = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // Get database connection
            conn = MyDB.getInstance().getConnection();
            String query = "SELECT * FROM assessmentresult ORDER BY taken_at DESC";
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();

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

    // Delete a result
    public void deleteResult(int resultId) throws SQLException {
        resultService.deleteResult(resultId);
    }

    // Get questions for an assessment
    public List<Question> getQuestionsByAssessment(int assessmentId) {
        try {
            return questionService.getQuestionsByAssessment(assessmentId);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Get result statistics for a user
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
                if ("High".equalsIgnoreCase(result.getRiskLevel()) || "Severe".equalsIgnoreCase(result.getRiskLevel())) {
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

    public int parseAnswerToScore(String answer, String scale) {
        if (answer == null || answer.trim().isEmpty()) {
            return 0;
        }

        String answerLower = answer.toLowerCase().trim();

        // Debug logging
        System.out.println("Parsing answer: '" + answer + "' from scale: '" + scale + "'");

        // If answer contains " - " format like "4 - Always"
        if (answer.contains(" - ")) {
            try {
                String[] parts = answer.split(" - ");
                return Integer.parseInt(parts[0].trim());
            } catch (NumberFormatException e) {
                // Continue with text parsing
            }
        }

        // If answer is a plain number
        try {
            return Integer.parseInt(answer.trim());
        } catch (NumberFormatException e) {
            // Continue with text parsing
        }

        // Parse text answers based on common mental health assessment scales
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
                answerLower.contains("no") || answerLower.contains("none")) {
            return 0;
        } else if (answerLower.contains("yes")) {
            // For yes/no questions, Yes = higher score (more symptomatic)
            return 1;
        } else if (answerLower.contains("no")) {
            return 0;
        }

        // Default medium score
        return 2;
    }

    // Parse scale to options
    public String[] parseScaleToOptions(String scale) {
        if (scale == null || scale.trim().isEmpty()) {
            return new String[]{"1", "2", "3", "4", "5"}; // Default
        }

        scale = scale.trim();

        // 1. Check for mapped scales first (e.g., "1=Never,2=Rarely,3=Sometimes")
        if (scale.contains("=")) {
            String[] items = scale.split(",");
            List<String> options = new ArrayList<>();

            for (String item : items) {
                if (item.contains("=")) {
                    String[] parts = item.split("=");
                    if (parts.length >= 2) {
                        // Store as "1 - Never" format for display
                        options.add(parts[0].trim() + " - " + parts[1].trim());
                    }
                }
            }

            if (!options.isEmpty()) {
                return options.toArray(new String[0]);
            }
        }

        // 2. Check for simple text scales (e.g., "yes/no", "never/rarely/sometimes/often/always")
        if (scale.contains("/")) {
            String[] parts = scale.split("/");
            // Return the actual text options, not numbers
            return parts;
        }

        // 3. Check for numeric range (e.g., "1-5", "1-10")
        if (scale.contains("-")) {
            try {
                String[] range = scale.split("-");
                int start = Integer.parseInt(range[0].trim());
                int end = Integer.parseInt(range[1].trim());

                List<String> options = new ArrayList<>();
                for (int i = start; i <= end; i++) {
                    options.add(String.valueOf(i));
                }
                return options.toArray(new String[0]);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                // If parsing fails, fall through to default
            }
        }

        // 4. Default fallback (should rarely be needed)
        return new String[]{"1", "2", "3", "4", "5"};
    }

    // ========== NEW AI METHODS ==========

    private String generateAIAnalysis(List<Question> questions,
                                      Map<Integer, Integer> scores,
                                      Map<Integer, String> originalAnswers,
                                      int totalScore, String riskLevel) {

        StringBuilder analysis = new StringBuilder();
        analysis.append("📊 **Assessment Analysis Report**\n\n");

        // Overall assessment
        analysis.append("### Overall Summary\n");
        analysis.append("- Total Score: ").append(totalScore).append(" out of ").append(questions.size() * 4).append("\n");
        analysis.append("- Risk Level: ").append(riskLevel).append("\n");

        // Add score interpretation
        double averageScore = totalScore / (double) questions.size();
        analysis.append("- Average Score per Question: ").append(String.format("%.2f", averageScore)).append("/4\n\n");

        // Detailed question-by-question analysis
        analysis.append("### Detailed Analysis\n");

        int concerningQuestions = 0;
        int severeQuestions = 0;

        for (Question question : questions) {
            int questionId = question.getQuestionId();
            Integer score = scores.get(questionId);
            String answer = originalAnswers.get(questionId);

            if (score != null && answer != null) {
                analysis.append("\n**Question:** ").append(question.getText()).append("\n");
                analysis.append("**Your Answer:** ").append(answer).append("\n");
                analysis.append("**Score:** ").append(score).append("/4\n");

                // Count concerning responses
                if (score >= 3) {
                    concerningQuestions++;
                    if (score == 4) severeQuestions++;
                }

                // Generate better insights
                String insight = generateEnhancedQuestionInsight(question, score, answer);
                analysis.append("**Insight:** ").append(insight).append("\n");
            }
        }

        // Pattern detection
        analysis.append("\n### Patterns Detected\n");
        String patterns = detectEnhancedPatterns(questions, scores, originalAnswers, concerningQuestions, severeQuestions);
        analysis.append(patterns);

        // Personalized insights based on actual scores
        analysis.append("\n### Personalized Insights\n");
        analysis.append(generateEnhancedInsights(questions, scores, totalScore, averageScore, concerningQuestions, severeQuestions));

        return analysis.toString();
    }

    private String generateEnhancedQuestionInsight(Question question, int score, String answer) {
        String text = question.getText().toLowerCase();

        // Based on score severity
        if (score == 4) {
            if (text.contains("sleep") || text.contains("insomnia") || text.contains("tired")) {
                return "⚠️ **Severe sleep concern** - Consistent sleep issues detected. This can significantly impact mental health.";
            } else if (text.contains("anxious") || text.contains("worry") || text.contains("nervous")) {
                return "⚠️ **High anxiety level** - Frequent anxiety may require professional support.";
            } else if (text.contains("sad") || text.contains("depress") || text.contains("hopeless")) {
                return "⚠️ **Severe mood concern** - Persistent low mood detected. Social support recommended.";
            } else if (text.contains("stress") || text.contains("overwhelm")) {
                return "⚠️ **High stress level** - Chronic stress can affect physical and mental health.";
            } else if (text.contains("interest") || text.contains("pleasure") || text.contains("enjoy")) {
                return "⚠️ **Loss of interest** - Anhedonia (loss of pleasure) is a key depression symptom.";
            } else if (text.contains("energy") || text.contains("fatigue") || text.contains("tired")) {
                return "⚠️ **Severe low energy** - This may indicate depression or other health issues.";
            }
        } else if (score == 3) {
            if (text.contains("sleep")) {
                return "🔶 **Moderate sleep concern** - Inconsistent sleep patterns noted.";
            } else if (text.contains("anxious") || text.contains("worry")) {
                return "🔶 **Moderate anxiety** - Manageable but regular anxiety detected.";
            } else if (text.contains("sad") || text.contains("depress")) {
                return "🔶 **Moderate mood concern** - Occasional low mood noted.";
            } else if (text.contains("stress")) {
                return "🔶 **Moderate stress** - Regular stress that may benefit from management techniques.";
            }
        } else if (score <= 1) {
            if (text.contains("sleep")) {
                return "✅ **Good sleep patterns** - Healthy sleep habits maintained.";
            } else if (text.contains("anxious") || text.contains("worry")) {
                return "✅ **Low anxiety** - Effective anxiety management.";
            } else if (text.contains("sad") || text.contains("depress")) {
                return "✅ **Stable mood** - Good emotional regulation.";
            } else if (text.contains("stress")) {
                return "✅ **Low stress** - Effective stress management.";
            }
        }

        // Default insights based on moderate scores
        return "📝 **Moderate response** - Within typical range. Monitor if patterns emerge.";
    }

    private String detectEnhancedPatterns(List<Question> questions, Map<Integer, Integer> scores,
                                          Map<Integer, String> originalAnswers,
                                          int concerningQuestions, int severeQuestions) {
        StringBuilder patterns = new StringBuilder();

        if (severeQuestions >= 2) {
            patterns.append("⚠️ **Multiple severe responses** - ").append(severeQuestions)
                    .append(" questions scored 4/4, indicating significant impact.\n");
        }

        if (concerningQuestions >= questions.size() / 2) {
            patterns.append("⚠️ **Widespread concerns** - Over half of responses show elevated scores.\n");
        }

        // Check for specific symptom clusters
        boolean hasSleepIssue = false;
        boolean hasMoodIssue = false;
        boolean hasAnxietyIssue = false;
        boolean hasInterestLoss = false;

        for (Question question : questions) {
            Integer score = scores.get(question.getQuestionId());
            String text = question.getText().toLowerCase();

            if (score != null && score >= 3) {
                if (text.contains("sleep")) hasSleepIssue = true;
                if (text.contains("sad") || text.contains("depress") || text.contains("hopeless")) hasMoodIssue = true;
                if (text.contains("anxious") || text.contains("worry") || text.contains("nervous")) hasAnxietyIssue = true;
                if (text.contains("interest") || text.contains("pleasure") || text.contains("enjoy")) hasInterestLoss = true;
            }
        }

        // Depression-like pattern
        if (hasMoodIssue && hasSleepIssue && hasInterestLoss) {
            patterns.append("🔍 **Depression-like pattern** - Multiple depression symptoms present.\n");
        }

        // Anxiety pattern
        if (hasAnxietyIssue && hasSleepIssue) {
            patterns.append("🔍 **Anxiety pattern** - Anxiety affecting sleep quality.\n");
        }

        if (patterns.length() == 0) {
            patterns.append("✅ **No concerning patterns detected** - Responses show typical variation.\n");
        }

        return patterns.toString();
    }

    private String generateEnhancedInsights(List<Question> questions, Map<Integer, Integer> scores,
                                            int totalScore, double averageScore,
                                            int concerningQuestions, int severeQuestions) {
        List<String> insights = new ArrayList<>();

        // Based on average score
        if (averageScore >= 3.5) {
            insights.add("**High intensity** - Multiple areas show significant concern requiring attention");
        } else if (averageScore >= 2.5) {
            insights.add("**Moderate impact** - Several areas suggest room for improvement");
        } else if (averageScore >= 1.5) {
            insights.add("**Mild concerns** - Some fluctuations noted but generally manageable");
        } else {
            insights.add("**Good baseline** - Most indicators within healthy range");
        }

        // Based on severe questions
        if (severeQuestions > 0) {
            insights.add("**" + severeQuestions + " severe responses** - These specific areas need immediate attention");
        }

        // Based on total concerning questions
        if (concerningQuestions >= 3) {
            insights.add("**Multiple elevated scores** (" + concerningQuestions + ") - Consider comprehensive evaluation");
        }

        // Add personalized advice
        if (averageScore >= 3.0) {
            insights.add("**Professional consultation recommended** - Scores suggest benefit from expert guidance");
        } else if (averageScore >= 2.0) {
            insights.add("**Self-care focus** - Implement stress management and wellness strategies");
        } else {
            insights.add("**Maintenance mode** - Continue healthy habits and regular check-ins");
        }

        // Format as bullet points
        StringBuilder result = new StringBuilder();
        for (String insight : insights) {
            result.append("• ").append(insight).append("\n");
        }
        return result.toString();
    }

    private String generateQuestionInsight(Question question, int score, String answer) {
        String text = question.getText().toLowerCase();

        if (text.contains("sleep")) {
            if (score >= 3) return "Sleep patterns may need improvement. Consider establishing a consistent sleep schedule.";
            else return "Good sleep habits are important for mental health.";
        } else if (text.contains("anxious") || text.contains("worry")) {
            if (score >= 3) return "High anxiety levels detected. Mindfulness exercises may help.";
            else return "Anxiety management appears to be effective.";
        } else if (text.contains("sad") || text.contains("depress")) {
            if (score >= 3) return "Mood indicators suggest potential concern. Social connection can help.";
            else return "Mood stability appears positive.";
        } else if (text.contains("stress")) {
            if (score >= 3) return "Stress levels are elevated. Consider stress-reduction techniques.";
            else return "Stress management appears effective.";
        } else if (text.contains("energy") || text.contains("tired")) {
            if (score >= 3) return "Low energy may indicate need for lifestyle adjustments.";
            else return "Energy levels appear satisfactory.";
        }

        return "Response noted for further monitoring.";
    }

    private String detectPatterns(List<Question> questions, Map<Integer, Integer> scores,
                                  Map<Integer, String> originalAnswers) {
        StringBuilder patterns = new StringBuilder();

        int highStressCount = 0;
        int sleepIssuesCount = 0;
        int moodConcernsCount = 0;

        for (Question question : questions) {
            String text = question.getText().toLowerCase();
            Integer score = scores.get(question.getQuestionId());

            if (score != null && score >= 3) {
                if (text.contains("stress")) highStressCount++;
                if (text.contains("sleep")) sleepIssuesCount++;
                if (text.contains("sad") || text.contains("depress") || text.contains("anxious")) moodConcernsCount++;
            }
        }

        if (highStressCount >= 2) {
            patterns.append("- Multiple stress-related concerns identified\n");
        }
        if (sleepIssuesCount >= 2) {
            patterns.append("- Consistent sleep pattern concerns\n");
        }
        if (moodConcernsCount >= 3) {
            patterns.append("- Several mood-related indicators noted\n");
        }

        if (patterns.length() == 0) {
            patterns.append("- No concerning patterns detected\n");
        }

        return patterns.toString();
    }

    private String generatePersonalizedInsights(List<Question> questions, Map<Integer, Integer> scores, int totalScore) {
        List<String> insights = new ArrayList<>();

        // Calculate average score
        double avgScore = scores.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);

        if (avgScore >= 3.5) {
            insights.add("Several areas show elevated scores suggesting multiple areas of concern");
        } else if (avgScore >= 2.5) {
            insights.add("Moderate concerns detected in some areas");
        } else {
            insights.add("Most responses indicate good mental health management");
        }

        // Check specific clusters
        long highScoringQuestions = scores.values().stream().filter(s -> s >= 4).count();
        if (highScoringQuestions >= 3) {
            insights.add("Multiple high-intensity responses suggest significant impact on daily functioning");
        }

        // Return formatted insights
        StringBuilder result = new StringBuilder();
        for (String insight : insights) {
            result.append("• ").append(insight).append("\n");
        }
        return result.toString();
    }

    private String generateInterpretation(String riskLevel, String aiAnalysis) {
        String base = "";
        switch (riskLevel.toLowerCase()) {
            case "low":
            case "minimal":
                base = "Your scores indicate minimal concerns in this area.";
                break;
            case "moderate":
            case "mild":
                base = "Your scores suggest some areas that may need attention.";
                break;
            case "high":
            case "severe":
                base = "Your scores indicate significant concerns that should be addressed.";
                break;
            default:
                base = "Assessment completed.";
        }

        return base + " The AI analysis provides personalized insights below.";
    }

    private String generateRecommendedContent(String riskLevel, int assessmentId, String aiAnalysis) {
        StringBuilder content = new StringBuilder();

        content.append("Based on your assessment results:\n");

        switch (riskLevel.toLowerCase()) {
            case "low":
            case "minimal":
                content.append("• Continue with healthy habits\n");
                content.append("• Mindfulness practices for maintenance\n");
                content.append("• Regular exercise routine\n");
                break;
            case "moderate":
            case "mild":
                content.append("• Stress management techniques\n");
                content.append("• Self-help resources and books\n");
                content.append("• Consider talking to a counselor\n");

                if (aiAnalysis.contains("sleep")) {
                    content.append("• Sleep hygiene improvement strategies\n");
                }
                if (aiAnalysis.contains("anxious")) {
                    content.append("• Anxiety reduction exercises\n");
                }
                break;
            case "high":
            case "severe":
                content.append("• Professional consultation recommended\n");
                content.append("• Support groups available\n");
                content.append("• Crisis hotline: 1-800-273-8255\n");

                if (aiAnalysis.contains("pattern")) {
                    content.append("• Comprehensive evaluation suggested\n");
                }
                break;
        }

        // Add AI-specific recommendations
        if (aiAnalysis.contains("stress") && aiAnalysis.contains("Multiple")) {
            content.append("• Consider stress management workshop\n");
        }

        return content.toString();
    }

    private boolean shouldSuggestSession(String riskLevel, String aiAnalysis) {
        if ("high".equalsIgnoreCase(riskLevel) || "severe".equalsIgnoreCase(riskLevel)) {
            return true;
        }

        // Also suggest based on AI analysis patterns
        if (aiAnalysis.contains("Multiple high-intensity") ||
                aiAnalysis.contains("significant impact")) {
            return true;
        }

        return false;
    }

    // ========== EXPORT METHODS ==========

    // Export result to text file
    public String exportResultToText(AssessmentResult result, String aiAnalysis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String export =
                "========================================\n" +
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

        return export;
    }

    // Export result to HTML
    public String exportResultToHTML(AssessmentResult result, String aiAnalysis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
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
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"header\">\n" +
                "        <h1>MENTIS ASSESSMENT REPORT</h1>\n" +
                "        <p>AI-Powered Mental Wellness Analysis</p>\n" +
                "    </div>\n" +

                "    <div class=\"section\">\n" +
                "        <h2>Report Information</h2>\n" +
                "        <table>\n" +
                "            <tr><th>Report Generated</th><td>" + new Date() + "</td></tr>\n" +
                "            <tr><th>Result ID</th><td>" + result.getResultId() + "</td></tr>\n" +
                "            <tr><th>User ID</th><td>" + result.getUserId() + "</td></tr>\n" +
                "            <tr><th>Assessment ID</th><td>" + result.getAssessmentId() + "</td></tr>\n" +
                "            <tr><th>Date Taken</th><td>" + sdf.format(result.getTakenAt()) + "</td></tr>\n" +
                "        </table>\n" +
                "    </div>\n" +

                "    <div class=\"section\">\n" +
                "        <h2>Score Summary</h2>\n" +
                "        <table>\n" +
                "            <tr><th>Total Score</th><td>" + result.getTotalScore() + "</td></tr>\n" +
                "            <tr><th>Risk Level</th><td class=\"risk-" + result.getRiskLevel().toLowerCase() + "\">" + result.getRiskLevel() + "</td></tr>\n" +
                "            <tr><th>Session Recommended</th><td>" + (result.isSuggestSession() ? "Yes" : "No") + "</td></tr>\n" +
                "        </table>\n" +
                "    </div>\n" +

                "    <div class=\"section\">\n" +
                "        <h2>AI Analysis Report</h2>\n" +
                "        <pre style=\"white-space: pre-wrap; background-color: #f8f8f8; padding: 15px;\">" +
                aiAnalysis.replace("\n", "<br>") + "</pre>\n" +
                "    </div>\n" +

                "    <div class=\"section\">\n" +
                "        <h2>Interpretation</h2>\n" +
                "        <p>" + result.getInterpretation() + "</p>\n" +
                "    </div>\n" +

                "    <div class=\"section\">\n" +
                "        <h2>Recommendations</h2>\n" +
                "        <p>" + result.getRecommendedContent().replace("\n", "<br>") + "</p>\n" +
                "    </div>\n" +

                "    <div class=\"disclaimer\">\n" +
                "        <h3>Disclaimer & Important Information</h3>\n" +
                "        <p>This report is generated by an AI system for informational purposes only.</p>\n" +
                "        <p>It is not a substitute for professional medical advice, diagnosis, or treatment.</p>\n" +
                "        <p>If you are experiencing a mental health emergency, please call your local emergency number or the National Suicide Prevention Lifeline at 1-800-273-8255.</p>\n" +
                "        <p><strong>Confidentiality:</strong> This report contains sensitive information. Please store it securely and share only with trusted healthcare providers.</p>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";

        return html;
    }

    // ========== HELPER METHODS ==========

    private String determineRiskLevel(int totalScore, int assessmentId) {
        // For depression assessment (usually 3 questions × 4 max = 12 max)
        if (assessmentId == 2) { // Depression screening
            if (totalScore <= 3) return "Minimal";
            else if (totalScore <= 6) return "Mild";
            else if (totalScore <= 9) return "Moderate";
            else return "Severe";
        }
        // For stress assessment
        else if (assessmentId == 1) {
            if (totalScore <= 4) return "Low";
            else if (totalScore <= 8) return "Moderate";
            else return "High";
        }
        // Default for 3 questions (max 12)
        else {
            int maxScore = 12; // 3 questions × 4 points each
            double percentage = (double) totalScore / maxScore * 100;

            if (percentage <= 25) return "Low";
            else if (percentage <= 50) return "Mild";
            else if (percentage <= 75) return "Moderate";
            else return "High";
        }
    }

    private int getLatestResultId(int userId) throws SQLException {
        List<AssessmentResult> results = resultService.getResultsByUser(userId);
        if (!results.isEmpty()) {
            // Assuming results are ordered by date with latest first
            return results.get(0).getResultId();
        }
        return -1;
    }
}