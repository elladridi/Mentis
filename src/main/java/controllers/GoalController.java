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
import javafx.stage.Modality;

public class GoalController {
    @FXML private TableView<Goal> tableGoals;
    @FXML private TableColumn<Goal, String> colDescription;
    @FXML private TableColumn<Goal, LocalDate> colDeadline;
    @FXML private TableColumn<Goal, Integer> colProgress;
    @FXML private TableColumn<Goal, String> colStatus;
    @FXML private TextField inputGoal;
    @FXML private DatePicker inputDeadline;
    @FXML private Slider inputProgress;
    @FXML private ComboBox<String> comboStatus;
    @FXML private TextField inputDescription;
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
        if (comboStatus != null) comboStatus.setItems(FXCollections.observableArrayList("En cours", "Terminé", "En attente"));
        refreshTable();
    }

    private void setupUI() {
        try {
            var resource = getClass().getResource("/fxml/bag.png");
            if (resource != null && tableGoals != null) {
                VBox root = (VBox) tableGoals.getParent();
                if (root != null) {
                    root.setAlignment(javafx.geometry.Pos.CENTER);
                    root.setPadding(new javafx.geometry.Insets(20));
                    root.setStyle("-fx-background-image: url('" + resource.toExternalForm() + "'); -fx-background-size: cover; -fx-background-position: center;");
                }
                tableGoals.setPrefWidth(850);
                tableGoals.setPrefHeight(350);
                tableGoals.setStyle("-fx-opacity: 0.95; -fx-background-radius: 10; -fx-focus-color: transparent;");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML public void handleUpdate() {
        if (selectedGoal == null) return;
        try {
            selectedGoal.setDescription(inputGoal.getText());
            selectedGoal.setDeadline(inputDeadline.getValue());
            selectedGoal.setProgress((int) inputProgress.getValue());
            selectedGoal.setStatus(comboStatus.getValue());
            gs.modifier(selectedGoal);
            refreshTable(); clearFields();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML public void handleSave() {
        try {
            String desc = inputGoal.getText();
            LocalDate date = inputDeadline.getValue();
            if (desc == null || desc.trim().isEmpty() || date == null) {
                new Alert(Alert.AlertType.WARNING, "La description et la date sont obligatoires !").showAndWait();
                return;
            }
            Goal g = new Goal(desc, date, (int) inputProgress.getValue(), comboStatus.getValue());
            gs.ajouter(g);
            refreshTable(); clearFields();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML public void handleDelete() {
        Goal selected = tableGoals.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try { gs.supprimer(selected.getId()); refreshTable(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @FXML public void loadSelectedGoal() {
        selectedGoal = tableGoals.getSelectionModel().getSelectedItem();
        if (selectedGoal != null) {
            inputGoal.setText(selectedGoal.getDescription());
            inputDeadline.setValue(selectedGoal.getDeadline());
            inputProgress.setValue(selectedGoal.getProgress());
            comboStatus.setValue(selectedGoal.getStatus());
        }
    }

    @FXML public void backToHome(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/HomeView.fxml"));
        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(root);
        ((Stage) scene.getWindow()).setTitle("MENTIS - Accueil");
    }

    public void refreshTable() {
        try {
            tableGoals.setItems(FXCollections.observableArrayList(gs.recupererTout()));
            tableGoals.refresh();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void clearFields() {
        inputGoal.clear();
        inputDeadline.setValue(null);
        inputProgress.setValue(0);
        if (comboStatus != null) comboStatus.getSelectionModel().clearSelection();
        selectedGoal = null;
    }

    @FXML public void handleAIStrategy(ActionEvent event) {
        String myGoal = (inputDescription != null && !inputDescription.getText().isEmpty()) ? inputDescription.getText() : (inputGoal != null ? inputGoal.getText() : "");
        if (myGoal == null || myGoal.trim().isEmpty()) {
            if (aiResponseArea != null) aiResponseArea.setText("Please enter a goal description first.");
            return;
        }
        
        if (aiResponseArea != null) aiResponseArea.setText("Mentis AI is thinking... Generating strategy for: " + myGoal);
        
        Task<String> aiTask = new Task<>() {
            @Override
            protected String call() throws Exception { 
                return GeminiService.getGoalAdvice(myGoal); 
            }
        };
        
        aiTask.setOnSucceeded(e -> {
            String cleanAdvice = GeminiService.parseResponse(aiTask.getValue());
            if (aiResponseArea != null) {
                aiResponseArea.setText(cleanAdvice);
            }
        });
        
        aiTask.setOnFailed(e -> { 
            if (aiResponseArea != null) aiResponseArea.setText("AI Error: Could not connect to the strategy service."); 
            aiTask.getException().printStackTrace();
        });
        
        new Thread(aiTask).start();
    }

    @FXML public void openChatWindow(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ChatView.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Mentis AI - Discussion");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}
