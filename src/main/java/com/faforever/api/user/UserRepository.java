package com.faforever.api.user;

import com.faforever.api.data.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
  Optional<User> findOneByLogin(String login);

  Optional<User> findOneByEmail(String email);

  Optional<User> findOneBySteamId(String steamId);

  Optional<User> findOneByLoginOrEmail(String login, String email);

  Optional<User> findOneByGogId(String gogId);

  boolean existsByEmail(String email);

  boolean existsByLogin(String login);
}
