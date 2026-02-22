package ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import services.EmailService;
import services.userservice;

public class ForgotPasswordDialog extends Stage {

    private TextField emailField;
    private TextField codeField;
    private PasswordField newPassField;
    private PasswordField confirmPassField;
    private Button sendBtn;
    private Button verifyBtn;
    private Button resetBtn;

    private String sentCode;
    private String verifiedEmail;
    private long expiryTime;

    // Color constants
    private static final Color BACKGROUND_GREEN = Color.rgb(240, 245, 242);
    private static final Color ACCENT_GREEN = Color.rgb(88, 139, 113);
    private static final Color ACCENT_GREEN_LIGHT = Color.rgb(200, 225, 210);
    private static final Color ACCENT_GREEN_DARK = Color.rgb(150, 190, 170);
    private static final Color ERROR_RED = Color.rgb(220, 80, 80);
    private static final Color TEXT_DARK = Color.rgb(60, 70, 80);
    private static final Color TEXT_GRAY = Color.GRAY;

    public ForgotPasswordDialog() {
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        setTitle("Reset Password");
        setMinWidth(500);
        setMinHeight(550);
        setResizable(false);

        // Main layout
        VBox root = new VBox(15);
        root.setStyle("-fx-background-color: #" + toHex(BACKGROUND_GREEN) + ";");
        root.setPadding(new Insets(30, 50, 30, 50));
        root.setAlignment(Pos.TOP_CENTER);

        initUI(root);

        Scene scene = new Scene(root);
        setScene(scene);
    }

    private void initUI(VBox root) {
        // Title
        Label title = new Label("Reset Password");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        title.setTextFill(Color.web(toHex(ACCENT_GREEN)));
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);

        // Step 1: Email
        VBox step1Box = createStepBox("Step 1: Enter your email");

        emailField = createTextField();
        emailField.setPromptText("Enter your email address");

        sendBtn = createButton("Send Code");
        sendBtn.setOnAction(e -> sendCode());

        HBox emailInputBox = new HBox(10);
        emailInputBox.setAlignment(Pos.CENTER_LEFT);
        emailInputBox.getChildren().addAll(emailField, sendBtn);
        HBox.setHgrow(emailField, Priority.ALWAYS);

        step1Box.getChildren().addAll(emailInputBox);

        // Step 2: Verification Code
        VBox step2Box = createStepBox("Step 2: Enter verification code");

        codeField = createTextField();
        codeField.setPromptText("Enter 6-digit code");
        codeField.setDisable(true);

        verifyBtn = createButton("Verify");
        verifyBtn.setDisable(true);
        verifyBtn.setOnAction(e -> verifyCode());

        HBox codeInputBox = new HBox(10);
        codeInputBox.setAlignment(Pos.CENTER_LEFT);
        codeInputBox.getChildren().addAll(codeField, verifyBtn);
        HBox.setHgrow(codeField, Priority.ALWAYS);

        step2Box.getChildren().add(codeInputBox);

        // Step 3: New Password
        VBox step3Box = createStepBox("Step 3: New password");

        newPassField = createPasswordField();
        newPassField.setPromptText("Enter new password (min 6 characters)");
        newPassField.setDisable(true);

        confirmPassField = createPasswordField();
        confirmPassField.setPromptText("Confirm new password");
        confirmPassField.setDisable(true);

        step3Box.getChildren().addAll(newPassField, confirmPassField);

        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 10, 0));

        resetBtn = createLargeButton("Reset Password");
        resetBtn.setDisable(true);
        resetBtn.setOnAction(e -> resetPassword());

        Button cancelBtn = createLargeButton("Cancel");
        cancelBtn.setStyle(
                "-fx-background-color: #" + toHex(ERROR_RED) + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 12 25;" +
                        "-fx-cursor: hand;"
        );
        cancelBtn.setOnAction(e -> close());

        buttonBox.getChildren().addAll(resetBtn, cancelBtn);

        // Info label
        Label infoLabel = new Label("Code expires in 10 minutes");
        infoLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        infoLabel.setTextFill(Color.web(toHex(TEXT_GRAY)));
        infoLabel.setAlignment(Pos.CENTER);
        infoLabel.setMaxWidth(Double.MAX_VALUE);
        infoLabel.setWrapText(true);
        infoLabel.setTextAlignment(TextAlignment.CENTER);

        // Add all to root
        root.getChildren().addAll(
                title,
                step1Box,
                step2Box,
                step3Box,
                buttonBox,
                infoLabel
        );

        VBox.setVgrow(step1Box, Priority.NEVER);
        VBox.setVgrow(step2Box, Priority.NEVER);
        VBox.setVgrow(step3Box, Priority.NEVER);
    }

    private VBox createStepBox(String titleText) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(10, 0, 10, 0));
        box.setAlignment(Pos.TOP_LEFT);

        Label label = new Label(titleText);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        label.setTextFill(Color.web(toHex(ACCENT_GREEN)));

        box.getChildren().add(label);
        return box;
    }

    private TextField createTextField() {
        TextField field = new TextField();
        field.setFont(Font.font("Arial", 14));
        field.setStyle(
                "-fx-background-color: #" + toHex(ACCENT_GREEN_LIGHT) + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-border-color: #" + toHex(ACCENT_GREEN_DARK) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-border-width: 2;" +
                        "-fx-padding: 10 15;"
        );
        field.setPrefHeight(45);
        return field;
    }

    private PasswordField createPasswordField() {
        PasswordField field = new PasswordField();
        field.setFont(Font.font("Arial", 14));
        field.setStyle(
                "-fx-background-color: #" + toHex(ACCENT_GREEN_LIGHT) + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-border-color: #" + toHex(ACCENT_GREEN_DARK) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-border-width: 2;" +
                        "-fx-padding: 10 15;"
        );
        field.setPrefHeight(45);
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    private Button createButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        button.setTextFill(Color.BLACK);
        button.setStyle(
                "-fx-background-color: #" + toHex(ACCENT_GREEN) + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;"
        );
        button.setPrefHeight(45);

        // Hover effect
        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(ACCENT_GREEN.darker()) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 10 20;" +
                                "-fx-cursor: hand;"
                )
        );
        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(ACCENT_GREEN) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 10 20;" +
                                "-fx-cursor: hand;"
                )
        );

        return button;
    }

    private Button createLargeButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        button.setTextFill(Color.BLACK);
        button.setStyle(
                "-fx-background-color: #" + toHex(ACCENT_GREEN) + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 12 25;" +
                        "-fx-cursor: hand;"
        );
        button.setPrefWidth(180);
        button.setPrefHeight(50);

        // Hover effect
        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(ACCENT_GREEN.darker()) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 12 25;" +
                                "-fx-cursor: hand;"
                )
        );
        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(ACCENT_GREEN) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 12 25;" +
                                "-fx-cursor: hand;"
                )
        );

        return button;
    }

    private void sendCode() {
        String email = emailField.getText().trim();

        if (!userservice.isValidEmail(email)) {
            showAlert("Error", "Invalid email!", Alert.AlertType.ERROR);
            return;
        }

        if (!userservice.emailExists(email)) {
            showAlert("Error", "Email not found!", Alert.AlertType.ERROR);
            return;
        }

        sentCode = EmailService.generateVerificationCode();
        expiryTime = System.currentTimeMillis() + 600000; // 10 min

        sendBtn.setDisable(true);
        sendBtn.setText("Sending...");

        // Run email sending in background thread
        new Thread(() -> {
            boolean ok = EmailService.sendVerificationCode(email, sentCode);

            Platform.runLater(() -> {
                if (ok) {
                    showAlert("Success", "Code sent to " + email, Alert.AlertType.INFORMATION);
                    emailField.setDisable(true);
                    codeField.setDisable(false);
                    verifyBtn.setDisable(false);
                    sendBtn.setText("Sent");
                } else {
                    showAlert("Error", "Failed to send email. Check configuration.", Alert.AlertType.ERROR);
                    sendBtn.setDisable(false);
                    sendBtn.setText("Retry");
                }
            });
        }).start();
    }

    private void verifyCode() {
        String code = codeField.getText().trim();

        if (code.isEmpty()) {
            showAlert("Error", "Enter verification code!", Alert.AlertType.ERROR);
            return;
        }

        if (System.currentTimeMillis() > expiryTime) {
            showAlert("Error", "Code expired! Please request a new one.", Alert.AlertType.ERROR);
            sendBtn.setDisable(false);
            sendBtn.setText("Send Code");
            return;
        }

        if (!code.equals(sentCode)) {
            showAlert("Error", "Wrong code!", Alert.AlertType.ERROR);
            return;
        }

        showAlert("Success", "Code verified!", Alert.AlertType.INFORMATION);
        verifiedEmail = emailField.getText();

        codeField.setDisable(true);
        verifyBtn.setDisable(true);
        newPassField.setDisable(false);
        confirmPassField.setDisable(false);
        resetBtn.setDisable(false);
    }

    private void resetPassword() {
        String p1 = newPassField.getText();
        String p2 = confirmPassField.getText();

        if (p1.length() < 6) {
            showAlert("Error", "Password too short (minimum 6 characters)!", Alert.AlertType.ERROR);
            return;
        }

        if (!p1.equals(p2)) {
            showAlert("Error", "Passwords don't match!", Alert.AlertType.ERROR);
            return;
        }

        boolean ok = userservice.updateUserPassword(verifiedEmail, p1);

        if (ok) {
            showAlert("Success", "Password reset! You can now login.", Alert.AlertType.INFORMATION);
            close();
        } else {
            showAlert("Error", "Failed to update password!", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
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