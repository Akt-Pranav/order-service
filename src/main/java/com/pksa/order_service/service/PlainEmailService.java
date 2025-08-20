package com.pksa.order_service.service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlainEmailService {
private final JavaMailSender mailSender;
public void sendWithAttachment(String to, String subject, String text, byte[] attachment, String filename)
        throws MessagingException {

    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

    helper.setTo(to);
    helper.setSubject(subject != null ? subject : "Admin Report");
    helper.setText(text != null ? text : "Please find the requested report attached.", false);
    helper.addAttachment(filename, new ByteArrayResource(attachment));

    mailSender.send(message);
}


}
