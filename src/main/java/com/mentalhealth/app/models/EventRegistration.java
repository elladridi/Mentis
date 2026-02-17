package com.mentalhealth.app.models;

import com.mentalhealth.app.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventRegistration {

    private int id;
    private int eventId;
    private String userName;
    private String email;
    private String phone;
    private String ticketType;
    private int numberOfTickets;
    private double totalPrice;
    private String status;
    private String paymentMethod;
    private String specialRequests;
    private LocalDateTime registrationDate;

    public EventRegistration() {}

    public EventRegistration(int eventId, String userName, String email,
                             String phone, String ticketType, int numberOfTickets,
                             double totalPrice, String paymentMethod) {
        this.eventId = eventId;
        this.userName = userName;
        this.email = email;
        this.phone = phone;
        this.ticketType = ticketType;
        this.numberOfTickets = numberOfTickets;
        this.totalPrice = totalPrice;
        this.status = "CONFIRMED";
        this.paymentMethod = paymentMethod;
        this.registrationDate = LocalDateTime.now();
    }

    // =================== GETTERS & SETTERS ===================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getTicketType() { return ticketType; }
    public void setTicketType(String ticketType) { this.ticketType = ticketType; }

    public int getNumberOfTickets() { return numberOfTickets; }
    public void setNumberOfTickets(int n) { this.numberOfTickets = n; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime rd) { this.registrationDate = rd; }

    // =================== BUSINESS LOGIC ===================

    public boolean isConfirmed() { return "CONFIRMED".equals(status); }
    public boolean isCancelled() { return "CANCELLED".equals(status); }
    public boolean isPending() { return "PENDING".equals(status); }
    public boolean isFreeTicket() { return totalPrice == 0; }

    public String getStatusEmoji() {
        return switch (status) {
            case "CONFIRMED" -> "✅";
            case "PENDING" -> "⏳";
            case "CANCELLED" -> "❌";
            default -> "❓";
        };
    }

    @Override
    public String toString() {
        return userName + " - " + ticketType + " (" + status + ")";
    }

    // =================== CREATE ===================

    public boolean save() {
        String sql = "INSERT INTO event_registrations (event_id, user_name, email, phone, " +
                "ticket_type, number_of_tickets, total_price, status, payment_method, " +
                "special_requests, registration_date) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, eventId);
            ps.setString(2, userName);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.setString(5, ticketType);
            ps.setInt(6, numberOfTickets);
            ps.setDouble(7, totalPrice);
            ps.setString(8, status);
            ps.setString(9, paymentMethod);
            ps.setString(10, specialRequests);
            ps.setTimestamp(11, Timestamp.valueOf(registrationDate));
            int rows = ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) this.id = keys.getInt(1);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error saving registration: " + e.getMessage());
            return false;
        }
    }

    // =================== READ ===================

    public static List<EventRegistration> findByEvent(int eventId) {
        List<EventRegistration> list = new ArrayList<>();
        String sql = "SELECT * FROM event_registrations WHERE event_id=? ORDER BY registration_date DESC";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(fromResultSet(rs));
        } catch (SQLException e) {
            System.err.println("Error fetching registrations: " + e.getMessage());
        }
        return list;
    }

    public static EventRegistration findById(int id) {
        String sql = "SELECT * FROM event_registrations WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return fromResultSet(rs);
        } catch (SQLException e) {
            System.err.println("Error finding registration: " + e.getMessage());
        }
        return null;
    }

    public static int countByEvent(int eventId) {
        String sql = "SELECT COUNT(*) FROM event_registrations WHERE event_id=? AND status!='CANCELLED'";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting: " + e.getMessage());
        }
        return 0;
    }

    public static int totalCount() {
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM event_registrations")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error total count: " + e.getMessage());
        }
        return 0;
    }

    public static int confirmedCount() {
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT COUNT(*) FROM event_registrations WHERE status='CONFIRMED'")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error confirmed count: " + e.getMessage());
        }
        return 0;
    }

    public static double totalRevenue() {
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT COALESCE(SUM(total_price),0) FROM event_registrations WHERE status='CONFIRMED'")) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error total revenue: " + e.getMessage());
        }
        return 0.0;
    }

    public static double revenueByEvent(int eventId) {
        String sql = "SELECT COALESCE(SUM(total_price),0) FROM event_registrations " +
                "WHERE event_id=? AND status='CONFIRMED'";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error event revenue: " + e.getMessage());
        }
        return 0.0;
    }

    public static int ticketsByEvent(int eventId) {
        String sql = "SELECT COALESCE(SUM(number_of_tickets),0) FROM event_registrations " +
                "WHERE event_id=? AND status='CONFIRMED'";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error tickets count: " + e.getMessage());
        }
        return 0;
    }

    // =================== UPDATE ===================

    public boolean update() {
        String sql = "UPDATE event_registrations SET user_name=?, email=?, phone=?, " +
                "ticket_type=?, number_of_tickets=?, total_price=?, status=?, " +
                "payment_method=?, special_requests=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, userName);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, ticketType);
            ps.setInt(5, numberOfTickets);
            ps.setDouble(6, totalPrice);
            ps.setString(7, status);
            ps.setString(8, paymentMethod);
            ps.setString(9, specialRequests);
            ps.setInt(10, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating registration: " + e.getMessage());
            return false;
        }
    }

    // =================== DELETE ===================

    public boolean delete() {
        String sql = "DELETE FROM event_registrations WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting registration: " + e.getMessage());
            return false;
        }
    }

    // =================== MAPPER ===================

    private static EventRegistration fromResultSet(ResultSet rs) throws SQLException {
        EventRegistration reg = new EventRegistration();
        reg.setId(rs.getInt("id"));
        reg.setEventId(rs.getInt("event_id"));
        reg.setUserName(rs.getString("user_name"));
        reg.setEmail(rs.getString("email"));
        reg.setPhone(rs.getString("phone"));
        reg.setTicketType(rs.getString("ticket_type"));
        reg.setNumberOfTickets(rs.getInt("number_of_tickets"));
        reg.setTotalPrice(rs.getDouble("total_price"));
        reg.setStatus(rs.getString("status"));
        reg.setPaymentMethod(rs.getString("payment_method"));
        reg.setSpecialRequests(rs.getString("special_requests"));
        Timestamp ts = rs.getTimestamp("registration_date");
        if (ts != null) reg.setRegistrationDate(ts.toLocalDateTime());
        return reg;
    }
}