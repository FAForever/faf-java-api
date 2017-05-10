package com.faforever.api.user;

import com.faforever.api.data.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
  User findOneByLoginIgnoreCase(String login);

  User findOneByEmailIgnoreCase(String email);

  boolean existsByEmailIgnoreCase(String email);

  boolean existsByLoginIgnoreCase(String login);
}
