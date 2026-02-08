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
        setLayout(null);
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

        JLabel logo = loadLogo();
        logo.setBounds(40, 30, 60, 60);
        add(logo);

        JLabel brand = new JLabel("Mentis");
        brand.setFont(new Font("Arial", Font.BOLD, 24));
        brand.setForeground(PRIMARY);
        brand.setBounds(110, 45, 150, 30);
        add(brand);

        JLabel title = new JLabel("Welcome to MENTIS", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 56));
        title.setForeground(TEXT);
        title.setBounds(0, 200, 924, 70);
        add(title);

        JLabel subtitle = new JLabel(
                "<html><center>Your space for mental well-being<br/>and personal development</center></html>",
                SwingConstants.CENTER
        );
        subtitle.setFont(new Font("Georgia", Font.ITALIC, 22));
        subtitle.setForeground(PRIMARY);
        subtitle.setBounds(0, 290, 924, 90);
        add(subtitle);

        // Buttons
        RoundedButton loginBtn = new RoundedButton("Log in");
        loginBtn.setMnemonic('L');
        loginBtn.setToolTipText("Go to login screen");
        loginBtn.setBounds(320, 430, 140, 45);
        loginBtn.addActionListener(e -> parentFrame.showLoginPanel());

        RoundedButton signupBtn = new RoundedButton("Sign up");
        signupBtn.setMnemonic('S');
        signupBtn.setToolTipText("Create a new account");
        signupBtn.setBounds(470, 430, 140, 45);
        signupBtn.addActionListener(e -> parentFrame.showSignUpPanel());

        add(loginBtn);
        add(signupBtn);

        JLabel footer = new JLabel(
                "© 2026 Mentis · Mental health, handled with care",
                SwingConstants.CENTER
        );
        footer.setFont(new Font("Arial", Font.PLAIN, 12));
        footer.setForeground(new Color(90, 90, 90));
        footer.setBounds(0, 560, 924, 20);
        add(footer);
    }

    /* ================= HELPERS ================= */

    private JLabel loadLogo() {
        try {
            URL url = getClass().getResource("/images/logo.png");
            if (url != null) {
                Image img = new ImageIcon(url).getImage()
                        .getScaledInstance(60, 60, Image.SCALE_SMOOTH);
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
            setFont(new Font("Arial", Font.BOLD, 16));
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

            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
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
            g2.setStroke(new BasicStroke(2));

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            g2.drawArc(cx - 18, cy - 18, 22, 30, 90, 180);
            g2.drawArc(cx - 10, cy - 20, 28, 24, 0, 180);
            g2.dispose();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(924, 600);
    }
}
