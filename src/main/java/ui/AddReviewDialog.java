package ui;

import controller.SessionReviewController;
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
import models.Session;
import models.SessionReview;
import services.ReviewModeratorService;
import services.ModerationResult;

import java.sql.SQLException;

public class AddReviewDialog extends Stage {

    private MentisLoginFrame parentApp;
    private SessionReviewController reviewController;
    private SessionReview existingReview;
    private Session session;
    private boolean isEdit;
    private ReviewModeratorService moderatorService; // ⭐ NEW

    private ComboBox<Integer> ratingCombo;
    private TextArea commentArea;
    private Label sessionInfoLabel;
    private Label moderationStatusLabel; // ⭐ NEW

    // Colors
    private static final Color BACKGROUND_LIGHT = Color.rgb(240, 248, 245);
    private static final Color ACCENT_DARK_GREEN = Color.rgb(60, 120, 90);
    private static final Color BUTTON_LIGHT_GREEN = Color.rgb(160, 200, 180);
    private static final Color TEXT_DARK = Color.rgb(40, 70, 50);
    private static final Color BORDER_LIGHT = Color.rgb(200, 220, 210);
    private static final Color STAR_GOLD = Color.rgb(255, 193, 7);
    private static final Color WARNING_RED = Color.rgb(231, 76, 60); // ⭐ NEW
    private static final Color INFO_BLUE = Color.rgb(52, 152, 219); // ⭐ NEW

    // Constructor for new review
    public AddReviewDialog(MentisLoginFrame parentApp, SessionReviewController reviewController, Session session) {
        this.parentApp = parentApp;
        this.reviewController = reviewController;
        this.session = session;
        this.isEdit = false;
        this.moderatorService = new ReviewModeratorService(); // ⭐ NEW

        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        setTitle("Write a Review");

        createUI();
        showAndWait();
    }

    // Constructor for editing existing review
    public AddReviewDialog(MentisLoginFrame parentApp, SessionReviewController reviewController,
                           SessionReview review, boolean isEdit) {
        this.parentApp = parentApp;
        this.reviewController = reviewController;
        this.existingReview = review;
        this.isEdit = isEdit;
        this.moderatorService = new ReviewModeratorService(); // ⭐ NEW

        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        setTitle(isEdit ? "Edit Review" : "Write a Review");

        createUI();
        if (isEdit && existingReview != null) {
            loadReviewData();
        }
        showAndWait();
    }

    private void createUI() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

        // Header
        root.setTop(createHeader());

        // Form
        root.setCenter(createForm());

        // Buttons
        root.setBottom(createButtonPanel());

        Scene scene = new Scene(root, 550, 650); // ⭐ Made slightly taller
        setScene(scene);
        setResizable(false);
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 30, 20, 30));
        header.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

        String titleText = isEdit ? "Edit Your Review" : "Write a Review";
        Label titleLabel = new Label(titleText);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        header.getChildren().add(titleLabel);
        return header;
    }

    private ScrollPane createForm() {
        VBox formPanel = new VBox(20);
        formPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        formPanel.setPadding(new Insets(20, 30, 20, 30));

        // Session Info
        VBox sessionBox = new VBox(10);
        sessionBox.setStyle("-fx-background-color: white; -fx-border-color: #" + toHex(BORDER_LIGHT) +
                "; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 15;");

        Label sessionTitle = new Label("Session Details");
        sessionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        sessionTitle.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        if (session != null) {
            sessionInfoLabel = new Label(
                    session.getTitle() + "\n" +
                            session.getSessionDate() + " at " + session.getStartTime() + " - " + session.getEndTime() + "\n" +
                            session.getLocation()
            );
        } else if (existingReview != null) {
            sessionInfoLabel = new Label(
                    existingReview.getSessionTitle() + "\n" +
                            existingReview.getSessionDate() + " at " + existingReview.getStartTime() + " - " + existingReview.getEndTime() + "\n" +
                            existingReview.getLocation()
            );
        }
        sessionInfoLabel.setFont(Font.font("Segoe UI", 14));
        sessionInfoLabel.setTextFill(Color.web(toHex(TEXT_DARK)));
        sessionInfoLabel.setWrapText(true);

        sessionBox.getChildren().addAll(sessionTitle, sessionInfoLabel);

        // Rating
        VBox ratingBox = new VBox(10);
        Label ratingLabel = new Label("Rating:");
        ratingLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        ratingLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        HBox ratingStars = new HBox(10);
        ratingStars.setAlignment(Pos.CENTER_LEFT);

        ratingCombo = new ComboBox<>();
        ratingCombo.getItems().addAll(1, 2, 3, 4, 5);
        ratingCombo.setValue(5);
        ratingCombo.setPrefHeight(40);
        ratingCombo.setPrefWidth(80);
        ratingCombo.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;"
        );

        Label starPreview = new Label("★★★★★");
        starPreview.setFont(Font.font("Segoe UI", 24));
        starPreview.setTextFill(Color.web(toHex(STAR_GOLD)));

        ratingCombo.setOnAction(e -> {
            int rating = ratingCombo.getValue();
            String stars = "★".repeat(rating) + "☆".repeat(5 - rating);
            starPreview.setText(stars);
        });

        ratingStars.getChildren().addAll(ratingCombo, starPreview);
        ratingBox.getChildren().addAll(ratingLabel, ratingStars);

        // Comment
        VBox commentBox = new VBox(10);
        Label commentLabel = new Label("Your Review:");
        commentLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        commentLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        commentArea = new TextArea();
        commentArea.setPromptText("Share your experience about this session...");
        commentArea.setPrefRowCount(5);
        commentArea.setWrapText(true);
        commentArea.setStyle(
                "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8;"
        );

        // ⭐ NEW: Moderation status label (initially hidden)
        moderationStatusLabel = new Label();
        moderationStatusLabel.setFont(Font.font("Segoe UI", 12));
        moderationStatusLabel.setWrapText(true);
        moderationStatusLabel.setPadding(new Insets(5));
        moderationStatusLabel.setVisible(false);

        // ⭐ NEW: Real-time moderation check as user types (optional)
        commentArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 10) { // Only check after 10 characters
                // You could add a "checking" indicator here
            }
        });

        commentBox.getChildren().addAll(commentLabel, commentArea, moderationStatusLabel);

        formPanel.getChildren().addAll(sessionBox, ratingBox, commentBox);

        ScrollPane scrollPane = new ScrollPane(formPanel);
        scrollPane.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        scrollPane.setBorder(null);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return scrollPane;
    }

    private HBox createButtonPanel() {
        HBox buttonPanel = new HBox(15);
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);
        buttonPanel.setPadding(new Insets(20, 30, 20, 30));
        buttonPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

        Button cancelButton = createButton("Cancel", BUTTON_LIGHT_GREEN);
        cancelButton.setOnAction(e -> close());

        Button saveButton = createButton(isEdit ? "Save Changes" : "Submit Review", ACCENT_DARK_GREEN);
        saveButton.setTextFill(Color.WHITE);
        saveButton.setOnAction(e -> saveReview());

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

    private void loadReviewData() {
        if (existingReview != null) {
            ratingCombo.setValue(existingReview.getRating());
            commentArea.setText(existingReview.getComment());
        }
    }

    // ⭐ NEW: AI-Powered review moderation
    private void saveReview() {
        // Validate
        if (commentArea.getText().trim().isEmpty()) {
            showAlert("Please write a review comment.", Alert.AlertType.WARNING);
            return;
        }

        int rating = ratingCombo.getValue();
        String comment = commentArea.getText().trim();

        // Show loading status
        moderationStatusLabel.setText("⏳ AI is checking your review...");
        moderationStatusLabel.setTextFill(Color.web(toHex(INFO_BLUE)));
        moderationStatusLabel.setVisible(true);

        // Run moderation in background to not freeze UI
        new Thread(() -> {
            try {
                // Call AI moderation service
                ModerationResult result = moderatorService.moderateReview(comment);

                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    moderationStatusLabel.setVisible(false);

                    if (!result.isAppropriate()) {
                        // Show warning dialog with AI suggestions
                        showModerationWarning(result);
                    } else {
                        // Review is appropriate, proceed with saving
                        proceedWithSave(rating, comment);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    moderationStatusLabel.setVisible(false);
                    // If AI fails, still allow saving but warn user
                    Alert warning = new Alert(Alert.AlertType.WARNING);
                    warning.setTitle("Moderation Service Unavailable");
                    warning.setHeaderText("Could not check review content");
                    warning.setContentText("Your review will be submitted without moderation. Please ensure it follows our guidelines.");
                    warning.initOwner(this);

                    ButtonType continueButton = new ButtonType("Continue Anyway");
                    ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                    warning.getButtonTypes().setAll(continueButton, cancelButton);

                    warning.showAndWait().ifPresent(response -> {
                        if (response == continueButton) {
                            proceedWithSave(rating, comment);
                        }
                    });
                });
            }
        }).start();
    }

    // ⭐ NEW: Handle inappropriate content
    private void showModerationWarning(ModerationResult result) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Review Content Warning");
        alert.setHeaderText("⚠️ Inappropriate Content Detected");

        String details = String.format(
                "Our AI has detected that your review may contain inappropriate language.\n\n" +
                        "📋 **Reason**: %s\n\n" +
                        "🔍 **Issues detected**:\n" +
                        "%s%s%s\n\n" +
                        "💡 **Suggested version**:\n\"%s\"\n\n" +
                        "What would you like to do?",
                result.getReason(),
                result.isContainsProfanity() ? "   • Contains profanity\n" : "",
                result.isContainsHateSpeech() ? "   • Contains hate speech\n" : "",
                result.isContainsHarassment() ? "   • Contains harassment\n" : "",
                result.getFilteredVersion()
        );

        Label contentLabel = new Label(details);
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(450);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setContent(contentLabel);
        dialogPane.setPrefWidth(500);

        ButtonType editButton = new ButtonType("✏️ Edit My Review");
        ButtonType useSuggestedButton = new ButtonType("✅ Use Suggested Version");
        ButtonType cancelButton = new ButtonType("❌ Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(editButton, useSuggestedButton, cancelButton);
        alert.initOwner(this);

        alert.showAndWait().ifPresent(response -> {
            if (response == editButton) {
                // Stay in dialog to edit
                moderationStatusLabel.setText("Please revise your review to be more respectful.");
                moderationStatusLabel.setTextFill(Color.web(toHex(WARNING_RED)));
                moderationStatusLabel.setVisible(true);
            } else if (response == useSuggestedButton) {
                // Use the filtered version and save
                commentArea.setText(result.getFilteredVersion());
                int newRating = ratingCombo.getValue();
                proceedWithSave(newRating, result.getFilteredVersion());
            }
            // Cancel just closes the dialog
        });
    }

    // ⭐ NEW: Proceed with saving after moderation passes
    private void proceedWithSave(int rating, String comment) {
        try {
            if (isEdit && existingReview != null) {
                reviewController.updateReview(
                        existingReview.getReviewId(),
                        parentApp.getUserId(),
                        rating,
                        comment
                );
                showAlert("Review updated successfully!", Alert.AlertType.INFORMATION);
            } else {
                reviewController.addReview(
                        session.getSessionId(),
                        parentApp.getUserId(),
                        rating,
                        comment
                );
                showAlert("Review submitted successfully!", Alert.AlertType.INFORMATION);
            }
            close();

        } catch (SQLException e) {
            showAlert("Error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Error" :
                type == Alert.AlertType.WARNING ? "Warning" : "Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(this);
        alert.showAndWait();
    }

    private String toHex(Color color) {
        return String.format("%02x%02x%02x",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }
}