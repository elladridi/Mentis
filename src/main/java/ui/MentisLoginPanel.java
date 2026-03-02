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
import models.user;
import services.userservice;
import services.RememberMeService;

public class MentisLoginPanel extends StackPane {

    private TextField emailField;
    private PasswordField passwordField;
    private Button loginButton;
    private Hyperlink forgotPasswordLink;
    private Hyperlink backLink;
    private MentisLoginFrame parentApp;

    // NEW: Remember Me checkbox
    private CheckBox rememberMeCheckBox;
    private RememberMeService rememberMeService;

    public MentisLoginPanel(MentisLoginFrame parentApp) {
        this.parentApp = parentApp;
        this.rememberMeService = new RememberMeService();
        initComponents();

        // Check for saved login on initialization
        checkRememberedUser();
    }

    private void initComponents() {
        setStyle("-fx-background-color: #D8E4DE;");

        VBox mainContainer = new VBox(30);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(100, 50, 100, 50));

        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(450);
        card.setMaxHeight(650); // Increased height for remember me
        card.setPadding(new Insets(40, 40, 40, 40));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 2);"
        );

        // Logo
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

        // Title
        Label title = new Label("Welcome Back");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#588B71"));
        card.getChildren().add(title);

        // Email Field
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
        card.getChildren().add(emailField);

        // Password Field
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
        passwordField.setOnAction(e -> handleLogin());
        card.getChildren().add(passwordField);

        // ===== REMEMBER ME CHECKBOX =====
        HBox rememberBox = new HBox(10);
        rememberBox.setAlignment(Pos.CENTER_LEFT);
        rememberBox.setMaxWidth(350);

        rememberMeCheckBox = new CheckBox("Remember me");
        rememberMeCheckBox.setFont(Font.font("Arial", 14));
        rememberMeCheckBox.setTextFill(Color.web("#2C3E50"));

        // Spacer to push forgot password to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        forgotPasswordLink = new Hyperlink("Forgot Password?");
        forgotPasswordLink.setFont(Font.font("Arial", 14));
        forgotPasswordLink.setTextFill(Color.web("#588B71"));
        forgotPasswordLink.setUnderline(true);
        forgotPasswordLink.setOnAction(e -> showForgotPasswordDialog());

        rememberBox.getChildren().addAll(rememberMeCheckBox, spacer, forgotPasswordLink);
        card.getChildren().add(rememberBox);

        // Login Button
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
        loginButton.setOnAction(e -> handleLogin());
        card.getChildren().add(loginButton);

        // Face ID Button
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
        faceLoginButton.setOnAction(e -> {
            FaceIDDialog dialog = new FaceIDDialog(parentApp, false, -1);
            dialog.show();
        });
        card.getChildren().add(faceLoginButton);

        // Back Link
        backLink = new Hyperlink("← Back to Welcome");
        backLink.setFont(Font.font("Arial", 16));
        backLink.setTextFill(Color.web("#588B71"));
        backLink.setOnAction(e -> parentApp.showWelcomePanel());
        backLink.setStyle("-fx-cursor: hand;");
        card.getChildren().add(backLink);

        mainContainer.getChildren().add(card);
        getChildren().add(mainContainer);
    }

    // ===== NEW: Check for remembered user on startup =====
    private void checkRememberedUser() {
        RememberMeService.RememberMeToken token = rememberMeService.getRememberedUser();

        if (token != null && !token.isExpired()) {
            System.out.println("🔍 Found remembered user: " + token.getEmail());

            // Auto-login without password
            setLoading(true);

            new Thread(() -> {
                user loggedUser = userservice.getuserByEmail(token.getEmail());

                javafx.application.Platform.runLater(() -> {
                    setLoading(false);

                    if (loggedUser != null) {
                        System.out.println("✅ Auto-login successful for: " + token.getEmail());
                        parentApp.login(
                                loggedUser.getType(),
                                loggedUser.getId(),
                                loggedUser.getFirstName() + " " + loggedUser.getLastName()
                        );
                    } else {
                        System.out.println("❌ Auto-login failed, clearing token");
                        rememberMeService.clearRememberMe();
                    }
                });
            }).start();
        }
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter email and password.");
            return;
        }

        setLoading(true);

        new Thread(() -> {
            user loggedUser = userservice.loginuser(email, password);

            javafx.application.Platform.runLater(() -> {
                setLoading(false);

                if (loggedUser != null) {
                    // ===== SAVE REMEMBER ME TOKEN IF CHECKED =====
                    if (rememberMeCheckBox.isSelected()) {
                        rememberMeService.saveRememberMeToken(
                                loggedUser.getId(),
                                loggedUser.getEmail(),
                                loggedUser.getType()
                        );
                        System.out.println("✅ Remember Me token saved for: " + email);
                    } else {
                        // Clear any existing token if unchecked
                        rememberMeService.clearRememberMe();
                    }

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

    private void showForgotPasswordDialog() {
        ForgotPasswordDialog dialog = new ForgotPasswordDialog();
        dialog.show();
    }

    private void setLoading(boolean loading) {
        loginButton.setDisable(loading);
        emailField.setDisable(loading);
        passwordField.setDisable(loading);
        rememberMeCheckBox.setDisable(loading);

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
}