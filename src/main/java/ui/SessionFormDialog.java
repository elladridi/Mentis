package ui;

import controller.SessionController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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

    // Color constants (same as SessionPanel)
    private static final Color BACKGROUND_LIGHT = Color.rgb(240, 248, 245);
    private static final Color CARD_WHITE = Color.WHITE;
    private static final Color ACCENT_DARK_GREEN = Color.rgb(60, 120, 90);
    private static final Color BORDER_LIGHT = Color.rgb(200, 220, 210);
    private static final Color BUTTON_LIGHT_GREEN = Color.rgb(160, 200, 180);
    private static final Color TEXT_DARK = Color.rgb(40, 70, 50);
    private static final Color TEXT_LIGHT = Color.rgb(100, 130, 110);
    private static final Color ERROR_RED = Color.rgb(192, 57, 43);

    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public SessionFormDialog(MentisLoginFrame parentApp, SessionController controller,
                             Session session, boolean isEdit) {
        this.parentApp = parentApp;
        this.controller = controller;
        this.session = session;

        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        setTitle(isEdit ? "Edit Session" : "Add New Session");

        // Main layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

        // Header
        root.setTop(createHeader(isEdit));

        // Form
        root.setCenter(createForm());

        // Buttons
        root.setBottom(createButtonPanel(isEdit));

        Scene scene = new Scene(root, 600, 650);
        setScene(scene);
        setResizable(false);

        if (isEdit && session != null) {
            loadSessionData();
        } else {
            // Set default values for new session
            setDefaultValues();
        }

        showAndWait();
    }

    private HBox createHeader(boolean isEdit) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 30, 20, 30));
        header.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

        Label titleLabel = new Label(isEdit ? "Edit Session" : "Add New Session");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        header.getChildren().add(titleLabel);
        return header;
    }

    private ScrollPane createForm() {
        GridPane formPanel = new GridPane();
        formPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        formPanel.setPadding(new Insets(20, 30, 20, 30));
        formPanel.setHgap(15);
        formPanel.setVgap(15);
        formPanel.setAlignment(Pos.TOP_CENTER);

        // Column constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(120);
        col1.setHalignment(javafx.geometry.HPos.RIGHT);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        col2.setFillWidth(true);

        formPanel.getColumnConstraints().addAll(col1, col2);

        int row = 0;

        // Title
        Label titleLabel = new Label("Title:");
        titleLabel.setFont(Font.font("Segoe UI", 14));
        titleLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        titleField = new TextField();
        titleField.setFont(Font.font("Segoe UI", 14));
        titleField.setPromptText("Enter session title");
        titleField.setStyle(
                "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8 12;"
        );
        titleField.setPrefHeight(40);

        formPanel.add(titleLabel, 0, row);
        formPanel.add(titleField, 1, row++);

        // Date
        Label dateLabel = new Label("Date:");
        dateLabel.setFont(Font.font("Segoe UI", 14));
        dateLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        datePicker = new DatePicker();
        // REMOVED: datePicker.setFont(Font.font("Segoe UI", 14)); - DatePicker doesn't have setFont() method
        datePicker.setPromptText("Select date");
        datePicker.setPrefHeight(40);
        datePicker.setMaxWidth(Double.MAX_VALUE);
        datePicker.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 14px;"
        );

        formPanel.add(dateLabel, 0, row);
        formPanel.add(datePicker, 1, row++);

        // Start Time
        Label startTimeLabel = new Label("Start Time:");
        startTimeLabel.setFont(Font.font("Segoe UI", 14));
        startTimeLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        startTimeField = new TextField();
        startTimeField.setFont(Font.font("Segoe UI", 14));
        startTimeField.setPromptText("HH:mm (e.g., 14:30)");
        startTimeField.setStyle(
                "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8 12;"
        );
        startTimeField.setPrefHeight(40);

        // Add time validation on focus loss
        startTimeField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // lost focus
                validateTimeField(startTimeField);
            }
        });

        formPanel.add(startTimeLabel, 0, row);
        formPanel.add(startTimeField, 1, row++);

        // End Time
        Label endTimeLabel = new Label("End Time:");
        endTimeLabel.setFont(Font.font("Segoe UI", 14));
        endTimeLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        endTimeField = new TextField();
        endTimeField.setFont(Font.font("Segoe UI", 14));
        endTimeField.setPromptText("HH:mm (e.g., 16:00)");
        endTimeField.setStyle(
                "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8 12;"
        );
        endTimeField.setPrefHeight(40);

        // Add time validation on focus loss
        endTimeField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // lost focus
                validateTimeField(endTimeField);
            }
        });

        formPanel.add(endTimeLabel, 0, row);
        formPanel.add(endTimeField, 1, row++);

        // Location
        Label locationLabel = new Label("Location:");
        locationLabel.setFont(Font.font("Segoe UI", 14));
        locationLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        locationField = new TextField();
        locationField.setFont(Font.font("Segoe UI", 14));
        locationField.setPromptText("Enter location");
        locationField.setStyle(
                "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8 12;"
        );
        locationField.setPrefHeight(40);

        formPanel.add(locationLabel, 0, row);
        formPanel.add(locationField, 1, row++);

        // Session Type
        Label typeLabel = new Label("Session Type:");
        typeLabel.setFont(Font.font("Segoe UI", 14));
        typeLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        String[] types = {"Individual", "Group", "Family", "Couple", "Online"};
        typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(types);
        typeCombo.setValue(types[0]); // Default to Individual
        typeCombo.setPrefHeight(40);
        typeCombo.setMaxWidth(Double.MAX_VALUE);
        typeCombo.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 14px;"
        );

        formPanel.add(typeLabel, 0, row);
        formPanel.add(typeCombo, 1, row++);

        // Status
        Label statusLabel = new Label("Status:");
        statusLabel.setFont(Font.font("Segoe UI", 14));
        statusLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        String[] statuses = {"active", "inactive", "scheduled", "completed", "cancelled"};
        statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(statuses);
        statusCombo.setValue("scheduled"); // Default to scheduled
        statusCombo.setPrefHeight(40);
        statusCombo.setMaxWidth(Double.MAX_VALUE);
        statusCombo.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 14px;"
        );

        formPanel.add(statusLabel, 0, row);
        formPanel.add(statusCombo, 1, row++);

        // Add info label about time format
        Label infoLabel = new Label("Time format: HH:mm (24-hour format)");
        infoLabel.setFont(Font.font("Segoe UI", 11));
        infoLabel.setTextFill(Color.web(toHex(TEXT_LIGHT)));
        formPanel.add(infoLabel, 1, row++);

        // Wrap in ScrollPane
        ScrollPane scrollPane = new ScrollPane(formPanel);
        scrollPane.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        scrollPane.setBorder(null);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return scrollPane;
    }

    private boolean validateTimeField(TextField timeField) {
        String timeText = timeField.getText().trim();
        if (timeText.isEmpty()) {
            return false;
        }

        try {
            // Try to parse the time
            LocalTime.parse(timeText, timeFormatter);
            timeField.setStyle(
                    "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                            "-fx-border-radius: 5;" +
                            "-fx-background-radius: 5;" +
                            "-fx-padding: 8 12;"
            );
            return true;
        } catch (Exception e) {
            // Invalid time format
            timeField.setStyle(
                    "-fx-border-color: #" + toHex(ERROR_RED) + ";" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 5;" +
                            "-fx-background-radius: 5;" +
                            "-fx-padding: 8 12;"
            );
            return false;
        }
    }

    private HBox createButtonPanel(boolean isEdit) {
        HBox buttonPanel = new HBox(15);
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);
        buttonPanel.setPadding(new Insets(20, 30, 20, 30));
        buttonPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

        Button cancelButton = createButton("Cancel", BUTTON_LIGHT_GREEN);
        cancelButton.setOnAction(e -> close());

        Button saveButton = createButton(isEdit ? "Save Changes" : "Add Session", ACCENT_DARK_GREEN);
        saveButton.setTextFill(Color.WHITE);
        saveButton.setOnAction(e -> saveSession());

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

        // Hover effect
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

    private void setDefaultValues() {
        datePicker.setValue(LocalDate.now()); // Default to today
        startTimeField.setText("09:00"); // Default start time
        endTimeField.setText("10:00"); // Default end time
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
        // Validate inputs
        if (titleField.getText().trim().isEmpty()) {
            showAlert("Please enter a title for the session.", Alert.AlertType.WARNING);
            return;
        }

        if (datePicker.getValue() == null) {
            showAlert("Please select a date for the session.", Alert.AlertType.WARNING);
            return;
        }

        // Validate start time
        if (!validateTimeField(startTimeField)) {
            showAlert("Please enter a valid start time in HH:mm format (e.g., 14:30).", Alert.AlertType.WARNING);
            return;
        }

        // Validate end time
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
            // Parse times
            LocalTime startTime = LocalTime.parse(startTimeField.getText().trim(), timeFormatter);
            LocalTime endTime = LocalTime.parse(endTimeField.getText().trim(), timeFormatter);

            // Validate that end time is after start time
            if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
                showAlert("End time must be after start time.", Alert.AlertType.WARNING);
                return;
            }

            if (session == null) {
                // Create new session
                Session newSession = new Session();
                newSession.setTitle(titleField.getText().trim());
                newSession.setSessionDate(datePicker.getValue());
                newSession.setStartTime(startTime);
                newSession.setEndTime(endTime);
                newSession.setLocation(locationField.getText().trim());
                newSession.setSessionType(typeCombo.getValue());
                newSession.setStatus(statusCombo.getValue());

                controller.createSession(newSession);

                showAlert("Session created successfully!", Alert.AlertType.INFORMATION);

            } else {
                // Update existing session
                session.setTitle(titleField.getText().trim());
                session.setSessionDate(datePicker.getValue());
                session.setStartTime(startTime);
                session.setEndTime(endTime);
                session.setLocation(locationField.getText().trim());
                session.setSessionType(typeCombo.getValue());
                session.setStatus(statusCombo.getValue());

                controller.updateSession(session);

                showAlert("Session updated successfully!", Alert.AlertType.INFORMATION);
            }

            close();
            // Refresh the sessions panel
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
                type == Alert.AlertType.WARNING ? "Warning" : "Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(this);
        alert.showAndWait();
    }

    // ================= UTILITY =================
    private String toHex(Color color) {
        return String.format("%02x%02x%02x",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }
}