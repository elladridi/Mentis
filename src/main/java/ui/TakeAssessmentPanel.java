package ui;

import controller.AssessmentController;
import controller.AssessmentResultController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import models.Assessment;
import models.AssessmentResult;
import models.Question;
import models.AdaptiveAssessmentSession;
import services.AdaptiveQuestionService;
import services.GeminiService;
import services.GeolocationService;
import services.YouTubeRecommendationService;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
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

    // Adaptive assessment fields
    private AdaptiveAssessmentSession adaptiveSession;
    private boolean useAdaptiveMode = true;
    private static final int MAX_ADAPTIVE_QUESTIONS = 8;
    private boolean adaptiveEnabled = true;
    private boolean isAdapting = false;

    // UI Components
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
    private Label userInfoLabel;
    private Label adaptiveStatusLabel;

    // For image handling
    private Map<Integer, Image> assessmentImages = new HashMap<>();

    // Color constants
    private static final Color BACKGROUND_BEIGE = Color.rgb(243, 243, 243);
    private static final Color CARD_WHITE = Color.WHITE;
    private static final Color ACCENT_GREEN = Color.rgb(108, 158, 131);
    private static final Color ACCENT_DARK_GREEN = Color.rgb(60, 120, 90);
    private static final Color ACCENT_LIGHT_GREEN = Color.rgb(200, 225, 210);
    private static final Color BORDER_LIGHT = Color.rgb(220, 220, 220);
    private static final Color TEXT_DARK = Color.rgb(40, 70, 50);
    private static final Color TEXT_LIGHT = Color.rgb(100, 130, 110);
    private static final Color TEXT_GRAY = Color.rgb(120, 120, 120);

    // Type colors
    private static final Color TYPE_DEPRESSION = Color.rgb(144, 127, 201);
    private static final Color TYPE_ANXIETY = Color.rgb(227, 149, 149);
    private static final Color TYPE_STRESS = Color.rgb(227, 206, 163);
    private static final Color TYPE_WELLNESS = Color.rgb(60, 120, 90);
    private static final Color TYPE_GENERAL = Color.rgb(165, 186, 227);
    private static final Color TYPE_DEFAULT = Color.rgb(165, 186, 227);

    public TakeAssessmentPanel(MentisLoginFrame parentApp, AssessmentController assessmentController,
                               AssessmentResultController resultController) {
        this.parentApp = parentApp;
        this.assessmentController = assessmentController;
        this.resultController = resultController;
        this.answers = new HashMap<>();
        this.userId = parentApp.getUserId();
        this.adaptiveService = new AdaptiveQuestionService();

        setStyle("-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";");
        setPadding(new Insets(0));
        setSpacing(0);
        VBox.setVgrow(this, Priority.ALWAYS);

        // Create content stack for panel switching
        contentStack = new StackPane();
        contentStack.setStyle("-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";");
        VBox.setVgrow(contentStack, Priority.ALWAYS);

        createSelectionPanel();
        createQuestionPanel();

        contentStack.getChildren().addAll(selectionPanel, questionPanel);
        showSelectionPanel();

        getChildren().add(contentStack);
    }

    private void createSelectionPanel() {
        selectionPanel = new VBox(20);
        selectionPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";");
        selectionPanel.setPadding(new Insets(45, 50, 45, 50));
        selectionPanel.setVisible(true);

        // Header - REMOVED user ID from top
        BorderPane headerPanel = new BorderPane();
        headerPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";");
        headerPanel.setPadding(new Insets(0, 0, 35, 0));

        Label titleLabel = new Label("Take Assessment!");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 42));
        titleLabel.setTextFill(Color.web(toHex(ACCENT_GREEN)));

        // Top right - Only Results link (removed user ID)
        HBox topRightPanel = new HBox(30);
        topRightPanel.setAlignment(Pos.CENTER_RIGHT);
        topRightPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";");

        Button resultsLink = createHeaderLink("Results");
        resultsLink.setOnAction(e -> parentApp.showResultsPanel());

        topRightPanel.getChildren().add(resultsLink);
        headerPanel.setLeft(titleLabel);
        headerPanel.setRight(topRightPanel);

        // Search Bar
        HBox searchPanel = new HBox(10);
        searchPanel.setAlignment(Pos.CENTER_LEFT);
        searchPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";");
        searchPanel.setPadding(new Insets(20, 0, 15, 0));

        Label searchLabel = new Label("Search Assessments:");
        searchLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        searchLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        searchField = new TextField();
        searchField.setPromptText("Type assessment title to search...");
        searchField.setPrefWidth(300);
        searchField.setStyle(
                "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-color: white;" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 10 15;"
        );

        Button searchButton = createSearchButton("Search");
        searchButton.setOnAction(e -> searchAssessments(searchField.getText().trim()));

        Button clearSearchButton = createClearButton("Clear");
        clearSearchButton.setOnAction(e -> {
            searchField.clear();
            refreshData();
        });

        searchPanel.getChildren().addAll(searchLabel, searchField, searchButton, clearSearchButton);

        // REMOVED the user panel with "Logged in as User ID: 0"

        // Header container
        VBox headerContainer = new VBox(0);
        headerContainer.setStyle("-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";");
        headerContainer.getChildren().addAll(headerPanel, searchPanel);

        // Cards grid
        cardsGrid = new GridPane();
        cardsGrid.setStyle("-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";");
        cardsGrid.setHgap(25);
        cardsGrid.setVgap(25);
        cardsGrid.setPadding(new Insets(10, 0, 10, 0));

        ScrollPane scrollPane = new ScrollPane(cardsGrid);
        scrollPane.setStyle("-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";");
        scrollPane.setBorder(null);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        selectionPanel.getChildren().addAll(headerContainer, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private Button createHeaderLink(String text) {
        Button link = new Button(text);
        link.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        link.setTextFill(Color.web(toHex(ACCENT_GREEN)));
        link.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-width: 0;" +
                        "-fx-cursor: hand;"
        );

        // Hover effect with underline
        link.setOnMouseEntered(e ->
                link.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-border-width: 0 0 2 0;" +
                                "-fx-border-color: #" + toHex(ACCENT_GREEN) + ";" +
                                "-fx-cursor: hand;"
                )
        );
        link.setOnMouseExited(e ->
                link.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-border-width: 0;" +
                                "-fx-cursor: hand;"
                )
        );

        return link;
    }

    private Button createSearchButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        button.setTextFill(Color.web(toHex(TEXT_DARK)));
        button.setStyle(
                "-fx-background-color: #" + toHex(ACCENT_LIGHT_GREEN) + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8 20;" +
                        "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(ACCENT_LIGHT_GREEN.darker()) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 8 20;" +
                                "-fx-cursor: hand;"
                )
        );
        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(ACCENT_LIGHT_GREEN) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 8 20;" +
                                "-fx-cursor: hand;"
                )
        );

        return button;
    }

    private Button createClearButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        button.setTextFill(Color.web(toHex(TEXT_LIGHT)));
        button.setStyle(
                "-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-padding: 8 15;" +
                        "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(BORDER_LIGHT) + ";" +
                                "-fx-border-color: #" + toHex(TEXT_LIGHT) + ";" +
                                "-fx-border-radius: 5;" +
                                "-fx-padding: 8 15;" +
                                "-fx-cursor: hand;"
                )
        );
        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";" +
                                "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                                "-fx-border-radius: 5;" +
                                "-fx-padding: 8 15;" +
                                "-fx-cursor: hand;"
                )
        );

        return button;
    }

    private void createQuestionPanel() {
        questionPanel = new VBox(20);
        questionPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";");
        questionPanel.setPadding(new Insets(30, 50, 30, 50));
        questionPanel.setVisible(false);

        // Top panel with back button only (removed adaptive status)
        HBox topPanel = new HBox();
        topPanel.setAlignment(Pos.CENTER_LEFT);
        topPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";");
        topPanel.setPadding(new Insets(0, 0, 15, 0));
        topPanel.setSpacing(20);

        Button backButton = new Button("← Back to Assessments");
        backButton.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        backButton.setTextFill(Color.web(toHex(ACCENT_GREEN)));
        backButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-width: 0;" +
                        "-fx-cursor: hand;"
        );
        backButton.setOnAction(e -> showSelectionPanel());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topPanel.getChildren().addAll(backButton, spacer);

        // Main content container
        VBox centerContainer = new VBox();
        centerContainer.setAlignment(Pos.CENTER);
        centerContainer.setStyle("-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";");
        VBox.setVgrow(centerContainer, Priority.ALWAYS);

        // Question card - FIXED: Image takes full width, no white sides
        VBox card = new VBox(20);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 30 0 30 0;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);"
        );
        card.setAlignment(Pos.TOP_CENTER);
        card.setMaxWidth(900);
        card.setMinHeight(500);
        card.setMaxHeight(Region.USE_COMPUTED_SIZE);

        // Question number
        questionNumberLabel = new Label("Question 1 of 8");
        questionNumberLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        questionNumberLabel.setTextFill(Color.web(toHex(TEXT_GRAY)));
        questionNumberLabel.setPadding(new Insets(0, 40, 10, 40));

        // Image display - FIXED: Full width, no white sides
        assessmentImageView = new ImageView();
        assessmentImageView.setFitWidth(900);
        assessmentImageView.setFitHeight(250);
        assessmentImageView.setPreserveRatio(true);
        assessmentImageView.setStyle(
                "-fx-background-color: #f5f5f5;" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-width: 1 0 1 0;"
        );

        // Question text
        questionTextLabel = new Label();
        questionTextLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        questionTextLabel.setTextFill(Color.web(toHex(TEXT_DARK)));
        questionTextLabel.setWrapText(true);
        questionTextLabel.setTextAlignment(TextAlignment.CENTER);
        questionTextLabel.setMaxWidth(700);
        questionTextLabel.setMinHeight(80);
        questionTextLabel.setPadding(new Insets(20, 40, 20, 40));

        // Answer combo box
        answerCombo = new ComboBox<>();
        answerCombo.setStyle(
                "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 15px;" +
                        "-fx-background-color: white;" +
                        "-fx-border-color: #" + toHex(ACCENT_GREEN) + ";" +
                        "-fx-border-width: 1.5;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8 12;"
        );
        answerCombo.setPrefWidth(500);
        answerCombo.setPrefHeight(45);
        answerCombo.setPromptText("Select your answer...");

        card.getChildren().addAll(questionNumberLabel, assessmentImageView, questionTextLabel, answerCombo);

        // Center the card
        HBox cardWrapper = new HBox(card);
        cardWrapper.setAlignment(Pos.CENTER);
        cardWrapper.setStyle("-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";");
        HBox.setHgrow(card, Priority.NEVER);

        centerContainer.getChildren().add(cardWrapper);

        // Navigation buttons
        HBox navPanel = new HBox(20);
        navPanel.setAlignment(Pos.CENTER);
        navPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";");
        navPanel.setPadding(new Insets(20, 0, 0, 0));

        prevButton = createNavButton("Previous", ACCENT_LIGHT_GREEN);
        prevButton.setDisable(true);
        prevButton.setOnAction(e -> showPreviousQuestion());

        nextButton = createNavButton("Next", ACCENT_LIGHT_GREEN);
        nextButton.setDisable(true);
        nextButton.setOnAction(e -> showNextQuestion());

        submitButton = createNavButton("Submit", ACCENT_GREEN);
        submitButton.setTextFill(Color.WHITE);
        submitButton.setDisable(true);
        submitButton.setOnAction(e -> submitAssessment());

        navPanel.getChildren().addAll(prevButton, nextButton, submitButton);

        questionPanel.getChildren().addAll(topPanel, centerContainer, navPanel);
        VBox.setVgrow(centerContainer, Priority.ALWAYS);
    }

    private Button createNavButton(String text, Color bgColor) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        button.setTextFill(bgColor == ACCENT_GREEN ? Color.WHITE : Color.web(toHex(TEXT_DARK)));
        button.setStyle(
                "-fx-background-color: #" + toHex(bgColor) + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 10 30;" +
                        "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(e -> {
            if (!button.isDisable()) {
                button.setStyle(
                        "-fx-background-color: #" + toHex(bgColor.darker()) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 10 30;" +
                                "-fx-cursor: hand;"
                );
            }
        });
        button.setOnMouseExited(e -> {
            if (!button.isDisable()) {
                button.setStyle(
                        "-fx-background-color: #" + toHex(bgColor) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 10 30;" +
                                "-fx-cursor: hand;"
                );
            }
        });

        return button;
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
            if (assessment.getTitle().toLowerCase().contains(searchLower) ||
                    (assessment.getDescription() != null &&
                            assessment.getDescription().toLowerCase().contains(searchLower)) ||
                    assessment.getType().toLowerCase().contains(searchLower)) {
                filteredAssessments.add(assessment);
            }
        }

        if (filteredAssessments.isEmpty()) {
            VBox noResultsBox = new VBox(10);
            noResultsBox.setAlignment(Pos.CENTER);
            noResultsBox.setPadding(new Insets(50, 0, 50, 0));

            Label title = new Label("No matching assessments found");
            title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
            title.setTextFill(Color.web(toHex(TEXT_GRAY)));

            Label subtitle = new Label("Try searching with different keywords");
            subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
            subtitle.setTextFill(Color.web(toHex(TEXT_LIGHT)));

            Label searchTerm = new Label("Search: \"" + searchText + "\"");
            searchTerm.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
            searchTerm.setTextFill(Color.web(toHex(TEXT_LIGHT)));

            noResultsBox.getChildren().addAll(title, subtitle, searchTerm);

            cardsGrid.add(noResultsBox, 0, 0);
            GridPane.setColumnSpan(noResultsBox, 2);
        } else {
            int col = 0, row = 0;
            for (Assessment assessment : filteredAssessments) {
                if (!assessmentImages.containsKey(assessment.getAssessmentId())) {
                    loadAssessmentImage(assessment);
                }

                VBox card = createAssessmentCard(assessment);
                cardsGrid.add(card, col, row);

                col++;
                if (col >= 2) {
                    col = 0;
                    row++;
                }
            }
        }
    }

    private void showEmptyState(GridPane grid, String message) {
        grid.getChildren().clear();
        Label emptyLabel = new Label(message);
        emptyLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 18));
        emptyLabel.setTextFill(Color.web(toHex(TEXT_GRAY)));
        emptyLabel.setAlignment(Pos.CENTER);
        grid.add(emptyLabel, 0, 0);
        GridPane.setColumnSpan(emptyLabel, 2);
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

        // Load all images first
        for (Assessment assessment : availableAssessments) {
            loadAssessmentImage(assessment);
        }

        int col = 0, row = 0;
        for (Assessment assessment : availableAssessments) {
            VBox card = createAssessmentCard(assessment);
            cardsGrid.add(card, col, row);

            col++;
            if (col >= 2) {
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
                    assessmentImageView.setFitWidth(900);
                    assessmentImageView.setFitHeight(250);
                    assessmentImageView.setPreserveRatio(true);
                    return;
                }
            } catch (Exception e) {
                System.err.println("Error loading assessment image: " + e.getMessage());
            }
        }

        // If no image or error, show a colored placeholder that spans full width
        assessmentImageView.setImage(null);
        assessmentImageView.setStyle(
                "-fx-background-color: #" + toHex(getTypeColor(assessment.getType())) + ";" +
                        "-fx-border-width: 1 0 1 0;" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";"
        );
    }

    private VBox createAssessmentCard(Assessment assessment) {
        VBox card = new VBox(0);
        card.setStyle(
                "-fx-background-color: #" + toHex(ACCENT_LIGHT_GREEN) + ";" +
                        "-fx-border-color: #" + toHex(ACCENT_LIGHT_GREEN) + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;"
        );
        card.setPrefWidth(450);
        card.setPrefHeight(350);
        card.setCursor(javafx.scene.Cursor.HAND);

        // Image area
        StackPane imagePanel = new StackPane();
        imagePanel.setStyle("-fx-background-color: white;");
        imagePanel.setPrefHeight(180);
        imagePanel.setPrefWidth(450);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(450);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);

        Image icon = assessmentImages.get(assessment.getAssessmentId());
        if (icon != null) {
            imageView.setImage(icon);
        } else {
            // Create colored placeholder
            Label placeholder = new Label(assessment.getTitle());
            placeholder.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
            placeholder.setTextFill(Color.WHITE);
            placeholder.setStyle("-fx-background-color: #" + toHex(getTypeColor(assessment.getType())) + ";");
            placeholder.setAlignment(Pos.CENTER);
            placeholder.setPrefSize(450, 180);
            imagePanel.getChildren().add(placeholder);
        }

        if (imageView.getImage() != null) {
            imagePanel.getChildren().add(imageView);
        }

        // Content area
        VBox contentPanel = new VBox(10);
        contentPanel.setStyle("-fx-background-color: #" + toHex(ACCENT_LIGHT_GREEN) + ";");
        contentPanel.setPadding(new Insets(25, 25, 20, 25));
        contentPanel.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(assessment.getTitle());
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web(toHex(TEXT_DARK)));
        titleLabel.setWrapText(true);
        titleLabel.setTextAlignment(TextAlignment.CENTER);

        Label typeLabel = new Label(assessment.getType());
        typeLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        typeLabel.setTextFill(Color.web(toHex(getTypeColor(assessment.getType()))));
        typeLabel.setPadding(new Insets(8, 0, 20, 0));

        // Description if available
        String description = assessment.getDescription();
        if (description != null && !description.isEmpty()) {
            Label descLabel = new Label();
            if (description.length() > 60) {
                descLabel.setText(description.substring(0, 57) + "...");
            } else {
                descLabel.setText(description);
            }
            descLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
            descLabel.setTextFill(Color.web(toHex(TEXT_LIGHT)));
            descLabel.setWrapText(true);
            descLabel.setTextAlignment(TextAlignment.CENTER);
            descLabel.setPadding(new Insets(0, 0, 10, 0));
            contentPanel.getChildren().add(descLabel);
        }

        // Take test button
        Button takeTestBtn = new Button("TAKE TEST");
        takeTestBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        takeTestBtn.setTextFill(Color.WHITE);
        takeTestBtn.setStyle(
                "-fx-background-color: #" + toHex(ACCENT_GREEN) + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 12 40;" +
                        "-fx-cursor: hand;"
        );
        takeTestBtn.setOnAction(e -> startAssessment(assessment));

        contentPanel.getChildren().addAll(titleLabel, typeLabel);
        contentPanel.getChildren().add(takeTestBtn);

        VBox.setVgrow(contentPanel, Priority.ALWAYS);

        card.getChildren().addAll(imagePanel, contentPanel);

        // Hover effect
        card.setOnMouseEntered(e ->
                card.setStyle(
                        "-fx-background-color: #" + toHex(ACCENT_LIGHT_GREEN) + ";" +
                                "-fx-border-color: #" + toHex(ACCENT_GREEN) + ";" +
                                "-fx-border-width: 3;" +
                                "-fx-border-radius: 5;" +
                                "-fx-background-radius: 5;"
                )
        );
        card.setOnMouseExited(e ->
                card.setStyle(
                        "-fx-background-color: #" + toHex(ACCENT_LIGHT_GREEN) + ";" +
                                "-fx-border-color: #" + toHex(ACCENT_LIGHT_GREEN) + ";" +
                                "-fx-border-width: 1;" +
                                "-fx-border-radius: 5;" +
                                "-fx-background-radius: 5;"
                )
        );

        return card;
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

    private void startAssessment(Assessment assessment) {
        try {
            if (userId <= 0) {
                showAlert("Authentication Error",
                        "User ID not found. Please login again.",
                        Alert.AlertType.WARNING);
                return;
            }

            currentAssessmentId = assessment.getAssessmentId();
            originalQuestions = resultController.getQuestionsByAssessment(currentAssessmentId);

            if (originalQuestions == null || originalQuestions.isEmpty()) {
                showAlert("Error", "No questions found for this assessment!", Alert.AlertType.ERROR);
                return;
            }

            // Initialize adaptive session
            adaptiveSession = new AdaptiveAssessmentSession(
                    currentAssessmentId, userId, originalQuestions);

            // Set adaptive mode
            adaptiveSession.setUseAIAdaptive(useAdaptiveMode);
            adaptiveEnabled = true;
            isAdapting = false;

            currentQuestions = new ArrayList<>(originalQuestions);
            currentQuestionIndex = 0;
            answers.clear();

            // Shuffle questions for better adaptive experience
            Collections.shuffle(currentQuestions);

            prevButton.setDisable(true);
            nextButton.setDisable(currentQuestions.size() <= 1);
            submitButton.setDisable(true);

            // Load and display assessment image
            loadAndDisplayAssessmentImage(assessment);

            showQuestion(currentQuestionIndex);
            showQuestionPanel();

        } catch (Exception e) {
            showAlert("Error", "Error starting assessment: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private Question getNextAdaptiveQuestion() {
        if (adaptiveSession == null || originalQuestions == null || !adaptiveEnabled) {
            return null;
        }

        // Check if we've reached the maximum number of adaptive questions
        if (adaptiveSession.getQuestionCount() >= MAX_ADAPTIVE_QUESTIONS) {
            adaptiveEnabled = false;
            return null;
        }

        // Get remaining questions that haven't been asked
        List<Question> asked = adaptiveSession.getAskedQuestions();
        List<Question> remaining = new ArrayList<>(originalQuestions);
        remaining.removeAll(asked);

        if (remaining.isEmpty()) {
            return null;
        }

        // Let the adaptive service decide the next question
        return adaptiveService.getNextAdaptiveQuestion(adaptiveSession, remaining);
    }

    private void showQuestion(int index) {
        if (currentQuestions == null || index < 0 || index >= currentQuestions.size()) {
            return;
        }

        Question question = currentQuestions.get(index);

        questionNumberLabel.setText("Question " + (index + 1) + " of " + currentQuestions.size());

        // Ensure full question text is displayed
        String questionText = question.getText();
        questionTextLabel.setText(questionText);

        // Force layout refresh
        questionTextLabel.setWrapText(true);
        questionTextLabel.autosize();

        String[] options = resultController.parseScaleToOptions(question.getScale());
        answerCombo.getItems().clear();
        answerCombo.getItems().addAll(options);

        if (answers.containsKey(question.getQuestionId())) {
            answerCombo.setValue(answers.get(question.getQuestionId()));
        } else {
            answerCombo.getSelectionModel().selectFirst();
        }

        prevButton.setDisable(index <= 0);
        nextButton.setDisable(index >= currentQuestions.size() - 1);
        submitButton.setDisable(false);
    }

    private void saveCurrentAnswer() {
        if (currentQuestions != null && currentQuestionIndex < currentQuestions.size()) {
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
        saveCurrentAnswer(); // Save current answer before moving

        // Add the answered question to adaptive session
        Question currentQ = currentQuestions.get(currentQuestionIndex);
        String answer = answers.get(currentQ.getQuestionId());
        int score = resultController.parseAnswerToScore(answer, currentQ.getScale());
        adaptiveSession.addAnsweredQuestion(currentQ, answer, score);

        // Adaptive starts after 2nd question (index 1)
        if (useAdaptiveMode && adaptiveEnabled && currentQuestionIndex >= 1 && !isAdapting) {
            // Try to get an adaptive next question
            Question adaptiveQuestion = getNextAdaptiveQuestion();

            if (adaptiveQuestion != null && !adaptiveSession.getAskedQuestions().contains(adaptiveQuestion)) {
                // Set flag to prevent multiple adaptations
                isAdapting = true;

                // Insert adaptive question at next position
                currentQuestions.add(currentQuestionIndex + 1, adaptiveQuestion);

                // Reset flag after a short delay
                Platform.runLater(() -> {
                    isAdapting = false;
                });
            }
        }

        // Move to next question
        if (currentQuestionIndex < currentQuestions.size() - 1) {
            currentQuestionIndex++;
            showQuestion(currentQuestionIndex);
        }
    }

    private void submitAssessment() {
        saveCurrentAnswer();

        // Proper validation - check if all questions have answers
        boolean allAnswered = true;
        for (Question question : currentQuestions) {
            if (!answers.containsKey(question.getQuestionId())) {
                allAnswered = false;
                break;
            }
        }

        if (!allAnswered) {
            showAlert("Incomplete Assessment",
                    "Please answer all questions before submitting!",
                    Alert.AlertType.WARNING);
            return;
        }

        // Add last question to adaptive session if not already added
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
            Map<String, Object> result = resultController.submitAssessment(
                    userId, currentAssessmentId, answerScores, originalAnswers);

            if ((Boolean) result.get("success")) {
                showResultsWithAI((Map<String, Object>) result.get("result"));
            } else {
                showAlert("Submission Error",
                        "Error submitting assessment: " + result.get("error"),
                        Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            showAlert("Error", "Error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showResultsWithAI(Map<String, Object> result) {
        Stage resultDialog = new Stage();
        resultDialog.initModality(Modality.APPLICATION_MODAL);
        resultDialog.setTitle("Assessment Results");
        resultDialog.setMinWidth(700);
        resultDialog.setMinHeight(800);

        BorderPane mainPanel = new BorderPane();
        mainPanel.setStyle("-fx-background-color: #f3f3f3;");

        TabPane tabbedPane = new TabPane();
        tabbedPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabbedPane.setStyle("-fx-background-color: white;");

        Tab summaryTab         = new Tab("📊 Summary");
        Tab analysisTab        = new Tab("🤖 AI Analysis");
        Tab recommendationsTab = new Tab("💡 Recommendations");

        summaryTab.setContent(createSummaryTab(result));
        analysisTab.setContent(createAnalysisTab(result));
        recommendationsTab.setContent(createRecommendationsTab(result));

        tabbedPane.getTabs().addAll(summaryTab, analysisTab, recommendationsTab);
        mainPanel.setCenter(tabbedPane);

        // ── Risk check (fixed) ───────────────────────────────────────────────
        String riskLevel = (String) result.get("riskLevel");
        System.out.println("[DEBUG] riskLevel = '" + riskLevel + "' → isCritical=" + isCriticalRisk(riskLevel));
        boolean isCritical = isCriticalRisk(riskLevel);

        // ── Buttons ──────────────────────────────────────────────────────────
        HBox buttonPanel = new HBox(20);
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.setStyle("-fx-background-color: #f3f3f3; -fx-padding: 15px;");

        Button exportTextBtn = createExportButton("Export as Text File");
        exportTextBtn.setOnAction(e -> exportAsText(result));

        Button exportHTMLBtn = createExportButton("Export as HTML");
        exportHTMLBtn.setOnAction(e -> exportAsHTML(result));

        Button closeBtn = createExportButton("Close");
        closeBtn.setOnAction(e -> { resultDialog.close(); showSelectionPanel(); });

        if (isCritical) {
            Button emergencyBtn = createEmergencyButton();
            emergencyBtn.setOnAction(e ->
                    GeolocationService.checkAndShowEmergencyResources(riskLevel, null)
            );
            buttonPanel.getChildren().addAll(emergencyBtn, exportTextBtn, exportHTMLBtn, closeBtn);

            // Show warning popup after dialog opens
            Platform.runLater(() -> {
                Alert warn = new Alert(Alert.AlertType.WARNING);
                warn.setTitle("⚠️ CRITICAL RISK DETECTED");
                warn.setHeaderText("Please seek immediate support");
                warn.setContentText(
                        "Your results indicate a HIGH risk level.\n\n" +
                                "Click '🚨 EMERGENCY HELP' below to find the nearest\n" +
                                "hospitals and mental health centers on a live map.\n\n" +
                                "Emergency Contacts:\n" +
                                "  • 911 (US) / 112 (EU) — Emergency Services\n" +
                                "  • 988 — Mental Health Crisis Line (US)\n" +
                                "  • Text HOME to 741741 — Crisis Text Line"
                );
                warn.show();
            });
        } else {
            buttonPanel.getChildren().addAll(exportTextBtn, exportHTMLBtn, closeBtn);
        }

        mainPanel.setBottom(buttonPanel);

        Scene scene = new Scene(mainPanel, 700, 800);
        resultDialog.setScene(scene);
        resultDialog.showAndWait();
    }

    // Add helper method to check critical risk
    private boolean isCriticalRisk(String riskLevel) {
        return GeolocationService.isCriticalRisk(riskLevel);
    }

    // Add emergency button creation
    private Button createEmergencyButton() {
        Button button = new Button("🚨 EMERGENCY HELP");
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        button.setTextFill(Color.WHITE);
        button.setStyle(
                "-fx-background-color: #ff4444;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(255,0,0,0.5), 10, 0, 0, 0);"
        );

        // Add pulsing animation effect
        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: #ff6666;" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 10 20;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(255,0,0,0.8), 15, 0, 0, 0);"
                )
        );

        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: #ff4444;" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 10 20;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(255,0,0,0.5), 10, 0, 0, 0);"
                )
        );

        return button;
    }

    private VBox createSummaryTab(Map<String, Object> result) {
        VBox panel = new VBox(20);
        panel.setStyle("-fx-background-color: white;");
        panel.setPadding(new Insets(20));

        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setFont(Font.font("Monospaced", 13));
        textArea.setWrapText(true);

        // Adaptive info block
        String adaptiveInfo = "";
        if (adaptiveSession != null && useAdaptiveMode) {
            adaptiveInfo =
                    "=== ADAPTIVE ASSESSMENT INFO ===\n" +
                            "Questions Asked : " + adaptiveSession.getQuestionCount() + "\n" +
                            "Category Scores : " + adaptiveSession.getCategoryScores() + "\n\n";
        }

        // Pull full AI analysis but cap at ~800 chars so it stays readable
        String fullAI = result.get("aiAnalysis") != null ? result.get("aiAnalysis").toString() : "";
        String keyInsights = fullAI.length() > 800 ? fullAI.substring(0, 800) + "…\n(see AI Analysis tab for full report)" : fullAI;

        String summary =
                "=== ASSESSMENT RESULTS ===\n\n" +
                        adaptiveInfo +
                        "Total Score      : " + result.get("totalScore") + "\n" +
                        "Risk Level       : " + result.get("riskLevel") + "\n" +
                        "Session Suggested: " + (Boolean.TRUE.equals(result.get("suggestSession")) ? "Yes" : "No") + "\n\n" +
                        "=== INTERPRETATION ===\n" +
                        result.get("interpretation") + "\n\n" +
                        "=== KEY INSIGHTS ===\n" +
                        keyInsights;

        textArea.setText(summary);
        VBox.setVgrow(textArea, Priority.ALWAYS);
        panel.getChildren().add(textArea);
        return panel;
    }

    private VBox createAnalysisTab(Map<String, Object> result) {
        VBox panel = new VBox();
        panel.setStyle("-fx-background-color: white;");
        panel.setPadding(new Insets(20));

        // Clean markdown symbols from the text
        String rawAnalysis = result.get("aiAnalysis").toString();
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
        VBox.setVgrow(textArea, Priority.ALWAYS);
        textArea.setPrefHeight(500);

        panel.getChildren().add(textArea);
        return panel;
    }


    private VBox createRecommendationsTab(Map<String, Object> result) {
        VBox panel = new VBox(0);
        panel.setStyle("-fx-background-color: white;");

        // ── Text recommendations (top half) ─────────────────────────────────
        Label recHeader = new Label("💡 Personalized Recommendations");
        recHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        recHeader.setTextFill(Color.web("#3c7860"));
        recHeader.setPadding(new Insets(16, 20, 6, 20));

        TextArea recText = new TextArea(result.get("recommendedContent").toString());
        recText.setEditable(false);
        recText.setFont(Font.font("Segoe UI", 13));
        recText.setWrapText(true);
        recText.setPrefHeight(160);
        recText.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #e0e0e0;");

        // ── YouTube section header ───────────────────────────────────────────
        HBox ytHeader = new HBox(10);
        ytHeader.setAlignment(Pos.CENTER_LEFT);
        ytHeader.setPadding(new Insets(14, 20, 6, 20));
        ytHeader.setStyle("-fx-background-color: #fff3f3; -fx-border-color: #ffcdd2; -fx-border-width: 1 0 1 0;");

        Label ytIcon = new Label("▶");
        ytIcon.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        ytIcon.setStyle("-fx-text-fill: #ff0000;");

        Label ytLabel = new Label("Therapy & Relaxation Videos");
        ytLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        ytLabel.setTextFill(Color.web("#333"));

        Label loadingLabel = new Label("Loading videos…");
        loadingLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        loadingLabel.setTextFill(Color.web("#888"));

        Region ytSpacer = new Region();
        HBox.setHgrow(ytSpacer, Priority.ALWAYS);
        ytHeader.getChildren().addAll(ytIcon, ytLabel, ytSpacer, loadingLabel);

        // ── WebView for video cards ──────────────────────────────────────────
        WebView webView = new WebView();
        webView.setContextMenuEnabled(false);
        webView.getEngine().setUserAgent("Mozilla/5.0 MentisMentalHealthApp/1.0");
        VBox.setVgrow(webView, Priority.ALWAYS);

        // Show placeholder skeleton while loading
        webView.getEngine().loadContent(buildSkeletonHtml());

        panel.getChildren().addAll(recHeader, recText, ytHeader, webView);

        // ── Fetch videos in background ───────────────────────────────────────
        String riskLevel      = (String) result.getOrDefault("riskLevel", "");
        String assessmentType = getAssessmentType();   // helper below

        Thread fetchThread = new Thread(() -> {
            List<YouTubeRecommendationService.VideoResult> videos =
                    YouTubeRecommendationService.fetchVideos(assessmentType, riskLevel, 6);

            Platform.runLater(() -> {
                loadingLabel.setText(videos.isEmpty()
                        ? "Could not load videos"
                        : videos.size() + " videos found");
                webView.getEngine().loadContent(buildVideoCardsHtml(videos));
            });
        });
        fetchThread.setDaemon(true);
        fetchThread.start();

        return panel;
    }

    /**
     * Get the assessment type string for the current assessment.
     * Looks it up from availableAssessments by currentAssessmentId.
     */
    private String getAssessmentType() {
        if (availableAssessments != null) {
            for (Assessment a : availableAssessments) {
                if (a.getAssessmentId() == currentAssessmentId) {
                    return a.getType();
                }
            }
        }
        return "general";
    }

    /**
     * Skeleton loading HTML shown while videos are fetching.
     */
    private String buildSkeletonHtml() {
        return "<!DOCTYPE html><html><head><style>" +
                "body{margin:0;padding:12px;background:#fafafa;font-family:Arial,sans-serif;}" +
                ".grid{display:flex;flex-wrap:wrap;gap:12px;}" +
                ".card{width:calc(33% - 8px);background:#f0f0f0;border-radius:8px;" +
                "height:180px;animation:pulse 1.2s infinite alternate;}" +
                "@keyframes pulse{from{opacity:0.5}to{opacity:1}}" +
                "</style></head><body>" +
                "<div class='grid'>" +
                "<div class='card'></div><div class='card'></div><div class='card'></div>" +
                "<div class='card'></div><div class='card'></div><div class='card'></div>" +
                "</div></body></html>";
    }

    /**
     * Build a rich HTML grid of video cards from fetched results.
     * Clicking a card opens it in the default browser via JavaScript bridge,
     * OR falls back to showing the URL in an alert (works without bridge too).
     */
    private String buildVideoCardsHtml(List<YouTubeRecommendationService.VideoResult> videos) {
        if (videos.isEmpty()) {
            return "<!DOCTYPE html><html><body style='font-family:Arial;padding:30px;color:#888;text-align:center;'>" +
                    "<p style='font-size:16px;'>⚠️ Could not load videos.<br>" +
                    "Check your YouTube API key in YouTubeRecommendationService.java<br>" +
                    "or your internet connection.</p></body></html>";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><style>\n")
                .append("*{margin:0;padding:0;box-sizing:border-box;}\n")
                .append("body{background:#fafafa;font-family:Arial,sans-serif;padding:12px;}\n")
                .append(".grid{display:flex;flex-wrap:wrap;gap:12px;}\n")
                .append(".card{\n")
                .append("  width:calc(33.33% - 8px);background:white;border-radius:10px;\n")
                .append("  box-shadow:0 2px 8px rgba(0,0,0,0.10);overflow:hidden;\n")
                .append("  cursor:pointer;transition:transform 0.15s,box-shadow 0.15s;\n")
                .append("  text-decoration:none;display:block;color:inherit;\n")
                .append("}\n")
                .append(".card:hover{transform:translateY(-3px);box-shadow:0 6px 18px rgba(0,0,0,0.16);}\n")
                .append(".thumb-wrap{position:relative;width:100%;padding-top:56.25%;overflow:hidden;background:#000;}\n")
                .append(".thumb-wrap img{position:absolute;top:0;left:0;width:100%;height:100%;object-fit:cover;}\n")
                .append(".play-overlay{\n")
                .append("  position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);\n")
                .append("  width:42px;height:42px;background:rgba(255,0,0,0.85);border-radius:50%;\n")
                .append("  display:flex;align-items:center;justify-content:center;\n")
                .append("  font-size:18px;color:white;pointer-events:none;\n")
                .append("}\n")
                .append(".info{padding:10px;}\n")
                .append(".title{font-size:12px;font-weight:bold;color:#111;line-height:1.4;\n")
                .append("  display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden;}\n")
                .append(".channel{font-size:11px;color:#606060;margin-top:4px;}\n")
                .append(".yt-badge{display:inline-block;margin-top:5px;background:#ff0000;\n")
                .append("  color:white;font-size:10px;padding:2px 7px;border-radius:3px;font-weight:bold;}\n")
                .append("</style></head><body>\n")
                .append("<div class='grid'>\n");

        for (YouTubeRecommendationService.VideoResult v : videos) {
            String safeTitle   = v.title.replace("'", "\\'").replace("\"", "&quot;");
            String safeUrl     = v.watchUrl;
            String safeThumb   = v.thumbnail.isEmpty()
                    ? "https://i.ytimg.com/vi/" + v.videoId + "/mqdefault.jpg"
                    : v.thumbnail;
            String safeChannel = v.channelTitle.replace("'", "\\'");

            sb.append("  <a class='card' href='").append(safeUrl).append("' ")
                    .append("onclick=\"window.open('").append(safeUrl).append("','_blank');return false;\">\n")
                    .append("    <div class='thumb-wrap'>\n")
                    .append("      <img src='").append(safeThumb).append("' alt='").append(safeTitle).append("'/>\n")
                    .append("      <div class='play-overlay'>▶</div>\n")
                    .append("    </div>\n")
                    .append("    <div class='info'>\n")
                    .append("      <div class='title'>").append(v.title.replace("<","&lt;").replace(">","&gt;")).append("</div>\n")
                    .append("      <div class='channel'>").append(v.channelTitle.replace("<","&lt;")).append("</div>\n")
                    .append("      <span class='yt-badge'>YouTube</span>\n")
                    .append("    </div>\n")
                    .append("  </a>\n");
        }

        sb.append("</div>\n");

        // JS bridge: open in system browser via JavaFX WebEngine
        sb.append("<script>\n")
                .append("document.querySelectorAll('.card').forEach(function(card){\n")
                .append("  card.addEventListener('click',function(e){\n")
                .append("    e.preventDefault();\n")
                .append("    var url = card.getAttribute('href');\n")
                .append("    // Try JavaFX hostServices bridge first\n")
                .append("    try { javaApp.openUrl(url); } catch(ex) {\n")
                .append("      // Fallback: load in current WebView\n")
                .append("      window.location.href = url;\n")
                .append("    }\n")
                .append("  });\n")
                .append("});\n")
                .append("</script>\n")
                .append("</body></html>");

        return sb.toString();
    }

    private Button createExportButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        button.setTextFill(Color.WHITE);
        button.setStyle(
                "-fx-background-color: #" + toHex(ACCENT_GREEN) + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(ACCENT_GREEN.darker()) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 10 20;" +
                                "-fx-cursor: hand;"
                )
        );
        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(ACCENT_GREEN) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 10 20;" +
                                "-fx-cursor: hand;"
                )
        );

        return button;
    }

    private void exportAsText(Map<String, Object> result) {
        try {
            AssessmentResult assessmentResult = getAssessmentResultFromMap(result);
            String aiAnalysis = (String) result.get("aiAnalysis");
            String content = resultController.exportResultToText(assessmentResult, aiAnalysis);

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Report as Text");
            fileChooser.setInitialFileName("mentis_assessment_report.txt");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Text Files", "*.txt")
            );

            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                Files.write(file.toPath(), content.getBytes());
                showAlert("Export Successful",
                        "Report exported successfully to:\n" + file.getAbsolutePath(),
                        Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            showAlert("Export Error",
                    "Error exporting report: " + e.getMessage(),
                    Alert.AlertType.ERROR);
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
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("HTML Files", "*.html")
            );

            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                Files.write(file.toPath(), content.getBytes());
                showAlert("Export Successful",
                        "HTML report exported successfully to:\n" + file.getAbsolutePath(),
                        Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            showAlert("Export Error",
                    "Error exporting HTML report: " + e.getMessage(),
                    Alert.AlertType.ERROR);
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

        // Clear the assessment image
        if (assessmentImageView != null) {
            assessmentImageView.setImage(null);
            assessmentImageView.setStyle(
                    "-fx-background-color: #f5f5f5;" +
                            "-fx-border-width: 1 0 1 0;" +
                            "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";"
            );
        }

        refreshData();
        selectionPanel.setVisible(true);
        questionPanel.setVisible(false);
    }

    private void showQuestionPanel() {
        selectionPanel.setVisible(false);
        questionPanel.setVisible(true);
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
        // Don't show user ID in UI anymore
        refreshData();
    }

    // ================= UTILITY =================
    private String toHex(Color color) {
        return String.format("%02x%02x%02x",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }
}