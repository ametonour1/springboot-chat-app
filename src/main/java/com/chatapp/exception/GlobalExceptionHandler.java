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
    String lang = request.getHeader("Accept-Language");
    if (lang == null || lang.isBlank()) {
        lang = "en"; // default language
    }
    String translated = translationService.getTranslation(lang, ex.getMessageKey());
    return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(Map.of(
                    "error", translated,
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
