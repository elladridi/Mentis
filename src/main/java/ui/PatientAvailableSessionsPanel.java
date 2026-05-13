package ui;

import controller.SessionController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import models.Session;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PatientAvailableSessionsPanel extends VBox {

    private MentisLoginFrame parentApp;
    private SessionController sessionController;
    private GridPane sessionsGrid;
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
    private static final Color STATUS_AVAILABLE = Color.web("#27AE60");

    public PatientAvailableSessionsPanel(MentisLoginFrame parentApp, SessionController sessionController) {
        this.parentApp = parentApp;
        this.sessionController = sessionController;

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

        Label titleLabel = new Label("📋 Available Sessions");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 40));
        titleLabel.setTextFill(EMERALD_DARK);

        Label subtitleLabel = new Label("Browse and reserve therapy sessions that fit your schedule");
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
        searchField.setOnAction(e -> searchSessions());

        typeFilterCombo = new ComboBox<>();
        typeFilterCombo.getItems().addAll("All Types", "Individual", "Group", "Family", "Couple", "Online");
        typeFilterCombo.setValue("All Types");
        typeFilterCombo.setPrefHeight(44);
        typeFilterCombo.setPrefWidth(140);
        typeFilterCombo.setStyle(pillInputStyle());
        typeFilterCombo.setOnAction(e -> filterSessions());

        Button searchButton = createPrimaryButton("Search");
        searchButton.setOnAction(e -> searchSessions());

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
        sessionsGrid = new GridPane();
        sessionsGrid.setHgap(26);
        sessionsGrid.setVgap(26);
        sessionsGrid.setPadding(new Insets(10, 0, 20, 0));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        sessionsGrid.getColumnConstraints().addAll(col1, col2);

        ScrollPane scrollPane = new ScrollPane(sessionsGrid);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setBorder(null);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        getChildren().add(scrollPane);
    }

    private void searchSessions() {
        String keyword = searchField.getText().trim();
        try {
            List<Session> sessions = sessionController.searchAvailableSessions(keyword);
            displaySessions(sessions);
        } catch (SQLException e) {
            showAlert("Error", "Search failed: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void filterSessions() {
        String selectedType = typeFilterCombo.getValue();
        try {
            List<Session> sessions = sessionController.filterAvailableSessionsByType(selectedType);
            String keyword = searchField.getText().trim();
            if (!keyword.isEmpty()) {
                sessions = sessions.stream()
                        .filter(s -> s.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                                s.getLocation().toLowerCase().contains(keyword.toLowerCase()))
                        .collect(java.util.stream.Collectors.toList());
            }
            displaySessions(sessions);
        } catch (SQLException e) {
            showAlert("Error", "Filter failed: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // DEBUG METHOD - Add this to see what's happening
    private void debugDatabase() {
        try {
            System.out.println("\n========== 🔍 DEBUG: Database Check ==========");

            // Get all sessions directly from controller
            List<Session> allSessions = sessionController.getAllSessions();
            System.out.println("📊 Total sessions in database: " + allSessions.size());

            if (allSessions.isEmpty()) {
                System.out.println("❌ No sessions found in database!");
            } else {
                for (Session s : allSessions) {
                    System.out.println("   🟢 ID: " + s.getSessionId() +
                            " | Title: " + s.getTitle() +
                            " | Status: '" + s.getStatus() + "'" +
                            " | Date: " + s.getSessionDate() +
                            " | Start: " + s.getStartTime() +
                            " | Reserved By: " + (s.getReservedBy() == null ? "NULL" : s.getReservedBy()));
                }
            }

            // Check available sessions
            List<Session> available = sessionController.getAvailableSessions();
            System.out.println("\n📋 Available sessions (after filter): " + available.size());

            if (available.isEmpty()) {
                System.out.println("❌ No available sessions found!");
                System.out.println("\n💡 Possible reasons:");
                System.out.println("   1. Session status is not 'active' or 'scheduled'");
                System.out.println("   2. Session date is in the past");
                System.out.println("   3. Session is already reserved (reserved_by is not NULL)");
            } else {
                for (Session s : available) {
                    System.out.println("   ✅ " + s.getTitle() + " | " + s.getSessionDate());
                }
            }

            System.out.println("==========================================\n");

        } catch (SQLException e) {
            System.out.println("❌ Debug error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void refreshData() {
        debugDatabase(); // ADD THIS LINE - shows debug info in console

        try {
            List<Session> sessions = sessionController.getAvailableSessions();
            displaySessions(sessions);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load sessions: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void displaySessions(List<Session> sessions) {
        sessionsGrid.getChildren().clear();

        if (sessions.isEmpty()) {
            VBox emptyBox = createEmptyState();
            sessionsGrid.add(emptyBox, 0, 0, 2, 1);
            return;
        }

        int row = 0;
        int col = 0;
        for (Session session : sessions) {
            VBox card = createSessionCard(session);
            sessionsGrid.add(card, col, row);
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

        Label iconLabel = new Label("📅");
        iconLabel.setFont(Font.font("Segoe UI Emoji", 54));

        Label titleLabel = new Label("No Sessions Available");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(INK);

        Label msgLabel = new Label("Check back later for new sessions");
        msgLabel.setFont(Font.font("Segoe UI", 14));
        msgLabel.setTextFill(MUTED);

        emptyBox.getChildren().addAll(iconLabel, titleLabel, msgLabel);
        return emptyBox;
    }

    private VBox createSessionCard(Session session) {
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

        // Title row
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(session.getTitle());
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(INK);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Label typeLabel = createBadge(session.getSessionType(), getTypeColor(session.getSessionType()));
        titleRow.getChildren().addAll(titleLabel, typeLabel);

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

        // Action buttons
        HBox actionRow = new HBox(12);
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        actionRow.setPadding(new Insets(12, 0, 0, 0));

        Label statusLabel = new Label("● Available");
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        statusLabel.setTextFill(STATUS_AVAILABLE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button detailsButton = createOutlineButton("View Details");
        detailsButton.setOnAction(e -> showSessionDetails(session));

        Button reserveButton = createPrimaryButton("Reserve Now");
        reserveButton.setOnAction(e -> reserveSession(session));

        actionRow.getChildren().addAll(statusLabel, spacer, detailsButton, reserveButton);

        card.getChildren().addAll(titleRow, detailsBox, actionRow);
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
                    showAlert("Success", "✨ Session reserved successfully! You can view it in 'My Sessions'.", Alert.AlertType.INFORMATION);
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
                "Date: %s\nTime: %s - %s\nLocation: %s\nType: %s\nStatus: %s\n\nThis session is available for reservation.",
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
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }
}