package com.chatapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatapp.service.EmailService;

@RestController
public class EmailController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/sendTestEmail")
    public String sendTestEmail() {
        emailService.sendSimpleEmail("onurewrenos@gmail.com", "Test Subject", "This is a test email.");
        return "Email sent!";
    }

    @GetMapping("/sendTemplateEmail")
    public String sendTemplateEmail() {
     emailService.sendHtmlEmail(
        "onurewrenos@gmail.com",
        "Welcome to the platform!",
        "Hey John ",
        "Thanks for joining. We’re excited to have you!"
    );
    return "Email sent!";
}
}
