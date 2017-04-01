package com.faforever.api.mod;

import com.faforever.api.data.domain.ModVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModVersionRepository extends JpaRepository<ModVersion, Integer> {
}
