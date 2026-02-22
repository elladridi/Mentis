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
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.AssessmentResult;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

public class ResultsPanel extends VBox {

    private MentisLoginFrame parentApp;  // FIXED: Changed from MentisLoginFrame to MentisLoginFrame
    private AssessmentResultController controller;
    private TableView<ResultModel> resultsTable;
    private ObservableList<ResultModel> resultData;
    private TextField userIdField;
    private Label userTypeLabel;
    private ComboBox<String> viewModeComboBox;
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

    public ResultsPanel(MentisLoginFrame parentApp, AssessmentResultController controller) {  // FIXED: Parameter type
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

        // Title
        Label titleLabel = new Label("Assessment Results");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 42));
        titleLabel.setTextFill(Color.web(toHex(ACCENT_GREEN)));

        // Top right panel
        HBox topRightPanel = new HBox(20);
        topRightPanel.setAlignment(Pos.CENTER_RIGHT);
        topRightPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_BEIGE) + ";");

        // User type indicator
        String userType = parentApp.getUserType();
        userTypeLabel = new Label("User: " + userType.toUpperCase());
        userTypeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        userTypeLabel.setTextFill(Color.web(toHex(ACCENT_GREEN)));
        userTypeLabel.setPadding(new Insets(0, 20, 0, 0));

        // Assessment button - ONLY FOR ADMIN
        if ("admin".equals(userType)) {
            Button assessmentLink = createHeaderLink("Assessment");
            // FIXED: Changed from showPanel to showAssessmentPanel
            assessmentLink.setOnAction(e -> parentApp.showAssessmentPanel());
            topRightPanel.getChildren().add(assessmentLink);
            topRightPanel.getChildren().add(createSpacer(20));
        }

        // View mode dropdown (for admin/psychologist only)
        viewModeComboBox = new ComboBox<>();
        viewModeComboBox.getItems().addAll("My Results", "All Results", "Filter by User ID");
        viewModeComboBox.setValue("My Results");
        // FIXED: Removed setFont and added to CSS
        viewModeComboBox.setVisible(false); // Initially hidden
        viewModeComboBox.setPrefWidth(150);
        viewModeComboBox.setStyle(
                "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 13px;"
        );
        viewModeComboBox.setOnAction(e -> onViewModeChanged());

        // User ID field
        userIdField = new TextField();
        userIdField.setPromptText("Enter User ID");
        userIdField.setStyle(
                "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-color: #" + toHex(ACCENT_LIGHT_GREEN) + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-border-color: #" + toHex(ACCENT_GREEN) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-padding: 8 12;"
        );
        userIdField.setPrefWidth(120);
        userIdField.setVisible(false);
        userIdField.setOnAction(e -> refreshData());

        // User ID label
        Label userIdLabel = new Label("User ID:");
        userIdLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        userIdLabel.setTextFill(Color.web(toHex(TEXT_DARK)));
        userIdLabel.setVisible(false);

        // Refresh button
        Button refreshButton = createRefreshButton();

        // Add components to top right panel
        topRightPanel.getChildren().add(userTypeLabel);

        if ("admin".equals(userType) || "psychologist".equals(userType)) {
            topRightPanel.getChildren().addAll(
                    createSpacer(20),
                    viewModeComboBox,
                    createSpacer(10),
                    userIdLabel,
                    userIdField
            );
        }

        topRightPanel.getChildren().addAll(createSpacer(10), refreshButton);

        headerPanel.setLeft(titleLabel);
        headerPanel.setRight(topRightPanel);

        getChildren().add(headerPanel);
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

        // Hover effect with underline
        link.setOnMouseEntered(e ->
                link.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-border-width: 0 0 2 0;" +
                                "-fx-border-color: #" + toHex(ACCENT_GREEN) + ";" +
                                "-fx-cursor: hand;"
                )
        );
        link.setOnMouseExited(e ->
                link.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-border-width: 0;" +
                                "-fx-cursor: hand;"
                )
        );

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

        // Hover effect
        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(ACCENT_GREEN.darker()) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 10 20;" +
                                "-fx-cursor: hand;"
                )
        );
        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(ACCENT_GREEN) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 10 20;" +
                                "-fx-cursor: hand;"
                )
        );

        button.setOnAction(e -> refreshData());
        return button;
    }

    private Region createSpacer(int width) {
        Region spacer = new Region();
        spacer.setPrefWidth(width);
        return spacer;
    }

    private void onViewModeChanged() {
        String selected = viewModeComboBox.getValue();
        if ("Filter by User ID".equals(selected)) {
            userIdField.setVisible(true);
            userIdField.requestFocus();

            // Also show the User ID label
            for (javafx.scene.Node node : ((HBox) userTypeLabel.getParent()).getChildren()) {
                if (node instanceof Label && ((Label) node).getText().equals("User ID:")) {
                    node.setVisible(true);
                    break;
                }
            }
        } else {
            userIdField.setVisible(false);
            userIdField.setText("");

            // Hide the User ID label
            for (javafx.scene.Node node : ((HBox) userTypeLabel.getParent()).getChildren()) {
                if (node instanceof Label && ((Label) node).getText().equals("User ID:")) {
                    node.setVisible(false);
                    break;
                }
            }
        }
        refreshData();
    }

    private void createTable() {
        resultsTable = new TableView<>();
        resultsTable.setStyle("-fx-background-color: white; -fx-border-color: #" + toHex(BORDER_LIGHT) + ";");
        // FIXED: setRowHeight doesn't exist in JavaFX TableView
        resultsTable.setFixedCellSize(50);  // Use setFixedCellSize instead
        resultsTable.setPlaceholder(new Label("No results found"));

        // Alternating row colors
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

        // Create columns
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

        // Style table header
        resultsTable.getColumns().forEach(col -> {
            col.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-text-fill: #" + toHex(TEXT_DARK) + ";" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                            "-fx-border-width: 0 0 2 0;"
            );
        });

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

    // Action Button Cell
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
            if (empty) {
                setGraphic(null);
            } else {
                setGraphic(viewButton);
            }
        }
    }

    // Result Model for TableView
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
            this.resultId = new SimpleIntegerProperty(resultId);
            this.userId = new SimpleIntegerProperty(userId);
            this.assessmentId = new SimpleIntegerProperty(assessmentId);
            this.totalScore = new SimpleIntegerProperty(totalScore);
            this.riskLevel = new SimpleStringProperty(riskLevel);
            this.takenAt = new SimpleStringProperty(takenAt);
            this.suggestSession = new SimpleBooleanProperty(suggestSession);
        }

        public int getResultId() { return resultId.get(); }
        public int getUserId() { return userId.get(); }
        public int getAssessmentId() { return assessmentId.get(); }
        public int getTotalScore() { return totalScore.get(); }
        public String getRiskLevel() { return riskLevel.get(); }
        public String getTakenAt() { return takenAt.get(); }
        public boolean isSuggestSession() { return suggestSession.get(); }

        // Property getters for TableView
        public SimpleIntegerProperty resultIdProperty() { return resultId; }
        public SimpleIntegerProperty userIdProperty() { return userId; }
        public SimpleIntegerProperty assessmentIdProperty() { return assessmentId; }
        public SimpleIntegerProperty totalScoreProperty() { return totalScore; }
        public SimpleStringProperty riskLevelProperty() { return riskLevel; }
        public SimpleStringProperty takenAtProperty() { return takenAt; }
        public SimpleBooleanProperty suggestSessionProperty() { return suggestSession; }
    }

    private void viewResultDetails(ResultModel resultModel) {
        try {
            String userType = parentApp.getUserType();
            AssessmentResult result = null;

            if ("patient".equals(userType)) {
                // Patient can only view their own results
                List<AssessmentResult> results = controller.getUserResults(parentApp.getUserId());
                for (AssessmentResult r : results) {
                    if (r.getResultId() == resultModel.getResultId() &&
                            r.getUserId() == parentApp.getUserId()) {
                        result = r;
                        break;
                    }
                }
            } else {
                // Admin/Psychologist can view any result
                List<AssessmentResult> allResults = controller.getAllResults();
                for (AssessmentResult r : allResults) {
                    if (r.getResultId() == resultModel.getResultId() &&
                            r.getUserId() == resultModel.getUserId()) {
                        result = r;
                        break;
                    }
                }
            }

            if (result != null) {
                showResultDialog(result);
            } else {
                showAlert("Error",
                        "Result not found or you don't have permission to view this result!",
                        Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            showAlert("Error", "Error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showResultDialog(AssessmentResult result) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        // FIXED: Removed initOwner since getScene() doesn't exist in MentisLoginFrame
        // dialog.initOwner(parentApp.getScene().getWindow());
        dialog.setTitle("Result Details");
        dialog.setMinWidth(500);
        dialog.setMinHeight(600);

        VBox content = new VBox(15);
        content.setStyle("-fx-background-color: white;");
        content.setPadding(new Insets(25));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Title
        Label titleLabel = new Label("Result Details");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web(toHex(ACCENT_GREEN)));

        Separator separator1 = new Separator();
        separator1.setStyle("-fx-background-color: #" + toHex(BORDER_LIGHT) + ";");

        // Details table
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(20);
        detailsGrid.setVgap(10);
        detailsGrid.setPadding(new Insets(15, 0, 15, 0));

        addDetailRow(detailsGrid, 0, "Result ID:", String.valueOf(result.getResultId()));
        addDetailRow(detailsGrid, 1, "User ID:", String.valueOf(result.getUserId()));
        addDetailRow(detailsGrid, 2, "Assessment ID:", String.valueOf(result.getAssessmentId()));
        addDetailRow(detailsGrid, 3, "Total Score:", String.valueOf(result.getTotalScore()));

        Label riskValue = new Label(result.getRiskLevel());
        riskValue.setTextFill(getRiskColor(result.getRiskLevel()));
        riskValue.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        addDetailRow(detailsGrid, 4, "Risk Level:", riskValue);

        addDetailRow(detailsGrid, 5, "Date Taken:", sdf.format(result.getTakenAt()));

        Separator separator2 = new Separator();
        separator2.setStyle("-fx-background-color: #" + toHex(BORDER_LIGHT) + ";");

        // Interpretation
        Label interpretationLabel = new Label("Interpretation:");
        interpretationLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        interpretationLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        Label interpretationText = new Label(result.getInterpretation());
        interpretationText.setWrapText(true);
        interpretationText.setFont(Font.font("Segoe UI", 13));

        Separator separator3 = new Separator();
        separator3.setStyle("-fx-background-color: #" + toHex(BORDER_LIGHT) + ";");

        // Recommendations
        Label recLabel = new Label("Recommendations:");
        recLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        recLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        Label recText = new Label(result.getRecommendedContent());
        recText.setWrapText(true);
        recText.setFont(Font.font("Segoe UI", 13));

        Separator separator4 = new Separator();
        separator4.setStyle("-fx-background-color: #" + toHex(BORDER_LIGHT) + ";");

        // Session suggested
        Label sessionLabel = new Label("Session Suggested: " + (result.isSuggestSession() ? "Yes" : "No"));
        sessionLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        sessionLabel.setTextFill(result.isSuggestSession() ?
                Color.web(toHex(RISK_HIGH)) : Color.web(toHex(RISK_LOW)));

        // Close button
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
                titleLabel, separator1, detailsGrid, separator2,
                interpretationLabel, interpretationText, separator3,
                recLabel, recText, separator4, sessionLabel, buttonBox
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setBorder(null);
        scrollPane.setStyle("-fx-background-color: white;");

        Scene scene = new Scene(scrollPane, 500, 600);
        dialog.setScene(scene);
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
        String lowerRisk = risk.toLowerCase();
        if (lowerRisk.contains("high") || lowerRisk.contains("severe")) {
            return RISK_HIGH;
        } else if (lowerRisk.contains("moderate") || lowerRisk.contains("mild")) {
            return RISK_MEDIUM;
        } else {
            return RISK_LOW;
        }
    }

    public void refreshData() {
        clearTable();

        String userType = parentApp.getUserType();
        int loggedInUserId = parentApp.getUserId();

        // Update UI based on user type
        setupUIForUserType(userType);

        try {
            List<AssessmentResult> results = null;
            String notificationMessage = "";

            if ("patient".equals(userType)) {
                // Patient: always see only their own results
                results = controller.getUserResults(loggedInUserId);
                notificationMessage = "Showing your results (" +
                        (results != null ? results.size() : 0) + " found)";

            } else if ("admin".equals(userType) || "psychologist".equals(userType)) {
                // Admin/Psychologist: based on view mode
                String viewMode = viewModeComboBox.getValue();

                if ("My Results".equals(viewMode)) {
                    results = controller.getUserResults(loggedInUserId);
                    notificationMessage = "Showing your results (" +
                            (results != null ? results.size() : 0) + " found)";

                } else if ("All Results".equals(viewMode)) {
                    results = controller.getAllResults();
                    notificationMessage = "Showing all results (" +
                            (results != null ? results.size() : 0) + " found)";

                } else if ("Filter by User ID".equals(viewMode)) {
                    String userIdText = userIdField.getText().trim();
                    if (!userIdText.isEmpty()) {
                        try {
                            int userId = Integer.parseInt(userIdText);
                            results = controller.getUserResults(userId);
                            notificationMessage = "Showing results for User ID: " + userId +
                                    " (" + (results != null ? results.size() : 0) + " found)";
                        } catch (NumberFormatException e) {
                            showAlert("Invalid Input",
                                    "Please enter a valid User ID number!",
                                    Alert.AlertType.WARNING);
                            return;
                        }
                    } else {
                        results = controller.getAllResults();
                        notificationMessage = "Showing all results (" +
                                (results != null ? results.size() : 0) + " found)";
                    }
                }
            }

            resultData = FXCollections.observableArrayList();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            if (results != null && !results.isEmpty()) {
                for (AssessmentResult result : results) {
                    ResultModel model = new ResultModel(
                            result.getResultId(),
                            result.getUserId(),
                            result.getAssessmentId(),
                            result.getTotalScore(),
                            result.getRiskLevel(),
                            sdf.format(result.getTakenAt()),
                            result.isSuggestSession()
                    );
                    resultData.add(model);
                }
                resultsTable.setItems(resultData);
                showNotification(notificationMessage, true);
            } else {
                resultsTable.setItems(FXCollections.observableArrayList());
                showNotification(notificationMessage + " - No results found", false);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error loading results: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupUIForUserType(String userType) {
        if ("patient".equals(userType)) {
            // Patients: simple view, no controls
            viewModeComboBox.setVisible(false);
            userIdField.setVisible(false);

            // Hide User ID label
            if (userTypeLabel.getParent() instanceof HBox) {
                for (javafx.scene.Node node : ((HBox) userTypeLabel.getParent()).getChildren()) {
                    if (node instanceof Label && ((Label) node).getText().equals("User ID:")) {
                        node.setVisible(false);
                        break;
                    }
                }
            }

            userTypeLabel.setText("Patient - Your Results");

        } else if ("admin".equals(userType) || "psychologist".equals(userType)) {
            // Admin/Psychologist: advanced controls
            viewModeComboBox.setVisible(true);
            userTypeLabel.setText(userType.toUpperCase());

            // Show/hide user ID field based on selection
            if (viewModeComboBox.getValue() != null &&
                    viewModeComboBox.getValue().equals("Filter by User ID")) {
                userIdField.setVisible(true);

                // Show User ID label
                if (userTypeLabel.getParent() instanceof HBox) {
                    for (javafx.scene.Node node : ((HBox) userTypeLabel.getParent()).getChildren()) {
                        if (node instanceof Label && ((Label) node).getText().equals("User ID:")) {
                            node.setVisible(true);
                            break;
                        }
                    }
                }
            } else {
                userIdField.setVisible(false);

                // Hide User ID label
                if (userTypeLabel.getParent() instanceof HBox) {
                    for (javafx.scene.Node node : ((HBox) userTypeLabel.getParent()).getChildren()) {
                        if (node instanceof Label && ((Label) node).getText().equals("User ID:")) {
                            node.setVisible(false);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void showNotification(String message, boolean isSuccess) {
        notificationLabel.setText(message);
        notificationLabel.setTextFill(isSuccess ?
                Color.web(toHex(ACCENT_GREEN)) : Color.web(toHex(RISK_HIGH)));
        notificationLabel.setVisible(true);

        // Auto-hide after 5 seconds
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                Platform.runLater(() -> notificationLabel.setVisible(false));
            } catch (InterruptedException e) {
                // Ignore
            }
        }).start();
    }

    private void clearTable() {
        if (resultData != null) {
            resultData.clear();
        }
        if (resultsTable != null) {
            resultsTable.setItems(FXCollections.observableArrayList());
        }
    }

    public void setUserId(int userId) {
        // This is called when panel is shown from patient dashboard
        String userType = parentApp.getUserType();
        if ("patient".equals(userType)) {
            // Auto-refresh for patient
            refreshData();
        } else {
            // For admin/psychologist, set the user ID field
            userIdField.setText(String.valueOf(userId));
            viewModeComboBox.setValue("Filter by User ID");
            refreshData();
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        // FIXED: Removed initOwner since getScene() doesn't exist in MentisLoginFrame
        // alert.initOwner(parentApp.getScene().getWindow());
        alert.showAndWait();
    }

    // ================= UTILITY =================
    private String toHex(Color color) {
        return String.format("%02x%02x%02x",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }

    // SimpleIntegerProperty wrapper
    public static class SimpleIntegerProperty extends javafx.beans.property.SimpleIntegerProperty {
        public SimpleIntegerProperty(int value) { super(value); }
    }

    // SimpleStringProperty wrapper
    public static class SimpleStringProperty extends javafx.beans.property.SimpleStringProperty {
        public SimpleStringProperty(String value) { super(value); }
    }

    // SimpleBooleanProperty wrapper
    public static class SimpleBooleanProperty extends javafx.beans.property.SimpleBooleanProperty {
        public SimpleBooleanProperty(boolean value) { super(value); }
    }
}