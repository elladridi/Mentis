package services;

import models.Goal;
import utils.MyDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GoalService {
    private Connection cnx;

    public GoalService() {
        // On récupère la connexion via le Singleton MyDB
        cnx = MyDB.getInstance().getConnection();
    }

    // CREATE : Ajouter un objectif
    public void ajouter(Goal g) throws SQLException {
        String sql = "INSERT INTO goal(description, deadline, progress, status) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, g.getDescription());
        ps.setDate(2, Date.valueOf(g.getDeadline()));
        ps.setInt(3, g.getProgress());
        ps.setString(4, g.getStatus());
        ps.executeUpdate();
        System.out.println("Objectif ajouté !");
    }

    // UPDATE : Modifier un objectif existant
    public void modifier(Goal g) throws SQLException {
        String sql = "UPDATE goal SET description=?, deadline=?, progress=?, status=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, g.getDescription());
        ps.setDate(2, Date.valueOf(g.getDeadline()));
        ps.setInt(3, g.getProgress());
        ps.setString(4, g.getStatus());
        ps.setInt(5, g.getId());
        ps.executeUpdate();
        System.out.println("Objectif mis à jour !");
    }

    // DELETE : Supprimer un objectif par son ID
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM goal WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Objectif supprimé !");
    }

    // READ : Récupérer tous les objectifs
    public List<Goal> recupererTout() throws SQLException {
        List<Goal> objectifs = new ArrayList<>();
        String sql = "SELECT * FROM goal";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Goal g = new Goal();
            g.setId(rs.getInt("id"));
            g.setDescription(rs.getString("description"));
            g.setDeadline(rs.getDate("deadline").toLocalDate());
            g.setProgress(rs.getInt("progress"));
            g.setStatus(rs.getString("status"));
            objectifs.add(g);
        }
        return objectifs;
    }
}