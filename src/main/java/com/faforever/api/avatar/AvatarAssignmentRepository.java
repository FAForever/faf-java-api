package com.faforever.api.avatar;

import com.faforever.api.data.domain.Avatar;
import com.faforever.api.data.domain.AvatarAssignment;
import com.faforever.api.data.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AvatarAssignmentRepository extends JpaRepository<AvatarAssignment, Integer> {
  Optional<AvatarAssignment> findOneByAvatarAndPlayer(Avatar avatar, Player player);

  Optional<AvatarAssignment> findOneByAvatarIdAndPlayerId(int avatarId, int playerId);

  Optional<AvatarAssignment> findOneById(Integer i);
}
