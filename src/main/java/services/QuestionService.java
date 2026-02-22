package services;

import models.Question;
import utils.DatabaseConnection;
import utils.DatabaseConnection;
import utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionService {

    private Connection cnx;

    public QuestionService() {
        cnx = MyDB.getInstance().getConnection();
    }

    // Add this method to your QuestionService class
    public void updateQuestion(Question question) throws SQLException {
        String query = "UPDATE question SET assessment_id = ?, text = ?, scale = ? WHERE question_id = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, question.getAssessmentId());
            pstmt.setString(2, question.getText());
            pstmt.setString(3, question.getScale());
            pstmt.setInt(4, question.getQuestionId());

            pstmt.executeUpdate();
        }
    }

    public boolean deleteQuestion(int questionId) throws SQLException {
        String query = "DELETE FROM question WHERE question_id = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, questionId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    // Make sure you have this addQuestion method
    public void createQuestion(Question question) throws SQLException {
        String query = "INSERT INTO question (assessment_id, text, scale) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, question.getAssessmentId());
            pstmt.setString(2, question.getText());
            pstmt.setString(3, question.getScale());

            pstmt.executeUpdate();

            // Get the generated ID
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    question.setQuestionId(rs.getInt(1));
                }
            }
        }
    }

    public List<Question> getQuestionsByAssessment(int assessmentId) throws SQLException {
        List<Question> list = new ArrayList<>();

        String sql = "SELECT * FROM Question WHERE assessment_id=" + assessmentId;
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Question q = new Question();
            q.setQuestionId(rs.getInt("question_id"));
            q.setAssessmentId(rs.getInt("assessment_id"));
            q.setText(rs.getString("text"));
            q.setScale(rs.getString("scale"));
            list.add(q);
        }
        return list;
    }

    // Add this method for getting a question by ID
    public Question getQuestionById(int questionId) throws SQLException {
        String sql = "SELECT * FROM Question WHERE question_id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, questionId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Question q = new Question();
            q.setQuestionId(rs.getInt("question_id"));
            q.setAssessmentId(rs.getInt("assessment_id"));
            q.setText(rs.getString("text"));
            q.setScale(rs.getString("scale"));
            return q;
        }
        return null;
    }


    // Get all questions
    public List<Question> getAllQuestions() throws SQLException {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT * FROM Question";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Question q = new Question();
            q.setQuestionId(rs.getInt("question_id"));
            q.setAssessmentId(rs.getInt("assessment_id"));
            q.setText(rs.getString("text"));
            q.setScale(rs.getString("scale"));
            list.add(q);
        }
        return list;
    }

}