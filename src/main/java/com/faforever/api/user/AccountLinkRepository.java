package com.faforever.api.user;

import com.faforever.api.data.domain.AccountLink;
import com.faforever.api.data.domain.LinkedServiceType;
import com.faforever.api.data.domain.Login;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountLinkRepository extends JpaRepository<AccountLink, String> {
  Optional<AccountLink> findOneByServiceIdAndServiceType(String serviceId, LinkedServiceType serviceType);

  boolean existsByUserAndServiceType(Login login, LinkedServiceType serviceType);
}
