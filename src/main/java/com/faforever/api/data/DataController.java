package com.faforever.api.data;

import com.yahoo.elide.Elide;
import com.yahoo.elide.ElideResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedHashMap;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * JSON-API compliant data API.
 */
@RestController
@RequestMapping(path = DataController.PATH_PREFIX)
public class DataController {

  public static final String PATH_PREFIX = "/data";
  private static final String JSON_API_MEDIA_TYPE = "application/vnd.api+json";

  private final Elide elide;

  public DataController(Elide elide) {
    this.elide = elide;
  }

  private static Object getPrincipal(final Authentication authentication) {
    return authentication != null ? authentication.getPrincipal() : null;
  }

  @RequestMapping(
    method = RequestMethod.GET,
    produces = JSON_API_MEDIA_TYPE,
    value = {"/{entity}", "/{entity}/{id}/relationships/{entity2}", "/{entity}/{id}/{child}", "/{entity}/{id}"})
  @Transactional(readOnly = true)
  @Cacheable(cacheResolver = "elideCacheResolver", keyGenerator = GetCacheKeyGenerator.NAME)
  public ResponseEntity<String> get(@RequestParam final Map<String, String> allRequestParams,
                                    final HttpServletRequest request,
                                    final Authentication authentication) {
    ElideResponse response = elide.get(
      getJsonApiPath(request),
      new MultivaluedHashMap<>(allRequestParams),
      getPrincipal(authentication)
    );
    return wrapResponse(response);
  }

  @RequestMapping(
    method = RequestMethod.POST,
    produces = JSON_API_MEDIA_TYPE,
    value = {"/{entity}", "/{entity}/{id}/relationships/{entity2}", "/{entity}/{id}/{child}", "/{entity}/{id}"})
  @Transactional
  @Cacheable(cacheResolver = "elideCacheResolver")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<String> post(@RequestBody final String body,
                                     final HttpServletRequest request,
                                     final Authentication authentication) {
    ElideResponse response = elide.post(
      getJsonApiPath(request),
      body,
      getPrincipal(authentication)
    );
    return wrapResponse(response);
  }

  @RequestMapping(
    method = RequestMethod.PATCH,
    produces = JSON_API_MEDIA_TYPE,
    value = {"/{entity}/{id}", "/{entity}/{id}/relationships/{entity2}"})
  @Transactional
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<String> patch(@RequestBody final String body,
                                      final HttpServletRequest request,
                                      final Authentication authentication) {
    ElideResponse response = elide.patch(JSON_API_MEDIA_TYPE,
      JSON_API_MEDIA_TYPE,
      getJsonApiPath(request),
      body,
      getPrincipal(authentication)
    );
    return wrapResponse(response);
  }

  @RequestMapping(
    method = RequestMethod.DELETE,
    produces = JSON_API_MEDIA_TYPE,
    value = {"/{entity}/{id}", "/{entity}/{id}/relationships/{entity2}"})
  @Transactional
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<String> delete(final HttpServletRequest request,
                                       final Authentication authentication) {
    ElideResponse response = elide.delete(
      getJsonApiPath(request),
      null,
      getPrincipal(authentication)
    );
    return wrapResponse(response);
  }

  private ResponseEntity<String> wrapResponse(ElideResponse response) {
    return ResponseEntity.status(response.getResponseCode()).body(response.getBody());
  }

  private String getJsonApiPath(HttpServletRequest request) {
    return ((String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)).replace(PATH_PREFIX, "");
  }

  @Component(GetCacheKeyGenerator.NAME)
  class GetCacheKeyGenerator implements KeyGenerator {
    static final String NAME = "elideGetCacheKeyGenerator";

    @Override
    public Object generate(Object target, Method method, Object... params) {
      @SuppressWarnings("unchecked")
      Map<String, String> allRequestParams = (Map<String, String>) params[0];
      final HttpServletRequest request = (HttpServletRequest) params[1];

      return method.getName() + getJsonApiPath(request) + allRequestParams;
    }
  }
}
