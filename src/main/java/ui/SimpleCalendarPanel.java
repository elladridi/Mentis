package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import services.CalendarService;
import services.CalendarService.CalendarEvent;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class SimpleCalendarPanel extends VBox {

    private MentisLoginFrame parentApp;
    private CalendarService calendarService;
    private YearMonth currentYearMonth;
    private GridPane calendarGrid;
    private Label monthLabel;
    private Label infoLabel;
    private ScrollPane scrollPane; // ⭐ Added ScrollPane

    private DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Colors
    private static final Color BACKGROUND_LIGHT = Color.rgb(240, 248, 245);
    private static final Color ACCENT_DARK_GREEN = Color.rgb(60, 120, 90);
    private static final Color RESERVED_DAY_COLOR = Color.rgb(88, 139, 113);
    private static final Color TODAY_COLOR = Color.rgb(255, 193, 7);
    private static final Color TEXT_DARK = Color.rgb(40, 70, 50);
    private static final Color BORDER_LIGHT = Color.rgb(200, 220, 210);

    public SimpleCalendarPanel(MentisLoginFrame parentApp) {
        this.parentApp = parentApp;
        this.calendarService = new CalendarService();
        this.currentYearMonth = YearMonth.now();

        setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        setPadding(new Insets(30));
        setSpacing(20);

        // ⭐ Create a content container that will go inside ScrollPane
        VBox contentContainer = new VBox(20);
        contentContainer.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");

        createHeader(contentContainer);
        createCalendarControls(contentContainer);
        createCalendarGrid(contentContainer);
        createInfoPanel(contentContainer);

        // ⭐ Wrap everything in a ScrollPane
        scrollPane = new ScrollPane(contentContainer);
        scrollPane.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
        scrollPane.setBorder(null);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Make ScrollPane take all available space
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().add(scrollPane);

        loadCalendarData();
    }

    private void createHeader(VBox container) {
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 20, 0));

        VBox titleBox = new VBox(10);
        Label titleLabel = new Label("Session Calendar");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        Label subtitleLabel = new Label("Days with reserved sessions are highlighted");
        subtitleLabel.setFont(Font.font("Segoe UI", 16));
        subtitleLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userInfoLabel = new Label(parentApp.getUserName());
        userInfoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        userInfoLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));

        headerBox.getChildren().addAll(titleBox, spacer, userInfoLabel);
        container.getChildren().add(headerBox);
    }

    private void createCalendarControls(VBox container) {
        HBox controlsBox = new HBox(20);
        controlsBox.setAlignment(Pos.CENTER);
        controlsBox.setPadding(new Insets(10, 0, 20, 0));

        Button prevButton = new Button("◀ Previous");
        prevButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        prevButton.setTextFill(Color.WHITE);
        prevButton.setStyle("-fx-background-color: #" + toHex(ACCENT_DARK_GREEN) + "; -fx-background-radius: 5; -fx-padding: 8 20; -fx-cursor: hand;");
        prevButton.setOnAction(e -> changeMonth(-1));

        monthLabel = new Label(currentYearMonth.format(monthFormatter));
        monthLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        monthLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));
        monthLabel.setMinWidth(250);
        monthLabel.setAlignment(Pos.CENTER);

        Button nextButton = new Button("Next ▶");
        nextButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        nextButton.setTextFill(Color.WHITE);
        nextButton.setStyle("-fx-background-color: #" + toHex(ACCENT_DARK_GREEN) + "; -fx-background-radius: 5; -fx-padding: 8 20; -fx-cursor: hand;");
        nextButton.setOnAction(e -> changeMonth(1));

        Button todayButton = new Button("Today");
        todayButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        todayButton.setTextFill(Color.WHITE);
        todayButton.setStyle("-fx-background-color: #" + toHex(ACCENT_DARK_GREEN) + "; -fx-background-radius: 5; -fx-padding: 8 20; -fx-cursor: hand;");
        todayButton.setOnAction(e -> goToToday());

        controlsBox.getChildren().addAll(prevButton, monthLabel, nextButton, todayButton);
        container.getChildren().add(controlsBox);
    }

    private void createCalendarGrid(VBox container) {
        calendarGrid = new GridPane();
        calendarGrid.setStyle("-fx-background-color: white; -fx-border-color: #" + toHex(BORDER_LIGHT) + "; -fx-border-width: 2;");
        calendarGrid.setPadding(new Insets(15));
        calendarGrid.setHgap(5);
        calendarGrid.setVgap(5);

        // Add day labels
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(days[i]);
            dayLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            dayLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setMaxWidth(Double.MAX_VALUE);
            dayLabel.setPadding(new Insets(10));
            dayLabel.setStyle("-fx-background-color: #" + toHex(BACKGROUND_LIGHT) + ";");
            calendarGrid.add(dayLabel, i, 0);
        }

        // Set column constraints - equal width
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            cc.setPercentWidth(14.28);
            calendarGrid.getColumnConstraints().add(cc);
        }

        // Make grid take full width
        calendarGrid.setMaxWidth(Double.MAX_VALUE);

        container.getChildren().add(calendarGrid);
    }

    private void createInfoPanel(VBox container) {
        HBox infoPanel = new HBox(30);
        infoPanel.setAlignment(Pos.CENTER);
        infoPanel.setPadding(new Insets(15, 0, 0, 0));

        // Legend
        HBox todayLegend = new HBox(10);
        todayLegend.setAlignment(Pos.CENTER_LEFT);
        Label todayDot = new Label("●");
        todayDot.setFont(Font.font("Segoe UI", 20));
        todayDot.setTextFill(Color.web(toHex(TODAY_COLOR)));
        Label todayText = new Label("Today");
        todayLegend.getChildren().addAll(todayDot, todayText);

        HBox reservedLegend = new HBox(10);
        reservedLegend.setAlignment(Pos.CENTER_LEFT);
        Label reservedDot = new Label("●");
        reservedDot.setFont(Font.font("Segoe UI", 20));
        reservedDot.setTextFill(Color.web(toHex(RESERVED_DAY_COLOR)));
        Label reservedText = new Label("Has reserved sessions");
        reservedLegend.getChildren().addAll(reservedDot, reservedText);

        infoPanel.getChildren().addAll(todayLegend, reservedLegend);

        infoLabel = new Label();
        infoLabel.setFont(Font.font("Segoe UI", 14));
        infoLabel.setTextFill(Color.web(toHex(ACCENT_DARK_GREEN)));
        infoLabel.setPadding(new Insets(10, 0, 0, 0));
        infoLabel.setAlignment(Pos.CENTER);

        VBox bottomBox = new VBox(10, infoPanel, infoLabel);
        bottomBox.setAlignment(Pos.CENTER);
        container.getChildren().add(bottomBox);
    }

    private void loadCalendarData() {
        try {
            Map<LocalDate, List<CalendarEvent>> sessionsByDate = calendarService.getSessionsByDate();
            updateCalendarGrid(sessionsByDate);

            int totalReserved = sessionsByDate.values().stream().mapToInt(List::size).sum();
            infoLabel.setText("Total reserved sessions: " + totalReserved);

        } catch (SQLException e) {
            showAlert("Error", "Failed to load calendar data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateCalendarGrid(Map<LocalDate, List<CalendarEvent>> sessionsByDate) {
        // Clear previous days (keep day labels)
        calendarGrid.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() - 1; // 0 for Monday

        int daysInMonth = currentYearMonth.lengthOfMonth();
        int row = 1;
        int col = dayOfWeek;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            boolean hasSessions = sessionsByDate.containsKey(date) && !sessionsByDate.get(date).isEmpty();
            boolean isToday = date.equals(LocalDate.now());

            StackPane dayCell = createDayCell(day, hasSessions, isToday);

            calendarGrid.add(dayCell, col, row);

            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
        }
    }

    private StackPane createDayCell(int day, boolean hasSessions, boolean isToday) {
        StackPane cell = new StackPane();
        cell.setMinHeight(80);
        cell.setStyle("-fx-background-color: white; -fx-border-color: #" + toHex(BORDER_LIGHT) + "; -fx-border-width: 1;");

        // Highlight if has sessions
        if (hasSessions) {
            cell.setStyle(cell.getStyle() + "-fx-background-color: #e8f5e9;"); // Light green background
        }

        // Highlight today with gold border
        if (isToday) {
            cell.setStyle(cell.getStyle() + "-fx-border-color: #" + toHex(TODAY_COLOR) + "; -fx-border-width: 3;");
        }

        Label dayLabel = new Label(String.valueOf(day));
        dayLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        dayLabel.setTextFill(Color.web(toHex(TEXT_DARK)));

        // Add a dot indicator for reserved days
        VBox content = new VBox(5);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(10));
        content.getChildren().add(dayLabel);

        if (hasSessions) {
            Label dotLabel = new Label("●");
            dotLabel.setFont(Font.font("Segoe UI", 16));
            dotLabel.setTextFill(Color.web(toHex(RESERVED_DAY_COLOR)));
            content.getChildren().add(dotLabel);
        }

        cell.getChildren().add(content);

        return cell;
    }

    private void changeMonth(int delta) {
        currentYearMonth = currentYearMonth.plusMonths(delta);
        monthLabel.setText(currentYearMonth.format(monthFormatter));
        loadCalendarData();

        // Scroll to top when changing months
        scrollPane.setVvalue(0);
    }

    private void goToToday() {
        currentYearMonth = YearMonth.now();
        monthLabel.setText(currentYearMonth.format(monthFormatter));
        loadCalendarData();

        // Scroll to top when going to today
        scrollPane.setVvalue(0);
    }

    public void refreshData() {
        loadCalendarData();
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