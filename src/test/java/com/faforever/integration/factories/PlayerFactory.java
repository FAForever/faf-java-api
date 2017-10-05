package com.faforever.integration.factories;

import com.faforever.api.data.domain.Player;
import com.faforever.api.data.domain.User;
import com.faforever.integration.TestDatabase;
import lombok.Builder;
import org.springframework.util.Assert;

import static org.junit.Assert.assertEquals;

public class PlayerFactory {
  private static final String DEFAULT_USERNAME = "JUnitTestUser_PlayerFactory";
  private static final String DEFAULT_PASSWORD = "testPassword";

  @Builder
  public static Player create(String login, String password, TestDatabase database) throws Exception {
    Assert.notNull(database, "'database' must not be null");
    User user = (User) new User()
      .setPassword(password != null ? password : DEFAULT_PASSWORD)
      .setLogin(login != null ? login : DEFAULT_USERNAME)
      .setEmail(login + "@junit-test.org");
    long count = database.getUserRepository().count();
    database.getUserRepository().save(user);
    assertEquals(count + 1, database.getUserRepository().count());
    return database.getPlayerRepository().findOne(user.getId());
  }
}
