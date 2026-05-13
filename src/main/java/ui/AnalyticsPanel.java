package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import services.AnalyticsService;

import java.util.LinkedHashMap;
import java.util.Map;

public class AnalyticsPanel extends VBox {

    private MentisLoginFrame parentApp;
    private AnalyticsService analyticsService;
    private VBox contentContainer;
    private ScrollPane scrollPane;

    // Symfony-style green colors
    private static final Color PAGE_BG = Color.web("#F8F9FA");
    private static final Color SOFT_GREEN_BG = Color.web("#F1F8E9");
    private static final Color EMERALD = Color.web("#50C878");
    private static final Color EMERALD_DARK = Color.web("#2E7D32");
    private static final Color EMERALD_MID = Color.web("#3A9B5E");
    private static final Color INK = Color.web("#1A3C34");
    private static final Color MUTED = Color.web("#6C757D");
    private static final Color LINE = Color.web("#E9ECEF");

    // Chart colors
    private static final Color CHART_COLOR_1 = Color.web("#3498DB");
    private static final Color CHART_COLOR_2 = Color.web("#2ECC71");
    private static final Color CHART_COLOR_3 = Color.web("#9B59B6");
    private static final Color CHART_COLOR_4 = Color.web("#F1C40F");
    private static final Color CHART_COLOR_5 = Color.web("#E67E22");
    private static final Color CHART_COLOR_6 = Color.web("#E74C3C");

    public AnalyticsPanel(MentisLoginFrame parentApp) {
        this.parentApp = parentApp;
        this.analyticsService = new AnalyticsService();

        setStyle("-fx-background-color: " + cssColor(PAGE_BG) + ";");
        setPadding(new Insets(44, 56, 44, 56));
        setSpacing(28);

        createHeader();
        createContent();
        refreshData();
    }

    private void createHeader() {
        VBox headerBox = new VBox(8);
        headerBox.setPadding(new Insets(0, 0, 24, 0));

        Label titleLabel = new Label("📊 Session Analytics");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 40));
        titleLabel.setTextFill(EMERALD_DARK);

        Label subtitleLabel = new Label("Insights and statistics about your sessions");
        subtitleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        subtitleLabel.setTextFill(MUTED);

        HBox userBar = new HBox();
        userBar.setAlignment(Pos.CENTER_RIGHT);
        userBar.setPadding(new Insets(16, 0, 0, 0));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userInfoLabel = new Label(parentApp.getUserName());
        userInfoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        userInfoLabel.setTextFill(MUTED);

        userBar.getChildren().addAll(spacer, userInfoLabel);

        headerBox.getChildren().addAll(titleLabel, subtitleLabel, userBar);
        getChildren().add(headerBox);
    }

    private void createContent() {
        contentContainer = new VBox(28);
        contentContainer.setFillWidth(true);

        scrollPane = new ScrollPane(contentContainer);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setBorder(null);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        getChildren().add(scrollPane);
    }

    private VBox createStatCard(String title, String value, String subtitle) {
        VBox card = new VBox(14);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20px;" +
                        "-fx-padding: 28px 20px;" +
                        cardShadow()
        );
        card.setPrefWidth(260);
        card.setMinWidth(220);
        card.setMinHeight(140);
        HBox.setHgrow(card, Priority.ALWAYS);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        titleLabel.setTextFill(MUTED);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 42));
        valueLabel.setTextFill(EMERALD_DARK);

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        subtitleLabel.setTextFill(MUTED);

        card.getChildren().addAll(titleLabel, valueLabel, subtitleLabel);
        return card;
    }

    private VBox createChartCard(String title, Map<String, Integer> data) {
        VBox card = new VBox(20);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20px;" +
                        "-fx-padding: 28px;" +
                        cardShadow()
        );
        card.setMinHeight(360);
        card.setPrefWidth(600);
        HBox.setHgrow(card, Priority.ALWAYS);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titleLabel.setTextFill(INK);

        card.getChildren().add(titleLabel);

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: " + cssColor(LINE) + ";");
        card.getChildren().add(separator);

        if (data == null || data.isEmpty()) {
            VBox emptyBox = new VBox(12);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(40, 0, 40, 0));

            Label emptyIcon = new Label("📭");
            emptyIcon.setFont(Font.font("Segoe UI Emoji", 44));

            Label noDataLabel = new Label("No data available yet");
            noDataLabel.setFont(Font.font("Segoe UI", 14));
            noDataLabel.setTextFill(MUTED);

            emptyBox.getChildren().addAll(emptyIcon, noDataLabel);
            card.getChildren().add(emptyBox);
            return card;
        }

        int maxValue = data.values().stream().max(Integer::compare).orElse(1);
        int colorIndex = 0;

        VBox chartContent = new VBox(12);
        chartContent.setFillWidth(true);

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            VBox rowContainer = new VBox(6);
            rowContainer.setPadding(new Insets(6, 0, 6, 0));
            rowContainer.setFillWidth(true);

            HBox headerRow = new HBox(15);
            headerRow.setAlignment(Pos.CENTER_LEFT);
            headerRow.setFillHeight(true);

            Label colorDot = new Label("●");
            colorDot.setFont(Font.font("Segoe UI", 18));
            Color dotColor = getColorByIndex(colorIndex++);
            colorDot.setTextFill(dotColor);
            colorDot.setPrefWidth(25);

            Label keyLabel = new Label(entry.getKey());
            keyLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
            keyLabel.setTextFill(INK);
            keyLabel.setPrefWidth(220);
            keyLabel.setWrapText(true);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label valueLabel = new Label(String.valueOf(entry.getValue()));
            valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
            valueLabel.setTextFill(EMERALD_DARK);
            valueLabel.setPrefWidth(50);
            valueLabel.setAlignment(Pos.CENTER_RIGHT);

            headerRow.getChildren().addAll(colorDot, keyLabel, spacer, valueLabel);

            double progress = entry.getValue() / (double) maxValue;
            ProgressBar bar = new ProgressBar(progress);
            bar.setPrefWidth(Double.MAX_VALUE);
            bar.setPrefHeight(12);
            bar.setStyle(
                    "-fx-accent: " + cssColor(dotColor) + ";" +
                            "-fx-background-radius: 999px;" +
                            "-fx-control-inner-background: " + cssColor(LINE) + ";"
            );

            rowContainer.getChildren().addAll(headerRow, bar);
            chartContent.getChildren().add(rowContainer);
        }

        card.getChildren().add(chartContent);
        return card;
    }

    private VBox createCompletionRateCard(double rate) {
        VBox card = new VBox(20);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20px;" +
                        "-fx-padding: 28px;" +
                        cardShadow()
        );
        card.setAlignment(Pos.CENTER);
        card.setMinHeight(360);
        card.setPrefWidth(600);
        HBox.setHgrow(card, Priority.ALWAYS);

        Label titleLabel = new Label("📊 Completion Rate");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titleLabel.setTextFill(INK);

        StackPane progressPane = new StackPane();
        progressPane.setAlignment(Pos.CENTER);
        progressPane.setPadding(new Insets(20, 0, 20, 0));

        ProgressIndicator progressIndicator = new ProgressIndicator(rate / 100.0);
        progressIndicator.setPrefSize(160, 160);
        progressIndicator.setStyle("-fx-progress-color: " + cssColor(EMERALD) + ";");

        Label percentageLabel = new Label(String.format("%.1f%%", rate));
        percentageLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        percentageLabel.setTextFill(EMERALD_DARK);

        progressPane.getChildren().addAll(progressIndicator, percentageLabel);

        Label subtitleLabel = new Label("Percentage of sessions completed out of all booked");
        subtitleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        subtitleLabel.setTextFill(MUTED);
        subtitleLabel.setWrapText(true);
        subtitleLabel.setAlignment(Pos.CENTER);

        card.getChildren().addAll(titleLabel, progressPane, subtitleLabel);
        return card;
    }

    private Color getColorByIndex(int index) {
        Color[] colors = {CHART_COLOR_1, CHART_COLOR_2, CHART_COLOR_3, CHART_COLOR_4, CHART_COLOR_5, CHART_COLOR_6};
        return colors[index % colors.length];
    }

    public void refreshData() {
        contentContainer.getChildren().clear();

        try {
            // Summary stats row
            AnalyticsService.AnalyticsSummary summary = analyticsService.getSummaryStats();

            HBox statsRow = new HBox(20);
            statsRow.setAlignment(Pos.CENTER);
            statsRow.setPadding(new Insets(0, 0, 15, 0));

            statsRow.getChildren().addAll(
                    createStatCard("Total Sessions", String.valueOf(summary.getTotalSessions()), "All time"),
                    createStatCard("Reserved Sessions", String.valueOf(summary.getTotalReserved()), "Booked"),
                    createStatCard("Unique Patients", String.valueOf(summary.getUniquePatients()), "Active patients"),
                    createStatCard("Avg Bookings", String.format("%.1f", summary.getAvgBookingsPerPatient()), "Per patient")
            );

            contentContainer.getChildren().add(statsRow);

            // Single column layout for better readability
            // Row 1: Session Types
            Map<String, Integer> typeData = analyticsService.getPopularSessionTypes();
            VBox typeCard = createChartCard("📊 Most Popular Session Types", typeData);
            contentContainer.getChildren().add(typeCard);

            // Row 2: Popular Locations
            Map<String, Integer> locationData = analyticsService.getPopularLocations();
            VBox locationCard = createChartCard("📍 Popular Locations", locationData);
            contentContainer.getChildren().add(locationCard);

            // Row 3: Most Booked Sessions
            Map<String, Integer> titleData = analyticsService.getPopularTitles();
            VBox titleCard = createChartCard("🔥 Most Booked Sessions", titleData);
            contentContainer.getChildren().add(titleCard);

            // Row 4: Session Status Distribution
            Map<String, Integer> statusData = analyticsService.getStatusDistribution();
            VBox statusCard = createChartCard("📌 Session Status Distribution", statusData);
            contentContainer.getChildren().add(statusCard);

            // Row 5: Bookings by Day of Week
            Map<String, Integer> dayData = analyticsService.getBookingsByDayOfWeek();
            VBox dayCard = createChartCard("📅 Bookings by Day of Week", dayData);
            contentContainer.getChildren().add(dayCard);

            // Row 6: Bookings by Hour
            Map<String, Integer> hourData = analyticsService.getBookingsByHour();
            VBox hourCard = createChartCard("⏰ Bookings by Hour", hourData);
            contentContainer.getChildren().add(hourCard);

            // Row 7: Monthly Trends
            Map<String, Integer> monthData = analyticsService.getMonthlyTrends();
            VBox monthCard = createChartCard("📈 Monthly Trends", monthData);
            contentContainer.getChildren().add(monthCard);

            // Row 8: Top Patients
            Map<String, Integer> patientData = analyticsService.getTopPatients();
            VBox patientCard = createChartCard("👥 Top Patients", patientData);
            contentContainer.getChildren().add(patientCard);

            // Row 9: Completion Rate
            double completionRate = analyticsService.getCompletionRate();
            VBox rateCard = createCompletionRateCard(completionRate);
            contentContainer.getChildren().add(rateCard);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load analytics: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String cssColor(Color color) {
        return "#" + toHex(color);
    }

    private String cardShadow() {
        return "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 5);";
    }

    private String toHex(Color color) {
        return String.format("%02x%02x%02x",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }
}