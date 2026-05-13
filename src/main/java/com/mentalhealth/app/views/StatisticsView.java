package com.mentalhealth.app.views;

import com.mentalhealth.app.models.Event;
import com.mentalhealth.app.models.EventRegistration;
import com.mentalhealth.app.utils.DatabaseConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StatisticsView {

    private static final String PRIMARY = "#50C878";
    private static final String PRIMARY_DARK = "#2E7D32";
    private static final String BLUE = "#4FACFE";
    private static final String PURPLE = "#9B5DE5";
    private static final String ORANGE = "#F39C12";
    private static final String RED = "#D62828";
    private static final String INK = "#1A3C34";
    private static final String MUTED = "#6C757D";
    private static final String BORDER = "#DDE5E2";

    public ScrollPane buildStatisticsView() {

        VBox page = new VBox(24);
        page.setPadding(new Insets(30));
        page.setStyle("-fx-background-color: transparent;");

        VBox hero = buildHero();
        HBox summaryCards = buildSummaryCards();

        GridPane chartsGrid = new GridPane();
        chartsGrid.setHgap(22);
        chartsGrid.setVgap(22);

        VBox eventTypeChart = buildEventTypeChart();
        VBox revenueChart = buildRevenueChart();
        VBox registrationsChart = buildRegistrationsChart();
        VBox ticketChart = buildTicketTypeChart();
        VBox statusChart = buildStatusChart();

        chartsGrid.add(eventTypeChart, 0, 0);
        chartsGrid.add(revenueChart, 1, 0);

        chartsGrid.add(registrationsChart, 0, 1, 2, 1);

        chartsGrid.add(ticketChart, 0, 2);
        chartsGrid.add(statusChart, 1, 2);

        page.getChildren().addAll(
                hero,
                summaryCards,
                chartsGrid
        );

        ScrollPane scrollPane = new ScrollPane(page);

        scrollPane.setFitToWidth(true);

        scrollPane.setStyle(
                "-fx-background: transparent;" +
                        "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;"
        );

        return scrollPane;
    }

    // =================== HERO ===================

    private VBox buildHero() {

        VBox hero = new VBox(10);

        hero.setPadding(new Insets(28));

        hero.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " +
                        PRIMARY + ", " + PRIMARY_DARK + ");" +
                        "-fx-background-radius: 28;" +
                        "-fx-effect: dropshadow(gaussian, rgba(47,93,82,0.20), 24, 0, 0, 8);"
        );

        Label title = new Label("Events Analytics Dashboard");

        title.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 32px;" +
                        "-fx-font-weight: 900;"
        );

        Label subtitle = new Label(
                "Track registrations, revenue, event engagement, attendance, and platform growth in real time."
        );

        subtitle.setWrapText(true);

        subtitle.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.88);" +
                        "-fx-font-size: 14px;"
        );

        HBox pills = new HBox(10);

        pills.getChildren().addAll(
                heroBadge("Live Metrics"),
                heroBadge("Shared Symfony + Java DB"),
                heroBadge("Real-Time Analytics")
        );

        hero.getChildren().addAll(title, subtitle, pills);

        return hero;
    }

    private Label heroBadge(String text) {

        Label badge = new Label(text);

        badge.setStyle(
                "-fx-background-color: rgba(255,255,255,0.22);" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 7 14;" +
                        "-fx-background-radius: 999;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;"
        );

        return badge;
    }

    // =================== SUMMARY ===================

    private HBox buildSummaryCards() {

        int totalEvents = Event.count();
        int totalParticipants = Event.totalParticipants();
        int totalRegistrations = EventRegistration.totalCount();
        int confirmed = EventRegistration.confirmedCount();
        double revenue = EventRegistration.totalRevenue();

        double avgTicket =
                confirmed > 0
                        ? revenue / confirmed
                        : 0;

        HBox row = new HBox(18);
        row.setAlignment(Pos.CENTER_LEFT);

        row.getChildren().addAll(
                statCard("Events", String.valueOf(totalEvents), PRIMARY),
                statCard("Participants", String.valueOf(totalParticipants), BLUE),
                statCard("Registrations", String.valueOf(totalRegistrations), PURPLE),
                statCard("Confirmed", String.valueOf(confirmed), ORANGE),
                statCard("Revenue", String.format("$%.2f", revenue), PRIMARY_DARK),
                statCard("Avg Ticket", String.format("$%.2f", avgTicket), RED)
        );

        return row;
    }

    private VBox statCard(String title, String value, String color) {

        VBox card = new VBox(8);

        card.setPadding(new Insets(18));
        card.setPrefWidth(170);

        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #FFFFFF, #F8FBFA);" +
                        "-fx-background-radius: 22;" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 22;" +
                        "-fx-effect: dropshadow(gaussian, rgba(47,93,82,0.08), 14, 0, 0, 5);"
        );

        Label valueLbl = new Label(value);

        valueLbl.setStyle(
                "-fx-text-fill: " + color + ";" +
                        "-fx-font-size: 26px;" +
                        "-fx-font-weight: 900;"
        );

        Label titleLbl = new Label(title);

        titleLbl.setStyle(
                "-fx-text-fill: " + MUTED + ";" +
                        "-fx-font-size: 12px;"
        );

        card.getChildren().addAll(valueLbl, titleLbl);

        return card;
    }

    // =================== CHART CONTAINERS ===================

    private VBox chartCard(String titleText) {

        VBox card = new VBox(16);

        card.setPadding(new Insets(24));

        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #FFFFFF, #F8FBFA);" +
                        "-fx-background-radius: 26;" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 26;" +
                        "-fx-effect: dropshadow(gaussian, rgba(47,93,82,0.08), 16, 0, 0, 5);"
        );

        Label title = new Label(titleText);

        title.setStyle(
                "-fx-text-fill: " + INK + ";" +
                        "-fx-font-size: 19px;" +
                        "-fx-font-weight: 900;"
        );

        card.getChildren().add(title);

        return card;
    }

    // =================== EVENT TYPES ===================

    private VBox buildEventTypeChart() {

        VBox card = chartCard("Events by Type");

        PieChart chart = new PieChart();
        chart.setLegendVisible(true);
        chart.setLabelsVisible(true);
        chart.setPrefSize(420, 320);

        Map<String, Integer> data = getEventCountByType();

        for (Map.Entry<String, Integer> entry : data.entrySet()) {

            chart.getData().add(
                    new PieChart.Data(
                            entry.getKey() + " (" + entry.getValue() + ")",
                            entry.getValue()
                    )
            );
        }

        card.getChildren().add(chart);

        return card;
    }

    // =================== REVENUE ===================

    private VBox buildRevenueChart() {

        VBox card = chartCard("Revenue by Event");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        xAxis.setLabel("Event");
        yAxis.setLabel("Revenue");

        BarChart<String, Number> chart =
                new BarChart<>(xAxis, yAxis);

        chart.setLegendVisible(false);
        chart.setPrefSize(420, 320);

        XYChart.Series<String, Number> series =
                new XYChart.Series<>();

        List<Event> events = Event.findAll();

        for (Event event : events) {

            double revenue =
                    EventRegistration.revenueByEvent(event.getId());

            if (revenue > 0) {

                String title =
                        event.getTitle().length() > 14
                                ? event.getTitle().substring(0, 14) + "..."
                                : event.getTitle();

                series.getData().add(
                        new XYChart.Data<>(title, revenue)
                );
            }
        }

        chart.getData().add(series);

        card.getChildren().add(chart);

        return card;
    }

    // =================== REGISTRATIONS OVER TIME ===================

    private VBox buildRegistrationsChart() {

        VBox card =
                chartCard("Registrations Over Time (Last 30 Days)");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        xAxis.setLabel("Date");
        yAxis.setLabel("Registrations");

        LineChart<String, Number> chart =
                new LineChart<>(xAxis, yAxis);

        chart.setPrefHeight(340);

        XYChart.Series<String, Number> series =
                new XYChart.Series<>();

        Map<String, Integer> data = getRegistrationsByDate();

        for (Map.Entry<String, Integer> entry : data.entrySet()) {

            series.getData().add(
                    new XYChart.Data<>(
                            entry.getKey(),
                            entry.getValue()
                    )
            );
        }

        chart.getData().add(series);

        card.getChildren().add(chart);

        return card;
    }

    // =================== TICKET TYPES ===================

    private VBox buildTicketTypeChart() {

        VBox card = chartCard("Ticket Type Distribution");

        PieChart chart = new PieChart();
        chart.setPrefSize(420, 320);

        Map<String, Integer> data =
                getTicketTypeDistribution();

        for (Map.Entry<String, Integer> entry : data.entrySet()) {

            chart.getData().add(
                    new PieChart.Data(
                            entry.getKey() + " (" + entry.getValue() + ")",
                            entry.getValue()
                    )
            );
        }

        card.getChildren().add(chart);

        return card;
    }

    // =================== STATUS ===================

    private VBox buildStatusChart() {

        VBox card = chartCard("Registration Status");

        PieChart chart = new PieChart();
        chart.setPrefSize(420, 320);

        Map<String, Integer> data =
                getStatusDistribution();

        for (Map.Entry<String, Integer> entry : data.entrySet()) {

            String emoji = switch (entry.getKey()) {
                case "CONFIRMED" -> "✅ ";
                case "PENDING" -> "⏳ ";
                case "CANCELLED" -> "❌ ";
                default -> "";
            };

            chart.getData().add(
                    new PieChart.Data(
                            emoji + entry.getKey() +
                                    " (" + entry.getValue() + ")",
                            entry.getValue()
                    )
            );
        }

        card.getChildren().add(chart);

        return card;
    }

    // =================== DATABASE METHODS ===================

    private Map<String, Integer> getEventCountByType() {

        Map<String, Integer> data = new LinkedHashMap<>();

        String sql =
                "SELECT event_type, COUNT(*) as count " +
                        "FROM events GROUP BY event_type ORDER BY count DESC";

        try (
                Statement st =
                        DatabaseConnection.getConnection().createStatement();

                ResultSet rs = st.executeQuery(sql)
        ) {

            while (rs.next()) {

                data.put(
                        rs.getString("event_type"),
                        rs.getInt("count")
                );
            }

        } catch (Exception e) {

            System.err.println(
                    "Error fetching event type stats: " +
                            e.getMessage()
            );
        }

        return data;
    }

    private Map<String, Integer> getRegistrationsByDate() {

        Map<String, Integer> data = new LinkedHashMap<>();

        DateTimeFormatter dtf =
                DateTimeFormatter.ofPattern("MM/dd");

        for (int i = 29; i >= 0; i--) {

            data.put(
                    LocalDate.now().minusDays(i).format(dtf),
                    0
            );
        }

        String sql =
                "SELECT DATE(registration_date) as reg_date, COUNT(*) as count " +
                        "FROM event_registrations " +
                        "WHERE registration_date >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
                        "GROUP BY DATE(registration_date) ORDER BY reg_date";

        try (
                Statement st =
                        DatabaseConnection.getConnection().createStatement();

                ResultSet rs = st.executeQuery(sql)
        ) {

            while (rs.next()) {

                LocalDate date =
                        rs.getDate("reg_date").toLocalDate();

                data.put(
                        date.format(dtf),
                        rs.getInt("count")
                );
            }

        } catch (Exception e) {

            System.err.println(
                    "Error fetching registrations chart: " +
                            e.getMessage()
            );
        }

        return data;
    }

    private Map<String, Integer> getTicketTypeDistribution() {

        Map<String, Integer> data = new LinkedHashMap<>();

        String sql =
                "SELECT ticket_type, COUNT(*) as count " +
                        "FROM event_registrations " +
                        "GROUP BY ticket_type ORDER BY count DESC";

        try (
                Statement st =
                        DatabaseConnection.getConnection().createStatement();

                ResultSet rs = st.executeQuery(sql)
        ) {

            while (rs.next()) {

                data.put(
                        rs.getString("ticket_type"),
                        rs.getInt("count")
                );
            }

        } catch (Exception e) {

            System.err.println(
                    "Error fetching ticket distribution: " +
                            e.getMessage()
            );
        }

        return data;
    }

    private Map<String, Integer> getStatusDistribution() {

        Map<String, Integer> data = new LinkedHashMap<>();

        String sql =
                "SELECT status, COUNT(*) as count " +
                        "FROM event_registrations " +
                        "GROUP BY status ORDER BY count DESC";

        try (
                Statement st =
                        DatabaseConnection.getConnection().createStatement();

                ResultSet rs = st.executeQuery(sql)
        ) {

            while (rs.next()) {

                data.put(
                        rs.getString("status"),
                        rs.getInt("count")
                );
            }

        } catch (Exception e) {

            System.err.println(
                    "Error fetching status stats: " +
                            e.getMessage()
            );
        }

        return data;
    }
}