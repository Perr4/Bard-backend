package io.bard.presenza_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica che la rotta di lettura di tutte le presenze esista (non 405).
 *
 * <p>presenza-service ha {@code permitAll} sulle rotte interne: il test verifica
 * solo il mapping della rotta.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class PresenzaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldListAllPresenze() throws Exception {
        mockMvc.perform(get("/api/presenza").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
