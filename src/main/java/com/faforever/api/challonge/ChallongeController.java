package com.faforever.api.challonge;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.config.FafApiProperties.Challonge;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.DefaultUriTemplateHandler;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/**
 * Forwards all requests to the Challonge API, using the configured API key. GET requests are allowed by anyone, all
 * other requests require the role {@code ROLE_TOURNAMENT_DIRECTORY}. <p><strong>CAVEAT</strong>: This controller is
 * only loaded if a Challonge API key is specified.</p>
 */
@RestController
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
@RequestMapping(path = ChallongeController.CHALLONGE_ROUTE)
@ConditionalOnProperty("faf-api.challonge.key")
public class ChallongeController {

  public static final String CHALLONGE_READ_CACHE_NAME = "challongeRead";
  static final String CHALLONGE_ROUTE = "/challonge";
  private final RestTemplate restTemplate;

  public ChallongeController(FafApiProperties properties) {
    Challonge challonge = properties.getChallonge();

    restTemplate = new RestTemplateBuilder()
      .rootUri(challonge.getBaseUrl())
      .uriTemplateHandler(new ChallongeUriTemplateHandler(challonge.getKey()))
      .build();
  }

  private static String translateRoute(HttpServletRequest request) {
    return ((String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE))
      .replace(CHALLONGE_ROUTE, "");
  }

  @Async
  @Cacheable(cacheNames = CHALLONGE_READ_CACHE_NAME)
  @RequestMapping(path = "/**", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<ResponseEntity<String>> get(HttpServletRequest request) {
    return CompletableFuture.completedFuture(restTemplate.getForEntity(translateRoute(request), String.class, ImmutableMap.of()));
  }

  @Async
  @Secured("ROLE_TOURNAMENT_DIRECTORY")
  @RequestMapping(path = "/**", method = {POST, PUT, DELETE}, produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<ResponseEntity<String>> write(@RequestBody(required = false) Object body, HttpMethod method, HttpServletRequest request) {
    return CompletableFuture.completedFuture(restTemplate.exchange(translateRoute(request), method, new HttpEntity<>(body), String.class));
  }

  @RequiredArgsConstructor
  private static class ChallongeUriTemplateHandler extends DefaultUriTemplateHandler {
    private final String apiKey;

    @Override
    protected UriComponentsBuilder initUriComponentsBuilder(String uriTemplate) {
      return super.initUriComponentsBuilder(uriTemplate).queryParam("api_key", apiKey);
    }
  }
}
