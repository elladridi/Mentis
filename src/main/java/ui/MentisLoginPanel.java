package ui;

import models.user;
import services.userservice;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class MentisLoginPanel extends JPanel {

    private RoundedTextField emailField;
    private RoundedPasswordField passwordField;
    private RoundedButton loginButton;
    private JLabel backLabel;
    private MentisLoginFrame parentFrame;

    public MentisLoginPanel(MentisLoginFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(new Color(216, 228, 222));
        initComponents();
    }

    private void initComponents() {
        // Main container panel
        JPanel mainContainer = new JPanel(new GridBagLayout());
        mainContainer.setBackground(new Color(216, 228, 222));
        mainContainer.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(100, 50, 100, 50);
        gbc.fill = GridBagConstraints.BOTH;

        // ===== Login Card =====
        JPanel card = new JPanel(new GridBagLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(450, 550)); // Bigger card

        GridBagConstraints cardGbc = new GridBagConstraints();
        cardGbc.gridx = 0;
        cardGbc.gridy = 0;
        cardGbc.weightx = 1.0;
        cardGbc.weighty = 0;
        cardGbc.fill = GridBagConstraints.HORIZONTAL;
        cardGbc.insets = new Insets(30, 30, 10, 30);

        // ===== Logo =====
        try {
            URL logoURL = getClass().getResource("/resources/logo.png");
            if (logoURL != null) {
                Image img = new ImageIcon(logoURL).getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                JLabel logo = new JLabel(new ImageIcon(img));
                logo.setHorizontalAlignment(SwingConstants.CENTER);
                cardGbc.gridy++;
                cardGbc.insets = new Insets(30, 30, 20, 30);
                card.add(logo, cardGbc);
            }
        } catch (Exception ignored) {}

        // ===== Title =====
        JLabel title = new JLabel("Welcome Back");
        title.setFont(new Font("Arial", Font.BOLD, 32)); // Bigger font
        title.setForeground(new Color(88, 139, 113));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        cardGbc.gridy++;
        cardGbc.insets = new Insets(0, 30, 30, 30);
        card.add(title, cardGbc);

        // ===== Email =====
        emailField = new RoundedTextField("Email");
        emailField.setFont(new Font("Arial", Font.PLAIN, 16)); // Bigger font
        emailField.setPreferredSize(new Dimension(350, 50)); // Bigger field
        cardGbc.gridy++;
        cardGbc.insets = new Insets(0, 30, 20, 30);
        card.add(emailField, cardGbc);

        // ===== Password =====
        passwordField = new RoundedPasswordField("Password");
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16)); // Bigger font
        passwordField.setPreferredSize(new Dimension(350, 50)); // Bigger field
        cardGbc.gridy++;
        card.add(passwordField, cardGbc);

        // ENTER key triggers login
        passwordField.addActionListener(e -> handleLogin());

        // ===== Login Button =====
        loginButton = new RoundedButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 18)); // Bigger font
        loginButton.setPreferredSize(new Dimension(350, 55)); // Bigger button
        cardGbc.gridy++;
        cardGbc.insets = new Insets(30, 30, 20, 30);
        card.add(loginButton, cardGbc);
        loginButton.addActionListener(e -> handleLogin());

        // ===== Back link =====
        backLabel = new JLabel("← Back to Welcome");
        backLabel.setFont(new Font("Arial", Font.PLAIN, 16)); // Bigger font
        backLabel.setForeground(new Color(88, 139, 113));
        backLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cardGbc.gridy++;
        cardGbc.insets = new Insets(10, 30, 30, 30);
        card.add(backLabel, cardGbc);
        backLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                parentFrame.showWelcomePanel();
            }
        });

        mainContainer.add(card, gbc);
        add(mainContainer, BorderLayout.CENTER);
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
                // Use parentFrame's login method with user information
                parentFrame.login(
                        loggedUser.getType(), // user type
                        loggedUser.getId(),   // user ID
                        loggedUser.getFirstName() + " " + loggedUser.getLastName() // user name
                );

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
            setFont(new Font("Arial", Font.BOLD, 18));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isEnabled() ? new Color(88, 139, 113) : Color.GRAY);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            super.paintComponent(g);
        }
    }

    class RoundedTextField extends JTextField {
        private final String placeholder;

        RoundedTextField(String placeholder) {
            this.placeholder = placeholder;
            setText(placeholder);
            setForeground(Color.GRAY);
            setFont(new Font("Arial", Font.PLAIN, 16));
            setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
            setOpaque(false);

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
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
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
            setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
            setOpaque(false);

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
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
            super.paintComponent(g);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(1400, 800); // Match frame size
    }
}