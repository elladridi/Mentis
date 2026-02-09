package ui;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import controller.AssessmentResultController;
import models.AssessmentResult;
import java.sql.SQLException;
import java.util.List;
import java.text.SimpleDateFormat;

public class ResultsPanel extends JPanel {
    private MentisLoginFrame parentFrame;
    private AssessmentResultController controller;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JTextField userIdField;
    private JLabel userTypeLabel;
    private JComboBox<String> viewModeComboBox;

    public ResultsPanel(MentisLoginFrame parentFrame, AssessmentResultController controller) {
        this.parentFrame = parentFrame;
        this.controller = controller;
        setLayout(new BorderLayout());
        setBackground(parentFrame.BACKGROUND_BEIGE);
        setBorder(BorderFactory.createEmptyBorder(45, 50, 45, 50));

        createHeader();
        createTable();
        refreshData();
    }

    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(parentFrame.BACKGROUND_BEIGE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 35, 0));

        JLabel titleLabel = new JLabel("Assessment Results");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 42));
        titleLabel.setForeground(parentFrame.ACCENT_GREEN);

        // Top right panel
        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        topRightPanel.setBackground(parentFrame.BACKGROUND_BEIGE);

        // User type indicator
        String userType = parentFrame.getUserType();
        userTypeLabel = new JLabel("User: " + userType.toUpperCase());
        userTypeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userTypeLabel.setForeground(parentFrame.ACCENT_GREEN);
        userTypeLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));

        // Assessment button - ONLY FOR ADMIN
        JButton assessmentLink = null;
        if ("admin".equals(userType)) {
            assessmentLink = createHeaderLink("Assessment");
            assessmentLink.addActionListener(e -> parentFrame.showPanel("ASSESSMENT"));
            topRightPanel.add(assessmentLink);
            topRightPanel.add(Box.createHorizontalStrut(20));
        }

        // View mode dropdown (for admin/psychologist only)
        viewModeComboBox = new JComboBox<>(new String[]{"My Results", "All Results", "Filter by User ID"});
        viewModeComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        viewModeComboBox.setVisible(false); // Initially hidden
        viewModeComboBox.addActionListener(e -> onViewModeChanged());

        // User ID field
        userIdField = new JTextField(10);
        userIdField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userIdField.setBackground(parentFrame.ACCENT_LIGHT_GREEN);
        userIdField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(parentFrame.ACCENT_LIGHT_GREEN, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        userIdField.setToolTipText("Enter User ID to filter results");
        userIdField.setVisible(false); // Initially hidden
        userIdField.addActionListener(e -> refreshData());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        refreshButton.setBackground(parentFrame.ACCENT_GREEN);
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        refreshButton.setFocusPainted(false);
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> refreshData());

        topRightPanel.add(userTypeLabel);

        // Only show view mode controls for admin/psychologist
        if ("admin".equals(userType) || "psychologist".equals(userType)) {
            topRightPanel.add(Box.createHorizontalStrut(20));
            topRightPanel.add(viewModeComboBox);
            topRightPanel.add(Box.createHorizontalStrut(10));
            topRightPanel.add(new JLabel("User ID:"));
            topRightPanel.add(userIdField);
        }

        topRightPanel.add(Box.createHorizontalStrut(10));
        topRightPanel.add(refreshButton);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(topRightPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
    }

    private void onViewModeChanged() {
        if (viewModeComboBox.getSelectedItem().equals("Filter by User ID")) {
            userIdField.setVisible(true);
            userIdField.requestFocus();
        } else {
            userIdField.setVisible(false);
            userIdField.setText("");
        }
        refreshData();
    }

    private JButton createHeaderLink(String text) {
        JButton link = new JButton(text);
        link.setFont(new Font("Segoe UI", Font.BOLD, 16));
        link.setForeground(parentFrame.ACCENT_GREEN);
        link.setBackground(parentFrame.BACKGROUND_BEIGE);
        link.setBorderPainted(false);
        link.setFocusPainted(false);
        link.setContentAreaFilled(false);
        link.setCursor(new Cursor(Cursor.HAND_CURSOR));

        link.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                link.setText("<html><u>" + text + "</u></html>");
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                link.setText(text);
            }
        });

        return link;
    }

    private void createTable() {
        String[] columns = {"ID", "User ID", "Assessment ID", "Score", "Risk Level", "Date", "Session Suggested", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7;
            }
        };

        resultsTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);

                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? parentFrame.CARD_WHITE : new Color(248, 248, 248));
                }

                // Color code risk levels
                if (column == 4) {
                    String risk = getValueAt(row, column).toString().toLowerCase();
                    if (risk.contains("high") || risk.contains("severe")) {
                        c.setForeground(new Color(180, 0, 0));
                    } else if (risk.contains("moderate") || risk.contains("mild")) {
                        c.setForeground(new Color(153, 102, 0));
                    } else {
                        c.setForeground(new Color(0, 128, 0));
                    }
                }

                return c;
            }
        };

        resultsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        resultsTable.setRowHeight(50);
        resultsTable.setBackground(parentFrame.CARD_WHITE);
        resultsTable.setGridColor(parentFrame.BORDER_LIGHT);
        resultsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        resultsTable.getTableHeader().setBackground(parentFrame.CARD_WHITE);
        resultsTable.getTableHeader().setForeground(parentFrame.TEXT_DARK);
        resultsTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, parentFrame.BORDER_LIGHT));

        // Column widths
        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        resultsTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        resultsTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        resultsTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        resultsTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        resultsTable.getColumnModel().getColumn(6).setPreferredWidth(130);
        resultsTable.getColumnModel().getColumn(7).setPreferredWidth(100);

        // Action buttons
        resultsTable.getColumnModel().getColumn(7).setCellRenderer(new ButtonRenderer());
        resultsTable.getColumnModel().getColumn(7).setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(parentFrame.BORDER_LIGHT, 1));
        scrollPane.getViewport().setBackground(parentFrame.CARD_WHITE);

        add(scrollPane, BorderLayout.CENTER);
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText("View");
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setBackground(parentFrame.CARD_WHITE);
            setForeground(parentFrame.TEXT_DARK);
            setFocusPainted(false);
            setBorder(BorderFactory.createLineBorder(parentFrame.BORDER_LIGHT, 1));
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int clickedRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            clickedRow = row;
            button.setText("View");
            button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            button.setBackground(parentFrame.ACCENT_LIGHT_GREEN);
            button.setForeground(parentFrame.TEXT_DARK);
            button.setBorder(BorderFactory.createLineBorder(parentFrame.ACCENT_GREEN, 1));
            return button;
        }

        public Object getCellEditorValue() {
            int resultId = (int) tableModel.getValueAt(clickedRow, 0);
            int userId = (int) tableModel.getValueAt(clickedRow, 1);
            viewResultDetails(resultId, userId);
            return "View";
        }
    }

    private void viewResultDetails(int resultId, int userId) {
        try {
            String userType = parentFrame.getUserType();
            AssessmentResult result = null;

            if (userType.equals("patient")) {
                // Patient can only view their own results
                List<AssessmentResult> results = controller.getUserResults(parentFrame.getUserId());
                for (AssessmentResult r : results) {
                    if (r.getResultId() == resultId && r.getUserId() == parentFrame.getUserId()) {
                        result = r;
                        break;
                    }
                }
            } else {
                // Admin/Psychologist can view any result
                // First try to get from current view
                List<AssessmentResult> allResults = controller.getAllResults();
                for (AssessmentResult r : allResults) {
                    if (r.getResultId() == resultId && r.getUserId() == userId) {
                        result = r;
                        break;
                    }
                }
            }

            if (result != null) {
                showResultDialog(result);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Result not found or you don't have permission to view this result!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showResultDialog(AssessmentResult result) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String details = "<html><div style='width: 450px; padding: 25px; font-family: Segoe UI;'>" +
                "<h2 style='color: rgb(108,158,131); margin-bottom: 15px;'>Result Details</h2>" +
                "<hr style='border: 1px solid rgb(220,220,220);'>" +
                "<table style='width: 100%; margin-top: 20px;'>" +
                "<tr><td style='padding: 8px 0;'><b>Result ID:</b></td><td>" + result.getResultId() + "</td></tr>" +
                "<tr><td style='padding: 8px 0;'><b>User ID:</b></td><td>" + result.getUserId() + "</td></tr>" +
                "<tr><td style='padding: 8px 0;'><b>Assessment ID:</b></td><td>" + result.getAssessmentId() + "</td></tr>" +
                "<tr><td style='padding: 8px 0;'><b>Total Score:</b></td><td>" + result.getTotalScore() + "</td></tr>" +
                "<tr><td style='padding: 8px 0;'><b>Risk Level:</b></td><td>" + result.getRiskLevel() + "</td></tr>" +
                "<tr><td style='padding: 8px 0;'><b>Date Taken:</b></td><td>" + sdf.format(result.getTakenAt()) + "</td></tr>" +
                "</table>" +
                "<hr style='border: 1px solid rgb(220,220,220); margin: 20px 0;'>" +
                "<p style='margin: 15px 0;'><b>Interpretation:</b><br>" + result.getInterpretation() + "</p>" +
                "<hr style='border: 1px solid rgb(220,220,220); margin: 20px 0;'>" +
                "<p style='margin: 15px 0;'><b>Recommendations:</b><br>" + result.getRecommendedContent().replace("\n", "<br>") + "</p>" +
                "<hr style='border: 1px solid rgb(220,220,220); margin: 20px 0;'>" +
                "<p style='margin: 15px 0;'><b>Session Suggested:</b> " + (result.isSuggestSession() ? "Yes" : "No") + "</p>" +
                "</div></html>";

        JOptionPane.showMessageDialog(this,
                details,
                "Result Details",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void refreshData() {
        clearTable();

        String userType = parentFrame.getUserType();
        int loggedInUserId = parentFrame.getUserId();

        // Update UI based on user type
        setupUIForUserType(userType);

        try {
            List<AssessmentResult> results = null;

            if (userType.equals("patient")) {
                // Patient: always see only their own results
                results = controller.getUserResults(loggedInUserId);
                showNotification("Showing your results (" + (results != null ? results.size() : 0) + " found)");

            } else if (userType.equals("admin") || userType.equals("psychologist")) {
                // Admin/Psychologist: based on view mode
                String viewMode = (String) viewModeComboBox.getSelectedItem();

                if (viewMode.equals("My Results")) {
                    results = controller.getUserResults(loggedInUserId);
                    showNotification("Showing your results (" + (results != null ? results.size() : 0) + " found)");

                } else if (viewMode.equals("All Results")) {
                    results = controller.getAllResults();
                    showNotification("Showing all results (" + (results != null ? results.size() : 0) + " found)");

                } else if (viewMode.equals("Filter by User ID")) {
                    String userIdText = userIdField.getText().trim();
                    if (!userIdText.isEmpty()) {
                        try {
                            int userId = Integer.parseInt(userIdText);
                            results = controller.getUserResults(userId);
                            showNotification("Showing results for User ID: " + userId + " (" + (results != null ? results.size() : 0) + " found)");
                        } catch (NumberFormatException e) {
                            JOptionPane.showMessageDialog(this,
                                    "Please enter a valid User ID number!",
                                    "Invalid Input",
                                    JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    } else {
                        results = controller.getAllResults();
                        showNotification("Showing all results (" + (results != null ? results.size() : 0) + " found)");
                    }
                }
            }

            if (results != null && !results.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                for (AssessmentResult result : results) {
                    tableModel.addRow(new Object[]{
                            result.getResultId(),
                            result.getUserId(),
                            result.getAssessmentId(),
                            result.getTotalScore(),
                            result.getRiskLevel(),
                            sdf.format(result.getTakenAt()),
                            result.isSuggestSession() ? "Yes" : "No",
                            "View"
                    });
                }
            } else {
                // No results message handled by showNotification
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading results: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupUIForUserType(String userType) {
        if (userType.equals("patient")) {
            // Patients: simple view, no controls
            viewModeComboBox.setVisible(false);
            userIdField.setVisible(false);
            userTypeLabel.setText("Patient - Your Results");

        } else if (userType.equals("admin") || userType.equals("psychologist")) {
            // Admin/Psychologist: advanced controls
            viewModeComboBox.setVisible(true);
            userTypeLabel.setText(userType.toUpperCase());

            // Show/hide user ID field based on selection
            if (viewModeComboBox.getSelectedItem().equals("Filter by User ID")) {
                userIdField.setVisible(true);
            } else {
                userIdField.setVisible(false);
            }
        }
    }

    private void showNotification(String message) {
        // Show in status bar or small notification
        System.out.println("ResultsPanel: " + message);
        // Optional: You could add a status bar at the bottom of the panel
    }

    private void clearTable() {
        tableModel.setRowCount(0);
    }

    public void setUserId(int userId) {
        // This is called when panel is shown from patient dashboard
        String userType = parentFrame.getUserType();
        if (userType.equals("patient")) {
            // Auto-refresh for patient
            refreshData();
        } else {
            // For admin/psychologist, set the user ID field
            userIdField.setText(String.valueOf(userId));
            viewModeComboBox.setSelectedItem("Filter by User ID");
            refreshData();
        }
    }
}