package com.faforever.api.coturn;

import com.faforever.api.data.domain.CoturnServer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoturnServerRepository extends JpaRepository<CoturnServer, Integer> {
}
