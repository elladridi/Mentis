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
import services.TranslationService;
import services.TextToSpeechService;
import services.VideoCallService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class PatientMySessionsPanel extends VBox {

    private MentisLoginFrame parentApp;
    private SessionController sessionController;
    private SessionReviewController reviewController;
    private TranslationService translationService;
    private TextToSpeechService ttsService;
    private VideoCallService videoCallService;
    private VBox upcomingContainer;
    private VBox pastContainer;
    private ScrollPane mainScrollPane;
    private VBox contentContainer;
    private ToggleGroup viewToggle;
    private Label userInfoLabel;
    private HBox toggleBox;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    // Symfony-style green colors
    private static final Color PAGE_BG = Color.web("#F8F9FA");
    private static final Color SOFT_GREEN_BG = Color.web("#F1F8E9");
    private static final Color EMERALD = Color.web("#50C878");
    private static final Color EMERALD_DARK = Color.web("#2E7D32");
    private static final Color EMERALD_MID = Color.web("#3A9B5E");
    private static final Color INK = Color.web("#1A3C34");
    private static final Color MUTED = Color.web("#6C757D");
    private static final Color LINE = Color.web("#E9ECEF");
    private static final Color STATUS_RESERVED = Color.web("#3498DB");
    private static final Color STATUS_COMPLETED = Color.web("#27AE60");
    private static final Color BUTTON_DANGER = Color.web("#E74C3C");
    private static final Color BUTTON_WARNING = Color.web("#F39C12");
    private static final Color BUTTON_VIDEO = Color.web("#E74C3C");
    private static final Color BUTTON_QR = Color.web("#9B59B6");
    private static final Color BUTTON_TRANSLATE = Color.web("#3498DB");
    private static final Color BUTTON_LISTEN = Color.web("#9B59B6");

    public PatientMySessionsPanel(MentisLoginFrame parentApp, SessionController sessionController) {
        this.parentApp = parentApp;
        this.sessionController = sessionController;
        this.reviewController = new SessionReviewController();
        this.translationService = new TranslationService();
        this.ttsService = new TextToSpeechService();
        this.videoCallService = new VideoCallService();

        setStyle("-fx-background-color: " + cssColor(PAGE_BG) + ";");
        setPadding(new Insets(44, 56, 44, 56));
        setSpacing(28);

        createHeader();
        createToggleView();
        createContentContainer();
        refreshData();
    }

    private void createHeader() {
        VBox headerBox = new VBox(8);
        headerBox.setPadding(new Insets(0, 0, 24, 0));

        Label titleLabel = new Label("📅 My Sessions");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 40));
        titleLabel.setTextFill(EMERALD_DARK);

        Label subtitleLabel = new Label("View your upcoming and past session reservations");
        subtitleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        subtitleLabel.setTextFill(MUTED);

        headerBox.getChildren().addAll(titleLabel, subtitleLabel);
        getChildren().add(headerBox);
    }

    private void createToggleView() {
        toggleBox = new HBox(16);
        toggleBox.setAlignment(Pos.CENTER_LEFT);
        toggleBox.setPadding(new Insets(0, 0, 20, 0));

        viewToggle = new ToggleGroup();

        RadioButton upcomingRadio = new RadioButton("📌 Upcoming Sessions");
        upcomingRadio.setToggleGroup(viewToggle);
        upcomingRadio.setSelected(true);
        upcomingRadio.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        upcomingRadio.setTextFill(EMERALD_DARK);
        upcomingRadio.setStyle("-fx-padding: 8px 0;");

        RadioButton pastRadio = new RadioButton("✅ Past Sessions");
        pastRadio.setToggleGroup(viewToggle);
        pastRadio.setFont(Font.font("Segoe UI", 14));
        pastRadio.setTextFill(MUTED);
        pastRadio.setStyle("-fx-padding: 8px 0;");

        viewToggle.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == upcomingRadio) {
                showUpcomingSessions();
            } else if (newVal == pastRadio) {
                showPastSessions();
            }
        });

        // Style the radio buttons with green selection
        String radioStyle = ".radio-button .dot {" +
                "-fx-background-color: " + cssColor(EMERALD) + ";" +
                "}";
        // Note: CSS styling for radio buttons is best handled in external CSS file

        toggleBox.getChildren().addAll(upcomingRadio, pastRadio);
        getChildren().add(toggleBox);
    }

    private void createContentContainer() {
        contentContainer = new VBox(20);
        contentContainer.setFillWidth(true);

        upcomingContainer = new VBox(20);
        upcomingContainer.setFillWidth(true);

        pastContainer = new VBox(20);
        pastContainer.setFillWidth(true);
        pastContainer.setVisible(false);
        pastContainer.setManaged(false);

        contentContainer.getChildren().addAll(upcomingContainer, pastContainer);

        mainScrollPane = new ScrollPane(contentContainer);
        mainScrollPane.setStyle("-fx-background-color: transparent;");
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
            VBox emptyBox = createEmptyState("📅", "No upcoming sessions",
                    "You haven't reserved any upcoming sessions yet.", true);
            upcomingContainer.getChildren().add(emptyBox);
            return;
        }

        for (Session session : sessions) {
            VBox sessionCard = createUpcomingSessionCard(session);
            upcomingContainer.getChildren().add(sessionCard);
        }

        Label countLabel = new Label("📌 You have " + sessions.size() + " upcoming session(s)");
        countLabel.setFont(Font.font("Segoe UI", 12));
        countLabel.setTextFill(MUTED);
        countLabel.setPadding(new Insets(5, 0, 0, 0));
        upcomingContainer.getChildren().add(countLabel);
    }

    private void displayPastSessions(List<Session> sessions) {
        pastContainer.getChildren().clear();

        if (sessions.isEmpty()) {
            VBox emptyBox = createEmptyState("📋", "No past sessions",
                    "You haven't attended any sessions yet.", false);
            pastContainer.getChildren().add(emptyBox);
            return;
        }

        for (Session session : sessions) {
            VBox sessionCard = createPastSessionCard(session);
            pastContainer.getChildren().add(sessionCard);
        }

        Label countLabel = new Label("✅ You have attended " + sessions.size() + " session(s)");
        countLabel.setFont(Font.font("Segoe UI", 12));
        countLabel.setTextFill(MUTED);
        countLabel.setPadding(new Insets(5, 0, 0, 0));
        pastContainer.getChildren().add(countLabel);
    }

    private VBox createEmptyState(String emoji, String title, String message, boolean isUpcoming) {
        VBox emptyBox = new VBox(20);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(60));
        emptyBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 24px;" +
                        cardShadow()
        );

        Label iconLabel = new Label(emoji);
        iconLabel.setFont(Font.font("Segoe UI Emoji", 54));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(INK);

        Label msgLabel = new Label(message);
        msgLabel.setFont(Font.font("Segoe UI", 14));
        msgLabel.setTextFill(MUTED);
        msgLabel.setWrapText(true);
        msgLabel.setAlignment(Pos.CENTER);

        emptyBox.getChildren().addAll(iconLabel, titleLabel, msgLabel);

        if (isUpcoming) {
            Button browseButton = createPrimaryButton("Browse Available Sessions");
            browseButton.setOnAction(e -> parentApp.showPatientAvailableSessionsPanel());
            emptyBox.getChildren().add(browseButton);
        }

        return emptyBox;
    }

    private VBox createUpcomingSessionCard(Session session) {
        VBox card = new VBox(16);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20px;" +
                        "-fx-padding: 22px;" +
                        cardShadow()
        );
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20px;" +
                        "-fx-padding: 22px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 18, 0, 0, 8);" +
                        "-fx-translate-y: -3px;"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20px;" +
                        "-fx-padding: 22px;" +
                        cardShadow()
        ));

        // Title and Type row
        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(session.getTitle());
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titleLabel.setTextFill(INK);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Label typeLabel = createBadge(session.getSessionType(), getTypeColor(session.getSessionType()));
        titleRow.getChildren().addAll(titleLabel, typeLabel);

        // Details grid
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(30);
        detailsGrid.setVgap(12);
        detailsGrid.setPadding(new Insets(12, 0, 8, 0));

        Label dateLabel = new Label("📅 " + session.getSessionDate().format(dateFormatter));
        dateLabel.setFont(Font.font("Segoe UI", 13));
        dateLabel.setTextFill(MUTED);

        Label timeLabel = new Label("⏰ " + session.getStartTime().format(timeFormatter) + " - " + session.getEndTime().format(timeFormatter));
        timeLabel.setFont(Font.font("Segoe UI", 13));
        timeLabel.setTextFill(MUTED);

        Label locationLabel = new Label("📍 " + session.getLocation());
        locationLabel.setFont(Font.font("Segoe UI", 13));
        locationLabel.setTextFill(MUTED);

        detailsGrid.add(dateLabel, 0, 0);
        detailsGrid.add(timeLabel, 1, 0);
        detailsGrid.add(locationLabel, 0, 1, 2, 1);

        // Action buttons row
        HBox actionRow = new HBox(12);
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        actionRow.setPadding(new Insets(12, 0, 0, 0));

        Label statusLabel = new Label("● Reserved");
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        statusLabel.setTextFill(STATUS_RESERVED);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Icon buttons
        Button translateBtn = createIconButton("🌐", "Translate", BUTTON_TRANSLATE);
        translateBtn.setOnAction(e -> openTranslationDialog(session));

        Button listenBtn = createIconButton("🔊", "Listen", BUTTON_LISTEN);
        listenBtn.setOnAction(e -> openListenDialog(session));

        Button qrBtn = createIconButton("📱", "QR Code", BUTTON_QR);
        qrBtn.setOnAction(e -> showQRCode(session));

        Button videoBtn = createIconButton("📹", "Video Call", BUTTON_VIDEO);
        videoBtn.setOnAction(e -> startVideoCall(session));

        Button detailsBtn = createOutlineButton("View Details");
        detailsBtn.setOnAction(e -> showSessionDetails(session));

        actionRow.getChildren().addAll(statusLabel, spacer, translateBtn, listenBtn, qrBtn, videoBtn, detailsBtn);

        // Cancel button (only for future sessions)
        if (session.getSessionDate().isAfter(LocalDate.now()) ||
                session.getSessionDate().isEqual(LocalDate.now())) {
            Button cancelBtn = createDangerButton("Cancel");
            cancelBtn.setOnAction(e -> cancelReservation(session));
            actionRow.getChildren().add(cancelBtn);
        }

        card.getChildren().addAll(titleRow, detailsGrid, actionRow);
        return card;
    }

    private VBox createPastSessionCard(Session session) {
        VBox card = new VBox(16);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20px;" +
                        "-fx-padding: 22px;" +
                        cardShadow()
        );

        // Title and Type row
        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(session.getTitle());
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titleLabel.setTextFill(INK);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Label typeLabel = createBadge(session.getSessionType(), getTypeColor(session.getSessionType()));
        titleRow.getChildren().addAll(titleLabel, typeLabel);

        // Details grid
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(30);
        detailsGrid.setVgap(12);
        detailsGrid.setPadding(new Insets(12, 0, 8, 0));

        Label dateLabel = new Label("📅 " + session.getSessionDate().format(dateFormatter));
        dateLabel.setFont(Font.font("Segoe UI", 13));
        dateLabel.setTextFill(MUTED);

        Label timeLabel = new Label("⏰ " + session.getStartTime().format(timeFormatter) + " - " + session.getEndTime().format(timeFormatter));
        timeLabel.setFont(Font.font("Segoe UI", 13));
        timeLabel.setTextFill(MUTED);

        Label locationLabel = new Label("📍 " + session.getLocation());
        locationLabel.setFont(Font.font("Segoe UI", 13));
        locationLabel.setTextFill(MUTED);

        detailsGrid.add(dateLabel, 0, 0);
        detailsGrid.add(timeLabel, 1, 0);
        detailsGrid.add(locationLabel, 0, 1, 2, 1);

        // Action buttons row
        HBox actionRow = new HBox(12);
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        actionRow.setPadding(new Insets(12, 0, 0, 0));

        Label statusLabel = new Label("✓ Completed");
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        statusLabel.setTextFill(STATUS_COMPLETED);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button detailsBtn = createOutlineButton("View Details");
        detailsBtn.setOnAction(e -> showSessionDetails(session));

        Button reviewBtn = createPrimaryButton("✏️ Write Review");
        reviewBtn.setOnAction(e -> openReviewDialog(session));

        actionRow.getChildren().addAll(statusLabel, spacer, reviewBtn, detailsBtn);

        card.getChildren().addAll(titleRow, detailsGrid, actionRow);
        return card;
    }

    private Label createBadge(String text, Color bgColor) {
        Label badge = new Label(text);
        badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        badge.setTextFill(Color.WHITE);
        badge.setPadding(new Insets(5, 14, 5, 14));
        badge.setStyle("-fx-background-color: " + cssColor(bgColor) + "; -fx-background-radius: 999px;");
        return badge;
    }

    private Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        button.setTextFill(Color.WHITE);
        button.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + cssColor(EMERALD) + ", " + cssColor(EMERALD_MID) + ");" +
                        "-fx-background-radius: 999px;" +
                        "-fx-padding: 9px 22px;" +
                        "-fx-cursor: hand;" +
                        cardShadow()
        );
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + cssColor(EMERALD_MID) + ", " + cssColor(EMERALD_DARK) + ");" +
                        "-fx-background-radius: 999px;" +
                        "-fx-padding: 9px 22px;" +
                        "-fx-cursor: hand;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + cssColor(EMERALD) + ", " + cssColor(EMERALD_MID) + ");" +
                        "-fx-background-radius: 999px;" +
                        "-fx-padding: 9px 22px;" +
                        "-fx-cursor: hand;" +
                        cardShadow()
        ));
        return button;
    }

    private Button createOutlineButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        button.setTextFill(MUTED);
        button.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 999px;" +
                        "-fx-border-color: " + cssColor(LINE) + ";" +
                        "-fx-border-radius: 999px;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-padding: 7px 18px;" +
                        "-fx-cursor: hand;"
        );
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: " + cssColor(SOFT_GREEN_BG) + ";" +
                        "-fx-background-radius: 999px;" +
                        "-fx-border-color: " + cssColor(EMERALD) + ";" +
                        "-fx-border-radius: 999px;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-padding: 7px 18px;" +
                        "-fx-cursor: hand;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 999px;" +
                        "-fx-border-color: " + cssColor(LINE) + ";" +
                        "-fx-border-radius: 999px;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-padding: 7px 18px;" +
                        "-fx-cursor: hand;"
        ));
        return button;
    }

    private Button createIconButton(String emoji, String text, Color bgColor) {
        Button button = new Button(emoji + " " + text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        button.setTextFill(Color.WHITE);
        button.setStyle(
                "-fx-background-color: " + cssColor(bgColor) + ";" +
                        "-fx-background-radius: 999px;" +
                        "-fx-padding: 6px 14px;" +
                        "-fx-cursor: hand;"
        );
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: " + cssColor(bgColor.darker()) + ";" +
                        "-fx-background-radius: 999px;" +
                        "-fx-padding: 6px 14px;" +
                        "-fx-cursor: hand;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: " + cssColor(bgColor) + ";" +
                        "-fx-background-radius: 999px;" +
                        "-fx-padding: 6px 14px;" +
                        "-fx-cursor: hand;"
        ));
        return button;
    }

    private Button createDangerButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        button.setTextFill(Color.WHITE);
        button.setStyle(
                "-fx-background-color: " + cssColor(BUTTON_DANGER) + ";" +
                        "-fx-background-radius: 999px;" +
                        "-fx-padding: 7px 18px;" +
                        "-fx-cursor: hand;"
        );
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: " + cssColor(BUTTON_DANGER.darker()) + ";" +
                        "-fx-background-radius: 999px;" +
                        "-fx-padding: 7px 18px;" +
                        "-fx-cursor: hand;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: " + cssColor(BUTTON_DANGER) + ";" +
                        "-fx-background-radius: 999px;" +
                        "-fx-padding: 7px 18px;" +
                        "-fx-cursor: hand;"
        ));
        return button;
    }

    private Color getTypeColor(String type) {
        if (type == null) return Color.web("#7F8C8D");
        switch (type.toLowerCase()) {
            case "individual": return Color.web("#5B8C5A");
            case "group": return Color.web("#27AE60");
            case "family": return Color.web("#8E44AD");
            case "couple": return Color.web("#E67E22");
            case "online": return Color.web("#3498DB");
            default: return Color.web("#7F8C8D");
        }
    }

    private void openTranslationDialog(Session session) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Translate Session Details");
        dialog.initStyle(StageStyle.UTILITY);

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: " + cssColor(PAGE_BG) + ";");

        Label titleLabel = new Label("🌐 Translate Session");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        titleLabel.setTextFill(EMERALD_DARK);

        ComboBox<String> languageCombo = new ComboBox<>();
        languageCombo.getItems().addAll(
                "French", "Spanish", "German", "Italian", "Portuguese",
                "Russian", "Japanese", "Chinese", "Arabic", "Hindi"
        );
        languageCombo.setValue("French");
        languageCombo.setPrefWidth(200);
        languageCombo.setStyle(pillInputStyle());

        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);
        resultArea.setPrefRowCount(8);
        resultArea.setPromptText("Translated text will appear here...");
        resultArea.setStyle("-fx-background-radius: 12px;");

        String originalText = String.format(
                "Session: %s\nDate: %s\nTime: %s - %s\nLocation: %s\nType: %s",
                session.getTitle(),
                session.getSessionDate().format(dateFormatter),
                session.getStartTime().format(timeFormatter),
                session.getEndTime().format(timeFormatter),
                session.getLocation(),
                session.getSessionType()
        );

        Button translateButton = createPrimaryButton("Translate");
        translateButton.setOnAction(e -> {
            String targetLang = languageCombo.getValue();
            String translated = translationService.translate(originalText, targetLang);
            resultArea.setText(translated);
        });

        Button closeButton = createOutlineButton("Close");
        closeButton.setOnAction(e -> dialog.close());

        HBox buttonBox = new HBox(15, translateButton, closeButton);
        buttonBox.setAlignment(Pos.CENTER);

        content.getChildren().addAll(titleLabel, languageCombo, resultArea, buttonBox);

        Scene scene = new Scene(content, 500, 450);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void openListenDialog(Session session) {
        String textToSpeak = String.format(
                "Session: %s. Date: %s. Time: from %s to %s. Location: %s. Type: %s.",
                session.getTitle(),
                session.getSessionDate().format(dateFormatter),
                session.getStartTime().format(timeFormatter),
                session.getEndTime().format(timeFormatter),
                session.getLocation(),
                session.getSessionType()
        );

        Stage audioDialog = new Stage();
        audioDialog.initModality(Modality.APPLICATION_MODAL);
        audioDialog.setTitle("Listen to Session Details");
        audioDialog.initStyle(StageStyle.UTILITY);

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: " + cssColor(PAGE_BG) + ";");

        Label titleLabel = new Label("🔊 Audio Player");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(EMERALD_DARK);

        Label sessionLabel = new Label(session.getTitle());
        sessionLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        sessionLabel.setTextFill(INK);

        ProgressIndicator playingIndicator = new ProgressIndicator();
        playingIndicator.setPrefSize(60, 60);
        playingIndicator.setVisible(false);

        Label statusLabel = new Label("Ready to play");
        statusLabel.setFont(Font.font("Segoe UI", 13));
        statusLabel.setTextFill(MUTED);

        Button playButton = createPrimaryButton("▶️ Play");
        playButton.setOnAction(e -> {
            ttsService.speak(textToSpeak, "English");
            statusLabel.setText("Speaking...");
            playingIndicator.setVisible(true);
        });

        Button stopButton = createDangerButton("⏹️ Stop");
        stopButton.setOnAction(e -> {
            ttsService.stop();
            statusLabel.setText("Stopped");
            playingIndicator.setVisible(false);
        });

        Button closeButton = createOutlineButton("Close");
        closeButton.setOnAction(e -> {
            ttsService.stop();
            audioDialog.close();
        });

        HBox controlBox = new HBox(15, playButton, stopButton, closeButton);
        controlBox.setAlignment(Pos.CENTER);

        content.getChildren().addAll(titleLabel, sessionLabel, playingIndicator, statusLabel, controlBox);

        Scene scene = new Scene(content, 450, 380);
        audioDialog.setScene(scene);
        audioDialog.showAndWait();

        ttsService.stop();
    }

    private void startVideoCall(Session session) {
        if (!session.getSessionType().equalsIgnoreCase("Online")) {
            showAlert("Not Available", "Video calls are only available for online sessions.", Alert.AlertType.WARNING);
            return;
        }

        LocalDateTime sessionTime = LocalDateTime.of(session.getSessionDate(), session.getStartTime());
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(sessionTime.minusMinutes(15))) {
            showAlert("Too Early", "Video call will be available 15 minutes before the session.\nSession starts at: " + session.getStartTime().format(timeFormatter), Alert.AlertType.WARNING);
            return;
        }

        if (now.isAfter(sessionTime.plusHours(1))) {
            showAlert("Session Ended", "This session has already ended.", Alert.AlertType.WARNING);
            return;
        }

        String meetingLink = videoCallService.generateMeetingLink(
                session.getSessionId(),
                parentApp.getUserId(),
                1
        );

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Start Video Call");
        confirm.setHeaderText("Join video session for: " + session.getTitle());
        confirm.setContentText(
                "You will be redirected to Jitsi Meet in your browser.\n\n" +
                        "📹 Make sure your camera is working\n" +
                        "🎤 Check your microphone\n\n" +
                        "Ready to join?"
        );

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                videoCallService.joinMeeting(meetingLink);
            }
        });
    }

    private void showQRCode(Session session) {
        try {
            byte[] qrBytes = QRCodeGenerator.getQRCodeBytes(session, parentApp.getUserId());
            if (qrBytes != null) {
                java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(qrBytes);
                Image qrImage = new Image(bis);

                Stage qrStage = new Stage();
                qrStage.initModality(Modality.APPLICATION_MODAL);
                qrStage.setTitle("QR Code - " + session.getTitle());
                qrStage.initStyle(StageStyle.UTILITY);

                VBox root = new VBox(20);
                root.setAlignment(Pos.CENTER);
                root.setPadding(new Insets(30));
                root.setStyle("-fx-background-color: white; -fx-background-radius: 20px;" + cardShadow());

                ImageView qrView = new ImageView(qrImage);
                qrView.setFitWidth(280);
                qrView.setFitHeight(280);
                qrView.setPreserveRatio(true);

                Label titleLabel = new Label(session.getTitle());
                titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
                titleLabel.setTextFill(INK);

                Label infoLabel = new Label("Show this QR code at the session for check-in");
                infoLabel.setFont(Font.font("Segoe UI", 12));
                infoLabel.setTextFill(MUTED);

                Button closeButton = createPrimaryButton("Close");
                closeButton.setOnAction(e -> qrStage.close());

                root.getChildren().addAll(titleLabel, qrView, infoLabel, closeButton);

                Scene scene = new Scene(root, 400, 480);
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
                "📅 Date: %s\n⏰ Time: %s - %s\n📍 Location: %s\n🏷️ Type: %s\n📌 Status: %s",
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

    private String pillInputStyle() {
        return "-fx-background-color: white;" +
                "-fx-background-radius: 999px;" +
                "-fx-border-radius: 999px;" +
                "-fx-border-color: " + cssColor(LINE) + ";" +
                "-fx-border-width: 1.5px;" +
                "-fx-padding: 10px 18px;" +
                "-fx-font-size: 14px;";
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