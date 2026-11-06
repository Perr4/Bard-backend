package it.chronos.auth;

import it.chronos.auth.model.UserAccount;
import it.chronos.auth.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test di integrazione dell'endpoint interno che espone l'anagrafica di un utente.
 *
 * <p>L'endpoint e' riservato ai servizi interni (oggi ballerini-service) e non
 * richiede autenticazione ({@code /api/auth/internal/**} e' permitAll).</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAccountRepository userRepository;

    @Test
    void shouldExposeAnagraficaByUserId() throws Exception {
        UserAccount user = new UserAccount();
        user.setEmail("sara.rossi@example.it");
        user.setPassword("{noop}segretissimo");
        user.setFirstName("Sara");
        user.setLastName("Rossi");
        user.setDataNascita(Date.valueOf("2000-01-15"));
        UserAccount saved = userRepository.saveAndFlush(user);

        mockMvc.perform(get("/api/auth/internal/users/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(saved.getId().toString()))
                .andExpect(jsonPath("$.firstName").value("Sara"))
                .andExpect(jsonPath("$.lastName").value("Rossi"))
                .andExpect(jsonPath("$.dataNascita").value("2000-01-15"));
    }

    @Test
    void shouldReturn404ForUnknownUser() throws Exception {
        mockMvc.perform(get("/api/auth/internal/users/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
