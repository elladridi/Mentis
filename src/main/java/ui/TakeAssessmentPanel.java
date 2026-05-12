package ui;

import controller.AssessmentController;
import controller.AssessmentResultController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.AdaptiveAssessmentSession;
import models.Assessment;
import models.AssessmentResult;
import models.Question;
import services.AdaptiveQuestionService;
import services.GeolocationService;
import services.YouTubeRecommendationService;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.*;

public class TakeAssessmentPanel extends VBox {

    private int userId;
    private MentisLoginFrame parentApp;
    private AssessmentController assessmentController;
    private AssessmentResultController resultController;
    private AdaptiveQuestionService adaptiveService;

    private List<Assessment> availableAssessments;
    private List<Question> currentQuestions;
    private List<Question> originalQuestions;
    private Map<Integer, String> answers;

    private int currentQuestionIndex = 0;
    private int currentAssessmentId = 0;

    private AdaptiveAssessmentSession adaptiveSession;
    private boolean useAdaptiveMode = true;
    private static final int MAX_ADAPTIVE_QUESTIONS = 8;
    private boolean adaptiveEnabled = true;
    private boolean isAdapting = false;

    private StackPane contentStack;
    private VBox selectionPanel;
    private VBox questionPanel;
    private GridPane cardsGrid;
    private Label questionNumberLabel;
    private Label questionTextLabel;
    private ComboBox<String> answerCombo;
    private Button prevButton;
    private Button nextButton;
    private Button submitButton;
    private ImageView assessmentImageView;
    private TextField searchField;
    private ProgressBar questionProgressBar;
    private Label progressMetaLabel;
    private Label calmHintLabel;

    private Map<Integer, Image> assessmentImages = new HashMap<>();

    private static final Color PAGE_BG = Color.web("#F8F9FA");
    private static final Color SOFT_GREEN_BG = Color.web("#F1F8E9");
    private static final Color EMERALD = Color.web("#50C878");
    private static final Color EMERALD_DARK = Color.web("#2E7D32");
    private static final Color EMERALD_MID = Color.web("#3A9B5E");
    private static final Color INK = Color.web("#1A3C34");
    private static final Color MUTED = Color.web("#6C757D");
    private static final Color LINE = Color.web("#E9ECEF");
    private static final Color DANGER = Color.web("#E74C3C");
    private static final Color WARNING = Color.web("#F39C12");

    private static final Color TYPE_DEPRESSION = Color.rgb(144, 127, 201);
    private static final Color TYPE_ANXIETY = Color.rgb(227, 149, 149);
    private static final Color TYPE_STRESS = Color.rgb(227, 206, 163);
    private static final Color TYPE_WELLNESS = Color.web("#50C878");
    private static final Color TYPE_GENERAL = Color.web("#74B9FF");
    private static final Color TYPE_DEFAULT = Color.web("#74B9FF");

    public TakeAssessmentPanel(MentisLoginFrame parentApp,
                               AssessmentController assessmentController,
                               AssessmentResultController resultController) {
        this.parentApp = parentApp;
        this.assessmentController = assessmentController;
        this.resultController = resultController;
        this.answers = new HashMap<>();
        this.userId = parentApp.getUserId();
        this.adaptiveService = new AdaptiveQuestionService();

        setStyle("-fx-background-color: " + cssColor(PAGE_BG) + ";");
        setPadding(new Insets(0));
        setSpacing(0);
        VBox.setVgrow(this, Priority.ALWAYS);

        contentStack = new StackPane();
        contentStack.setStyle("-fx-background-color: " + cssColor(PAGE_BG) + ";");
        VBox.setVgrow(contentStack, Priority.ALWAYS);

        createSelectionPanel();
        createQuestionPanel();

        contentStack.getChildren().addAll(selectionPanel, questionPanel);
        showSelectionPanel();

        getChildren().add(contentStack);
    }

    private void createSelectionPanel() {
        selectionPanel = new VBox(28);
        selectionPanel.setPadding(new Insets(46, 56, 46, 56));
        selectionPanel.setStyle("-fx-background-color: " + cssColor(PAGE_BG) + ";");

        VBox hero = new VBox(10);
        hero.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Take Assessment");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 46));
        titleLabel.setTextFill(EMERALD_DARK);

        Label subtitle = new Label("Choose an assessment to begin your mental wellness journey");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 18));
        subtitle.setTextFill(MUTED);

        Button resultsLink = createSymfonyOutlineButton("📊 Results");
        resultsLink.setOnAction(e -> parentApp.showResultsPanel());

        hero.getChildren().addAll(titleLabel, subtitle, resultsLink);

        HBox filtersCard = new HBox(14);
        filtersCard.setAlignment(Pos.CENTER);
        filtersCard.setPadding(new Insets(18, 22, 18, 22));
        filtersCard.setMaxWidth(980);
        filtersCard.setStyle(glassCardStyle());

        searchField = new TextField();
        searchField.setPromptText("🔍 Search title, description, type...");
        searchField.setPrefWidth(430);
        searchField.setStyle(pillInputStyle());
        searchField.setOnAction(e -> searchAssessments(searchField.getText().trim()));

        Button searchButton = createSymfonyPrimaryButton("Filter");
        searchButton.setOnAction(e -> searchAssessments(searchField.getText().trim()));

        Button clearButton = createSymfonyOutlineButton("Clear");
        clearButton.setOnAction(e -> {
            searchField.clear();
            refreshData();
        });

        filtersCard.getChildren().addAll(searchField, searchButton, clearButton);

        cardsGrid = new GridPane();
        cardsGrid.setHgap(26);
        cardsGrid.setVgap(26);
        cardsGrid.setPadding(new Insets(8, 0, 8, 0));
        cardsGrid.setAlignment(Pos.TOP_CENTER);
        cardsGrid.setStyle("-fx-background-color: transparent;");

        ScrollPane scrollPane = cleanScrollPane(cardsGrid);

        selectionPanel.getChildren().addAll(hero, filtersCard, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private void createQuestionPanel() {
        questionPanel = new VBox(22);
        questionPanel.setPadding(new Insets(34, 56, 34, 56));
        questionPanel.setStyle("-fx-background-color: " + cssColor(PAGE_BG) + ";");
        questionPanel.setVisible(false);

        Button backButton = createSymfonyOutlineButton("← Back to Assessments");
        backButton.setTextFill(EMERALD);
        backButton.setOnAction(e -> showSelectionPanel());

        progressMetaLabel = new Label("❔ Question 1 of 1");
        progressMetaLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        progressMetaLabel.setTextFill(MUTED);

        calmHintLabel = new Label("🕒 Take your time");
        calmHintLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        calmHintLabel.setTextFill(MUTED);

        Region metaSpacer = new Region();
        HBox progressMeta = new HBox(progressMetaLabel, metaSpacer, calmHintLabel);
        progressMeta.setAlignment(Pos.CENTER);
        HBox.setHgrow(metaSpacer, Priority.ALWAYS);

        questionProgressBar = new ProgressBar(0);
        questionProgressBar.setMaxWidth(Double.MAX_VALUE);
        questionProgressBar.setPrefHeight(10);
        questionProgressBar.setStyle(
                "-fx-accent: " + cssColor(EMERALD) + ";" +
                        "-fx-background-radius: 999;" +
                        "-fx-control-inner-background: #E9ECEF;"
        );

        VBox topBlock = new VBox(14, backButton, progressMeta, questionProgressBar);
        topBlock.setMaxWidth(920);
        topBlock.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(0);
        card.setMaxWidth(860);
        card.setMinHeight(500);
        card.setStyle(glassCardStyle() + "-fx-padding: 0;");
        card.setAlignment(Pos.TOP_CENTER);

        StackPane imageHolder = new StackPane();
        imageHolder.setPrefHeight(220);
        imageHolder.setMaxWidth(Double.MAX_VALUE);
        imageHolder.setStyle(
                "-fx-background-color: " + gradient(EMERALD, EMERALD_DARK) + ";" +
                        "-fx-background-radius: 20 20 0 0;"
        );

        assessmentImageView = new ImageView();
        assessmentImageView.setFitHeight(220);
        assessmentImageView.setFitWidth(860);
        assessmentImageView.setPreserveRatio(false);
        imageHolder.getChildren().add(assessmentImageView);

        VBox body = new VBox(22);
        body.setPadding(new Insets(30, 42, 34, 42));
        body.setAlignment(Pos.CENTER);

        questionNumberLabel = new Label("Question 1 of 1");
        questionNumberLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        questionNumberLabel.setTextFill(MUTED);

        questionTextLabel = new Label("Loading...");
        questionTextLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 22));
        questionTextLabel.setTextFill(EMERALD_DARK);
        questionTextLabel.setWrapText(true);
        questionTextLabel.setTextAlignment(TextAlignment.CENTER);
        questionTextLabel.setAlignment(Pos.CENTER);
        questionTextLabel.setMaxWidth(720);
        questionTextLabel.setMinHeight(92);

        answerCombo = new ComboBox<>();
        answerCombo.setPrefWidth(520);
        answerCombo.setPrefHeight(48);
        answerCombo.setPromptText("Select your answer...");
        answerCombo.setStyle(pillInputStyle());

        body.getChildren().addAll(questionNumberLabel, questionTextLabel, answerCombo);
        card.getChildren().addAll(imageHolder, body);

        HBox tipCard = new HBox(14);
        tipCard.setAlignment(Pos.CENTER_LEFT);
        tipCard.setMaxWidth(860);
        tipCard.setPadding(new Insets(16, 20, 16, 20));
        tipCard.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        softShadow()
        );

        Label bulb = createIconCircle("💡", WARNING, Color.web("#F1C40F"), 46);
        VBox tipText = new VBox(2);
        Label tipTitle = new Label("Tip");
        tipTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        tipTitle.setTextFill(INK);
        Label tipBody = new Label("Answer honestly for the most accurate results. There are no right or wrong answers.");
        tipBody.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        tipBody.setTextFill(MUTED);
        tipBody.setWrapText(true);
        tipText.getChildren().addAll(tipTitle, tipBody);
        tipCard.getChildren().addAll(bulb, tipText);

        HBox navPanel = new HBox(14);
        navPanel.setAlignment(Pos.CENTER);

        prevButton = createSymfonyOutlineButton("← Previous");
        prevButton.setDisable(true);
        prevButton.setOnAction(e -> showPreviousQuestion());

        nextButton = createSymfonyPrimaryButton("Next →");
        nextButton.setDisable(true);
        nextButton.setOnAction(e -> showNextQuestion());

        submitButton = createSymfonyPrimaryButton("✓ Submit & Analyze");
        submitButton.setDisable(true);
        submitButton.setOnAction(e -> submitAssessment());

        navPanel.getChildren().addAll(prevButton, nextButton, submitButton);

        VBox centered = new VBox(24, topBlock, card, tipCard, navPanel);
        centered.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(centered, Priority.ALWAYS);

        questionPanel.getChildren().add(centered);
    }

    private VBox createAssessmentCard(Assessment assessment) {
        VBox card = new VBox(0);
        card.setPrefWidth(330);
        card.setPrefHeight(430);
        card.setMaxHeight(430);
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setStyle(glassCardStyle());

        StackPane imagePanel = new StackPane();
        imagePanel.setPrefSize(330, 200);
        imagePanel.setMaxHeight(200);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(330);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(false);

        Image icon = assessmentImages.get(assessment.getAssessmentId());

        if (icon != null) {
            imageView.setImage(icon);
            imagePanel.getChildren().add(imageView);
        } else {
            imagePanel.setStyle(
                    "-fx-background-color: " + gradient(getTypeColor(assessment.getType()), getTypeColor(assessment.getType()).darker()) + ";" +
                            "-fx-background-radius: 20 20 0 0;"
            );

            Label brain = new Label("🧠");
            brain.setFont(Font.font("Segoe UI Emoji", FontWeight.BOLD, 54));
            brain.setTextFill(Color.WHITE);
            imagePanel.getChildren().add(brain);
        }

        Label typeBadge = createBadge(assessment.getType(), getTypeColor(assessment.getType()));
        StackPane.setAlignment(typeBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(typeBadge, new Insets(14));
        imagePanel.getChildren().add(typeBadge);

        VBox body = new VBox(8);
        body.setPadding(new Insets(18, 22, 18, 22));
        body.setAlignment(Pos.TOP_CENTER);
        body.setPrefHeight(230);
        body.setMaxHeight(230);
        body.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 0 0 20 20;");

        Label titleLabel = new Label(assessment.getTitle());
        titleLabel.setStyle("-fx-text-fill: #2E7D32; -fx-font-size: 18px; -fx-font-weight: bold;");
        titleLabel.setWrapText(true);
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMaxWidth(286);

        String description = assessment.getDescription();
        Label descLabel = new Label(
                description == null || description.isBlank()
                        ? "Start this wellness check and receive personalized insights."
                        : description.length() > 80 ? description.substring(0, 77) + "..." : description
        );
        descLabel.setStyle("-fx-text-fill: #6C757D; -fx-font-size: 13px;");
        descLabel.setWrapText(true);
        descLabel.setTextAlignment(TextAlignment.CENTER);
        descLabel.setAlignment(Pos.CENTER);
        descLabel.setMaxWidth(286);

        Label brand = new Label("🌿 Mentis Assessment");
        brand.setStyle("-fx-text-fill: #6C757D; -fx-font-size: 12px;");

        Button takeTestBtn = createSymfonyPrimaryButton("▶ Start Assessment");
        takeTestBtn.setMaxWidth(Double.MAX_VALUE);
        takeTestBtn.setOnAction(e -> startAssessment(assessment));

        body.getChildren().addAll(titleLabel, descLabel, brand, takeTestBtn);

        card.getChildren().addAll(imagePanel, body);

        card.setOnMouseEntered(e -> card.setStyle(
                glassCardStyle() +
                        "-fx-translate-y: -8;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 28, 0, 0, 14);"
        ));

        card.setOnMouseExited(e -> card.setStyle(glassCardStyle()));

        return card;
    }

    private void searchAssessments(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            refreshData();
            return;
        }

        cardsGrid.getChildren().clear();

        if (availableAssessments == null || availableAssessments.isEmpty()) {
            showEmptyState(cardsGrid, "No assessments available");
            return;
        }

        String searchLower = searchText.toLowerCase().trim();
        List<Assessment> filteredAssessments = new ArrayList<>();

        for (Assessment assessment : availableAssessments) {
            String title = assessment.getTitle() == null ? "" : assessment.getTitle().toLowerCase();
            String description = assessment.getDescription() == null ? "" : assessment.getDescription().toLowerCase();
            String type = assessment.getType() == null ? "" : assessment.getType().toLowerCase();

            if (title.contains(searchLower) || description.contains(searchLower) || type.contains(searchLower)) {
                filteredAssessments.add(assessment);
            }
        }

        if (filteredAssessments.isEmpty()) {
            showEmptyState(cardsGrid, "No matching assessments found");
            return;
        }

        displayCards(filteredAssessments);
    }

    public void refreshData() {
        try {
            availableAssessments = assessmentController.getActiveAssessments();
            assessmentImages.clear();
            displayAssessmentCards();
        } catch (Exception e) {
            showAlert("Error", "Error loading assessments: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void displayAssessmentCards() {
        cardsGrid.getChildren().clear();

        if (availableAssessments == null || availableAssessments.isEmpty()) {
            showEmptyState(cardsGrid, "No assessments available");
            return;
        }

        for (Assessment assessment : availableAssessments) {
            loadAssessmentImage(assessment);
        }

        displayCards(availableAssessments);
    }

    private void displayCards(List<Assessment> assessments) {
        cardsGrid.getChildren().clear();
        int col = 0;
        int row = 0;

        for (Assessment assessment : assessments) {
            if (!assessmentImages.containsKey(assessment.getAssessmentId())) {
                loadAssessmentImage(assessment);
            }

            VBox card = createAssessmentCard(assessment);
            cardsGrid.add(card, col, row);

            col++;
            if (col >= 3) {
                col = 0;
                row++;
            }
        }
    }

    private void loadAssessmentImage(Assessment assessment) {
        if (assessment.getImagePath() != null && !assessment.getImagePath().isEmpty()) {
            try {
                File imgFile = new File(assessment.getImagePath());
                if (imgFile.exists()) {
                    Image image = new Image(new FileInputStream(imgFile));
                    assessmentImages.put(assessment.getAssessmentId(), image);
                }
            } catch (Exception e) {
                System.err.println("Error loading image for assessment " + assessment.getTitle() + ": " + e.getMessage());
            }
        }
    }

    private void loadAndDisplayAssessmentImage(Assessment assessment) {
        if (assessmentImageView == null) return;

        if (assessment.getImagePath() != null && !assessment.getImagePath().isEmpty()) {
            try {
                File imgFile = new File(assessment.getImagePath());
                if (imgFile.exists()) {
                    Image image = new Image(new FileInputStream(imgFile));
                    assessmentImageView.setImage(image);
                    assessmentImageView.setFitWidth(860);
                    assessmentImageView.setFitHeight(220);
                    assessmentImageView.setPreserveRatio(false);
                    return;
                }
            } catch (Exception e) {
                System.err.println("Error loading assessment image: " + e.getMessage());
            }
        }

        assessmentImageView.setImage(null);
        assessmentImageView.setStyle(
                "-fx-background-color: " + gradient(getTypeColor(assessment.getType()), getTypeColor(assessment.getType()).darker()) + ";"
        );
    }

    private void startAssessment(Assessment assessment) {
        try {
            if (userId <= 0) {
                showAlert("Authentication Error", "User ID not found. Please login again.", Alert.AlertType.WARNING);
                return;
            }

            currentAssessmentId = assessment.getAssessmentId();
            originalQuestions = resultController.getQuestionsByAssessment(currentAssessmentId);

            if (originalQuestions == null || originalQuestions.isEmpty()) {
                showAlert("Error", "No questions found for this assessment!", Alert.AlertType.ERROR);
                return;
            }

            adaptiveSession = new AdaptiveAssessmentSession(currentAssessmentId, userId, originalQuestions);
            adaptiveSession.setUseAIAdaptive(useAdaptiveMode);
            adaptiveEnabled = true;
            isAdapting = false;

            currentQuestions = new ArrayList<>(originalQuestions);
            currentQuestionIndex = 0;
            answers.clear();

            Collections.shuffle(currentQuestions);

            prevButton.setDisable(true);
            nextButton.setDisable(currentQuestions.size() <= 1);
            submitButton.setDisable(true);

            loadAndDisplayAssessmentImage(assessment);
            showQuestion(currentQuestionIndex);
            showQuestionPanel();

        } catch (Exception e) {
            showAlert("Error", "Error starting assessment: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private Question getNextAdaptiveQuestion() {
        if (adaptiveSession == null || originalQuestions == null || !adaptiveEnabled) return null;

        if (adaptiveSession.getQuestionCount() >= MAX_ADAPTIVE_QUESTIONS) {
            adaptiveEnabled = false;
            return null;
        }

        List<Question> asked = adaptiveSession.getAskedQuestions();
        List<Question> remaining = new ArrayList<>(originalQuestions);
        remaining.removeAll(asked);

        if (remaining.isEmpty()) return null;

        return adaptiveService.getNextAdaptiveQuestion(adaptiveSession, remaining);
    }

    private void showQuestion(int index) {
        if (currentQuestions == null || index < 0 || index >= currentQuestions.size()) return;

        Question question = currentQuestions.get(index);

        questionNumberLabel.setText("Question " + (index + 1) + " of " + currentQuestions.size());
        updateQuestionProgress(index);

        questionTextLabel.setText(question.getText());
        questionTextLabel.setWrapText(true);
        questionTextLabel.autosize();

        String[] options = resultController.parseScaleToOptions(question.getScale());
        answerCombo.getItems().clear();
        answerCombo.getItems().addAll(options);

        if (answers.containsKey(question.getQuestionId())) {
            answerCombo.setValue(answers.get(question.getQuestionId()));
        } else {
            answerCombo.getSelectionModel().clearSelection();
            answerCombo.setPromptText("Select your answer...");
        }

        prevButton.setDisable(index <= 0);
        nextButton.setDisable(index >= currentQuestions.size() - 1);
        submitButton.setDisable(false);
    }

    private void updateQuestionProgress(int index) {
        if (currentQuestions == null || currentQuestions.isEmpty()) return;

        int total = currentQuestions.size();
        double progress = (index + 1.0) / total;

        if (questionProgressBar != null) questionProgressBar.setProgress(progress);
        if (progressMetaLabel != null) progressMetaLabel.setText("❔ Question " + (index + 1) + " of " + total);
    }

    private void saveCurrentAnswer() {
        if (currentQuestions != null && currentQuestionIndex < currentQuestions.size() && answerCombo.getValue() != null) {
            Question question = currentQuestions.get(currentQuestionIndex);
            answers.put(question.getQuestionId(), answerCombo.getValue());
        }
    }

    private void showPreviousQuestion() {
        if (currentQuestionIndex > 0) {
            saveCurrentAnswer();
            currentQuestionIndex--;
            showQuestion(currentQuestionIndex);
        }
    }

    private void showNextQuestion() {
        if (answerCombo.getValue() == null || answerCombo.getValue().isBlank()) {
            showAlert("Answer Required", "Please select your answer before continuing.", Alert.AlertType.WARNING);
            return;
        }

        saveCurrentAnswer();

        Question currentQ = currentQuestions.get(currentQuestionIndex);
        String answer = answers.get(currentQ.getQuestionId());
        int score = resultController.parseAnswerToScore(answer, currentQ.getScale());
        adaptiveSession.addAnsweredQuestion(currentQ, answer, score);

        if (useAdaptiveMode && adaptiveEnabled && currentQuestionIndex >= 1 && !isAdapting) {
            Question adaptiveQuestion = getNextAdaptiveQuestion();

            if (adaptiveQuestion != null && !adaptiveSession.getAskedQuestions().contains(adaptiveQuestion)
                    && !currentQuestions.contains(adaptiveQuestion)) {
                isAdapting = true;
                currentQuestions.add(currentQuestionIndex + 1, adaptiveQuestion);
                Platform.runLater(() -> isAdapting = false);
            }
        }

        if (currentQuestionIndex < currentQuestions.size() - 1) {
            currentQuestionIndex++;
            showQuestion(currentQuestionIndex);
        }
    }

    private void submitAssessment() {
        if (answerCombo.getValue() == null || answerCombo.getValue().isBlank()) {
            showAlert("Answer Required", "Please select your answer before submitting.", Alert.AlertType.WARNING);
            return;
        }

        saveCurrentAnswer();

        for (Question question : currentQuestions) {
            if (!answers.containsKey(question.getQuestionId())) {
                showAlert("Incomplete Assessment", "Please answer all questions before submitting!", Alert.AlertType.WARNING);
                return;
            }
        }

        if (adaptiveSession != null && adaptiveSession.getAskedQuestions().size() < currentQuestions.size()) {
            Question lastQ = currentQuestions.get(currentQuestionIndex);
            String answer = answers.get(lastQ.getQuestionId());
            int score = resultController.parseAnswerToScore(answer, lastQ.getScale());
            adaptiveSession.addAnsweredQuestion(lastQ, answer, score);
        }

        Map<Integer, Integer> answerScores = new HashMap<>();
        Map<Integer, String> originalAnswers = new HashMap<>();

        for (Question question : currentQuestions) {
            String answer = answers.get(question.getQuestionId());
            if (answer != null) {
                int score = resultController.parseAnswerToScore(answer, question.getScale());
                answerScores.put(question.getQuestionId(), score);
                originalAnswers.put(question.getQuestionId(), answer);
            }
        }

        try {
            Map<String, Object> result = resultController.submitAssessment(userId, currentAssessmentId, answerScores, originalAnswers);

            if (Boolean.TRUE.equals(result.get("success"))) {
                showResultsWithAI((Map<String, Object>) result.get("result"));
            } else {
                showAlert("Submission Error", "Error submitting assessment: " + result.get("error"), Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            showAlert("Error", "Error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showResultsWithAI(Map<String, Object> result) {
        Stage resultDialog = new Stage();
        resultDialog.initModality(Modality.APPLICATION_MODAL);
        resultDialog.setTitle("Assessment Results");
        resultDialog.setMinWidth(900);
        resultDialog.setMinHeight(760);

        BorderPane mainPanel = new BorderPane();
        mainPanel.setStyle("-fx-background-color: #F8F9FA;");
        mainPanel.setPadding(new Insets(18));

        TabPane tabbedPane = new TabPane();
        tabbedPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabbedPane.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-border-radius: 16;");

        Tab summaryTab = new Tab("📊 Summary");
        Tab analysisTab = new Tab("🤖 AI Analysis");
        Tab recommendationsTab = new Tab("💡 Recommendations");

        summaryTab.setContent(createSummaryTab(result));
        analysisTab.setContent(createAnalysisTab(result));
        recommendationsTab.setContent(createRecommendationsTab(result));

        tabbedPane.getTabs().addAll(summaryTab, analysisTab, recommendationsTab);
        mainPanel.setCenter(tabbedPane);

        String riskLevel = (String) result.get("riskLevel");
        boolean isCritical = isCriticalRisk(riskLevel);

        HBox buttonPanel = new HBox(14);
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.setStyle("-fx-background-color: #F8F9FA; -fx-padding: 18px;");

        Button exportTextBtn = createSymfonyPrimaryButton("Export Text");
        exportTextBtn.setOnAction(e -> exportAsText(result));

        Button exportHTMLBtn = createSymfonyPrimaryButton("Export HTML");
        exportHTMLBtn.setOnAction(e -> exportAsHTML(result));

        Button closeBtn = createSymfonyOutlineButton("Close");
        closeBtn.setOnAction(e -> {
            resultDialog.close();
            showSelectionPanel();
        });

        if (isCritical) {
            Button emergencyBtn = createEmergencyButton();
            emergencyBtn.setOnAction(e -> GeolocationService.checkAndShowEmergencyResources(riskLevel, null));
            buttonPanel.getChildren().addAll(emergencyBtn, exportTextBtn, exportHTMLBtn, closeBtn);

            Platform.runLater(() -> {
                Alert warn = new Alert(Alert.AlertType.WARNING);
                warn.setTitle("⚠️ CRITICAL RISK DETECTED");
                warn.setHeaderText("Please seek immediate support");
                warn.setContentText(
                        "Your results indicate a HIGH risk level.\n\n" +
                                "Click '🚨 EMERGENCY HELP' below to find nearby hospitals and mental health centers.\n\n" +
                                "Emergency Contacts:\n" +
                                "• 911 (US) / 112 (EU) — Emergency Services\n" +
                                "• 988 — Mental Health Crisis Line (US)\n" +
                                "• Text HOME to 741741 — Crisis Text Line"
                );
                warn.show();
            });
        } else {
            buttonPanel.getChildren().addAll(exportTextBtn, exportHTMLBtn, closeBtn);
        }

        mainPanel.setBottom(buttonPanel);

        Scene scene = new Scene(mainPanel, 900, 760);
        resultDialog.setScene(scene);
        resultDialog.showAndWait();
    }

    private VBox createSummaryTab(Map<String, Object> result) {
        VBox panel = new VBox(22);
        panel.setStyle("-fx-background-color: white;");
        panel.setPadding(new Insets(26));

        HBox cards = new HBox(16);
        cards.setAlignment(Pos.CENTER);
        cards.getChildren().addAll(
                createMetricCard("⭐", String.valueOf(result.get("totalScore")), "Total Score", EMERALD),
                createMetricCard("📈", String.valueOf(result.get("riskLevel")), "Risk Level", riskColor(String.valueOf(result.get("riskLevel")))),
                createMetricCard("📅", "Today", "Date Taken", EMERALD)
        );

        VBox details = createTextSection("Assessment Details", buildSummaryText(result));
        VBox interpretation = createTextSection("Interpretation", String.valueOf(result.get("interpretation")));

        panel.getChildren().addAll(cards, details, interpretation);
        return panel;
    }

    private VBox createAnalysisTab(Map<String, Object> result) {
        VBox panel = new VBox(16);
        panel.setStyle("-fx-background-color: white;");
        panel.setPadding(new Insets(26));

        Label title = new Label("🤖 AI-Powered Analysis");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setTextFill(EMERALD_DARK);

        String rawAnalysis = result.get("aiAnalysis") == null ? "AI analysis was not generated for this result." : result.get("aiAnalysis").toString();
        String cleanedAnalysis = rawAnalysis
                .replace("**", "")
                .replace("###", "")
                .replace("##", "")
                .replace("# ", "")
                .replace("•", "→");

        TextArea textArea = new TextArea(cleanedAnalysis);
        textArea.setEditable(false);
        textArea.setFont(Font.font("Segoe UI", 14));
        textArea.setWrapText(true);
        textArea.setStyle(
                "-fx-background-color: #F8F9FA;" +
                        "-fx-border-color: #E9ECEF;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;"
        );
        VBox.setVgrow(textArea, Priority.ALWAYS);

        panel.getChildren().addAll(title, textArea);
        return panel;
    }

    private VBox createRecommendationsTab(Map<String, Object> result) {
        VBox panel = new VBox(0);
        panel.setStyle("-fx-background-color: white;");

        Label recHeader = new Label("💡 Personalized Recommendations");
        recHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        recHeader.setTextFill(EMERALD_DARK);
        recHeader.setPadding(new Insets(18, 22, 8, 22));

        TextArea recText = new TextArea(String.valueOf(result.get("recommendedContent")));
        recText.setEditable(false);
        recText.setFont(Font.font("Segoe UI", 13));
        recText.setWrapText(true);
        recText.setPrefHeight(160);
        recText.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #E9ECEF;");

        HBox ytHeader = new HBox(10);
        ytHeader.setAlignment(Pos.CENTER_LEFT);
        ytHeader.setPadding(new Insets(14, 22, 8, 22));
        ytHeader.setStyle("-fx-background-color: #FFF3F3; -fx-border-color: #FFCDD2; -fx-border-width: 1 0 1 0;");

        Label ytIcon = new Label("▶");
        ytIcon.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        ytIcon.setStyle("-fx-text-fill: #FF0000;");

        Label ytLabel = new Label("Therapy & Relaxation Videos");
        ytLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        ytLabel.setTextFill(INK);

        Label loadingLabel = new Label("Loading videos…");
        loadingLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        loadingLabel.setTextFill(MUTED);

        Region ytSpacer = new Region();
        HBox.setHgrow(ytSpacer, Priority.ALWAYS);
        ytHeader.getChildren().addAll(ytIcon, ytLabel, ytSpacer, loadingLabel);

        WebView webView = new WebView();
        webView.setContextMenuEnabled(false);
        webView.getEngine().setUserAgent("Mozilla/5.0 MentisMentalHealthApp/1.0");
        VBox.setVgrow(webView, Priority.ALWAYS);
        webView.getEngine().loadContent(buildSkeletonHtml());

        panel.getChildren().addAll(recHeader, recText, ytHeader, webView);

        String riskLevel = (String) result.getOrDefault("riskLevel", "");
        String assessmentType = getAssessmentType();

        Thread fetchThread = new Thread(() -> {
            List<YouTubeRecommendationService.VideoResult> videos =
                    YouTubeRecommendationService.fetchVideos(assessmentType, riskLevel, 6);

            Platform.runLater(() -> {
                loadingLabel.setText(videos.isEmpty() ? "Could not load videos" : videos.size() + " videos found");
                webView.getEngine().loadContent(buildVideoCardsHtml(videos));
            });
        });
        fetchThread.setDaemon(true);
        fetchThread.start();

        return panel;
    }

    private VBox createMetricCard(String icon, String value, String label, Color color) {
        VBox card = new VBox(7);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(240);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: #F1F8E9;" +
                        "-fx-background-radius: 16;"
        );

        Label i = new Label(icon);
        i.setFont(Font.font("Segoe UI Emoji", 30));

        Label v = new Label(value == null ? "N/A" : value);
        v.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        v.setTextFill(color);
        v.setWrapText(true);
        v.setTextAlignment(TextAlignment.CENTER);

        Label l = new Label(label);
        l.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        l.setTextFill(MUTED);

        card.getChildren().addAll(i, v, l);
        return card;
    }

    private VBox createTextSection(String title, String content) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(18));
        box.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 14;");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        titleLabel.setTextFill(EMERALD_DARK);

        Separator separator = new Separator();

        Label contentLabel = new Label(content == null ? "N/A" : content);
        contentLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        contentLabel.setTextFill(MUTED);
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-line-spacing: 5;");

        box.getChildren().addAll(titleLabel, separator, contentLabel);
        return box;
    }

    private String buildSummaryText(Map<String, Object> result) {
        String adaptiveInfo = "";
        if (adaptiveSession != null && useAdaptiveMode) {
            adaptiveInfo =
                    "Adaptive Questions Asked: " + adaptiveSession.getQuestionCount() + "\n" +
                            "Category Scores: " + adaptiveSession.getCategoryScores() + "\n\n";
        }

        return adaptiveInfo +
                "Total Score: " + result.get("totalScore") + "\n" +
                "Risk Level: " + result.get("riskLevel") + "\n" +
                "Session Suggested: " + (Boolean.TRUE.equals(result.get("suggestSession")) ? "Yes" : "No");
    }

    private Color riskColor(String riskLevel) {
        if (riskLevel == null) return EMERALD;
        String r = riskLevel.toLowerCase();
        if (r.contains("high") || r.contains("severe") || r.contains("critical")) return DANGER;
        if (r.contains("moderate") || r.contains("mild")) return WARNING;
        return EMERALD;
    }

    private boolean isCriticalRisk(String riskLevel) {
        return GeolocationService.isCriticalRisk(riskLevel);
    }

    private Button createEmergencyButton() {
        Button button = new Button("🚨 EMERGENCY HELP");
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        button.setTextFill(Color.WHITE);
        button.setStyle(
                "-fx-background-color: #E74C3C;" +
                        "-fx-background-radius: 999;" +
                        "-fx-padding: 11 24;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(231,76,60,0.45), 16, 0, 0, 7);"
        );
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: #C0392B;" +
                        "-fx-background-radius: 999;" +
                        "-fx-padding: 11 24;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(231,76,60,0.65), 20, 0, 0, 8);"
        ));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: #E74C3C;" +
                        "-fx-background-radius: 999;" +
                        "-fx-padding: 11 24;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(231,76,60,0.45), 16, 0, 0, 7);"
        ));
        return button;
    }

    private String getAssessmentType() {
        if (availableAssessments != null) {
            for (Assessment a : availableAssessments) {
                if (a.getAssessmentId() == currentAssessmentId) return a.getType();
            }
        }
        return "general";
    }

    private String buildSkeletonHtml() {
        return "<!DOCTYPE html><html><head><style>" +
                "body{margin:0;padding:12px;background:#fafafa;font-family:Arial,sans-serif;}" +
                ".grid{display:flex;flex-wrap:wrap;gap:12px;}" +
                ".card{width:calc(33% - 8px);background:#f0f0f0;border-radius:12px;height:180px;animation:pulse 1.2s infinite alternate;}" +
                "@keyframes pulse{from{opacity:0.5}to{opacity:1}}" +
                "</style></head><body>" +
                "<div class='grid'>" +
                "<div class='card'></div><div class='card'></div><div class='card'></div>" +
                "<div class='card'></div><div class='card'></div><div class='card'></div>" +
                "</div></body></html>";
    }

    private String buildVideoCardsHtml(List<YouTubeRecommendationService.VideoResult> videos) {
        if (videos.isEmpty()) {
            return "<!DOCTYPE html><html><body style='font-family:Arial;padding:30px;color:#888;text-align:center;'>" +
                    "<p style='font-size:16px;'>⚠️ Could not load videos.<br>Check your YouTube API key or internet connection.</p>" +
                    "</body></html>";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><style>\n")
                .append("*{margin:0;padding:0;box-sizing:border-box;}\n")
                .append("body{background:#fafafa;font-family:Arial,sans-serif;padding:12px;}\n")
                .append(".grid{display:flex;flex-wrap:wrap;gap:12px;}\n")
                .append(".card{width:calc(33.33% - 8px);background:white;border-radius:14px;box-shadow:0 2px 8px rgba(0,0,0,0.10);overflow:hidden;cursor:pointer;transition:transform 0.15s,box-shadow 0.15s;text-decoration:none;display:block;color:inherit;}\n")
                .append(".card:hover{transform:translateY(-3px);box-shadow:0 6px 18px rgba(0,0,0,0.16);}\n")
                .append(".thumb-wrap{position:relative;width:100%;padding-top:56.25%;overflow:hidden;background:#000;}\n")
                .append(".thumb-wrap img{position:absolute;top:0;left:0;width:100%;height:100%;object-fit:cover;}\n")
                .append(".play-overlay{position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);width:42px;height:42px;background:rgba(255,0,0,0.85);border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:18px;color:white;pointer-events:none;}\n")
                .append(".info{padding:10px;}\n")
                .append(".title{font-size:12px;font-weight:bold;color:#111;line-height:1.4;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden;}\n")
                .append(".channel{font-size:11px;color:#606060;margin-top:4px;}\n")
                .append(".yt-badge{display:inline-block;margin-top:5px;background:#ff0000;color:white;font-size:10px;padding:2px 7px;border-radius:999px;font-weight:bold;}\n")
                .append("</style></head><body><div class='grid'>\n");

        for (YouTubeRecommendationService.VideoResult v : videos) {
            String safeTitle = escapeHtml(v.title);
            String safeUrl = v.watchUrl;
            String safeThumb = v.thumbnail.isEmpty() ? "https://i.ytimg.com/vi/" + v.videoId + "/mqdefault.jpg" : v.thumbnail;
            String safeChannel = escapeHtml(v.channelTitle);

            sb.append("<a class='card' href='").append(safeUrl).append("'>")
                    .append("<div class='thumb-wrap'>")
                    .append("<img src='").append(safeThumb).append("' alt='").append(safeTitle).append("'/>")
                    .append("<div class='play-overlay'>▶</div>")
                    .append("</div>")
                    .append("<div class='info'>")
                    .append("<div class='title'>").append(safeTitle).append("</div>")
                    .append("<div class='channel'>").append(safeChannel).append("</div>")
                    .append("<span class='yt-badge'>YouTube</span>")
                    .append("</div>")
                    .append("</a>");
        }

        sb.append("</div></body></html>");
        return sb.toString();
    }

    private void exportAsText(Map<String, Object> result) {
        try {
            AssessmentResult assessmentResult = getAssessmentResultFromMap(result);
            String aiAnalysis = (String) result.get("aiAnalysis");
            String content = resultController.exportResultToText(assessmentResult, aiAnalysis);

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Report as Text");
            fileChooser.setInitialFileName("mentis_assessment_report.txt");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                Files.write(file.toPath(), content.getBytes());
                showAlert("Export Successful", "Report exported successfully to:\n" + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            showAlert("Export Error", "Error exporting report: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void exportAsHTML(Map<String, Object> result) {
        try {
            AssessmentResult assessmentResult = getAssessmentResultFromMap(result);
            String aiAnalysis = (String) result.get("aiAnalysis");
            String content = resultController.exportResultToHTML(assessmentResult, aiAnalysis);

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Report as HTML");
            fileChooser.setInitialFileName("mentis_assessment_report.html");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML Files", "*.html"));

            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                Files.write(file.toPath(), content.getBytes());
                showAlert("Export Successful", "HTML report exported successfully to:\n" + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            showAlert("Export Error", "Error exporting HTML report: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private AssessmentResult getAssessmentResultFromMap(Map<String, Object> result) {
        AssessmentResult assessmentResult = new AssessmentResult();
        assessmentResult.setResultId((int) result.getOrDefault("resultId", 0));
        assessmentResult.setUserId(userId);
        assessmentResult.setAssessmentId(currentAssessmentId);
        assessmentResult.setTotalScore((int) result.get("totalScore"));
        assessmentResult.setRiskLevel((String) result.get("riskLevel"));
        assessmentResult.setInterpretation((String) result.get("interpretation"));
        assessmentResult.setRecommendedContent((String) result.get("recommendedContent"));
        assessmentResult.setSuggestSession((boolean) result.get("suggestSession"));
        assessmentResult.setTakenAt(new Date());
        return assessmentResult;
    }

    private void showSelectionPanel() {
        currentQuestions = null;
        answers.clear();
        currentQuestionIndex = 0;
        isAdapting = false;

        if (assessmentImageView != null) {
            assessmentImageView.setImage(null);
            assessmentImageView.setStyle("-fx-background-color: " + gradient(EMERALD, EMERALD_DARK) + ";");
        }

        refreshData();
        selectionPanel.setVisible(true);
        questionPanel.setVisible(false);
    }

    private void showQuestionPanel() {
        selectionPanel.setVisible(false);
        questionPanel.setVisible(true);
    }

    private void showEmptyState(GridPane grid, String message) {
        grid.getChildren().clear();

        VBox empty = new VBox(14);
        empty.setAlignment(Pos.CENTER);
        empty.setPadding(new Insets(50));
        empty.setPrefWidth(760);
        empty.setStyle(
                "-fx-background-color: " + cssColor(SOFT_GREEN_BG) + ";" +
                        "-fx-background-radius: 20;" +
                        softShadow()
        );

        Label icon = new Label("📋");
        icon.setFont(Font.font("Segoe UI Emoji", 54));

        Label title = new Label(message);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(MUTED);

        Label hint = new Label("Try adjusting your search or clearing filters.");
        hint.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        hint.setTextFill(MUTED);

        Button reset = createSymfonyPrimaryButton("View All Assessments");
        reset.setOnAction(e -> {
            if (searchField != null) searchField.clear();
            refreshData();
        });

        empty.getChildren().addAll(icon, title, hint, reset);
        grid.add(empty, 0, 0);
        GridPane.setColumnSpan(empty, 3);
    }

    private Color getTypeColor(String type) {
        if (type == null) return TYPE_DEFAULT;
        switch (type.toLowerCase()) {
            case "depression": return TYPE_DEPRESSION;
            case "anxiety": return TYPE_ANXIETY;
            case "stress": return TYPE_STRESS;
            case "wellness": return TYPE_WELLNESS;
            case "general": return TYPE_GENERAL;
            default: return TYPE_DEFAULT;
        }
    }

    private Button createSymfonyPrimaryButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        button.setTextFill(Color.WHITE);
        button.setStyle(
                "-fx-background-color: " + gradient(EMERALD, EMERALD_MID) + ";" +
                        "-fx-background-radius: 999;" +
                        "-fx-padding: 11 26;" +
                        "-fx-cursor: hand;" +
                        softShadow()
        );
        button.setOnMouseEntered(e -> {
            if (!button.isDisable()) {
                button.setStyle(
                        "-fx-background-color: " + gradient(EMERALD, EMERALD_DARK) + ";" +
                                "-fx-background-radius: 999;" +
                                "-fx-padding: 11 26;" +
                                "-fx-cursor: hand;" +
                                "-fx-translate-y: -2;" +
                                "-fx-effect: dropshadow(gaussian, rgba(80,200,120,0.35), 18, 0, 0, 8);"
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
                                softShadow()
                );
            }
        });
        return button;
    }

    private Button createSymfonyOutlineButton(String text) {
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
                button.setStyle(
                        "-fx-background-color: #F1F8E9;" +
                                "-fx-background-radius: 999;" +
                                "-fx-border-radius: 999;" +
                                "-fx-border-color: " + cssColor(EMERALD) + ";" +
                                "-fx-border-width: 1.5;" +
                                "-fx-padding: 10 24;" +
                                "-fx-cursor: hand;"
                );
            }
        });
        button.setOnMouseExited(e -> {
            if (!button.isDisable()) {
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

    private Label createBadge(String text, Color bg) {
        Label badge = new Label(text == null ? "General" : text);
        badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        badge.setTextFill(Color.WHITE);
        badge.setPadding(new Insets(6, 13, 6, 13));
        badge.setStyle("-fx-background-color: " + cssColor(bg) + "; -fx-background-radius: 999;");
        return badge;
    }

    private Label createIconCircle(String icon, Color left, Color right, double size) {
        Label circle = new Label(icon);
        circle.setFont(Font.font("Segoe UI Emoji", FontWeight.BOLD, size * 0.38));
        circle.setTextFill(Color.WHITE);
        circle.setAlignment(Pos.CENTER);
        circle.setMinSize(size, size);
        circle.setPrefSize(size, size);
        circle.setMaxSize(size, size);
        circle.setStyle(
                "-fx-background-color: " + gradient(left, right) + ";" +
                        "-fx-background-radius: 999;" +
                        softShadow()
        );
        return circle;
    }

    private ScrollPane cleanScrollPane(Region content) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle(
                "-fx-background: transparent;" +
                        "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;"
        );
        return scrollPane;
    }

    private String pillInputStyle() {
        return "-fx-background-color: white;" +
                "-fx-background-radius: 999;" +
                "-fx-border-radius: 999;" +
                "-fx-border-width: 2;" +
                "-fx-border-color: " + cssColor(EMERALD) + ";" +
                "-fx-padding: 11 18;" +
                "-fx-font-size: 14px;" +
                "-fx-font-family: 'Segoe UI';";
    }

    private String glassCardStyle() {
        return "-fx-background-color: white;" +
                "-fx-background-radius: 20;" +
                "-fx-border-radius: 20;" +
                "-fx-border-color: transparent;" +
                cardShadow();
    }

    private String cardShadow() {
        return "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 18, 0, 0, 8);";
    }

    private String softShadow() {
        return "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 5);";
    }

    private String gradient(Color left, Color right) {
        return "linear-gradient(to bottom right, " + cssColor(left) + ", " + cssColor(right) + ")";
    }

    private String cssColor(Color color) {
        return "#" + toHex(color);
    }

    private String toHex(Color color) {
        return String.format("%02x%02x%02x",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
        refreshData();
    }
}
