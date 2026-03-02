package ui;

import controller.ContentNodeController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import models.ContentNode;
import models.user;
import services.userservice;
import services.LocalTTSService;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import services.SummarizationService;

/**
 * ContentUploadPanel - UI for uploading and managing PDF content.
 * FIXED: Admin table now visible with TTS buttons
 */
public class ContentUploadPanel extends VBox {

    private ContentNodeController controller;
    private MentisLoginFrame parentApp;
    private int currentUserId;
    private String currentUserRole;

    // Color scheme
    private static final Color BACKGROUND_GREEN = Color.rgb(240, 245, 242);
    private static final Color ACCENT_GREEN = Color.rgb(88, 139, 113);
    private static final Color ACCENT_GREEN_LIGHT = Color.rgb(200, 225, 210);
    private static final Color ERROR_RED = Color.rgb(220, 80, 80);
    private static final Color TEXT_DARK = Color.rgb(60, 70, 80);
    private static final Color TEXT_GRAY = Color.rgb(128, 128, 128);
    private static final Color SUCCESS_GREEN = Color.rgb(76, 175, 80);
    private static final Color WARNING_ORANGE = Color.rgb(255, 152, 0);
    private static final Color EDIT_BLUE = Color.rgb(52, 152, 219);
    private static final Color CARD_BG = Color.rgb(255, 255, 255);
    private static final Color CARD_BORDER = Color.rgb(220, 220, 220);
    private static final Color CARD_HOVER = Color.rgb(245, 250, 248);
    private static final Color TTS_BLUE = Color.rgb(52, 152, 219);
    private static final Color TTS_STOP_RED = Color.rgb(231, 76, 60);

    // UI Components
    private TextField titleField;
    private TextArea descriptionArea;
    private Label pdfFileLabel;
    private File selectedPdfFile;
    private TableView<ContentNode> contentTable;
    private Button undoButton;
    private Button redoButton;
    private Label roleInfoLabel;
    private VBox formSection;
    private HBox toolbarBox;
    private ComboBox<user> patientAssignmentCombo;
    private ListView<user> assignedPatientsListView;
    private List<user> allPatients;
    private ScrollPane mainScrollPane;

    // TTS Components
    private Button globalStopButton;
    private boolean isPlaying = false;

    // For patient card view
    private VBox patientViewContainer;
    private GridPane patientCardsGrid;
    private ScrollPane patientCardsScrollPane;
    private Label patientViewTitle;

    // For edit functionality
    private ContentNode currentEditingNode = null;
    private Button updateButton;
    private Button cancelButton;
    private Label editingLabel;

    private Label createSummaryBadge(ContentNode node) {
        if (node.getDescription().length() > 200) {
            String summary = SummarizationService.smartSummarize(node.getDescription(), 2);

            Label summaryLabel = new Label("📋 " + summary);
            summaryLabel.setFont(Font.font("Arial", 11));
            summaryLabel.setTextFill(Color.web("#7f8c8d"));
            summaryLabel.setWrapText(true);
            summaryLabel.setMaxHeight(60);

            Tooltip tooltip = new Tooltip(summary);
            tooltip.setMaxWidth(300);
            tooltip.setWrapText(true);
            summaryLabel.setTooltip(tooltip);

            return summaryLabel;
        }
        return null;
    }


    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private void loadAllPatients() {
        try {
            allPatients = controller.getAllPatients();
            System.out.println(" PATIENT LIST LOADED:");
            System.out.println("  - Total patients: " + allPatients.size());

            for (user u : allPatients) {
                System.out.println("    ✓ ID: " + u.getId() + " | Name: " + u.getFirstName() + " " + u.getLastName() + " | Email: " + u.getEmail());
            }

            if (allPatients.isEmpty()) {
                System.err.println(" WARNING: No patients found in database!");
            }

        } catch (Exception e) {
            allPatients = new ArrayList<>();
            System.err.println(" Error loading patients: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public ContentUploadPanel(MentisLoginFrame parentApp) {
        this.parentApp = parentApp;
        this.controller = new ContentNodeController();

        this.currentUserId = parentApp.getUserId();
        this.currentUserRole = parentApp.getUserType();

        this.controller.setCurrentUser(currentUserId, currentUserRole);

        System.out.println(" ContentUploadPanel created");
        System.out.println("  - User ID: " + currentUserId);
        System.out.println("  - User Role: " + currentUserRole);
        System.out.println("  - Is Admin: " + "admin".equalsIgnoreCase(currentUserRole));

        loadAllPatients();

        setStyle("-fx-background-color: #" + toHex(BACKGROUND_GREEN) + ";");
        setPadding(new Insets(0));

        mainScrollPane = new ScrollPane();
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setFitToHeight(true);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScrollPane.setStyle("-fx-background-color: #" + toHex(BACKGROUND_GREEN) + "; -fx-background: #" + toHex(BACKGROUND_GREEN) + ";");
        mainScrollPane.setBorder(null);

        VBox contentBox = new VBox(20);
        contentBox.setPadding(new Insets(20, 40, 20, 40));
        contentBox.setStyle("-fx-background-color: #" + toHex(BACKGROUND_GREEN) + ";");

        // Initialize patient card view
        createPatientCardView();

        initializeUI(contentBox);

        mainScrollPane.setContent(contentBox);
        getChildren().add(mainScrollPane);
        VBox.setVgrow(mainScrollPane, Priority.ALWAYS);
    }

    // Create TTS toolbar for admin/psychologist
    private HBox createTTSToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10));
        toolbar.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-radius: 8; " +
                "-fx-border-color: #" + toHex(ACCENT_GREEN_LIGHT) + "; -fx-border-width: 1;");

        Label ttsLabel = new Label("🔊 Text-to-Speech:");
        ttsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        Button demoBtn = new Button("▶️ Demo");
        demoBtn.setStyle("-fx-background-color: #" + toHex(TTS_BLUE) + "; -fx-text-fill: white; -fx-padding: 8 16; -fx-cursor: hand; -fx-background-radius: 5;");
        demoBtn.setOnAction(e -> {
            String demoText = "Welcome to Mentis content management system. You can listen to any content by clicking the listen button.";
            LocalTTSService.speakAsync(demoText, () -> {
                System.out.println("✅ Demo playback finished");
            });
        });

        globalStopButton = new Button("⏹️ Stop All");
        globalStopButton.setStyle("-fx-background-color: #" + toHex(TTS_STOP_RED) + "; -fx-text-fill: white; -fx-padding: 8 16; -fx-cursor: hand; -fx-background-radius: 5;");
        globalStopButton.setDisable(true);
        globalStopButton.setOnAction(e -> {
            LocalTTSService.stopSpeaking();
            globalStopButton.setDisable(true);
        });

        // Update stop button state
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(500), e -> {
            boolean speaking = LocalTTSService.isSpeaking();
            globalStopButton.setDisable(!speaking);
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label infoLabel = new Label("Local TTS - No internet required");
        infoLabel.setFont(Font.font("Arial", 11));
        infoLabel.setTextFill(Color.web(toHex(TEXT_GRAY)));

        toolbar.getChildren().addAll(ttsLabel, demoBtn, globalStopButton, spacer, infoLabel);

        return toolbar;
    }

    // Create patient card view
    private void createPatientCardView() {
        patientViewContainer = new VBox(20);
        patientViewContainer.setPadding(new Insets(20));
        patientViewContainer.setStyle("-fx-background-color: #" + toHex(BACKGROUND_GREEN) + ";");
        patientViewContainer.setVisible(false);
        patientViewContainer.setManaged(false);

        // Header
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 20, 0));

        patientViewTitle = new Label("Your Content Library");
        patientViewTitle.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        patientViewTitle.setTextFill(Color.web(toHex(ACCENT_GREEN)));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(patientViewTitle, spacer);

        // Cards grid - 2 columns
        patientCardsGrid = new GridPane();
        patientCardsGrid.setHgap(20);
        patientCardsGrid.setVgap(20);
        patientCardsGrid.setPadding(new Insets(10));
        patientCardsGrid.setAlignment(Pos.TOP_CENTER);

        patientCardsScrollPane = new ScrollPane(patientCardsGrid);
        patientCardsScrollPane.setFitToWidth(true);
        patientCardsScrollPane.setStyle("-fx-background-color: #" + toHex(BACKGROUND_GREEN) + "; -fx-background: #" + toHex(BACKGROUND_GREEN) + ";");
        patientCardsScrollPane.setBorder(null);
        patientCardsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        patientCardsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        patientViewContainer.getChildren().addAll(headerBox, patientCardsScrollPane);
        VBox.setVgrow(patientCardsScrollPane, Priority.ALWAYS);
    }

    // Load patient content as cards
    private void loadPatientCards() {
        patientCardsGrid.getChildren().clear();

        try {
            List<ContentNode> patientContent = controller.getViewableContentNodes(null);

            if (patientContent.isEmpty()) {
                Label emptyLabel = new Label("No content available for you");
                emptyLabel.setFont(Font.font("Arial", 18));
                emptyLabel.setTextFill(Color.web(toHex(TEXT_GRAY)));
                patientCardsGrid.add(emptyLabel, 0, 0, 2, 1);
                GridPane.setHalignment(emptyLabel, javafx.geometry.HPos.CENTER);
                return;
            }

            int col = 0;
            int row = 0;

            for (ContentNode node : patientContent) {
                VBox card = createContentCard(node);
                patientCardsGrid.add(card, col, row);

                col++;
                if (col >= 2) { // 2 cards per row
                    col = 0;
                    row++;
                }
            }

        } catch (Exception e) {
            showAlert("Error", "Failed to load content: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Create individual content card with TTS buttons
    private VBox createContentCard(ContentNode node) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setPrefWidth(400);
        card.setPrefHeight(320); // Increased height for TTS buttons
        card.setStyle("-fx-background-color: #" + toHex(CARD_BG) + ";" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: #" + toHex(CARD_BORDER) + ";" +
                "-fx-border-radius: 12;" +
                "-fx-border-width: 1;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        // Hover effect
        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color: #" + toHex(CARD_HOVER) + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #" + toHex(ACCENT_GREEN) + ";" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 2;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);" +
                        "-fx-cursor: hand;")
        );

        card.setOnMouseExited(e ->
                card.setStyle("-fx-background-color: #" + toHex(CARD_BG) + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #" + toHex(CARD_BORDER) + ";" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);")
        );

        // Click to directly open PDF
        card.setOnMouseClicked(e -> openPdfDirectly(node));

        // Header with PDF icon
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setSpacing(10);

        Label iconLabel;
        if (node.getPdfPath() != null && !node.getPdfPath().isEmpty()) {
            iconLabel = new Label("📄");
            iconLabel.setFont(Font.font("Arial", 24));
            iconLabel.setTextFill(Color.web(toHex(SUCCESS_GREEN)));
        } else {
            iconLabel = new Label("📝");
            iconLabel.setFont(Font.font("Arial", 24));
            iconLabel.setTextFill(Color.web(toHex(TEXT_GRAY)));
        }

        Label pdfStatusLabel;
        if (node.getPdfPath() != null && !node.getPdfPath().isEmpty()) {
            pdfStatusLabel = new Label("PDF Available");
            pdfStatusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            pdfStatusLabel.setTextFill(Color.web(toHex(SUCCESS_GREEN)));
            pdfStatusLabel.setStyle("-fx-background-color: #e8f5e9; -fx-background-radius: 10; -fx-padding: 4 10;");
        } else {
            pdfStatusLabel = new Label("No PDF");
            pdfStatusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            pdfStatusLabel.setTextFill(Color.web(toHex(TEXT_GRAY)));
            pdfStatusLabel.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 10; -fx-padding: 4 10;");
        }

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(iconLabel, pdfStatusLabel, headerSpacer);

        // Title
        Label titleLabel = new Label(node.getTitle());
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web(toHex(TEXT_DARK)));
        titleLabel.setWrapText(true);
        titleLabel.setMaxHeight(60);

        // Description (truncated to 100 chars)
        String desc = node.getDescription();
        if (desc.length() > 100) {
            desc = desc.substring(0, 97) + "...";
        }
        Label descLabel = new Label(desc);
        descLabel.setFont(Font.font("Arial", 13));
        descLabel.setTextFill(Color.web(toHex(TEXT_GRAY)));
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(80);
        Label summaryBadge = createSummaryBadge(node);
        if (summaryBadge != null) {
            card.getChildren().add(4, summaryBadge); // Add before audioBox
        }
        // TTS Audio buttons
        HBox audioBox = new HBox(10);
        audioBox.setAlignment(Pos.CENTER_LEFT);
        audioBox.setPadding(new Insets(5, 0, 5, 0));

        Button listenBtn = new Button("🔊 Listen");
        listenBtn.setStyle("-fx-background-color: #" + toHex(TTS_BLUE) + "; -fx-text-fill: white; -fx-padding: 6 12; -fx-cursor: hand; -fx-font-size: 11px; -fx-background-radius: 5;");
        listenBtn.setOnAction(event -> {
            event.consume();
            String textToSpeak = "Title: " + node.getTitle() + ". summary " + node.getDescription();
            LocalTTSService.speakAsync(textToSpeak, null);
        });

        Button stopBtn = new Button("⏹️");
        stopBtn.setStyle("-fx-background-color: #" + toHex(TTS_STOP_RED) + "; -fx-text-fill: white; -fx-padding: 6 12; -fx-cursor: hand; -fx-font-size: 11px; -fx-background-radius: 5;");
        stopBtn.setOnAction(event -> {
            event.consume();
            LocalTTSService.stopSpeaking();
        });

        audioBox.getChildren().addAll(listenBtn, stopBtn);

        HBox footerBox = new HBox();
        footerBox.setAlignment(Pos.CENTER_RIGHT);
        footerBox.setPadding(new Insets(10, 0, 0, 0));

        Label dateLabel = new Label("Added: " + node.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        dateLabel.setFont(Font.font("Arial", 11));
        dateLabel.setTextFill(Color.web(toHex(TEXT_GRAY)));

        footerBox.getChildren().add(dateLabel);

        card.getChildren().addAll(headerBox, titleLabel, descLabel, audioBox, footerBox);

        return card;
    }

    private void openPdfDirectly(ContentNode node) {
        if (node.getPdfPath() == null || node.getPdfPath().isEmpty()) {
            showAlert("No PDF", "This content does not have an associated PDF file.", Alert.AlertType.INFORMATION);
            return;
        }

        try {
            File pdfFile = new File(node.getPdfPath());
            if (pdfFile.exists()) {
                if (currentUserId > 0) {
                    controller.logAccess(currentUserId, node.getNodeId());
                    System.out.println("✅ ACCESS LOGGED: User " + currentUserId + " opened: " + node.getTitle());
                }

                java.awt.Desktop.getDesktop().open(pdfFile);
                System.out.println("📄 Opened PDF: " + pdfFile.getAbsolutePath());

            } else {
                System.err.println("❌ PDF file not found: " + pdfFile.getAbsolutePath());
                showAlert("Error", "PDF file not found at:\n" + node.getPdfPath(), Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            System.err.println("❌ Error opening PDF: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Could not open PDF: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void initializeUI(VBox container) {
        Label pageTitle = new Label("Content Management");
        pageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        pageTitle.setTextFill(Color.web(toHex(ACCENT_GREEN)));

        HBox roleInfoBox = createRoleInfoBox();

        container.getChildren().add(pageTitle);
        container.getChildren().add(roleInfoBox);

        boolean isAdmin = "admin".equalsIgnoreCase(currentUserRole);
        boolean isPatient = "patient".equalsIgnoreCase(currentUserRole);
        boolean canCreate = "admin".equalsIgnoreCase(currentUserRole) || "psychologist".equalsIgnoreCase(currentUserRole);

        if (isAdmin) {
            System.out.println("✅ ADMIN detected - creating Undo/Redo toolbar");
            toolbarBox = createToolbar();
            if (toolbarBox != null) {
                container.getChildren().add(toolbarBox);
            }
        }

        container.getChildren().add(new Separator());

        formSection = createFormSection();
        formSection.setVisible(canCreate);
        formSection.setManaged(canCreate);
        container.getChildren().add(formSection);

        if (isAdmin) {
            HBox ttsToolbar = createTTSToolbar();
            container.getChildren().add(ttsToolbar);
        }

        container.getChildren().add(patientViewContainer);

        if (isPatient) {
            patientViewContainer.setVisible(true);
            patientViewContainer.setManaged(true);
            loadPatientCards();
        } else {
            patientViewContainer.setVisible(false);
            patientViewContainer.setManaged(false);

            VBox tableSection = createTableSection();
            container.getChildren().add(tableSection);
            VBox.setVgrow(tableSection, Priority.ALWAYS);
        }
    }

    private HBox createRoleInfoBox() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(5, 0, 5, 0));

        boolean isAdmin = "admin".equalsIgnoreCase(currentUserRole);
        boolean isPatient = "patient".equalsIgnoreCase(currentUserRole);
        boolean canCreate = "admin".equalsIgnoreCase(currentUserRole) || "psychologist".equalsIgnoreCase(currentUserRole);

        String roleText = "Logged in as: " + currentUserRole.toUpperCase() + " | ";

        if (canCreate) {
            roleText += "Can create/edit/assign content";
            if (isAdmin) {
                roleText += " | Can delete ANY content | Undo/Redo available";
            } else {
                roleText += " | Can delete own content";
            }
        } else if (isPatient) {
            roleText += "Click any card to open PDF directly | 🔊 Listen button to hear content";
        } else {
            roleText += "View only - Content assigned to you";
        }

        roleInfoLabel = new Label(roleText);
        roleInfoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        roleInfoLabel.setTextFill(canCreate ? Color.web(toHex(ACCENT_GREEN)) : Color.web(toHex(WARNING_ORANGE)));
        roleInfoLabel.setPadding(new Insets(8, 15, 8, 15));
        roleInfoLabel.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-border-radius: 20; -fx-border-color: #" +
                (canCreate ? toHex(ACCENT_GREEN) : toHex(WARNING_ORANGE)) + "; -fx-border-width: 1;");

        box.getChildren().add(roleInfoLabel);
        return box;
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(12, 15, 12, 15));
        toolbar.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-radius: 8; " +
                "-fx-border-color: #" + toHex(ACCENT_GREEN) + "; -fx-border-width: 1;");

        Label historyLabel = new Label("History:");
        historyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        historyLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        undoButton = new Button("Undo");
        undoButton.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        undoButton.setTextFill(Color.web(toHex(TEXT_DARK)));
        undoButton.setStyle("-fx-background-color: #" + toHex(ACCENT_GREEN_LIGHT) + ";" +
                "-fx-background-radius: 5;" + "-fx-padding: 8 16;" + "-fx-cursor: hand; " +
                "-fx-border-color: #" + toHex(ACCENT_GREEN) + "; -fx-border-width: 1; -fx-border-radius: 5;");
        undoButton.setOnAction(e -> performUndo());

        redoButton = new Button("Redo");
        redoButton.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        redoButton.setTextFill(Color.web(toHex(TEXT_DARK)));
        redoButton.setStyle("-fx-background-color: #" + toHex(ACCENT_GREEN_LIGHT) + ";" +
                "-fx-background-radius: 5;" + "-fx-padding: 8 16;" + "-fx-cursor: hand; " +
                "-fx-border-color: #" + toHex(ACCENT_GREEN) + "; -fx-border-width: 1; -fx-border-radius: 5;");
        redoButton.setOnAction(e -> performRedo());

        updateHistoryButtons();

        toolbar.getChildren().addAll(historyLabel, undoButton, redoButton);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label infoLabel = new Label("You can undo/redo create and update operations");
        infoLabel.setFont(Font.font("Arial", 11));
        infoLabel.setTextFill(Color.web(toHex(TEXT_GRAY)));

        toolbar.getChildren().addAll(spacer, infoLabel);

        return toolbar;
    }

    private VBox createFormSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-radius: 8; " +
                "-fx-border-color: #" + toHex(ACCENT_GREEN_LIGHT) + "; -fx-border-width: 1;");

        editingLabel = new Label("");
        editingLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        editingLabel.setTextFill(Color.web(toHex(EDIT_BLUE)));
        editingLabel.setVisible(false);

        Label sectionTitle = new Label("Create New Content");
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        sectionTitle.setTextFill(Color.web(toHex(ACCENT_GREEN)));

        // Title field
        VBox titleBox = new VBox(5);
        Label titleLabel = new Label("Title *");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        titleField = new TextField();
        titleField.setPromptText("Enter content title (3-255 characters)");
        titleField.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #" + toHex(ACCENT_GREEN_LIGHT) + "; -fx-border-radius: 5;");
        titleBox.getChildren().addAll(titleLabel, titleField);

        // Description field
        VBox descBox = new VBox(5);
        Label descLabel = new Label("Description *");
        descLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Enter content description (10-5000 characters)");
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #" + toHex(ACCENT_GREEN_LIGHT) + "; -fx-border-radius: 5;");
        descBox.getChildren().addAll(descLabel, descriptionArea);

        // PDF file upload
        VBox pdfBox = new VBox(5);
        Label pdfLabel = new Label("PDF File (Optional)");
        pdfLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        HBox pdfInputBox = new HBox(10);
        pdfInputBox.setAlignment(Pos.CENTER_LEFT);

        Button selectPdfBtn = createActionButton("Select PDF");
        selectPdfBtn.setOnAction(e -> selectPdfFile());

        pdfFileLabel = new Label("No file selected");
        pdfFileLabel.setFont(Font.font("Arial", 11));
        pdfFileLabel.setTextFill(Color.web(toHex(TEXT_GRAY)));

        pdfInputBox.getChildren().addAll(selectPdfBtn, pdfFileLabel);
        pdfBox.getChildren().addAll(pdfLabel, pdfInputBox);

        // Patient Assignment Section
        VBox assignmentBox = new VBox(10);
        assignmentBox.setPadding(new Insets(10, 0, 5, 0));
        assignmentBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-padding: 15;");

        Label assignmentLabel = new Label("Assign to Patients:");
        assignmentLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        assignmentLabel.setTextFill(Color.web(toHex(ACCENT_GREEN)));

        Label selectedCountLabel = new Label("0 patients selected");
        selectedCountLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        selectedCountLabel.setTextFill(Color.web(toHex(TEXT_GRAY)));

        HBox addPatientBox = new HBox(10);
        addPatientBox.setAlignment(Pos.CENTER_LEFT);

        patientAssignmentCombo = new ComboBox<>();
        patientAssignmentCombo.setItems(javafx.collections.FXCollections.observableArrayList(allPatients));
        patientAssignmentCombo.setCellFactory(param -> new ListCell<user>() {
            @Override
            protected void updateItem(user item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getFirstName() + " " + item.getLastName() + " (ID: " + item.getId() + ")");
                }
            }
        });
        patientAssignmentCombo.setButtonCell(new ListCell<user>() {
            @Override
            protected void updateItem(user item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getFirstName() + " " + item.getLastName());
                }
            }
        });
        patientAssignmentCombo.setPromptText("Select patient to assign");
        patientAssignmentCombo.setPrefWidth(350);
        patientAssignmentCombo.setStyle("-fx-font-size: 12px;");

        Button addPatientBtn = new Button("Add");
        addPatientBtn.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        addPatientBtn.setStyle("-fx-background-color: #" + toHex(ACCENT_GREEN) + "; -fx-text-fill: white; " +
                "-fx-background-radius: 5; -fx-padding: 8 20; -fx-cursor: hand;");
        addPatientBtn.setOnAction(e -> {
            addSelectedPatient();
            selectedCountLabel.setText(assignedPatientsListView.getItems().size() + " patients selected");
        });

        addPatientBox.getChildren().addAll(patientAssignmentCombo, addPatientBtn);

        assignedPatientsListView = new ListView<>();
        assignedPatientsListView.setPrefHeight(120);
        assignedPatientsListView.setStyle("-fx-background-radius: 5; -fx-border-color: #" + toHex(ACCENT_GREEN_LIGHT) + ";");
        assignedPatientsListView.setCellFactory(param -> new ListCell<user>() {
            @Override
            protected void updateItem(user item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("✓ " + item.getFirstName() + " " + item.getLastName() + " (ID: " + item.getId() + ")");
                    setFont(Font.font("Arial", 12));
                }
            }
        });

        HBox removeBox = new HBox(10);
        removeBox.setAlignment(Pos.CENTER_RIGHT);

        Button removePatientBtn = new Button("Remove Selected");
        removePatientBtn.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        removePatientBtn.setStyle("-fx-background-color: #" + toHex(ERROR_RED) + "; -fx-text-fill: white; " +
                "-fx-background-radius: 5; -fx-padding: 8 20; -fx-cursor: hand;");
        removePatientBtn.setOnAction(e -> {
            removeSelectedPatient();
            selectedCountLabel.setText(assignedPatientsListView.getItems().size() + " patients selected");
        });

        Button clearAllBtn = new Button("Clear All");
        clearAllBtn.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        clearAllBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; " +
                "-fx-background-radius: 5; -fx-padding: 8 20; -fx-cursor: hand;");
        clearAllBtn.setOnAction(e -> {
            assignedPatientsListView.getItems().clear();
            selectedCountLabel.setText("0 patients selected");
        });

        removeBox.getChildren().addAll(selectedCountLabel, clearAllBtn, removePatientBtn);
        HBox.setHgrow(selectedCountLabel, Priority.ALWAYS);
        selectedCountLabel.setAlignment(Pos.CENTER_LEFT);

        assignmentBox.getChildren().addAll(assignmentLabel, addPatientBox, assignedPatientsListView, removeBox);

        // Button Panel
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button submitBtn = createActionButton("Upload Content");
        submitBtn.setStyle("-fx-background-color: #" + toHex(SUCCESS_GREEN) + ";" +
                "-fx-background-radius: 5;" + "-fx-padding: 10 25;" + "-fx-cursor: hand;");
        submitBtn.setOnAction(e -> submitContent());

        updateButton = createActionButton("Update Content");
        updateButton.setStyle("-fx-background-color: #" + toHex(EDIT_BLUE) + "; -fx-text-fill: white;" +
                "-fx-background-radius: 5;" + "-fx-padding: 10 25;" + "-fx-cursor: hand;");
        updateButton.setOnAction(e -> updateContent());
        updateButton.setVisible(false);

        cancelButton = createActionButton("Cancel");
        cancelButton.setStyle("-fx-background-color: #" + toHex(TEXT_GRAY) + ";" +
                "-fx-text-fill: white;" + "-fx-background-radius: 5;" + "-fx-padding: 10 25;" + "-fx-cursor: hand;");
        cancelButton.setOnAction(e -> cancelEdit());
        cancelButton.setVisible(false);

        Button resetBtn = createActionButton("Reset");
        resetBtn.setStyle("-fx-background-color: #" + toHex(TEXT_GRAY) + ";" +
                "-fx-text-fill: white;" + "-fx-background-radius: 5;" + "-fx-padding: 10 25;" + "-fx-cursor: hand;");
        resetBtn.setOnAction(e -> resetForm());

        buttonBox.getChildren().addAll(resetBtn, cancelButton, updateButton, submitBtn);

        Label infoLabel = new Label("Max PDF size: 50 MB | Required fields marked with * | Double-click content to edit");
        infoLabel.setFont(Font.font("Arial", 11));
        infoLabel.setTextFill(Color.web(toHex(TEXT_GRAY)));

        section.getChildren().addAll(editingLabel, sectionTitle, titleBox, descBox, pdfBox, assignmentBox, buttonBox, infoLabel);
        return section;
    }

    // Edit methods
    private void editContent(ContentNode node) {
        if (!canModify(node)) {
            showAlert("Access Denied", "You don't have permission to edit this content", Alert.AlertType.ERROR);
            return;
        }

        currentEditingNode = node;

        titleField.setText(node.getTitle());
        descriptionArea.setText(node.getDescription());

        assignedPatientsListView.getItems().clear();
        List<Integer> assignedIds = node.getAssignedUsersList();
        for (user patient : allPatients) {
            if (assignedIds.contains(patient.getId())) {
                assignedPatientsListView.getItems().add(patient);
            }
        }

        editingLabel.setText("EDITING: " + node.getTitle());
        editingLabel.setVisible(true);
        updateButton.setVisible(true);
        cancelButton.setVisible(true);

        System.out.println("✏️ Editing content: ID=" + node.getNodeId() + ", Title=" + node.getTitle());
    }

    private boolean canModify(ContentNode node) {
        if ("admin".equalsIgnoreCase(currentUserRole)) {
            return true;
        }
        if ("psychologist".equalsIgnoreCase(currentUserRole) && node.getCreatedBy() == currentUserId) {
            return true;
        }
        return false;
    }

    private void updateContent() {
        if (currentEditingNode == null) {
            return;
        }

        try {
            String title = titleField.getText().trim();
            String description = descriptionArea.getText().trim();

            if (title.isEmpty() || title.length() < 3) {
                showAlert("Validation Error", "Title must be at least 3 characters", Alert.AlertType.WARNING);
                return;
            }

            if (description.isEmpty() || description.length() < 10) {
                showAlert("Validation Error", "Description must be at least 10 characters", Alert.AlertType.WARNING);
                return;
            }

            List<Integer> assignedUserIds = getAssignedPatientIds();

            System.out.println("🔄 UPDATING content ID: " + currentEditingNode.getNodeId());
            System.out.println("   New assigned patients: " + assignedUserIds);

            controller.updateContentNode(
                    currentEditingNode.getNodeId(),
                    title,
                    description,
                    selectedPdfFile,
                    null,
                    assignedUserIds
            );

            showAlert("Success", "Content updated successfully!", Alert.AlertType.INFORMATION);
            cancelEdit();

            // Refresh appropriate view based on role
            if ("patient".equalsIgnoreCase(currentUserRole)) {
                loadPatientCards();
            } else {
                loadContentTable();
            }

            updateHistoryButtons();

        } catch (SecurityException e) {
            showAlert("Access Denied", e.getMessage(), Alert.AlertType.ERROR);
        } catch (IllegalArgumentException e) {
            showAlert("Validation Error", e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Error", "Failed to update: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void cancelEdit() {
        currentEditingNode = null;
        resetForm();
        editingLabel.setVisible(false);
        updateButton.setVisible(false);
        cancelButton.setVisible(false);
        System.out.println("✖️ Edit cancelled");
    }

    private void addSelectedPatient() {
        user selected = patientAssignmentCombo.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean exists = assignedPatientsListView.getItems().stream()
                    .anyMatch(u -> u.getId() == selected.getId());

            if (!exists) {
                assignedPatientsListView.getItems().add(selected);
                System.out.println("✅ ADDED patient: " + selected.getFirstName() + " " + selected.getLastName() +
                        " (ID: " + selected.getId() + ")");
            }
            patientAssignmentCombo.getSelectionModel().clearSelection();
        }
    }

    private void removeSelectedPatient() {
        user selected = assignedPatientsListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            assignedPatientsListView.getItems().remove(selected);
            System.out.println("✅ REMOVED patient: " + selected.getFirstName() + " " + selected.getLastName() +
                    " (ID: " + selected.getId() + ")");
        }
    }

    private List<Integer> getAssignedPatientIds() {
        return assignedPatientsListView.getItems().stream()
                .map(user::getId)
                .collect(Collectors.toList());
    }

    private VBox createTableSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-radius: 8; " +
                "-fx-border-color: #" + toHex(ACCENT_GREEN_LIGHT) + "; -fx-border-width: 1;");

        Label sectionTitle = new Label("Content Library");
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        sectionTitle.setTextFill(Color.web(toHex(ACCENT_GREEN)));

        contentTable = new TableView<>();
        contentTable.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5;");
        contentTable.setPlaceholder(new Label("No content available"));
        contentTable.setPrefHeight(700);
        contentTable.setMinHeight(500);

        VBox.setVgrow(contentTable, Priority.ALWAYS);

        contentTable.setRowFactory(tv -> {
            TableRow<ContentNode> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    ContentNode node = row.getItem();
                    if (canModify(node)) {
                        editContent(node);
                    } else if (node.getPdfPath() != null && !node.getPdfPath().isEmpty()) {
                        try {
                            controller.openPdfFile(node.getPdfPath());
                            if (currentUserId > 0) {
                                controller.logAccess(currentUserId, node.getNodeId());
                            }
                        } catch (Exception e) {
                            showAlert("Error", "Could not open PDF: " + e.getMessage(), Alert.AlertType.ERROR);
                        }
                    }
                }
            });
            return row;
        });

        TableColumn<ContentNode, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));
        titleCol.setPrefWidth(220);
        titleCol.setMinWidth(150);
        titleCol.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-weight: bold;");

        TableColumn<ContentNode, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDescription().length() > 60 ?
                        cellData.getValue().getDescription().substring(0, 57) + "..." :
                        cellData.getValue().getDescription()
        ));
        descCol.setPrefWidth(300);
        descCol.setMinWidth(200);
        descCol.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<ContentNode, String> summaryCol = new TableColumn<>("Quick Summary");
        summaryCol.setCellValueFactory(cellData -> {
            String desc = cellData.getValue().getDescription();
            if (desc.length() > 100) {
                String summary = SummarizationService.smartSummarize(desc, 1);
                return new javafx.beans.property.SimpleStringProperty(summary);
            }
            return new javafx.beans.property.SimpleStringProperty(desc);
        });
        summaryCol.setPrefWidth(250);
        summaryCol.setMinWidth(200);
        summaryCol.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<ContentNode, String> pdfCol = new TableColumn<>("PDF");
        pdfCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getPdfPath() != null ? "Yes" : "No"
        ));
        pdfCol.setPrefWidth(70);
        pdfCol.setMinWidth(60);
        pdfCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<ContentNode, String> assignedCol = new TableColumn<>("Assigned");
        assignedCol.setCellValueFactory(cellData -> {
            int count = cellData.getValue().getAssignedUsersList().size();
            return new javafx.beans.property.SimpleStringProperty(count + " patient(s)");
        });
        assignedCol.setPrefWidth(120);
        assignedCol.setMinWidth(100);
        assignedCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<ContentNode, String> createdByCol = new TableColumn<>("Created By");
        createdByCol.setCellValueFactory(cellData -> {
            int creatorId = cellData.getValue().getCreatedBy();
            String creatorName = getUsernameById(creatorId);
            return new javafx.beans.property.SimpleStringProperty(creatorName);
        });
        createdByCol.setPrefWidth(180);
        createdByCol.setMinWidth(150);
        createdByCol.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<ContentNode, String> dateCol = new TableColumn<>("Created");
        dateCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCreatedAt().format(dateFormatter)
        ));
        dateCol.setPrefWidth(150);
        dateCol.setMinWidth(120);
        dateCol.setStyle("-fx-alignment: CENTER;");

        // Actions column with buttons
        TableColumn<ContentNode, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(300);
        actionCol.setMinWidth(280);
        actionCol.setCellFactory(col -> new TableCell<ContentNode, Void>() {
            private final Button viewBtn = new Button("View PDF");
            private final Button listenBtn = new Button("🔊 Listen");
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(8);

            {
                viewBtn.setStyle("-fx-background-color: #" + toHex(ACCENT_GREEN_LIGHT) + "; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 5 8; -fx-background-radius: 3;");
                viewBtn.setOnAction(e -> {
                    ContentNode node = getTableView().getItems().get(getIndex());
                    try {
                        controller.openPdfFile(node.getPdfPath());
                        if (currentUserId > 0) {
                            controller.logAccess(currentUserId, node.getNodeId());
                        }
                    } catch (Exception ex) {
                        showAlert("Error", "Could not open PDF: " + ex.getMessage(), Alert.AlertType.ERROR);
                    }
                });

                listenBtn.setStyle("-fx-background-color: #" + toHex(TTS_BLUE) + "; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 5 8; -fx-background-radius: 3;");
                listenBtn.setOnAction(e -> {
                    ContentNode node = getTableView().getItems().get(getIndex());
                    String textToSpeak = "Title: " + node.getTitle() + ". " + node.getDescription();
                    LocalTTSService.speakAsync(textToSpeak, null);
                });

                editBtn.setStyle("-fx-background-color: #" + toHex(EDIT_BLUE) + "; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 5 8; -fx-background-radius: 3;");
                editBtn.setOnAction(e -> {
                    ContentNode node = getTableView().getItems().get(getIndex());
                    if (canModify(node)) {
                        editContent(node);
                    } else {
                        showAlert("Access Denied", "You don't have permission to edit this content", Alert.AlertType.ERROR);
                    }
                });

                deleteBtn.setStyle("-fx-background-color: #" + toHex(ERROR_RED) + "; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 5 8; -fx-background-radius: 3;");
                deleteBtn.setOnAction(e -> {
                    ContentNode node = getTableView().getItems().get(getIndex());
                    deleteContentNode(node);
                });

                pane.setAlignment(Pos.CENTER);
                pane.getChildren().addAll(viewBtn, listenBtn, editBtn, deleteBtn);
                pane.setSpacing(5);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ContentNode node = getTableView().getItems().get(getIndex());

                    boolean canDelete = "admin".equalsIgnoreCase(currentUserRole) ||
                            ("psychologist".equalsIgnoreCase(currentUserRole) &&
                                    node.getCreatedBy() == currentUserId);
                    deleteBtn.setVisible(canDelete);
                    deleteBtn.setManaged(canDelete);

                    boolean canEdit = canModify(node);
                    editBtn.setVisible(canEdit);
                    editBtn.setManaged(canEdit);

                    boolean hasPdf = node.getPdfPath() != null && !node.getPdfPath().isEmpty();
                    viewBtn.setVisible(hasPdf);
                    viewBtn.setManaged(hasPdf);

                    // Listen button always visible for all content
                    listenBtn.setVisible(true);
                    listenBtn.setManaged(true);

                    setGraphic(pane);
                }
            }
        });

        contentTable.getColumns().addAll(titleCol, descCol, pdfCol, assignedCol, createdByCol, dateCol, actionCol);

        contentTable.getColumns().add(2, summaryCol);

        contentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        section.getChildren().addAll(sectionTitle, contentTable);
        return section;
    }
    private String getUsernameById(int userId) {
        try {
            user u = userservice.getuserById(userId);
            if (u != null) {
                return u.getFirstName() + " " + u.getLastName();
            }
        } catch (Exception e) {
            // Ignore
        }
        return "User " + userId;
    }

    private void deleteContentNode(ContentNode node) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Content");
        confirm.setContentText("Are you sure you want to delete '" + node.getTitle() + "'?\nThis action cannot be undone.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                controller.deleteContentNode(node.getNodeId());
                showAlert("Success", "Content deleted successfully", Alert.AlertType.INFORMATION);

                if (currentEditingNode != null && currentEditingNode.getNodeId() == node.getNodeId()) {
                    cancelEdit();
                }

                // Refresh appropriate view based on role
                if ("patient".equalsIgnoreCase(currentUserRole)) {
                    loadPatientCards();
                } else {
                    loadContentTable();
                }

                updateHistoryButtons();
            } catch (SecurityException e) {
                showAlert("Access Denied", e.getMessage(), Alert.AlertType.ERROR);
            } catch (Exception e) {
                showAlert("Error", "Failed to delete: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void selectPdfFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select PDF File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            selectedPdfFile = file;
            pdfFileLabel.setText(file.getName() + " (" + formatFileSize(file.length()) + ")");
        }
    }

    private void submitContent() {
        try {
            String title = titleField.getText().trim();
            String description = descriptionArea.getText().trim();

            if (title.isEmpty() || title.length() < 3) {
                showAlert("Validation Error", "Title must be at least 3 characters", Alert.AlertType.WARNING);
                return;
            }

            if (description.isEmpty() || description.length() < 10) {
                showAlert("Validation Error", "Description must be at least 10 characters", Alert.AlertType.WARNING);
                return;
            }

            List<Integer> assignedUserIds = getAssignedPatientIds();

            if (assignedUserIds.isEmpty()) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("No Patients Assigned");
                confirm.setHeaderText("No patients have been assigned to this content");
                confirm.setContentText("Do you want to continue without assigning any patients?");

                Optional<ButtonType> result = confirm.showAndWait();
                if (result.isPresent() && result.get() != ButtonType.OK) {
                    return;
                }
            }

            System.out.println("🚀 CREATING content with assigned patients: " + assignedUserIds);

            int nodeId = controller.createContentNode(title, description, selectedPdfFile, null, assignedUserIds);

            if (nodeId > 0) {
                showAlert("Success", "Content created successfully!\nAssigned to: " + assignedUserIds.size() + " patients", Alert.AlertType.INFORMATION);
                resetForm();

                // Refresh appropriate view based on role
                if ("patient".equalsIgnoreCase(currentUserRole)) {
                    loadPatientCards();
                } else {
                    loadContentTable();
                }

                updateHistoryButtons();
            }
        } catch (SecurityException e) {
            showAlert("Access Denied", e.getMessage(), Alert.AlertType.ERROR);
        } catch (IllegalArgumentException e) {
            showAlert("Validation Error", e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Error", "Failed to create: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void resetForm() {
        titleField.clear();
        descriptionArea.clear();
        selectedPdfFile = null;
        pdfFileLabel.setText("No file selected");
        assignedPatientsListView.getItems().clear();

        if (currentEditingNode != null) {
            cancelEdit();
        }
        System.out.println("  - Form reset, cleared assigned patients");
    }

    private void performUndo() {
        if (controller.canUndo()) {
            controller.undo();

            // Refresh appropriate view based on role
            if ("patient".equalsIgnoreCase(currentUserRole)) {
                loadPatientCards();
            } else {
                loadContentTable();
            }

            updateHistoryButtons();
            showAlert("Undo Successful", controller.getRedoDescription(), Alert.AlertType.INFORMATION);
        }
    }

    private void performRedo() {
        if (controller.canRedo()) {
            controller.redo();

            // Refresh appropriate view based on role
            if ("patient".equalsIgnoreCase(currentUserRole)) {
                loadPatientCards();
            } else {
                loadContentTable();
            }

            updateHistoryButtons();
            showAlert("Redo Successful", controller.getUndoDescription(), Alert.AlertType.INFORMATION);
        }
    }

    public void updateHistoryButtons() {
        if (undoButton != null && redoButton != null) {
            undoButton.setDisable(!controller.canUndo());
            redoButton.setDisable(!controller.canRedo());
        }
    }

    public void loadContentTable() {
        try {
            List<ContentNode> content = controller.getViewableContentNodes(null);
            if (content != null) {
                contentTable.getItems().setAll(content);
                System.out.println("✅ Loaded " + content.size() + " content items for admin/psychologist");
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to load content: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void setUserId(int userId) {
        this.currentUserId = userId;
        this.currentUserRole = parentApp.getUserType();
        this.controller.setCurrentUser(userId, currentUserRole);

        if (roleInfoLabel != null) {
            boolean isAdmin = "admin".equalsIgnoreCase(currentUserRole);
            boolean canCreate = "admin".equalsIgnoreCase(currentUserRole) || "psychologist".equalsIgnoreCase(currentUserRole);

            String roleText = "Logged in as: " + currentUserRole.toUpperCase() + " | ";
            roleText += canCreate ? "Can create/edit/assign content" : "View only";
            if (isAdmin) {
                roleText += " | Undo/Redo available";
            }
            roleInfoLabel.setText(roleText);
            roleInfoLabel.setTextFill(canCreate ? Color.web(toHex(ACCENT_GREEN)) : Color.web(toHex(WARNING_ORANGE)));
        }

        if (formSection != null) {
            boolean canCreate = "admin".equalsIgnoreCase(currentUserRole) || "psychologist".equalsIgnoreCase(currentUserRole);
            formSection.setVisible(canCreate);
            formSection.setManaged(canCreate);
        }

        loadAllPatients();
        if (patientAssignmentCombo != null) {
            patientAssignmentCombo.setItems(javafx.collections.FXCollections.observableArrayList(allPatients));
        }

        // Update view based on user role
        if ("patient".equalsIgnoreCase(currentUserRole)) {
            patientViewContainer.setVisible(true);
            patientViewContainer.setManaged(true);
            if (contentTable != null) {
                contentTable.setVisible(false);
                contentTable.setManaged(false);
            }
            loadPatientCards();
            System.out.println("✅ Patient view: showing cards");
        } else {
            patientViewContainer.setVisible(false);
            patientViewContainer.setManaged(false);
            contentTable.setVisible(true);
            contentTable.setManaged(true);
            loadContentTable();
            System.out.println("✅ Admin view: showing table with " + contentTable.getItems().size() + " items");
        }

        updateHistoryButtons();

        if (currentEditingNode != null) {
            cancelEdit();
        }
    }

    private Button createActionButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        button.setTextFill(Color.WHITE);
        button.setStyle("-fx-background-color: #" + toHex(ACCENT_GREEN) + ";" +
                "-fx-background-radius: 5;" + "-fx-padding: 10 20;" + "-fx-cursor: hand;");
        return button;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    private String toHex(Color color) {
        return String.format("%02x%02x%02x",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    public void refreshData() {
        if ("patient".equalsIgnoreCase(currentUserRole)) {
            loadPatientCards();
        } else {
            loadContentTable();
        }
        updateHistoryButtons();
    }

    public ContentNodeController getController() {
        return controller;
    }
}