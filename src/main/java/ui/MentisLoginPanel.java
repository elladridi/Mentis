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
import ui.FaceIDDialog;  // Add this with other imports
import models.user;
import services.userservice;

/**
 * JavaFX Login Panel for Mentis
 */
public class MentisLoginPanel extends StackPane {

    private TextField emailField;
    private PasswordField passwordField;
    private Button loginButton;
    private Hyperlink forgotPasswordLink;
    private Hyperlink backLink;
    private MentisLoginFrame parentApp;

    public MentisLoginPanel(MentisLoginFrame parentApp) {
        this.parentApp = parentApp;
        initComponents();
        applyStyles();
    }

    private void initComponents() {
        // Set background color
        setStyle("-fx-background-color: #D8E4DE;"); // Light sage green

        // Main container with centering
        VBox mainContainer = new VBox(30);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(100, 50, 100, 50));

        // ===== Login Card =====
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(450);
        card.setMaxHeight(600);
        card.setPadding(new Insets(40, 40, 40, 40));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 2);"
        );

        // ===== Logo =====
        try {
            Image logo = new Image(getClass().getResourceAsStream("/resources/logo.png"));
            ImageView logoView = new ImageView(logo);
            logoView.setFitWidth(120);
            logoView.setFitHeight(120);
            logoView.setPreserveRatio(true);
            card.getChildren().add(logoView);
        } catch (Exception e) {
            System.out.println("Logo not found: " + e.getMessage());
        }

        // ===== Title =====
        Label title = new Label("Welcome Back");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#588B71")); // Sage green
        card.getChildren().add(title);

        // ===== Email Field =====
        emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setPrefHeight(50);
        emailField.setMaxWidth(350);
        emailField.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #E0E0E0;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;" +
                        "-fx-padding: 15 20;" +
                        "-fx-font-size: 16px;"
        );
        emailField.setOnMouseEntered(e ->
                emailField.setStyle(emailField.getStyle() + "-fx-border-color: #588B71;")
        );
        emailField.setOnMouseExited(e ->
                emailField.setStyle(emailField.getStyle().replace("-fx-border-color: #588B71;", "-fx-border-color: #E0E0E0;"))
        );
        card.getChildren().add(emailField);

        // ===== Password Field =====
        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(50);
        passwordField.setMaxWidth(350);
        passwordField.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #E0E0E0;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;" +
                        "-fx-padding: 15 20;" +
                        "-fx-font-size: 16px;"
        );
        passwordField.setOnMouseEntered(e ->
                passwordField.setStyle(passwordField.getStyle() + "-fx-border-color: #588B71;")
        );
        passwordField.setOnMouseExited(e ->
                passwordField.setStyle(passwordField.getStyle().replace("-fx-border-color: #588B71;", "-fx-border-color: #E0E0E0;"))
        );

        // ENTER key triggers login
        passwordField.setOnAction(e -> handleLogin());
        card.getChildren().add(passwordField);

        // ===== Forgot Password Link =====
        forgotPasswordLink = new Hyperlink("Forgot Password?");
        forgotPasswordLink.setFont(Font.font("Arial", 14));
        forgotPasswordLink.setTextFill(Color.web("#588B71"));
        forgotPasswordLink.setUnderline(true);
        forgotPasswordLink.setOnAction(e -> showForgotPasswordDialog());
        forgotPasswordLink.setStyle("-fx-cursor: hand;");
        card.getChildren().add(forgotPasswordLink);

        // ===== Login Button =====
        loginButton = new Button("Login");
        loginButton.setPrefHeight(55);
        loginButton.setMaxWidth(350);
        loginButton.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        loginButton.setTextFill(Color.WHITE);
        loginButton.setStyle(
                "-fx-background-color: #588B71;" +
                        "-fx-background-radius: 15;" +
                        "-fx-cursor: hand;"
        );

        // Button hover effect
        loginButton.setOnMouseEntered(e ->
                loginButton.setStyle(
                        "-fx-background-color: #629F89;" +
                                "-fx-background-radius: 15;" +
                                "-fx-cursor: hand;"
                )
        );
        loginButton.setOnMouseExited(e ->
                loginButton.setStyle(
                        "-fx-background-color: #588B71;" +
                                "-fx-background-radius: 15;" +
                                "-fx-cursor: hand;"
                )
        );
        loginButton.setOnAction(e -> handleLogin());
        card.getChildren().add(loginButton);

        // ===== Face ID Login Button =====
        Button faceLoginButton = new Button("👤 Login with Face ID");
        faceLoginButton.setPrefHeight(50);
        faceLoginButton.setMaxWidth(350);
        faceLoginButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        faceLoginButton.setTextFill(Color.WHITE);
        faceLoginButton.setStyle(
                "-fx-background-color: #6B9C89;" +
                        "-fx-background-radius: 15;" +
                        "-fx-cursor: hand;"
        );
        faceLoginButton.setOnMouseEntered(e ->
                faceLoginButton.setStyle(
                        "-fx-background-color: #7AAF99;" +
                                "-fx-background-radius: 15;" +
                                "-fx-cursor: hand;"
                )
        );
        faceLoginButton.setOnMouseExited(e ->
                faceLoginButton.setStyle(
                        "-fx-background-color: #6B9C89;" +
                                "-fx-background-radius: 15;" +
                                "-fx-cursor: hand;"
                )
        );

        // FIXED: Add action handler for Face ID button
        faceLoginButton.setOnAction(e -> {
            try {
                FaceIDDialog dialog = new FaceIDDialog(parentApp, false, -1);
                dialog.show();
            } catch (Exception ex) {
                showError("Face ID feature not available: " + ex.getMessage());
            }
        });

        card.getChildren().add(faceLoginButton);

        // ===== Back Link =====
        backLink = new Hyperlink("← Back to Welcome");
        backLink.setFont(Font.font("Arial", 16));
        backLink.setTextFill(Color.web("#588B71"));
        backLink.setOnAction(e -> parentApp.showWelcomePanel());
        backLink.setStyle("-fx-cursor: hand;");
        card.getChildren().add(backLink);

        mainContainer.getChildren().add(card);
        getChildren().add(mainContainer);
    }

    private void applyStyles() {
        // Apply custom CSS if needed
        try {
            String css = getClass().getResource("/styles/mentis-login.css").toExternalForm();
            getStylesheets().add(css);
        } catch (Exception e) {
            System.out.println("CSS file not found, using inline styles");
        }
    }

    // ================= LOGIN LOGIC =================
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter email and password.");
            return;
        }

        setLoading(true);

        // Run in background thread
        new Thread(() -> {
            user loggedUser = userservice.loginuser(email, password);

            // Update UI on JavaFX Application Thread
            javafx.application.Platform.runLater(() -> {
                setLoading(false);

                if (loggedUser != null) {
                    parentApp.login(
                            loggedUser.getType(),
                            loggedUser.getId(),
                            loggedUser.getFirstName() + " " + loggedUser.getLastName()
                    );
                } else {
                    showError("Invalid email or password.");
                }
            });
        }).start();
    }

    // ================= FORGOT PASSWORD =================
    private void showForgotPasswordDialog() {
        ForgotPasswordDialog dialog = new ForgotPasswordDialog();
        dialog.show();
    }

    private void setLoading(boolean loading) {
        loginButton.setDisable(loading);
        emailField.setDisable(loading);
        passwordField.setDisable(loading);

        if (loading) {
            loginButton.setText("Logging in...");
            setCursor(javafx.scene.Cursor.WAIT);
        } else {
            loginButton.setText("Login");
            setCursor(javafx.scene.Cursor.DEFAULT);
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ================= FACE ID REGISTRATION =================
    // This method can be called after successful login to enable Face ID
    public void enableFaceRegistration(int userId) {
        try {
            FaceIDDialog dialog = new FaceIDDialog(parentApp, true, userId);
            dialog.show();
        } catch (Exception e) {
            showError("Face ID registration not available: " + e.getMessage());
        }
    }
}