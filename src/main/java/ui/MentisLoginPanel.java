package ui;

import models.user;
import services.userservice;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;

public class MentisLoginPanel extends JPanel {

    private RoundedTextField emailField;
    private RoundedPasswordField passwordField;

    private RoundedButton loginButton;
    private JLabel backLabel;
    private MentisLoginFrame parentFrame;

    public MentisLoginPanel(MentisLoginFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(null);
        setBackground(new Color(216, 228, 222));
        initComponents();
    }

    private void initComponents() {

        // ===== Login Card =====
        JPanel card = new JPanel(null) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            }
        };
        card.setBounds(250, 80, 320, 420);
        card.setOpaque(false);
        add(card);

        // ===== Logo =====
        try {
            URL logoURL = getClass().getResource("/resources/logo.png");
            if (logoURL != null) {
                Image img = new ImageIcon(logoURL).getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);
                JLabel logo = new JLabel(new ImageIcon(img));
                logo.setBounds(115, 20, 90, 90);
                card.add(logo);
            }
        } catch (Exception ignored) {}

        // ===== Title =====
        JLabel title = new JLabel("Welcome Back");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(88, 139, 113));
        title.setBounds(0, 120, 320, 30);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(title);

        // ===== Email =====
        emailField = new RoundedTextField("Email");
        emailField.setBounds(35, 170, 250, 45);
        card.add(emailField);

        // ===== Password =====
        passwordField = new RoundedPasswordField("Password");
        passwordField.setBounds(35, 230, 250, 45);
        card.add(passwordField);

        // ENTER key triggers login
        passwordField.addActionListener(e -> handleLogin());

        // ===== Login Button =====
        loginButton = new RoundedButton("Login");
        loginButton.setBounds(35, 295, 250, 45);
        loginButton.addActionListener(e -> handleLogin());
        card.add(loginButton);

        // ===== Back link =====
        backLabel = new JLabel("← Back to Welcome");
        backLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        backLabel.setForeground(new Color(88, 139, 113));
        backLabel.setBounds(20, 20, 150, 20);
        backLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                parentFrame.showWelcomePanel();
            }
        });
        add(backLabel);
    }

    // ================= LOGIN LOGIC =================

    private void handleLogin() {
        String email = emailField.getTextValue();
        String password = passwordField.getPasswordValue();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter email and password.");
            return;
        }

        setLoading(true);

        SwingUtilities.invokeLater(() -> {
            user loggedUser = userservice.loginuser(email, password);

            setLoading(false);

            if (loggedUser != null) {
                JOptionPane.showMessageDialog(this,
                        "Welcome " + loggedUser.getFirstName(),
                        "Login Successful",
                        JOptionPane.INFORMATION_MESSAGE);

                switch (loggedUser.getType()) {
                    case "Admin" -> parentFrame.showAdminDashboard();
                    case "Doctor" -> System.out.println("Doctor dashboard");
                    case "Patient" -> System.out.println("Patient dashboard");
                }

            } else {
                showError("Invalid email or password.");
            }
        });
    }

    private void setLoading(boolean loading) {
        loginButton.setEnabled(!loading);
        setCursor(loading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
                : Cursor.getDefaultCursor());
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Login Error", JOptionPane.ERROR_MESSAGE);
    }

    // ================= CUSTOM COMPONENTS =================

    class RoundedButton extends JButton {
        RoundedButton(String text) {
            super(text);
            setFont(new Font("Arial", Font.BOLD, 15));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isEnabled() ? new Color(88, 139, 113) : Color.GRAY);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
            super.paintComponent(g);
        }
    }

    class RoundedTextField extends JTextField {
        private final String placeholder;

        RoundedTextField(String placeholder) {
            this.placeholder = placeholder;
            setText(placeholder);
            setForeground(Color.GRAY);
            setFont(new Font("Arial", Font.PLAIN, 14));
            setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

            addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(java.awt.event.FocusEvent e) {
                    if (getText().equals(placeholder)) {
                        setText("");
                        setForeground(Color.BLACK);
                    }
                }

                public void focusLost(java.awt.event.FocusEvent e) {
                    if (getText().isEmpty()) {
                        setText(placeholder);
                        setForeground(Color.GRAY);
                    }
                }
            });
        }

        String getTextValue() {
            return getText().equals(placeholder) ? "" : getText();
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            super.paintComponent(g);
        }
    }

    class RoundedPasswordField extends JPasswordField {
        private final String placeholder;

        RoundedPasswordField(String placeholder) {
            this.placeholder = placeholder;
            setEchoChar((char) 0);
            setText(placeholder);
            setForeground(Color.GRAY);
            setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

            addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(java.awt.event.FocusEvent e) {
                    if (String.valueOf(getPassword()).equals(placeholder)) {
                        setText("");
                        setEchoChar('•');
                        setForeground(Color.BLACK);
                    }
                }

                public void focusLost(java.awt.event.FocusEvent e) {
                    if (getPassword().length == 0) {
                        setEchoChar((char) 0);
                        setText(placeholder);
                        setForeground(Color.GRAY);
                    }
                }
            });
        }

        String getPasswordValue() {
            String pass = String.valueOf(getPassword());
            return pass.equals(placeholder) ? "" : pass;
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            super.paintComponent(g);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(824, 600);
    }
}
