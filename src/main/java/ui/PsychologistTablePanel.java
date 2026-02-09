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

public class PsychologistTablePanel extends JPanel {

    private MentisLoginFrame parentFrame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton backButton;

    public PsychologistTablePanel(MentisLoginFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout()); // Changed from setLayout(null)
        setBackground(new Color(240, 245, 242));

        initComponents();
        loadPsychologistsFromDatabase();
    }

    private void initComponents() {
        // REMOVED: SidebarPanel sidebar = new SidebarPanel();
        // sidebar.setBounds(0, 0, 210, 600);
        // add(sidebar);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(240, 245, 242));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));

        JLabel headerLabel = new JLabel("Mentis - Psychologists");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setForeground(new Color(88, 139, 113));
        headerPanel.add(headerLabel, BorderLayout.WEST);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(new Color(240, 245, 242));

        // Back button
        backButton = new JButton("← Back to Dashboard");
        backButton.setFont(new Font("Arial", Font.PLAIN, 14));
        backButton.setForeground(new Color(88, 139, 113));
        backButton.setBackground(Color.WHITE);
        backButton.setBorder(BorderFactory.createLineBorder(new Color(88, 139, 113), 2));
        backButton.setFocusPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> parentFrame.showAdminDashboard());

        // Add psychologist button
        addButton = new JButton("Add psychologist");
        addButton.setFont(new Font("Arial", Font.BOLD, 16));
        addButton.setForeground(Color.BLACK);
        addButton.setBackground(new Color(88, 139, 113));
        addButton.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        addButton.setFocusPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.addActionListener(e -> showAddPsychologistDialog());

        buttonPanel.add(backButton);
        buttonPanel.add(addButton);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Table column names
        String[] columnNames = {"CIN", "firstname", "lastname", "phone", "date of birth", "email", "Action"};

        // Create table model
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only Action column is editable (for buttons)
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
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(88, 139, 113), 2));
        scrollPane.setPreferredSize(new Dimension(800, 400));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadPsychologistsFromDatabase() {
        // Clear existing rows
        tableModel.setRowCount(0);

        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM user WHERE type = 'psychologist'";
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
                row.add("Actions"); // Placeholder for action buttons

                tableModel.addRow(row);
            }

            conn.close();
            System.out.println("Loaded " + tableModel.getRowCount() + " psychologists");

        } catch (SQLException e) {
            System.err.println("Error loading psychologists: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading psychologists: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddPsychologistDialog() {
        parentFrame.showAddPsychologistDialog(this);
    }

    private void editPsychologist(int row) {
        int id = (int) tableModel.getValueAt(row, 0);
        String firstname = (String) tableModel.getValueAt(row, 1);
        String lastname = (String) tableModel.getValueAt(row, 2);
        String phone = (String) tableModel.getValueAt(row, 3);
        String dob = (String) tableModel.getValueAt(row, 4);
        String email = (String) tableModel.getValueAt(row, 5);

        parentFrame.showUpdatePsychologistDialog(this, id, firstname, lastname, phone, dob, email);
    }

    private void deletePsychologist(int row) {
        int id = (int) tableModel.getValueAt(row, 0);
        String name = tableModel.getValueAt(row, 1) + " " + tableModel.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete psychologist: " + name + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = userservice.deleteuser(id);

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Psychologist deleted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadPsychologistsFromDatabase(); // Refresh table
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to delete psychologist!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refreshTable() {
        loadPsychologistsFromDatabase();
    }

    // Button Renderer for Action column
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

    // Button Editor for Action column
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
            editButton.addActionListener(e -> editPsychologist(currentRow));

            deleteButton = new JButton("🗑");
            deleteButton.setPreferredSize(new Dimension(35, 30));
            deleteButton.setFocusPainted(false);
            deleteButton.addActionListener(e -> deletePsychologist(currentRow));

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

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(924, 600);
    }
}