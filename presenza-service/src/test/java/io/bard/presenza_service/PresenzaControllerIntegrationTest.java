package io.bard.presenza_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica le rotte di lettura e scrittura delle presenze.
 *
 * <p>presenza-service ha {@code permitAll} sulle rotte interne: i test verificano
 * solo il mapping delle rotte.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PresenzaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldListAllPresenze() throws Exception {
        mockMvc.perform(get("/api/presenza").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCreatePresenzaKeepingBallerinoId() throws Exception {
        UUID ballerinoId = UUID.randomUUID();
        String body = """
                {"date":"2026-11-17","ballerino_id":"%s"}
                """.formatted(ballerinoId);

        mockMvc.perform(post("/api/presenza")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ballerino_id").value(ballerinoId.toString()))
                .andExpect(jsonPath("$.date").value("2026-11-17"));
    }
}
