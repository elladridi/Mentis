package ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.*;
import javafx.concurrent.Task;
import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import services.FaceRecognitionService;
import services.userservice;
import models.user;

import java.io.ByteArrayInputStream;
import java.io.File;

public class FaceIDDialog extends Stage {

    private VideoCapture camera;
    private ImageView cameraView;
    private Label statusLabel;
    private Button captureButton;
    private Button cancelButton;
    private FaceRecognitionService faceService;
    private boolean isRegistration = false;
    private int userId = -1;
    private MentisLoginFrame parentApp;
    private volatile boolean isRunning = true;

    // For multi-sample registration
    private int captureCount = 0;
    private static final int REQUIRED_SAMPLES = 5;

    public FaceIDDialog(MentisLoginFrame parentApp, boolean isRegistration, int userId) {
        this.parentApp = parentApp;
        this.isRegistration = isRegistration;
        this.userId = userId;
        this.faceService = new FaceRecognitionService();

        initModality(Modality.APPLICATION_MODAL);
        setTitle(isRegistration ? "Register Face ID" : "Face ID Login");
        setWidth(640);
        setHeight(580);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #D8E4DE;");

        // Title
        Label title = new Label(isRegistration ? "Register Your Face" : "Face ID Login");
        title.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#588B71"));

        // Camera view
        cameraView = new ImageView();
        cameraView.setFitWidth(500);
        cameraView.setFitHeight(400);
        cameraView.setPreserveRatio(true);
        cameraView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 5);");

        // Status label
        statusLabel = new Label(getInitialInstruction());
        statusLabel.setFont(javafx.scene.text.Font.font("Arial", 14));

        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        captureButton = new Button(isRegistration ? "Capture Face" : "Verify Face");
        captureButton.setStyle(
                "-fx-background-color: #588B71;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-padding: 10 25;" +
                        "-fx-background-radius: 8;"
        );

        cancelButton = new Button("Cancel");
        cancelButton.setStyle(
                "-fx-background-color: #c0392b;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-padding: 10 25;" +
                        "-fx-background-radius: 8;"
        );

        buttonBox.getChildren().addAll(captureButton, cancelButton);

        root.getChildren().addAll(title, cameraView, statusLabel, buttonBox);

        Scene scene = new Scene(root);
        setScene(scene);

        // Start camera in background thread
        startCamera();

        // Button actions
        captureButton.setOnAction(e -> handleCapture());
        cancelButton.setOnAction(e -> {
            isRunning = false;
            stopCamera();
            close();
        });

        setOnCloseRequest(e -> {
            isRunning = false;
            stopCamera();
        });
    }

    private String getInitialInstruction() {
        if (isRegistration) {
            return "Capture 1 of " + REQUIRED_SAMPLES + " - Look straight at camera";
        } else {
            return "Position your face in the camera";
        }
    }

    private String getNextInstruction(int count) {
        switch(count) {
            case 1: return "Look straight at camera";
            case 2: return "Turn head slightly left";
            case 3: return "Turn head slightly right";
            case 4: return "Tilt head up slightly";
            case 5: return "Tilt head down slightly";
            default: return "Look natural";
        }
    }

    private void startCamera() {
        Task<Void> cameraTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                camera = new VideoCapture(0); // 0 = default camera
                if (!camera.isOpened()) {
                    Platform.runLater(() ->
                            statusLabel.setText("❌ Cannot open camera")
                    );
                    return null;
                }

                Mat frame = new Mat();
                while (isRunning && camera.isOpened()) {
                    camera.read(frame);
                    if (!frame.empty()) {
                        MatOfByte buffer = new MatOfByte();
                        org.opencv.imgcodecs.Imgcodecs.imencode(".png", frame, buffer);
                        Image image = new Image(new ByteArrayInputStream(buffer.toArray()));

                        // Update UI on JavaFX thread
                        Platform.runLater(() -> cameraView.setImage(image));
                    }
                    Thread.sleep(30);
                }
                return null;
            }
        };

        Thread thread = new Thread(cameraTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void stopCamera() {
        if (camera != null && camera.isOpened()) {
            camera.release();
        }
    }

    private void handleCapture() {
        captureButton.setDisable(true);

        // Capture current frame
        Image currentFrame = cameraView.getImage();
        if (currentFrame == null) {
            Platform.runLater(() -> {
                statusLabel.setText("❌ No image captured");
                captureButton.setDisable(false);
            });
            return;
        }

        // Convert to file
        File tempFile = FaceRecognitionService.imageToFile(currentFrame);

        if (isRegistration) {
            // REGISTRATION MODE - handle multiple samples
            captureCount++;

            // Update status on UI thread
            String instruction = "Capture " + captureCount + " of " + REQUIRED_SAMPLES +
                    " - " + getNextInstruction(captureCount);
            Platform.runLater(() -> statusLabel.setText(instruction));

            // Run face detection in background thread
            Task<Void> registrationTask = new Task<Void>() {
                @Override
                protected Void call() {
                    boolean success = faceService.registerFaceSample(userId, tempFile);

                    Platform.runLater(() -> {
                        if (success) {
                            if (captureCount >= REQUIRED_SAMPLES) {
                                statusLabel.setText("✅ Registration complete!");
                                showAlert(Alert.AlertType.INFORMATION, "Success",
                                        "Face registered with " + REQUIRED_SAMPLES + " samples!");
                                isRunning = false;
                                stopCamera();
                                close();
                            } else {
                                statusLabel.setText("Capture " + (captureCount + 1) + " of " +
                                        REQUIRED_SAMPLES + " - " +
                                        getNextInstruction(captureCount + 1));
                                captureButton.setDisable(false);
                            }
                        } else {
                            statusLabel.setText("❌ No face detected. Try again.");
                            captureButton.setDisable(false);
                        }
                    });
                    return null;
                }
            };

            new Thread(registrationTask).start();

        } else {
            // LOGIN MODE - verify face
            statusLabel.setText("Processing...");

            Task<Void> loginTask = new Task<Void>() {
                @Override
                protected Void call() {
                    int verifiedUserId = faceService.verifyFace(tempFile);

                    Platform.runLater(() -> {
                        if (verifiedUserId > 0) {
                            statusLabel.setText("✅ Face verified! Logging in...");

                            // Get user and login
                            user loggedUser = userservice.getuserById(verifiedUserId);
                            if (loggedUser != null) {
                                parentApp.login(
                                        loggedUser.getType(),
                                        loggedUser.getId(),
                                        loggedUser.getFirstname() + " " + loggedUser.getLastname()
                                );
                                isRunning = false;
                                stopCamera();
                                close();
                            }
                        } else {
                            statusLabel.setText("❌ Face not recognized. Try again.");
                            captureButton.setDisable(false);
                        }
                    });
                    return null;
                }
            };

            new Thread(loginTask).start();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}