package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.util.Vector;
import utils.DatabaseConnection;
import models.user;
import services.userservice;

public class PatientTablePanel extends JPanel {

    private MentisLoginFrame parentFrame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton backButton;

    public PatientTablePanel(MentisLoginFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(null);
        setBackground(new Color(240, 245, 242));

        initComponents();
        loadPatientsFromDatabase();
    }

    private void initComponents() {
        // Sidebar
        SidebarPanel sidebar = new SidebarPanel();
        sidebar.setBounds(0, 0, 210, 600);
        add(sidebar);

        // Header
        JLabel headerLabel = new JLabel("Mentis - Patients");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setForeground(new Color(88, 139, 113));
        headerLabel.setBounds(250, 30, 400, 40);
        add(headerLabel);

        // Table column names
        String[] columnNames = {"CIN", "firstname", "lastname", "phone", "date of birth", "email", "Action"};

        // Create table model
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };

        // Create table
        table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(40);
        table.setGridColor(new Color(88, 139, 113));
        table.setSelectionBackground(new Color(200, 225, 210));

        // Style table header
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setForeground(new Color(88, 139, 113));
        header.setBackground(Color.WHITE);

        // Add action buttons to table
        table.getColumn("Action").setCellRenderer(new ButtonRenderer());
        table.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));

        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(235, 90, 670, 450);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(88, 139, 113), 2));
        add(scrollPane);

        // Back button
        backButton = new JButton("← Back to Dashboard");
        backButton.setFont(new Font("Arial", Font.PLAIN, 14));
        backButton.setForeground(new Color(88, 139, 113));
        backButton.setBackground(Color.WHITE);
        backButton.setBorder(BorderFactory.createLineBorder(new Color(88, 139, 113), 2));
        backButton.setFocusPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.setBounds(235, 550, 160, 35);
        backButton.addActionListener(e -> parentFrame.showAdminDashboard());
        add(backButton);
    }

    private void loadPatientsFromDatabase() {
        tableModel.setRowCount(0);

        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM user WHERE type = 'Patient'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("firstname"));
                row.add(rs.getString("lastname"));
                row.add(rs.getString("phone"));
                row.add(rs.getString("dateofbirth"));
                row.add(rs.getString("email"));
                row.add("Actions");

                tableModel.addRow(row);
            }

            conn.close();
            System.out.println("Loaded " + tableModel.getRowCount() + " patients");

        } catch (SQLException e) {
            System.err.println("Error loading patients: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading patients: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editPatient(int row) {
        int id = (int) tableModel.getValueAt(row, 0);
        String firstname = (String) tableModel.getValueAt(row, 1);
        String lastname = (String) tableModel.getValueAt(row, 2);
        String phone = (String) tableModel.getValueAt(row, 3);
        String dob = (String) tableModel.getValueAt(row, 4);
        String email = (String) tableModel.getValueAt(row, 5);

        parentFrame.showUpdatePatientDialog(this, id, firstname, lastname, phone, dob, email);
    }

    private void deletePatient(int row) {
        int id = (int) tableModel.getValueAt(row, 0);
        String name = tableModel.getValueAt(row, 1) + " " + tableModel.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete patient: " + name + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = userservice.deleteuser(id);

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Patient deleted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadPatientsFromDatabase();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to delete patient!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refreshTable() {
        loadPatientsFromDatabase();
    }

    // Button Renderer
    class ButtonRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private JButton editButton;
        private JButton deleteButton;

        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
            setOpaque(true);

            editButton = new JButton("✏");
            editButton.setPreferredSize(new Dimension(35, 30));
            editButton.setFocusPainted(false);

            deleteButton = new JButton("🗑");
            deleteButton.setPreferredSize(new Dimension(35, 30));
            deleteButton.setFocusPainted(false);

            add(editButton);
            add(deleteButton);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    // Button Editor
    class ButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private JButton editButton;
        private JButton deleteButton;
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);

            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

            editButton = new JButton("✏");
            editButton.setPreferredSize(new Dimension(35, 30));
            editButton.setFocusPainted(false);
            editButton.addActionListener(e -> editPatient(currentRow));

            deleteButton = new JButton("🗑");
            deleteButton.setPreferredSize(new Dimension(35, 30));
            deleteButton.setFocusPainted(false);
            deleteButton.addActionListener(e -> deletePatient(currentRow));

            panel.add(editButton);
            panel.add(deleteButton);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentRow = row;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "Actions";
        }
    }

    // Sidebar
    class SidebarPanel extends JPanel {
        public SidebarPanel() {
            setLayout(null);
            setBackground(new Color(230, 240, 235));

            JLabel logoLabel = new JLabel("🧠 Mentis");
            logoLabel.setFont(new Font("Arial", Font.BOLD, 24));
            logoLabel.setForeground(new Color(88, 139, 113));
            logoLabel.setBounds(20, 20, 180, 40);
            add(logoLabel);

            int yPos = 100;
            String[] menuItems = {"Dashboard", "session booking", "Assessment",
                    "Mood tracking", "Content", "Events"};

            for (String item : menuItems) {
                JLabel menuLabel = createMenuItem(item, yPos);
                add(menuLabel);
                yPos += 50;
            }

            JLabel settingsLabel = createMenuItem("Settings", 500);
            add(settingsLabel);

            setBorder(BorderFactory.createMatteBorder(0, 0, 0, 3, new Color(88, 139, 113)));
        }

        private JLabel createMenuItem(String text, int y) {
            JLabel label = new JLabel(text);
            label.setFont(new Font("Arial", Font.PLAIN, 16));
            label.setForeground(Color.BLACK);
            label.setBounds(20, y, 180, 35);
            label.setCursor(new Cursor(Cursor.HAND_CURSOR));
            label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            return label;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(924, 600);
    }
}