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
import services.VideoCallService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SessionPanel extends VBox {

    private MentisLoginFrame parentApp;
    private SessionController controller;
    private VideoCallService videoCallService;
    private TableView<Session> sessionTable;
    private List<Session> sessions;

    // Symfony-style green colors
    private static final Color PAGE_BG = Color.web("#F8F9FA");
    private static final Color SOFT_GREEN_BG = Color.web("#F1F8E9");
    private static final Color EMERALD = Color.web("#50C878");
    private static final Color EMERALD_DARK = Color.web("#2E7D32");
    private static final Color EMERALD_MID = Color.web("#3A9B5E");
    private static final Color INK = Color.web("#1A3C34");
    private static final Color MUTED = Color.web("#6C757D");
    private static final Color LINE = Color.web("#E9ECEF");
    private static final Color VIDEO_BUTTON_COLOR = Color.web("#E74C3C");

    // Status colors
    private static final Color STATUS_ACTIVE = Color.web("#27AE60");
    private static final Color STATUS_INACTIVE = Color.web("#E74C3C");
    private static final Color STATUS_SCHEDULED = Color.web("#3498DB");
    private static final Color STATUS_COMPLETED = Color.web("#9B59B6");
    private static final Color STATUS_CANCELLED = Color.web("#F39C12");
    private static final Color STATUS_DEFAULT = Color.web("#7F8C8D");

    // Session type colors
    private static final Color TYPE_INDIVIDUAL = Color.web("#5B8C5A");
    private static final Color TYPE_GROUP = Color.web("#27AE60");
    private static final Color TYPE_FAMILY = Color.web("#8E44AD");
    private static final Color TYPE_COUPLE = Color.web("#E67E22");
    private static final Color TYPE_ONLINE = Color.web("#3498DB");
    private static final Color TYPE_DEFAULT = Color.web("#7F8C8D");

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public SessionPanel(MentisLoginFrame parentApp, SessionController controller) {
        this.parentApp = parentApp;
        this.controller = controller;
        this.videoCallService = new VideoCallService();

        setStyle("-fx-background-color: " + cssColor(PAGE_BG) + ";");
        setPadding(new Insets(44, 56, 44, 56));
        setSpacing(28);

        createHeader();
        createTable();
        refreshData();
    }

    private void createHeader() {
        VBox headerContainer = new VBox(20);

        HBox titlePanel = new HBox();
        titlePanel.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(6);
        Label titleLabel = new Label("📋 Manage Sessions");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 40));
        titleLabel.setTextFill(EMERALD_DARK);

        Label subtitleLabel = new Label("Create, edit, and manage therapy sessions");
        subtitleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        subtitleLabel.setTextFill(MUTED);

        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addButton = createPrimaryButton("+ Add Session");
        addButton.setOnAction(e -> showAddSessionDialog());

        titlePanel.getChildren().addAll(titleBox, spacer, addButton);
        headerContainer.getChildren().add(titlePanel);

        getChildren().add(headerContainer);
    }

    private Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        button.setTextFill(Color.WHITE);
        button.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + cssColor(EMERALD) + ", " + cssColor(EMERALD_MID) + ");" +
                        "-fx-background-radius: 999px;" +
                        "-fx-padding: 11px 28px;" +
                        "-fx-cursor: hand;" +
                        cardShadow()
        );
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + cssColor(EMERALD_MID) + ", " + cssColor(EMERALD_DARK) + ");" +
                        "-fx-background-radius: 999px;" +
                        "-fx-padding: 11px 28px;" +
                        "-fx-cursor: hand;" +
                        "-fx-translate-y: -2px;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + cssColor(EMERALD) + ", " + cssColor(EMERALD_MID) + ");" +
                        "-fx-background-radius: 999px;" +
                        "-fx-padding: 11px 28px;" +
                        "-fx-cursor: hand;" +
                        cardShadow()
        ));
        return button;
    }

    private Button createOutlineButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        button.setTextFill(MUTED);
        button.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 999px;" +
                        "-fx-border-color: " + cssColor(LINE) + ";" +
                        "-fx-border-radius: 999px;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-padding: 6px 16px;" +
                        "-fx-cursor: hand;"
        );
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: " + cssColor(SOFT_GREEN_BG) + ";" +
                        "-fx-background-radius: 999px;" +
                        "-fx-border-color: " + cssColor(EMERALD) + ";" +
                        "-fx-border-radius: 999px;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-padding: 6px 16px;" +
                        "-fx-cursor: hand;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 999px;" +
                        "-fx-border-color: " + cssColor(LINE) + ";" +
                        "-fx-border-radius: 999px;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-padding: 6px 16px;" +
                        "-fx-cursor: hand;"
        ));
        return button;
    }

    private Label createBadge(String text, Color bgColor) {
        Label badge = new Label(text);
        badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        badge.setTextFill(Color.WHITE);
        badge.setPadding(new Insets(5, 14, 5, 14));
        badge.setStyle("-fx-background-color: " + cssColor(bgColor) + "; -fx-background-radius: 999px;");
        return badge;
    }

    private void createTable() {
        sessionTable = new TableView<>();
        sessionTable.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 5);"
        );
        sessionTable.setFixedCellSize(65);
        sessionTable.setPlaceholder(new Label("No sessions available. Click + Add Session to create one."));

        TableColumn<Session, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(200);
        titleCol.setStyle(columnStyle());

        TableColumn<Session, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("sessionDate"));
        dateCol.setPrefWidth(110);
        dateCol.setStyle(columnStyle());
        dateCol.setCellFactory(column -> new TableCell<Session, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.format(dateFormatter));
            }
        });

        TableColumn<Session, LocalTime> startTimeCol = new TableColumn<>("Start");
        startTimeCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        startTimeCol.setPrefWidth(90);
        startTimeCol.setStyle(columnStyle());
        startTimeCol.setCellFactory(column -> new TableCell<Session, LocalTime>() {
            @Override
            protected void updateItem(LocalTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.format(timeFormatter));
            }
        });

        TableColumn<Session, LocalTime> endTimeCol = new TableColumn<>("End");
        endTimeCol.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        endTimeCol.setPrefWidth(90);
        endTimeCol.setStyle(columnStyle());
        endTimeCol.setCellFactory(column -> new TableCell<Session, LocalTime>() {
            @Override
            protected void updateItem(LocalTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.format(timeFormatter));
            }
        });

        TableColumn<Session, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        locationCol.setPrefWidth(150);
        locationCol.setStyle(columnStyle());

        TableColumn<Session, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("sessionType"));
        typeCol.setPrefWidth(110);
        typeCol.setStyle(columnStyle());
        typeCol.setCellFactory(column -> new TableCell<Session, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setGraphic(createBadge(item, getTypeColor(item)));
                    setText(null);
                }
            }
        });

        TableColumn<Session, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(110);
        statusCol.setStyle(columnStyle());
        statusCol.setCellFactory(column -> new TableCell<Session, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setGraphic(createBadge(item, getStatusColor(item)));
                    setText(null);
                }
            }
        });

        TableColumn<Session, Void> videoCol = new TableColumn<>("Video");
        videoCol.setPrefWidth(80);
        videoCol.setStyle(columnStyle());
        videoCol.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Session, Void> call(TableColumn<Session, Void> param) {
                return new TableCell<>() {
                    private final Button videoButton = new Button("📹 Call");
                    {
                        videoButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                        videoButton.setTextFill(Color.WHITE);
                        videoButton.setStyle(
                                "-fx-background-color: " + cssColor(VIDEO_BUTTON_COLOR) + ";" +
                                        "-fx-background-radius: 999px;" +
                                        "-fx-padding: 4px 12px;" +
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
                            if (session.getSessionType().equalsIgnoreCase("Online") && session.getReservedBy() != null) {
                                setGraphic(videoButton);
                                setAlignment(Pos.CENTER);
                            } else {
                                setGraphic(null);
                            }
                        }
                    }
                };
            }
        });

        TableColumn<Session, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(110);
        actionsCol.setStyle(columnStyle());
        actionsCol.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Session, Void> call(TableColumn<Session, Void> param) {
                return new TableCell<>() {
                    private final Button manageButton = createOutlineButton("Manage");
                    {
                        manageButton.setOnAction(e -> {
                            Session session = getTableView().getItems().get(getIndex());
                            showManageDialog(session);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) setGraphic(null);
                        else {
                            setGraphic(manageButton);
                            setAlignment(Pos.CENTER);
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

    private String columnStyle() {
        return "-fx-alignment: CENTER-LEFT;" +
                "-fx-font-size: 14px;" +
                "-fx-border-color: " + cssColor(LINE) + ";" +
                "-fx-border-width: 0 0 1 0;";
    }

    private void showManageDialog(Session session) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("Manage Session");
        dialog.setMinWidth(680);
        dialog.setMinHeight(520);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + cssColor(PAGE_BG) + ";");
        root.setPadding(new Insets(24));

        VBox contentPanel = new VBox(20);
        contentPanel.setStyle(cardStyle());

        Label headerLabel = new Label("⚙ Manage Session");
        headerLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        headerLabel.setTextFill(EMERALD_DARK);

        Label titleDisplay = new Label(session.getTitle());
        titleDisplay.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        titleDisplay.setTextFill(INK);
        titleDisplay.setWrapText(true);

        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(24);
        detailsGrid.setVgap(14);
        detailsGrid.setPadding(new Insets(20, 0, 20, 0));

        Label dateLabel = new Label("📅 Date:");
        dateLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        dateLabel.setTextFill(INK);
        Label dateValue = new Label(session.getSessionDate().format(dateFormatter));
        dateValue.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        dateValue.setTextFill(MUTED);

        Label timeLabel = new Label("⏰ Time:");
        timeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        timeLabel.setTextFill(INK);
        Label timeValue = new Label(session.getStartTime().format(timeFormatter) + " - " + session.getEndTime().format(timeFormatter));
        timeValue.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        timeValue.setTextFill(MUTED);

        Label locationLabel = new Label("📍 Location:");
        locationLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        locationLabel.setTextFill(INK);
        Label locationValue = new Label(session.getLocation());
        locationValue.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        locationValue.setTextFill(MUTED);

        Label typeLabel = new Label("🏷️ Type:");
        typeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        typeLabel.setTextFill(INK);
        Label typeValue = new Label(session.getSessionType());
        typeValue.setTextFill(getTypeColor(session.getSessionType()));
        typeValue.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));

        Label statusLabel = new Label("Status:");
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        statusLabel.setTextFill(INK);
        Label statusValue = new Label(session.getStatus());
        statusValue.setTextFill(getStatusColor(session.getStatus()));
        statusValue.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));

        detailsGrid.add(dateLabel, 0, 0);
        detailsGrid.add(dateValue, 1, 0);
        detailsGrid.add(timeLabel, 2, 0);
        detailsGrid.add(timeValue, 3, 0);
        detailsGrid.add(locationLabel, 0, 1);
        detailsGrid.add(locationValue, 1, 1);
        detailsGrid.add(typeLabel, 2, 1);
        detailsGrid.add(typeValue, 3, 1);
        detailsGrid.add(statusLabel, 0, 2);
        detailsGrid.add(statusValue, 1, 2);

        Label questionLabel = new Label("What would you like to do?");
        questionLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
        questionLabel.setTextFill(MUTED);
        questionLabel.setPadding(new Insets(10, 0, 0, 0));

        contentPanel.getChildren().addAll(headerLabel, titleDisplay, detailsGrid, questionLabel);

        HBox buttonPanel = new HBox(14);
        buttonPanel.setPadding(new Insets(20, 0, 0, 0));
        buttonPanel.setAlignment(Pos.CENTER);

        Button editBtn = createManageButton("✏️ Edit", EMERALD);
        editBtn.setOnAction(e -> { dialog.close(); showEditSessionDialog(session); });

        Button deleteBtn = createManageButton("🗑️ Delete", Color.web("#E74C3C"));
        deleteBtn.setOnAction(e -> {
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

        Button toggleBtn = createManageButton(
                "active".equals(session.getStatus()) ? "🔴 Deactivate" : "🟢 Activate",
                Color.web("#F39C12")
        );
        toggleBtn.setOnAction(e -> {
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

        Button cancelBtn = createManageButton("Close", MUTED);
        cancelBtn.setOnAction(e -> dialog.close());

        buttonPanel.getChildren().addAll(editBtn, deleteBtn, toggleBtn, cancelBtn);
        contentPanel.getChildren().add(buttonPanel);

        root.setCenter(contentPanel);
        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    private Button createManageButton(String text, Color bgColor) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        button.setTextFill(Color.WHITE);
        button.setStyle(
                "-fx-background-color: " + cssColor(bgColor) + ";" +
                        "-fx-background-radius: 999px;" +
                        "-fx-padding: 8px 22px;" +
                        "-fx-cursor: hand;"
        );
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: " + cssColor(bgColor.darker()) + ";" +
                        "-fx-background-radius: 999px;" +
                        "-fx-padding: 8px 22px;" +
                        "-fx-cursor: hand;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: " + cssColor(bgColor) + ";" +
                        "-fx-background-radius: 999px;" +
                        "-fx-padding: 8px 22px;" +
                        "-fx-cursor: hand;"
        ));
        return button;
    }

    private void startVideoCall(Session session) {
        if (!session.getSessionType().equalsIgnoreCase("Online")) {
            showAlert("Not Available", "Video calls are only available for online sessions.", Alert.AlertType.WARNING);
            return;
        }

        if (session.getReservedBy() == null) {
            showAlert("No Patient", "This session has no patient reserved yet.", Alert.AlertType.WARNING);
            return;
        }

        LocalDateTime sessionTime = LocalDateTime.of(session.getSessionDate(), session.getStartTime());
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(sessionTime.minusMinutes(15))) {
            showAlert("Too Early", "Video call will be available 15 minutes before the session.", Alert.AlertType.WARNING);
            return;
        }

        if (now.isAfter(sessionTime.plusHours(1))) {
            showAlert("Session Ended", "This session has already ended.", Alert.AlertType.WARNING);
            return;
        }

        String meetingLink = videoCallService.generateMeetingLink(
                session.getSessionId(),
                session.getReservedBy(),
                parentApp.getUserId()
        );

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Start Video Call");
        confirm.setHeaderText("Start video session for: " + session.getTitle());
        confirm.setContentText("You will be redirected to Jitsi Meet in your browser.\n\nReady to start?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                videoCallService.joinMeeting(meetingLink);
            }
        });
    }

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
                sessionTable.setPlaceholder(new Label("No sessions available. Click + Add Session to create one."));
                return;
            }
            sessionTable.getItems().addAll(sessions);
        } catch (SQLException e) {
            sessionTable.setPlaceholder(new Label("Database Error: " + e.getMessage()));
            showAlert("Database Error", "Cannot connect to database.\nError: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String cssColor(Color color) {
        return "#" + toHex(color);
    }

    private String cardShadow() {
        return "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 5);";
    }

    private String cardStyle() {
        return "-fx-background-color: white;" +
                "-fx-background-radius: 20px;" +
                "-fx-padding: 28px;" +
                cardShadow();
    }

    private String toHex(Color color) {
        return String.format("%02x%02x%02x",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }
}