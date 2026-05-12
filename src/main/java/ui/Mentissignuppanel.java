package ui;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import models.user;
import services.CVParserService;
import services.CVSummarizationService;
import services.userservice;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Mentissignuppanel extends StackPane {

    private static final Color PRIMARY = Color.web("#50C878");
    private static final Color PRIMARY_DARK = Color.web("#2E7D32");
    private static final Color PRIMARY_MID = Color.web("#3A9B5E");
    private static final Color INK = Color.web("#1A3C34");
    private static final Color MUTED = Color.web("#6C757D");

    private RoundedTextField firstNameField;
    private RoundedTextField lastNameField;
    private RoundedTextField phoneField;
    private RoundedTextField dobField;
    private RoundedTextField emailField;
    private RoundedPasswordField passwordField;
    private ComboBox<String> typeComboBox;
    private RoundedButton signUpButton;
    private CheckBox enableFaceIDCheckBox;

    private VBox cvUploadSection;
    private Button uploadCVButton;
    private Label cvStatusLabel;
    private TextArea summaryArea;
    private ProgressIndicator aiProgressIndicator;
    private Label cvInfoLabel;

    private CVParserService cvParser;
    private CVSummarizationService cvSummarizer;

    private final MentisLoginFrame parentApp;

    public Mentissignuppanel(MentisLoginFrame parentApp) {
        this.parentApp = parentApp;
        this.cvParser = new CVParserService();
        this.cvSummarizer = new CVSummarizationService();

        setStyle("-fx-background-color: linear-gradient(to bottom right, #F5F7FA, #E8F5E9);");
        setPadding(new Insets(0));

        initComponents();
        startEntranceAnimation();
    }

    private void initComponents() {
        Pane background = createBackgroundDecor();

        BorderPane page = new BorderPane();
        page.setPadding(new Insets(28, 48, 28, 48));
        page.setStyle("-fx-background-color: transparent;");

        page.setTop(createHeader());

        ScrollPane scrollPane = new ScrollPane(createCenterContent());
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(
                "-fx-background: transparent;" +
                        "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;"
        );

        page.setCenter(scrollPane);

        getChildren().addAll(background, page);
    }

    private Pane createBackgroundDecor() {
        Pane pane = new Pane();
        pane.setMouseTransparent(true);

        Circle glow1 = new Circle(210);
        glow1.setFill(Color.web("#50C878", 0.16));
        glow1.setEffect(new GaussianBlur(70));
        glow1.setTranslateX(-440);
        glow1.setTranslateY(-260);

        Circle glow2 = new Circle(260);
        glow2.setFill(Color.web("#9B5DE5", 0.08));
        glow2.setEffect(new GaussianBlur(80));
        glow2.setTranslateX(480);
        glow2.setTranslateY(270);

        pane.getChildren().addAll(glow1, glow2);
        return pane;
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        HBox brand = new HBox(12);
        brand.setAlignment(Pos.CENTER_LEFT);

        Node logo = loadLogo();
        if (logo == null) logo = new BrainLogo();

        VBox text = new VBox(1);
        Label name = new Label("MENTIS");
        name.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 26));
        name.setTextFill(PRIMARY_DARK);

        Label subtitle = new Label("Mental Health Platform");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        subtitle.setTextFill(MUTED);

        text.getChildren().addAll(name, subtitle);
        brand.getChildren().addAll(logo, text);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label backLabel = new Label("← Back");
        backLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        backLabel.setTextFill(PRIMARY_DARK);
        backLabel.setCursor(Cursor.HAND);
        backLabel.setOnMouseClicked(e -> parentApp.showWelcomePanel());

        header.getChildren().addAll(brand, spacer, backLabel);
        return header;
    }

    private VBox createCenterContent() {
        VBox center = new VBox(24);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(24, 20, 36, 20));

        Label badge = new Label("✨ Join Mentis");
        badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        badge.setTextFill(PRIMARY_DARK);
        badge.setPadding(new Insets(8, 16, 8, 16));
        badge.setStyle(
                "-fx-background-color: #F1F8E9;" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-color: #C8E6D2;" +
                        "-fx-border-radius: 999;"
        );

        Label title = new Label("Create Your Account");
        title.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 46));
        title.setTextFill(INK);

        Label subtitle = new Label("Start your wellness journey with personalized assessments, sessions, goals, and AI-powered recommendations.");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
        subtitle.setTextFill(MUTED);
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(760);
        subtitle.setAlignment(Pos.CENTER);
        subtitle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        VBox formPanel = createFormPanel();

        center.getChildren().addAll(badge, title, subtitle, formPanel);
        return center;
    }

    private VBox createFormPanel() {
        VBox card = new VBox(18);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(34));
        card.setMaxWidth(940);
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.94);" +
                        "-fx-background-radius: 34;" +
                        "-fx-border-radius: 34;" +
                        "-fx-border-color: rgba(255,255,255,0.8);" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 34, 0, 0, 14);"
        );

        HBox row1 = new HBox(18);
        row1.setAlignment(Pos.CENTER);
        firstNameField = createField("First Name");
        lastNameField = createField("Last Name");
        row1.getChildren().addAll(firstNameField, lastNameField);
        HBox.setHgrow(firstNameField, Priority.ALWAYS);
        HBox.setHgrow(lastNameField, Priority.ALWAYS);

        HBox row2 = new HBox(18);
        row2.setAlignment(Pos.CENTER);
        phoneField = createField("Phone");
        dobField = createField("YYYY-MM-DD");
        row2.getChildren().addAll(phoneField, dobField);
        HBox.setHgrow(phoneField, Priority.ALWAYS);
        HBox.setHgrow(dobField, Priority.ALWAYS);

        HBox row3 = new HBox(18);
        row3.setAlignment(Pos.CENTER);

        typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("Select Type", "Patient", "Psychologist", "Admin");
        typeComboBox.setValue("Select Type");
        typeComboBox.setPrefHeight(56);
        typeComboBox.setMaxWidth(Double.MAX_VALUE);
        styleCombo(typeComboBox);

        typeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            toggleCVSection("Psychologist".equals(newVal));
        });

        emailField = createField("Email");
        row3.getChildren().addAll(typeComboBox, emailField);
        HBox.setHgrow(typeComboBox, Priority.ALWAYS);
        HBox.setHgrow(emailField, Priority.ALWAYS);

        HBox row4 = new HBox();
        row4.setAlignment(Pos.CENTER);
        passwordField = new RoundedPasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(56);
        passwordField.setMaxWidth(Double.MAX_VALUE);
        row4.getChildren().add(passwordField);
        HBox.setHgrow(passwordField, Priority.ALWAYS);

        cvUploadSection = createCVUploadSection();
        cvUploadSection.setVisible(false);
        cvUploadSection.setManaged(false);

        HBox optionsRow = new HBox(12);
        optionsRow.setAlignment(Pos.CENTER_LEFT);
        optionsRow.setMaxWidth(Double.MAX_VALUE);

        enableFaceIDCheckBox = new CheckBox("Enable Face ID for faster login");
        enableFaceIDCheckBox.setFont(Font.font("Segoe UI", 14));
        enableFaceIDCheckBox.setTextFill(INK);
        enableFaceIDCheckBox.setTooltip(new Tooltip("After registration, you can use your face to login"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label secureNote = new Label("Private by design");
        secureNote.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        secureNote.setTextFill(PRIMARY_DARK);
        secureNote.setPadding(new Insets(7, 13, 7, 13));
        secureNote.setStyle("-fx-background-color: #F1F8E9; -fx-background-radius: 999;");

        optionsRow.getChildren().addAll(enableFaceIDCheckBox, spacer, secureNote);

        signUpButton = new RoundedButton("Create Account");
        signUpButton.setPrefWidth(320);
        signUpButton.setPrefHeight(58);
        signUpButton.setOnAction(e -> handleSignup());

        HBox buttonRow = new HBox(signUpButton);
        buttonRow.setAlignment(Pos.CENTER);
        buttonRow.setPadding(new Insets(10, 0, 0, 0));

        Label loginLink = new Label("Already have an account? Login");
        loginLink.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        loginLink.setTextFill(PRIMARY_DARK);
        loginLink.setCursor(Cursor.HAND);
        loginLink.setOnMouseClicked(e -> parentApp.showLoginPanel());
        loginLink.setAlignment(Pos.CENTER);
        loginLink.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(
                row1, row2, row3, row4,
                cvUploadSection,
                optionsRow,
                buttonRow,
                loginLink
        );

        return card;
    }

    private void toggleCVSection(boolean show) {
        cvUploadSection.setVisible(show);
        cvUploadSection.setManaged(show);

        if (show) clearCVData();
    }

    private void clearCVData() {
        cvStatusLabel.setText("No file selected");
        if (summaryArea != null) {
            summaryArea.clear();
            summaryArea.setVisible(false);
            summaryArea.setManaged(false);
        }
    }

    private VBox createCVUploadSection() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(20));
        panel.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #F0FAF4, #E8F5E9);" +
                        "-fx-background-radius: 22;" +
                        "-fx-border-radius: 22;" +
                        "-fx-border-color: #C8E6D2;" +
                        "-fx-border-width: 1.5;"
        );

        HBox headerBox = new HBox(12);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(48, 48);
        iconBox.setMaxSize(48, 48);
        iconBox.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #50C878, #2E7D32);" +
                        "-fx-background-radius: 16;"
        );

        Label iconLabel = new Label("📄");
        iconLabel.setFont(Font.font("Segoe UI Emoji", 24));
        iconBox.getChildren().add(iconLabel);

        VBox titleBox = new VBox(3);
        cvInfoLabel = new Label("Psychologist CV Upload");
        cvInfoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        cvInfoLabel.setTextFill(PRIMARY_DARK);

        Label descLabel = new Label("Upload your CV to auto-fill professional information using AI.");
        descLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        descLabel.setTextFill(MUTED);
        descLabel.setWrapText(true);

        titleBox.getChildren().addAll(cvInfoLabel, descLabel);
        headerBox.getChildren().addAll(iconBox, titleBox);

        HBox cvUploadBox = new HBox(12);
        cvUploadBox.setAlignment(Pos.CENTER_LEFT);

        uploadCVButton = new Button("Upload CV");
        uploadCVButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        uploadCVButton.setTextFill(Color.WHITE);
        uploadCVButton.setCursor(Cursor.HAND);
        uploadCVButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #50C878, #2E7D32);" +
                        "-fx-background-radius: 999;" +
                        "-fx-padding: 9 20;"
        );
        uploadCVButton.setOnAction(e -> handleCVUpload());

        cvStatusLabel = new Label("No file selected");
        cvStatusLabel.setFont(Font.font("Segoe UI", 12));
        cvStatusLabel.setTextFill(MUTED);

        aiProgressIndicator = new ProgressIndicator();
        aiProgressIndicator.setPrefSize(25, 25);
        aiProgressIndicator.setVisible(false);

        cvUploadBox.getChildren().addAll(uploadCVButton, cvStatusLabel, aiProgressIndicator);

        summaryArea = new TextArea();
        summaryArea.setPrefRowCount(4);
        summaryArea.setWrapText(true);
        summaryArea.setPromptText("AI summary will appear here after upload...");
        summaryArea.setEditable(true);
        summaryArea.setVisible(false);
        summaryArea.setManaged(false);
        summaryArea.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-color: #C8E6D2;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 13px;"
        );

        panel.getChildren().addAll(headerBox, cvUploadBox, summaryArea);

        return panel;
    }

    private void handleCVUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload CV");
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
            protected Void call() {
                try {
                    String cvText = cvParser.extractTextFromCV(cvFile);
                    CVSummarizationService.CVSummary summary = cvSummarizer.summarizeCV(cvText);

                    javafx.application.Platform.runLater(() -> {
                        if (!summary.getFirstname().isEmpty()) firstNameField.setText(summary.getFirstname());
                        if (!summary.getLastname().isEmpty()) lastNameField.setText(summary.getLastname());
                        if (!summary.getEmail().isEmpty()) emailField.setText(summary.getEmail());
                        if (!summary.getPhone().isEmpty()) phoneField.setText(summary.getPhone());
                        if (!summary.getDateofbirth().isEmpty()) dobField.setText(summary.getDateofbirth());

                        StringBuilder extracted = new StringBuilder("✅ CV Processed!\n\n");
                        extracted.append("Extracted Information:\n");
                        extracted.append("• Name: ").append(summary.getFirstname()).append(" ").append(summary.getLastname()).append("\n");
                        extracted.append("• Email: ").append(summary.getEmail().isEmpty() ? "Not found" : summary.getEmail()).append("\n");
                        extracted.append("• Phone: ").append(summary.getPhone().isEmpty() ? "Not found" : summary.getPhone()).append("\n");
                        extracted.append("• DOB: ").append(summary.getDateofbirth().isEmpty() ? "Not found" : summary.getDateofbirth()).append("\n\n");
                        extracted.append("You can edit any field below.");

                        summaryArea.setText(extracted.toString());
                        cvStatusLabel.setText("✓ CV processed and form auto-filled.");

                        if (!summary.getFirstname().isEmpty() || !summary.getLastname().isEmpty()) {
                            showInfo("CV processed successfully! Form has been auto-filled.");
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
        field.setPrefHeight(56);
        return field;
    }

    private void handleSignup() {
        signUpButton.setDisable(true);

        String fn = firstNameField.getText().trim();
        String ln = lastNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String dobString = dobField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String type = typeComboBox.getValue();
        boolean enableFaceID = enableFaceIDCheckBox.isSelected();

        if (type == null || type.equals("Select Type")) {
            showError("Please select a user type");
            signUpButton.setDisable(false);
            return;
        }

        if (fn.isEmpty() || ln.isEmpty() || phone.isEmpty() ||
                dobString.isEmpty() || email.isEmpty() || password.isEmpty()) {
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

        LocalDate dob;
        try {
            dob = LocalDate.parse(dobString);
        } catch (DateTimeParseException e) {
            showError("Invalid date format. Please use YYYY-MM-DD format.");
            signUpButton.setDisable(false);
            return;
        }

        user u = new user(fn, ln, phone, dob, type, email, password);

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
            FaceIDDialog dialog = new FaceIDDialog(parentApp, true, userId);
            dialog.setOnHidden(e -> parentApp.showLoginPanel());
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Face ID registration failed: " + e.getMessage());
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

    private void styleCombo(ComboBox<String> combo) {
        combo.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-color: #E9ECEF;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-padding: 8 14;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 15px;"
        );
    }

    private Node loadLogo() {
        try {
            String[] paths = {"/logo.png", "/resources/logo.png", "/images/logo.png"};

            for (String path : paths) {
                if (getClass().getResourceAsStream(path) == null) continue;

                Image image = new Image(getClass().getResourceAsStream(path));

                if (!image.isError()) {
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(58);
                    imageView.setFitHeight(58);
                    imageView.setPreserveRatio(true);
                    return imageView;
                }
            }
        } catch (Exception e) {
            // fallback below
        }

        return null;
    }

    private void startEntranceAnimation() {
        setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(800), this);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    class RoundedButton extends Button {
        RoundedButton(String text) {
            super(text + "  →");
            setTextFill(Color.WHITE);
            setCursor(Cursor.HAND);
            setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

            String base =
                    "-fx-background-color: linear-gradient(to right, #50C878, #2E7D32);" +
                            "-fx-background-radius: 999;" +
                            "-fx-effect: dropshadow(gaussian, rgba(80,200,120,0.35), 18, 0, 0, 7);";

            String hover =
                    "-fx-background-color: linear-gradient(to right, #3A9B5E, #2E7D32);" +
                            "-fx-background-radius: 999;" +
                            "-fx-translate-y: -2;" +
                            "-fx-effect: dropshadow(gaussian, rgba(80,200,120,0.48), 24, 0, 0, 9);";

            setStyle(base);
            setOnMouseEntered(e -> setStyle(hover));
            setOnMouseExited(e -> setStyle(base));

            setOnMousePressed(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(90), this);
                scale.setToX(0.97);
                scale.setToY(0.97);
                scale.play();
            });

            setOnMouseReleased(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(120), this);
                scale.setToX(1);
                scale.setToY(1);
                scale.play();
            });
        }
    }

    class RoundedTextField extends TextField {
        RoundedTextField() {
            super();
            setFont(Font.font("Segoe UI", 15));

            String base =
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 16;" +
                            "-fx-border-radius: 16;" +
                            "-fx-border-color: #E9ECEF;" +
                            "-fx-border-width: 1.5;" +
                            "-fx-padding: 13 18;" +
                            "-fx-prompt-text-fill: #9AA4AE;";

            String focus =
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 16;" +
                            "-fx-border-radius: 16;" +
                            "-fx-border-color: #50C878;" +
                            "-fx-border-width: 2;" +
                            "-fx-padding: 13 18;" +
                            "-fx-effect: dropshadow(gaussian, rgba(80,200,120,0.18), 12, 0, 0, 4);";

            setStyle(base);
            focusedProperty().addListener((obs, oldVal, focused) -> setStyle(focused ? focus : base));
        }
    }

    class RoundedPasswordField extends PasswordField {
        RoundedPasswordField() {
            super();
            setFont(Font.font("Segoe UI", 15));

            String base =
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 16;" +
                            "-fx-border-radius: 16;" +
                            "-fx-border-color: #E9ECEF;" +
                            "-fx-border-width: 1.5;" +
                            "-fx-padding: 13 18;" +
                            "-fx-prompt-text-fill: #9AA4AE;";

            String focus =
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 16;" +
                            "-fx-border-radius: 16;" +
                            "-fx-border-color: #50C878;" +
                            "-fx-border-width: 2;" +
                            "-fx-padding: 13 18;" +
                            "-fx-effect: dropshadow(gaussian, rgba(80,200,120,0.18), 12, 0, 0, 4);";

            setStyle(base);
            focusedProperty().addListener((obs, oldVal, focused) -> setStyle(focused ? focus : base));
        }
    }

    class BrainLogo extends StackPane {
        BrainLogo() {
            setPrefSize(58, 58);
            setMaxSize(58, 58);
            setMinSize(58, 58);

            Circle bg = new Circle(29);
            bg.setFill(Color.web("#F1F8E9"));
            bg.setStroke(Color.web("#C8E6D2"));
            bg.setStrokeWidth(1.5);

            Arc leftArc = arc(18, 29, 13, 17, 90, 180, PRIMARY_DARK);
            Arc rightArc = arc(40, 29, 13, 17, 270, 180, PRIMARY_DARK);
            Arc topArc = arc(29, 22, 17, 12, 180, 180, PRIMARY);

            getChildren().addAll(bg, leftArc, rightArc, topArc);
        }
    }

    private Arc arc(double x, double y, double rx, double ry, double start, double length, Color color) {
        Arc arc = new Arc();
        arc.setCenterX(x);
        arc.setCenterY(y);
        arc.setRadiusX(rx);
        arc.setRadiusY(ry);
        arc.setStartAngle(start);
        arc.setLength(length);
        arc.setType(ArcType.OPEN);
        arc.setStroke(color);
        arc.setStrokeWidth(2.7);
        arc.setFill(null);
        return arc;
    }
}
