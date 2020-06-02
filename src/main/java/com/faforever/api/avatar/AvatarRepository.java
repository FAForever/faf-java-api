package com.faforever.api.avatar;

import com.faforever.api.data.domain.Avatar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AvatarRepository extends JpaRepository<Avatar, Integer> {
  Optional<Avatar> findOneByFilename(String filename);
  Optional<Avatar> findById(Integer id);
}
