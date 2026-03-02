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
import javafx.stage.FileChooser;
import javafx.concurrent.Task;
import models.user;
import services.userservice;
import services.CVParserService;
import services.CVSummarizationService;

import java.io.File;

public class Mentissignuppanel extends VBox {

    // ⭐ MERGED: Using both your constants and their structure
    private static final Color BG_COLOR = MentisLoginFrame.BACKGROUND_LIGHT;
    private static final Color PRIMARY = MentisLoginFrame.ACCENT_DARK_GREEN;
    private static final Color TEXT_GRAY = Color.GRAY;
    private static final Color TEXT_BLACK = Color.BLACK;
    private static final Color WHITE = Color.WHITE;

    // Regular fields
    private RoundedTextField firstNameField;
    private RoundedTextField lastNameField;
    private RoundedTextField phoneField;
    private RoundedTextField dobField;
    private RoundedTextField emailField;
    private RoundedPasswordField passwordField;
    private ComboBox<String> typeComboBox;
    private RoundedButton signUpButton;
    private CheckBox enableFaceIDCheckBox;

    // CV Upload Section (Only for Psychologists)
    private VBox cvUploadSection;
    private Button uploadCVButton;
    private Label cvStatusLabel;
    private TextArea summaryArea;
    private ProgressIndicator aiProgressIndicator;
    private Label cvInfoLabel;

    // Services
    private CVParserService cvParser;
    private CVSummarizationService cvSummarizer;

    private final MentisLoginFrame parentApp;

    public Mentissignuppanel(MentisLoginFrame parentApp) {
        this.parentApp = parentApp;
        this.cvParser = new CVParserService();
        this.cvSummarizer = new CVSummarizationService();

        setStyle("-fx-background-color: #" + toHex(BG_COLOR) + ";");
        setAlignment(Pos.CENTER);
        setPadding(new Insets(0));
        setSpacing(0);

        initComponents();
    }

    private void initComponents() {
        BorderPane mainContainer = new BorderPane();
        mainContainer.setStyle("-fx-background-color: #" + toHex(BG_COLOR) + ";");

        // Header
        HBox headerPanel = createHeader();
        mainContainer.setTop(headerPanel);

        // Center content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setFitToWidth(true);
        scrollPane.setBorder(null);

        VBox centerPanel = createCenterContent();
        scrollPane.setContent(centerPanel);

        mainContainer.setCenter(scrollPane);

        getChildren().add(mainContainer);
        VBox.setVgrow(mainContainer, Priority.ALWAYS);
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 30, 20, 30));
        header.setStyle("-fx-background-color: transparent;");

        Label backLabel = new Label("← Back");
        backLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        backLabel.setTextFill(Color.web(toHex(PRIMARY)));
        backLabel.setCursor(javafx.scene.Cursor.HAND);
        backLabel.setOnMouseClicked(e -> parentApp.showWelcomePanel());

        header.getChildren().add(backLabel);
        return header;
    }

    private VBox createCenterContent() {
        VBox center = new VBox(20);
        center.setAlignment(Pos.CENTER);
        center.setStyle("-fx-background-color: transparent;");
        center.setPadding(new Insets(20, 50, 40, 50));

        // Title
        Label title = new Label("Create Account");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        title.setTextFill(Color.web(toHex(PRIMARY)));

        // Logo
        HBox logoContainer = loadLogo();

        // Form panel
        VBox formPanel = createFormPanel();

        center.getChildren().addAll(title, logoContainer, formPanel);
        VBox.setVgrow(formPanel, Priority.ALWAYS);

        return center;
    }

    private HBox loadLogo() {
        HBox logoContainer = new HBox();
        logoContainer.setAlignment(Pos.CENTER);
        logoContainer.setStyle("-fx-background-color: transparent;");

        try {
            Image logo = new Image(getClass().getResourceAsStream("/resources/logo.png"));
            ImageView logoView = new ImageView(logo);
            logoView.setFitWidth(100);
            logoView.setFitHeight(100);
            logoView.setPreserveRatio(true);
            logoContainer.getChildren().add(logoView);
        } catch (Exception e) {
            System.err.println("Logo not found: " + e.getMessage());
        }
        return logoContainer;
    }

    private VBox createFormPanel() {
        VBox formPanel = new VBox(15);
        formPanel.setAlignment(Pos.CENTER);
        formPanel.setStyle("-fx-background-color: transparent;");
        formPanel.setPadding(new Insets(20, 0, 20, 0));
        formPanel.setMaxWidth(900);

        // Row 1: First Name & Last Name
        HBox row1 = new HBox(20);
        row1.setAlignment(Pos.CENTER);
        firstNameField = createField("First Name");
        lastNameField = createField("Last Name");
        row1.getChildren().addAll(firstNameField, lastNameField);
        HBox.setHgrow(firstNameField, Priority.ALWAYS);
        HBox.setHgrow(lastNameField, Priority.ALWAYS);

        // Row 2: Phone & Date of Birth
        HBox row2 = new HBox(20);
        row2.setAlignment(Pos.CENTER);
        phoneField = createField("Phone");
        dobField = createField("YYYY-MM-DD");
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
        typeComboBox.setPrefHeight(55);
        typeComboBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-radius: 15;" +
                        "-fx-padding: 8 15;" +
                        "-fx-font-family: 'Arial';" +
                        "-fx-font-size: 16px;"
        );

        // Add listener for type selection - to show/hide CV section
        typeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            toggleCVSection("Psychologist".equals(newVal));
        });

        emailField = createField("Email");
        row3.getChildren().addAll(typeComboBox, emailField);
        HBox.setHgrow(typeComboBox, Priority.ALWAYS);
        HBox.setHgrow(emailField, Priority.ALWAYS);

        // Row 4: Password
        HBox row4 = new HBox();
        row4.setAlignment(Pos.CENTER);
        passwordField = new RoundedPasswordField();
        passwordField.setPrefWidth(620);
        passwordField.setPrefHeight(55);
        passwordField.setPromptText("Password");
        row4.getChildren().add(passwordField);
        HBox.setHgrow(passwordField, Priority.ALWAYS);

        // ===== CV UPLOAD SECTION - ONLY FOR PSYCHOLOGISTS =====
        cvUploadSection = createCVUploadSection();
        cvUploadSection.setVisible(false); // Initially hidden
        cvUploadSection.setManaged(false);

        // Face ID Option
        HBox rowFace = new HBox(10);
        rowFace.setAlignment(Pos.CENTER_LEFT);
        enableFaceIDCheckBox = new CheckBox("Enable Face ID for faster login");
        enableFaceIDCheckBox.setFont(Font.font("Arial", 14));
        enableFaceIDCheckBox.setTextFill(Color.web(toHex(PRIMARY.darker())));
        Tooltip faceTooltip = new Tooltip("After registration, you can use your face to login");
        enableFaceIDCheckBox.setTooltip(faceTooltip);
        rowFace.getChildren().add(enableFaceIDCheckBox);

        // Sign Up Button
        HBox row5 = new HBox();
        row5.setAlignment(Pos.CENTER);
        row5.setPadding(new Insets(20, 0, 10, 0));
        signUpButton = new RoundedButton("Sign Up");
        signUpButton.setPrefWidth(300);
        signUpButton.setPrefHeight(60);
        signUpButton.setOnAction(e -> handleSignup());
        row5.getChildren().add(signUpButton);

        // Login link
        Label loginLink = new Label("Already have an account? Login");
        loginLink.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        loginLink.setTextFill(Color.web(toHex(PRIMARY)));
        loginLink.setCursor(javafx.scene.Cursor.HAND);
        loginLink.setOnMouseClicked(e -> parentApp.showLoginPanel());
        loginLink.setAlignment(Pos.CENTER);
        loginLink.setMaxWidth(Double.MAX_VALUE);

        formPanel.getChildren().addAll(
                row1, row2, row3, row4,
                cvUploadSection, // This will only show for psychologists
                rowFace, row5, loginLink
        );

        return formPanel;
    }

    /**
     * Toggle CV section visibility based on user type
     */
    private void toggleCVSection(boolean show) {
        cvUploadSection.setVisible(show);
        cvUploadSection.setManaged(show);

        if (show) {
            // Clear any previous CV data when switching to psychologist
            clearCVData();
        }
    }

    /**
     * Clear CV-related data
     */
    private void clearCVData() {
        cvStatusLabel.setText("No file selected");
        if (summaryArea != null) {
            summaryArea.clear();
            summaryArea.setVisible(false);
            summaryArea.setManaged(false);
        }
    }

    private VBox createCVUploadSection() {
        VBox panel = new VBox(10);
        panel.setStyle(
                "-fx-background-color: #f0f8ff;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: #" + toHex(PRIMARY) + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-padding: 15;"
        );

        // Header with icon
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label("📄");
        iconLabel.setFont(Font.font("Arial", 24));

        cvInfoLabel = new Label("Psychologist CV Upload (Optional)");
        cvInfoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        cvInfoLabel.setTextFill(Color.web(toHex(PRIMARY)));

        headerBox.getChildren().addAll(iconLabel, cvInfoLabel);

        // Description
        Label descLabel = new Label("Upload your CV to auto-fill your professional information. You can still fill manually.");
        descLabel.setFont(Font.font("Arial", 12));
        descLabel.setTextFill(Color.GRAY);
        descLabel.setWrapText(true);

        // CV Upload Row
        HBox cvUploadBox = new HBox(10);
        cvUploadBox.setAlignment(Pos.CENTER_LEFT);

        uploadCVButton = new Button("📄 Upload CV (PDF/DOCX)");
        uploadCVButton.setStyle(
                "-fx-background-color: #" + toHex(PRIMARY) + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 8 15;" +
                        "-fx-background-radius: 8;"
        );
        uploadCVButton.setOnAction(e -> handleCVUpload());

        cvStatusLabel = new Label("No file selected");
        cvStatusLabel.setFont(Font.font("Arial", 11));
        cvStatusLabel.setTextFill(Color.GRAY);

        aiProgressIndicator = new ProgressIndicator();
        aiProgressIndicator.setPrefSize(25, 25);
        aiProgressIndicator.setVisible(false);

        cvUploadBox.getChildren().addAll(uploadCVButton, cvStatusLabel, aiProgressIndicator);

        // Summary area (initially hidden)
        summaryArea = new TextArea();
        summaryArea.setPrefRowCount(3);
        summaryArea.setWrapText(true);
        summaryArea.setPromptText("AI summary will appear here after upload...");
        summaryArea.setEditable(true);
        summaryArea.setVisible(false);
        summaryArea.setManaged(false);

        panel.getChildren().addAll(headerBox, descLabel, cvUploadBox, summaryArea);

        return panel;
    }

    private void handleCVUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload CV (Optional)");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Word Documents", "*.docx"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(getScene().getWindow());
        if (selectedFile != null) {
            cvStatusLabel.setText(selectedFile.getName());
            processCV(selectedFile);
        }
    }

    private void processCV(File cvFile) {
        uploadCVButton.setDisable(true);
        aiProgressIndicator.setVisible(true);
        summaryArea.setVisible(true);
        summaryArea.setManaged(true);
        summaryArea.setText("⏳ Processing CV with AI... This may take a few seconds.");

        Task<Void> processingTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // Extract text from CV
                    String cvText = cvParser.extractTextFromCV(cvFile);

                    // Get AI summary
                    CVSummarizationService.CVSummary summary = cvSummarizer.summarizeCV(cvText);

                    // Update UI on JavaFX thread
                    javafx.application.Platform.runLater(() -> {
                        // Auto-fill the fields with extracted data
                        if (!summary.getFirstname().isEmpty()) {
                            firstNameField.setText(summary.getFirstname());
                        }
                        if (!summary.getLastname().isEmpty()) {
                            lastNameField.setText(summary.getLastname());
                        }
                        if (!summary.getEmail().isEmpty()) {
                            emailField.setText(summary.getEmail());
                        }
                        if (!summary.getPhone().isEmpty()) {
                            phoneField.setText(summary.getPhone());
                        }
                        if (!summary.getDateofbirth().isEmpty()) {
                            dobField.setText(summary.getDateofbirth());
                        }

                        // Create summary message
                        StringBuilder extracted = new StringBuilder("✅ CV Processed!\n\n");
                        extracted.append("Extracted Information:\n");
                        extracted.append("• Name: ").append(summary.getFirstname()).append(" ").append(summary.getLastname()).append("\n");
                        extracted.append("• Email: ").append(summary.getEmail().isEmpty() ? "Not found" : summary.getEmail()).append("\n");
                        extracted.append("• Phone: ").append(summary.getPhone().isEmpty() ? "Not found" : summary.getPhone()).append("\n");
                        extracted.append("• DOB: ").append(summary.getDateofbirth().isEmpty() ? "Not found" : summary.getDateofbirth()).append("\n\n");
                        extracted.append("You can edit any field below:");

                        summaryArea.setText(extracted.toString());

                        cvStatusLabel.setText("✓ CV processed! Form auto-filled.");

                        // Show success message
                        if (!summary.getFirstname().isEmpty() || !summary.getLastname().isEmpty()) {
                            showInfo("CV processed successfully! Form has been auto-filled. You can edit any information.");
                        } else {
                            showAlert("Warning",
                                    "Could not extract much information from the CV. Please fill in the fields manually.",
                                    Alert.AlertType.WARNING);
                        }
                    });

                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        summaryArea.setText("❌ Error processing CV: " + e.getMessage() + "\n\nPlease fill the form manually.");
                        showAlert("Error", "Failed to process CV: " + e.getMessage(), Alert.AlertType.ERROR);
                    });
                }

                javafx.application.Platform.runLater(() -> {
                    uploadCVButton.setDisable(false);
                    aiProgressIndicator.setVisible(false);
                });

                return null;
            }
        };

        new Thread(processingTask).start();
    }

    private RoundedTextField createField(String placeholder) {
        RoundedTextField field = new RoundedTextField();
        field.setPromptText(placeholder);
        field.setPrefHeight(55);
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
        boolean enableFaceID = enableFaceIDCheckBox.isSelected();

        // Validation
        if (type == null || type.equals("Select Type")) {
            showError("Please select a user type");
            signUpButton.setDisable(false);
            return;
        }

        if (fn.isEmpty() || ln.isEmpty() || phone.isEmpty() ||
                dob.isEmpty() || email.isEmpty() || password.isEmpty()) {
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

        // Create user object
        user u = new user(fn, ln, phone, dob, type, email, password);

        // Register user
        if (userservice.registeruser(u)) {
            showSuccess("Account created successfully!");

            if (enableFaceID) {
                showInfo("Please register your face now for quick login");
                user newUser = userservice.getuserByEmail(email);
                if (newUser != null) {
                    openFaceIDRegistration(newUser.getId());
                }
            } else {
                parentApp.showLoginPanel();
            }

        } else {
            showError("Registration failed");
            signUpButton.setDisable(false);
        }
    }

    private void openFaceIDRegistration(int userId) {
        try {
            // Assuming FaceIDDialog exists in your project
            // If not, you may need to comment this out or implement it
            // FaceIDDialog dialog = new FaceIDDialog(parentApp, true, userId);
            // dialog.setOnHidden(e -> parentApp.showLoginPanel());
            // dialog.show();

            // For now, just go to login panel
            parentApp.showLoginPanel();
        } catch (Exception e) {
            e.printStackTrace();
            parentApp.showLoginPanel();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        signUpButton.setDisable(false);
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

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
            setCursor(javafx.scene.Cursor.HAND);
            setStyle(
                    "-fx-background-color: #" + toHex(PRIMARY) + ";" +
                            "-fx-background-radius: 15;" +
                            "-fx-padding: 12 30;"
            );
            setOnMouseEntered(e ->
                    setStyle(
                            "-fx-background-color: #" + toHex(PRIMARY.darker()) + ";" +
                                    "-fx-background-radius: 15;"
                    )
            );
            setOnMouseExited(e ->
                    setStyle(
                            "-fx-background-color: #" + toHex(PRIMARY) + ";" +
                                    "-fx-background-radius: 15;"
                    )
            );
        }
    }

    class RoundedTextField extends TextField {
        RoundedTextField() {
            super();
            setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 15;" +
                            "-fx-border-radius: 15;" +
                            "-fx-border-color: transparent;" +
                            "-fx-padding: 12 20;" +
                            "-fx-prompt-text-fill: #808080;" +
                            "-fx-font-family: 'Arial';" +
                            "-fx-font-size: 16px;"
            );
            focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    setStyle(
                            "-fx-background-color: white;" +
                                    "-fx-background-radius: 15;" +
                                    "-fx-border-radius: 15;" +
                                    "-fx-border-color: #" + toHex(PRIMARY) + ";" +
                                    "-fx-border-width: 2;" +
                                    "-fx-padding: 12 20;"
                    );
                } else {
                    setStyle(
                            "-fx-background-color: white;" +
                                    "-fx-background-radius: 15;" +
                                    "-fx-border-radius: 15;" +
                                    "-fx-border-color: transparent;" +
                                    "-fx-padding: 12 20;"
                    );
                }
            });
        }
    }

    class RoundedPasswordField extends PasswordField {
        RoundedPasswordField() {
            super();
            setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 15;" +
                            "-fx-border-radius: 15;" +
                            "-fx-border-color: transparent;" +
                            "-fx-padding: 12 20;" +
                            "-fx-prompt-text-fill: #808080;" +
                            "-fx-font-family: 'Arial';" +
                            "-fx-font-size: 16px;"
            );
            focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    setStyle(
                            "-fx-background-color: white;" +
                                    "-fx-background-radius: 15;" +
                                    "-fx-border-radius: 15;" +
                                    "-fx-border-color: #" + toHex(PRIMARY) + ";" +
                                    "-fx-border-width: 2;" +
                                    "-fx-padding: 12 20;"
                    );
                } else {
                    setStyle(
                            "-fx-background-color: white;" +
                                    "-fx-background-radius: 15;" +
                                    "-fx-border-radius: 15;" +
                                    "-fx-border-color: transparent;" +
                                    "-fx-padding: 12 20;"
                    );
                }
            });
        }
    }
}