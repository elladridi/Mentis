package ui;

import controller.QuestionController;
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
import models.Assessment;
import models.Question;
import services.GeminiService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * AI Question Generator Dialog (Admin only)
 *
 * Flow:
 *  1. Admin picks a topic / tone and clicks "Generate with AI"
 *  2. Gemini returns a list of questions with suggested scales
 *  3. Each question is shown with a CheckBox — admin validates/edits
 *  4. Admin clicks "Save Selected" → approved questions go to DB
 */
public class AIQuestionGeneratorDialog {

    // ── colours (match the rest of the app) ──────────────────────
    private static final Color ACCENT_GREEN      = Color.rgb(108, 158, 131);
    private static final Color BACKGROUND_BEIGE  = Color.rgb(243, 243, 243);
    private static final Color BORDER_LIGHT      = Color.rgb(220, 220, 220);
    private static final Color TEXT_DARK         = Color.rgb(60, 70, 80);

    private final Assessment assessment;
    private final QuestionController questionService;
    private final Runnable onSaveCallback;   // called after questions are saved so parent refreshes

    // UI references we need after generation
    private VBox questionsContainer;
    private Label statusLabel;
    private Button generateBtn;
    private Button saveBtn;

    // Generated question rows (so we can collect them on save)
    private final List<GeneratedQuestionRow> rows = new ArrayList<>();

    public AIQuestionGeneratorDialog(Assessment assessment,
                                     QuestionController questionService,
                                     Runnable onSaveCallback) {
        this.assessment      = assessment;
        this.questionService = questionService;
        this.onSaveCallback  = onSaveCallback;
    }

    public void show() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("🤖 AI Question Generator — " + assessment.getTitle());
        dialog.setMinWidth(780);
        dialog.setMinHeight(650);

        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";");
        root.setPadding(new Insets(30));

        // ── Header ────────────────────────────────────────────────
        Label title = new Label("🤖 AI Question Generator");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(toHex(ACCENT_GREEN)));

        Label subtitle = new Label("Generate mental health assessment questions for: " + assessment.getTitle());
        subtitle.setFont(Font.font("Segoe UI", 14));
        subtitle.setTextFill(Color.GRAY);

        // ── Controls ──────────────────────────────────────────────
        VBox controlsCard = card();
        controlsCard.setSpacing(15);

        // Number of questions
        HBox countRow = new HBox(15);
        countRow.setAlignment(Pos.CENTER_LEFT);
        Label countLbl = fieldLabel("Number of questions:");
        ComboBox<String> countBox = new ComboBox<>();
        countBox.getItems().addAll("3", "5", "7", "10");
        countBox.setValue("5");
        styleCombo(countBox);
        countRow.getChildren().addAll(countLbl, countBox);

        // Assessment type hint
        HBox typeRow = new HBox(15);
        typeRow.setAlignment(Pos.CENTER_LEFT);
        Label typeLbl = fieldLabel("Assessment focus:");
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll(
                "General mental wellness",
                "Anxiety & stress",
                "Depression & mood",
                "Sleep quality",
                "Social functioning",
                "Work-related burnout",
                "Self-esteem & confidence"
        );
        typeBox.setValue("General mental wellness");
        styleCombo(typeBox);
        typeBox.setPrefWidth(280);
        typeRow.getChildren().addAll(typeLbl, typeBox);

        // Scale preference
        HBox scaleRow = new HBox(15);
        scaleRow.setAlignment(Pos.CENTER_LEFT);
        Label scaleLbl = fieldLabel("Answer scale:");
        ComboBox<String> scaleBox = new ComboBox<>();
        scaleBox.getItems().addAll(
                "Never/Rarely/Sometimes/Often/Always",
                "Not at all/A little/Moderately/Quite a bit/Extremely",
                "1-5 numeric",
                "Yes/No",
                "Never/Sometimes/Half the time/Usually/Always"
        );
        scaleBox.setValue("Never/Rarely/Sometimes/Often/Always");
        styleCombo(scaleBox);
        scaleBox.setPrefWidth(350);
        scaleRow.getChildren().addAll(scaleLbl, scaleBox);

        // Extra context (optional)
        HBox contextRow = new HBox(15);
        contextRow.setAlignment(Pos.CENTER_LEFT);
        Label ctxLbl = fieldLabel("Extra context (optional):");
        TextField contextField = new TextField();
        contextField.setPromptText("e.g. for university students, ages 18-25");
        contextField.setPrefWidth(340);
        contextField.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8;"
        );
        contextRow.getChildren().addAll(ctxLbl, contextField);

        // Generate button
        generateBtn = new Button("✨ Generate with AI");
        generateBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        generateBtn.setTextFill(Color.WHITE);
        generateBtn.setStyle(
                "-fx-background-color: #" + toHex(ACCENT_GREEN) + ";" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12 30;" +
                        "-fx-cursor: hand;"
        );
        generateBtn.setOnMouseEntered(e -> generateBtn.setStyle(
                "-fx-background-color: #" + toHex(ACCENT_GREEN.darker()) + ";" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12 30;" +
                        "-fx-cursor: hand;"
        ));
        generateBtn.setOnMouseExited(e -> generateBtn.setStyle(
                "-fx-background-color: #" + toHex(ACCENT_GREEN) + ";" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12 30;" +
                        "-fx-cursor: hand;"
        ));

        statusLabel = new Label("");
        statusLabel.setFont(Font.font("Segoe UI", 13));
        statusLabel.setTextFill(Color.GRAY);

        HBox genRow = new HBox(20);
        genRow.setAlignment(Pos.CENTER_LEFT);
        genRow.getChildren().addAll(generateBtn, statusLabel);

        controlsCard.getChildren().addAll(countRow, typeRow, scaleRow, contextRow, genRow);

        // ── Generated Questions Area ───────────────────────────────
        Label reviewTitle = new Label("📋 Review Generated Questions");
        reviewTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        reviewTitle.setTextFill(Color.web(toHex(TEXT_DARK)));

        Label reviewHint = new Label("Check the questions you want to keep. You can edit the text and scale before saving.");
        reviewHint.setFont(Font.font("Segoe UI", 13));
        reviewHint.setTextFill(Color.GRAY);

        questionsContainer = new VBox(12);
        questionsContainer.setStyle("-fx-background-color: transparent;");

        Label placeholder = new Label("Generated questions will appear here after you click \"Generate with AI\".");
        placeholder.setFont(Font.font("Segoe UI", 14));
        placeholder.setTextFill(Color.LIGHTGRAY);
        placeholder.setPadding(new Insets(30));
        questionsContainer.getChildren().add(placeholder);

        // ── Bottom buttons ─────────────────────────────────────────
        saveBtn = new Button("💾 Save Selected Questions");
        saveBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        saveBtn.setTextFill(Color.WHITE);
        saveBtn.setDisable(true);
        saveBtn.setStyle(
                "-fx-background-color: #425a3f;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12 30;" +
                        "-fx-cursor: hand;"
        );

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        cancelBtn.setTextFill(Color.web(toHex(TEXT_DARK)));
        cancelBtn.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12 30;" +
                        "-fx-cursor: hand;"
        );
        cancelBtn.setOnAction(e -> dialog.close());

        HBox bottomBar = new HBox(15);
        bottomBar.setAlignment(Pos.CENTER_RIGHT);
        bottomBar.getChildren().addAll(cancelBtn, saveBtn);

        // ── Wire up Generate ──────────────────────────────────────
        generateBtn.setOnAction(e -> {
            int count = Integer.parseInt(countBox.getValue());
            String focus = typeBox.getValue();
            String scale = scaleBox.getValue();
            String context = contextField.getText().trim();
            generateQuestions(count, focus, scale, context);
        });

        // ── Wire up Save ─────────────────────────────────────────
        saveBtn.setOnAction(e -> {
            saveSelectedQuestions(dialog);
        });

        // ── Assemble ──────────────────────────────────────────────
        root.getChildren().addAll(
                title, subtitle,
                new Separator(),
                controlsCard,
                new Separator(),
                reviewTitle, reviewHint,
                questionsContainer,
                new Separator(),
                bottomBar
        );

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";");

        dialog.setScene(new Scene(scroll, 780, 700));
        dialog.showAndWait();
    }

    // ═══════════════════════════════════════════════════════════════
    //  GENERATION
    // ═══════════════════════════════════════════════════════════════

    private void generateQuestions(int count, String focus, String scale, String context) {
        generateBtn.setDisable(true);
        saveBtn.setDisable(true);
        statusLabel.setText("⏳ Asking Gemini to generate questions...");
        statusLabel.setTextFill(Color.GRAY);
        rows.clear();
        questionsContainer.getChildren().clear();

        // Run on background thread so UI doesn't freeze
        new Thread(() -> {
            try {
                String prompt = buildPrompt(count, focus, scale, context);
                String response = GeminiService.generateContent(prompt);
                List<ParsedQuestion> parsed = parseGeminiResponse(response, scale);

                // Back on FX thread
                javafx.application.Platform.runLater(() -> {
                    if (parsed.isEmpty()) {
                        statusLabel.setText("⚠️ Could not parse questions. Try again.");
                        statusLabel.setTextFill(Color.ORANGE);
                        generateBtn.setDisable(false);
                        return;
                    }

                    questionsContainer.getChildren().clear();
                    rows.clear();

                    for (int i = 0; i < parsed.size(); i++) {
                        GeneratedQuestionRow row = new GeneratedQuestionRow(i + 1, parsed.get(i));
                        rows.add(row);
                        questionsContainer.getChildren().add(row.build());
                    }

                    statusLabel.setText("✅ " + parsed.size() + " questions generated! Review and save below.");
                    statusLabel.setTextFill(Color.web(toHex(ACCENT_GREEN)));
                    generateBtn.setDisable(false);
                    saveBtn.setDisable(false);
                });

            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("❌ Gemini error: " + ex.getMessage());
                    statusLabel.setTextFill(Color.RED);
                    generateBtn.setDisable(false);
                });
            }
        }).start();
    }

    private String buildPrompt(int count, String focus, String scale, String context) {
        StringBuilder p = new StringBuilder();
        p.append("You are an expert clinical psychologist and mental health assessment designer. ");
        p.append("Generate exactly ").append(count).append(" original mental health assessment questions ");
        p.append("focused on: ").append(focus).append(". ");

        if (!context.isEmpty()) {
            p.append("Additional context: ").append(context).append(". ");
        }

        p.append("Each question must use this answer scale: ").append(scale).append(".\n\n");
        p.append("CRITICAL FORMAT RULES — follow exactly:\n");
        p.append("- Output ONLY the questions, nothing else.\n");
        p.append("- Number each question like: 1. [question text]\n");
        p.append("- After each question on the NEXT line write: SCALE: ").append(scale).append("\n");
        p.append("- Do NOT include explanations, headers, or any other text.\n");
        p.append("- Questions must be clear, clinically appropriate, and non-leading.\n");
        p.append("- Questions should be in first-person (e.g. 'I feel...', 'Over the past week...').\n\n");
        p.append("Example format:\n");
        p.append("1. Over the past two weeks, how often have you felt little interest or pleasure in doing things?\n");
        p.append("SCALE: Never/Rarely/Sometimes/Often/Always\n");
        p.append("2. How frequently have you felt down, depressed, or hopeless?\n");
        p.append("SCALE: Never/Rarely/Sometimes/Often/Always\n\n");
        p.append("Now generate ").append(count).append(" questions:");

        return p.toString();
    }

    // ═══════════════════════════════════════════════════════════════
    //  PARSING
    // ═══════════════════════════════════════════════════════════════

    private List<ParsedQuestion> parseGeminiResponse(String response, String defaultScale) {
        List<ParsedQuestion> list = new ArrayList<>();
        String[] lines = response.split("\n");

        String currentQuestion = null;
        String currentScale = defaultScale;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // Detect numbered question lines: "1." "2." etc.
            if (line.matches("^\\d+\\.\\s+.+")) {
                // Save previous question if any
                if (currentQuestion != null && !currentQuestion.isEmpty()) {
                    list.add(new ParsedQuestion(currentQuestion.trim(), currentScale));
                }
                // Strip leading number
                currentQuestion = line.replaceFirst("^\\d+\\.\\s*", "").trim();
                currentScale = defaultScale; // reset scale for new question
            } else if (line.toUpperCase().startsWith("SCALE:")) {
                currentScale = line.substring(6).trim();
            } else if (currentQuestion != null) {
                // Continuation of question text
                currentQuestion += " " + line;
            }
        }

        // Don't forget the last one
        if (currentQuestion != null && !currentQuestion.isEmpty()) {
            list.add(new ParsedQuestion(currentQuestion.trim(), currentScale));
        }

        return list;
    }

    // ═══════════════════════════════════════════════════════════════
    //  SAVE
    // ═══════════════════════════════════════════════════════════════

    private void saveSelectedQuestions(Stage dialog) {
        List<Question> toSave = new ArrayList<>();

        for (GeneratedQuestionRow row : rows) {
            if (row.isSelected()) {
                String text  = row.getQuestionText().trim();
                String scale = row.getScale().trim();

                if (text.isEmpty()) continue;

                Question q = new Question();
                q.setAssessmentId(assessment.getAssessmentId());
                q.setText(text);
                q.setScale(scale);
                toSave.add(q);
            }
        }

        if (toSave.isEmpty()) {
            showAlert("No Questions Selected",
                    "Please select at least one question to save.", Alert.AlertType.WARNING);
            return;
        }

        int saved = 0;
        List<String> failed = new ArrayList<>();

        for (Question q : toSave) {
            try {
                questionService.createQuestion(q);
                saved++;
            } catch (SQLException e) {
                failed.add(q.getText().substring(0, Math.min(40, q.getText().length())) + "...");
            }
        }

        String msg = saved + " question(s) saved successfully!";
        if (!failed.isEmpty()) {
            msg += "\n\nFailed to save " + failed.size() + " question(s).";
        }

        showAlert("Save Complete", msg, Alert.AlertType.INFORMATION);

        if (onSaveCallback != null) onSaveCallback.run();

        if (failed.isEmpty()) dialog.close();
    }

    // ═══════════════════════════════════════════════════════════════
    //  INNER: Row representing one generated question
    // ═══════════════════════════════════════════════════════════════

    private class GeneratedQuestionRow {
        private final int number;
        private final ParsedQuestion parsed;

        private CheckBox checkBox;
        private TextField questionField;
        private TextField scaleField;

        GeneratedQuestionRow(int number, ParsedQuestion parsed) {
            this.number = number;
            this.parsed = parsed;
        }

        VBox build() {
            VBox card = new VBox(10);
            card.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                            "-fx-border-radius: 8;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 15;"
            );

            // Header row: checkbox + number + AI badge
            checkBox = new CheckBox();
            checkBox.setSelected(true); // default: all selected

            Label numLabel = new Label("Q" + number);
            numLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            numLabel.setTextFill(Color.web(toHex(ACCENT_GREEN)));

            Label aiBadge = new Label("✨ AI Generated");
            aiBadge.setFont(Font.font("Segoe UI", 11));
            aiBadge.setStyle(
                    "-fx-background-color: #e8f4ec;" +
                            "-fx-text-fill: #" + toHex(ACCENT_GREEN) + ";" +
                            "-fx-border-color: #" + toHex(ACCENT_GREEN) + ";" +
                            "-fx-border-radius: 10;" +
                            "-fx-background-radius: 10;" +
                            "-fx-padding: 2 8;"
            );

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox headerRow = new HBox(10);
            headerRow.setAlignment(Pos.CENTER_LEFT);
            headerRow.getChildren().addAll(checkBox, numLabel, spacer, aiBadge);

            // Question text field (editable)
            Label qLbl = new Label("Question text:");
            qLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            qLbl.setTextFill(Color.web(toHex(TEXT_DARK)));

            questionField = new TextField(parsed.text);
            questionField.setFont(Font.font("Segoe UI", 13));
            questionField.setPrefWidth(680);
            questionField.setStyle(
                    "-fx-background-color: #f9f9f9;" +
                            "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                            "-fx-border-radius: 5;" +
                            "-fx-background-radius: 5;" +
                            "-fx-padding: 8;"
            );

            // Scale field (editable)
            Label sLbl = new Label("Answer scale:");
            sLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            sLbl.setTextFill(Color.web(toHex(TEXT_DARK)));

            scaleField = new TextField(parsed.scale);
            scaleField.setFont(Font.font("Segoe UI", 13));
            scaleField.setStyle(
                    "-fx-background-color: #f9f9f9;" +
                            "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                            "-fx-border-radius: 5;" +
                            "-fx-background-radius: 5;" +
                            "-fx-padding: 8;"
            );

            // Dim card when unchecked
            checkBox.selectedProperty().addListener((obs, old, selected) -> {
                card.setOpacity(selected ? 1.0 : 0.45);
                questionField.setEditable(selected);
                scaleField.setEditable(selected);
            });

            card.getChildren().addAll(headerRow, qLbl, questionField, sLbl, scaleField);
            return card;
        }

        boolean isSelected()      { return checkBox != null && checkBox.isSelected(); }
        String getQuestionText()  { return questionField != null ? questionField.getText() : parsed.text; }
        String getScale()         { return scaleField   != null ? scaleField.getText()    : parsed.scale; }
    }

    // ═══════════════════════════════════════════════════════════════
    //  INNER: Simple data holder for a parsed question
    // ═══════════════════════════════════════════════════════════════

    private static class ParsedQuestion {
        final String text;
        final String scale;
        ParsedQuestion(String text, String scale) {
            this.text  = text;
            this.scale = scale;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  UI HELPERS
    // ═══════════════════════════════════════════════════════════════

    private VBox card() {
        VBox box = new VBox();
        box.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 20;"
        );
        return box;
    }

    private Label fieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lbl.setTextFill(Color.web(toHex(TEXT_DARK)));
        lbl.setMinWidth(200);
        return lbl;
    }

    private void styleCombo(ComboBox<?> box) {
        box.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;"
        );
        box.setPrefWidth(200);
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
                (int)(color.getRed()   * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue()  * 255));
    }
}