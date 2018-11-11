package com.faforever.api.user;

import com.faforever.api.data.domain.LegacyAccessLevel;
import com.faforever.api.data.domain.LobbyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;

@Repository
public interface LobbyGroupRepository extends JpaRepository<LobbyGroup, Integer> {
  Set<LobbyGroup> findAllByAccessLevelIn(Collection<LegacyAccessLevel> accessLevels);
}
