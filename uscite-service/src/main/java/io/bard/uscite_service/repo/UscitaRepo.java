package io.bard.uscite_service.repo;

import io.bard.uscite_service.model.Uscita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UscitaRepo extends JpaRepository<Uscita, UUID> {

    /**
     * Tutti i ballerini: usati da «tutti i ballerini escono assieme».
     *
     * <p>La tabella {@code ballerino} appartiene a ballerini-service, ma tutti i
     * servizi condividono lo stesso database: la lettura e' diretta.</p>
     */
    @Query(value = "SELECT ballerino_id FROM ballerino", nativeQuery = true)
    List<UUID> findAllBallerinoIds();
}
