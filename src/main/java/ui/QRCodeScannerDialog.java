package ui;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle; // ⭐ ADD THIS IMPORT
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QRCodeScannerDialog extends Stage {

    private MentisLoginFrame parentApp;
    private VBox resultContainer;
    private ImageView qrImageView;

    // Colors
    private static final Color BACKGROUND_LIGHT = Color.rgb(240, 248, 245);
    private static final Color ACCENT_DARK_GREEN = Color.rgb(60, 120, 90);
    private static final Color BUTTON_LIGHT_GREEN = Color.rgb(160, 200, 180);
    private static final Color TEXT_DARK = Color.rgb(40, 70, 50);
    private static final Color TEXT_LIGHT = Color.rgb(100, 130, 110);
    private static final Color BORDER_LIGHT = Color.rgb(200, 220, 210);
    private static final Color CARD_WHITE = Color.WHITE;
    private static final Color ERROR_RED = Color.rgb(192, 57, 43); // ⭐ ADD THIS

    public QRCodeScannerDialog(MentisLoginFrame parentApp) {
        this.parentApp = parentApp;

        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        setTitle("Scan QR Code");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

        root.setTop(createHeader());
        root.setCenter(createContent());
        root.setBottom(createButtonPanel());

        Scene scene = new Scene(root, 650, 750);
        setScene(scene);
        showAndWait();
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 30, 20, 30));

        Label titleLabel = new Label("QR Code Scanner");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        header.getChildren().add(titleLabel);
        return header;
    }

    private VBox createContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20, 30, 20, 30));
        content.setAlignment(Pos.TOP_CENTER);

        // QR Code preview area
        qrImageView = new ImageView();
        qrImageView.setFitWidth(250);
        qrImageView.setFitHeight(250);
        qrImageView.setPreserveRatio(true);
        qrImageView.setStyle("-fx-border-color: #" + toHex(BORDER_LIGHT) + "; -fx-border-width: 2; -fx-border-radius: 5;");

        VBox previewBox = new VBox(10);
        previewBox.setAlignment(Pos.CENTER);
        previewBox.getChildren().addAll(new Label("QR Code Preview"), qrImageView);

        // Buttons for selecting QR code
        HBox buttonRow = new HBox(15);
        buttonRow.setAlignment(Pos.CENTER);

        Button selectFileButton = new Button("Select QR Code Image");
        selectFileButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        selectFileButton.setTextFill(Color.WHITE);
        selectFileButton.setStyle("-fx-background-color: #" + toHex(ACCENT_DARK_GREEN) + "; -fx-background-radius: 5; -fx-padding: 10 20; -fx-cursor: hand;");
        selectFileButton.setOnAction(e -> selectQRCodeFile());

        buttonRow.getChildren().add(selectFileButton);

        // Result container (will show formatted session details)
        resultContainer = new VBox(15);
        resultContainer.setFillWidth(true);
        resultContainer.setStyle("-fx-background-color: white; -fx-border-color: #" + toHex(BORDER_LIGHT) + "; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 20;");
        resultContainer.setVisible(false);

        ScrollPane scrollPane = new ScrollPane(resultContainer);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setBorder(null);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);

        content.getChildren().addAll(previewBox, buttonRow, new Label("Session Details:"), scrollPane);
        return content;
    }

    private HBox createButtonPanel() {
        HBox buttonPanel = new HBox(15);
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);
        buttonPanel.setPadding(new Insets(20, 30, 20, 30));

        Button closeButton = new Button("Close");
        closeButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        closeButton.setTextFill(Color.web(toHex(TEXT_DARK)));
        closeButton.setStyle("-fx-background-color: #" + toHex(BUTTON_LIGHT_GREEN) + "; -fx-background-radius: 5; -fx-padding: 10 25; -fx-cursor: hand;");
        closeButton.setOnAction(e -> close());

        buttonPanel.getChildren().add(closeButton);
        return buttonPanel;
    }

    private void selectQRCodeFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select QR Code Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif")
        );

        File file = fileChooser.showOpenDialog(this);
        if (file != null) {
            try {
                // Display the image
                Image image = new Image(file.toURI().toString());
                qrImageView.setImage(image);

                // Decode QR code
                String result = decodeQRCode(file);
                if (result != null) {
                    displayFormattedSessionDetails(result);
                } else {
                    showError("No QR code found in the image.");
                }
            } catch (Exception e) {
                showError("Error reading QR code: " + e.getMessage());
            }
        }
    }

    private String decodeQRCode(File qrCodeFile) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(qrCodeFile);
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
            Map<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");

            Result result = new MultiFormatReader().decode(bitmap, hints);
            return result.getText();
        } catch (NotFoundException e) {
            return null;
        }
    }

    private void displayFormattedSessionDetails(String qrData) {
        resultContainer.getChildren().clear();
        resultContainer.setVisible(true);

        // Parse QR data
        Map<String, String> sessionData = parseQRData(qrData);

        // Create header
        Label headerLabel = new Label("🎟️ SESSION TICKET");
        headerLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        headerLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));
        headerLabel.setAlignment(Pos.CENTER);
        headerLabel.setMaxWidth(Double.MAX_VALUE);

        // Session title
        String title = sessionData.getOrDefault("Title", "Unknown Session");
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        // Create a card for details
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(20);
        detailsGrid.setVgap(15);
        detailsGrid.setPadding(new Insets(20));
        detailsGrid.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 10; -fx-background-radius: 10;");

        // Add details with icons
        int row = 0;

        // Session ID
        addDetailRow(detailsGrid, row++, "🆔 Session ID:", sessionData.getOrDefault("Session ID", "N/A"));

        // Date
        addDetailRow(detailsGrid, row++, "📅 Date:", sessionData.getOrDefault("Date", "N/A"));

        // Time
        String time = sessionData.getOrDefault("Time", "N/A");
        addDetailRow(detailsGrid, row++, "⏰ Time:", time);

        // Location
        addDetailRow(detailsGrid, row++, "📍 Location:", sessionData.getOrDefault("Location", "N/A"));

        // Type
        addDetailRow(detailsGrid, row++, "📋 Type:", sessionData.getOrDefault("Type", "N/A"));

        // Patient ID
        addDetailRow(detailsGrid, row++, "👤 Patient ID:", sessionData.getOrDefault("Patient ID", "N/A"));

        // Create a stylish ticket-like design
        VBox ticketBox = new VBox(20);
        ticketBox.setStyle("-fx-background-color: white; -fx-border-color: #" + toHex(ACCENT_DARK_GREEN) + "; -fx-border-width: 3; -fx-border-radius: 15; -fx-background-radius: 15; -fx-padding: 25;");
        ticketBox.setAlignment(Pos.CENTER);
        ticketBox.setMaxWidth(500);

        // Add perforated effect (dotted line)
        Separator separator = new Separator();
        separator.setStyle("-fx-border-style: dashed; -fx-border-color: #" + toHex(BORDER_LIGHT) + ";");

        // Add barcode decoration
        HBox barcodeBox = new HBox(2);
        barcodeBox.setAlignment(Pos.CENTER);
        for (int i = 0; i < 20; i++) {
            int height = 20 + (int)(Math.random() * 20);
            Rectangle bar = new Rectangle(); // Now works with import
            bar.setWidth(8);
            bar.setHeight(height);
            bar.setFill(Color.web(toHex(ACCENT_DARK_GREEN)));
            barcodeBox.getChildren().add(bar);
        }

        ticketBox.getChildren().addAll(
                headerLabel,
                titleLabel,
                separator,
                detailsGrid,
                barcodeBox,
                new Label("Present this QR code at the session entrance")
        );

        // Add to container
        resultContainer.getChildren().add(ticketBox);
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        labelNode.setTextFill(Color.web(toHex(TEXT_DARK)));

        Label valueNode = new Label(value);
        valueNode.setFont(Font.font("Segoe UI", 14));
        valueNode.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));
        valueNode.setWrapText(true);

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    private Map<String, String> parseQRData(String qrData) {
        Map<String, String> data = new HashMap<>();
        String[] lines = qrData.split("\n");

        for (String line : lines) {
            if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                String key = parts[0].trim();
                String value = parts[1].trim();
                data.put(key, value);
            }
        }
        return data;
    }

    private void showError(String message) {
        resultContainer.setVisible(true);
        resultContainer.getChildren().clear();

        Label errorLabel = new Label("❌ " + message);
        errorLabel.setFont(Font.font("Segoe UI", 14));
        errorLabel.setTextFill(Color.web(toHex(ERROR_RED)));
        errorLabel.setWrapText(true);

        resultContainer.getChildren().add(errorLabel);
    }

    private String toHex(Color color) {
        return String.format("%02x%02x%02x",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }
}