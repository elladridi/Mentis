package ui.ui; // Indispensable car ton fichier est dans ui/ui

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ZikouLauncher extends Application {
    @Override
    public void start(Stage primaryStage) {
        // On crée ton panel
        WellbeingPanel myPanel = new WellbeingPanel(null);

        // CORRECTION : La Scene prend le panel, puis la largeur, puis la hauteur
        Scene scene = new Scene(myPanel, 1400, 800);

        primaryStage.setTitle("Test Wellbeing Mentis");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}