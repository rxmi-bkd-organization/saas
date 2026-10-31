package org.bkd.saas.user.db;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<AppUserEntity, UUID> {
  Optional<AppUserEntity> findByEmail(String email);
}
