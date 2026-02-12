package ui;

import controller.AssessmentController;
import controller.AssessmentResultController;
import controller.QuestionController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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

    // Panels (will be created as needed)
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
    private Label userInfoLabel;

    @Override
    public void start(Stage primaryStage) {
        // Initialize controllers
        initializeControllers();

        // Create root layout
        root = new StackPane();
        root.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

        // Main container with BorderPane
        mainContainer = new BorderPane();

        // Create sidebar (initially hidden)
        sidebar = createSidebar();
        sidebar.setVisible(false);

        // Create content area with StackPane for panel switching
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

        // Initialize panels
        initializePanels();

        // Add panels to content area
        contentArea.getChildren().addAll(
                welcomePanel,
                loginPanel,
                signupPanel,
                adminDashboardPanel,
                psychologistTablePanel,
                patientTablePanel
        );

        // Hide all panels initially
        hideAllPanels();

        // Show welcome panel
        showWelcomePanel();

        // Layout
        mainContainer.setLeft(sidebar);
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
            System.out.println("All controllers initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing controllers: " + e.getMessage());
            e.printStackTrace();

            // Create mock controllers for testing
            assessmentController = createMockAssessmentController();
            questionController = createMockQuestionController();
            resultController = createMockResultController();

            System.out.println("Using mock controllers for testing");

            showAlert(Alert.AlertType.WARNING,
                    "Warning - Test Mode",
                    "Database connection failed. Using test mode.\nYou can test navigation but data won't be saved.\nError: " + e.getMessage());
        }
    }

    private void initializePanels() {
        welcomePanel = new MentisWelcomePanel(this);
        loginPanel = new MentisLoginPanel(this);
        signupPanel = new Mentissignuppanel(this);
        adminDashboardPanel = new AdminDashboardPanel(this);
        psychologistTablePanel = new PsychologistTablePanel(this);
        patientTablePanel = new PatientTablePanel(this);

        // Other panels will be created on demand
        assessmentPanel = null;
        questionPanel = null;
        takeAssessmentPanel = null;
        resultsPanel = null;
    }

    private void hideAllPanels() {
        welcomePanel.setVisible(false);
        loginPanel.setVisible(false);
        signupPanel.setVisible(false);
        adminDashboardPanel.setVisible(false);
        psychologistTablePanel.setVisible(false);
        patientTablePanel.setVisible(false);

        if (assessmentPanel != null) assessmentPanel.setVisible(false);
        if (questionPanel != null) questionPanel.setVisible(false);
        if (takeAssessmentPanel != null) takeAssessmentPanel.setVisible(false);
        if (resultsPanel != null) resultsPanel.setVisible(false);
    }

    // ================= SIDEBAR CREATION =================
    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #" + toHex(SIDEBAR_BG) + ";");
        sidebar.setPadding(new Insets(20, 15, 20, 15));
        sidebar.setSpacing(10);

        // Header with logo
        HBox header = createSidebarHeader();
        sidebar.getChildren().add(header);

        // User info label
        userInfoLabel = new Label("Not logged in");
        userInfoLabel.setFont(Font.font("Segoe UI", 14));
        userInfoLabel.setTextFill(Color.web(toHex(TEXT_LIGHT)));
        userInfoLabel.setAlignment(Pos.CENTER);
        userInfoLabel.setMaxWidth(Double.MAX_VALUE);
        sidebar.getChildren().add(userInfoLabel);

        // Separator
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

        // Logo
        ImageView logoView = createLogoImageView();
        if (logoView != null) {
            logoView.setFitWidth(50);
            logoView.setFitHeight(50);
            header.getChildren().add(logoView);
        } else {
            // Fallback emoji
            Label emojiLogo = new Label("🧠");
            emojiLogo.setFont(Font.font("Segoe UI", 32));
            emojiLogo.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));
            header.getChildren().add(emojiLogo);
        }

        // App name
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
            e.printStackTrace();
        }
        return null;
    }

    private void updateSidebarMenu() {
        // Clear existing menu items (keep header, user info, and first separator)
        sidebar.getChildren().removeIf(node ->
                node instanceof Button || (node instanceof Separator && sidebar.getChildren().indexOf(node) > 2)
        );

        // Add menu items based on user type
        addSidebarMenuItems();

        // Add logout button at bottom
        if (!currentUserType.isEmpty()) {
            VBox spacer = new VBox();
            VBox.setVgrow(spacer, Priority.ALWAYS);
            sidebar.getChildren().add(spacer);

            Button logoutBtn = createSidebarButton("Logout");
            logoutBtn.setOnAction(e -> logout());
            sidebar.getChildren().add(logoutBtn);
        }
    }

    // ================= FIXED: User-specific sidebar menu items =================
    private void addSidebarMenuItems() {
        if ("admin".equals(currentUserType)) {
            // Admin menu items with proper navigation targets
            addSidebarButton("Dashboard", "ADMIN_DASHBOARD");
            addSidebarButton("Sessions", "ADMIN_DASHBOARD");
            addSidebarButton("Assessments", "ASSESSMENT");
            addSidebarButton("Mood Tracking", "ADMIN_DASHBOARD");
            addSidebarButton("Content", "ADMIN_DASHBOARD");
            addSidebarButton("Event", "ADMIN_DASHBOARD");

        } else if ("psychologist".equals(currentUserType)) {
            // Psychologist menu items - all go to RESULTS panel
            addSidebarButton("Dashboard", "RESULTS");
            addSidebarButton("Sessions", "RESULTS");
            addSidebarButton("Assessments", "RESULTS");
            addSidebarButton("Mood Tracking", "RESULTS");
            addSidebarButton("Content", "RESULTS");
            addSidebarButton("Event", "RESULTS");

        } else if ("patient".equals(currentUserType)) {
            // Patient menu items - all go to TAKE_ASSESSMENT panel
            addSidebarButton("Dashboard", "TAKE_ASSESSMENT");
            addSidebarButton("Session", "TAKE_ASSESSMENT");
            addSidebarButton("Assessment", "TAKE_ASSESSMENT");
            addSidebarButton("Mood Tracking", "TAKE_ASSESSMENT");
            addSidebarButton("Content", "TAKE_ASSESSMENT");
            addSidebarButton("Event", "TAKE_ASSESSMENT");
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
        button.setFont(Font.font("Segoe UI", 16));
        button.setTextFill(Color.web(toHex(TEXT_DARK)));
        button.setStyle(
                "-fx-background-color: #" + toHex(SIDEBAR_BG) + ";" +
                        "-fx-background-radius: 0;" +
                        "-fx-border-width: 0;" +
                        "-fx-alignment: CENTER_LEFT;" +
                        "-fx-padding: 10 20 10 20;"
        );

        // Hover effect
        button.setOnMouseEntered(e ->
                button.setStyle("-fx-background-color: #" + toHex(HOVER_GREEN) + "; -fx-background-radius: 0; -fx-alignment: CENTER_LEFT; -fx-padding: 10 20 10 20;"));
        button.setOnMouseExited(e ->
                button.setStyle("-fx-background-color: #" + toHex(SIDEBAR_BG) + "; -fx-background-radius: 0; -fx-alignment: CENTER_LEFT; -fx-padding: 10 20 10 20;"));

        return button;
    }

    // ================= FIXED: Navigation handler with target panel =================
    private void handleSidebarNavigation(String menuText, String targetPanel) {
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
        hideAllPanels();
        welcomePanel.setVisible(true);
        sidebar.setVisible(false);
    }

    public void showLoginPanel() {
        hideAllPanels();
        loginPanel.setVisible(true);
        sidebar.setVisible(false);
    }

    public void showSignUpPanel() {
        hideAllPanels();
        signupPanel.setVisible(true);
        sidebar.setVisible(false);
    }

    public void showAdminDashboard() {
        hideAllPanels();
        adminDashboardPanel.setVisible(true);
        sidebar.setVisible(true);
        adminDashboardPanel.refreshData();
    }

    public void showPsychologistTablePanel() {
        hideAllPanels();
        psychologistTablePanel.setVisible(true);
        sidebar.setVisible(true);
        psychologistTablePanel.refreshTable();
    }

    public void showPatientTablePanel() {
        hideAllPanels();
        patientTablePanel.setVisible(true);
        sidebar.setVisible(true);
        patientTablePanel.refreshTable();
    }

    public void showAssessmentPanel() {
        if (assessmentPanel == null) {
            assessmentPanel = new AssessmentPanel(this, assessmentController);
            contentArea.getChildren().add(assessmentPanel);
        }

        hideAllPanels();
        assessmentPanel.setVisible(true);
        sidebar.setVisible(true);
        assessmentPanel.refreshData();
    }

    public void showQuestionPanel() {
        if (questionPanel == null) {
            questionPanel = new QuestionPanel(this, questionController, assessmentController);
            contentArea.getChildren().add(questionPanel);
        }

        hideAllPanels();
        questionPanel.setVisible(true);
        sidebar.setVisible(true);
        questionPanel.refreshData();
    }

    public void showTakeAssessmentPanel() {
        if (takeAssessmentPanel == null) {
            takeAssessmentPanel = new TakeAssessmentPanel(this, assessmentController, resultController);
            contentArea.getChildren().add(takeAssessmentPanel);
        }

        takeAssessmentPanel.setUserId(currentUserId);
        hideAllPanels();
        takeAssessmentPanel.setVisible(true);
        sidebar.setVisible(true);
        takeAssessmentPanel.refreshData();
    }

    public void showResultsPanel() {
        if (resultsPanel == null) {
            resultsPanel = new ResultsPanel(this, resultController);
            contentArea.getChildren().add(resultsPanel);
        }

        resultsPanel.setUserId(currentUserId);
        hideAllPanels();
        resultsPanel.setVisible(true);
        sidebar.setVisible(true);
        resultsPanel.refreshData();
    }

    public void showQuestionPanelWithAssessment(int assessmentId) {
        showQuestionPanel();
        if (questionPanel != null) {
            questionPanel.setCurrentAssessmentId(assessmentId);
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

        System.out.println("User logged in: " + userName + " (" + this.currentUserType + ") ID: " + userId);

        // Update sidebar
        userInfoLabel.setText(userName + " (" + userType + ")");
        sidebar.setVisible(true);
        updateSidebarMenu();

        // Navigate to appropriate panel
        switch (this.currentUserType) {
            case "admin":
                showAdminDashboard();
                break;
            case "patient":
                showTakeAssessmentPanel();
                break;
            case "psychologist":
                showResultsPanel();
                break;
            default:
                showWelcomePanel();
        }
    }

    public void logout() {
        System.out.println("User logging out: " + currentUserName);

        currentUserType = "";
        currentUserId = 0;
        currentUserName = "";

        userInfoLabel.setText("Not logged in");
        sidebar.setVisible(false);
        updateSidebarMenu();
        showWelcomePanel();
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

    // ================= UTILITY METHODS =================
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

    // ================= GETTERS =================
    public AssessmentController getAssessmentController() { return assessmentController; }
    public QuestionController getQuestionController() { return questionController; }
    public AssessmentResultController getResultController() { return resultController; }
    public String getUserType() { return currentUserType; }
    public int getUserId() { return currentUserId; }
    public String getUserName() { return currentUserName; }
    public int getLoggedInUserId() { return currentUserId; }

    public static void main(String[] args) {
        launch(args);
    }
}