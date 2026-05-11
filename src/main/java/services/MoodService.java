package services;

import models.Mood;
import utils.z.MyDBMentis;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MoodService {
    private Connection cnx;

    public MoodService() {
        cnx = MyDBMentis.getInstance().getConnection();
    }

    private Connection getConnection() {
        if (cnx == null) {
            cnx = MyDBMentis.getInstance().getConnection();
        }
        return cnx;
    }

    public void addMood(Mood m) throws SQLException {
        String sql = "INSERT INTO mood (feeling, note, created_at, updated_at, user_id) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = getConnection().prepareStatement(sql);
        ps.setString(1, m.getFeeling());
        ps.setString(2, m.getNote());
        ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
        ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
        ps.setInt(5, 1); // Default user_id = 1
        ps.executeUpdate();
    }

    public List<Mood> getAllMoods() throws SQLException {
        List<Mood> list = new ArrayList<>();
        String sql = "SELECT * FROM mood ORDER BY created_at DESC";
        Statement st = getConnection().createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            Mood mood = new Mood(
                    rs.getString("feeling"),
                    rs.getString("note"),
                    rs.getTimestamp("created_at").toLocalDateTime()
            );
            mood.setId(rs.getInt("id"));
            list.add(mood);
        }
        return list;
    }

    public void deleteMoodById(int id) throws SQLException {
        String sql = "DELETE FROM mood WHERE id = ?";
        PreparedStatement ps = getConnection().prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public void updateMoodById(int id, Mood newMood) throws SQLException {
        String sql = "UPDATE mood SET feeling = ?, note = ?, updated_at = ? WHERE id = ?";
        PreparedStatement ps = getConnection().prepareStatement(sql);
        ps.setString(1, newMood.getFeeling());
        ps.setString(2, newMood.getNote());
        ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
        ps.setInt(4, id);
        ps.executeUpdate();
    }
}