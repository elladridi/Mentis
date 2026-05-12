package ui;

import controller.AssessmentController;
import controller.AssessmentResultController;
import controller.ContentNodeController;
import controller.ContentPathController;
import controller.QuestionController;
import controller.SessionController;
import controller.SessionReviewController;
import services.RememberMeService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Optional;

public class MentisLoginFrame extends Application {

    // JavaFX Color constants
    public static final Color BACKGROUND_LIGHT = Color.web("#F8F9FA");
    public static final Color CARD_WHITE = Color.web("#FFFFFF");
    public static final Color ACCENT_GREEN = Color.web("#50C878");
    public static final Color ACCENT_DARK_GREEN = Color.web("#2E7D32");
    public static final Color TEXT_DARK = Color.web("#1A3C34");
    public static final Color TEXT_LIGHT = Color.web("#6C757D");
    public static final Color BORDER_LIGHT = Color.web("#E9ECEF");
    public static final Color BUTTON_LIGHT_GREEN = Color.web("#A8E6CF");
    public static final Color SIDEBAR_BG = Color.web("#FFFFFF");
    public static final Color HOVER_GREEN = Color.web("#50C878");
    public static final Color ERROR_RED = Color.web("#E74C3C");
    public static final Color SUCCESS_GREEN = Color.web("#50C878");
    public static final Color WARNING_ORANGE = Color.web("#F39C12");

    private StackPane root;
    private BorderPane mainContainer;
    private VBox sidebar;
    private StackPane contentArea;

    // Navigation
    private String currentUserType = "";
    private int currentUserId = 0;
    private String currentUserName = "";
    private String currentUserEmail = "";
    private String currentUserPhone = "";

    // Controllers
    private AssessmentController assessmentController;
    private QuestionController questionController;
    private AssessmentResultController resultController;
    private ContentPathController contentPathController;
    private SessionController sessionController;
    private SessionReviewController sessionReviewController;

    // Remember Me Service
    private RememberMeService rememberMeService;

    // Panels - ALL panels are created ONCE and REUSED!
    private MentisWelcomePanel welcomePanel;
    private MentisLoginPanel loginPanel;
    private Mentissignuppanel signupPanel;
    private AdminDashboardPanel adminDashboardPanel;
    private PsychologistTablePanel psychologistTablePanel;
    private PatientTablePanel patientTablePanel;
    private AssessmentPanel assessmentPanel;
    private QuestionPanel questionPanel;
    private TakeAssessmentPanel takeAssessmentPanel;
    private ResultsPanel resultsPanel;
    private ContentUploadPanel contentUploadPanel;
    private AccessLogsPanel accessLogsPanel;

    // Session Module Panels
    private SessionPanel sessionPanel;
    private PatientAvailableSessionsPanel patientAvailableSessionsPanel;
    private PatientMySessionsPanel patientMySessionsPanel;
    private MyReviewsPanel myReviewsPanel;
    private ReservationsPanel reservationsPanel;
    private RecommendationsPanel recommendationsPanel;
    private SimpleCalendarPanel simpleCalendarPanel;
    private AnalyticsPanel analyticsPanel;

    // Event Panel (created on demand)
    private javafx.scene.layout.BorderPane eventPanel;

    private Label userInfoLabel;
    private VBox sidebarMenu;
    private Button logoutButton;
    private VBox sidebarFooter;

    // Track current visible panel
    private Node currentVisiblePanel = null;

    @Override
    public void start(Stage primaryStage) {
        // Initialize controllers
        initializeControllers();

        // Initialize Remember Me Service
        rememberMeService = new RememberMeService();

        // Create root layout
        root = new StackPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #EEF8F2, #F8FCF9);");

        // Main container with BorderPane
        mainContainer = new BorderPane();

        // Create sidebar (initially not added to layout)
        sidebar = createSidebar();

        // Create content area
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: transparent; -fx-padding: 22;");

        // Initialize ALL panels at startup
        initializeAllPanels();

        // Add ALL panels to content area ONCE
        contentArea.getChildren().addAll(
                welcomePanel,
                loginPanel,
                signupPanel,
                adminDashboardPanel,
                psychologistTablePanel,
                patientTablePanel,
                assessmentPanel,
                questionPanel,
                takeAssessmentPanel,
                resultsPanel,
                contentUploadPanel,
                sessionPanel,
                patientAvailableSessionsPanel,
                patientMySessionsPanel,
                myReviewsPanel,
                reservationsPanel,
                recommendationsPanel,
                simpleCalendarPanel,
                analyticsPanel
                // accessLogsPanel & eventPanel are added on demand
        );

        // Hide all panels initially
        hideAllPanels();

        // Show welcome panel (this will set the layout correctly)
        showWelcomePanel();

        // Layout - initially set only the center (no sidebar)
        mainContainer.setCenter(contentArea);

        root.getChildren().add(mainContainer);

        // Create scene
        Scene scene = new Scene(root, 1400, 820);

        // Setup stage
        primaryStage.setTitle("Mentis - Mental Health Companion");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Check for remembered user after UI is shown
        checkRememberedUser();
    }

    private void initializeControllers() {
        try {
            System.out.println("Initializing controllers...");
            assessmentController = new AssessmentController();
            questionController = new QuestionController();
            resultController = new AssessmentResultController();
            contentPathController = new ContentPathController();
            sessionController = new SessionController();
            sessionReviewController = new SessionReviewController();
            System.out.println("All controllers initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing controllers: " + e.getMessage());
            e.printStackTrace();

            // Create mock controllers for testing
            assessmentController = createMockAssessmentController();
            questionController = createMockQuestionController();
            resultController = createMockResultController();
            contentPathController = new ContentPathController();
            sessionController = createMockSessionController();
            sessionReviewController = createMockSessionReviewController();

            System.out.println("Using mock controllers for testing");

            showAlert(Alert.AlertType.WARNING,
                    "Warning - Test Mode",
                    "Database connection failed. Using test mode.\nYou can test navigation but data won't be saved.\nError: " + e.getMessage());
        }
    }

    // Check for remembered user
    private void checkRememberedUser() {
        RememberMeService.RememberMeToken token = rememberMeService.getRememberedUser();

        if (token != null && !token.isExpired()) {
            System.out.println("🔍 Found remembered user: " + token.getEmail());

            Platform.runLater(() -> {
                showLoginPanel();

                new Thread(() -> {
                    models.user loggedUser = services.userservice.getuserByEmail(token.getEmail());

                    Platform.runLater(() -> {
                        if (loggedUser != null) {
                            System.out.println("✅ Auto-login successful for: " + token.getEmail());
                            login(
                                    loggedUser.getType(),
                                    loggedUser.getId(),
                                    loggedUser.getFirstName() + " " + loggedUser.getLastName()
                            );
                        } else {
                            System.out.println("❌ Auto-login failed, clearing token");
                            rememberMeService.clearRememberMe();
                            showLoginPanel();
                        }
                    });
                }).start();
            });
        }
    }

    /**
     * Initialize ALL panels ONCE and REUSE them!
     */
    private void initializeAllPanels() {
        System.out.println("Initializing all panels...");

        welcomePanel = new MentisWelcomePanel(this);
        loginPanel = new MentisLoginPanel(this);
        signupPanel = new Mentissignuppanel(this);
        adminDashboardPanel = new AdminDashboardPanel(this);
        psychologistTablePanel = new PsychologistTablePanel(this);
        patientTablePanel = new PatientTablePanel(this);
        assessmentPanel = new AssessmentPanel(this, assessmentController);
        questionPanel = new QuestionPanel(this, questionController, assessmentController);
        takeAssessmentPanel = new TakeAssessmentPanel(this, assessmentController, resultController);
        resultsPanel = new ResultsPanel(this, resultController);
        contentUploadPanel = new ContentUploadPanel(this);

        // Session Module Panels
        sessionPanel = new SessionPanel(this, sessionController);
        patientAvailableSessionsPanel = new PatientAvailableSessionsPanel(this, sessionController);
        patientMySessionsPanel = new PatientMySessionsPanel(this, sessionController);
        myReviewsPanel = new MyReviewsPanel(this, sessionReviewController);
        reservationsPanel = new ReservationsPanel(this, sessionController);
        recommendationsPanel = new RecommendationsPanel(this, sessionController);
        simpleCalendarPanel = new SimpleCalendarPanel(this);
        analyticsPanel = new AnalyticsPanel(this);

        // Dynamic panels - created on demand
        accessLogsPanel = null;
        eventPanel = null;

        System.out.println("All panels initialized successfully");
    }

    /**
     * Hide EVERY panel properly!
     */
    private void hideAllPanels() {
        if (welcomePanel != null) welcomePanel.setVisible(false);
        if (loginPanel != null) loginPanel.setVisible(false);
        if (signupPanel != null) signupPanel.setVisible(false);
        if (adminDashboardPanel != null) adminDashboardPanel.setVisible(false);
        if (psychologistTablePanel != null) psychologistTablePanel.setVisible(false);
        if (patientTablePanel != null) patientTablePanel.setVisible(false);
        if (assessmentPanel != null) assessmentPanel.setVisible(false);
        if (questionPanel != null) questionPanel.setVisible(false);
        if (takeAssessmentPanel != null) takeAssessmentPanel.setVisible(false);
        if (resultsPanel != null) resultsPanel.setVisible(false);
        if (contentUploadPanel != null) contentUploadPanel.setVisible(false);
        if (accessLogsPanel != null) accessLogsPanel.setVisible(false);
        if (sessionPanel != null) sessionPanel.setVisible(false);
        if (patientAvailableSessionsPanel != null) patientAvailableSessionsPanel.setVisible(false);
        if (patientMySessionsPanel != null) patientMySessionsPanel.setVisible(false);
        if (myReviewsPanel != null) myReviewsPanel.setVisible(false);
        if (reservationsPanel != null) reservationsPanel.setVisible(false);
        if (recommendationsPanel != null) recommendationsPanel.setVisible(false);
        if (simpleCalendarPanel != null) simpleCalendarPanel.setVisible(false);
        if (analyticsPanel != null) analyticsPanel.setVisible(false);
        if (eventPanel != null) eventPanel.setVisible(false);

        currentVisiblePanel = null;

        System.out.println("All panels hidden");
    }

    /**
     * Show ONLY one panel, hide all others
     */
    private void showOnlyPanel(Node panelToShow) {
        hideAllPanels();

        if (panelToShow != null) {
            panelToShow.setVisible(true);
            currentVisiblePanel = panelToShow;

            boolean isFullWidthPanel =
                    panelToShow == welcomePanel ||
                            panelToShow == loginPanel ||
                            panelToShow == signupPanel;

            if (isFullWidthPanel) {
                mainContainer.setLeft(null);
                mainContainer.setCenter(contentArea);
                System.out.println("  - FULL WIDTH MODE: " + panelToShow.getClass().getSimpleName() + " (sidebar removed)");
            } else {
                mainContainer.setLeft(sidebar);
                mainContainer.setCenter(contentArea);
                sidebar.setVisible(true);
                System.out.println("  - SIDEBAR MODE: " + panelToShow.getClass().getSimpleName() + " (sidebar visible)");
            }

            System.out.println("  - Showing panel: " + panelToShow.getClass().getSimpleName());
        }
    }


    // ================= MODERN FRAME STYLE HELPERS =================
    private String css(Color color) {
        return "#" + toHex(color);
    }

    private String emeraldGradient() {
        return "linear-gradient(to right, #50C878, #3A9B5E)";
    }

    private String sidebarGradient() {
        return "linear-gradient(to bottom, #FFFFFF, #F4FBF7)";
    }

    private DropShadow sidebarShadow() {
        DropShadow shadow = new DropShadow();
        shadow.setRadius(30);
        shadow.setOffsetX(5);
        shadow.setOffsetY(0);
        shadow.setColor(Color.rgb(80, 120, 95, 0.10));
        return shadow;
    }

    private String modernSidebarButtonStyle(boolean active) {
        if (active) {
            return "-fx-background-color: linear-gradient(to right, #E8F5E9, #F6FCF8);" +
                    "-fx-text-fill: #1A3C34;" +
                    "-fx-background-radius: 18;" +
                    "-fx-border-radius: 18;" +
                    "-fx-border-color: #DCEFE4;" +
                    "-fx-cursor: hand;" +
                    "-fx-alignment: CENTER_LEFT;" +
                    "-fx-padding: 0 0 0 18;" +
                    "-fx-font-weight: 700;";
        }

        return "-fx-background-color: transparent;" +
                "-fx-text-fill: #1A3C34;" +
                "-fx-background-radius: 18;" +
                "-fx-cursor: hand;" +
                "-fx-alignment: CENTER_LEFT;" +
                "-fx-padding: 0 0 0 18;" +
                "-fx-font-weight: 600;";
    }

    private String iconForMenu(String text) {
        switch (text) {
            case "Dashboard": return "🏠";
            case "Manage Sessions": return "📅";
            case "Reservations": return "🗓";
            case "Session Calendar": return "📆";
            case "Analytics": return "📊";
            case "Assessments": return "📋";
            case "Psychologists": return "👩‍⚕️";
            case "Patients": return "👥";
            case "Mood Tracking": return "😊";
            case "Wellbeing": return "🌿";
            case "Content": return "📚";
            case "Event": return "🎟";
            case "Access Logs": return "🔐";
            case "Available Sessions": return "🔎";
            case "My Sessions": return "🧾";
            case "My Reviews": return "⭐";
            case "Recommended For You": return "✨";
            case "Take Assessment": return "▶";
            case "My Results": return "📈";
            case "Logout": return "🚪";
            default: return "•";
        }
    }

    // ================= SIDEBAR CREATION =================
    private VBox createSidebar() {
        VBox sidebar = new VBox(16);
        sidebar.setPrefWidth(280);
        sidebar.setMinWidth(280);
        sidebar.setMaxWidth(280);
        sidebar.setPadding(new Insets(44, 24, 22, 24));
        sidebar.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #FFFFFF, #F6FCF8);" +
                        "-fx-border-color: #E7F1EB;" +
                        "-fx-border-width: 0 1 0 0;"
        );
        sidebar.setEffect(sidebarShadow());

        HBox header = createSidebarHeader();
        sidebar.getChildren().add(header);

        VBox userCard = new VBox(6);
        userCard.setPadding(new Insets(16, 18, 16, 18));
        userCard.setMaxWidth(Double.MAX_VALUE);
        userCard.setStyle(
                "-fx-background-color: #F1F8E9;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-radius: 20;" +
                        "-fx-border-color: #DCEFE4;"
        );

        Label userSmall = new Label("Signed in as");
        userSmall.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        userSmall.setTextFill(Color.web("#7C9286"));

        userInfoLabel = new Label("Not logged in");
        userInfoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        userInfoLabel.setTextFill(Color.web("#1A3C34"));
        userInfoLabel.setWrapText(true);
        userInfoLabel.setMaxWidth(Double.MAX_VALUE);

        userCard.getChildren().addAll(userSmall, userInfoLabel);
        sidebar.getChildren().add(userCard);

        Separator separatorTop = new Separator();
        separatorTop.setStyle("-fx-background-color: #DDEBE4;");
        sidebar.getChildren().add(separatorTop);

        sidebarMenu = new VBox(10);
        sidebarMenu.setFillWidth(true);
        sidebarMenu.setStyle("-fx-background-color: transparent;");

        ScrollPane menuScroll = new ScrollPane(sidebarMenu);
        menuScroll.setFitToWidth(true);
        menuScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        menuScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        menuScroll.setStyle(
                "-fx-background: transparent;" +
                        "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;"
        );
        VBox.setVgrow(menuScroll, Priority.ALWAYS);
        sidebar.getChildren().add(menuScroll);

        sidebarFooter = new VBox(12);
        sidebarFooter.setFillWidth(true);

        Separator separatorBottom = new Separator();
        separatorBottom.setStyle("-fx-background-color: #DDEBE4;");

        logoutButton = createSidebarButton("Logout");
        logoutButton.setOnAction(e -> logout());
        logoutButton.setVisible(false);
        logoutButton.setManaged(false);

        sidebarFooter.getChildren().addAll(separatorBottom, logoutButton);
        sidebar.getChildren().add(sidebarFooter);

        return sidebar;
    }

    private HBox createSidebarHeader() {

        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(6, 0, 18, 6));

        // ===== LOGO =====
        ImageView logoView = createLogoImageView();

        if (logoView != null) {
            logoView.setFitWidth(58);
            logoView.setFitHeight(58);
            logoView.setPreserveRatio(true);
        }

        // ===== TEXT =====
        VBox textBox = new VBox(2);
        textBox.setAlignment(Pos.CENTER_LEFT);

        Label appName = new Label("MENTIS");
        appName.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 28));
        appName.setTextFill(Color.web("#2E7D32"));

        Label subtitle = new Label("Mental Health Platform");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        subtitle.setTextFill(Color.web("#7C9286"));

        textBox.getChildren().addAll(appName, subtitle);

        // ===== ADD TO HEADER =====
        if (logoView != null) {
            header.getChildren().addAll(logoView, textBox);
        } else {

            Label fallbackLogo = new Label("☘");
            fallbackLogo.setFont(Font.font("Segoe UI Emoji", FontWeight.BOLD, 48));
            fallbackLogo.setTextFill(Color.web("#2E7D32"));

            header.getChildren().addAll(fallbackLogo, textBox);
        }

        return header;
    }

    private ImageView createLogoImageView() {
        try {
            String[] possiblePaths = {
                    "/resources/logo.png",
                    "/images/logo.png",
                    "/logo.png"
            };

            for (String path : possiblePaths) {
                java.net.URL imageUrl = getClass().getResource(path);
                if (imageUrl != null) {
                    Image image = new Image(imageUrl.toExternalForm());
                    System.out.println("Found logo at: " + imageUrl);
                    return new ImageView(image);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading logo: " + e.getMessage());
        }
        return null;
    }


    private Label createSidebarSectionLabel(String text) {
        Label label = new Label(text.toUpperCase());
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        label.setTextFill(Color.web("#7C9286"));
        label.setPadding(new Insets(14, 0, 4, 12));
        return label;
    }

    private void addSidebarGroup(String title, String icon, String[][] items) {
        VBox groupBox = new VBox(7);
        groupBox.setStyle("-fx-background-color: transparent;");

        Button groupButton = new Button("▸  " + icon + "  " + title);
        groupButton.setMaxWidth(Double.MAX_VALUE);
        groupButton.setPrefHeight(52);
        groupButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        groupButton.setTextFill(Color.web("#1A3C34"));
        groupButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #F1F8E9, #F9FDFB);" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-radius: 18;" +
                        "-fx-border-color: #DCEFE4;" +
                        "-fx-cursor: hand;" +
                        "-fx-alignment: CENTER_LEFT;" +
                        "-fx-padding: 0 0 0 18;"
        );

        VBox childrenBox = new VBox(5);
        childrenBox.setVisible(false);
        childrenBox.setManaged(false);
        childrenBox.setPadding(new Insets(2, 0, 4, 18));

        for (String[] item : items) {
            Button child = createSidebarSubButton(item[0]);
            child.setOnAction(e -> handleSidebarNavigation(item[0], item[1]));
            childrenBox.getChildren().add(child);
        }

        groupButton.setOnAction(e -> {
            boolean open = !childrenBox.isVisible();
            childrenBox.setVisible(open);
            childrenBox.setManaged(open);
            groupButton.setText((open ? "▾  " : "▸  ") + icon + "  " + title);
        });

        groupBox.getChildren().addAll(groupButton, childrenBox);
        sidebarMenu.getChildren().add(groupBox);
    }

    private void addSidebarCard(String text, String icon, String targetPanel) {
        Button button = new Button(icon + "  " + text + "                                      ›");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(52);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        button.setTextFill(Color.web("#1A3C34"));
        button.setStyle(
                "-fx-background-color: linear-gradient(to right, #F1F8E9, #F9FDFB);" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-radius: 18;" +
                        "-fx-border-color: #DCEFE4;" +
                        "-fx-cursor: hand;" +
                        "-fx-alignment: CENTER_LEFT;" +
                        "-fx-padding: 0 0 0 18;"
        );

        button.setOnMouseEntered(e -> {
            button.setStyle(
                    "-fx-background-color: linear-gradient(to right, #E8F5E9, #FFFFFF);" +
                            "-fx-background-radius: 18;" +
                            "-fx-border-radius: 18;" +
                            "-fx-border-color: #BFE6CC;" +
                            "-fx-cursor: hand;" +
                            "-fx-alignment: CENTER_LEFT;" +
                            "-fx-padding: 0 0 0 18;" +
                            "-fx-effect: dropshadow(gaussian, rgba(80,200,120,0.18), 12, 0.2, 0, 4);"
            );
        });

        button.setOnMouseExited(e -> {
            button.setStyle(
                    "-fx-background-color: linear-gradient(to right, #F1F8E9, #F9FDFB);" +
                            "-fx-background-radius: 18;" +
                            "-fx-border-radius: 18;" +
                            "-fx-border-color: #DCEFE4;" +
                            "-fx-cursor: hand;" +
                            "-fx-alignment: CENTER_LEFT;" +
                            "-fx-padding: 0 0 0 18;"
            );
        });

        button.setOnAction(e -> handleSidebarNavigation(text, targetPanel));
        sidebarMenu.getChildren().add(button);
    }

    private Button createSidebarSubButton(String text) {
        Button button = new Button("•  " + text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(34);
        button.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        button.setTextFill(Color.web("#1A3C34"));
        button.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-background-radius: 14;" +
                        "-fx-cursor: hand;" +
                        "-fx-alignment: CENTER_LEFT;" +
                        "-fx-padding: 0 0 0 14;"
        );

        button.setOnMouseEntered(e -> {
            button.setTextFill(Color.web("#2E7D32"));
            button.setStyle(
                    "-fx-background-color: rgba(80,200,120,0.10);" +
                            "-fx-background-radius: 14;" +
                            "-fx-cursor: hand;" +
                            "-fx-alignment: CENTER_LEFT;" +
                            "-fx-padding: 0 0 0 14;"
            );
            button.setTranslateX(3);
        });

        button.setOnMouseExited(e -> {
            button.setTextFill(Color.web("#1A3C34"));
            button.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-background-radius: 14;" +
                            "-fx-cursor: hand;" +
                            "-fx-alignment: CENTER_LEFT;" +
                            "-fx-padding: 0 0 0 14;"
            );
            button.setTranslateX(0);
        });

        return button;
    }

    private void addSidebarSingle(String text, String targetPanel) {
        addSidebarCard(text, iconForMenu(text), targetPanel);
    }

    private void updateSidebarMenu() {
        if (sidebarMenu != null) {
            sidebarMenu.getChildren().clear();
        }

        addSidebarMenuItems();

        boolean loggedIn = !currentUserType.isEmpty();
        if (logoutButton != null) {
            logoutButton.setVisible(loggedIn);
            logoutButton.setManaged(loggedIn);
        }
        if (sidebarFooter != null) {
            sidebarFooter.setVisible(loggedIn);
            sidebarFooter.setManaged(loggedIn);
        }
    }

    private void addSidebarMenuItems() {
        if ("admin".equals(currentUserType)) {

            addSidebarSingle("Dashboard", "ADMIN_DASHBOARD");

            addSidebarGroup("Users", "👥", new String[][] {
                    {"Psychologists", "PSYCHOLOGIST"},
                    {"Patients", "PATIENT"},
                    {"Access Logs", "ACCESS_LOGS"}
            });

            addSidebarGroup("Sessions", "📅", new String[][] {
                    {"Manage Sessions", "SESSION_ADMIN"},
                    {"Reservations", "RESERVATIONS"},
                    {"Session Calendar", "CALENDAR"},
                    {"Analytics", "ANALYTICS"}
            });

            addSidebarGroup("Assessments", "📋", new String[][] {
                    {"Assessments", "ASSESSMENT"}
            });

            addSidebarGroup("Mood / Goals", "🌿", new String[][] {
                    {"Mood Tracking", "ADMIN_DASHBOARD"},
                    {"Wellbeing", "WELLBEING"}
            });

            addSidebarSingle("Content", "CONTENT");
            addSidebarSingle("Event", "EVENT");

        } else if ("psychologist".equals(currentUserType)) {

            addSidebarSingle("Dashboard", "RESULTS");

            addSidebarGroup("Sessions", "📅", new String[][] {
                    {"Manage Sessions", "SESSION_ADMIN"},
                    {"Reservations", "RESERVATIONS"},
                    {"Session Calendar", "CALENDAR"},
                    {"Analytics", "ANALYTICS"}
            });

            addSidebarGroup("Assessments", "📋", new String[][] {
                    {"Assessments", "RESULTS"}
            });

            addSidebarGroup("Mood / Wellbeing", "🌿", new String[][] {
                    {"Mood Tracking", "RESULTS"},
                    {"Wellbeing", "WELLBEING"}
            });

            addSidebarSingle("Content", "CONTENT");
            addSidebarSingle("Event", "EVENT");

        } else if ("patient".equals(currentUserType)) {

            addSidebarSingle("Dashboard", "PATIENT_DASHBOARD");

            addSidebarGroup("Sessions", "📅", new String[][] {
                    {"Available Sessions", "PATIENT_AVAILABLE_SESSIONS"},
                    {"My Sessions", "PATIENT_MY_SESSIONS"},
                    {"My Reviews", "PATIENT_MY_REVIEWS"},
                    {"Recommended For You", "RECOMMENDATIONS"}
            });

            addSidebarGroup("Assessment", "📋", new String[][] {
                    {"Take Assessment", "TAKE_ASSESSMENT"},
                    {"My Results", "RESULTS"}
            });

            addSidebarGroup("Goals / My Mood", "🌿", new String[][] {
                    {"Mood Tracking", "PATIENT_DASHBOARD"},
                    {"Wellbeing", "WELLBEING"}
            });

            addSidebarSingle("Content", "CONTENT");
            addSidebarSingle("Event", "EVENT");
        }
    }

    private void addSidebarButton(String text, String targetPanel) {
        Button button = createSidebarButton(text);
        button.setOnAction(e -> handleSidebarNavigation(text, targetPanel));
        sidebar.getChildren().add(button);
    }

    private Button createSidebarButton(String text) {
        String label = iconForMenu(text) + "  " + text;

        Button button = new Button(label);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(48);
        button.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        button.setTextFill(Color.web("#1A3C34"));
        button.setStyle(modernSidebarButtonStyle(false));

        button.setOnMouseEntered(e -> {
            button.setTextFill(Color.web("#2E7D32"));
            button.setStyle(modernSidebarButtonStyle(true));
            button.setTranslateX(3);
        });

        button.setOnMouseExited(e -> {
            button.setTextFill(Color.web("#1A3C34"));
            button.setStyle(modernSidebarButtonStyle(false));
            button.setTranslateX(0);
        });

        return button;
    }

    private void handleSidebarNavigation(String menuText, String targetPanel) {
        System.out.println("🔵 Navigation to: " + targetPanel);

        switch (targetPanel) {
            case "ADMIN_DASHBOARD":
                showAdminDashboard();
                break;
            case "ASSESSMENT":
                showAssessmentPanel();
                break;
            case "RESULTS":
                showResultsPanel();
                break;
            case "TAKE_ASSESSMENT":
                showTakeAssessmentPanel();
                break;
            case "WELLBEING":
                openWellbeingDashboard();
                break;
            case "CONTENT":
                showContentUploadPanel();
                break;
            case "ACCESS_LOGS":
                showAccessLogsPanel();
                break;
            case "SESSION_ADMIN":
                showSessionPanel();
                break;
            case "RESERVATIONS":
                showReservationsPanel();
                break;
            case "CALENDAR":
                showSimpleCalendarPanel();
                break;
            case "ANALYTICS":
                showAnalyticsPanel();
                break;
            case "PATIENT_AVAILABLE_SESSIONS":
                showPatientAvailableSessionsPanel();
                break;
            case "PATIENT_MY_SESSIONS":
                showPatientMySessionsPanel();
                break;
            case "PATIENT_MY_REVIEWS":
                showMyReviewsPanel();
                break;
            case "RECOMMENDATIONS":
                showRecommendationsPanel();
                break;
            case "PATIENT_DASHBOARD":
                showTakeAssessmentPanel();
                break;
            case "PSYCHOLOGIST":
                showPsychologistTablePanel();
                break;
            case "PATIENT":
                showPatientTablePanel();
                break;
            case "EVENT":
                showEventPanel();
                break;
            case "LOGOUT":
                logout();
                break;
            default:
                showComingSoon(menuText);
                break;
        }
    }

    // ================= NAVIGATION METHODS =================
    public void showWelcomePanel() {
        System.out.println("🔵 Showing Welcome Panel");
        showOnlyPanel(welcomePanel);
    }

    public void showLoginPanel() {
        System.out.println("🔵 Showing Login Panel");
        showOnlyPanel(loginPanel);
    }

    public void showSignUpPanel() {
        System.out.println("🔵 Showing SignUp Panel");
        showOnlyPanel(signupPanel);
    }

    public void showAdminDashboard() {
        System.out.println("🔵 Showing Admin Dashboard");
        showOnlyPanel(adminDashboardPanel);
        adminDashboardPanel.refreshData();
    }

    /**
     * Event Panel - Recreated each time to get fresh data from EventController
     */
    public void showEventPanel() {
        System.out.println("📌 Showing Event Panel");

        // Remove old event panel if it exists
        if (eventPanel != null) {
            contentArea.getChildren().remove(eventPanel);
        }

        // Create fresh EventController and get its view
        com.mentalhealth.app.controllers.EventController eventController =
                new com.mentalhealth.app.controllers.EventController();
        eventPanel = eventController.getView();

        // Add to content area and show
        contentArea.getChildren().add(eventPanel);
        showOnlyPanel(eventPanel);
    }

    /**
     * Access Logs panel - CREATED ONCE, REUSED!
     */
    public void showAccessLogsPanel() {
        System.out.println("🔵 Showing Access Logs Panel");

        try {
            contentPathController.setCurrentUser(currentUserId, currentUserType);

            if (accessLogsPanel == null) {
                accessLogsPanel = new AccessLogsPanel(this, contentPathController);
                contentArea.getChildren().add(accessLogsPanel);
                System.out.println("  - Created new AccessLogsPanel");
            }

            showOnlyPanel(accessLogsPanel);
            accessLogsPanel.refreshData();

            System.out.println("✅ Access Logs Panel shown");

        } catch (Exception e) {
            System.err.println("❌ Error showing Access Logs: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot open access logs: " + e.getMessage());
        }
    }

    public void showPsychologistTablePanel() {
        showOnlyPanel(psychologistTablePanel);
        psychologistTablePanel.refreshTable();
    }

    public void showPatientTablePanel() {
        showOnlyPanel(patientTablePanel);
        patientTablePanel.refreshTable();
    }

    public void showAssessmentPanel() {
        System.out.println("🔵 Showing Assessment Panel");
        showOnlyPanel(assessmentPanel);
        assessmentPanel.refreshData();
    }

    public void showQuestionPanel() {
        showOnlyPanel(questionPanel);
        questionPanel.refreshData();
    }

    public void showTakeAssessmentPanel() {
        System.out.println("🔵 Showing Take Assessment Panel");
        takeAssessmentPanel.setUserId(currentUserId);
        showOnlyPanel(takeAssessmentPanel);
        takeAssessmentPanel.refreshData();
    }

    public void showResultsPanel() {
        System.out.println("🔵 Showing Results Panel");
        resultsPanel.setUserId(currentUserId);
        showOnlyPanel(resultsPanel);
        resultsPanel.refreshData();
    }

    public void showContentUploadPanel() {
        System.out.println("🔵 Showing Content Upload Panel");
        contentUploadPanel.setUserId(currentUserId);
        showOnlyPanel(contentUploadPanel);
        contentUploadPanel.loadContentTable();
        contentUploadPanel.updateHistoryButtons();
        System.out.println("✅ ContentUploadPanel shown for user: " + currentUserId + " (" + currentUserType + ")");
    }

    // ========== SESSION MODULE NAVIGATION METHODS ==========

    public void showSessionPanel() {
        System.out.println("🔵 Showing Admin/Psychologist Session Panel");
        showOnlyPanel(sessionPanel);
        sessionPanel.refreshData();
    }

    public void showReservationsPanel() {
        System.out.println("🔵 Showing Reservations Panel");
        showOnlyPanel(reservationsPanel);
        reservationsPanel.refreshData();
    }

    public void showSimpleCalendarPanel() {
        System.out.println("🔵 Showing Simple Calendar Panel");
        showOnlyPanel(simpleCalendarPanel);
        simpleCalendarPanel.refreshData();
    }

    public void showAnalyticsPanel() {
        System.out.println("🔵 Showing Analytics Panel");
        showOnlyPanel(analyticsPanel);
        analyticsPanel.refreshData();
    }

    public void showPatientAvailableSessionsPanel() {
        System.out.println("🔵 Showing Patient Available Sessions Panel");
        showOnlyPanel(patientAvailableSessionsPanel);
        patientAvailableSessionsPanel.refreshData();
    }

    public void showPatientMySessionsPanel() {
        System.out.println("🔵 Showing Patient My Sessions Panel");
        showOnlyPanel(patientMySessionsPanel);
        patientMySessionsPanel.refreshData();
    }

    public void showMyReviewsPanel() {
        System.out.println("🔵 Showing My Reviews Panel");
        showOnlyPanel(myReviewsPanel);
        myReviewsPanel.refreshData();
    }

    public void showRecommendationsPanel() {
        System.out.println("🔵 Showing Recommendations Panel");
        showOnlyPanel(recommendationsPanel);
        recommendationsPanel.refreshData();
    }

    public void showQuestionPanelWithAssessment(int assessmentId) {
        showQuestionPanel();
        questionPanel.setCurrentAssessmentId(assessmentId);
    }

    private void openWellbeingDashboard() {
        try {
            javafx.fxml.FXMLLoader loader =
                    new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/HomeView.fxml"));
            javafx.scene.Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Mentis - Wellbeing Dashboard");
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Unable to open Wellbeing dashboard: " + e.getMessage());
        }
    }

    private void showComingSoon(String feature) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Coming Soon");
        alert.setHeaderText(null);
        alert.setContentText(feature + " feature coming soon!");
        alert.showAndWait();
    }

    // ================= LOGIN/LOGOUT SYSTEM =================
    public void login(String userType, int userId, String userName) {
        this.currentUserType = userType.toLowerCase();
        this.currentUserId = userId;
        this.currentUserName = userName;

        System.out.println("========== LOGIN DEBUG ==========");
        System.out.println("UserType: " + userType);
        System.out.println("UserId: " + userId);
        System.out.println("UserName: " + userName);

        // Fetch user email and phone from database
        String userEmail = "";
        String userPhone = "";

        try {
            java.sql.Connection conn = utils.MyDB.getInstance().getConnection();
            if (conn != null) {
                java.sql.PreparedStatement ps = conn.prepareStatement(
                        "SELECT * FROM user WHERE id = ?");
                ps.setInt(1, userId);
                java.sql.ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    java.sql.ResultSetMetaData metaData = rs.getMetaData();
                    System.out.println("Available columns in user table:");
                    for (int i = 1; i <= metaData.getColumnCount(); i++) {
                        System.out.println("  - " + metaData.getColumnName(i));
                    }

                    try { userEmail = rs.getString("email"); }
                    catch (Exception e) { System.err.println("Column 'email' not found"); }

                    try { userPhone = rs.getString("phone"); }
                    catch (Exception e) { System.err.println("Column 'phone' not found"); }

                    System.out.println("Fetched email: '" + userEmail + "'");
                    System.out.println("Fetched phone: '" + userPhone + "'");
                }
                rs.close();
                ps.close();
            } else {
                System.err.println("Database connection is null!");
            }
        } catch (Exception e) {
            System.err.println("Error fetching user details: " + e.getMessage());
            e.printStackTrace();
        }

        // Store locally
        this.currentUserEmail = userEmail;
        this.currentUserPhone = userPhone;

        // Populate UserSession for Events module
        com.mentalhealth.app.utils.UserSession.getInstance().login(
                userId, userName, userEmail, userPhone, userType);

        System.out.println("========== UserSession populated ==========");
        System.out.println("  UserId: " + com.mentalhealth.app.utils.UserSession.getInstance().getUserId());
        System.out.println("  UserName: " + com.mentalhealth.app.utils.UserSession.getInstance().getUserName());
        System.out.println("  UserEmail: " + com.mentalhealth.app.utils.UserSession.getInstance().getUserEmail());
        System.out.println("  UserPhone: " + com.mentalhealth.app.utils.UserSession.getInstance().getUserPhone());
        System.out.println("  UserType: " + com.mentalhealth.app.utils.UserSession.getInstance().getUserType());
        System.out.println("==========================================");

        System.out.println("✅ User logged in: " + userName + " (" + this.currentUserType + ") ID: " + userId);

        userInfoLabel.setText(userName + "\n" + userType);
        updateSidebarMenu();

        // Update user context in all relevant panels
        contentUploadPanel.setUserId(userId);
        takeAssessmentPanel.setUserId(userId);
        resultsPanel.setUserId(userId);

        // Reset dynamic panels so they get recreated with new user context
        if (accessLogsPanel != null) {
            contentArea.getChildren().remove(accessLogsPanel);
            accessLogsPanel = null;
        }

        // Reset event panel on new login
        if (eventPanel != null) {
            contentArea.getChildren().remove(eventPanel);
            eventPanel = null;
        }

        // Navigate to appropriate panel based on user type
        switch (this.currentUserType) {
            case "admin":
                showAdminDashboard();
                break;
            case "patient":
                showPatientAvailableSessionsPanel();
                break;
            case "psychologist":
                showResultsPanel();
                break;
            default:
                showWelcomePanel();
        }
    }

    /**
     * Logout - COMPLETELY CLEAN STATE!
     */
    public void logout() {
        System.out.println("🔴 Logging out: " + currentUserName);

        // Clear Events module UserSession
        com.mentalhealth.app.utils.UserSession.getInstance().logout();

        // Clear Remember Me token
        if (rememberMeService != null) {
            rememberMeService.clearRememberMe();
        }

        // Clear user data
        currentUserType = "";
        currentUserId = 0;
        currentUserName = "";
        currentUserEmail = "";
        currentUserPhone = "";

        // Reset dynamic panels
        if (accessLogsPanel != null) {
            contentArea.getChildren().remove(accessLogsPanel);
            accessLogsPanel = null;
        }

        // Clean up event panel
        if (eventPanel != null) {
            contentArea.getChildren().remove(eventPanel);
            eventPanel = null;
        }

        // Update UI
        userInfoLabel.setText("Not logged in");
        updateSidebarMenu();

        showWelcomePanel();

        System.out.println("✅ Logout complete");
    }

    // ================= DIALOG METHODS =================
    public void showAddPsychologistDialog(PsychologistTablePanel panel) {
        AddPsychologistDialog dialog = new AddPsychologistDialog(panel);
        dialog.showAndWait();
    }

    public void showUpdatePsychologistDialog(PsychologistTablePanel panel, int id,
                                             String firstName, String lastName,
                                             String phone, String dob, String email) {
        UpdatePsychologistDialog dialog = new UpdatePsychologistDialog(
                panel, id, firstName, lastName, phone, dob, email);
        dialog.showAndWait();
    }

    public void showUpdatePatientDialog(PatientTablePanel panel, int id,
                                        String firstName, String lastName,
                                        String phone, String dob, String email) {
        UpdatePatientDialog dialog = new UpdatePatientDialog(
                panel, id, firstName, lastName, phone, dob, email);
        dialog.showAndWait();
    }

    public void showAlert(Alert.AlertType type, String title, String content) {
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

    // ================= MOCK CONTROLLERS =================
    private AssessmentController createMockAssessmentController() {
        return new AssessmentController() {
            @Override
            public java.util.List<models.Assessment> getAllAssessments() throws java.sql.SQLException {
                System.out.println("Mock: getAllAssessments() called");
                return new java.util.ArrayList<>();
            }
            @Override
            public java.util.List<models.Assessment> getActiveAssessments() throws java.sql.SQLException {
                System.out.println("Mock: getActiveAssessments() called");
                return new java.util.ArrayList<>();
            }
            @Override
            public void createAssessment(models.Assessment assessment) throws java.sql.SQLException {
                System.out.println("Mock: createAssessment() called");
            }
            @Override
            public void updateAssessment(models.Assessment assessment) throws java.sql.SQLException {
                System.out.println("Mock: updateAssessment() called");
            }
            @Override
            public void deleteAssessment(int assessmentId) throws java.sql.SQLException {
                System.out.println("Mock: deleteAssessment() called for ID: " + assessmentId);
            }
            @Override
            public boolean updateAssessmentStatus(int assessmentId, String status) throws java.sql.SQLException {
                System.out.println("Mock: updateAssessmentStatus() called - ID: " + assessmentId + ", Status: " + status);
                return true;
            }
        };
    }

    private QuestionController createMockQuestionController() {
        return new QuestionController() {
            @Override
            public java.util.List<models.Question> getAllQuestions() throws java.sql.SQLException {
                System.out.println("Mock: getAllQuestions() called");
                return new java.util.ArrayList<>();
            }
            @Override
            public java.util.List<models.Question> getQuestionsByAssessment(int assessmentId) throws java.sql.SQLException {
                System.out.println("Mock: getQuestionsByAssessment() called for ID: " + assessmentId);
                return new java.util.ArrayList<>();
            }
        };
    }

    private AssessmentResultController createMockResultController() {
        return new AssessmentResultController() {
            @Override
            public java.util.List<models.AssessmentResult> getUserResults(int userId) throws java.sql.SQLException {
                System.out.println("Mock: getUserResults() called for user: " + userId);
                return new java.util.ArrayList<>();
            }
        };
    }

    private SessionController createMockSessionController() {
        return new SessionController() {
            @Override
            public java.util.List<models.Session> getAllSessions() throws java.sql.SQLException {
                System.out.println("Mock: getAllSessions() called");
                return new java.util.ArrayList<>();
            }
            @Override
            public java.util.List<models.Session> getActiveSessions() throws java.sql.SQLException {
                System.out.println("Mock: getActiveSessions() called");
                return new java.util.ArrayList<>();
            }
            @Override
            public void createSession(models.Session session) throws java.sql.SQLException {
                System.out.println("Mock: createSession() called");
            }
            @Override
            public void updateSession(models.Session session) throws java.sql.SQLException {
                System.out.println("Mock: updateSession() called");
            }
            @Override
            public void deleteSession(int sessionId) throws java.sql.SQLException {
                System.out.println("Mock: deleteSession() called for ID: " + sessionId);
            }
            @Override
            public boolean updateSessionStatus(int sessionId, String status) throws java.sql.SQLException {
                System.out.println("Mock: updateSessionStatus() called - ID: " + sessionId + ", Status: " + status);
                return true;
            }
            @Override
            public java.util.List<models.Session> getAvailableSessions() throws java.sql.SQLException {
                System.out.println("Mock: getAvailableSessions() called");
                return new java.util.ArrayList<>();
            }
            @Override
            public java.util.List<models.Session> getPatientSessions(int patientId) throws java.sql.SQLException {
                System.out.println("Mock: getPatientSessions() called for patient: " + patientId);
                return new java.util.ArrayList<>();
            }
            @Override
            public void reserveSession(int sessionId, int patientId) throws java.sql.SQLException {
                System.out.println("Mock: reserveSession() called - Session: " + sessionId + ", Patient: " + patientId);
            }
            @Override
            public void cancelReservation(int sessionId, int patientId) throws java.sql.SQLException {
                System.out.println("Mock: cancelReservation() called - Session: " + sessionId + ", Patient: " + patientId);
            }
        };
    }

    @Override
    public void stop() {
        try {
            services.LocalTTSService.shutdown();
            System.out.println("✅ TTS resources cleaned up");
        } catch (Exception e) {
            System.err.println("❌ Error cleaning up TTS: " + e.getMessage());
        }
        System.out.println("Application stopped");
    }

    private SessionReviewController createMockSessionReviewController() {
        return new SessionReviewController() {
            @Override
            public java.util.List<models.SessionReview> getMyReviews(int patientId) throws java.sql.SQLException {
                System.out.println("Mock: getMyReviews() called for patient: " + patientId);
                return new java.util.ArrayList<>();
            }
            @Override
            public void addReview(int sessionId, int patientId, int rating, String comment) throws java.sql.SQLException {
                System.out.println("Mock: addReview() called");
            }
            @Override
            public void updateReview(int reviewId, int patientId, int rating, String comment) throws java.sql.SQLException {
                System.out.println("Mock: updateReview() called");
            }
            @Override
            public void deleteReview(int reviewId, int patientId) throws java.sql.SQLException {
                System.out.println("Mock: deleteReview() called");
            }
        };
    }

    // ================= GETTERS =================
    public AssessmentController getAssessmentController() { return assessmentController; }
    public QuestionController getQuestionController() { return questionController; }
    public AssessmentResultController getResultController() { return resultController; }
    public ContentPathController getContentPathController() { return contentPathController; }
    public SessionController getSessionController() { return sessionController; }
    public SessionReviewController getSessionReviewController() { return sessionReviewController; }
    public String getUserType() { return currentUserType; }
    public int getUserId() { return currentUserId; }
    public String getUserName() { return currentUserName; }
    public String getUserEmail() { return currentUserEmail; }
    public String getUserPhone() { return currentUserPhone; }
    public int getLoggedInUserId() { return currentUserId; }

    public static void main(String[] args) {
        launch(args);
    }
}