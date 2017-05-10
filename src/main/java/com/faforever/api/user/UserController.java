package com.faforever.api.user;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @ApiOperation("Registers a new account that needs to be activated.")
  @RequestMapping(path = "/register", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public void register(@RequestParam("username") String username,
                       @RequestParam("email") String email,
                       @RequestParam("password") String password) {
    userService.register(username, email, password);
  }

  @ApiOperation("Activates a previously registered account.")
  @RequestMapping(path = "/activate", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public void activate(@RequestParam("token") String token) {
    userService.activate(token);
  }

  @PreAuthorize("#oauth2.hasScope('change_password') and hasRole('ROLE_USER')")
  @ApiOperation("Changes the password of a previously registered account.")
  @RequestMapping(path = "/changePassword", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public void changePassword(@RequestParam("newPassword") String newPassword, Authentication authentication) {
    userService.changePassword(newPassword, userService.getUser(authentication));
  }

  @PreAuthorize("#oauth2.hasScope('change_login') and hasRole('ROLE_USER')")
  @ApiOperation("Changes the login of a previously registered account.")
  @RequestMapping(path = "/changeUsername", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public void changeLogin(@RequestParam("newUsername") String newUsername, Authentication authentication) {
    userService.changeLogin(newUsername, userService.getUser(authentication));
  }

  @ApiOperation("Sends a password reset to the email linked by this account.")
  @RequestMapping(path = "/resetPassword", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public void resetPassword(@RequestParam("email") String email) {
    userService.resetPassword(email);
  }

  @ApiOperation("Sets a new password for an account.")
  @RequestMapping(path = "/claimPasswordResetToken", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public void claimPasswordResetToken(@RequestParam("token") String token, @RequestParam("newPassword") String newPassword) {
    userService.claimPasswordResetToken(token, newPassword);
  }
}
