package uniearn.services.users.mail;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import uniearn.server.security.SecurityCallbackServer;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class EmailService {

    //  Gmail + App Password
    private static final String SENDER_EMAIL = "i.ferjani.26@gmail.com";
    private static final String SENDER_PASSWORD = "vhdc lkos auhr dgns";

    // ── Shared session builder (avoid duplication) ───────────

    private Session buildSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });
    }


    public void sendPasswordResetEmail(String toEmail, String resetToken) throws MessagingException, UnsupportedEncodingException {
        Session session = buildSession();

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

    // ── Intruder alert with photo + IP + action buttons ──────────────

    public void sendIntruderAlert(String toEmail, String ownerName, String ipAddress,
                                  File capturedPhoto, String confirmToken, String lockToken)
            throws MessagingException, IOException {

        String timestamp  = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm:ss"));
        String confirmUrl = "http://localhost:" + SecurityCallbackServer.PORT
                + "/confirm?token=" + confirmToken;
        String lockUrl    = "http://localhost:" + SecurityCallbackServer.PORT
                + "/lockme?token="  + lockToken;

        Message message = new MimeMessage(buildSession());
        message.setFrom(new InternetAddress(SENDER_EMAIL, "UniEarn Security"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("🚨 Tentative de connexion suspecte — UniEarn");

        boolean hasPhoto = capturedPhoto != null && capturedPhoto.exists();

        if (hasPhoto) {
            // Multipart: HTML body + inline webcam photo
            MimeMultipart mp = new MimeMultipart("related");

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(
                    buildAlertBody(ownerName, toEmail, timestamp, ipAddress,
                            confirmUrl, lockUrl, true),
                    "text/html; charset=utf-8");
            mp.addBodyPart(htmlPart);

            MimeBodyPart imgPart = new MimeBodyPart();
            imgPart.attachFile(capturedPhoto);
            imgPart.setContentID("<intruder_photo>");
            imgPart.setDisposition(MimeBodyPart.INLINE);
            mp.addBodyPart(imgPart);

            message.setContent(mp);
        } else {
            // No webcam — send HTML-only
            message.setContent(
                    buildAlertBody(ownerName, toEmail, timestamp, ipAddress,
                            confirmUrl, lockUrl, false),
                    "text/html; charset=utf-8");
        }

        Transport.send(message);
        System.out.println("✓ Intruder alert sent to: " + toEmail);
    }

    private String buildAlertBody(String ownerName, String email, String timestamp,
                                  String ip, String confirmUrl, String lockUrl,
                                  boolean hasPhoto) {

        String photoBlock = hasPhoto
                ? "<div style='margin:24px 0;text-align:center'>"
                + "<p style='color:#64748b;font-size:13px;margin-bottom:10px'>📸 Photo capturée au moment de la tentative :</p>"
                + "<img src='cid:intruder_photo' style='max-width:100%;border-radius:12px;"
                + "border:3px solid #fca5a5;box-shadow:0 4px 12px rgba(0,0,0,.1)' alt='Photo intrus'/>"
                + "</div>"
                : "<div style='background:#fef9c3;border-radius:8px;padding:12px;margin:16px 0;"
                + "border-left:4px solid #f59e0b'>"
                + "<p style='color:#92400e;font-size:13px;margin:0'>"
                + "⚠ Aucune webcam disponible — photo non capturée.</p></div>";

        return
                // outer wrapper
                "<div style='font-family:Segoe UI,Arial,sans-serif;max-width:560px;margin:auto;"
                        + "background:#f8fafc;padding:20px'>"
                        + "<div style='background:#fff;border-radius:16px;overflow:hidden;"
                        + "box-shadow:0 4px 24px rgba(0,0,0,.08)'>"

                        // red header band
                        + "<div style='background:linear-gradient(135deg,#dc2626,#991b1b);padding:28px 32px'>"
                        + "<h1 style='color:#fff;font-size:22px;margin:0'>🚨 Alerte de Sécurité UniEarn</h1>"
                        + "<p style='color:rgba(255,255,255,.8);font-size:13px;margin:6px 0 0'>"
                        + "Tentative de connexion suspecte détectée</p>"
                        + "</div>"

                        // body
                        + "<div style='padding:32px'>"
                        + "<p style='color:#0f172a;font-size:16px'>Bonjour <strong>" + ownerName + "</strong>,</p>"
                        + "<p style='color:#475569;font-size:14px;line-height:1.6'>"
                        + "Quelqu'un a tenté de se connecter à votre compte avec un mauvais mot de passe "
                        + "<strong style='color:#dc2626'>3 fois de suite</strong>.</p>"

                        // details table
                        + "<div style='background:#f8fafc;border-radius:10px;border:1px solid #e2e8f0;"
                        + "overflow:hidden;margin:20px 0'>"
                        + "<table style='width:100%;border-collapse:collapse;font-size:13px'>"
                        + "<tr style='border-bottom:1px solid #e2e8f0'>"
                        + "<td style='padding:12px 16px;color:#64748b;font-weight:600;width:42%'>📅 Date &amp; Heure</td>"
                        + "<td style='padding:12px 16px;color:#0f172a;font-weight:bold'>" + timestamp + "</td></tr>"
                        + "<tr style='border-bottom:1px solid #e2e8f0'>"
                        + "<td style='padding:12px 16px;color:#64748b;font-weight:600'>🌐 Adresse IP</td>"
                        + "<td style='padding:12px 16px;color:#0f172a;font-weight:bold;font-family:monospace'>" + ip + "</td></tr>"
                        + "<tr>"
                        + "<td style='padding:12px 16px;color:#64748b;font-weight:600'>📧 Compte ciblé</td>"
                        + "<td style='padding:12px 16px;color:#0f172a;font-weight:bold'>" + email + "</td></tr>"
                        + "</table></div>"

                        // photo or warning
                        + photoBlock

                        // question banner
                        + "<div style='background:#fffbeb;border:1px solid #fde68a;border-radius:12px;"
                        + "padding:20px;margin:24px 0;text-align:center'>"
                        + "<p style='color:#92400e;font-weight:bold;font-size:16px;margin:0 0 6px'>❓ Était-ce vous ?</p>"
                        + "<p style='color:#78350f;font-size:13px;margin:0'>"
                        + "Cliquez sur le bouton qui correspond à votre situation.</p>"
                        + "</div>"

                        // action buttons — using a table for email client compatibility
                        + "<table style='width:100%;border-collapse:collapse;margin-bottom:24px'><tr>"

                        // YES button
                        + "<td style='width:50%;padding-right:8px'>"
                        + "<a href='" + confirmUrl + "' "
                        + "style='display:block;text-align:center;padding:16px 12px;"
                        + "background:linear-gradient(135deg,#16a34a,#15803d);"
                        + "color:#fff;text-decoration:none;border-radius:12px;"
                        + "font-weight:bold;font-size:14px;"
                        + "box-shadow:0 4px 12px rgba(22,163,74,.35)'>"
                        + "✅ Oui, c'est moi"
                        + "<br><span style='font-size:11px;font-weight:normal;opacity:.9'>"
                        + "Réinitialiser mon mot de passe</span></a></td>"

                        // NO button
                        + "<td style='width:50%;padding-left:8px'>"
                        + "<a href='" + lockUrl + "' "
                        + "style='display:block;text-align:center;padding:16px 12px;"
                        + "background:linear-gradient(135deg,#dc2626,#b91c1c);"
                        + "color:#fff;text-decoration:none;border-radius:12px;"
                        + "font-weight:bold;font-size:14px;"
                        + "box-shadow:0 4px 12px rgba(220,38,38,.35)'>"
                        + "🚫 Non, ce n'est pas moi"
                        + "<br><span style='font-size:11px;font-weight:normal;opacity:.9'>"
                        + "Verrouiller mon compte 15 min</span></a></td>"

                        + "</tr></table>"

                        // important note
                        + "<div style='background:#f1f5f9;border-radius:8px;padding:14px'>"
                        + "<p style='color:#64748b;font-size:12px;margin:0;line-height:1.6'>"
                        + "⚠ <strong>Important :</strong>"
                        + "Si vous ne reconnaissez pas cette tentative, verrouillez immédiatement "
                        + "et changez votre mot de passe.</p>"
                        + "</div>"

                        + "</div></div>" // /body-padding /card
                        + "<p style='text-align:center;color:#94a3b8;font-size:11px;margin-top:16px'>"
                        + "© 2026 UniEarn — Ne répondez pas à cet email</p>"
                        + "</div>"; // /wrapper
    }
}