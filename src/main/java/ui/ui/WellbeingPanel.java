package ui.ui; // UNE SEULE LIGNE PACKAGE ICI

import ui.MentisLoginFrame; // Importe la classe du dossier parent
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class WellbeingPanel extends VBox {
    private MentisLoginFrame parentApp;

    public WellbeingPanel(MentisLoginFrame parentApp) {
        this.parentApp = parentApp;
        try {
            // Assure-toi que MoodView.fxml est bien dans src/main/resources
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MoodView.fxml"));
            Parent root = loader.load();
            this.getChildren().add(root);
            VBox.setVgrow(root, Priority.ALWAYS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}