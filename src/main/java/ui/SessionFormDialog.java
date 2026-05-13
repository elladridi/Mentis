package ui;

import controller.SessionController;
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

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class SessionFormDialog extends Stage {

    private MentisLoginFrame parentApp;
    private SessionController controller;
    private Session session;

    private TextField titleField;
    private DatePicker datePicker;
    private TextField startTimeField;
    private TextField endTimeField;
    private TextField locationField;
    private ComboBox<String> typeCombo;
    private ComboBox<String> statusCombo;

    // Symfony-style green colors
    private static final Color PAGE_BG = Color.web("#F8F9FA");
    private static final Color SOFT_GREEN_BG = Color.web("#F1F8E9");
    private static final Color EMERALD = Color.web("#50C878");
    private static final Color EMERALD_DARK = Color.web("#2E7D32");
    private static final Color EMERALD_MID = Color.web("#3A9B5E");
    private static final Color INK = Color.web("#1A3C34");
    private static final Color MUTED = Color.web("#6C757D");
    private static final Color LINE = Color.web("#E9ECEF");
    private static final Color ERROR_RED = Color.web("#E74C3C");
    private static final Color SUCCESS_GREEN = Color.web("#27AE60");

    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public SessionFormDialog(MentisLoginFrame parentApp, SessionController controller,
                             Session session, boolean isEdit) {
        this.parentApp = parentApp;
        this.controller = controller;
        this.session = session;

        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        setTitle(isEdit ? "Edit Session" : "Add New Session");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + cssColor(PAGE_BG) + ";");

        root.setTop(createHeader(isEdit));
        root.setCenter(createForm());
        root.setBottom(createButtonPanel(isEdit));

        Scene scene = new Scene(root, 620, 680);
        setScene(scene);
        setResizable(false);

        if (isEdit && session != null) {
            loadSessionData();
        } else {
            setDefaultValues();
        }

        showAndWait();
    }

    private HBox createHeader(boolean isEdit) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(28, 35, 24, 35));
        header.setStyle("-fx-background-color: " + cssColor(PAGE_BG) + ";");

        VBox headerContent = new VBox(6);
        Label titleLabel = new Label(isEdit ? "✏️ Edit Session" : "✨ Create New Session");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleLabel.setTextFill(EMERALD_DARK);

        Label subtitleLabel = new Label(isEdit ? "Modify session details" : "Create a new therapy session");
        subtitleLabel.setFont(Font.font("Segoe UI", 14));
        subtitleLabel.setTextFill(MUTED);

        headerContent.getChildren().addAll(titleLabel, subtitleLabel);
        header.getChildren().add(headerContent);
        return header;
    }

    private ScrollPane createForm() {
        GridPane formPanel = new GridPane();
        formPanel.setStyle("-fx-background-color: " + cssColor(PAGE_BG) + ";");
        formPanel.setPadding(new Insets(20, 40, 20, 40));
        formPanel.setHgap(18);
        formPanel.setVgap(18);
        formPanel.setAlignment(Pos.TOP_CENTER);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(130);
        col1.setHalignment(javafx.geometry.HPos.RIGHT);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        col2.setFillWidth(true);

        formPanel.getColumnConstraints().addAll(col1, col2);

        int row = 0;

        // Title
        Label titleLabel = createFormLabel("Title");
        titleField = createTextField("Enter session title", "📝");
        formPanel.add(titleLabel, 0, row);
        formPanel.add(titleField, 1, row++);

        // Date
        Label dateLabel = createFormLabel("Date");
        datePicker = new DatePicker();
        datePicker.setPromptText("Select date");
        datePicker.setPrefHeight(44);
        datePicker.setMaxWidth(Double.MAX_VALUE);
        datePicker.setStyle(pillInputStyle());
        formPanel.add(dateLabel, 0, row);
        formPanel.add(datePicker, 1, row++);

        // Start Time
        Label startTimeLabel = createFormLabel("Start Time");
        startTimeField = createTextField("HH:mm (e.g., 14:30)", "⏰");
        startTimeField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) validateTimeField(startTimeField);
        });
        formPanel.add(startTimeLabel, 0, row);
        formPanel.add(startTimeField, 1, row++);

        // End Time
        Label endTimeLabel = createFormLabel("End Time");
        endTimeField = createTextField("HH:mm (e.g., 16:00)", "⏰");
        endTimeField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) validateTimeField(endTimeField);
        });
        formPanel.add(endTimeLabel, 0, row);
        formPanel.add(endTimeField, 1, row++);

        // Location
        Label locationLabel = createFormLabel("Location");
        locationField = createTextField("Enter location", "📍");
        formPanel.add(locationLabel, 0, row);
        formPanel.add(locationField, 1, row++);

        // Session Type
        Label typeLabel = createFormLabel("Session Type");
        String[] types = {"Individual", "Group", "Family", "Couple", "Online"};
        typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(types);
        typeCombo.setValue(types[0]);
        typeCombo.setPrefHeight(44);
        typeCombo.setMaxWidth(Double.MAX_VALUE);
        typeCombo.setStyle(pillInputStyle());
        formPanel.add(typeLabel, 0, row);
        formPanel.add(typeCombo, 1, row++);

        // Status
        Label statusLabel = createFormLabel("Status");
        String[] statuses = {"active", "inactive", "scheduled", "completed", "cancelled"};
        statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(statuses);
        statusCombo.setValue("scheduled");
        statusCombo.setPrefHeight(44);
        statusCombo.setMaxWidth(Double.MAX_VALUE);
        statusCombo.setStyle(pillInputStyle());
        formPanel.add(statusLabel, 0, row);
        formPanel.add(statusCombo, 1, row++);

        // Info label
        Label infoLabel = new Label("💡 Time format: HH:mm (24-hour format)");
        infoLabel.setFont(Font.font("Segoe UI", 11));
        infoLabel.setTextFill(MUTED);
        formPanel.add(infoLabel, 1, row++);

        ScrollPane scrollPane = new ScrollPane(formPanel);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setBorder(null);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return scrollPane;
    }

    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        label.setTextFill(INK);
        return label;
    }

    private TextField createTextField(String prompt, String emoji) {
        TextField field = new TextField();
        field.setFont(Font.font("Segoe UI", 14));
        field.setPromptText(emoji + " " + prompt);
        field.setStyle(pillInputStyle());
        field.setPrefHeight(44);
        return field;
    }

    private boolean validateTimeField(TextField timeField) {
        String timeText = timeField.getText().trim();
        if (timeText.isEmpty()) return false;

        try {
            LocalTime.parse(timeText, timeFormatter);
            timeField.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 999px;" +
                            "-fx-border-radius: 999px;" +
                            "-fx-border-color: " + cssColor(SUCCESS_GREEN) + ";" +
                            "-fx-border-width: 2px;" +
                            "-fx-padding: 10px 18px;" +
                            "-fx-font-size: 14px;"
            );
            return true;
        } catch (Exception e) {
            timeField.setStyle(
                    "-fx-background-color: #FFF5F5;" +
                            "-fx-background-radius: 999px;" +
                            "-fx-border-radius: 999px;" +
                            "-fx-border-color: " + cssColor(ERROR_RED) + ";" +
                            "-fx-border-width: 2px;" +
                            "-fx-padding: 10px 18px;" +
                            "-fx-font-size: 14px;"
            );
            return false;
        }
    }

    private HBox createButtonPanel(boolean isEdit) {
        HBox buttonPanel = new HBox(16);
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);
        buttonPanel.setPadding(new Insets(20, 40, 32, 40));
        buttonPanel.setStyle("-fx-background-color: " + cssColor(PAGE_BG) + ";");

        Button cancelButton = createOutlineButton("Cancel");
        cancelButton.setOnAction(e -> close());

        Button saveButton = createPrimaryButton(isEdit ? "Save Changes" : "Create Session");
        saveButton.setOnAction(e -> saveSession());

        buttonPanel.getChildren().addAll(cancelButton, saveButton);
        return buttonPanel;
    }

    private Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        button.setTextFill(Color.WHITE);
        button.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + cssColor(EMERALD) + ", " + cssColor(EMERALD_MID) + ");" +
                        "-fx-background-radius: 999px;" +
                        "-fx-padding: 12px 32px;" +
                        "-fx-cursor: hand;" +
                        cardShadow()
        );
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + cssColor(EMERALD_MID) + ", " + cssColor(EMERALD_DARK) + ");" +
                        "-fx-background-radius: 999px;" +
                        "-fx-padding: 12px 32px;" +
                        "-fx-cursor: hand;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + cssColor(EMERALD) + ", " + cssColor(EMERALD_MID) + ");" +
                        "-fx-background-radius: 999px;" +
                        "-fx-padding: 12px 32px;" +
                        "-fx-cursor: hand;" +
                        cardShadow()
        ));
        return button;
    }

    private Button createOutlineButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        button.setTextFill(MUTED);
        button.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 999px;" +
                        "-fx-border-color: " + cssColor(LINE) + ";" +
                        "-fx-border-radius: 999px;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-padding: 11px 28px;" +
                        "-fx-cursor: hand;"
        );
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: " + cssColor(SOFT_GREEN_BG) + ";" +
                        "-fx-background-radius: 999px;" +
                        "-fx-border-color: " + cssColor(EMERALD) + ";" +
                        "-fx-border-radius: 999px;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-padding: 11px 28px;" +
                        "-fx-cursor: hand;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 999px;" +
                        "-fx-border-color: " + cssColor(LINE) + ";" +
                        "-fx-border-radius: 999px;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-padding: 11px 28px;" +
                        "-fx-cursor: hand;"
        ));
        return button;
    }

    private String pillInputStyle() {
        return "-fx-background-color: white;" +
                "-fx-background-radius: 999px;" +
                "-fx-border-radius: 999px;" +
                "-fx-border-color: " + cssColor(LINE) + ";" +
                "-fx-border-width: 1.5px;" +
                "-fx-padding: 10px 18px;" +
                "-fx-font-size: 14px;" +
                "-fx-font-family: 'Segoe UI';";
    }

    private void setDefaultValues() {
        datePicker.setValue(LocalDate.now());
        startTimeField.setText("09:00");
        endTimeField.setText("10:00");
    }

    private void loadSessionData() {
        if (session != null) {
            titleField.setText(session.getTitle());
            datePicker.setValue(session.getSessionDate());
            startTimeField.setText(session.getStartTime().format(timeFormatter));
            endTimeField.setText(session.getEndTime().format(timeFormatter));
            locationField.setText(session.getLocation());
            typeCombo.setValue(session.getSessionType());
            statusCombo.setValue(session.getStatus());
        }
    }

    private void saveSession() {
        if (titleField.getText().trim().isEmpty()) {
            showAlert("Please enter a title for the session.", Alert.AlertType.WARNING);
            return;
        }

        if (datePicker.getValue() == null) {
            showAlert("Please select a date for the session.", Alert.AlertType.WARNING);
            return;
        }

        if (!validateTimeField(startTimeField)) {
            showAlert("Please enter a valid start time in HH:mm format (e.g., 14:30).", Alert.AlertType.WARNING);
            return;
        }

        if (!validateTimeField(endTimeField)) {
            showAlert("Please enter a valid end time in HH:mm format (e.g., 16:00).", Alert.AlertType.WARNING);
            return;
        }

        if (locationField.getText().trim().isEmpty()) {
            showAlert("Please enter a location for the session.", Alert.AlertType.WARNING);
            return;
        }

        if (typeCombo.getValue() == null) {
            showAlert("Please select a session type.", Alert.AlertType.WARNING);
            return;
        }

        try {
            LocalTime startTime = LocalTime.parse(startTimeField.getText().trim(), timeFormatter);
            LocalTime endTime = LocalTime.parse(endTimeField.getText().trim(), timeFormatter);

            if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
                showAlert("End time must be after start time.", Alert.AlertType.WARNING);
                return;
            }

            if (session == null) {
                Session newSession = new Session();
                newSession.setTitle(titleField.getText().trim());
                newSession.setSessionDate(datePicker.getValue());
                newSession.setStartTime(startTime);
                newSession.setEndTime(endTime);
                newSession.setLocation(locationField.getText().trim());
                newSession.setSessionType(typeCombo.getValue());
                newSession.setStatus(statusCombo.getValue());

                controller.createSession(newSession);
                showAlert("✨ Session created successfully!", Alert.AlertType.INFORMATION);
            } else {
                session.setTitle(titleField.getText().trim());
                session.setSessionDate(datePicker.getValue());
                session.setStartTime(startTime);
                session.setEndTime(endTime);
                session.setLocation(locationField.getText().trim());
                session.setSessionType(typeCombo.getValue());
                session.setStatus(statusCombo.getValue());

                controller.updateSession(session);
                showAlert("✅ Session updated successfully!", Alert.AlertType.INFORMATION);
            }

            close();
            if (parentApp != null) {
                parentApp.showSessionPanel();
            }

        } catch (SQLException e) {
            showAlert("Error saving session: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
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

    private String cssColor(Color color) {
        return "#" + toHex(color);
    }

    private String cardShadow() {
        return "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 5);";
    }

    private String toHex(Color color) {
        return String.format("%02x%02x%02x",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }
}