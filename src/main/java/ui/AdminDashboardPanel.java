package ui;

import utils.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDashboardPanel extends JPanel {

    private MentisLoginFrame parentFrame;
    private int psychologistCount;
    private int patientCount;

    public AdminDashboardPanel(MentisLoginFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 248, 246));

        loadStatistics();

        // REMOVED: add(new SidebarPanel(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);
    }

    // ================= MAIN CONTENT =================
    private JPanel createMainContent() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);

        mainPanel.add(createHeader(), BorderLayout.NORTH);
        mainPanel.add(createCenterContent(), BorderLayout.CENTER);

        return mainPanel;
    }

    // ================= HEADER =================
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(new Color(60, 110, 90));

        JTextField search = new JTextField("Search...");
        search.setPreferredSize(new Dimension(220, 35));
        search.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        header.add(title, BorderLayout.WEST);
        header.add(search, BorderLayout.EAST);

        return header;
    }

    // ================= CENTER =================
    private JPanel createCenterContent() {
        JPanel center = new JPanel(new BorderLayout(20, 20));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(20, 30, 30, 30));

        center.add(createStatsPanel(), BorderLayout.NORTH);
        center.add(new ChartPanel(psychologistCount, patientCount), BorderLayout.CENTER);

        return center;
    }

    // ================= STATS =================
    private JPanel createStatsPanel() {
        JPanel stats = new JPanel(new GridLayout(1, 2, 20, 0));
        stats.setOpaque(false);

        stats.add(createStatCard(
                "Psychologists",
                psychologistCount,
                new Color(90, 150, 230),
                e -> parentFrame.showPsychologistTablePanel()
        ));

        stats.add(createStatCard(
                "Patients",
                patientCount,
                new Color(100, 180, 120),
                e -> parentFrame.showPatientTablePanel()
        ));

        return stats;
    }

    private JPanel createStatCard(String title, int value, Color color, java.awt.event.ActionListener action) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel number = new JLabel(String.valueOf(value));
        number.setFont(new Font("Segoe UI", Font.BOLD, 36));
        number.setForeground(color);

        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        label.setForeground(Color.GRAY);

        JButton button = new JButton("View list");
        button.addActionListener(action);
        button.setFocusPainted(false);

        card.add(label, BorderLayout.NORTH);
        card.add(number, BorderLayout.CENTER);
        card.add(button, BorderLayout.SOUTH);

        return card;
    }

    // ================= DATABASE =================
    private void loadStatistics() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            psychologistCount = getCount(conn, "psychologist");
            patientCount = getCount(conn, "Patient");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getCount(Connection conn, String type) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user WHERE type = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // ================= CHART =================
    class ChartPanel extends JPanel {
        int p1, p2;

        ChartPanel(int p1, int p2) {
            this.p1 = p1;
            this.p2 = p2;
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            setPreferredSize(new Dimension(400, 200));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            int max = Math.max(p1, p2);
            if (max == 0) max = 1;

            int base = getHeight() - 50;
            int barWidth = 80;

            // Add labels for clarity
            g2.setColor(Color.BLACK);
            g2.drawString("Psychologists", 150, base + 20);
            g2.drawString("Patients", 300, base + 20);

            g2.setColor(new Color(90, 150, 230));
            g2.fillRect(150, base - (p1 * 200 / max), barWidth, p1 * 200 / max);

            g2.setColor(new Color(100, 180, 120));
            g2.fillRect(300, base - (p2 * 200 / max), barWidth, p2 * 200 / max);

            // Add value labels
            g2.setColor(Color.BLACK);
            g2.drawString(String.valueOf(p1), 150 + barWidth/2 - 10, base - (p1 * 200 / max) - 10);
            g2.drawString(String.valueOf(p2), 300 + barWidth/2 - 10, base - (p2 * 200 / max) - 10);
        }
    }

    // Method to refresh dashboard data
    public void refreshData() {
        loadStatistics();
        repaint();
    }
}