package fr.adcoop.jeudutao.infra.web.i18n;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/i18n")
public class I18nController {

    private final TranslationService translationService;

    public I18nController(TranslationService translationService) {
        this.translationService = translationService;
    }

    @GetMapping("/{locale}")
    public ResponseEntity<Map<String, String>> getTranslations(@PathVariable("locale") String locale) {
        var translations = translationService.getTranslations(locale);
        if (translations.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(translations);
    }
}
