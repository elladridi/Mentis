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
    private VBox sessionsContainer;
    private TextField searchField;
    private ComboBox<String> typeFilterCombo;
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

    public PatientAvailableSessionsPanel(MentisLoginFrame parentApp, SessionController sessionController) {
        this.parentApp = parentApp;
        this.sessionController = sessionController;

        setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        setPadding(new Insets(30));
        setSpacing(20);

        createHeader();
        createSearchBar();
        createSessionsContainer();
        refreshData();
    }

    private void createHeader() {
        VBox headerBox = new VBox(10);

        Label titleLabel = new Label("Available Sessions");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        Label subtitleLabel = new Label("Browse and reserve therapy sessions that fit your schedule");
        subtitleLabel.setFont(Font.font("Segoe UI", 16));
        subtitleLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        headerBox.getChildren().addAll(titleLabel, subtitleLabel);
        getChildren().add(headerBox);
    }

    private void createSearchBar() {
        HBox searchBar = new HBox(15);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setPadding(new Insets(20, 0, 20, 0));

        // Search field
        searchField = new TextField();
        searchField.setPromptText("Search by title or location...");
        searchField.setPrefHeight(40);
        searchField.setPrefWidth(300);
        searchField.setStyle(
                "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8 12;"
        );
        searchField.setOnAction(e -> searchSessions());

        // Type filter
        typeFilterCombo = new ComboBox<>();
        typeFilterCombo.getItems().addAll("All Types", "Individual", "Group", "Family", "Couple", "Online");
        typeFilterCombo.setValue("All Types");
        typeFilterCombo.setPrefHeight(40);
        typeFilterCombo.setPrefWidth(150);
        typeFilterCombo.setOnAction(e -> filterSessions());

        // Search button
        Button searchButton = new Button("Search");
        searchButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        searchButton.setTextFill(Color.WHITE);
        searchButton.setStyle("-fx-background-color: #" + toHex(ACCENT_DARK_GREEN) + "; -fx-background-radius: 5; -fx-padding: 10 25; -fx-cursor: hand;");
        searchButton.setOnAction(e -> searchSessions());

        // Clear button
        Button clearButton = new Button("Clear");
        clearButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        clearButton.setTextFill(Color.web(toHex(TEXT_DARK)));
        clearButton.setStyle("-fx-background-color: #" + toHex(BUTTON_LIGHT_GREEN) + "; -fx-background-radius: 5; -fx-padding: 10 25; -fx-cursor: hand;");
        clearButton.setOnAction(e -> {
            searchField.clear();
            typeFilterCombo.setValue("All Types");
            refreshData();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // User info - FIXED HERE
        userInfoLabel = new Label("Logged in as: " + parentApp.getUserName());
        userInfoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        userInfoLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        searchBar.getChildren().addAll(searchField, typeFilterCombo, searchButton, clearButton, spacer, userInfoLabel);
        getChildren().add(searchBar);
    }

    private void createSessionsContainer() {
        sessionsContainer = new VBox(15);
        sessionsContainer.setFillWidth(true);

        ScrollPane scrollPane = new ScrollPane(sessionsContainer);
        scrollPane.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
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
            // Also apply search filter if there's a search term
            String keyword = searchField.getText().trim();
            if (!keyword.isEmpty()) {
                sessions = sessionController.searchAvailableSessions(keyword);
            }
            displaySessions(sessions);
        } catch (SQLException e) {
            showAlert("Error", "Filter failed: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void refreshData() {
        try {
            List<Session> sessions = sessionController.getAvailableSessions();
            displaySessions(sessions);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load sessions: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void displaySessions(List<Session> sessions) {
        sessionsContainer.getChildren().clear();

        if (sessions.isEmpty()) {
            VBox emptyBox = new VBox(20);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(50));

            Label noDataLabel = new Label("📅 No sessions available at the moment");
            noDataLabel.setFont(Font.font("Segoe UI", 18));
            noDataLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

            Label suggestionLabel = new Label("Check back later for new sessions");
            suggestionLabel.setFont(Font.font("Segoe UI", 14));
            suggestionLabel.setTextFill(Color.web(toHex(TEXT_LIGHT)));

            emptyBox.getChildren().addAll(noDataLabel, suggestionLabel);
            sessionsContainer.getChildren().add(emptyBox);
            return;
        }

        for (Session session : sessions) {
            VBox sessionCard = createSessionCard(session);
            sessionsContainer.getChildren().add(sessionCard);
        }

        // Show count
        Label countLabel = new Label("Found " + sessions.size() + " available session(s)");
        countLabel.setFont(Font.font("Segoe UI", 12));
        countLabel.setTextFill(Color.web(toHex(TEXT_LIGHT)));
        countLabel.setPadding(new Insets(5, 0, 0, 0));
        sessionsContainer.getChildren().add(countLabel);
    }

    private VBox createSessionCard(Session session) {
        VBox card = new VBox(15);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 20;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);"
        );

        // First row: Title and Type
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(session.getTitle());
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        Label typeLabel = new Label(session.getSessionType());
        typeLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        typeLabel.setTextFill(Color.WHITE);
        typeLabel.setStyle("-fx-background-color: #" + toHex(getTypeColor(session.getSessionType())) + "; -fx-background-radius: 15; -fx-padding: 5 15;");

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        titleRow.getChildren().addAll(titleLabel, spacer1, typeLabel);

        // Second row: Date, Time, Location
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(30);
        detailsGrid.setVgap(10);

        // Date
        Label dateIcon = new Label("📅");
        dateIcon.setFont(Font.font("Segoe UI", 16));
        Label dateValue = new Label(session.getSessionDate().format(dateFormatter));
        dateValue.setFont(Font.font("Segoe UI", 14));

        HBox dateBox = new HBox(5, dateIcon, dateValue);

        // Time
        Label timeIcon = new Label("⏰");
        timeIcon.setFont(Font.font("Segoe UI", 16));
        String timeRange = session.getStartTime().format(timeFormatter) + " - " + session.getEndTime().format(timeFormatter);
        Label timeValue = new Label(timeRange);
        timeValue.setFont(Font.font("Segoe UI", 14));

        HBox timeBox = new HBox(5, timeIcon, timeValue);

        // Location
        Label locationIcon = new Label("📍");
        locationIcon.setFont(Font.font("Segoe UI", 16));
        Label locationValue = new Label(session.getLocation());
        locationValue.setFont(Font.font("Segoe UI", 14));

        HBox locationBox = new HBox(5, locationIcon, locationValue);

        detailsGrid.add(dateBox, 0, 0);
        detailsGrid.add(timeBox, 1, 0);
        detailsGrid.add(locationBox, 0, 1, 2, 1);

        // Third row: Status and Reserve button
        HBox actionRow = new HBox(15);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        Label statusLabel = new Label("● Available");
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        statusLabel.setTextFill(Color.web(toHex(Color.rgb(39, 174, 96))));

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        Button detailsButton = new Button("View Details");
        detailsButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        detailsButton.setTextFill(Color.web(toHex(TEXT_DARK)));
        detailsButton.setStyle("-fx-background-color: #" + toHex(BUTTON_LIGHT_GREEN) + "; -fx-background-radius: 5; -fx-padding: 8 20; -fx-cursor: hand;");
        detailsButton.setOnAction(e -> showSessionDetails(session));

        Button reserveButton = new Button("Reserve Now");
        reserveButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        reserveButton.setTextFill(Color.WHITE);
        reserveButton.setStyle("-fx-background-color: #" + toHex(ACCENT_DARK_GREEN) + "; -fx-background-radius: 5; -fx-padding: 8 25; -fx-cursor: hand;");
        reserveButton.setOnAction(e -> reserveSession(session));

        actionRow.getChildren().addAll(statusLabel, spacer2, detailsButton, reserveButton);

        card.getChildren().addAll(titleRow, detailsGrid, actionRow);
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
        confirm.setContentText("Are you sure you want to reserve:\n\n" +
                session.getTitle() + "\n" +
                session.getSessionDate().format(dateFormatter) + " at " +
                session.getStartTime().format(timeFormatter));

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    sessionController.reserveSession(session.getSessionId(), parentApp.getUserId());
                    showAlert("Success", "Session reserved successfully! You can view it in 'My Sessions'.", Alert.AlertType.INFORMATION);
                    refreshData(); // Refresh the list
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
                "Date: %s\n" +
                        "Time: %s - %s\n" +
                        "Location: %s\n" +
                        "Type: %s\n" +
                        "Status: %s\n\n" +
                        "This session is available for reservation.",
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
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }
}