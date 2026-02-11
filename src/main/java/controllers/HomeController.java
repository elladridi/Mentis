package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;

public class HomeController {

    @FXML private StackPane mainContainer;
    @FXML private Button btnGoals;
    @FXML private Button btnSessions;
    @FXML private Button btnAssessment;
    @FXML private PieChart userDistributionChart;
    @FXML private ScrollPane reviewsScrollPane;
    @FXML private VBox reviewsBox;

    @FXML
    public void initialize() {
        // 1. Background
        try {
            var resource = getClass().getResource("/fxml/bag.png");
            if (resource != null) {
                mainContainer.setStyle("-fx-background-image: url('" + resource.toExternalForm() + "'); " +
                        "-fx-background-size: cover; " +
                        "-fx-background-position: center; " +
                        "-fx-background-repeat: no-repeat;");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        setupStatistics();
        setupReviews();
        startAutoScroll();
        applyFloatingAnimation(mainContainer);
    }

    private void setupStatistics() {
        if (userDistributionChart != null) {
            int countPatients = 124;
            int countPsychos = 38;

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    new PieChart.Data("Patients (" + countPatients + ")", countPatients),
                    new PieChart.Data("Psychologues (" + countPsychos + ")", countPsychos)
            );

            userDistributionChart.setData(pieChartData);
            userDistributionChart.setLegendSide(Side.BOTTOM);
            userDistributionChart.setLabelsVisible(true);
        }
    }

    private void setupReviews() {
        if (reviewsBox != null) {
            reviewsBox.getChildren().clear();

            // Données fictives pour les avis
            String[][] reviewData = {
                    {"Sarah J.", "⭐ Mentis has truly revolutionized my routine!"},
                    {"Mark Wilson", "The interface is so zen and peaceful, love it."},
                    {"Elena R.", "The mood tracking feature is incredibly accurate."},
                    {"Dr. Aris", "A huge thanks to the professional psychologists!"},
                    {"James L.", "Simple, efficient, and beautifully designed."},
                    {"Sophia K.", "Finally an app that understands mental health."}
            };

            for (String[] data : reviewData) {
                String userName = data[0];
                String userMsg = data[1];

                // 1. La Carte principale (VBox)
                VBox card = new VBox();
                card.setSpacing(10);
                card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 4); " +
                        "-fx-border-color: #E8F5E9; -fx-border-radius: 15;");

                // 2. En-tête (Avatar + Nom) dans une HBox
                javafx.scene.layout.HBox header = new javafx.scene.layout.HBox(10);
                header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                // Création d'un Avatar circulaire (Cercle avec initiale)
                StackPane avatarFrame = new StackPane();
                javafx.scene.shape.Circle avatarCircle = new javafx.scene.shape.Circle(18, javafx.scene.paint.Color.web("#4CAF50"));
                Label initial = new Label(userName.substring(0, 1));
                initial.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");
                avatarFrame.getChildren().addAll(avatarCircle, initial);

                VBox nameContainer = new VBox(2);
                Label nameLabel = new Label(userName);
                nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 13;");
                Label dateLabel = new Label("Recently");
                dateLabel.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 10;");
                nameContainer.getChildren().addAll(nameLabel, dateLabel);

                header.getChildren().addAll(avatarFrame, nameContainer);

                // 3. Le message
                Label text = new Label(userMsg);
                text.setWrapText(true);
                text.setStyle("-fx-text-fill: #576574; -fx-font-size: 13; -fx-font-style: italic;");

                card.getChildren().addAll(header, text);
                reviewsBox.getChildren().add(card);
            }
        }
    }

    private void startAutoScroll() {
        if (reviewsScrollPane != null) {
            Timeline timeline = new Timeline(new KeyFrame(Duration.millis(30), e -> {
                double vValue = reviewsScrollPane.getVvalue();
                if (vValue >= reviewsScrollPane.getVmax()) {
                    reviewsScrollPane.setVvalue(0);
                } else {
                    reviewsScrollPane.setVvalue(vValue + 0.003); // Vitesse fluide
                }
            }));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        }
    }

    @FXML
    public void goToMood(ActionEvent event) throws IOException {
        switchScene(event, "/fxml/MoodView.fxml", "MENTIS - Mood Tracker");
    }

    @FXML
    public void goToGoals(ActionEvent event) throws IOException {
        switchScene(event, "/fxml/GoalView.fxml", "MENTIS - Goal Tracking");
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        System.exit(0);
    }

    private void applyFloatingAnimation(Node node) {
        FadeTransition fade = new FadeTransition(Duration.millis(800), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);

        TranslateTransition translate = new TranslateTransition(Duration.millis(800), node);
        translate.setFromY(30);
        translate.setToY(0);

        new ParallelTransition(fade, translate).play();
    }

    public void setupUser(String role) {
        System.out.println("Utilisateur connecté : " + role);
    }

    private void switchScene(ActionEvent event, String fxmlFile, String title) throws IOException {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Scene currentScene = ((Node) event.getSource()).getScene();
            currentScene.setRoot(root);
            Stage stage = (Stage) currentScene.getWindow();
            stage.setTitle(title);
            stage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}