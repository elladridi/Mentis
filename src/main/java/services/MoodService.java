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

    public java.util.Map<String, Integer> getFeelingCounts() throws SQLException {
        java.util.Map<String, Integer> counts = new java.util.LinkedHashMap<>();
        // Initialize with standard categories to match legend
        counts.put("Very Happy", 0); counts.put("Happy", 0);
        counts.put("Neutral", 0); counts.put("Sad", 0); counts.put("Very Sad", 0);

        String sql = "SELECT feeling, COUNT(*) as count FROM mood GROUP BY feeling";
        Statement st = getConnection().createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            String f = rs.getString("feeling").toLowerCase();
            if (f.contains("motivé") || f.contains("super")) counts.put("Very Happy", counts.get("Very Happy") + rs.getInt("count"));
            else if (f.contains("heureux") || f.contains("bien")) counts.put("Happy", counts.get("Happy") + rs.getInt("count"));
            else if (f.contains("triste") || f.contains("énervé")) counts.put("Sad", counts.get("Sad") + rs.getInt("count"));
            else if (f.contains("mal") || f.contains("très triste")) counts.put("Very Sad", counts.get("Very Sad") + rs.getInt("count"));
            else counts.put("Neutral", counts.get("Neutral") + rs.getInt("count"));
        }
        return counts;
    }

    public java.util.Map<String, Double> getMoodTrendLast7Days() throws SQLException {
        java.util.Map<String, Double> trends = new java.util.LinkedHashMap<>();
        String sql = "SELECT DATE(created_at) as date, AVG(CASE " +
                     "WHEN feeling LIKE '%motivé%' OR feeling LIKE '%super%' THEN 5 " +
                     "WHEN feeling LIKE '%heureux%' OR feeling LIKE '%bien%' THEN 4 " +
                     "WHEN feeling LIKE '%triste%' OR feeling LIKE '%énervé%' THEN 2 " +
                     "WHEN feeling LIKE '%mal%' OR feeling LIKE '%très triste%' THEN 1 " +
                     "ELSE 3 END) as avg_mood " +
                     "FROM mood WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
                     "GROUP BY DATE(created_at) ORDER BY date ASC";
        Statement st = getConnection().createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            trends.put(rs.getDate("date").toString(), rs.getDouble("avg_mood"));
        }
        return trends;
    }
}