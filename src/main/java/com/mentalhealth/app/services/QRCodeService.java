package com.mentalhealth.app.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.mentalhealth.app.models.Event;
import com.mentalhealth.app.models.EventRegistration;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class QRCodeService {

    private static final int QR_WIDTH = 300;
    private static final int QR_HEIGHT = 300;

    /**
     * Generate QR code content string for a registration
     */
    public static String generateQRContent(EventRegistration registration, Event event) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        StringBuilder content = new StringBuilder();
        content.append("===== MENTIS EVENT TICKET =====\n");
        content.append("Confirmation #: REG-").append(String.format("%06d", registration.getId())).append("\n");
        content.append("Event: ").append(event.getTitle()).append("\n");
        content.append("Date: ").append(event.getDateTime().format(dtf)).append("\n");
        content.append("Location: ").append(event.getLocation()).append("\n");
        content.append("--------------------------------\n");
        content.append("Attendee: ").append(registration.getUserName()).append("\n");
        content.append("Email: ").append(registration.getEmail()).append("\n");
        content.append("Ticket: ").append(registration.getTicketType()).append("\n");
        content.append("Qty: ").append(registration.getNumberOfTickets()).append("\n");
        content.append("Status: ").append(registration.getStatus()).append("\n");
        content.append("================================");

        return content.toString();
    }

    /**
     * Generate QR code as BufferedImage
     */
    public static BufferedImage generateQRCodeImage(String content) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 2);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);

        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    /**
     * Generate QR code as JavaFX Image (for displaying in UI)
     */
    public static Image generateQRCodeFXImage(EventRegistration registration, Event event) {
        try {
            String content = generateQRContent(registration, event);
            BufferedImage bufferedImage = generateQRCodeImage(content);
            return SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (WriterException e) {
            System.err.println("Error generating QR code: " + e.getMessage());
            return null;
        }
    }

    /**
     * Generate QR code and save to file
     */
    public static File saveQRCodeToFile(EventRegistration registration, Event event, String directory) {
        try {
            String content = generateQRContent(registration, event);
            BufferedImage qrImage = generateQRCodeImage(content);

            // Create directory if it doesn't exist
            File dir = new File(directory);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = "QR_REG_" + registration.getId() + "_" + System.currentTimeMillis() + ".png";
            File outputFile = new File(dir, fileName);
            ImageIO.write(qrImage, "PNG", outputFile);

            System.out.println("QR Code saved: " + outputFile.getAbsolutePath());
            return outputFile;
        } catch (WriterException | IOException e) {
            System.err.println("Error saving QR code: " + e.getMessage());
            return null;
        }
    }

    /**
     * Generate QR code as byte array (for email attachment or PDF embedding)
     */
    public static byte[] generateQRCodeBytes(EventRegistration registration, Event event) {
        try {
            String content = generateQRContent(registration, event);
            BufferedImage qrImage = generateQRCodeImage(content);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "PNG", baos);
            return baos.toByteArray();
        } catch (WriterException | IOException e) {
            System.err.println("Error generating QR bytes: " + e.getMessage());
            return null;
        }
    }
}
