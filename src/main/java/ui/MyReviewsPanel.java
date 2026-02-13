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
    private Label userInfoLabel;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter reviewDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Colors
    private static final Color BACKGROUND_LIGHT = Color.rgb(240, 248, 245);
    private static final Color ACCENT_DARK_GREEN = Color.rgb(60, 120, 90);
    private static final Color BUTTON_LIGHT_GREEN = Color.rgb(160, 200, 180);
    private static final Color TEXT_DARK = Color.rgb(40, 70, 50);
    private static final Color TEXT_LIGHT = Color.rgb(100, 130, 110);
    private static final Color BORDER_LIGHT = Color.rgb(200, 220, 210);
    private static final Color STAR_GOLD = Color.rgb(255, 193, 7);
    private static final Color STAR_GREY = Color.rgb(200, 200, 200);

    public MyReviewsPanel(MentisLoginFrame parentApp, SessionReviewController reviewController) {
        this.parentApp = parentApp;
        this.reviewController = reviewController;

        setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        setPadding(new Insets(30));
        setSpacing(20);

        createHeader();
        createContent();
        refreshData();
    }

    private void createHeader() {
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 20, 0));

        VBox titleBox = new VBox(10);
        Label titleLabel = new Label("My Reviews");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        Label subtitleLabel = new Label("View and manage your session reviews");
        subtitleLabel.setFont(Font.font("Segoe UI", 16));
        subtitleLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        userInfoLabel = new Label(parentApp.getUserName());
        userInfoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        userInfoLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        headerBox.getChildren().addAll(titleBox, spacer, userInfoLabel);
        getChildren().add(headerBox);
    }

    private void createContent() {
        reviewsContainer = new VBox(15);
        reviewsContainer.setFillWidth(true);

        ScrollPane scrollPane = new ScrollPane(reviewsContainer);
        scrollPane.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
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

            // Show count
            Label countLabel = new Label("You have written " + reviews.size() + " review(s)");
            countLabel.setFont(Font.font("Segoe UI", 12));
            countLabel.setTextFill(Color.web(toHex(TEXT_LIGHT)));
            countLabel.setPadding(new Insets(5, 0, 0, 0));
            reviewsContainer.getChildren().add(countLabel);

        } catch (SQLException e) {
            showAlert("Error", "Failed to load reviews: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private VBox createEmptyState() {
        VBox emptyBox = new VBox(20);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(50));
        emptyBox.setStyle("-fx-background-color: white; -fx-border-radius: 10; -fx-background-radius: 10;");

        Label titleLabel = new Label("📝 No Reviews Yet");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        Label msgLabel = new Label("You haven't written any reviews yet.\nReviews can only be added for past sessions you've attended.");
        msgLabel.setFont(Font.font("Segoe UI", 14));
        msgLabel.setTextFill(Color.web(toHex(TEXT_LIGHT)));
        msgLabel.setWrapText(true);
        msgLabel.setAlignment(Pos.CENTER);
        msgLabel.setMaxWidth(400);

        Button browseButton = new Button("Go to My Sessions");
        browseButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        browseButton.setTextFill(Color.WHITE);
        browseButton.setStyle("-fx-background-color: #" + toHex(ACCENT_DARK_GREEN) + "; -fx-background-radius: 5; -fx-padding: 10 25; -fx-cursor: hand;");
        browseButton.setOnAction(e -> parentApp.showPatientMySessionsPanel());

        emptyBox.getChildren().addAll(titleLabel, msgLabel, browseButton);
        return emptyBox;
    }

    private VBox createReviewCard(SessionReview review) {
        VBox card = new VBox(15);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 20;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);"
        );

        // Header with session title and date
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(review.getSessionTitle());
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        Label dateLabel = new Label(review.getSessionDate().format(dateFormatter));
        dateLabel.setFont(Font.font("Segoe UI", 14));
        dateLabel.setTextFill(Color.web(toHex(TEXT_LIGHT)));

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        // Star rating
        HBox starsBox = createStarDisplay(review.getRating());

        headerRow.getChildren().addAll(titleLabel, dateLabel, spacer1, starsBox);

        // Review details
        VBox detailsBox = new VBox(10);
        detailsBox.setPadding(new Insets(10, 0, 10, 0));

        // Time and location
        HBox metaBox = new HBox(20);
        Label timeLabel = new Label("⏰ " + review.getStartTime().format(timeFormatter) + " - " + review.getEndTime().format(timeFormatter));
        timeLabel.setFont(Font.font("Segoe UI", 13));
        timeLabel.setTextFill(Color.web(toHex(TEXT_LIGHT)));

        Label locationLabel = new Label("📍 " + review.getLocation());
        locationLabel.setFont(Font.font("Segoe UI", 13));
        locationLabel.setTextFill(Color.web(toHex(TEXT_LIGHT)));

        metaBox.getChildren().addAll(timeLabel, locationLabel);

        // Comment
        Label commentLabel = new Label("\"" + review.getComment() + "\"");
        commentLabel.setFont(Font.font("Segoe UI", 14));
        commentLabel.setTextFill(Color.web(toHex(TEXT_DARK)));
        commentLabel.setWrapText(true);
        commentLabel.setMaxWidth(600);

        // Review date
        Label reviewDateLabel = new Label("Reviewed on: " + review.getReviewDate().format(reviewDateFormatter));
        reviewDateLabel.setFont(Font.font("Segoe UI", 12));
        reviewDateLabel.setTextFill(Color.web(toHex(TEXT_LIGHT)));

        detailsBox.getChildren().addAll(metaBox, commentLabel, reviewDateLabel);

        // Action buttons
        HBox actionRow = new HBox(10);
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        actionRow.setPadding(new Insets(10, 0, 0, 0));

        Button editButton = new Button("Edit");
        editButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        editButton.setTextFill(Color.WHITE);
        editButton.setStyle("-fx-background-color: #" + toHex(ACCENT_DARK_GREEN) + "; -fx-background-radius: 5; -fx-padding: 8 20; -fx-cursor: hand;");
        editButton.setOnAction(e -> showEditReviewDialog(review));

        Button deleteButton = new Button("Delete");
        deleteButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        deleteButton.setTextFill(Color.WHITE);
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-background-radius: 5; -fx-padding: 8 20; -fx-cursor: hand;");
        deleteButton.setOnAction(e -> deleteReview(review));

        actionRow.getChildren().addAll(editButton, deleteButton);

        card.getChildren().addAll(headerRow, detailsBox, new Separator(), actionRow);
        return card;
    }

    private HBox createStarDisplay(int rating) {
        HBox starsBox = new HBox(2);
        for (int i = 1; i <= 5; i++) {
            Label star = new Label("★");
            star.setFont(Font.font("Segoe UI", 18));
            if (i <= rating) {
                star.setTextFill(Color.web(toHex(STAR_GOLD)));
            } else {
                star.setTextFill(Color.web(toHex(STAR_GREY)));
            }
            starsBox.getChildren().add(star);
        }
        return starsBox;
    }

    private void showEditReviewDialog(SessionReview review) {
        new AddReviewDialog(parentApp, reviewController, review, true);
        refreshData(); // Refresh after dialog closes
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

    private String toHex(Color color) {
        return String.format("%02x%02x%02x",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}