package test;

import models.Goal;
import services.GoalService;
import java.time.LocalDate;
import java.sql.SQLException;

public class test_MainGoal {
    public static void main(String[] args) {
        GoalService gs = new GoalService();
        try {
            // 1. Test Ajout
            Goal g1 = new Goal("Apprendre JavaFX", LocalDate.now().plusDays(7), 10, "En cours");
            gs.ajouter(g1);

            // 2. Affichage
            System.out.println("Liste des objectifs : " + gs.recupererTout());

        } catch (SQLException e) {
            System.err.println("Erreur SQL : " + e.getMessage());
        }
    }
}