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
    private GridPane recommendationsGrid;
    private TextField searchField;
    private ComboBox<String> typeFilterCombo;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    // Symfony-style green colors
    private static final Color PAGE_BG = Color.web("#F8F9FA");
    private static final Color SOFT_GREEN_BG = Color.web("#F1F8E9");
    private static final Color EMERALD = Color.web("#50C878");
    private static final Color EMERALD_DARK = Color.web("#2E7D32");
    private static final Color EMERALD_MID = Color.web("#3A9B5E");
    private static final Color INK = Color.web("#1A3C34");
    private static final Color MUTED = Color.web("#6C757D");
    private static final Color LINE = Color.web("#E9ECEF");
    private static final Color RECOMMENDED_BADGE = Color.web("#FF9800");

    public RecommendationsPanel(MentisLoginFrame parentApp, SessionController sessionController) {
        this.parentApp = parentApp;
        this.sessionController = sessionController;
        this.reviewController = new SessionReviewController();
        this.recommendationService = new RecommendationService();

        setStyle("-fx-background-color: " + cssColor(PAGE_BG) + ";");
        setPadding(new Insets(44, 56, 44, 56));
        setSpacing(28);

        createHeader();
        createSearchBar();
        createContent();
        refreshData();
    }

    private void createHeader() {
        VBox headerBox = new VBox(8);
        headerBox.setPadding(new Insets(0, 0, 20, 0));

        Label titleLabel = new Label("🎯 Recommended For You");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 40));
        titleLabel.setTextFill(EMERALD_DARK);

        Label subtitleLabel = new Label("Personalized session recommendations based on your interests");
        subtitleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        subtitleLabel.setTextFill(MUTED);

        headerBox.getChildren().addAll(titleLabel, subtitleLabel);
        getChildren().add(headerBox);
    }

    private void createSearchBar() {
        HBox searchBar = new HBox(14);
        searchBar.setPadding(new Insets(0, 0, 24, 0));
        searchBar.setAlignment(Pos.CENTER);

        searchField = new TextField();
        searchField.setPromptText("🔍 Search by title or location...");
        searchField.setPrefWidth(340);
        searchField.setStyle(pillInputStyle());
        searchField.setOnAction(e -> filterRecommendations());

        typeFilterCombo = new ComboBox<>();
        typeFilterCombo.getItems().addAll("All Types", "Individual", "Group", "Family", "Couple", "Online");
        typeFilterCombo.setValue("All Types");
        typeFilterCombo.setPrefHeight(44);
        typeFilterCombo.setPrefWidth(140);
        typeFilterCombo.setStyle(pillInputStyle());
        typeFilterCombo.setOnAction(e -> filterRecommendations());

        Button searchButton = createPrimaryButton("Search");
        searchButton.setOnAction(e -> filterRecommendations());

        Button clearButton = createOutlineButton("Clear");
        clearButton.setOnAction(e -> {
            searchField.clear();
            typeFilterCombo.setValue("All Types");
            refreshData();
        });

        searchBar.getChildren().addAll(searchField, typeFilterCombo, searchButton, clearButton);
        getChildren().add(searchBar);
    }

    private void createContent() {
        recommendationsGrid = new GridPane();
        recommendationsGrid.setHgap(26);
        recommendationsGrid.setVgap(26);
        recommendationsGrid.setPadding(new Insets(10, 0, 20, 0));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        recommendationsGrid.getColumnConstraints().addAll(col1, col2);

        ScrollPane scrollPane = new ScrollPane(recommendationsGrid);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setBorder(null);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        getChildren().add(scrollPane);
    }

    private void filterRecommendations() {
        String keyword = searchField.getText().trim().toLowerCase();
        String selectedType = typeFilterCombo.getValue();

        try {
            List<Session> allRecommendations = recommendationService.getRecommendationsForPatient(parentApp.getUserId());

            List<Session> filtered = allRecommendations.stream()
                    .filter(s -> keyword.isEmpty() ||
                            s.getTitle().toLowerCase().contains(keyword) ||
                            s.getLocation().toLowerCase().contains(keyword))
                    .filter(s -> "All Types".equals(selectedType) ||
                            s.getSessionType().equalsIgnoreCase(selectedType))
                    .toList();

            displayRecommendations(filtered);
        } catch (SQLException e) {
            showAlert("Error", "Failed to filter recommendations: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void refreshData() {
        recommendationsGrid.getChildren().clear();

        try {
            List<Session> recommendations = recommendationService.getRecommendationsForPatient(parentApp.getUserId());

            if (recommendations.isEmpty()) {
                VBox emptyBox = createEmptyState();
                recommendationsGrid.add(emptyBox, 0, 0, 2, 1);
                return;
            }

            displayRecommendations(recommendations);

        } catch (SQLException e) {
            showAlert("Error", "Failed to load recommendations: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void displayRecommendations(List<Session> recommendations) {
        recommendationsGrid.getChildren().clear();

        if (recommendations.isEmpty()) {
            VBox emptyBox = createEmptyState();
            recommendationsGrid.add(emptyBox, 0, 0, 2, 1);
            return;
        }

        int row = 0;
        int col = 0;
        for (Session session : recommendations) {
            VBox card = createRecommendationCard(session);
            recommendationsGrid.add(card, col, row);
            col++;
            if (col == 2) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createEmptyState() {
        VBox emptyBox = new VBox(20);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(60));
        emptyBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 24px;" +
                        cardShadow()
        );

        Label iconLabel = new Label("🎯");
        iconLabel.setFont(Font.font("Segoe UI Emoji", 54));

        Label titleLabel = new Label("No Recommendations Yet");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(INK);

        Label msgLabel = new Label("Book some sessions first and we'll recommend similar ones you might like!");
        msgLabel.setFont(Font.font("Segoe UI", 14));
        msgLabel.setTextFill(MUTED);
        msgLabel.setWrapText(true);
        msgLabel.setAlignment(Pos.CENTER);

        Button browseButton = createPrimaryButton("Browse Available Sessions");
        browseButton.setOnAction(e -> parentApp.showPatientAvailableSessionsPanel());

        emptyBox.getChildren().addAll(iconLabel, titleLabel, msgLabel, browseButton);
        return emptyBox;
    }

    private VBox createRecommendationCard(Session session) {
        VBox card = new VBox(14);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20px;" +
                        "-fx-padding: 22px;" +
                        cardShadow()
        );
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20px;" +
                        "-fx-padding: 22px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 18, 0, 0, 8);" +
                        "-fx-translate-y: -4px;"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20px;" +
                        "-fx-padding: 22px;" +
                        cardShadow()
        ));

        // Header row with badge
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label badgeLabel = new Label("RECOMMENDED");
        badgeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        badgeLabel.setTextFill(Color.WHITE);
        badgeLabel.setStyle("-fx-background-color: " + cssColor(RECOMMENDED_BADGE) + "; -fx-background-radius: 20px; -fx-padding: 4px 12px;");

        Label titleLabel = new Label(session.getTitle());
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(INK);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Label typeLabel = createBadge(session.getSessionType(), getTypeColor(session.getSessionType()));

        headerRow.getChildren().addAll(badgeLabel, titleLabel, typeLabel);

        // Details
        VBox detailsBox = new VBox(8);
        Label dateLabel = new Label("📅 " + session.getSessionDate().format(dateFormatter));
        dateLabel.setFont(Font.font("Segoe UI", 13));
        dateLabel.setTextFill(MUTED);

        Label timeLabel = new Label("⏰ " + session.getStartTime().format(timeFormatter) + " - " + session.getEndTime().format(timeFormatter));
        timeLabel.setFont(Font.font("Segoe UI", 13));
        timeLabel.setTextFill(MUTED);

        Label locationLabel = new Label("📍 " + session.getLocation());
        locationLabel.setFont(Font.font("Segoe UI", 13));
        locationLabel.setTextFill(MUTED);

        detailsBox.getChildren().addAll(dateLabel, timeLabel, locationLabel);

        // Rating
        double avgRating = 0;
        try {
            avgRating = reviewController.getAverageRating(session.getSessionId());
        } catch (SQLException e) {
            // Ignore
        }
        Label ratingLabel = new Label("⭐ " + String.format("%.1f", avgRating) + " average rating");
        ratingLabel.setFont(Font.font("Segoe UI", 12));
        ratingLabel.setTextFill(MUTED);

        // Action buttons
        HBox actionRow = new HBox(12);
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        actionRow.setPadding(new Insets(12, 0, 0, 0));

        Button detailsButton = createOutlineButton("View Details");
        detailsButton.setOnAction(e -> showSessionDetails(session));

        Button reserveButton = createPrimaryButton("Reserve Now");
        reserveButton.setOnAction(e -> reserveSession(session));

        actionRow.getChildren().addAll(detailsButton, reserveButton);

        card.getChildren().addAll(headerRow, detailsBox, ratingLabel, actionRow);
        return card;
    }

    private Label createBadge(String text, Color bgColor) {
        Label badge = new Label(text);
        badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        badge.setTextFill(Color.WHITE);
        badge.setPadding(new Insets(5, 14, 5, 14));
        badge.setStyle("-fx-background-color: " + cssColor(bgColor) + "; -fx-background-radius: 999px;");
        return badge;
    }

    private Color getTypeColor(String type) {
        if (type == null) return Color.web("#3498DB");
        switch (type.toLowerCase()) {
            case "individual": return Color.web("#5B8C5A");
            case "group": return Color.web("#27AE60");
            case "family": return Color.web("#8E44AD");
            case "couple": return Color.web("#E67E22");
            case "online": return Color.web("#3498DB");
            default: return Color.web("#7F8C8D");
        }
    }

    private void reserveSession(Session session) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Reservation");
        confirm.setHeaderText("Reserve Session");
        confirm.setContentText("Are you sure you want to reserve:\n\n" +
                session.getTitle() + "\n" +
                session.getSessionDate().format(dateFormatter) + " at " +
                session.getStartTime().format(timeFormatter));

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    sessionController.reserveSession(session.getSessionId(), parentApp.getUserId());
                    showAlert("Success", "✨ Session reserved successfully!", Alert.AlertType.INFORMATION);
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

    private Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        button.setTextFill(Color.WHITE);
        button.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + cssColor(EMERALD) + ", " + cssColor(EMERALD_MID) + ");" +
                        "-fx-background-radius: 999px;" +
                        "-fx-padding: 9px 22px;" +
                        "-fx-cursor: hand;" +
                        cardShadow()
        );
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + cssColor(EMERALD_MID) + ", " + cssColor(EMERALD_DARK) + ");" +
                        "-fx-background-radius: 999px;" +
                        "-fx-padding: 9px 22px;" +
                        "-fx-cursor: hand;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + cssColor(EMERALD) + ", " + cssColor(EMERALD_MID) + ");" +
                        "-fx-background-radius: 999px;" +
                        "-fx-padding: 9px 22px;" +
                        "-fx-cursor: hand;" +
                        cardShadow()
        ));
        return button;
    }

    private Button createOutlineButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        button.setTextFill(MUTED);
        button.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 999px;" +
                        "-fx-border-color: " + cssColor(LINE) + ";" +
                        "-fx-border-radius: 999px;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-padding: 8px 20px;" +
                        "-fx-cursor: hand;"
        );
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: " + cssColor(SOFT_GREEN_BG) + ";" +
                        "-fx-background-radius: 999px;" +
                        "-fx-border-color: " + cssColor(EMERALD) + ";" +
                        "-fx-border-radius: 999px;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-padding: 8px 20px;" +
                        "-fx-cursor: hand;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 999px;" +
                        "-fx-border-color: " + cssColor(LINE) + ";" +
                        "-fx-border-radius: 999px;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-padding: 8px 20px;" +
                        "-fx-cursor: hand;"
        ));
        return button;
    }

    private String pillInputStyle() {
        return "-fx-background-color: white;" +
                "-fx-background-radius: 999px;" +
                "-fx-border-radius: 999px;" +
                "-fx-border-color: " + cssColor(LINE) + ";" +
                "-fx-border-width: 1.5px;" +
                "-fx-padding: 10px 18px;" +
                "-fx-font-size: 14px;" +
                "-fx-font-family: 'Segoe UI';";
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String cssColor(Color color) {
        return "#" + toHex(color);
    }

    private String cardShadow() {
        return "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 5);";
    }

    private String toHex(Color color) {
        return String.format("%02x%02x%02x",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}