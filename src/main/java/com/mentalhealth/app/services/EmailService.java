package com.mentalhealth.app.services;

import com.mentalhealth.app.models.Event;
import com.mentalhealth.app.models.EventRegistration;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class EmailService {

    // ⚠️ CONFIGURE THESE WITH YOUR GMAIL CREDENTIALS
    // For Gmail, you need to use an "App Password" (not your regular password)
    // Go to: Google Account → Security → 2-Step Verification → App Passwords
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_FROM = "your-email@gmail.com";  // ← Change this
    private static final String EMAIL_PASSWORD = "your-app-password"; // ← Change this (App Password, not regular password)

    /**
     * Send confirmation email with QR code attachment
     */
    public static boolean sendConfirmationEmail(EventRegistration registration, Event event) {
        try {
            // Setup mail properties
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.trust", SMTP_HOST);

            // Create session with authentication
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                }
            });

            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM, "MENTIS Events"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(registration.getEmail()));
            message.setSubject("🎟 Registration Confirmed: " + event.getTitle());

            // Create multipart message (HTML + attachment)
            Multipart multipart = new MimeMultipart();

            // Part 1: HTML content
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(buildEmailHTML(registration, event), "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);

            // Part 2: QR Code attachment
            byte[] qrCodeBytes = QRCodeService.generateQRCodeBytes(registration, event);
            if (qrCodeBytes != null) {
                MimeBodyPart qrPart = new MimeBodyPart();
                DataSource dataSource = new ByteArrayDataSource(qrCodeBytes, "image/png");
                qrPart.setDataHandler(new DataHandler(dataSource));
                qrPart.setFileName("ticket_qr_code.png");
                qrPart.setHeader("Content-ID", "<qrcode>");
                multipart.addBodyPart(qrPart);
            }

            message.setContent(multipart);

            // Send email
            Transport.send(message);
            System.out.println("✅ Confirmation email sent to: " + registration.getEmail());
            return true;

        } catch (Exception e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Build beautiful HTML email content
     */
    private static String buildEmailHTML(EventRegistration registration, Event event) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' HH:mm");
        String confirmationNumber = String.format("REG-%06d", registration.getId());

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f5f5f5; margin: 0; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 15px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #2F5D52 0%%, #3E6F64 100%%); padding: 30px; text-align: center; }
                    .header h1 { color: #ffffff; margin: 0; font-size: 28px; }
                    .header p { color: #9BC7B5; margin: 10px 0 0 0; }
                    .content { padding: 30px; }
                    .success-badge { background: #9BC7B5; color: #2F5D52; padding: 10px 20px; border-radius: 20px; display: inline-block; font-weight: bold; margin-bottom: 20px; }
                    .event-card { background: #F1F6F4; border-radius: 10px; padding: 20px; margin: 20px 0; border-left: 4px solid #2F5D52; }
                    .event-card h2 { color: #2F5D52; margin: 0 0 15px 0; }
                    .detail-row { display: flex; margin: 10px 0; }
                    .detail-label { color: #6B7280; width: 120px; }
                    .detail-value { color: #1E1E1E; font-weight: 500; }
                    .ticket-info { background: #2F5D52; color: white; padding: 20px; border-radius: 10px; margin: 20px 0; }
                    .ticket-info h3 { margin: 0 0 15px 0; }
                    .confirmation-number { font-size: 24px; font-weight: bold; color: #9BC7B5; }
                    .qr-section { text-align: center; padding: 20px; background: #F1F6F4; border-radius: 10px; margin: 20px 0; }
                    .qr-section p { color: #6B7280; margin: 10px 0; }
                    .footer { background: #F1F6F4; padding: 20px; text-align: center; color: #6B7280; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Registration Confirmed!</h1>
                        <p>MENTIS Mental Health Platform</p>
                    </div>
                    
                    <div class="content">
                        <span class="success-badge">✅ CONFIRMED</span>
                        
                        <p>Dear <strong>%s</strong>,</p>
                        <p>Your registration has been successfully confirmed. Please find your ticket details below.</p>
                        
                        <div class="event-card">
                            <h2>📌 %s</h2>
                            <div class="detail-row">
                                <span class="detail-label">📅 Date & Time:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">📍 Location:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">🏷 Event Type:</span>
                                <span class="detail-value">%s</span>
                            </div>
                        </div>
                        
                        <div class="ticket-info">
                            <h3>🎟 Your Ticket</h3>
                            <div class="detail-row">
                                <span class="detail-label" style="color:#9BC7B5;">Confirmation #:</span>
                                <span class="confirmation-number">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label" style="color:#9BC7B5;">Ticket Type:</span>
                                <span class="detail-value" style="color:white;">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label" style="color:#9BC7B5;">Quantity:</span>
                                <span class="detail-value" style="color:white;">%d ticket(s)</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label" style="color:#9BC7B5;">Total Price:</span>
                                <span class="detail-value" style="color:white;">%s</span>
                            </div>
                        </div>
                        
                        <div class="qr-section">
                            <h3>📱 Your QR Code</h3>
                            <p>Present this QR code at the event entrance for quick check-in.</p>
                            <p><strong>QR code is attached to this email.</strong></p>
                        </div>
                        
                        <p>If you have any questions, please don't hesitate to contact us.</p>
                        <p>See you at the event! 🎉</p>
                    </div>
                    
                    <div class="footer">
                        <p>© 2024 MENTIS - Mental Health Platform</p>
                        <p>This is an automated message. Please do not reply directly to this email.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                registration.getUserName(),
                event.getTitle(),
                event.getDateTime().format(dtf),
                event.getLocation(),
                event.getEventType(),
                confirmationNumber,
                registration.getTicketType(),
                registration.getNumberOfTickets(),
                registration.isFreeTicket() ? "FREE" : String.format("$%.2f", registration.getTotalPrice())
        );
    }

    /**
     * Test email configuration
     */
    public static boolean testConnection() {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                }
            });

            Transport transport = session.getTransport("smtp");
            transport.connect();
            transport.close();
            System.out.println("✅ Email configuration is valid!");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Email configuration error: " + e.getMessage());
            return false;
        }
    }
}