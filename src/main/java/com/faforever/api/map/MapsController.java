package com.faforever.api.map;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Player;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.player.PlayerService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import javax.validation.ValidationException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping(path = "/maps")
public class MapsController {
  private final MapService mapService;
  private final FafApiProperties fafApiProperties;
  private final ObjectMapper objectMapper;
  private final PlayerService playerService;

  @Inject
  public MapsController(MapService mapService, FafApiProperties fafApiProperties, ObjectMapper objectMapper, PlayerService playerService) {
    this.mapService = mapService;
    this.fafApiProperties = fafApiProperties;
    this.objectMapper = objectMapper;
    this.playerService = playerService;
  }

  @ApiOperation("Uploads a map")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Success", response = Void.class),
      @ApiResponse(code = 401, message = "Unauthorized"),
      @ApiResponse(code = 500, message = "Failure")})
  @RequestMapping(path = "/upload", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public void uploadMap(@RequestParam("file") MultipartFile file,
                        @RequestParam("metadata") String jsonString,
                        Authentication authentication) throws IOException {
    Player player = playerService.getPlayer(authentication);
    if (file == null) {
      throw new ApiException(new Error(ErrorCode.UPLOAD_FILE_MISSING));
    }

    String extension = Files.getFileExtension(file.getOriginalFilename());
    if (Arrays.stream(fafApiProperties.getMap().getAllowedExtensions()).noneMatch(extension::equals)) {
      throw new ApiException(new Error(ErrorCode.UPLOAD_INVALID_FILE_EXTENSION, fafApiProperties.getMap().getAllowedExtensions()));
    }

    boolean ranked;
    try {
      JsonNode node = objectMapper.readTree(jsonString);
      ranked = node.path("is_ranked").asBoolean(false);
    } catch (IOException e) {
      throw new ApiException(new Error(ErrorCode.MAP_NO_VALID_JSON_METADATA));
    }

    mapService.uploadMap(file.getBytes(), file.getOriginalFilename(), player, ranked);
  }

  @ExceptionHandler(ValidationException.class)
  public Map<String, Serializable> handleValidationException(ValidationException exception) {
    return errorResponse(ErrorCode.VALIDATION_FAILED.getTitle(), exception.getMessage());
  }

  private Map<String, Serializable> errorResponse(String title, String message) {
    ImmutableMap<String, Serializable> error = ImmutableMap.of(
        "title", title,
        "detail", message);
    return ImmutableMap.of("errors", new ImmutableMap[]{error});
  }
}
