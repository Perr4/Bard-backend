package io.bard.learning_service.repository;

import io.bard.common.model.Presenza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PresenzaRepo extends JpaRepository<Presenza, UUID> {
}
