package ui;

import java.util.ArrayList;
import java.util.Date;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.awt.image.BufferedImage;
import controller.AssessmentController;
import controller.AssessmentResultController;
import models.AssessmentResult;
import models.Assessment;
import models.Question;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Files;
import javax.swing.JFileChooser;
import javax.swing.JTabbedPane;

public class TakeAssessmentPanel extends JPanel {
    private int userId;
    private MentisLoginFrame parentFrame;
    private AssessmentController assessmentController;
    private AssessmentResultController resultController;
    private List<Assessment> availableAssessments;
    private List<Question> currentQuestions;
    private Map<Integer, String> answers;
    private int currentQuestionIndex = 0;
    private int currentAssessmentId = 0;
    private JLabel imageLabel; // ADD THIS LINE - field for the image label

    // UI Components
    private JPanel selectionPanel;
    private JPanel cardsPanel;
    private JPanel questionPanel;
    private JLabel questionNumberLabel;
    private JLabel questionTextLabel;
    private JComboBox<String> answerCombo;
    private JButton prevButton;
    private JButton nextButton;
    private JButton submitButton;

    // For image handling
    private Map<Integer, ImageIcon> assessmentImages = new HashMap<>();

    public TakeAssessmentPanel(MentisLoginFrame parentFrame, AssessmentController assessmentController,
                               AssessmentResultController resultController) {

        this.parentFrame = parentFrame;
        this.assessmentController = assessmentController;
        this.resultController = resultController;
        this.answers = new HashMap<>();

        // Get userId from parentFrame
        this.userId = parentFrame.getUserId();

        setLayout(new CardLayout());
        setBackground(parentFrame.BACKGROUND_BEIGE);

        // Create both panels
        createSelectionPanel();
        createQuestionPanel();

        // Add to card layout
        add(selectionPanel, "SELECTION");
        add(questionPanel, "QUESTIONS");

        // Show selection initially
        showSelectionPanel();
    }

    private void createSelectionPanel() {
        selectionPanel = new JPanel(new BorderLayout());
        selectionPanel.setBackground(parentFrame.BACKGROUND_BEIGE);
        selectionPanel.setBorder(BorderFactory.createEmptyBorder(45, 50, 45, 50));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(parentFrame.BACKGROUND_BEIGE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 35, 0));

        JLabel titleLabel = new JLabel("Take Assessment!");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 42));
        titleLabel.setForeground(parentFrame.ACCENT_GREEN);

        // Top right - User info and links
        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 30, 0));
        topRightPanel.setBackground(parentFrame.BACKGROUND_BEIGE);

        // Display user ID instead of input field
        JLabel userInfoLabel = new JLabel("User ID: " + userId);
        userInfoLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userInfoLabel.setForeground(parentFrame.TEXT_DARK);
        userInfoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));

        JButton resultsLink = createHeaderLink("Results");
        resultsLink.addActionListener(e -> parentFrame.showPanel("RESULTS"));

        topRightPanel.add(userInfoLabel);
        topRightPanel.add(resultsLink);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(topRightPanel, BorderLayout.EAST);

        // ========== ADD SEARCH BAR HERE ==========
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(parentFrame.BACKGROUND_BEIGE);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 15, 0));

        // Search label and field
        JPanel searchInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchInputPanel.setBackground(parentFrame.BACKGROUND_BEIGE);

        JLabel searchLabel = new JLabel("Search Assessments:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchLabel.setForeground(parentFrame.TEXT_DARK);

        JTextField searchField = new JTextField(25);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBackground(parentFrame.CARD_WHITE);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(parentFrame.BORDER_LIGHT, 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        searchField.setToolTipText("Type assessment title to search...");

        JButton searchButton = new JButton("Search");
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        searchButton.setBackground(parentFrame.ACCENT_LIGHT_GREEN);
        searchButton.setForeground(parentFrame.TEXT_DARK);
        searchButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        searchButton.setFocusPainted(false);
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchButton.addActionListener(e -> searchAssessments(searchField.getText().trim()));

        // Clear search button
        JButton clearSearchButton = new JButton("Clear");
        clearSearchButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        clearSearchButton.setBackground(parentFrame.BACKGROUND_BEIGE);
        clearSearchButton.setForeground(parentFrame.TEXT_LIGHT);
        clearSearchButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        clearSearchButton.setFocusPainted(false);
        clearSearchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearSearchButton.addActionListener(e -> {
            searchField.setText("");
            refreshData(); // Show all assessments
        });

        searchInputPanel.add(searchLabel);
        searchInputPanel.add(searchField);
        searchInputPanel.add(searchButton);
        searchInputPanel.add(clearSearchButton);

        searchPanel.add(searchInputPanel, BorderLayout.CENTER);
        // ========== END SEARCH BAR ==========

        // User info display panel
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userPanel.setBackground(parentFrame.BACKGROUND_BEIGE);
        userPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        JLabel userLabel = new JLabel("Logged in as User ID: " + userId);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userLabel.setForeground(parentFrame.ACCENT_GREEN);
        userLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(parentFrame.ACCENT_LIGHT_GREEN, 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        userPanel.add(userLabel);

        // Header container - updated to include search
        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.setBackground(parentFrame.BACKGROUND_BEIGE);
        headerContainer.add(headerPanel, BorderLayout.NORTH);
        headerContainer.add(searchPanel, BorderLayout.CENTER); // Add search panel
        headerContainer.add(userPanel, BorderLayout.SOUTH);

        selectionPanel.add(headerContainer, BorderLayout.NORTH);

        // Assessment cards grid - USE GridBagLayout for better control
        cardsPanel = new JPanel(new GridBagLayout());
        cardsPanel.setBackground(parentFrame.BACKGROUND_BEIGE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.NORTHWEST;

        JScrollPane scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(parentFrame.BACKGROUND_BEIGE);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        selectionPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void searchAssessments(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            refreshData(); // Show all if search is empty
            return;
        }

        cardsPanel.removeAll();
        cardsPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.NORTHWEST;

        if (availableAssessments == null || availableAssessments.isEmpty()) {
            JLabel emptyLabel = new JLabel("No assessments available", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            emptyLabel.setForeground(parentFrame.TEXT_GRAY);
            cardsPanel.add(emptyLabel, gbc);
            cardsPanel.revalidate();
            cardsPanel.repaint();
            return;
        }

        String searchLower = searchText.toLowerCase().trim();
        List<Assessment> filteredAssessments = new ArrayList<>();

        // Filter assessments by title
        for (Assessment assessment : availableAssessments) {
            if (assessment.getTitle().toLowerCase().contains(searchLower) ||
                    (assessment.getDescription() != null &&
                            assessment.getDescription().toLowerCase().contains(searchLower)) ||
                    assessment.getType().toLowerCase().contains(searchLower)) {
                filteredAssessments.add(assessment);
            }
        }

        if (filteredAssessments.isEmpty()) {
            // Show "no results" message
            JLabel noResultsLabel = new JLabel(
                    "<html><div style='text-align: center;'>" +
                            "<h3 style='color: #666;'>No matching assessments found</h3>" +
                            "<p style='color: #888;'>Try searching with different keywords</p>" +
                            "<p style='color: #888;'>Search: \"" + searchText + "\"</p>" +
                            "</div></html>",
                    SwingConstants.CENTER
            );
            noResultsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            cardsPanel.add(noResultsLabel, gbc);
        } else {
            // Show filtered assessments in a grid
            cardsPanel.setLayout(new GridLayout(0, 2, 25, 25));
            for (Assessment assessment : filteredAssessments) {
                // Load image if not already loaded
                if (!assessmentImages.containsKey(assessment.getAssessmentId())) {
                    loadAssessmentImage(assessment);
                }

                JPanel card = createAssessmentCard(assessment);
                cardsPanel.add(card);
            }
        }

        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private void createQuestionPanel() {
        questionPanel = new JPanel(new BorderLayout());
        questionPanel.setBackground(parentFrame.BACKGROUND_BEIGE);
        questionPanel.setBorder(BorderFactory.createEmptyBorder(45, 50, 45, 50));

        // Back button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(parentFrame.BACKGROUND_BEIGE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        JButton backButton = new JButton("← Back to Assessments");
        backButton.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        backButton.setForeground(new Color(90, 150, 120));
        backButton.setBackground(parentFrame.BACKGROUND_BEIGE);
        backButton.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        backButton.setFocusPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> showSelectionPanel());
        topPanel.add(backButton);

        questionPanel.add(topPanel, BorderLayout.NORTH);

        // Question card
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(parentFrame.CARD_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(parentFrame.BORDER_LIGHT, 1, true),
                BorderFactory.createEmptyBorder(50, 60, 50, 60)
        ));

        // Question number and image area
        JPanel topCardPanel = new JPanel();
        topCardPanel.setLayout(new BoxLayout(topCardPanel, BoxLayout.Y_AXIS));
        topCardPanel.setBackground(parentFrame.CARD_WHITE);

        questionNumberLabel = new JLabel("Question 1 of 10", SwingConstants.CENTER);
        questionNumberLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        questionNumberLabel.setForeground(parentFrame.TEXT_GRAY);
        questionNumberLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        questionNumberLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));

        // Image display - will be updated when assessment starts
        imageLabel = new JLabel("", SwingConstants.CENTER);
        imageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        imageLabel.setForeground(parentFrame.TEXT_GRAY);
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imageLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        imageLabel.setPreferredSize(new Dimension(400, 200));
        imageLabel.setMinimumSize(new Dimension(400, 200));
        imageLabel.setMaximumSize(new Dimension(400, 200));
        imageLabel.setOpaque(true);
        imageLabel.setBackground(new Color(240, 240, 240));

        topCardPanel.add(questionNumberLabel);
        topCardPanel.add(imageLabel);

        // Question text and answer section
        JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
        middlePanel.setBackground(parentFrame.CARD_WHITE);
        middlePanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 40, 40));

        questionTextLabel = new JLabel("[question text will appear here]", SwingConstants.CENTER);
        questionTextLabel.setFont(new Font("Segoe UI", Font.BOLD, 19));
        questionTextLabel.setForeground(parentFrame.TEXT_DARK);
        questionTextLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        questionTextLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 35, 0));

        // Answer combo box
        answerCombo = new JComboBox<>();
        answerCombo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        answerCombo.setBackground(parentFrame.CARD_WHITE);
        answerCombo.setMaximumSize(new Dimension(600, 50));
        answerCombo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(parentFrame.ACCENT_GREEN, 2, true),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)
        ));

        middlePanel.add(questionTextLabel);
        middlePanel.add(answerCombo);

        card.add(topCardPanel, BorderLayout.NORTH);
        card.add(middlePanel, BorderLayout.CENTER);

        // Center the card
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(parentFrame.BACKGROUND_BEIGE);
        centerPanel.add(card);
        questionPanel.add(centerPanel, BorderLayout.CENTER);

        // Navigation buttons
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 25));
        navPanel.setBackground(parentFrame.BACKGROUND_BEIGE);
        navPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));

        prevButton = new JButton("Previous");
        styleNavButton(prevButton, parentFrame.ACCENT_LIGHT_GREEN);
        prevButton.setEnabled(false);
        prevButton.addActionListener(e -> showPreviousQuestion());

        nextButton = new JButton("Next");
        styleNavButton(nextButton, parentFrame.ACCENT_LIGHT_GREEN);
        nextButton.setEnabled(false);
        nextButton.addActionListener(e -> showNextQuestion());

        submitButton = new JButton("Submit");
        styleNavButton(submitButton, parentFrame.ACCENT_GREEN);
        submitButton.setForeground(Color.WHITE);
        submitButton.setEnabled(false);
        submitButton.addActionListener(e -> submitAssessment());

        navPanel.add(prevButton);
        navPanel.add(nextButton);
        navPanel.add(submitButton);
        questionPanel.add(navPanel, BorderLayout.SOUTH);
    }

    private JButton createHeaderLink(String text) {
        JButton link = new JButton(text);
        link.setFont(new Font("Segoe UI", Font.BOLD, 16));
        link.setForeground(parentFrame.ACCENT_GREEN);
        link.setBackground(parentFrame.BACKGROUND_BEIGE);
        link.setBorderPainted(false);
        link.setFocusPainted(false);
        link.setContentAreaFilled(false);
        link.setCursor(new Cursor(Cursor.HAND_CURSOR));

        link.addActionListener(e -> {
            if (text.equals("Results")) {
                parentFrame.showPanel("RESULTS");
            }
        });

        return link;
    }

    private void styleNavButton(JButton button, Color bgColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(bgColor);
        button.setForeground(new Color(40, 70, 50));
        button.setBorder(BorderFactory.createEmptyBorder(12, 35, 12, 35));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (bgColor.equals(parentFrame.ACCENT_GREEN)) {
            button.setForeground(Color.WHITE);
        }
    }

    public void refreshData() {
        try {
            availableAssessments = assessmentController.getActiveAssessments();
            assessmentImages.clear(); // Clear previous images
            displayAssessmentCards();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading assessments: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayAssessmentCards() {
        cardsPanel.removeAll();

        // Use GridLayout for cards display
        cardsPanel.setLayout(new GridLayout(0, 2, 25, 25));

        if (availableAssessments == null || availableAssessments.isEmpty()) {
            JLabel emptyLabel = new JLabel("No assessments available", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            emptyLabel.setForeground(new Color(40, 70, 50));
            cardsPanel.add(emptyLabel);
        } else {
            // Load all images first
            for (Assessment assessment : availableAssessments) {
                loadAssessmentImage(assessment);
            }

            // Then create cards
            for (Assessment assessment : availableAssessments) {
                JPanel card = createAssessmentCard(assessment);
                cardsPanel.add(card);
            }
        }

        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private void loadAndDisplayAssessmentImage(Assessment assessment) {
        if (imageLabel == null) return;

        if (assessment.getImagePath() != null && !assessment.getImagePath().isEmpty()) {
            try {
                File imgFile = new File(assessment.getImagePath());
                if (imgFile.exists()) {
                    // Load the image
                    BufferedImage originalImage = ImageIO.read(imgFile);
                    if (originalImage != null) {
                        // Resize to fit the question panel (400x200)
                        int targetWidth = 400;
                        int targetHeight = 200;

                        // Calculate scaling to fit within the area
                        double widthScale = (double) targetWidth / originalImage.getWidth();
                        double heightScale = (double) targetHeight / originalImage.getHeight();
                        double scale = Math.min(widthScale, heightScale); // Fit inside (not fill)

                        int scaledWidth = (int) (originalImage.getWidth() * scale);
                        int scaledHeight = (int) (originalImage.getHeight() * scale);

                        // Create scaled image
                        Image scaledImage = originalImage.getScaledInstance(
                                scaledWidth, scaledHeight, Image.SCALE_SMOOTH);

                        // Create centered icon
                        BufferedImage outputImage = new BufferedImage(
                                targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
                        Graphics2D g2d = outputImage.createGraphics();

                        // Set background color
                        g2d.setColor(new Color(240, 240, 240));
                        g2d.fillRect(0, 0, targetWidth, targetHeight);

                        // Center the scaled image
                        int x = (targetWidth - scaledWidth) / 2;
                        int y = (targetHeight - scaledHeight) / 2;
                        g2d.drawImage(scaledImage, x, y, null);
                        g2d.dispose();

                        ImageIcon icon = new ImageIcon(outputImage);
                        imageLabel.setIcon(icon);
                        imageLabel.setText(""); // Clear text
                        imageLabel.setBackground(new Color(240, 240, 240));
                        return;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error loading assessment image: " + e.getMessage());
            }
        }

        // If no image or error, show a colored placeholder
        imageLabel.setIcon(null);
        imageLabel.setText(assessment.getTitle());
        imageLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        imageLabel.setForeground(Color.WHITE);
        imageLabel.setBackground(getTypeColor(assessment.getType()));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
    }

    private void loadAssessmentImage(Assessment assessment) {
        if (assessment.getImagePath() != null && !assessment.getImagePath().isEmpty()) {
            try {
                File imgFile = new File(assessment.getImagePath());
                if (imgFile.exists()) {
                    // Load image
                    BufferedImage originalImage = ImageIO.read(imgFile);
                    if (originalImage != null) {
                        // Calculate scaling to fill 450x180 without distortion
                        int targetWidth = 450;
                        int targetHeight = 180;

                        // Calculate scaling factors
                        double widthScale = (double) targetWidth / originalImage.getWidth();
                        double heightScale = (double) targetHeight / originalImage.getHeight();

                        // Use the larger scale to fill the area (crop if needed)
                        double scale = Math.max(widthScale, heightScale);

                        int scaledWidth = (int) (originalImage.getWidth() * scale);
                        int scaledHeight = (int) (originalImage.getHeight() * scale);

                        // Create scaled image
                        Image scaledImage = originalImage.getScaledInstance(
                                scaledWidth, scaledHeight, Image.SCALE_SMOOTH);

                        // Create BufferedImage from scaled image
                        BufferedImage outputImage = new BufferedImage(
                                targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
                        Graphics2D g2d = outputImage.createGraphics();

                        // Set background color (will show if image doesn't fill completely)
                        g2d.setColor(getTypeColor(assessment.getType()));
                        g2d.fillRect(0, 0, targetWidth, targetHeight);

                        // Center the scaled image
                        int x = (targetWidth - scaledWidth) / 2;
                        int y = (targetHeight - scaledHeight) / 2;
                        g2d.drawImage(scaledImage, x, y, null);
                        g2d.dispose();

                        ImageIcon icon = new ImageIcon(outputImage);
                        assessmentImages.put(assessment.getAssessmentId(), icon);
                    }
                }
            } catch (Exception e) {
                // Image loading failed, will use placeholder
                System.err.println("Error loading image for assessment " + assessment.getTitle() + ": " + e.getMessage());
            }
        }
    }

    private JPanel createAssessmentCard(Assessment assessment) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(parentFrame.ACCENT_LIGHT_GREEN);
        card.setBorder(BorderFactory.createLineBorder(parentFrame.ACCENT_LIGHT_GREEN, 1, true));
        card.setPreferredSize(new Dimension(450, 350));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Image area
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(Color.WHITE);
        imagePanel.setPreferredSize(new Dimension(450, 180));
        imagePanel.setBorder(null);

        JLabel imageLabel = new JLabel("", SwingConstants.CENTER);
        imageLabel.setOpaque(true);

        // Check if we have an image for this assessment
        ImageIcon icon = assessmentImages.get(assessment.getAssessmentId());
        if (icon != null) {
            imageLabel.setIcon(icon);
            imageLabel.setText("");
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        } else {
            // Create a full-color placeholder that fills the entire area
            imageLabel.setText(assessment.getTitle());
            imageLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            imageLabel.setForeground(Color.WHITE);
            imageLabel.setBackground(getTypeColor(assessment.getType()));
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        }

        // Add image label to fill the entire image panel
        imagePanel.add(imageLabel, BorderLayout.CENTER);

        // Content area
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(parentFrame.ACCENT_LIGHT_GREEN);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 20, 25));

        JLabel titleLabel = new JLabel(assessment.getTitle(), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(40, 70, 50));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel typeLabel = new JLabel(assessment.getType(), SwingConstants.CENTER);
        typeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        typeLabel.setForeground(getTypeColor(assessment.getType()));
        typeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        typeLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 20, 0));

        // Add description if available (truncated)
        String description = assessment.getDescription();
        if (description != null && description.length() > 0) {
            JLabel descLabel = new JLabel("", SwingConstants.CENTER);
            if (description.length() > 60) {
                descLabel.setText("<html><center>" + description.substring(0, 57) + "...</center></html>");
            } else {
                descLabel.setText("<html><center>" + description + "</center></html>");
            }
            descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            descLabel.setForeground(parentFrame.TEXT_LIGHT);
            descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            descLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            contentPanel.add(descLabel);
        }

        // Take test button - MAKE SURE THIS IS VISIBLE
        JButton takeTestBtn = new JButton("TAKE TEST");
        takeTestBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        takeTestBtn.setBackground(parentFrame.ACCENT_GREEN);
        takeTestBtn.setForeground(Color.WHITE);
        takeTestBtn.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));
        takeTestBtn.setFocusPainted(false);
        takeTestBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        takeTestBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        takeTestBtn.addActionListener(e -> startAssessment(assessment));

        contentPanel.add(titleLabel);
        contentPanel.add(typeLabel);
        contentPanel.add(Box.createVerticalGlue()); // This pushes content up
        contentPanel.add(takeTestBtn);

        card.add(imagePanel, BorderLayout.NORTH);
        card.add(contentPanel, BorderLayout.CENTER);

        // Hover effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBorder(BorderFactory.createLineBorder(parentFrame.ACCENT_GREEN, 3, true));
                // Slight darken effect on image when hovering
                if (icon != null) {
                    imageLabel.setBackground(new Color(240, 240, 240));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBorder(BorderFactory.createLineBorder(parentFrame.ACCENT_LIGHT_GREEN, 1, true));
                if (icon != null) {
                    imageLabel.setBackground(Color.WHITE);
                }
            }
        });

        return card;
    }

    private Color getTypeColor(String type) {
        if (type == null) return new Color(165, 186, 227);

        switch (type.toLowerCase()) {
            case "depression":
                return new Color(144, 127, 201); // Purple
            case "anxiety":
                return new Color(227, 149, 149); // Red
            case "stress":
                return new Color(227, 206, 163); // Orange
            case "wellness":
                return parentFrame.ACCENT_DARK_GREEN; // Green
            case "general":
                return new Color(165, 186, 227); // Blue
            default:
                return new Color(165, 186, 227);
        }
    }

    private Color getTypeBackgroundColor(String type) {
        if (type == null) return new Color(240, 240, 240);

        switch (type.toLowerCase()) {
            case "depression":
                return new Color(240, 235, 255); // Light purple
            case "anxiety":
                return new Color(255, 235, 235); // Light red
            case "stress":
                return new Color(255, 245, 215); // Light orange
            case "wellness":
                return new Color(235, 255, 240); // Light green
            case "general":
                return new Color(235, 245, 255); // Light blue
            default:
                return new Color(240, 240, 240); // Light gray
        }
    }

    private void startAssessment(Assessment assessment) {
        try {
            // No longer need to check user ID input since it comes from login
            if (userId <= 0) {
                JOptionPane.showMessageDialog(this,
                        "User ID not found. Please login again.",
                        "Authentication Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            currentAssessmentId = assessment.getAssessmentId();
            currentQuestions = resultController.getQuestionsByAssessment(currentAssessmentId);

            if (currentQuestions == null || currentQuestions.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No questions found for this assessment!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            currentQuestionIndex = 0;
            answers.clear();

            prevButton.setEnabled(false);
            nextButton.setEnabled(currentQuestions.size() > 1);
            submitButton.setEnabled(true);

            // Load and display assessment image
            loadAndDisplayAssessmentImage(assessment);

            showQuestion(currentQuestionIndex);
            showQuestionPanel();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error starting assessment: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showQuestion(int index) {
        if (currentQuestions == null || index < 0 || index >= currentQuestions.size()) {
            return;
        }

        Question question = currentQuestions.get(index);

        questionNumberLabel.setText("Question " + (index + 1) + " of " + currentQuestions.size());
        questionTextLabel.setText("<html><div style='text-align: center; width: 500px;'>" +
                question.getText() + "</div></html>");

        String[] options = resultController.parseScaleToOptions(question.getScale());
        answerCombo.removeAllItems();

        for (String option : options) {
            answerCombo.addItem(option);
        }

        if (answers.containsKey(question.getQuestionId())) {
            answerCombo.setSelectedItem(answers.get(question.getQuestionId()));
        } else {
            answerCombo.setSelectedIndex(0);
        }

        prevButton.setEnabled(index > 0);
        nextButton.setEnabled(index < currentQuestions.size() - 1);
    }

    private void saveCurrentAnswer() {
        if (currentQuestions != null && currentQuestionIndex < currentQuestions.size()) {
            Question question = currentQuestions.get(currentQuestionIndex);
            answers.put(question.getQuestionId(), (String) answerCombo.getSelectedItem());
        }
    }

    private void showPreviousQuestion() {
        if (currentQuestionIndex > 0) {
            saveCurrentAnswer();
            currentQuestionIndex--;
            showQuestion(currentQuestionIndex);
        }
    }

    private void showNextQuestion() {
        if (currentQuestionIndex < currentQuestions.size() - 1) {
            saveCurrentAnswer();
            currentQuestionIndex++;
            showQuestion(currentQuestionIndex);
        }
    }

    private void submitAssessment() {
        saveCurrentAnswer();

        if (answers.size() < currentQuestions.size()) {
            JOptionPane.showMessageDialog(this,
                    "Please answer all questions before submitting!",
                    "Incomplete Assessment",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Map<Integer, Integer> answerScores = new HashMap<>();
        Map<Integer, String> originalAnswers = new HashMap<>();

        for (Question question : currentQuestions) {
            String answer = answers.get(question.getQuestionId());
            if (answer != null) {
                int score = resultController.parseAnswerToScore(answer, question.getScale());
                answerScores.put(question.getQuestionId(), score);
                originalAnswers.put(question.getQuestionId(), answer);
            }
        }

        try {
            Map<String, Object> result = resultController.submitAssessment(
                    userId, currentAssessmentId, answerScores, originalAnswers);

            if ((Boolean) result.get("success")) {
                showResultsWithAI((Map<String, Object>) result.get("result"));
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error submitting assessment: " + result.get("error"),
                        "Submission Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showResultsWithAI(Map<String, Object> result) {
        // Create a custom dialog for showing results with export option
        JDialog resultDialog = new JDialog(parentFrame, "Assessment Results", true);
        resultDialog.setSize(700, 800);
        resultDialog.setLocationRelativeTo(parentFrame);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(parentFrame.BACKGROUND_BEIGE);

        // Create tabbed pane for different views
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Tab 1: Summary
        JPanel summaryPanel = createSummaryTab(result);
        tabbedPane.addTab("📊 Summary", summaryPanel);

        // Tab 2: AI Analysis
        JPanel analysisPanel = createAnalysisTab(result);
        tabbedPane.addTab("🤖 AI Analysis", analysisPanel);

        // Tab 3: Recommendations
        JPanel recommendationsPanel = createRecommendationsTab(result);
        tabbedPane.addTab("💡 Recommendations", recommendationsPanel);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Export button panel at the bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        buttonPanel.setBackground(parentFrame.BACKGROUND_BEIGE);

        JButton exportTextBtn = new JButton("Export as Text File");
        styleExportButton(exportTextBtn);
        exportTextBtn.addActionListener(e -> exportAsText(result));

        JButton exportHTMLBtn = new JButton("Export as HTML");
        styleExportButton(exportHTMLBtn);
        exportHTMLBtn.addActionListener(e -> exportAsHTML(result));

        JButton closeBtn = new JButton("Close");
        styleExportButton(closeBtn);
        closeBtn.addActionListener(e -> {
            resultDialog.dispose();
            showSelectionPanel();
        });

        buttonPanel.add(exportTextBtn);
        buttonPanel.add(exportHTMLBtn);
        buttonPanel.add(closeBtn);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        resultDialog.add(mainPanel);
        resultDialog.setVisible(true);
    }

    private JPanel createSummaryTab(Map<String, Object> result) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        String summary = "=== ASSESSMENT RESULTS ===\n\n" +
                "Total Score: " + result.get("totalScore") + "\n" +
                "Risk Level: " + result.get("riskLevel") + "\n" +
                "Session Suggested: " + (result.get("suggestSession").equals(true) ? "Yes" : "No") + "\n\n" +

                "=== INTERPRETATION ===\n" +
                result.get("interpretation") + "\n\n" +

                "=== KEY INSIGHTS ===\n" +
                result.get("aiAnalysis").toString().split("\n\n")[0]; // Just the summary part

        textArea.setText(summary);

        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAnalysisTab(Map<String, Object> result) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JTextArea textArea = new JTextArea(result.get("aiAnalysis").toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRecommendationsTab(Map<String, Object> result) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextArea textArea = new JTextArea(result.get("recommendedContent").toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        return panel;
    }

    private void styleExportButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(parentFrame.ACCENT_GREEN);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void exportAsText(Map<String, Object> result) {
        try {
            // Get the result object
            AssessmentResult assessmentResult = getAssessmentResultFromMap(result);
            String aiAnalysis = (String) result.get("aiAnalysis");

            String content = resultController.exportResultToText(assessmentResult, aiAnalysis);

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File("mentis_assessment_report.txt"));
            fileChooser.setDialogTitle("Save Report as Text");

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".txt")) {
                    file = new File(file.getAbsolutePath() + ".txt");
                }

                Files.write(file.toPath(), content.getBytes());

                JOptionPane.showMessageDialog(this,
                        "Report exported successfully to:\n" + file.getAbsolutePath(),
                        "Export Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error exporting report: " + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportAsHTML(Map<String, Object> result) {
        try {
            AssessmentResult assessmentResult = getAssessmentResultFromMap(result);
            String aiAnalysis = (String) result.get("aiAnalysis");

            String content = resultController.exportResultToHTML(assessmentResult, aiAnalysis);

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File("mentis_assessment_report.html"));
            fileChooser.setDialogTitle("Save Report as HTML");

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".html")) {
                    file = new File(file.getAbsolutePath() + ".html");
                }

                Files.write(file.toPath(), content.getBytes());

                JOptionPane.showMessageDialog(this,
                        "HTML report exported successfully to:\n" + file.getAbsolutePath(),
                        "Export Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error exporting HTML report: " + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private AssessmentResult getAssessmentResultFromMap(Map<String, Object> result) {
        // Create a dummy AssessmentResult object for export
        AssessmentResult assessmentResult = new AssessmentResult();
        assessmentResult.setResultId((int) result.getOrDefault("resultId", 0));
        assessmentResult.setUserId(userId);
        assessmentResult.setAssessmentId(currentAssessmentId);
        assessmentResult.setTotalScore((int) result.get("totalScore"));
        assessmentResult.setRiskLevel((String) result.get("riskLevel"));
        assessmentResult.setInterpretation((String) result.get("interpretation"));
        assessmentResult.setRecommendedContent((String) result.get("recommendedContent"));
        assessmentResult.setSuggestSession((boolean) result.get("suggestSession"));
        assessmentResult.setTakenAt(new Date());

        return assessmentResult;
    }

    private void showSelectionPanel() {
        currentQuestions = null;
        answers.clear();
        currentQuestionIndex = 0;

        // Clear the assessment image
        if (imageLabel != null) {
            imageLabel.setIcon(null);
            imageLabel.setText("[image]");
            imageLabel.setBackground(new Color(250, 250, 250));
            imageLabel.setForeground(parentFrame.TEXT_GRAY);
            imageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        }

        refreshData();
        CardLayout cl = (CardLayout) getLayout();
        cl.show(this, "SELECTION");
    }

    private void showQuestionPanel() {
        CardLayout cl = (CardLayout) getLayout();
        cl.show(this, "QUESTIONS");
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
        refreshData();
    }
}