package com.chatapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

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
}
