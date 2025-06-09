package com.chatapp.service;

import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.scheduling.annotation.Async;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    // Send a simple text email
    public void sendSimpleEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("onour.amet.develop@gmail.com"); 
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        try {
            javaMailSender.send(message);
            System.out.println("Email sent successfully.");
        } catch (MailException e) {
            e.printStackTrace(); // You can handle the exception however you like
        }
    }
    @Async
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> templateVariables) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
    
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(to);
            helper.setSubject(subject);
    
            // Create a context and set template variables from the map
            Context context = new Context();
            context.setVariables(templateVariables);
    
            // Process the template with the variables
            String htmlContent = templateEngine.process(templateName, context);
            helper.setText(htmlContent, true); // true = HTML content
    
            // Send the email
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace(); // Handle the error appropriately (e.g., log it)
        }
    }
}
