package it.chronos.auth.repository;

import it.chronos.auth.model.UserRole;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    Optional<UserRole> findByName(String name);
}
