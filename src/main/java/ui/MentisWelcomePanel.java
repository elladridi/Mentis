package ui;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.scene.Cursor;

public class MentisWelcomePanel extends VBox {

    private static final Color BG_COLOR = Color.rgb(216, 228, 222);
    private static final Color PRIMARY = Color.rgb(88, 139, 113);
    private static final Color TEXT_DARK = Color.rgb(35, 35, 35);
    private static final Color TEXT_LIGHT = Color.rgb(90, 90, 90);
    private static final Color BUTTON_HOVER = Color.rgb(98, 159, 133);

    private final MentisLoginFrame parentApp;

    public MentisWelcomePanel(MentisLoginFrame parentApp) {
        this.parentApp = parentApp;

        setStyle("-fx-background-color: #" + toHex(BG_COLOR) + ";");
        setAlignment(Pos.CENTER);
        setPadding(new Insets(0));

        initComponents();
        startFadeIn();
    }

    /* ================= FADE IN ================= */
    private void startFadeIn() {
        FadeTransition fade = new FadeTransition(Duration.millis(800), this);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    /* ================= UI ================= */
    private void initComponents() {
        // Main content container with GridPane for centering
        GridPane mainContent = new GridPane();
        mainContent.setStyle("-fx-background-color: #" + toHex(BG_COLOR) + ";");
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setVgap(20);
        mainContent.setHgap(0);

        // Column constraints for centering
        ColumnConstraints col = new ColumnConstraints();
        col.setHgrow(Priority.ALWAYS);
        col.setHalignment(javafx.geometry.HPos.CENTER);
        mainContent.getColumnConstraints().add(col);

        // Row constraints for vertical distribution
        RowConstraints row1 = new RowConstraints();
        row1.setVgrow(Priority.NEVER);
        row1.setValignment(javafx.geometry.VPos.TOP);

        RowConstraints row2 = new RowConstraints();
        row2.setVgrow(Priority.NEVER);

        RowConstraints row3 = new RowConstraints();
        row3.setVgrow(Priority.NEVER);

        RowConstraints row4 = new RowConstraints();
        row4.setVgrow(Priority.NEVER);

        RowConstraints row5 = new RowConstraints();
        row5.setVgrow(Priority.ALWAYS);
        row5.setValignment(javafx.geometry.VPos.BOTTOM);

        mainContent.getRowConstraints().addAll(row1, row2, row3, row4, row5);

        int row = 0;

        // Header with logo and brand name
        HBox headerPanel = createHeaderPanel();
        mainContent.add(headerPanel, 0, row++);
        GridPane.setMargin(headerPanel, new Insets(20, 0, 0, 0));

        // Spacer
        Region spacer1 = new Region();
        spacer1.setPrefHeight(150);
        mainContent.add(spacer1, 0, row++);

        // Title
        Label title = new Label("Welcome to MENTIS");
        title.setFont(Font.font("Serif", FontWeight.BOLD, 72));
        title.setTextFill(Color.web(toHex(TEXT_DARK)));
        title.setTextAlignment(TextAlignment.CENTER);
        mainContent.add(title, 0, row++);

        // Subtitle
        // Subtitle - FIXED CENTERING
        Label subtitle = new Label("Your space for mental well-being and personal development");
        subtitle.setFont(Font.font("Georgia", FontPosture.ITALIC, 20));
        subtitle.setTextFill(Color.web(toHex(PRIMARY)));
        subtitle.setTextAlignment(TextAlignment.CENTER);
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(800);
        subtitle.setAlignment(Pos.CENTER); // ← ADD THIS LINE
        subtitle.setMaxWidth(Double.MAX_VALUE); // ← ADD THIS LINE
        mainContent.add(subtitle, 0, row++);
        GridPane.setMargin(subtitle, new Insets(20, 0, 40, 0));
        GridPane.setHalignment(subtitle, javafx.geometry.HPos.CENTER); // ← ADD THIS LINE

        // Buttons panel
        HBox buttonPanel = createButtonPanel();
        mainContent.add(buttonPanel, 0, row++);
        GridPane.setMargin(buttonPanel, new Insets(20, 0, 60, 0));

        // Footer
        Label footer = new Label("© 2026 Mentis · Mental health, handled with care");
        footer.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        footer.setTextFill(Color.web(toHex(TEXT_LIGHT)));
        footer.setTextAlignment(TextAlignment.CENTER);
        mainContent.add(footer, 0, row++);
        GridPane.setMargin(footer, new Insets(0, 0, 30, 0));

        getChildren().add(mainContent);
        VBox.setVgrow(mainContent, Priority.ALWAYS);
    }

    private HBox createHeaderPanel() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #" + toHex(BG_COLOR) + ";");
        header.setPadding(new Insets(10, 0, 0, 30));

        // Logo
        Node logo = loadLogo();
        if (logo != null) {
            header.getChildren().add(logo);
        } else {
            // Fallback brain logo
            BrainLogo brainLogo = new BrainLogo();
            brainLogo.setPrefSize(80, 80);
            header.getChildren().add(brainLogo);
        }

        // Brand name
        Label brand = new Label("Mentis");
        brand.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        brand.setTextFill(Color.web(toHex(PRIMARY)));

        header.getChildren().add(brand);

        return header;
    }

    private HBox createButtonPanel() {
        HBox buttonPanel = new HBox(30);
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.setStyle("-fx-background-color: #" + toHex(BG_COLOR) + ";");

        // Login button
        RoundedButton loginBtn = new RoundedButton("Log in");
        loginBtn.setPrefWidth(180);
        loginBtn.setPrefHeight(60);
        loginBtn.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        loginBtn.setTooltip(new javafx.scene.control.Tooltip("Go to login screen"));
        loginBtn.setOnAction(e -> parentApp.showLoginPanel());

        // Sign up button
        RoundedButton signupBtn = new RoundedButton("Sign up");
        signupBtn.setPrefWidth(180);
        signupBtn.setPrefHeight(60);
        signupBtn.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        signupBtn.setTooltip(new javafx.scene.control.Tooltip("Create a new account"));
        signupBtn.setOnAction(e -> parentApp.showSignUpPanel());

        buttonPanel.getChildren().addAll(loginBtn, signupBtn);

        return buttonPanel;
    }

    /* ================= HELPERS ================= */
    private Node loadLogo() {
        try {
            // Try multiple paths for the logo
            String[] paths = {
                    "/logo.png",
                    "/resources/logo.png",
                    "/images/logo.png"
            };

            for (String path : paths) {
                javafx.scene.image.Image image = new javafx.scene.image.Image(getClass().getResourceAsStream(path));
                if (!image.isError()) {
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(80);
                    imageView.setFitHeight(80);
                    imageView.setPreserveRatio(true);
                    return imageView;
                }
            }
        } catch (Exception e) {
            // Logo not found, will use BrainLogo
        }
        return null;
    }

    /* ================= CUSTOM COMPONENTS ================= */

    class RoundedButton extends Button {

        public RoundedButton(String text) {
            super(text);

            setTextFill(Color.WHITE);
            setCursor(Cursor.HAND);

            // Base style
            setStyle(
                    "-fx-background-color: #" + toHex(PRIMARY) + ";" +
                            "-fx-background-radius: 40;" +
                            "-fx-border-radius: 40;" +
                            "-fx-padding: 15 30;"
            );

            // Pressed effect
            setOnMousePressed(e ->
                    setStyle(
                            "-fx-background-color: #" + toHex(PRIMARY.darker()) + ";" +
                                    "-fx-background-radius: 40;" +
                                    "-fx-border-radius: 40;" +
                                    "-fx-padding: 15 30;"
                    )
            );

            // Released effect
            setOnMouseReleased(e ->
                    setStyle(
                            "-fx-background-color: #" + toHex(PRIMARY) + ";" +
                                    "-fx-background-radius: 40;" +
                                    "-fx-border-radius: 40;" +
                                    "-fx-padding: 15 30;"
                    )
            );

            // Hover effect
            setOnMouseEntered(e ->
                    setStyle(
                            "-fx-background-color: #" + toHex(BUTTON_HOVER) + ";" +
                                    "-fx-background-radius: 40;" +
                                    "-fx-border-radius: 40;" +
                                    "-fx-padding: 15 30;"
                    )
            );

            setOnMouseExited(e ->
                    setStyle(
                            "-fx-background-color: #" + toHex(PRIMARY) + ";" +
                                    "-fx-background-radius: 40;" +
                                    "-fx-border-radius: 40;" +
                                    "-fx-padding: 15 30;"
                    )
            );

            // Accessibility (JavaFX handles this automatically)
        }
    }

    class BrainLogo extends StackPane {

        public BrainLogo() {
            setPrefSize(80, 80);
            setMaxSize(80, 80);

            // Create brain shape using arcs
            Arc leftArc = new Arc();
            leftArc.setCenterX(25);
            leftArc.setCenterY(40);
            leftArc.setRadiusX(20);
            leftArc.setRadiusY(25);
            leftArc.setStartAngle(90);
            leftArc.setLength(180);
            leftArc.setType(ArcType.OPEN);
            leftArc.setStroke(Color.web(toHex(PRIMARY)));
            leftArc.setStrokeWidth(3);
            leftArc.setFill(null);

            Arc rightArc = new Arc();
            rightArc.setCenterX(55);
            rightArc.setCenterY(40);
            rightArc.setRadiusX(20);
            rightArc.setRadiusY(25);
            rightArc.setStartAngle(270);
            rightArc.setLength(180);
            rightArc.setType(ArcType.OPEN);
            rightArc.setStroke(Color.web(toHex(PRIMARY)));
            rightArc.setStrokeWidth(3);
            rightArc.setFill(null);

            Arc topArc = new Arc();
            topArc.setCenterX(40);
            topArc.setCenterY(30);
            topArc.setRadiusX(25);
            topArc.setRadiusY(20);
            topArc.setStartAngle(180);
            topArc.setLength(180);
            topArc.setType(ArcType.OPEN);
            topArc.setStroke(Color.web(toHex(PRIMARY)));
            topArc.setStrokeWidth(3);
            topArc.setFill(null);

            getChildren().addAll(leftArc, rightArc, topArc);
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            // Center the arcs in the available space
            double w = getWidth();
            double h = getHeight();

            // Adjust arc positions to be centered
            for (javafx.scene.Node node : getChildren()) {
                if (node instanceof Arc) {
                    Arc arc = (Arc) node;
                    if (arc.getCenterX() == 25) {
                        arc.setCenterX(w * 0.3);
                    } else if (arc.getCenterX() == 55) {
                        arc.setCenterX(w * 0.7);
                    } else if (arc.getCenterX() == 40) {
                        arc.setCenterX(w * 0.5);
                    }
                    arc.setCenterY(h * 0.5);
                }
            }
        }
    }

    /* ================= UTILITY ================= */
    private String toHex(Color color) {
        return String.format("%02x%02x%02x",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }
}