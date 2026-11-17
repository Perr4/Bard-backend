package io.bard.learning_service;

import io.bard.learning_service.dto.UserCreate;
import io.bard.learning_service.service.AuthCliente;
import io.bard.learning_service.service.AuthCliente.RegisterResponse;
import io.bard.learning_service.service.AuthCliente.UserAnagrafica;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test di integrazione della creazione del profilo ballerino.
 *
 * <p>Il client Feign verso auth-service e' mocked: i test verificano la validazione
 * dell'input, la composizione del DTO e il collegamento tra utente e profilo, non la
 * disponibilita' del servizio remoto.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BallerinoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthCliente authCliente;

    @Test
    void shouldCreateBallerinoWithComposedAnagrafica() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authCliente.register(any(UserCreate.class)))
                .thenReturn(new RegisterResponse(userId, "sara@example.it", "Sara Rossi", List.of("STUDENT")));
        when(authCliente.findAnagrafica(userId))
                .thenReturn(new UserAnagrafica(userId, "Sara", "Rossi", Date.valueOf("2000-01-15")));

        String body = """
                {"user":{"email":"sara@example.it","password":"segretissimo","firstName":"Sara","lastName":"Rossi","dataNascita":"2000-01-15"},"sezione":"Ballo","sesso":"F"}
                """;

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
                // eta' calcolata dal servizio dalla data di nascita
                .andExpect(jsonPath("$.eta").value(26))
                .andExpect(jsonPath("$.sezione").value("Ballo"));
    }

    @Test
    void shouldRejectCreateWithoutUser() throws Exception {
        String body = """
                {"sezione":"Ballo","sesso":"F"}
                """;

        mockMvc.perform(post("/api/ballerino")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectCreateWhenUserCannotBeCreated() throws Exception {
        when(authCliente.register(any(UserCreate.class))).thenReturn(null);

        String body = """
                {"user":{"email":"sara@example.it","password":"segretissimo","firstName":"Sara","lastName":"Rossi","dataNascita":"2000-01-15"},"sezione":"Ballo"}
                """;

        mockMvc.perform(post("/api/ballerino")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
