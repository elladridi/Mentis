package utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import models.Session;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class QRCodeGenerator {

    private static final int QR_CODE_SIZE = 300;

    public static String generateQRCode(Session session, int patientId) {
        try {
            // Create QR code data (JSON-like string)
            String qrData = String.format(
                    "Session ID: %d\nTitle: %s\nDate: %s\nTime: %s - %s\nLocation: %s\nType: %s\nPatient ID: %d",
                    session.getSessionId(),
                    session.getTitle(),
                    session.getSessionDate().toString(),
                    session.getStartTime().toString(),
                    session.getEndTime().toString(),
                    session.getLocation(),
                    session.getSessionType(),
                    patientId
            );

            // Set QR code parameters
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    qrData,
                    BarcodeFormat.QR_CODE,
                    QR_CODE_SIZE,
                    QR_CODE_SIZE,
                    hints
            );

            // Create directory if it doesn't exist
            String directory = "qr_codes";
            Path dirPath = FileSystems.getDefault().getPath(directory);
            if (!java.nio.file.Files.exists(dirPath)) {
                java.nio.file.Files.createDirectories(dirPath);
            }

            // Save QR code as PNG
            String fileName = "session_" + session.getSessionId() + "_patient_" + patientId + ".png";
            Path filePath = FileSystems.getDefault().getPath(directory, fileName);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", filePath);

            return filePath.toString();

        } catch (WriterException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] getQRCodeBytes(Session session, int patientId) {
        try {
            String qrData = String.format(
                    "Session ID: %d\nTitle: %s\nDate: %s\nTime: %s - %s\nLocation: %s\nType: %s\nPatient ID: %d",
                    session.getSessionId(),
                    session.getTitle(),
                    session.getSessionDate().toString(),
                    session.getStartTime().toString(),
                    session.getEndTime().toString(),
                    session.getLocation(),
                    session.getSessionType(),
                    patientId
            );

            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    qrData,
                    BarcodeFormat.QR_CODE,
                    QR_CODE_SIZE,
                    QR_CODE_SIZE,
                    hints
            );

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return outputStream.toByteArray();

        } catch (WriterException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}