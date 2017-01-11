package com.faforever.api.user;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

/**
 * Provides the route {@code /me} which returns the currently logged in user's information.
 */
@RestController
public class MeController {

  @RequestMapping(method = RequestMethod.GET, value = "/me")
  @ApiOperation(value = "Returns the authentication object of the current user")
  @Secured({"ROLE_USER"})
  public Map<String, Serializable> me(HttpServletResponse response,
                                      @AuthenticationPrincipal FafUserDetails authentication) throws IOException {
   return ImmutableMap.of("id", authentication.getId(), "type", "player");
  }
}
