package com.faforever.api.user;

import com.faforever.api.data.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

  User findOneByLoginIgnoreCase(String login);
}
