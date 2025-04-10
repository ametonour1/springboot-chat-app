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


}
