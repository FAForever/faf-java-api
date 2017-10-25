package com.faforever.api.user;


import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.data.domain.User;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.security.OAuthScope;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.util.MultiValueMap;

import java.util.Collections;

import static junitx.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerTest extends AbstractIntegrationTest {
  @MockBean
  private AnopeUserRepository anopeUserRepository;

  @Autowired
  private UserRepository userRepository;

  @Test
  @WithUserDetails(AUTH_USER)
  public void changePasswordWithSuccess() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("currentPassword", AUTH_USER);
    params.add("newPassword", "newPassword");

    RequestPostProcessor oauthToken = oAuthHelper.addBearerToken(Sets.newHashSet(OAuthScope._WRITE_ACCOUNT_DATA));
    mockMvc.perform(post("/users/changePassword").with(oauthToken).params(params))
      .andExpect(status().isOk());

    User user = userRepository.findOneByLoginIgnoreCase(AUTH_USER).get();
    assertEquals(user.getPassword(), "5c29a959abce4eda5f0e7a4e7ea53dce4fa0f0abbe8eaa63717e2fed5f193d31");
    verify(anopeUserRepository, times(1)).updatePassword(eq(AUTH_USER), anyString());
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void changePasswordWithWrongScope() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("currentPassword", AUTH_USER);
    params.add("newPassword", "newPassword");

    RequestPostProcessor oauthToken = oAuthHelper.addBearerToken(Collections.emptySet());
    mockMvc.perform(post("/users/changePassword").with(oauthToken).params(params))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void changePasswordWithWrongPassword() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("currentPassword", "wrongPassword");
    params.add("newPassword", "newPassword");

    RequestPostProcessor oauthToken = oAuthHelper.addBearerToken(Sets.newHashSet(OAuthScope._WRITE_ACCOUNT_DATA));
    MvcResult mvcResult = mockMvc.perform(post("/users/changePassword").with(oauthToken).params(params))
      .andExpect(status().is4xxClientError())
      .andReturn();

    assertApiError(mvcResult, ErrorCode.PASSWORD_CHANGE_FAILED_WRONG_PASSWORD);
  }
}
