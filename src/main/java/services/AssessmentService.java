package services;

import models.Assessment;
import utils.DatabaseConnection;
import utils.DatabaseConnection;
import utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssessmentService {

    private Connection cnx;

    public AssessmentService() {
        cnx = MyDB.getInstance().getConnection();
    }

    // CREATE - Updated to include image path
    public void addAssessment(Assessment a) throws SQLException {
        String sql = "INSERT INTO Assessment(title, description, type, status, image_path) VALUES(?,?,?,?,?)";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, a.getTitle());
        ps.setString(2, a.getDescription());
        ps.setString(3, a.getType());
        ps.setString(4, a.getStatus());
        ps.setString(5, a.getImagePath());

        ps.executeUpdate();
        System.out.println("Assessment added!");
    }

    public List<Assessment> getAllAssessments() throws SQLException {
        List<Assessment> list = new ArrayList<>();

        String sql = "SELECT * FROM Assessment";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Assessment a = new Assessment();
            a.setAssessmentId(rs.getInt("assessment_id"));
            a.setTitle(rs.getString("title"));
            a.setDescription(rs.getString("description"));
            a.setType(rs.getString("type"));
            a.setStatus(rs.getString("status"));
            a.setImagePath(rs.getString("image_path"));

            list.add(a);
        }
        return list;
    }

    // UPDATE - Updated to include image path
    public void updateAssessment(Assessment a) throws SQLException {
        String sql = "UPDATE Assessment SET title=?, description=?, type=?, status=?, image_path=? WHERE assessment_id=?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, a.getTitle());
        ps.setString(2, a.getDescription());
        ps.setString(3, a.getType());
        ps.setString(4, a.getStatus());
        ps.setString(5, a.getImagePath());
        ps.setInt(6, a.getAssessmentId());

        ps.executeUpdate();
        System.out.println("Assessment updated!");
    }

    // DELETE
    public void deleteAssessment(int id) throws SQLException {
        String sql = "DELETE FROM Assessment WHERE assessment_id=?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();

        System.out.println("Assessment deleted!");
    }

    // Get assessment by ID
    public Assessment getAssessmentById(int id) throws SQLException {
        String sql = "SELECT * FROM Assessment WHERE assessment_id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Assessment a = new Assessment();
            a.setAssessmentId(rs.getInt("assessment_id"));
            a.setTitle(rs.getString("title"));
            a.setDescription(rs.getString("description"));
            a.setType(rs.getString("type"));
            a.setStatus(rs.getString("status"));
            a.setImagePath(rs.getString("image_path"));
            return a;
        }
        return null;
    }

    public boolean updateAssessmentStatus(int assessmentId, String status) throws SQLException {
        String query = "UPDATE assessment SET status = ? WHERE assessment_id = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, assessmentId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating assessment status: " + e.getMessage());
            throw e;
        }
    }
}