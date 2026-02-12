package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class login extends JPanel {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public login() {
        setLayout(null);
        setBackground(new Color(216, 228, 222)); // Light sage green background

        initComponents();
    }

    private void initComponents() {
        // Logo and Title
        JLabel titleLabel = new JLabel("Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(new Color(88, 139, 113)); // Sage green
        titleLabel.setBounds(0, 40, getPreferredSize().width, 60);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel);

        // Logo circle panel
        CircleLogoPanel logoPanel = new CircleLogoPanel();
        logoPanel.setBounds(312, 120, 200, 200);
        add(logoPanel);

        // Username field
        usernameField = new RoundedTextField(20);
        usernameField.setText("Username");
        usernameField.setForeground(Color.GRAY);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 16));
        usernameField.setBounds(287, 340, 250, 45);
        usernameField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (usernameField.getText().equals("Username")) {
                    usernameField.setText("");
                    usernameField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (usernameField.getText().isEmpty()) {
                    usernameField.setForeground(Color.GRAY);
                    usernameField.setText("Username");
                }
            }
        });
        add(usernameField);

        // Password field
        passwordField = new RoundedPasswordField(20);
        passwordField.setEchoChar((char)0);
        passwordField.setText("Password");
        passwordField.setForeground(Color.GRAY);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordField.setBounds(287, 400, 250, 45);
        passwordField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (String.valueOf(passwordField.getPassword()).equals("Password")) {
                    passwordField.setText("");
                    passwordField.setEchoChar('•');
                    passwordField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (passwordField.getPassword().length == 0) {
                    passwordField.setEchoChar((char)0);
                    passwordField.setForeground(Color.GRAY);
                    passwordField.setText("Password");
                }
            }
        });
        add(passwordField);

        // Contact information
        JLabel contactLabel = new JLabel("<html><center>Contact Us<br>Email: info@mentis.com<br>Phone: +123 456 7890</center></html>");
        contactLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        contactLabel.setForeground(new Color(100, 100, 100));
        contactLabel.setBounds(0, 520, getPreferredSize().width, 60);
        contactLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(contactLabel);
    }

    // Custom rounded text field
    class RoundedTextField extends JTextField {
        private Shape shape;

        public RoundedTextField(int size) {
            super(size);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 25, 25);
            super.paintComponent(g);
            g2.dispose();
        }

        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(200, 200, 200));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 25, 25);
            g2.dispose();
        }

        public boolean contains(int x, int y) {
            if (shape == null || !shape.getBounds().equals(getBounds())) {
                shape = new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 25, 25);
            }
            return shape.contains(x, y);
        }
    }

    // Custom rounded password field
    class RoundedPasswordField extends JPasswordField {
        private Shape shape;

        public RoundedPasswordField(int size) {
            super(size);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 25, 25);
            super.paintComponent(g);
            g2.dispose();
        }

        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(200, 200, 200));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 25, 25);
            g2.dispose();
        }

        public boolean contains(int x, int y) {
            if (shape == null || !shape.getBounds().equals(getBounds())) {
                shape = new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 25, 25);
            }
            return shape.contains(x, y);
        }
    }

    // Custom circle logo panel
    class CircleLogoPanel extends JPanel {

        public CircleLogoPanel() {
            setOpaque(false);
            setLayout(new BorderLayout());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw white circle
            g2.setColor(Color.WHITE);
            g2.fillOval(0, 0, getWidth(), getHeight());

            // Draw brain/head outline
            g2.setColor(new Color(88, 139, 113)); // Sage green
            g2.setStroke(new BasicStroke(2.5f));

            // Head outline
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;

            // Draw simplified head profile
            g2.drawArc(centerX - 35, centerY - 30, 40, 50, 90, 180);

            // Draw brain outline (simplified)
            g2.drawArc(centerX - 25, centerY - 35, 45, 40, 0, 180);
            g2.drawLine(centerX - 25, centerY - 15, centerX - 15, centerY - 10);
            g2.drawLine(centerX - 15, centerY - 10, centerX - 5, centerY - 15);
            g2.drawLine(centerX - 5, centerY - 15, centerX + 5, centerY - 10);
            g2.drawLine(centerX + 5, centerY - 10, centerX + 15, centerY - 15);

            // Draw "Mentis" text
            g2.setFont(new Font("Arial", Font.BOLD, 24));
            FontMetrics fm = g2.getFontMetrics();
            String text = "Mentis";
            int textWidth = fm.stringWidth(text);
            g2.drawString(text, (getWidth() - textWidth) / 2, getHeight() - 30);

            g2.dispose();
        }
    }

    public String getUsername() {
        return usernameField.getText().equals("Username") ? "" : usernameField.getText();
    }

    public String getPassword() {
        String pass = String.valueOf(passwordField.getPassword());
        return pass.equals("Password") ? "" : pass;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(824, 600);
    }
}