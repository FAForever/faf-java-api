package com.faforever.api.user;


import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.User;
import com.faforever.api.email.EmailSender;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.security.FafTokenService;
import com.faforever.api.security.FafTokenType;
import com.faforever.api.security.OAuthScope;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.MultiValueMap;

import java.time.Duration;

import static junitx.framework.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerTest extends AbstractIntegrationTest {
  private static final String NEW_USER = "newUser";
  private static final String NEW_PASSWORD = "newPassword";
  private static final String NEW_EMAIL = "test@faforever.com";

  @MockBean
  private AnopeUserRepository anopeUserRepository;

  @MockBean
  private EmailSender emailSender;

  @MockBean
  private SteamService steamService;

  @Autowired
  private FafTokenService fafTokenService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private FafApiProperties fafApiProperties;

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
    String token = fafTokenService.createToken(FafTokenType.REGISTRATION,
      Duration.ofSeconds(100),
      ImmutableMap.of(
        UserService.KEY_USERNAME, NEW_USER,
        UserService.KEY_EMAIL, NEW_EMAIL,
        UserService.KEY_PASSWORD, NEW_PASSWORD
      ));

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
  @WithUserDetails(AUTH_USER)
  public void changeEmailWithSuccess() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("currentPassword", AUTH_USER);
    params.add("newEmail", NEW_EMAIL);

    mockMvc.perform(
      post("/users/changeEmail")
        .with(getOAuthToken(OAuthScope._WRITE_ACCOUNT_DATA))
        .params(params))
      .andExpect(status().isOk());

    User user = userRepository.findOneByLoginIgnoreCase(AUTH_USER).get();
    assertEquals(user.getEmail(), NEW_EMAIL);
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void changeEmailWithWrongScope() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("currentPassword", AUTH_USER);
    params.add("newEmail", NEW_EMAIL);

    mockMvc.perform(
      post("/users/changeEmail")
        .params(params))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void changeEmailWithWrongPassword() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("currentPassword", "wrongPassword");
    params.add("newEmail", NEW_EMAIL);

    MvcResult mvcResult = mockMvc.perform(
      post("/users/changeEmail")
        .with(getOAuthToken(OAuthScope._WRITE_ACCOUNT_DATA))
        .params(params))
      .andExpect(status().is4xxClientError())
      .andReturn();

    assertApiError(mvcResult, ErrorCode.EMAIL_CHANGE_FAILED_WRONG_PASSWORD);
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void changeEmailWithInvalidEmail() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("currentPassword", AUTH_USER);
    params.add("newEmail", "invalid-email");

    MvcResult mvcResult = mockMvc.perform(
      post("/users/changeEmail")
        .with(getOAuthToken(OAuthScope._WRITE_ACCOUNT_DATA))
        .params(params))
      .andExpect(status().is4xxClientError())
      .andReturn();

    assertApiError(mvcResult, ErrorCode.EMAIL_INVALID);
  }

  @Test
  @WithAnonymousUser
  public void resetPasswordWithUsername() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("identifier", AUTH_USER);
    params.add("newPassword", NEW_PASSWORD);

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
    params.add("newPassword", NEW_PASSWORD);

    mockMvc.perform(
      post("/users/resetPassword")
        .params(params))
      .andExpect(status().isOk());

    verify(emailSender, times(1)).sendMail(anyString(), anyString(), eq("user@faforever.com"), anyString(), anyString());
  }

  @Test
  @WithAnonymousUser
  public void confirmPasswordReset() throws Exception {
    String token = fafTokenService.createToken(FafTokenType.PASSWORD_RESET,
      Duration.ofSeconds(100),
      ImmutableMap.of(UserService.KEY_USER_ID, String.valueOf(1),
        UserService.KEY_PASSWORD, NEW_PASSWORD));

    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("token", token);

    mockMvc.perform(
      post("/users/confirmPasswordReset")
        .params(params))
      .andExpect(status().isFound())
      .andExpect(redirectedUrl("http://localhost/password_resetted"));
  }

  @Test
  @WithAnonymousUser
  public void buildSteamLinkUrlUnauthorized() throws Exception {
    mockMvc.perform(
      post("/users/buildSteamLinkUrl"))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void buildSteamLinkUrlWithWrongScope() throws Exception {
    mockMvc.perform(
      post("/users/buildSteamLinkUrl"))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void buildSteamLinkUrlAlreadyLinked() throws Exception {
    MvcResult result = mockMvc.perform(
      post("/users/buildSteamLinkUrl")
        .with(getOAuthToken(OAuthScope._WRITE_ACCOUNT_DATA)))
      .andExpect(status().is4xxClientError())
      .andReturn();

    assertApiError(result, ErrorCode.STEAM_ID_UNCHANGEABLE);
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void buildSteamLinkUrl() throws Exception {
    when(steamService.buildLoginUrl(any())).thenReturn("steamUrl");

    mockMvc.perform(
      post("/users/buildSteamLinkUrl")
        .with(getOAuthToken(OAuthScope._WRITE_ACCOUNT_DATA)))
      .andExpect(status().isOk());

    verify(steamService, times(1)).buildLoginUrl(anyString());
  }

  @Test
  @WithAnonymousUser
  public void linkToSteam() throws Exception {
    assertThat(userRepository.findOne(1).getSteamId(), nullValue());

    when(steamService.parseSteamIdFromLoginRedirect(any())).thenReturn("12345");
    when(steamService.ownsForgedAlliance(anyString())).thenReturn(true);

    String token = fafTokenService.createToken(
      FafTokenType.LINK_TO_STEAM,
      Duration.ofSeconds(100),
      ImmutableMap.of(UserService.KEY_USER_ID, "1"));

    mockMvc.perform(
      get(String.format("/users/linkToSteam?token=%s&openid.identity=http://steamcommunity.com/openid/id/12345", token)))
      .andExpect(status().isFound())
      .andExpect(redirectedUrl(fafApiProperties.getLinkToSteam().getSuccessRedirectUrl()));

    assertThat(userRepository.findOne(1).getSteamId(), is("12345"));
  }

  @Test
  @WithAnonymousUser
  public void changeUsernameUnauthorized() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("newUsername", NEW_USER);

    mockMvc.perform(
      post("/users/changeUsername")
        .params(params))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void changeUsernameWithWrongScope() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("newUsername", NEW_USER);

    mockMvc.perform(
      post("/users/changeUsername")
        .params(params))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void changeUsernameSuccess() throws Exception {
    assertThat(userRepository.findOne(1).getLogin(), is(AUTH_USER));

    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("newUsername", NEW_USER);

    mockMvc.perform(
      post("/users/changeUsername")
        .with(getOAuthToken(OAuthScope._WRITE_ACCOUNT_DATA))
        .params(params))
      .andExpect(status().isOk());

    assertThat(userRepository.findOne(1).getLogin(), is(NEW_USER));
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void changeUsernameTooEarly() throws Exception {
    assertThat(userRepository.findOne(2).getLogin(), is(AUTH_MODERATOR));

    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("newUsername", NEW_USER);

    MvcResult result = mockMvc.perform(
      post("/users/changeUsername")
        .with(getOAuthToken(OAuthScope._WRITE_ACCOUNT_DATA))
        .params(params))
      .andExpect(status().is4xxClientError())
      .andReturn();

    assertApiError(result, ErrorCode.USERNAME_CHANGE_TOO_EARLY);

    assertThat(userRepository.findOne(2).getLogin(), is(AUTH_MODERATOR));
  }
}
