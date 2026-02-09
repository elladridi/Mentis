package ui;

import controller.AssessmentController;
import controller.AssessmentResultController;
import controller.QuestionController;

import javax.swing.*;
import java.awt.*;

public class MentisLoginFrame extends JFrame {

    public static final Color BACKGROUND_LIGHT = new Color(240, 248, 245);
    public static final Color CARD_WHITE = new Color(255, 255, 255);
    public static final Color ACCENT_GREEN = new Color(90, 150, 120);
    public static final Color ACCENT_DARK_GREEN = new Color(60, 120, 90);
    public static final Color TEXT_DARK = new Color(40, 70, 50);
    public static final Color TEXT_LIGHT = new Color(100, 130, 110);
    public static final Color BORDER_LIGHT = new Color(200, 220, 210);
    public static final Color BUTTON_LIGHT_GREEN = new Color(160, 200, 180);
    public static final Color SIDEBAR_BG = new Color(245, 250, 248);
    public static final Color BACKGROUND_BEIGE = new Color(243, 243, 243);
    public static final Color HIGHLIGHT_GREEN = new Color(100, 180, 140);
    public static final Color WARNING_RED = new Color(197, 134, 134);
    public static final Color INFO_BLUE = new Color(132, 160, 205);
    public static final Color DISABLED_GRAY = new Color(200, 200, 200);
    public static final Color ACCENT_LIGHT_GREEN = new Color(120, 180, 150);
    public static final Color TEXT_GRAY = new Color(120, 120, 120);
    public static final Color HOVER_GREEN = new Color(140, 190, 160);
    public static final Color ERROR_RED = new Color(200, 119, 119);
    public static final Color WARNING_ORANGE = new Color(255, 152, 0);
    public static final Color SUCCESS_GREEN = new Color(153, 205, 156);

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel sidebarPanel;

    // User information
    private String userType = ""; // "admin", "patient", "psychologist", or empty for not logged in
    private int userId = 0;
    private String userName = "";

    // Panels
    private AdminDashboardPanel adminDashboardPanel;
    private MentisWelcomePanel welcomePanel;
    private MentisLoginPanel loginPanel;
    private Mentissignuppanel signUpPanel;
    private PsychologistTablePanel psychologistTablePanel;
    private PatientTablePanel patientTablePanel;
    private AssessmentPanel assessmentPanel;
    private QuestionPanel questionPanel;
    private TakeAssessmentPanel takeAssessmentPanel;
    private ResultsPanel resultsPanel;
    private JPanel patientDashboardPanel;
    private JPanel psychologistDashboardPanel;

    // Controllers
    private AssessmentController assessmentController;
    private QuestionController questionController;
    private AssessmentResultController resultController;

    public MentisLoginFrame() {
        setTitle("Mentis - Mental Health Companion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 800);
        setLocationRelativeTo(null);

        // Initialize controllers FIRST
        initializeControllers();

        // Create main container with BorderLayout
        JPanel container = new JPanel(new BorderLayout());

        // Create sidebar (initially hidden)
        sidebarPanel = createSidebar();
        sidebarPanel.setVisible(false);

        // Create main content area with CardLayout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Create all panels
        welcomePanel = new MentisWelcomePanel(this);
        loginPanel = new MentisLoginPanel(this);
        signUpPanel = new Mentissignuppanel(this);
        adminDashboardPanel = new AdminDashboardPanel(this);
        psychologistTablePanel = new PsychologistTablePanel(this);
        patientTablePanel = new PatientTablePanel(this);

        // Create placeholder panels for patient and psychologist dashboards
        patientDashboardPanel = createPlaceholderPanel("Patient Dashboard");
        psychologistDashboardPanel = createPlaceholderPanel("Psychologist Dashboard");

        // Initialize other panels (will be created when needed)
        assessmentPanel = null;
        questionPanel = null;
        takeAssessmentPanel = null;
        resultsPanel = null;

        // Add panels to CardLayout
        mainPanel.add(welcomePanel, "WELCOME");
        mainPanel.add(loginPanel, "LOGIN");
        mainPanel.add(signUpPanel, "SIGNUP");
        mainPanel.add(adminDashboardPanel, "ADMIN_DASHBOARD");
        mainPanel.add(psychologistTablePanel, "PSYCHOLOGIST_TABLE");
        mainPanel.add(patientTablePanel, "PATIENT_TABLE");
        mainPanel.add(patientDashboardPanel, "PATIENT_DASHBOARD");
        mainPanel.add(psychologistDashboardPanel, "PSYCHOLOGIST_DASHBOARD");

        // Layout
        container.add(sidebarPanel, BorderLayout.WEST);
        container.add(mainPanel, BorderLayout.CENTER);

        add(container);

        // Show welcome panel first
        showWelcomePanel();
    }

    private JPanel createPlaceholderPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_LIGHT);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(ACCENT_DARK_GREEN);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));

        panel.add(titleLabel, BorderLayout.CENTER);
        return panel;
    }

    private void initializeControllers() {
        try {
            System.out.println("Initializing controllers...");

            // Initialize AssessmentController
            assessmentController = new AssessmentController();
            System.out.println("AssessmentController initialized");

            // Initialize other controllers
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

            JOptionPane.showMessageDialog(this,
                    "Database connection failed. Using test mode.\n" +
                            "You can test navigation but data won't be saved.\n" +
                            "Error: " + e.getMessage(),
                    "Warning - Test Mode",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    // Create mock controllers
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

    // ================= SIDEBAR CREATION =================
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(220, 800));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        addSidebarHeader(sidebar);
        // Menu items will be added when user logs in

        return sidebar;
    }

    private void addSidebarHeader(JPanel sidebar) {
        sidebar.add(Box.createVerticalStrut(30));

        // Create a panel for the logo and text - CHANGED TO HORIZONTAL
        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.X_AXIS)); // Changed from Y_AXIS to X_AXIS
        logoPanel.setBackground(SIDEBAR_BG);
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15)); // Add some padding

        // Try to load the actual logo image
        JLabel logoImageLabel = createLogoImageLabel();

        // If no image loaded, use emoji as fallback
        if (logoImageLabel == null) {
            JLabel emojiLogo = new JLabel("🧠");
            emojiLogo.setFont(new Font("Segoe UI", Font.PLAIN, 32)); // Smaller size for side-by-side
            emojiLogo.setForeground(ACCENT_DARK_GREEN);
            emojiLogo.setAlignmentY(Component.CENTER_ALIGNMENT);
            logoPanel.add(emojiLogo);
            logoPanel.add(Box.createHorizontalStrut(10)); // Add spacing between logo and text
        } else {
            logoImageLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
            logoPanel.add(logoImageLabel);
            logoPanel.add(Box.createHorizontalStrut(15)); // Add spacing between logo and text
        }

        // Add app name
        JLabel appName = new JLabel("Mentis");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 24)); // Slightly smaller font
        appName.setForeground(ACCENT_DARK_GREEN);
        appName.setAlignmentY(Component.CENTER_ALIGNMENT);
        logoPanel.add(appName);

        // Center the logo panel horizontally in the sidebar
        JPanel centeredLogoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centeredLogoPanel.setBackground(SIDEBAR_BG);
        centeredLogoPanel.add(logoPanel);

        sidebar.add(centeredLogoPanel);
        sidebar.add(Box.createVerticalStrut(40));

        // User info display
        JLabel userInfoLabel = new JLabel("Not logged in");
        userInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userInfoLabel.setForeground(TEXT_LIGHT);
        userInfoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        userInfoLabel.setName("userInfoLabel");
        sidebar.add(userInfoLabel);

        sidebar.add(Box.createVerticalStrut(40));

        // Add separator line
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setMaximumSize(new Dimension(180, 2));
        separator.setForeground(BORDER_LIGHT);
        separator.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(separator);

        sidebar.add(Box.createVerticalStrut(20));
    }

    private JLabel createLogoImageLabel() {
        try {
            // Try different possible paths for the logo
            String[] possiblePaths = {
                    "/resources/logo.png",
                    "/images/logo.png",
                    "logo.png",
                    "resources/logo.png",
                    "images/logo.png"
            };

            for (String path : possiblePaths) {
                java.net.URL logoURL = getClass().getResource(path);
                if (logoURL != null) {
                    System.out.println("Found logo at: " + path);
                    ImageIcon originalIcon = new ImageIcon(logoURL);
                    Image scaledImage = originalIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH); // Smaller size (40x40)
                    JLabel label = new JLabel(new ImageIcon(scaledImage));
                    label.setAlignmentY(Component.CENTER_ALIGNMENT); // Changed from CENTER_ALIGNMENT_X to CENTER_ALIGNMENT_Y
                    return label;
                }
            }

            // If not found in resources, try absolute path as fallback
            java.net.URL logoURL = getClass().getClassLoader().getResource("logo.png");
            if (logoURL != null) {
                ImageIcon originalIcon = new ImageIcon(logoURL);
                Image scaledImage = originalIcon.getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH); // Smaller size (40x40)
                JLabel label = new JLabel(new ImageIcon(scaledImage));
                label.setAlignmentY(Component.CENTER_ALIGNMENT); // Changed from CENTER_ALIGNMENT_X to CENTER_ALIGNMENT_Y
                return label;
            }

            System.out.println("Logo not found in any of the paths");

        } catch (Exception e) {
            System.err.println("Error loading logo image: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private void updateUserInfoInSidebar() {
        for (Component comp : sidebarPanel.getComponents()) {
            if (comp.getName() != null && comp.getName().equals("userInfoLabel")) {
                String displayName = userName.isEmpty() ? "Not logged in" : userName + " (" + userType + ")";
                ((JLabel) comp).setText(displayName);
                break;
            }
        }
    }

    private void updateSidebarMenu() {
        // Clear existing menu items
        Component[] components = sidebarPanel.getComponents();
        java.util.List<Component> toRemove = new java.util.ArrayList<>();

        // Keep only header components (logo and user info)
        for (Component comp : components) {
            if (comp instanceof JButton) {
                toRemove.add(comp);
            }
        }

        for (Component comp : toRemove) {
            sidebarPanel.remove(comp);
        }

        // Add menu items based on user type
        addSidebarMenuItems(sidebarPanel);

        sidebarPanel.revalidate();
        sidebarPanel.repaint();
    }

    private void addSidebarMenuItems(JPanel sidebar) {
        if ("admin".equals(userType)) {
            addSidebarButton(sidebar, "Dashboard", "ADMIN_DASHBOARD");
            addSidebarButton(sidebar, "Sessions", "ADMIN_DASHBOARD");
            addSidebarButton(sidebar, "Assessments", "ASSESSMENT");
            addSidebarButton(sidebar, "Mood Tracking", "ADMIN_DASHBOARD");
            addSidebarButton(sidebar, "Content", "ADMIN_DASHBOARD");
            addSidebarButton(sidebar, "Event", "ADMIN_DASHBOARD");
        } else if ("psychologist".equals(userType)) {
            addSidebarButton(sidebar, "Dashboard", "RESULTS");
            addSidebarButton(sidebar, "Sessions", "RESULTS");
            addSidebarButton(sidebar, "Assessments", "RESULTS");
            addSidebarButton(sidebar, "Mood Tracking", "RESULTS");
            addSidebarButton(sidebar, "Content", "RESULTS");
            addSidebarButton(sidebar, "Event", "RESULTS");
        } else if ("patient".equals(userType)) {
            addSidebarButton(sidebar, "Dashboard", "TAKE_ASSESSMENT");
            addSidebarButton(sidebar, "Session", "TAKE_ASSESSMENT");
            addSidebarButton(sidebar, "Assessment", "TAKE_ASSESSMENT");
            addSidebarButton(sidebar, "Mood Tracking", "TAKE_ASSESSMENT");
            addSidebarButton(sidebar, "Content", "TAKE_ASSESSMENT");
            addSidebarButton(sidebar, "Event", "TAKE_ASSESSMENT");
        }

        sidebar.add(Box.createVerticalGlue());

        // Common settings and logout for all logged-in users
        if (!userType.isEmpty()) {
            addSidebarButton(sidebar, "Settings", "SETTINGS");
            addSidebarButton(sidebar, "Logout", "LOGOUT");
        }

        sidebar.revalidate();
        sidebar.repaint();
    }

    private void addSidebarButton(JPanel sidebar, String text, String panelName) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setForeground(TEXT_DARK);
        button.setBackground(SIDEBAR_BG);
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(HOVER_GREEN);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(SIDEBAR_BG);
            }
        });

        // Action listener
        button.addActionListener(e -> handleSidebarNavigation(panelName));

        sidebar.add(button);
    }

    private void handleSidebarNavigation(String panelName) {
        if ("LOGOUT".equals(panelName)) {
            logout();
            return;
        }

        // Navigation based on panel name
        switch (panelName) {
            case "ADMIN_DASHBOARD":
                showAdminDashboard();
                break;
            case "PSYCHOLOGIST_DASHBOARD":
                showPsychologistDashboard();
                break;
            case "PATIENT_DASHBOARD":
                showPatientDashboard();
                break;
            case "PSYCHOLOGIST_TABLE":
                showPsychologistTablePanel();
                break;
            case "PATIENT_TABLE":
                showPatientTablePanel();
                break;
            case "ASSESSMENT":
                showAssessmentPanel();
                break;
            case "QUESTIONS":
                showQuestionPanel();
                break;
            case "RESULTS":
                showResultsPanel();
                break;
            case "TAKE_ASSESSMENT":
                showTakeAssessmentPanel();
                break;
            case "MY_RESULTS":
                showResultsPanel();
                break;
            case "SETTINGS":
                JOptionPane.showMessageDialog(this,
                        "Settings feature coming soon!",
                        "Coming Soon",
                        JOptionPane.INFORMATION_MESSAGE);
                break;
            default:
                // For other panels, show coming soon message
                String buttonText = "";
                for (Component comp : sidebarPanel.getComponents()) {
                    if (comp instanceof JButton && ((JButton) comp).getActionListeners().length > 0) {
                        // Find which button was clicked
                        buttonText = ((JButton) comp).getText();
                    }
                }
                JOptionPane.showMessageDialog(this,
                        buttonText + " feature coming soon!",
                        "Coming Soon",
                        JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ================= LOGIN/LOGOUT SYSTEM =================
    public void login(String userType, int userId, String userName) {
        this.userType = userType.toLowerCase();
        this.userId = userId;
        this.userName = userName;

        System.out.println("User logged in: " + userName + " (" + this.userType + ") ID: " + userId);

        // Update sidebar user info
        updateUserInfoInSidebar();

        // Show sidebar for all logged-in users
        sidebarPanel.setVisible(true);

        // Refresh sidebar menu based on user type
        updateSidebarMenu();

        // Navigate to appropriate panel based on user type
        switch (this.userType) {
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

        revalidate();
        repaint();
    }

    public void logout() {
        System.out.println("User logging out: " + userName);

        userType = "";
        userId = 0;
        userName = "";

        // Hide sidebar
        sidebarPanel.setVisible(false);

        // Clear sidebar menu
        updateSidebarMenu();

        // Reset user info label
        updateUserInfoInSidebar();

        // Show welcome panel
        showWelcomePanel();

        revalidate();
        repaint();
    }

    // ================= NAVIGATION METHODS =================
    public void showWelcomePanel() {
        sidebarPanel.setVisible(false);
        showPanel("WELCOME");
    }

    public void showLoginPanel() {
        sidebarPanel.setVisible(false);
        showPanel("LOGIN");
    }

    public void showSignUpPanel() {
        sidebarPanel.setVisible(false);
        showPanel("SIGNUP");
    }

    public void showAdminDashboard() {
        if (adminDashboardPanel != null) {
            adminDashboardPanel.refreshData();
        }
        showPanel("ADMIN_DASHBOARD");
    }

    public void showPatientDashboard() {
        showPanel("PATIENT_DASHBOARD");
    }

    public void showPsychologistDashboard() {
        showPanel("PSYCHOLOGIST_DASHBOARD");
    }

    public void showPsychologistTablePanel() {
        if (psychologistTablePanel != null) {
            psychologistTablePanel.refreshTable();
        }
        showPanel("PSYCHOLOGIST_TABLE");
    }

    public void showPatientTablePanel() {
        if (patientTablePanel != null) {
            patientTablePanel.refreshTable();
        }
        showPanel("PATIENT_TABLE");
    }

    public void showAssessmentPanel() {
        // Create AssessmentPanel if it doesn't exist
        if (assessmentPanel == null) {
            assessmentPanel = new AssessmentPanel(this, assessmentController);
            mainPanel.add(assessmentPanel, "ASSESSMENT");
        }

        // Refresh and show the panel
        try {
            assessmentPanel.refreshData();
            showPanel("ASSESSMENT");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading assessments: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void showQuestionPanel() {
        // Create QuestionPanel if it doesn't exist
        if (questionPanel == null) {
            questionPanel = new QuestionPanel(this, questionController, assessmentController);
            mainPanel.add(questionPanel, "QUESTIONS");
        }

        // Refresh and show the panel
        try {
            questionPanel.refreshData();
            showPanel("QUESTIONS");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading questions: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void showTakeAssessmentPanel() {
        // Create TakeAssessmentPanel if it doesn't exist
        if (takeAssessmentPanel == null) {
            takeAssessmentPanel = new TakeAssessmentPanel(this, assessmentController, resultController);
            mainPanel.add(takeAssessmentPanel, "TAKE_ASSESSMENT");
        }

        // Set user ID for the take assessment panel
        takeAssessmentPanel.setUserId(userId);

        // Refresh and show the panel
        try {
            takeAssessmentPanel.refreshData();
            showPanel("TAKE_ASSESSMENT");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading assessments: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void showResultsPanel() {
        // Create ResultsPanel if it doesn't exist
        if (resultsPanel == null) {
            resultsPanel = new ResultsPanel(this, resultController);
            mainPanel.add(resultsPanel, "RESULTS");
        }

        // Set user ID for the results panel
        resultsPanel.setUserId(userId);

        // Refresh and show the panel
        try {
            resultsPanel.refreshData();
            showPanel("RESULTS");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading results: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Show QuestionPanel with specific assessment ID (for Manage Questions button)
    public void showQuestionPanelWithAssessment(int assessmentId) {
        showQuestionPanel();
        if (questionPanel != null) {
            questionPanel.setCurrentAssessmentId(assessmentId);
        }
    }

    // Generic showPanel method
    public void showPanel(String panelName) {
        System.out.println("Navigating to: " + panelName);

        if (cardLayout != null && mainPanel != null) {
            // Create panel if it doesn't exist
            if (panelNeedsCreation(panelName)) {
                createPanel(panelName);
            }

            cardLayout.show(mainPanel, panelName);

            // Refresh data when panel is shown
            refreshPanelData(panelName);
        }
    }

    private boolean panelNeedsCreation(String panelName) {
        switch (panelName) {
            case "ASSESSMENT":
                return assessmentPanel == null;
            case "QUESTIONS":
                return questionPanel == null;
            case "TAKE_ASSESSMENT":
                return takeAssessmentPanel == null;
            case "RESULTS":
                return resultsPanel == null;
            default:
                return false;
        }
    }

    private void createPanel(String panelName) {
        switch (panelName) {
            case "ASSESSMENT":
                assessmentPanel = new AssessmentPanel(this, assessmentController);
                mainPanel.add(assessmentPanel, "ASSESSMENT");
                break;
            case "QUESTIONS":
                questionPanel = new QuestionPanel(this, questionController, assessmentController);
                mainPanel.add(questionPanel, "QUESTIONS");
                break;
            case "TAKE_ASSESSMENT":
                takeAssessmentPanel = new TakeAssessmentPanel(this, assessmentController, resultController);
                mainPanel.add(takeAssessmentPanel, "TAKE_ASSESSMENT");
                break;
            case "RESULTS":
                resultsPanel = new ResultsPanel(this, resultController);
                mainPanel.add(resultsPanel, "RESULTS");
                break;
        }
    }

    private void refreshPanelData(String panelName) {
        switch (panelName) {
            case "ASSESSMENT":
                if (assessmentPanel != null) assessmentPanel.refreshData();
                break;
            case "QUESTIONS":
                if (questionPanel != null) questionPanel.refreshData();
                break;
            case "TAKE_ASSESSMENT":
                if (takeAssessmentPanel != null) takeAssessmentPanel.refreshData();
                break;
            case "RESULTS":
                if (resultsPanel != null) resultsPanel.refreshData();
                break;
            case "ADMIN_DASHBOARD":
                if (adminDashboardPanel != null) adminDashboardPanel.refreshData();
                break;
            case "PSYCHOLOGIST_TABLE":
                if (psychologistTablePanel != null) psychologistTablePanel.refreshTable();
                break;
            case "PATIENT_TABLE":
                if (patientTablePanel != null) patientTablePanel.refreshTable();
                break;
        }
    }

    // ================= DIALOG METHODS =================
    public void showAddPsychologistDialog(PsychologistTablePanel panel) {
        AddPsychologistDialog dialog = new AddPsychologistDialog(this, panel);
        dialog.setVisible(true);
    }

    public void showUpdatePsychologistDialog(PsychologistTablePanel panel, int id,
                                             String firstName, String lastName,
                                             String phone, String dob, String email) {
        UpdatePsychologistDialog dialog = new UpdatePsychologistDialog(
                this, panel, id, firstName, lastName, phone, dob, email);
        dialog.setVisible(true);
    }

    public void showUpdatePatientDialog(PatientTablePanel panel, int id,
                                        String firstName, String lastName,
                                        String phone, String dob, String email) {
        UpdatePatientDialog dialog = new UpdatePatientDialog(
                this, panel, id, firstName, lastName, phone, dob, email);
        dialog.setVisible(true);
    }

    // ================= GETTERS =================
    public AssessmentController getAssessmentController() {
        return assessmentController;
    }

    public QuestionController getQuestionController() {
        return questionController;
    }

    public AssessmentResultController getResultController() {
        return resultController;
    }

    public String getUserType() {
        return userType;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    // Get logged in user ID for panels
    public int getLoggedInUserId() {
        return userId;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            MentisLoginFrame frame = new MentisLoginFrame();
            frame.setVisible(true);
        });
    }
}