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
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.AssessmentResult;
import models.Session;
import services.AssessmentService;
import services.SessionService;
import services.userservice;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ResultsPanel extends VBox {

    private MentisLoginFrame parentApp;
    private AssessmentResultController controller;
    private TableView<ResultModel> resultsTable;
    private ObservableList<ResultModel> resultData;
    private Label userTypeLabel;
    private Label notificationLabel;

    private userservice userService;
    private AssessmentService assessmentService;

    private static final Color RISK_HIGH = Color.web("#E74C3C");
    private static final Color RISK_MEDIUM = Color.web("#F39C12");
    private static final Color RISK_LOW = Color.web("#27AE60");


    // Symfony-like colors and helpers
    private static final Color PAGE_BG = Color.web("#F8F9FA");
    private static final Color SOFT_GREEN = Color.web("#F1F8E9");
    private static final Color EMERALD = Color.web("#50C878");
    private static final Color EMERALD_DARK = Color.web("#2E7D32");
    private static final Color EMERALD_MID = Color.web("#3A9B5E");
    private static final Color INK = Color.web("#1A3C34");
    private static final Color MUTED = Color.web("#6C757D");
    private static final Color LINE = Color.web("#E9ECEF");
    private static final Color DANGER = Color.web("#E74C3C");
    private static final Color WARNING = Color.web("#F39C12");

    private String css(Color color) {
        return "#" + toHex(color);
    }

    private String gradient(Color left, Color right) {
        return "linear-gradient(to bottom right, " + css(left) + ", " + css(right) + ")";
    }

    private String softShadow() {
        return "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 18, 0, 0, 8);";
    }

    private String cardStyle() {
        return "-fx-background-color: white;" +
                "-fx-background-radius: 24;" +
                "-fx-border-radius: 24;" +
                "-fx-border-color: transparent;" +
                softShadow();
    }

    private String pillInputStyle() {
        return "-fx-background-color: white;" +
                "-fx-background-radius: 999;" +
                "-fx-border-radius: 999;" +
                "-fx-border-color: " + css(EMERALD) + ";" +
                "-fx-border-width: 2;" +
                "-fx-padding: 10 18;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-size: 14px;";
    }

    private Button primaryButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        button.setTextFill(Color.WHITE);
        button.setStyle(
                "-fx-background-color: " + gradient(EMERALD, EMERALD_MID) + ";" +
                        "-fx-background-radius: 999;" +
                        "-fx-padding: 11 26;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(80,200,120,0.30), 16, 0, 0, 7);"
        );
        button.setOnMouseEntered(e -> {
            if (!button.isDisable()) {
                button.setStyle(
                        "-fx-background-color: " + gradient(EMERALD, EMERALD_DARK) + ";" +
                                "-fx-background-radius: 999;" +
                                "-fx-padding: 11 26;" +
                                "-fx-cursor: hand;" +
                                "-fx-translate-y: -2;" +
                                "-fx-effect: dropshadow(gaussian, rgba(80,200,120,0.45), 22, 0, 0, 9);"
                );
            }
        });
        button.setOnMouseExited(e -> {
            if (!button.isDisable()) {
                button.setStyle(
                        "-fx-background-color: " + gradient(EMERALD, EMERALD_MID) + ";" +
                                "-fx-background-radius: 999;" +
                                "-fx-padding: 11 26;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(80,200,120,0.30), 16, 0, 0, 7);"
                );
            }
        });
        return button;
    }

    private Button outlineButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        button.setTextFill(MUTED);
        button.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-radius: 999;" +
                        "-fx-border-color: #CED4DA;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-padding: 10 24;" +
                        "-fx-cursor: hand;"
        );
        button.setOnMouseEntered(e -> {
            if (!button.isDisable()) {
                button.setTextFill(EMERALD_DARK);
                button.setStyle(
                        "-fx-background-color: #F1F8E9;" +
                                "-fx-background-radius: 999;" +
                                "-fx-border-radius: 999;" +
                                "-fx-border-color: " + css(EMERALD) + ";" +
                                "-fx-border-width: 1.5;" +
                                "-fx-padding: 10 24;" +
                                "-fx-cursor: hand;"
                );
            }
        });
        button.setOnMouseExited(e -> {
            if (!button.isDisable()) {
                button.setTextFill(MUTED);
                button.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-background-radius: 999;" +
                                "-fx-border-radius: 999;" +
                                "-fx-border-color: #CED4DA;" +
                                "-fx-border-width: 1.5;" +
                                "-fx-padding: 10 24;" +
                                "-fx-cursor: hand;"
                );
            }
        });
        return button;
    }

    private Label badge(String text, Color bg, Color fg) {
        Label label = new Label(text == null ? "N/A" : text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        label.setTextFill(fg);
        label.setPadding(new Insets(6, 13, 6, 13));
        label.setStyle("-fx-background-color: " + css(bg) + "; -fx-background-radius: 999;");
        return label;
    }

    private void styleTable(TableView<ResultModel> table) {

        table.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 24;" +
                        "-fx-border-radius: 24;" +
                        "-fx-border-color: transparent;" +
                        softShadow()
        );

        table.setFixedCellSize(68);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.setRowFactory(tv -> new TableRow<ResultModel>() {

            @Override
            protected void updateItem(ResultModel item, boolean empty) {

                super.updateItem(item, empty);

                if (empty || item == null) {

                    setStyle("-fx-background-color: transparent;");

                } else {

                    if (getIndex() % 2 == 0) {

                        setStyle("-fx-background-color: white;");

                    } else {

                        setStyle("-fx-background-color: #FBFCFC;");

                    }
                }
            }
        });
    }

    public ResultsPanel(MentisLoginFrame parentApp, AssessmentResultController controller) {
        this.parentApp = parentApp;
        this.controller = controller;
        this.userService = new userservice();
        this.assessmentService = new AssessmentService();

        setStyle("-fx-background-color: " + css(PAGE_BG) + ";");
        setPadding(new Insets(44, 56, 44, 56));
        setSpacing(24);

        createHeader();
        createTable();
        createNotificationBar();
        refreshData();
    }

    private void createHeader() {
        HBox headerPanel = new HBox();
        headerPanel.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);

        String userType = parentApp.getUserType();
        String title = "psychologist".equalsIgnoreCase(userType)
                ? "📈 Patient Assessment Results"
                : "📋 My Assessment Results";

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 40));
        titleLabel.setTextFill(EMERALD_DARK);

        Label subtitle = new Label("psychologist".equalsIgnoreCase(userType)
                ? "Manage all patient assessments"
                : "Your personal mental health journey");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        subtitle.setTextFill(MUTED);

        titleBox.getChildren().addAll(titleLabel, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        userTypeLabel = new Label(getUserTypeDisplay(userType));
        userTypeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        userTypeLabel.setTextFill(EMERALD_DARK);
        userTypeLabel.setPadding(new Insets(0, 8, 0, 0));

        if ("admin".equalsIgnoreCase(userType)) {
            Button assessmentLink = outlineButton("📋 Assessment");
            assessmentLink.setOnAction(e -> parentApp.showAssessmentPanel());
            actions.getChildren().add(assessmentLink);
        }

        if (!"admin".equalsIgnoreCase(userType) && !"psychologist".equalsIgnoreCase(userType)) {
            Button progressBtn = primaryButton("📈 My Progress");
            progressBtn.setOnAction(e -> showProgressDialog());
            actions.getChildren().add(progressBtn);
        }

        Button refreshButton = outlineButton("⟳ Refresh");
        refreshButton.setOnAction(e -> refreshData());

        actions.getChildren().addAll(userTypeLabel, refreshButton);
        headerPanel.getChildren().addAll(titleBox, spacer, actions);

        getChildren().add(headerPanel);
    }

    private String getUserTypeDisplay(String userType) {
        if (userType == null) return "User";
        switch (userType.toLowerCase()) {
            case "patient": return "Patient - Your Results";
            case "psychologist": return "Psychologist - Patient Results";
            case "admin": return "Administrator - All Results";
            default: return "User: " + userType;
        }
    }

    private void createTable() {
        resultsTable = new TableView<>();
        styleTable(resultsTable);
        resultsTable.setPlaceholder(emptyLabel("No results found"));

        TableColumn<ResultModel, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("resultId"));
        idCol.setPrefWidth(55);
        idCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<ResultModel, String> userNameCol = new TableColumn<>("User Name");
        userNameCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
        userNameCol.setPrefWidth(170);
        userNameCol.setCellFactory(col -> new TextCell(false));

        TableColumn<ResultModel, String> assessmentTitleCol = new TableColumn<>("Assessment");
        assessmentTitleCol.setCellValueFactory(new PropertyValueFactory<>("assessmentTitle"));
        assessmentTitleCol.setPrefWidth(220);
        assessmentTitleCol.setCellFactory(col -> new TextCell(true));

        TableColumn<ResultModel, Integer> scoreCol = new TableColumn<>("Score");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("totalScore"));
        scoreCol.setPrefWidth(80);
        scoreCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<ResultModel, String> riskCol = new TableColumn<>("Risk Level");
        riskCol.setCellValueFactory(new PropertyValueFactory<>("riskLevel"));
        riskCol.setPrefWidth(130);
        riskCol.setCellFactory(col -> new TableCell<ResultModel, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : badge(item, getRiskColor(item), Color.WHITE));
                setText(null);
                setAlignment(Pos.CENTER);
            }
        });

        TableColumn<ResultModel, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("takenAt"));
        dateCol.setPrefWidth(120);
        dateCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<ResultModel, Boolean> sessionCol = new TableColumn<>("Session Suggested");
        sessionCol.setCellValueFactory(new PropertyValueFactory<>("suggestSession"));
        sessionCol.setPrefWidth(150);
        sessionCol.setCellFactory(col -> new TableCell<ResultModel, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null :
                        badge(item ? "Yes" : "No", item ? DANGER : MUTED, Color.WHITE));
                setText(null);
                setAlignment(Pos.CENTER);
            }
        });

        TableColumn<ResultModel, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(110);
        actionCol.setCellFactory(col -> new ActionButtonCell());
        actionCol.setStyle("-fx-alignment: CENTER;");

        resultsTable.getColumns().addAll(idCol, userNameCol, assessmentTitleCol, scoreCol,
                riskCol, dateCol, sessionCol, actionCol);

        resultsTable.getColumns().forEach(col -> col.setStyle(
                "-fx-background-color: #F8F9FA;" +
                        "-fx-text-fill: " + css(EMERALD_DARK) + ";" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-color: #E9ECEF;" +
                        "-fx-border-width: 0 0 1 0;"
        ));

        VBox.setVgrow(resultsTable, Priority.ALWAYS);
        getChildren().add(resultsTable);
    }

    private class TextCell extends TableCell<ResultModel, String> {
        private final boolean bold;
        TextCell(boolean bold) { this.bold = bold; }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty || item == null ? null : item);
            setTextFill(bold ? INK : MUTED);
            setFont(Font.font("Segoe UI", bold ? FontWeight.BOLD : FontWeight.NORMAL, 13));
        }
    }

    private void createNotificationBar() {
        notificationLabel = new Label("");
        notificationLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        notificationLabel.setPadding(new Insets(10, 16, 10, 16));
        notificationLabel.setVisible(false);
        notificationLabel.setStyle("-fx-background-color: #F1F8E9; -fx-background-radius: 999;");
        getChildren().add(notificationLabel);
    }

    class ActionButtonCell extends TableCell<ResultModel, Void> {
        private final Button viewButton;

        public ActionButtonCell() {
            this.viewButton = outlineButton("👁 View");
            viewButton.setOnAction(e -> {
                ResultModel result = getTableView().getItems().get(getIndex());
                viewResultDetails(result);
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : viewButton);
            setAlignment(Pos.CENTER);
        }
    }

    public static class ResultModel {
        private final SimpleIntegerProperty resultId;
        private final SimpleIntegerProperty userId;
        private final SimpleIntegerProperty assessmentId;
        private final SimpleStringProperty userName;
        private final SimpleStringProperty assessmentTitle;
        private final SimpleIntegerProperty totalScore;
        private final SimpleStringProperty riskLevel;
        private final SimpleStringProperty takenAt;
        private final SimpleBooleanProperty suggestSession;

        public ResultModel(int resultId, int userId, int assessmentId, String userName, String assessmentTitle,
                           int totalScore, String riskLevel, String takenAt, boolean suggestSession) {
            this.resultId = new SimpleIntegerProperty(resultId);
            this.userId = new SimpleIntegerProperty(userId);
            this.assessmentId = new SimpleIntegerProperty(assessmentId);
            this.userName = new SimpleStringProperty(userName);
            this.assessmentTitle = new SimpleStringProperty(assessmentTitle);
            this.totalScore = new SimpleIntegerProperty(totalScore);
            this.riskLevel = new SimpleStringProperty(riskLevel);
            this.takenAt = new SimpleStringProperty(takenAt);
            this.suggestSession = new SimpleBooleanProperty(suggestSession);
        }

        public int getResultId() { return resultId.get(); }
        public int getUserId() { return userId.get(); }
        public int getAssessmentId() { return assessmentId.get(); }
        public String getUserName() { return userName.get(); }
        public String getAssessmentTitle() { return assessmentTitle.get(); }
        public int getTotalScore() { return totalScore.get(); }
        public String getRiskLevel() { return riskLevel.get(); }
        public String getTakenAt() { return takenAt.get(); }
        public boolean isSuggestSession() { return suggestSession.get(); }

        public SimpleIntegerProperty resultIdProperty() { return resultId; }
        public SimpleIntegerProperty userIdProperty() { return userId; }
        public SimpleIntegerProperty assessmentIdProperty() { return assessmentId; }
        public SimpleStringProperty userNameProperty() { return userName; }
        public SimpleStringProperty assessmentTitleProperty() { return assessmentTitle; }
        public SimpleIntegerProperty totalScoreProperty() { return totalScore; }
        public SimpleStringProperty riskLevelProperty() { return riskLevel; }
        public SimpleStringProperty takenAtProperty() { return takenAt; }
        public SimpleBooleanProperty suggestSessionProperty() { return suggestSession; }
    }

    private void viewResultDetails(ResultModel resultModel) {
        try {
            String userType = parentApp.getUserType();
            if ("patient".equalsIgnoreCase(userType) && resultModel.getUserId() != parentApp.getUserId()) {
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

            if (result != null) showResultDialog(result, resultModel.getUserName(), resultModel.getAssessmentTitle());
            else showAlert("Error", "Result not found!", Alert.AlertType.ERROR);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showResultDialog(AssessmentResult result, String userName, String assessmentTitle) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Result Details");
        dialog.setMinWidth(680);
        dialog.setMinHeight(650);

        VBox content = new VBox(18);
        content.setStyle("-fx-background-color: " + css(PAGE_BG) + ";");
        content.setPadding(new Insets(26));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Label titleLabel = new Label("📋 Result Details");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleLabel.setTextFill(EMERALD_DARK);

        HBox metrics = new HBox(14);
        metrics.getChildren().addAll(
                metricCard("⭐", String.valueOf(result.getTotalScore()), "Total Score", EMERALD),
                metricCard("📈", result.getRiskLevel(), "Risk Level", getRiskColor(result.getRiskLevel())),
                metricCard("📅", sdf.format(result.getTakenAt()), "Date Taken", EMERALD)
        );

        VBox detailsCard = new VBox(12);
        detailsCard.setPadding(new Insets(20));
        detailsCard.setStyle(cardStyle());

        addDetailLabel(detailsCard, "User", userName + " (ID: " + result.getUserId() + ")");
        addDetailLabel(detailsCard, "Assessment", assessmentTitle + " (ID: " + result.getAssessmentId() + ")");
        addDetailLabel(detailsCard, "Session Suggested", result.isSuggestSession() ? "Yes" : "No");

        VBox interpretationCard = textCard("Interpretation", result.getInterpretation());
        VBox recCard = textCard("Recommendations", result.getRecommendedContent());

        Button closeButton = primaryButton("Close");
        closeButton.setOnAction(e -> dialog.close());

        HBox buttonBox = new HBox(closeButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        content.getChildren().addAll(titleLabel, metrics, detailsCard, interpretationCard, recCard, buttonBox);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: " + css(PAGE_BG) + "; -fx-border-color: transparent;");

        dialog.setScene(new Scene(scrollPane, 680, 650));
        dialog.showAndWait();
    }

    private void addDetailLabel(VBox box, String label, String value) {
        Label l = new Label(label + ": " + value);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        l.setTextFill(INK);
        box.getChildren().add(l);
    }

    private VBox textCard(String title, String text) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle(cardStyle());

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        titleLabel.setTextFill(EMERALD_DARK);

        Label body = new Label(text == null ? "N/A" : text);
        body.setFont(Font.font("Segoe UI", 13));
        body.setTextFill(MUTED);
        body.setWrapText(true);

        card.getChildren().addAll(titleLabel, body);
        return card;
    }

    private VBox metricCard(String icon, String value, String label, Color color) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(16));
        card.setPrefWidth(190);
        card.setStyle("-fx-background-color: #F1F8E9; -fx-background-radius: 18;");

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Segoe UI Emoji", 26));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        valueLabel.setTextFill(color);
        valueLabel.setWrapText(true);

        Label small = new Label(label);
        small.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        small.setTextFill(MUTED);

        card.getChildren().addAll(iconLabel, valueLabel, small);
        return card;
    }

    private Color getRiskColor(String risk) {
        if (risk == null) return RISK_LOW;
        String r = risk.toLowerCase();
        if (r.contains("high") || r.contains("severe")) return RISK_HIGH;
        if (r.contains("moderate") || r.contains("mild")) return RISK_MEDIUM;
        return RISK_LOW;
    }

    private void showProgressDialog() {
        try {
            List<AssessmentResult> results = controller.getUserResults(parentApp.getUserId());

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("My Progress Dashboard");
            dialog.setMinWidth(820);
            dialog.setMinHeight(700);

            VBox root = new VBox(22);
            root.setStyle("-fx-background-color: " + css(PAGE_BG) + ";");
            root.setPadding(new Insets(30));

            Label title = new Label("📈 Progress Dashboard");
            title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
            title.setTextFill(EMERALD_DARK);

            if (results == null || results.isEmpty()) {
                VBox empty = new VBox(12);
                empty.setAlignment(Pos.CENTER);
                empty.setPadding(new Insets(44));
                empty.setStyle(cardStyle());
                Label icon = new Label("📈");
                icon.setFont(Font.font("Segoe UI Emoji", 48));
                Label msg = new Label("No assessment results yet. Take an assessment to start tracking!");
                msg.setFont(Font.font("Segoe UI", 15));
                msg.setTextFill(MUTED);
                empty.getChildren().addAll(icon, msg);
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

            Button closeBtn = primaryButton("Close");
            closeBtn.setOnAction(e -> dialog.close());

            HBox btnBox = new HBox(closeBtn);
            btnBox.setAlignment(Pos.CENTER_RIGHT);
            root.getChildren().add(btnBox);

            ScrollPane scroll = new ScrollPane(root);
            scroll.setFitToWidth(true);
            scroll.setStyle("-fx-background-color: " + css(PAGE_BG) + "; -fx-border-color: transparent;");

            dialog.setScene(new Scene(scroll, 820, 700));
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

        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(
                statCard("Assessments Taken", String.valueOf(total), "50C878"),
                statCard("Average Score", String.format("%.1f", avg), "2196F3"),
                statCard("Latest Score", latest + trend, latest > avg ? "D32F2F" : "388E3C"),
                statCard("High Risk Count", String.valueOf(highRiskCount), highRiskCount > 0 ? "D32F2F" : "388E3C")
        );
        return row;
    }

    private VBox statCard(String label, String value, String hexColor) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #F1F8E9; -fx-background-radius: 20;" + softShadow());
        card.setPadding(new Insets(20, 24, 20, 24));
        card.setPrefWidth(170);

        Label val = new Label(value);
        val.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        val.setStyle("-fx-text-fill: #" + hexColor + ";");

        Label lbl = new Label(label);
        lbl.setFont(Font.font("Segoe UI", 12));
        lbl.setTextFill(MUTED);
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
        chart.setPrefHeight(300);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(true);
        chart.setAnimated(false);
        chart.setStyle(cardStyle());

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");

        for (AssessmentResult r : results) {
            series.getData().add(new XYChart.Data<>(sdf.format(r.getTakenAt()), r.getTotalScore()));
        }

        chart.getData().add(series);
        return chart;
    }

    private HBox buildRiskBreakdown(List<AssessmentResult> results) {
        HBox container = new HBox(16);
        container.setStyle(cardStyle() + "-fx-padding: 20;");
        container.setAlignment(Pos.CENTER_LEFT);

        Label heading = new Label("Risk Level Breakdown:");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        heading.setTextFill(EMERALD_DARK);
        container.getChildren().add(heading);

        Map<String, Long> counts = new LinkedHashMap<>();
        for (AssessmentResult r : results) {
            counts.merge(r.getRiskLevel(), 1L, Long::sum);
        }

        for (Map.Entry<String, Long> entry : counts.entrySet()) {
            Color color = getRiskColor(entry.getKey());
            double pct = (entry.getValue() * 100.0) / results.size();

            VBox chip = new VBox(4);
            chip.setAlignment(Pos.CENTER);
            chip.setStyle(
                    "-fx-background-color: " + css(color.deriveColor(0, 1, 1, 0.14)) + ";" +
                            "-fx-border-color: " + css(color) + ";" +
                            "-fx-border-radius: 12;" +
                            "-fx-background-radius: 12;" +
                            "-fx-padding: 10 16;"
            );

            Label riskLabel = new Label(entry.getKey());
            riskLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            riskLabel.setTextFill(color);

            Label countLabel = new Label(entry.getValue() + "x (" + (int) pct + "%)");
            countLabel.setFont(Font.font("Segoe UI", 12));
            countLabel.setTextFill(color);

            chip.getChildren().addAll(riskLabel, countLabel);
            container.getChildren().add(chip);
        }

        return container;
    }

    public void refreshData() {
        clearTable();

        String userType = parentApp.getUserType();
        int loggedInUserId = parentApp.getUserId();

        try {
            List<AssessmentResult> results;
            String displayMessage;

            if ("patient".equalsIgnoreCase(userType)) {
                results = controller.getUserResults(loggedInUserId);
                displayMessage = "Showing your results";
            } else if ("psychologist".equalsIgnoreCase(userType)) {
                results = getResultsForPsychologistPatients(loggedInUserId);
                displayMessage = "Showing results of your patients";
            } else {
                results = controller.getAllResults();
                displayMessage = "Showing all results";
            }

            resultData = FXCollections.observableArrayList();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            if (results != null && !results.isEmpty()) {
                for (AssessmentResult result : results) {
                    String userName = "Unknown User";
                    try {
                        models.user user = userService.getuserById(result.getUserId());
                        if (user != null) userName = user.getFirstName() + " " + user.getLastName();
                    } catch (Exception e) {
                        System.err.println("Error fetching user for ID " + result.getUserId() + ": " + e.getMessage());
                    }

                    String assessmentTitle = "Unknown Assessment";
                    try {
                        models.Assessment assessment = assessmentService.getAssessmentById(result.getAssessmentId());
                        if (assessment != null) assessmentTitle = assessment.getTitle();
                    } catch (Exception e) {
                        System.err.println("Error fetching assessment for ID " + result.getAssessmentId() + ": " + e.getMessage());
                    }

                    resultData.add(new ResultModel(
                            result.getResultId(),
                            result.getUserId(),
                            result.getAssessmentId(),
                            userName,
                            assessmentTitle,
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

    private List<AssessmentResult> getResultsForPsychologistPatients(int psychologistId) throws SQLException {
        List<AssessmentResult> filteredResults = new ArrayList<>();

        try {
            List<AssessmentResult> allResults = controller.getAllResults();
            SessionService sessionService = new SessionService();
            List<Session> psychologistSessions = sessionService.getSessionsByPsychologist(psychologistId);

            Set<Integer> patientIds = new HashSet<>();
            for (Session session : psychologistSessions) {
                if (session.getReservedBy() != null) patientIds.add(session.getReservedBy());
            }

            for (AssessmentResult result : allResults) {
                if (patientIds.contains(result.getUserId())) filteredResults.add(result);
            }

        } catch (Exception e) {
            System.err.println("Error filtering results for psychologist: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }

        return filteredResults;
    }

    private void showNotification(String message, boolean isSuccess) {
        notificationLabel.setText(message);
        notificationLabel.setTextFill(isSuccess ? EMERALD_DARK : DANGER);
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

    private Label emptyLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
        label.setTextFill(MUTED);
        return label;
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
