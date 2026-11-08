package io.bard.learning_service;

import io.bard.common.model.Presenza;
import io.bard.learning_service.repository.PresenzaRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.sql.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@EntityScan(basePackages = {"io.bard.learning_service.model", "io.bard.common.model"})
class PresenzaRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private PresenzaRepo repo;

    @Test
    void savesPresenzaWithoutBallerinoId() {
        Presenza p = new Presenza();
        p.setDate(Date.valueOf("2025-01-10"));
        // ballerinoId lasciato null (drift risolto: campo nullable)
        Presenza saved = repo.save(p);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getBallerinoId()).isNull();
    }

    @Test
    void savesPresenzaWithBallerinoId() {
        UUID ballerinoId = UUID.randomUUID();
        Presenza p = new Presenza();
        p.setDate(Date.valueOf("2025-01-11"));
        p.setBallerinoId(ballerinoId);
        Presenza saved = repo.save(p);
        assertThat(saved.getBallerinoId()).isEqualTo(ballerinoId);
    }
}
