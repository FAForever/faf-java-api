package com.faforever.api.user;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping(path = "/users")
public class UserController {
  private final FafApiProperties fafApiProperties;
  private final UserService userService;

  public UserController(FafApiProperties fafApiProperties, UserService userService) {
    this.fafApiProperties = fafApiProperties;
    this.userService = userService;
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

  @ApiOperation("Sends a password reset to the username OR email linked by this account.")
  @RequestMapping(path = "/resetPassword", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public void resetPassword(@RequestParam("identifier") String identifier) {
    userService.resetPassword(identifier);
  }

  @ApiOperation("Sets a new password for an account.")
  @RequestMapping(path = "/confirmPasswordReset", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public void claimPasswordResetToken(HttpServletResponse response,
                                      @RequestParam("token") String token,
                                      @RequestParam("newPassword") String newPassword) throws IOException {
    userService.claimPasswordResetToken(token, newPassword);
    response.sendRedirect(fafApiProperties.getPasswordReset().getSuccessRedirectUrl());
  }
}
