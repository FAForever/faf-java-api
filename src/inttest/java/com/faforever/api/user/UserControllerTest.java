package com.faforever.api.user;


import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.data.domain.User;
import com.faforever.api.email.EmailSender;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.security.OAuthScope;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.MultiValueMap;

import static junitx.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerTest extends AbstractIntegrationTest {
  public static final String NEW_USER = "newUser";
  public static final String NEW_PASSWORD = "newPassword";
  public static final String NEW_EMAIL = "test@faforever.com";

  @MockBean
  private AnopeUserRepository anopeUserRepository;

  @MockBean
  private EmailSender emailSender;

  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  @Test
  @WithAnonymousUser
  public void registerWithSuccess() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("username", NEW_USER);
    params.add("email", NEW_EMAIL);
    params.add("password", NEW_PASSWORD);

    mockMvc.perform(post("/users/register").params(params))
      .andExpect(status().isOk());

    verify(emailSender, times(1)).sendMail(anyString(), anyString(), eq(NEW_EMAIL), anyString(), anyString());
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void registerWithAuthentication() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("username", NEW_USER);
    params.add("email", NEW_EMAIL);
    params.add("password", NEW_PASSWORD);

    MvcResult result = mockMvc.perform(post("/users/register").params(params))
      .andExpect(status().is4xxClientError())
      .andReturn();

    assertApiError(result, ErrorCode.ALREADY_REGISTERED);
  }

  @Test
  @WithAnonymousUser
  public void activateWithSuccess() throws Exception {
    String token = userService.createRegistrationToken(NEW_USER, NEW_EMAIL, NEW_PASSWORD);

    mockMvc.perform(get("/users/activate?token=" + token))
      .andExpect(status().isFound())
      .andExpect(redirectedUrl("http://localhost/account_activated"));
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void changePasswordWithSuccess() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("currentPassword", AUTH_USER);
    params.add("newPassword", NEW_PASSWORD);

    mockMvc.perform(
      post("/users/changePassword")
        .with(getOAuthToken(OAuthScope._WRITE_ACCOUNT_DATA))
        .params(params))
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
    params.add("newPassword", NEW_PASSWORD);

    mockMvc.perform(
      post("/users/changePassword")
        .params(params))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void changePasswordWithWrongPassword() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("currentPassword", "wrongPassword");
    params.add("newPassword", NEW_PASSWORD);

    MvcResult mvcResult = mockMvc.perform(
      post("/users/changePassword")
        .with(getOAuthToken(OAuthScope._WRITE_ACCOUNT_DATA))
        .params(params))
      .andExpect(status().is4xxClientError())
      .andReturn();

    assertApiError(mvcResult, ErrorCode.PASSWORD_CHANGE_FAILED_WRONG_PASSWORD);
  }

  @Test
  @WithAnonymousUser
  public void resetPasswordWithUsername() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("identifier", AUTH_USER);

    mockMvc.perform(
      post("/users/resetPassword")
        .params(params))
      .andExpect(status().isOk());

    verify(emailSender, times(1)).sendMail(anyString(), anyString(), eq("user@faforever.com"), anyString(), anyString());
  }

  @Test
  @WithAnonymousUser
  public void resetPasswordWithEmail() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("identifier", "user@faforever.com");

    mockMvc.perform(
      post("/users/resetPassword")
        .params(params))
      .andExpect(status().isOk());

    verify(emailSender, times(1)).sendMail(anyString(), anyString(), eq("user@faforever.com"), anyString(), anyString());
  }

  @Test
  @WithAnonymousUser
  public void confirmPasswordReset() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("token", userService.createPasswordResetToken(1));
    params.add("newPassword", NEW_PASSWORD);

    mockMvc.perform(
      post("/users/confirmPasswordReset")
        .params(params))
      .andExpect(status().isFound())
      .andExpect(redirectedUrl("http://localhost/password_resetted"));
  }
}
