package ui;

import javax.swing.*;
import java.awt.*;
import controller.QuestionController;
import controller.AssessmentController;
import models.Question;
import models.Assessment;
import java.sql.SQLException;
import java.util.List;

public class QuestionFormDialog extends JDialog {
    private MentisLoginFrame parentFrame;
    private QuestionController questionController;
    private AssessmentController assessmentController;
    private Question question; // null for add, not null for edit
    private int assessmentId; // for adding questions to specific assessment

    private JComboBox<Assessment> assessmentCombo;
    private JTextArea questionTextArea;
    private JTextField scaleField;
    private JComboBox<String> questionTypeCombo;

    public QuestionFormDialog(MentisLoginFrame parentFrame, QuestionController questionController,
                              AssessmentController assessmentController, Question question,
                              Integer specificAssessmentId, boolean isEdit) {
        super(parentFrame, isEdit ? "Edit Question" : "Add Question", true);
        this.parentFrame = parentFrame;
        this.questionController = questionController;
        this.assessmentController = assessmentController;
        this.question = question;
        this.assessmentId = specificAssessmentId != null ? specificAssessmentId : -1;

        setSize(700, 550);
        setLocationRelativeTo(parentFrame);
        setLayout(new BorderLayout());
        getContentPane().setBackground(parentFrame.BACKGROUND_LIGHT);

        createForm();
        if (isEdit && question != null) {
            // Populate fields if editing
            if (question.getText() != null) {
                questionTextArea.setText(question.getText());
            }
            if (question.getScale() != null) {
                scaleField.setText(question.getScale());
            }
        }

        setVisible(true);
    }

    private void createForm() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(parentFrame.BACKGROUND_LIGHT);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel(question == null ? "Add New Question" : "Edit Question");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(parentFrame.ACCENT_DARK_GREEN);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(parentFrame.BACKGROUND_LIGHT);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Assessment selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Assessment:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        try {
            List<Assessment> assessments = assessmentController.getAllAssessments();
            Assessment[] assessmentArray = assessments.toArray(new Assessment[0]);
            assessmentCombo = new JComboBox<>(assessmentArray);
            assessmentCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value,
                                                              int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Assessment) {
                        Assessment a = (Assessment) value;
                        setText(a.getTitle() + " (" + a.getType() + ")");
                    }
                    return this;
                }
            });
            assessmentCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            // If specific assessment ID provided, select it
            if (assessmentId != -1) {
                for (int i = 0; i < assessmentCombo.getItemCount(); i++) {
                    if (assessmentCombo.getItemAt(i).getAssessmentId() == assessmentId) {
                        assessmentCombo.setSelectedIndex(i);
                        if (question != null) { // Editing existing question
                            assessmentCombo.setEnabled(false); // Can't change assessment for existing question
                        }
                        break;
                    }
                }
            }

            formPanel.add(assessmentCombo, gbc);
        } catch (SQLException e) {
            JLabel errorLabel = new JLabel("Error loading assessments");
            errorLabel.setForeground(Color.RED);
            formPanel.add(errorLabel, gbc);
        }

        // Question Text
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel questionLabel = new JLabel("Question Text:");
        questionLabel.setVerticalAlignment(SwingConstants.TOP);
        formPanel.add(questionLabel, gbc);

        gbc.gridx = 1;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        questionTextArea = new JTextArea(4, 30);
        questionTextArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        questionTextArea.setLineWrap(true);
        questionTextArea.setWrapStyleWord(true);
        JScrollPane questionScroll = new JScrollPane(questionTextArea);
        questionScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(parentFrame.BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        formPanel.add(questionScroll, gbc);

        // Scale/Options
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0;
        formPanel.add(new JLabel("Scale/Options:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        scaleField = new JTextField(30);
        scaleField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        scaleField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(parentFrame.BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        JLabel scaleHint = new JLabel("<html><small>Examples: 1-5 or Never,Rarely,Sometimes,Often,Always or Yes,No</small></html>");
        scaleHint.setForeground(parentFrame.TEXT_LIGHT);

        JPanel scalePanel = new JPanel(new BorderLayout(0, 5));
        scalePanel.setBackground(parentFrame.BACKGROUND_LIGHT);
        scalePanel.add(scaleField, BorderLayout.NORTH);
        scalePanel.add(scaleHint, BorderLayout.SOUTH);

        formPanel.add(scalePanel, gbc);

        // Display selected assessment type (read-only)
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Assessment Type:"), gbc);

        gbc.gridx = 1;
        JLabel typeDisplayLabel = new JLabel();
        typeDisplayLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        typeDisplayLabel.setForeground(parentFrame.ACCENT_DARK_GREEN);

        // Update type display when assessment selection changes
        if (assessmentCombo != null) {
            assessmentCombo.addActionListener(e -> {
                Assessment selected = (Assessment) assessmentCombo.getSelectedItem();
                if (selected != null) {
                    typeDisplayLabel.setText(selected.getType());
                }
            });

            // Set initial value
            if (assessmentCombo.getSelectedItem() != null) {
                typeDisplayLabel.setText(((Assessment) assessmentCombo.getSelectedItem()).getType());
            }
        }

        formPanel.add(typeDisplayLabel, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        buttonPanel.setBackground(parentFrame.BACKGROUND_LIGHT);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 20, 30));

        JButton cancelButton = new JButton("Cancel");
        styleButton(cancelButton, parentFrame.BUTTON_LIGHT_GREEN);
        cancelButton.addActionListener(e -> dispose());

        JButton saveButton = new JButton(question == null ? "Add Question" : "Save Changes");
        styleButton(saveButton, parentFrame.ACCENT_DARK_GREEN);
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> saveQuestion());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(bgColor == parentFrame.ACCENT_DARK_GREEN ? Color.WHITE : parentFrame.TEXT_DARK);
        button.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
    }

    private void saveQuestion() {
        // Validate inputs
        if (questionTextArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter the question text.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (scaleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter the scale or options for the question.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int selectedAssessmentId;
            if (assessmentCombo != null && assessmentCombo.getSelectedItem() != null) {
                selectedAssessmentId = ((Assessment) assessmentCombo.getSelectedItem()).getAssessmentId();
            } else if (question != null) {
                selectedAssessmentId = question.getAssessmentId();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Please select an assessment.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (question == null) {
                // Create new question
                Question newQuestion = new Question();
                newQuestion.setAssessmentId(selectedAssessmentId);
                newQuestion.setText(questionTextArea.getText().trim());
                newQuestion.setScale(scaleField.getText().trim());

                questionController.createQuestion(newQuestion);

                JOptionPane.showMessageDialog(this,
                        "Question created successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

            } else {
                // Update existing question
                question.setAssessmentId(selectedAssessmentId);
                question.setText(questionTextArea.getText().trim());
                question.setScale(scaleField.getText().trim());

                questionController.updateQuestion(question);

                JOptionPane.showMessageDialog(this,
                        "Question updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            dispose();
            parentFrame.showPanel("QUESTIONS"); // Refresh the questions panel

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving question: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}