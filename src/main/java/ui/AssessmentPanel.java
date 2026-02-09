package ui;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import controller.AssessmentController;
import models.Assessment;
import java.sql.SQLException;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class AssessmentPanel extends JPanel {
    private MentisLoginFrame parentFrame;
    private AssessmentController controller;
    private JTable assessmentTable;
    private DefaultTableModel tableModel;
    private List<Assessment> assessments;

    // Track if data has been loaded
    private boolean dataLoaded = false;

    public AssessmentPanel(MentisLoginFrame parentFrame, AssessmentController controller) {
        this.parentFrame = parentFrame;
        this.controller = controller;
        setLayout(new BorderLayout());
        setBackground(parentFrame.BACKGROUND_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        createHeader();
        createTable();
        // Don't refresh data in constructor
    }

    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(parentFrame.BACKGROUND_LIGHT);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        // Tabs
        JPanel tabsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 30, 0));
        tabsPanel.setBackground(parentFrame.BACKGROUND_LIGHT);

        JLabel assessmentTab = new JLabel("Assessment");
        assessmentTab.setFont(new Font("Segoe UI", Font.BOLD, 18));
        assessmentTab.setForeground(parentFrame.ACCENT_DARK_GREEN);
        assessmentTab.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, parentFrame.ACCENT_DARK_GREEN));


        JLabel resultsTab = new JLabel("Results");
        resultsTab.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        resultsTab.setForeground(parentFrame.TEXT_LIGHT);
        resultsTab.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resultsTab.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                parentFrame.showResultsPanel(); // FIXED: Use showResultsPanel()
            }
        });

        tabsPanel.add(assessmentTab);
        tabsPanel.add(resultsTab);
        headerPanel.add(tabsPanel, BorderLayout.EAST);

        // Title and ADD button
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(parentFrame.BACKGROUND_LIGHT);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));

        JLabel titleLabel = new JLabel("Manage assessments");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(parentFrame.ACCENT_DARK_GREEN);

        JButton addButton = new JButton("ADD");
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addButton.setBackground(parentFrame.BUTTON_LIGHT_GREEN);
        addButton.setForeground(parentFrame.TEXT_DARK);
        addButton.setBorder(BorderFactory.createEmptyBorder(12, 40, 12, 40));
        addButton.setFocusPainted(false);
        addButton.setOpaque(true);
        addButton.setBorderPainted(false);
        addButton.addActionListener(e -> showAddAssessmentDialog());

        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(addButton, BorderLayout.EAST);

        JPanel combinedHeader = new JPanel(new BorderLayout());
        combinedHeader.setBackground(parentFrame.BACKGROUND_LIGHT);
        combinedHeader.add(headerPanel, BorderLayout.NORTH);
        combinedHeader.add(titlePanel, BorderLayout.CENTER);

        add(combinedHeader, BorderLayout.NORTH);
    }

    private void createTable() {
        String[] columns = {"Title", "Type", "Status", "Description", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only Actions column is editable
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return String.class;
            }
        };

        assessmentTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);

                if (!isRowSelected(row)) {
                    c.setBackground(parentFrame.CARD_WHITE);
                }

                c.setForeground(parentFrame.TEXT_DARK);

                return c;
            }
        };

        assessmentTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        assessmentTable.setRowHeight(70);
        assessmentTable.setBackground(parentFrame.CARD_WHITE);
        assessmentTable.setGridColor(parentFrame.BORDER_LIGHT);
        assessmentTable.setShowGrid(true);
        assessmentTable.setIntercellSpacing(new Dimension(1, 1));
        assessmentTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        assessmentTable.getTableHeader().setBackground(parentFrame.CARD_WHITE);
        assessmentTable.getTableHeader().setForeground(parentFrame.TEXT_DARK);
        assessmentTable.getTableHeader().setReorderingAllowed(false);
        assessmentTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, parentFrame.BORDER_LIGHT));

        // Column widths
        int colWidth = 200;
        assessmentTable.getColumnModel().getColumn(0).setPreferredWidth(colWidth);
        assessmentTable.getColumnModel().getColumn(1).setPreferredWidth(colWidth);
        assessmentTable.getColumnModel().getColumn(2).setPreferredWidth(colWidth);
        assessmentTable.getColumnModel().getColumn(3).setPreferredWidth(colWidth);
        assessmentTable.getColumnModel().getColumn(4).setPreferredWidth(150);

        // Manage button renderer and editor
        assessmentTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        assessmentTable.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(assessmentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(parentFrame.BORDER_LIGHT));
        scrollPane.getViewport().setBackground(parentFrame.CARD_WHITE);

        add(scrollPane, BorderLayout.CENTER);
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText("MANAGE");
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setBackground(parentFrame.CARD_WHITE);
            setForeground(parentFrame.TEXT_DARK);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
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
            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            clickedRow = row;
            button.setText("MANAGE");
            button.setFont(new Font("Segoe UI", Font.BOLD, 13));
            button.setBackground(parentFrame.CARD_WHITE);
            button.setForeground(parentFrame.TEXT_DARK);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
            return button;
        }

        public Object getCellEditorValue() {
            if (dataLoaded && assessments != null && clickedRow < assessments.size()) {
                showManageDialog(clickedRow);
            }
            return "MANAGE";
        }
    }

    private void showManageDialog(int row) {
        if (assessments == null || row >= assessments.size()) {
            return;
        }

        Assessment assessment = assessments.get(row);

        // Create dialog matching Page 3 design
        JDialog dialog = new JDialog(parentFrame, "Manage Assessment", true);
        dialog.setSize(900, 600);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(parentFrame.BACKGROUND_LIGHT);

        // Left side - info
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(parentFrame.BACKGROUND_LIGHT);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 30, 0);

        JLabel headerLabel = new JLabel("Manage Assessment");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        headerLabel.setForeground(parentFrame.ACCENT_DARK_GREEN);
        leftPanel.add(headerLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        JLabel titleDisplay = new JLabel(assessment.getTitle());
        titleDisplay.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleDisplay.setForeground(parentFrame.TEXT_DARK);
        leftPanel.add(titleDisplay, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 20, 0);
        JLabel typeDisplay = new JLabel(assessment.getType());
        typeDisplay.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        typeDisplay.setForeground(parentFrame.TEXT_LIGHT);
        leftPanel.add(typeDisplay, gbc);

        // Add description if available
        if (assessment.getDescription() != null && !assessment.getDescription().isEmpty()) {
            gbc.gridy++;
            gbc.insets = new Insets(0, 0, 30, 0);
            JLabel descDisplay = new JLabel("<html><div style='width: 350px;'>" +
                    assessment.getDescription() + "</div></html>");
            descDisplay.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            descDisplay.setForeground(parentFrame.TEXT_DARK);
            leftPanel.add(descDisplay, gbc);
        }

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 5, 0);
        JLabel statusLabel = new JLabel("Status: " + assessment.getStatus());
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusLabel.setForeground(getStatusColor(assessment.getStatus()));
        leftPanel.add(statusLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(30, 0, 5, 0);
        JLabel questionLabel = new JLabel("What would you like to do?");
        questionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        questionLabel.setForeground(parentFrame.TEXT_DARK);
        leftPanel.add(questionLabel, gbc);

        // Right side - image
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(parentFrame.BACKGROUND_LIGHT);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 40));

        JLabel imageLabel = new JLabel("", SwingConstants.CENTER);
        imageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        imageLabel.setForeground(parentFrame.TEXT_LIGHT);
        imageLabel.setBackground(new Color(230, 240, 235));
        imageLabel.setOpaque(true);
        imageLabel.setPreferredSize(new Dimension(400, 500));
        imageLabel.setBorder(BorderFactory.createLineBorder(parentFrame.BORDER_LIGHT, 1, true));

        // Try to load assessment image
        if (assessment.getImagePath() != null && !assessment.getImagePath().isEmpty()) {
            try {
                File imgFile = new File(assessment.getImagePath());
                if (imgFile.exists()) {
                    // Load and resize image for display
                    BufferedImage originalImage = ImageIO.read(imgFile);
                    if (originalImage != null) {
                        // Calculate scaling to fit 400x500 while maintaining aspect ratio
                        int maxWidth = 380; // Account for border
                        int maxHeight = 480; // Account for border

                        int originalWidth = originalImage.getWidth();
                        int originalHeight = originalImage.getHeight();

                        double widthRatio = (double) maxWidth / originalWidth;
                        double heightRatio = (double) maxHeight / originalHeight;
                        double scale = Math.min(widthRatio, heightRatio);

                        int scaledWidth = (int) (originalWidth * scale);
                        int scaledHeight = (int) (originalHeight * scale);

                        Image scaledImage = originalImage.getScaledInstance(
                                scaledWidth, scaledHeight, Image.SCALE_SMOOTH);

                        ImageIcon icon = new ImageIcon(scaledImage);
                        imageLabel.setIcon(icon);
                        imageLabel.setText(""); // Clear placeholder text

                        // Center the image in the label
                        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
                    }
                }
            } catch (Exception e) {
                // Image loading failed, show placeholder
                imageLabel.setText("Image Not Available");
                System.err.println("Error loading image: " + e.getMessage());
            }
        } else {
            // No image available
            imageLabel.setText("No Image Available");
            imageLabel.setBackground(getTypeBackgroundColor(assessment.getType()));
            imageLabel.setForeground(getTypeColor(assessment.getType()));
        }

        rightPanel.add(imageLabel);

        // Buttons at bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 20));
        buttonPanel.setBackground(parentFrame.BACKGROUND_LIGHT);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 40, 40, 40));

        String[] buttonLabels = {"EDIT", "DELETE", "MANAGE\nQUESTIONS", "ACTIVATE", "CANCEL"};
        for (String label : buttonLabels) {
            JButton btn = new JButton(label.replace("\n", " "));
            btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btn.setBackground(parentFrame.BUTTON_LIGHT_GREEN);
            btn.setForeground(parentFrame.TEXT_DARK);
            btn.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
            btn.setFocusPainted(false);
            btn.setOpaque(true);
            btn.setBorderPainted(false);

            if (label.equals("CANCEL")) {
                btn.addActionListener(e -> dialog.dispose());
            } else if (label.contains("QUESTIONS")) {
                btn.addActionListener(e -> {
                    dialog.dispose();
                    parentFrame.showQuestionPanelWithAssessment(assessment.getAssessmentId());
                });
            } else if (label.equals("EDIT")) {
                btn.addActionListener(e -> {
                    dialog.dispose();
                    showEditAssessmentDialog(assessment);
                });
            } else if (label.equals("DELETE")) {
                btn.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(dialog,
                            "Are you sure you want to delete this assessment?\n" +
                                    "This will also delete all associated questions.",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        try {
                            controller.deleteAssessment(assessment.getAssessmentId());
                            JOptionPane.showMessageDialog(dialog,
                                    "Assessment deleted successfully!",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE);
                            dialog.dispose();
                            refreshData();
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(dialog,
                                    "Error deleting assessment: " + ex.getMessage(),
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
            } else if (label.equals("ACTIVATE")) {
                btn.addActionListener(e -> {
                    try {
                        String newStatus = "Active".equals(assessment.getStatus()) ? "Inactive" : "Active";
                        if (controller.updateAssessmentStatus(assessment.getAssessmentId(), newStatus)) {
                            JOptionPane.showMessageDialog(dialog,
                                    "Assessment status updated to: " + newStatus,
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE);
                            dialog.dispose();
                            refreshData();
                        }
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(dialog,
                                "Error updating status: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            }

            buttonPanel.add(btn);
        }

        // Layout
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(parentFrame.BACKGROUND_LIGHT);
        contentPanel.add(leftPanel, BorderLayout.WEST);
        contentPanel.add(rightPanel, BorderLayout.CENTER);

        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // Add these helper methods to the AssessmentPanel class
    private Color getStatusColor(String status) {
        if (status == null) return parentFrame.TEXT_GRAY;

        switch (status.toLowerCase()) {
            case "active":
                return new Color(39, 174, 96); // Green
            case "inactive":
                return new Color(192, 57, 43); // Red
            case "draft":
                return new Color(230, 126, 34); // Orange
            default:
                return parentFrame.TEXT_GRAY;
        }
    }

    private Color getTypeColor(String type) {
        if (type == null) return new Color(80, 100, 120);

        switch (type.toLowerCase()) {
            case "depression":
                return new Color(91, 44, 111); // Deep purple
            case "anxiety":
                return new Color(192, 57, 43); // Deep red
            case "stress":
                return new Color(230, 126, 34); // Deep orange
            case "wellness":
                return new Color(39, 174, 96); // Deep green
            case "general":
                return new Color(52, 152, 219); // Deep blue
            default:
                return new Color(80, 100, 120);
        }
    }

    private Color getTypeBackgroundColor(String type) {
        if (type == null) return new Color(240, 240, 240);

        switch (type.toLowerCase()) {
            case "depression":
                return new Color(245, 235, 255); // Light purple
            case "anxiety":
                return new Color(255, 235, 235); // Light red
            case "stress":
                return new Color(255, 245, 215); // Light orange
            case "wellness":
                return new Color(235, 255, 240); // Light green
            case "general":
                return new Color(235, 245, 255); // Light blue
            default:
                return new Color(240, 240, 240);
        }
    }


    private void showAddAssessmentDialog() {
        new AssessmentFormDialog(parentFrame, controller, null, false);
    }

    private void showEditAssessmentDialog(Assessment assessment) {
        new AssessmentFormDialog(parentFrame, controller, assessment, true);
    }


    public void refreshData() {
        // Clear existing data
        tableModel.setRowCount(0);
        dataLoaded = false;

        try {
            // Check if controller is connected
            assessments = controller.getAllAssessments();

            if (assessments == null || assessments.isEmpty()) {
                // Show empty message
                tableModel.addRow(new Object[]{
                        "No assessments available",
                        "",
                        "Click ADD to create one",
                        "",
                        ""
                });
                return;
            }

            for (Assessment assessment : assessments) {
                tableModel.addRow(new Object[]{
                        assessment.getTitle(),
                        assessment.getType(),
                        assessment.getStatus(),
                        assessment.getDescription().length() > 50 ?
                                assessment.getDescription().substring(0, 47) + "..." :
                                assessment.getDescription(),
                        "MANAGE"
                });
            }

            dataLoaded = true;

        } catch (SQLException e) {
            // Show error in table
            tableModel.addRow(new Object[]{
                    "Database Error",
                    e.getMessage(),
                    "Please check connection",
                    "",
                    ""
            });
            System.err.println("Database error: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Cannot connect to database.\n" +
                            "Error: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            tableModel.addRow(new Object[]{
                    "Error loading data",
                    e.getMessage(),
                    "",
                    "",
                    ""
            });
            System.err.println("Error: " + e.getMessage());
        }
    }
}