package com.faforever.api.email;

import com.faforever.api.data.domain.DomainBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DomainBlacklistRepository extends JpaRepository<DomainBlacklist, Integer> {
  boolean existsByDomain(String domain);
}
