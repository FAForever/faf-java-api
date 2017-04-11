package com.faforever.api.user;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
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
  @RequestMapping(path = "/activate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public void activate(@RequestParam("token") String token) {
    userService.activate(token);
  }
}
