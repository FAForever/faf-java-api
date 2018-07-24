package com.faforever.api.login;

import com.faforever.api.AbstractIntegrationTest;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LoginTest extends AbstractIntegrationTest {
  private MultiValueMap<String, String> params;

  @Override
  @Before
  public void setUp() {
    super.setUp();
    params = new LinkedMultiValueMap<>();
    params.add("grant_type", "password");
    params.add("client_id", "test");
    params.add("client_secret", "test");
    params.add("username", "MODERATOR");
    params.add("password", "MODERATOR");
  }

  @Test
  public void retrieveModeratorToken() throws Exception {
    performOAuthTokenRequest(params)
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.access_token", is(notNullValue())));
  }

  @Test
  public void wrongUserName() throws Exception {
    params.set("username", "invalid_username");
    performOAuthTokenRequest(params)
      .andExpect(status().isBadRequest());
  }

  @Test
  public void wrongPassword() throws Exception {
    params.set("password", "invalid_password");
    performOAuthTokenRequest(params)
      .andExpect(status().isBadRequest());
  }

  @Test
  public void invalidClientSecret() throws Exception {
    params.set("client_secret", "invalid_client_secret");
    performOAuthTokenRequest(params)
      .andExpect(status().isUnauthorized());
  }

  @Test
  public void invalidClientId() throws Exception {
    params.set("client_id", "invalid_client_id");
    performOAuthTokenRequest(params)
      .andExpect(status().isUnauthorized());
  }

  @NotNull
  private ResultActions performOAuthTokenRequest(MultiValueMap<String, String> params) throws Exception {
    return mockMvc.perform(
      post("/oauth/token")
        .params(params));
  }
}
