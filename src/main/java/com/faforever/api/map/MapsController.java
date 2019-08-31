package com.faforever.api.map;

import com.faforever.api.config.FafApiProperties;
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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@RequestMapping(path = "/maps")
@Slf4j
@AllArgsConstructor
public class MapsController {
  private final MapService mapService;
  private final FafApiProperties fafApiProperties;
  private final ObjectMapper objectMapper;
  private final PlayerService playerService;


  @RequestMapping(path = "/validate", method = RequestMethod.GET, produces = APPLICATION_JSON_UTF8_VALUE)
  public ModelAndView showValidationForm(Map<String, Object> model) {
    return new ModelAndView("validate_map_metadata.html");
  }

  @ApiOperation("Validate map metadata")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Success"),
    @ApiResponse(code = 422, message = "Containing information about the errors in the payload")})
  @RequestMapping(
    path = "/validate",
    method = RequestMethod.POST,
    produces = APPLICATION_JSON_UTF8_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE
  )
  public void validateMapMetadata(@RequestBody MapValidationRequest mapValidationRequest) {
    mapService.validate(mapValidationRequest);
  }

  @ApiOperation("Upload a map")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Success"),
    @ApiResponse(code = 401, message = "Unauthorized"),
    @ApiResponse(code = 500, message = "Failure")})
  @RequestMapping(path = "/upload", method = RequestMethod.POST, produces = APPLICATION_JSON_UTF8_VALUE)
  public void uploadMap(@RequestParam("file") MultipartFile file,
                        @RequestParam("metadata") String jsonString,
                        Authentication authentication) throws IOException {
    if (file == null) {
      throw new ApiException(new Error(ErrorCode.UPLOAD_FILE_MISSING));
    }

    String extension = Files.getFileExtension(file.getOriginalFilename());
    if (!fafApiProperties.getMap().getAllowedExtensions().contains(extension)) {
      throw new ApiException(new Error(ErrorCode.UPLOAD_INVALID_FILE_EXTENSIONS, fafApiProperties.getMap().getAllowedExtensions()));
    }

    boolean ranked;
    try {
      JsonNode node = objectMapper.readTree(jsonString);
      ranked = node.path("isRanked").asBoolean(false);
    } catch (IOException e) {
      log.debug("Could not parse metadata", e);
      throw new ApiException(new Error(ErrorCode.INVALID_METADATA, e.getMessage()));
    }

    Player player = playerService.getPlayer(authentication);
    mapService.uploadMap(file.getInputStream(), file.getOriginalFilename(), player, ranked);
  }
}
