package ui;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
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
import javafx.util.Duration;
import models.user;
import services.RememberMeService;
import services.userservice;

public class MentisLoginPanel extends StackPane {

    private TextField emailField;
    private PasswordField passwordField;
    private Button loginButton;
    private Hyperlink forgotPasswordLink;
    private Hyperlink backLink;
    private MentisLoginFrame parentApp;

    private CheckBox rememberMeCheckBox;
    private RememberMeService rememberMeService;

    private static final Color PRIMARY = Color.web("#50C878");
    private static final Color PRIMARY_DARK = Color.web("#2E7D32");
    private static final Color PRIMARY_MID = Color.web("#3A9B5E");
    private static final Color INK = Color.web("#1A3C34");
    private static final Color MUTED = Color.web("#6C757D");
    private static final Color LINE = Color.web("#E9ECEF");

    public MentisLoginPanel(MentisLoginFrame parentApp) {
        this.parentApp = parentApp;
        this.rememberMeService = new RememberMeService();

        initComponents();
        startEntranceAnimation();
        checkRememberedUser();
    }

    private void initComponents() {
        setStyle("-fx-background-color: linear-gradient(to bottom right, #F5F7FA, #E8F5E9);");

        Pane background = createBackgroundDecor();

        BorderPane page = new BorderPane();
        page.setPadding(new Insets(34, 54, 34, 54));
        page.setStyle("-fx-background-color: transparent;");

        page.setTop(createTopBar());

        HBox center = new HBox(46);
        center.setAlignment(Pos.CENTER);

        VBox leftHero = createLeftHero();
        VBox card = createLoginCard();

        center.getChildren().addAll(leftHero, card);
        page.setCenter(center);

        getChildren().addAll(background, page);
    }

    private Pane createBackgroundDecor() {
        Pane pane = new Pane();
        pane.setMouseTransparent(true);

        Circle glow1 = new Circle(210);
        glow1.setFill(Color.web("#50C878", 0.18));
        glow1.setEffect(new GaussianBlur(70));
        glow1.setTranslateX(-430);
        glow1.setTranslateY(-250);

        Circle glow2 = new Circle(250);
        glow2.setFill(Color.web("#9B5DE5", 0.09));
        glow2.setEffect(new GaussianBlur(80));
        glow2.setTranslateX(470);
        glow2.setTranslateY(270);

        Circle glow3 = new Circle(130);
        glow3.setFill(Color.web("#38F9D7", 0.13));
        glow3.setEffect(new GaussianBlur(55));
        glow3.setTranslateX(260);
        glow3.setTranslateY(-245);

        StackPane.setAlignment(glow1, Pos.TOP_LEFT);
        StackPane.setAlignment(glow2, Pos.BOTTOM_RIGHT);
        StackPane.setAlignment(glow3, Pos.TOP_RIGHT);

        pane.getChildren().addAll(glow1, glow2, glow3);
        return pane;
    }

    private HBox createTopBar() {
        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);

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

        backLink = new Hyperlink("← Back to Welcome");
        backLink.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        backLink.setTextFill(PRIMARY_DARK);
        backLink.setStyle("-fx-border-color: transparent; -fx-underline: false;");
        backLink.setOnAction(e -> parentApp.showWelcomePanel());

        top.getChildren().addAll(brand, spacer, backLink);
        return top;
    }

    private VBox createLeftHero() {
        VBox hero = new VBox(22);
        hero.setAlignment(Pos.CENTER_LEFT);
        hero.setMaxWidth(520);

        Label badge = new Label("🔐 Secure wellness access");
        badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        badge.setTextFill(PRIMARY_DARK);
        badge.setPadding(new Insets(8, 16, 8, 16));
        badge.setStyle(
                "-fx-background-color: #F1F8E9;" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-color: #C8E6D2;" +
                        "-fx-border-radius: 999;"
        );

        Label title = new Label("Continue your\nwellness journey.");
        title.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 54));
        title.setTextFill(INK);
        title.setLineSpacing(-3);

        Label subtitle = new Label("Access your assessments, therapy sessions, mood tracking, goals, content, events, and AI-powered recommendations.");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 17));
        subtitle.setTextFill(MUTED);
        subtitle.setWrapText(true);
        subtitle.setLineSpacing(4);
        subtitle.setMaxWidth(470);

        HBox chips = new HBox(10);
        chips.getChildren().addAll(
                chip("🧠 AI Insights"),
                chip("📊 Progress"),
                chip("🌿 Wellbeing")
        );

        VBox miniCard = new VBox(12);
        miniCard.setPadding(new Insets(20));
        miniCard.setMaxWidth(420);
        miniCard.setStyle(
                "-fx-background-color: rgba(255,255,255,0.72);" +
                        "-fx-background-radius: 24;" +
                        "-fx-border-color: rgba(255,255,255,0.8);" +
                        "-fx-border-radius: 24;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 22, 0, 0, 8);"
        );

        Label miniTitle = new Label("Today inside Mentis");
        miniTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        miniTitle.setTextFill(INK);

        miniCard.getChildren().addAll(
                miniTitle,
                planRow("📝", "Take an assessment", "5 min"),
                planRow("📅", "View sessions", "Available"),
                planRow("🎵", "Wellness content", "Personalized")
        );

        hero.getChildren().addAll(badge, title, subtitle, chips, miniCard);
        return hero;
    }

    private VBox createLoginCard() {
        VBox card = new VBox(18);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(460);
        card.setMinWidth(460);
        card.setPadding(new Insets(38));
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.94);" +
                        "-fx-background-radius: 34;" +
                        "-fx-border-radius: 34;" +
                        "-fx-border-color: rgba(255,255,255,0.8);" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.11), 34, 0, 0, 14);"
        );

        Node logo = loadLogo();
        if (logo == null) logo = new BrainLogoLarge();
        card.getChildren().add(logo);

        Label title = new Label("Welcome Back");
        title.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 32));
        title.setTextFill(INK);

        Label subtitle = new Label("Log in to continue to your Mentis space");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        subtitle.setTextFill(MUTED);

        emailField = new TextField();
        emailField.setPromptText("Email address");
        styleInput(emailField);

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        styleInput(passwordField);
        passwordField.setOnAction(e -> handleLogin());

        HBox rememberBox = new HBox(10);
        rememberBox.setAlignment(Pos.CENTER_LEFT);
        rememberBox.setMaxWidth(360);

        rememberMeCheckBox = new CheckBox("Remember me");
        rememberMeCheckBox.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        rememberMeCheckBox.setTextFill(INK);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        forgotPasswordLink = new Hyperlink("Forgot Password?");
        forgotPasswordLink.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        forgotPasswordLink.setTextFill(PRIMARY_DARK);
        forgotPasswordLink.setStyle("-fx-border-color: transparent;");
        forgotPasswordLink.setOnAction(e -> showForgotPasswordDialog());

        rememberBox.getChildren().addAll(rememberMeCheckBox, spacer, forgotPasswordLink);

        loginButton = primaryButton("Login");
        loginButton.setOnAction(e -> handleLogin());

        Button faceLoginButton = secondaryButton("👤 Login with Face ID");
        faceLoginButton.setOnAction(e -> {
            FaceIDDialog dialog = new FaceIDDialog(parentApp, false, -1);
            dialog.show();
        });

        Label signupHint = new Label("New here? Create an account from the welcome screen.");
        signupHint.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        signupHint.setTextFill(MUTED);
        signupHint.setWrapText(true);

        card.getChildren().addAll(
                title,
                subtitle,
                emailField,
                passwordField,
                rememberBox,
                loginButton,
                faceLoginButton,
                signupHint
        );

        return card;
    }

    private void styleInput(TextField field) {
        field.setPrefHeight(54);
        field.setMaxWidth(360);
        field.setFont(Font.font("Segoe UI", 15));
        String base =
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-color: #E9ECEF;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-padding: 13 18;" +
                        "-fx-prompt-text-fill: #9AA4AE;";

        String focus =
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-color: #50C878;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-width: 2;" +
                        "-fx-padding: 13 18;" +
                        "-fx-effect: dropshadow(gaussian, rgba(80,200,120,0.18), 12, 0, 0, 4);";

        field.setStyle(base);
        field.focusedProperty().addListener((obs, oldVal, focused) -> field.setStyle(focused ? focus : base));
    }

    private Button primaryButton(String text) {
        Button button = new Button(text + "  →");
        button.setPrefHeight(56);
        button.setMaxWidth(360);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        button.setTextFill(Color.WHITE);
        button.setCursor(Cursor.HAND);

        String base =
                "-fx-background-color: linear-gradient(to right, #50C878, #2E7D32);" +
                        "-fx-background-radius: 999;" +
                        "-fx-effect: dropshadow(gaussian, rgba(80,200,120,0.35), 18, 0, 0, 7);";

        String hover =
                "-fx-background-color: linear-gradient(to right, #3A9B5E, #2E7D32);" +
                        "-fx-background-radius: 999;" +
                        "-fx-translate-y: -2;" +
                        "-fx-effect: dropshadow(gaussian, rgba(80,200,120,0.48), 24, 0, 0, 9);";

        button.setStyle(base);
        button.setOnMouseEntered(e -> button.setStyle(hover));
        button.setOnMouseExited(e -> button.setStyle(base));
        addPressAnimation(button);

        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(text);
        button.setPrefHeight(52);
        button.setMaxWidth(360);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        button.setTextFill(PRIMARY_DARK);
        button.setCursor(Cursor.HAND);

        String base =
                "-fx-background-color: #F1F8E9;" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-color: #C8E6D2;" +
                        "-fx-border-radius: 999;" +
                        "-fx-border-width: 1.5;";

        String hover =
                "-fx-background-color: white;" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-color: #50C878;" +
                        "-fx-border-radius: 999;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-translate-y: -2;";

        button.setStyle(base);
        button.setOnMouseEntered(e -> button.setStyle(hover));
        button.setOnMouseExited(e -> button.setStyle(base));
        addPressAnimation(button);

        return button;
    }

    private Label chip(String text) {
        Label chip = new Label(text);
        chip.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        chip.setTextFill(PRIMARY_DARK);
        chip.setPadding(new Insets(8, 13, 8, 13));
        chip.setStyle(
                "-fx-background-color: #F1F8E9;" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-color: #DCEFE4;" +
                        "-fx-border-radius: 999;"
        );
        return chip;
    }

    private HBox planRow(String icon, String text, String meta) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label i = new Label(icon);
        i.setFont(Font.font("Segoe UI Emoji", 17));

        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        label.setTextFill(INK);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label m = new Label(meta);
        m.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        m.setTextFill(PRIMARY_DARK);

        row.getChildren().addAll(i, label, spacer, m);
        return row;
    }

    private Node loadLogo() {
        try {
            String[] paths = {"/logo.png", "/resources/logo.png", "/images/logo.png"};

            for (String path : paths) {
                if (getClass().getResourceAsStream(path) == null) continue;

                Image image = new Image(getClass().getResourceAsStream(path));

                if (!image.isError()) {
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(64);
                    imageView.setFitHeight(64);
                    imageView.setPreserveRatio(true);
                    return imageView;
                }
            }
        } catch (Exception e) {
            // fallback below
        }

        return null;
    }

    private void checkRememberedUser() {
        RememberMeService.RememberMeToken token = rememberMeService.getRememberedUser();

        if (token != null && !token.isExpired()) {
            setLoading(true);

            new Thread(() -> {
                user loggedUser = userservice.getuserByEmail(token.getEmail());

                javafx.application.Platform.runLater(() -> {
                    setLoading(false);

                    if (loggedUser != null) {
                        parentApp.login(
                                loggedUser.getType(),
                                loggedUser.getId(),
                                loggedUser.getFirstname() + " " + loggedUser.getLastname()
                        );
                    } else {
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
                    if (rememberMeCheckBox.isSelected()) {
                        rememberMeService.saveRememberMeToken(
                                loggedUser.getId(),
                                loggedUser.getEmail(),
                                loggedUser.getType()
                        );
                    } else {
                        rememberMeService.clearRememberMe();
                    }

                    parentApp.login(
                            loggedUser.getType(),
                            loggedUser.getId(),
                            loggedUser.getFirstname() + " " + loggedUser.getLastname()
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
            setCursor(Cursor.WAIT);
        } else {
            loginButton.setText("Login  →");
            setCursor(Cursor.DEFAULT);
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void startEntranceAnimation() {
        setOpacity(0);

        FadeTransition fade = new FadeTransition(Duration.millis(850), this);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        TranslateTransition slide = new TranslateTransition(Duration.millis(850), this);
        slide.setFromY(18);
        slide.setToY(0);
        slide.play();
    }

    private void addPressAnimation(Button button) {
        button.setOnMousePressed(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(90), button);
            scale.setToX(0.97);
            scale.setToY(0.97);
            scale.play();
        });

        button.setOnMouseReleased(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(120), button);
            scale.setToX(1);
            scale.setToY(1);
            scale.play();
        });
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

    class BrainLogoLarge extends StackPane {
        BrainLogoLarge() {
            setPrefSize(82, 82);
            setMaxSize(82, 82);

            Circle bg = new Circle(41);
            bg.setFill(Color.web("#F1F8E9"));
            bg.setStroke(Color.web("#C8E6D2"));
            bg.setStrokeWidth(1.8);

            Arc leftArc = arc(26, 41, 18, 24, 90, 180, PRIMARY_DARK);
            Arc rightArc = arc(56, 41, 18, 24, 270, 180, PRIMARY_DARK);
            Arc topArc = arc(41, 31, 25, 17, 180, 180, PRIMARY);

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
