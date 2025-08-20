package com.pksa.order_service.service;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendBeautifulReportEmail(String to, String subject, String duration, 
                                       String format, byte[] reportData, String filename,
                                       Integer totalOrders, Integer uniqueCustomers, 
                                       Double totalRevenue) throws MessagingException {
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        // Prepare template variables
        Context context = new Context();
        context.setVariable("duration", duration);
        context.setVariable("format", format);
        context.setVariable("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        context.setVariable("fileSize", formatFileSize(reportData.length));
        context.setVariable("totalOrders", totalOrders);
        context.setVariable("uniqueCustomers", uniqueCustomers);
        context.setVariable("totalRevenue", totalRevenue != null ? String.format("%.2f", totalRevenue) : null);
        
        // Process the HTML template
        String htmlContent = templateEngine.process("email/admin-report-email", context);
        
        // Set email properties
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // true = HTML content
        
        // Attach the report file
        helper.addAttachment(filename, new ByteArrayResource(reportData));
        
        mailSender.send(message);
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        return (bytes / (1024 * 1024)) + " MB";
    }
}
