package ui;

import controller.SessionController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import models.Session;
import services.VideoCallService; // ⭐ NEW IMPORT

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SessionPanel extends VBox {

    private MentisLoginFrame parentApp;
    private SessionController controller;
    private VideoCallService videoCallService; // ⭐ NEW SERVICE
    private TableView<Session> sessionTable;
    private List<Session> sessions;

    // Color constants (same as AssessmentPanel)
    private static final Color BACKGROUND_LIGHT = Color.rgb(240, 248, 245);
    private static final Color CARD_WHITE = Color.WHITE;
    private static final Color ACCENT_DARK_GREEN = Color.rgb(60, 120, 90);
    private static final Color BUTTON_LIGHT_GREEN = Color.rgb(160, 200, 180);
    private static final Color BORDER_LIGHT = Color.rgb(200, 220, 210);
    private static final Color TEXT_DARK = Color.rgb(40, 70, 50);
    private static final Color TEXT_LIGHT = Color.rgb(100, 130, 110);
    private static final Color VIDEO_BUTTON_COLOR = Color.rgb(231, 76, 60); // ⭐ NEW COLOR

    // Status colors
    private static final Color STATUS_ACTIVE = Color.rgb(39, 174, 96);
    private static final Color STATUS_INACTIVE = Color.rgb(192, 57, 43);
    private static final Color STATUS_SCHEDULED = Color.rgb(52, 152, 219);
    private static final Color STATUS_COMPLETED = Color.rgb(155, 89, 182);
    private static final Color STATUS_CANCELLED = Color.rgb(230, 126, 34);
    private static final Color STATUS_DEFAULT = Color.rgb(120, 120, 120);

    // Session type colors
    private static final Color TYPE_INDIVIDUAL = Color.rgb(41, 128, 185);
    private static final Color TYPE_GROUP = Color.rgb(39, 174, 96);
    private static final Color TYPE_FAMILY = Color.rgb(142, 68, 173);
    private static final Color TYPE_COUPLE = Color.rgb(230, 126, 34);
    private static final Color TYPE_ONLINE = Color.rgb(52, 152, 219);
    private static final Color TYPE_DEFAULT = Color.rgb(80, 100, 120);

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public SessionPanel(MentisLoginFrame parentApp, SessionController controller) {
        this.parentApp = parentApp;
        this.controller = controller;
        this.videoCallService = new VideoCallService(); // ⭐ INITIALIZE

        setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        setPadding(new Insets(40, 50, 40, 50));
        setSpacing(30);

        createHeader();
        createTable();
        refreshData();
    }

    // ================= UPDATED: Removed Reservations tab =================
    private void createHeader() {
        VBox headerContainer = new VBox(30);
        headerContainer.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

        // Tabs - ONLY Sessions tab, removed Reservations
        HBox tabsPanel = new HBox(30);
        tabsPanel.setAlignment(Pos.CENTER_RIGHT);
        tabsPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

        Label sessionsTab = new Label("Sessions");
        sessionsTab.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        sessionsTab.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));
        sessionsTab.setBorder(new Border(
                new BorderStroke(Color.web(toHex(ACCENT_DARK_GREEN)),
                        BorderStrokeStyle.SOLID,
                        CornerRadii.EMPTY,
                        new BorderWidths(0, 0, 3, 0))
        ));
        sessionsTab.setPadding(new Insets(0, 0, 5, 0));

        // REMOVED: Reservations tab completely
        tabsPanel.getChildren().addAll(sessionsTab);

        // Title and ADD button
        HBox titlePanel = new HBox();
        titlePanel.setAlignment(Pos.CENTER_LEFT);
        titlePanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

        Label titleLabel = new Label("Manage Sessions");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addButton = createAddButton();
        addButton.setOnAction(e -> showAddSessionDialog());

        titlePanel.getChildren().addAll(titleLabel, spacer, addButton);
        headerContainer.getChildren().addAll(tabsPanel, titlePanel);

        getChildren().add(headerContainer);
    }

    private Button createAddButton() {
        Button button = new Button("ADD SESSION");
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
        sessionTable = new TableView<>();
        sessionTable.setStyle("-fx-background-color: white; -fx-border-color: #" + toHex(BORDER_LIGHT) + ";");
        sessionTable.setFixedCellSize(70);
        sessionTable.setPlaceholder(new Label("No sessions available. Click ADD SESSION to create one."));

        // Create columns
        TableColumn<Session, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(150);

        TableColumn<Session, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("sessionDate"));
        dateCol.setPrefWidth(100);
        dateCol.setCellFactory(column -> new TableCell<Session, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(dateFormatter));
                }
            }
        });

        TableColumn<Session, LocalTime> startTimeCol = new TableColumn<>("Start Time");
        startTimeCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        startTimeCol.setPrefWidth(100);
        startTimeCol.setCellFactory(column -> new TableCell<Session, LocalTime>() {
            @Override
            protected void updateItem(LocalTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(timeFormatter));
                }
            }
        });

        TableColumn<Session, LocalTime> endTimeCol = new TableColumn<>("End Time");
        endTimeCol.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        endTimeCol.setPrefWidth(100);
        endTimeCol.setCellFactory(column -> new TableCell<Session, LocalTime>() {
            @Override
            protected void updateItem(LocalTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(timeFormatter));
                }
            }
        });

        TableColumn<Session, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        locationCol.setPrefWidth(150);

        TableColumn<Session, String> typeCol = new TableColumn<>("Session Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("sessionType"));
        typeCol.setPrefWidth(120);
        typeCol.setCellFactory(column -> new TableCell<Session, String>() {
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

        TableColumn<Session, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(column -> new TableCell<Session, String>() {
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

        // ⭐ NEW: Video Call column for psychologists
        TableColumn<Session, Void> videoCol = new TableColumn<>("Video");
        videoCol.setPrefWidth(80);
        videoCol.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Session, Void> call(TableColumn<Session, Void> param) {
                return new TableCell<>() {
                    private final Button videoButton = new Button("📹");

                    {
                        videoButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
                        videoButton.setTextFill(Color.WHITE);
                        videoButton.setStyle(
                                "-fx-background-color: #" + toHex(VIDEO_BUTTON_COLOR) + ";" +
                                        "-fx-background-radius: 5;" +
                                        "-fx-padding: 5 10;" +
                                        "-fx-cursor: hand;"
                        );
                        videoButton.setTooltip(new Tooltip("Start video call"));

                        videoButton.setOnAction(e -> {
                            Session session = getTableView().getItems().get(getIndex());
                            startVideoCall(session);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Session session = getTableView().getItems().get(getIndex());
                            // Only show video button for online sessions that are reserved
                            if (session.getSessionType().equalsIgnoreCase("Online") && session.getReservedBy() != null) {
                                setGraphic(videoButton);
                            } else {
                                setGraphic(null);
                            }
                        }
                    }
                };
            }
        });

        TableColumn<Session, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(120);
        actionsCol.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Session, Void> call(TableColumn<Session, Void> param) {
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
                            Session session = getTableView().getItems().get(getIndex());
                            showManageDialog(session);
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

        sessionTable.getColumns().addAll(titleCol, dateCol, startTimeCol, endTimeCol,
                locationCol, typeCol, statusCol, videoCol, actionsCol);

        VBox.setVgrow(sessionTable, Priority.ALWAYS);
        getChildren().add(sessionTable);
    }

    // ================= UPDATED: Removed VIEW RESERVATIONS button =================
    private void showManageDialog(Session session) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("Manage Session");
        dialog.setMinWidth(700);
        dialog.setMinHeight(500);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

        // Main content panel
        VBox contentPanel = new VBox(20);
        contentPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        contentPanel.setPadding(new Insets(40, 40, 40, 40));

        Label headerLabel = new Label("Manage Session");
        headerLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        headerLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        Label titleDisplay = new Label(session.getTitle());
        titleDisplay.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleDisplay.setTextFill(Color.web(toHex(TEXT_DARK)));
        titleDisplay.setWrapText(true);

        // Session details grid
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(20);
        detailsGrid.setVgap(15);
        detailsGrid.setPadding(new Insets(20, 0, 20, 0));

        // Row 1: Date and Time
        Label dateLabel = new Label("Date:");
        dateLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        dateLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        Label dateValue = new Label(session.getSessionDate().format(dateFormatter));
        dateValue.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));

        Label timeLabel = new Label("Time:");
        timeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        timeLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        Label timeValue = new Label(session.getStartTime().format(timeFormatter) + " - " +
                session.getEndTime().format(timeFormatter));
        timeValue.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));

        detailsGrid.add(dateLabel, 0, 0);
        detailsGrid.add(dateValue, 1, 0);
        detailsGrid.add(timeLabel, 2, 0);
        detailsGrid.add(timeValue, 3, 0);

        // Row 2: Location and Type
        Label locationLabel = new Label("Location:");
        locationLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        locationLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        Label locationValue = new Label(session.getLocation());
        locationValue.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));

        Label typeLabel = new Label("Type:");
        typeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        typeLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        Label typeValue = new Label(session.getSessionType());
        typeValue.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        typeValue.setTextFill(Color.web(toHex(getTypeColor(session.getSessionType()))));

        detailsGrid.add(locationLabel, 0, 1);
        detailsGrid.add(locationValue, 1, 1);
        detailsGrid.add(typeLabel, 2, 1);
        detailsGrid.add(typeValue, 3, 1);

        // Status
        Label statusLabel = new Label("Current Status: " + session.getStatus());
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        statusLabel.setTextFill(Color.web(toHex(getStatusColor(session.getStatus()))));
        statusLabel.setPadding(new Insets(10, 0, 0, 0));

        Label questionLabel = new Label("What would you like to do?");
        questionLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        questionLabel.setTextFill(Color.web(toHex(TEXT_DARK)));
        questionLabel.setPadding(new Insets(30, 0, 0, 0));

        contentPanel.getChildren().addAll(headerLabel, titleDisplay, detailsGrid,
                statusLabel, questionLabel);

        // Button panel - REMOVED "VIEW RESERVATIONS" button
        HBox buttonPanel = new HBox(15);
        buttonPanel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        buttonPanel.setPadding(new Insets(20, 40, 40, 40));
        buttonPanel.setAlignment(Pos.CENTER_LEFT);

        // Only EDIT, DELETE, ACTIVATE/DEACTIVATE, CANCEL - removed VIEW RESERVATIONS
        String[] buttonLabels = {"EDIT", "DELETE", "ACTIVATE/\nDEACTIVATE", "CANCEL"};
        for (String label : buttonLabels) {
            Button btn = createDialogButton(label.replace("\n", " "));

            if (label.equals("CANCEL")) {
                btn.setOnAction(e -> dialog.close());
            } else if (label.equals("EDIT")) {
                btn.setOnAction(e -> {
                    dialog.close();
                    showEditSessionDialog(session);
                });
            } else if (label.equals("DELETE")) {
                btn.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirm Delete");
                    confirm.setHeaderText(null);
                    confirm.setContentText("Are you sure you want to delete this session?");
                    confirm.initOwner(dialog);

                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            try {
                                controller.deleteSession(session.getSessionId());
                                showAlert("Success", "Session deleted successfully!", Alert.AlertType.INFORMATION);
                                dialog.close();
                                refreshData();
                            } catch (SQLException ex) {
                                showAlert("Error", "Error deleting session: " + ex.getMessage(), Alert.AlertType.ERROR);
                            }
                        }
                    });
                });
            } else if (label.contains("ACTIVATE")) {
                btn.setOnAction(e -> {
                    try {
                        String newStatus = "active".equals(session.getStatus()) ? "inactive" : "active";
                        if (controller.updateSessionStatus(session.getSessionId(), newStatus)) {
                            showAlert("Success", "Session status updated to: " + newStatus, Alert.AlertType.INFORMATION);
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

        root.setCenter(contentPanel);
        root.setBottom(buttonPanel);

        Scene scene = new Scene(root);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // ⭐ NEW: Video call method for psychologists
    private void startVideoCall(Session session) {
        // Check if session is online type
        if (!session.getSessionType().equalsIgnoreCase("Online")) {
            showAlert("Not Available", "Video calls are only available for online sessions.", Alert.AlertType.WARNING);
            return;
        }

        // Check if session is reserved by someone
        if (session.getReservedBy() == null) {
            showAlert("No Patient", "This session has no patient reserved yet.", Alert.AlertType.WARNING);
            return;
        }

        // Check if it's time for the session
        LocalDateTime sessionTime = LocalDateTime.of(session.getSessionDate(), session.getStartTime());
        LocalDateTime now = LocalDateTime.now();

        // Allow calls 15 minutes before until session end
        if (now.isBefore(sessionTime.minusMinutes(15))) {
            showAlert("Too Early", "Video call will be available 15 minutes before the session.\n" +
                            "Session starts at: " + session.getStartTime().format(timeFormatter),
                    Alert.AlertType.WARNING);
            return;
        }

        if (now.isAfter(sessionTime.plusHours(1))) {
            showAlert("Session Ended", "This session has already ended.", Alert.AlertType.WARNING);
            return;
        }

        // Generate meeting link
        String meetingLink = videoCallService.generateMeetingLink(
                session.getSessionId(),
                session.getReservedBy(),
                parentApp.getUserId()
        );

        // Show confirmation dialog
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Start Video Call");
        confirm.setHeaderText("Start video session for: " + session.getTitle());
        confirm.setContentText(
                "You will be redirected to Jitsi Meet in your browser.\n\n" +
                        "📹 Make sure your camera is working\n" +
                        "🎤 Check your microphone\n" +
                        "👤 Patient ID: " + session.getReservedBy() + "\n\n" +
                        "Ready to start?"
        );

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                videoCallService.joinMeeting(meetingLink);
            }
        });
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
            case "scheduled": return STATUS_SCHEDULED;
            case "completed": return STATUS_COMPLETED;
            case "cancelled": return STATUS_CANCELLED;
            default: return STATUS_DEFAULT;
        }
    }

    private Color getTypeColor(String type) {
        if (type == null) return TYPE_DEFAULT;
        switch (type.toLowerCase()) {
            case "individual": return TYPE_INDIVIDUAL;
            case "group": return TYPE_GROUP;
            case "family": return TYPE_FAMILY;
            case "couple": return TYPE_COUPLE;
            case "online": return TYPE_ONLINE;
            default: return TYPE_DEFAULT;
        }
    }

    private void showAddSessionDialog() {
        new SessionFormDialog(parentApp, controller, null, false);
    }

    private void showEditSessionDialog(Session session) {
        new SessionFormDialog(parentApp, controller, session, true);
    }

    public void refreshData() {
        sessionTable.getItems().clear();

        try {
            sessions = controller.getAllSessions();

            if (sessions == null || sessions.isEmpty()) {
                sessionTable.setPlaceholder(new Label("No sessions available. Click ADD SESSION to create one."));
                return;
            }

            sessionTable.getItems().addAll(sessions);

        } catch (SQLException e) {
            e.printStackTrace();
            sessionTable.setPlaceholder(new Label("Database Error: " + e.getMessage()));
            showAlert("Database Error",
                    "Cannot connect to database.\nError: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            sessionTable.setPlaceholder(new Label("Error loading data: " + e.getMessage()));
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