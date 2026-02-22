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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Assessment;
import models.AssessmentResult;
import models.Question;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

public class TakeAssessmentPanel extends VBox {

    private int userId;
    private MentisLoginFrame parentApp;  // FIXED: Changed from MentisLoginFrame to MentisLoginFrame
    private AssessmentController assessmentController;
    private AssessmentResultController resultController;
    private List<Assessment> availableAssessments;
    private List<Question> currentQuestions;
    private Map<Integer, String> answers;
    private int currentQuestionIndex = 0;
    private int currentAssessmentId = 0;

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

    public TakeAssessmentPanel(MentisLoginFrame parentApp, AssessmentController assessmentController,  // FIXED: Parameter type
                               AssessmentResultController resultController) {
        this.parentApp = parentApp;
        this.assessmentController = assessmentController;
        this.resultController = resultController;
        this.answers = new HashMap<>();
        this.userId = parentApp.getUserId();

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

        // Header
        BorderPane headerPanel = new BorderPane();
        headerPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";");
        headerPanel.setPadding(new Insets(0, 0, 35, 0));

        Label titleLabel = new Label("Take Assessment!");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 42));
        titleLabel.setTextFill(Color.web(toHex(ACCENT_GREEN)));

        // Top right - User info and links
        HBox topRightPanel = new HBox(30);
        topRightPanel.setAlignment(Pos.CENTER_RIGHT);
        topRightPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";");

        userInfoLabel = new Label("User ID: " + userId);
        userInfoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        userInfoLabel.setTextFill(Color.web(toHex(TEXT_DARK)));
        userInfoLabel.setPadding(new Insets(0, 20, 0, 0));

        Button resultsLink = createHeaderLink("Results");
        // FIXED: Changed from showPanel to showResultsPanel
        resultsLink.setOnAction(e -> parentApp.showResultsPanel());

        topRightPanel.getChildren().addAll(userInfoLabel, resultsLink);
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
        // FIXED: Removed setFont and added to CSS
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

        // User info display
        HBox userPanel = new HBox();
        userPanel.setAlignment(Pos.CENTER_LEFT);
        userPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";");
        userPanel.setPadding(new Insets(0, 0, 30, 0));

        Label userLabel = new Label("Logged in as User ID: " + userId);
        userLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        userLabel.setTextFill(Color.web(toHex(ACCENT_GREEN)));
        userLabel.setStyle(
                "-fx-border-color: #" + toHex(ACCENT_LIGHT_GREEN) + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 5;" +
                        "-fx-padding: 10 15;"
        );

        userPanel.getChildren().add(userLabel);

        // Header container
        VBox headerContainer = new VBox(0);
        headerContainer.setStyle("-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";");
        headerContainer.getChildren().addAll(headerPanel, searchPanel, userPanel);

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

    private void createQuestionPanel() {
        questionPanel = new VBox(20);
        questionPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";");
        questionPanel.setPadding(new Insets(45, 50, 45, 50));
        questionPanel.setVisible(false);

        // Back button
        HBox topPanel = new HBox();
        topPanel.setAlignment(Pos.CENTER_LEFT);
        topPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";");
        topPanel.setPadding(new Insets(0, 0, 30, 0));

        Button backButton = new Button("← Back to Assessments");
        backButton.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
        backButton.setTextFill(Color.web(toHex(ACCENT_GREEN)));
        backButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-width: 0;" +
                        "-fx-cursor: hand;"
        );
        backButton.setOnAction(e -> showSelectionPanel());

        topPanel.getChildren().add(backButton);

        // Question card
        VBox card = new VBox(25);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;"
        );
        card.setPadding(new Insets(50, 60, 50, 60));
        card.setAlignment(Pos.TOP_CENTER);
        card.setMaxWidth(800);
        card.setMaxHeight(600);

        // Question number
        questionNumberLabel = new Label("Question 1 of 10");
        questionNumberLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        questionNumberLabel.setTextFill(Color.web(toHex(TEXT_GRAY)));
        questionNumberLabel.setPadding(new Insets(0, 0, 25, 0));

        // Image display
        assessmentImageView = new ImageView();
        assessmentImageView.setFitWidth(400);
        assessmentImageView.setFitHeight(200);
        assessmentImageView.setPreserveRatio(true);
        assessmentImageView.setStyle(
                "-fx-background-color: #f0f0f0;" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;"
        );

        // Question text
        questionTextLabel = new Label("[question text will appear here]");
        questionTextLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 19));
        questionTextLabel.setTextFill(Color.web(toHex(TEXT_DARK)));
        questionTextLabel.setWrapText(true);
        questionTextLabel.setTextAlignment(TextAlignment.CENTER);
        questionTextLabel.setMaxWidth(600);
        questionTextLabel.setPadding(new Insets(0, 0, 35, 0));

        // Answer combo box - FIXED: Removed setFont and added to CSS
        answerCombo = new ComboBox<>();
        answerCombo.setStyle(
                "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 15px;" +
                        "-fx-background-color: white;" +
                        "-fx-border-color: #" + toHex(ACCENT_GREEN) + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8 15;"
        );
        answerCombo.setPrefWidth(600);
        answerCombo.setPrefHeight(50);

        card.getChildren().addAll(questionNumberLabel, assessmentImageView, questionTextLabel, answerCombo);

        // Center the card
        HBox centerBox = new HBox();
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setStyle("-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";");
        centerBox.getChildren().add(card);
        HBox.setHgrow(card, Priority.NEVER);

        // Navigation buttons
        HBox navPanel = new HBox(25);
        navPanel.setAlignment(Pos.CENTER);
        navPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";");
        navPanel.setPadding(new Insets(30, 0, 0, 0));

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

        questionPanel.getChildren().addAll(topPanel, centerBox, navPanel);
        VBox.setVgrow(centerBox, Priority.ALWAYS);
    }

    private Button createNavButton(String text, Color bgColor) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        button.setTextFill(bgColor == ACCENT_GREEN ? Color.WHITE : Color.web(toHex(TEXT_DARK)));
        button.setStyle(
                "-fx-background-color: #" + toHex(bgColor) + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 12 35;" +
                        "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(e -> {
            if (!button.isDisable()) {
                button.setStyle(
                        "-fx-background-color: #" + toHex(bgColor.darker()) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 12 35;" +
                                "-fx-cursor: hand;"
                );
            }
        });
        button.setOnMouseExited(e -> {
            if (!button.isDisable()) {
                button.setStyle(
                        "-fx-background-color: #" + toHex(bgColor) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 12 35;" +
                                "-fx-cursor: hand;"
                );
            }
        });

        return button;
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
                    assessmentImageView.setFitWidth(400);
                    assessmentImageView.setFitHeight(200);
                    assessmentImageView.setPreserveRatio(true);
                    return;
                }
            } catch (Exception e) {
                System.err.println("Error loading assessment image: " + e.getMessage());
            }
        }

        // If no image or error, show a colored placeholder
        assessmentImageView.setImage(null);
        assessmentImageView.setStyle(
                "-fx-background-color: #" + toHex(getTypeColor(assessment.getType())) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;"
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
            currentQuestions = resultController.getQuestionsByAssessment(currentAssessmentId);

            if (currentQuestions == null || currentQuestions.isEmpty()) {
                showAlert("Error", "No questions found for this assessment!", Alert.AlertType.ERROR);
                return;
            }

            currentQuestionIndex = 0;
            answers.clear();

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

    private void showQuestion(int index) {
        if (currentQuestions == null || index < 0 || index >= currentQuestions.size()) {
            return;
        }

        Question question = currentQuestions.get(index);

        questionNumberLabel.setText("Question " + (index + 1) + " of " + currentQuestions.size());
        questionTextLabel.setText(question.getText());

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
        if (currentQuestionIndex < currentQuestions.size() - 1) {
            saveCurrentAnswer();
            currentQuestionIndex++;
            showQuestion(currentQuestionIndex);
        }
    }

    private void submitAssessment() {
        saveCurrentAnswer();

        if (answers.size() < currentQuestions.size()) {
            showAlert("Incomplete Assessment",
                    "Please answer all questions before submitting!",
                    Alert.AlertType.WARNING);
            return;
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
        // FIXED: Removed initOwner since getScene() doesn't exist in MentisLoginFrame
        // resultDialog.initOwner(parentApp.getScene().getWindow());
        resultDialog.setTitle("Assessment Results");
        resultDialog.setMinWidth(700);
        resultDialog.setMinHeight(800);

        BorderPane mainPanel = new BorderPane();
        mainPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";");

        // Tabbed pane
        TabPane tabbedPane = new TabPane();
        tabbedPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabbedPane.setStyle("-fx-background-color: white;");

        // Tab 1: Summary
        Tab summaryTab = new Tab("📊 Summary");
        summaryTab.setContent(createSummaryTab(result));

        // Tab 2: AI Analysis
        Tab analysisTab = new Tab("🤖 AI Analysis");
        analysisTab.setContent(createAnalysisTab(result));

        // Tab 3: Recommendations
        Tab recommendationsTab = new Tab("💡 Recommendations");
        recommendationsTab.setContent(createRecommendationsTab(result));

        tabbedPane.getTabs().addAll(summaryTab, analysisTab, recommendationsTab);
        mainPanel.setCenter(tabbedPane);

        // Export button panel
        HBox buttonPanel = new HBox(20);
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";");
        buttonPanel.setPadding(new Insets(15, 0, 15, 0));

        Button exportTextBtn = createExportButton("Export as Text File");
        exportTextBtn.setOnAction(e -> exportAsText(result));

        Button exportHTMLBtn = createExportButton("Export as HTML");
        exportHTMLBtn.setOnAction(e -> exportAsHTML(result));

        Button closeBtn = createExportButton("Close");
        closeBtn.setOnAction(e -> {
            resultDialog.close();
            showSelectionPanel();
        });

        buttonPanel.getChildren().addAll(exportTextBtn, exportHTMLBtn, closeBtn);
        mainPanel.setBottom(buttonPanel);

        Scene scene = new Scene(mainPanel, 700, 800);
        resultDialog.setScene(scene);
        resultDialog.showAndWait();
    }

    private VBox createSummaryTab(Map<String, Object> result) {
        VBox panel = new VBox(20);
        panel.setStyle("-fx-background-color: white;");
        panel.setPadding(new Insets(20, 20, 20, 20));

        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setFont(Font.font("Monospaced", 14));
        textArea.setWrapText(true);

        String summary = "=== ASSESSMENT RESULTS ===\n\n" +
                "Total Score: " + result.get("totalScore") + "\n" +
                "Risk Level: " + result.get("riskLevel") + "\n" +
                "Session Suggested: " + (result.get("suggestSession").equals(true) ? "Yes" : "No") + "\n\n" +

                "=== INTERPRETATION ===\n" +
                result.get("interpretation") + "\n\n" +

                "=== KEY INSIGHTS ===\n" +
                result.get("aiAnalysis").toString().split("\n\n")[0];

        textArea.setText(summary);
        VBox.setVgrow(textArea, Priority.ALWAYS);
        panel.getChildren().add(textArea);

        return panel;
    }

    private VBox createAnalysisTab(Map<String, Object> result) {
        VBox panel = new VBox();
        panel.setStyle("-fx-background-color: white;");
        panel.setPadding(new Insets(20, 20, 20, 20));

        TextArea textArea = new TextArea(result.get("aiAnalysis").toString());
        textArea.setEditable(false);
        textArea.setFont(Font.font("Monospaced", 13));
        textArea.setWrapText(true);
        VBox.setVgrow(textArea, Priority.ALWAYS);

        panel.getChildren().add(textArea);
        return panel;
    }

    private VBox createRecommendationsTab(Map<String, Object> result) {
        VBox panel = new VBox();
        panel.setStyle("-fx-background-color: white;");
        panel.setPadding(new Insets(20, 20, 20, 20));

        TextArea textArea = new TextArea(result.get("recommendedContent").toString());
        textArea.setEditable(false);
        textArea.setFont(Font.font("Segoe UI", 14));
        textArea.setWrapText(true);
        VBox.setVgrow(textArea, Priority.ALWAYS);

        panel.getChildren().add(textArea);
        return panel;
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

            // FIXED: Removed initOwner since getScene() doesn't exist in MentisLoginFrame
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

            // FIXED: Removed initOwner since getScene() doesn't exist in MentisLoginFrame
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

        // Clear the assessment image
        if (assessmentImageView != null) {
            assessmentImageView.setImage(null);
            assessmentImageView.setStyle(
                    "-fx-background-color: #fafafa;" +
                            "-fx-border-radius: 5;" +
                            "-fx-background-radius: 5;"
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
        // FIXED: Removed initOwner since getScene() doesn't exist in MentisLoginFrame
        // alert.initOwner(parentApp.getScene().getWindow());
        alert.showAndWait();
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
        if (userInfoLabel != null) {
            userInfoLabel.setText("User ID: " + userId);
        }
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