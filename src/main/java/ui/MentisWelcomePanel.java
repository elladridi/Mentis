package ui;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class MentisWelcomePanel extends StackPane {

    private static final Color BG_START = Color.web("#F5F7FA");
    private static final Color BG_END = Color.web("#E8F5E9");
    private static final Color PRIMARY = Color.web("#50C878");
    private static final Color PRIMARY_DARK = Color.web("#2E7D32");
    private static final Color PRIMARY_MID = Color.web("#3A9B5E");
    private static final Color INK = Color.web("#1A3C34");
    private static final Color MUTED = Color.web("#6C757D");
    private static final Color WHITE = Color.WHITE;
    private static final Color SOFT_GREEN = Color.web("#F1F8E9");

    private final MentisLoginFrame parentApp;

    public MentisWelcomePanel(MentisLoginFrame parentApp) {
        this.parentApp = parentApp;

        setStyle("-fx-background-color: linear-gradient(to bottom right, #F5F7FA, #E8F5E9);");
        setPadding(new Insets(0));

        initComponents();
        startEntranceAnimation();
    }

    private void initComponents() {
        Pane backgroundDecor = createBackgroundDecor();

        BorderPane page = new BorderPane();
        page.setPadding(new Insets(34, 54, 34, 54));
        page.setStyle("-fx-background-color: transparent;");

        page.setTop(createHeader());

        StackPane centerWrapper = new StackPane();
        centerWrapper.setPadding(new Insets(30, 0, 20, 0));

        HBox hero = createHeroSection();
        centerWrapper.getChildren().add(hero);

        page.setCenter(centerWrapper);
        page.setBottom(createFooter());

        getChildren().addAll(backgroundDecor, page);
    }

    private Pane createBackgroundDecor() {
        Pane pane = new Pane();
        pane.setMouseTransparent(true);

        Circle glow1 = new Circle(190);
        glow1.setFill(Color.web("#50C878", 0.18));
        glow1.setEffect(new GaussianBlur(65));
        glow1.setTranslateX(-420);
        glow1.setTranslateY(-220);

        Circle glow2 = new Circle(250);
        glow2.setFill(Color.web("#9B5DE5", 0.10));
        glow2.setEffect(new GaussianBlur(80));
        glow2.setTranslateX(470);
        glow2.setTranslateY(250);

        Circle glow3 = new Circle(125);
        glow3.setFill(Color.web("#38F9D7", 0.16));
        glow3.setEffect(new GaussianBlur(60));
        glow3.setTranslateX(260);
        glow3.setTranslateY(-260);

        StackPane.setAlignment(glow1, Pos.TOP_LEFT);
        StackPane.setAlignment(glow2, Pos.BOTTOM_RIGHT);
        StackPane.setAlignment(glow3, Pos.TOP_RIGHT);

        pane.getChildren().addAll(glow1, glow2, glow3);
        return pane;
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        HBox brand = new HBox(13);
        brand.setAlignment(Pos.CENTER_LEFT);

        Node logo = loadLogo();
        if (logo == null) {
            logo = new BrainLogo();
        }

        VBox brandText = new VBox(1);
        brandText.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label("MENTIS");
        name.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 27));
        name.setTextFill(PRIMARY_DARK);

        Label tag = new Label("Mental Health Platform");
        tag.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        tag.setTextFill(MUTED);

        brandText.getChildren().addAll(name, tag);
        brand.getChildren().addAll(logo, brandText);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox miniNav = new HBox(10);
        miniNav.setAlignment(Pos.CENTER_RIGHT);
        miniNav.getChildren().addAll(
                createMiniChip("AI Wellness"),
                createMiniChip("Therapy Sessions"),
                createMiniChip("Mood Tracking")
        );

        header.getChildren().addAll(brand, spacer, miniNav);
        return header;
    }

    private HBox createHeroSection() {
        HBox hero = new HBox(42);
        hero.setAlignment(Pos.CENTER);
        hero.setMaxWidth(1180);

        VBox leftCard = createHeroCard();
        VBox rightShowcase = createShowcasePanel();

        HBox.setHgrow(leftCard, Priority.ALWAYS);
        HBox.setHgrow(rightShowcase, Priority.ALWAYS);

        hero.getChildren().addAll(leftCard, rightShowcase);
        return hero;
    }

    private VBox createHeroCard() {
        VBox card = new VBox(22);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(42));
        card.setMaxWidth(610);
        card.setMinHeight(520);
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.92);" +
                        "-fx-background-radius: 34;" +
                        "-fx-border-radius: 34;" +
                        "-fx-border-color: rgba(255,255,255,0.75);" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 34, 0, 0, 14);"
        );

        Label badge = new Label("✨ AI-powered mental wellness");
        badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        badge.setTextFill(PRIMARY_DARK);
        badge.setPadding(new Insets(8, 16, 8, 16));
        badge.setStyle(
                "-fx-background-color: #F1F8E9;" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-color: #C8E6D2;" +
                        "-fx-border-radius: 999;"
        );

        Label title = new Label("Welcome to\nMENTIS");
        title.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 62));
        title.setTextFill(INK);
        title.setLineSpacing(-4);

        Label subtitle = new Label(
                "Your calm space for mental well-being, therapy sessions, mood tracking, assessments, goals, events, and personalized AI recommendations."
        );
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 17));
        subtitle.setTextFill(MUTED);
        subtitle.setWrapText(true);
        subtitle.setLineSpacing(4);
        subtitle.setMaxWidth(520);

        HBox featureRow = new HBox(10);
        featureRow.setAlignment(Pos.CENTER_LEFT);
        featureRow.getChildren().addAll(
                createFeatureChip("🧠 AI Insights"),
                createFeatureChip("📊 Progress"),
                createFeatureChip("🌿 Wellbeing")
        );

        HBox buttonPanel = createButtonPanel();

        Label trustNote = new Label("Private • Supportive • Built for your wellness journey");
        trustNote.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        trustNote.setTextFill(MUTED);

        card.getChildren().addAll(badge, title, subtitle, featureRow, buttonPanel, trustNote);

        return card;
    }

    private VBox createShowcasePanel() {
        VBox showcase = new VBox(18);
        showcase.setAlignment(Pos.CENTER);
        showcase.setPadding(new Insets(30));
        showcase.setMaxWidth(470);
        showcase.setMinHeight(520);
        showcase.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #1A3C34, #2E7D32);" +
                        "-fx-background-radius: 34;" +
                        "-fx-effect: dropshadow(gaussian, rgba(46,125,50,0.25), 34, 0, 0, 16);"
        );

        Label topBadge = new Label("LIVE WELLNESS DASHBOARD");
        topBadge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        topBadge.setTextFill(Color.web("#BFF4D0"));
        topBadge.setPadding(new Insets(6, 14, 6, 14));
        topBadge.setStyle("-fx-background-color: rgba(255,255,255,0.12); -fx-background-radius: 999;");

        StackPane orbit = createWellnessOrbit();

        HBox statsRow = new HBox(12);
        statsRow.setAlignment(Pos.CENTER);
        statsRow.getChildren().addAll(
                createDarkMetric("😊", "Mood", "Stable"),
                createDarkMetric("📈", "Growth", "+18%"),
                createDarkMetric("🧘", "Calm", "92%")
        );

        VBox miniScreen = createMiniScreen();

        showcase.getChildren().addAll(topBadge, orbit, statsRow, miniScreen);
        return showcase;
    }

    private StackPane createWellnessOrbit() {
        StackPane root = new StackPane();
        root.setPrefSize(240, 180);

        Circle outer = new Circle(82);
        outer.setFill(Color.TRANSPARENT);
        outer.setStroke(Color.web("#BFF4D0", 0.32));
        outer.setStrokeWidth(2);

        Circle inner = new Circle(58);
        inner.setFill(Color.web("#FFFFFF", 0.12));
        inner.setStroke(Color.web("#FFFFFF", 0.22));
        inner.setStrokeWidth(1.5);

        Label icon = new Label("🌿");
        icon.setFont(Font.font("Segoe UI Emoji", 54));

        Label node1 = orbitDot("AI");
        node1.setTranslateX(-95);
        node1.setTranslateY(-35);

        Label node2 = orbitDot("Mood");
        node2.setTranslateX(95);
        node2.setTranslateY(-18);

        Label node3 = orbitDot("Care");
        node3.setTranslateX(8);
        node3.setTranslateY(84);

        root.getChildren().addAll(outer, inner, icon, node1, node2, node3);
        return root;
    }

    private Label orbitDot(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        label.setTextFill(Color.WHITE);
        label.setPadding(new Insets(6, 10, 6, 10));
        label.setStyle("-fx-background-color: rgba(255,255,255,0.16); -fx-background-radius: 999;");
        return label;
    }

    private VBox createMiniScreen() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(18));
        panel.setMaxWidth(350);
        panel.setStyle(
                "-fx-background-color: rgba(255,255,255,0.12);" +
                        "-fx-background-radius: 22;" +
                        "-fx-border-radius: 22;" +
                        "-fx-border-color: rgba(255,255,255,0.18);"
        );

        Label title = new Label("Today’s Wellness Plan");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        title.setTextFill(Color.WHITE);

        panel.getChildren().addAll(
                title,
                createPlanRow("📝", "Take assessment", "5 min"),
                createPlanRow("🎵", "Relaxation playlist", "12 min"),
                createPlanRow("📅", "Therapy session", "Scheduled")
        );

        return panel;
    }

    private HBox createPlanRow(String icon, String title, String meta) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 0, 8, 0));

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Segoe UI Emoji", 18));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        titleLabel.setTextFill(Color.web("#EAFBF0"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label metaLabel = new Label(meta);
        metaLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        metaLabel.setTextFill(Color.web("#BFF4D0"));

        row.getChildren().addAll(iconLabel, titleLabel, spacer, metaLabel);
        return row;
    }

    private VBox createDarkMetric(String icon, String label, String value) {
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(12, 14, 12, 14));
        card.setPrefWidth(105);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.12); -fx-background-radius: 18;");

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Segoe UI Emoji", 22));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        valueLabel.setTextFill(Color.WHITE);

        Label labelNode = new Label(label);
        labelNode.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        labelNode.setTextFill(Color.web("#BFF4D0"));

        card.getChildren().addAll(iconLabel, valueLabel, labelNode);
        return card;
    }

    private HBox createButtonPanel() {
        HBox buttonPanel = new HBox(16);
        buttonPanel.setAlignment(Pos.CENTER_LEFT);

        Button loginBtn = createPrimaryButton("Log in", "Go to login screen");
        loginBtn.setOnAction(e -> parentApp.showLoginPanel());

        Button signupBtn = createSecondaryButton("Create account", "Create a new account");
        signupBtn.setOnAction(e -> parentApp.showSignUpPanel());

        buttonPanel.getChildren().addAll(loginBtn, signupBtn);
        return buttonPanel;
    }

    private Button createPrimaryButton(String text, String tooltip) {
        Button button = new Button(text + "  →");
        button.setPrefWidth(180);
        button.setPrefHeight(56);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        button.setTextFill(Color.WHITE);
        button.setCursor(Cursor.HAND);
        button.setTooltip(new Tooltip(tooltip));

        String base = "-fx-background-color: linear-gradient(to right, #50C878, #2E7D32);" +
                "-fx-background-radius: 999;" +
                "-fx-border-radius: 999;" +
                "-fx-effect: dropshadow(gaussian, rgba(80,200,120,0.35), 18, 0, 0, 7);";

        String hover = "-fx-background-color: linear-gradient(to right, #3A9B5E, #2E7D32);" +
                "-fx-background-radius: 999;" +
                "-fx-border-radius: 999;" +
                "-fx-translate-y: -2;" +
                "-fx-effect: dropshadow(gaussian, rgba(80,200,120,0.50), 24, 0, 0, 9);";

        button.setStyle(base);
        button.setOnMouseEntered(e -> button.setStyle(hover));
        button.setOnMouseExited(e -> button.setStyle(base));

        addButtonPressAnimation(button);
        return button;
    }

    private Button createSecondaryButton(String text, String tooltip) {
        Button button = new Button(text);
        button.setPrefWidth(180);
        button.setPrefHeight(56);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        button.setTextFill(PRIMARY_DARK);
        button.setCursor(Cursor.HAND);
        button.setTooltip(new Tooltip(tooltip));

        String base = "-fx-background-color: white;" +
                "-fx-background-radius: 999;" +
                "-fx-border-radius: 999;" +
                "-fx-border-color: #C8E6D2;" +
                "-fx-border-width: 2;";

        String hover = "-fx-background-color: #F1F8E9;" +
                "-fx-background-radius: 999;" +
                "-fx-border-radius: 999;" +
                "-fx-border-color: #50C878;" +
                "-fx-border-width: 2;" +
                "-fx-translate-y: -2;";

        button.setStyle(base);
        button.setOnMouseEntered(e -> button.setStyle(hover));
        button.setOnMouseExited(e -> button.setStyle(base));

        addButtonPressAnimation(button);
        return button;
    }

    private Label createFeatureChip(String text) {
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

    private Label createMiniChip(String text) {
        Label chip = new Label(text);
        chip.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        chip.setTextFill(MUTED);
        chip.setPadding(new Insets(8, 14, 8, 14));
        chip.setStyle(
                "-fx-background-color: rgba(255,255,255,0.72);" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-color: rgba(80,200,120,0.18);" +
                        "-fx-border-radius: 999;"
        );
        return chip;
    }

    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(8, 0, 0, 0));

        Label label = new Label("© 2026 Mentis · Mental health, handled with care · Private by design");
        label.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        label.setTextFill(MUTED);
        label.setTextAlignment(TextAlignment.CENTER);

        footer.getChildren().add(label);
        return footer;
    }

    private Node loadLogo() {
        try {
            String[] paths = {
                    "/logo.png",
                    "/resources/logo.png",
                    "/images/logo.png"
            };

            for (String path : paths) {
                if (getClass().getResourceAsStream(path) == null) continue;

                javafx.scene.image.Image image =
                        new javafx.scene.image.Image(getClass().getResourceAsStream(path));

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

        FadeTransition fade = new FadeTransition(Duration.millis(850), this);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setInterpolator(Interpolator.EASE_OUT);
        fade.play();

        for (int i = 0; i < getChildren().size(); i++) {
            Node node = getChildren().get(i);
            TranslateTransition slide = new TranslateTransition(Duration.millis(900), node);
            slide.setFromY(22);
            slide.setToY(0);
            slide.setInterpolator(Interpolator.EASE_OUT);
            slide.play();
        }
    }

    private void addButtonPressAnimation(Button button) {
        button.setOnMousePressed(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(90), button);
            scale.setToX(0.96);
            scale.setToY(0.96);
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

        public BrainLogo() {
            setPrefSize(58, 58);
            setMaxSize(58, 58);
            setMinSize(58, 58);

            Circle bg = new Circle(29);
            bg.setFill(Color.web("#F1F8E9"));
            bg.setStroke(Color.web("#C8E6D2"));
            bg.setStrokeWidth(1.5);

            Arc leftArc = new Arc();
            leftArc.setCenterX(18);
            leftArc.setCenterY(29);
            leftArc.setRadiusX(13);
            leftArc.setRadiusY(17);
            leftArc.setStartAngle(90);
            leftArc.setLength(180);
            leftArc.setType(ArcType.OPEN);
            leftArc.setStroke(PRIMARY_DARK);
            leftArc.setStrokeWidth(2.7);
            leftArc.setFill(null);

            Arc rightArc = new Arc();
            rightArc.setCenterX(40);
            rightArc.setCenterY(29);
            rightArc.setRadiusX(13);
            rightArc.setRadiusY(17);
            rightArc.setStartAngle(270);
            rightArc.setLength(180);
            rightArc.setType(ArcType.OPEN);
            rightArc.setStroke(PRIMARY_DARK);
            rightArc.setStrokeWidth(2.7);
            rightArc.setFill(null);

            Arc topArc = new Arc();
            topArc.setCenterX(29);
            topArc.setCenterY(22);
            topArc.setRadiusX(17);
            topArc.setRadiusY(12);
            topArc.setStartAngle(180);
            topArc.setLength(180);
            topArc.setType(ArcType.OPEN);
            topArc.setStroke(PRIMARY);
            topArc.setStrokeWidth(2.7);
            topArc.setFill(null);

            getChildren().addAll(bg, leftArc, rightArc, topArc);
        }
    }

    private String toHex(Color color) {
        return String.format("%02x%02x%02x",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}
