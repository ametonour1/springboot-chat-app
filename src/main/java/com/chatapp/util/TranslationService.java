package com.chatapp.util;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class TranslationService {

    private final Map<String, Map<String, String>> translations = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void loadTranslations() throws IOException {
        loadLanguage("en");
        loadLanguage("es");
        loadLanguage("gr");
    }

    private void loadLanguage(String langCode) throws IOException {
        String fileName = "/i18n/" + langCode + ".json";
        try (InputStream is = getClass().getResourceAsStream(fileName)) {
            if (is != null) {
                Map<String, String> map = objectMapper.readValue(is, new TypeReference<>() {});
                translations.put(langCode, map);
            } else {
                System.out.println("Translation file not found: " + fileName);
            }
        }
    }

    public String getTranslation(String langCode, String messageKey) {
        Map<String, String> langMap = translations.get(langCode);
        System.out.println("langcode: " + langCode);

        if (langMap == null) {
            return "Language not supported";
        }
        return langMap.getOrDefault(messageKey, "Message key not found");
    }
}
