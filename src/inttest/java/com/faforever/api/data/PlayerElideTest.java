package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.data.domain.GroupPermission;
import com.faforever.api.player.PlayerRepository;
import com.faforever.api.security.OAuthScope;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultUser.sql")
public class PlayerElideTest extends AbstractIntegrationTest {

  @Autowired
  PlayerRepository playerRepository;

  @Test
  public void restrictedResultWithoutScope() throws Exception {
    mockMvc.perform(get("/data/player")
      .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_READ_ACCOUNT_PRIVATE_DETAILS)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(5)))
      .andExpect(jsonPath("$.data[0].attributes.login", is("USER")))
      .andExpect(jsonPath("$.data[1].attributes.login", is("MODERATOR")))
      .andExpect(jsonPath("$.data[2].attributes.login", is("ADMIN")))
      .andExpect(jsonPath("$.data[3].attributes.login", is("BANNED")))
      .andExpect(jsonPath("$.data[4].attributes.login", is("ACTIVE_USER")))
      .andExpect(jsonPath("$.data[0].attributes", not(hasKey("email"))))
      .andExpect(jsonPath("$.data[1].attributes", not(hasKey("email"))))
      .andExpect(jsonPath("$.data[2].attributes", not(hasKey("email"))))
      .andExpect(jsonPath("$.data[3].attributes", not(hasKey("email"))))
      // you are allowed to see your own stuff
      .andExpect(jsonPath("$.data[4].attributes.email", is("active-user@faforever.com")))
      .andExpect(jsonPath("$.data[0].attributes", not(hasKey("password"))))
      .andExpect(jsonPath("$.data[1].attributes", not(hasKey("password"))))
      .andExpect(jsonPath("$.data[2].attributes", not(hasKey("password"))))
      .andExpect(jsonPath("$.data[3].attributes", not(hasKey("password"))))
      // nobody can see passwords!
      .andExpect(jsonPath("$.data[4].attributes", not(hasKey("password"))))
      .andExpect(jsonPath("$.data[0].attributes", not(hasKey("recentIpAddress"))))
      .andExpect(jsonPath("$.data[1].attributes", not(hasKey("recentIpAddress"))))
      .andExpect(jsonPath("$.data[2].attributes", not(hasKey("recentIpAddress"))))
      .andExpect(jsonPath("$.data[3].attributes", not(hasKey("recentIpAddress"))))
      // you are allowed to see your own stuff
      .andExpect(jsonPath("$.data[4].attributes.recentIpAddress", is("127.0.0.1")));


    mockMvc.perform(get("/data/player?filter=email==active-user@faforever.com")
      .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_READ_ACCOUNT_PRIVATE_DETAILS)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(1)));

    mockMvc.perform(get("/data/player?filter=email==user@faforever.com")
      .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_READ_ACCOUNT_PRIVATE_DETAILS)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(0)));
  }

  @Test
  public void restrictedResultWithoutRole() throws Exception {
    mockMvc.perform(get("/data/player")
      .with(getOAuthTokenWithTestUser(OAuthScope._READ_SENSIBLE_USERDATA, NO_AUTHORITIES)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(5)))
      .andExpect(jsonPath("$.data[0].attributes.login", is("USER")))
      .andExpect(jsonPath("$.data[1].attributes.login", is("MODERATOR")))
      .andExpect(jsonPath("$.data[2].attributes.login", is("ADMIN")))
      .andExpect(jsonPath("$.data[3].attributes.login", is("BANNED")))
      .andExpect(jsonPath("$.data[4].attributes.login", is("ACTIVE_USER")))
      .andExpect(jsonPath("$.data[0].attributes", not(hasKey("email"))))
      .andExpect(jsonPath("$.data[1].attributes", not(hasKey("email"))))
      .andExpect(jsonPath("$.data[2].attributes", not(hasKey("email"))))
      .andExpect(jsonPath("$.data[3].attributes", not(hasKey("email"))))
      // you are allowed to see your own stuff
      .andExpect(jsonPath("$.data[4].attributes.email", is("active-user@faforever.com")))
      .andExpect(jsonPath("$.data[0].attributes", not(hasKey("password"))))
      .andExpect(jsonPath("$.data[1].attributes", not(hasKey("password"))))
      .andExpect(jsonPath("$.data[2].attributes", not(hasKey("password"))))
      .andExpect(jsonPath("$.data[3].attributes", not(hasKey("password"))))
      // nobody can see passwords!
      .andExpect(jsonPath("$.data[4].attributes", not(hasKey("password"))))
      .andExpect(jsonPath("$.data[0].attributes", not(hasKey("recentIpAddress"))))
      .andExpect(jsonPath("$.data[1].attributes", not(hasKey("recentIpAddress"))))
      .andExpect(jsonPath("$.data[2].attributes", not(hasKey("recentIpAddress"))))
      .andExpect(jsonPath("$.data[3].attributes", not(hasKey("recentIpAddress"))))
      // you are allowed to see your own stuff
      .andExpect(jsonPath("$.data[4].attributes.recentIpAddress", is("127.0.0.1")));


    mockMvc.perform(get("/data/player?filter=email==active-user@faforever.com")
      .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_READ_ACCOUNT_PRIVATE_DETAILS)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(1)));

    mockMvc.perform(get("/data/player?filter=email==user@faforever.com")
      .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_READ_ACCOUNT_PRIVATE_DETAILS)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(0)));
  }

  @Test
  public void canSeePrivateDetailsWithScopeAndRole() throws Exception {
    mockMvc.perform(get("/data/player")
      .with(getOAuthTokenWithTestUser(OAuthScope._READ_SENSIBLE_USERDATA, GroupPermission.ROLE_READ_ACCOUNT_PRIVATE_DETAILS)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(5)))
      .andExpect(jsonPath("$.data[0].attributes.login", is("USER")))
      .andExpect(jsonPath("$.data[1].attributes.login", is("MODERATOR")))
      .andExpect(jsonPath("$.data[2].attributes.login", is("ADMIN")))
      .andExpect(jsonPath("$.data[3].attributes.login", is("BANNED")))
      .andExpect(jsonPath("$.data[4].attributes.login", is("ACTIVE_USER")))
      .andExpect(jsonPath("$.data[0].attributes", hasKey("email")))
      .andExpect(jsonPath("$.data[1].attributes", hasKey("email")))
      .andExpect(jsonPath("$.data[2].attributes", hasKey("email")))
      .andExpect(jsonPath("$.data[3].attributes", hasKey("email")))
      .andExpect(jsonPath("$.data[4].attributes", hasKey("email")))
      // nobody can see passwords!
      .andExpect(jsonPath("$.data[0].attributes", not(hasKey("password"))))
      .andExpect(jsonPath("$.data[1].attributes", not(hasKey("password"))))
      .andExpect(jsonPath("$.data[2].attributes", not(hasKey("password"))))
      .andExpect(jsonPath("$.data[3].attributes", not(hasKey("password"))))
      .andExpect(jsonPath("$.data[4].attributes", not(hasKey("password"))))
      .andExpect(jsonPath("$.data[0].attributes", hasKey("recentIpAddress")))
      .andExpect(jsonPath("$.data[1].attributes", hasKey("recentIpAddress")))
      .andExpect(jsonPath("$.data[2].attributes", hasKey("recentIpAddress")))
      .andExpect(jsonPath("$.data[3].attributes", hasKey("recentIpAddress")))
      .andExpect(jsonPath("$.data[4].attributes", hasKey("recentIpAddress")));

    mockMvc.perform(get("/data/player?filter=email==active-user@faforever.com")
      .with(getOAuthTokenWithTestUser(OAuthScope._READ_SENSIBLE_USERDATA, GroupPermission.ROLE_READ_ACCOUNT_PRIVATE_DETAILS)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(1)));

    mockMvc.perform(get("/data/player?filter=email==user@faforever.com")
      .with(getOAuthTokenWithTestUser(OAuthScope._READ_SENSIBLE_USERDATA, GroupPermission.ROLE_READ_ACCOUNT_PRIVATE_DETAILS)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(1)));
  }
}
