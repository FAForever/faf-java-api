package com.faforever.api.map;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Player;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.player.PlayerService;
import com.faforever.api.security.OAuthScope;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
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

  @Operation(summary = "Validate map name")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Information about derived names to be used in the scenario.lua"),
    @ApiResponse(responseCode = "422", description = "A list of reasons why the name is not valid.")
  })
  @RequestMapping(
    path = "/validateMapName",
    method = RequestMethod.POST,
    produces = APPLICATION_JSON_UTF8_VALUE
  )
  public MapNameValidationResponse validateMapName(@RequestParam("mapName") String mapName) {
    return mapService.requestMapNameValidation(mapName);
  }

  @Operation(summary = "Validate scenario.lua")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Valid without further information"),
    @ApiResponse(responseCode = "422", description = "A list of errors in the scenario.lua")})
  @RequestMapping(
    path = "/validateScenarioLua",
    method = RequestMethod.POST,
    produces = APPLICATION_JSON_UTF8_VALUE
  )
  public void validateScenarioLua(@RequestParam(name = "scenarioLua") String scenarioLua) {
    mapService.validateScenarioLua(scenarioLua);
  }

  @Operation(summary = "Upload a map")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(responseCode = "401", description = "Unauthorized"),
    @ApiResponse(responseCode = "500", description = "Failure")})
  @RequestMapping(path = "/upload", method = RequestMethod.POST, produces = APPLICATION_JSON_UTF8_VALUE)
  @PreAuthorize("hasScope('" + OAuthScope._UPLOAD_MAP + "')")
  public void uploadMap(@RequestParam("file") MultipartFile file,
                        @Deprecated @RequestParam(value = "metadata", required = false) String metadataJsonString,
                        @RequestPart(value = "metadata", required = false) MapUploadMetadata metadata,
                        Authentication authentication) throws IOException {
    if (metadataJsonString == null && metadata == null) {
      throw ApiException.of(ErrorCode.PARAMETER_MISSING, "metadata");
    }

    boolean ranked = false;
    Integer licenseId = null;
    if (metadataJsonString != null) {
      try {
        JsonNode node = objectMapper.readTree(metadataJsonString);
        ranked = node.path("isRanked").asBoolean(false);
      } catch (IOException e) {
        log.debug("Could not parse metadata", e);
        throw ApiException.of(ErrorCode.INVALID_METADATA, e.getMessage());
      }
    }
    if (metadata != null) {
      ranked = metadata.isRanked();
      licenseId = metadata.licenseId();
    }

    Player player = playerService.getPlayer(authentication);
    mapService.uploadMap(file.getInputStream(), file.getOriginalFilename(), player, ranked, licenseId);
  }
}
