package com.faforever.api.security;

import com.faforever.api.client.OAuthClient;
import com.faforever.api.client.OAuthClientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/oauth")
public class OAuthController {
  private OAuthClientRepository oAuthClientRepository;

  public OAuthController(OAuthClientRepository oAuthClientRepository) {
    this.oAuthClientRepository = oAuthClientRepository;
  }

  @GetMapping("/confirm_access")
  public ModelAndView confirmAccess(
    @RequestParam("client_id") String clientId,
    @RequestParam("scope") String scope) {
    OAuthClient client = oAuthClientRepository.findOne(clientId);
    final Set<OAuthScope> defaultScopes = getScopesFromScopeString(client.getDefaultScope());
    final Set<OAuthScope> requestScopes = getScopesFromScopeString(scope);
    Set<OAuthScope> scopes = new HashSet<>(defaultScopes);
    scopes.addAll(requestScopes);

    return new ModelAndView("oauth_confirm_access")
      .addObject("client", client)
      .addObject("scopes", scopes);
  }

  private Set<OAuthScope> getScopesFromScopeString(String scopeString) {
    return Arrays.stream(scopeString.split(" "))
      .filter(s -> !s.isEmpty())
      .map(OAuthScope::fromKey)
      .collect(Collectors.toSet());
  }
}
