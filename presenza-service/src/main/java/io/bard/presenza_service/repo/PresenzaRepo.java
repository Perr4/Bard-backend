package io.bard.presenza_service.repo;

import io.bard.presenza_service.model.Presenza;
import jakarta.ws.rs.QueryParam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PresenzaRepo  extends JpaRepository<Presenza, UUID> {
    @Query("SELECT p FROM Presenza p WHERE ballerino_id = :id")
    List<Presenza> findAllByBallerinoId(@QueryParam("id") UUID id);
}
