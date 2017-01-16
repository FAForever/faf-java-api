package com.faforever.api.data;

import com.yahoo.elide.Elide;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedHashMap;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = JsonApiController.PATH_PREFIX)
public class JsonApiController {

  static final String PATH_PREFIX = "/data";
  static final String JSON_API_MEDIA_TYPE = "application/vnd.api+json";

  private Elide elide;

  public JsonApiController(Elide elide) {
    this.elide = elide;
  }

  @RequestMapping(
      method = RequestMethod.GET,
      produces = JSON_API_MEDIA_TYPE,
      value = {"/{entity}", "/{entity}/{id}/relationships/{entity2}", "/{entity}/{id}/{child}", "/{entity}/{id}"})
  @Transactional(readOnly = true)
  @Cacheable(cacheResolver = "elideCacheResolver")
  public String jsonApiGet(@RequestParam Map<String, String> allRequestParams, HttpServletRequest request, Authentication authentication) {
    return elide.get(
        getJsonApiPath(request),
        new MultivaluedHashMap<>(allRequestParams),
        authentication != null ? authentication.getPrincipal() : null
    ).getBody();
  }
  
  @RequestMapping(
      method = RequestMethod.PATCH,
      produces = JSON_API_MEDIA_TYPE,
      value = {"/{entity}/{id}", "/{entity}/{id}/relationships/{entity2}"})
  @Transactional
  public String jsonApiPatch(final HttpServletRequest request, Authentication authentication) throws IOException {
    // Note: We can only read the body ONCE with getReader
    String body = request.getReader().lines().collect(Collectors.joining());
    return elide.patch(JSON_API_MEDIA_TYPE,
        JSON_API_MEDIA_TYPE,
        getJsonApiPath(request),
        body,
        authentication != null ? authentication.getPrincipal() : null
    ).getBody();
  }

  @CrossOrigin(origins = "*")
  @RequestMapping(
      method = RequestMethod.PATCH,
      produces = JSON_API_MEDIA_TYPE,
      value = {"/{entity}/{id}", "/{entity}/{id}/relationships/{entity2}"})
  @Transactional
  public String jsonApiPatch(final HttpServletRequest request, Authentication authentication) throws IOException {
    // Note: We can only read the body ONCE with getReader
    String body = request.getReader().lines().collect(Collectors.joining());
    return elide.patch(JSON_API_MEDIA_TYPE,
        JSON_API_MEDIA_TYPE,
        getJsonApiPath(request),
        body,
        authentication != null ? authentication.getPrincipal() : null
    ).getBody();
  }

  public static String getJsonApiPath(HttpServletRequest request) {
    return ((String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)).replace(PATH_PREFIX, "");
  }
}
