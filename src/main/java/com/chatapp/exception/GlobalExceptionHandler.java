package com.chatapp.exception;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import javax.servlet.http.HttpServletRequest;
import com.chatapp.util.TranslationService;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final TranslationService translationService;

    public GlobalExceptionHandler(TranslationService translationService) {
        this.translationService = translationService;
    }

@ExceptionHandler(BaseLocalizedException.class)
public ResponseEntity<Map<String, String>> handleLocalizedException(
        BaseLocalizedException ex,
        HttpServletRequest request
) {
      String lang = request.getHeader("X-Language");

    if (lang == null || lang.isBlank()) {
        // Try Accept-Language header
        String acceptLang = request.getHeader("Accept-Language");
        if (acceptLang != null && !acceptLang.isBlank()) {
            // Extract the first language code (e.g., "en-US,en;q=0.9" → "en")
            lang = acceptLang.split(",")[0].split("-")[0];
        } else {
            lang = "en"; // fallback default
        }
    }
    String translated = translationService.getTranslation(lang, ex.getMessageKey());
    return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(Map.of(
                    "message", translated,
                    "key", ex.getMessageKey()
            ));
}

    // Optional: handle other general errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleOtherExceptions(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Unexpected error", "details", ex.getMessage()));
    }
}
