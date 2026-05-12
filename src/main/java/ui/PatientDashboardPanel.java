package ui;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import models.user;
import services.userservice;
import utils.z.DatabaseConnectionMentis;

import java.lang.reflect.Method;
import java.sql.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class PatientDashboardPanel extends VBox {

    private final MentisLoginFrame parentApp;

    private int userId;
    private String userName;

    private user currentUser;

    private int assessmentsTaken = 0;
    private int badgesEarned = 0;
    private int totalBadges = 8;
    private int totalXP = 0;
    private int averageScore = 0;
    private String latestRisk = "N/A";

    private Label welcomeTitle;
    private Label levelLabel;
    private Label nextLevelLabel;
    private ProgressBar levelProgressBar;

    private Label assessmentsValueLabel;
    private Label badgesValueLabel;
    private Label xpValueLabel;
    private Label averageScoreValueLabel;

    private VBox badgesGrid;
    private VBox recentTimelineBox;
    private VBox profileInfoBox;
    private VBox faceIdCardContainer;

    private ObservableList<ResultRow> recentResults = FXCollections.observableArrayList();

    private static final Color PRIMARY = Color.web("#50C878");
    private static final Color PRIMARY_DARK = Color.web("#2E7D32");
    private static final Color PRIMARY_MID = Color.web("#3A9B5E");
    private static final Color INK = Color.web("#1A3C34");
    private static final Color TEXT = Color.web("#2D3748");
    private static final Color MUTED = Color.web("#6C757D");
    private static final Color LINE = Color.web("#E9ECEF");
    private static final Color GOLD = Color.web("#F1C40F");
    private static final Color ORANGE = Color.web("#E67E22");
    private static final Color PURPLE = Color.web("#9B59B6");
    private static final Color BLUE = Color.web("#4FACFE");
    private static final Color RED = Color.web("#E74C3C");

    public PatientDashboardPanel(MentisLoginFrame parentApp) {
        this.parentApp = parentApp;
        this.userId = parentApp.getUserId();
        this.userName = parentApp.getUserName();

        setPadding(new Insets(0));
        setSpacing(0);
        setStyle("-fx-background-color: linear-gradient(to bottom right, #F5F7FA, #E8F5E9);");

        loadDashboardData();

        ScrollPane scrollPane = new ScrollPane(createPage());
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle(
                "-fx-background: transparent;" +
                        "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;"
        );

        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        getChildren().add(scrollPane);

        playEntranceAnimation();
    }

    public void refreshData() {
        this.userId = parentApp.getUserId();
        this.userName = parentApp.getUserName();

        loadDashboardData();

        if (welcomeTitle != null) {
            welcomeTitle.setText("Welcome back, " + getFirstName() + "! 🌟");
        }

        double progress = totalBadges == 0 ? 0 : (double) badgesEarned / (double) totalBadges;
        if (levelLabel != null) levelLabel.setText("🏅 Level " + badgesEarned);
        if (nextLevelLabel != null) nextLevelLabel.setText("🎯 Next Level: " + Math.round(progress * 100) + "%");
        if (levelProgressBar != null) levelProgressBar.setProgress(progress);

        if (assessmentsValueLabel != null) assessmentsValueLabel.setText(String.valueOf(assessmentsTaken));
        if (badgesValueLabel != null) badgesValueLabel.setText(String.valueOf(badgesEarned));
        if (xpValueLabel != null) xpValueLabel.setText(String.valueOf(totalXP));
        if (averageScoreValueLabel != null) averageScoreValueLabel.setText(String.valueOf(averageScore));



        if (recentTimelineBox != null) {
            recentTimelineBox.getChildren().clear();
            recentTimelineBox.getChildren().addAll(createTimelineContent().getChildren());
        }

        if (profileInfoBox != null) {
            profileInfoBox.getChildren().clear();
            profileInfoBox.getChildren().addAll(createProfileInfoContent().getChildren());
        }

        if (faceIdCardContainer != null) {
            faceIdCardContainer.getChildren().clear();
            faceIdCardContainer.getChildren().add(createFaceIdCard());
        }
    }

    private VBox createPage() {
        VBox page = new VBox(24);
        page.setPadding(new Insets(28, 32, 36, 32));
        page.setStyle("-fx-background-color: transparent;");

        page.getChildren().addAll(
                createHeroSection(),
                createFaceIdWrapper(),
                createQuickAccessSection(),
                createProfileAndStatsSection(),
                createRecentActivitySection()
        );

        return page;
    }

    private VBox createHeroSection() {
        VBox hero = new VBox(14);
        hero.setAlignment(Pos.CENTER);
        hero.setPadding(new Insets(32, 28, 32, 28));
        hero.setStyle(
                "-fx-background-color: rgba(255,255,255,0.90);" +
                        "-fx-background-radius: 32;" +
                        "-fx-border-color: rgba(255,255,255,0.75);" +
                        "-fx-border-radius: 32;" +
                        "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.08), 28, 0, 0, 12);"
        );

        HBox floatingIcons = new HBox(46);
        floatingIcons.setAlignment(Pos.CENTER);
        floatingIcons.getChildren().addAll(
                floatingIcon("🌟", 0),
                floatingIcon("🏆", 100),
                floatingIcon("🎯", 200),
                floatingIcon("💪", 300)
        );

        welcomeTitle = new Label("Welcome back, " + getFirstName() + "! 🌟");
        welcomeTitle.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 44));
        welcomeTitle.setTextFill(PRIMARY_DARK);
        welcomeTitle.setTextAlignment(TextAlignment.CENTER);
        welcomeTitle.setWrapText(true);

        Label subtitle = new Label("Your mental wellness journey continues here.");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 17));
        subtitle.setTextFill(MUTED);
        subtitle.setTextAlignment(TextAlignment.CENTER);

        VBox levelBox = new VBox(7);
        levelBox.setAlignment(Pos.CENTER);
        levelBox.setMaxWidth(360);

        HBox labels = new HBox();
        labels.setAlignment(Pos.CENTER);
        levelLabel = new Label("🏅 Level " + badgesEarned);
        levelLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        levelLabel.setTextFill(TEXT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        double progress = totalBadges == 0 ? 0 : (double) badgesEarned / (double) totalBadges;
        nextLevelLabel = new Label("🎯 Next Level: " + Math.round(progress * 100) + "%");
        nextLevelLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        nextLevelLabel.setTextFill(TEXT);

        labels.getChildren().addAll(levelLabel, spacer, nextLevelLabel);

        levelProgressBar = new ProgressBar(progress);
        levelProgressBar.setPrefHeight(11);
        levelProgressBar.setMaxWidth(Double.MAX_VALUE);
        levelProgressBar.setStyle("-fx-accent: #50C878; -fx-control-inner-background: #E9ECEF;");

        levelBox.getChildren().addAll(labels, levelProgressBar);

        hero.getChildren().addAll(floatingIcons, welcomeTitle, subtitle, levelBox);

        return hero;
    }

    private Label floatingIcon(String icon, int delayMillis) {
        Label label = new Label(icon);
        label.setFont(Font.font("Segoe UI Emoji", 26));

        TranslateTransition transition = new TranslateTransition(Duration.millis(1900 + delayMillis), label);
        transition.setFromY(0);
        transition.setToY(-10);
        transition.setAutoReverse(true);
        transition.setCycleCount(TranslateTransition.INDEFINITE);
        transition.setDelay(Duration.millis(delayMillis));
        transition.play();

        return label;
    }

    private VBox createFaceIdWrapper() {
        faceIdCardContainer = new VBox();
        faceIdCardContainer.getChildren().add(createFaceIdCard());
        return faceIdCardContainer;
    }

    private Node createFaceIdCard() {
        boolean faceEnabled = false;
        if (userId > 0) {
            faceEnabled = userservice.hasFaceEnabled(userId);
        }

        HBox card = new HBox(22);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(24, 26, 24, 26));
        card.setStyle(
                "-fx-background-color: " + (faceEnabled
                        ? "linear-gradient(to bottom right, #50C878, #2E7D32)"
                        : "linear-gradient(to bottom right, #667EEA, #764BA2)") + ";" +
                        "-fx-background-radius: 24;" +
                        "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.18), 24, 0, 0, 10);"
        );

        Label icon = new Label(faceEnabled ? "✅" : "🛡️");
        icon.setFont(Font.font("Segoe UI Emoji", 48));

        VBox textBox = new VBox(4);
        Label title = new Label(faceEnabled ? "Face ID Active" : "🔐 Fortify Your Account");
        title.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 24));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label(faceEnabled
                ? "Your account is secured with biometric authentication."
                : "Enable Face ID for lightning-fast, secure access.");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        subtitle.setTextFill(Color.web("#FFFFFF", 0.82));

        textBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button button = faceEnabled ? createGlassButton("Disable") : createGlassButton("Enable Face ID");
        final boolean enabledNow = faceEnabled;
        button.setOnAction(e -> {
            if (enabledNow) {
                disableFaceId();
            } else {
                openFaceIdRegistration();
            }
        });

        card.getChildren().addAll(icon, textBox, spacer, button);
        return card;
    }



    private Node createMilestoneBanner() {
        HBox banner = new HBox(10);
        banner.setAlignment(Pos.CENTER);
        banner.setPadding(new Insets(14, 22, 14, 22));
        banner.setStyle(
                "-fx-background-color: linear-gradient(to right, #FFF8D6, #FFEFA1, #FFF8D6);" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-color: #F1C40F;" +
                        "-fx-border-radius: 999;"
        );

        Label text = new Label("🎉 Milestone Unlocked! You've earned " + badgesEarned + " badges 🎊");
        text.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 15));
        text.setTextFill(Color.web("#7A5A00"));

        banner.getChildren().add(text);
        return banner;
    }

    private VBox createQuickAccessSection() {
        VBox card = new VBox(18);
        card.setPadding(new Insets(24));
        card.setStyle(cardStyle(25));

        Label title = new Label("⚡ Quick Access");
        title.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 20));
        title.setTextFill(INK);

        HBox grid = new HBox(16);
        grid.setAlignment(Pos.CENTER);

        VBox takeAssessment = createQuickActionCard("📋", "Take Assessment", "+" + Math.max(10, 50 - badgesEarned * 5) + " XP");
        takeAssessment.setOnMouseClicked(e -> parentApp.showTakeAssessmentPanel());

        VBox progress = createQuickActionCard("📈", "View Progress", "Track your journey");
        progress.setOnMouseClicked(e -> parentApp.showResultsPanel());

        VBox content = createQuickActionCard("📚", "View Content", "Learn & grow");
        content.setOnMouseClicked(e -> parentApp.showContentUploadPanel());

        VBox chatbot = createQuickActionCard("🤖", "AI Support", "24/7 support");
        chatbot.setOnMouseClicked(e -> showInfo("AI Chatbot will open from your chatbot module."));

        HBox.setHgrow(takeAssessment, Priority.ALWAYS);
        HBox.setHgrow(progress, Priority.ALWAYS);
        HBox.setHgrow(content, Priority.ALWAYS);
        HBox.setHgrow(chatbot, Priority.ALWAYS);

        grid.getChildren().addAll(takeAssessment, progress, content, chatbot);

        card.getChildren().addAll(title, grid);
        return card;
    }

    private VBox createQuickActionCard(String icon, String title, String subtitle) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(18));
        card.setMinHeight(140);
        card.setCursor(Cursor.HAND);
        card.setStyle(
                "-fx-background-color: #F8F9FA;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: #E9ECEF;" +
                        "-fx-border-radius: 18;"
        );

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Segoe UI Emoji", 36));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        titleLabel.setTextFill(INK);

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        subtitleLabel.setTextFill(MUTED);

        card.getChildren().addAll(iconLabel, titleLabel, subtitleLabel);

        String base = card.getStyle();
        String hover =
                "-fx-background-color: linear-gradient(to bottom right, #50C878, #2E7D32);" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: transparent;" +
                        "-fx-border-radius: 18;" +
                        "-fx-translate-y: -5;" +
                        "-fx-effect: dropshadow(gaussian, rgba(80,200,120,0.30), 24, 0, 0, 10);";

        card.setOnMouseEntered(e -> {
            card.setStyle(hover);
            titleLabel.setTextFill(Color.WHITE);
            subtitleLabel.setTextFill(Color.web("#FFFFFF", 0.88));
        });

        card.setOnMouseExited(e -> {
            card.setStyle(base);
            titleLabel.setTextFill(INK);
            subtitleLabel.setTextFill(MUTED);
        });

        return card;
    }

    private HBox createProfileAndStatsSection() {
        HBox row = new HBox(22);
        row.setAlignment(Pos.TOP_CENTER);

        VBox profile = createProfileCard();
        VBox stats = createActivityStatsCard();

        HBox.setHgrow(profile, Priority.ALWAYS);
        HBox.setHgrow(stats, Priority.ALWAYS);

        profile.setMaxWidth(Double.MAX_VALUE);
        stats.setMaxWidth(Double.MAX_VALUE);

        row.getChildren().addAll(profile, stats);
        return row;
    }

    private VBox createProfileCard() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(24));
        card.setPrefWidth(460);
        card.setStyle(cardStyle(25));

        StackPane avatar = new StackPane();
        avatar.setPrefSize(84, 84);
        avatar.setMaxSize(84, 84);
        avatar.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #50C878, #2E7D32);" +
                        "-fx-background-radius: 999;" +
                        "-fx-effect: dropshadow(gaussian, rgba(80,200,120,0.28), 20, 0, 0, 7);"
        );

        Label initials = new Label(getInitials());
        initials.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 26));
        initials.setTextFill(Color.WHITE);
        avatar.getChildren().add(initials);

        HBox avatarWrap = new HBox(avatar);
        avatarWrap.setAlignment(Pos.CENTER);

        Label name = new Label(getFullName());
        name.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 22));
        name.setTextFill(INK);
        name.setAlignment(Pos.CENTER);
        name.setMaxWidth(Double.MAX_VALUE);

        profileInfoBox = new VBox(0);
        profileInfoBox.getChildren().addAll(createProfileInfoContent().getChildren());

        card.getChildren().addAll(avatarWrap, name, profileInfoBox);
        return card;
    }

    private VBox createProfileInfoContent() {
        VBox box = new VBox(0);
        box.getChildren().addAll(
                infoRow("✉️ Email", safe(currentUser == null ? parentApp.getUserEmail() : currentUser.getEmail())),
                infoRow("📞 Phone", safe(currentUser == null ? parentApp.getUserPhone() : currentUser.getPhone())),
                infoRow("📅 Birth", getBirthDate()),
                infoRow("📊 Age", getAgeText()),
                infoRow("📌 Status", "Active Member")
        );
        return box;
    }

    private HBox infoRow(String label, String value) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 0, 12, 0));
        row.setStyle("-fx-border-color: transparent transparent #E9ECEF transparent;");

        Label l = new Label(label);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        l.setTextFill(MUTED);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label v = new Label(value == null || value.trim().isEmpty() ? "N/A" : value);
        v.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        v.setTextFill(TEXT);

        row.getChildren().addAll(l, spacer, v);
        return row;
    }

    private VBox createActivityStatsCard() {
        VBox card = new VBox(18);
        card.setPadding(new Insets(24));
        card.setStyle(cardStyle(25));

        Label title = new Label("📊 Activity Overview");
        title.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 20));
        title.setTextFill(INK);

        GridPane stats = new GridPane();
        stats.setHgap(14);
        stats.setVgap(14);

        VBox assessments = createMiniStat("Assessments Taken", String.valueOf(assessmentsTaken), PRIMARY);
        VBox badges = createMiniStat("Badges Earned", String.valueOf(badgesEarned), GOLD);
        VBox xp = createMiniStat("Total XP", String.valueOf(totalXP), PURPLE);
        VBox avg = createMiniStat("Average Score", String.valueOf(averageScore), BLUE);

        assessmentsValueLabel = (Label) assessments.getChildren().get(0);
        badgesValueLabel = (Label) badges.getChildren().get(0);
        xpValueLabel = (Label) xp.getChildren().get(0);
        averageScoreValueLabel = (Label) avg.getChildren().get(0);

        stats.add(assessments, 0, 0);
        stats.add(badges, 1, 0);
        stats.add(xp, 0, 1);
        stats.add(avg, 1, 1);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(50);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(50);
        stats.getColumnConstraints().addAll(c1, c2);

        Node chart = createRiskPieChart();

        card.getChildren().addAll(title, stats, chart);
        return card;
    }

    private VBox createMiniStat(String label, String value, Color color) {
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(18));
        box.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #F8F9FA, #E9ECEF);" +
                        "-fx-background-radius: 16;"
        );

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 32));
        valueLabel.setTextFill(color);

        Label labelNode = new Label(label);
        labelNode.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        labelNode.setTextFill(MUTED);
        labelNode.setTextAlignment(TextAlignment.CENTER);
        labelNode.setWrapText(true);

        box.getChildren().addAll(valueLabel, labelNode);
        return box;
    }

    private Node createRiskPieChart() {
        PieChart chart = new PieChart();
        chart.setTitle("Latest Risk: " + latestRisk);
        chart.setLabelsVisible(false);
        chart.setLegendVisible(true);
        chart.setPrefHeight(230);

        int safe = 0;
        int moderate = 0;
        int high = 0;

        for (ResultRow r : recentResults) {
            String risk = r.getRiskLevel().toLowerCase();
            if (risk.contains("high") || risk.contains("severe")) high++;
            else if (risk.contains("moderate") || risk.contains("mild")) moderate++;
            else safe++;
        }

        if (recentResults.isEmpty()) {
            chart.getData().add(new PieChart.Data("No data", 1));
        } else {
            chart.getData().add(new PieChart.Data("Low / Minimal", Math.max(0, safe)));
            chart.getData().add(new PieChart.Data("Moderate / Mild", Math.max(0, moderate)));
            chart.getData().add(new PieChart.Data("High / Severe", Math.max(0, high)));
        }

        return chart;
    }

    private VBox createRecentActivitySection() {
        VBox card = new VBox(0);
        card.setStyle(cardStyle(22));

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 24, 20, 24));
        header.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #FCFFFD, #F8FBFF);" +
                        "-fx-background-radius: 22 22 0 0;" +
                        "-fx-border-color: #EDF2F7;" +
                        "-fx-border-width: 0 0 1 0;"
        );

        Label title = new Label("🕘 Recent Activity");
        title.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 20));
        title.setTextFill(INK);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewAll = createOutlineButton("View Results");
        viewAll.setOnAction(e -> parentApp.showResultsPanel());

        header.getChildren().addAll(title, spacer, viewAll);

        recentTimelineBox = new VBox();
        recentTimelineBox.setPadding(new Insets(20, 24, 24, 24));
        recentTimelineBox.getChildren().addAll(createTimelineContent().getChildren());

        card.getChildren().addAll(header, recentTimelineBox);
        return card;
    }

    private VBox createTimelineContent() {
        VBox timeline = new VBox(14);

        if (recentResults.isEmpty()) {
            VBox empty = new VBox(10);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(34));

            Label icon = new Label("🌱");
            icon.setFont(Font.font("Segoe UI Emoji", 44));

            Label text = new Label("No recent assessments yet. Start your first assessment today!");
            text.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
            text.setTextFill(MUTED);

            Button start = createPrimaryButton("Take Assessment");
            start.setOnAction(e -> parentApp.showTakeAssessmentPanel());

            empty.getChildren().addAll(icon, text, start);
            timeline.getChildren().add(empty);
            return timeline;
        }

        int max = Math.min(5, recentResults.size());
        for (int i = 0; i < max; i++) {
            timeline.getChildren().add(createTimelineItem(recentResults.get(i)));
        }

        return timeline;
    }

    private HBox createTimelineItem(ResultRow result) {
        HBox item = new HBox(14);
        item.setAlignment(Pos.TOP_LEFT);
        item.setPadding(new Insets(14));
        item.setStyle(
                "-fx-background-color: #F8F9FA;" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-color: #E9ECEF;" +
                        "-fx-border-radius: 16;"
        );

        StackPane iconCircle = new StackPane();
        iconCircle.setPrefSize(42, 42);
        iconCircle.setMaxSize(42, 42);
        iconCircle.setStyle(
                "-fx-background-color: " + riskColor(result.getRiskLevel()) + ";" +
                        "-fx-background-radius: 999;"
        );

        Label icon = new Label("📋");
        icon.setFont(Font.font("Segoe UI Emoji", 18));
        iconCircle.getChildren().add(icon);

        VBox content = new VBox(7);
        Label title = new Label(result.getAssessmentTitle());
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        title.setTextFill(INK);

        Label date = new Label(result.getTakenAt());
        date.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        date.setTextFill(MUTED);

        HBox badges = new HBox(8);
        badges.getChildren().addAll(
                smallBadge("Score: " + result.getTotalScore(), Color.web("#6C757D")),
                smallBadge(result.getRiskLevel(), Color.web(riskColor(result.getRiskLevel())))
        );

        content.getChildren().addAll(title, date, badges);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button view = createOutlineButton("View →");
        view.setOnAction(e -> parentApp.showResultsPanel());

        item.getChildren().addAll(iconCircle, content, spacer, view);
        return item;
    }

    private Label smallBadge(String text, Color color) {
        Label badge = new Label(text);
        badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        badge.setTextFill(Color.WHITE);
        badge.setPadding(new Insets(5, 10, 5, 10));
        badge.setStyle("-fx-background-color: " + css(color) + "; -fx-background-radius: 999;");
        return badge;
    }

    private Label smallPillText(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        label.setTextFill(TEXT);
        return label;
    }

    private Button createPrimaryButton(String text) {
        Button button = new Button(text + "  →");
        button.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 13));
        button.setTextFill(Color.WHITE);
        button.setCursor(Cursor.HAND);
        button.setPadding(new Insets(10, 18, 10, 18));
        button.setStyle(
                "-fx-background-color: linear-gradient(to right, #50C878, #2E7D32);" +
                        "-fx-background-radius: 999;" +
                        "-fx-effect: dropshadow(gaussian, rgba(80,200,120,0.28), 16, 0, 0, 6);"
        );
        addButtonPress(button);
        return button;
    }

    private Button createOutlineButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        button.setTextFill(PRIMARY_DARK);
        button.setCursor(Cursor.HAND);
        button.setPadding(new Insets(8, 14, 8, 14));
        button.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-color: #C8E6D2;" +
                        "-fx-border-radius: 999;"
        );
        return button;
    }

    private Button createGlassButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 13));
        button.setTextFill(PRIMARY_DARK);
        button.setCursor(Cursor.HAND);
        button.setPadding(new Insets(11, 22, 11, 22));
        button.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 999;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.16), 12, 0, 0, 4);"
        );
        addButtonPress(button);
        return button;
    }

    private void addButtonPress(Button button) {
        button.setOnMousePressed(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(80), button);
            st.setToX(0.96);
            st.setToY(0.96);
            st.play();
        });

        button.setOnMouseReleased(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(110), button);
            st.setToX(1);
            st.setToY(1);
            st.play();
        });
    }

    private String cardStyle(int radius) {
        return "-fx-background-color: white;" +
                "-fx-background-radius: " + radius + ";" +
                "-fx-border-color: rgba(255,255,255,0.75);" +
                "-fx-border-radius: " + radius + ";" +
                "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.08), 24, 0, 0, 9);";
    }

    private void addHoverScale(Node node) {
        node.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(160), node);
            st.setToX(1.04);
            st.setToY(1.04);
            st.play();
        });

        node.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(160), node);
            st.setToX(1);
            st.setToY(1);
            st.play();
        });
    }

    private void playEntranceAnimation() {
        setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(700), this);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void openFaceIdRegistration() {
        try {
            FaceIDDialog dialog = new FaceIDDialog(parentApp, true, userId);
            dialog.setOnHidden(e -> refreshData());
            dialog.show();
        } catch (Exception e) {
            showInfo("Could not open Face ID registration: " + e.getMessage());
        }
    }

    private void disableFaceId() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Disable Face ID");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to disable Face ID?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean ok = userservice.disableFaceID(userId);
                if (ok) {
                    showInfo("Face ID disabled.");
                    refreshData();
                } else {
                    showInfo("Could not disable Face ID.");
                }
            }
        });
    }

    private void loadDashboardData() {
        currentUser = userId > 0 ? userservice.getuserById(userId) : null;
        recentResults.clear();

        assessmentsTaken = 0;
        averageScore = 0;
        latestRisk = "N/A";

        loadResultsFromDatabase();

        badgesEarned = calculateEarnedBadges();
        totalXP = badgesEarned * 100 + assessmentsTaken * 50;
    }

    private void loadResultsFromDatabase() {
        if (userId <= 0) return;

        String sql =
                "SELECT r.result_id, r.assessment_id, r.total_score, r.risk_level, r.taken_at, " +
                        "a.title AS assessment_title " +
                        "FROM assessment_result r " +
                        "LEFT JOIN assessment a ON r.assessment_id = a.assessment_id " +
                        "WHERE r.user_id = ? " +
                        "ORDER BY r.taken_at DESC";

        int scoreSum = 0;

        try (Connection conn = DatabaseConnectionMentis.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ResultRow row = new ResultRow(
                        rs.getInt("result_id"),
                        safeDefault(rs.getString("assessment_title"), "Assessment"),
                        rs.getInt("total_score"),
                        safeDefault(rs.getString("risk_level"), "N/A"),
                        safeDefault(rs.getString("taken_at"), "N/A")
                );

                recentResults.add(row);
                scoreSum += row.getTotalScore();
            }

            assessmentsTaken = recentResults.size();
            if (assessmentsTaken > 0) {
                averageScore = Math.round((float) scoreSum / assessmentsTaken);
                latestRisk = recentResults.get(0).getRiskLevel();
            }

        } catch (SQLException e) {
            System.err.println("Could not load patient dashboard results: " + e.getMessage());
        }
    }

    private int calculateEarnedBadges() {
        int count = 0;
        if (assessmentsTaken >= 1) count++;
        if (assessmentsTaken >= 3) count++;
        if (assessmentsTaken >= 5) count++;
        if (assessmentsTaken >= 7) count++;
        if (averageScore > 0) count++;
        if (latestRisk != null && !"N/A".equalsIgnoreCase(latestRisk)) count++;
        if (totalXP >= 500) count++;
        if (userservice.hasFaceEnabled(userId)) count++;
        return Math.min(totalBadges, count);
    }

    private String getFirstName() {
        if (currentUser != null && currentUser.getFirstname() != null && !currentUser.getFirstname().trim().isEmpty()) {
            return currentUser.getFirstname();
        }

        if (userName != null && !userName.trim().isEmpty()) {
            return userName.split(" ")[0];
        }

        return "Patient";
    }

    private String getFullName() {
        if (currentUser != null) {
            String name = safe(currentUser.getFirstname()) + " " + safe(currentUser.getLastname());
            if (!name.trim().isEmpty()) return name.trim();
        }

        return userName == null || userName.trim().isEmpty() ? "Patient" : userName;
    }

    private String getInitials() {
        String first = "";
        String last = "";

        if (currentUser != null) {
            first = safe(currentUser.getFirstname());
            last = safe(currentUser.getLastname());
        } else if (userName != null) {
            String[] parts = userName.trim().split(" ");
            if (parts.length > 0) first = parts[0];
            if (parts.length > 1) last = parts[1];
        }

        String initials = "";
        if (!first.isEmpty()) initials += first.substring(0, 1);
        if (!last.isEmpty()) initials += last.substring(0, 1);

        return initials.isEmpty() ? "P" : initials.toUpperCase();
    }

    private String getBirthDate() {
        if (currentUser == null || currentUser.getDateofbirth() == null) return "N/A";
        return currentUser.getDateofbirth().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private String getAgeText() {
        if (currentUser == null || currentUser.getDateofbirth() == null) return "N/A";
        int age = Period.between(currentUser.getDateofbirth(), LocalDate.now()).getYears();
        return age + " years";
    }

    private String riskColor(String risk) {
        if (risk == null) return "#50C878";
        String r = risk.toLowerCase();

        if (r.contains("high") || r.contains("severe")) return "#E74C3C";
        if (r.contains("moderate") || r.contains("mild")) return "#F1C40F";
        return "#50C878";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String safeDefault(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private String css(Color color) {
        return String.format("#%02x%02x%02x",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mentis");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static class BadgeInfo {
        String name;
        String description;
        String icon;
        Color color;
        boolean earned;
        int progress;

        BadgeInfo(String name, String description, String icon, Color color, boolean earned, int progress) {
            this.name = name;
            this.description = description;
            this.icon = icon;
            this.color = color;
            this.earned = earned;
            this.progress = progress;
        }
    }

    public static class ResultRow {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty assessmentTitle;
        private final SimpleIntegerProperty totalScore;
        private final SimpleStringProperty riskLevel;
        private final SimpleStringProperty takenAt;

        public ResultRow(int id, String assessmentTitle, int totalScore, String riskLevel, String takenAt) {
            this.id = new SimpleIntegerProperty(id);
            this.assessmentTitle = new SimpleStringProperty(assessmentTitle);
            this.totalScore = new SimpleIntegerProperty(totalScore);
            this.riskLevel = new SimpleStringProperty(riskLevel);
            this.takenAt = new SimpleStringProperty(takenAt);
        }

        public int getId() { return id.get(); }
        public String getAssessmentTitle() { return assessmentTitle.get(); }
        public int getTotalScore() { return totalScore.get(); }
        public String getRiskLevel() { return riskLevel.get(); }
        public String getTakenAt() { return takenAt.get(); }

        public SimpleIntegerProperty idProperty() { return id; }
        public SimpleStringProperty assessmentTitleProperty() { return assessmentTitle; }
        public SimpleIntegerProperty totalScoreProperty() { return totalScore; }
        public SimpleStringProperty riskLevelProperty() { return riskLevel; }
        public SimpleStringProperty takenAtProperty() { return takenAt; }
    }
}
