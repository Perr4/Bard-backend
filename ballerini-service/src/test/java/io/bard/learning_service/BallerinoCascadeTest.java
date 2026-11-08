package io.bard.learning_service;

import io.bard.common.model.Presenza;
import io.bard.common.model.Sezione;
import io.bard.learning_service.model.Ballerino;
import io.bard.learning_service.repository.BalleriniRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@EntityScan(basePackages = {"io.bard.learning_service.model", "io.bard.common.model"})
class BallerinoCascadeTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BalleriniRepo repo;

    @Test
    void cascadesPresenzeOnSave() {
        Ballerino b = new Ballerino();
        b.setUserId(UUID.randomUUID());
        b.setEta(20);
        b.setSezione(Sezione.GRUPPO);
        b.setSesso("F");

        Presenza p1 = new Presenza();
        p1.setDate(Date.valueOf("2025-02-01"));
        Presenza p2 = new Presenza();
        p2.setDate(Date.valueOf("2025-02-02"));
        b.setPresenze(List.of(p1, p2));

        Ballerino saved = repo.saveAndFlush(b);
        em.clear();

        Ballerino reloaded = repo.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getPresenze()).hasSize(2);
    }
}
