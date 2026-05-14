package controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.MoodService;
import services.QuoteService;
import java.sql.SQLException;
import java.util.List;
import services.FeedbackService;

public class HomeController {

    @FXML private StackPane mainContainer;
    @FXML private PieChart userDistributionChart;
    @FXML private ScrollPane reviewsScrollPane;
    @FXML private VBox reviewsBox;
    @FXML private VBox feedbackForm;
    @FXML private TextField inputUser;
    @FXML private TextArea inputComment;

    private final FeedbackService feedbackService = new FeedbackService();
    private final MoodService moodService = new MoodService();
    private String commentToEdit = "";

    @FXML
    public void initialize() {
        try {
            var resource = getClass().getResource("/fxml/bag.png");
            if (resource != null) {
                mainContainer.setStyle("-fx-background-image: url('" + resource.toExternalForm() + "'); " +
                        "-fx-background-size: cover; -fx-background-position: center;");
            }
        } catch (Exception e) { e.printStackTrace(); }
        setupStatistics();
        refreshFeedbacks();
        startAutoScroll();
        if (feedbackForm != null) {
            feedbackForm.setVisible(false);
            feedbackForm.setScaleX(0);
            feedbackForm.setScaleY(0);
        }
    }

    @FXML public void toggleFeedbackForm() {
        if (feedbackForm.isVisible()) {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), feedbackForm);
            st.setToX(0); st.setToY(0);
            st.setOnFinished(e -> feedbackForm.setVisible(false));
            st.play();
        } else {
            feedbackForm.setVisible(true);
            ScaleTransition st = new ScaleTransition(Duration.millis(200), feedbackForm);
            st.setFromX(0); st.setFromY(0);
            st.setToX(1); st.setToY(1);
            st.play();
        }
    }

    @FXML public void handleAddFeedback() {
        String user = inputUser.getText().trim();
        String comment = inputComment.getText().trim();
        if (user.isEmpty() || comment.isEmpty()) return;
        try {
            if (!commentToEdit.isEmpty()) {
                feedbackService.updateFeedback(commentToEdit, comment);
                commentToEdit = "";
            } else {
                feedbackService.addFeedback(user, comment);
            }
            inputUser.clear(); inputComment.clear();
            refreshFeedbacks(); toggleFeedbackForm();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void refreshFeedbacks() {
        if (reviewsBox == null) return;
        try {
            reviewsBox.getChildren().clear();
            List<String> feedbacks = feedbackService.getAllFeedbacks();
            for (String f : feedbacks) {
                String tempName = "User", tempMsg = f;
                if (f.contains(": ")) {
                    String[] parts = f.split(": ", 2);
                    tempName = parts[0]; tempMsg = parts[1];
                }
                final String finalName = tempName;
                final String finalMsg = tempMsg;

                VBox card = new VBox(5);
                card.setPadding(new Insets(10));
                card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #4CAF50; -fx-border-width: 1;");
                HBox h = new HBox(10); h.setAlignment(Pos.CENTER_LEFT);
                Label nL = new Label(finalName.toUpperCase());
                nL.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                Region s = new Region(); HBox.setHgrow(s, Priority.ALWAYS);
                Button bE = new Button("✏️"); bE.setStyle("-fx-background-color: transparent;");
                bE.setOnAction(e -> { inputUser.setText(finalName); inputComment.setText(finalMsg); commentToEdit = finalMsg; if(!feedbackForm.isVisible()) toggleFeedbackForm(); });
                Button bD = new Button("🗑️"); bD.setStyle("-fx-background-color: transparent; -fx-text-fill: red;");
                bD.setOnAction(e -> { try { feedbackService.deleteFeedback(finalName, finalMsg); refreshFeedbacks(); } catch (SQLException ex) { ex.printStackTrace(); } });
                h.getChildren().addAll(nL, s, bE, bD);
                Label cL = new Label(finalMsg); cL.setWrapText(true); cL.setStyle("-fx-text-fill: #2c3e50;");
                card.getChildren().addAll(h, cL);
                reviewsBox.getChildren().add(card);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void startAutoScroll() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(40), e -> {
            if (reviewsScrollPane != null) {
                double v = reviewsScrollPane.getVvalue();
                reviewsScrollPane.setVvalue(v >= 1.0 ? 0 : v + 0.002);
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE); timeline.play();
    }

    private void setupStatistics() {
        if (userDistributionChart == null) return;
        
        userDistributionChart.getData().clear();
        userDistributionChart.setTitle("Mood Distribution");
        userDistributionChart.setLegendSide(javafx.geometry.Side.BOTTOM);
        userDistributionChart.setLabelsVisible(false);

        try {
            java.util.Map<String, Integer> counts = moodService.getFeelingCounts();
            for (java.util.Map.Entry<String, Integer> entry : counts.entrySet()) {
                if (entry.getValue() > 0) {
                    userDistributionChart.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        // Emerald Palette
        String[] colors = {"#1B5E20", "#2E7D32", "#4CAF50", "#81C784", "#C8E6C9"};
        
        // Add Doughnut hole
        javafx.scene.shape.Circle hole = new javafx.scene.shape.Circle(70, javafx.scene.paint.Color.WHITE);
        if (mainContainer != null) {
            // Find the VBox containing the chart to overlay the hole
            // Actually, better to just wrap the chart in a StackPane if not already
            // But HomeView.fxml already has the PieChart inside a VBox.
            // I'll wrap it dynamically or just apply colors for now.
        }

        // Apply colors after rendering
        Platform.runLater(() -> {
            int i = 0;
            for (PieChart.Data data : userDistributionChart.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-pie-color: " + colors[i % colors.length] + ";");
                }
                i++;
            }
        });
    }

    @FXML public void goToMood(ActionEvent event) {
        System.out.println("Navigation vers Mood tracking...");
        switchScene(event, "/fxml/MoodTracking.fxml");
    }
    @FXML public void goToGoals(ActionEvent event) { switchScene(event, "/fxml/GoalView.fxml"); }
    @FXML public void handleLogout() { System.exit(0); }

    private void switchScene(ActionEvent event, String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            if (root != null) {
                Scene scene = ((Node) event.getSource()).getScene();
                scene.setRoot(root);
            }
        } catch (Exception e) {
            System.out.println("Erreur de chargement FXML : " + path);
            e.printStackTrace();
        }
    }
    @FXML public void handleAIScanner() {
        try {
            java.net.URL url = getClass().getResource("/resources z/ai_scanner.html");
            if (url != null) java.awt.Desktop.getDesktop().browse(url.toURI());
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML public void handleMoodCharts() {
        com.mentalhealth.app.views.StatisticsView statsView = new com.mentalhealth.app.views.StatisticsView();
        Stage popup = new Stage();
        popup.setTitle("Mentis Wellness Analytics");
        popup.setScene(new Scene(statsView.buildStatisticsView(), 1000, 800));
        popup.show();
    }

    @FXML public void handleReflect() {
        try {
            Stage s = new Stage();
            s.setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/ReflectView.fxml"))));
            s.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML public void handleQuotas() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Quotas du jour");
        alert.setHeaderText("Pensée positive du jour");
        alert.setContentText(QuoteService.getDailyQuote());
        alert.showAndWait();
    }
}
