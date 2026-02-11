package services;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.Random;

public class EmailService {

    // IMPORTANT: Update these with your email credentials
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_FROM = "elladridi96@gmail.com"; // ⬅️ CHANGE THIS
    private static final String EMAIL_PASSWORD = "jxgd pxrc clck zpcd"; // ⬅️ CHANGE THIS

    public static String generateVerificationCode() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    public static boolean sendVerificationCode(String toEmail, String code) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Mentis - Password Reset Code");
            message.setContent(getEmailHTML(code), "text/html");

            Transport.send(message);
            System.out.println("✅ Email sent to: " + toEmail);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Email error: " + e.getMessage());
            return false;
        }
    }

    private static String getEmailHTML(String code) {
        return "<html><body style='font-family:Arial;padding:20px;background:#f4f4f4'>" +
                "<div style='max-width:600px;margin:auto;background:white;border-radius:10px;box-shadow:0 2px 10px rgba(0,0,0,0.1)'>" +
                "<div style='background:linear-gradient(135deg,#588b71,#a0d4b8);color:white;padding:30px;text-align:center'>" +
                "<h1>🧠 Mentis</h1></div><div style='padding:40px'><h2>Password Reset</h2>" +
                "<p>Your verification code is:</p>" +
                "<div style='background:#f0f8f4;border:2px dashed #588b71;border-radius:8px;padding:20px;text-align:center;margin:20px 0'>" +
                "<span style='font-size:36px;font-weight:bold;color:#588b71;letter-spacing:5px'>" + code + "</span></div>" +
                "<p><strong>Expires in 10 minutes</strong></p>" +
                "<p style='color:#d9534f'>⚠️ Never share this code</p></div></div></body></html>";
    }
}