package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import controller.AssessmentController;
import models.Assessment;
import java.sql.SQLException;

public class AssessmentFormDialog extends JDialog {
    private MentisLoginFrame parentFrame;
    private AssessmentController controller;
    private Assessment assessment;

    private JTextField titleField;
    private JComboBox<String> typeCombo;
    private JTextArea descriptionArea;
    private JComboBox<String> statusCombo;
    private JLabel imagePreviewLabel;
    private File selectedImageFile;
    private String imagePathToSave;
    private JLabel uploadStatusLabel;

    public AssessmentFormDialog(MentisLoginFrame parentFrame, AssessmentController controller,
                                Assessment assessment, boolean isEdit) {
        super(parentFrame, isEdit ? "Edit Assessment" : "Add Assessment", true);
        this.parentFrame = parentFrame;
        this.controller = controller;
        this.assessment = assessment;

        setSize(800, 700); // Increased size for image upload
        setLocationRelativeTo(parentFrame);
        setLayout(new BorderLayout());
        getContentPane().setBackground(parentFrame.BACKGROUND_LIGHT);

        createForm();
        if (isEdit && assessment != null) {
            loadAssessmentData();
        }

        setVisible(true);
    }

    private void createForm() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(parentFrame.BACKGROUND_LIGHT);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel(assessment == null ? "Add New Assessment" : "Edit Assessment");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(parentFrame.ACCENT_DARK_GREEN);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // Main form panel with scroll
        JPanel mainFormPanel = new JPanel(new BorderLayout());
        mainFormPanel.setBackground(parentFrame.BACKGROUND_LIGHT);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(parentFrame.BACKGROUND_LIGHT);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Title:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        titleField = new JTextField(30);
        titleField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(parentFrame.BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        formPanel.add(titleField, gbc);

        // Type
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Type:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        String[] types = {"Depression", "Anxiety", "Stress", "Wellness", "General", "Custom"};
        typeCombo = new JComboBox<>(types);
        typeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        typeCombo.setBackground(parentFrame.CARD_WHITE);
        formPanel.add(typeCombo, gbc);

        // Status
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Status:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        String[] statuses = {"Active", "Inactive", "Draft"};
        statusCombo = new JComboBox<>(statuses);
        statusCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusCombo.setBackground(parentFrame.CARD_WHITE);
        formPanel.add(statusCombo, gbc);

        // Description
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel descLabel = new JLabel("Description:");
        descLabel.setVerticalAlignment(SwingConstants.TOP);
        formPanel.add(descLabel, gbc);

        gbc.gridx = 1;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        descriptionArea = new JTextArea(4, 30);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(parentFrame.BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        formPanel.add(scrollPane, gbc);

        // Image Upload Section
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0;
        formPanel.add(new JLabel("Image:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.5;

        // Image upload panel
        JPanel imageUploadPanel = createImageUploadPanel();
        formPanel.add(imageUploadPanel, gbc);

        JScrollPane formScrollPane = new JScrollPane(formPanel);
        formScrollPane.setBorder(null);
        formScrollPane.getViewport().setBackground(parentFrame.BACKGROUND_LIGHT);
        mainFormPanel.add(formScrollPane, BorderLayout.CENTER);

        add(mainFormPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        buttonPanel.setBackground(parentFrame.BACKGROUND_LIGHT);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 20, 30));

        JButton cancelButton = new JButton("Cancel");
        styleButton(cancelButton, parentFrame.BUTTON_LIGHT_GREEN);
        cancelButton.addActionListener(e -> dispose());

        JButton saveButton = new JButton(assessment == null ? "Add Assessment" : "Save Changes");
        styleButton(saveButton, parentFrame.ACCENT_DARK_GREEN);
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> saveAssessment());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createImageUploadPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(parentFrame.CARD_WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(parentFrame.BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Preview area
        imagePreviewLabel = new JLabel("", SwingConstants.CENTER);
        imagePreviewLabel.setPreferredSize(new Dimension(300, 200));
        imagePreviewLabel.setBackground(new Color(240, 240, 240));
        imagePreviewLabel.setOpaque(true);
        imagePreviewLabel.setBorder(BorderFactory.createLineBorder(parentFrame.BORDER_LIGHT, 1));

        // Set default placeholder
        ImageIcon placeholder = createPlaceholderIcon(300, 200, "No Image Selected");
        imagePreviewLabel.setIcon(placeholder);
        imagePreviewLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        imagePreviewLabel.setVerticalTextPosition(SwingConstants.CENTER);

        // Upload status label
        uploadStatusLabel = new JLabel("Drag & drop image here or click to browse", SwingConstants.CENTER);
        uploadStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        uploadStatusLabel.setForeground(parentFrame.TEXT_LIGHT);

        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBackground(parentFrame.CARD_WHITE);
        previewPanel.add(imagePreviewLabel, BorderLayout.CENTER);
        previewPanel.add(uploadStatusLabel, BorderLayout.SOUTH);

        // Setup drag & drop
        setupDragAndDrop(imagePreviewLabel);

        // Click to browse
        imagePreviewLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        imagePreviewLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                browseForImage();
            }
        });

        // Control buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setBackground(parentFrame.CARD_WHITE);

        JButton browseButton = new JButton("Browse...");
        styleSmallButton(browseButton);
        browseButton.addActionListener(e -> browseForImage());

        JButton clearButton = new JButton("Clear Image");
        styleSmallButton(clearButton);
        clearButton.addActionListener(e -> clearImage());

        buttonPanel.add(browseButton);
        buttonPanel.add(clearButton);

        panel.add(previewPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void setupDragAndDrop(JLabel dropTarget) {
        new DropTarget(dropTarget, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    Transferable transferable = dtde.getTransferable();

                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        java.util.List<File> fileList = (java.util.List<File>)
                                transferable.getTransferData(DataFlavor.javaFileListFlavor);

                        if (!fileList.isEmpty()) {
                            File file = fileList.get(0);
                            handleImageFile(file);
                        }
                    }
                    dtde.dropComplete(true);
                } catch (Exception e) {
                    dtde.dropComplete(false);
                    JOptionPane.showMessageDialog(AssessmentFormDialog.this,
                            "Error processing dropped file: " + e.getMessage(),
                            "Upload Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void browseForImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Assessment Image");

        // Set file filter for images
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                String name = f.getName().toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                        name.endsWith(".png") || name.endsWith(".gif") ||
                        name.endsWith(".bmp");
            }

            @Override
            public String getDescription() {
                return "Image files (*.jpg, *.jpeg, *.png, *.gif, *.bmp)";
            }
        });

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            handleImageFile(selectedFile);
        }
    }

    private void handleImageFile(File imageFile) {
        try {
            // Check file size (limit to 5MB)
            long fileSize = imageFile.length();
            if (fileSize > 5 * 1024 * 1024) {
                JOptionPane.showMessageDialog(this,
                        "Image file is too large. Maximum size is 5MB.",
                        "File Too Large",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Validate it's an image
            BufferedImage img = ImageIO.read(imageFile);
            if (img == null) {
                throw new IOException("Not a valid image file");
            }

            // Resize image for preview
            ImageIcon icon = resizeImageIcon(new ImageIcon(img), 300, 200);
            imagePreviewLabel.setIcon(icon);
            imagePreviewLabel.setText("");

            // Update status
            uploadStatusLabel.setText(imageFile.getName() + " (" +
                    String.format("%.1f", fileSize / 1024.0) + " KB)");
            uploadStatusLabel.setForeground(parentFrame.ACCENT_DARK_GREEN);

            selectedImageFile = imageFile;

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid image file: " + e.getMessage(),
                    "Invalid File",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private ImageIcon resizeImageIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image resizedImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }

    private void clearImage() {
        ImageIcon placeholder = createPlaceholderIcon(300, 200, "No Image Selected");
        imagePreviewLabel.setIcon(placeholder);
        uploadStatusLabel.setText("Drag & drop image here or click to browse");
        uploadStatusLabel.setForeground(parentFrame.TEXT_LIGHT);
        selectedImageFile = null;
        imagePathToSave = null;
    }

    private ImageIcon createPlaceholderIcon(int width, int height, String text) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Fill background
        g2d.setColor(new Color(240, 240, 240));
        g2d.fillRect(0, 0, width, height);

        // Draw border
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{5, 5}, 0));
        g2d.drawRect(10, 10, width - 20, height - 20);

        // Draw camera icon
        g2d.setColor(new Color(150, 150, 150));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(width/2 - 30, height/2 - 40, 60, 60);
        g2d.fillOval(width/2 - 20, height/2 - 30, 40, 40);
        g2d.setColor(new Color(240, 240, 240));
        g2d.fillOval(width/2 - 10, height/2 - 20, 20, 20);

        // Draw text
        g2d.setColor(new Color(100, 100, 100));
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g2d.drawString(text, (width - textWidth) / 2, height - 20);

        g2d.dispose();
        return new ImageIcon(image);
    }

    private void styleSmallButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setBackground(parentFrame.BUTTON_LIGHT_GREEN);
        button.setForeground(parentFrame.TEXT_DARK);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(bgColor == parentFrame.ACCENT_DARK_GREEN ? Color.WHITE : parentFrame.TEXT_DARK);
        button.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
    }

    private void loadAssessmentData() {
        if (assessment != null) {
            titleField.setText(assessment.getTitle());
            typeCombo.setSelectedItem(assessment.getType());
            descriptionArea.setText(assessment.getDescription());
            statusCombo.setSelectedItem(assessment.getStatus());

            // Load image if exists
            if (assessment.getImagePath() != null && !assessment.getImagePath().isEmpty()) {
                try {
                    File imgFile = new File(assessment.getImagePath());
                    if (imgFile.exists()) {
                        handleImageFile(imgFile);
                    }
                } catch (Exception e) {
                    // Image file not found, keep placeholder
                }
            }
        }
    }

    private String saveImageToStorage() {
        if (selectedImageFile == null) {
            return assessment != null ? assessment.getImagePath() : null;
        }

        try {
            // Create images directory if it doesn't exist
            File imagesDir = new File("assessment_images");
            if (!imagesDir.exists()) {
                imagesDir.mkdir();
            }

            // Generate unique filename
            String timestamp = String.valueOf(System.currentTimeMillis());
            String originalName = selectedImageFile.getName();
            String extension = "";

            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalName.substring(dotIndex);
            }

            String newFileName = "assessment_" + timestamp + extension;
            File destination = new File(imagesDir, newFileName);

            // Copy file
            Files.copy(selectedImageFile.toPath(), destination.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

            // Return relative path (better for portability)
            return "assessment_images/" + newFileName;


        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving image: " + e.getMessage(),
                    "Image Save Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private void saveAssessment() {
        // Validate inputs
        if (titleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a title for the assessment.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Save image first
            String savedImagePath = saveImageToStorage();
            if (selectedImageFile != null && savedImagePath == null) {
                return; // Image save failed
            }

            if (assessment == null) {
                // Create new assessment
                Assessment newAssessment = new Assessment();
                newAssessment.setTitle(titleField.getText().trim());
                newAssessment.setType((String) typeCombo.getSelectedItem());
                newAssessment.setDescription(descriptionArea.getText().trim());
                newAssessment.setStatus((String) statusCombo.getSelectedItem());
                newAssessment.setImagePath(savedImagePath);

                controller.createAssessment(newAssessment);

                JOptionPane.showMessageDialog(this,
                        "Assessment created successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

            } else {
                // Update existing assessment
                assessment.setTitle(titleField.getText().trim());
                assessment.setType((String) typeCombo.getSelectedItem());
                assessment.setDescription(descriptionArea.getText().trim());
                assessment.setStatus((String) statusCombo.getSelectedItem());

                // Only update image path if a new image was selected
                if (savedImagePath != null) {
                    assessment.setImagePath(savedImagePath);
                }

                controller.updateAssessment(assessment);

                JOptionPane.showMessageDialog(this,
                        "Assessment updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            dispose();
            parentFrame.showPanel("ASSESSMENTS"); // Refresh the assessments panel

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving assessment: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}