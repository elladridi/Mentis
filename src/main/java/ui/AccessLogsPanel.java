package ui;

import controller.ContentPathController;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.ContentPath;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * AccessLogsPanel - View access logs in the same window (ADMIN ONLY)
 * INCLUDES: Statistics dashboard as inner class
 */
public class AccessLogsPanel extends VBox {

    private ContentPathController controller;
    private MentisLoginFrame parentApp;
    private TableView<ContentPathModel> logTable;
    private TextField userIdField;
    private Label totalLogsLabel;
    private VBox contentContainer;
    private Label deniedLabel;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Color BACKGROUND_GREEN = Color.rgb(240, 245, 242);
    private static final Color ACCENT_GREEN = Color.rgb(88, 139, 113);
    private static final Color ERROR_RED = Color.rgb(220, 80, 80);
    private static final Color TEXT_DARK = Color.rgb(60, 70, 80);
    private static final Color STATS_PURPLE = Color.rgb(155, 89, 182);

    public AccessLogsPanel(MentisLoginFrame parentApp, ContentPathController controller) {
        this.parentApp = parentApp;
        this.controller = controller;

        System.out.println(" Creating AccessLogsPanel with ContentPathController");

        setStyle("-fx-background-color: #" + toHex(BACKGROUND_GREEN) + ";");
        setPadding(new Insets(30, 40, 30, 40));
        setSpacing(20);

        deniedLabel = new Label("ACCESS DENIED - ADMIN ONLY");
        deniedLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        deniedLabel.setTextFill(Color.web(toHex(ERROR_RED)));
        deniedLabel.setAlignment(Pos.CENTER);
        deniedLabel.setVisible(false);

        contentContainer = new VBox(20);
        contentContainer.setVisible(false);

        initializeUI();

        getChildren().addAll(deniedLabel, contentContainer);

        checkAdminStatus();

        System.out.println("AccessLogsPanel created successfully");
    }

    public void checkAdminStatus() {
        String userType = parentApp.getUserType();
        System.out.println("  - Checking admin status. User type: " + userType);

        if ("admin".equalsIgnoreCase(userType)) {
            deniedLabel.setVisible(false);
            contentContainer.setVisible(true);
            System.out.println("  - Admin access GRANTED");
        } else {
            deniedLabel.setVisible(true);
            contentContainer.setVisible(false);
            System.out.println("  - Admin access DENIED (role: " + userType + ")");
        }
    }

    private void initializeUI() {
        // Header with back button
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 10, 0));

        Button backBtn = new Button("← Back to Content");
        backBtn.setFont(Font.font("Arial", 14));
        backBtn.setTextFill(Color.web(toHex(ACCENT_GREEN)));
        backBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        backBtn.setOnAction(e -> parentApp.showContentUploadPanel());

        Label titleLabel = new Label("Content Access Logs");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web(toHex(ACCENT_GREEN)));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(backBtn, spacer, titleLabel);

        // Filter controls
        HBox filterBox = new HBox(15);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.setPadding(new Insets(15));
        filterBox.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        Label filterLabel = new Label("Filter by User ID:");
        filterLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        userIdField = new TextField();
        userIdField.setPromptText("Enter User ID");
        userIdField.setPrefWidth(200);
        userIdField.setStyle("-fx-padding: 8; -fx-background-radius: 5; -fx-border-radius: 5;");

        Button filterBtn = new Button("Apply Filter");
        filterBtn.setStyle("-fx-background-color: #" + toHex(ACCENT_GREEN) + "; -fx-text-fill: white; -fx-padding: 8 20;");
        filterBtn.setOnAction(e -> filterByUser());

        Button clearBtn = new Button("Show All");
        clearBtn.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 8 20;");
        clearBtn.setOnAction(e -> refreshData());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle("-fx-background-color: #" + toHex(ACCENT_GREEN) + "; -fx-text-fill: white; -fx-padding: 8 20;");
        refreshBtn.setOnAction(e -> refreshData());

        filterBox.getChildren().addAll(filterLabel, userIdField, filterBtn, clearBtn, refreshBtn);

        // Stats bar
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setPadding(new Insets(10, 15, 10, 15));
        statsBox.setStyle("-fx-background-color: #e8f5e9; -fx-background-radius: 8;");

        totalLogsLabel = new Label("Total Logs: 0");
        totalLogsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        totalLogsLabel.setTextFill(Color.web(toHex(ACCENT_GREEN)));

        statsBox.getChildren().add(totalLogsLabel);

        createTable();

        // Action buttons row with Statistics button
        HBox actionBox = new HBox(15);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.setPadding(new Insets(10, 0, 0, 0));

        Button statsBtn = new Button("📊 View Statistics");
        statsBtn.setStyle("-fx-background-color: #" + toHex(STATS_PURPLE) + "; -fx-text-fill: white; -fx-padding: 10 20; -fx-cursor: hand; -fx-font-size: 12px; -fx-font-weight: bold;");
        statsBtn.setOnAction(e -> showStatistics());

        Button exportCSVBtn = new Button("Export to CSV");
        exportCSVBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 10 20; -fx-cursor: hand; -fx-font-size: 12px;");
        exportCSVBtn.setOnAction(e -> exportLogs("csv"));

        Button exportHTMLBtn = new Button("Export to HTML");
        exportHTMLBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-padding: 10 20; -fx-cursor: hand; -fx-font-size: 12px;");
        exportHTMLBtn.setOnAction(e -> exportLogs("html"));

        Button deleteOldBtn = new Button("Delete Logs Older Than 30 Days");
        deleteOldBtn.setStyle("-fx-background-color: #" + toHex(ERROR_RED) + "; -fx-text-fill: white; -fx-padding: 10 20; -fx-cursor: hand; -fx-font-size: 12px;");
        deleteOldBtn.setOnAction(e -> deleteOldLogs());

        actionBox.getChildren().addAll(statsBtn, exportCSVBtn, exportHTMLBtn, deleteOldBtn);

        contentContainer.getChildren().addAll(headerBox, filterBox, statsBox, logTable, actionBox);
        VBox.setVgrow(logTable, Priority.ALWAYS);
    }

    private void createTable() {
        logTable = new TableView<>();
        logTable.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5;");
        logTable.setPlaceholder(new Label("No access logs found"));
        logTable.setPrefHeight(600);

        TableColumn<ContentPathModel, Integer> idCol = new TableColumn<>("Log ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("pathId"));
        idCol.setPrefWidth(80);
        idCol.setMinWidth(80);

        TableColumn<ContentPathModel, Integer> userIdCol = new TableColumn<>("User ID");
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        userIdCol.setPrefWidth(80);
        userIdCol.setMinWidth(80);

        TableColumn<ContentPathModel, String> userNameCol = new TableColumn<>("User Name");
        userNameCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
        userNameCol.setPrefWidth(150);
        userNameCol.setMinWidth(120);

        TableColumn<ContentPathModel, Integer> nodeIdCol = new TableColumn<>("Content ID");
        nodeIdCol.setCellValueFactory(new PropertyValueFactory<>("nodeId"));
        nodeIdCol.setPrefWidth(100);
        nodeIdCol.setMinWidth(80);

        TableColumn<ContentPathModel, String> contentTitleCol = new TableColumn<>("Content Title");
        contentTitleCol.setCellValueFactory(new PropertyValueFactory<>("contentTitle"));
        contentTitleCol.setPrefWidth(300);
        contentTitleCol.setMinWidth(200);

        TableColumn<ContentPathModel, String> accessedAtCol = new TableColumn<>("Accessed At");
        accessedAtCol.setCellValueFactory(new PropertyValueFactory<>("accessedAt"));
        accessedAtCol.setPrefWidth(180);
        accessedAtCol.setMinWidth(150);

        TableColumn<ContentPathModel, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(100);
        actionCol.setMinWidth(80);
        actionCol.setCellFactory(col -> new TableCell<ContentPathModel, Void>() {
            private final Button deleteBtn = new Button("Delete");
            {
                deleteBtn.setStyle("-fx-background-color: #" + toHex(ERROR_RED) + "; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10; -fx-cursor: hand;");
                deleteBtn.setOnAction(e -> {
                    ContentPathModel log = getTableView().getItems().get(getIndex());
                    deleteLog(log);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });

        logTable.getColumns().addAll(idCol, userIdCol, userNameCol, nodeIdCol, contentTitleCol, accessedAtCol, actionCol);
        logTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // Show statistics panel
    private void showStatistics() {
        try {
            ContentPathStatsPanel statsPanel = new ContentPathStatsPanel(parentApp, controller);

            Stage stage = new Stage();
            stage.setTitle("Content Access Statistics");
            stage.setScene(new Scene(statsPanel, 1200, 800));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(getScene().getWindow());
            stage.show();

        } catch (Exception e) {
            showAlert("Error", "Could not open statistics: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void filterByUser() {
        String userIdText = userIdField.getText().trim();
        if (userIdText.isEmpty()) {
            refreshData();
            return;
        }

        try {
            int userId = Integer.parseInt(userIdText);
            List<ContentPath> logs = controller.getAccessLogsByUser(userId);
            updateTable(logs);
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "User ID must be a number", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateTable(List<ContentPath> logs) {
        ObservableList<ContentPathModel> models = FXCollections.observableArrayList();
        if (logs != null) {
            for (ContentPath log : logs) {
                models.add(new ContentPathModel(log));
            }
        }
        logTable.setItems(models);
        totalLogsLabel.setText("Total Logs: " + models.size());
    }

    private void deleteLog(ContentPathModel log) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Access Log");
        confirm.setContentText("Delete log for " + log.getUserName() + "?\nContent: " + log.getContentTitle());

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                controller.deleteAccessLog(log.getPathId());
                refreshData();
                showAlert("Success", "Access log deleted", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void deleteOldLogs() {
        TextInputDialog dialog = new TextInputDialog("30");
        dialog.setTitle("Delete Old Logs");
        dialog.setHeaderText("Delete access logs older than X days");
        dialog.setContentText("Enter days:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int days = Integer.parseInt(result.get());
                int deleted = controller.deleteOldAccessLogs(days);
                showAlert("Success", deleted + " old access logs deleted", Alert.AlertType.INFORMATION);
                refreshData();
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid number", Alert.AlertType.ERROR);
            } catch (Exception e) {
                showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void exportLogs(String format) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Access Logs");

            if ("csv".equals(format)) {
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
                fileChooser.setInitialFileName("mentis_access_logs_" +
                        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv");
            } else {
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("HTML Files", "*.html"));
                fileChooser.setInitialFileName("mentis_access_logs_" +
                        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".html");
            }

            File file = fileChooser.showSaveDialog(getScene().getWindow());
            if (file != null) {
                String content;
                if ("csv".equals(format)) {
                    content = controller.exportAccessLogsToCSV();
                } else {
                    content = controller.exportAccessLogsToHTML();
                }

                java.nio.file.Files.write(file.toPath(), content.getBytes());
                showAlert("Export Successful", "Logs exported to:\n" + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            showAlert("Export Failed", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    public void refreshData() {
        checkAdminStatus();

        if ("admin".equalsIgnoreCase(parentApp.getUserType())) {
            try {
                System.out.println("🔄 Refreshing AccessLogsPanel");
                List<ContentPath> logs = controller.getAllAccessLogs();
                updateTable(logs);
            } catch (Exception e) {
                System.err.println("❌ Error loading logs: " + e.getMessage());
                e.printStackTrace();
                showAlert("Error", "Failed to load logs: " + e.getMessage(), Alert.AlertType.ERROR);
            }
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
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }

    public static class ContentPathModel {
        private final SimpleIntegerProperty pathId;
        private final SimpleIntegerProperty userId;
        private final SimpleStringProperty userName;
        private final SimpleIntegerProperty nodeId;
        private final SimpleStringProperty contentTitle;
        private final SimpleStringProperty accessedAt;

        public ContentPathModel(ContentPath path) {
            this.pathId = new SimpleIntegerProperty(path.getPathId());
            this.userId = new SimpleIntegerProperty(path.getUserId());

            String name = path.getUserFullName();
            this.userName = new SimpleStringProperty(name != null && !name.trim().isEmpty() ? name : "User " + path.getUserId());

            this.nodeId = new SimpleIntegerProperty(path.getNodeId());

            String title = path.getContentTitle();
            this.contentTitle = new SimpleStringProperty(title != null ? title : "Content " + path.getNodeId());

            this.accessedAt = new SimpleStringProperty(
                    path.getAccessedAt() != null ? path.getAccessedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "N/A"
            );
        }

        public int getPathId() { return pathId.get(); }
        public int getUserId() { return userId.get(); }
        public String getUserName() { return userName.get(); }
        public int getNodeId() { return nodeId.get(); }
        public String getContentTitle() { return contentTitle.get(); }
        public String getAccessedAt() { return accessedAt.get(); }
    }

    // ==================== INNER CLASS: ContentPathStatsPanel ====================

    /**
     * ContentPathStatsPanel - Statistics dashboard for content access logs
     * INNER CLASS of AccessLogsPanel
     */
    private class ContentPathStatsPanel extends VBox {

        private Label totalViewsLabel;
        private Label uniqueUsersLabel;
        private Label avgViewsPerDayLabel;
        private Label activeUsersLabel;
        private LineChart<String, Number> viewsChart;
        private BarChart<String, Number> topContentChart;
        private PieChart userTypeChart;

        private static final Color CHART_BLUE = Color.rgb(52, 152, 219);
        private static final Color CHART_PURPLE = Color.rgb(155, 89, 182);
        private static final Color CHART_ORANGE = Color.rgb(230, 126, 34);
        private static final Color CHART_RED = Color.rgb(192, 57, 43);
        private static final Color CHART_GREEN = Color.rgb(39, 174, 96);

        public ContentPathStatsPanel(MentisLoginFrame parentApp, ContentPathController controller) {
            setStyle("-fx-background-color: #" + toHex(BACKGROUND_GREEN) + ";");
            setPadding(new Insets(30));
            setSpacing(25);

            initializeUI();
            refreshData();
        }

        private void initializeUI() {
            // Header with close button
            HBox headerBox = new HBox();
            headerBox.setAlignment(Pos.CENTER_LEFT);
            headerBox.setPadding(new Insets(0, 0, 20, 0));

            Button closeBtn = new Button("✕ Close");
            closeBtn.setFont(Font.font("Arial", 14));
            closeBtn.setTextFill(Color.web(toHex(ACCENT_GREEN)));
            closeBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-border-color: " + toHex(ACCENT_GREEN) + "; -fx-border-radius: 5; -fx-padding: 8 20;");
            closeBtn.setOnAction(e -> {
                Stage stage = (Stage) getScene().getWindow();
                stage.close();
            });

            Label titleLabel = new Label("📊 Content Access Statistics");
            titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
            titleLabel.setTextFill(Color.web(toHex(ACCENT_GREEN)));

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            headerBox.getChildren().addAll(titleLabel, spacer, closeBtn);

            // Stats cards row
            HBox statsCards = createStatsCards();

            // Charts row 1 - Line chart and Bar chart
            HBox chartsRow1 = createChartsRow1();

            // Charts row 2 - Pie chart and summary
            HBox chartsRow2 = createChartsRow2();

            // Refresh button
            HBox buttonBox = new HBox(15);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.setPadding(new Insets(20, 0, 0, 0));

            Button refreshBtn = new Button("🔄 Refresh Statistics");
            refreshBtn.setStyle("-fx-background-color: #" + toHex(ACCENT_GREEN) + "; -fx-text-fill: white; " +
                    "-fx-padding: 12 25; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
            refreshBtn.setOnAction(e -> refreshData());

            buttonBox.getChildren().add(refreshBtn);

            getChildren().addAll(headerBox, statsCards, chartsRow1, chartsRow2, buttonBox);
        }

        private HBox createStatsCards() {
            HBox box = new HBox(20);
            box.setAlignment(Pos.CENTER);
            box.setPadding(new Insets(0, 0, 20, 0));

            // Total Views Card
            VBox totalViewsCard = createStatCard("Total Views", "0", ACCENT_GREEN);
            totalViewsLabel = (Label) totalViewsCard.getChildren().get(1);

            // Unique Users Card
            VBox uniqueUsersCard = createStatCard("Unique Users", "0", CHART_BLUE);
            uniqueUsersLabel = (Label) uniqueUsersCard.getChildren().get(1);

            // Avg Views/Day Card
            VBox avgViewsCard = createStatCard("Avg Views/Day", "0", CHART_PURPLE);
            avgViewsPerDayLabel = (Label) avgViewsCard.getChildren().get(1);

            // Active Users Card
            VBox activeUsersCard = createStatCard("Active Users (30d)", "0", CHART_ORANGE);
            activeUsersLabel = (Label) activeUsersCard.getChildren().get(1);

            box.getChildren().addAll(totalViewsCard, uniqueUsersCard, avgViewsCard, activeUsersCard);
            return box;
        }

        private VBox createStatCard(String title, String value, Color color) {
            VBox card = new VBox(10);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
            card.setPrefWidth(200);
            card.setAlignment(Pos.CENTER);

            Label titleLabel = new Label(title);
            titleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
            titleLabel.setTextFill(Color.GRAY);

            Label valueLabel = new Label(value);
            valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
            valueLabel.setTextFill(Color.web(toHex(color)));

            card.getChildren().addAll(titleLabel, valueLabel);
            return card;
        }

        private HBox createChartsRow1() {
            HBox box = new HBox(20);
            box.setAlignment(Pos.CENTER);
            box.setPrefHeight(400);

            // Views over time chart
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Date");
            yAxis.setLabel("Views");

            viewsChart = new LineChart<>(xAxis, yAxis);
            viewsChart.setTitle("📈 Views Over Time (Last 30 Days)");
            viewsChart.setPrefWidth(600);
            viewsChart.setPrefHeight(350);
            viewsChart.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 10;");
            viewsChart.setLegendVisible(false);
            viewsChart.setAnimated(false);

            // Top content chart
            CategoryAxis contentXAxis = new CategoryAxis();
            NumberAxis contentYAxis = new NumberAxis();
            contentXAxis.setLabel("Content");
            contentYAxis.setLabel("Views");

            topContentChart = new BarChart<>(contentXAxis, contentYAxis);
            topContentChart.setTitle("📊 Most Viewed Content (Top 10)");
            topContentChart.setPrefWidth(600);
            topContentChart.setPrefHeight(350);
            topContentChart.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 10;");
            topContentChart.setLegendVisible(false);
            topContentChart.setAnimated(false);

            box.getChildren().addAll(viewsChart, topContentChart);
            return box;
        }

        private HBox createChartsRow2() {
            HBox box = new HBox(20);
            box.setAlignment(Pos.CENTER);
            box.setPadding(new Insets(20, 0, 20, 0));
            box.setPrefHeight(350);

            // User access distribution pie chart
            userTypeChart = new PieChart();
            userTypeChart.setTitle("👥 User Access Distribution");
            userTypeChart.setPrefWidth(500);
            userTypeChart.setPrefHeight(300);
            userTypeChart.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 10;");
            userTypeChart.setLegendVisible(true);
            userTypeChart.setAnimated(false);

            // Summary panel
            VBox summaryBox = new VBox(15);
            summaryBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
            summaryBox.setPrefWidth(450);
            summaryBox.setPrefHeight(300);

            Label summaryTitle = new Label("📋 Summary");
            summaryTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            summaryTitle.setTextFill(Color.web(toHex(ACCENT_GREEN)));

            // Summary content will be updated in refreshData
            VBox summaryContent = new VBox(10);
            summaryContent.setId("summaryContent");

            summaryBox.getChildren().addAll(summaryTitle, summaryContent);

            box.getChildren().addAll(userTypeChart, summaryBox);
            return box;
        }

        public void refreshData() {
            try {
                List<ContentPath> allLogs = controller.getAllAccessLogs();

                // Calculate statistics
                int totalViews = allLogs.size();
                long uniqueUsers = allLogs.stream().map(ContentPath::getUserId).distinct().count();

                // Views in last 30 days
                LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
                long last30DaysViews = allLogs.stream()
                        .filter(log -> log.getAccessedAt() != null)
                        .filter(log -> log.getAccessedAt().toLocalDate().isAfter(thirtyDaysAgo))
                        .count();
                double avgViewsPerDay = last30DaysViews / 30.0;

                // Active users count
                int activeUsers = controller.getActiveUsersCount();

                // Update labels
                totalViewsLabel.setText(String.valueOf(totalViews));
                uniqueUsersLabel.setText(String.valueOf(uniqueUsers));
                avgViewsPerDayLabel.setText(String.format("%.1f", avgViewsPerDay));
                activeUsersLabel.setText(String.valueOf(activeUsers));

                // Update charts
                updateViewsChart(allLogs);
                updateTopContentChart(allLogs);
                updatePieChart(allLogs);
                updateSummary(allLogs);

            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to load statistics: " + e.getMessage());
            }
        }

        private void updateViewsChart(List<ContentPath> logs) {
            viewsChart.getData().clear();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Views");

            // Group by date for last 30 days
            LocalDate startDate = LocalDate.now().minusDays(29);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");

            for (int i = 0; i < 30; i++) {
                LocalDate date = startDate.plusDays(i);
                String dateStr = date.format(formatter);

                long count = logs.stream()
                        .filter(log -> log.getAccessedAt() != null)
                        .filter(log -> log.getAccessedAt().toLocalDate().equals(date))
                        .count();

                series.getData().add(new XYChart.Data<>(dateStr, count));
            }

            viewsChart.getData().add(series);
        }

        private void updateTopContentChart(List<ContentPath> logs) {
            topContentChart.getData().clear();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Views");

            // Group by content title and get top 10
            Map<String, Long> contentViews = logs.stream()
                    .filter(log -> log.getContentTitle() != null)
                    .collect(Collectors.groupingBy(
                            ContentPath::getContentTitle,
                            Collectors.counting()
                    ));

            contentViews.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(10)
                    .forEach(entry -> {
                        String title = entry.getKey();
                        if (title.length() > 25) {
                            title = title.substring(0, 22) + "...";
                        }
                        series.getData().add(new XYChart.Data<>(title, entry.getValue()));
                    });

            topContentChart.getData().add(series);

            // Color the bars
            int colorIndex = 0;
            Color[] colors = {CHART_BLUE, CHART_PURPLE, CHART_ORANGE, CHART_GREEN, CHART_RED};
            for (XYChart.Series<String, Number> s : topContentChart.getData()) {
                for (XYChart.Data<String, Number> data : s.getData()) {
                    if (data.getNode() != null) {
                        data.getNode().setStyle("-fx-bar-fill: #" + toHex(colors[colorIndex % colors.length]) + ";");
                        colorIndex++;
                    }
                }
            }
        }

        private void updatePieChart(List<ContentPath> logs) {
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

            // Group by user - show top 5 users
            Map<Integer, Long> userViews = logs.stream()
                    .collect(Collectors.groupingBy(
                            ContentPath::getUserId,
                            Collectors.counting()
                    ));

            userViews.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                    .limit(5)
                    .forEach(entry -> {
                        String label = "User " + entry.getKey() + " (" + entry.getValue() + " views)";
                        pieChartData.add(new PieChart.Data(label, entry.getValue()));
                    });

            userTypeChart.setData(pieChartData);
        }

        private void updateSummary(List<ContentPath> logs) {
            // Find the summaryContent VBox by ID
            VBox summaryContent = (VBox) lookup("#summaryContent");
            if (summaryContent == null) return;

            summaryContent.getChildren().clear();

            // Most active user
            Map<Integer, Long> userViews = logs.stream()
                    .collect(Collectors.groupingBy(
                            ContentPath::getUserId,
                            Collectors.counting()
                    ));

            Integer mostActiveUser = userViews.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            Label mostActiveLabel = new Label("Most Active User: " +
                    (mostActiveUser != null ? "User " + mostActiveUser + " (" + userViews.get(mostActiveUser) + " views)" : "N/A"));
            mostActiveLabel.setFont(Font.font("Arial", 14));
            mostActiveLabel.setTextFill(Color.web(toHex(CHART_PURPLE)));

            // Most viewed content
            Map<String, Long> contentViews = logs.stream()
                    .filter(log -> log.getContentTitle() != null)
                    .collect(Collectors.groupingBy(
                            ContentPath::getContentTitle,
                            Collectors.counting()
                    ));

            String mostViewedContent = contentViews.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            Label mostViewedLabel = new Label("Most Viewed Content: " +
                    (mostViewedContent != null ? mostViewedContent + " (" + contentViews.get(mostViewedContent) + " views)" : "N/A"));
            mostViewedLabel.setFont(Font.font("Arial", 14));
            mostViewedLabel.setTextFill(Color.web(toHex(CHART_ORANGE)));

            // Peak day
            Map<LocalDate, Long> dailyViews = logs.stream()
                    .filter(log -> log.getAccessedAt() != null)
                    .collect(Collectors.groupingBy(
                            log -> log.getAccessedAt().toLocalDate(),
                            Collectors.counting()
                    ));

            LocalDate peakDay = dailyViews.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            Label peakDayLabel = new Label("Peak Access Day: " +
                    (peakDay != null ? peakDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " (" + dailyViews.get(peakDay) + " views)" : "N/A"));
            peakDayLabel.setFont(Font.font("Arial", 14));
            peakDayLabel.setTextFill(Color.web(toHex(CHART_GREEN)));

            // Total content accessed
            long totalContent = logs.stream().map(ContentPath::getNodeId).distinct().count();
            Label totalContentLabel = new Label("Total Content Accessed: " + totalContent);
            totalContentLabel.setFont(Font.font("Arial", 14));
            totalContentLabel.setTextFill(Color.web(toHex(CHART_BLUE)));

            summaryContent.getChildren().addAll(mostActiveLabel, mostViewedLabel, peakDayLabel, totalContentLabel);
        }

        private void showAlert(String title, String content) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        }
    }
}