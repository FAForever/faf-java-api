package com.faforever.api.user;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping(path = "/users")
public class UserController {
  private final FafApiProperties fafApiProperties;
  private final UserService userService;
  private final SteamService steamService;

  public UserController(FafApiProperties fafApiProperties, UserService userService, SteamService steamService) {
    this.fafApiProperties = fafApiProperties;
    this.userService = userService;
    this.steamService = steamService;
  }

  @ApiOperation("Registers a new account that needs to be activated.")
  @RequestMapping(path = "/register", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public void register(HttpServletRequest request,
                       @RequestParam("username") String username,
                       @RequestParam("email") String email,
                       @RequestParam("password") String password) {
    if (request.isUserInRole("USER")) {
      throw new ApiException(new Error(ErrorCode.ALREADY_REGISTERED));
    }

    userService.register(username, email, password);
  }

  @ApiOperation("Activates a previously registered account.")
  @RequestMapping(path = "/activate", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public void activate(HttpServletResponse response,
                       @RequestParam("token") String token) throws IOException {
    userService.activate(token);
    response.sendRedirect(fafApiProperties.getRegistration().getSuccessRedirectUrl());
  }

  @PreAuthorize("#oauth2.hasScope('write_account_data') and hasRole('ROLE_USER')")
  @ApiOperation("Changes the password of a previously registered account.")
  @RequestMapping(path = "/changePassword", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public void changePassword(@RequestParam("currentPassword") String currentPassword, @RequestParam("newPassword") String newPassword, Authentication authentication) {
    userService.changePassword(currentPassword, newPassword, userService.getUser(authentication));
  }

  @PreAuthorize("#oauth2.hasScope('write_account_data') and hasRole('ROLE_USER')")
  @ApiOperation("Changes the login of a previously registered account.")
  @RequestMapping(path = "/changeUsername", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public void changeLogin(@RequestParam("newUsername") String newUsername, Authentication authentication) {
    userService.changeLogin(newUsername, userService.getUser(authentication));
  }


  @PreAuthorize("#oauth2.hasScope('write_account_data') and hasRole('ROLE_USER')")
  @ApiOperation("Changes the email of a previously registered account.")
  @RequestMapping(path = "/changeEmail", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public void changeEmail(@RequestParam("currentPassword") String currentPassword, @RequestParam("newEmail") String newEmail, Authentication authentication) {
    userService.changeEmail(currentPassword, newEmail, userService.getUser(authentication));
  }


  @ApiOperation("Sends a password reset to the username OR email linked by this account.")
  @RequestMapping(path = "/resetPassword", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public void resetPassword(@RequestParam("identifier") String identifier,
                            @RequestParam("newPassword") String newPassword) {
    userService.resetPassword(identifier, newPassword);
  }

  @ApiOperation("Sets a new password for an account.")
  @RequestMapping(path = "/confirmPasswordReset/{token}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public void claimPasswordResetToken(HttpServletResponse response,
                                      @PathVariable("token") String token) throws IOException {
    userService.claimPasswordResetToken(token);
    response.sendRedirect(fafApiProperties.getPasswordReset().getSuccessRedirectUrl());
  }

  @PreAuthorize("#oauth2.hasScope('write_account_data') and hasRole('ROLE_USER')")
  @ApiOperation("Creates an URL to the steam platform to initiate the Link To Steam process.")
  @RequestMapping(path = "/buildSteamLinkUrl", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public Map<String, Serializable> buildSteamLinkUrl(Authentication authentication) {
    String steamUrl = userService.buildSteamLinkUrl(userService.getUser(authentication));
    return ImmutableMap.of("steam_url", steamUrl);
  }

  @ApiOperation("Processes the Steam redirect and creates the steam link in the user account.")
  @RequestMapping(path = "/linkToSteam", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public void linkToSteam(HttpServletRequest request,
                          HttpServletResponse response,
                          @RequestParam("token") String token) throws IOException {
    try {
      userService.linkToSteam(token, steamService.parseSteamIdFromLoginRedirect(request));
      response.sendRedirect(fafApiProperties.getLinkToSteam().getSuccessRedirectUrl());
    } catch (ApiException e) {
      String errorCodes = Stream.of(e.getErrors())
        .map(error -> String.valueOf(error.getErrorCode().getCode()))
        .collect(Collectors.joining(","));
      response.sendRedirect(String.format(fafApiProperties.getLinkToSteam().getErrorRedirectUrlFormat(), errorCodes));
    }
  }
}
