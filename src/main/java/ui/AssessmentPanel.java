package ui;

import controller.AssessmentController;
import javafx.application.Platform;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.List;

public class AssessmentPanel extends VBox {

    private MentisLoginFrame parentApp;
    private AssessmentController controller;
    private TableView<Assessment> assessmentTable;
    private List<Assessment> assessments;

    // Color constants
    private static final Color BACKGROUND_LIGHT = Color.rgb(240, 248, 245);
    private static final Color CARD_WHITE = Color.WHITE;
    private static final Color ACCENT_DARK_GREEN = Color.rgb(60, 120, 90);
    private static final Color BUTTON_LIGHT_GREEN = Color.rgb(160, 200, 180);
    private static final Color BORDER_LIGHT = Color.rgb(200, 220, 210);
    private static final Color TEXT_DARK = Color.rgb(40, 70, 50);
    private static final Color TEXT_LIGHT = Color.rgb(100, 130, 110);

    // Status colors
    private static final Color STATUS_ACTIVE = Color.rgb(39, 174, 96);
    private static final Color STATUS_INACTIVE = Color.rgb(192, 57, 43);
    private static final Color STATUS_DRAFT = Color.rgb(230, 126, 34);
    private static final Color STATUS_DEFAULT = Color.rgb(120, 120, 120);

    // Type colors
    private static final Color TYPE_DEPRESSION = Color.rgb(91, 44, 111);
    private static final Color TYPE_ANXIETY = Color.rgb(192, 57, 43);
    private static final Color TYPE_STRESS = Color.rgb(230, 126, 34);
    private static final Color TYPE_WELLNESS = Color.rgb(39, 174, 96);
    private static final Color TYPE_GENERAL = Color.rgb(52, 152, 219);
    private static final Color TYPE_DEFAULT = Color.rgb(80, 100, 120);

    public AssessmentPanel(MentisLoginFrame parentApp, AssessmentController controller) {
        this.parentApp = parentApp;
        this.controller = controller;

        setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        setPadding(new Insets(40, 50, 40, 50));
        setSpacing(30);

        createHeader();
        createTable();
    }

    private void createHeader() {
        VBox headerContainer = new VBox(30);
        headerContainer.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

        // Tabs
        HBox tabsPanel = new HBox(30);
        tabsPanel.setAlignment(Pos.CENTER_RIGHT);
        tabsPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

        Label assessmentTab = new Label("Assessment");
        assessmentTab.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        assessmentTab.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));
        assessmentTab.setBorder(new Border(
                new BorderStroke(Color.web(toHex(ACCENT_DARK_GREEN)),
                        BorderStrokeStyle.SOLID,
                        CornerRadii.EMPTY,
                        new BorderWidths(0, 0, 3, 0))
        ));
        assessmentTab.setPadding(new Insets(0, 0, 5, 0));

        Label resultsTab = new Label("Results");
        resultsTab.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 18));
        resultsTab.setTextFill(Color.web(toHex(TEXT_LIGHT)));
        resultsTab.setCursor(javafx.scene.Cursor.HAND);
        resultsTab.setOnMouseClicked(e -> parentApp.showResultsPanel());

        tabsPanel.getChildren().addAll(assessmentTab, resultsTab);

        // Title and ADD button
        HBox titlePanel = new HBox();
        titlePanel.setAlignment(Pos.CENTER_LEFT);
        titlePanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

        Label titleLabel = new Label("Manage assessments");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addButton = createAddButton();
        addButton.setOnAction(e -> showAddAssessmentDialog());

        titlePanel.getChildren().addAll(titleLabel, spacer, addButton);
        headerContainer.getChildren().addAll(tabsPanel, titlePanel);

        getChildren().add(headerContainer);
    }

    private Button createAddButton() {
        Button button = new Button("ADD");
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        button.setTextFill(Color.web(toHex(TEXT_DARK)));
        button.setStyle(
                "-fx-background-color: #" + toHex(BUTTON_LIGHT_GREEN) + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 12 40;" +
                        "-fx-cursor: hand;"
        );

        // Hover effect
        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(BUTTON_LIGHT_GREEN.darker()) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 12 40;" +
                                "-fx-cursor: hand;"
                )
        );
        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(BUTTON_LIGHT_GREEN) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 12 40;" +
                                "-fx-cursor: hand;"
                )
        );

        return button;
    }

    private void createTable() {
        assessmentTable = new TableView<>();
        assessmentTable.setStyle("-fx-background-color: white; -fx-border-color: #" + toHex(BORDER_LIGHT) + ";");
        assessmentTable.setFixedCellSize(70);  // This sets the row height
        assessmentTable.setPlaceholder(new Label("No assessments available. Click ADD to create one."));

        // Create columns
        TableColumn<Assessment, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(200);

        TableColumn<Assessment, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(200);
        typeCol.setCellFactory(column -> new TableCell<Assessment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setTextFill(Color.web(toHex(getTypeColor(item))));
                }
            }
        });

        TableColumn<Assessment, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(200);
        statusCol.setCellFactory(column -> new TableCell<Assessment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setTextFill(Color.web(toHex(getStatusColor(item))));
                    setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
                }
            }
        });

        TableColumn<Assessment, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(200);
        descCol.setCellFactory(column -> new TableCell<Assessment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String displayText = item.length() > 50 ? item.substring(0, 47) + "..." : item;
                    setText(displayText);
                }
            }
        });

        TableColumn<Assessment, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(150);
        actionsCol.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Assessment, Void> call(TableColumn<Assessment, Void> param) {
                return new TableCell<>() {
                    private final Button manageButton = new Button("MANAGE");

                    {
                        manageButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                        manageButton.setTextFill(Color.web(toHex(TEXT_DARK)));
                        manageButton.setStyle(
                                "-fx-background-color: #" + toHex(CARD_WHITE) + ";" +
                                        "-fx-background-radius: 5;" +
                                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                                        "-fx-border-radius: 5;" +
                                        "-fx-padding: 8 15;" +
                                        "-fx-cursor: hand;"
                        );

                        manageButton.setOnAction(e -> {
                            Assessment assessment = getTableView().getItems().get(getIndex());
                            showManageDialog(assessment);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(manageButton);
                        }
                    }
                };
            }
        });

        assessmentTable.getColumns().addAll(titleCol, typeCol, statusCol, descCol, actionsCol);

        VBox.setVgrow(assessmentTable, Priority.ALWAYS);
        getChildren().add(assessmentTable);
    }

    private void showManageDialog(Assessment assessment) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("Manage Assessment");
        dialog.setMinWidth(900);
        dialog.setMinHeight(600);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

        // Left panel - Info
        VBox leftPanel = new VBox(20);
        leftPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        leftPanel.setPadding(new Insets(40, 40, 40, 20));
        leftPanel.setPrefWidth(400);

        Label headerLabel = new Label("Manage Assessment");
        headerLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        headerLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        Label titleDisplay = new Label(assessment.getTitle());
        titleDisplay.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleDisplay.setTextFill(Color.web(toHex(TEXT_DARK)));
        titleDisplay.setWrapText(true);

        Label typeDisplay = new Label(assessment.getType());
        typeDisplay.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 18));
        typeDisplay.setTextFill(Color.web(toHex(TEXT_LIGHT)));

        leftPanel.getChildren().addAll(headerLabel, titleDisplay, typeDisplay);

        // Description
        if (assessment.getDescription() != null && !assessment.getDescription().isEmpty()) {
            Label descDisplay = new Label(assessment.getDescription());
            descDisplay.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
            descDisplay.setTextFill(Color.web(toHex(TEXT_DARK)));
            descDisplay.setWrapText(true);
            descDisplay.setMaxWidth(350);
            leftPanel.getChildren().add(descDisplay);
        }

        // Status
        Label statusLabel = new Label("Status: " + assessment.getStatus());
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        statusLabel.setTextFill(Color.web(toHex(getStatusColor(assessment.getStatus()))));
        leftPanel.getChildren().add(statusLabel);

        Label questionLabel = new Label("What would you like to do?");
        questionLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        questionLabel.setTextFill(Color.web(toHex(TEXT_DARK)));
        questionLabel.setPadding(new Insets(30, 0, 0, 0));
        leftPanel.getChildren().add(questionLabel);

        // Right panel - Image
        VBox rightPanel = new VBox();
        rightPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        rightPanel.setPadding(new Insets(40, 40, 40, 20));
        rightPanel.setAlignment(Pos.CENTER);

        StackPane imageContainer = new StackPane();
        imageContainer.setStyle(
                "-fx-background-color: #e6f0eb;" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;"
        );
        imageContainer.setPrefSize(400, 500);
        imageContainer.setMaxSize(400, 500);

        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(380);
        imageView.setFitHeight(480);

        // Try to load assessment image
        if (assessment.getImagePath() != null && !assessment.getImagePath().isEmpty()) {
            try {
                File imgFile = new File(assessment.getImagePath());
                if (imgFile.exists()) {
                    Image image = new Image(new FileInputStream(imgFile));
                    imageView.setImage(image);
                } else {
                    imageView.setImage(null);
                    Label noImageLabel = new Label("Image Not Available");
                    noImageLabel.setFont(Font.font("Segoe UI", 14));
                    noImageLabel.setTextFill(Color.web(toHex(TEXT_LIGHT)));
                    imageContainer.getChildren().add(noImageLabel);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Label errorLabel = new Label("Image Not Available");
                errorLabel.setFont(Font.font("Segoe UI", 14));
                errorLabel.setTextFill(Color.web(toHex(TEXT_LIGHT)));
                imageContainer.getChildren().add(errorLabel);
            }
        } else {
            Label noImageLabel = new Label("No Image Available");
            noImageLabel.setFont(Font.font("Segoe UI", 14));
            noImageLabel.setTextFill(Color.web(toHex(getTypeColor(assessment.getType()))));
            imageContainer.setStyle(imageContainer.getStyle() +
                    "-fx-background-color: #" + toHex(getTypeBackgroundColor(assessment.getType())) + ";");
            imageContainer.getChildren().add(noImageLabel);
        }

        if (imageView.getImage() != null) {
            imageContainer.getChildren().add(imageView);
        }
        rightPanel.getChildren().add(imageContainer);

        // Button panel
        HBox buttonPanel = new HBox(15);
        buttonPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        buttonPanel.setPadding(new Insets(20, 40, 40, 40));
        buttonPanel.setAlignment(Pos.CENTER_LEFT);

        String[] buttonLabels = {"EDIT", "DELETE", "MANAGE\nQUESTIONS", "ACTIVATE", "CANCEL"};
        for (String label : buttonLabels) {
            Button btn = createDialogButton(label.replace("\n", " "));

            if (label.equals("CANCEL")) {
                btn.setOnAction(e -> dialog.close());
            } else if (label.contains("QUESTIONS")) {
                btn.setOnAction(e -> {
                    dialog.close();
                    parentApp.showQuestionPanelWithAssessment(assessment.getAssessmentId());
                });
            } else if (label.equals("EDIT")) {
                btn.setOnAction(e -> {
                    dialog.close();
                    showEditAssessmentDialog(assessment);
                });
            } else if (label.equals("DELETE")) {
                btn.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirm Delete");
                    confirm.setHeaderText(null);
                    confirm.setContentText("Are you sure you want to delete this assessment?\n" +
                            "This will also delete all associated questions.");
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
            } else if (label.equals("ACTIVATE")) {
                btn.setOnAction(e -> {
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
            }

            buttonPanel.getChildren().add(btn);
        }

        // Layout
        HBox contentPanel = new HBox();
        contentPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        contentPanel.getChildren().addAll(leftPanel, rightPanel);
        HBox.setHgrow(leftPanel, Priority.NEVER);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        root.setCenter(contentPanel);
        root.setBottom(buttonPanel);

        Scene scene = new Scene(root);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private Button createDialogButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        button.setTextFill(Color.web(toHex(TEXT_DARK)));
        button.setStyle(
                "-fx-background-color: #" + toHex(BUTTON_LIGHT_GREEN) + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 12 25;" +
                        "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(BUTTON_LIGHT_GREEN.darker()) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 12 25;" +
                                "-fx-cursor: hand;"
                )
        );
        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(BUTTON_LIGHT_GREEN) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 12 25;" +
                                "-fx-cursor: hand;"
                )
        );

        return button;
    }

    // ================= HELPER METHODS =================

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
        if (type == null) return Color.rgb(240, 240, 240);
        switch (type.toLowerCase()) {
            case "depression": return Color.rgb(245, 235, 255);
            case "anxiety": return Color.rgb(255, 235, 235);
            case "stress": return Color.rgb(255, 245, 215);
            case "wellness": return Color.rgb(235, 255, 240);
            case "general": return Color.rgb(235, 245, 255);
            default: return Color.rgb(240, 240, 240);
        }
    }

    private void showAddAssessmentDialog() {
        new AssessmentFormDialog(parentApp, controller, null, false);
    }

    private void showEditAssessmentDialog(Assessment assessment) {
        new AssessmentFormDialog(parentApp, controller, assessment, true);
    }

    public void refreshData() {
        // Clear existing data
        assessmentTable.getItems().clear();

        try {
            assessments = controller.getAllAssessments();

            if (assessments == null || assessments.isEmpty()) {
                assessmentTable.setPlaceholder(new Label("No assessments available. Click ADD to create one."));
                return;
            }

            assessmentTable.getItems().addAll(assessments);

        } catch (SQLException e) {
            e.printStackTrace();
            assessmentTable.setPlaceholder(new Label("Database Error: " + e.getMessage()));
            showAlert("Database Error",
                    "Cannot connect to database.\nError: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            assessmentTable.setPlaceholder(new Label("Error loading data: " + e.getMessage()));
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