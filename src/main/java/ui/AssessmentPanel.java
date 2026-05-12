package ui;

import controller.AssessmentController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import models.Assessment;

import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.List;

public class AssessmentPanel extends VBox {

    private MentisLoginFrame parentApp;
    private AssessmentController controller;
    private TableView<Assessment> assessmentTable;
    private List<Assessment> assessments;

    private static final Color STATUS_ACTIVE = Color.web("#27AE60");
    private static final Color STATUS_INACTIVE = Color.web("#E74C3C");
    private static final Color STATUS_DRAFT = Color.web("#F39C12");
    private static final Color STATUS_DEFAULT = Color.web("#6C757D");

    private static final Color TYPE_DEPRESSION = Color.web("#9B59B6");
    private static final Color TYPE_ANXIETY = Color.web("#E74C3C");
    private static final Color TYPE_STRESS = Color.web("#F39C12");
    private static final Color TYPE_WELLNESS = Color.web("#27AE60");
    private static final Color TYPE_GENERAL = Color.web("#3498DB");
    private static final Color TYPE_DEFAULT = Color.web("#3498DB");


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

    private void styleTable(TableView<Assessment> table) {

        table.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 24;" +
                        "-fx-border-radius: 24;" +
                        "-fx-border-color: transparent;" +
                        softShadow()
        );

        table.setFixedCellSize(68);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.setRowFactory(tv -> new TableRow<Assessment>() {

            @Override
            protected void updateItem(Assessment item, boolean empty) {

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

    public AssessmentPanel(MentisLoginFrame parentApp, AssessmentController controller) {
        this.parentApp = parentApp;
        this.controller = controller;

        setStyle("-fx-background-color: " + css(PAGE_BG) + ";");
        setPadding(new Insets(44, 56, 44, 56));
        setSpacing(28);

        createHeader();
        createTable();
    }

    private void createHeader() {
        VBox headerContainer = new VBox(22);
        headerContainer.setStyle("-fx-background-color: transparent;");

        HBox tabsPanel = new HBox(16);
        tabsPanel.setAlignment(Pos.CENTER_RIGHT);

        Button assessmentTab = outlineButton("📋 Assessment");
        assessmentTab.setTextFill(EMERALD_DARK);
        assessmentTab.setStyle(
                "-fx-background-color: #F1F8E9;" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-radius: 999;" +
                        "-fx-border-color: " + css(EMERALD) + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-padding: 10 24;"
        );

        Button resultsTab = outlineButton("📊 Results");
        resultsTab.setOnAction(e -> parentApp.showResultsPanel());

        tabsPanel.getChildren().addAll(assessmentTab, resultsTab);

        HBox titlePanel = new HBox();
        titlePanel.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label titleLabel = new Label("📋 Manage Assessments");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 40));
        titleLabel.setTextFill(EMERALD_DARK);

        Label subtitle = new Label("Create, update, activate, and organize your Mentis assessments");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        subtitle.setTextFill(MUTED);

        titleBox.getChildren().addAll(titleLabel, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addButton = primaryButton("➕ Add New Assessment");
        addButton.setOnAction(e -> showAddAssessmentDialog());

        titlePanel.getChildren().addAll(titleBox, spacer, addButton);
        headerContainer.getChildren().addAll(tabsPanel, titlePanel);

        getChildren().add(headerContainer);
    }

    private void createTable() {
        assessmentTable = new TableView<>();
        styleTable(assessmentTable);
        assessmentTable.setPlaceholder(emptyLabel("No assessments available. Click Add New Assessment to create one."));

        TableColumn<Assessment, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(220);
        titleCol.setCellFactory(column -> new TableCell<Assessment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setTextFill(INK);
                setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            }
        });

        TableColumn<Assessment, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(160);
        typeCol.setCellFactory(column -> new TableCell<Assessment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : badge(item, getTypeColor(item), Color.WHITE));
                setText(null);
                setAlignment(Pos.CENTER_LEFT);
            }
        });

        TableColumn<Assessment, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(150);
        statusCol.setCellFactory(column -> new TableCell<Assessment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : badge(item, getStatusColor(item), Color.WHITE));
                setText(null);
                setAlignment(Pos.CENTER_LEFT);
            }
        });

        TableColumn<Assessment, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(280);
        descCol.setCellFactory(column -> new TableCell<Assessment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setText("No description");
                    setTextFill(MUTED);
                } else {
                    setText(item.length() > 70 ? item.substring(0, 67) + "..." : item);
                    setTextFill(MUTED);
                }
                setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
            }
        });

        TableColumn<Assessment, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(170);
        actionsCol.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Assessment, Void> call(TableColumn<Assessment, Void> param) {
                return new TableCell<>() {
                    private final Button manageButton = outlineButton("⚙ Manage");

                    {
                        manageButton.setOnAction(e -> {
                            Assessment assessment = getTableView().getItems().get(getIndex());
                            showManageDialog(assessment);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : manageButton);
                        setAlignment(Pos.CENTER);
                    }
                };
            }
        });

        assessmentTable.getColumns().addAll(titleCol, typeCol, statusCol, descCol, actionsCol);
        assessmentTable.getColumns().forEach(col -> col.setStyle(
                "-fx-background-color: #F8F9FA;" +
                        "-fx-text-fill: " + css(EMERALD_DARK) + ";" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-color: #E9ECEF;" +
                        "-fx-border-width: 0 0 1 0;"
        ));

        VBox.setVgrow(assessmentTable, Priority.ALWAYS);
        getChildren().add(assessmentTable);
    }

    private void showManageDialog(Assessment assessment) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("Manage Assessment");
        dialog.setMinWidth(940);
        dialog.setMinHeight(620);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + css(PAGE_BG) + ";");
        root.setPadding(new Insets(26));

        VBox leftPanel = new VBox(18);
        leftPanel.setPadding(new Insets(30));
        leftPanel.setPrefWidth(420);
        leftPanel.setStyle(cardStyle());

        Label headerLabel = new Label("⚙ Manage Assessment");
        headerLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 30));
        headerLabel.setTextFill(EMERALD_DARK);

        Label titleDisplay = new Label(assessment.getTitle());
        titleDisplay.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        titleDisplay.setTextFill(INK);
        titleDisplay.setWrapText(true);

        Label typeBadge = badge(assessment.getType(), getTypeColor(assessment.getType()), Color.WHITE);
        Label statusBadge = badge(assessment.getStatus(), getStatusColor(assessment.getStatus()), Color.WHITE);
        HBox badges = new HBox(10, typeBadge, statusBadge);
        badges.setAlignment(Pos.CENTER_LEFT);

        leftPanel.getChildren().addAll(headerLabel, titleDisplay, badges);

        if (assessment.getDescription() != null && !assessment.getDescription().isEmpty()) {
            Label descDisplay = new Label(assessment.getDescription());
            descDisplay.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
            descDisplay.setTextFill(MUTED);
            descDisplay.setWrapText(true);
            descDisplay.setMaxWidth(350);
            leftPanel.getChildren().add(descDisplay);
        }

        Label questionLabel = new Label("What would you like to do?");
        questionLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        questionLabel.setTextFill(EMERALD_DARK);
        questionLabel.setPadding(new Insets(20, 0, 0, 0));
        leftPanel.getChildren().add(questionLabel);

        VBox rightPanel = new VBox();
        rightPanel.setPadding(new Insets(0, 0, 0, 24));
        rightPanel.setAlignment(Pos.CENTER);

        StackPane imageContainer = new StackPane();
        imageContainer.setStyle(
                "-fx-background-color: " + gradient(getTypeBackgroundColor(assessment.getType()), Color.WHITE) + ";" +
                        "-fx-background-radius: 24;" +
                        "-fx-border-radius: 24;" +
                        "-fx-border-color: #E9ECEF;" +
                        softShadow()
        );
        imageContainer.setPrefSize(420, 430);
        imageContainer.setMaxSize(420, 430);

        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(false);
        imageView.setFitWidth(420);
        imageView.setFitHeight(430);

        if (assessment.getImagePath() != null && !assessment.getImagePath().isEmpty()) {
            try {
                File imgFile = new File(assessment.getImagePath());
                if (imgFile.exists()) {
                    imageView.setImage(new Image(new FileInputStream(imgFile)));
                    imageContainer.getChildren().add(imageView);
                } else {
                    imageContainer.getChildren().add(emptyImageLabel("Image Not Available"));
                }
            } catch (Exception e) {
                imageContainer.getChildren().add(emptyImageLabel("Image Not Available"));
            }
        } else {
            imageContainer.getChildren().add(emptyImageLabel("No Image Available"));
        }

        rightPanel.getChildren().add(imageContainer);

        HBox contentPanel = new HBox(leftPanel, rightPanel);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        root.setCenter(contentPanel);

        HBox buttonPanel = new HBox(12);
        buttonPanel.setPadding(new Insets(22, 0, 0, 0));
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);

        Button editBtn = outlineButton("✏ Edit");
        editBtn.setOnAction(e -> {
            dialog.close();
            showEditAssessmentDialog(assessment);
        });

        Button deleteBtn = outlineButton("🗑 Delete");
        deleteBtn.setTextFill(DANGER);
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Delete");
            confirm.setHeaderText(null);
            confirm.setContentText("Are you sure you want to delete this assessment?\nThis will also delete all associated questions.");
            confirm.initOwner(dialog);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        controller.deleteAssessment(assessment.getAssessmentId());
                        showAlert("Success", "Assessment deleted successfully!", Alert.AlertType.INFORMATION);
                        dialog.close();
                        refreshData();
                    } catch (SQLException ex) {
                        showAlert("Error", "Error deleting assessment: " + ex.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            });
        });

        Button questionsBtn = primaryButton("❔ Manage Questions");
        questionsBtn.setOnAction(e -> {
            dialog.close();
            parentApp.showQuestionPanelWithAssessment(assessment.getAssessmentId());
        });

        Button activateBtn = outlineButton("Active".equals(assessment.getStatus()) ? "🔒 Deactivate" : "✅ Activate");
        activateBtn.setOnAction(e -> {
            try {
                String newStatus = "Active".equals(assessment.getStatus()) ? "Inactive" : "Active";
                if (controller.updateAssessmentStatus(assessment.getAssessmentId(), newStatus)) {
                    showAlert("Success", "Assessment status updated to: " + newStatus, Alert.AlertType.INFORMATION);
                    dialog.close();
                    refreshData();
                }
            } catch (SQLException ex) {
                showAlert("Error", "Error updating status: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        Button cancelBtn = outlineButton("Cancel");
        cancelBtn.setOnAction(e -> dialog.close());

        buttonPanel.getChildren().addAll(editBtn, deleteBtn, questionsBtn, activateBtn, cancelBtn);
        root.setBottom(buttonPanel);

        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    private Label emptyImageLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        label.setTextFill(MUTED);
        return label;
    }

    private Label emptyLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
        label.setTextFill(MUTED);
        return label;
    }

    private Color getStatusColor(String status) {
        if (status == null) return STATUS_DEFAULT;
        switch (status.toLowerCase()) {
            case "active": return STATUS_ACTIVE;
            case "inactive": return STATUS_INACTIVE;
            case "draft": return STATUS_DRAFT;
            default: return STATUS_DEFAULT;
        }
    }

    private Color getTypeColor(String type) {
        if (type == null) return TYPE_DEFAULT;
        switch (type.toLowerCase()) {
            case "depression": return TYPE_DEPRESSION;
            case "anxiety": return TYPE_ANXIETY;
            case "stress": return TYPE_STRESS;
            case "wellness": return TYPE_WELLNESS;
            case "general": return TYPE_GENERAL;
            default: return TYPE_DEFAULT;
        }
    }

    private Color getTypeBackgroundColor(String type) {
        if (type == null) return Color.web("#F8F9FA");
        switch (type.toLowerCase()) {
            case "depression": return Color.web("#F3E5F5");
            case "anxiety": return Color.web("#FFEBEE");
            case "stress": return Color.web("#FFF3E0");
            case "wellness": return Color.web("#E8F5E9");
            case "general": return Color.web("#E3F2FD");
            default: return Color.web("#F8F9FA");
        }
    }

    private void showAddAssessmentDialog() {
        new AssessmentFormDialog(parentApp, controller, null, false);
    }

    private void showEditAssessmentDialog(Assessment assessment) {
        new AssessmentFormDialog(parentApp, controller, assessment, true);
    }

    public void refreshData() {
        assessmentTable.getItems().clear();

        try {
            assessments = controller.getAllAssessments();

            if (assessments == null || assessments.isEmpty()) {
                assessmentTable.setPlaceholder(emptyLabel("No assessments available. Click Add New Assessment to create one."));
                return;
            }

            assessmentTable.getItems().addAll(assessments);

        } catch (SQLException e) {
            e.printStackTrace();
            assessmentTable.setPlaceholder(emptyLabel("Database Error: " + e.getMessage()));
            showAlert("Database Error", "Cannot connect to database.\nError: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            assessmentTable.setPlaceholder(emptyLabel("Error loading data: " + e.getMessage()));
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
}
