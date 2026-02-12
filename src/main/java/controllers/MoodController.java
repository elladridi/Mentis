package controllers;
import javafx.stage.Modality;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import models.Mood;
import services.MoodService;
import java.sql.SQLException;
import java.time.LocalDateTime;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MoodController {
    @FXML private TableView<Mood> tableMoods;
    @FXML private TableColumn<Mood, String> colFeeling;
    @FXML private TableColumn<Mood, String> colNote;
    @FXML private TableColumn<Mood, LocalDateTime> colDate;
    @FXML private TextField inputFeeling;
    @FXML private TextField inputNote;

    // Ajout du ComboBox pour les emojis
    @FXML private ComboBox<String> comboEmoji;

    private final MoodService ms = new MoodService();
    private Mood selectedMood = null;

    @FXML
    public void initialize() {
        // 1. Liaison des colonnes (Ton code ancien)
        colFeeling.setCellValueFactory(new PropertyValueFactory<>("feeling"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        // 2. Configuration du Background et des dimensions (Ton code ancien)
        try {
            var resource = getClass().getResource("/fxml/bag.png");

            if (resource != null) {
                String imageUrl = resource.toExternalForm();
                VBox root = (VBox) tableMoods.getParent();

                root.setAlignment(javafx.geometry.Pos.CENTER);
                root.setPadding(new javafx.geometry.Insets(20));

                root.setStyle(
                        "-fx-background-image: url('" + imageUrl + "'); " +
                                "-fx-background-size: cover; " +
                                "-fx-background-position: center center; " +
                                "-fx-background-repeat: no-repeat;"
                );

                // Dimensions GRANDES et suppression du contour bleu
                tableMoods.setPrefWidth(700);
                tableMoods.setMaxWidth(900);
                tableMoods.setPrefHeight(300);

                tableMoods.setStyle(
                        "-fx-opacity: 0.9; " +
                                "-fx-background-radius: 15; " +
                                "-fx-background-color: transparent; " +
                                "-fx-border-color: transparent; " +
                                "-fx-focus-color: transparent; " +
                                "-fx-faint-focus-color: transparent;"
                );

                inputFeeling.setPrefWidth(400);
                inputFeeling.setMaxWidth(500);
                inputNote.setPrefWidth(400);
                inputNote.setMaxWidth(500);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. NOUVEAU : Configuration du ComboBox d'emojis sans supprimer le texte
        if (comboEmoji != null) {
            comboEmoji.setItems(FXCollections.observableArrayList(
                    "😊 Heureux", "😐 Neutre", "😔 Triste", "😠 Énervé", "😴 Fatigué", "🚀 Motivé"
            ));
            comboEmoji.setPromptText("Ajouter un emoji rapide");

            // Quand on choisit un emoji, il s'ajoute au TextField existant
            comboEmoji.setOnAction(event -> {
                String selected = comboEmoji.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    // On garde le texte actuel et on ajoute l'emoji
                    String currentText = inputFeeling.getText();
                    inputFeeling.setText(currentText + " " + selected);
                }
            });
        }

        refreshTable();
        tableMoods.setRowFactory(tv -> new TableRow<Mood>() {
            @Override
            protected void updateItem(Mood item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    // Si la note contient "bien" ou "heureux", on colore en vert clair
                    if (item.getNote().toLowerCase().contains("bien") || item.getFeeling().contains("😊")) {
                        setStyle("-fx-background-color: #c8e6c9;"); // Vert
                    } else if (item.getNote().toLowerCase().contains("mal") || item.getFeeling().contains("😔")) {
                        setStyle("-fx-background-color: #ffcdd2;"); // Rouge
                    } else {
                        setStyle("");
                    }
                }
            }
        });
    }

    // --- TOUTES TES MÉTHODES ANCIENNES SONT ICI ---

    @FXML
    public void loadSelectedMood() {
        selectedMood = tableMoods.getSelectionModel().getSelectedItem();
        if (selectedMood != null) {
            inputFeeling.setText(selectedMood.getFeeling());
            inputNote.setText(selectedMood.getNote());
        }
    }

    @FXML
    public void handleUpdate() {
        if (selectedMood == null) return;
        try {
            Mood updated = new Mood(inputFeeling.getText(), inputNote.getText(), LocalDateTime.now());
            ms.updateMood(selectedMood, updated);
            refreshTable();
            clearFields();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    public void handleSave() {
        try {
            ms.addMood(new Mood(inputFeeling.getText(), inputNote.getText(), LocalDateTime.now()));
            refreshTable();
            clearFields();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    public void handleDelete() {
        Mood selected = tableMoods.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                ms.deleteMood(selected.getFeeling(), selected.getNote());
                refreshTable();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    @FXML
    public void openReflect(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReflectView.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("MENTIS - Daily Reflection");
        stage.initModality(Modality.APPLICATION_MODAL); // Bloque la fenêtre principale
        stage.setScene(new Scene(root));
        stage.show();
    }
    @FXML
    public void backToHome(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/HomeView.fxml"));

        // On change juste le contenu de la scène existante
        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(root);

        Stage stage = (Stage) scene.getWindow();
        stage.setTitle("MENTIS - Accueil");
    }

    public void refreshTable() {
        try {
            tableMoods.setItems(FXCollections.observableArrayList(ms.getAllMoods()));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void clearFields() {
        inputFeeling.clear();
        inputNote.clear();
        if (comboEmoji != null) comboEmoji.getSelectionModel().clearSelection();
        selectedMood = null;
    }
}