package ui;

import controller.SessionController;
import controller.SessionReviewController;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Session;
import utils.QRCodeGenerator;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PatientMySessionsPanel extends VBox {

    private MentisLoginFrame parentApp;
    private SessionController sessionController;
    private SessionReviewController reviewController;
    private VBox upcomingContainer;
    private VBox pastContainer;
    private ScrollPane mainScrollPane;
    private VBox contentContainer;
    private ToggleGroup viewToggle;
    private Label userInfoLabel;
    private HBox toggleBox;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    // Colors
    private static final Color BACKGROUND_LIGHT = Color.rgb(240, 248, 245);
    private static final Color ACCENT_DARK_GREEN = Color.rgb(60, 120, 90);
    private static final Color BUTTON_LIGHT_GREEN = Color.rgb(160, 200, 180);
    private static final Color TEXT_DARK = Color.rgb(40, 70, 50);
    private static final Color TEXT_LIGHT = Color.rgb(100, 130, 110);
    private static final Color BORDER_LIGHT = Color.rgb(200, 220, 210);
    private static final Color CARD_WHITE = Color.WHITE;
    private static final Color REVIEW_BUTTON_COLOR = Color.rgb(255, 193, 7);
    private static final Color QR_BUTTON_COLOR = Color.rgb(155, 89, 182); // Purple for QR code

    public PatientMySessionsPanel(MentisLoginFrame parentApp, SessionController sessionController) {
        this.parentApp = parentApp;
        this.sessionController = sessionController;
        this.reviewController = new SessionReviewController();

        setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        setPadding(new Insets(30));
        setSpacing(20);

        createHeader();
        createToggleView();
        createContentContainer();
        refreshData();
    }

    private void createHeader() {
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 20, 0));

        VBox titleBox = new VBox(10);
        Label titleLabel = new Label("My Sessions");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        Label subtitleLabel = new Label("View your upcoming and past session reservations");
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

    private void createToggleView() {
        toggleBox = new HBox(20);
        toggleBox.setAlignment(Pos.CENTER_LEFT);
        toggleBox.setPadding(new Insets(0, 0, 20, 0));

        viewToggle = new ToggleGroup();

        RadioButton upcomingRadio = new RadioButton("Upcoming Sessions");
        upcomingRadio.setToggleGroup(viewToggle);
        upcomingRadio.setSelected(true);
        upcomingRadio.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        upcomingRadio.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        RadioButton pastRadio = new RadioButton("Past Sessions");
        pastRadio.setToggleGroup(viewToggle);
        pastRadio.setFont(Font.font("Segoe UI", 14));
        pastRadio.setTextFill(Color.web(toHex(TEXT_DARK)));

        viewToggle.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == upcomingRadio) {
                showUpcomingSessions();
            } else if (newVal == pastRadio) {
                showPastSessions();
            }
        });

        toggleBox.getChildren().addAll(upcomingRadio, pastRadio);
        getChildren().add(toggleBox);
    }

    private void createContentContainer() {
        contentContainer = new VBox(15);
        contentContainer.setFillWidth(true);

        upcomingContainer = new VBox(15);
        upcomingContainer.setFillWidth(true);

        pastContainer = new VBox(15);
        pastContainer.setFillWidth(true);
        pastContainer.setVisible(false);
        pastContainer.setManaged(false);

        contentContainer.getChildren().addAll(upcomingContainer, pastContainer);

        mainScrollPane = new ScrollPane(contentContainer);
        mainScrollPane.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        mainScrollPane.setBorder(null);
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox.setVgrow(mainScrollPane, Priority.ALWAYS);
        getChildren().add(mainScrollPane);
    }

    private void showUpcomingSessions() {
        upcomingContainer.setVisible(true);
        upcomingContainer.setManaged(true);
        pastContainer.setVisible(false);
        pastContainer.setManaged(false);

        try {
            List<Session> upcomingSessions = sessionController.getPatientUpcomingSessions(parentApp.getUserId());
            displayUpcomingSessions(upcomingSessions);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load upcoming sessions: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        mainScrollPane.setVvalue(0);
    }

    private void showPastSessions() {
        upcomingContainer.setVisible(false);
        upcomingContainer.setManaged(false);
        pastContainer.setVisible(true);
        pastContainer.setManaged(true);

        try {
            List<Session> pastSessions = sessionController.getPatientPastSessions(parentApp.getUserId());
            displayPastSessions(pastSessions);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load past sessions: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        mainScrollPane.setVvalue(0);
    }

    public void refreshData() {
        try {
            List<Session> upcomingSessions = sessionController.getPatientUpcomingSessions(parentApp.getUserId());
            displayUpcomingSessions(upcomingSessions);

            List<Session> pastSessions = sessionController.getPatientPastSessions(parentApp.getUserId());
            displayPastSessions(pastSessions);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load sessions: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void displayUpcomingSessions(List<Session> sessions) {
        upcomingContainer.getChildren().clear();

        if (sessions.isEmpty()) {
            VBox emptyBox = createEmptyState("📅 No upcoming sessions",
                    "You haven't reserved any upcoming sessions yet. Check Available Sessions to book!", true);
            upcomingContainer.getChildren().add(emptyBox);
            return;
        }

        for (Session session : sessions) {
            VBox sessionCard = createUpcomingSessionCard(session);
            upcomingContainer.getChildren().add(sessionCard);
        }

        Label countLabel = new Label("You have " + sessions.size() + " upcoming session(s)");
        countLabel.setFont(Font.font("Segoe UI", 12));
        countLabel.setTextFill(Color.web(toHex(TEXT_LIGHT)));
        countLabel.setPadding(new Insets(5, 0, 0, 0));
        upcomingContainer.getChildren().add(countLabel);
    }

    private void displayPastSessions(List<Session> sessions) {
        pastContainer.getChildren().clear();

        if (sessions.isEmpty()) {
            VBox emptyBox = createEmptyState("📋 No past sessions",
                    "You haven't attended any sessions yet. They will appear here after the session date.", false);
            pastContainer.getChildren().add(emptyBox);
            return;
        }

        for (Session session : sessions) {
            VBox sessionCard = createPastSessionCard(session);
            pastContainer.getChildren().add(sessionCard);
        }

        Label countLabel = new Label("You have attended " + sessions.size() + " session(s)");
        countLabel.setFont(Font.font("Segoe UI", 12));
        countLabel.setTextFill(Color.web(toHex(TEXT_LIGHT)));
        countLabel.setPadding(new Insets(5, 0, 0, 0));
        pastContainer.getChildren().add(countLabel);
    }

    private VBox createEmptyState(String title, String message, boolean isUpcoming) {
        VBox emptyBox = new VBox(20);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(50));
        emptyBox.setStyle("-fx-background-color: white; -fx-border-radius: 10; -fx-background-radius: 10;");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        Label msgLabel = new Label(message);
        msgLabel.setFont(Font.font("Segoe UI", 14));
        msgLabel.setTextFill(Color.web(toHex(TEXT_LIGHT)));
        msgLabel.setWrapText(true);
        msgLabel.setAlignment(Pos.CENTER);
        msgLabel.setMaxWidth(400);

        emptyBox.getChildren().addAll(titleLabel, msgLabel);

        if (isUpcoming) {
            Button browseButton = new Button("Browse Available Sessions");
            browseButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            browseButton.setTextFill(Color.WHITE);
            browseButton.setStyle("-fx-background-color: #" + toHex(ACCENT_DARK_GREEN) + "; -fx-background-radius: 5; -fx-padding: 10 25; -fx-cursor: hand;");
            browseButton.setOnAction(e -> parentApp.showPatientAvailableSessionsPanel());
            emptyBox.getChildren().add(browseButton);
        }

        return emptyBox;
    }

    // ⭐ UPDATED: Added QR Code button to upcoming sessions
    private VBox createUpcomingSessionCard(Session session) {
        VBox card = new VBox(15);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 20;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);"
        );

        // First row: Title and Type
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(session.getTitle());
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        Label typeLabel = new Label(session.getSessionType());
        typeLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        typeLabel.setTextFill(Color.WHITE);
        typeLabel.setStyle("-fx-background-color: #" + toHex(getTypeColor(session.getSessionType())) + "; -fx-background-radius: 15; -fx-padding: 5 15;");

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        titleRow.getChildren().addAll(titleLabel, spacer1, typeLabel);

        // Second row: Date, Time, Location
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(30);
        detailsGrid.setVgap(10);

        HBox dateBox = new HBox(5, new Label("📅"), new Label(session.getSessionDate().format(dateFormatter)));
        HBox timeBox = new HBox(5, new Label("⏰"), new Label(session.getStartTime().format(timeFormatter) + " - " + session.getEndTime().format(timeFormatter)));
        HBox locationBox = new HBox(5, new Label("📍"), new Label(session.getLocation()));

        detailsGrid.add(dateBox, 0, 0);
        detailsGrid.add(timeBox, 1, 0);
        detailsGrid.add(locationBox, 0, 1, 2, 1);

        // Third row: Status and buttons
        HBox actionRow = new HBox(15);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        Label statusLabel = new Label("● Reserved");
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        statusLabel.setTextFill(Color.web(toHex(Color.rgb(52, 152, 219))));

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        Button detailsButton = new Button("View Details");
        detailsButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        detailsButton.setTextFill(Color.web(toHex(TEXT_DARK)));
        detailsButton.setStyle("-fx-background-color: #" + toHex(BUTTON_LIGHT_GREEN) + "; -fx-background-radius: 5; -fx-padding: 8 20; -fx-cursor: hand;");
        detailsButton.setOnAction(e -> showSessionDetails(session));

        // ⭐ NEW: QR Code button
        Button qrButton = new Button("QR Code");
        qrButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        qrButton.setTextFill(Color.WHITE);
        qrButton.setStyle("-fx-background-color: #" + toHex(QR_BUTTON_COLOR) + "; -fx-background-radius: 5; -fx-padding: 8 20; -fx-cursor: hand;");
        qrButton.setOnAction(e -> showQRCode(session));

        // Only show cancel button for future sessions
        if (session.getSessionDate().isAfter(LocalDate.now()) ||
                session.getSessionDate().isEqual(LocalDate.now())) {
            Button cancelButton = new Button("Cancel");
            cancelButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            cancelButton.setTextFill(Color.WHITE);
            cancelButton.setStyle("-fx-background-color: #e74c3c; -fx-background-radius: 5; -fx-padding: 8 20; -fx-cursor: hand;");
            cancelButton.setOnAction(e -> cancelReservation(session));
            actionRow.getChildren().add(cancelButton);
        }

        actionRow.getChildren().addAll(statusLabel, spacer2, qrButton, detailsButton);

        card.getChildren().addAll(titleRow, detailsGrid, actionRow);
        return card;
    }

    private VBox createPastSessionCard(Session session) {
        VBox card = new VBox(15);
        card.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 20;" +
                        "-fx-opacity: 0.9;"
        );

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(session.getTitle());
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web(toHex(TEXT_LIGHT)));

        Label typeLabel = new Label(session.getSessionType());
        typeLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        typeLabel.setTextFill(Color.WHITE);
        typeLabel.setStyle("-fx-background-color: #" + toHex(getTypeColor(session.getSessionType())) + "; -fx-background-radius: 15; -fx-padding: 5 15; -fx-opacity: 0.7;");

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        titleRow.getChildren().addAll(titleLabel, spacer1, typeLabel);

        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(30);
        detailsGrid.setVgap(10);

        HBox dateBox = new HBox(5, new Label("📅"), new Label(session.getSessionDate().format(dateFormatter)));
        HBox timeBox = new HBox(5, new Label("⏰"), new Label(session.getStartTime().format(timeFormatter) + " - " + session.getEndTime().format(timeFormatter)));
        HBox locationBox = new HBox(5, new Label("📍"), new Label(session.getLocation()));

        detailsGrid.add(dateBox, 0, 0);
        detailsGrid.add(timeBox, 1, 0);
        detailsGrid.add(locationBox, 0, 1, 2, 1);

        HBox actionRow = new HBox(15);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        Label statusLabel = new Label("✓ Completed");
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        statusLabel.setTextFill(Color.web(toHex(Color.rgb(39, 174, 96))));

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        Button detailsButton = new Button("View Details");
        detailsButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        detailsButton.setTextFill(Color.web(toHex(TEXT_DARK)));
        detailsButton.setStyle("-fx-background-color: #" + toHex(BUTTON_LIGHT_GREEN) + "; -fx-background-radius: 5; -fx-padding: 8 20; -fx-cursor: hand;");
        detailsButton.setOnAction(e -> showSessionDetails(session));

        Button reviewButton = new Button("Write Review");
        reviewButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        reviewButton.setTextFill(Color.WHITE);
        reviewButton.setStyle("-fx-background-color: #" + toHex(REVIEW_BUTTON_COLOR) + "; -fx-background-radius: 5; -fx-padding: 8 20; -fx-cursor: hand;");
        reviewButton.setOnAction(e -> openReviewDialog(session));

        actionRow.getChildren().addAll(statusLabel, spacer2, reviewButton, detailsButton);

        card.getChildren().addAll(titleRow, detailsGrid, actionRow);
        return card;
    }

    // ⭐ NEW: Method to show QR Code
    private void showQRCode(Session session) {
        try {
            // Generate QR code
            byte[] qrBytes = QRCodeGenerator.getQRCodeBytes(session, parentApp.getUserId());
            if (qrBytes != null) {
                // Convert to JavaFX Image
                java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(qrBytes);
                Image qrImage = new Image(bis);

                // Show in dialog
                Stage qrStage = new Stage();
                qrStage.initModality(Modality.APPLICATION_MODAL);
                qrStage.setTitle("QR Code - " + session.getTitle());

                VBox root = new VBox(20);
                root.setAlignment(Pos.CENTER);
                root.setPadding(new Insets(30));
                root.setStyle("-fx-background-color: white;");

                ImageView qrView = new ImageView(qrImage);
                qrView.setFitWidth(300);
                qrView.setFitHeight(300);
                qrView.setPreserveRatio(true);

                Label titleLabel = new Label(session.getTitle());
                titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

                Label infoLabel = new Label("Show this QR code at the session for check-in");
                infoLabel.setFont(Font.font("Segoe UI", 12));
                infoLabel.setTextFill(Color.web(toHex(TEXT_LIGHT)));

                Button closeButton = new Button("Close");
                closeButton.setOnAction(e -> qrStage.close());
                closeButton.setStyle("-fx-background-color: #" + toHex(ACCENT_DARK_GREEN) + "; -fx-text-fill: white; -fx-padding: 10 25; -fx-cursor: hand;");

                root.getChildren().addAll(titleLabel, qrView, infoLabel, closeButton);

                Scene scene = new Scene(root, 400, 500);
                qrStage.setScene(scene);
                qrStage.showAndWait();
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to generate QR code: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void openReviewDialog(Session session) {
        try {
            boolean hasReviewed = reviewController.hasReviewed(session.getSessionId(), parentApp.getUserId());

            if (hasReviewed) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Review Already Exists");
                alert.setHeaderText("You have already reviewed this session");
                alert.setContentText("Would you like to view all your reviews?");

                ButtonType viewButton = new ButtonType("View My Reviews");
                ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                alert.getButtonTypes().setAll(viewButton, cancel);

                alert.showAndWait().ifPresent(response -> {
                    if (response == viewButton) {
                        parentApp.showMyReviewsPanel();
                    }
                });
            } else {
                AddReviewDialog dialog = new AddReviewDialog(parentApp, reviewController, session);
                dialog.setOnHidden(e -> {
                    try {
                        List<Session> pastSessions = sessionController.getPatientPastSessions(parentApp.getUserId());
                        displayPastSessions(pastSessions);
                    } catch (SQLException ex) {
                        showAlert("Error", "Failed to refresh sessions: " + ex.getMessage(), Alert.AlertType.ERROR);
                    }
                });
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to check review status: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private Color getTypeColor(String type) {
        if (type == null) return Color.rgb(80, 100, 120);
        switch (type.toLowerCase()) {
            case "individual": return Color.rgb(41, 128, 185);
            case "group": return Color.rgb(39, 174, 96);
            case "family": return Color.rgb(142, 68, 173);
            case "couple": return Color.rgb(230, 126, 34);
            case "online": return Color.rgb(52, 152, 219);
            default: return Color.rgb(80, 100, 120);
        }
    }

    private void cancelReservation(Session session) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Reservation");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("Do you want to cancel your reservation for:\n\n" +
                session.getTitle() + "\n" +
                session.getSessionDate().format(dateFormatter) + " at " +
                session.getStartTime().format(timeFormatter));

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    sessionController.cancelReservation(session.getSessionId(), parentApp.getUserId());
                    showAlert("Success", "Reservation cancelled successfully.", Alert.AlertType.INFORMATION);
                    refreshData();
                } catch (SQLException e) {
                    showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void showSessionDetails(Session session) {
        Alert details = new Alert(Alert.AlertType.INFORMATION);
        details.setTitle("Session Details");
        details.setHeaderText(session.getTitle());

        String content = String.format(
                "Date: %s\nTime: %s - %s\nLocation: %s\nType: %s\nStatus: %s",
                session.getSessionDate().format(dateFormatter),
                session.getStartTime().format(timeFormatter),
                session.getEndTime().format(timeFormatter),
                session.getLocation(),
                session.getSessionType(),
                session.getStatus()
        );

        details.setContentText(content);
        details.showAndWait();
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