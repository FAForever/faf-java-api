package com.faforever.api.data;

import com.faforever.api.security.ElideUser;
import com.yahoo.elide.ElideResponse;
import com.yahoo.elide.core.request.route.Route;
import com.yahoo.elide.core.security.User;
import com.yahoo.elide.jsonapi.JsonApi;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.faforever.api.data.JsonApiMediaType.JSON_API_MEDIA_TYPE;
import static com.faforever.api.data.JsonApiMediaType.JSON_API_PATCH_MEDIA_TYPE;

/**
 * JSON-API compliant data API.
 */
@RestController
@RequestMapping(path = DataController.PATH_PREFIX)
@Secured("ROLE_USER")
public class DataController {

  public static final String PATH_PREFIX = "/data";
  public static final String API_VERSION = "";

  private final JsonApi jsonApi;

  public DataController(JsonApi jsonApi) {
    this.jsonApi = jsonApi;
  }

  private static User getPrincipal(final Authentication authentication) {
    return new ElideUser(authentication);
  }

  //!!! No @Transactional - transactions are being handled by Elide
  @GetMapping(value = {"/{entity}", "/{entity}/**"}, produces = JSON_API_MEDIA_TYPE)
  @Cacheable(cacheResolver = "elideCacheResolver", keyGenerator = GetCacheKeyGenerator.NAME)
  public ResponseEntity<String> get(
    @RequestParam final MultiValueMap<String, String> allRequestParams,
    final HttpServletRequest request,
    final Authentication authentication
  ) {
    ElideResponse<String> response = jsonApi.get(
      Route.builder()
        .baseUrl(getBaseUrlEndpoint())
        .path(getJsonApiPath(request))
        .apiVersion(API_VERSION)
        .parameters(allRequestParams)
        .build(),
      getPrincipal(authentication),
      UUID.randomUUID()
    );
    return wrapResponse(response);
  }

  //!!! No @Transactional - transactions are being handled by Elide
  @PostMapping(value = "/operations", consumes = JsonApi.AtomicOperations.MEDIA_TYPE, produces = JsonApi.AtomicOperations.MEDIA_TYPE)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<String> operations(
    @RequestParam final MultiValueMap<String, String> allRequestParams,
    @RequestBody final String body,
    final HttpServletRequest request,
    final Authentication authentication
  ) {
    ElideResponse<String> response = jsonApi.operations(
      Route.builder()
        .baseUrl(getBaseUrlEndpoint())
        .path(getJsonApiPath(request))
        .apiVersion(API_VERSION)
        .parameters(allRequestParams)
        .build(),
      body,
      getPrincipal(authentication),
      UUID.randomUUID()
    );
    return wrapResponse(response);
  }

  //!!! No @Transactional - transactions are being handled by Elide
  @PostMapping(value = "/**", consumes = {JSON_API_MEDIA_TYPE, MediaType.APPLICATION_JSON_VALUE}, produces = JSON_API_MEDIA_TYPE)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<String> post(
    @RequestParam final MultiValueMap<String, String> allRequestParams,
    @RequestBody final String body,
    final HttpServletRequest request,
    final Authentication authentication
  ) {
    ElideResponse<String> response = jsonApi.post(
      Route.builder()
        .baseUrl(getBaseUrlEndpoint())
        .path(getJsonApiPath(request))
        .apiVersion(API_VERSION)
        .parameters(allRequestParams)
        .build(),
      body,
      getPrincipal(authentication),
      UUID.randomUUID()
    );
    return wrapResponse(response);
  }

  //!!! No @Transactional - transactions are being handled by Elide
  @PatchMapping(value = "/**", consumes = {JSON_API_MEDIA_TYPE, MediaType.APPLICATION_JSON_VALUE, JSON_API_PATCH_MEDIA_TYPE}, produces = {JSON_API_MEDIA_TYPE, JSON_API_PATCH_MEDIA_TYPE})
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<String> patch(
    @RequestParam final MultiValueMap<String, String> allRequestParams,
    @RequestBody final String body,
    @RequestHeader(name = HttpHeaders.CONTENT_TYPE, required = false) String contentTypeHeader,
    @RequestHeader(name = HttpHeaders.ACCEPT, required = false) String acceptHeader,
    final HttpServletRequest request,
    final Authentication authentication
  ) {
    //content-type and accept headers are used by elide to decide json patch extension should be used
    ElideResponse<String> response = jsonApi.patch(
      Route.builder()
        .baseUrl(getBaseUrlEndpoint())
        .path(getJsonApiPath(request))
        .apiVersion(API_VERSION)
        .parameters(allRequestParams)
        .headers(Map.of(
          "content-type", contentTypeHeader != null ? List.of(contentTypeHeader) : List.of(),
          "accept", acceptHeader != null ? List.of(acceptHeader) : List.of()
        ))
        .build(),
      body,
      getPrincipal(authentication),
      UUID.randomUUID()
    );
    if (JSON_API_PATCH_MEDIA_TYPE.equals(contentTypeHeader)) {
      return wrapResponse(response, JSON_API_PATCH_MEDIA_TYPE);
    }
    return wrapResponse(response);
  }

  //!!! No @Transactional - transactions are being handled by Elide
  @DeleteMapping(value = "/**", produces = JSON_API_MEDIA_TYPE)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<String> delete(
    @RequestParam final MultiValueMap<String, String> allRequestParams,
    @RequestBody(required = false) final String body,
    final HttpServletRequest request,
    final Authentication authentication
  ) {
    ElideResponse<String> response = jsonApi.delete(
      Route.builder()
        .baseUrl(getBaseUrlEndpoint())
        .path(getJsonApiPath(request))
        .apiVersion(API_VERSION)
        .parameters(allRequestParams)
        .build(),
      body,
      getPrincipal(authentication),
      UUID.randomUUID()
    );
    return wrapResponse(response);
  }

  private ResponseEntity<String> wrapResponse(ElideResponse<String> response) {
    return ResponseEntity.status(response.getStatus()).body(response.getBody());
  }

  private ResponseEntity<String> wrapResponse(ElideResponse<String> response, String contentTypeHeader) {
    return ResponseEntity.status(response.getStatus()).header(HttpHeaders.CONTENT_TYPE, contentTypeHeader).body(response.getBody());
  }

  private String getJsonApiPath(HttpServletRequest request) {
    String pathname = (String) request
      .getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

    return pathname.replaceFirst(PATH_PREFIX, "");
  }


  private String getBaseUrlEndpoint() {
    return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
      + PATH_PREFIX + "/";
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
