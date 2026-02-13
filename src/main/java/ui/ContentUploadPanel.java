package ui;

import controller.ContentNodeController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import models.ContentNode;
import models.user;
import services.userservice;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ContentUploadPanel - UI for uploading and managing PDF content.
 * ADDED: Patient View with PDF frame showing content
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

    // NEW: For patient view
    private VBox patientViewContainer;
    private Label patientContentTitle;
    private Label patientContentDescription;
    private WebView pdfWebView;
    private Button backToLibraryBtn;
    private ListView<ContentNode> patientContentList;
    private ContentNode currentPatientViewingNode;

    // For edit functionality
    private ContentNode currentEditingNode = null;
    private Button updateButton;
    private Button cancelButton;
    private Label editingLabel;

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

        // Initialize patient view
        createPatientView();

        initializeUI(contentBox);

        mainScrollPane.setContent(contentBox);
        getChildren().add(mainScrollPane);
        VBox.setVgrow(mainScrollPane, Priority.ALWAYS);
    }

    // NEW: Create patient view with PDF frame
    private void createPatientView() {
        patientViewContainer = new VBox(20);
        patientViewContainer.setPadding(new Insets(20));
        patientViewContainer.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-radius: 8; " +
                "-fx-border-color: #" + toHex(ACCENT_GREEN_LIGHT) + "; -fx-border-width: 1;");
        patientViewContainer.setVisible(false);
        patientViewContainer.setManaged(false);

        // Header with back button
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 10, 0));

        backToLibraryBtn = new Button("← Back to Library");
        backToLibraryBtn.setFont(Font.font("Arial", 14));
        backToLibraryBtn.setTextFill(Color.web(toHex(ACCENT_GREEN)));
        backToLibraryBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-border-color: " + toHex(ACCENT_GREEN) + "; -fx-border-radius: 5; -fx-padding: 8 20;");
        backToLibraryBtn.setOnAction(e -> showLibraryView());

        Label viewTitle = new Label("Content Viewer");
        viewTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        viewTitle.setTextFill(Color.web(toHex(ACCENT_GREEN)));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(backToLibraryBtn, spacer, viewTitle);

        // Content title
        patientContentTitle = new Label("Select content to view");
        patientContentTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        patientContentTitle.setTextFill(Color.web(toHex(TEXT_DARK)));
        patientContentTitle.setWrapText(true);

        // Content description
        patientContentDescription = new Label("");
        patientContentDescription.setFont(Font.font("Arial", 14));
        patientContentDescription.setTextFill(Color.web(toHex(TEXT_GRAY)));
        patientContentDescription.setWrapText(true);
        patientContentDescription.setPadding(new Insets(0, 0, 10, 0));

        // PDF WebView
        pdfWebView = new WebView();
        pdfWebView.setPrefHeight(600);
        pdfWebView.setStyle("-fx-border-color: #" + toHex(ACCENT_GREEN_LIGHT) + "; -fx-border-width: 1; -fx-border-radius: 5;");

        // Content list on the side (for patient to select)
        VBox listBox = new VBox(10);
        listBox.setPrefWidth(300);
        listBox.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-radius: 8; " +
                "-fx-border-color: #" + toHex(ACCENT_GREEN_LIGHT) + "; -fx-border-width: 1; -fx-padding: 15;");

        Label listTitle = new Label("Your Content");
        listTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        listTitle.setTextFill(Color.web(toHex(ACCENT_GREEN)));

        patientContentList = new ListView<>();
        patientContentList.setPrefHeight(500);
        patientContentList.setCellFactory(param -> new ListCell<ContentNode>() {
            @Override
            protected void updateItem(ContentNode item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitle());
                    if (item.getPdfPath() != null && !item.getPdfPath().isEmpty()) {
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #" + toHex(ACCENT_GREEN) + ";");
                    }
                }
            }
        });

        patientContentList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1 && !patientContentList.getSelectionModel().isEmpty()) {
                ContentNode selected = patientContentList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    viewPatientContent(selected);
                }
            }
        });

        listBox.getChildren().addAll(listTitle, patientContentList);

        // Main content area
        VBox contentArea = new VBox(10);
        contentArea.getChildren().addAll(patientContentTitle, patientContentDescription, pdfWebView);
        VBox.setVgrow(pdfWebView, Priority.ALWAYS);

        // Split view
        HBox splitView = new HBox(20);
        splitView.getChildren().addAll(listBox, contentArea);
        HBox.setHgrow(contentArea, Priority.ALWAYS);

        patientViewContainer.getChildren().addAll(headerBox, splitView);
    }

    // NEW: View content in patient mode
    private void viewPatientContent(ContentNode node) {
        currentPatientViewingNode = node;

        patientContentTitle.setText(node.getTitle());
        patientContentDescription.setText(node.getDescription());

        if (node.getPdfPath() != null && !node.getPdfPath().isEmpty()) {
            try {
                File pdfFile = new File(node.getPdfPath());
                if (pdfFile.exists()) {
                    // Load PDF in WebView
                    pdfWebView.getEngine().load(pdfFile.toURI().toString());

                    // Log access
                    if (currentUserId > 0) {
                        controller.logAccess(currentUserId, node.getNodeId());
                        System.out.println("✅ ACCESS LOGGED: User " + currentUserId + " viewed: " + node.getTitle());
                    }
                } else {
                    pdfWebView.getEngine().loadContent("<html><body style='font-family: Arial; color: red; padding: 20px;'>" +
                            "<h2>PDF File Not Found</h2>" +
                            "<p>The PDF file for this content is missing.</p>" +
                            "<p>Path: " + node.getPdfPath() + "</p></body></html>");
                }
            } catch (Exception e) {
                pdfWebView.getEngine().loadContent("<html><body style='font-family: Arial; color: red; padding: 20px;'>" +
                        "<h2>Error Loading PDF</h2>" +
                        "<p>" + e.getMessage() + "</p></body></html>");
                e.printStackTrace();
            }
        } else {
            pdfWebView.getEngine().loadContent("<html><body style='font-family: Arial; color: gray; padding: 20px;'>" +
                    "<h2>No PDF Available</h2>" +
                    "<p>This content does not have an associated PDF file.</p></body></html>");
        }
    }

    // NEW: Switch to patient view
    private void showPatientView() {
        if (formSection != null) formSection.setVisible(false);
        if (contentTable != null) contentTable.setVisible(false);
        patientViewContainer.setVisible(true);
        patientViewContainer.setManaged(true);

        // Load patient's content into list
        try {
            List<ContentNode> patientContent = controller.getViewableContentNodes(null);
            patientContentList.getItems().setAll(patientContent);
        } catch (Exception e) {
            showAlert("Error", "Failed to load content: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // NEW: Switch back to library view
    private void showLibraryView() {
        patientViewContainer.setVisible(false);
        patientViewContainer.setManaged(false);
        if (formSection != null && ("admin".equalsIgnoreCase(currentUserRole) || "psychologist".equalsIgnoreCase(currentUserRole))) {
            formSection.setVisible(true);
        }
        if (contentTable != null) contentTable.setVisible(true);
        currentPatientViewingNode = null;
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

        // Form section - for Admin AND Psychologist
        formSection = createFormSection();
        formSection.setVisible(canCreate);
        formSection.setManaged(canCreate);
        container.getChildren().add(formSection);

        // Patient view container
        container.getChildren().add(patientViewContainer);

        // Table section - for everyone, but patients will have a button to switch to patient view
        VBox tableSection = createTableSection();
        container.getChildren().add(tableSection);
        VBox.setVgrow(tableSection, Priority.ALWAYS);

        // Add patient view button for patients
        if (isPatient) {
            Button patientViewBtn = new Button("📖 Open Content Viewer");
            patientViewBtn.setStyle("-fx-background-color: #" + toHex(ACCENT_GREEN) + "; -fx-text-fill: white; " +
                    "-fx-padding: 12 25; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
            patientViewBtn.setOnAction(e -> showPatientView());

            HBox buttonBox = new HBox();
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.setPadding(new Insets(10, 0, 10, 0));
            buttonBox.getChildren().add(patientViewBtn);

            container.getChildren().add(buttonBox);
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
            roleText += "View content in the Content Viewer";
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
            loadContentTable();
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
        section.setPadding(new Insets(20));
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

        // Double-click to edit (if has permission) otherwise view PDF
        contentTable.setRowFactory(tv -> {
            TableRow<ContentNode> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    ContentNode node = row.getItem();
                    if (canModify(node)) {
                        editContent(node);
                    } else if (node.getPdfPath() != null && !node.getPdfPath().isEmpty()) {
                        viewPdfContent(node);
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
        actionCol.setPrefWidth(250);
        actionCol.setMinWidth(200);
        actionCol.setCellFactory(col -> new TableCell<ContentNode, Void>() {
            private final Button viewBtn = new Button("View PDF");
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(8);

            {
                viewBtn.setStyle("-fx-background-color: #" + toHex(ACCENT_GREEN_LIGHT) + "; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 5 10; -fx-background-radius: 3;");
                viewBtn.setOnAction(e -> {
                    ContentNode node = getTableView().getItems().get(getIndex());
                    viewPdfContent(node);
                });

                editBtn.setStyle("-fx-background-color: #" + toHex(EDIT_BLUE) + "; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 5 10; -fx-background-radius: 3;");
                editBtn.setOnAction(e -> {
                    ContentNode node = getTableView().getItems().get(getIndex());
                    if (canModify(node)) {
                        editContent(node);
                    } else {
                        showAlert("Access Denied", "You don't have permission to edit this content", Alert.AlertType.ERROR);
                    }
                });

                deleteBtn.setStyle("-fx-background-color: #" + toHex(ERROR_RED) + "; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 5 10; -fx-background-radius: 3;");
                deleteBtn.setOnAction(e -> {
                    ContentNode node = getTableView().getItems().get(getIndex());
                    deleteContentNode(node);
                });

                pane.setAlignment(Pos.CENTER);
                pane.getChildren().addAll(viewBtn, editBtn, deleteBtn);
                pane.setSpacing(8);
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

                    setGraphic(pane);
                }
            }
        });

        contentTable.getColumns().addAll(titleCol, descCol, pdfCol, assignedCol, createdByCol, dateCol, actionCol);
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

    private void viewPdfContent(ContentNode node) {
        try {
            if (currentUserId > 0 && node != null) {
                controller.logAccess(currentUserId, node.getNodeId());
                System.out.println("✅ ACCESS LOGGED: User " + currentUserId + " viewed: " + node.getTitle());
            }
            controller.openPdfFile(node.getPdfPath());
        } catch (Exception e) {
            showAlert("Error", "Could not open PDF: " + e.getMessage(), Alert.AlertType.ERROR);
        }
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

                loadContentTable();
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
                loadContentTable();
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
            loadContentTable();
            updateHistoryButtons();
            showAlert("Undo Successful", controller.getRedoDescription(), Alert.AlertType.INFORMATION);
        }
    }

    private void performRedo() {
        if (controller.canRedo()) {
            controller.redo();
            loadContentTable();
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

        loadContentTable();
        updateHistoryButtons();

        if (currentEditingNode != null) {
            cancelEdit();
        }

        // If patient, make sure patient view is initialized but hidden
        if ("patient".equalsIgnoreCase(currentUserRole)) {
            // Patient view will be shown when they click the button
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
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }

    public void refreshData() {
        loadContentTable();
        updateHistoryButtons();
    }

    public ContentNodeController getController() {
        return controller;
    }
}