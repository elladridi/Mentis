package com.mentalhealth.app.models;

import com.mentalhealth.app.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventRegistration {

    private int id;
    private int eventId;
    private Integer userId;
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
    private LocalDateTime updatedAt;
    private String qrCodePath;
    private String confirmationNumber;

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
        this.updatedAt = LocalDateTime.now();
    }

    // =================== GETTERS & SETTERS ===================

    public int getId() { return id; }
    public void setId(int id) {
        this.id = id;
        if (this.confirmationNumber == null && id > 0) {
            this.confirmationNumber = String.format("REG-%06d", id);
        }
    }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        this.email = email == null ? null : email.trim().toLowerCase();
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getTicketType() { return ticketType; }
    public void setTicketType(String ticketType) {
        this.ticketType = ticketType == null ? "STANDARD" : ticketType;
    }

    public int getNumberOfTickets() { return numberOfTickets; }
    public void setNumberOfTickets(int n) { this.numberOfTickets = Math.max(1, n); }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = Math.max(0, totalPrice); }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status == null ? "CONFIRMED" : status;
    }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime rd) { this.registrationDate = rd; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getQrCodePath() { return qrCodePath; }
    public void setQrCodePath(String qrCodePath) { this.qrCodePath = qrCodePath; }

    public String getConfirmationNumber() { return confirmationNumber; }
    public void setConfirmationNumber(String confirmationNumber) { this.confirmationNumber = confirmationNumber; }

    // =================== BUSINESS LOGIC ===================

    public boolean isConfirmed() { return "CONFIRMED".equalsIgnoreCase(status); }
    public boolean isCancelled() { return "CANCELLED".equalsIgnoreCase(status); }
    public boolean isPending() { return "PENDING".equalsIgnoreCase(status); }
    public boolean isFreeTicket() { return totalPrice == 0; }

    public String getFormattedConfirmationNumber() {
        if (confirmationNumber != null && !confirmationNumber.trim().isEmpty()) {
            return confirmationNumber;
        }
        return id > 0 ? String.format("REG-%06d", id) : "REG-PENDING";
    }

    public double getTicketMultiplier() {
        if (ticketType == null) return 1.0;

        return switch (ticketType) {
            case "VIP" -> 1.5;
            case "EARLY_BIRD" -> 0.8;
            case "GROUP" -> 0.9;
            default -> 1.0;
        };
    }

    public double calculateTotalPrice(Event event) {
        if (event == null) return 0.0;
        return event.getPrice() * numberOfTickets * getTicketMultiplier();
    }

    public String getTicketTypeLabel() {
        if (ticketType == null) return "Standard";

        return switch (ticketType) {
            case "VIP" -> "VIP (+50%)";
            case "EARLY_BIRD" -> "Early Bird (-20%)";
            case "GROUP" -> "Group (-10%)";
            default -> "Standard";
        };
    }

    public String getStatusEmoji() {
        if (status == null) return "❓";

        return switch (status) {
            case "CONFIRMED" -> "✅";
            case "PENDING" -> "⏳";
            case "CANCELLED" -> "❌";
            default -> "❓";
        };
    }

    @Override
    public String toString() {
        return getFormattedConfirmationNumber() + " - " + userName + " - " + ticketType + " (" + status + ")";
    }

    // =================== CREATE ===================

    public boolean save() {
        if (registrationDate == null) {
            registrationDate = LocalDateTime.now();
        }

        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }

        String sql = "INSERT INTO event_registrations (" +
                "event_id, user_id, user_name, email, phone, " +
                "ticket_type, number_of_tickets, total_price, status, payment_method, " +
                "special_requests, registration_date, updated_at, qr_code_path, confirmation_number" +
                ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, eventId);

            if (userId != null) {
                ps.setInt(2, userId);
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            ps.setString(3, userName);
            ps.setString(4, email);
            ps.setString(5, phone);
            ps.setString(6, ticketType == null ? "STANDARD" : ticketType);
            ps.setInt(7, numberOfTickets);
            ps.setDouble(8, totalPrice);
            ps.setString(9, status == null ? "CONFIRMED" : status);
            ps.setString(10, paymentMethod);
            ps.setString(11, specialRequests);
            ps.setTimestamp(12, Timestamp.valueOf(registrationDate));
            ps.setTimestamp(13, Timestamp.valueOf(updatedAt));
            ps.setString(14, qrCodePath);
            ps.setString(15, confirmationNumber);

            int rows = ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                this.id = keys.getInt(1);

                if (this.confirmationNumber == null || this.confirmationNumber.trim().isEmpty()) {
                    this.confirmationNumber = String.format("REG-%06d", this.id);
                    updateConfirmationNumberOnly();
                }
            }

            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Error saving registration: " + e.getMessage());
            return false;
        }
    }

    private void updateConfirmationNumberOnly() {
        String sql = "UPDATE event_registrations SET confirmation_number=?, updated_at=? WHERE id=?";

        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, confirmationNumber);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating confirmation number: " + e.getMessage());
        }
    }

    // =================== READ ===================

    public static List<EventRegistration> findByEvent(int eventId) {
        List<EventRegistration> list = new ArrayList<>();

        String sql = "SELECT * FROM event_registrations WHERE event_id=? ORDER BY registration_date DESC";

        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, eventId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(fromResultSet(rs));
            }

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
            if (rs.next()) {
                return fromResultSet(rs);
            }

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
            System.err.println("Error counting registrations: " + e.getMessage());
        }

        return 0;
    }

    public static int totalCount() {
        String sql = "SELECT COUNT(*) FROM event_registrations";

        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("Error total registration count: " + e.getMessage());
        }

        return 0;
    }

    public static int confirmedCount() {
        String sql = "SELECT COUNT(*) FROM event_registrations WHERE status='CONFIRMED'";

        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("Error confirmed registration count: " + e.getMessage());
        }

        return 0;
    }

    public static double totalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_price),0) FROM event_registrations WHERE status='CONFIRMED'";

        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {

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
        updatedAt = LocalDateTime.now();

        String sql = "UPDATE event_registrations SET " +
                "user_id=?, user_name=?, email=?, phone=?, " +
                "ticket_type=?, number_of_tickets=?, total_price=?, " +
                "status=?, payment_method=?, special_requests=?, " +
                "updated_at=?, qr_code_path=?, confirmation_number=? " +
                "WHERE id=?";

        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {

            if (userId != null) {
                ps.setInt(1, userId);
            } else {
                ps.setNull(1, Types.INTEGER);
            }

            ps.setString(2, userName);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.setString(5, ticketType == null ? "STANDARD" : ticketType);
            ps.setInt(6, numberOfTickets);
            ps.setDouble(7, totalPrice);
            ps.setString(8, status == null ? "CONFIRMED" : status);
            ps.setString(9, paymentMethod);
            ps.setString(10, specialRequests);
            ps.setTimestamp(11, Timestamp.valueOf(updatedAt));
            ps.setString(12, qrCodePath);
            ps.setString(13, confirmationNumber);
            ps.setInt(14, id);

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

        reg.id = rs.getInt("id");
        reg.eventId = rs.getInt("event_id");

        int uid = rs.getInt("user_id");
        if (!rs.wasNull()) {
            reg.userId = uid;
        }

        reg.userName = rs.getString("user_name");
        reg.email = rs.getString("email");
        reg.phone = rs.getString("phone");
        reg.ticketType = rs.getString("ticket_type");
        reg.numberOfTickets = rs.getInt("number_of_tickets");
        reg.totalPrice = rs.getDouble("total_price");
        reg.status = rs.getString("status");
        reg.paymentMethod = rs.getString("payment_method");
        reg.specialRequests = rs.getString("special_requests");

        Timestamp registrationTs = rs.getTimestamp("registration_date");
        if (registrationTs != null) {
            reg.registrationDate = registrationTs.toLocalDateTime();
        }

        Timestamp updatedTs = rs.getTimestamp("updated_at");
        if (updatedTs != null) {
            reg.updatedAt = updatedTs.toLocalDateTime();
        }

        reg.qrCodePath = rs.getString("qr_code_path");
        reg.confirmationNumber = rs.getString("confirmation_number");

        if ((reg.confirmationNumber == null || reg.confirmationNumber.trim().isEmpty()) && reg.id > 0) {
            reg.confirmationNumber = String.format("REG-%06d", reg.id);
        }

        return reg;
    }
}