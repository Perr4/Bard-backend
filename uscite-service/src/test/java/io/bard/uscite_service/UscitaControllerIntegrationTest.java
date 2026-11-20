package io.bard.uscite_service;

import io.bard.uscite_service.model.Numero;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica le rotte di lettura e scrittura delle uscite.
 *
 * <p>uscite-service ha {@code permitAll} sulle rotte interne (la protezione reale
 * e' al gateway): questi test verificano il comportamento con dati corretti.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UscitaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldListAllUscite() throws Exception {
        mockMvc.perform(get("/api/uscite").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldCreateUscitaWithParticipants() throws Exception {
        UUID ballerinoId = UUID.randomUUID();
        String body = """
                {"data":"2026-11-17","luogo":"Piazza Eleonora, Cagliari","tipologia":"Festa paesana",
                 "partecipazioni":[{"ballerinoId":"%s","numero":"PASSU"}]}
                """.formatted(ballerinoId);

        mockMvc.perform(post("/api/uscite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.luogo").value("Piazza Eleonora, Cagliari"))
                .andExpect(jsonPath("$.data").value("2026-11-17"))
                .andExpect(jsonPath("$.partecipazioni", hasSize(1)))
                .andExpect(jsonPath("$.partecipazioni[0].ballerinoId").value(ballerinoId.toString()))
                .andExpect(jsonPath("$.partecipazioni[0].numero").value("PASSU"));
    }

    @Test
    void shouldRejectUscitaWithoutRequiredFields() throws Exception {
        mockMvc.perform(post("/api/uscite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404ForUnknownUscita() throws Exception {
        mockMvc.perform(get("/api/uscite/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteUscita() throws Exception {
        String body = """
                {"data":"2026-11-17","luogo":"Piazza Eleonora, Cagliari"}
                """;

        String id = mockMvc.perform(post("/api/uscite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()
                .replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(delete("/api/uscite/" + id))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/uscite/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRankTop5ByNumberOfParticipants() throws Exception {
        // un'uscita con un solo partecipante e una con due: la seconda deve prima
        mockMvc.perform(post("/api/uscite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"data":"2026-10-01","luogo":"Sa secondary","partecipazioni":[
                                  {"ballerinoId":"%s","numero":"DANZA"}]}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/uscite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"data":"2026-10-02","luogo":"Sa prima","partecipazioni":[
                                  {"ballerinoId":"%s","numero":"DANZA"},
                                  {"ballerinoId":"%s","numero":"PASSU"}]}
                                """.formatted(UUID.randomUUID(), UUID.randomUUID())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/uscite/top5").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].luogo").value("Sa prima"))
                .andExpect(jsonPath("$[0].partecipazioni", hasSize(2)));
    }
}
