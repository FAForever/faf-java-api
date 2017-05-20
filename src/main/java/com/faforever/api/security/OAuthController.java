package com.faforever.api.security;

import com.faforever.api.client.OAuthClient;
import com.faforever.api.client.OAuthClientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/oauth")
public class OAuthController {
  private OAuthClientRepository oAuthClientRepository;

  public OAuthController(OAuthClientRepository oAuthClientRepository) {
    this.oAuthClientRepository = oAuthClientRepository;
  }

  @GetMapping("/confirm_access")
  public ModelAndView confirmAccess(@RequestParam(value = "client_id") String clientId) {
    OAuthClient client = oAuthClientRepository.findOne(clientId);
    return new ModelAndView("oauth_confirm_access").addObject("client", client);
  }

}
