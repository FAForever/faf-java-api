package com.faforever.api.ban;

import com.faforever.api.data.domain.User;
import com.faforever.api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BanService {
  private final UserRepository userRepository;

  public boolean hasActiveGlobalBan(User user) {
    return user.isGlobalBanned();
  }

  public boolean hasActiveGlobalBan(String username) {
    return userRepository.findOneByLogin(username)
      .map(this::hasActiveGlobalBan)
      .orElse(false);
  }
}
