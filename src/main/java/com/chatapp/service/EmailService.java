package com.chatapp.service;

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

    public void sendHtmlEmail(String to, String subject, String title, String message) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(to);
            helper.setSubject(subject);

            Context context = new Context();
            context.setVariable("title", title);
            context.setVariable("message", message);

            String htmlContent = templateEngine.process("TestTemplate", context);
            helper.setText(htmlContent, true); // true = HTML

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace(); // or log the error
        }
    }
}
