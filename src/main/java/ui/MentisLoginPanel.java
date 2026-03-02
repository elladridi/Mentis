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
        // ⭐ Utilise la même couleur que le reste de l'application
        setStyle("-fx-background-color: #" + toHex(MentisLoginFrame.BACKGROUND_LIGHT) + ";");

        // ⭐ Main container avec centrage parfait
        BorderPane mainContainer = new BorderPane();
        mainContainer.setStyle("-fx-background-color: #" + toHex(MentisLoginFrame.BACKGROUND_LIGHT) + ";");

        // ===== Login Card centrée =====
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
        title.setTextFill(Color.web(toHex(MentisLoginFrame.ACCENT_DARK_GREEN)));
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
                emailField.setStyle(emailField.getStyle() + "-fx-border-color: #" + toHex(MentisLoginFrame.ACCENT_DARK_GREEN) + ";")
        );
        emailField.setOnMouseExited(e ->
                emailField.setStyle(emailField.getStyle().replace("-fx-border-color: #" + toHex(MentisLoginFrame.ACCENT_DARK_GREEN) + ";", "-fx-border-color: #E0E0E0;"))
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
                passwordField.setStyle(passwordField.getStyle() + "-fx-border-color: #" + toHex(MentisLoginFrame.ACCENT_DARK_GREEN) + ";")
        );
        passwordField.setOnMouseExited(e ->
                passwordField.setStyle(passwordField.getStyle().replace("-fx-border-color: #" + toHex(MentisLoginFrame.ACCENT_DARK_GREEN) + ";", "-fx-border-color: #E0E0E0;"))
        );

        // ENTER key triggers login
        passwordField.setOnAction(e -> handleLogin());
        card.getChildren().add(passwordField);

        // ===== Forgot Password Link =====
        forgotPasswordLink = new Hyperlink("Forgot Password?");
        forgotPasswordLink.setFont(Font.font("Arial", 14));
        forgotPasswordLink.setTextFill(Color.web(toHex(MentisLoginFrame.ACCENT_DARK_GREEN)));
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
                "-fx-background-color: #" + toHex(MentisLoginFrame.ACCENT_DARK_GREEN) + ";" +
                        "-fx-background-radius: 15;" +
                        "-fx-cursor: hand;"
        );

        // Button hover effect
        loginButton.setOnMouseEntered(e ->
                loginButton.setStyle(
                        "-fx-background-color: #" + toHex(MentisLoginFrame.ACCENT_DARK_GREEN.darker()) + ";" +
                                "-fx-background-radius: 15;" +
                                "-fx-cursor: hand;"
                )
        );
        loginButton.setOnMouseExited(e ->
                loginButton.setStyle(
                        "-fx-background-color: #" + toHex(MentisLoginFrame.ACCENT_DARK_GREEN) + ";" +
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
        backLink.setTextFill(Color.web(toHex(MentisLoginFrame.ACCENT_DARK_GREEN)));
        backLink.setOnAction(e -> parentApp.showWelcomePanel());
        backLink.setStyle("-fx-cursor: hand;");
        card.getChildren().add(backLink);

        // ⭐ Centre la carte au milieu de l'écran
        mainContainer.setCenter(card);

        // ⭐ Ajoute un padding pour éviter que la carte touche les bords
        BorderPane.setMargin(card, new Insets(50));

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

    private String toHex(Color color) {
        return String.format("%02x%02x%02x",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }
}