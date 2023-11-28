package com.faforever.api.user;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.security.FafRole;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.faforever.api.data.domain.GroupPermission.ROLE_READ_ACCOUNT_PRIVATE_DETAILS;
import static com.faforever.api.data.domain.GroupPermission.ROLE_USER;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MeControllerTest extends AbstractIntegrationTest {
  @Test
  public void withoutTokenUnauthorized() throws Exception {
    mockMvc.perform(get("/me"))
      .andExpect(status().isForbidden());
  }

  @Test
  public void withActiveUserGetResult() throws Exception {
    mockMvc.perform(get("/me")
        .with(getOAuthTokenWithActiveUser(Set.of(), Set.of(ROLE_READ_ACCOUNT_PRIVATE_DETAILS))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.id", is("me")))
      .andExpect(jsonPath("$.data.attributes.userId", is(USERID_ACTIVE_USER)))
      .andExpect(jsonPath("$.data.attributes.userName", is(AUTH_ACTIVE_USER)))
      .andExpect(jsonPath("$.data.attributes.email", is("active-user@faforever.com")))
      .andExpect(jsonPath("$.data.attributes.permissions",
        containsInAnyOrder(
          ROLE_READ_ACCOUNT_PRIVATE_DETAILS, FafRole.ROLE_PREFIX + ROLE_READ_ACCOUNT_PRIVATE_DETAILS,
          ROLE_USER, FafRole.ROLE_PREFIX + ROLE_USER
          )));
  }
}
