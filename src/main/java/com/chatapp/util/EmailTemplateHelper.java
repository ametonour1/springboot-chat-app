package com.chatapp.util;

import com.chatapp.model.User;
import com.chatapp.util.TranslationService;

import java.util.Map;

public class EmailTemplateHelper {

    public static Map<String, Object> buildVerificationEmailContent(User user, String verificationLink, String lang, TranslationService translationService) {
        return Map.of(
            "greeting", translationService.getTranslation(lang, "email.verify.greeting").replace("{username}", user.getUsername()),
            "instruction", translationService.getTranslation(lang, "email.verify.instruction"),
            "buttonText", translationService.getTranslation(lang, "email.verify.button"),
            "footerNote", translationService.getTranslation(lang, "email.verify.footerNote"),
            "verificationLink", verificationLink
        );
    }

    public static String getVerificationEmailSubject(String lang, TranslationService translationService) {
        return translationService.getTranslation(lang, "email.verify.subject");
    }

       public static Map<String, Object> buildPasswordResetEmailContent(User user, String resetLink, String lang, TranslationService translationService) {
        return Map.of(
            "username", user.getUsername(),
            "resetLink", resetLink,
            "greeting", translationService.getTranslation(lang, "email.reset.greeting").replace("{username}", user.getUsername()),
            "instruction", translationService.getTranslation(lang, "email.reset.instruction"),
            "buttonText", translationService.getTranslation(lang, "email.reset.button"),
            "footerNote", translationService.getTranslation(lang, "email.reset.footerNote")
        );
    }

    public static String getPasswordResetEmailSubject(String lang, TranslationService translationService) {
        return translationService.getTranslation(lang, "email.reset.subject");
    }
}