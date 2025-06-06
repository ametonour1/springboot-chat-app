package com.chatapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.chatapp.util.TranslationService;

@RestController
@RequestMapping("/api/test")
public class TestController {

     private final TranslationService translationService;

    public TestController(TranslationService translationService) {
        this.translationService = translationService;
    }

    @GetMapping("/public/message")
    public String getMessage(@RequestParam String lang, @RequestParam String key) {
        String translatedMessage = translationService.getTranslation(lang, key);
        System.out.println("Translated message: " + translatedMessage);
        return translatedMessage;
    }

    @GetMapping("/public")
    public ResponseEntity<String> publicEndpoint() {
        return ResponseEntity.ok("This is a public endpoint.");
    }

    @GetMapping("/private")
    public ResponseEntity<String> privateEndpoint(Authentication authentication) {
        String username = authentication.getName();
        Object details = authentication.getDetails();

        if (details != null) {
            Long userId = (Long) details;
            return ResponseEntity.ok("Hello, " + username + "! Your userId is " + userId + " and you accessed a secured endpoint.");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User ID not available.");
        }
    }
}
