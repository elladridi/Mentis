package ui;

import controller.AssessmentController;
import controller.QuestionController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;
import models.Assessment;
import models.Question;

import java.sql.SQLException;
import java.util.List;

public class QuestionFormDialog extends Stage {

    private MentisLoginFrame parentApp;
    private QuestionController questionController;
    private AssessmentController assessmentController;
    private Question question; // null for add, not null for edit
    private int assessmentId; // for adding questions to specific assessment

    private ComboBox<Assessment> assessmentCombo;
    private TextArea questionTextArea;
    private TextField scaleField;
    private Label typeDisplayLabel;

    // Color constants
    private static final Color BACKGROUND_LIGHT = Color.rgb(240, 248, 245);
    private static final Color ACCENT_DARK_GREEN = Color.rgb(60, 120, 90);
    private static final Color BUTTON_LIGHT_GREEN = Color.rgb(160, 200, 180);
    private static final Color BORDER_LIGHT = Color.rgb(200, 220, 210);
    private static final Color TEXT_DARK = Color.rgb(40, 70, 50);
    private static final Color TEXT_LIGHT = Color.rgb(100, 130, 110);
    private static final Color ERROR_RED = Color.rgb(200, 80, 80);

    public QuestionFormDialog(MentisLoginFrame parentApp, QuestionController questionController,
                              AssessmentController assessmentController, Question question,
                              Integer specificAssessmentId, boolean isEdit) {
        this.parentApp = parentApp;
        this.questionController = questionController;
        this.assessmentController = assessmentController;
        this.question = question;
        this.assessmentId = specificAssessmentId != null ? specificAssessmentId : -1;

        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        setTitle(isEdit ? "Edit Question" : "Add Question");

        // Main layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

        // Header
        root.setTop(createHeader(isEdit));

        // Form
        root.setCenter(createForm());

        // Buttons
        root.setBottom(createButtonPanel(isEdit));

        Scene scene = new Scene(root, 700, 550);
        setScene(scene);
        setResizable(false);

        if (isEdit && question != null) {
            loadQuestionData();
        }

        showAndWait();
    }

    private HBox createHeader(boolean isEdit) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 30, 20, 30));
        header.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

        Label titleLabel = new Label(isEdit ? "Edit Question" : "Add New Question");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        header.getChildren().add(titleLabel);
        return header;
    }

    private ScrollPane createForm() {
        GridPane formPanel = new GridPane();
        formPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        formPanel.setPadding(new Insets(20, 30, 20, 30));
        formPanel.setHgap(15);
        formPanel.setVgap(15);
        formPanel.setAlignment(Pos.TOP_CENTER);

        // Column constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(120);
        col1.setHalignment(javafx.geometry.HPos.RIGHT);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        col2.setFillWidth(true);

        formPanel.getColumnConstraints().addAll(col1, col2);

        int row = 0;

        // Assessment selection
        Label assessmentLabel = new Label("Assessment:");
        assessmentLabel.setFont(Font.font("Segoe UI", 14));
        assessmentLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        try {
            List<Assessment> assessments = assessmentController.getAllAssessments();
            ObservableList<Assessment> assessmentList = FXCollections.observableArrayList(assessments);

            assessmentCombo = new ComboBox<>(assessmentList);
            assessmentCombo.setPrefHeight(40);
            assessmentCombo.setMaxWidth(Double.MAX_VALUE);
            assessmentCombo.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                            "-fx-border-radius: 5;" +
                            "-fx-background-radius: 5;"
            );

// Style the button cell (what's displayed when nothing is selected/popup closed)
            assessmentCombo.setButtonCell(new ListCell<Assessment>() {
                @Override
                protected void updateItem(Assessment item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getTitle() + " (" + item.getType() + ")");
                        setFont(Font.font("Segoe UI", 14));
                    }
                }
            });

// Style the popup list items
            assessmentCombo.setCellFactory(lv -> new ListCell<Assessment>() {
                @Override
                protected void updateItem(Assessment item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getTitle() + " (" + item.getType() + ")");
                        setFont(Font.font("Segoe UI", 14));
                    }
                }
            });

            // Custom converter to display assessment title and type
            assessmentCombo.setConverter(new StringConverter<Assessment>() {
                @Override
                public String toString(Assessment assessment) {
                    return assessment == null ? "" :
                            assessment.getTitle() + " (" + assessment.getType() + ")";
                }

                @Override
                public Assessment fromString(String string) {
                    return assessmentCombo.getItems().stream()
                            .filter(a -> (a.getTitle() + " (" + a.getType() + ")").equals(string))
                            .findFirst()
                            .orElse(null);
                }
            });

            // If specific assessment ID provided, select it
            if (assessmentId != -1) {
                for (Assessment a : assessmentList) {
                    if (a.getAssessmentId() == assessmentId) {
                        assessmentCombo.setValue(a);
                        if (question != null) { // Editing existing question
                            assessmentCombo.setDisable(true); // Can't change assessment for existing question
                        }
                        break;
                    }
                }
            }

            formPanel.add(assessmentLabel, 0, row);
            formPanel.add(assessmentCombo, 1, row++);

        } catch (SQLException e) {
            Label errorLabel = new Label("Error loading assessments");
            errorLabel.setTextFill(Color.web(toHex(ERROR_RED)));
            formPanel.add(assessmentLabel, 0, row);
            formPanel.add(errorLabel, 1, row++);
        }

        // Question Text
        Label questionLabel = new Label("Question Text:");
        questionLabel.setFont(Font.font("Segoe UI", 14));
        questionLabel.setTextFill(Color.web(toHex(TEXT_DARK)));
        questionLabel.setAlignment(Pos.TOP_RIGHT);

        questionTextArea = new TextArea();
        questionTextArea.setFont(Font.font("Segoe UI", 14));
        questionTextArea.setWrapText(true);
        questionTextArea.setPrefRowCount(4);
        questionTextArea.setStyle(
                "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8;"
        );

        formPanel.add(questionLabel, 0, row);
        formPanel.add(questionTextArea, 1, row++);

        // Scale/Options
        Label scaleLabel = new Label("Scale/Options:");
        scaleLabel.setFont(Font.font("Segoe UI", 14));
        scaleLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        VBox scaleContainer = new VBox(5);
        scaleContainer.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

        scaleField = new TextField();
        scaleField.setFont(Font.font("Segoe UI", 14));
        scaleField.setPrefHeight(40);
        scaleField.setStyle(
                "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8 12;"
        );

        Label scaleHint = new Label("Examples: 1-5 or Never,Rarely,Sometimes,Often,Always or Yes,No");
        scaleHint.setFont(Font.font("Segoe UI", 12));
        scaleHint.setTextFill(Color.web(toHex(TEXT_LIGHT)));

        scaleContainer.getChildren().addAll(scaleField, scaleHint);

        formPanel.add(scaleLabel, 0, row);
        formPanel.add(scaleContainer, 1, row++);

        // Assessment Type Display
        Label typeLabel = new Label("Assessment Type:");
        typeLabel.setFont(Font.font("Segoe UI", 14));
        typeLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        typeDisplayLabel = new Label();
        typeDisplayLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        typeDisplayLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        // Update type display when assessment selection changes
        if (assessmentCombo != null) {
            assessmentCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    typeDisplayLabel.setText(newVal.getType());
                }
            });

            // Set initial value
            if (assessmentCombo.getValue() != null) {
                typeDisplayLabel.setText(assessmentCombo.getValue().getType());
            }
        }

        formPanel.add(typeLabel, 0, row);
        formPanel.add(typeDisplayLabel, 1, row++);

        // Wrap in ScrollPane
        ScrollPane scrollPane = new ScrollPane(formPanel);
        scrollPane.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        scrollPane.setBorder(null);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return scrollPane;
    }

    private HBox createButtonPanel(boolean isEdit) {
        HBox buttonPanel = new HBox(15);
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);
        buttonPanel.setPadding(new Insets(20, 30, 20, 30));
        buttonPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

        Button cancelButton = createButton("Cancel", BUTTON_LIGHT_GREEN);
        cancelButton.setOnAction(e -> close());

        Button saveButton = createButton(isEdit ? "Save Changes" : "Add Question", ACCENT_DARK_GREEN);
        saveButton.setTextFill(Color.WHITE);
        saveButton.setOnAction(e -> saveQuestion());

        buttonPanel.getChildren().addAll(cancelButton, saveButton);
        return buttonPanel;
    }

    private Button createButton(String text, Color bgColor) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        button.setTextFill(bgColor == ACCENT_DARK_GREEN ? Color.WHITE : Color.web(toHex(TEXT_DARK)));
        button.setStyle(
                "-fx-background-color: #" + toHex(bgColor) + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 12 30;" +
                        "-fx-cursor: hand;"
        );

        // Hover effect
        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(bgColor.darker()) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 12 30;" +
                                "-fx-cursor: hand;"
                )
        );
        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(bgColor) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 12 30;" +
                                "-fx-cursor: hand;"
                )
        );

        return button;
    }

    private void loadQuestionData() {
        if (question != null) {
            questionTextArea.setText(question.getText());
            scaleField.setText(question.getScale());
        }
    }

    private void saveQuestion() {
        // Validate inputs
        if (questionTextArea.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter the question text.", Alert.AlertType.WARNING);
            return;
        }

        if (scaleField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter the scale or options for the question.", Alert.AlertType.WARNING);
            return;
        }

        try {
            int selectedAssessmentId;
            if (assessmentCombo != null && assessmentCombo.getValue() != null) {
                selectedAssessmentId = assessmentCombo.getValue().getAssessmentId();
            } else if (question != null) {
                selectedAssessmentId = question.getAssessmentId();
            } else {
                showAlert("Validation Error", "Please select an assessment.", Alert.AlertType.WARNING);
                return;
            }

            if (question == null) {
                // Create new question
                Question newQuestion = new Question();
                newQuestion.setAssessmentId(selectedAssessmentId);
                newQuestion.setText(questionTextArea.getText().trim());
                newQuestion.setScale(scaleField.getText().trim());

                questionController.createQuestion(newQuestion);

                showAlert("Success", "Question created successfully!", Alert.AlertType.INFORMATION);

            } else {
                // Update existing question
                question.setAssessmentId(selectedAssessmentId);
                question.setText(questionTextArea.getText().trim());
                question.setScale(scaleField.getText().trim());

                questionController.updateQuestion(question);

                showAlert("Success", "Question updated successfully!", Alert.AlertType.INFORMATION);
            }

            close();

            // Refresh the questions panel
            if (parentApp != null) {
                // Navigate to questions panel and refresh
                parentApp.showQuestionPanel();
            }

        } catch (SQLException e) {
            showAlert("Database Error", "Error saving question: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Error", "Error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner(this);
        alert.showAndWait();
    }

    // ================= UTILITY =================
    private String toHex(Color color) {
        return String.format("%02x%02x%02x",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }
}