package ui;

import controller.SessionController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import models.Session;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReservationsPanel extends VBox {

    private MentisLoginFrame parentApp;
    private SessionController sessionController;
    private TableView<Session> reservationsTable;
    private SimpleCalendarPanel calendarPanel; // ⭐ NEW: Calendar panel
    private Label userInfoLabel;
    private Button toggleViewButton;
    private boolean isTableView = true; // ⭐ Track current view

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    // Colors
    private static final Color BACKGROUND_LIGHT = Color.rgb(240, 248, 245);
    private static final Color ACCENT_DARK_GREEN = Color.rgb(60, 120, 90);
    private static final Color BUTTON_LIGHT_GREEN = Color.rgb(160, 200, 180);
    private static final Color TEXT_DARK = Color.rgb(40, 70, 50);
    private static final Color TEXT_LIGHT = Color.rgb(100, 130, 110);
    private static final Color BORDER_LIGHT = Color.rgb(200, 220, 210);
    private static final Color STATUS_CONFIRMED = Color.rgb(52, 152, 219);
    private static final Color STATUS_COMPLETED = Color.rgb(39, 174, 96);
    private static final Color STATUS_CANCELLED = Color.rgb(192, 57, 43);

    public ReservationsPanel(MentisLoginFrame parentApp, SessionController sessionController) {
        this.parentApp = parentApp;
        this.sessionController = sessionController;

        setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        setPadding(new Insets(30));
        setSpacing(20);

        createHeader();
        createTable();
        createCalendarPanel(); // ⭐ NEW: Initialize calendar panel
        refreshData();
    }

    private void createHeader() {
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 20, 0));

        VBox titleBox = new VBox(10);
        Label titleLabel = new Label("Session Reservations");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        Label subtitleLabel = new Label("View all patient reservations");
        subtitleLabel.setFont(Font.font("Segoe UI", 16));
        subtitleLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // ⭐ NEW: Toggle View Button
        toggleViewButton = new Button("📅 Switch to Calendar View");
        toggleViewButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        toggleViewButton.setTextFill(Color.WHITE);
        toggleViewButton.setStyle("-fx-background-color: #" + toHex(ACCENT_DARK_GREEN) + "; -fx-background-radius: 5; -fx-padding: 10 20; -fx-cursor: hand;");
        toggleViewButton.setOnAction(e -> toggleView());

        userInfoLabel = new Label(parentApp.getUserName() + " (" + parentApp.getUserType() + ")");
        userInfoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        userInfoLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        headerBox.getChildren().addAll(titleBox, spacer, toggleViewButton, userInfoLabel);
        getChildren().add(headerBox);
    }

    private void createTable() {
        reservationsTable = new TableView<>();
        reservationsTable.setStyle("-fx-background-color: white; -fx-border-color: #" + toHex(BORDER_LIGHT) + ";");
        reservationsTable.setFixedCellSize(50);
        reservationsTable.setPlaceholder(new Label("No reservations found"));

        // Session Title Column
        TableColumn<Session, String> titleCol = new TableColumn<>("Session Title");
        titleCol.setCellValueFactory(cellData -> {
            Session session = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(session.getTitle());
        });
        titleCol.setPrefWidth(200);

        // Patient ID Column
        TableColumn<Session, String> patientCol = new TableColumn<>("Patient ID");
        patientCol.setCellValueFactory(cellData -> {
            Session session = cellData.getValue();
            String patientId = session.getReservedBy() != null ? String.valueOf(session.getReservedBy()) : "-";
            return new javafx.beans.property.SimpleStringProperty(patientId);
        });
        patientCol.setPrefWidth(100);

        // Date Column
        TableColumn<Session, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> {
            Session session = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(session.getSessionDate().format(dateFormatter));
        });
        dateCol.setPrefWidth(100);

        // Time Column
        TableColumn<Session, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(cellData -> {
            Session session = cellData.getValue();
            String timeRange = session.getStartTime().format(timeFormatter) + " - " + session.getEndTime().format(timeFormatter);
            return new javafx.beans.property.SimpleStringProperty(timeRange);
        });
        timeCol.setPrefWidth(150);

        // Location Column
        TableColumn<Session, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(cellData -> {
            Session session = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(session.getLocation());
        });
        locationCol.setPrefWidth(150);

        // Type Column
        TableColumn<Session, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cellData -> {
            Session session = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(session.getSessionType());
        });
        typeCol.setPrefWidth(100);

        // Status Column
        TableColumn<Session, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> {
            Session session = cellData.getValue();
            String status = session.getReservedBy() != null ? "Reserved" : "Available";
            return new javafx.beans.property.SimpleStringProperty(status);
        });
        statusCol.setCellFactory(column -> new TableCell<Session, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Reserved".equals(item)) {
                        setTextFill(Color.web(toHex(STATUS_CONFIRMED)));
                        setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                    } else {
                        setTextFill(Color.web(toHex(TEXT_LIGHT)));
                    }
                }
            }
        });
        statusCol.setPrefWidth(100);

        reservationsTable.getColumns().addAll(titleCol, patientCol, dateCol, timeCol, locationCol, typeCol, statusCol);

        VBox.setVgrow(reservationsTable, Priority.ALWAYS);
        getChildren().add(reservationsTable);
    }

    // ⭐ NEW: Create calendar panel
    private void createCalendarPanel() {
        calendarPanel = new SimpleCalendarPanel(parentApp);
        calendarPanel.setVisible(false); // Hidden by default
        calendarPanel.setManaged(false);
        getChildren().add(calendarPanel);
    }

    // ⭐ NEW: Toggle between table and calendar views
    private void toggleView() {
        isTableView = !isTableView;

        if (isTableView) {
            // Show table, hide calendar
            reservationsTable.setVisible(true);
            reservationsTable.setManaged(true);
            calendarPanel.setVisible(false);
            calendarPanel.setManaged(false);
            toggleViewButton.setText("📅 Switch to Calendar View");
            refreshData(); // Refresh table data
        } else {
            // Show calendar, hide table
            reservationsTable.setVisible(false);
            reservationsTable.setManaged(false);
            calendarPanel.setVisible(true);
            calendarPanel.setManaged(true);
            calendarPanel.refreshData(); // Refresh calendar data
            toggleViewButton.setText("📋 Switch to Table View");
        }
    }

    public void refreshData() {
        try {
            List<Session> allSessions = sessionController.getAllSessions();
            List<Session> reservedSessions = allSessions.stream()
                    .filter(s -> s.getReservedBy() != null)
                    .toList();

            reservationsTable.getItems().clear();
            reservationsTable.getItems().addAll(reservedSessions);

        } catch (SQLException e) {
            showAlert("Error", "Failed to load reservations: " + e.getMessage(), Alert.AlertType.ERROR);
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
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}