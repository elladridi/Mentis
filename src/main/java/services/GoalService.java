package services;

import models.Goal;
import utils.z.MyDBMentis;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GoalService {
    private Connection cnx;

    public GoalService() {
        // On récupère la connexion via le Singleton MyDB
        cnx = MyDBMentis.getInstance().getConnection();
    }

    private Connection getConnection() {
        if (cnx == null) {
            cnx = MyDBMentis.getInstance().getConnection();
        }
        return cnx;
    }

    // CREATE : Ajouter un objectif
    public void ajouter(Goal g) throws SQLException {
        String sql = "INSERT INTO goal (title, description, deadline, is_completed, user_id, created_at, updated_at) VALUES (?, ?, ?, ?, 1, NOW(), NOW())";
        PreparedStatement ps = getConnection().prepareStatement(sql);
        ps.setString(1, g.getDescription()); // Mapping description to title
        ps.setString(2, ""); // Empty description
        ps.setDate(3, Date.valueOf(g.getDeadline()));
        ps.setInt(4, g.getProgress() > 0 ? 1 : 0); // is_completed (0 or 1)
        ps.executeUpdate();
        System.out.println("Objectif ajouté !");
    }

    // UPDATE : Modifier un objectif existant
    public void modifier(Goal g) throws SQLException {
        String sql = "UPDATE goal SET title=?, description=?, deadline=?, is_completed=?, updated_at=NOW() WHERE id=?";
        PreparedStatement ps = getConnection().prepareStatement(sql);
        ps.setString(1, g.getDescription());
        ps.setString(2, "");
        ps.setDate(3, Date.valueOf(g.getDeadline()));
        ps.setInt(4, g.getProgress() > 0 ? 1 : 0);
        ps.setInt(5, g.getId());
        ps.executeUpdate();
        System.out.println("Objectif mis à jour !");
    }

    // DELETE : Supprimer un objectif par son ID
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM goal WHERE id = ?";
        PreparedStatement ps = getConnection().prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Objectif supprimé !");
    }

    public List<Goal> recupererTout() throws SQLException {
        List<Goal> objectifs = new ArrayList<>();
        String sql = "SELECT * FROM goal ORDER BY deadline ASC";
        Statement st = getConnection().createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Goal g = new Goal();
            g.setId(rs.getInt("id"));
            g.setDescription(rs.getString("title"));
            g.setDeadline(rs.getDate("deadline").toLocalDate());
            g.setProgress(rs.getInt("is_completed"));
            g.setStatus(g.getProgress() > 0 ? "Terminé" : "En cours");
            objectifs.add(g);
        }
        return objectifs;
    }

    public java.util.Map<String, Integer> getGoalStatusCounts() throws SQLException {
        java.util.Map<String, Integer> counts = new java.util.LinkedHashMap<>();
        String sql = "SELECT is_completed, COUNT(*) as count FROM goal GROUP BY is_completed";
        Statement st = getConnection().createStatement();
        ResultSet rs = st.executeQuery(sql);
        int completed = 0, pending = 0;
        while (rs.next()) {
            if (rs.getInt("is_completed") == 1) completed = rs.getInt("count");
            else pending = rs.getInt("count");
        }
        counts.put("Completed", completed);
        counts.put("Pending", pending);
        return counts;
    }
}