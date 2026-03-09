package fr.adcoop.jeudutao.api.i18n;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class I18nControllerTest {

    private MockMvc mockMvc;
    private TranslationService translationService;

    @BeforeEach
    void setUp() {
        translationService = mock(TranslationService.class);
        reset(translationService);
        mockMvc = MockMvcBuilders.standaloneSetup(new I18nController(translationService)).build();
    }

    @Test
    void getTranslations_ofFr_returnsJsonMap() throws Exception {
        when(translationService.getTranslations("fr"))
                .thenReturn(Map.of("landing.title", "Le Jeu du Tao"));

        mockMvc.perform(get("/api/i18n/fr"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$['landing.title']").value("Le Jeu du Tao"));
    }

    @Test
    void getTranslations_ofUnknownLocale_returns404() throws Exception {
        when(translationService.getTranslations("zh")).thenReturn(Map.of());

        mockMvc.perform(get("/api/i18n/zh"))
                .andExpect(status().isNotFound());
    }
}
