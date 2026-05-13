package ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class AdminDashboardPanel extends VBox {

    private MentisLoginFrame parentApp;

    private int psychologistCount;
    private int patientCount;
    private int adminCount;
    private int totalUsers;

    private TextField searchField;
    private ListView<UserSearchResult> searchResultsList;
    private VBox searchResultsContainer;
    private ObservableList<UserSearchResult> allUsersList;
    private FilteredList<UserSearchResult> filteredUsers;

    private Map<String, Integer> userIdMap = new HashMap<String, Integer>();

    private Label patientCountLabel;
    private Label psychologistCountLabel;
    private Label totalUsersLabel;
    private Label adminCountLabel;
    private Label dateChip;
    private Label timeChip;

    private static final Color EMERALD = Color.web("#50C878");
    private static final Color EMERALD_DARK = Color.web("#2E7D32");
    private static final Color INK = Color.web("#1A3C34");
    private static final Color TEXT_DARK = Color.web("#2D3748");
    private static final Color MUTED = Color.web("#6C757D");
    private static final Color BLUE = Color.web("#667EEA");
    private static final Color PURPLE = Color.web("#764BA2");
    private static final Color PINK = Color.web("#F5576C");
    private static final Color ORANGE = Color.web("#F39C12");
    private static final Color PATIENT_COLOR = Color.web("#43E97B");
    private static final Color PSYCHOLOGIST_COLOR = Color.web("#9B5DE5");

    public AdminDashboardPanel(MentisLoginFrame parentApp) {
        this.parentApp = parentApp;

        setStyle("-fx-background-color: linear-gradient(to bottom right, #F5F7FA, #E8F5E9);");
        setPadding(new Insets(0));
        setSpacing(0);

        loadStatistics();
        loadAllUsers();

        ScrollPane scrollPane = new ScrollPane(createMainContent());
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");

        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        getChildren().add(scrollPane);

        startClock();
    }

    private static class UserSearchResult {
        private final int id;
        private final String firstName;
        private final String lastName;
        private final String email;
        private final String type;
        private final String displayName;

        public UserSearchResult(int id, String firstName, String lastName, String email, String type) {
            this.id = id;
            this.firstName = firstName == null ? "" : firstName;
            this.lastName = lastName == null ? "" : lastName;
            this.email = email == null ? "" : email;
            this.type = type == null ? "" : type;

            String icon = "👤 ";
            if ("psychologist".equalsIgnoreCase(type)) icon = "🧠 ";
            else if ("patient".equalsIgnoreCase(type)) icon = "💚 ";
            else if ("admin".equalsIgnoreCase(type)) icon = "👑 ";

            this.displayName = icon + this.firstName + " " + this.lastName + " (" + this.email + ")";
        }

        public int getId() { return id; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getFullName() { return firstName + " " + lastName; }
        public String getEmail() { return email; }
        public String getType() { return type; }

        @Override
        public String toString() { return displayName; }
    }

    private VBox createMainContent() {
        VBox mainContent = new VBox(24);
        mainContent.setPadding(new Insets(28, 32, 32, 32));
        mainContent.setStyle("-fx-background-color: transparent;");

        mainContent.getChildren().addAll(
                createWelcomeSection(),
                createSearchResultsPanel(),
                createKpiGrid(),
                createUserHubSection(),
                createChartSection(),
                createQuickActionsSection()
        );

        return mainContent;
    }

    private VBox createWelcomeSection() {
        VBox glassPanel = new VBox(18);
        glassPanel.setPadding(new Insets(30));
        glassPanel.setStyle(
                "-fx-background-color: #FFFFFF;" +
                        "-fx-background-radius: 30;" +
                        "-fx-border-radius: 30;" +
                        "-fx-border-color: #E9ECEF;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 28, 0, 0, 10);"
        );

        HBox row = new HBox(24);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox left = new VBox(10);
        left.setAlignment(Pos.CENTER_LEFT);

        Label rank = new Label("MASTER ADMIN");
        rank.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        rank.setPadding(new Insets(6, 16, 6, 16));
        rank.setStyle(
                "-fx-background-color: linear-gradient(to right, #F1C40F, #E67E22);" +
                        "-fx-background-radius: 999;" +
                        "-fx-text-fill: #1A3C34;"
        );

        Label welcome = new Label("Welcome back!");
        welcome.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 42));
        welcome.setStyle("-fx-text-fill: #1A3C34;");

        Label dashboardTitle = new Label("Mentis Admin Dashboard");
        dashboardTitle.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 30));
        dashboardTitle.setStyle("-fx-text-fill: #2E7D32;");

        Label subtitle = new Label("Empower minds, transform lives, and lead the wellness revolution.");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
        subtitle.setWrapText(true);
        subtitle.setStyle("-fx-text-fill: #6C757D;");

        HBox chips = new HBox(10);
        chips.setAlignment(Pos.CENTER_LEFT);

        dateChip = createChip("mai 13, 2026");
        timeChip = createChip(LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm a")));

        chips.getChildren().addAll(dateChip, timeChip);

        left.getChildren().addAll(rank, welcome, dashboardTitle, subtitle, chips);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(left, spacer, createAdminAvatar());
        glassPanel.getChildren().add(row);

        return glassPanel;
    }

    private StackPane createAdminAvatar() {
        StackPane outer = new StackPane();
        outer.setPrefSize(125, 125);
        outer.setMaxSize(125, 125);
        outer.setMinSize(125, 125);
        outer.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #50C878, #2E7D32);" +
                        "-fx-background-radius: 999;" +
                        "-fx-effect: dropshadow(gaussian, rgba(80,200,120,0.42), 28, 0, 0, 8);"
        );

        StackPane inner = new StackPane();
        inner.setPrefSize(98, 98);
        inner.setMaxSize(98, 98);
        inner.setStyle("-fx-background-color: white; -fx-background-radius: 999;");

        Label crown = new Label("👑");
        crown.setFont(Font.font("Segoe UI Emoji", 46));
        inner.getChildren().add(crown);

        StackPane status = new StackPane();
        status.setPrefSize(22, 22);
        status.setMaxSize(22, 22);
        status.setStyle("-fx-background-color: #50C878; -fx-background-radius: 999; -fx-border-color: white; -fx-border-width: 3; -fx-border-radius: 999;");
        StackPane.setAlignment(status, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(status, new Insets(0, 9, 9, 0));

        outer.getChildren().addAll(inner, status);
        return outer;
    }

    private HBox createKpiGrid() {
        HBox grid = new HBox(18);
        grid.setAlignment(Pos.CENTER);
        grid.setFillHeight(true);

        VBox total = createKpiCard("👥", totalUsers, "Total Users", "+12%", "up", BLUE, PURPLE, 0.75);
        VBox patients = createKpiCard("💚", patientCount, "Active Patients", "+8%", "up", PATIENT_COLOR, Color.web("#38F9D7"), 0.85);
        VBox psychologists = createKpiCard("🧠", psychologistCount, "Psychologists", "+6%", "up", PSYCHOLOGIST_COLOR, PINK, 0.68);
        VBox admins = createKpiCard("👑", adminCount, "Admins", "stable", "neutral", ORANGE, Color.web("#FEE140"), 0.40);

        totalUsersLabel = (Label) ((VBox) total.getChildren().get(1)).getChildren().get(0);
        patientCountLabel = (Label) ((VBox) patients.getChildren().get(1)).getChildren().get(0);
        psychologistCountLabel = (Label) ((VBox) psychologists.getChildren().get(1)).getChildren().get(0);
        adminCountLabel = (Label) ((VBox) admins.getChildren().get(1)).getChildren().get(0);

        HBox.setHgrow(total, Priority.ALWAYS);
        HBox.setHgrow(patients, Priority.ALWAYS);
        HBox.setHgrow(psychologists, Priority.ALWAYS);
        HBox.setHgrow(admins, Priority.ALWAYS);
        grid.getChildren().addAll(total, patients, psychologists, admins);
        return grid;
    }

    private VBox createKpiCard(String icon, int value, String label, String trend, String trendType, Color leftColor, Color rightColor, double progress) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setMinHeight(180);
        card.setMaxWidth(Double.MAX_VALUE);
        String normal = "-fx-background-color: white; -fx-background-radius: 25; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 22, 0, 0, 8);";
        String hover = "-fx-background-color: white; -fx-background-radius: 25; -fx-translate-y: -5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.16), 32, 0, 0, 14);";
        card.setStyle(normal);

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(58, 58);
        iconBox.setMaxSize(58, 58);
        iconBox.setStyle("-fx-background-color: " + gradient(leftColor, rightColor) + "; -fx-background-radius: 18;");
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Segoe UI Emoji", 25));
        iconBox.getChildren().add(iconLabel);

        VBox info = new VBox(2);
        Label valueLabel = new Label(String.valueOf(value));
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 33));
        valueLabel.setTextFill(TEXT_DARK);
        Label labelText = new Label(label);
        labelText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        labelText.setTextFill(MUTED);
        Label trendLabel = new Label(("up".equals(trendType) ? "↑ " : "neutral".equals(trendType) ? "• " : "↓ ") + trend);
        trendLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        trendLabel.setTextFill("up".equals(trendType) ? EMERALD : MUTED);
        info.getChildren().addAll(valueLabel, labelText, trendLabel);

        ProgressBar progressBar = new ProgressBar(progress);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(5);
        progressBar.setStyle("-fx-accent: #50C878; -fx-control-inner-background: #E9ECEF;");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        card.getChildren().addAll(iconBox, info, spacer, progressBar);
        addHoverLift(card, normal, hover);
        return card;
    }

    private VBox createUserHubSection() {
        VBox section = new VBox(0);
        section.setStyle(sectionCardStyle());

        HBox header = createSectionHeader("💚", "Patient & Psychologist Wellness Hub", "Manage and monitor users from one modern dashboard", "Refresh", new Runnable() {
            @Override public void run() { refreshData(); }
        });

        VBox filtersPanel = createFiltersPanel();

        VBox body = new VBox(18);
        body.setPadding(new Insets(22, 26, 26, 26));
        body.setStyle("-fx-background-color: #F8F9FA;");

        HBox modernStatsGrid = new HBox(16);
        modernStatsGrid.setAlignment(Pos.CENTER);
        VBox totalCard = createSmallStatCard("👥", "Total Users", totalUsers, gradient(BLUE, PURPLE));
        VBox patientCard = createSmallStatCard("💚", "Patients", patientCount, gradient(PATIENT_COLOR, Color.web("#38F9D7")));
        VBox psychCard = createSmallStatCard("🧠", "Psychologists", psychologistCount, gradient(PSYCHOLOGIST_COLOR, PINK));
        VBox adminCard = createSmallStatCard("👑", "Admins", adminCount, gradient(ORANGE, Color.web("#FEE140")));
        HBox.setHgrow(totalCard, Priority.ALWAYS);
        HBox.setHgrow(patientCard, Priority.ALWAYS);
        HBox.setHgrow(psychCard, Priority.ALWAYS);
        HBox.setHgrow(adminCard, Priority.ALWAYS);
        modernStatsGrid.getChildren().addAll(totalCard, patientCard, psychCard, adminCard);

        HBox managementCards = new HBox(18);
        managementCards.setAlignment(Pos.CENTER);
        VBox patientsHub = createManagementCard("Patient Wellness Hub", "Manage and monitor patient progress", "💚", PATIENT_COLOR, "View Patients", new Runnable() {
            @Override public void run() { parentApp.showPatientTablePanel(); }
        });
        VBox expertsHub = createManagementCard("Psychology Experts", "Manage mental health professionals", "🧠", PSYCHOLOGIST_COLOR, "View Psychologists", new Runnable() {
            @Override public void run() { parentApp.showPsychologistTablePanel(); }
        });
        HBox.setHgrow(patientsHub, Priority.ALWAYS);
        HBox.setHgrow(expertsHub, Priority.ALWAYS);
        managementCards.getChildren().addAll(patientsHub, expertsHub);

        body.getChildren().addAll(modernStatsGrid, managementCards);
        section.getChildren().addAll(header, filtersPanel, body);
        return section;
    }

    private VBox createFiltersPanel() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(22, 26, 22, 26));
        panel.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #E9ECEF; -fx-border-width: 1 0 1 0;");

        Label title = new Label("🔍 Search users");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        title.setTextFill(EMERALD_DARK);

        HBox searchRow = new HBox(12);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        searchField = new TextField();
        searchField.setPromptText("Search patients, psychologists, admins...");
        searchField.setPrefHeight(44);
        searchField.setMaxWidth(Double.MAX_VALUE);
        searchField.setStyle(pillInputStyle());
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button clearButton = outlineButton("Reset");
        clearButton.setOnAction(e -> { searchField.clear(); hideSearchResults(); });

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) performSearch(newValue.trim());
            else hideSearchResults();
        });

        searchRow.getChildren().addAll(searchField, clearButton);
        panel.getChildren().addAll(title, searchRow);
        return panel;
    }

    private HBox createSectionHeader(String icon, String title, String subtitle, String buttonText, Runnable action) {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(22, 26, 22, 26));
        header.setStyle("-fx-background-color: linear-gradient(to bottom right, #F8F9FA, #E9ECEF); -fx-background-radius: 30 30 0 0;");

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(52, 52);
        iconBox.setMaxSize(52, 52);
        iconBox.setStyle("-fx-background-color: linear-gradient(to bottom right, #50C878, #2E7D32); -fx-background-radius: 16;");
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Segoe UI Emoji", 24));
        iconBox.getChildren().add(iconLabel);

        VBox text = new VBox(3);
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        titleLabel.setTextFill(TEXT_DARK);
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        subtitleLabel.setTextFill(MUTED);
        text.getChildren().addAll(titleLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button button = primaryButton(buttonText);
        button.setOnAction(e -> action.run());
        header.getChildren().addAll(iconBox, text, spacer, button);
        return header;
    }

    private VBox createSmallStatCard(String icon, String label, int value, String bgGradient) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(16));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(Double.MAX_VALUE);
        String normal = "-fx-background-color: white; -fx-background-radius: 16;";
        String hover = "-fx-background-color: white; -fx-background-radius: 16; -fx-translate-y: -2; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 16, 0, 0, 6);";
        card.setStyle(normal);

        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(52, 52);
        iconBox.setMaxSize(52, 52);
        iconBox.setStyle("-fx-background-color: " + bgGradient + "; -fx-background-radius: 14;");
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Segoe UI Emoji", 23));
        iconBox.getChildren().add(iconLabel);

        VBox detail = new VBox(3);
        Label labelNode = new Label(label);
        labelNode.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        labelNode.setTextFill(MUTED);
        Label number = new Label(String.valueOf(value));
        number.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        number.setTextFill(TEXT_DARK);
        detail.getChildren().addAll(labelNode, number);
        row.getChildren().addAll(iconBox, detail);
        card.getChildren().add(row);
        addHoverLift(card, normal, hover);
        return card;
    }

    private VBox createManagementCard(String title, String subtitle, String icon, Color color, String buttonText, Runnable action) {
        VBox card = new VBox(16);
        card.setPadding(new Insets(22));
        card.setMinHeight(175);
        card.setMaxWidth(Double.MAX_VALUE);
        String normal = "-fx-background-color: white; -fx-background-radius: 24; -fx-border-radius: 24; -fx-border-color: #EEF2F7; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.05), 14, 0, 0, 5);";
        String hover = "-fx-background-color: white; -fx-background-radius: 24; -fx-border-radius: 24; -fx-border-color: #DDEBE4; -fx-translate-y: -3; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.12), 24, 0, 0, 10);";
        card.setStyle(normal);

        HBox top = new HBox(14);
        top.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(58, 58);
        iconBox.setMaxSize(58, 58);
        iconBox.setStyle("-fx-background-color: " + css(color.deriveColor(0, 1, 1, 0.18)) + "; -fx-background-radius: 18; -fx-border-color: " + css(color.deriveColor(0, 1, 1, 0.35)) + "; -fx-border-radius: 18;");
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Segoe UI Emoji", 27));
        iconBox.getChildren().add(iconLabel);

        VBox titles = new VBox(4);
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 19));
        titleLabel.setTextFill(TEXT_DARK);
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        subtitleLabel.setTextFill(MUTED);
        subtitleLabel.setWrapText(true);
        titles.getChildren().addAll(titleLabel, subtitleLabel);
        top.getChildren().addAll(iconBox, titles);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        Button button = primaryButton(buttonText);
        button.setOnAction(e -> action.run());
        card.getChildren().addAll(top, spacer, button);
        addHoverLift(card, normal, hover);
        return card;
    }

    private VBox createSearchResultsPanel() {
        searchResultsContainer = new VBox(12);
        searchResultsContainer.setPadding(new Insets(18));
        searchResultsContainer.setStyle("-fx-background-color: white; -fx-background-radius: 22; -fx-border-radius: 22; -fx-border-color: #E9ECEF; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 20, 0, 0, 8);");
        searchResultsContainer.setVisible(false);
        searchResultsContainer.setManaged(false);

        HBox resultsHeader = new HBox();
        resultsHeader.setAlignment(Pos.CENTER_LEFT);
        Label resultsTitle = new Label("Search Results");
        resultsTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        resultsTitle.setTextFill(EMERALD_DARK);
        Label resultsCount = new Label();
        resultsCount.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        resultsCount.setTextFill(MUTED);
        resultsCount.setPadding(new Insets(0, 0, 0, 8));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button closeResults = outlineButton("✕");
        closeResults.setOnAction(e -> hideSearchResults());
        resultsHeader.getChildren().addAll(resultsTitle, resultsCount, spacer, closeResults);

        searchResultsList = new ListView<UserSearchResult>();
        searchResultsList.setPrefHeight(235);
        searchResultsList.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 16; -fx-border-radius: 16; -fx-border-color: #E9ECEF;");
        searchResultsList.setCellFactory(lv -> new ListCell<UserSearchResult>() {
            @Override
            protected void updateItem(UserSearchResult user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox cell = new HBox(12);
                    cell.setAlignment(Pos.CENTER_LEFT);
                    cell.setPadding(new Insets(8));
                    StackPane avatar = new StackPane();
                    avatar.setPrefSize(44, 44);
                    avatar.setMaxSize(44, 44);
                    avatar.setStyle("-fx-background-color: " + userTypeGradient(user.getType()) + "; -fx-background-radius: 14;");
                    Label initials = new Label(getInitials(user.getFirstName(), user.getLastName()));
                    initials.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                    initials.setTextFill(Color.WHITE);
                    avatar.getChildren().add(initials);
                    VBox details = new VBox(3);
                    Label nameLabel = new Label(user.getFullName());
                    nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
                    nameLabel.setTextFill(TEXT_DARK);
                    Label emailLabel = new Label(user.getEmail());
                    emailLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
                    emailLabel.setTextFill(MUTED);
                    Label typeBadge = createBadge(user.getType(), getUserTypeColor(user.getType()));
                    details.getChildren().addAll(nameLabel, emailLabel, typeBadge);
                    cell.getChildren().addAll(avatar, details);
                    setGraphic(cell);
                    setText(null);
                }
            }
        });
        searchResultsList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                UserSearchResult selected = searchResultsList.getSelectionModel().getSelectedItem();
                if (selected != null) navigateToUser(selected);
            }
        });
        searchResultsContainer.getChildren().addAll(resultsHeader, searchResultsList);
        return searchResultsContainer;
    }

    private VBox createChartSection() {
        VBox chartContainer = new VBox(0);
        chartContainer.setStyle(sectionCardStyle());
        HBox header = createSectionHeader("📊", "User Distribution", "Overview of Mentis users by role", "Reload", new Runnable() {
            @Override public void run() { refreshData(); }
        });
        VBox chartBody = new VBox(16);
        chartBody.setPadding(new Insets(26));
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Count");
        BarChart<String, Number> barChart = new BarChart<String, Number>(xAxis, yAxis);
        barChart.setTitle("Psychologists vs Patients vs Admins");
        barChart.setLegendVisible(false);
        barChart.setAnimated(true);
        barChart.setPrefHeight(310);
        barChart.setStyle("-fx-background-color: transparent;");
        XYChart.Series<String, Number> series = new XYChart.Series<String, Number>();
        series.getData().add(new XYChart.Data<String, Number>("Patients", patientCount));
        series.getData().add(new XYChart.Data<String, Number>("Psychologists", psychologistCount));
        series.getData().add(new XYChart.Data<String, Number>("Admins", adminCount));
        barChart.getData().add(series);
        Platform.runLater(new Runnable() { @Override public void run() { styleChartBars(series); } });
        chartBody.getChildren().add(barChart);
        chartContainer.getChildren().addAll(header, chartBody);
        return chartContainer;
    }

    private VBox createQuickActionsSection() {
        VBox section = new VBox(0);
        section.setStyle(sectionCardStyle());
        HBox header = createSectionHeader("⚡", "Quick Actions", "Jump into the most important admin workflows", "Refresh", new Runnable() {
            @Override public void run() { refreshData(); }
        });
        HBox actions = new HBox(16);
        actions.setPadding(new Insets(24, 26, 26, 26));
        actions.setAlignment(Pos.CENTER);
        VBox patientAction = createActionTile("💚", "Patients", "Manage patient profiles", new Runnable() { @Override public void run() { parentApp.showPatientTablePanel(); } });
        VBox psychologistAction = createActionTile("🧠", "Psychologists", "Manage mental health experts", new Runnable() { @Override public void run() { parentApp.showPsychologistTablePanel(); } });
        VBox assessmentAction = createActionTile("📋", "Assessments", "Open assessment management", new Runnable() { @Override public void run() { parentApp.showAssessmentPanel(); } });
        VBox logsAction = createActionTile("🔐", "Access Logs", "Review system activity", new Runnable() { @Override public void run() { parentApp.showAccessLogsPanel(); } });
        HBox.setHgrow(patientAction, Priority.ALWAYS);
        HBox.setHgrow(psychologistAction, Priority.ALWAYS);
        HBox.setHgrow(assessmentAction, Priority.ALWAYS);
        HBox.setHgrow(logsAction, Priority.ALWAYS);
        actions.getChildren().addAll(patientAction, psychologistAction, assessmentAction, logsAction);
        section.getChildren().addAll(header, actions);
        return section;
    }

    private VBox createActionTile(String icon, String title, String subtitle, Runnable action) {
        VBox tile = new VBox(8);
        tile.setAlignment(Pos.CENTER);
        tile.setPadding(new Insets(20));
        tile.setMinHeight(155);
        tile.setMaxWidth(Double.MAX_VALUE);
        String normal = "-fx-background-color: #F8F9FA; -fx-background-radius: 20; -fx-border-radius: 20; -fx-border-color: #E9ECEF; -fx-cursor: hand;";
        String hover = "-fx-background-color: #F1F8E9; -fx-background-radius: 20; -fx-border-radius: 20; -fx-border-color: #C8E6D2; -fx-cursor: hand; -fx-translate-y: -3; -fx-effect: dropshadow(gaussian, rgba(80,200,120,0.18), 18, 0, 0, 8);";
        tile.setStyle(normal);
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Segoe UI Emoji", 34));
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        titleLabel.setTextFill(TEXT_DARK);
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        subtitleLabel.setTextFill(MUTED);
        subtitleLabel.setWrapText(true);
        subtitleLabel.setTextAlignment(TextAlignment.CENTER);
        tile.getChildren().addAll(iconLabel, titleLabel, subtitleLabel);
        tile.setOnMouseClicked(e -> action.run());
        addHoverLift(tile, normal, hover);
        return tile;
    }

    private void performSearch(String query) {
        if (allUsersList == null || allUsersList.isEmpty()) return;
        String lowerQuery = query.toLowerCase();
        filteredUsers = new FilteredList<UserSearchResult>(allUsersList, user ->
                user.getFirstName().toLowerCase().contains(lowerQuery) ||
                        user.getLastName().toLowerCase().contains(lowerQuery) ||
                        user.getFullName().toLowerCase().contains(lowerQuery) ||
                        user.getEmail().toLowerCase().contains(lowerQuery) ||
                        user.getType().toLowerCase().contains(lowerQuery)
        );
        ObservableList<UserSearchResult> results = FXCollections.observableArrayList();
        for (UserSearchResult user : filteredUsers) results.add(user);
        searchResultsList.setItems(results);
        Label resultsCount = (Label) ((HBox) searchResultsContainer.getChildren().get(0)).getChildren().get(1);
        resultsCount.setText("(" + results.size() + " found)");
        searchResultsList.setPlaceholder(new Label("No users found"));
        searchResultsContainer.setVisible(true);
        searchResultsContainer.setManaged(true);
    }

    private void hideSearchResults() {
        if (searchResultsContainer != null) {
            searchResultsContainer.setVisible(false);
            searchResultsContainer.setManaged(false);
        }
        if (searchResultsList != null) searchResultsList.setItems(FXCollections.observableArrayList());
    }

    private void navigateToUser(UserSearchResult user) {
        String userType = user.getType().toLowerCase();
        if (userType.contains("psychologist")) parentApp.showPsychologistTablePanel();
        else if (userType.contains("patient")) parentApp.showPatientTablePanel();
        else if (userType.contains("admin")) showTemporaryMessage(user.getFullName() + " is an administrator");
        hideSearchResults();
        searchField.clear();
    }

    private void loadAllUsers() {
        allUsersList = FXCollections.observableArrayList();
        userIdMap.clear();
        String sql = "SELECT id, firstname, lastname, email, type FROM user ORDER BY type, firstname";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
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
        } catch (SQLException e) {
            System.err.println("Error loading users for search: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showTemporaryMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Navigation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    Thread.sleep(1500);
                    Platform.runLater(new Runnable() { @Override public void run() { alert.close(); } });
                } catch (InterruptedException e) { e.printStackTrace(); }
            }
        }).start();
        alert.show();
    }

    private void loadStatistics() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            psychologistCount = getCount(conn, "psychologist");
            patientCount = getCount(conn, "Patient");
            adminCount = getCount(conn, "admin");
            totalUsers = getTotalCount(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            psychologistCount = 0;
            patientCount = 0;
            adminCount = 0;
            totalUsers = 0;
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

    private int getTotalCount(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public void refreshData() {
        loadStatistics();
        loadAllUsers();
        getChildren().clear();
        ScrollPane scrollPane = new ScrollPane(createMainContent());
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        getChildren().add(scrollPane);
    }

    private Label createChip(String text) {
        Label chip = new Label(text);
        chip.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        chip.setPadding(new Insets(8, 16, 8, 16));
        chip.setStyle(
                "-fx-background-color: #F1F3F5;" +
                        "-fx-background-radius: 999;" +
                        "-fx-text-fill: #2D3748;"
        );
        return chip;
    }

    private Label createBadge(String text, Color color) {
        Label badge = new Label(text == null ? "N/A" : text);
        badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        badge.setTextFill(color);
        badge.setPadding(new Insets(4, 10, 4, 10));
        badge.setStyle("-fx-background-color: " + css(color.deriveColor(0, 1, 1, 0.13)) + "; -fx-background-radius: 999; -fx-border-color: " + css(color.deriveColor(0, 1, 1, 0.28)) + "; -fx-border-radius: 999;");
        return badge;
    }

    private Button primaryButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        button.setTextFill(Color.WHITE);
        button.setPadding(new Insets(10, 24, 10, 24));
        String normal = "-fx-background-color: linear-gradient(to bottom right, #50C878, #2E7D32); -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(80,200,120,0.28), 14, 0, 0, 5);";
        String hover = "-fx-background-color: linear-gradient(to bottom right, #3A9B5E, #2E7D32); -fx-background-radius: 12; -fx-cursor: hand; -fx-translate-y: -2; -fx-effect: dropshadow(gaussian, rgba(80,200,120,0.38), 18, 0, 0, 7);";
        button.setStyle(normal);
        button.setOnMouseEntered(e -> button.setStyle(hover));
        button.setOnMouseExited(e -> button.setStyle(normal));
        return button;
    }

    private Button outlineButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        button.setTextFill(TEXT_DARK);
        button.setPadding(new Insets(9, 18, 9, 18));
        String normal = "-fx-background-color: white; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #E9ECEF; -fx-border-width: 1; -fx-cursor: hand;";
        String hover = "-fx-background-color: #F1F8E9; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #50C878; -fx-border-width: 1; -fx-cursor: hand;";
        button.setStyle(normal);
        button.setOnMouseEntered(e -> { button.setTextFill(EMERALD_DARK); button.setStyle(hover); });
        button.setOnMouseExited(e -> { button.setTextFill(TEXT_DARK); button.setStyle(normal); });
        return button;
    }

    private String pillInputStyle() {
        return "-fx-background-color: white; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #E9ECEF; -fx-border-width: 1; -fx-padding: 10 16; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px;";
    }

    private String sectionCardStyle() {
        return "-fx-background-color: white; -fx-background-radius: 30; -fx-border-radius: 30; -fx-border-color: transparent; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 24, 0, 0, 9);";
    }

    private void addHoverLift(Region node, String normalStyle, String hoverStyle) {
        node.setOnMouseEntered(e -> node.setStyle(hoverStyle));
        node.setOnMouseExited(e -> node.setStyle(normalStyle));
    }

    private void styleChartBars(XYChart.Series<String, Number> series) {
        for (XYChart.Data<String, Number> data : series.getData()) {
            Node node = data.getNode();
            if (node == null) continue;
            if ("Patients".equals(data.getXValue())) node.setStyle("-fx-bar-fill: #43E97B;");
            else if ("Psychologists".equals(data.getXValue())) node.setStyle("-fx-bar-fill: #9B5DE5;");
            else node.setStyle("-fx-bar-fill: #F39C12;");
        }
    }

    private Color getUserTypeColor(String type) {
        if (type == null) return MUTED;
        if ("psychologist".equalsIgnoreCase(type)) return PSYCHOLOGIST_COLOR;
        if ("patient".equalsIgnoreCase(type)) return PATIENT_COLOR;
        if ("admin".equalsIgnoreCase(type)) return ORANGE;
        return MUTED;
    }

    private String userTypeGradient(String type) {
        if (type == null) return "linear-gradient(to bottom right, #6C757D, #ADB5BD)";
        if ("psychologist".equalsIgnoreCase(type)) return "linear-gradient(to bottom right, #9B5DE5, #F15BB5)";
        if ("patient".equalsIgnoreCase(type)) return "linear-gradient(to bottom right, #43E97B, #38F9D7)";
        if ("admin".equalsIgnoreCase(type)) return "linear-gradient(to bottom right, #F1C40F, #E67E22)";
        return "linear-gradient(to bottom right, #6C757D, #ADB5BD)";
    }

    private String getInitials(String firstName, String lastName) {
        String first = firstName == null || firstName.isEmpty() ? "" : firstName.substring(0, 1);
        String last = lastName == null || lastName.isEmpty() ? "" : lastName.substring(0, 1);
        String initials = (first + last).toUpperCase();
        return initials.isEmpty() ? "NA" : initials;
    }

    private void startClock() {
        Thread clockThread = new Thread(new Runnable() {
            @Override public void run() {
                while (true) {
                    try {
                        Thread.sleep(30000);
                        Platform.runLater(new Runnable() {
                            @Override public void run() {
                                if (dateChip != null) dateChip.setText("📅 " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
                                if (timeChip != null) timeChip.setText("🕒 " + LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm a")));
                            }
                        });
                    } catch (InterruptedException e) { return; }
                }
            }
        });
        clockThread.setDaemon(true);
        clockThread.start();
    }

    private String css(Color color) { return "#" + toHex(color); }
    private String gradient(Color left, Color right) { return "linear-gradient(to bottom right, " + css(left) + ", " + css(right) + ")"; }

    private String toHex(Color color) {
        return String.format("%02x%02x%02x", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }
}
