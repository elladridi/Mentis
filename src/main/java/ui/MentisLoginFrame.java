package ui;

import controller.AssessmentController;
import controller.AssessmentResultController;
import controller.ContentNodeController;
import controller.ContentPathController;
import controller.QuestionController;
import controller.SessionController;
import controller.SessionReviewController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
    public static final Color BACKGROUND_LIGHT = Color.rgb(240, 248, 245);
    public static final Color CARD_WHITE = Color.rgb(255, 255, 255);
    public static final Color ACCENT_GREEN = Color.rgb(90, 150, 120);
    public static final Color ACCENT_DARK_GREEN = Color.rgb(60, 120, 90);
    public static final Color TEXT_DARK = Color.rgb(40, 70, 50);
    public static final Color TEXT_LIGHT = Color.rgb(100, 130, 110);
    public static final Color BORDER_LIGHT = Color.rgb(200, 220, 210);
    public static final Color BUTTON_LIGHT_GREEN = Color.rgb(160, 200, 180);
    public static final Color SIDEBAR_BG = Color.rgb(245, 250, 248);
    public static final Color HOVER_GREEN = Color.rgb(140, 190, 160);
    public static final Color ERROR_RED = Color.rgb(200, 119, 119);
    public static final Color SUCCESS_GREEN = Color.rgb(153, 205, 156);
    public static final Color WARNING_ORANGE = Color.rgb(255, 152, 0);

    private StackPane root;
    private BorderPane mainContainer;
    private VBox sidebar;
    private StackPane contentArea;

    // Navigation
    private String currentUserType = "";
    private int currentUserId = 0;
    private String currentUserName = "";

    // Controllers
    private AssessmentController assessmentController;
    private QuestionController questionController;
    private AssessmentResultController resultController;
    private ContentPathController contentPathController;
    private SessionController sessionController;
    private SessionReviewController sessionReviewController;

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

    private Label userInfoLabel;

    // Track current visible panel
    private Node currentVisiblePanel = null;

    @Override
    public void start(Stage primaryStage) {
        // Initialize controllers
        initializeControllers();

        // Create root layout
        root = new StackPane();
        root.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

        // Main container with BorderPane
        mainContainer = new BorderPane();

        // Create sidebar (initially not added to layout)
        sidebar = createSidebar();

        // Create content area
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

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
                recommendationsPanel
        );

        // Hide all panels initially
        hideAllPanels();

        // Show welcome panel (this will set the layout correctly)
        showWelcomePanel();

        // Layout - initially set only the center (no sidebar)
        mainContainer.setCenter(contentArea);

        root.getChildren().add(mainContainer);

        // Create scene
        Scene scene = new Scene(root, 1400, 800);

        // Setup stage
        primaryStage.setTitle("Mentis - Mental Health Companion");
        primaryStage.setScene(scene);
        primaryStage.show();
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

        // ⚠️ CRITICAL: DO NOT create AccessLogsPanel here - create on demand!
        accessLogsPanel = null;

        System.out.println("All panels initialized successfully");
    }

    /**
     * Hide EVERY panel properly!
     */
    private void hideAllPanels() {
        // Hide ALL panels - no exceptions!
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

        currentVisiblePanel = null;

        System.out.println("All panels hidden");
    }

    /**
     * Show ONLY one panel, hide all others
     * This method also handles sidebar visibility and layout
     */
    private void showOnlyPanel(Node panelToShow) {
        hideAllPanels();

        if (panelToShow != null) {
            panelToShow.setVisible(true);
            currentVisiblePanel = panelToShow;

            // List of panels that should be FULL WIDTH (no sidebar)
            boolean isFullWidthPanel =
                    panelToShow == welcomePanel ||
                            panelToShow == loginPanel ||
                            panelToShow == signupPanel;

            if (isFullWidthPanel) {
                // Remove sidebar completely for full-width panels
                mainContainer.setLeft(null);
                // Content area takes full width
                mainContainer.setCenter(contentArea);
                System.out.println("  - FULL WIDTH MODE: " + panelToShow.getClass().getSimpleName() + " (sidebar removed)");
            } else {
                // Add sidebar back for dashboard panels
                mainContainer.setLeft(sidebar);
                mainContainer.setCenter(contentArea);
                sidebar.setVisible(true);
                System.out.println("  - SIDEBAR MODE: " + panelToShow.getClass().getSimpleName() + " (sidebar visible)");
            }

            System.out.println("  - Showing panel: " + panelToShow.getClass().getSimpleName());
        }
    }

    // ================= SIDEBAR CREATION =================
    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #" + toHex(SIDEBAR_BG) + ";");
        sidebar.setPadding(new Insets(20, 15, 20, 15));
        sidebar.setSpacing(10);

        HBox header = createSidebarHeader();
        sidebar.getChildren().add(header);

        userInfoLabel = new Label("Not logged in");
        userInfoLabel.setFont(Font.font("Segoe UI", 14));
        userInfoLabel.setTextFill(Color.web(toHex(TEXT_LIGHT)));
        userInfoLabel.setAlignment(Pos.CENTER);
        userInfoLabel.setMaxWidth(Double.MAX_VALUE);
        sidebar.getChildren().add(userInfoLabel);

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #" + toHex(BORDER_LIGHT) + ";");
        separator.setMaxWidth(180);
        sidebar.getChildren().add(separator);

        return sidebar;
    }

    private HBox createSidebarHeader() {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));

        ImageView logoView = createLogoImageView();
        if (logoView != null) {
            logoView.setFitWidth(50);
            logoView.setFitHeight(50);
            header.getChildren().add(logoView);
        } else {
            Label fallbackLogo = new Label("M");
            fallbackLogo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
            fallbackLogo.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));
            header.getChildren().add(fallbackLogo);
        }

        Label appName = new Label("Mentis");
        appName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        appName.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));
        header.getChildren().add(appName);

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

    private void updateSidebarMenu() {
        // Clear existing menu items
        sidebar.getChildren().removeIf(node ->
                node instanceof Button || (node instanceof Separator && sidebar.getChildren().indexOf(node) > 2)
        );

        // Add menu items based on user type
        addSidebarMenuItems();

        // Add Access Logs button ONLY for admin
        if ("admin".equals(currentUserType)) {
            addSidebarButton("Access Logs", "ACCESS_LOGS");
        }

        // Add logout button
        if (!currentUserType.isEmpty()) {
            VBox spacer = new VBox();
            VBox.setVgrow(spacer, Priority.ALWAYS);
            sidebar.getChildren().add(spacer);

            Button logoutBtn = createSidebarButton("Logout");
            logoutBtn.setOnAction(e -> logout());
            sidebar.getChildren().add(logoutBtn);
        }
    }

    private void addSidebarMenuItems() {
        if ("admin".equals(currentUserType)) {
            // Admin menu items
            addSidebarButton("Dashboard", "ADMIN_DASHBOARD");
            addSidebarButton("Manage Sessions", "SESSION_ADMIN");
            addSidebarButton("Reservations", "RESERVATIONS");
            addSidebarButton("Assessments", "ASSESSMENT");
            addSidebarButton("Psychologists", "PSYCHOLOGIST");
            addSidebarButton("Patients", "PATIENT");
            addSidebarButton("Mood Tracking", "ADMIN_DASHBOARD");
            addSidebarButton("Wellbeing", "WELLBEING");
            addSidebarButton("Content", "CONTENT");
            addSidebarButton("Event", "ADMIN_DASHBOARD");

        } else if ("psychologist".equals(currentUserType)) {
            // Psychologist menu items
            addSidebarButton("Dashboard", "RESULTS");
            addSidebarButton("Manage Sessions", "SESSION_ADMIN");
            addSidebarButton("Reservations", "RESERVATIONS");
            addSidebarButton("Assessments", "RESULTS");
            addSidebarButton("Mood Tracking", "RESULTS");
            addSidebarButton("Wellbeing", "WELLBEING");
            addSidebarButton("Content", "CONTENT");
            addSidebarButton("Event", "RESULTS");

        } else if ("patient".equals(currentUserType)) {
            // Patient menu items
            addSidebarButton("Dashboard", "PATIENT_DASHBOARD");
            addSidebarButton("Available Sessions", "PATIENT_AVAILABLE_SESSIONS");
            addSidebarButton("My Sessions", "PATIENT_MY_SESSIONS");
            addSidebarButton("My Reviews", "PATIENT_MY_REVIEWS");
            addSidebarButton("Recommended For You", "RECOMMENDATIONS");
            addSidebarButton("Take Assessment", "TAKE_ASSESSMENT");
            addSidebarButton("My Results", "RESULTS");
            addSidebarButton("Mood Tracking", "PATIENT_DASHBOARD");
            addSidebarButton("Wellbeing", "WELLBEING");
            addSidebarButton("Content", "CONTENT");
            addSidebarButton("Event", "PATIENT_DASHBOARD");
        }
    }

    private void addSidebarButton(String text, String targetPanel) {
        Button button = createSidebarButton(text);
        button.setOnAction(e -> handleSidebarNavigation(text, targetPanel));
        sidebar.getChildren().add(button);
    }

    private Button createSidebarButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(45);
        button.setFont(Font.font("Segoe UI", 15));
        button.setTextFill(Color.web(toHex(TEXT_DARK)));
        button.setStyle(
                "-fx-background-color: #" + toHex(SIDEBAR_BG) + ";" +
                        "-fx-background-radius: 0;" +
                        "-fx-border-width: 0;" +
                        "-fx-alignment: CENTER_LEFT;" +
                        "-fx-padding: 10 20 10 20;"
        );

        button.setOnMouseEntered(e ->
                button.setStyle("-fx-background-color: #" + toHex(HOVER_GREEN) + "; -fx-background-radius: 0; -fx-alignment: CENTER_LEFT; -fx-padding: 10 20 10 20;"));
        button.setOnMouseExited(e ->
                button.setStyle("-fx-background-color: #" + toHex(SIDEBAR_BG) + "; -fx-background-radius: 0; -fx-alignment: CENTER_LEFT; -fx-padding: 10 20 10 20;"));

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
     * Access Logs panel - CREATED ONCE, REUSED!
     */
    public void showAccessLogsPanel() {
        System.out.println("🔵 Showing Access Logs Panel");

        try {
            // Set current user in ContentPathController
            contentPathController.setCurrentUser(currentUserId, currentUserType);

            // Create panel ONLY ONCE!
            if (accessLogsPanel == null) {
                accessLogsPanel = new AccessLogsPanel(this, contentPathController);
                contentArea.getChildren().add(accessLogsPanel);
                System.out.println("  - Created new AccessLogsPanel");
            }

            // Show the panel
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

    /**
     * Content panel - ALREADY CREATED, JUST REUSE!
     */
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

        System.out.println("✅ User logged in: " + userName + " (" + this.currentUserType + ") ID: " + userId);

        userInfoLabel.setText(userName + " (" + userType + ")");
        updateSidebarMenu();

        // Update user context in all relevant panels
        contentUploadPanel.setUserId(userId);
        takeAssessmentPanel.setUserId(userId);
        resultsPanel.setUserId(userId);

        // Reset AccessLogsPanel so it gets recreated with new user context
        if (accessLogsPanel != null) {
            accessLogsPanel = null;
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

        // Clear user data
        currentUserType = "";
        currentUserId = 0;
        currentUserName = "";

        // Reset dynamic panels to force fresh state on next login
        accessLogsPanel = null;

        // Update UI
        userInfoLabel.setText("Not logged in");
        updateSidebarMenu();

        // Show welcome panel (this will remove sidebar)
        showWelcomePanel();

        System.out.println("✅ Logout complete - Welcome Panel shown, sidebar removed");
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
    public int getLoggedInUserId() { return currentUserId; }

    public static void main(String[] args) {
        launch(args);
    }
}