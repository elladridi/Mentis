package ui;

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
import services.ReminderService;
import java.time.format.DateTimeFormatter;

public class ReminderDialog extends Stage {

    private MentisLoginFrame parentApp;
    private ReminderService.PendingReminder reminder;
    private ReminderService reminderService;
    private Runnable onConfirm;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    // Colors
    private static final Color BACKGROUND_LIGHT = Color.rgb(240, 248, 245);
    private static final Color ACCENT_DARK_GREEN = Color.rgb(60, 120, 90);
    private static final Color CARD_WHITE = Color.WHITE;
    private static final Color TEXT_DARK = Color.rgb(40, 70, 50);
    private static final Color BORDER_LIGHT = Color.rgb(200, 220, 210);

    public ReminderDialog(MentisLoginFrame parentApp, ReminderService.PendingReminder reminder, Runnable onConfirm) {
        this.parentApp = parentApp;
        this.reminder = reminder;
        this.onConfirm = onConfirm;
        this.reminderService = new ReminderService();

        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        setTitle("Session Reminder");

        createUI();
    }

    private void createUI() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

        // ========== HEADER ==========
        VBox header = new VBox(5);
        header.setPadding(new Insets(20, 20, 10, 20));
        header.setAlignment(Pos.CENTER);

        Label iconLabel = new Label("🔔");
        iconLabel.setFont(Font.font("Segoe UI", 40));

        Label titleLabel = new Label("Session Tomorrow!");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        Label subLabel = new Label("Your session is in less than 24 hours");
        subLabel.setFont(Font.font("Segoe UI", 12));
        subLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        header.getChildren().addAll(iconLabel, titleLabel, subLabel);
        root.setTop(header);

        // ========== CENTER CONTENT ==========
        VBox center = new VBox(10);
        center.setPadding(new Insets(5, 20, 15, 20));
        center.setAlignment(Pos.CENTER);

        // Session Card - smaller
        VBox sessionCard = new VBox(10);
        sessionCard.setStyle(
                "-fx-background-color: " + toHex(CARD_WHITE) + ";" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-radius: 15;" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-padding: 15;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);"
        );
        sessionCard.setMaxWidth(400);

        // Session title
        Label sessionTitle = new Label(reminder.getSessionTitle());
        sessionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        sessionTitle.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));
        sessionTitle.setWrapText(true);
        sessionTitle.setAlignment(Pos.CENTER);

        // Session type badge
        Label typeBadge = new Label(reminder.getSessionType());
        typeBadge.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        typeBadge.setTextFill(Color.WHITE);
        typeBadge.setStyle("-fx-background-color: #" + toHex(getTypeColor(reminder.getSessionType())) + "; -fx-background-radius: 15; -fx-padding: 3 12;");
        typeBadge.setAlignment(Pos.CENTER);

        // Details grid - more compact
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(15);
        detailsGrid.setVgap(8);
        detailsGrid.setPadding(new Insets(5, 0, 5, 0));

        // Date
        Label dateIcon = new Label("📅");
        dateIcon.setFont(Font.font("Segoe UI", 14));
        Label dateLabel = new Label(reminder.getSessionDate().format(dateFormatter));
        dateLabel.setFont(Font.font("Segoe UI", 13));
        detailsGrid.add(dateIcon, 0, 0);
        detailsGrid.add(dateLabel, 1, 0);

        // Time
        Label timeIcon = new Label("⏰");
        timeIcon.setFont(Font.font("Segoe UI", 14));
        Label timeLabel = new Label(reminder.getStartTime().format(timeFormatter) + " - " +
                reminder.getEndTime().format(timeFormatter));
        timeLabel.setFont(Font.font("Segoe UI", 13));
        detailsGrid.add(timeIcon, 0, 1);
        detailsGrid.add(timeLabel, 1, 1);

        // Location
        Label locationIcon = new Label("📍");
        locationIcon.setFont(Font.font("Segoe UI", 14));
        Label locationLabel = new Label(reminder.getLocation());
        locationLabel.setFont(Font.font("Segoe UI", 13));
        detailsGrid.add(locationIcon, 0, 2);
        detailsGrid.add(locationLabel, 1, 2);

        // Weather Section - more compact
        HBox weatherBox = new HBox(8);
        weatherBox.setStyle(
                "-fx-background-color: #f0f9ff;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-padding: 10;" +
                        "-fx-border-color: #c8e6f5;"
        );
        weatherBox.setAlignment(Pos.CENTER_LEFT);

        Label weatherIcon = new Label("🌤️");
        weatherIcon.setFont(Font.font("Segoe UI", 16));

        Label weatherLabel = new Label(reminder.getWeatherForecast());
        weatherLabel.setFont(Font.font("Segoe UI", 12));
        weatherLabel.setWrapText(true);

        weatherBox.getChildren().addAll(weatherIcon, weatherLabel);

        // Confirmation question
        Label confirmQuestion = new Label("Confirm your attendance:");
        confirmQuestion.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        confirmQuestion.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));
        confirmQuestion.setPadding(new Insets(5, 0, 0, 0));

        sessionCard.getChildren().addAll(sessionTitle, typeBadge, detailsGrid, weatherBox, confirmQuestion);
        center.getChildren().add(sessionCard);
        root.setCenter(center);

        // ========== BUTTON PANEL ==========
        HBox buttonPanel = new HBox(15);
        buttonPanel.setPadding(new Insets(10, 20, 20, 20));
        buttonPanel.setAlignment(Pos.CENTER);

        // Confirm button - smaller
        Button confirmButton = new Button("✅ Yes, I'll be there");
        confirmButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        confirmButton.setTextFill(Color.WHITE);
        confirmButton.setStyle(
                "-fx-background-color: #" + toHex(ACCENT_DARK_GREEN) + ";" +
                        "-fx-background-radius: 25;" +
                        "-fx-padding: 10 25;" +
                        "-fx-cursor: hand;"
        );

        confirmButton.setOnMouseEntered(e ->
                confirmButton.setStyle(
                        "-fx-background-color: #" + toHex(ACCENT_DARK_GREEN.darker()) + ";" +
                                "-fx-background-radius: 25;" +
                                "-fx-padding: 10 25;" +
                                "-fx-cursor: hand;"
                )
        );
        confirmButton.setOnMouseExited(e ->
                confirmButton.setStyle(
                        "-fx-background-color: #" + toHex(ACCENT_DARK_GREEN) + ";" +
                                "-fx-background-radius: 25;" +
                                "-fx-padding: 10 25;" +
                                "-fx-cursor: hand;"
                )
        );

        confirmButton.setOnAction(e -> handleConfirm());

        // Later button - smaller
        Button laterButton = new Button("⏰ Later");
        laterButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        laterButton.setTextFill(Color.WHITE);
        laterButton.setStyle(
                "-fx-background-color: #e67e22;" +
                        "-fx-background-radius: 25;" +
                        "-fx-padding: 10 25;" +
                        "-fx-cursor: hand;"
        );

        laterButton.setOnMouseEntered(e ->
                laterButton.setStyle(
                        "-fx-background-color: #d35400;" +
                                "-fx-background-radius: 25;" +
                                "-fx-padding: 10 25;" +
                                "-fx-cursor: hand;"
                )
        );
        laterButton.setOnMouseExited(e ->
                laterButton.setStyle(
                        "-fx-background-color: #e67e22;" +
                                "-fx-background-radius: 25;" +
                                "-fx-padding: 10 25;" +
                                "-fx-cursor: hand;"
                )
        );

        laterButton.setOnAction(e -> close());

        buttonPanel.getChildren().addAll(confirmButton, laterButton);
        root.setBottom(buttonPanel);

        // Smaller scene size
        Scene scene = new Scene(root, 450, 480);
        setScene(scene);
    }

    private void handleConfirm() {
        try {
            reminderService.confirmReminder(
                    reminder.getReminderId(),
                    reminder.getSessionId(),
                    reminder.getPatientId()
            );

            showSuccessDialog();

        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Could not confirm. Please try again.");
        }
    }

    private void showSuccessDialog() {
        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Confirmed!");
        success.setHeaderText(null);
        success.setContentText(
                "✅ Thank you for confirming!\n\n" +
                        "See you tomorrow!"
        );
        success.showAndWait();

        if (onConfirm != null) {
            onConfirm.run();
        }

        close();
    }

    private void showErrorDialog(String message) {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle("Error");
        error.setHeaderText(null);
        error.setContentText(message);
        error.showAndWait();
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

    private String toHex(Color color) {
        return String.format("%02x%02x%02x",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }
}