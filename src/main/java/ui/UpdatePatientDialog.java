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

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class UpdatePatientDialog extends Stage {

    private int userId;
    private PatientTablePanel parentPanel;

    // UI Components (inherited from AddPsychologistDialog style)
    private TextField firstNameField;
    private TextField lastNameField;
    private TextField dobField;
    private TextField emailField;
    private TextField phoneField;
    private Button updateButton;
    private Button cancelButton;

    // Color constants matching main app
    private static final Color BACKGROUND_LIGHT = Color.rgb(245, 248, 246);
    private static final Color ACCENT_GREEN = Color.rgb(88, 139, 113);
    private static final Color ERROR_RED = Color.rgb(200, 80, 80);
    private static final Color TEXT_DARK = Color.rgb(40, 70, 50);
    private static final Color BORDER_LIGHT = Color.rgb(200, 220, 210);

    public UpdatePatientDialog(PatientTablePanel parentPanel,
                               int id, String firstName, String lastName,
                               String phone, String dob, String email) {

        this.parentPanel = parentPanel;
        this.userId = id;

        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        setTitle("Update Patient");

        // Main layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        root.setPadding(new Insets(20));

        // Header
        root.setTop(createHeader());

        // Form
        root.setCenter(createForm(firstName, lastName, phone, dob, email));

        // Buttons
        root.setBottom(createButtons());

        // Scene
        Scene scene = new Scene(root, 600, 520);
        setScene(scene);
        setResizable(false);

        showAndWait();
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 20, 0));

        Label title = new Label("Update Patient");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(toHex(ACCENT_GREEN)));

        header.getChildren().add(title);
        return header;
    }

    private GridPane createForm(String firstName, String lastName,
                                String phone, String dob, String email) {
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(10));
        form.setAlignment(Pos.CENTER);

        // Initialize fields with existing values
        firstNameField = createField(firstName);
        lastNameField = createField(lastName);
        dobField = createField(dob);
        emailField = createField(email);
        phoneField = createField(phone);

        // Make email field non-editable (primary key/identifier)
        emailField.setEditable(false);
        emailField.setStyle(
                "-fx-background-color: #f0f0f0;" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 12 20;"
        );

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

        updateButton = createPrimaryButton("Update");
        cancelButton = createDangerButton("Cancel");

        updateButton.setOnAction(e -> handleUpdate());
        cancelButton.setOnAction(e -> close());

        buttonBox.getChildren().addAll(updateButton, cancelButton);
        return buttonBox;
    }

    private void handleUpdate() {
        // Get user by ID
        user u = userservice.getuserById(userId);
        if (u == null) {
            showError("User not found!");
            return;
        }

        // Validate inputs
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String dobString = dobField.getText().trim();  // ✅ Changed variable name
        String email = emailField.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty() ||
                phone.isEmpty() || dobString.isEmpty() || email.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        if (!userservice.isValidEmail(email)) {
            showError("Invalid email format.");
            return;
        }

        if (!userservice.isValidDate(dobString)) {
            showError("Date must be YYYY-MM-DD.");
            return;
        }
        LocalDate dob = null;
        try {
            dob = LocalDate.parse(dobString);
        } catch (DateTimeParseException e) {
            showError("Invalid date format. Please use YYYY-MM-DD (e.g., 1990-05-15)");
            return;
        }
        // Update user object
        u.setFirstname(firstName);
        u.setLastname(lastName);
        u.setPhone(phone);
        u.setDateofbirth(dob);
        u.setEmail(email);

        // Save to database
        if (userservice.updateuser(u)) {
            showSuccess("Patient updated successfully!");
            if (parentPanel != null) {
                parentPanel.refreshTable();
            }
            close();
        } else {
            showError("Failed to update patient.");
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

    private TextField createField(String text) {
        TextField field = new TextField(text);
        field.setFont(Font.font("Arial", 14));
        field.setPrefWidth(300);
        field.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 12 20;"
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