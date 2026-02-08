package ui;

import javax.swing.*;
import java.awt.*;

public class MentisLoginFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private MentisWelcomePanel welcomePanel;
    private MentisLoginPanel loginPanel;
    private Mentissignuppanel signUpPanel;
    private AdminDashboardPanel adminDashboardPanel;
    private PsychologistTablePanel psychologistTablePanel;
    private PatientTablePanel patientTablePanel;

    public MentisLoginFrame() {
        setTitle("Mentis - Mental Health Companion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Use CardLayout for panel switching
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Create all panels
        welcomePanel = new MentisWelcomePanel(this);
        loginPanel = new MentisLoginPanel(this);
        signUpPanel = new Mentissignuppanel(this);
        adminDashboardPanel = new AdminDashboardPanel(this);
        psychologistTablePanel = new PsychologistTablePanel(this);
        patientTablePanel = new PatientTablePanel(this);

        // Add panels to CardLayout
        mainPanel.add(welcomePanel, "WELCOME");
        mainPanel.add(loginPanel, "LOGIN");
        mainPanel.add(signUpPanel, "SIGNUP");
        mainPanel.add(adminDashboardPanel, "ADMIN_DASHBOARD");
        mainPanel.add(psychologistTablePanel, "PSYCHOLOGIST_TABLE");
        mainPanel.add(patientTablePanel, "PATIENT_TABLE");

        add(mainPanel);

        // Show welcome panel first
        cardLayout.show(mainPanel, "WELCOME");

        pack();
        setLocationRelativeTo(null); // Center the frame
    }

    // Navigation methods
    public void showWelcomePanel() {
        cardLayout.show(mainPanel, "WELCOME");
    }

    public void showLoginPanel() {
        cardLayout.show(mainPanel, "LOGIN");
    }

    public void showSignUpPanel() {
        cardLayout.show(mainPanel, "SIGNUP");
    }

    public void showAdminDashboard() {
        // Refresh statistics when showing dashboard
        mainPanel.remove(adminDashboardPanel);
        adminDashboardPanel = new AdminDashboardPanel(this);
        mainPanel.add(adminDashboardPanel, "ADMIN_DASHBOARD");
        cardLayout.show(mainPanel, "ADMIN_DASHBOARD");
    }

    public void showPsychologistTablePanel() {
        psychologistTablePanel.refreshTable();
        cardLayout.show(mainPanel, "PSYCHOLOGIST_TABLE");
    }

    public void showPatientTablePanel() {
        patientTablePanel.refreshTable();
        cardLayout.show(mainPanel, "PATIENT_TABLE");
    }

    // Dialog methods
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Use system look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            MentisLoginFrame frame = new MentisLoginFrame();
            frame.setVisible(true);
        });
    }
}