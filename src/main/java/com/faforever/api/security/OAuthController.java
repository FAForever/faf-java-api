package com.faforever.api.security;

import com.faforever.api.client.OAuthClient;
import com.faforever.api.client.OAuthClientRepository;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/oauth")
@SessionAttributes("authorizationRequest")
public class OAuthController {
  private OAuthClientRepository oAuthClientRepository;

  public OAuthController(OAuthClientRepository oAuthClientRepository) {
    this.oAuthClientRepository = oAuthClientRepository;
  }

  @GetMapping("/confirm_access")
  public ModelAndView confirmAccess(Map<String, Object> model) {
    final AuthorizationRequest authorizationRequest = (AuthorizationRequest) model.get("authorizationRequest");
    final OAuthClient client = oAuthClientRepository.findOne(authorizationRequest.getClientId());
    Set<OAuthScope> scopes = getScopesFromScopeString(authorizationRequest.getScope());

    return new ModelAndView("oauth_confirm_access")
      .addObject("client", client)
      .addObject("scopes", scopes);
  }

  private Set<OAuthScope> getScopesFromScopeString(Set<String> scopesString) {
    return scopesString.stream()
      .filter(s -> !s.isEmpty())
      .map(OAuthScope::fromKey)
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toSet());
  }
}
