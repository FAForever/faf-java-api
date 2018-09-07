package com.faforever.api.user;

import com.faforever.api.data.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
  Optional<User> findOneByLoginIgnoreCase(String login);

  Optional<User> findOneByEmailIgnoreCase(String email);

  Optional<User> findOneBySteamIdIgnoreCase(String steamId);

  boolean existsByEmailIgnoreCase(String email);

  boolean existsByLoginIgnoreCase(String login);
}
