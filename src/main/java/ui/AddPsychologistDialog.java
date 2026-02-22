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
import models.user;
import services.userservice;

public class AddPsychologistDialog extends Stage {

    private TextField firstNameField;
    private TextField lastNameField;
    private TextField dobField;
    private TextField emailField;
    private TextField phoneField;
    private Button addButton;
    private Button cancelButton;
    private PsychologistTablePanel parentPanel;

    // Color constants matching main app
    private static final Color BACKGROUND_LIGHT = Color.rgb(245, 248, 246);
    private static final Color ACCENT_GREEN = Color.rgb(88, 139, 113);
    private static final Color ERROR_RED = Color.rgb(200, 80, 80);
    private static final Color TEXT_DARK = Color.rgb(40, 70, 50);

    public AddPsychologistDialog(PsychologistTablePanel parentPanel) {
        this.parentPanel = parentPanel;

        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        setTitle("Add Psychologist");

        // Main layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        root.setPadding(new Insets(20));

        // Header
        root.setTop(createHeader());

        // Form
        root.setCenter(createForm());

        // Buttons
        root.setBottom(createButtons());

        // Scene
        Scene scene = new Scene(root, 600, 520);
        setScene(scene);
        setResizable(false);
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 20, 0));

        Label title = new Label("Add Psychologist");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(toHex(ACCENT_GREEN)));

        header.getChildren().add(title);
        return header;
    }

    private GridPane createForm() {
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(10));
        form.setAlignment(Pos.CENTER);

        // Initialize fields
        firstNameField = createField();
        lastNameField = createField();
        dobField = createField("YYYY-MM-DD");
        emailField = createField();
        phoneField = createField();

        // Add rows
        addRow(form, 0, "First Name:", firstNameField);
        addRow(form, 1, "Last Name:", lastNameField);
        addRow(form, 2, "Date of Birth:", dobField);
        addRow(form, 3, "Email:", emailField);
        addRow(form, 4, "Phone:", phoneField);

        return form;
    }

    private HBox createButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        addButton = createPrimaryButton("Add");
        cancelButton = createDangerButton("Cancel");

        addButton.setOnAction(e -> handleAdd());
        cancelButton.setOnAction(e -> close());

        buttonBox.getChildren().addAll(addButton, cancelButton);
        return buttonBox;
    }

    private void handleAdd() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String dob = dobField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        // Validation
        if (firstName.isEmpty() || lastName.isEmpty() || dob.isEmpty()
                || email.isEmpty() || phone.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        if (!userservice.isValidEmail(email)) {
            showError("Invalid email format.");
            return;
        }

        if (!userservice.isValidDate(dob)) {
            showError("Date must be YYYY-MM-DD.");
            return;
        }

        if (userservice.emailExists(email)) {
            showError("Email already exists.");
            return;
        }

        // Create and register user
        user psych = new user(firstName, lastName, phone, dob,
                "psychologist", email, "doctor123");

        if (userservice.registeruser(psych)) {
            showSuccess("Psychologist added successfully!");
            if (parentPanel != null) {
                parentPanel.refreshTable();
            }
            close();
        } else {
            showError("Failed to add psychologist.");
        }
    }

    // ================= HELPER METHODS =================

    private void addRow(GridPane grid, int row, String labelText, TextField field) {
        Label label = new Label(labelText);
        label.setFont(Font.font("Arial", 14));
        label.setTextFill(Color.web(toHex(TEXT_DARK)));

        grid.add(label, 0, row);
        grid.add(field, 1, row);

        // Set column constraints for better layout
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(120);
        col1.setHalignment(javafx.geometry.HPos.RIGHT);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setMinWidth(300);
        col2.setHgrow(Priority.ALWAYS);

        if (grid.getColumnConstraints().isEmpty()) {
            grid.getColumnConstraints().addAll(col1, col2);
        }
    }

    private TextField createField() {
        return createField("");
    }

    private TextField createField(String promptText) {
        TextField field = new TextField();
        field.setPromptText(promptText);
        field.setFont(Font.font("Arial", 14));
        field.setPrefWidth(300);
        field.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #d3d3d3;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8;"
        );
        return field;
    }

    private Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        button.setTextFill(Color.BLACK);
        button.setStyle(
                "-fx-background-color: #" + toHex(ACCENT_GREEN) + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 10 25;" +
                        "-fx-cursor: hand;"
        );

        // Hover effect
        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(ACCENT_GREEN.darker()) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 10 25;" +
                                "-fx-cursor: hand;"
                )
        );
        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(ACCENT_GREEN) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 10 25;" +
                                "-fx-cursor: hand;"
                )
        );

        return button;
    }

    private Button createDangerButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        button.setTextFill(Color.BLACK);
        button.setStyle(
                "-fx-background-color: #" + toHex(ERROR_RED) + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 10 25;" +
                        "-fx-cursor: hand;"
        );

        // Hover effect
        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(ERROR_RED.darker()) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 10 25;" +
                                "-fx-cursor: hand;"
                )
        );
        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(ERROR_RED) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 10 25;" +
                                "-fx-cursor: hand;"
                )
        );

        return button;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(this);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(this);
        alert.showAndWait();
    }

    // ================= UTILITY METHODS =================

    private String toHex(Color color) {
        return String.format("%02x%02x%02x",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }
}