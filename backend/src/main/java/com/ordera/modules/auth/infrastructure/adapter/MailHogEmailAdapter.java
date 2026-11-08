package com.ordera.modules.auth.infrastructure.adapter;

import com.ordera.modules.auth.application.port.EmailSenderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Adapter gửi email qua MailHog (Mock SMTP cổng 1025) phục vụ kiểm thử và phát triển.
 */
@Slf4j
@Component
public class MailHogEmailAdapter implements EmailSenderPort {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    public MailHogEmailAdapter(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    @Override
    public void sendEmail(String toRecipient, String subject, String content) {
        log.info("[MailHog SMTP] Sending Email to [{}], Subject: [{}], Body: [{}]", toRecipient, subject, content);
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("no-reply@ordera.no");
                message.setTo(toRecipient);
                message.setSubject(subject);
                message.setText(content);
                mailSender.send(message);
                log.info("[MailHog SMTP] Successfully dispatched email to [{}]", toRecipient);
            } catch (Exception e) {
                log.warn("[MailHog SMTP] Failed to send email via SMTP, logged as fallback: {}", e.getMessage());
            }
        } else {
            log.debug("[MailHog SMTP] JavaMailSender bean not present, email logged only.");
        }
    }
}
