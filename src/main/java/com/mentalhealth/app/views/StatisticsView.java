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

    public ScrollPane buildStatisticsView() {
        VBox container = new VBox(25);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: #FFFFFF;");

        // Page title
        Label title = ComponentFactory.pageTitle("📊 Event Statistics Dashboard");

        // Summary stats row
        HBox summaryRow = buildSummaryStats();

        // Charts grid
        GridPane chartsGrid = new GridPane();
        chartsGrid.setHgap(20);
        chartsGrid.setVgap(20);

        // Row 1: Pie chart + Bar chart
        VBox eventTypePie = buildEventTypeChart();
        VBox revenueBar = buildRevenueByEventChart();
        chartsGrid.add(eventTypePie, 0, 0);
        chartsGrid.add(revenueBar, 1, 0);

        // Row 2: Line chart (registrations over time)
        VBox registrationsLine = buildRegistrationsOverTimeChart();
        chartsGrid.add(registrationsLine, 0, 1, 2, 1);

        // Row 3: Ticket type distribution + Status distribution
        VBox ticketTypePie = buildTicketTypeChart();
        VBox statusPie = buildStatusChart();
        chartsGrid.add(ticketTypePie, 0, 2);
        chartsGrid.add(statusPie, 1, 2);

        container.getChildren().addAll(title, summaryRow, chartsGrid);

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #FFFFFF; -fx-background-color: #FFFFFF;");
        return scrollPane;
    }

    private HBox buildSummaryStats() {
        HBox stats = new HBox(20);
        stats.setAlignment(Pos.CENTER_LEFT);
        stats.setPadding(new Insets(20));
        stats.setStyle("-fx-background-color: #F1F6F4; -fx-background-radius: 15;" +
                "-fx-border-color: #DDE5E2; -fx-border-radius: 15;");

        int totalEvents = Event.count();
        int totalParticipants = Event.totalParticipants();
        int totalRegistrations = EventRegistration.totalCount();
        int confirmedRegistrations = EventRegistration.confirmedCount();
        double totalRevenue = EventRegistration.totalRevenue();
        double avgTicketPrice = totalRegistrations > 0 ? totalRevenue / confirmedRegistrations : 0;

        stats.getChildren().addAll(
                buildStatCard("📅", "Total Events", String.valueOf(totalEvents), "#2F5D52"),
                buildStatCard("👥", "Participants", String.valueOf(totalParticipants), "#3E6F64"),
                buildStatCard("🎟", "Registrations", String.valueOf(totalRegistrations), "#9BC7B5"),
                buildStatCard("✅", "Confirmed", String.valueOf(confirmedRegistrations), "#3E6F64"),
                buildStatCard("💵", "Total Revenue", String.format("$%.2f", totalRevenue), "#2F5D52"),
                buildStatCard("📊", "Avg. Ticket", String.format("$%.2f", avgTicketPrice), "#6B7280")
        );

        return stats;
    }

    private VBox buildStatCard(String icon, String label, String value, String color) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15, 25, 15, 25));
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;" +
                "-fx-border-color: #DDE5E2; -fx-border-radius: 10;");

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 24px;");

        Label valueLbl = new Label(value);
        valueLbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label labelLbl = new Label(label);
        labelLbl.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11px;");

        card.getChildren().addAll(iconLbl, valueLbl, labelLbl);
        return card;
    }

    private VBox buildEventTypeChart() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15;" +
                "-fx-border-color: #DDE5E2; -fx-border-radius: 15;");

        Label title = ComponentFactory.sectionTitle("🏷 Events by Type");

        PieChart pieChart = new PieChart();
        pieChart.setTitle("");
        pieChart.setLegendVisible(true);
        pieChart.setPrefSize(350, 300);

        // Get data from database
        Map<String, Integer> data = getEventCountByType();
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            pieChart.getData().add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
        }

        container.getChildren().addAll(title, pieChart);
        return container;
    }

    private VBox buildRevenueByEventChart() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15;" +
                "-fx-border-color: #DDE5E2; -fx-border-radius: 15;");

        Label title = ComponentFactory.sectionTitle("💰 Revenue by Event");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Event");
        yAxis.setLabel("Revenue ($)");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("");
        barChart.setLegendVisible(false);
        barChart.setPrefSize(350, 300);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue");

        List<Event> events = Event.findAll();
        for (Event event : events) {
            double revenue = EventRegistration.revenueByEvent(event.getId());
            if (revenue > 0) {
                String shortTitle = event.getTitle().length() > 15 ?
                        event.getTitle().substring(0, 15) + "..." : event.getTitle();
                series.getData().add(new XYChart.Data<>(shortTitle, revenue));
            }
        }

        barChart.getData().add(series);
        container.getChildren().addAll(title, barChart);
        return container;
    }

    private VBox buildRegistrationsOverTimeChart() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15;" +
                "-fx-border-color: #DDE5E2; -fx-border-radius: 15;");

        Label title = ComponentFactory.sectionTitle("📈 Registrations Over Time (Last 30 Days)");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        yAxis.setLabel("Registrations");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("");
        lineChart.setPrefSize(750, 300);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Registrations");

        Map<String, Integer> data = getRegistrationsByDate();
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        lineChart.getData().add(series);
        container.getChildren().addAll(title, lineChart);
        return container;
    }

    private VBox buildTicketTypeChart() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15;" +
                "-fx-border-color: #DDE5E2; -fx-border-radius: 15;");

        Label title = ComponentFactory.sectionTitle("🎫 Ticket Types Distribution");

        PieChart pieChart = new PieChart();
        pieChart.setPrefSize(350, 300);

        Map<String, Integer> data = getTicketTypeDistribution();
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            pieChart.getData().add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
        }

        container.getChildren().addAll(title, pieChart);
        return container;
    }

    private VBox buildStatusChart() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15;" +
                "-fx-border-color: #DDE5E2; -fx-border-radius: 15;");

        Label title = ComponentFactory.sectionTitle("📊 Registration Status");

        PieChart pieChart = new PieChart();
        pieChart.setPrefSize(350, 300);

        Map<String, Integer> data = getStatusDistribution();
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            String statusEmoji = switch (entry.getKey()) {
                case "CONFIRMED" -> "✅ ";
                case "PENDING" -> "⏳ ";
                case "CANCELLED" -> "❌ ";
                default -> "";
            };
            pieChart.getData().add(new PieChart.Data(statusEmoji + entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
        }

        container.getChildren().addAll(title, pieChart);
        return container;
    }

    // =================== DATA FETCHING METHODS ===================

    private Map<String, Integer> getEventCountByType() {
        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = "SELECT event_type, COUNT(*) as count FROM events GROUP BY event_type ORDER BY count DESC";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                data.put(rs.getString("event_type"), rs.getInt("count"));
            }
        } catch (Exception e) {
            System.err.println("Error fetching event type stats: " + e.getMessage());
        }
        return data;
    }

    private Map<String, Integer> getRegistrationsByDate() {
        Map<String, Integer> data = new LinkedHashMap<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd");

        // Initialize last 30 days with 0
        for (int i = 29; i >= 0; i--) {
            data.put(LocalDate.now().minusDays(i).format(dtf), 0);
        }

        String sql = "SELECT DATE(registration_date) as reg_date, COUNT(*) as count " +
                "FROM event_registrations " +
                "WHERE registration_date >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
                "GROUP BY DATE(registration_date) ORDER BY reg_date";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                LocalDate date = rs.getDate("reg_date").toLocalDate();
                data.put(date.format(dtf), rs.getInt("count"));
            }
        } catch (Exception e) {
            System.err.println("Error fetching registration stats: " + e.getMessage());
        }
        return data;
    }

    private Map<String, Integer> getTicketTypeDistribution() {
        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = "SELECT ticket_type, COUNT(*) as count FROM event_registrations GROUP BY ticket_type ORDER BY count DESC";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                data.put(rs.getString("ticket_type"), rs.getInt("count"));
            }
        } catch (Exception e) {
            System.err.println("Error fetching ticket type stats: " + e.getMessage());
        }
        return data;
    }

    private Map<String, Integer> getStatusDistribution() {
        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = "SELECT status, COUNT(*) as count FROM event_registrations GROUP BY status ORDER BY count DESC";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                data.put(rs.getString("status"), rs.getInt("count"));
            }
        } catch (Exception e) {
            System.err.println("Error fetching status stats: " + e.getMessage());
        }
        return data;
    }
}