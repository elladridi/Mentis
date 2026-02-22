package controllers;

import services.GeminiService;
import javafx.fxml.FXMLLoader;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import models.Goal;
import services.GoalService;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.IOException;
import javafx.concurrent.Task;
import java.sql.SQLException;
import java.time.LocalDate;
import javafx.application.Platform;

public class GoalController {
    // Éléments de la Table
    @FXML private TableView<Goal> tableGoals;
    @FXML private TableColumn<Goal, String> colDescription;
    @FXML private TableColumn<Goal, LocalDate> colDeadline;
    @FXML private TableColumn<Goal, Integer> colProgress;
    @FXML private TableColumn<Goal, String> colStatus;

    // Éléments du Formulaire
    @FXML private TextField inputGoal;
    @FXML private DatePicker inputDeadline;
    @FXML private Slider inputProgress;
    @FXML private ComboBox<String> comboStatus;
    @FXML private TextField inputDescription;

    // Zone de réponse de l'IA
    @FXML private TextArea aiResponseArea;

    private final GoalService gs = new GoalService();
    private Goal selectedGoal = null;

    @FXML
    public void initialize() {
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDeadline.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        colProgress.setCellValueFactory(new PropertyValueFactory<>("progress"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        setupUI();

        if (comboStatus != null) {
            comboStatus.setItems(FXCollections.observableArrayList("En cours", "Terminé", "En attente"));
        }
        refreshTable();
    }

    private void setupUI() {
        try {
            var resource = getClass().getResource("/fxml/bag.png");
            if (resource != null) {
                VBox root = (VBox) tableGoals.getParent();
                root.setAlignment(javafx.geometry.Pos.CENTER);
                root.setPadding(new javafx.geometry.Insets(20));
                root.setStyle("-fx-background-image: url('" + resource.toExternalForm() + "'); " +
                        "-fx-background-size: cover; -fx-background-position: center;");

                tableGoals.setPrefWidth(850);
                tableGoals.setPrefHeight(350);
                tableGoals.setStyle("-fx-opacity: 0.95; -fx-background-radius: 10; -fx-focus-color: transparent;");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleUpdate() {
        if (selectedGoal == null) return;
        try {
            selectedGoal.setDescription(inputGoal.getText());
            selectedGoal.setDeadline(inputDeadline.getValue());
            selectedGoal.setProgress((int) inputProgress.getValue());
            selectedGoal.setStatus(comboStatus.getValue());

            gs.modifier(selectedGoal);
            refreshTable();
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleSave() {
        try {
            String desc = inputGoal.getText();
            LocalDate date = inputDeadline.getValue();

            if (desc == null || desc.trim().isEmpty() || date == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Champs manquants");
                alert.setContentText("Zikou, la description et la date sont obligatoires !");
                alert.showAndWait();
                return;
            }

            Goal g = new Goal(desc, date, (int) inputProgress.getValue(), comboStatus.getValue());
            gs.ajouter(g);
            refreshTable();
            clearFields();
            System.out.println("Succès : Objectif '" + desc + "' ajouté !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleDelete() {
        Goal selected = tableGoals.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                gs.supprimer(selected.getId());
                refreshTable();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void loadSelectedGoal() {
        selectedGoal = tableGoals.getSelectionModel().getSelectedItem();
        if (selectedGoal != null) {
            inputGoal.setText(selectedGoal.getDescription());
            inputDeadline.setValue(selectedGoal.getDeadline());
            inputProgress.setValue(selectedGoal.getProgress());
            comboStatus.setValue(selectedGoal.getStatus());
        }
    }

    @FXML
    public void backToHome(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/HomeView.fxml"));
        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(root);
        Stage stage = (Stage) scene.getWindow();
        stage.setTitle("MENTIS - Accueil");
    }

    public void refreshTable() {
        try {
            tableGoals.setItems(FXCollections.observableArrayList(gs.recupererTout()));
            tableGoals.refresh(); // Force le dessin des lignes
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        inputGoal.clear();
        inputDeadline.setValue(null);
        inputProgress.setValue(0);
        comboStatus.getSelectionModel().clearSelection();
        selectedGoal = null;
    }



    // --- LOGIQUE IA AVEC FENÊTRE POP-UP ---
    @FXML
    public void handleAIStrategy(ActionEvent event) {
        try {
            if (inputDescription == null) {
                System.out.println("ERREUR : Le TextField n'est pas lié.");
                return;
            }

            String myGoal = inputDescription.getText();

            if (myGoal == null || myGoal.trim().isEmpty()) {
                // On peut aussi mettre une petite alerte ici
                return;
            }

            // Optionnel : tu peux garder ce message pour montrer que l'IA travaille
            if (aiResponseArea != null) aiResponseArea.setText("Mentis AI is thinking...");

            Task<String> aiTask = new Task<>() {
                @Override
                protected String call() throws Exception {
                    return services.GeminiService.getGoalAdvice(myGoal);
                }
            };

            // --- C'EST ICI QUE ÇA CHANGE ---
            aiTask.setOnSucceeded(e -> {
                String rawResponse = aiTask.getValue();
                // On nettoie la réponse avec ta méthode parseResponse
                String cleanAdvice = services.GeminiService.parseResponse(rawResponse);

                // On affiche la Pop-up au lieu du TextArea
                showAIAlert(myGoal, cleanAdvice);

                // On efface le "Thinking..." du bas
                if (aiResponseArea != null) aiResponseArea.setText("");
            });

            aiTask.setOnFailed(e -> {
                if (aiResponseArea != null) aiResponseArea.setText("AI Error.");
                aiTask.getException().printStackTrace();
            });

            new Thread(aiTask).start();

        } catch (Exception e) {
            System.out.println("Erreur imprévue : " + e.getMessage());
        }
    }
    // LA MÉTHODE QUI CRÉE LA FENÊTRE POP-UP (La partie qui te plaisait)
    private void showAIAlert(String goal, String advice) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mentis AI - Stratégie de Succès");
        alert.setHeaderText("Conseils pour l'objectif : " + goal);

        // Création d'un TextArea interne pour que le texte soit lisible et copiable
        TextArea textArea = new TextArea(advice);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefHeight(300); // On lui donne de la place !
        textArea.setPrefWidth(500);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }
} // <--- Fermeture de la classe
