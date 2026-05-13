package ui;

import controller.SessionReviewController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import models.SessionReview;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MyReviewsPanel extends VBox {

    private MentisLoginFrame parentApp;
    private SessionReviewController reviewController;
    private VBox reviewsContainer;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter reviewDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Symfony-style green colors
    private static final Color PAGE_BG = Color.web("#F8F9FA");
    private static final Color SOFT_GREEN_BG = Color.web("#F1F8E9");
    private static final Color EMERALD = Color.web("#50C878");
    private static final Color EMERALD_DARK = Color.web("#2E7D32");
    private static final Color EMERALD_MID = Color.web("#3A9B5E");
    private static final Color INK = Color.web("#1A3C34");
    private static final Color MUTED = Color.web("#6C757D");
    private static final Color LINE = Color.web("#E9ECEF");
    private static final Color STAR_GOLD = Color.web("#FFC107");
    private static final Color STAR_GREY = Color.web("#CED4DA");

    public MyReviewsPanel(MentisLoginFrame parentApp, SessionReviewController reviewController) {
        this.parentApp = parentApp;
        this.reviewController = reviewController;

        setStyle("-fx-background-color: " + cssColor(PAGE_BG) + ";");
        setPadding(new Insets(44, 56, 44, 56));
        setSpacing(28);

        createHeader();
        createContent();
        refreshData();
    }

    private void createHeader() {
        VBox headerBox = new VBox(8);
        headerBox.setPadding(new Insets(0, 0, 20, 0));

        Label titleLabel = new Label("📝 My Reviews");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 40));
        titleLabel.setTextFill(EMERALD_DARK);

        Label subtitleLabel = new Label("View and manage your session reviews");
        subtitleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        subtitleLabel.setTextFill(MUTED);

        headerBox.getChildren().addAll(titleLabel, subtitleLabel);
        getChildren().add(headerBox);
    }

    private void createContent() {
        reviewsContainer = new VBox(20);
        reviewsContainer.setFillWidth(true);

        ScrollPane scrollPane = new ScrollPane(reviewsContainer);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setBorder(null);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        getChildren().add(scrollPane);
    }

    public void refreshData() {
        reviewsContainer.getChildren().clear();

        try {
            List<SessionReview> reviews = reviewController.getMyReviews(parentApp.getUserId());

            if (reviews.isEmpty()) {
                VBox emptyBox = createEmptyState();
                reviewsContainer.getChildren().add(emptyBox);
                return;
            }

            for (SessionReview review : reviews) {
                VBox reviewCard = createReviewCard(review);
                reviewsContainer.getChildren().add(reviewCard);
            }

        } catch (SQLException e) {
            showAlert("Error", "Failed to load reviews: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private VBox createEmptyState() {
        VBox emptyBox = new VBox(20);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(60));
        emptyBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 24px;" +
                        cardShadow()
        );

        Label iconLabel = new Label("📝");
        iconLabel.setFont(Font.font("Segoe UI Emoji", 54));

        Label titleLabel = new Label("No Reviews Yet");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(INK);

        Label msgLabel = new Label("You haven't written any reviews yet.\nReviews can only be added for past sessions you've attended.");
        msgLabel.setFont(Font.font("Segoe UI", 14));
        msgLabel.setTextFill(MUTED);
        msgLabel.setWrapText(true);
        msgLabel.setAlignment(Pos.CENTER);

        Button browseButton = createPrimaryButton("Go to My Sessions");
        browseButton.setOnAction(e -> parentApp.showPatientMySessionsPanel());

        emptyBox.getChildren().addAll(iconLabel, titleLabel, msgLabel, browseButton);
        return emptyBox;
    }

    private VBox createReviewCard(SessionReview review) {
        VBox card = new VBox(15);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 24px;" +
                        "-fx-padding: 24px;" +
                        cardShadow()
        );

        // Header row
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(review.getSessionTitle());
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titleLabel.setTextFill(INK);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label dateLabel = new Label(review.getSessionDate().format(dateFormatter));
        dateLabel.setFont(Font.font("Segoe UI", 13));
        dateLabel.setTextFill(MUTED);
        dateLabel.setStyle("-fx-background-color: " + cssColor(SOFT_GREEN_BG) + "; -fx-padding: 4px 12px; -fx-background-radius: 20px;");

        headerRow.getChildren().addAll(titleLabel, spacer, dateLabel);

        // Time and location
        HBox metaBox = new HBox(20);
        Label timeLabel = new Label("⏰ " + review.getStartTime().format(timeFormatter) + " - " + review.getEndTime().format(timeFormatter));
        timeLabel.setFont(Font.font("Segoe UI", 13));
        timeLabel.setTextFill(MUTED);

        Label locationLabel = new Label("📍 " + review.getLocation());
        locationLabel.setFont(Font.font("Segoe UI", 13));
        locationLabel.setTextFill(MUTED);

        metaBox.getChildren().addAll(timeLabel, locationLabel);

        // Star rating
        HBox starsBox = createStarDisplay(review.getRating());

        // Comment bubble
        Label commentLabel = new Label("💬 \"" + review.getComment() + "\"");
        commentLabel.setFont(Font.font("Segoe UI", 14));
        commentLabel.setTextFill(INK);
        commentLabel.setWrapText(true);
        commentLabel.setStyle(
                "-fx-background-color: " + cssColor(SOFT_GREEN_BG) + ";" +
                        "-fx-padding: 14px;" +
                        "-fx-background-radius: 16px;"
        );

        // Review date
        Label reviewDateLabel = new Label("Reviewed on: " + review.getReviewDate().format(reviewDateFormatter));
        reviewDateLabel.setFont(Font.font("Segoe UI", 11));
        reviewDateLabel.setTextFill(MUTED);

        // Action buttons
        HBox actionRow = new HBox(12);
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        actionRow.setPadding(new Insets(10, 0, 0, 0));

        Button editButton = createOutlineButton("✏️ Edit");
        editButton.setOnAction(e -> showEditReviewDialog(review));

        Button deleteButton = new Button("🗑️ Delete");
        deleteButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        deleteButton.setTextFill(Color.WHITE);
        deleteButton.setStyle(
                "-fx-background-color: #E74C3C;" +
                        "-fx-background-radius: 999px;" +
                        "-fx-padding: 8px 20px;" +
                        "-fx-cursor: hand;"
        );
        deleteButton.setOnAction(e -> deleteReview(review));

        actionRow.getChildren().addAll(editButton, deleteButton);

        card.getChildren().addAll(headerRow, metaBox, starsBox, commentLabel, reviewDateLabel, new Separator(), actionRow);
        return card;
    }

    private HBox createStarDisplay(int rating) {
        HBox starsBox = new HBox(4);
        starsBox.setAlignment(Pos.CENTER_LEFT);
        for (int i = 1; i <= 5; i++) {
            Label star = new Label("★");
            star.setFont(Font.font("Segoe UI", 18));
            if (i <= rating) {
                star.setTextFill(STAR_GOLD);
            } else {
                star.setTextFill(STAR_GREY);
            }
            starsBox.getChildren().add(star);
        }
        return starsBox;
    }

    private Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        button.setTextFill(Color.WHITE);
        button.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + cssColor(EMERALD) + ", " + cssColor(EMERALD_MID) + ");" +
                        "-fx-background-radius: 999px;" +
                        "-fx-padding: 12px 28px;" +
                        "-fx-cursor: hand;" +
                        cardShadow()
        );
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + cssColor(EMERALD_MID) + ", " + cssColor(EMERALD_DARK) + ");" +
                        "-fx-background-radius: 999px;" +
                        "-fx-padding: 12px 28px;" +
                        "-fx-cursor: hand;" +
                        "-fx-translate-y: -2px;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + cssColor(EMERALD) + ", " + cssColor(EMERALD_MID) + ");" +
                        "-fx-background-radius: 999px;" +
                        "-fx-padding: 12px 28px;" +
                        "-fx-cursor: hand;" +
                        cardShadow()
        ));
        return button;
    }

    private Button createOutlineButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        button.setTextFill(EMERALD_DARK);
        button.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 999px;" +
                        "-fx-border-color: " + cssColor(EMERALD) + ";" +
                        "-fx-border-radius: 999px;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-padding: 8px 20px;" +
                        "-fx-cursor: hand;"
        );
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: " + cssColor(SOFT_GREEN_BG) + ";" +
                        "-fx-background-radius: 999px;" +
                        "-fx-border-color: " + cssColor(EMERALD_DARK) + ";" +
                        "-fx-border-radius: 999px;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-padding: 8px 20px;" +
                        "-fx-cursor: hand;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 999px;" +
                        "-fx-border-color: " + cssColor(EMERALD) + ";" +
                        "-fx-border-radius: 999px;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-padding: 8px 20px;" +
                        "-fx-cursor: hand;"
        ));
        return button;
    }

    private void showEditReviewDialog(SessionReview review) {
        new AddReviewDialog(parentApp, reviewController, review, true);
        refreshData();
    }

    private void deleteReview(SessionReview review) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Review");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("Do you want to delete your review for:\n\n" +
                review.getSessionTitle() + " on " +
                review.getSessionDate().format(dateFormatter));

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    reviewController.deleteReview(review.getReviewId(), parentApp.getUserId());
                    showAlert("Success", "Review deleted successfully!", Alert.AlertType.INFORMATION);
                    refreshData();
                } catch (SQLException e) {
                    showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String cssColor(Color color) {
        return "#" + toHex(color);
    }

    private String cardShadow() {
        return "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 5);";
    }

    private String toHex(Color color) {
        return String.format("%02x%02x%02x",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}