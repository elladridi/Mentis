package ui;

import javax.swing.*;
import java.awt.*;
import models.user;
import services.userservice;

public class AddPsychologistDialog extends JDialog {

    protected JTextField firstNameField, lastNameField, dobField, emailField, phoneField;
    protected JButton addButton, cancelButton;
    protected PsychologistTablePanel parentPanel;

    public AddPsychologistDialog(JFrame parent, PsychologistTablePanel parentPanel) {
        super(parent, "Add Psychologist", true);
        this.parentPanel = parentPanel;

        setSize(600, 520);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 248, 246));

        add(createHeader(), BorderLayout.NORTH);
        add(createForm(), BorderLayout.CENTER);
        add(createButtons(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel();
        panel.setBackground(getContentPane().getBackground());

        JLabel title = new JLabel("Add Psychologist");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(new Color(88, 139, 113));
        panel.add(title);

        return panel;
    }

    private JPanel createForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(getContentPane().getBackground());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        firstNameField = createField();
        lastNameField = createField();
        dobField = createField("YYYY-MM-DD");
        emailField = createField();
        phoneField = createField();

        addRow(panel, gbc, 0, "First Name", firstNameField);
        addRow(panel, gbc, 1, "Last Name", lastNameField);
        addRow(panel, gbc, 2, "Date of Birth", dobField);
        addRow(panel, gbc, 3, "Email", emailField);
        addRow(panel, gbc, 4, "Phone", phoneField);

        return panel;
    }

    private JPanel createButtons() {
        JPanel panel = new JPanel();
        panel.setBackground(getContentPane().getBackground());

        addButton = createPrimaryButton("Add");
        cancelButton = createDangerButton("Cancel");

        addButton.addActionListener(e -> handleAdd());
        cancelButton.addActionListener(e -> dispose());

        panel.add(addButton);
        panel.add(cancelButton);

        return panel;
    }

    private void handleAdd() {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String dob = dobField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();

        if (firstName.isEmpty() || lastName.isEmpty() || dob.isEmpty()
                || email.isEmpty() || phone.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        if (!userservice.isValidEmail(email)) {
            showError("Invalid email format.");
            return;
        }

        if (!userservice.isValidDate(dob)) {
            showError("Date must be YYYY-MM-DD.");
            return;
        }

        if (userservice.emailExists(email)) {
            showError("Email already exists.");
            return;
        }

        user psych = new user(firstName, lastName, phone, dob,
                "psychologist", email, "doctor123");

        if (userservice.registeruser(psych)) {
            JOptionPane.showMessageDialog(this, "Psychologist added successfully!");
            parentPanel.refreshTable();
            dispose();
        } else {
            showError("Failed to add psychologist.");
        }
    }

    // ---------- Helpers ----------

    private void addRow(JPanel panel, GridBagConstraints gbc, int y, String label, JTextField field) {
        gbc.gridx = 0; gbc.gridy = y;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private JTextField createField() {
        return createField("");
    }

    private JTextField createField(String placeholder) {
        JTextField field = new JTextField(18);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        return field;
    }

    private JButton createPrimaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(88, 139, 113));
        b.setForeground(Color.BLACK);
        b.setFocusPainted(false);
        return b;
    }

    private JButton createDangerButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(200, 80, 80));
        b.setForeground(Color.BLACK);
        b.setFocusPainted(false);
        return b;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
