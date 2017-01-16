package com.faforever.api.data;

import com.google.common.collect.ImmutableMap;
import com.yahoo.elide.Elide;
import com.yahoo.elide.ElideResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MultivaluedHashMap;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

@RestController
@RequestMapping(path = JsonApiController.PATH_PREFIX)
public class JsonApiController {

  static final String PATH_PREFIX = "/data";
  static final String JSON_API_MEDIA_TYPE = "application/vnd.api+json";

  private Elide elide;

  public JsonApiController(Elide elide) {
    this.elide = elide;
  }

  public static String getJsonApiPath(HttpServletRequest request) {
    return ((String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)).replace(PATH_PREFIX, "");
  }
  
  @CrossOrigin(origins = "*")
  @RequestMapping(
      method = RequestMethod.GET,
      produces = JSON_API_MEDIA_TYPE,
      value = {"/{entity}", "/{entity}/{id}/relationships/{entity2}", "/{entity}/{id}/{child}", "/{entity}/{id}"})
  @Transactional(readOnly = true)
  @Cacheable(cacheResolver = "elideCacheResolver")
  public String jsonApiGet(@RequestParam final Map<String, String> allRequestParams,
                           final HttpServletRequest request,
                           final Authentication authentication) {
    return elide.get(
        getJsonApiPath(request),
        new MultivaluedHashMap<>(allRequestParams),
        getPrincipal(authentication)
    ).getBody();
  }

  @CrossOrigin(origins = "*") // this is needed otherwise I get always an Invalid CORS Request message
  @RequestMapping(
      method = RequestMethod.PATCH,
      produces = JSON_API_MEDIA_TYPE,
      value = {"/{entity}/{id}", "/{entity}/{id}/relationships/{entity2}"})
  @Transactional
  public String jsonApiPatch(@RequestBody final String body,
                             final HttpServletRequest request,
                             final Authentication authentication) {
    return elide.patch(JSON_API_MEDIA_TYPE,
        JSON_API_MEDIA_TYPE,
        getJsonApiPath(request),
        body,
        getPrincipal(authentication)
    ).getBody();
  }

  @CrossOrigin(origins = "*") // this is needed otherwise I get always an Invalid CORS Request message
  @RequestMapping(
      method = RequestMethod.DELETE,
      produces = JSON_API_MEDIA_TYPE,
      value = {"/{entity}/{id}", "/{entity}/{id}/relationships/{entity2}"})
  @Transactional
  public void jsonApiDelete(final HttpServletRequest request,
                           final Authentication authentication) throws JsonApiException {
    ElideResponse response = elide.delete(
        getJsonApiPath(request),
        null,
        getPrincipal(authentication)
    );
    if (response.getResponseCode() / 100 != 2) {
      throw new JsonApiException("No Permission");
    }
  }

  public static Object getPrincipal(final Authentication authentication) {
    return authentication != null ? authentication.getPrincipal() : null;
  }

  public static String getJsonApiPath(HttpServletRequest request) {
    return ((String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)).replace(PATH_PREFIX, "");
  }


  // Show error message as result
  @ExceptionHandler(JsonApiException.class)
  public Map<String, Serializable> handleClanException(JsonApiException ex, HttpServletResponse response) throws IOException {
    response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
    ImmutableMap source = ImmutableMap.of("pointer", "");
    ImmutableMap<String, Serializable> error = ImmutableMap.of(
        "title", ex.getMessage(),
        "source", source);
    return ImmutableMap.of("errors", new ImmutableMap[]{error});
  }
}
