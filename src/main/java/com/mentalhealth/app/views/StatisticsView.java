package com.mentalhealth.app.views;
 
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import services.GoalService;
import services.MoodService;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.scene.Node;
import javafx.application.Platform;
 
public class StatisticsView {
 
    private static final String PRIMARY = "#50C878";
    private static final String PRIMARY_DARK = "#1B5E20";
    private static final String BORDER = "#E0E0E0";
    private static final String INK = "#1A3C34";
    private static final String MUTED = "#757575";
 
    private final MoodService moodService = new MoodService();
    private final GoalService goalService = new GoalService();
 
    public ScrollPane buildStatisticsView() {
        VBox page = new VBox(30);
        page.setPadding(new Insets(30));
        page.setStyle("-fx-background-color: #F8FBFA;");
 
        // Header
        VBox header = new VBox(8);
        Label title = new Label("Wellness Analytics");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: 900; -fx-text-fill: " + PRIMARY_DARK + ";");
        Label subtitle = new Label("Real-time data synchronization with Mentis Web Dashboard");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: " + MUTED + ";");
        header.getChildren().addAll(title, subtitle);
 
        // Top Row: Distribution Charts
        HBox topRow = new HBox(25);
        topRow.setAlignment(Pos.CENTER);
        
        Node moodChart = buildMoodDistributionChart();
        Node goalChart = buildGoalProgressChart();
        
        topRow.getChildren().addAll(
                buildChartCard("Mood Distribution", moodChart),
                buildChartCard("Goal Progress", goalChart)
        );
 
        // Bottom Row: Trend Chart
        VBox trendCard = buildChartCard("Mood Evolution (Last 7 Days)", buildMoodTrendChart());
        trendCard.setPrefHeight(400);
 
        page.getChildren().addAll(header, topRow, trendCard);
 
        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return scrollPane;
    }
 
    private VBox buildChartCard(String titleText, Node chart) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-border-color: " + BORDER + "; -fx-border-radius: 20; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);");
        HBox.setHgrow(card, Priority.ALWAYS);
 
        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + INK + ";");
        
        card.getChildren().addAll(title, chart);
        VBox.setVgrow(chart, Priority.ALWAYS);
        return card;
    }
 
    private Node buildMoodDistributionChart() {
        PieChart pieChart = new PieChart();
        pieChart.setLegendVisible(false);
        pieChart.setLabelsVisible(false);
        pieChart.setPrefSize(300, 300);
        pieChart.setMinSize(300, 300);
 
        int vh=0,h=0,n=0,s=0,vs=0;
        try {
            Map<String, Integer> data = moodService.getFeelingCounts();
            vh = data.getOrDefault("Very Happy", 0);
            h = data.getOrDefault("Happy", 0);
            n = data.getOrDefault("Neutral", 0);
            s = data.getOrDefault("Sad", 0);
            vs = data.getOrDefault("Very Sad", 0);
            
            pieChart.getData().add(new PieChart.Data("Very Happy", vh > 0 ? vh : 0.001));
            pieChart.getData().add(new PieChart.Data("Happy", h > 0 ? h : 0.001));
            pieChart.getData().add(new PieChart.Data("Neutral", n > 0 ? n : 0.001));
            pieChart.getData().add(new PieChart.Data("Sad", s > 0 ? s : 0.001));
            pieChart.getData().add(new PieChart.Data("Very Sad", vs > 0 ? vs : 0.001));
        } catch (SQLException e) { e.printStackTrace(); }
 
        Circle hole = new Circle(65, Color.WHITE);
        StackPane chartStack = new StackPane(pieChart, hole);
        chartStack.setAlignment(Pos.CENTER);
 
        VBox legend = new VBox(8);
        legend.setAlignment(Pos.CENTER_LEFT);
        String[] colors = {"#1B5E20", "#2E7D32", "#4CAF50", "#81C784", "#C8E6C9"};
        String[] labels = {"Very Happy", "Happy", "Neutral", "Sad", "Very Sad"};
        int[] counts = {vh, h, n, s, vs};
        
        for (int i = 0; i < labels.length; i++) {
            HBox item = new HBox(8, new Circle(5, Color.web(colors[i])), new Label(labels[i] + " (" + counts[i] + ")"));
            item.setAlignment(Pos.CENTER_LEFT);
            legend.getChildren().add(item);
        }
 
        HBox container = new HBox(20, chartStack, legend);
        container.setAlignment(Pos.CENTER);
 
        Platform.runLater(() -> {
            int i = 0;
            for (PieChart.Data d : pieChart.getData()) {
                if (d.getNode() != null) d.getNode().setStyle("-fx-pie-color: " + colors[i % colors.length] + ";");
                i++;
            }
        });
        return container;
    }
 
    private Node buildGoalProgressChart() {
        PieChart pieChart = new PieChart();
        pieChart.setLegendVisible(false);
        pieChart.setLabelsVisible(false);
        pieChart.setPrefSize(300, 300);
        pieChart.setMinSize(300, 300);
 
        int completed = 0, pending = 0;
        try {
            Map<String, Integer> data = goalService.getGoalStatusCounts();
            completed = data.getOrDefault("Completed", 0);
            pending = data.getOrDefault("Pending", 0);
            
            pieChart.getData().add(new PieChart.Data("Completed", completed > 0 ? completed : 0.001));
            pieChart.getData().add(new PieChart.Data("Pending", pending > 0 ? pending : 0.001));
        } catch (SQLException e) { e.printStackTrace(); }
 
        VBox legend = new VBox(8);
        legend.setAlignment(Pos.CENTER_LEFT);
        legend.getChildren().addAll(
            new HBox(8, new Circle(5, Color.web("#2E7D32")), new Label("Completed (" + completed + ")")),
            new HBox(8, new Circle(5, Color.web("#FFC107")), new Label("Pending (" + pending + ")"))
        );
 
        HBox container = new HBox(20, pieChart, legend);
        container.setAlignment(Pos.CENTER);
 
        Platform.runLater(() -> {
            for (PieChart.Data d : pieChart.getData()) {
                if (d.getNode() != null) {
                    if (d.getName().equals("Completed")) d.getNode().setStyle("-fx-pie-color: #2E7D32;");
                    else d.getNode().setStyle("-fx-pie-color: #FFC107;");
                }
            }
        });
        return container;
    }
 
    private Node buildMoodTrendChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Days");
        
        NumberAxis yAxis = new NumberAxis(1, 5, 1);
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
            @Override public String toString(Number object) {
                return switch (object.intValue()) {
                    case 5 -> "Very Happy";
                    case 4 -> "Happy";
                    case 3 -> "Neutral";
                    case 2 -> "Sad";
                    case 1 -> "Very Sad";
                    default -> "";
                };
            }
        });
 
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setLegendVisible(false);
        lineChart.setCreateSymbols(true);
        lineChart.setPrefHeight(300);
 
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        
        // Prepare timeline for last 7 days
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM");
        Map<String, Double> fullTrend = new TreeMap<>();
        for (int i = 6; i >= 0; i--) {
            fullTrend.put(LocalDate.now().minusDays(i).format(dtf), 3.0); // Default to Neutral
        }
 
        try {
            Map<String, Double> realData = moodService.getMoodTrendLast7Days();
            for (Map.Entry<String, Double> entry : realData.entrySet()) {
                // Parse the date from SQL (yyyy-MM-dd) to dd/MM
                String dateKey = LocalDate.parse(entry.getKey()).format(dtf);
                fullTrend.put(dateKey, entry.getValue());
            }
        } catch (Exception e) { e.printStackTrace(); }
 
        for (Map.Entry<String, Double> entry : fullTrend.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
 
        lineChart.getData().add(series);
        
        Platform.runLater(() -> {
            if (series.getNode() != null) {
                series.getNode().setStyle("-fx-stroke: " + PRIMARY + "; -fx-stroke-width: 3px;");
            }
        });
 
        return lineChart;
    }
}