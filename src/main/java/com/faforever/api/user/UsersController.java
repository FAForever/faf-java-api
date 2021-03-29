package com.faforever.api.user;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.User;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.security.OAuthScope;
import com.faforever.api.user.UserService.CallbackResult;
import com.faforever.api.utils.RemoteAddressUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UsersController {
  private final FafApiProperties fafApiProperties;
  private final UserService userService;
  private final SteamService steamService;
  private final RecaptchaService recaptchaService;
  private final ObjectMapper objectMapper;

  @ApiOperation("Registers a new account that needs to be activated.")
  @PostMapping(path = "/register", produces = APPLICATION_JSON_VALUE)
  public void register(HttpServletRequest request,
                       @RequestParam("username") String username,
                       @RequestParam("email") String email,
                       @RequestParam(value = "recaptchaResponse", required = false) String recaptchaResponse) {
    if (request.isUserInRole("USER")) {
      throw new ApiException(new Error(ErrorCode.ALREADY_REGISTERED));
    }

    recaptchaService.validateResponse(recaptchaResponse);
    userService.register(username, email);
  }

  @ApiOperation("Activates a previously registered account.")
  @PostMapping(path = "/activate", produces = APPLICATION_JSON_VALUE)
  public void activate(HttpServletRequest request,
                       @RequestParam("token") String registrationToken,
                       @RequestParam("password") String password) {
    userService.activate(registrationToken, password, RemoteAddressUtil.getRemoteAddress(request));
  }

  @PreAuthorize("#oauth2.hasScope('" + OAuthScope._WRITE_ACCOUNT_DATA + "') and hasRole('ROLE_USER')")
  @ApiOperation("Changes the password of a previously registered account.")
  @PostMapping(path = "/changePassword", produces = APPLICATION_JSON_VALUE)
  public void changePassword(@RequestParam("currentPassword") String currentPassword,
                             @RequestParam("newPassword") String newPassword,
                             Authentication authentication) {
    userService.changePassword(currentPassword, newPassword, userService.getUser(authentication));
  }

  @PreAuthorize("#oauth2.hasScope('" + OAuthScope._WRITE_ACCOUNT_DATA + "') and hasRole('ROLE_USER')")
  @ApiOperation("Changes the login of a previously registered account.")
  @PostMapping(path = "/changeUsername", produces = APPLICATION_JSON_VALUE)
  public void changeLogin(HttpServletRequest request,
                          @RequestParam("newUsername") String newUsername,
                          Authentication authentication) {
    userService.changeLogin(
      newUsername,
      userService.getUser(authentication),
      RemoteAddressUtil.getRemoteAddress(request));
  }

  @PreAuthorize("#oauth2.hasScope('" + OAuthScope._ADMINISTRATIVE_ACTION + "') and hasAnyRole('ROLE_ADMIN_ACCOUNT_NAME_CHANGE')")
  @ApiOperation("Force the change of the login of a user with the given userId.")
  @PostMapping(path = "/{userId}/forceChangeUsername", produces = APPLICATION_JSON_VALUE)
  public void forceChangeLogin(HttpServletRequest request,
                               @RequestParam("newUsername") String newUsername,
                               @PathVariable("userId") int userId) {
    User user = userService.getUser(userId);
    userService.changeLoginForced(newUsername, user, RemoteAddressUtil.getRemoteAddress(request));
  }

  @PreAuthorize("#oauth2.hasScope('" + OAuthScope._WRITE_ACCOUNT_DATA + "') and hasRole('ROLE_USER')")
  @ApiOperation("Changes the email of a previously registered account.")
  @PostMapping(path = "/changeEmail", produces = APPLICATION_JSON_VALUE)
  public void changeEmail(HttpServletRequest request,
                          @RequestParam("currentPassword") String currentPassword,
                          @RequestParam("newEmail") String newEmail, Authentication authentication) {
    userService.changeEmail(
      currentPassword,
      newEmail,
      userService.getUser(authentication),
      RemoteAddressUtil.getRemoteAddress(request)
    );
  }


  @ApiOperation("Sends a password reset request to the username OR email linked by this account.")
  @PostMapping(path = "/requestPasswordReset", produces = APPLICATION_JSON_VALUE)
  public void requestPasswordReset(@RequestParam("identifier") String identifier,
                                   @RequestParam(value = "recaptchaResponse", required = false) String recaptchaResponse) {
    recaptchaService.validateResponse(recaptchaResponse);
    userService.requestPasswordReset(identifier);
  }

  @ApiOperation("Sets a new password for an account.")
  @PostMapping(path = "/performPasswordReset", produces = APPLICATION_JSON_VALUE)
  public void performPasswordReset(HttpServletResponse response,
                                   @RequestParam("token") String token,
                                   @RequestParam("newPassword") String newPassword) {
    userService.performPasswordReset(token, newPassword);
  }

  @PreAuthorize("#oauth2.hasScope('" + OAuthScope._WRITE_ACCOUNT_DATA + "') and hasRole('ROLE_USER')")
  @ApiOperation("Creates an URL to the steam platform to initiate the Link To Steam process.")
  @PostMapping(path = "/buildSteamLinkUrl", produces = APPLICATION_JSON_VALUE)
  public Map<String, Serializable> buildSteamLinkUrl(Authentication authentication,
                                                     @RequestParam("callbackUrl") String callbackUrl) {
    String steamUrl = userService.buildSteamLinkUrl(userService.getUser(authentication), callbackUrl);
    return Map.of("steamUrl", steamUrl);
  }

  @ApiOperation("Build a password reset link via Steam.")
  @PostMapping(path = "/buildSteamPasswordResetUrl", produces = APPLICATION_JSON_VALUE)
  public Map<String, Serializable> buildSteamPasswordResetUrl() {
    String steamUrl = userService.buildSteamPasswordResetUrl();
    return Map.of("steamUrl", steamUrl);
  }

  @ApiOperation("Sends a password reset request to the username OR email linked by this account.")
  @GetMapping(path = "/requestPasswordResetViaSteam", produces = APPLICATION_JSON_VALUE)
  public void requestPasswordResetViaSteam(HttpServletRequest request,
                                           HttpServletResponse response) {
    steamService.validateSteamRedirect(request);
    String steamId = steamService.parseSteamIdFromLoginRedirect(request);
    CallbackResult result = userService.requestPasswordResetViaSteam(steamId);
    redirectCallbackResult(response, result);
  }

  @ApiOperation("Processes the Steam redirect and creates the steam link in the user account.")
  @GetMapping(path = "/linkToSteam", produces = APPLICATION_JSON_VALUE)
  public void linkToSteam(HttpServletRequest request,
                          HttpServletResponse response,
                          @RequestParam("token") String token) {
    steamService.validateSteamRedirect(request);
    CallbackResult result = userService.linkToSteam(token, steamService.parseSteamIdFromLoginRedirect(request));
    redirectCallbackResult(response, result);
  }

  @PreAuthorize("#oauth2.hasScope('" + OAuthScope._WRITE_ACCOUNT_DATA + "') and hasRole('ROLE_USER')")
  @ApiOperation("Trigger resynchronisation of the users account with all related systems.")
  @PostMapping(path = "/resyncAccount", produces = APPLICATION_JSON_VALUE)
  public void resynchronizeAccount(Authentication authentication) {
    userService.resynchronizeAccount(userService.getUser(authentication));
  }

  @SneakyThrows
  private void redirectCallbackResult(HttpServletResponse response, CallbackResult result) {
    if (result.getErrors().isEmpty()) {
      response.sendRedirect(result.getCallbackUrl());
    } else {
      UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(result.getCallbackUrl());
      String errorsJson = objectMapper.writeValueAsString(result.getErrors());
      uriBuilder.queryParam("errors", errorsJson);
      response.sendRedirect(uriBuilder.toUriString());
    }
  }

}
