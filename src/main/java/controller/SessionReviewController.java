package controller;

import models.SessionReview;
import services.SessionReviewService;

import java.sql.SQLException;
import java.util.List;

public class SessionReviewController {

    private SessionReviewService reviewService;

    public SessionReviewController() {
        this.reviewService = new SessionReviewService();
    }

    // Add a review
    public void addReview(int sessionId, int patientId, int rating, String comment) throws SQLException {
        if (rating < 1 || rating > 5) {
            throw new SQLException("Rating must be between 1 and 5");
        }

        SessionReview review = new SessionReview(sessionId, patientId, rating, comment);
        reviewService.addReview(review);
    }

    // Get all reviews by patient
    public List<SessionReview> getMyReviews(int patientId) throws SQLException {
        return reviewService.getReviewsByPatient(patientId);
    }

    // Get reviews for a specific session
    public List<SessionReview> getSessionReviews(int sessionId) throws SQLException {
        return reviewService.getReviewsBySession(sessionId);
    }

    // Get review by ID
    public SessionReview getReviewById(int reviewId) throws SQLException {
        return reviewService.getReviewById(reviewId);
    }

    // Update a review
    public void updateReview(int reviewId, int patientId, int rating, String comment) throws SQLException {
        SessionReview review = new SessionReview();
        review.setReviewId(reviewId);
        review.setPatientId(patientId);
        review.setRating(rating);
        review.setComment(comment);

        reviewService.updateReview(review); // Fixed: Added closing parenthesis
    }

    // Delete a review
    public void deleteReview(int reviewId, int patientId) throws SQLException {
        reviewService.deleteReview(reviewId, patientId);
    }

    // Check if patient already reviewed a session
    public boolean hasReviewed(int sessionId, int patientId) throws SQLException {
        return reviewService.hasPatientReviewed(sessionId, patientId);
    }

    // Get average rating for a session
    public double getAverageRating(int sessionId) throws SQLException {
        return reviewService.getAverageRating(sessionId);
    }

    // Get review count for a session
    public int getReviewCount(int sessionId) throws SQLException {
        return reviewService.getReviewCount(sessionId);
    }
}