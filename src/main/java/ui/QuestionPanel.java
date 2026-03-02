package ui;

import controller.AssessmentController;
import controller.QuestionController;
import ui.AIQuestionGeneratorDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import models.Assessment;
import models.Question;

import java.sql.SQLException;
import java.util.List;

public class QuestionPanel extends VBox {

    private MentisLoginFrame parentApp;  // FIXED: Changed from MentisLoginFrame to MentisLoginFrame
    private QuestionController questionController;
    private AssessmentController assessmentController;
    private TableView<QuestionModel> questionTable;
    private ObservableList<QuestionModel> questionData;
    private List<Assessment> assessments;
    private int currentAssessmentId = -1;

    // UI Components that need to be updated
    private Label assessmentTitleLabel;
    private Label assessmentTypeLabel;
    private Label assessmentNameLabel;

    // Color constants
    private static final Color BACKGROUND_LIGHT = Color.rgb(240, 248, 245);
    private static final Color CARD_WHITE = Color.WHITE;
    private static final Color ACCENT_DARK_GREEN = Color.rgb(60, 120, 90);
    private static final Color BUTTON_LIGHT_GREEN = Color.rgb(160, 200, 180);
    private static final Color BORDER_LIGHT = Color.rgb(200, 220, 210);
    private static final Color TEXT_DARK = Color.rgb(40, 70, 50);
    private static final Color TEXT_LIGHT = Color.rgb(100, 130, 110);

    public QuestionPanel(MentisLoginFrame parentApp, QuestionController questionController,  // FIXED: Parameter type
                         AssessmentController assessmentController) {
        this.parentApp = parentApp;
        this.questionController = questionController;
        this.assessmentController = assessmentController;

        setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        setPadding(new Insets(40, 50, 40, 50));
        setSpacing(30);

        createHeader();
        createTable();
    }

    private void createHeader() {
        VBox headerContainer = new VBox(20);
        headerContainer.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

        // Top right - CANCEL button
        HBox topRight = new HBox();
        topRight.setAlignment(Pos.CENTER_RIGHT);
        topRight.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

        Button cancelButton = createCancelButton();
        topRight.getChildren().add(cancelButton);

        // Title section
        HBox titlePanel = new HBox();
        titlePanel.setAlignment(Pos.CENTER_LEFT);
        titlePanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        titlePanel.setPadding(new Insets(20, 0, 0, 0));

        // Left side titles
        VBox titleLeft = new VBox(5);
        titleLeft.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

        Label mainTitle = new Label("Manage questions for Assessment");
        mainTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        mainTitle.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        assessmentNameLabel = new Label("All Assessments");
        assessmentNameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        assessmentNameLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        assessmentTypeLabel = new Label("Viewing all questions");
        assessmentTypeLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        assessmentTypeLabel.setTextFill(Color.web(toHex(TEXT_LIGHT)));

        titleLeft.getChildren().addAll(mainTitle, assessmentNameLabel, assessmentTypeLabel);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Button container for ADD and AI Generate buttons
        HBox buttonContainer = new HBox(10); // 10px spacing between buttons
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);

        // ADD button
        Button addButton = createAddButton();
        addButton.setOnAction(e -> showAddQuestionDialog());

        // AI Generate button
        Button aiGenerateBtn = createAIGenerateButton();

        buttonContainer.getChildren().addAll(addButton, aiGenerateBtn);

        titlePanel.getChildren().addAll(titleLeft, spacer, buttonContainer);

        headerContainer.getChildren().addAll(topRight, titlePanel);
        getChildren().add(headerContainer);
    }

    // Add this new method to create the AI Generate button
    private Button createAIGenerateButton() {
        Button aiGenerateBtn = new Button("🤖 GENERATE WITH AI");
        aiGenerateBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        aiGenerateBtn.setTextFill(Color.WHITE);
        aiGenerateBtn.setStyle(
                "-fx-background-color: #425a3f;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 12 25;" +
                        "-fx-cursor: hand;"
        );

        // Hover effect
        aiGenerateBtn.setOnMouseEntered(e -> aiGenerateBtn.setStyle(
                "-fx-background-color: #273e1d;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 12 25;" +
                        "-fx-cursor: hand;"
        ));

        aiGenerateBtn.setOnMouseExited(e -> aiGenerateBtn.setStyle(
                "-fx-background-color: #425a3f;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 12 25;" +
                        "-fx-cursor: hand;"
        ));

        aiGenerateBtn.setOnAction(e -> {
            if (currentAssessmentId <= 0) {
                showAlert("No Assessment Selected",
                        "Please select an assessment first before generating questions.\n\n" +
                                "Go back to the Assessment Panel and select a specific assessment.",
                        Alert.AlertType.WARNING);
                return;
            }

            // Find the selected assessment
            Assessment selectedAssessment = null;
            try {
                if (assessments != null) {
                    for (Assessment assessment : assessments) {
                        if (assessment.getAssessmentId() == currentAssessmentId) {
                            selectedAssessment = assessment;
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (selectedAssessment == null) {
                showAlert("Error", "Could not find the selected assessment.", Alert.AlertType.ERROR);
                return;
            }

            // Open the AI Question Generator dialog
            new AIQuestionGeneratorDialog(
                    selectedAssessment,
                    questionController,  // pass your existing QuestionController instance
                    this::refreshData     // refresh method
            ).show();
        });

        return aiGenerateBtn;
    }

    private Button createCancelButton() {
        Button button = new Button("CANCEL");
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        button.setTextFill(Color.web(toHex(TEXT_DARK)));
        button.setStyle(
                "-fx-background-color: #" + toHex(BUTTON_LIGHT_GREEN) + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 12 40;" +
                        "-fx-cursor: hand;"
        );

        // Hover effect
        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(BUTTON_LIGHT_GREEN.darker()) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 12 40;" +
                                "-fx-cursor: hand;"
                )
        );
        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(BUTTON_LIGHT_GREEN) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 12 40;" +
                                "-fx-cursor: hand;"
                )
        );

        // FIXED: Changed from showPanel to showAssessmentPanel
        button.setOnAction(e -> parentApp.showAssessmentPanel());
        return button;
    }

    private Button createAddButton() {
        Button button = new Button("ADD");
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        button.setTextFill(Color.web(toHex(TEXT_DARK)));
        button.setStyle(
                "-fx-background-color: #" + toHex(BUTTON_LIGHT_GREEN) + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 12 40;" +
                        "-fx-cursor: hand;"
        );

        // Hover effect
        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(BUTTON_LIGHT_GREEN.darker()) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 12 40;" +
                                "-fx-cursor: hand;"
                )
        );
        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(BUTTON_LIGHT_GREEN) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 12 40;" +
                                "-fx-cursor: hand;"
                )
        );

        return button;
    }

    private void createTable() {
        questionTable = new TableView<>();
        questionTable.setStyle("-fx-background-color: white; -fx-border-color: #" + toHex(BORDER_LIGHT) + ";");
        // FIXED: setRowHeight doesn't exist in JavaFX TableView
        questionTable.setFixedCellSize(70);  // Use setFixedCellSize instead
        questionTable.setPlaceholder(new Label("No questions available. Click ADD to create one."));

        // Create columns
        TableColumn<QuestionModel, String> questionCol = new TableColumn<>("Question");
        questionCol.setCellValueFactory(new PropertyValueFactory<>("questionText"));
        questionCol.setPrefWidth(300);
        questionCol.setCellFactory(column -> new TableCell<QuestionModel, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String displayText = item.length() > 80 ? item.substring(0, 77) + "..." : item;
                    setText(displayText);
                    setWrapText(true);
                }
            }
        });

        TableColumn<QuestionModel, String> scaleCol = new TableColumn<>("Scale");
        scaleCol.setCellValueFactory(new PropertyValueFactory<>("scale"));
        scaleCol.setPrefWidth(150);
        scaleCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<QuestionModel, String> assessmentCol = new TableColumn<>("Assessment");
        assessmentCol.setCellValueFactory(new PropertyValueFactory<>("assessmentName"));
        assessmentCol.setPrefWidth(200);
        assessmentCol.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<QuestionModel, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("assessmentType"));
        typeCol.setPrefWidth(150);
        typeCol.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<QuestionModel, Void> editCol = new TableColumn<>("Edit");
        editCol.setPrefWidth(80);
        editCol.setCellFactory(col -> new ActionButtonCell("Edit", true));

        TableColumn<QuestionModel, Void> deleteCol = new TableColumn<>("Delete");
        deleteCol.setPrefWidth(80);
        deleteCol.setCellFactory(col -> new ActionButtonCell("Delete", false));

        questionTable.getColumns().addAll(questionCol, scaleCol, assessmentCol, typeCol, editCol, deleteCol);

        // Style table header
        questionTable.getColumns().forEach(col -> {
            col.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-text-fill: #" + toHex(ACCENT_DARK_GREEN) + ";" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;"
            );
        });

        VBox.setVgrow(questionTable, Priority.ALWAYS);
        getChildren().add(questionTable);
    }

    // Action Button Cell
    class ActionButtonCell extends TableCell<QuestionModel, Void> {
        private final Button button;
        private final boolean isEdit;

        public ActionButtonCell(String text, boolean isEdit) {
            this.isEdit = isEdit;
            this.button = new Button(text);
            button.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
            button.setTextFill(Color.web(toHex(TEXT_DARK)));
            button.setStyle(
                    "-fx-background-color: #" + toHex(CARD_WHITE) + ";" +
                            "-fx-background-radius: 5;" +
                            "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                            "-fx-border-radius: 5;" +
                            "-fx-padding: 8 15;" +
                            "-fx-cursor: hand;"
            );

            button.setOnAction(e -> {
                QuestionModel question = getTableView().getItems().get(getIndex());
                if (isEdit) {
                    showEditQuestionDialog(question);
                } else {
                    showDeleteQuestionDialog(question);
                }
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                setGraphic(button);
            }
        }
    }

    // Question Model for TableView
    public static class QuestionModel {
        private final SimpleIntegerProperty questionId;
        private final SimpleIntegerProperty assessmentId;
        private final SimpleStringProperty questionText;
        private final SimpleStringProperty scale;
        private final SimpleStringProperty assessmentName;
        private final SimpleStringProperty assessmentType;

        public QuestionModel(int questionId, int assessmentId, String questionText,
                             String scale, String assessmentName, String assessmentType) {
            this.questionId = new SimpleIntegerProperty(questionId);
            this.assessmentId = new SimpleIntegerProperty(assessmentId);
            this.questionText = new SimpleStringProperty(questionText);
            this.scale = new SimpleStringProperty(scale);
            this.assessmentName = new SimpleStringProperty(assessmentName);
            this.assessmentType = new SimpleStringProperty(assessmentType);
        }

        public int getQuestionId() { return questionId.get(); }
        public int getAssessmentId() { return assessmentId.get(); }
        public String getQuestionText() { return questionText.get(); }
        public String getScale() { return scale.get(); }
        public String getAssessmentName() { return assessmentName.get(); }
        public String getAssessmentType() { return assessmentType.get(); }

        public void setQuestionId(int id) { this.questionId.set(id); }
        public void setAssessmentId(int id) { this.assessmentId.set(id); }
        public void setQuestionText(String text) { this.questionText.set(text); }
        public void setScale(String scale) { this.scale.set(scale); }
        public void setAssessmentName(String name) { this.assessmentName.set(name); }
        public void setAssessmentType(String type) { this.assessmentType.set(type); }

        // Property getters for TableView
        public SimpleIntegerProperty questionIdProperty() { return questionId; }
        public SimpleIntegerProperty assessmentIdProperty() { return assessmentId; }
        public SimpleStringProperty questionTextProperty() { return questionText; }
        public SimpleStringProperty scaleProperty() { return scale; }
        public SimpleStringProperty assessmentNameProperty() { return assessmentName; }
        public SimpleStringProperty assessmentTypeProperty() { return assessmentType; }
    }

    // SimpleIntegerProperty wrapper
    public static class SimpleIntegerProperty extends javafx.beans.property.SimpleIntegerProperty {
        public SimpleIntegerProperty(int value) { super(value); }
    }

    // SimpleStringProperty wrapper
    public static class SimpleStringProperty extends javafx.beans.property.SimpleStringProperty {
        public SimpleStringProperty(String value) { super(value); }
    }

    private void showDeleteQuestionDialog(QuestionModel questionModel) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete this question?\n\n" +
                questionModel.getQuestionText());

        // FIXED: Removed initOwner since getScene() doesn't exist in MentisLoginFrame
        // confirm.initOwner(parentApp.getScene().getWindow());

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    Question question = new Question();
                    question.setQuestionId(questionModel.getQuestionId());
                    questionController.deleteQuestion(question.getQuestionId());

                    showAlert("Success", "Question deleted successfully!", Alert.AlertType.INFORMATION);
                    refreshData();
                } catch (SQLException e) {
                    showAlert("Error", "Error deleting question: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void showAddQuestionDialog() {
        new QuestionFormDialog(parentApp, questionController, assessmentController,
                null, currentAssessmentId, false);
    }

    private void showEditQuestionDialog(QuestionModel questionModel) {
        try {
            List<Question> questions = questionController.getAllQuestions();
            Question questionToEdit = null;
            for (Question q : questions) {
                if (q.getQuestionId() == questionModel.getQuestionId()) {
                    questionToEdit = q;
                    break;
                }
            }

            if (questionToEdit != null) {
                new QuestionFormDialog(parentApp, questionController, assessmentController,
                        questionToEdit, questionToEdit.getAssessmentId(), true);
            }
        } catch (SQLException e) {
            showAlert("Error", "Error loading question: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void refreshData() {
        questionData = FXCollections.observableArrayList();

        try {
            assessments = assessmentController.getAllAssessments();

            List<Question> questions;
            if (currentAssessmentId > 0) {
                questions = questionController.getQuestionsByAssessment(currentAssessmentId);

                for (Assessment assessment : assessments) {
                    if (assessment.getAssessmentId() == currentAssessmentId) {
                        assessmentNameLabel.setText(assessment.getTitle());
                        assessmentTypeLabel.setText(assessment.getType() + " Assessment");
                        break;
                    }
                }
            } else {
                questions = questionController.getAllQuestions();
                assessmentNameLabel.setText("All Assessments");
                assessmentTypeLabel.setText("Viewing all questions");
            }

            if (questions == null || questions.isEmpty()) {
                questionTable.setPlaceholder(new Label(
                        currentAssessmentId > 0 ?
                                "No questions available for this assessment. Click ADD to create one." :
                                "No questions available. Click ADD to create one."
                ));
                questionTable.setItems(FXCollections.observableArrayList());
                return;
            }

            for (Question question : questions) {
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

                QuestionModel model = new QuestionModel(
                        question.getQuestionId(),
                        question.getAssessmentId(),
                        question.getText(),
                        question.getScale(),
                        assessmentName,
                        assessmentType
                );
                questionData.add(model);
            }

            questionTable.setItems(questionData);

        } catch (SQLException e) {
            e.printStackTrace();
            questionTable.setPlaceholder(new Label("Database Error: " + e.getMessage()));
            showAlert("Database Error",
                    "Cannot connect to database.\nError: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            questionTable.setPlaceholder(new Label("Error loading data: " + e.getMessage()));
        }
    }

    public void setCurrentAssessmentId(int assessmentId) {
        this.currentAssessmentId = assessmentId;
        refreshData();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        // FIXED: Removed initOwner since getScene() doesn't exist in MentisLoginFrame
        // alert.initOwner(parentApp.getScene().getWindow());

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