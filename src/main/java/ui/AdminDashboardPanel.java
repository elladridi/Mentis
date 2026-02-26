package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AdminDashboardPanel extends VBox {

    private MentisLoginFrame parentApp;
    private int psychologistCount;
    private int patientCount;

    // Search related components
    private TextField searchField;
    private ListView<UserSearchResult> searchResultsList;
    private VBox searchResultsContainer;
    private ObservableList<UserSearchResult> allUsersList;
    private FilteredList<UserSearchResult> filteredUsers;

    // Map to store user IDs for navigation
    private Map<String, Integer> userIdMap = new HashMap<>();

    // Color constants
    private static final Color BACKGROUND_LIGHT = Color.rgb(245, 248, 246);
    private static final Color CARD_WHITE = Color.WHITE;
    private static final Color ACCENT_GREEN = Color.rgb(60, 110, 90);
    private static final Color PSYCHOLOGIST_COLOR = Color.rgb(90, 150, 230);
    private static final Color PATIENT_COLOR = Color.rgb(100, 180, 120);
    private static final Color TEXT_DARK = Color.rgb(60, 110, 90);
    private static final Color TEXT_GRAY = Color.GRAY;
    private static final Color BORDER_LIGHT = Color.rgb(220, 220, 220);

    public AdminDashboardPanel(MentisLoginFrame parentApp) {
        this.parentApp = parentApp;

        setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        setPadding(new Insets(0));
        setSpacing(0);

        // Load statistics
        loadStatistics();

        // Load all users for search
        loadAllUsers();

        // Create main content
        VBox mainContent = createMainContent();
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        getChildren().add(mainContent);
    }

    // ================= USER SEARCH RESULT MODEL =================
    private static class UserSearchResult {
        private final int id;
        private final String firstName;
        private final String lastName;
        private final String email;
        private final String type;
        private final String displayName;

        public UserSearchResult(int id, String firstName, String lastName, String email, String type) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.type = type;

            // Format display name based on type
            String icon = "";
            if ("psychologist".equalsIgnoreCase(type)) {
                icon = "👨‍⚕️ ";
            } else if ("Patient".equalsIgnoreCase(type)) {
                icon = "👤 ";
            } else if ("admin".equalsIgnoreCase(type)) {
                icon = "👑 ";
            }

            this.displayName = String.format("%s%s %s (%s)",
                    icon, firstName, lastName, email);
        }

        public int getId() { return id; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getFullName() { return firstName + " " + lastName; }
        public String getEmail() { return email; }
        public String getType() { return type; }

        @Override
        public String toString() {
            return displayName;
        }
    }

    // ================= MAIN CONTENT =================
    private VBox createMainContent() {
        VBox mainContent = new VBox();
        mainContent.setStyle("-fx-background-color: transparent;");

        mainContent.getChildren().addAll(
                createHeader(),
                createSearchResultsPanel(),
                createCenterContent()
        );

        return mainContent;
    }

    // ================= HEADER WITH DYNAMIC SEARCH =================
    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white;");
        header.setPadding(new Insets(20, 30, 20, 30));
        header.setSpacing(20);

        Label title = new Label("Dashboard");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        title.setTextFill(Color.web(toHex(TEXT_DARK)));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Search container
        HBox searchContainer = new HBox(10);
        searchContainer.setAlignment(Pos.CENTER_RIGHT);

        // Search icon
        Label searchIcon = new Label("🔍");
        searchIcon.setFont(Font.font("Segoe UI", 18));
        searchIcon.setTextFill(Color.web(toHex(TEXT_GRAY)));

        // Search field with dynamic search
        searchField = new TextField();
        searchField.setPromptText("Search by name...");
        searchField.setPrefWidth(300);
        searchField.setPrefHeight(40);
        searchField.setStyle(
                "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8 15;" +
                        "-fx-font-size: 14px;"
        );

        // Add live search listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                performSearch(newValue.trim());
            } else {
                hideSearchResults();
            }
        });

        // Clear button
        Button clearButton = new Button("✕");
        clearButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #" + toHex(TEXT_GRAY) + ";" +
                        "-fx-font-size: 16px;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 8 12;"
        );
        clearButton.setVisible(false);
        clearButton.setOnAction(e -> {
            searchField.clear();
            hideSearchResults();
            clearButton.setVisible(false);
        });

        // Show clear button when text is entered
        searchField.textProperty().addListener((obs, old, newVal) -> {
            clearButton.setVisible(newVal != null && !newVal.isEmpty());
        });

        searchContainer.getChildren().addAll(searchIcon, searchField, clearButton);
        header.getChildren().addAll(title, spacer, searchContainer);

        return header;
    }

    // ================= SEARCH RESULTS PANEL =================
    private VBox createSearchResultsPanel() {
        searchResultsContainer = new VBox(10);
        searchResultsContainer.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);"
        );
        searchResultsContainer.setPadding(new Insets(15));
        searchResultsContainer.setMaxWidth(600);
        searchResultsContainer.setVisible(false);
        searchResultsContainer.setManaged(false);

        // Results header
        HBox resultsHeader = new HBox();
        resultsHeader.setAlignment(Pos.CENTER);
        resultsHeader.setPadding(new Insets(0, 0, 10, 0));

        Label resultsTitle = new Label("Search Results");
        resultsTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        resultsTitle.setTextFill(Color.web(toHex(TEXT_DARK)));

        Label resultsCount = new Label();
        resultsCount.setFont(Font.font("Segoe UI", 14));
        resultsCount.setTextFill(Color.web(toHex(TEXT_GRAY)));
        resultsCount.setPadding(new Insets(0, 0, 0, 10));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeResults = new Button("✕");
        closeResults.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #" + toHex(TEXT_GRAY) + ";" +
                        "-fx-font-size: 16px;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 5 10;"
        );
        closeResults.setOnAction(e -> hideSearchResults());

        resultsHeader.getChildren().addAll(resultsTitle, resultsCount, spacer, closeResults);

        // Results list with custom cell factory for better display
        searchResultsList = new ListView<>();
        searchResultsList.setPrefHeight(250);
        searchResultsList.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;"
        );

        // Custom cell factory to show user details
        searchResultsList.setCellFactory(lv -> new ListCell<UserSearchResult>() {
            @Override
            protected void updateItem(UserSearchResult user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Create custom cell content
                    VBox cellContent = new VBox(3);

                    // Name line with icon
                    Label nameLabel = new Label(user.getFullName());
                    nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

                    // Set color based on type
                    Color typeColor;
                    if ("psychologist".equalsIgnoreCase(user.getType())) {
                        typeColor = PSYCHOLOGIST_COLOR;
                    } else if ("Patient".equalsIgnoreCase(user.getType())) {
                        typeColor = PATIENT_COLOR;
                    } else {
                        typeColor = ACCENT_GREEN;
                    }
                    nameLabel.setTextFill(Color.web(toHex(typeColor)));

                    // Email and type line
                    HBox detailsBox = new HBox(10);
                    Label emailLabel = new Label(user.getEmail());
                    emailLabel.setFont(Font.font("Segoe UI", 12));
                    emailLabel.setTextFill(Color.web(toHex(TEXT_GRAY)));

                    Label typeLabel = new Label("[" + user.getType() + "]");
                    typeLabel.setFont(Font.font("Segoe UI", 12));
                    typeLabel.setTextFill(Color.web(toHex(TEXT_GRAY)));

                    detailsBox.getChildren().addAll(emailLabel, typeLabel);

                    cellContent.getChildren().addAll(nameLabel, detailsBox);

                    setGraphic(cellContent);
                }
            }
        });

        // Double-click to navigate to user
        searchResultsList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                UserSearchResult selected = searchResultsList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    navigateToUser(selected);
                }
            }
        });

        searchResultsContainer.getChildren().addAll(resultsHeader, searchResultsList);

        return searchResultsContainer;
    }

    // ================= PERFORM DYNAMIC SEARCH BY NAME =================
    private void performSearch(String query) {
        if (allUsersList == null || allUsersList.isEmpty()) {
            return;
        }

        String lowerQuery = query.toLowerCase();

        // Filter users based on name only
        filteredUsers = new FilteredList<>(allUsersList, user ->
                user.getFirstName().toLowerCase().contains(lowerQuery) ||
                        user.getLastName().toLowerCase().contains(lowerQuery) ||
                        user.getFullName().toLowerCase().contains(lowerQuery)
        );

        // Update results list
        ObservableList<UserSearchResult> results = FXCollections.observableArrayList();
        for (UserSearchResult user : filteredUsers) {
            results.add(user);
        }

        searchResultsList.setItems(results);

        // Update results count
        Label resultsCount = (Label) ((HBox) searchResultsContainer.getChildren().get(0)).getChildren().get(1);
        resultsCount.setText("(" + results.size() + " found)");

        // Show results panel if there are results
        if (!results.isEmpty()) {
            searchResultsContainer.setVisible(true);
            searchResultsContainer.setManaged(true);
        } else {
            // Show "no results" message
            searchResultsList.setPlaceholder(new Label("No users found with that name"));
            searchResultsContainer.setVisible(true);
            searchResultsContainer.setManaged(true);
        }
    }

    // ================= HIDE SEARCH RESULTS =================
    private void hideSearchResults() {
        searchResultsContainer.setVisible(false);
        searchResultsContainer.setManaged(false);
        searchResultsList.setItems(FXCollections.observableArrayList());
    }

    // ================= NAVIGATE TO USER =================
    private void navigateToUser(UserSearchResult user) {
        String userType = user.getType().toLowerCase();

        // Show brief message
        showTemporaryMessage("Navigating to " + user.getFullName() + " (" + userType + ")");

        // Navigate based on user type
        if (userType.contains("psychologist")) {
            parentApp.showPsychologistTablePanel();
            // TODO: Add method to highlight specific psychologist in table
        } else if (userType.contains("patient")) {
            parentApp.showPatientTablePanel();
            // TODO: Add method to highlight specific patient in table
        } else if (userType.contains("admin")) {
            // Admin stays on dashboard
            showTemporaryMessage(user.getFullName() + " is an administrator");
        }

        // Hide search results after navigation
        hideSearchResults();
        searchField.clear();
    }

    // ================= LOAD ALL USERS FOR SEARCH =================
    private void loadAllUsers() {
        allUsersList = FXCollections.observableArrayList();
        userIdMap.clear();

        String sql = "SELECT id, firstname, lastname, email, type FROM user ORDER BY type, firstname";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String firstName = rs.getString("firstname");
                String lastName = rs.getString("lastname");
                String email = rs.getString("email");
                String type = rs.getString("type");

                UserSearchResult user = new UserSearchResult(id, firstName, lastName, email, type);
                allUsersList.add(user);
                userIdMap.put(firstName + " " + lastName, id);
            }

            System.out.println("✅ Loaded " + allUsersList.size() + " users for name search");

        } catch (SQLException e) {
            System.err.println("❌ Error loading users for search: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ================= SHOW TEMPORARY MESSAGE =================
    private void showTemporaryMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Navigation");
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Auto-close after 1.5 seconds
        new Thread(() -> {
            try {
                Thread.sleep(1500);
                javafx.application.Platform.runLater(() -> {
                    alert.close();
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        alert.show();
    }

    // ================= CENTER CONTENT =================
    private VBox createCenterContent() {
        VBox center = new VBox(20);
        center.setStyle("-fx-background-color: transparent;");
        center.setPadding(new Insets(20, 30, 30, 30));

        center.getChildren().addAll(
                createStatsPanel(),
                createChartSection()
        );

        VBox.setVgrow(center.getChildren().get(1), Priority.ALWAYS);

        return center;
    }

    // ================= STATS PANEL =================
    private HBox createStatsPanel() {
        HBox stats = new HBox(20);
        stats.setStyle("-fx-background-color: transparent;");
        stats.setAlignment(Pos.CENTER);

        VBox psychologistCard = createStatCard(
                "Psychologists",
                psychologistCount,
                PSYCHOLOGIST_COLOR,
                () -> parentApp.showPsychologistTablePanel()
        );

        VBox patientCard = createStatCard(
                "Patients",
                patientCount,
                PATIENT_COLOR,
                () -> parentApp.showPatientTablePanel()
        );

        HBox.setHgrow(psychologistCard, Priority.ALWAYS);
        HBox.setHgrow(patientCard, Priority.ALWAYS);

        stats.getChildren().addAll(psychologistCard, patientCard);
        return stats;
    }

    private VBox createStatCard(String title, int value, Color color, Runnable action) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefHeight(180);

        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);")
        );
        card.setOnMouseExited(e ->
                card.setStyle("-fx-background-color: white; -fx-background-radius: 10;")
        );

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        titleLabel.setTextFill(Color.web(toHex(TEXT_GRAY)));

        Label numberLabel = new Label(String.valueOf(value));
        numberLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        numberLabel.setTextFill(Color.web(toHex(color)));

        Button viewButton = new Button("View list");
        viewButton.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        viewButton.setTextFill(Color.WHITE);
        viewButton.setStyle(
                "-fx-background-color: #" + toHex(color) + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8 16;" +
                        "-fx-cursor: hand;"
        );

        viewButton.setOnMouseEntered(e ->
                viewButton.setStyle(
                        "-fx-background-color: #" + toHex(color.darker()) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 8 16;" +
                                "-fx-cursor: hand;"
                )
        );
        viewButton.setOnMouseExited(e ->
                viewButton.setStyle(
                        "-fx-background-color: #" + toHex(color) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 8 16;" +
                                "-fx-cursor: hand;"
                )
        );

        viewButton.setOnAction(e -> action.run());

        card.getChildren().addAll(titleLabel, numberLabel, viewButton);
        return card;
    }

    // ================= CHART SECTION =================
    private VBox createChartSection() {
        VBox chartContainer = new VBox(10);
        chartContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        chartContainer.setPadding(new Insets(20));
        chartContainer.setPrefHeight(300);

        Label chartTitle = new Label("User Distribution");
        chartTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        chartTitle.setTextFill(Color.web(toHex(TEXT_DARK)));

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Count");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Psychologists vs Patients");
        barChart.setLegendVisible(false);
        barChart.setAnimated(true);
        barChart.setStyle("-fx-background-color: transparent;");
        barChart.setPrefHeight(250);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Psychologists", psychologistCount));
        series.getData().add(new XYChart.Data<>("Patients", patientCount));

        barChart.getData().add(series);

        for (XYChart.Data<String, Number> data : series.getData()) {
            if (data.getXValue().equals("Psychologists")) {
                data.getNode().setStyle("-fx-bar-fill: #" + toHex(PSYCHOLOGIST_COLOR) + ";");
            } else {
                data.getNode().setStyle("-fx-bar-fill: #" + toHex(PATIENT_COLOR) + ";");
            }
        }

        chartContainer.getChildren().addAll(chartTitle, barChart);
        VBox.setVgrow(barChart, Priority.ALWAYS);

        return chartContainer;
    }

    // ================= DATABASE =================
    private void loadStatistics() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            psychologistCount = getCount(conn, "psychologist");
            patientCount = getCount(conn, "Patient");
        } catch (SQLException e) {
            e.printStackTrace();
            psychologistCount = 0;
            patientCount = 0;
        }
    }

    private int getCount(Connection conn, String type) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user WHERE type = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // ================= REFRESH =================
    public void refreshData() {
        loadStatistics();
        loadAllUsers();

        VBox centerContent = (VBox) getChildren().get(0);
        VBox center = (VBox) centerContent.getChildren().get(2);

        HBox statsPanel = (HBox) center.getChildren().get(0);
        statsPanel.getChildren().clear();
        statsPanel.getChildren().addAll(
                createStatCard("Psychologists", psychologistCount, PSYCHOLOGIST_COLOR,
                        () -> parentApp.showPsychologistTablePanel()),
                createStatCard("Patients", patientCount, PATIENT_COLOR,
                        () -> parentApp.showPatientTablePanel())
        );

        VBox chartSection = (VBox) center.getChildren().get(1);
        BarChart<String, Number> barChart = (BarChart<String, Number>) chartSection.getChildren().get(1);
        XYChart.Series<String, Number> series = barChart.getData().get(0);
        series.getData().get(0).setYValue(psychologistCount);
        series.getData().get(1).setYValue(patientCount);

        series.getData().get(0).getNode().setStyle("-fx-bar-fill: #" + toHex(PSYCHOLOGIST_COLOR) + ";");
        series.getData().get(1).getNode().setStyle("-fx-bar-fill: #" + toHex(PATIENT_COLOR) + ";");
    }

    // ================= UTILITY =================
    private String toHex(Color color) {
        return String.format("%02x%02x%02x",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }
}