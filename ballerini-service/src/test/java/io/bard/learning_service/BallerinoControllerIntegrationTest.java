package io.bard.learning_service;

import io.bard.learning_service.config.AuthServiceClient;
import io.bard.learning_service.config.AuthServiceClient.UserAnagrafica;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test di integrazione della creazione del profilo ballerino.
 *
 * <p>Il client verso auth-service e' mocked: i test verificano la validazione
 * dell'input e la composizione del DTO, non la disponibilita' del servizio remoto.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BallerinoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthServiceClient authServiceClient;

    @Test
    void shouldCreateBallerinoWithComposedAnagrafica() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authServiceClient.findAnagrafica(userId))
                .thenReturn(new UserAnagrafica(userId, "Sara", "Rossi", Date.valueOf("2000-01-15")));

        String body = """
                {"userId":"%s","eta":25,"sezione":"GRUPPO","sesso":"F"}
                """.formatted(userId);

        mockMvc.perform(post("/api/ballerino")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.firstName").value("Sara"))
                .andExpect(jsonPath("$.lastName").value("Rossi"))
                // il formato dipende dal mapper HTTP (Jackson 3 in Spring 7):
                // si verifica la data senza dipendere dal formato esatto
                .andExpect(jsonPath("$.dataNascita").value(containsString("2000-01-15")))
                .andExpect(jsonPath("$.eta").value(25))
                .andExpect(jsonPath("$.sezione").value("GRUPPO"));
    }

    @Test
    void shouldRejectCreateWithoutUserId() throws Exception {
        String body = """
                {"eta":25,"sezione":"GRUPPO"}
                """;

        mockMvc.perform(post("/api/ballerino")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectCreateWithUnknownUserId() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authServiceClient.findAnagrafica(userId)).thenReturn(null);

        String body = """
                {"userId":"%s","eta":25}
                """.formatted(userId);

        mockMvc.perform(post("/api/ballerino")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
