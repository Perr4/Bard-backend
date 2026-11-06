package io.bard.learning_service.repository;

import io.bard.learning_service.model.Ballerino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BalleriniRepo extends JpaRepository<Ballerino, UUID> {
}
