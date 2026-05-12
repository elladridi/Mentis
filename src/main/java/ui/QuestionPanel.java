package ui;

import controller.AssessmentController;
import controller.QuestionController;
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

    private MentisLoginFrame parentApp;
    private QuestionController questionController;
    private AssessmentController assessmentController;
    private TableView<QuestionModel> questionTable;
    private ObservableList<QuestionModel> questionData;
    private List<Assessment> assessments;
    private int currentAssessmentId = -1;

    private Label assessmentTypeLabel;
    private Label assessmentNameLabel;


    // Symfony-like colors and helpers
    private static final Color PAGE_BG = Color.web("#F8F9FA");
    private static final Color SOFT_GREEN = Color.web("#F1F8E9");
    private static final Color EMERALD = Color.web("#50C878");
    private static final Color EMERALD_DARK = Color.web("#2E7D32");
    private static final Color EMERALD_MID = Color.web("#3A9B5E");
    private static final Color INK = Color.web("#1A3C34");
    private static final Color MUTED = Color.web("#6C757D");
    private static final Color LINE = Color.web("#E9ECEF");
    private static final Color DANGER = Color.web("#E74C3C");
    private static final Color WARNING = Color.web("#F39C12");

    private String css(Color color) {
        return "#" + toHex(color);
    }

    private String gradient(Color left, Color right) {
        return "linear-gradient(to bottom right, " + css(left) + ", " + css(right) + ")";
    }

    private String softShadow() {
        return "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 18, 0, 0, 8);";
    }

    private String cardStyle() {
        return "-fx-background-color: white;" +
                "-fx-background-radius: 24;" +
                "-fx-border-radius: 24;" +
                "-fx-border-color: transparent;" +
                softShadow();
    }

    private String pillInputStyle() {
        return "-fx-background-color: white;" +
                "-fx-background-radius: 999;" +
                "-fx-border-radius: 999;" +
                "-fx-border-color: " + css(EMERALD) + ";" +
                "-fx-border-width: 2;" +
                "-fx-padding: 10 18;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-size: 14px;";
    }

    private Button primaryButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        button.setTextFill(Color.WHITE);
        button.setStyle(
                "-fx-background-color: " + gradient(EMERALD, EMERALD_MID) + ";" +
                        "-fx-background-radius: 999;" +
                        "-fx-padding: 11 26;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(80,200,120,0.30), 16, 0, 0, 7);"
        );
        button.setOnMouseEntered(e -> {
            if (!button.isDisable()) {
                button.setStyle(
                        "-fx-background-color: " + gradient(EMERALD, EMERALD_DARK) + ";" +
                                "-fx-background-radius: 999;" +
                                "-fx-padding: 11 26;" +
                                "-fx-cursor: hand;" +
                                "-fx-translate-y: -2;" +
                                "-fx-effect: dropshadow(gaussian, rgba(80,200,120,0.45), 22, 0, 0, 9);"
                );
            }
        });
        button.setOnMouseExited(e -> {
            if (!button.isDisable()) {
                button.setStyle(
                        "-fx-background-color: " + gradient(EMERALD, EMERALD_MID) + ";" +
                                "-fx-background-radius: 999;" +
                                "-fx-padding: 11 26;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(80,200,120,0.30), 16, 0, 0, 7);"
                );
            }
        });
        return button;
    }

    private Button outlineButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        button.setTextFill(MUTED);
        button.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-radius: 999;" +
                        "-fx-border-color: #CED4DA;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-padding: 10 24;" +
                        "-fx-cursor: hand;"
        );
        button.setOnMouseEntered(e -> {
            if (!button.isDisable()) {
                button.setTextFill(EMERALD_DARK);
                button.setStyle(
                        "-fx-background-color: #F1F8E9;" +
                                "-fx-background-radius: 999;" +
                                "-fx-border-radius: 999;" +
                                "-fx-border-color: " + css(EMERALD) + ";" +
                                "-fx-border-width: 1.5;" +
                                "-fx-padding: 10 24;" +
                                "-fx-cursor: hand;"
                );
            }
        });
        button.setOnMouseExited(e -> {
            if (!button.isDisable()) {
                button.setTextFill(MUTED);
                button.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-background-radius: 999;" +
                                "-fx-border-radius: 999;" +
                                "-fx-border-color: #CED4DA;" +
                                "-fx-border-width: 1.5;" +
                                "-fx-padding: 10 24;" +
                                "-fx-cursor: hand;"
                );
            }
        });
        return button;
    }

    private Label badge(String text, Color bg, Color fg) {
        Label label = new Label(text == null ? "N/A" : text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        label.setTextFill(fg);
        label.setPadding(new Insets(6, 13, 6, 13));
        label.setStyle("-fx-background-color: " + css(bg) + "; -fx-background-radius: 999;");
        return label;
    }

    private void styleTable(TableView<QuestionModel> table) {

        table.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 24;" +
                        "-fx-border-radius: 24;" +
                        "-fx-border-color: transparent;" +
                        softShadow()
        );

        table.setFixedCellSize(68);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.setRowFactory(tv -> new TableRow<QuestionModel>() {

            @Override
            protected void updateItem(QuestionModel item, boolean empty) {

                super.updateItem(item, empty);

                if (empty || item == null) {

                    setStyle("-fx-background-color: transparent;");

                } else {

                    if (getIndex() % 2 == 0) {

                        setStyle("-fx-background-color: white;");

                    } else {

                        setStyle("-fx-background-color: #FBFCFC;");

                    }
                }
            }
        });
    }

    public QuestionPanel(MentisLoginFrame parentApp, QuestionController questionController,
                         AssessmentController assessmentController) {
        this.parentApp = parentApp;
        this.questionController = questionController;
        this.assessmentController = assessmentController;

        setStyle("-fx-background-color: " + css(PAGE_BG) + ";");
        setPadding(new Insets(44, 56, 44, 56));
        setSpacing(28);

        createHeader();
        createTable();
    }

    private void createHeader() {
        VBox headerContainer = new VBox(22);
        headerContainer.setStyle("-fx-background-color: transparent;");

        HBox titlePanel = new HBox();
        titlePanel.setAlignment(Pos.CENTER_LEFT);

        VBox titleLeft = new VBox(4);

        Label mainTitle = new Label("❔ Manage Questions");
        mainTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 40));
        mainTitle.setTextFill(EMERALD_DARK);

        assessmentNameLabel = new Label("All Assessments");
        assessmentNameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        assessmentNameLabel.setTextFill(INK);

        assessmentTypeLabel = new Label("Viewing all questions");
        assessmentTypeLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
        assessmentTypeLabel.setTextFill(MUTED);

        titleLeft.getChildren().addAll(mainTitle, assessmentNameLabel, assessmentTypeLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);

        Button backButton = outlineButton("← Back");
        backButton.setOnAction(e -> parentApp.showAssessmentPanel());

        Button aiGenerateBtn = createAIGenerateButton();

        Button addButton = primaryButton("➕ Add Question");
        addButton.setOnAction(e -> showAddQuestionDialog());

        buttonContainer.getChildren().addAll(backButton, aiGenerateBtn, addButton);

        titlePanel.getChildren().addAll(titleLeft, spacer, buttonContainer);
        headerContainer.getChildren().add(titlePanel);
        getChildren().add(headerContainer);
    }

    private Button createAIGenerateButton() {
        Button aiGenerateBtn = primaryButton("🤖 Generate with AI");
        aiGenerateBtn.setOnAction(e -> {
            if (currentAssessmentId <= 0) {
                showAlert("No Assessment Selected",
                        "Please select an assessment first before generating questions.\n\nGo back to the Assessment Panel and select a specific assessment.",
                        Alert.AlertType.WARNING);
                return;
            }

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

            new AIQuestionGeneratorDialog(selectedAssessment, questionController, this::refreshData).show();
        });

        return aiGenerateBtn;
    }

    private void createTable() {
        questionTable = new TableView<>();
        styleTable(questionTable);
        questionTable.setPlaceholder(emptyLabel("No questions available. Click Add Question to create one."));

        TableColumn<QuestionModel, String> questionCol = new TableColumn<>("Question");
        questionCol.setCellValueFactory(new PropertyValueFactory<>("questionText"));
        questionCol.setPrefWidth(330);
        questionCol.setCellFactory(column -> new TableCell<QuestionModel, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.length() > 90 ? item.substring(0, 87) + "..." : item);
                    setWrapText(true);
                    setTextFill(INK);
                    setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                }
            }
        });

        TableColumn<QuestionModel, String> scaleCol = new TableColumn<>("Scale");
        scaleCol.setCellValueFactory(new PropertyValueFactory<>("scale"));
        scaleCol.setPrefWidth(210);
        scaleCol.setCellFactory(column -> new TableCell<QuestionModel, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : badge("📏 " + item, Color.web("#E3F2FD"), Color.web("#1565C0")));
                setText(null);
                setAlignment(Pos.CENTER_LEFT);
            }
        });

        TableColumn<QuestionModel, String> assessmentCol = new TableColumn<>("Assessment");
        assessmentCol.setCellValueFactory(new PropertyValueFactory<>("assessmentName"));
        assessmentCol.setPrefWidth(210);
        assessmentCol.setCellFactory(column -> new TableCell<QuestionModel, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : badge(item, SOFT_GREEN, EMERALD_DARK));
                setText(null);
                setAlignment(Pos.CENTER_LEFT);
            }
        });

        TableColumn<QuestionModel, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("assessmentType"));
        typeCol.setPrefWidth(140);
        typeCol.setCellFactory(column -> new TableCell<QuestionModel, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setTextFill(MUTED);
                setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
            }
        });

        TableColumn<QuestionModel, Void> editCol = new TableColumn<>("Edit");
        editCol.setPrefWidth(90);
        editCol.setCellFactory(col -> new ActionButtonCell("✏ Edit", true));

        TableColumn<QuestionModel, Void> deleteCol = new TableColumn<>("Delete");
        deleteCol.setPrefWidth(100);
        deleteCol.setCellFactory(col -> new ActionButtonCell("🗑 Delete", false));

        questionTable.getColumns().addAll(questionCol, scaleCol, assessmentCol, typeCol, editCol, deleteCol);

        questionTable.getColumns().forEach(col -> col.setStyle(
                "-fx-background-color: #F8F9FA;" +
                        "-fx-text-fill: " + css(EMERALD_DARK) + ";" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-color: #E9ECEF;" +
                        "-fx-border-width: 0 0 1 0;"
        ));

        VBox.setVgrow(questionTable, Priority.ALWAYS);
        getChildren().add(questionTable);
    }

    class ActionButtonCell extends TableCell<QuestionModel, Void> {
        private final Button button;
        private final boolean isEdit;

        public ActionButtonCell(String text, boolean isEdit) {
            this.isEdit = isEdit;
            this.button = isEdit ? outlineButton(text) : outlineButton(text);
            if (!isEdit) button.setTextFill(DANGER);

            button.setOnAction(e -> {
                QuestionModel question = getTableView().getItems().get(getIndex());
                if (isEdit) showEditQuestionDialog(question);
                else showDeleteQuestionDialog(question);
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : button);
            setAlignment(Pos.CENTER);
        }
    }

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

        public SimpleIntegerProperty questionIdProperty() { return questionId; }
        public SimpleIntegerProperty assessmentIdProperty() { return assessmentId; }
        public SimpleStringProperty questionTextProperty() { return questionText; }
        public SimpleStringProperty scaleProperty() { return scale; }
        public SimpleStringProperty assessmentNameProperty() { return assessmentName; }
        public SimpleStringProperty assessmentTypeProperty() { return assessmentType; }
    }

    public static class SimpleIntegerProperty extends javafx.beans.property.SimpleIntegerProperty {
        public SimpleIntegerProperty(int value) { super(value); }
    }

    public static class SimpleStringProperty extends javafx.beans.property.SimpleStringProperty {
        public SimpleStringProperty(String value) { super(value); }
    }

    private void showDeleteQuestionDialog(QuestionModel questionModel) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete this question?\n\n" + questionModel.getQuestionText());

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
                questionTable.setPlaceholder(emptyLabel(
                        currentAssessmentId > 0 ?
                                "No questions available for this assessment. Click Add Question to create one." :
                                "No questions available. Click Add Question to create one."
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

                questionData.add(new QuestionModel(
                        question.getQuestionId(),
                        question.getAssessmentId(),
                        question.getText(),
                        question.getScale(),
                        assessmentName,
                        assessmentType
                ));
            }

            questionTable.setItems(questionData);

        } catch (SQLException e) {
            e.printStackTrace();
            questionTable.setPlaceholder(emptyLabel("Database Error: " + e.getMessage()));
            showAlert("Database Error", "Cannot connect to database.\nError: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            questionTable.setPlaceholder(emptyLabel("Error loading data: " + e.getMessage()));
        }
    }

    public void setCurrentAssessmentId(int assessmentId) {
        this.currentAssessmentId = assessmentId;
        refreshData();
    }

    private Label emptyLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
        label.setTextFill(MUTED);
        return label;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
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
}
