package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.Cursor;
import models.user;
import services.userservice;

public class Mentissignuppanel extends VBox {

    // ⭐ Utilise les constantes de MentisLoginFrame
    private static final Color BG_COLOR = MentisLoginFrame.BACKGROUND_LIGHT;
    private static final Color PRIMARY = MentisLoginFrame.ACCENT_DARK_GREEN;
    private static final Color TEXT_GRAY = Color.GRAY;
    private static final Color TEXT_BLACK = Color.BLACK;
    private static final Color WHITE = Color.WHITE;

    private RoundedTextField firstNameField;
    private RoundedTextField lastNameField;
    private RoundedTextField phoneField;
    private RoundedTextField dobField;
    private RoundedTextField emailField;
    private RoundedPasswordField passwordField;
    private ComboBox<String> typeComboBox;
    private RoundedButton signUpButton;

    private final MentisLoginFrame parentApp;

    public Mentissignuppanel(MentisLoginFrame parentApp) {
        this.parentApp = parentApp;

        setStyle("-fx-background-color: #" + toHex(BG_COLOR) + ";");
        setAlignment(Pos.CENTER);
        setPadding(new Insets(0));
        setSpacing(0);

        initComponents();
    }

    private void initComponents() {
        // Main container
        BorderPane mainContainer = new BorderPane();
        mainContainer.setStyle("-fx-background-color: #" + toHex(BG_COLOR) + ";");

        // Header panel
        HBox headerPanel = createHeader();
        mainContainer.setTop(headerPanel);

        // Center content
        VBox centerPanel = createCenterContent();
        mainContainer.setCenter(centerPanel);

        getChildren().add(mainContainer);
        VBox.setVgrow(mainContainer, Priority.ALWAYS);
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 30, 20, 30));
        header.setStyle("-fx-background-color: #" + toHex(BG_COLOR) + ";");

        Label backLabel = new Label("← Back");
        backLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        backLabel.setTextFill(Color.web(toHex(PRIMARY)));
        backLabel.setCursor(Cursor.HAND);
        backLabel.setOnMouseClicked(e -> parentApp.showWelcomePanel());

        header.getChildren().add(backLabel);
        return header;
    }

    private VBox createCenterContent() {
        VBox center = new VBox(20);
        center.setAlignment(Pos.CENTER);
        center.setStyle("-fx-background-color: #" + toHex(BG_COLOR) + ";");
        center.setPadding(new Insets(20, 50, 40, 50));

        // Title
        Label title = new Label("Create Account");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        title.setTextFill(Color.web(toHex(PRIMARY)));
        title.setTextAlignment(TextAlignment.CENTER);

        // Logo
        HBox logoContainer = loadLogo();

        // Form panel
        VBox formPanel = createFormPanel();

        // Login link
        Label loginLink = new Label("Already have an account? Login");
        loginLink.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        loginLink.setTextFill(Color.web(toHex(PRIMARY)));
        loginLink.setCursor(Cursor.HAND);
        loginLink.setOnMouseClicked(e -> parentApp.showLoginPanel());
        loginLink.setAlignment(Pos.CENTER);
        loginLink.setMaxWidth(Double.MAX_VALUE);

        center.getChildren().addAll(title, logoContainer, formPanel, loginLink);

        // Set VGrow for form panel
        VBox.setVgrow(formPanel, Priority.ALWAYS);

        return center;
    }

    private HBox loadLogo() {
        HBox logoContainer = new HBox();
        logoContainer.setAlignment(Pos.CENTER);
        logoContainer.setStyle("-fx-background-color: #" + toHex(BG_COLOR) + ";");

        try {
            Image logo = new Image(getClass().getResourceAsStream("/resources/logo.png"));
            ImageView logoView = new ImageView(logo);
            logoView.setFitWidth(140);
            logoView.setFitHeight(140);
            logoView.setPreserveRatio(true);
            logoContainer.getChildren().add(logoView);
        } catch (Exception e) {
            // Logo not found, skip
            System.err.println("Logo not found: " + e.getMessage());
        }

        return logoContainer;
    }

    private VBox createFormPanel() {
        VBox formPanel = new VBox(15);
        formPanel.setAlignment(Pos.CENTER);
        formPanel.setStyle("-fx-background-color: #" + toHex(BG_COLOR) + ";");
        formPanel.setPadding(new Insets(20, 50, 20, 50));
        formPanel.setMaxWidth(900);

        // Row 1: First Name & Last Name
        HBox row1 = new HBox(20);
        row1.setAlignment(Pos.CENTER);

        firstNameField = createField("First Name");
        firstNameField.setPrefWidth(300);
        firstNameField.setPrefHeight(50);

        lastNameField = createField("Last Name");
        lastNameField.setPrefWidth(300);
        lastNameField.setPrefHeight(50);

        row1.getChildren().addAll(firstNameField, lastNameField);
        HBox.setHgrow(firstNameField, Priority.ALWAYS);
        HBox.setHgrow(lastNameField, Priority.ALWAYS);

        // Row 2: Phone & Date of Birth
        HBox row2 = new HBox(20);
        row2.setAlignment(Pos.CENTER);

        phoneField = createField("Phone");
        phoneField.setPrefWidth(300);
        phoneField.setPrefHeight(50);

        dobField = createField("YYYY-MM-DD");
        dobField.setPrefWidth(300);
        dobField.setPrefHeight(50);

        row2.getChildren().addAll(phoneField, dobField);
        HBox.setHgrow(phoneField, Priority.ALWAYS);
        HBox.setHgrow(dobField, Priority.ALWAYS);

        // Row 3: User Type & Email
        HBox row3 = new HBox(20);
        row3.setAlignment(Pos.CENTER);

        typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("Select Type", "Patient", "Psychologist", "Admin");
        typeComboBox.setValue("Select Type");
        typeComboBox.setPrefWidth(300);
        typeComboBox.setPrefHeight(50);
        typeComboBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 25;" +
                        "-fx-border-radius: 25;" +
                        "-fx-padding: 5 15;" +
                        "-fx-font-family: 'Arial';" +
                        "-fx-font-size: 16px;"
        );

        // Style the popup list items with the same font
        typeComboBox.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                        setFont(Font.font("Arial", 16));
                        setStyle("-fx-padding: 8 15;");
                    }
                }
            };
            return cell;
        });

        // Also style the button cell (the selected item display)
        typeComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setFont(Font.font("Arial", 16));
                }
            }
        });

        emailField = createField("Email");
        emailField.setPrefWidth(300);
        emailField.setPrefHeight(50);

        row3.getChildren().addAll(typeComboBox, emailField);
        HBox.setHgrow(typeComboBox, Priority.ALWAYS);
        HBox.setHgrow(emailField, Priority.ALWAYS);

        // Row 4: Password
        HBox row4 = new HBox();
        row4.setAlignment(Pos.CENTER);

        passwordField = new RoundedPasswordField();
        passwordField.setPrefWidth(620);
        passwordField.setPrefHeight(50);
        passwordField.setPromptText("Password");
        passwordField.setStyle(passwordField.getStyle() + "-fx-font-family: 'Arial'; -fx-font-size: 16px;");

        row4.getChildren().add(passwordField);
        HBox.setHgrow(passwordField, Priority.ALWAYS);

        // Row 5: Sign Up Button
        HBox row5 = new HBox();
        row5.setAlignment(Pos.CENTER);
        row5.setPadding(new Insets(20, 0, 10, 0));

        signUpButton = new RoundedButton("Sign Up");
        signUpButton.setPrefWidth(300);
        signUpButton.setPrefHeight(60);
        signUpButton.setStyle(signUpButton.getStyle() + "-fx-font-family: 'Arial'; -fx-font-size: 18px; -fx-font-weight: bold;");
        signUpButton.setOnAction(e -> handleSignup());

        row5.getChildren().add(signUpButton);

        formPanel.getChildren().addAll(row1, row2, row3, row4, row5);

        return formPanel;
    }

    private RoundedTextField createField(String placeholder) {
        RoundedTextField field = new RoundedTextField();
        field.setPromptText(placeholder);
        field.setStyle(field.getStyle() + "-fx-font-family: 'Arial'; -fx-font-size: 16px;");
        field.setPrefHeight(50);
        return field;
    }

    private void handleSignup() {
        signUpButton.setDisable(true);

        String fn = firstNameField.getText().trim();
        String ln = lastNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String dob = dobField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String type = typeComboBox.getValue();

        // Validation
        if (fn.isEmpty() || fn.equals("First Name") ||
                ln.isEmpty() || ln.equals("Last Name") ||
                phone.isEmpty() || phone.equals("Phone") ||
                dob.isEmpty() || dob.equals("YYYY-MM-DD") ||
                email.isEmpty() || email.equals("Email") ||
                password.isEmpty() || password.equals("Password") ||
                type == null || type.equals("Select Type")) {

            showError("All fields are required");
            signUpButton.setDisable(false);
            return;
        }

        if (!userservice.isValidEmail(email)) {
            showError("Invalid email format");
            signUpButton.setDisable(false);
            return;
        }

        if (userservice.emailExists(email)) {
            showError("Email already exists");
            signUpButton.setDisable(false);
            return;
        }

        user u = new user(fn, ln, phone, dob, type, email, password);

        if (userservice.registeruser(u)) {
            showSuccess("Account created successfully!");
            parentApp.showLoginPanel();
        } else {
            showError("Registration failed");
            signUpButton.setDisable(false);
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ================= UTILITY =================
    private String toHex(Color color) {
        return String.format("%02x%02x%02x",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }

    // ================= CUSTOM UI COMPONENTS =================

    class RoundedButton extends Button {
        RoundedButton(String text) {
            super(text);
            setTextFill(Color.WHITE);
            setCursor(Cursor.HAND);

            // Apply rounded style
            setStyle(
                    "-fx-background-color: #" + toHex(PRIMARY) + ";" +
                            "-fx-background-radius: 30;" +
                            "-fx-border-radius: 30;" +
                            "-fx-padding: 12 30;"
            );

            // Hover effect
            setOnMouseEntered(e ->
                    setStyle(
                            "-fx-background-color: #" + toHex(PRIMARY.darker()) + ";" +
                                    "-fx-background-radius: 30;" +
                                    "-fx-border-radius: 30;" +
                                    "-fx-padding: 12 30;"
                    )
            );

            setOnMouseExited(e ->
                    setStyle(
                            "-fx-background-color: #" + toHex(PRIMARY) + ";" +
                                    "-fx-background-radius: 30;" +
                                    "-fx-border-radius: 30;" +
                                    "-fx-padding: 12 30;"
                    )
            );
        }
    }

    class RoundedTextField extends TextField {
        RoundedTextField() {
            super();

            // Apply rounded style
            setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 25;" +
                            "-fx-border-radius: 25;" +
                            "-fx-padding: 12 20;" +
                            "-fx-prompt-text-fill: #808080;"
            );

            // Add focus effect
            focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    setStyle(
                            "-fx-background-color: white;" +
                                    "-fx-background-radius: 25;" +
                                    "-fx-border-radius: 25;" +
                                    "-fx-border-color: #" + toHex(PRIMARY) + ";" +
                                    "-fx-border-width: 2;" +
                                    "-fx-padding: 12 20;" +
                                    "-fx-prompt-text-fill: #808080;"
                    );
                } else {
                    setStyle(
                            "-fx-background-color: white;" +
                                    "-fx-background-radius: 25;" +
                                    "-fx-border-radius: 25;" +
                                    "-fx-border-color: transparent;" +
                                    "-fx-padding: 12 20;" +
                                    "-fx-prompt-text-fill: #808080;"
                    );
                }
            });
        }
    }

    class RoundedPasswordField extends PasswordField {
        RoundedPasswordField() {
            super();

            // Apply rounded style
            setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 25;" +
                            "-fx-border-radius: 25;" +
                            "-fx-padding: 12 20;" +
                            "-fx-prompt-text-fill: #808080;"
            );

            // Add focus effect
            focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    setStyle(
                            "-fx-background-color: white;" +
                                    "-fx-background-radius: 25;" +
                                    "-fx-border-radius: 25;" +
                                    "-fx-border-color: #" + toHex(PRIMARY) + ";" +
                                    "-fx-border-width: 2;" +
                                    "-fx-padding: 12 20;" +
                                    "-fx-prompt-text-fill: #808080;"
                    );
                } else {
                    setStyle(
                            "-fx-background-color: white;" +
                                    "-fx-background-radius: 25;" +
                                    "-fx-border-radius: 25;" +
                                    "-fx-border-color: transparent;" +
                                    "-fx-padding: 12 20;" +
                                    "-fx-prompt-text-fill: #808080;"
                    );
                }
            });
        }
    }
}