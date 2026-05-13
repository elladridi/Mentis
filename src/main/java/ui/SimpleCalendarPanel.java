package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import models.Session;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SimpleCalendarPanel extends VBox {

    private MentisLoginFrame parentApp;
    private GridPane calendarGrid;
    private Label monthYearLabel;
    private YearMonth currentYearMonth;
    private List<Session> sessions;

    private Button prevMonthButton;
    private Button nextMonthButton;

    // Symfony-style green colors
    private static final Color PAGE_BG = Color.web("#F8F9FA");
    private static final Color SOFT_GREEN_BG = Color.web("#F1F8E9");
    private static final Color EMERALD = Color.web("#50C878");
    private static final Color EMERALD_DARK = Color.web("#2E7D32");
    private static final Color EMERALD_MID = Color.web("#3A9B5E");
    private static final Color INK = Color.web("#1A3C34");
    private static final Color MUTED = Color.web("#6C757D");
    private static final Color LINE = Color.web("#E9ECEF");
    private static final Color DAY_HOVER = Color.web("#F1F8E9");
    private static final Color SESSION_BADGE = Color.web("#E8F5E9");

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public SimpleCalendarPanel(MentisLoginFrame parentApp) {
        this.parentApp = parentApp;
        this.sessions = new ArrayList<>();
        this.currentYearMonth = YearMonth.now();

        setStyle("-fx-background-color: white; -fx-background-radius: 20px; " + cardShadow());
        setPadding(new Insets(20));
        setSpacing(15);

        createHeader();
        createCalendarGrid();
        refreshData();
    }

    private void createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 15, 0));

        prevMonthButton = createIconButton("◀");
        prevMonthButton.setOnAction(e -> changeMonth(-1));

        monthYearLabel = new Label();
        monthYearLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        monthYearLabel.setTextFill(EMERALD_DARK);
        monthYearLabel.setPrefWidth(200);
        monthYearLabel.setAlignment(Pos.CENTER);

        nextMonthButton = createIconButton("▶");
        nextMonthButton.setOnAction(e -> changeMonth(1));

        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        header.getChildren().addAll(prevMonthButton, leftSpacer, monthYearLabel, rightSpacer, nextMonthButton);
        getChildren().add(header);
    }

    private Button createIconButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        button.setTextFill(EMERALD_DARK);
        button.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-background-radius: 999px;" +
                        "-fx-padding: 8px 16px;" +
                        "-fx-cursor: hand;"
        );
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: " + cssColor(SOFT_GREEN_BG) + ";" +
                        "-fx-background-radius: 999px;" +
                        "-fx-padding: 8px 16px;" +
                        "-fx-cursor: hand;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-background-radius: 999px;" +
                        "-fx-padding: 8px 16px;" +
                        "-fx-cursor: hand;"
        ));
        return button;
    }

    private void createCalendarGrid() {
        calendarGrid = new GridPane();
        calendarGrid.setHgap(8);
        calendarGrid.setVgap(8);
        calendarGrid.setAlignment(Pos.CENTER);
        calendarGrid.setPadding(new Insets(10));

        // Day headers
        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < dayNames.length; i++) {
            Label dayLabel = new Label(dayNames[i]);
            dayLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            dayLabel.setTextFill(EMERALD_DARK);
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setPrefWidth(100);
            dayLabel.setPadding(new Insets(8));
            dayLabel.setStyle("-fx-background-color: " + cssColor(SOFT_GREEN_BG) + "; -fx-background-radius: 10px;");
            calendarGrid.add(dayLabel, i, 0);
        }

        getChildren().add(calendarGrid);
    }

    private void changeMonth(int delta) {
        currentYearMonth = currentYearMonth.plusMonths(delta);
        updateCalendar();
    }

    private void updateCalendar() {
        monthYearLabel.setText(currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));

        calendarGrid.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeekValue = firstOfMonth.getDayOfWeek().getValue();
        int startOffset = dayOfWeekValue - 1;

        int daysInMonth = currentYearMonth.lengthOfMonth();
        int row = 1;
        int col = 0;

        for (int i = 0; i < startOffset; i++) {
            VBox emptyCell = createEmptyCell();
            calendarGrid.add(emptyCell, col, row);
            col++;
        }

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            VBox dayCell = createDayCell(date);
            calendarGrid.add(dayCell, col, row);
            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
        }

        while (col < 7 && col != 0) {
            VBox emptyCell = createEmptyCell();
            calendarGrid.add(emptyCell, col, row);
            col++;
        }
    }

    private VBox createEmptyCell() {
        VBox cell = new VBox();
        cell.setPrefWidth(100);
        cell.setPrefHeight(80);
        cell.setAlignment(Pos.CENTER);
        cell.setStyle("-fx-background-color: transparent;");
        return cell;
    }

    private VBox createDayCell(LocalDate date) {
        VBox cell = new VBox(5);
        cell.setPrefWidth(100);
        cell.setPrefHeight(80);
        cell.setAlignment(Pos.TOP_CENTER);
        cell.setPadding(new Insets(8, 4, 4, 4));
        cell.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12px;" +
                        "-fx-border-color: " + cssColor(LINE) + ";" +
                        "-fx-border-radius: 12px;" +
                        "-fx-border-width: 1px;"
        );
        cell.setOnMouseEntered(e -> cell.setStyle(
                "-fx-background-color: " + cssColor(DAY_HOVER) + ";" +
                        "-fx-background-radius: 12px;" +
                        "-fx-border-color: " + cssColor(EMERALD) + ";" +
                        "-fx-border-radius: 12px;" +
                        "-fx-border-width: 1.5px;"
        ));
        cell.setOnMouseExited(e -> cell.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12px;" +
                        "-fx-border-color: " + cssColor(LINE) + ";" +
                        "-fx-border-radius: 12px;" +
                        "-fx-border-width: 1px;"
        ));

        Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dayLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        // Highlight today
        if (date.equals(LocalDate.now())) {
            dayLabel.setTextFill(EMERALD_DARK);
            dayLabel.setStyle("-fx-background-color: " + cssColor(EMERALD) + "; -fx-background-radius: 999px; -fx-padding: 2px 6px;");
        } else {
            dayLabel.setTextFill(INK);
        }

        cell.getChildren().add(dayLabel);

        // Add session indicators
        for (Session session : sessions) {
            if (session.getSessionDate().equals(date) && session.getReservedBy() != null) {
                Label sessionBadge = new Label("📅");
                sessionBadge.setFont(Font.font("Segoe UI Emoji", 10));
                sessionBadge.setTooltip(new Tooltip(session.getTitle() + "\n" + session.getStartTime()));
                cell.getChildren().add(sessionBadge);
            }
        }

        return cell;
    }

    public void refreshData() {
        try {
            sessions = parentApp.getSessionController().getAllSessions();
            updateCalendar();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String cssColor(Color color) {
        return "#" + toHex(color);
    }

    private String cardShadow() {
        return "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 5);";
    }

    private String toHex(Color color) {
        return String.format("%02x%02x%02x",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }
}