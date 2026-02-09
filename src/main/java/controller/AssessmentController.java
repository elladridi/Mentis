package controller;

import models.Assessment;
import services.AssessmentService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AssessmentController {
    private AssessmentService assessmentService;

    public AssessmentController() {
        this.assessmentService = new AssessmentService();
    }

    public void createAssessment(Assessment assessment) throws SQLException {
        assessmentService.addAssessment(assessment);
    }

    public void createAssessment(String title, String description, String type, String status) throws SQLException {
        Assessment assessment = new Assessment(title, description, type, status);
        assessmentService.addAssessment(assessment);
    }
    // Get all assessments
    public List<Assessment> getAllAssessments() throws SQLException {
        return assessmentService.getAllAssessments();
    }

    // Get assessment by ID
    public Assessment getAssessmentById(int assessmentId) throws SQLException {
        return assessmentService.getAssessmentById(assessmentId);
    }

    public void updateAssessment(int id, String title, String description, String type, String status) throws SQLException {
        Assessment assessment = new Assessment(title, description, type, status);
        assessment.setAssessmentId(id);
        assessmentService.updateAssessment(assessment);
    }

    public void updateAssessment(Assessment assessment) throws SQLException {
        assessmentService.updateAssessment(assessment);
    }

    // Delete an assessment
    public void deleteAssessment(int id) throws SQLException {
        assessmentService.deleteAssessment(id);
    }

    // Search assessments by type
    public List<Assessment> searchAssessmentsByType(String type) throws SQLException {
        List<Assessment> allAssessments = assessmentService.getAllAssessments();
        List<Assessment> filtered = new ArrayList<>();

        for (Assessment assessment : allAssessments) {
            if (assessment.getType().equalsIgnoreCase(type)) {
                filtered.add(assessment);
            }
        }

        return filtered;
    }

    // Get active assessments only
    public List<Assessment> getActiveAssessments() throws SQLException {
        List<Assessment> allAssessments = assessmentService.getAllAssessments();
        List<Assessment> activeAssessments = new ArrayList<>();

        for (Assessment assessment : allAssessments) {
            if ("active".equalsIgnoreCase(assessment.getStatus())) {
                activeAssessments.add(assessment);
            }
        }

        return activeAssessments;
    }
    // In AssessmentController.java
    public boolean updateAssessmentStatus(int assessmentId, String status) throws SQLException {
        try {
            // Check if assessment exists first
            Assessment assessment = assessmentService.getAssessmentById(assessmentId);
            if (assessment == null) {
                return false;
            }

            // Update status
            return assessmentService.updateAssessmentStatus(assessmentId, status);
        } catch (SQLException e) {
            throw new SQLException("Error updating assessment status: " + e.getMessage(), e);
        }
    }
}
