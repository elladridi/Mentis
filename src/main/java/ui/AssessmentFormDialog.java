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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
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

    // Color constants
    private static final Color BACKGROUND_LIGHT = Color.rgb(240, 248, 245);
    private static final Color CARD_WHITE = Color.WHITE;
    private static final Color ACCENT_DARK_GREEN = Color.rgb(60, 120, 90);
    private static final Color BORDER_LIGHT = Color.rgb(200, 220, 210);
    private static final Color BUTTON_LIGHT_GREEN = Color.rgb(160, 200, 180);
    private static final Color TEXT_DARK = Color.rgb(40, 70, 50);
    private static final Color TEXT_LIGHT = Color.rgb(100, 130, 110);
    private static final Color PLACEHOLDER_BG = Color.rgb(240, 240, 240);
    private static final Color PLACEHOLDER_BORDER = Color.rgb(200, 200, 200);

    public AssessmentFormDialog(MentisLoginFrame parentApp, AssessmentController controller,
                                Assessment assessment, boolean isEdit) {
        this.parentApp = parentApp;
        this.controller = controller;
        this.assessment = assessment;

        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        setTitle(isEdit ? "Edit Assessment" : "Add Assessment");

        // Main layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

        // Header
        root.setTop(createHeader(isEdit));

        // Form
        root.setCenter(createForm());

        // Buttons
        root.setBottom(createButtonPanel(isEdit));

        Scene scene = new Scene(root, 800, 700);
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
        header.setPadding(new Insets(20, 30, 20, 30));
        header.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

        Label titleLabel = new Label(isEdit ? "Edit Assessment" : "Add New Assessment");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        header.getChildren().add(titleLabel);
        return header;
    }

    private ScrollPane createForm() {
        GridPane formPanel = new GridPane();
        formPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        formPanel.setPadding(new Insets(20, 30, 20, 30));
        formPanel.setHgap(15);
        formPanel.setVgap(15);
        formPanel.setAlignment(Pos.TOP_CENTER);

        // Column constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(100);
        col1.setHalignment(javafx.geometry.HPos.RIGHT);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        col2.setFillWidth(true);

        formPanel.getColumnConstraints().addAll(col1, col2);

        int row = 0;

        // Title
        Label titleLabel = new Label("Title:");
        titleLabel.setFont(Font.font("Segoe UI", 14));
        titleLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        titleField = new TextField();
        titleField.setFont(Font.font("Segoe UI", 14));
        titleField.setStyle(
                "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8 12;"
        );
        titleField.setPrefHeight(40);

        formPanel.add(titleLabel, 0, row);
        formPanel.add(titleField, 1, row++);

        // Type
        Label typeLabel = new Label("Type:");
        typeLabel.setFont(Font.font("Segoe UI", 14));
        typeLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        // Type ComboBox - FIXED
        String[] types = {"Depression", "Anxiety", "Stress", "Wellness", "General", "Custom"};
        typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(types);
        typeCombo.setValue(types[0]);
        typeCombo.setPrefHeight(40);
        typeCombo.setMaxWidth(Double.MAX_VALUE);
        typeCombo.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 14px;"
        );

// Status ComboBox - FIXED

        formPanel.add(typeLabel, 0, row);
        formPanel.add(typeCombo, 1, row++);

        // Status
        Label statusLabel = new Label("Status:");
        statusLabel.setFont(Font.font("Segoe UI", 14));
        statusLabel.setTextFill(Color.web(toHex(TEXT_DARK)));
        String[] statuses = {"Active", "Inactive", "Draft"};
        statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(statuses);
        statusCombo.setValue(statuses[0]);
        statusCombo.setPrefHeight(40);
        statusCombo.setMaxWidth(Double.MAX_VALUE);
        statusCombo.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 14px;"
        );


        formPanel.add(statusLabel, 0, row);
        formPanel.add(statusCombo, 1, row++);

        // Description
        Label descLabel = new Label("Description:");
        descLabel.setFont(Font.font("Segoe UI", 14));
        descLabel.setTextFill(Color.web(toHex(TEXT_DARK)));
        descLabel.setAlignment(Pos.TOP_RIGHT);

        descriptionArea = new TextArea();
        descriptionArea.setFont(Font.font("Segoe UI", 14));
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setStyle(
                "-fx-border-color: #" + toHex(BORDER_LIGHT) + ";" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8;"
        );

        formPanel.add(descLabel, 0, row);
        formPanel.add(descriptionArea, 1, row++);

        // Image Upload Section
        Label imageLabel = new Label("Image:");
        imageLabel.setFont(Font.font("Segoe UI", 14));
        imageLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        VBox imageUploadPanel = createImageUploadPanel();
        formPanel.add(imageLabel, 0, row);
        formPanel.add(imageUploadPanel, 1, row++);

        // Wrap in ScrollPane
        ScrollPane scrollPane = new ScrollPane(formPanel);
        scrollPane.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        scrollPane.setBorder(null);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        return scrollPane;
    }

    private VBox createImageUploadPanel() {
        VBox panel = new VBox(10);
        panel.setStyle("-fx-background-color: white; -fx-border-color: #" + toHex(BORDER_LIGHT) +
                "; -fx-border-radius: 5; -fx-background-radius: 5;");
        panel.setPadding(new Insets(15));
        panel.setAlignment(Pos.CENTER);

        // Preview area
        VBox previewContainer = new VBox(10);
        previewContainer.setAlignment(Pos.CENTER);

        imagePreview = new ImageView();
        imagePreview.setFitWidth(300);
        imagePreview.setFitHeight(200);
        imagePreview.setPreserveRatio(true);
        imagePreview.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // Set default placeholder
        setPlaceholderImage();

        uploadStatusLabel = new Label("Drag & drop image here or click to browse");
        uploadStatusLabel.setFont(Font.font("Segoe UI", 12));
        uploadStatusLabel.setTextFill(Color.web(toHex(TEXT_LIGHT)));

        // Make preview clickable
        imagePreview.setPickOnBounds(true);
        imagePreview.setOnMouseClicked(e -> browseForImage());
        imagePreview.setCursor(javafx.scene.Cursor.HAND);

        // Setup drag and drop
        setupDragAndDrop(imagePreview);

        previewContainer.getChildren().addAll(imagePreview, uploadStatusLabel);

        // Control buttons
        HBox buttonPanel = new HBox(10);
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.setPadding(new Insets(5, 0, 0, 0));

        Button browseButton = createSmallButton("Browse...");
        browseButton.setOnAction(e -> browseForImage());

        Button clearButton = createSmallButton("Clear Image");
        clearButton.setOnAction(e -> clearImage());

        buttonPanel.getChildren().addAll(browseButton, clearButton);

        panel.getChildren().addAll(previewContainer, buttonPanel);
        return panel;
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

        // Add filters for image files
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files",
                        "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(this);
        if (selectedFile != null) {
            handleImageFile(selectedFile);
        }
    }

    private boolean handleImageFile(File imageFile) {
        try {
            // Check file size (limit to 5MB)
            long fileSize = imageFile.length();
            if (fileSize > 5 * 1024 * 1024) {
                showAlert("Image file is too large. Maximum size is 5MB.", Alert.AlertType.WARNING);
                return false;
            }

            // Validate it's an image
            Image image = new Image(new FileInputStream(imageFile));
            if (image.isError()) {
                throw new IOException("Not a valid image file");
            }

            // Set preview
            ImageView tempView = new ImageView(image);
            tempView.setFitWidth(300);
            tempView.setFitHeight(200);
            tempView.setPreserveRatio(true);
            imagePreview.setImage(tempView.getImage());

            // Update status
            uploadStatusLabel.setText(imageFile.getName() + " (" +
                    String.format("%.1f", fileSize / 1024.0) + " KB)");
            uploadStatusLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

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
        uploadStatusLabel.setTextFill(Color.web(toHex(TEXT_LIGHT)));
        selectedImageFile = null;
        imagePathToSave = null;
    }

    private void setPlaceholderImage() {
        // Create placeholder image programmatically
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(300, 200);
        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();

        // Fill background
        gc.setFill(PLACEHOLDER_BG);
        gc.fillRect(0, 0, 300, 200);

        // Draw border
        gc.setStroke(PLACEHOLDER_BORDER);
        gc.setLineDashes(5);
        gc.strokeRect(10, 10, 280, 180);

        // Draw camera icon
        gc.setFill(Color.rgb(150, 150, 150));
        gc.fillOval(120, 60, 60, 60);
        gc.setFill(PLACEHOLDER_BG);
        gc.fillOval(130, 70, 40, 40);
        gc.setFill(Color.rgb(150, 150, 150));
        gc.fillOval(140, 80, 20, 20);

        // Draw text
        gc.setFill(Color.rgb(100, 100, 100));
        gc.setFont(Font.font("Arial", 12));
        gc.fillText("No Image Selected", 100, 170);

        imagePreview.setImage(canvas.snapshot(null, null));
        imagePreview.setStyle("-fx-effect: none;");
    }

    private Button createSmallButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        button.setTextFill(Color.web(toHex(TEXT_DARK)));
        button.setStyle(
                "-fx-background-color: #" + toHex(BUTTON_LIGHT_GREEN) + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8 20;" +
                        "-fx-cursor: hand;"
        );
        return button;
    }

    private HBox createButtonPanel(boolean isEdit) {
        HBox buttonPanel = new HBox(15);
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);
        buttonPanel.setPadding(new Insets(20, 30, 20, 30));
        buttonPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

        Button cancelButton = createButton("Cancel", BUTTON_LIGHT_GREEN);
        cancelButton.setOnAction(e -> close());

        Button saveButton = createButton(isEdit ? "Save Changes" : "Add Assessment", ACCENT_DARK_GREEN);
        saveButton.setTextFill(Color.WHITE);
        saveButton.setOnAction(e -> saveAssessment());

        buttonPanel.getChildren().addAll(cancelButton, saveButton);
        return buttonPanel;
    }

    private Button createButton(String text, Color bgColor) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        button.setTextFill(bgColor == ACCENT_DARK_GREEN ? Color.WHITE : Color.web(toHex(TEXT_DARK)));
        button.setStyle(
                "-fx-background-color: #" + toHex(bgColor) + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 12 30;" +
                        "-fx-cursor: hand;"
        );

        // Hover effect
        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(bgColor.darker()) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 12 30;" +
                                "-fx-cursor: hand;"
                )
        );
        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: #" + toHex(bgColor) + ";" +
                                "-fx-background-radius: 5;" +
                                "-fx-padding: 12 30;" +
                                "-fx-cursor: hand;"
                )
        );

        return button;
    }

    private void loadAssessmentData() {
        if (assessment != null) {
            titleField.setText(assessment.getTitle());
            typeCombo.setValue(assessment.getType());
            descriptionArea.setText(assessment.getDescription());
            statusCombo.setValue(assessment.getStatus());

            // Load image if exists
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
            // Create images directory if it doesn't exist
            File imagesDir = new File("assessment_images");
            if (!imagesDir.exists()) {
                imagesDir.mkdirs();
            }

            // Generate unique filename
            String timestamp = String.valueOf(System.currentTimeMillis());
            String originalName = selectedImageFile.getName();
            String extension = "";

            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalName.substring(dotIndex);
            }

            String newFileName = "assessment_" + timestamp + extension;
            File destination = new File(imagesDir, newFileName);

            // Copy file
            Files.copy(selectedImageFile.toPath(), destination.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

            // Return relative path
            return "assessment_images/" + newFileName;

        } catch (IOException e) {
            showAlert("Error saving image: " + e.getMessage(), Alert.AlertType.ERROR);
            return null;
        }
    }

    private void saveAssessment() {
        // Validate inputs
        if (titleField.getText().trim().isEmpty()) {
            showAlert("Please enter a title for the assessment.", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Save image first
            String savedImagePath = saveImageToStorage();
            if (selectedImageFile != null && savedImagePath == null) {
                return; // Image save failed
            }

            if (assessment == null) {
                // Create new assessment
                Assessment newAssessment = new Assessment();
                newAssessment.setTitle(titleField.getText().trim());
                newAssessment.setType(typeCombo.getValue());
                newAssessment.setDescription(descriptionArea.getText().trim());
                newAssessment.setStatus(statusCombo.getValue());
                newAssessment.setImagePath(savedImagePath);

                controller.createAssessment(newAssessment);

                showAlert("Assessment created successfully!", Alert.AlertType.INFORMATION);

            } else {
                // Update existing assessment
                assessment.setTitle(titleField.getText().trim());
                assessment.setType(typeCombo.getValue());
                assessment.setDescription(descriptionArea.getText().trim());
                assessment.setStatus(statusCombo.getValue());

                // Only update image path if a new image was selected
                if (savedImagePath != null) {
                    assessment.setImagePath(savedImagePath);
                }

                controller.updateAssessment(assessment);

                showAlert("Assessment updated successfully!", Alert.AlertType.INFORMATION);
            }

            close();
            // Refresh the assessments panel
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
        alert.showAndWait();
    }

    // ================= UTILITY =================
    private String toHex(Color color) {
        return String.format("%02x%02x%02x",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }
}