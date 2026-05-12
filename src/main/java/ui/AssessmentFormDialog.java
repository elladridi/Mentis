package ui;

import controller.AssessmentController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.Assessment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

public class AssessmentFormDialog extends Stage {

    private MentisLoginFrame parentApp;
    private AssessmentController controller;
    private Assessment assessment;

    private TextField titleField;
    private ComboBox<String> typeCombo;
    private TextArea descriptionArea;
    private ComboBox<String> statusCombo;
    private ImageView imagePreview;
    private File selectedImageFile;
    private String imagePathToSave;
    private Label uploadStatusLabel;

    // Modern color scheme matching Symfony
    private static final String GRADIENT_START = "#50C878";
    private static final String GRADIENT_END = "#2E7D32";
    private static final Color BACKGROUND_LIGHT = Color.rgb(248, 250, 248);
    private static final Color CARD_WHITE = Color.WHITE;
    private static final Color TEXT_DARK = Color.rgb(46, 125, 50);
    private static final Color TEXT_MUTED = Color.rgb(108, 117, 125);
    private static final Color BORDER_COLOR = Color.rgb(222, 226, 230);
    private static final Color PREVIEW_BG = Color.rgb(248, 249, 250);
    private static final Color SUCCESS_BG = Color.rgb(80, 200, 120, 0.1);
    private static final Color ERROR_COLOR = Color.rgb(220, 53, 69);

    public AssessmentFormDialog(MentisLoginFrame parentApp, AssessmentController controller,
                                Assessment assessment, boolean isEdit) {
        this.parentApp = parentApp;
        this.controller = controller;
        this.assessment = assessment;

        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        setTitle(isEdit ? "Edit Assessment - Mentis" : "Add Assessment - Mentis");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + toHex(BACKGROUND_LIGHT) + ";");
        root.setTop(createHeader(isEdit));
        root.setCenter(createForm());
        root.setBottom(createButtonPanel(isEdit));

        Scene scene = new Scene(root, 900, 750);
        setScene(scene);
        setResizable(false);

        if (isEdit && assessment != null) {
            loadAssessmentData();
        }

        showAndWait();
    }

    private HBox createHeader(boolean isEdit) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(25, 35, 20, 35));
        header.setStyle("-fx-background-color: " + toHex(BACKGROUND_LIGHT) + ";");

        VBox headerContent = new VBox(5);

        Label titleLabel = new Label(isEdit ? "✏️ Edit Assessment" : "✨ Create New Assessment");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: " + GRADIENT_END + ";");

        Label subtitleLabel = new Label(isEdit ? "Update assessment details and settings" : "Build a new mental health assessment");
        subtitleLabel.setFont(Font.font("Segoe UI", 14));
        subtitleLabel.setTextFill(TEXT_MUTED);

        headerContent.getChildren().addAll(titleLabel, subtitleLabel);
        header.getChildren().add(headerContent);

        return header;
    }

    private ScrollPane createForm() {
        VBox mainContainer = new VBox(20);
        mainContainer.setStyle("-fx-background-color: " + toHex(BACKGROUND_LIGHT) + ";");
        mainContainer.setPadding(new Insets(10, 35, 20, 35));

        // Create all sections
        VBox basicInfoSection = createBasicInfoSection();
        VBox descriptionSection = createDescriptionSection();
        VBox imageSection = createImageSection();

        // Wrap sections in cards
        VBox basicCard = createCard("📝 Basic Information", basicInfoSection);
        VBox descCard = createCard("📄 Description", descriptionSection);
        VBox imageCard = createCard("🖼️ Assessment Image", imageSection);

        mainContainer.getChildren().addAll(basicCard, descCard, imageCard);

        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setStyle("-fx-background-color: " + toHex(BACKGROUND_LIGHT) + ";");
        scrollPane.setBorder(null);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return scrollPane;
    }

    private VBox createCard(String title, VBox content) {
        VBox card = new VBox(15);
        card.setStyle(
                "-fx-background-color: " + toHex(CARD_WHITE) + ";" +
                        "-fx-background-radius: 16px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);"
        );
        card.setPadding(new Insets(20));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        titleLabel.setStyle("-fx-text-fill: " + GRADIENT_END + ";");

        card.getChildren().addAll(titleLabel, content);
        return card;
    }

    private VBox createBasicInfoSection() {
        VBox section = new VBox(15);

        // Title field
        VBox titleBox = new VBox(5);
        Label titleLabel = new Label("Title *");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        titleLabel.setTextFill(TEXT_DARK);

        titleField = new TextField();
        titleField.setPromptText("Enter assessment title");
        styleTextField(titleField);

        titleBox.getChildren().addAll(titleLabel, titleField);

        // Type and Status row
        HBox rowBox = new HBox(20);

        VBox typeBox = new VBox(5);
        Label typeLabel = new Label("Type");
        typeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        typeLabel.setTextFill(TEXT_DARK);

        String[] types = {"Depression", "Anxiety", "Stress", "Wellness", "General", "Custom"};
        typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(types);
        typeCombo.setValue(types[0]);
        typeCombo.setPrefHeight(40);
        typeCombo.setMaxWidth(Double.MAX_VALUE);
        styleComboBox(typeCombo);
        HBox.setHgrow(typeCombo, Priority.ALWAYS);

        typeBox.getChildren().addAll(typeLabel, typeCombo);

        VBox statusBox = new VBox(5);
        Label statusLabel = new Label("Status");
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        statusLabel.setTextFill(TEXT_DARK);

        String[] statuses = {"Active", "Inactive", "Draft"};
        statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(statuses);
        statusCombo.setValue(statuses[0]);
        statusCombo.setPrefHeight(40);
        statusCombo.setMaxWidth(Double.MAX_VALUE);
        styleComboBox(statusCombo);
        HBox.setHgrow(statusCombo, Priority.ALWAYS);

        statusBox.getChildren().addAll(statusLabel, statusCombo);

        rowBox.getChildren().addAll(typeBox, statusBox);
        HBox.setHgrow(typeBox, Priority.ALWAYS);
        HBox.setHgrow(statusBox, Priority.ALWAYS);

        section.getChildren().addAll(titleBox, rowBox);

        return section;
    }

    private VBox createDescriptionSection() {
        VBox section = new VBox(5);

        Label descLabel = new Label("Description");
        descLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        descLabel.setTextFill(TEXT_DARK);

        descriptionArea = new TextArea();
        descriptionArea.setFont(Font.font("Segoe UI", 14));
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setPromptText("Enter assessment description...");
        styleTextArea(descriptionArea);

        section.getChildren().addAll(descLabel, descriptionArea);

        return section;
    }

    private VBox createImageSection() {
        VBox section = new VBox(10);

        Label imageLabel = new Label("Image");
        imageLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        imageLabel.setTextFill(TEXT_DARK);

        VBox imageContainer = new VBox(10);
        imageContainer.setStyle(
                "-fx-background-color: " + toHex(PREVIEW_BG) + ";" +
                        "-fx-background-radius: 12px;" +
                        "-fx-border-color: " + toHex(BORDER_COLOR) + ";" +
                        "-fx-border-radius: 12px;" +
                        "-fx-border-style: dashed;"
        );
        imageContainer.setPadding(new Insets(20));
        imageContainer.setAlignment(Pos.CENTER);

        imagePreview = new ImageView();
        imagePreview.setFitWidth(300);
        imagePreview.setFitHeight(200);
        imagePreview.setPreserveRatio(true);
        imagePreview.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        setPlaceholderImage();

        // Make preview clickable
        imagePreview.setPickOnBounds(true);
        imagePreview.setOnMouseClicked(e -> browseForImage());
        imagePreview.setCursor(javafx.scene.Cursor.HAND);

        // Setup drag and drop
        setupDragAndDrop(imagePreview);

        uploadStatusLabel = new Label("Drag & drop image here or click to browse");
        uploadStatusLabel.setFont(Font.font("Segoe UI", 12));
        uploadStatusLabel.setTextFill(TEXT_MUTED);

        HBox buttonRow = new HBox(10);
        buttonRow.setAlignment(Pos.CENTER);

        Button browseButton = createSmallButton("Browse...");
        browseButton.setOnAction(e -> browseForImage());

        Button clearButton = createSmallButton("Clear Image");
        clearButton.setOnAction(e -> clearImage());

        buttonRow.getChildren().addAll(browseButton, clearButton);

        imageContainer.getChildren().addAll(imagePreview, uploadStatusLabel, buttonRow);
        section.getChildren().addAll(imageLabel, imageContainer);

        Label helpText = new Label("JPG, PNG, WebP — Max 5MB — Recommended: 800×500px");
        helpText.setFont(Font.font("Segoe UI", 11));
        helpText.setTextFill(TEXT_MUTED);
        section.getChildren().add(helpText);

        return section;
    }

    private Button createSmallButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", 12));
        button.setStyle(
                "-fx-background-color: " + toHex(PREVIEW_BG) + ";" +
                        "-fx-border-color: " + toHex(BORDER_COLOR) + ";" +
                        "-fx-border-radius: 20px;" +
                        "-fx-background-radius: 20px;" +
                        "-fx-padding: 8 20;" +
                        "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: " + toHex(SUCCESS_BG) + ";" +
                                "-fx-border-color: " + GRADIENT_START + ";" +
                                "-fx-border-radius: 20px;" +
                                "-fx-background-radius: 20px;" +
                                "-fx-padding: 8 20;" +
                                "-fx-cursor: hand;"
                )
        );

        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: " + toHex(PREVIEW_BG) + ";" +
                                "-fx-border-color: " + toHex(BORDER_COLOR) + ";" +
                                "-fx-border-radius: 20px;" +
                                "-fx-background-radius: 20px;" +
                                "-fx-padding: 8 20;" +
                                "-fx-cursor: hand;"
                )
        );

        return button;
    }

    private HBox createButtonPanel(boolean isEdit) {
        HBox panel = new HBox(15);
        panel.setAlignment(Pos.CENTER_RIGHT);
        panel.setPadding(new Insets(25, 35, 30, 35));
        panel.setStyle("-fx-background-color: " + toHex(BACKGROUND_LIGHT) + ";");

        Button cancelButton = createModernButton("Cancel", false);
        cancelButton.setOnAction(e -> close());

        Button saveButton = createModernButton(isEdit ? "Save Changes" : "Create Assessment", true);
        saveButton.setOnAction(e -> saveAssessment());

        panel.getChildren().addAll(cancelButton, saveButton);
        return panel;
    }

    private Button createModernButton(String text, boolean isPrimary) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        if (isPrimary) {
            button.setStyle(
                    "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, " + GRADIENT_START + ", " + GRADIENT_END + ");" +
                            "-fx-background-radius: 25px;" +
                            "-fx-text-fill: white;" +
                            "-fx-padding: 12 35;" +
                            "-fx-cursor: hand;"
            );
            button.setOnMouseEntered(e ->
                    button.setStyle(
                            "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, " + GRADIENT_END + ", " + GRADIENT_START + ");" +
                                    "-fx-background-radius: 25px;" +
                                    "-fx-text-fill: white;" +
                                    "-fx-padding: 12 35;" +
                                    "-fx-cursor: hand;"
                    )
            );
        } else {
            button.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-border-color: " + toHex(BORDER_COLOR) + ";" +
                            "-fx-border-radius: 25px;" +
                            "-fx-text-fill: " + toHex(TEXT_MUTED) + ";" +
                            "-fx-padding: 12 35;" +
                            "-fx-cursor: hand;"
            );
            button.setOnMouseEntered(e ->
                    button.setStyle(
                            "-fx-background-color: " + toHex(PREVIEW_BG) + ";" +
                                    "-fx-border-color: " + toHex(BORDER_COLOR) + ";" +
                                    "-fx-border-radius: 25px;" +
                                    "-fx-text-fill: " + toHex(TEXT_DARK) + ";" +
                                    "-fx-padding: 12 35;" +
                                    "-fx-cursor: hand;"
                    )
            );
        }

        button.setOnMouseExited(e -> {
            if (isPrimary) {
                button.setStyle(
                        "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, " + GRADIENT_START + ", " + GRADIENT_END + ");" +
                                "-fx-background-radius: 25px;" +
                                "-fx-text-fill: white;" +
                                "-fx-padding: 12 35;" +
                                "-fx-cursor: hand;"
                );
            } else {
                button.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-border-color: " + toHex(BORDER_COLOR) + ";" +
                                "-fx-border-radius: 25px;" +
                                "-fx-text-fill: " + toHex(TEXT_MUTED) + ";" +
                                "-fx-padding: 12 35;" +
                                "-fx-cursor: hand;"
                );
            }
        });

        return button;
    }

    private void styleTextField(TextField field) {
        field.setStyle(
                "-fx-border-color: " + toHex(BORDER_COLOR) + ";" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-padding: 10;" +
                        "-fx-font-family: 'Segoe UI';"
        );

        field.focusedProperty().addListener((obs, old, nw) -> {
            if (nw) {
                field.setStyle(
                        "-fx-border-color: " + GRADIENT_START + ";" +
                                "-fx-border-radius: 8px;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-padding: 10;" +
                                "-fx-font-family: 'Segoe UI';" +
                                "-fx-border-width: 2px;"
                );
            } else {
                field.setStyle(
                        "-fx-border-color: " + toHex(BORDER_COLOR) + ";" +
                                "-fx-border-radius: 8px;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-padding: 10;" +
                                "-fx-font-family: 'Segoe UI';" +
                                "-fx-border-width: 1px;"
                );
            }
        });
    }

    private void styleTextArea(TextArea field) {
        field.setStyle(
                "-fx-border-color: " + toHex(BORDER_COLOR) + ";" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-padding: 10;" +
                        "-fx-font-family: 'Segoe UI';"
        );

        field.focusedProperty().addListener((obs, old, nw) -> {
            if (nw) {
                field.setStyle(
                        "-fx-border-color: " + GRADIENT_START + ";" +
                                "-fx-border-radius: 8px;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-padding: 10;" +
                                "-fx-font-family: 'Segoe UI';" +
                                "-fx-border-width: 2px;"
                );
            } else {
                field.setStyle(
                        "-fx-border-color: " + toHex(BORDER_COLOR) + ";" +
                                "-fx-border-radius: 8px;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-padding: 10;" +
                                "-fx-font-family: 'Segoe UI';" +
                                "-fx-border-width: 1px;"
                );
            }
        });
    }

    private void styleComboBox(ComboBox<?> comboBox) {
        comboBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: " + toHex(BORDER_COLOR) + ";" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 13px;"
        );

        comboBox.focusedProperty().addListener((obs, old, nw) -> {
            if (nw) {
                comboBox.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-border-color: " + GRADIENT_START + ";" +
                                "-fx-border-radius: 8px;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-font-family: 'Segoe UI';" +
                                "-fx-font-size: 13px;" +
                                "-fx-border-width: 2px;"
                );
            } else {
                comboBox.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-border-color: " + toHex(BORDER_COLOR) + ";" +
                                "-fx-border-radius: 8px;" +
                                "-fx-background-radius: 8px;" +
                                "-fx-font-family: 'Segoe UI';" +
                                "-fx-font-size: 13px;" +
                                "-fx-border-width: 1px;"
                );
            }
        });
    }

    private void setupDragAndDrop(ImageView target) {
        target.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
            }
            event.consume();
        });

        target.setOnDragDropped(event -> {
            javafx.scene.input.Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles()) {
                File file = db.getFiles().get(0);
                success = handleImageFile(file);
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void browseForImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Assessment Image");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files",
                        "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp", "*.webp"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(this);
        if (selectedFile != null) {
            handleImageFile(selectedFile);
        }
    }

    private boolean handleImageFile(File imageFile) {
        try {
            long fileSize = imageFile.length();
            if (fileSize > 5 * 1024 * 1024) {
                showAlert("Image file is too large. Maximum size is 5MB.", Alert.AlertType.WARNING);
                return false;
            }

            Image image = new Image(new FileInputStream(imageFile));
            if (image.isError()) {
                throw new IOException("Not a valid image file");
            }

            imagePreview.setImage(image);
            uploadStatusLabel.setText(imageFile.getName() + " (" +
                    String.format("%.1f", fileSize / 1024.0) + " KB)");
            uploadStatusLabel.setTextFill(Color.web(GRADIENT_END));

            selectedImageFile = imageFile;
            return true;

        } catch (IOException e) {
            showAlert("Invalid image file: " + e.getMessage(), Alert.AlertType.ERROR);
            return false;
        }
    }

    private void clearImage() {
        setPlaceholderImage();
        uploadStatusLabel.setText("Drag & drop image here or click to browse");
        uploadStatusLabel.setTextFill(TEXT_MUTED);
        selectedImageFile = null;
        imagePathToSave = null;
    }

    private void setPlaceholderImage() {
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(300, 200);
        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.rgb(248, 249, 250));
        gc.fillRect(0, 0, 300, 200);

        gc.setStroke(Color.rgb(222, 226, 230));
        gc.setLineDashes(5);
        gc.strokeRect(10, 10, 280, 180);

        gc.setFill(Color.rgb(150, 150, 150));
        gc.fillOval(120, 60, 60, 60);
        gc.setFill(Color.rgb(248, 249, 250));
        gc.fillOval(130, 70, 40, 40);
        gc.setFill(Color.rgb(150, 150, 150));
        gc.fillOval(140, 80, 20, 20);

        gc.setFill(Color.rgb(100, 100, 100));
        gc.setFont(Font.font("Arial", 12));
        gc.fillText("No Image Selected", 100, 170);

        imagePreview.setImage(canvas.snapshot(null, null));
    }

    private void loadAssessmentData() {
        if (assessment != null) {
            titleField.setText(assessment.getTitle());
            typeCombo.setValue(assessment.getType());
            descriptionArea.setText(assessment.getDescription());
            statusCombo.setValue(assessment.getStatus());

            if (assessment.getImagePath() != null && !assessment.getImagePath().isEmpty()) {
                try {
                    File imgFile = new File(assessment.getImagePath());
                    if (imgFile.exists()) {
                        handleImageFile(imgFile);
                    }
                } catch (Exception e) {
                    // Image file not found, keep placeholder
                }
            }
        }
    }

    private String saveImageToStorage() {
        if (selectedImageFile == null) {
            return assessment != null ? assessment.getImagePath() : null;
        }

        try {
            File imagesDir = new File("assessment_images");
            if (!imagesDir.exists()) {
                imagesDir.mkdirs();
            }

            String timestamp = String.valueOf(System.currentTimeMillis());
            String originalName = selectedImageFile.getName();
            String extension = "";

            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalName.substring(dotIndex);
            }

            String newFileName = "assessment_" + timestamp + extension;
            File destination = new File(imagesDir, newFileName);

            Files.copy(selectedImageFile.toPath(), destination.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

            return "assessment_images/" + newFileName;

        } catch (IOException e) {
            showAlert("Error saving image: " + e.getMessage(), Alert.AlertType.ERROR);
            return null;
        }
    }

    private void saveAssessment() {
        if (titleField.getText().trim().isEmpty()) {
            showAlert("Please enter a title for the assessment.", Alert.AlertType.WARNING);
            return;
        }

        try {
            String savedImagePath = saveImageToStorage();
            if (selectedImageFile != null && savedImagePath == null) {
                return;
            }

            if (assessment == null) {
                Assessment newAssessment = new Assessment();
                newAssessment.setTitle(titleField.getText().trim());
                newAssessment.setType(typeCombo.getValue());
                newAssessment.setDescription(descriptionArea.getText().trim());
                newAssessment.setStatus(statusCombo.getValue());
                newAssessment.setImagePath(savedImagePath);

                controller.createAssessment(newAssessment);
                showAlert("✨ Assessment created successfully!", Alert.AlertType.INFORMATION);

            } else {
                assessment.setTitle(titleField.getText().trim());
                assessment.setType(typeCombo.getValue());
                assessment.setDescription(descriptionArea.getText().trim());
                assessment.setStatus(statusCombo.getValue());

                if (savedImagePath != null) {
                    assessment.setImagePath(savedImagePath);
                }

                controller.updateAssessment(assessment);
                showAlert("✅ Assessment updated successfully!", Alert.AlertType.INFORMATION);
            }

            close();
            if (parentApp != null) {
                parentApp.showAssessmentPanel();
            }

        } catch (SQLException e) {
            showAlert("Error saving assessment: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Error" :
                type == Alert.AlertType.WARNING ? "Warning" : "Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(this);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: " + toHex(CARD_WHITE) + ";" +
                        "-fx-background-radius: 12px;" +
                        "-fx-padding: 20;"
        );

        alert.showAndWait();
    }

    private String toHex(Color color) {
        return String.format("#%02x%02x%02x",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }

    private String toHex(String hexColor) {
        return hexColor;
    }
}