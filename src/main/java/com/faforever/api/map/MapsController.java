package com.faforever.api.map;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Player;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.player.PlayerRepository;
import com.faforever.api.utils.AuthenticationHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.json.JSONException;
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
import java.util.Arrays;

@RestController
@RequestMapping(path = "/maps")
public class MapsController {
  private final PlayerRepository playerRepository;
  private final MapService mapService;
  private final FafApiProperties fafApiProperties;
  private final ObjectMapper objectMapper;

  @Inject
  public MapsController(PlayerRepository playerRepository, MapService mapService, FafApiProperties fafApiProperties, ObjectMapper objectMapper) {
    this.playerRepository = playerRepository;
    this.mapService = mapService;
    this.fafApiProperties = fafApiProperties;
    this.objectMapper = objectMapper;
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
    Player player = AuthenticationHelper.getPlayer(authentication, playerRepository);
    if (file == null) {
      throw new ApiException(new Error(ErrorCode.UPLOAD_FILE_MISSING));
    }
    String extension = Files.getFileExtension(file.getOriginalFilename());
    if (Arrays.asList(fafApiProperties.getMap().getAllowedExtensions()).stream().noneMatch(
        allowedExtension -> extension.equals(allowedExtension))) {
      throw new ApiException(new Error(ErrorCode.UPLOAD_INVALID_FILE_EXTENSION, fafApiProperties.getMap().getAllowedExtensions()));
    }
    boolean ranked;
    try {
      ranked = objectMapper.readTree(jsonString).get("is_ranked").asBoolean(false);
    } catch (JSONException e) {
      throw new ApiException(new Error(ErrorCode.MAP_NO_VALID_JSON_METADATA));
    }
    mapService.uploadMap(file.getBytes(), file.getOriginalFilename(), player, ranked);
  }

  @ExceptionHandler(ValidationException.class)
  public void handleClanException(ValidationException ex) {
    throw new ApiException(new Error(ErrorCode.VALIDATION_FAILED, ex.getMessage()));
  }
}
