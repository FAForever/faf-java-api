package com.faforever.api.data;

import com.google.common.collect.ImmutableMap;
import com.yahoo.elide.Elide;
import com.yahoo.elide.ElideResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import javax.ws.rs.core.MultivaluedHashMap;
import java.io.Serializable;
import java.util.Map;

@RestController
@RequestMapping(path = JsonApiController.PATH_PREFIX)
public class JsonApiController {

  public static final String PATH_PREFIX = "/data";
  private static final String JSON_API_MEDIA_TYPE = "application/vnd.api+json";

  private Elide elide;

  public JsonApiController(Elide elide) {
    this.elide = elide;
  }

  @CrossOrigin(origins = "*")
  @RequestMapping(
      method = RequestMethod.GET,
      produces = JSON_API_MEDIA_TYPE,
      value = {"/{entity}", "/{entity}/{id}/relationships/{entity2}", "/{entity}/{id}/{child}", "/{entity}/{id}"})
  @Transactional(readOnly = true)
  @Cacheable(cacheResolver = "elideCacheResolver")
  public ResponseEntity<String> jsonApiGet(@RequestParam final Map<String, String> allRequestParams,
                                           final HttpServletRequest request,
                                           final Authentication authentication) {
    ElideResponse response = elide.get(
        getJsonApiPath(request),
        new MultivaluedHashMap<>(allRequestParams),
        getPrincipal(authentication)
    );
    return encodeResponse(response);
  }

  @CrossOrigin(origins = "*") // this is needed otherwise I get always an Invalid CORS Request message
  @RequestMapping(
      method = RequestMethod.POST,
      produces = JSON_API_MEDIA_TYPE,
      value = {"/{entity}", "/{entity}/{id}/relationships/{entity2}", "/{entity}/{id}/{child}", "/{entity}/{id}"})
  @Transactional
  @Cacheable(cacheResolver = "elideCacheResolver")
  public ResponseEntity<String> jsonApiPost(@RequestBody final String body,
                                            final HttpServletRequest request,
                                            final Authentication authentication) {
    ElideResponse response = elide.post(
        getJsonApiPath(request),
        body,
        getPrincipal(authentication)
    );
    return encodeResponse(response);
  }

  @CrossOrigin(origins = "*") // this is needed otherwise I get always an Invalid CORS Request message
  @RequestMapping(
      method = RequestMethod.PATCH,
      produces = JSON_API_MEDIA_TYPE,
      value = {"/{entity}/{id}", "/{entity}/{id}/relationships/{entity2}"})
  @Transactional
  public ResponseEntity<String> jsonApiPatch(@RequestBody final String body,
                                             final HttpServletRequest request,
                                             final Authentication authentication) {
    ElideResponse response = elide.patch(JSON_API_MEDIA_TYPE,
        JSON_API_MEDIA_TYPE,
        getJsonApiPath(request),
        body,
        getPrincipal(authentication)
    );
    return encodeResponse(response);
  }

  @CrossOrigin(origins = "*") // this is needed otherwise I get always an Invalid CORS Request message
  @RequestMapping(
      method = RequestMethod.DELETE,
      produces = JSON_API_MEDIA_TYPE,
      value = {"/{entity}/{id}", "/{entity}/{id}/relationships/{entity2}"})
  @Transactional
  public ResponseEntity<String> jsonApiDelete(final HttpServletRequest request,
                                              final Authentication authentication) {
    ElideResponse response = elide.delete(
        getJsonApiPath(request),
        null,
        getPrincipal(authentication)
    );
    return encodeResponse(response);
  }

  private ResponseEntity<String> encodeResponse(ElideResponse response) {
    return ResponseEntity.status(response.getResponseCode()).body(response.getBody());
  }

  private static Object getPrincipal(final Authentication authentication) {
    return authentication != null ? authentication.getPrincipal() : null;
  }

  private String getJsonApiPath(HttpServletRequest request) {
    return ((String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)).replace(PATH_PREFIX, "");
  }


  @ExceptionHandler(ValidationException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  @ResponseBody
  public Map<String, Serializable> processValidationError(ValidationException ex) {
    return errorResponse(ex.getMessage());
  }

  private Map<String, Serializable> errorResponse(String title) {
    ImmutableMap source = ImmutableMap.of("pointer", "");
    ImmutableMap<String, Serializable> error = ImmutableMap.of(
        "title", title,
        "source", source);
    return ImmutableMap.of("errors", new ImmutableMap[]{error});
  }
}
