package fr.adcoop.jeudutao.infra.web.i18n;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@Service
public class TranslationService {

    public Map<String, String> getTranslations(String locale) {
        try {
            var bundle = ResourceBundle.getBundle("messages", Locale.forLanguageTag(locale));
            var translations = new HashMap<String, String>();
            bundle.getKeys().asIterator().forEachRemaining(key -> translations.put(key, bundle.getString(key)));
            return Collections.unmodifiableMap(translations);
        } catch (MissingResourceException e) {
            return Map.of();
        }
    }
}
