package services;

import models.Mood;
import utils.MyDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MoodService {
    private Connection cnx;

    public MoodService() {
        cnx = MyDB.getInstance().getConnection();
    }

    public void addMood(Mood m) throws SQLException {
        String sql = "INSERT INTO mood(feeling, note, date) VALUES (?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, m.getFeeling());
        ps.setString(2, m.getNote());
        ps.setTimestamp(3, Timestamp.valueOf(m.getDate()));
        ps.executeUpdate();
    }

    public List<Mood> getAllMoods() throws SQLException {
        List<Mood> list = new ArrayList<>();
        String sql = "SELECT * FROM mood";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            list.add(new Mood(
                    rs.getString("feeling"),
                    rs.getString("note"),
                    rs.getTimestamp("date").toLocalDateTime()
            ));
        }
        return list;
    }

    public void deleteMood(String feeling, String note) throws SQLException {
        String sql = "DELETE FROM mood WHERE feeling = ? AND note = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, feeling);
        ps.setString(2, note);
        ps.executeUpdate();
    }
    public void updateMood(Mood oldMood, Mood newMood) throws SQLException {
        String sql = "UPDATE mood SET feeling = ?, note = ?, date = ? WHERE feeling = ? AND note = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, newMood.getFeeling());
        ps.setString(2, newMood.getNote());
        ps.setTimestamp(3, Timestamp.valueOf(newMood.getDate()));
        ps.setString(4, oldMood.getFeeling());
        ps.setString(5, oldMood.getNote());
        ps.executeUpdate();
    }
}