package controller;

import models.Assessment;
import models.Question;
import services.AssessmentService;
import services.QuestionService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuestionController {
    private QuestionService questionService;
    private AssessmentService assessmentService;


    public QuestionController() {
        this.questionService = new QuestionService();
        this.assessmentService = new AssessmentService();
    }

    // Create question with Question object (for dialog)
    public void createQuestion(Question question) throws SQLException {
        questionService.createQuestion(question);
    }

    // Update question with Question object
    public void updateQuestion(Question question) throws SQLException {
        // First check what methods are available in the service
        try {
            // Try to call updateQuestion(Question) if it exists
            questionService.updateQuestion(question);
        } catch (Exception e) {
            // If that fails, try to update using other available methods
            // You might need to implement updateQuestion(int, String, String) in service
            throw new SQLException("Cannot update question: " + e.getMessage());
        }
    }

    // Old method for backward compatibility
    public void addQuestion(int assessmentId, String text, String scale) throws SQLException {
        Question question = new Question(assessmentId, text, scale);
        questionService.updateQuestion(question);
    }

    // Get questions by assessment ID
    public List<Question> getQuestionsByAssessment(int assessmentId) throws SQLException {
        return questionService.getQuestionsByAssessment(assessmentId);
    }

    // Get all questions (for admin view)
    public List<Question> getAllQuestions() throws SQLException {
        List<Assessment> assessments = assessmentService.getAllAssessments();
        List<Question> allQuestions = new ArrayList<>();

        for (Assessment assessment : assessments) {
            List<Question> questions = questionService.getQuestionsByAssessment(assessment.getAssessmentId());
            allQuestions.addAll(questions);
        }

        return allQuestions;
    }

    // Delete a question
    public void deleteQuestion(int questionId) throws SQLException {
        questionService.deleteQuestion(questionId);
    }

    // Get assessment for a specific question
    public Assessment getAssessmentForQuestion(int questionId) throws SQLException {
        List<Question> allQuestions = getAllQuestions();
        for (Question question : allQuestions) {
            if (question.getQuestionId() == questionId) {
                List<Assessment> assessments = assessmentService.getAllAssessments();
                for (Assessment assessment : assessments) {
                    if (assessment.getAssessmentId() == question.getAssessmentId()) {
                        return assessment;
                    }
                }
            }
        }
        return null;
    }

    public List<Assessment> getAllAssessments() throws SQLException {
        return assessmentService.getAllAssessments();
    }

    // Count questions in an assessment
    public int countQuestionsInAssessment(int assessmentId) throws SQLException {
        List<Question> questions = questionService.getQuestionsByAssessment(assessmentId);
        return questions.size();
    }
}