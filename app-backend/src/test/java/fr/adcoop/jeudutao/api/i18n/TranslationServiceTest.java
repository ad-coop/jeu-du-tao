package fr.adcoop.jeudutao.api.i18n;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TranslationServiceTest {

    private final TranslationService service = new TranslationService();

    @Test
    void getTranslations_ofFr_returnsAllKeys() {
        Map<String, String> translations = service.getTranslations("fr");

        assertThat(translations).isNotEmpty();
        assertThat(translations).containsKey("landing.title");
    }

    @Test
    void getTranslations_ofUnsupportedLocale_returnsEmpty() {
        Map<String, String> translations = service.getTranslations("zh");

        assertThat(translations).isEmpty();
    }
}
