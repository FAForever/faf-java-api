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

  static final String PATH_PREFIX = "/data";
  static final String JSON_API_MEDIA_TYPE = "application/vnd.api+json";

  private Elide elide;

  public JsonApiController(Elide elide) {
    this.elide = elide;
  }

  @CrossOrigin(origins = "*") // this is needed otherwise I get always an Invalid CORS Request message
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
      method = RequestMethod.POST,
      produces = JSON_API_MEDIA_TYPE,
      value = {"/{entity}", "/{entity}/{id}/relationships/{entity2}", "/{entity}/{id}/{child}", "/{entity}/{id}"})
  @Transactional
  @Cacheable(cacheResolver = "elideCacheResolver")
  public String jsonApiPost(@RequestBody final String body,
                            final HttpServletRequest request,
                            final Authentication authentication) throws JsonApiException {
    ElideResponse response =  elide.post(
        getJsonApiPath(request),
        body,
        getPrincipal(authentication)
    );
    return getResponse(response);
  }

  @CrossOrigin(origins = "*") // this is needed otherwise I get always an Invalid CORS Request message
  @RequestMapping(
      method = RequestMethod.PATCH,
      produces = JSON_API_MEDIA_TYPE,
      value = {"/{entity}/{id}", "/{entity}/{id}/relationships/{entity2}"})
  @Transactional
  public String jsonApiPatch(@RequestBody final String body,
                             final HttpServletRequest request,
                             final Authentication authentication) throws JsonApiException {
    ElideResponse response = elide.patch(JSON_API_MEDIA_TYPE,
        JSON_API_MEDIA_TYPE,
        getJsonApiPath(request),
        body,
        getPrincipal(authentication)
    );
    return getResponse(response);
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
    getResponse(response);
  }

  public String getResponse(ElideResponse response) throws JsonApiException {
    if (response.getResponseCode() / 100 != 2) {
      throw new JsonApiException("No Permission");
    }
    return response.getBody();
  }

  public static Object getPrincipal(final Authentication authentication) {
    return authentication != null ? authentication.getPrincipal() : null;
  }

  public static String getJsonApiPath(HttpServletRequest request) {
    return ((String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)).replace(PATH_PREFIX, "");
  }


  // Show valid json error message as result
  @ExceptionHandler(ValidationException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  @ResponseBody
  public Map<String, Serializable> processValidationError(ValidationException ex) {
    return errorResponse(ex.getMessage());
  }

  // Show valid json error message as result
  @ExceptionHandler(JsonApiException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  @ResponseBody
  public Map<String, Serializable> handleClanException(JsonApiException ex) {
    return errorResponse(ex.getMessage());
  }

  public Map<String, Serializable> errorResponse(String title) {
    ImmutableMap source = ImmutableMap.of("pointer", "");
    ImmutableMap<String, Serializable> error = ImmutableMap.of(
        "title", title,
        "source", source);
    return ImmutableMap.of("errors", new ImmutableMap[]{error});
  }
}
