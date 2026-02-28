package uniearn.services.users;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class EmailService {

    //  Gmail + App Password
    private static final String SENDER_EMAIL = "i.ferjani.26@gmail.com";
    private static final String SENDER_PASSWORD = "vhdc lkos auhr dgns";

    public void sendPasswordResetEmail(String toEmail, String resetToken) throws MessagingException, UnsupportedEncodingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SENDER_EMAIL, "UniEarn"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("🔐 Réinitialisation de votre mot de passe UniEarn");

        // HTML email body
        String htmlBody = buildEmailBody(resetToken);
        message.setContent(htmlBody, "text/html; charset=utf-8");

        Transport.send(message);
        System.out.println("✓ Reset email sent to: " + toEmail);
    }

    private String buildEmailBody(String token) {
        return """
            <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 480px; margin: auto; padding: 32px; background: #ffffff; border-radius: 12px; border: 1px solid #e2e8f0;">
                <div style="text-align:center; margin-bottom: 24px;">
                    <h1 style="color: #2563EB; font-size: 28px; margin: 0;">🎓 UniEarn</h1>
                    <p style="color: #94a3b8; font-size: 13px; margin: 4px 0 0;">Plateforme de freelancing étudiant</p>
                </div>
                <h2 style="color: #0f172a; font-size: 20px;">Réinitialisation du mot de passe</h2>
                <p style="color: #475569;">Votre code de réinitialisation est :</p>
                <div style="background: #f1f5f9; border-radius: 10px; padding: 20px; text-align: center; margin: 24px 0;">
                    <span style="font-size: 36px; font-weight: bold; letter-spacing: 10px; color: #2563EB;">%s</span>
                </div>
                <p style="color: #64748b; font-size: 13px;">Ce code expire dans <strong>15 minutes</strong>. Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.</p>
                <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 24px 0;"/>
                <p style="color: #94a3b8; font-size: 12px; text-align: center;">© 2026 UniEarn — Ne répondez pas à cet email</p>
            </div>
            """.formatted(token);
    }
}