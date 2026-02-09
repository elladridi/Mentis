package ui;

import models.user;
import services.userservice;

import javax.swing.*;
import java.awt.*;
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
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        initComponents();
    }

    private void initComponents() {
        // Main container
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(BG_COLOR);

        // Header panel
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        headerPanel.setBackground(BG_COLOR);

        JLabel back = new JLabel("← Back");
        back.setFont(new Font("Arial", Font.PLAIN, 16));
        back.setForeground(PRIMARY);
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                parentFrame.showWelcomePanel();
            }
        });
        headerPanel.add(back);

        mainContainer.add(headerPanel, BorderLayout.NORTH);

        // Center content panel
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(BG_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 20, 0);

        // Title
        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 48)); // Bigger font
        title.setForeground(PRIMARY);
        centerPanel.add(title, gbc);

        // Logo
        gbc.gridy++;
        gbc.insets = new Insets(20, 0, 30, 0);
        JPanel logoPanel = loadLogo();
        if (logoPanel != null) {
            centerPanel.add(logoPanel, gbc);
        }

        // Form panel
        gbc.gridy++;
        JPanel formPanel = createFormPanel();
        centerPanel.add(formPanel, gbc);

        // Login link at bottom
        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.insets = new Insets(30, 0, 30, 0);
        JLabel loginLink = new JLabel("Already have an account? Login", SwingConstants.CENTER);
        loginLink.setFont(new Font("Arial", Font.PLAIN, 16)); // Bigger font
        loginLink.setForeground(PRIMARY);
        loginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                parentFrame.showLoginPanel();
            }
        });
        centerPanel.add(loginLink, gbc);

        mainContainer.add(centerPanel, BorderLayout.CENTER);
        add(mainContainer, BorderLayout.CENTER);
    }

    private JPanel loadLogo() {
        try {
            URL url = getClass().getResource("/resources/logo.png");
            if (url != null) {
                Image img = new ImageIcon(url).getImage()
                        .getScaledInstance(140, 140, Image.SCALE_SMOOTH); // Bigger logo
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                panel.setBackground(BG_COLOR);
                panel.add(new JLabel(new ImageIcon(img)));
                return panel;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BG_COLOR);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 20, 10, 20);

        // Row 1: First Name & Last Name
        firstNameField = createField("First Name");
        firstNameField.setPreferredSize(new Dimension(300, 50)); // Bigger field
        firstNameField.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 0;
        formPanel.add(firstNameField, gbc);

        lastNameField = createField("Last Name");
        lastNameField.setPreferredSize(new Dimension(300, 50)); // Bigger field
        lastNameField.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 1;
        formPanel.add(lastNameField, gbc);

        // Row 2: Phone & Date of Birth
        gbc.gridy++;
        gbc.gridx = 0;
        phoneField = createField("Phone");
        phoneField.setPreferredSize(new Dimension(300, 50));
        phoneField.setFont(new Font("Arial", Font.PLAIN, 16));
        formPanel.add(phoneField, gbc);

        gbc.gridx = 1;
        dobField = createField("YYYY-MM-DD");
        dobField.setPreferredSize(new Dimension(300, 50));
        dobField.setFont(new Font("Arial", Font.PLAIN, 16));
        formPanel.add(dobField, gbc);

        // Row 3: User Type & Email
        gbc.gridy++;
        gbc.gridx = 0;

        typeComboBox = new JComboBox<>(new String[]{
                "Select Type", "Patient", "Psychologist", "Admin"
        });
        typeComboBox.setFont(new Font("Arial", Font.PLAIN, 16)); // Bigger font
        typeComboBox.setPreferredSize(new Dimension(300, 50)); // Bigger combobox
        typeComboBox.setBackground(Color.WHITE);
        formPanel.add(typeComboBox, gbc);

        gbc.gridx = 1;
        emailField = createField("Email");
        emailField.setPreferredSize(new Dimension(300, 50));
        emailField.setFont(new Font("Arial", Font.PLAIN, 16));
        formPanel.add(emailField, gbc);

        // Row 4: Password
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        passwordField = new RoundedPasswordField();
        passwordField.setPreferredSize(new Dimension(620, 50)); // Wider field
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        addPasswordPlaceholder(passwordField);
        formPanel.add(passwordField, gbc);

        // Row 5: Sign Up Button
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 20, 10, 20);

        signUpButton = new RoundedButton("Sign Up");
        signUpButton.setFont(new Font("Arial", Font.BOLD, 18)); // Bigger font
        signUpButton.setPreferredSize(new Dimension(300, 60)); // Bigger button
        signUpButton.addActionListener(e -> handleSignup());
        formPanel.add(signUpButton, gbc);

        return formPanel;
    }

    private RoundedTextField createField(String placeholder) {
        RoundedTextField field = new RoundedTextField();
        field.setText(placeholder);
        field.setForeground(Color.GRAY);
        addPlaceholder(field, placeholder);
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
        return new Dimension(1400, 800); // Match frame size
    }

    /* ---------------- CUSTOM UI COMPONENTS ---------------- */
    class RoundedButton extends JButton {
        RoundedButton(String text) {
            super(text);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setForeground(Color.WHITE);
            setFont(new Font("Arial", Font.BOLD, 18));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(PRIMARY);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            super.paintComponent(g);
        }
    }

    class RoundedTextField extends JTextField {
        RoundedTextField() {
            super();
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
            super.paintComponent(g);
        }
    }

    class RoundedPasswordField extends JPasswordField {
        RoundedPasswordField() {
            super();
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(100, 50, 100, 50));
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
            super.paintComponent(g);
        }
    }
}