package com.faforever.api.user;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Repository to access Anope's @{NickCore} table (the one that contains usernames and passwords).
 */
@Repository
@Slf4j
public class AnopeUserRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public AnopeUserRepository(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  // Don't make this package private, see https://jira.spring.io/browse/SPR-15911
  public void updatePassword(String username, String password) {
    jdbcTemplate.update("UPDATE `faf-anope`.anope_db_NickCore SET pass = :password WHERE display = :username",
      ImmutableMap.of(
        "password", password,
        "username", username
      ));
  }
}
