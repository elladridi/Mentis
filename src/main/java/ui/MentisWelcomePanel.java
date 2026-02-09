package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;

public class MentisWelcomePanel extends JPanel {

    private static final Color BG = new Color(216, 228, 222);
    private static final Color PRIMARY = new Color(88, 139, 113);
    private static final Color TEXT = new Color(35, 35, 35);

    private float alpha = 0f; // for fade-in
    private final MentisLoginFrame parentFrame;

    public MentisWelcomePanel(MentisLoginFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(BG);
        setFocusable(true);

        initComponents();
        startFadeIn();
    }

    /* ================= FADE IN ================= */
    private void startFadeIn() {
        Timer timer = new Timer(25, e -> {
            alpha += 0.04f;
            if (alpha >= 1f) {
                alpha = 1f;
                ((Timer) e.getSource()).stop();
            }
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        super.paintComponent(g2);
        g2.dispose();
    }

    /* ================= UI ================= */
    private void initComponents() {
        // Main content panel with GridBagLayout for centering
        JPanel mainContentPanel = new JPanel(new GridBagLayout());
        mainContentPanel.setBackground(BG);
        mainContentPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);

        // Header with logo and brand name
        JPanel headerPanel = createHeaderPanel();
        mainContentPanel.add(headerPanel, gbc);

        // Title
        gbc.gridy++;
        gbc.insets = new Insets(200, 0, 20, 0);
        JLabel title = new JLabel("Welcome to MENTIS");
        title.setFont(new Font("Serif", Font.BOLD, 72)); // Increased from 56 to 72
        title.setForeground(TEXT);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        mainContentPanel.add(title, gbc);

        // Subtitle
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 40, 0);
        JLabel subtitle = new JLabel(
                "<html><center><span style='font-size:16pt'>Your space for mental well-being<br/>and personal development</span></center></html>",
                SwingConstants.CENTER
        );
        subtitle.setFont(new Font("Georgia", Font.ITALIC, 16)); // Using HTML for bigger font
        subtitle.setForeground(PRIMARY);
        mainContentPanel.add(subtitle, gbc);

        // Buttons panel
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);
        JPanel buttonPanel = createButtonPanel();
        mainContentPanel.add(buttonPanel, gbc);

        // Footer
        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.insets = new Insets(60, 0, 30, 0);
        JLabel footer = new JLabel(
                "© 2026 Mentis · Mental health, handled with care",
                SwingConstants.CENTER
        );
        footer.setFont(new Font("Arial", Font.PLAIN, 14)); // Increased from 12 to 14
        footer.setForeground(new Color(90, 90, 90));
        mainContentPanel.add(footer, gbc);

        // Add main content panel to center
        add(mainContentPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        headerPanel.setBackground(BG);
        headerPanel.setOpaque(false);

        JLabel logo = loadLogo();
        headerPanel.add(logo);

        JLabel brand = new JLabel("Mentis");
        brand.setFont(new Font("Arial", Font.BOLD, 28)); // Increased from 24 to 28
        brand.setForeground(PRIMARY);
        headerPanel.add(brand);

        return headerPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        buttonPanel.setBackground(BG);
        buttonPanel.setOpaque(false);

        // Buttons
        RoundedButton loginBtn = new RoundedButton("Log in");
        loginBtn.setPreferredSize(new Dimension(180, 60)); // Increased size
        loginBtn.setFont(new Font("Arial", Font.BOLD, 18)); // Increased font
        loginBtn.setMnemonic('L');
        loginBtn.setToolTipText("Go to login screen");
        loginBtn.addActionListener(e -> parentFrame.showLoginPanel());

        RoundedButton signupBtn = new RoundedButton("Sign up");
        signupBtn.setPreferredSize(new Dimension(180, 60)); // Increased size
        signupBtn.setFont(new Font("Arial", Font.BOLD, 18)); // Increased font
        signupBtn.setMnemonic('S');
        signupBtn.setToolTipText("Create a new account");
        signupBtn.addActionListener(e -> parentFrame.showSignUpPanel());

        buttonPanel.add(loginBtn);
        buttonPanel.add(signupBtn);

        return buttonPanel;
    }

    /* ================= HELPERS ================= */
    private JLabel loadLogo() {
        try {
            URL url = getClass().getClassLoader().getResource("logo.png");
            if (url != null) {
                Image img = new ImageIcon(url).getImage()
                        .getScaledInstance(80, 80, Image.SCALE_SMOOTH); // Increased from 60 to 80
                return new JLabel(new ImageIcon(img));
            }
        } catch (Exception ignored) {}
        return new BrainLogo();
    }

    /* ================= CUSTOM COMPONENTS ================= */
    class RoundedButton extends JButton {

        public RoundedButton(String text) {
            super(text);
            setFocusPainted(true);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFont(new Font("Arial", Font.BOLD, 18)); // Increased from 16 to 18
            setForeground(Color.WHITE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAccessibleContext();
        }

        private void setAccessibleContext() {
            getAccessibleContext().setAccessibleName(getText());
            getAccessibleContext().setAccessibleDescription(
                    "Button to navigate to " + getText()
            );
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (getModel().isPressed()) {
                g2.setColor(PRIMARY.darker());
            } else if (getModel().isRollover()) {
                g2.setColor(new Color(98, 159, 133));
            } else {
                g2.setColor(PRIMARY);
            }

            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40); // Increased corner radius
            g2.dispose();

            super.paintComponent(g);
        }
    }

    class BrainLogo extends JLabel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(PRIMARY);
            g2.setStroke(new BasicStroke(3)); // Increased stroke width

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            g2.drawArc(cx - 25, cy - 25, 30, 40, 90, 180); // Increased size
            g2.drawArc(cx - 15, cy - 30, 35, 32, 0, 180); // Increased size
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(80, 80); // Increased size
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(1400, 800); // Match the frame size
    }
}