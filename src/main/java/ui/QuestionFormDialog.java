package ui;

import controller.AssessmentController;
import controller.QuestionController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.Assessment;
import models.Question;

import java.sql.SQLException;
import java.util.List;

public class QuestionFormDialog extends Stage {

    private MentisLoginFrame parentApp;
    private QuestionController questionController;
    private AssessmentController assessmentController;
    private Question question;
    private int assessmentId;

    private ComboBox<Assessment> assessmentCombo;
    private TextArea questionTextArea;
    private ComboBox<String> scaleCombo;
    private TextField customScaleField;
    private Label scalePreviewLabel;
    private Label typeDisplayLabel;
    private Label previewQuestionText;
    private Label previewScaleLabel;

    // Scale presets
    private static final String[][] SCALE_PRESETS = {
            {"Frequency — Never / Rarely / Sometimes / Often / Always",
                    "Never/Rarely/Sometimes/Often/Always"},
            {"Intensity — Not at all / A little / Moderately / Quite a bit / Extremely",
                    "Not at all/A little/Moderately/Quite a bit/Extremely"},
            {"Frequency 2 — Never / Sometimes / Half the time / Usually / Always",
                    "Never/Sometimes/Half the time/Usually/Always"},
            {"Agreement — Strongly Disagree / Disagree / Neutral / Agree / Strongly Agree",
                    "Strongly Disagree/Disagree/Neutral/Agree/Strongly Agree"},
            {"PHQ-9 — Not at all / Several days / More than half / Nearly every day",
                    "Not at all/Several days/More than half/Nearly every day"},
            {"Numeric — 1 to 5", "1-5"},
            {"Numeric — 1 to 10", "1-10"},
            {"Binary — Yes / No", "Yes/No"},
            {"Scored — 0=Never / 1=Rarely / 2=Sometimes / 3=Often / 4=Always",
                    "0=Never,1=Rarely,2=Sometimes,3=Often,4=Always"},
            {"✏️  Custom — type your own", "CUSTOM"},
    };

    // Modern color scheme matching Symfony
    private static final String GRADIENT_START = "#50C878";
    private static final String GRADIENT_END = "#2E7D32";
    private static final Color BACKGROUND_LIGHT = Color.rgb(248, 250, 248);
    private static final Color CARD_WHITE = Color.WHITE;
    private static final Color TEXT_DARK = Color.rgb(46, 125, 50);
    private static final Color TEXT_MUTED = Color.rgb(108, 117, 125);
    private static final Color BORDER_COLOR = Color.rgb(222, 226, 230);
    private static final Color PREVIEW_BG = Color.rgb(248, 249, 250);
    private static final Color SUCCESS_BG = Color.rgb(80, 200, 120, 0.1);
    private static final Color WARNING_BG = Color.rgb(255, 193, 7, 0.1);

    public QuestionFormDialog(
            MentisLoginFrame parentApp,
            QuestionController questionController,
            AssessmentController assessmentController,
            Question question,
            Integer specificAssessmentId,
            boolean isEdit
    ) {
        this.parentApp = parentApp;
        this.questionController = questionController;
        this.assessmentController = assessmentController;
        this.question = question;
        this.assessmentId = specificAssessmentId != null ? specificAssessmentId : -1;

        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        setTitle(isEdit ? "Edit Question - Mentis" : "Add Question - Mentis");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + toHex(BACKGROUND_LIGHT) + ";");
        root.setTop(createHeader(isEdit));
        root.setCenter(createForm());
        root.setBottom(createButtonPanel(isEdit));

        Scene scene = new Scene(root, 850, 750);
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
        header.setPadding(new Insets(25, 35, 20, 35));
        header.setStyle("-fx-background-color: " + toHex(BACKGROUND_LIGHT) + ";");

        VBox headerContent = new VBox(5);

        Label titleLabel = new Label(isEdit ? "✏️ Edit Question" : "✨ Add New Question");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: " + GRADIENT_END + ";");

        Label subtitleLabel = new Label(isEdit ? "Update question details and settings" : "Create a new question for an assessment");
        subtitleLabel.setFont(Font.font("Segoe UI", 14));
        subtitleLabel.setTextFill(TEXT_MUTED);

        headerContent.getChildren().addAll(titleLabel, subtitleLabel);
        header.getChildren().add(headerContent);

        return header;
    }

    private ScrollPane createForm() {
        VBox mainContainer = new VBox(20);
        mainContainer.setStyle("-fx-background-color: " + toHex(BACKGROUND_LIGHT) + ";");
        mainContainer.setPadding(new Insets(10, 35, 20, 35));

        // Create all sections
        VBox assessmentSection = createAssessmentSection();
        VBox questionSection = createQuestionSection();
        VBox scaleSection = createScaleSection();
        VBox previewSection = createPreviewSection();

        // Wrap sections in cards
        VBox assessmentCard = createCard("📋 Assessment", assessmentSection);
        VBox questionCard = createCard("❓ Question Text", questionSection);
        VBox scaleCard = createCard("📊 Answer Scale", scaleSection);
        VBox previewCard = createCard("👁️ Preview", previewSection);

        mainContainer.getChildren().addAll(assessmentCard, questionCard, scaleCard, previewCard);

        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setStyle("-fx-background-color: " + toHex(BACKGROUND_LIGHT) + ";");
        scrollPane.setBorder(null);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return scrollPane;
    }

    private VBox createCard(String title, VBox content) {
        VBox card = new VBox(15);
        card.setStyle(
                "-fx-background-color: " + toHex(CARD_WHITE) + ";" +
                        "-fx-background-radius: 16px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);"
        );
        card.setPadding(new Insets(20));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        titleLabel.setStyle("-fx-text-fill: " + GRADIENT_END + ";");

        card.getChildren().addAll(titleLabel, content);
        return card;
    }

    private VBox createAssessmentSection() {
        VBox section = new VBox(10);

        try {
            List<Assessment> assessments = assessmentController.getAllAssessments();
            ObservableList<Assessment> list = FXCollections.observableArrayList(assessments);

            assessmentCombo = new ComboBox<>(list);
            assessmentCombo.setPrefHeight(45);
            assessmentCombo.setMaxWidth(Double.MAX_VALUE);
            styleComboBox(assessmentCombo);

            assessmentCombo.setButtonCell(new ListCell<Assessment>() {
                @Override
                protected void updateItem(Assessment item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null
                            : item.getTitle() + " (" + item.getType() + ")");
                    setFont(Font.font("Segoe UI", 13));
                }
            });
            assessmentCombo.setCellFactory(lv -> new ListCell<Assessment>() {
                @Override
                protected void updateItem(Assessment item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null
                            : item.getTitle() + " (" + item.getType() + ")");
                    setFont(Font.font("Segoe UI", 13));
                }
            });

            if (assessmentId != -1) {
                for (Assessment a : list) {
                    if (a.getAssessmentId() == assessmentId) {
                        assessmentCombo.setValue(a);
                        if (question != null) assessmentCombo.setDisable(true);
                        break;
                    }
                }
            }

            // Type display
            HBox typeBox = new HBox(10);
            typeBox.setAlignment(Pos.CENTER_LEFT);
            Label typeLabel = new Label("Assessment Type:");
            typeLabel.setFont(Font.font("Segoe UI", 12));
            typeLabel.setTextFill(TEXT_MUTED);

            typeDisplayLabel = new Label("—");
            typeDisplayLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            typeDisplayLabel.setTextFill(Color.web(GRADIENT_END));
            typeDisplayLabel.setStyle(
                    "-fx-background-color: " + toHex(SUCCESS_BG) + ";" +
                            "-fx-background-radius: 20px;" +
                            "-fx-padding: 5 15;"
            );

            typeBox.getChildren().addAll(typeLabel, typeDisplayLabel);

            if (assessmentCombo.getValue() != null) {
                typeDisplayLabel.setText(assessmentCombo.getValue().getType());
            }

            assessmentCombo.valueProperty().addListener((obs, old, nw) -> {
                if (nw != null) typeDisplayLabel.setText(nw.getType());
            });

            section.getChildren().addAll(assessmentCombo, typeBox);

            Label helpText = new Label("Select which assessment this question belongs to");
            helpText.setFont(Font.font("Segoe UI", 11));
            helpText.setTextFill(TEXT_MUTED);
            section.getChildren().add(helpText);

        } catch (SQLException e) {
            Label errorLabel = new Label("⚠️ Error loading assessments: " + e.getMessage());
            errorLabel.setTextFill(Color.RED);
            section.getChildren().add(errorLabel);
        }

        return section;
    }

    private VBox createQuestionSection() {
        VBox section = new VBox(10);

        questionTextArea = new TextArea();
        questionTextArea.setFont(Font.font("Segoe UI", 14));
        questionTextArea.setWrapText(true);
        questionTextArea.setPrefRowCount(4);
        questionTextArea.setPromptText("Enter your question here...");
        styleTextArea(questionTextArea);

        section.getChildren().add(questionTextArea);

        // Live preview listener
        questionTextArea.textProperty().addListener((obs, old, nw) -> {
            if (previewQuestionText != null) {
                previewQuestionText.setText(nw.isEmpty() ? "Question preview will appear here" : nw);
                previewQuestionText.setStyle(nw.isEmpty() ?
                        "-fx-text-fill: " + toHex(TEXT_MUTED) + ";" :
                        "-fx-text-fill: " + GRADIENT_END + ";-fx-font-weight: bold;");
            }
        });

        return section;
    }

    private VBox createScaleSection() {
        VBox section = new VBox(12);

        // Scale ComboBox
        ObservableList<String> scaleLabels = FXCollections.observableArrayList();
        for (String[] preset : SCALE_PRESETS) {
            scaleLabels.add(preset[0]);
        }

        scaleCombo = new ComboBox<>(scaleLabels);
        scaleCombo.setPrefHeight(45);
        scaleCombo.setMaxWidth(Double.MAX_VALUE);
        scaleCombo.setPromptText("-- Select an answer scale --");
        styleComboBox(scaleCombo);

        // Custom scale field
        customScaleField = new TextField();
        customScaleField.setPromptText("Type options separated by / e.g., Poor/Fair/Good/Very Good/Excellent");
        customScaleField.setVisible(false);
        customScaleField.setManaged(false);
        styleTextField(customScaleField);

        // Preview area
        scalePreviewLabel = new Label();
        scalePreviewLabel.setFont(Font.font("Segoe UI", 12));
        scalePreviewLabel.setWrapText(true);
        scalePreviewLabel.setStyle(
                "-fx-background-color: " + toHex(PREVIEW_BG) + ";" +
                        "-fx-background-radius: 8px;" +
                        "-fx-padding: 10;" +
                        "-fx-border-color: " + toHex(BORDER_COLOR) + ";" +
                        "-fx-border-radius: 8px;"
        );
        scalePreviewLabel.setVisible(false);
        scalePreviewLabel.setManaged(false);

        // Info box
        VBox infoBox = new VBox(8);
        infoBox.setStyle(
                "-fx-background-color: " + toHex(WARNING_BG) + ";" +
                        "-fx-background-radius: 12px;" +
                        "-fx-padding: 12;"
        );

        Label infoTitle = new Label("💡 Examples:");
        infoTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        infoTitle.setTextFill(Color.web(GRADIENT_END));

        HBox examplesBox = new HBox(15);
        examplesBox.setAlignment(Pos.CENTER_LEFT);
        String[] examples = {"Never/Rarely/Sometimes/Often/Always", "1-5 numeric", "Yes/No", "1=Never,2=Rarely,3=Sometimes"};
        for (String example : examples) {
            Label exampleLabel = new Label(example);
            exampleLabel.setFont(Font.font("Courier New", 11));
            exampleLabel.setStyle(
                    "-fx-background-color: " + toHex(CARD_WHITE) + ";" +
                            "-fx-background-radius: 20px;" +
                            "-fx-padding: 4 12;" +
                            "-fx-border-color: " + toHex(BORDER_COLOR) + ";" +
                            "-fx-border-radius: 20px;"
            );
            examplesBox.getChildren().add(exampleLabel);
        }

        infoBox.getChildren().addAll(infoTitle, examplesBox);

        // Event handler
        scaleCombo.setOnAction(e -> {
            int idx = scaleCombo.getSelectionModel().getSelectedIndex();
            if (idx < 0) return;

            boolean isCustom = SCALE_PRESETS[idx][1].equals("CUSTOM");

            customScaleField.setVisible(isCustom);
            customScaleField.setManaged(isCustom);

            if (!isCustom) {
                String value = SCALE_PRESETS[idx][1];
                updateScalePreview(value);
            } else {
                scalePreviewLabel.setVisible(false);
                scalePreviewLabel.setManaged(false);
            }

            // Update preview
            if (previewScaleLabel != null) {
                if (!isCustom && SCALE_PRESETS[idx][1] != null) {
                    updatePreviewScale(SCALE_PRESETS[idx][0]);
                } else if (isCustom && !customScaleField.getText().isEmpty()) {
                    updatePreviewScale("Custom: " + customScaleField.getText());
                }
            }
        });

        customScaleField.textProperty().addListener((obs, old, nw) -> {
            updateScalePreview(nw);
            if (previewScaleLabel != null && scaleCombo.getSelectionModel().getSelectedIndex() >= 0) {
                int idx = scaleCombo.getSelectionModel().getSelectedIndex();
                if (idx >= 0 && SCALE_PRESETS[idx][1].equals("CUSTOM")) {
                    updatePreviewScale("Custom: " + (nw.isEmpty() ? "No scale entered" : nw));
                }
            }
        });

        section.getChildren().addAll(scaleCombo, customScaleField, scalePreviewLabel, infoBox);

        return section;
    }

    private VBox createPreviewSection() {
        VBox section = new VBox(12);

        VBox previewBox = new VBox(10);
        previewBox.setStyle(
                "-fx-background-color: " + toHex(CARD_WHITE) + ";" +
                        "-fx-background-radius: 12px;" +
                        "-fx-padding: 15;" +
                        "-fx-border-color: " + toHex(BORDER_COLOR) + ";" +
                        "-fx-border-radius: 12px;"
        );

        previewQuestionText = new Label("Question preview will appear here");
        previewQuestionText.setFont(Font.font("Segoe UI", 14));
        previewQuestionText.setWrapText(true);
        previewQuestionText.setTextFill(TEXT_MUTED);

        Separator separator = new Separator();

        HBox scaleInfoBox = new HBox(10);
        scaleInfoBox.setAlignment(Pos.CENTER_LEFT);
        Label scaleInfoLabel = new Label("Scale:");
        scaleInfoLabel.setFont(Font.font("Segoe UI", 12));
        scaleInfoLabel.setTextFill(TEXT_MUTED);

        previewScaleLabel = new Label("No scale selected");
        previewScaleLabel.setFont(Font.font("Segoe UI", 12));
        previewScaleLabel.setStyle(
                "-fx-background-color: " + toHex(SUCCESS_BG) + ";" +
                        "-fx-background-radius: 20px;" +
                        "-fx-padding: 4 12;"
        );

        scaleInfoBox.getChildren().addAll(scaleInfoLabel, previewScaleLabel);

        previewBox.getChildren().addAll(previewQuestionText, separator, scaleInfoBox);
        section.getChildren().add(previewBox);

        return section;
    }

    private HBox createButtonPanel(boolean isEdit) {
        HBox panel = new HBox(15);
        panel.setAlignment(Pos.CENTER_RIGHT);
        panel.setPadding(new Insets(25, 35, 30, 35));
        panel.setStyle("-fx-background-color: " + toHex(BACKGROUND_LIGHT) + ";");

        Button cancelButton = createModernButton("Cancel", false);
        cancelButton.setOnAction(e -> close());

        Button saveButton = createModernButton(isEdit ? "Save Changes" : "Add Question", true);
        saveButton.setOnAction(e -> saveQuestion());

        panel.getChildren().addAll(cancelButton, saveButton);
        return panel;
    }

    private Button createModernButton(String text, boolean isPrimary) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        if (isPrimary) {
            button.setStyle(
                    "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, " + GRADIENT_START + ", " + GRADIENT_END + ");" +
                            "-fx-background-radius: 25px;" +
                            "-fx-text-fill: white;" +
                            "-fx-padding: 12 35;" +
                            "-fx-cursor: hand;"
            );
            button.setOnMouseEntered(e ->
                    button.setStyle(
                            "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, " + GRADIENT_END + ", " + GRADIENT_START + ");" +
                                    "-fx-background-radius: 25px;" +
                                    "-fx-text-fill: white;" +
                                    "-fx-padding: 12 35;" +
                                    "-fx-cursor: hand;"
                    )
            );
        } else {
            button.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-border-color: " + toHex(BORDER_COLOR) + ";" +
                            "-fx-border-radius: 25px;" +
                            "-fx-text-fill: " + toHex(TEXT_MUTED) + ";" +
                            "-fx-padding: 12 35;" +
                            "-fx-cursor: hand;"
            );
            button.setOnMouseEntered(e ->
                    button.setStyle(
                            "-fx-background-color: " + toHex(PREVIEW_BG) + ";" +
                                    "-fx-border-color: " + toHex(BORDER_COLOR) + ";" +
                                    "-fx-border-radius: 25px;" +
                                    "-fx-text-fill: " + toHex(TEXT_DARK) + ";" +
                                    "-fx-padding: 12 35;" +
                                    "-fx-cursor: hand;"
                    )
            );
        }

        button.setOnMouseExited(e -> {
            if (isPrimary) {
                button.setStyle(
                        "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, " + GRADIENT_START + ", " + GRADIENT_END + ");" +
                                "-fx-background-radius: 25px;" +
                                "-fx-text-fill: white;" +
                                "-fx-padding: 12 35;" +
                                "-fx-cursor: hand;"
                );
            } else {
                button.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-border-color: " + toHex(BORDER_COLOR) + ";" +
                                "-fx-border-radius: 25px;" +
                                "-fx-text-fill: " + toHex(TEXT_MUTED) + ";" +
                                "-fx-padding: 12 35;" +
                                "-fx-cursor: hand;"
                );
            }
        });

        return button;
    }

    private void styleTextField(TextField field) {
        field.setStyle(
                "-fx-border-color: " + toHex(BORDER_COLOR) + ";" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-padding: 10;" +
                        "-fx-font-family: 'Segoe UI';"
        );

        field.focusedProperty().addListener((obs, old, nw) -> {
            if (nw) {
                field.setStyle(
                        "-fx-border-color: " + GRADIENT_START + ";" +
                                "-fx-border-radius: 8px;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-padding: 10;" +
                                "-fx-font-family: 'Segoe UI';" +
                                "-fx-border-width: 2px;"
                );
            } else {
                field.setStyle(
                        "-fx-border-color: " + toHex(BORDER_COLOR) + ";" +
                                "-fx-border-radius: 8px;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-padding: 10;" +
                                "-fx-font-family: 'Segoe UI';" +
                                "-fx-border-width: 1px;"
                );
            }
        });
    }

    private void styleTextArea(TextArea field) {
        field.setStyle(
                "-fx-border-color: " + toHex(BORDER_COLOR) + ";" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-padding: 10;" +
                        "-fx-font-family: 'Segoe UI';"
        );

        field.focusedProperty().addListener((obs, old, nw) -> {
            if (nw) {
                field.setStyle(
                        "-fx-border-color: " + GRADIENT_START + ";" +
                                "-fx-border-radius: 8px;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-padding: 10;" +
                                "-fx-font-family: 'Segoe UI';" +
                                "-fx-border-width: 2px;"
                );
            } else {
                field.setStyle(
                        "-fx-border-color: " + toHex(BORDER_COLOR) + ";" +
                                "-fx-border-radius: 8px;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-padding: 10;" +
                                "-fx-font-family: 'Segoe UI';" +
                                "-fx-border-width: 1px;"
                );
            }
        });
    }

    private void styleComboBox(ComboBox<?> comboBox) {
        comboBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: " + toHex(BORDER_COLOR) + ";" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 13px;"
        );

        comboBox.focusedProperty().addListener((obs, old, nw) -> {
            if (nw) {
                comboBox.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-border-color: " + GRADIENT_START + ";" +
                                "-fx-border-radius: 8px;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-font-family: 'Segoe UI';" +
                                "-fx-font-size: 13px;" +
                                "-fx-border-width: 2px;"
                );
            } else {
                comboBox.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-border-color: " + toHex(BORDER_COLOR) + ";" +
                                "-fx-border-radius: 8px;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-font-family: 'Segoe UI';" +
                                "-fx-font-size: 13px;" +
                                "-fx-border-width: 1px;"
                );
            }
        });
    }

    private void updatePreviewScale(String scaleText) {
        if (previewScaleLabel != null) {
            previewScaleLabel.setText(scaleText);
        }
    }

    private void loadQuestionData() {
        if (question == null) return;

        questionTextArea.setText(question.getText());

        String existingScale = question.getScale();
        if (existingScale == null || existingScale.isEmpty()) return;

        boolean matched = false;
        for (int i = 0; i < SCALE_PRESETS.length; i++) {
            if (SCALE_PRESETS[i][1].equals(existingScale)) {
                scaleCombo.getSelectionModel().select(i);
                updateScalePreview(existingScale);
                updatePreviewScale(SCALE_PRESETS[i][0]);
                matched = true;
                break;
            }
        }

        if (!matched) {
            for (int i = 0; i < SCALE_PRESETS.length; i++) {
                if (SCALE_PRESETS[i][1].equals("CUSTOM")) {
                    scaleCombo.getSelectionModel().select(i);
                    break;
                }
            }
            customScaleField.setVisible(true);
            customScaleField.setManaged(true);
            customScaleField.setText(existingScale);
            updateScalePreview(existingScale);
            updatePreviewScale("Custom: " + existingScale);
        }
    }

    private void updateScalePreview(String scaleValue) {
        if (scaleValue == null || scaleValue.isEmpty() || scaleValue.equals("CUSTOM")) {
            scalePreviewLabel.setVisible(false);
            scalePreviewLabel.setManaged(false);
            return;
        }

        String[] options = parseScaleOptions(scaleValue);
        if (options.length == 0) {
            scalePreviewLabel.setVisible(false);
            scalePreviewLabel.setManaged(false);
            return;
        }

        StringBuilder preview = new StringBuilder("📊 Options:  ");
        for (int i = 0; i < options.length; i++) {
            preview.append(options[i].trim());
            if (i < options.length - 1) preview.append("  •  ");
        }

        scalePreviewLabel.setText(preview.toString());
        scalePreviewLabel.setVisible(true);
        scalePreviewLabel.setManaged(true);
    }

    private String[] parseScaleOptions(String scale) {
        if (scale == null || scale.isEmpty()) {
            return new String[0];
        }
        scale = scale.trim();

        if (scale.contains("/")) {
            return scale.split("/");
        }
        if (scale.contains(",") && !scale.contains("=")) {
            return scale.split(",");
        }
        if (scale.contains("=")) {
            String[] pairs = scale.split(",");
            String[] vals = new String[pairs.length];
            for (int i = 0; i < pairs.length; i++) {
                String[] kv = pairs[i].split("=");
                vals[i] = kv.length > 1 ? kv[1].trim() : kv[0].trim();
            }
            return vals;
        }
        if (scale.matches("\\d+-\\d+")) {
            String[] parts = scale.split("-");
            int start = Integer.parseInt(parts[0]);
            int end = Integer.parseInt(parts[1]);
            String[] nums = new String[end - start + 1];
            for (int i = 0; i <= end - start; i++) nums[i] = String.valueOf(start + i);
            return nums;
        }

        return new String[]{scale};
    }

    private void saveQuestion() {
        String text = questionTextArea.getText().trim();
        if (text.isEmpty()) {
            showAlert("Validation Error", "Please enter the question text.", Alert.AlertType.WARNING);
            return;
        }

        String scaleValue = getSelectedScaleValue();
        if (scaleValue == null || scaleValue.isEmpty()) {
            showAlert("Validation Error", "Please select an answer scale.", Alert.AlertType.WARNING);
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
                Question newQ = new Question();
                newQ.setAssessmentId(selectedAssessmentId);
                newQ.setText(text);
                newQ.setScale(scaleValue);
                questionController.createQuestion(newQ);
                showAlert("Success", "✨ Question created successfully!", Alert.AlertType.INFORMATION);
            } else {
                question.setAssessmentId(selectedAssessmentId);
                question.setText(text);
                question.setScale(scaleValue);
                questionController.updateQuestion(question);
                showAlert("Success", "✅ Question updated successfully!", Alert.AlertType.INFORMATION);
            }

            close();
            if (parentApp != null) parentApp.showQuestionPanel();

        } catch (SQLException e) {
            showAlert("Database Error", "Error saving question: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Error", "Error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private String getSelectedScaleValue() {
        int idx = scaleCombo.getSelectionModel().getSelectedIndex();
        if (idx < 0) return null;

        String value = SCALE_PRESETS[idx][1];

        if (value.equals("CUSTOM")) {
            String custom = customScaleField.getText().trim();
            return custom.isEmpty() ? null : custom;
        }

        return value;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner(this);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: " + toHex(CARD_WHITE) + ";" +
                        "-fx-background-radius: 12px;" +
                        "-fx-padding: 20;"
        );

        alert.showAndWait();
    }

    private String toHex(Color color) {
        return String.format("#%02x%02x%02x",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }

    private String toHex(String hexColor) {
        return hexColor;
    }
}