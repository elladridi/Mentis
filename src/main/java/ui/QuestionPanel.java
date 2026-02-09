package ui;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import controller.QuestionController;
import controller.AssessmentController;
import models.Question;
import models.Assessment;
import java.sql.SQLException;
import java.util.List;

public class QuestionPanel extends JPanel {
    private MentisLoginFrame parentFrame;
    private QuestionController questionController;
    private AssessmentController assessmentController;
    private JTable questionTable;
    private DefaultTableModel tableModel;
    private List<Assessment> assessments;
    private List<Question> questions;
    private int currentAssessmentId = -1;

    // Track if data has been loaded
    private boolean dataLoaded = false;

    public QuestionPanel(MentisLoginFrame parentFrame, QuestionController questionController,
                         AssessmentController assessmentController) {
        this.parentFrame = parentFrame;
        this.questionController = questionController;
        this.assessmentController = assessmentController;
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

        // Top right - CANCEL button
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topRight.setBackground(parentFrame.BACKGROUND_LIGHT);

        JButton cancelButton = new JButton("CANCEL");
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelButton.setBackground(parentFrame.BUTTON_LIGHT_GREEN);
        cancelButton.setForeground(parentFrame.TEXT_DARK);
        cancelButton.setBorder(BorderFactory.createEmptyBorder(12, 40, 12, 40));
        cancelButton.setFocusPainted(false);
        cancelButton.setOpaque(true);
        cancelButton.setBorderPainted(false);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
                                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                            parentFrame.showPanel("ASSESSMENTS");
                                        }
                                    });
        cancelButton.addActionListener(e -> parentFrame.showPanel("ASSESSMENTS"));

        topRight.add(cancelButton);
        headerPanel.add(topRight, BorderLayout.NORTH);

        // Title section
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(parentFrame.BACKGROUND_LIGHT);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JPanel titleLeft = new JPanel(new GridBagLayout());
        titleLeft.setBackground(parentFrame.BACKGROUND_LIGHT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 0);

        JLabel titleLabel = new JLabel("Manage questions for Assessment");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(parentFrame.ACCENT_DARK_GREEN);
        titleLeft.add(titleLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);
        JLabel assessmentTitle = new JLabel("All Assessments");
        assessmentTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        assessmentTitle.setForeground(parentFrame.TEXT_DARK);
        titleLeft.add(assessmentTitle, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(5, 0, 0, 0);
        JLabel assessmentType = new JLabel("Viewing all questions");
        assessmentType.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        assessmentType.setForeground(parentFrame.TEXT_LIGHT);
        titleLeft.add(assessmentType, gbc);

        titlePanel.add(titleLeft, BorderLayout.WEST);

        // ADD button
        JButton addButton = new JButton("ADD");
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addButton.setBackground(parentFrame.BUTTON_LIGHT_GREEN);
        addButton.setForeground(parentFrame.TEXT_DARK);
        addButton.setBorder(BorderFactory.createEmptyBorder(12, 40, 12, 40));
        addButton.setFocusPainted(false);
        addButton.setOpaque(true);
        addButton.setBorderPainted(false);
        addButton.addActionListener(e -> showAddQuestionDialog());

        titlePanel.add(addButton, BorderLayout.EAST);

        headerPanel.add(titlePanel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);
    }

    private void createTable() {
        String[] columns = {"Question", "Scale", "Assessment", "Type", "Edit", "Delete"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4 || column == 5; // Only Edit and Delete columns are editable
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return String.class;
            }
        };

        questionTable = new JTable(tableModel) {
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

        questionTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        questionTable.setRowHeight(70);
        questionTable.setBackground(parentFrame.CARD_WHITE);
        questionTable.setGridColor(parentFrame.BORDER_LIGHT);
        questionTable.setShowGrid(true);
        questionTable.setIntercellSpacing(new Dimension(1, 1));
        questionTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        questionTable.getTableHeader().setBackground(parentFrame.CARD_WHITE);
        questionTable.getTableHeader().setForeground(parentFrame.TEXT_DARK);
        questionTable.getTableHeader().setReorderingAllowed(false);
        questionTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, parentFrame.BORDER_LIGHT));

        // Column widths
        questionTable.getColumnModel().getColumn(0).setPreferredWidth(300); // Question
        questionTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Scale
        questionTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Assessment
        questionTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Type
        questionTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Edit
        questionTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Delete

        // Button renderers
        questionTable.getColumnModel().getColumn(4).setCellRenderer(new EditButtonRenderer());
        questionTable.getColumnModel().getColumn(4).setCellEditor(new EditButtonEditor(new JCheckBox()));
        questionTable.getColumnModel().getColumn(5).setCellRenderer(new DeleteButtonRenderer());
        questionTable.getColumnModel().getColumn(5).setCellEditor(new DeleteButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(questionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(parentFrame.BORDER_LIGHT));
        scrollPane.getViewport().setBackground(parentFrame.CARD_WHITE);

        add(scrollPane, BorderLayout.CENTER);
    }

    class EditButtonRenderer extends JButton implements TableCellRenderer {
        public EditButtonRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText("Edit");
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setBackground(parentFrame.CARD_WHITE);
            setForeground(parentFrame.TEXT_DARK);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
            return this;
        }
    }

    class EditButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int clickedRow;

        public EditButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            clickedRow = row;
            button.setText("Edit");
            button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            button.setBackground(parentFrame.CARD_WHITE);
            button.setForeground(parentFrame.TEXT_DARK);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
            return button;
        }

        public Object getCellEditorValue() {
            if (dataLoaded && questions != null && clickedRow < questions.size()) {
                showEditQuestionDialog(clickedRow);
            }
            return "Edit";
        }
    }

    class DeleteButtonRenderer extends JButton implements TableCellRenderer {
        public DeleteButtonRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText("Delete");
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setBackground(parentFrame.CARD_WHITE);
            setForeground(parentFrame.TEXT_DARK);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
            return this;
        }
    }

    class DeleteButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int clickedRow;

        public DeleteButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            clickedRow = row;
            button.setText("Delete");
            button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            button.setBackground(parentFrame.CARD_WHITE);
            button.setForeground(parentFrame.TEXT_DARK);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
            return button;
        }

        public Object getCellEditorValue() {
            if (dataLoaded && questions != null && clickedRow < questions.size()) {
                showDeleteQuestionDialog(clickedRow);
            }
            return "Delete";
        }
    }


    private void showDeleteQuestionDialog(int row) {
        if (questions == null || row >= questions.size()) {
            return;
        }

        Question question = questions.get(row);
        int confirm = JOptionPane.showConfirmDialog(parentFrame,
                "Are you sure you want to delete this question?\n\n" +
                        question.getText(),
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                questionController.deleteQuestion(question.getQuestionId());
                JOptionPane.showMessageDialog(parentFrame,
                        "Question deleted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(parentFrame,
                        "Error deleting question: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAddQuestionDialog() {
        new QuestionFormDialog(parentFrame, questionController, assessmentController,
                null, currentAssessmentId, false);
    }

    private void showEditQuestionDialog(int row) {
        if (questions == null || row >= questions.size()) {
            return;
        }

        Question question = questions.get(row);
        new QuestionFormDialog(parentFrame, questionController, assessmentController,
                question, question.getAssessmentId(), true);
    }

    public void refreshData() {
        // Clear existing data
        tableModel.setRowCount(0);
        dataLoaded = false;

        try {
            // Load assessments and questions
            assessments = assessmentController.getAllAssessments();

            // Load questions based on currentAssessmentId
            if (currentAssessmentId > 0) {
                // Load questions only for the specific assessment
                questions = questionController.getQuestionsByAssessment(currentAssessmentId);
            } else {
                // Load all questions
                questions = questionController.getAllQuestions();
            }

            if (questions == null || questions.isEmpty()) {
                String message = currentAssessmentId > 0 ?
                        "No questions available for this assessment" :
                        "No questions available";

                tableModel.addRow(new Object[]{
                        message,
                        "",
                        "Click ADD to create one",
                        "",
                        "",
                        ""
                });
                return;
            }

            for (Question question : questions) {
                // Find assessment name
                String assessmentName = "Unknown";
                String assessmentType = "";
                if (assessments != null) {
                    for (Assessment assessment : assessments) {
                        if (assessment.getAssessmentId() == question.getAssessmentId()) {
                            assessmentName = assessment.getTitle();
                            assessmentType = assessment.getType();
                            break;
                        }
                    }
                }

                // Truncate question text if too long
                String questionText = question.getText();
                if (questionText.length() > 80) {
                    questionText = questionText.substring(0, 77) + "...";
                }

                tableModel.addRow(new Object[]{
                        questionText,
                        question.getScale(),
                        assessmentName,
                        assessmentType,
                        "Edit",
                        "Delete"
                });
            }

            dataLoaded = true;

        } catch (SQLException e) {
            tableModel.addRow(new Object[]{
                    "Database Error",
                    e.getMessage(),
                    "Please check connection",
                    "",
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
                    "",
                    ""
            });
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void setCurrentAssessmentId(int assessmentId) {
        this.currentAssessmentId = assessmentId;
        refreshData();
    }
}