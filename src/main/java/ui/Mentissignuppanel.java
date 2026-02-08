package ui;

import models.user;
import services.userservice;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;

public class Mentissignuppanel extends JPanel {

    private static final Color BG_COLOR = new Color(216, 228, 222);
    private static final Color PRIMARY = new Color(88, 139, 113);

    private RoundedTextField firstNameField;
    private RoundedTextField lastNameField;
    private RoundedTextField phoneField;
    private RoundedTextField dobField;
    private RoundedTextField emailField;
    private RoundedPasswordField passwordField;
    private JComboBox<String> typeComboBox;
    private RoundedButton signUpButton;

    private final MentisLoginFrame parentFrame;

    public Mentissignuppanel(MentisLoginFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(null);
        setBackground(BG_COLOR);
        initComponents();
    }

    private void initComponents() {

        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 40));
        title.setForeground(PRIMARY);
        title.setBounds(0, 20, 924, 50);
        add(title);

        loadLogo();

        int y = 200;

        firstNameField = createField("First Name", 200, y);
        lastNameField  = createField("Last Name", 474, y);

        y += 50;
        phoneField = createField("Phone", 200, y);
        dobField   = createField("YYYY-MM-DD", 474, y);

        y += 50;
        typeComboBox = new JComboBox<>(new String[]{
                "Select Type", "Patient", "Psychologist", "Admin"
        });
        typeComboBox.setBounds(200, y, 250, 40);
        add(typeComboBox);

        emailField = createField("Email", 474, y);

        y += 50;
        passwordField = new RoundedPasswordField(20);
        passwordField.setBounds(337, y, 250, 40);
        addPasswordPlaceholder(passwordField);
        add(passwordField);

        y += 60;
        signUpButton = new RoundedButton("Sign Up");
        signUpButton.setBounds(362, y, 200, 45);
        signUpButton.addActionListener(e -> handleSignup());
        add(signUpButton);

        JLabel back = new JLabel("Already have an account? Login", SwingConstants.CENTER);
        back.setForeground(PRIMARY);
        back.setBounds(0, 560, 924, 20);
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                parentFrame.showLoginPanel();
            }
        });
        add(back);
    }

    private void loadLogo() {
        try {
            URL url = getClass().getResource("/resources/logo.png");
            if (url != null) {
                Image img = new ImageIcon(url).getImage()
                        .getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                JLabel logo = new JLabel(new ImageIcon(img));
                logo.setBounds(402, 70, 120, 120);
                add(logo);
            }
        } catch (Exception ignored) {}
    }

    private RoundedTextField createField(String placeholder, int x, int y) {
        RoundedTextField field = new RoundedTextField(20);
        field.setText(placeholder);
        field.setForeground(Color.GRAY);
        field.setBounds(x, y, 250, 40);
        addPlaceholder(field, placeholder);
        add(field);
        return field;
    }

    private void addPlaceholder(JTextField field, String text) {
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (field.getText().equals(text)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(text);
                    field.setForeground(Color.GRAY);
                }
            }
        });
    }

    private void addPasswordPlaceholder(JPasswordField field) {
        field.setEchoChar((char) 0);
        field.setText("Password");
        field.setForeground(Color.GRAY);

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (String.valueOf(field.getPassword()).equals("Password")) {
                    field.setText("");
                    field.setEchoChar('•');
                    field.setForeground(Color.BLACK);
                }
            }

            public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getPassword().length == 0) {
                    field.setEchoChar((char) 0);
                    field.setText("Password");
                    field.setForeground(Color.GRAY);
                }
            }
        });
    }

    private void handleSignup() {

        signUpButton.setEnabled(false);

        String fn = value(firstNameField, "First Name");
        String ln = value(lastNameField, "Last Name");
        String phone = value(phoneField, "Phone");
        String dob = value(dobField, "YYYY-MM-DD");
        String email = value(emailField, "Email");
        String password = getPassword();
        String type = typeComboBox.getSelectedIndex() == 0 ? "" :
                typeComboBox.getSelectedItem().toString();

        if (fn.isEmpty() || ln.isEmpty() || phone.isEmpty() ||
                dob.isEmpty() || email.isEmpty() || password.isEmpty() || type.isEmpty()) {
            error("All fields are required");
            return;
        }

        if (!userservice.isValidEmail(email)) {
            error("Invalid email format");
            return;
        }

        if (userservice.emailExists(email)) {
            error("Email already exists");
            return;
        }

        user u = new user(fn, ln, phone, dob, type, email, password);

        if (userservice.registeruser(u)) {
            JOptionPane.showMessageDialog(this, "Account created successfully!");
            parentFrame.showLoginPanel();
        } else {
            error("Registration failed");
        }
    }

    private String value(JTextField field, String placeholder) {
        return field.getText().equals(placeholder) ? "" : field.getText().trim();
    }

    private String getPassword() {
        String pass = String.valueOf(passwordField.getPassword());
        return pass.equals("Password") ? "" : pass;
    }

    private void error(String msg) {
        signUpButton.setEnabled(true);
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(924, 600);
    }

    /* ---------------- CUSTOM UI COMPONENTS ---------------- */

    class RoundedButton extends JButton {
        RoundedButton(String text) {
            super(text);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setForeground(Color.WHITE);
            setFont(new Font("Arial", Font.BOLD, 16));
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(PRIMARY);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
            super.paintComponent(g);
        }
    }

    class RoundedTextField extends JTextField {
        RoundedTextField(int size) {
            super(size);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
            super.paintComponent(g);
        }
    }

    class RoundedPasswordField extends JPasswordField {
        RoundedPasswordField(int size) {
            super(size);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        }
    }
}
