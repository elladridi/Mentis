package services;
 
import utils.z.MyDBMentis;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
 
public class FeedbackService {
    private Connection conn;
 
    private Connection getConn() throws SQLException {
        try {
            if (conn == null || conn.isClosed()) {
                conn = MyDBMentis.getInstance().getConnection();
            }
            return conn;
        } catch (RuntimeException ex) {
            throw new SQLException("Database unavailable for feedback service.", ex);
        }
    }
 
    public void addFeedback(String user, String msg) throws SQLException {
        String req = "INSERT INTO feedback (username, comment) VALUES (?, ?)";
        PreparedStatement ps = getConn().prepareStatement(req);
        ps.setString(1, user);
        ps.setString(2, msg);
        ps.executeUpdate();
    }
 
    public void deleteFeedback(String username, String comment) throws SQLException {
        String req = "DELETE FROM feedback WHERE username = ? AND comment = ?";
        PreparedStatement ps = getConn().prepareStatement(req);
        ps.setString(1, username);
        ps.setString(2, comment);
        ps.executeUpdate();
    }
 
    public void updateFeedback(String oldComment, String newComment) throws SQLException {
        String req = "UPDATE feedback SET comment = ? WHERE comment = ?";
        PreparedStatement ps = getConn().prepareStatement(req);
        ps.setString(1, newComment);
        ps.setString(2, oldComment);
        ps.executeUpdate();
    }
 
    public List<String> getAllFeedbacks() throws SQLException {
        List<String> list = new ArrayList<>();
        String req = "SELECT username, comment FROM feedback ORDER BY date_added DESC";
        Statement st = getConn().createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            list.add(rs.getString("username") + ": " + rs.getString("comment"));
        }
        return list;
    }
}
