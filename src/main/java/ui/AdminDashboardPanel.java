package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDashboardPanel extends VBox {

    private MentisLoginFrame parentApp;
    private int psychologistCount;
    private int patientCount;

    // Color constants
    private static final Color BACKGROUND_LIGHT = Color.rgb(245, 248, 246);
    private static final Color CARD_WHITE = Color.WHITE;
    private static final Color ACCENT_GREEN = Color.rgb(60, 110, 90);
    private static final Color PSYCHOLOGIST_COLOR = Color.rgb(90, 150, 230);
    private static final Color PATIENT_COLOR = Color.rgb(100, 180, 120);
    private static final Color TEXT_DARK = Color.rgb(60, 110, 90);
    private static final Color TEXT_GRAY = Color.GRAY;
    private static final Color BORDER_LIGHT = Color.rgb(220, 220, 220);

    public AdminDashboardPanel(MentisLoginFrame parentApp) {
        this.parentApp = parentApp;

        setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        setPadding(new Insets(0));
        setSpacing(0);

        // Load statistics
        loadStatistics();

        // Create main content
        VBox mainContent = createMainContent();
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        getChildren().add(mainContent);
    }

    // ================= MAIN CONTENT =================
    private VBox createMainContent() {
        VBox mainContent = new VBox();
        mainContent.setStyle("-fx-background-color: transparent;");

        mainContent.getChildren().addAll(
                createHeader(),
                createCenterContent()
        );

        return mainContent;
    }

    // ================= HEADER =================
    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white;");
        header.setPadding(new Insets(20, 30, 20, 30));
        header.setSpacing(20);

        Label title = new Label("Dashboard");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        title.setTextFill(Color.web(toHex(TEXT_DARK)));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField search = new TextField();
        search.setPromptText("Search...");
        search.setPrefWidth(220);
        search.setPrefHeight(35);
        search.setStyle(
                "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8 12;"
        );

        header.getChildren().addAll(title, spacer, search);
        return header;
    }

    // ================= CENTER =================
    private VBox createCenterContent() {
        VBox center = new VBox(20);
        center.setStyle("-fx-background-color: transparent;");
        center.setPadding(new Insets(20, 30, 30, 30));

        center.getChildren().addAll(
                createStatsPanel(),
                createChartSection()
        );

        VBox.setVgrow(center.getChildren().get(1), Priority.ALWAYS);

        return center;
    }

    // ================= STATS =================
    private HBox createStatsPanel() {
        HBox stats = new HBox(20);
        stats.setStyle("-fx-background-color: transparent;");
        stats.setAlignment(Pos.CENTER);

        // Create stat cards
        VBox psychologistCard = createStatCard(
                "Psychologists",
                psychologistCount,
                PSYCHOLOGIST_COLOR,
                () -> parentApp.showPsychologistTablePanel()
        );

        VBox patientCard = createStatCard(
                "Patients",
                patientCount,
                PATIENT_COLOR,
                () -> parentApp.showPatientTablePanel()
        );

        // Make cards expand equally
        HBox.setHgrow(psychologistCard, Priority.ALWAYS);
        HBox.setHgrow(patientCard, Priority.ALWAYS);

        stats.getChildren().addAll(psychologistCard, patientCard);
        return stats;
    }

    private VBox createStatCard(String title, int value, Color color, Runnable action) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefHeight(180);

        // Add hover effect
        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);")
        );
        card.setOnMouseExited(e ->
                card.setStyle("-fx-background-color: white; -fx-background-radius: 10;")
        );

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        titleLabel.setTextFill(Color.web(toHex(TEXT_GRAY)));

        Label numberLabel = new Label(String.valueOf(value));
        numberLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        numberLabel.setTextFill(Color.web(toHex(color)));

        Button viewButton = new Button("View list");
        viewButton.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        viewButton.setTextFill(Color.WHITE);
        viewButton.setStyle(
                "-fx-background-color: #" + toHex(color) + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8 16;" +
                        "-fx-cursor: hand;"
        );

        // Button hover effect
        viewButton.setOnMouseEntered(e ->
                viewButton.setStyle(
                        "-fx-background-color: #" + toHex(color.darker()) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 8 16;" +
                                "-fx-cursor: hand;"
                )
        );
        viewButton.setOnMouseExited(e ->
                viewButton.setStyle(
                        "-fx-background-color: #" + toHex(color) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 8 16;" +
                                "-fx-cursor: hand;"
                )
        );

        viewButton.setOnAction(e -> action.run());

        card.getChildren().addAll(titleLabel, numberLabel, viewButton);
        return card;
    }

    // ================= CHART SECTION =================
    private VBox createChartSection() {
        VBox chartContainer = new VBox(10);
        chartContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        chartContainer.setPadding(new Insets(20));
        chartContainer.setPrefHeight(300);

        Label chartTitle = new Label("User Distribution");
        chartTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        chartTitle.setTextFill(Color.web(toHex(TEXT_DARK)));

        // Create bar chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Count");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Psychologists vs Patients");
        barChart.setLegendVisible(false);
        barChart.setAnimated(true);
        barChart.setStyle("-fx-background-color: transparent;");
        barChart.setPrefHeight(250);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Psychologists", psychologistCount));
        series.getData().add(new XYChart.Data<>("Patients", patientCount));

        barChart.getData().add(series);

        // Style the bars
        for (XYChart.Data<String, Number> data : series.getData()) {
            if (data.getXValue().equals("Psychologists")) {
                data.getNode().setStyle("-fx-bar-fill: #" + toHex(PSYCHOLOGIST_COLOR) + ";");
            } else {
                data.getNode().setStyle("-fx-bar-fill: #" + toHex(PATIENT_COLOR) + ";");
            }

            // Add value labels on bars
            data.getNode().setOnMouseEntered(null); // Remove default tooltip
        }

        chartContainer.getChildren().addAll(chartTitle, barChart);
        VBox.setVgrow(barChart, Priority.ALWAYS);

        return chartContainer;
    }

    // ================= CUSTOM CANVAS CHART (Alternative to BarChart) =================
    private VBox createCustomChart() {
        VBox container = new VBox(10);
        container.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        container.setPadding(new Insets(20));
        container.setPrefHeight(300);

        Label title = new Label("User Distribution");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setTextFill(Color.web(toHex(TEXT_DARK)));

        Canvas canvas = new Canvas(600, 200);
        drawChart(canvas.getGraphicsContext2D());

        container.getChildren().addAll(title, canvas);
        return container;
    }

    private void drawChart(GraphicsContext gc) {
        int max = Math.max(psychologistCount, patientCount);
        if (max == 0) max = 1;

        int baseY = 180;
        int barWidth = 80;
        int maxBarHeight = 120;

        // Clear canvas
        gc.clearRect(0, 0, 600, 200);

        // Draw Psychologists bar
        gc.setFill(PSYCHOLOGIST_COLOR);
        int p1Height = psychologistCount * maxBarHeight / max;
        gc.fillRect(150, baseY - p1Height, barWidth, p1Height);

        // Draw Patients bar
        gc.setFill(PATIENT_COLOR);
        int p2Height = patientCount * maxBarHeight / max;
        gc.fillRect(300, baseY - p2Height, barWidth, p2Height);

        // Draw labels
        gc.setFill(Color.BLACK);
        gc.setFont(javafx.scene.text.Font.font("Segoe UI", 14));
        gc.fillText("Psychologists", 150, baseY + 30);
        gc.fillText("Patients", 300, baseY + 30);

        // Draw value labels
        gc.setFill(Color.BLACK);
        gc.fillText(String.valueOf(psychologistCount), 150 + barWidth/2 - 10, baseY - p1Height - 10);
        gc.fillText(String.valueOf(patientCount), 300 + barWidth/2 - 10, baseY - p2Height - 10);
    }

    // ================= DATABASE =================
    private void loadStatistics() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            psychologistCount = getCount(conn, "psychologist");
            patientCount = getCount(conn, "Patient");
        } catch (SQLException e) {
            e.printStackTrace();
            // Set default values on error
            psychologistCount = 0;
            patientCount = 0;
        }
    }

    private int getCount(Connection conn, String type) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user WHERE type = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // ================= REFRESH =================
    public void refreshData() {
        loadStatistics();

        // Rebuild stats panel
        VBox centerContent = (VBox) getChildren().get(0);
        VBox center = (VBox) centerContent.getChildren().get(1);

        // Update stats panel
        HBox statsPanel = (HBox) center.getChildren().get(0);
        statsPanel.getChildren().clear();
        statsPanel.getChildren().addAll(
                createStatCard("Psychologists", psychologistCount, PSYCHOLOGIST_COLOR,
                        () -> parentApp.showPsychologistTablePanel()),
                createStatCard("Patients", patientCount, PATIENT_COLOR,
                        () -> parentApp.showPatientTablePanel())
        );

        // Update chart
        VBox chartSection = (VBox) center.getChildren().get(1);
        BarChart<String, Number> barChart = (BarChart<String, Number>) chartSection.getChildren().get(1);
        XYChart.Series<String, Number> series = barChart.getData().get(0);
        series.getData().get(0).setYValue(psychologistCount);
        series.getData().get(1).setYValue(patientCount);

        // Update bar colors
        series.getData().get(0).getNode().setStyle("-fx-bar-fill: #" + toHex(PSYCHOLOGIST_COLOR) + ";");
        series.getData().get(1).getNode().setStyle("-fx-bar-fill: #" + toHex(PATIENT_COLOR) + ";");
    }

    // ================= UTILITY =================
    private String toHex(Color color) {
        return String.format("%02x%02x%02x",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }
}