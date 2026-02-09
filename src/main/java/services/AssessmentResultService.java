package services;

import models.AssessmentResult;
import utils.DatabaseConnection;
import utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssessmentResultService {

    private Connection cnx;

    public AssessmentResultService() {
        cnx = MyDB.getInstance().getConnection();
    }

    public void addResult(AssessmentResult r) throws SQLException {
        String sql = "INSERT INTO AssessmentResult(user_id, assessment_id, total_score, risk_level, " +
                "interpretation, recommended_content, suggest_session, taken_at) " +
                "VALUES(?,?,?,?,?,?,?,?)";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, r.getUserId());
        ps.setInt(2, r.getAssessmentId());
        ps.setInt(3, r.getTotalScore());
        ps.setString(4, r.getRiskLevel());
        ps.setString(5, r.getInterpretation());
        ps.setString(6, r.getRecommendedContent());
        ps.setBoolean(7, r.isSuggestSession());
        ps.setDate(8, new java.sql.Date(r.getTakenAt().getTime()));

        ps.executeUpdate();
        System.out.println("Assessment result added!");
    }

    // READ ALL results for a user
    public List<AssessmentResult> getResultsByUser(int userId) throws SQLException {
        List<AssessmentResult> list = new ArrayList<>();

        String sql = "SELECT * FROM AssessmentResult WHERE user_id=" + userId;
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            AssessmentResult r = new AssessmentResult();
            r.setResultId(rs.getInt("result_id"));
            r.setUserId(rs.getInt("user_id"));
            r.setAssessmentId(rs.getInt("assessment_id"));
            r.setTotalScore(rs.getInt("total_score"));
            r.setRiskLevel(rs.getString("risk_level"));
            r.setInterpretation(rs.getString("interpretation"));
            r.setRecommendedContent(rs.getString("recommended_content"));
            r.setSuggestSession(rs.getBoolean("suggest_session"));
            r.setTakenAt(rs.getDate("taken_at"));

            list.add(r);
        }
        return list;
    }

    // DELETE a result
    public void deleteResult(int resultId) throws SQLException {
        String sql = "DELETE FROM AssessmentResult WHERE result_id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, resultId);
        ps.executeUpdate();
        System.out.println("Assessment result deleted!");
    }
}
