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

public class AIQuestionGeneratorDialog {

    private static final Color PAGE_BG = Color.web("#F8F9FA");
    private static final Color EMERALD = Color.web("#50C878");
    private static final Color EMERALD_DARK = Color.web("#2E7D32");
    private static final Color EMERALD_MID = Color.web("#3A9B5E");
    private static final Color INK = Color.web("#1A3C34");
    private static final Color MUTED = Color.web("#6C757D");
    private static final Color LINE = Color.web("#E9ECEF");
    private static final Color WARNING = Color.web("#F39C12");
    private static final Color DANGER = Color.web("#E74C3C");

    private final Assessment assessment;
    private final QuestionController questionService;
    private final Runnable onSaveCallback;

    private VBox questionsContainer;
    private Label statusLabel;
    private Button generateBtn;
    private Button saveBtn;

    private final List<GeneratedQuestionRow> rows = new ArrayList<>();

    public AIQuestionGeneratorDialog(Assessment assessment,
                                     QuestionController questionService,
                                     Runnable onSaveCallback) {
        this.assessment = assessment;
        this.questionService = questionService;
        this.onSaveCallback = onSaveCallback;
    }

    public void show() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("AI Question Generator - " + assessment.getTitle());
        dialog.setMinWidth(820);
        dialog.setMinHeight(720);

        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: " + css(PAGE_BG) + ";");
        root.setPadding(new Insets(30));

        Label title = new Label("🤖 AI Question Generator");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 30));
        title.setTextFill(EMERALD_DARK);

        Label subtitle = new Label("Generate questions for: " + assessment.getTitle());
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        subtitle.setTextFill(MUTED);

        VBox controlsCard = card();
        controlsCard.setSpacing(16);

        HBox countRow = new HBox(15);
        countRow.setAlignment(Pos.CENTER_LEFT);
        Label countLbl = fieldLabel("Number of questions:");
        ComboBox<String> countBox = new ComboBox<>();
        countBox.getItems().addAll("3", "5", "7", "10");
        countBox.setValue("5");
        styleCombo(countBox);
        countRow.getChildren().addAll(countLbl, countBox);

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
        typeBox.setPrefWidth(300);
        typeRow.getChildren().addAll(typeLbl, typeBox);

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
        scaleBox.setPrefWidth(380);
        scaleRow.getChildren().addAll(scaleLbl, scaleBox);

        HBox contextRow = new HBox(15);
        contextRow.setAlignment(Pos.CENTER_LEFT);
        Label ctxLbl = fieldLabel("Extra context (optional):");
        TextField contextField = new TextField();
        contextField.setPromptText("e.g. for university students, ages 18-25");
        contextField.setPrefWidth(380);
        contextField.setStyle(pillInputStyle());
        contextRow.getChildren().addAll(ctxLbl, contextField);

        generateBtn = primaryButton("✨ Generate Questions with AI");
        statusLabel = new Label("");
        statusLabel.setFont(Font.font("Segoe UI", 13));
        statusLabel.setTextFill(MUTED);

        HBox genRow = new HBox(18, generateBtn, statusLabel);
        genRow.setAlignment(Pos.CENTER_LEFT);

        controlsCard.getChildren().addAll(countRow, typeRow, scaleRow, contextRow, genRow);

        Label reviewTitle = new Label("📋 Review Generated Questions");
        reviewTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        reviewTitle.setTextFill(EMERALD_DARK);

        Label reviewHint = new Label("Check the questions you want to keep. You can edit the text and scale before saving.");
        reviewHint.setFont(Font.font("Segoe UI", 13));
        reviewHint.setTextFill(MUTED);

        questionsContainer = new VBox(12);
        questionsContainer.setPadding(new Insets(14));
        questionsContainer.setStyle("-fx-background-color: white; -fx-background-radius: 20;" + softShadow());

        VBox placeholder = new VBox(10);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setPadding(new Insets(42));
        Label icon = new Label("💡");
        icon.setFont(Font.font("Segoe UI Emoji", 42));
        Label placeholderText = new Label("Generated questions will appear here after clicking Generate");
        placeholderText.setFont(Font.font("Segoe UI", 14));
        placeholderText.setTextFill(MUTED);
        placeholder.getChildren().addAll(icon, placeholderText);
        questionsContainer.getChildren().add(placeholder);

        saveBtn = primaryButton("💾 Save Selected Questions");
        saveBtn.setDisable(true);

        Button cancelBtn = outlineButton("Cancel");
        cancelBtn.setOnAction(e -> dialog.close());

        HBox bottomBar = new HBox(15, cancelBtn, saveBtn);
        bottomBar.setAlignment(Pos.CENTER_RIGHT);

        generateBtn.setOnAction(e -> {
            int count = Integer.parseInt(countBox.getValue());
            String focus = typeBox.getValue();
            String scale = scaleBox.getValue();
            String context = contextField.getText().trim();
            generateQuestions(count, focus, scale, context);
        });

        saveBtn.setOnAction(e -> saveSelectedQuestions(dialog));

        root.getChildren().addAll(title, subtitle, controlsCard, reviewTitle, reviewHint, questionsContainer, bottomBar);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: " + css(PAGE_BG) + "; -fx-border-color: transparent;");

        dialog.setScene(new Scene(scroll, 820, 720));
        dialog.showAndWait();
    }

    private void generateQuestions(int count, String focus, String scale, String context) {
        generateBtn.setDisable(true);
        saveBtn.setDisable(true);
        statusLabel.setText("⏳ Asking Gemini to generate questions...");
        statusLabel.setTextFill(MUTED);
        rows.clear();
        questionsContainer.getChildren().clear();

        new Thread(() -> {
            try {
                String prompt = buildPrompt(count, focus, scale, context);
                String response = GeminiService.generateContent(prompt);
                List<ParsedQuestion> parsed = parseGeminiResponse(response, scale);

                javafx.application.Platform.runLater(() -> {
                    if (parsed.isEmpty()) {
                        statusLabel.setText("⚠ Could not parse questions. Try again.");
                        statusLabel.setTextFill(WARNING);
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

                    statusLabel.setText("✅ " + parsed.size() + " questions generated!");
                    statusLabel.setTextFill(EMERALD_DARK);
                    generateBtn.setDisable(false);
                    saveBtn.setDisable(false);
                });

            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("❌ Gemini error: " + ex.getMessage());
                    statusLabel.setTextFill(DANGER);
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

        if (!context.isEmpty()) p.append("Additional context: ").append(context).append(". ");

        p.append("Each question must use this answer scale: ").append(scale).append(".\n\n");
        p.append("CRITICAL FORMAT RULES — follow exactly:\n");
        p.append("- Output ONLY the questions, nothing else.\n");
        p.append("- Number each question like: 1. [question text]\n");
        p.append("- After each question on the NEXT line write: SCALE: ").append(scale).append("\n");
        p.append("- Do NOT include explanations, headers, or any other text.\n");
        p.append("- Questions must be clear, clinically appropriate, and non-leading.\n");
        p.append("- Questions should be in first-person.\n\n");
        p.append("Now generate ").append(count).append(" questions:");

        return p.toString();
    }

    private List<ParsedQuestion> parseGeminiResponse(String response, String defaultScale) {
        List<ParsedQuestion> list = new ArrayList<>();
        String[] lines = response.split("\n");

        String currentQuestion = null;
        String currentScale = defaultScale;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.matches("^\\d+\\.\\s+.+")) {
                if (currentQuestion != null && !currentQuestion.isEmpty()) {
                    list.add(new ParsedQuestion(currentQuestion.trim(), currentScale));
                }
                currentQuestion = line.replaceFirst("^\\d+\\.\\s*", "").trim();
                currentScale = defaultScale;
            } else if (line.toUpperCase().startsWith("SCALE:")) {
                currentScale = line.substring(6).trim();
            } else if (currentQuestion != null) {
                currentQuestion += " " + line;
            }
        }

        if (currentQuestion != null && !currentQuestion.isEmpty()) {
            list.add(new ParsedQuestion(currentQuestion.trim(), currentScale));
        }

        return list;
    }

    private void saveSelectedQuestions(Stage dialog) {
        List<Question> toSave = new ArrayList<>();

        for (GeneratedQuestionRow row : rows) {
            if (row.isSelected()) {
                String text = row.getQuestionText().trim();
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
            showAlert("No Questions Selected", "Please select at least one question to save.", Alert.AlertType.WARNING);
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
        if (!failed.isEmpty()) msg += "\n\nFailed to save " + failed.size() + " question(s).";

        showAlert("Save Complete", msg, Alert.AlertType.INFORMATION);

        if (onSaveCallback != null) onSaveCallback.run();
        if (failed.isEmpty()) dialog.close();
    }

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
            card.setPadding(new Insets(16));
            card.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 16; -fx-border-color: #E9ECEF; -fx-border-radius: 16;");

            checkBox = new CheckBox();
            checkBox.setSelected(true);

            Label numLabel = badge("Q" + number, EMERALD, Color.WHITE);
            Label aiBadge = badge("✨ AI Generated", Color.web("#E8F5E9"), EMERALD_DARK);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox headerRow = new HBox(10, checkBox, numLabel, spacer, aiBadge);
            headerRow.setAlignment(Pos.CENTER_LEFT);

            Label qLbl = fieldSmallLabel("Question text:");
            questionField = new TextField(parsed.text);
            questionField.setFont(Font.font("Segoe UI", 13));
            questionField.setPrefWidth(700);
            questionField.setStyle(pillInputStyle());

            Label sLbl = fieldSmallLabel("Answer scale:");
            scaleField = new TextField(parsed.scale);
            scaleField.setFont(Font.font("Segoe UI", 13));
            scaleField.setStyle(pillInputStyle());

            checkBox.selectedProperty().addListener((obs, old, selected) -> {
                card.setOpacity(selected ? 1.0 : 0.45);
                questionField.setEditable(selected);
                scaleField.setEditable(selected);
            });

            card.getChildren().addAll(headerRow, qLbl, questionField, sLbl, scaleField);
            return card;
        }

        boolean isSelected() { return checkBox != null && checkBox.isSelected(); }
        String getQuestionText() { return questionField != null ? questionField.getText() : parsed.text; }
        String getScale() { return scaleField != null ? scaleField.getText() : parsed.scale; }
    }

    private static class ParsedQuestion {
        final String text;
        final String scale;
        ParsedQuestion(String text, String scale) {
            this.text = text;
            this.scale = scale;
        }
    }

    private VBox card() {
        VBox box = new VBox();
        box.setPadding(new Insets(22));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 24; -fx-border-radius: 24;" + softShadow());
        return box;
    }

    private Label fieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lbl.setTextFill(EMERALD_DARK);
        lbl.setMinWidth(210);
        return lbl;
    }

    private Label fieldSmallLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lbl.setTextFill(MUTED);
        return lbl;
    }

    private void styleCombo(ComboBox<?> box) {
        box.setStyle(pillInputStyle());
        box.setPrefWidth(220);
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
        return button;
    }

    private Label badge(String text, Color bg, Color fg) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        label.setTextFill(fg);
        label.setPadding(new Insets(6, 13, 6, 13));
        label.setStyle("-fx-background-color: " + css(bg) + "; -fx-background-radius: 999;");
        return label;
    }

    private String pillInputStyle() {
        return "-fx-background-color: white;" +
                "-fx-background-radius: 999;" +
                "-fx-border-radius: 999;" +
                "-fx-border-color: #CED4DA;" +
                "-fx-border-width: 1.5;" +
                "-fx-padding: 10 16;" +
                "-fx-font-family: 'Segoe UI';";
    }

    private String softShadow() {
        return "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 18, 0, 0, 8);";
    }

    private String gradient(Color left, Color right) {
        return "linear-gradient(to bottom right, " + css(left) + ", " + css(right) + ")";
    }

    private String css(Color color) {
        return "#" + toHex(color);
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
