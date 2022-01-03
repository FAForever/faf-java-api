package com.faforever.api.user;


import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.data.domain.GroupPermission;
import com.faforever.api.data.domain.LinkedServiceType;
import com.faforever.api.data.domain.User;
import com.faforever.api.email.EmailSender;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.security.FafTokenService;
import com.faforever.api.security.FafTokenType;
import com.faforever.api.security.OAuthScope;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.MultiValueMap;

import java.time.Duration;
import java.util.Map;

import static junitx.framework.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UsersControllerTest extends AbstractIntegrationTest {
  private static final String NEW_USER = "newUser";
  private static final String NEW_PASSWORD = "newPassword";
  private static final String NEW_EMAIL = "test@faforever.com";

  @MockBean
  private AnopeUserRepository anopeUserRepository;

  @MockBean
  private EmailSender emailSender;

  @MockBean
  private SteamService steamService;

  @MockBean
  private GogService gogService;

  @Autowired
  private FafTokenService fafTokenService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private AccountLinkRepository accountLinkRepository;

  @Test
  public void registerWithSuccess() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("username", NEW_USER);
    params.add("email", NEW_EMAIL);

    mockMvc.perform(post("/users/register")
      .params(params)
    ).andExpect(status().isOk());

    verify(emailSender, times(1)).sendMail(anyString(), anyString(), eq(NEW_EMAIL), anyString(), anyString());
  }

  @Test
  public void registerWithAuthentication() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("username", NEW_USER);
    params.add("email", NEW_EMAIL);
    params.add("password", NEW_PASSWORD);

    MvcResult result = mockMvc.perform(post("/users/register")
      .with(getOAuthTokenForUserId(USERID_USER))
      .params(params)
    ).andExpect(status().is4xxClientError())
      .andReturn();

    assertApiError(result, ErrorCode.ALREADY_REGISTERED);
  }

  @Test
  @WithAnonymousUser
  public void activateWithSuccess() throws Exception {
    String token = fafTokenService.createToken(FafTokenType.REGISTRATION,
      Duration.ofSeconds(100),
      Map.of(
        UserService.KEY_USERNAME, NEW_USER,
        UserService.KEY_EMAIL, NEW_EMAIL
      ));

    mockMvc.perform(
      post("/users/activate")
        .param("token", token)
        .param("password", NEW_PASSWORD))
      .andExpect(status().isOk());
  }

  @Test
  public void changePasswordWithSuccess() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("currentPassword", AUTH_USER);
    params.add("newPassword", NEW_PASSWORD);

    mockMvc.perform(
      post("/users/changePassword")
        .with(getOAuthTokenForUserId(USERID_USER, OAuthScope._WRITE_ACCOUNT_DATA))
        .params(params))
      .andExpect(status().isOk());

    User user = userRepository.getById(USERID_USER);
    assertEquals(user.getPassword(), "5c29a959abce4eda5f0e7a4e7ea53dce4fa0f0abbe8eaa63717e2fed5f193d31");
    verify(anopeUserRepository, times(1)).updatePassword(eq(AUTH_USER), anyString());
  }

  @Test
  public void changePasswordWithoutScope() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("currentPassword", AUTH_USER);
    params.add("newPassword", NEW_PASSWORD);

    mockMvc.perform(
      post("/users/changePassword")
        .with(getOAuthTokenForUserId(USERID_USER, NO_SCOPE))
        .params(params))
      .andExpect(status().isForbidden());
  }

  @Test
  public void changePasswordWithWrongPassword() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("currentPassword", "wrongPassword");
    params.add("newPassword", NEW_PASSWORD);

    MvcResult mvcResult = mockMvc.perform(
      post("/users/changePassword")
        .with(getOAuthTokenForUserId(USERID_USER, OAuthScope._WRITE_ACCOUNT_DATA))
        .params(params))
      .andExpect(status().is4xxClientError())
      .andReturn();

    assertApiError(mvcResult, ErrorCode.PASSWORD_CHANGE_FAILED_WRONG_PASSWORD);
  }

  @Test
  public void changeEmailWithSuccess() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("currentPassword", AUTH_USER);
    params.add("newEmail", NEW_EMAIL);

    mockMvc.perform(
      post("/users/changeEmail")
        .with(getOAuthTokenForUserId(USERID_USER, OAuthScope._WRITE_ACCOUNT_DATA))
        .params(params))
      .andExpect(status().isOk());

    User user = userRepository.findOneByLogin(AUTH_USER).get();
    assertEquals(user.getEmail(), NEW_EMAIL);
  }

  @Test
  public void changeEmailWithoutScope() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("currentPassword", AUTH_USER);
    params.add("newEmail", NEW_EMAIL);

    mockMvc.perform(
      post("/users/changeEmail")
        .with(getOAuthTokenForUserId(USERID_USER, NO_SCOPE))
        .params(params))
      .andExpect(status().isForbidden());
  }

  @Test
  public void changeEmailWithWrongPassword() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("currentPassword", "wrongPassword");
    params.add("newEmail", NEW_EMAIL);

    MvcResult mvcResult = mockMvc.perform(
      post("/users/changeEmail")
        .with(getOAuthTokenForUserId(USERID_USER, OAuthScope._WRITE_ACCOUNT_DATA))
        .params(params))
      .andExpect(status().is4xxClientError())
      .andReturn();

    assertApiError(mvcResult, ErrorCode.EMAIL_CHANGE_FAILED_WRONG_PASSWORD);
  }

  @Test
  public void changeEmailWithInvalidEmail() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("currentPassword", AUTH_USER);
    params.add("newEmail", "invalid-email");

    MvcResult mvcResult = mockMvc.perform(
      post("/users/changeEmail")
        .with(getOAuthTokenForUserId(USERID_USER, OAuthScope._WRITE_ACCOUNT_DATA))
        .params(params))
      .andExpect(status().is4xxClientError())
      .andReturn();

    assertApiError(mvcResult, ErrorCode.EMAIL_INVALID);
  }

  @Test
  public void resetPasswordWithUsername() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("identifier", AUTH_USER);

    mockMvc.perform(
      post("/users/requestPasswordReset")
        .params(params))
      .andExpect(status().isOk());

    verify(emailSender, times(1)).sendMail(anyString(), anyString(), eq("user@faforever.com"), anyString(), anyString());
  }

  @Test
  public void resetPasswordWithEmail() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("identifier", "user@faforever.com");

    mockMvc.perform(
      post("/users/requestPasswordReset")
        .params(params))
      .andExpect(status().isOk());

    verify(emailSender, times(1)).sendMail(anyString(), anyString(), eq("user@faforever.com"), anyString(), anyString());
  }

  @Test
  public void performPasswordReset() throws Exception {
    String token = fafTokenService.createToken(FafTokenType.PASSWORD_RESET,
      Duration.ofSeconds(100),
      Map.of(UserService.KEY_USER_ID, String.valueOf(1)));

    mockMvc.perform(
      post("/users/performPasswordReset")
        .param("token", token)
        .param("newPassword", NEW_PASSWORD))
      .andExpect(status().isOk());
  }

  @Test
  public void buildSteamLinkUrlUnauthorized() throws Exception {
    mockMvc.perform(
      post("/users/buildSteamLinkUrl?callbackUrl=foo"))
      .andExpect(status().isForbidden());
  }

  @Test
  public void buildSteamLinkUrlWithWrongScope() throws Exception {
    mockMvc.perform(
      post("/users/buildSteamLinkUrl?callbackUrl=foo"))
      .andExpect(status().isForbidden());
  }

  @Test
  public void buildSteamLinkUrlAlreadyLinked() throws Exception {
    MvcResult result = mockMvc.perform(
      post("/users/buildSteamLinkUrl?callbackUrl=foo")
        .with(getOAuthTokenForUserId(USERID_MODERATOR, OAuthScope._WRITE_ACCOUNT_DATA)))
      .andExpect(status().is4xxClientError())
      .andReturn();

    assertApiError(result, ErrorCode.STEAM_ID_UNCHANGEABLE);
  }

  @Test
  public void buildSteamLinkUrl() throws Exception {
    when(steamService.buildLoginUrl(any())).thenReturn("steamUrl");

    mockMvc.perform(
      post("/users/buildSteamLinkUrl?callbackUrl=foo")
        .with(getOAuthTokenForUserId(USERID_USER, OAuthScope._WRITE_ACCOUNT_DATA)))
      .andExpect(status().isOk());

    verify(steamService, times(1)).buildLoginUrl(anyString());
  }

  @Test
  public void linkToSteam() throws Exception {
    String steamId = "12345";

    assertThat(accountLinkRepository.findOneByServiceIdAndServiceType(steamId, LinkedServiceType.STEAM).isEmpty(), is(true));

    String callbackUrl = "http://faforever.com";
    String token = fafTokenService.createToken(
      FafTokenType.LINK_TO_STEAM,
      Duration.ofSeconds(100),
      Map.of(
        UserService.KEY_USER_ID, "1",
        UserService.KEY_STEAM_CALLBACK_URL, callbackUrl
      ));

    when(steamService.parseSteamIdFromLoginRedirect(any())).thenReturn(steamId);
    when(steamService.ownsForgedAlliance(anyString())).thenReturn(true);

    mockMvc.perform(
      get(String.format("/users/linkToSteam?callbackUrl=%s&token=%s&openid.identity=http://steamcommunity.com/openid/id/%s", callbackUrl, token, steamId)))
      .andExpect(status().isFound())
      .andExpect(redirectedUrl(callbackUrl));

    assertThat(accountLinkRepository.findOneByServiceIdAndServiceType(steamId, LinkedServiceType.STEAM).get().getUser().getId(), is(1));
  }

  @Test
  public void linkToSteamAlreadyLinkedAccount() throws Exception {
    String steamId = "1234";
    User userThatOwnsSteamId = accountLinkRepository.findOneByServiceIdAndServiceType(steamId, LinkedServiceType.STEAM).get().getUser();
    assertThat(userThatOwnsSteamId.getId(), is(2));

    String callbackUrl = "http://faforever.com";
    String token = fafTokenService.createToken(
      FafTokenType.LINK_TO_STEAM,
      Duration.ofSeconds(100),
      Map.of(
        UserService.KEY_USER_ID, "1",
        UserService.KEY_STEAM_CALLBACK_URL, callbackUrl
      ));

    when(steamService.parseSteamIdFromLoginRedirect(any())).thenReturn(steamId);
    when(steamService.ownsForgedAlliance(anyString())).thenReturn(true);

    mockMvc.perform(
      get(String.format("/users/linkToSteam?callbackUrl=%s&token=%s&openid.identity=http://steamcommunity.com/openid/id/%s", callbackUrl, token, steamId)))
      .andExpect(status().isFound())
      .andExpect(redirectedUrlPattern(callbackUrl + "?errors=*" + ErrorCode.STEAM_ID_ALREADY_LINKED.getCode() + "*" + userThatOwnsSteamId.getLogin() + "*"));
    //We expect and error with code STEAM_ID_ALREADY_LINKED and that the error message contains the user that this steam account was linked to already which is MODERATOR with id 2

    assertThat(accountLinkRepository.existsByUserAndServiceType(userRepository.getReferenceById(1), LinkedServiceType.STEAM), is(false));
  }

  @Test
  public void changeUsernameUnauthorized() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("newUsername", NEW_USER);

    mockMvc.perform(
      post("/users/changeUsername")
        .params(params))
      .andExpect(status().isForbidden());
  }

  @Test
  public void changeUsernameWithWrongScope() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("newUsername", NEW_USER);

    mockMvc.perform(
      post("/users/changeUsername")
        .with(getOAuthTokenForUserId(USERID_USER, NO_SCOPE))
        .params(params))
      .andExpect(status().isForbidden());
  }

  @Test
  public void changeUsernameSuccess() throws Exception {
    assertThat(userRepository.getById(1).getLogin(), is(AUTH_USER));

    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("newUsername", NEW_USER);

    mockMvc.perform(
      post("/users/changeUsername")
        .with(getOAuthTokenForUserId(USERID_USER, OAuthScope._WRITE_ACCOUNT_DATA))
        .params(params))
      .andExpect(status().isOk());

    assertThat(userRepository.getById(1).getLogin(), is(NEW_USER));
  }

  @Test
  public void changeUsernameForcedByUser() throws Exception {
    assertThat(userRepository.getById(1).getLogin(), is(AUTH_USER));

    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("newUsername", NEW_USER);

    mockMvc.perform(
      post("/users/1/forceChangeUsername")
        .with(getOAuthTokenForUserId(USERID_USER, OAuthScope._WRITE_ACCOUNT_DATA))
        .params(params))
      .andExpect(status().is4xxClientError())
      .andReturn();

    assertThat(userRepository.getById(1).getLogin(), is(not(NEW_USER)));
  }

  @Test
  public void changeUsernameForcedByModerator() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("newUsername", NEW_USER);

    mockMvc.perform(
      post("/users/2/forceChangeUsername")
        .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_ADMIN_ACCOUNT_NAME_CHANGE))
        .params(params))
      .andExpect(status().isOk());

    assertThat(userRepository.getById(2).getLogin(), is(NEW_USER));
  }

  @Test
  public void changeUsernameForcedByModeratorWithoutScope() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("newUsername", NEW_USER);

    mockMvc.perform(
      post("/users/2/forceChangeUsername")
        .with(getOAuthTokenWithActiveUser(NO_SCOPE, GroupPermission.ROLE_ADMIN_ACCOUNT_NAME_CHANGE))
        .params(params))
      .andExpect(status().is4xxClientError());
  }

  @Test
  public void changeUsernameForcedByModeratorWithoutRole() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("newUsername", NEW_USER);

    mockMvc.perform(
      post("/users/2/forceChangeUsername")
        .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES))
        .params(params))
      .andExpect(status().is4xxClientError());
  }

  @Test
  public void changeUsernameTooEarly() throws Exception {
    assertThat(userRepository.getById(2).getLogin(), is(AUTH_MODERATOR));

    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("newUsername", NEW_USER);

    MvcResult result = mockMvc.perform(
      post("/users/changeUsername")
        .with(getOAuthTokenForUserId(USERID_MODERATOR, OAuthScope._WRITE_ACCOUNT_DATA))
        .params(params))
      .andExpect(status().is4xxClientError())
      .andReturn();

    assertApiError(result, ErrorCode.USERNAME_CHANGE_TOO_EARLY);

    assertThat(userRepository.getById(2).getLogin(), is(AUTH_MODERATOR));
  }

  @Test
  public void changeUsernameTooEarlyButForced() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("newUsername", NEW_USER);

    mockMvc.perform(
      post("/users/2/forceChangeUsername")
        .with(getOAuthTokenWithActiveUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_ADMIN_ACCOUNT_NAME_CHANGE))
        .params(params))
      .andExpect(status().isOk())
      .andReturn();

    assertThat(userRepository.getById(2).getLogin(), is(NEW_USER));
  }

  @Test
  public void resyncAccountSuccess() throws Exception {
    mockMvc.perform(
      post("/users/resyncAccount")
        .with(getOAuthTokenForUserId(USERID_USER, OAuthScope._WRITE_ACCOUNT_DATA)))
      .andExpect(status().isOk());
  }

  @Test
  public void buildGogProfileToken() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("gogUsername", "someUsername");
    when(gogService.buildGogToken(any())).thenReturn("theToken");

    mockMvc.perform(
      get("/users/buildGogProfileToken")
        .with(getOAuthTokenForUserId(USERID_USER, OAuthScope._WRITE_ACCOUNT_DATA))
        .params(params))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.gogToken", is("theToken")));

    verify(gogService).buildGogToken(any());
  }

  @Test
  public void linkToGogWithoutOAuthScopeFails() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("gogUsername", "someUsername");

    mockMvc.perform(
      post("/users/linkToGog")
        .with(getOAuthTokenForUserId(USERID_USER, NO_SCOPE))
        .params(params))
      .andExpect(status().isForbidden());
  }

  @Test
  public void linkToGogSuccess() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("gogUsername", "someUsername");

    when(gogService.buildGogToken(any())).thenReturn("theToken");

    mockMvc.perform(
      post("/users/linkToGog")
        .with(getOAuthTokenForUserId(USERID_USER, OAuthScope._WRITE_ACCOUNT_DATA))
        .params(params))
      .andExpect(status().isOk());

    assertThat(accountLinkRepository.existsByUserAndServiceType(userRepository.getReferenceById(1), LinkedServiceType.GOG), is(true));
  }

  @Test
  public void linkToGogAlreadyLinked() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("gogUsername", "username");

    when(gogService.buildGogToken(any())).thenReturn("theToken");

    mockMvc.perform(
        post("/users/linkToGog")
          .with(getOAuthTokenForUserId(USERID_USER, OAuthScope._WRITE_ACCOUNT_DATA))
          .params(params))
      .andExpect(status().isUnprocessableEntity());

    assertThat(accountLinkRepository.existsByUserAndServiceType(userRepository.getReferenceById(1), LinkedServiceType.GOG), is(false));
  }
}
