package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import services.AnalyticsService;

import java.util.Map;

public class AnalyticsPanel extends VBox {

    private MentisLoginFrame parentApp;
    private AnalyticsService analyticsService;
    private VBox contentContainer;
    private ScrollPane scrollPane;

    private static final Color BACKGROUND_LIGHT = Color.rgb(240, 248, 245);
    private static final Color ACCENT_DARK_GREEN = Color.rgb(60, 120, 90);
    private static final Color CARD_WHITE = Color.WHITE;
    private static final Color TEXT_DARK = Color.rgb(40, 70, 50);
    private static final Color TEXT_LIGHT = Color.rgb(100, 130, 110);
    private static final Color BORDER_LIGHT = Color.rgb(200, 220, 210);
    private static final Color CHART_COLOR_1 = Color.rgb(52, 152, 219);  // Blue
    private static final Color CHART_COLOR_2 = Color.rgb(46, 204, 113);  // Green
    private static final Color CHART_COLOR_3 = Color.rgb(155, 89, 182);  // Purple
    private static final Color CHART_COLOR_4 = Color.rgb(241, 196, 15);  // Yellow
    private static final Color CHART_COLOR_5 = Color.rgb(230, 126, 34);  // Orange

    public AnalyticsPanel(MentisLoginFrame parentApp) {
        this.parentApp = parentApp;
        this.analyticsService = new AnalyticsService();

        setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        setPadding(new Insets(30));
        setSpacing(20);

        createHeader();
        createContent();
        refreshData();
    }

    private void createHeader() {
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 20, 0));

        VBox titleBox = new VBox(10);
        Label titleLabel = new Label("Session Analytics");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        Label subtitleLabel = new Label("Insights and statistics about your sessions");
        subtitleLabel.setFont(Font.font("Segoe UI", 16));
        subtitleLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userInfoLabel = new Label(parentApp.getUserName());
        userInfoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        userInfoLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        headerBox.getChildren().addAll(titleBox, spacer, userInfoLabel);
        getChildren().add(headerBox);
    }

    private void createContent() {
        contentContainer = new VBox(20);
        contentContainer.setFillWidth(true);

        scrollPane = new ScrollPane(contentContainer);
        scrollPane.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        scrollPane.setBorder(null);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        getChildren().add(scrollPane);
    }

    private VBox createStatCard(String title, String value, String subtitle) {
        VBox card = new VBox(10);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 20;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);"
        );
        card.setPrefWidth(200);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        titleLabel.setTextFill(Color.web(toHex(TEXT_LIGHT)));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        valueLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        subtitleLabel.setTextFill(Color.web(toHex(TEXT_LIGHT)));

        card.getChildren().addAll(titleLabel, valueLabel, subtitleLabel);
        return card;
    }

    private VBox createChartCard(String title, Map<String, Integer> data, Color... colors) {
        VBox card = new VBox(15);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 20;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);"
        );

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        card.getChildren().add(titleLabel);

        if (data.isEmpty()) {
            Label noDataLabel = new Label("No data available");
            noDataLabel.setFont(Font.font("Segoe UI", 14));
            noDataLabel.setTextFill(Color.web(toHex(TEXT_LIGHT)));
            card.getChildren().add(noDataLabel);
            return card;
        }

        int maxValue = data.values().stream().max(Integer::compare).orElse(1);
        int colorIndex = 0;

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);

            // Color indicator
            Label colorDot = new Label("●");
            colorDot.setFont(Font.font("Segoe UI", 20));
            Color dotColor = getColorByIndex(colorIndex++, colors);
            colorDot.setTextFill(dotColor);

            // Label
            Label keyLabel = new Label(entry.getKey());
            keyLabel.setFont(Font.font("Segoe UI", 14));
            keyLabel.setTextFill(Color.web(toHex(TEXT_DARK)));
            keyLabel.setPrefWidth(150);

            // Bar
            int barLength = (int) ((entry.getValue() * 200.0) / maxValue);
            ProgressBar bar = new ProgressBar(entry.getValue() / (double) maxValue);
            bar.setPrefWidth(200);
            bar.setStyle("-fx-accent: #" + toHex(dotColor) + ";");

            // Value
            Label valueLabel = new Label(String.valueOf(entry.getValue()));
            valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            valueLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));
            valueLabel.setPrefWidth(40);

            row.getChildren().addAll(colorDot, keyLabel, bar, valueLabel);
            card.getChildren().add(row);
        }

        return card;
    }

    private Color getColorByIndex(int index, Color... colors) {
        if (colors != null && colors.length > 0) {
            return colors[index % colors.length];
        }
        Color[] defaultColors = {CHART_COLOR_1, CHART_COLOR_2, CHART_COLOR_3, CHART_COLOR_4, CHART_COLOR_5};
        return defaultColors[index % defaultColors.length];
    }

    public void refreshData() {
        contentContainer.getChildren().clear();

        try {
            // Summary stats row
            AnalyticsService.AnalyticsSummary summary = analyticsService.getSummaryStats();

            HBox statsRow = new HBox(20);
            statsRow.setAlignment(Pos.CENTER);
            statsRow.setPadding(new Insets(0, 0, 20, 0));

            statsRow.getChildren().addAll(
                    createStatCard("Total Sessions", String.valueOf(summary.getTotalSessions()), "All time"),
                    createStatCard("Reserved Sessions", String.valueOf(summary.getTotalReserved()), "Booked"),
                    createStatCard("Unique Patients", String.valueOf(summary.getUniquePatients()), "Active patients"),
                    createStatCard("Avg Bookings", String.format("%.1f", summary.getAvgBookingsPerPatient()), "Per patient")
            );

            contentContainer.getChildren().add(statsRow);

            // Popular Session Types
            Map<String, Integer> typeData = analyticsService.getPopularSessionTypes();
            VBox typeCard = createChartCard("📊 Most Popular Session Types", typeData);
            contentContainer.getChildren().add(typeCard);

            // Popular Titles
            Map<String, Integer> titleData = analyticsService.getPopularTitles();
            VBox titleCard = createChartCard("🔥 Most Booked Sessions", titleData);
            contentContainer.getChildren().add(titleCard);

            // Popular Locations
            Map<String, Integer> locationData = analyticsService.getPopularLocations();
            VBox locationCard = createChartCard("📍 Popular Locations", locationData);
            contentContainer.getChildren().add(locationCard);

            // Bookings by Day of Week
            Map<String, Integer> dayData = analyticsService.getBookingsByDayOfWeek();
            VBox dayCard = createChartCard("📅 Bookings by Day of Week", dayData);
            contentContainer.getChildren().add(dayCard);

            // Bookings by Hour
            Map<String, Integer> hourData = analyticsService.getBookingsByHour();
            VBox hourCard = createChartCard("⏰ Bookings by Hour", hourData);
            contentContainer.getChildren().add(hourCard);

            // Monthly Trends
            Map<String, Integer> monthData = analyticsService.getMonthlyTrends();
            VBox monthCard = createChartCard("📈 Monthly Trends", monthData);
            contentContainer.getChildren().add(monthCard);

            // Top Patients
            Map<String, Integer> patientData = analyticsService.getTopPatients();
            VBox patientCard = createChartCard("👥 Top Patients (Most Bookings)", patientData);
            contentContainer.getChildren().add(patientCard);

            // Status Distribution
            Map<String, Integer> statusData = analyticsService.getStatusDistribution();
            VBox statusCard = createChartCard("📌 Session Status Distribution", statusData);
            contentContainer.getChildren().add(statusCard);

            // Completion Rate
            double completionRate = analyticsService.getCompletionRate();
            VBox rateCard = createStatCard("📊 Completion Rate",
                    String.format("%.1f%%", completionRate),
                    "Percentage of sessions booked");
            rateCard.setPrefWidth(Double.MAX_VALUE);
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

    private String toHex(Color color) {
        return String.format("%02x%02x%02x",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}