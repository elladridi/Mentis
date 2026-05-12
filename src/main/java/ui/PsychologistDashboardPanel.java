package ui;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import utils.DatabaseConnection;

import java.lang.reflect.Method;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class PsychologistDashboardPanel extends VBox {

    private final MentisLoginFrame parentApp;
    private final int psychologistId;
    private final String psychologistName;

    private ObservableList<ResultRow> allResults;
    private ObservableList<PatientRow> allPatients;
    private FilteredList<ResultRow> filteredResults;

    private TableView<ResultRow> resultsTable;
    private TableView<PatientRow> patientsTable;

    private TextField patientSearchField;
    private ComboBox<String> riskFilterBox;
    private DatePicker dateFilterPicker;

    private Label totalPatientsLabel;
    private Label totalAssessmentsLabel;
    private Label highRiskLabel;
    private Label clockLabel;
    private VBox alertListBox;
    private Label alertBadge;

    private int totalPatients = 0;
    private int totalAssessments = 0;
    private int highRiskCount = 0;

    private static final Color EMERALD = Color.web("#50C878");
    private static final Color EMERALD_DARK = Color.web("#2E7D32");
    private static final Color EMERALD_MID = Color.web("#3A9B5E");
    private static final Color INK = Color.web("#1A3C34");
    private static final Color TEXT = Color.web("#2D3748");
    private static final Color MUTED = Color.web("#6C757D");
    private static final Color LINE = Color.web("#E9ECEF");
    private static final Color RED = Color.web("#E74C3C");
    private static final Color ORANGE = Color.web("#F39C12");
    private static final Color BLUE = Color.web("#4FACFE");
    private static final Color PURPLE = Color.web("#9B5DE5");

    public PsychologistDashboardPanel(MentisLoginFrame parentApp, int psychologistId, String psychologistName) {
        this.parentApp = parentApp;
        this.psychologistId = psychologistId;
        this.psychologistName = psychologistName == null || psychologistName.trim().isEmpty()
                ? "Doctor"
                : psychologistName;

        setPadding(new Insets(0));
        setSpacing(0);
        setStyle("-fx-background-color: linear-gradient(to bottom right, #F5F7FA, #E8F5E9);");

        loadDashboardData();

        ScrollPane scrollPane = new ScrollPane(createPage());
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle(
                "-fx-background: transparent;" +
                        "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;"
        );

        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        getChildren().add(scrollPane);

        startClock();
    }

    public PsychologistDashboardPanel(MentisLoginFrame parentApp) {
        this(parentApp, parentApp.getUserId(), "Psychologist");
    }

    private VBox createPage() {
        VBox page = new VBox(24);
        page.setPadding(new Insets(28, 32, 32, 32));
        page.setStyle("-fx-background-color: transparent;");

        page.getChildren().addAll(
                createTopNav(),
                createHeroSection(),
                createFaceIdCard(),
                createStatsGrid(),
                createMainTabs(),
                createProfileCard()
        );

        return page;
    }

    private HBox createTopNav() {
        HBox nav = new HBox(18);
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.setPadding(new Insets(18, 22, 18, 22));
        nav.setStyle(glassCardStyle(22));

        StackPane bell = new StackPane();
        bell.setPrefSize(52, 52);
        bell.setMaxSize(52, 52);
        bell.setCursor(Cursor.HAND);
        bell.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #FFF4F4, #FFF8F8);" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-color: #FFD9D9;" +
                        "-fx-border-radius: 16;" +
                        "-fx-effect: dropshadow(gaussian, rgba(231,76,60,0.10), 14, 0, 0, 5);"
        );

        Label bellIcon = new Label("🔔");
        bellIcon.setFont(Font.font("Segoe UI Emoji", 22));

        alertBadge = new Label(String.valueOf(highRiskCount));
        alertBadge.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 10));
        alertBadge.setTextFill(Color.WHITE);
        alertBadge.setAlignment(Pos.CENTER);
        alertBadge.setMinSize(22, 22);
        alertBadge.setPrefSize(22, 22);
        alertBadge.setStyle("-fx-background-color: #E74C3C; -fx-background-radius: 999;");
        alertBadge.setVisible(highRiskCount > 0);
        StackPane.setAlignment(alertBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(alertBadge, new Insets(-5, -5, 0, 0));

        bell.getChildren().addAll(bellIcon, alertBadge);
        bell.setOnMouseClicked(e -> showCrisisAlertsDialog());

        VBox titleBox = new VBox(3);
        Label title = new Label("🧠 Mentis");
        title.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 28));
        title.setTextFill(INK);

        Label subtitle = new Label("Psychologist Command Center");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        subtitle.setTextFill(MUTED);

        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        clockLabel = createChip("🕒 " + LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm a")));

        Label welcomeChip = createChip("👋 Welcome, Dr. " + psychologistName);

        Button logoutBtn = createPrimaryButton("Logout");
        logoutBtn.setOnAction(e -> invokeParent("logout"));

        nav.getChildren().addAll(bell, titleBox, spacer, clockLabel, welcomeChip, logoutBtn);
        return nav;
    }

    private VBox createHeroSection() {
        VBox hero = new VBox(18);
        hero.setPadding(new Insets(34, 34, 34, 34));
        hero.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #0F172A, #16213E, #1E293B);" +
                        "-fx-background-radius: 28;" +
                        "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.22), 36, 0, 0, 16);"
        );

        HBox row = new HBox(28);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox textBox = new VBox(14);

        Label badge = new Label("🩺 PROFESSIONAL CARE WORKSPACE");
        badge.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 12));
        badge.setTextFill(Color.web("#9DF2B3"));
        badge.setPadding(new Insets(8, 14, 8, 14));
        badge.setStyle(
                "-fx-background-color: rgba(80,200,120,0.14);" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-color: rgba(157,242,179,0.20);" +
                        "-fx-border-radius: 999;"
        );

        Label title = new Label("Welcome to Your Psychologist Dashboard");
        title.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 40));
        title.setTextFill(Color.WHITE);
        title.setWrapText(true);

        Label subtitle = new Label(
                "Monitor assessments, follow patient progress, review high-risk cases, and manage therapeutic content from one calm and powerful workspace."
        );
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
        subtitle.setTextFill(Color.web("#FFFFFF", 0.78));
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(760);
        subtitle.setLineSpacing(5);

        HBox tags = new HBox(10);
        tags.getChildren().addAll(
                createDarkTag("💚 Patient Monitoring"),
                createDarkTag("📊 Assessment Insights"),
                createDarkTag("🛡 Crisis Response")
        );

        textBox.getChildren().addAll(badge, title, subtitle, tags);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        StackPane orb = createHeroOrb();

        row.getChildren().addAll(textBox, spacer, orb);
        hero.getChildren().add(row);
        return hero;
    }

    private StackPane createHeroOrb() {
        StackPane outer = new StackPane();
        outer.setPrefSize(220, 220);
        outer.setMaxSize(220, 220);
        outer.setStyle(
                "-fx-background-color: radial-gradient(center 30% 30%, radius 70%, rgba(255,255,255,0.20), transparent), " +
                        "linear-gradient(to bottom right, rgba(80,200,120,0.95), rgba(102,126,234,0.95));" +
                        "-fx-background-radius: 999;" +
                        "-fx-effect: dropshadow(gaussian, rgba(80,200,120,0.30), 34, 0, 0, 12);"
        );

        VBox inner = new VBox(6);
        inner.setAlignment(Pos.CENTER);
        inner.setPadding(new Insets(14));
        inner.setMaxSize(176, 176);
        inner.setStyle(
                "-fx-background-color: rgba(255,255,255,0.12);" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-color: rgba(255,255,255,0.20);" +
                        "-fx-border-radius: 999;"
        );

        Label label = new Label("HIGH RISK CASES");
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        label.setTextFill(Color.web("#FFFFFF", 0.85));

        Label value = new Label(String.valueOf(highRiskCount));
        value.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 52));
        value.setTextFill(Color.WHITE);

        Label small = new Label("priority cases to monitor");
        small.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        small.setTextFill(Color.web("#FFFFFF", 0.88));

        inner.getChildren().addAll(label, value, small);
        outer.getChildren().add(inner);
        return outer;
    }

    private VBox createFaceIdCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(24));
        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #667EEA, #764BA2);" +
                        "-fx-background-radius: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.22), 24, 0, 0, 10);"
        );

        HBox row = new HBox(18);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox text = new VBox(5);

        Label title = new Label("🔐 Face ID Security");
        title.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 22));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Use biometric access for a faster and more secure login experience.");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        subtitle.setTextFill(Color.web("#FFFFFF", 0.90));

        text.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button button = createWhiteButton("Manage Face ID");
        button.setOnAction(e -> showInfo("Face ID management is available from the login security module."));

        row.getChildren().addAll(text, spacer, button);
        card.getChildren().add(row);

        return card;
    }

    private HBox createStatsGrid() {
        HBox grid = new HBox(18);
        grid.setAlignment(Pos.CENTER);

        VBox patients = createStatCard("👥", "Total Patients", totalPatients,
                "Patients currently in your care",
                "linear-gradient(to bottom right, #50C878, #3A9B5E)");

        VBox assessments = createStatCard("📊", "Total Assessments", totalAssessments,
                "Recorded mental health evaluations",
                "linear-gradient(to bottom right, #4FACFE, #00C6FF)");

        VBox highRisk = createStatCard("⚠️", "High Risk Cases", highRiskCount,
                "Cases requiring immediate attention",
                "linear-gradient(to bottom right, #FF6A88, #FF4B2B)");

        HBox.setHgrow(patients, Priority.ALWAYS);
        HBox.setHgrow(assessments, Priority.ALWAYS);
        HBox.setHgrow(highRisk, Priority.ALWAYS);

        grid.getChildren().addAll(patients, assessments, highRisk);

        totalPatientsLabel = (Label) patients.getChildren().get(2);
        totalAssessmentsLabel = (Label) assessments.getChildren().get(2);
        highRiskLabel = (Label) highRisk.getChildren().get(2);

        return grid;
    }

    private VBox createStatCard(String icon, String title, int value, String foot, String gradient) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(24));
        card.setMinHeight(175);
        card.setStyle(
                "-fx-background-color: " + gradient + ";" +
                        "-fx-background-radius: 22;" +
                        "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.10), 20, 0, 0, 8);"
        );

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Segoe UI Emoji", 30));

        Label label = new Label(title);
        label.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        label.setTextFill(Color.web("#FFFFFF", 0.92));

        Label valueLabel = new Label(String.valueOf(value));
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 34));
        valueLabel.setTextFill(Color.WHITE);

        Label footLabel = new Label(foot);
        footLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        footLabel.setTextFill(Color.web("#FFFFFF", 0.90));
        footLabel.setWrapText(true);

        card.getChildren().addAll(iconLabel, label, valueLabel, footLabel);
        addHover(card, card.getStyle(), card.getStyle() + "-fx-translate-y: -4;");

        return card;
    }

    private TabPane createMainTabs() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle(
                "-fx-background-color: rgba(255,255,255,0.86);" +
                        "-fx-background-radius: 26;" +
                        "-fx-border-radius: 26;" +
                        "-fx-border-color: rgba(255,255,255,0.70);" +
                        "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.08), 26, 0, 0, 10);"
        );

        Tab resultsTab = new Tab("📊 Assessment Results");
        resultsTab.setContent(createResultsTab());

        Tab patientsTab = new Tab("👥 My Patients");
        patientsTab.setContent(createPatientsTab());

        Tab contentTab = new Tab("📚 Content Library");
        contentTab.setContent(createContentTab());

        tabs.getTabs().addAll(resultsTab, patientsTab, contentTab);
        return tabs;
    }

    private VBox createResultsTab() {
        VBox box = new VBox(18);
        box.setPadding(new Insets(24));

        HBox filters = new HBox(12);
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.setPadding(new Insets(16));
        filters.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #FBFFFC, #F8FBFF);" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: #EDF2F7;" +
                        "-fx-border-radius: 18;"
        );

        patientSearchField = new TextField();
        patientSearchField.setPromptText("Filter by patient name...");
        patientSearchField.setPrefHeight(42);
        patientSearchField.setStyle(inputStyle());
        HBox.setHgrow(patientSearchField, Priority.ALWAYS);

        riskFilterBox = new ComboBox<String>();
        riskFilterBox.getItems().addAll("", "Minimal", "Low", "Mild", "Moderate", "High", "Severe");
        riskFilterBox.setValue("");
        riskFilterBox.setPromptText("Risk level");
        riskFilterBox.setPrefHeight(42);
        riskFilterBox.setStyle(inputStyle());

        dateFilterPicker = new DatePicker();
        dateFilterPicker.setPrefHeight(42);
        dateFilterPicker.setStyle(inputStyle());

        Button reset = createOutlineButton("Reset");
        reset.setOnAction(e -> {
            patientSearchField.clear();
            riskFilterBox.setValue("");
            dateFilterPicker.setValue(null);
            applyFilters();
        });

        patientSearchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        riskFilterBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        dateFilterPicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        filters.getChildren().addAll(patientSearchField, riskFilterBox, dateFilterPicker, reset);

        resultsTable = createResultsTable();

        VBox tableCard = new VBox(0);
        tableCard.setStyle(cardStyle(22));
        tableCard.getChildren().addAll(createSectionHeader("📊 Patient Assessment Results"), resultsTable);
        VBox.setVgrow(resultsTable, Priority.ALWAYS);

        box.getChildren().addAll(filters, tableCard);
        return box;
    }

    private TableView<ResultRow> createResultsTable() {
        TableView<ResultRow> table = new TableView<ResultRow>();
        table.setFixedCellSize(68);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No assessment results found yet."));
        table.setStyle(tableStyle());

        TableColumn<ResultRow, Number> idCol = new TableColumn<ResultRow, Number>("ID");
        idCol.setCellValueFactory(data -> data.getValue().idProperty());

        TableColumn<ResultRow, String> patientCol = new TableColumn<ResultRow, String>("Patient");
        patientCol.setCellValueFactory(new PropertyValueFactory<ResultRow, String>("patientName"));

        TableColumn<ResultRow, String> assessmentCol = new TableColumn<ResultRow, String>("Assessment");
        assessmentCol.setCellValueFactory(new PropertyValueFactory<ResultRow, String>("assessmentTitle"));

        TableColumn<ResultRow, Number> scoreCol = new TableColumn<ResultRow, Number>("Score");
        scoreCol.setCellValueFactory(data -> data.getValue().totalScoreProperty());

        TableColumn<ResultRow, String> riskCol = new TableColumn<ResultRow, String>("Risk Level");
        riskCol.setCellValueFactory(new PropertyValueFactory<ResultRow, String>("riskLevel"));
        riskCol.setCellFactory(col -> new RiskCell<ResultRow>());

        TableColumn<ResultRow, String> dateCol = new TableColumn<ResultRow, String>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<ResultRow, String>("takenAt"));

        TableColumn<ResultRow, String> sessionCol = new TableColumn<ResultRow, String>("Session");
        sessionCol.setCellValueFactory(new PropertyValueFactory<ResultRow, String>("suggestSession"));
        sessionCol.setCellFactory(col -> new SessionCell<ResultRow>());

        TableColumn<ResultRow, Void> actionsCol = new TableColumn<ResultRow, Void>("Actions");
        actionsCol.setCellFactory(col -> new ResultActionCell());

        table.getColumns().addAll(idCol, patientCol, assessmentCol, scoreCol, riskCol, dateCol, sessionCol, actionsCol);

        filteredResults = new FilteredList<ResultRow>(allResults, p -> true);
        table.setItems(filteredResults);

        styleModernTable(table);
        return table;
    }

    private VBox createPatientsTab() {
        VBox box = new VBox(18);
        box.setPadding(new Insets(24));

        patientsTable = createPatientsTable();

        VBox tableCard = new VBox(0);
        tableCard.setStyle(cardStyle(22));
        tableCard.getChildren().addAll(createSectionHeader("👥 My Patients"), patientsTable);
        VBox.setVgrow(patientsTable, Priority.ALWAYS);

        box.getChildren().add(tableCard);
        return box;
    }

    private TableView<PatientRow> createPatientsTable() {
        TableView<PatientRow> table = new TableView<PatientRow>();
        table.setFixedCellSize(68);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No patients found."));
        table.setStyle(tableStyle());

        TableColumn<PatientRow, Number> idCol = new TableColumn<PatientRow, Number>("ID");
        idCol.setCellValueFactory(data -> data.getValue().idProperty());

        TableColumn<PatientRow, String> nameCol = new TableColumn<PatientRow, String>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<PatientRow, String>("fullName"));

        TableColumn<PatientRow, String> emailCol = new TableColumn<PatientRow, String>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<PatientRow, String>("email"));

        TableColumn<PatientRow, String> phoneCol = new TableColumn<PatientRow, String>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<PatientRow, String>("phone"));

        TableColumn<PatientRow, String> dobCol = new TableColumn<PatientRow, String>("Date of Birth");
        dobCol.setCellValueFactory(new PropertyValueFactory<PatientRow, String>("dob"));

        TableColumn<PatientRow, Void> actionsCol = new TableColumn<PatientRow, Void>("Actions");
        actionsCol.setCellFactory(col -> new PatientActionCell());

        table.getColumns().addAll(idCol, nameCol, emailCol, phoneCol, dobCol, actionsCol);
        table.setItems(allPatients);

        styleModernTable(table);
        return table;
    }

    private VBox createContentTab() {
        VBox box = new VBox(18);
        box.setPadding(new Insets(24));

        VBox card = new VBox(18);
        card.setPadding(new Insets(28));
        card.setStyle(cardStyle(22));

        Label title = new Label("📚 Content Library");
        title.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 24));
        title.setTextFill(INK);

        Label subtitle = new Label(
                "Create and curate healing resources for your patients, organize your library, and deliver better personalized support."
        );
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
        subtitle.setTextFill(MUTED);
        subtitle.setWrapText(true);

        HBox tiles = new HBox(16);
        tiles.getChildren().addAll(
                createActionTile("🧘", "Meditations", "Guided sessions and calming resources"),
                createActionTile("🎵", "Music Therapy", "Relaxing playlists and audio content"),
                createActionTile("📖", "Articles", "Educational wellness resources")
        );

        Button manage = createPrimaryButton("Go to Content Manager");
        manage.setOnAction(e -> invokeParent("showContentPanel"));

        card.getChildren().addAll(title, subtitle, tiles, manage);
        box.getChildren().add(card);
        return box;
    }

    private VBox createActionTile(String icon, String title, String subtitle) {
        VBox tile = new VBox(8);
        tile.setAlignment(Pos.CENTER);
        tile.setPadding(new Insets(18));
        tile.setMaxWidth(Double.MAX_VALUE);
        tile.setMinHeight(145);
        tile.setStyle(
                "-fx-background-color: #F8F9FA;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: #E9ECEF;" +
                        "-fx-border-radius: 18;"
        );

        Label i = new Label(icon);
        i.setFont(Font.font("Segoe UI Emoji", 32));

        Label t = new Label(title);
        t.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        t.setTextFill(INK);

        Label s = new Label(subtitle);
        s.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        s.setTextFill(MUTED);
        s.setWrapText(true);
        s.setAlignment(Pos.CENTER);

        tile.getChildren().addAll(i, t, s);
        HBox.setHgrow(tile, Priority.ALWAYS);
        return tile;
    }

    private VBox createProfileCard() {
        VBox profile = new VBox(18);
        profile.setPadding(new Insets(24));
        profile.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #F8FFFB, #F7FAFF);" +
                        "-fx-background-radius: 22;" +
                        "-fx-border-color: #E6F3EB;" +
                        "-fx-border-width: 0 0 0 4;" +
                        "-fx-border-radius: 22;" +
                        "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.06), 18, 0, 0, 7);"
        );

        Label title = new Label("Your Professional Profile");
        title.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 24));
        title.setTextFill(INK);

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);

        grid.add(profileItem("Name", "Dr. " + psychologistName), 0, 0);
        grid.add(profileItem("Account Type", "Psychologist"), 1, 0);
        grid.add(profileItem("Workspace", "Mentis Care Center"), 0, 1);
        grid.add(profileItem("Status", "Active"), 1, 1);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(50);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(c1, c2);

        profile.getChildren().addAll(title, grid);
        return profile;
    }

    private VBox profileItem(String label, String value) {
        VBox item = new VBox(4);
        item.setPadding(new Insets(14, 16, 14, 16));
        item.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: #EDF2F7;" +
                        "-fx-border-radius: 14;"
        );

        Label l = new Label(label);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        l.setTextFill(MUTED);

        Label v = new Label(value);
        v.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        v.setTextFill(TEXT);

        item.getChildren().addAll(l, v);
        return item;
    }

    private HBox createSectionHeader(String text) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 20, 18, 20));
        header.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #FCFFFD, #F8FBFF);" +
                        "-fx-background-radius: 22 22 0 0;" +
                        "-fx-border-color: #EDF2F7;" +
                        "-fx-border-width: 0 0 1 0;"
        );

        Label title = new Label(text);
        title.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 20));
        title.setTextFill(INK);

        header.getChildren().add(title);
        return header;
    }

    private void showCrisisAlertsDialog() {
        Dialog<Void> dialog = new Dialog<Void>();
        dialog.setTitle("Crisis Alerts");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefWidth(520);
        dialog.getDialogPane().setPrefHeight(560);

        VBox content = new VBox(14);
        content.setPadding(new Insets(18));
        content.setStyle("-fx-background-color: white;");

        Label title = new Label("⚠️ High Risk Patient Alerts");
        title.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 22));
        title.setTextFill(RED);

        alertListBox = new VBox(10);

        ObservableList<ResultRow> highRisk = FXCollections.observableArrayList();
        for (ResultRow row : allResults) {
            if (isCritical(row.getRiskLevel())) {
                highRisk.add(row);
            }
        }

        if (highRisk.isEmpty()) {
            VBox empty = new VBox(10);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(50));
            Label icon = new Label("✅");
            icon.setFont(Font.font("Segoe UI Emoji", 46));
            Label msg = new Label("No crisis alerts right now.");
            msg.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
            msg.setTextFill(MUTED);
            empty.getChildren().addAll(icon, msg);
            alertListBox.getChildren().add(empty);
        } else {
            for (ResultRow row : highRisk) {
                alertListBox.getChildren().add(createAlertItem(row));
            }
        }

        ScrollPane scroll = new ScrollPane(alertListBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        content.getChildren().addAll(title, scroll);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private HBox createAlertItem(ResultRow result) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(14));
        item.setStyle(
                "-fx-background-color: #FFF4F4;" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-color: #FFD4D4;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-width: 0 0 0 4;"
        );

        Label icon = new Label("🚨");
        icon.setFont(Font.font("Segoe UI Emoji", 28));

        VBox details = new VBox(4);
        Label name = new Label(result.getPatientName());
        name.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 15));
        name.setTextFill(Color.web("#C0392B"));

        Label meta = new Label(result.getAssessmentTitle() + " • Score " + result.getTotalScore() + " • " + result.getTakenAt());
        meta.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        meta.setTextFill(MUTED);

        Label risk = createRiskBadge(result.getRiskLevel());

        details.getChildren().addAll(name, meta, risk);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button view = createOutlineButton("View");
        view.setOnAction(e -> showInfo("Open result #" + result.getId() + " from the Results module."));

        item.getChildren().addAll(icon, details, spacer, view);
        return item;
    }

    private void applyFilters() {
        if (filteredResults == null) return;

        String patientFilter = patientSearchField == null ? "" : patientSearchField.getText().trim().toLowerCase();
        String riskFilter = riskFilterBox == null || riskFilterBox.getValue() == null ? "" : riskFilterBox.getValue();
        LocalDate dateFilter = dateFilterPicker == null ? null : dateFilterPicker.getValue();

        filteredResults.setPredicate(row -> {
            boolean show = true;

            if (!patientFilter.isEmpty() && !row.getPatientName().toLowerCase().contains(patientFilter)) {
                show = false;
            }

            if (!riskFilter.isEmpty() && !riskFilter.equalsIgnoreCase(row.getRiskLevel())) {
                show = false;
            }

            if (dateFilter != null && !row.getTakenAt().startsWith(dateFilter.toString())) {
                show = false;
            }

            return show;
        });
    }

    private void loadDashboardData() {
        allResults = FXCollections.observableArrayList();
        allPatients = FXCollections.observableArrayList();

        loadResults();
        loadPatients();

        totalPatients = allPatients.size();
        totalAssessments = allResults.size();

        highRiskCount = 0;
        for (ResultRow row : allResults) {
            if (isCritical(row.getRiskLevel())) highRiskCount++;
        }
    }

    private void loadResults() {
        String sql =
                "SELECT r.result_id, r.user_id, r.assessment_id, r.total_score, r.risk_level, " +
                        "r.suggest_session, r.taken_at, " +
                        "u.firstname, u.lastname, a.title AS assessment_title " +
                        "FROM assessment_result r " +
                        "LEFT JOIN user u ON r.user_id = u.id " +
                        "LEFT JOIN assessment a ON r.assessment_id = a.assessment_id " +
                        "ORDER BY r.taken_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String first = safe(rs.getString("firstname"));
                String last = safe(rs.getString("lastname"));
                String patientName = (first + " " + last).trim();
                if (patientName.isEmpty()) patientName = "Unknown Patient";

                allResults.add(new ResultRow(
                        rs.getInt("result_id"),
                        rs.getInt("user_id"),
                        patientName,
                        safeDefault(rs.getString("assessment_title"), "Assessment"),
                        rs.getInt("total_score"),
                        safeDefault(rs.getString("risk_level"), "N/A"),
                        yesNo(rs.getBoolean("suggest_session")),
                        safeDefault(rs.getString("taken_at"), "N/A")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Could not load psychologist results: " + e.getMessage());
        }
    }

    private void loadPatients() {
        String sql = "SELECT id, firstname, lastname, email, phone, dateofbirth FROM user WHERE type = 'Patient' ORDER BY firstname";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                allPatients.add(new PatientRow(
                        rs.getInt("id"),
                        safe(rs.getString("firstname")),
                        safe(rs.getString("lastname")),
                        safe(rs.getString("email")),
                        safe(rs.getString("phone")),
                        safeDefault(rs.getString("dateofbirth"), "N/A")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Could not load psychologist patients: " + e.getMessage());
        }
    }

    public void refreshData() {
        loadDashboardData();

        if (totalPatientsLabel != null) totalPatientsLabel.setText(String.valueOf(totalPatients));
        if (totalAssessmentsLabel != null) totalAssessmentsLabel.setText(String.valueOf(totalAssessments));
        if (highRiskLabel != null) highRiskLabel.setText(String.valueOf(highRiskCount));
        if (alertBadge != null) {
            alertBadge.setText(String.valueOf(highRiskCount));
            alertBadge.setVisible(highRiskCount > 0);
        }

        if (resultsTable != null) {
            filteredResults = new FilteredList<ResultRow>(allResults, p -> true);
            resultsTable.setItems(filteredResults);
            applyFilters();
        }

        if (patientsTable != null) {
            patientsTable.setItems(allPatients);
        }
    }

    private void styleModernTable(TableView<?> table) {

        table.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            Platform.runLater(() -> {

                Node header = table.lookup("TableHeaderRow");

                if (header != null) {
                    header.setStyle(
                            "-fx-background-color: linear-gradient(to right, #50C878, #2E7D32);" +
                                    "-fx-background-radius: 20 20 0 0;"
                    );
                }

                table.lookupAll(".column-header").forEach(node ->
                        node.setStyle(
                                "-fx-background-color: transparent;" +
                                        "-fx-border-color: transparent;" +
                                        "-fx-padding: 16 10;"
                        )
                );

                table.lookupAll(".column-header .label").forEach(node ->
                        node.setStyle(
                                "-fx-text-fill: white;" +
                                        "-fx-font-size: 13px;" +
                                        "-fx-font-weight: bold;"
                        )
                );
            });
        });

        table.setRowFactory(tv -> new TableRow() {

            @Override
            protected void updateItem(Object item, boolean empty) {

                super.updateItem(item, empty);

                if (empty || item == null) {

                    setStyle("-fx-background-color: transparent;");

                } else {

                    setStyle(
                            getIndex() % 2 == 0
                                    ? "-fx-background-color: white;"
                                    : "-fx-background-color: #F8FBF9;"
                    );
                }
            }
        });
    }    private String tableStyle() {
        return "-fx-background-color: white;" +
                "-fx-background-radius: 0 0 20 20;" +
                "-fx-border-color: transparent;" +
                "-fx-selection-bar: #D7F5E3;" +
                "-fx-selection-bar-non-focused: #D7F5E3;";
    }

    class RiskCell<S> extends TableCell<S, String> {
        @Override
        protected void updateItem(String risk, boolean empty) {
            super.updateItem(risk, empty);
            if (empty || risk == null) {
                setGraphic(null);
            } else {
                setGraphic(createRiskBadge(risk));
            }
        }
    }

    class SessionCell<S> extends TableCell<S, String> {
        @Override
        protected void updateItem(String value, boolean empty) {
            super.updateItem(value, empty);
            if (empty || value == null) {
                setGraphic(null);
            } else {
                Label badge = new Label(value);
                badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                badge.setPadding(new Insets(6, 12, 6, 12));
                if ("Yes".equalsIgnoreCase(value)) {
                    badge.setTextFill(Color.web("#B42318"));
                    badge.setStyle("-fx-background-color: #FFE2E2; -fx-background-radius: 999;");
                } else {
                    badge.setTextFill(Color.web("#475569"));
                    badge.setStyle("-fx-background-color: #EEF2F7; -fx-background-radius: 999;");
                }
                setGraphic(badge);
            }
        }
    }

    class ResultActionCell extends TableCell<ResultRow, Void> {
        private final HBox box = new HBox(8);
        private final Button viewBtn = createSmallPrimaryButton("View");
        private final Button pdfBtn = createSmallGrayButton("PDF");

        public ResultActionCell() {
            box.setAlignment(Pos.CENTER);
            box.getChildren().addAll(viewBtn, pdfBtn);
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setGraphic(null);
            } else {
                ResultRow row = getTableView().getItems().get(getIndex());
                viewBtn.setOnAction(e -> showInfo("Open result #" + row.getId() + " from My Results."));
                pdfBtn.setOnAction(e -> showInfo("Export result #" + row.getId() + " from the Result module."));
                setGraphic(box);
            }
        }
    }

    class PatientActionCell extends TableCell<PatientRow, Void> {
        private final HBox box = new HBox(8);
        private final Button resultsBtn = createSmallPrimaryButton("Results");

        public PatientActionCell() {
            box.setAlignment(Pos.CENTER);
            box.getChildren().add(resultsBtn);
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setGraphic(null);
            } else {
                PatientRow row = getTableView().getItems().get(getIndex());
                resultsBtn.setOnAction(e -> {
                    patientSearchField.setText(row.getFullName());
                    TabPane tabPane = (TabPane) ((VBox) getParentPanel()).lookup(".tab-pane");
                    showInfo("Filtered assessment results for " + row.getFullName());
                });
                setGraphic(box);
            }
        }

        private Node getParentPanel() {
            return PsychologistDashboardPanel.this;
        }
    }

    public static class ResultRow {
        private final SimpleIntegerProperty id;
        private final SimpleIntegerProperty patientId;
        private final SimpleStringProperty patientName;
        private final SimpleStringProperty assessmentTitle;
        private final SimpleIntegerProperty totalScore;
        private final SimpleStringProperty riskLevel;
        private final SimpleStringProperty suggestSession;
        private final SimpleStringProperty takenAt;

        public ResultRow(int id, int patientId, String patientName, String assessmentTitle,
                         int totalScore, String riskLevel, String suggestSession, String takenAt) {
            this.id = new SimpleIntegerProperty(id);
            this.patientId = new SimpleIntegerProperty(patientId);
            this.patientName = new SimpleStringProperty(patientName);
            this.assessmentTitle = new SimpleStringProperty(assessmentTitle);
            this.totalScore = new SimpleIntegerProperty(totalScore);
            this.riskLevel = new SimpleStringProperty(riskLevel);
            this.suggestSession = new SimpleStringProperty(suggestSession);
            this.takenAt = new SimpleStringProperty(takenAt);
        }

        public int getId() { return id.get(); }
        public int getPatientId() { return patientId.get(); }
        public String getPatientName() { return patientName.get(); }
        public String getAssessmentTitle() { return assessmentTitle.get(); }
        public int getTotalScore() { return totalScore.get(); }
        public String getRiskLevel() { return riskLevel.get(); }
        public String getSuggestSession() { return suggestSession.get(); }
        public String getTakenAt() { return takenAt.get(); }

        public SimpleIntegerProperty idProperty() { return id; }
        public SimpleIntegerProperty patientIdProperty() { return patientId; }
        public SimpleStringProperty patientNameProperty() { return patientName; }
        public SimpleStringProperty assessmentTitleProperty() { return assessmentTitle; }
        public SimpleIntegerProperty totalScoreProperty() { return totalScore; }
        public SimpleStringProperty riskLevelProperty() { return riskLevel; }
        public SimpleStringProperty suggestSessionProperty() { return suggestSession; }
        public SimpleStringProperty takenAtProperty() { return takenAt; }
    }

    public static class PatientRow {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty firstName;
        private final SimpleStringProperty lastName;
        private final SimpleStringProperty email;
        private final SimpleStringProperty phone;
        private final SimpleStringProperty dob;

        public PatientRow(int id, String firstName, String lastName, String email, String phone, String dob) {
            this.id = new SimpleIntegerProperty(id);
            this.firstName = new SimpleStringProperty(firstName);
            this.lastName = new SimpleStringProperty(lastName);
            this.email = new SimpleStringProperty(email);
            this.phone = new SimpleStringProperty(phone);
            this.dob = new SimpleStringProperty(dob);
        }

        public int getId() { return id.get(); }
        public String getFirstName() { return firstName.get(); }
        public String getLastName() { return lastName.get(); }
        public String getFullName() { return (getFirstName() + " " + getLastName()).trim(); }
        public String getEmail() { return email.get(); }
        public String getPhone() { return phone.get(); }
        public String getDob() { return dob.get(); }

        public SimpleIntegerProperty idProperty() { return id; }
        public SimpleStringProperty firstNameProperty() { return firstName; }
        public SimpleStringProperty lastNameProperty() { return lastName; }
        public SimpleStringProperty fullNameProperty() { return new SimpleStringProperty(getFullName()); }
        public SimpleStringProperty emailProperty() { return email; }
        public SimpleStringProperty phoneProperty() { return phone; }
        public SimpleStringProperty dobProperty() { return dob; }
    }

    private Label createRiskBadge(String risk) {
        Label badge = new Label(risk);
        badge.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 12));
        badge.setPadding(new Insets(6, 12, 6, 12));

        String r = risk == null ? "" : risk.toLowerCase();

        if (r.contains("severe")) {
            badge.setTextFill(Color.WHITE);
            badge.setStyle("-fx-background-color: #E74C3C; -fx-background-radius: 999;");
        } else if (r.contains("high")) {
            badge.setTextFill(Color.web("#721C24"));
            badge.setStyle("-fx-background-color: #F8D7DA; -fx-background-radius: 999;");
        } else if (r.contains("moderate") || r.contains("mild")) {
            badge.setTextFill(Color.web("#856404"));
            badge.setStyle("-fx-background-color: #FFF3CD; -fx-background-radius: 999;");
        } else {
            badge.setTextFill(Color.web("#155724"));
            badge.setStyle("-fx-background-color: #D4EDDA; -fx-background-radius: 999;");
        }

        return badge;
    }

    private Label createDarkTag(String text) {
        Label tag = new Label(text);
        tag.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        tag.setTextFill(Color.web("#F8FAFC"));
        tag.setPadding(new Insets(10, 14, 10, 14));
        tag.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08);" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-color: rgba(255,255,255,0.10);" +
                        "-fx-border-radius: 999;"
        );
        return tag;
    }

    private Label createChip(String text) {
        Label chip = new Label(text);
        chip.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        chip.setTextFill(TEXT);
        chip.setPadding(new Insets(10, 14, 10, 14));
        chip.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #F0FFF5, #F7FAFF);" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-color: #DCEFE3;" +
                        "-fx-border-radius: 999;"
        );
        return chip;
    }

    private Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        button.setTextFill(Color.WHITE);
        button.setCursor(Cursor.HAND);
        button.setPadding(new Insets(11, 18, 11, 18));
        button.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #50C878, #2E7D32);" +
                        "-fx-background-radius: 14;" +
                        "-fx-effect: dropshadow(gaussian, rgba(80,200,120,0.25), 14, 0, 0, 5);"
        );
        return button;
    }

    private Button createWhiteButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 13));
        button.setTextFill(PURPLE);
        button.setCursor(Cursor.HAND);
        button.setPadding(new Insets(12, 22, 12, 22));
        button.setStyle("-fx-background-color: white; -fx-background-radius: 14;");
        return button;
    }

    private Button createOutlineButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        button.setTextFill(TEXT);
        button.setCursor(Cursor.HAND);
        button.setPadding(new Insets(10, 18, 10, 18));
        button.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #E9ECEF;" +
                        "-fx-border-radius: 12;"
        );
        return button;
    }

    private Button createSmallPrimaryButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        button.setTextFill(Color.WHITE);
        button.setCursor(Cursor.HAND);
        button.setStyle(
                "-fx-background-color: #50C878;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 7 12;"
        );
        return button;
    }

    private Button createSmallGrayButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        button.setTextFill(Color.WHITE);
        button.setCursor(Cursor.HAND);
        button.setStyle(
                "-fx-background-color: #6C757D;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 7 12;"
        );
        return button;
    }

    private String inputStyle() {
        return "-fx-background-color: white;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: #E9ECEF;" +
                "-fx-border-radius: 12;" +
                "-fx-padding: 9 14;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-size: 13px;";
    }

    private String glassCardStyle(int radius) {
        return "-fx-background-color: rgba(255,255,255,0.82);" +
                "-fx-background-radius: " + radius + ";" +
                "-fx-border-color: rgba(255,255,255,0.75);" +
                "-fx-border-radius: " + radius + ";" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.08), 24, 0, 0, 9);";
    }

    private String cardStyle(int radius) {
        return "-fx-background-color: white;" +
                "-fx-background-radius: " + radius + ";" +
                "-fx-border-color: #EDF2F7;" +
                "-fx-border-radius: " + radius + ";" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.06), 18, 0, 0, 7);";
    }

    private void addHover(Region node, String normal, String hover) {
        node.setOnMouseEntered(e -> node.setStyle(hover));
        node.setOnMouseExited(e -> node.setStyle(normal));
    }

    private boolean isCritical(String risk) {
        if (risk == null) return false;
        return risk.equalsIgnoreCase("High") || risk.equalsIgnoreCase("Severe");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String safeDefault(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private String yesNo(boolean value) {
        return value ? "Yes" : "No";
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mentis");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void invokeParent(String methodName) {
        try {
            Method method = parentApp.getClass().getMethod(methodName);
            method.invoke(parentApp);
        } catch (Exception e) {
            showInfo("Navigation method not found: " + methodName);
        }
    }

    private void startClock() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(30000);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                if (clockLabel != null) {
                                    clockLabel.setText("🕒 " + LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm a")));
                                }
                            }
                        });
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
