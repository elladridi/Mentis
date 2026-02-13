package ui;

import controller.SessionController;
import controller.SessionReviewController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import models.Session;
import services.RecommendationService;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RecommendationsPanel extends VBox {

    private MentisLoginFrame parentApp;
    private SessionController sessionController;
    private SessionReviewController reviewController;
    private RecommendationService recommendationService;
    private VBox recommendationsContainer;
    private Label userInfoLabel;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    // Colors
    private static final Color BACKGROUND_LIGHT = Color.rgb(240, 248, 245);
    private static final Color ACCENT_DARK_GREEN = Color.rgb(60, 120, 90);
    private static final Color BUTTON_LIGHT_GREEN = Color.rgb(160, 200, 180);
    private static final Color TEXT_DARK = Color.rgb(40, 70, 50);
    private static final Color TEXT_LIGHT = Color.rgb(100, 130, 110);
    private static final Color BORDER_LIGHT = Color.rgb(200, 220, 210);
    private static final Color RECOMMENDATION_COLOR = Color.rgb(255, 193, 7);

    public RecommendationsPanel(MentisLoginFrame parentApp, SessionController sessionController) {
        this.parentApp = parentApp;
        this.sessionController = sessionController;
        this.reviewController = new SessionReviewController();
        this.recommendationService = new RecommendationService();

        setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        setPadding(new Insets(30));
        setSpacing(20);

        createHeader();
        createContent();
        refreshData();
    }

    private void createHeader() {
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 20, 0));

        VBox titleBox = new VBox(10);
        Label titleLabel = new Label("Recommended For You");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        Label subtitleLabel = new Label("Personalized session recommendations based on your history");
        subtitleLabel.setFont(Font.font("Segoe UI", 16));
        subtitleLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        userInfoLabel = new Label(parentApp.getUserName());
        userInfoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        userInfoLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        headerBox.getChildren().addAll(titleBox, spacer, userInfoLabel);
        getChildren().add(headerBox);
    }

    private void createContent() {
        recommendationsContainer = new VBox(15);
        recommendationsContainer.setFillWidth(true);

        ScrollPane scrollPane = new ScrollPane(recommendationsContainer);
        scrollPane.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        scrollPane.setBorder(null);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        getChildren().add(scrollPane);
    }

    public void refreshData() {
        recommendationsContainer.getChildren().clear();

        try {
            List<Session> recommendations = recommendationService.getRecommendationsForPatient(parentApp.getUserId());

            if (recommendations.isEmpty()) {
                VBox emptyBox = createEmptyState();
                recommendationsContainer.getChildren().add(emptyBox);
                return;
            }

            // Add header with explanation
            Label infoLabel = new Label("Based on your session history, we think you'll like these:");
            infoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            infoLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));
            infoLabel.setPadding(new Insets(0, 0, 10, 0));
            recommendationsContainer.getChildren().add(infoLabel);

            for (Session session : recommendations) {
                VBox recommendationCard = createRecommendationCard(session);
                recommendationsContainer.getChildren().add(recommendationCard);
            }

            // Show count
            Label countLabel = new Label("Found " + recommendations.size() + " recommendations for you");
            countLabel.setFont(Font.font("Segoe UI", 12));
            countLabel.setTextFill(Color.web(toHex(TEXT_LIGHT)));
            countLabel.setPadding(new Insets(5, 0, 0, 0));
            recommendationsContainer.getChildren().add(countLabel);

        } catch (SQLException e) {
            showAlert("Error", "Failed to load recommendations: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private VBox createEmptyState() {
        VBox emptyBox = new VBox(20);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(50));
        emptyBox.setStyle("-fx-background-color: white; -fx-border-radius: 10; -fx-background-radius: 10;");

        Label titleLabel = new Label("🎯 No Recommendations Yet");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        Label msgLabel = new Label("Book some sessions first and we'll recommend similar ones you might like!");
        msgLabel.setFont(Font.font("Segoe UI", 14));
        msgLabel.setTextFill(Color.web(toHex(TEXT_LIGHT)));
        msgLabel.setWrapText(true);
        msgLabel.setAlignment(Pos.CENTER);
        msgLabel.setMaxWidth(400);

        Button browseButton = new Button("Browse Available Sessions");
        browseButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        browseButton.setTextFill(Color.WHITE);
        browseButton.setStyle("-fx-background-color: #" + toHex(ACCENT_DARK_GREEN) + "; -fx-background-radius: 5; -fx-padding: 10 25; -fx-cursor: hand;");
        browseButton.setOnAction(e -> parentApp.showPatientAvailableSessionsPanel());

        emptyBox.getChildren().addAll(titleLabel, msgLabel, browseButton);
        return emptyBox;
    }

    private VBox createRecommendationCard(Session session) {
        VBox card = new VBox(15);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 20;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);"
        );

        // Header with recommendation badge
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label badgeLabel = new Label("RECOMMENDED");
        badgeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        badgeLabel.setTextFill(Color.WHITE);
        badgeLabel.setStyle("-fx-background-color: #" + toHex(RECOMMENDATION_COLOR) + "; -fx-background-radius: 5; -fx-padding: 3 10;");

        Label titleLabel = new Label(session.getTitle());
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        Label typeLabel = new Label(session.getSessionType());
        typeLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        typeLabel.setTextFill(Color.WHITE);
        typeLabel.setStyle("-fx-background-color: #" + toHex(getTypeColor(session.getSessionType())) + "; -fx-background-radius: 15; -fx-padding: 5 15;");

        headerRow.getChildren().addAll(badgeLabel, titleLabel, spacer1, typeLabel);

        // Details
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(30);
        detailsGrid.setVgap(10);

        HBox dateBox = new HBox(5, new Label("📅"), new Label(session.getSessionDate().format(dateFormatter)));
        HBox timeBox = new HBox(5, new Label("⏰"), new Label(session.getStartTime().format(timeFormatter) + " - " + session.getEndTime().format(timeFormatter)));
        HBox locationBox = new HBox(5, new Label("📍"), new Label(session.getLocation()));

        detailsGrid.add(dateBox, 0, 0);
        detailsGrid.add(timeBox, 1, 0);
        detailsGrid.add(locationBox, 0, 1, 2, 1);

        // Popularity and rating
        HBox statsRow = new HBox(20);
        statsRow.setPadding(new Insets(10, 0, 10, 0));

        Label popularityLabel = new Label("🔥 " + session.getPopularity() + " booked");
        popularityLabel.setFont(Font.font("Segoe UI", 12));
        popularityLabel.setTextFill(Color.web(toHex(TEXT_LIGHT)));

        double avgRating = 0;
        try {
            avgRating = reviewController.getAverageRating(session.getSessionId());
        } catch (SQLException e) {
            // Ignore
        }
        Label ratingLabel = new Label("⭐ " + String.format("%.1f", avgRating) + " average");
        ratingLabel.setFont(Font.font("Segoe UI", 12));
        ratingLabel.setTextFill(Color.web(toHex(TEXT_LIGHT)));

        statsRow.getChildren().addAll(popularityLabel, ratingLabel);

        // Action buttons
        HBox actionRow = new HBox(10);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        Button detailsButton = new Button("View Details");
        detailsButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        detailsButton.setTextFill(Color.web(toHex(TEXT_DARK)));
        detailsButton.setStyle("-fx-background-color: #" + toHex(BUTTON_LIGHT_GREEN) + "; -fx-background-radius: 5; -fx-padding: 8 20;");
        detailsButton.setOnAction(e -> showSessionDetails(session));

        Button reserveButton = new Button("Reserve Now");
        reserveButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        reserveButton.setTextFill(Color.WHITE);
        reserveButton.setStyle("-fx-background-color: #" + toHex(ACCENT_DARK_GREEN) + "; -fx-background-radius: 5; -fx-padding: 8 20;");
        reserveButton.setOnAction(e -> reserveSession(session));

        actionRow.getChildren().addAll(detailsButton, reserveButton);

        card.getChildren().addAll(headerRow, detailsGrid, statsRow, actionRow);
        return card;
    }

    private Color getTypeColor(String type) {
        if (type == null) return Color.rgb(80, 100, 120);
        switch (type.toLowerCase()) {
            case "individual": return Color.rgb(41, 128, 185);
            case "group": return Color.rgb(39, 174, 96);
            case "family": return Color.rgb(142, 68, 173);
            case "couple": return Color.rgb(230, 126, 34);
            case "online": return Color.rgb(52, 152, 219);
            default: return Color.rgb(80, 100, 120);
        }
    }

    private void reserveSession(Session session) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Reservation");
        confirm.setHeaderText("Reserve Session");
        confirm.setContentText("Reserve: " + session.getTitle() + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    sessionController.reserveSession(session.getSessionId(), parentApp.getUserId());
                    showAlert("Success", "Session reserved!", Alert.AlertType.INFORMATION);
                    refreshData();
                } catch (SQLException e) {
                    showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void showSessionDetails(Session session) {
        Alert details = new Alert(Alert.AlertType.INFORMATION);
        details.setTitle("Session Details");
        details.setHeaderText(session.getTitle());

        String content = String.format(
                "Date: %s\nTime: %s - %s\nLocation: %s\nType: %s\nStatus: %s",
                session.getSessionDate().format(dateFormatter),
                session.getStartTime().format(timeFormatter),
                session.getEndTime().format(timeFormatter),
                session.getLocation(),
                session.getSessionType(),
                session.getStatus()
        );

        details.setContentText(content);
        details.showAndWait();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String toHex(Color color) {
        return String.format("%02x%02x%02x",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}