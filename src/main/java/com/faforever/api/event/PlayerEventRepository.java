package com.faforever.api.event;

import com.faforever.api.data.domain.PlayerEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerEventRepository extends JpaRepository<PlayerEvent, String> {

  Optional<PlayerEvent> findOneByEventIdAndPlayerId(String eventId, int playerId);
}
