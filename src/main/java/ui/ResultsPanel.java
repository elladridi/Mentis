package ui;

import controller.AssessmentResultController;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.AssessmentResult;
import javafx.scene.chart.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.sql.SQLException;
import java.util.List;

public class ResultsPanel extends VBox {

    private MentisLoginFrame parentApp;
    private AssessmentResultController controller;
    private TableView<ResultModel> resultsTable;
    private ObservableList<ResultModel> resultData;
    private Label userTypeLabel;
    private Label notificationLabel;

    // Color constants
    private static final Color BACKGROUND_BEIGE = Color.rgb(243, 243, 243);
    private static final Color CARD_WHITE = Color.WHITE;
    private static final Color ACCENT_GREEN = Color.rgb(108, 158, 131);
    private static final Color ACCENT_LIGHT_GREEN = Color.rgb(200, 225, 210);
    private static final Color BORDER_LIGHT = Color.rgb(220, 220, 220);
    private static final Color TEXT_DARK = Color.rgb(60, 70, 80);
    private static final Color RISK_HIGH = Color.rgb(180, 0, 0);
    private static final Color RISK_MEDIUM = Color.rgb(153, 102, 0);
    private static final Color RISK_LOW = Color.rgb(0, 128, 0);

    public ResultsPanel(MentisLoginFrame parentApp, AssessmentResultController controller) {
        this.parentApp = parentApp;
        this.controller = controller;

        setStyle("-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";");
        setPadding(new Insets(45, 50, 45, 50));
        setSpacing(35);

        createHeader();
        createTable();
        createNotificationBar();
        refreshData();
    }

    private void createHeader() {
        BorderPane headerPanel = new BorderPane();
        headerPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";");

        Label titleLabel = new Label("Assessment Results");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 42));
        titleLabel.setTextFill(Color.web(toHex(ACCENT_GREEN)));

        HBox topRightPanel = new HBox(20);
        topRightPanel.setAlignment(Pos.CENTER_RIGHT);
        topRightPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";");

        String userType = parentApp.getUserType();
        userTypeLabel = new Label(getUserTypeDisplay(userType));
        userTypeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        userTypeLabel.setTextFill(Color.web(toHex(ACCENT_GREEN)));
        userTypeLabel.setPadding(new Insets(0, 20, 0, 0));

        // Admin gets the Assessment navigation link
        if ("admin".equals(userType)) {
            Button assessmentLink = createHeaderLink("Assessment");
            assessmentLink.setOnAction(e -> parentApp.showAssessmentPanel());
            topRightPanel.getChildren().addAll(assessmentLink, createSpacer(20));
        }

        Button refreshButton = createRefreshButton();

        // Show progress button for patients only (not admin / psychologist)
        if (!"admin".equals(userType) && !"psychologist".equals(userType)) {
            Button progressBtn = createProgressButton();
            topRightPanel.getChildren().addAll(userTypeLabel, createSpacer(10), progressBtn, createSpacer(10), refreshButton);
        } else {
            topRightPanel.getChildren().addAll(userTypeLabel, createSpacer(10), refreshButton);
        }

        headerPanel.setLeft(titleLabel);
        headerPanel.setRight(topRightPanel);

        getChildren().add(headerPanel);
    }

    private String getUserTypeDisplay(String userType) {
        switch (userType) {
            case "patient":      return "Patient - Your Results";
            case "psychologist": return "PSYCHOLOGIST - All Results";
            case "admin":        return "ADMINISTRATOR - All Results";
            default:             return "User: " + userType.toUpperCase();
        }
    }

    private Button createHeaderLink(String text) {
        Button link = new Button(text);
        link.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        link.setTextFill(Color.web(toHex(ACCENT_GREEN)));
        link.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-width: 0;" +
                        "-fx-cursor: hand;"
        );
        link.setOnMouseEntered(e -> link.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-width: 0 0 2 0;" +
                        "-fx-border-color: #" + toHex(ACCENT_GREEN) + ";" +
                        "-fx-cursor: hand;"
        ));
        link.setOnMouseExited(e -> link.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-width: 0;" +
                        "-fx-cursor: hand;"
        ));
        return link;
    }

    private Button createRefreshButton() {
        Button button = new Button("Refresh");
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        button.setTextFill(Color.WHITE);
        button.setStyle(
                "-fx-background-color: #" + toHex(ACCENT_GREEN) + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;"
        );
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: #" + toHex(ACCENT_GREEN.darker()) + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: #" + toHex(ACCENT_GREEN) + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;"
        ));
        button.setOnAction(e -> refreshData());
        return button;
    }

    private Button createProgressButton() {
        Button button = new Button("📈 My Progress");
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        button.setTextFill(Color.web(toHex(ACCENT_GREEN)));
        button.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #" + toHex(ACCENT_GREEN) + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-background-radius: 5;" +
                        "-fx-border-radius: 5;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;"
        );
        button.setOnMouseEntered(e -> {
            button.setStyle(
                    "-fx-background-color: #" + toHex(ACCENT_GREEN) + ";" +
                            "-fx-border-color: #" + toHex(ACCENT_GREEN) + ";" +
                            "-fx-border-width: 2;" +
                            "-fx-background-radius: 5;" +
                            "-fx-border-radius: 5;" +
                            "-fx-padding: 10 20;" +
                            "-fx-cursor: hand;"
            );
            button.setTextFill(Color.WHITE);
        });
        button.setOnMouseExited(e -> {
            button.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-border-color: #" + toHex(ACCENT_GREEN) + ";" +
                            "-fx-border-width: 2;" +
                            "-fx-background-radius: 5;" +
                            "-fx-border-radius: 5;" +
                            "-fx-padding: 10 20;" +
                            "-fx-cursor: hand;"
            );
            button.setTextFill(Color.web(toHex(ACCENT_GREEN)));
        });
        button.setOnAction(e -> showProgressDialog());
        return button;
    }

    private Region createSpacer(int width) {
        Region spacer = new Region();
        spacer.setPrefWidth(width);
        return spacer;
    }

    // ═══════════════════════════════════════════════════════════════
    //  TABLE
    // ═══════════════════════════════════════════════════════════════

    private void createTable() {
        resultsTable = new TableView<>();
        resultsTable.setStyle("-fx-background-color: white; -fx-border-color: #" + toHex(BORDER_LIGHT) + ";");
        resultsTable.setFixedCellSize(50);
        resultsTable.setPlaceholder(new Label("No results found"));

        resultsTable.setRowFactory(tv -> new TableRow<ResultModel>() {
            @Override
            protected void updateItem(ResultModel item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    setStyle(getIndex() % 2 == 0 ?
                            "-fx-background-color: white;" :
                            "-fx-background-color: #f8f8f8;");
                }
            }
        });

        TableColumn<ResultModel, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("resultId"));
        idCol.setPrefWidth(50);
        idCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<ResultModel, Integer> userIdCol = new TableColumn<>("User ID");
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        userIdCol.setPrefWidth(80);
        userIdCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<ResultModel, Integer> assessmentIdCol = new TableColumn<>("Assessment ID");
        assessmentIdCol.setCellValueFactory(new PropertyValueFactory<>("assessmentId"));
        assessmentIdCol.setPrefWidth(110);
        assessmentIdCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<ResultModel, Integer> scoreCol = new TableColumn<>("Score");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("totalScore"));
        scoreCol.setPrefWidth(70);
        scoreCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<ResultModel, String> riskCol = new TableColumn<>("Risk Level");
        riskCol.setCellValueFactory(new PropertyValueFactory<>("riskLevel"));
        riskCol.setPrefWidth(100);
        riskCol.setStyle("-fx-alignment: CENTER;");
        riskCol.setCellFactory(col -> new TableCell<ResultModel, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setTextFill(getRiskColor(item));
                    setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                }
            }
        });

        TableColumn<ResultModel, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("takenAt"));
        dateCol.setPrefWidth(120);
        dateCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<ResultModel, Boolean> sessionCol = new TableColumn<>("Session Suggested");
        sessionCol.setCellValueFactory(new PropertyValueFactory<>("suggestSession"));
        sessionCol.setPrefWidth(130);
        sessionCol.setStyle("-fx-alignment: CENTER;");
        sessionCol.setCellFactory(col -> new TableCell<ResultModel, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Yes" : "No");
                    setTextFill(item ? Color.web(toHex(RISK_HIGH)) : Color.web(toHex(RISK_LOW)));
                }
            }
        });

        TableColumn<ResultModel, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(100);
        actionCol.setCellFactory(col -> new ActionButtonCell());
        actionCol.setStyle("-fx-alignment: CENTER;");

        resultsTable.getColumns().addAll(idCol, userIdCol, assessmentIdCol, scoreCol,
                riskCol, dateCol, sessionCol, actionCol);

        resultsTable.getColumns().forEach(col -> col.setStyle(
                "-fx-background-color: white;" +
                        "-fx-text-fill: #" + toHex(TEXT_DARK) + ";" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-width: 0 0 2 0;"
        ));

        VBox.setVgrow(resultsTable, Priority.ALWAYS);
        getChildren().add(resultsTable);
    }

    private void createNotificationBar() {
        notificationLabel = new Label("");
        notificationLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        notificationLabel.setTextFill(Color.web(toHex(ACCENT_GREEN)));
        notificationLabel.setPadding(new Insets(10, 0, 0, 0));
        notificationLabel.setVisible(false);
        getChildren().add(notificationLabel);
    }

    // ═══════════════════════════════════════════════════════════════
    //  ACTION CELL
    // ═══════════════════════════════════════════════════════════════

    class ActionButtonCell extends TableCell<ResultModel, Void> {
        private final Button viewButton;

        public ActionButtonCell() {
            this.viewButton = new Button("View");
            viewButton.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
            viewButton.setTextFill(Color.web(toHex(TEXT_DARK)));
            viewButton.setStyle(
                    "-fx-background-color: #" + toHex(CARD_WHITE) + ";" +
                            "-fx-background-radius: 5;" +
                            "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                            "-fx-border-radius: 5;" +
                            "-fx-padding: 8 15;" +
                            "-fx-cursor: hand;"
            );
            viewButton.setOnAction(e -> {
                ResultModel result = getTableView().getItems().get(getIndex());
                viewResultDetails(result);
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : viewButton);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  RESULT MODEL
    // ═══════════════════════════════════════════════════════════════

    public static class ResultModel {
        private final SimpleIntegerProperty resultId;
        private final SimpleIntegerProperty userId;
        private final SimpleIntegerProperty assessmentId;
        private final SimpleIntegerProperty totalScore;
        private final SimpleStringProperty riskLevel;
        private final SimpleStringProperty takenAt;
        private final SimpleBooleanProperty suggestSession;

        public ResultModel(int resultId, int userId, int assessmentId, int totalScore,
                           String riskLevel, String takenAt, boolean suggestSession) {
            this.resultId      = new SimpleIntegerProperty(resultId);
            this.userId        = new SimpleIntegerProperty(userId);
            this.assessmentId  = new SimpleIntegerProperty(assessmentId);
            this.totalScore    = new SimpleIntegerProperty(totalScore);
            this.riskLevel     = new SimpleStringProperty(riskLevel);
            this.takenAt       = new SimpleStringProperty(takenAt);
            this.suggestSession = new SimpleBooleanProperty(suggestSession);
        }

        public int getResultId()      { return resultId.get(); }
        public int getUserId()        { return userId.get(); }
        public int getAssessmentId()  { return assessmentId.get(); }
        public int getTotalScore()    { return totalScore.get(); }
        public String getRiskLevel()  { return riskLevel.get(); }
        public String getTakenAt()    { return takenAt.get(); }
        public boolean isSuggestSession() { return suggestSession.get(); }

        public SimpleIntegerProperty resultIdProperty()     { return resultId; }
        public SimpleIntegerProperty userIdProperty()       { return userId; }
        public SimpleIntegerProperty assessmentIdProperty() { return assessmentId; }
        public SimpleIntegerProperty totalScoreProperty()   { return totalScore; }
        public SimpleStringProperty riskLevelProperty()     { return riskLevel; }
        public SimpleStringProperty takenAtProperty()       { return takenAt; }
        public SimpleBooleanProperty suggestSessionProperty() { return suggestSession; }
    }

    // ═══════════════════════════════════════════════════════════════
    //  VIEW RESULT DETAILS
    // ═══════════════════════════════════════════════════════════════

    private void viewResultDetails(ResultModel resultModel) {
        try {
            String userType = parentApp.getUserType();
            if ("patient".equals(userType) && resultModel.getUserId() != parentApp.getUserId()) {
                showAlert("Access Denied", "You can only view your own results!", Alert.AlertType.ERROR);
                return;
            }

            AssessmentResult result = null;
            for (AssessmentResult r : controller.getAllResults()) {
                if (r.getResultId() == resultModel.getResultId()) {
                    result = r;
                    break;
                }
            }

            if (result != null) {
                showResultDialog(result);
            } else {
                showAlert("Error", "Result not found!", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showResultDialog(AssessmentResult result) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Result Details");
        dialog.setMinWidth(500);
        dialog.setMinHeight(600);

        VBox content = new VBox(15);
        content.setStyle("-fx-background-color: white;");
        content.setPadding(new Insets(25));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Label titleLabel = new Label("Result Details");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web(toHex(ACCENT_GREEN)));

        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(20);
        detailsGrid.setVgap(10);
        detailsGrid.setPadding(new Insets(15, 0, 15, 0));

        addDetailRow(detailsGrid, 0, "Result ID:",     String.valueOf(result.getResultId()));
        addDetailRow(detailsGrid, 1, "User ID:",       String.valueOf(result.getUserId()));
        addDetailRow(detailsGrid, 2, "Assessment ID:", String.valueOf(result.getAssessmentId()));
        addDetailRow(detailsGrid, 3, "Total Score:",   String.valueOf(result.getTotalScore()));

        Label riskValue = new Label(result.getRiskLevel());
        riskValue.setTextFill(getRiskColor(result.getRiskLevel()));
        riskValue.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        addDetailRow(detailsGrid, 4, "Risk Level:", riskValue);
        addDetailRow(detailsGrid, 5, "Date Taken:", sdf.format(result.getTakenAt()));

        Label interpretationLabel = new Label("Interpretation:");
        interpretationLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        interpretationLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        Label interpretationText = new Label(result.getInterpretation());
        interpretationText.setWrapText(true);
        interpretationText.setFont(Font.font("Segoe UI", 13));

        Label recLabel = new Label("Recommendations:");
        recLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        recLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        Label recText = new Label(result.getRecommendedContent());
        recText.setWrapText(true);
        recText.setFont(Font.font("Segoe UI", 13));

        Label sessionLabel = new Label("Session Suggested: " + (result.isSuggestSession() ? "Yes" : "No"));
        sessionLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        sessionLabel.setTextFill(result.isSuggestSession() ?
                Color.web(toHex(RISK_HIGH)) : Color.web(toHex(RISK_LOW)));

        Button closeButton = new Button("Close");
        closeButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        closeButton.setTextFill(Color.WHITE);
        closeButton.setStyle(
                "-fx-background-color: #" + toHex(ACCENT_GREEN) + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 12 30;" +
                        "-fx-cursor: hand;"
        );
        closeButton.setOnAction(e -> dialog.close());

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(closeButton);

        content.getChildren().addAll(
                titleLabel, new Separator(), detailsGrid, new Separator(),
                interpretationLabel, interpretationText, new Separator(),
                recLabel, recText, new Separator(),
                sessionLabel, buttonBox
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setBorder(null);
        scrollPane.setStyle("-fx-background-color: white;");

        dialog.setScene(new Scene(scrollPane, 500, 600));
        dialog.showAndWait();
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lbl.setTextFill(Color.web(toHex(TEXT_DARK)));
        Label val = new Label(value);
        val.setFont(Font.font("Segoe UI", 13));
        val.setTextFill(Color.web(toHex(TEXT_DARK)));
        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    private void addDetailRow(GridPane grid, int row, String label, Node valueNode) {
        Label lbl = new Label(label);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lbl.setTextFill(Color.web(toHex(TEXT_DARK)));
        grid.add(lbl, 0, row);
        grid.add(valueNode, 1, row);
    }

    private Color getRiskColor(String risk) {
        String r = risk.toLowerCase();
        if (r.contains("high") || r.contains("severe"))       return RISK_HIGH;
        if (r.contains("moderate") || r.contains("mild"))     return RISK_MEDIUM;
        return RISK_LOW;
    }

    // ═══════════════════════════════════════════════════════════════
    //  PROGRESS DIALOG
    // ═══════════════════════════════════════════════════════════════

    private void showProgressDialog() {
        try {
            List<AssessmentResult> results = controller.getUserResults(parentApp.getUserId());

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("My Progress Dashboard");
            dialog.setMinWidth(750);
            dialog.setMinHeight(650);

            VBox root = new VBox(20);
            root.setStyle("-fx-background-color: #f3f3f3;");
            root.setPadding(new Insets(30));

            Label title = new Label("📈 Your Progress Dashboard");
            title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
            title.setTextFill(Color.web(toHex(ACCENT_GREEN)));

            if (results == null || results.isEmpty()) {
                Label empty = new Label("No assessment results yet. Take an assessment to start tracking!");
                empty.setFont(Font.font("Segoe UI", 15));
                empty.setTextFill(Color.GRAY);
                root.getChildren().addAll(title, empty);
            } else {
                results.sort(Comparator.comparing(AssessmentResult::getTakenAt));
                root.getChildren().addAll(
                        title,
                        buildStatsRow(results),
                        buildScoreChart(results),
                        buildRiskBreakdown(results)
                );
            }

            Button closeBtn = new Button("Close");
            closeBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            closeBtn.setTextFill(Color.WHITE);
            closeBtn.setStyle(
                    "-fx-background-color: #" + toHex(ACCENT_GREEN) + ";" +
                            "-fx-background-radius: 5;" +
                            "-fx-padding: 12 30;" +
                            "-fx-cursor: hand;"
            );
            closeBtn.setOnAction(e -> dialog.close());

            HBox btnBox = new HBox(closeBtn);
            btnBox.setAlignment(Pos.CENTER_RIGHT);
            root.getChildren().add(btnBox);

            ScrollPane scroll = new ScrollPane(root);
            scroll.setFitToWidth(true);
            scroll.setStyle("-fx-background-color: #f3f3f3;");

            dialog.setScene(new Scene(scroll, 750, 650));
            dialog.showAndWait();

        } catch (Exception e) {
            showAlert("Error", "Could not load progress: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private HBox buildStatsRow(List<AssessmentResult> results) {
        int total = results.size();
        double avg = results.stream().mapToInt(AssessmentResult::getTotalScore).average().orElse(0);
        int latest = results.get(results.size() - 1).getTotalScore();
        long highRiskCount = results.stream()
                .filter(r -> r.getRiskLevel().equalsIgnoreCase("high")
                        || r.getRiskLevel().equalsIgnoreCase("severe"))
                .count();

        String trend = "";
        if (results.size() >= 2) {
            int prev = results.get(results.size() - 2).getTotalScore();
            trend = latest > prev ? " ↑" : latest < prev ? " ↓" : " →";
        }

        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(
                statCard("Assessments Taken", String.valueOf(total), toHex(ACCENT_GREEN)),
                statCard("Average Score", String.format("%.1f", avg), "5b7fa6"),
                statCard("Latest Score", latest + trend, latest > avg ? "c0392b" : "27ae60"),
                statCard("High Risk Count", String.valueOf(highRiskCount), highRiskCount > 0 ? "c0392b" : "27ae60")
        );
        return row;
    }

    private VBox statCard(String label, String value, String hexColor) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 6, 0, 0, 2);"
        );
        card.setPadding(new Insets(18, 22, 18, 22));
        card.setPrefWidth(160);

        Label val = new Label(value);
        val.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        val.setStyle("-fx-text-fill: #" + hexColor + ";");

        Label lbl = new Label(label);
        lbl.setFont(Font.font("Segoe UI", 12));
        lbl.setTextFill(Color.GRAY);
        lbl.setWrapText(true);
        lbl.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        card.getChildren().addAll(val, lbl);
        return card;
    }

    private LineChart<String, Number> buildScoreChart(List<AssessmentResult> results) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Total Score");
        yAxis.setAutoRanging(true);

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Score Over Time");
        chart.setPrefHeight(280);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(true);
        chart.setAnimated(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");

        for (AssessmentResult r : results) {
            series.getData().add(new XYChart.Data<>(sdf.format(r.getTakenAt()), r.getTotalScore()));
        }

        chart.getData().add(series);
        chart.setStyle(
                ".chart-series-line { -fx-stroke: #" + toHex(ACCENT_GREEN) + "; -fx-stroke-width: 2.5px; }" +
                        ".chart-line-symbol { -fx-background-color: #" + toHex(ACCENT_GREEN) + ", white; }"
        );
        return chart;
    }

    private HBox buildRiskBreakdown(List<AssessmentResult> results) {
        HBox container = new HBox(20);
        container.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 20;"
        );
        container.setAlignment(Pos.CENTER_LEFT);

        Label heading = new Label("Risk Level Breakdown:");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        heading.setTextFill(Color.web(toHex(TEXT_DARK)));
        container.getChildren().add(heading);

        Map<String, Long> counts = new LinkedHashMap<>();
        for (AssessmentResult r : results) {
            counts.merge(r.getRiskLevel(), 1L, Long::sum);
        }

        Map<String, String> riskColors = new LinkedHashMap<>();
        riskColors.put("Low",      "27ae60");
        riskColors.put("Minimal",  "2ecc71");
        riskColors.put("Mild",     "f39c12");
        riskColors.put("Moderate", "e67e22");
        riskColors.put("High",     "c0392b");
        riskColors.put("Severe",   "922b21");

        for (Map.Entry<String, Long> entry : counts.entrySet()) {
            String color = riskColors.getOrDefault(entry.getKey(), "888888");
            double pct = (entry.getValue() * 100.0) / results.size();

            VBox chip = new VBox(4);
            chip.setAlignment(Pos.CENTER);
            chip.setStyle(
                    "-fx-background-color: #" + color + "22;" +
                            "-fx-border-color: #" + color + ";" +
                            "-fx-border-radius: 8;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 10 16;"
            );

            Label riskLabel = new Label(entry.getKey());
            riskLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            riskLabel.setStyle("-fx-text-fill: #" + color + ";");

            Label countLabel = new Label(entry.getValue() + "x  (" + (int) pct + "%)");
            countLabel.setFont(Font.font("Segoe UI", 12));
            countLabel.setStyle("-fx-text-fill: #" + color + ";");

            chip.getChildren().addAll(riskLabel, countLabel);
            container.getChildren().add(chip);
        }

        return container;
    }

    // ═══════════════════════════════════════════════════════════════
    //  DATA REFRESH
    // ═══════════════════════════════════════════════════════════════

    public void refreshData() {
        clearTable();

        String userType = parentApp.getUserType();
        int loggedInUserId = parentApp.getUserId();

        try {
            List<AssessmentResult> results;
            String displayMessage;

            if ("patient".equals(userType)) {
                results = controller.getUserResults(loggedInUserId);
                displayMessage = "Showing your results";
            } else {
                results = controller.getAllResults();
                displayMessage = "Showing all results";
            }

            resultData = FXCollections.observableArrayList();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            if (results != null && !results.isEmpty()) {
                for (AssessmentResult result : results) {
                    resultData.add(new ResultModel(
                            result.getResultId(),
                            result.getUserId(),
                            result.getAssessmentId(),
                            result.getTotalScore(),
                            result.getRiskLevel(),
                            sdf.format(result.getTakenAt()),
                            result.isSuggestSession()
                    ));
                }
                resultsTable.setItems(resultData);
                showNotification(displayMessage + " (" + results.size() + " found)", true);
            } else {
                resultsTable.setItems(FXCollections.observableArrayList());
                showNotification(displayMessage + " - No results found", false);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error loading results: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showNotification(String message, boolean isSuccess) {
        notificationLabel.setText(message);
        notificationLabel.setTextFill(isSuccess ?
                Color.web(toHex(ACCENT_GREEN)) : Color.web(toHex(RISK_HIGH)));
        notificationLabel.setVisible(true);

        new Thread(() -> {
            try {
                Thread.sleep(5000);
                Platform.runLater(() -> notificationLabel.setVisible(false));
            } catch (InterruptedException e) {
                // ignore
            }
        }).start();
    }

    private void clearTable() {
        if (resultData != null) resultData.clear();
        if (resultsTable != null) resultsTable.setItems(FXCollections.observableArrayList());
    }

    public void setUserId(int userId) {
        refreshData();
    }

    // ═══════════════════════════════════════════════════════════════
    //  UTILITIES
    // ═══════════════════════════════════════════════════════════════

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String toHex(Color color) {
        return String.format("%02x%02x%02x",
                (int)(color.getRed()   * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue()  * 255));
    }

    // These thin wrappers let ResultModel use the short import names
    public static class SimpleIntegerProperty extends javafx.beans.property.SimpleIntegerProperty {
        public SimpleIntegerProperty(int value) { super(value); }
    }
    public static class SimpleStringProperty extends javafx.beans.property.SimpleStringProperty {
        public SimpleStringProperty(String value) { super(value); }
    }
    public static class SimpleBooleanProperty extends javafx.beans.property.SimpleBooleanProperty {
        public SimpleBooleanProperty(boolean value) { super(value); }
    }
}