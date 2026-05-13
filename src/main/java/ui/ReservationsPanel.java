package ui;

import controller.SessionController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
    private SimpleCalendarPanel calendarPanel;
    private Label userInfoLabel;
    private Button toggleViewButton;
    private boolean isTableView = true;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    // Symfony-style green colors
    private static final Color PAGE_BG = Color.web("#F8F9FA");
    private static final Color SOFT_GREEN_BG = Color.web("#F1F8E9");
    private static final Color EMERALD = Color.web("#50C878");
    private static final Color EMERALD_DARK = Color.web("#2E7D32");
    private static final Color EMERALD_MID = Color.web("#3A9B5E");
    private static final Color INK = Color.web("#1A3C34");
    private static final Color MUTED = Color.web("#6C757D");
    private static final Color LINE = Color.web("#E9ECEF");
    private static final Color STATUS_RESERVED = Color.web("#3498DB");
    private static final Color STATUS_COMPLETED = Color.web("#27AE60");
    private static final Color STATUS_CANCELLED = Color.web("#E74C3C");

    public ReservationsPanel(MentisLoginFrame parentApp, SessionController sessionController) {
        this.parentApp = parentApp;
        this.sessionController = sessionController;

        setStyle("-fx-background-color: " + cssColor(PAGE_BG) + ";");
        setPadding(new Insets(44, 56, 44, 56));
        setSpacing(28);

        createHeader();
        createTable();
        createCalendarPanel();
        refreshData();
    }

    private void createHeader() {
        VBox headerBox = new VBox(8);
        headerBox.setPadding(new Insets(0, 0, 24, 0));

        Label titleLabel = new Label("📋 Session Reservations");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 40));
        titleLabel.setTextFill(EMERALD_DARK);

        Label subtitleLabel = new Label("View and manage all patient session reservations");
        subtitleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        subtitleLabel.setTextFill(MUTED);

        HBox actionBar = new HBox();
        actionBar.setAlignment(Pos.CENTER_RIGHT);
        actionBar.setPadding(new Insets(20, 0, 0, 0));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toggleViewButton = createOutlineButton("📅 Calendar View");
        toggleViewButton.setOnAction(e -> toggleView());

        userInfoLabel = new Label(parentApp.getUserName() + " (" + parentApp.getUserType() + ")");
        userInfoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        userInfoLabel.setTextFill(MUTED);
        userInfoLabel.setPadding(new Insets(0, 0, 0, 20));

        actionBar.getChildren().addAll(spacer, toggleViewButton, userInfoLabel);

        headerBox.getChildren().addAll(titleLabel, subtitleLabel, actionBar);
        getChildren().add(headerBox);
    }

    private void createTable() {
        reservationsTable = new TableView<>();
        reservationsTable.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 5);"
        );
        reservationsTable.setFixedCellSize(60);
        reservationsTable.setPlaceholder(createEmptyTableLabel());

        // Session Title Column
        TableColumn<Session, String> titleCol = new TableColumn<>("Session Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(220);
        titleCol.setStyle(columnStyle());
        titleCol.setCellFactory(column -> new TableCell<Session, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                    setTextFill(INK);
                }
            }
        });

        // Patient ID Column
        TableColumn<Session, Integer> patientCol = new TableColumn<>("Patient ID");
        patientCol.setCellValueFactory(new PropertyValueFactory<>("reservedBy"));
        patientCol.setPrefWidth(100);
        patientCol.setStyle(columnStyle());
        patientCol.setCellFactory(column -> new TableCell<Session, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("-");
                } else {
                    setText(String.valueOf(item));
                    setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
                    setTextFill(MUTED);
                }
            }
        });

        // Date Column
        TableColumn<Session, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> {
            Session session = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(session.getSessionDate().format(dateFormatter));
        });
        dateCol.setPrefWidth(110);
        dateCol.setStyle(columnStyle());
        dateCol.setCellFactory(column -> new TableCell<Session, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
                    setTextFill(MUTED);
                }
            }
        });

        // Time Column
        TableColumn<Session, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(cellData -> {
            Session session = cellData.getValue();
            String timeRange = session.getStartTime().format(timeFormatter) + " - " + session.getEndTime().format(timeFormatter);
            return new javafx.beans.property.SimpleStringProperty(timeRange);
        });
        timeCol.setPrefWidth(130);
        timeCol.setStyle(columnStyle());
        timeCol.setCellFactory(column -> new TableCell<Session, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
                    setTextFill(MUTED);
                }
            }
        });

        // Location Column
        TableColumn<Session, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        locationCol.setPrefWidth(140);
        locationCol.setStyle(columnStyle());
        locationCol.setCellFactory(column -> new TableCell<Session, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
                    setTextFill(MUTED);
                }
            }
        });

        // Type Column
        TableColumn<Session, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("sessionType"));
        typeCol.setPrefWidth(100);
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

        // Status Column
        TableColumn<Session, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> {
            Session session = cellData.getValue();
            String status = session.getReservedBy() != null ? "Reserved" : "Available";
            return new javafx.beans.property.SimpleStringProperty(status);
        });
        statusCol.setPrefWidth(100);
        statusCol.setStyle(columnStyle());
        statusCol.setCellFactory(column -> new TableCell<Session, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Color color = "Reserved".equals(item) ? STATUS_RESERVED : MUTED;
                    setGraphic(createBadge(item, color));
                    setText(null);
                }
            }
        });

        reservationsTable.getColumns().addAll(titleCol, patientCol, dateCol, timeCol, locationCol, typeCol, statusCol);

        VBox.setVgrow(reservationsTable, Priority.ALWAYS);
        getChildren().add(reservationsTable);
    }

    private Label createEmptyTableLabel() {
        Label label = new Label("📭 No reservations found");
        label.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        label.setTextFill(MUTED);
        return label;
    }

    private Label createBadge(String text, Color bgColor) {
        Label badge = new Label(text);
        badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        badge.setTextFill(Color.WHITE);
        badge.setPadding(new Insets(5, 14, 5, 14));
        badge.setStyle("-fx-background-color: " + cssColor(bgColor) + "; -fx-background-radius: 999px;");
        return badge;
    }

    private Color getTypeColor(String type) {
        if (type == null) return Color.web("#7F8C8D");
        switch (type.toLowerCase()) {
            case "individual": return Color.web("#5B8C5A");
            case "group": return Color.web("#27AE60");
            case "family": return Color.web("#8E44AD");
            case "couple": return Color.web("#E67E22");
            case "online": return Color.web("#3498DB");
            default: return Color.web("#7F8C8D");
        }
    }

    private String columnStyle() {
        return "-fx-alignment: CENTER-LEFT;" +
                "-fx-font-size: 13px;" +
                "-fx-border-color: " + cssColor(LINE) + ";" +
                "-fx-border-width: 0 0 1 0;";
    }

    private void createCalendarPanel() {
        calendarPanel = new SimpleCalendarPanel(parentApp);
        calendarPanel.setVisible(false);
        calendarPanel.setManaged(false);
        calendarPanel.setStyle("-fx-background-color: white; -fx-background-radius: 20px; " + cardShadow());
        getChildren().add(calendarPanel);
        VBox.setVgrow(calendarPanel, Priority.ALWAYS);
    }

    private void toggleView() {
        isTableView = !isTableView;

        if (isTableView) {
            reservationsTable.setVisible(true);
            reservationsTable.setManaged(true);
            calendarPanel.setVisible(false);
            calendarPanel.setManaged(false);
            toggleViewButton.setText("📅 Calendar View");
            toggleViewButton.setStyle(createOutlineButtonStyle());
            refreshData();
        } else {
            reservationsTable.setVisible(false);
            reservationsTable.setManaged(false);
            calendarPanel.setVisible(true);
            calendarPanel.setManaged(true);
            calendarPanel.refreshData();
            toggleViewButton.setText("📋 Table View");
            toggleViewButton.setStyle(createOutlineButtonStyle());
        }
    }

    private Button createOutlineButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        button.setTextFill(MUTED);
        button.setStyle(createOutlineButtonStyle());
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: " + cssColor(SOFT_GREEN_BG) + ";" +
                        "-fx-background-radius: 999px;" +
                        "-fx-border-color: " + cssColor(EMERALD) + ";" +
                        "-fx-border-radius: 999px;" +
                        "-fx-border-width: 1.5px;" +
                        "-fx-padding: 8px 20px;" +
                        "-fx-cursor: hand;"
        ));
        button.setOnMouseExited(e -> button.setStyle(createOutlineButtonStyle()));
        return button;
    }

    private String createOutlineButtonStyle() {
        return "-fx-background-color: white;" +
                "-fx-background-radius: 999px;" +
                "-fx-border-color: " + cssColor(LINE) + ";" +
                "-fx-border-radius: 999px;" +
                "-fx-border-width: 1.5px;" +
                "-fx-padding: 8px 20px;" +
                "-fx-cursor: hand;";
    }

    public void refreshData() {
        try {
            List<Session> allSessions = sessionController.getAllSessions();
            List<Session> reservedSessions = allSessions.stream()
                    .filter(s -> s.getReservedBy() != null)
                    .toList();

            reservationsTable.getItems().clear();
            if (reservedSessions.isEmpty()) {
                reservationsTable.setPlaceholder(createEmptyTableLabel());
            } else {
                reservationsTable.getItems().addAll(reservedSessions);
            }

            if (calendarPanel != null && calendarPanel.isVisible()) {
                calendarPanel.refreshData();
            }

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

    private String cssColor(Color color) {
        return "#" + toHex(color);
    }

    private String cardShadow() {
        return "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 5);";
    }

    private String toHex(Color color) {
        return String.format("%02x%02x%02x",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }
}