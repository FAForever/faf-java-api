package com.faforever.api.data;

import com.yahoo.elide.Elide;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedHashMap;
import java.util.Map;

@RestController
@RequestMapping(path = JsonApiController.PATH_PREFIX)
public class JsonApiController {

  static final String PATH_PREFIX = "/data";

  private Elide elide;

  public JsonApiController(Elide elide) {
    this.elide = elide;
  }

  @RequestMapping(
      method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE,
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

  public static String getJsonApiPath(HttpServletRequest request) {
    return ((String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)).replace(PATH_PREFIX, "");
  }
}
