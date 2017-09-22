package com.faforever.api.map;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Map;
import com.faforever.api.data.domain.MapVersion;
import com.faforever.api.data.domain.Player;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.player.PlayerService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;

@RestController
@RequestMapping(path = "/maps")
@Slf4j
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
  @CacheEvict({Map.TYPE_NAME, MapVersion.TYPE_NAME})
  public void uploadMap(@RequestParam("file") MultipartFile file,
                        @RequestParam("metadata") String jsonString,
                        Authentication authentication) throws IOException {
    if (file == null) {
      throw new ApiException(new Error(ErrorCode.UPLOAD_FILE_MISSING));
    }

    String extension = Files.getFileExtension(file.getOriginalFilename());
    if (Arrays.stream(fafApiProperties.getMap().getAllowedExtensions()).noneMatch(extension::equals)) {
      throw new ApiException(new Error(ErrorCode.UPLOAD_INVALID_FILE_EXTENSION, (Object[]) fafApiProperties.getMap().getAllowedExtensions()));
    }

    boolean ranked;
    try {
      JsonNode node = objectMapper.readTree(jsonString);
      ranked = node.path("is_ranked").asBoolean(false);
    } catch (IOException e) {
      log.debug("Could not parse metadata", e);
      throw new ApiException(new Error(ErrorCode.MAP_UPLOAD_INVALID_METADATA, e.getMessage()));
    }

    Player player = playerService.getPlayer(authentication);
    mapService.uploadMap(file.getBytes(), file.getOriginalFilename(), player, ranked);
  }
}
