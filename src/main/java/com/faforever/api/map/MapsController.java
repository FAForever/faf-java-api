package com.faforever.api.map;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Player;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.player.PlayerRepository;
import com.faforever.api.utils.AuthenticationHelper;
import com.faforever.api.utils.JsonApiErrorBuilder;
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
import javax.servlet.http.HttpServletResponse;
import javax.validation.ValidationException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping(path = "/maps")
public class MapsController {
  private final PlayerRepository playerRepository;
  private final MapService mapService;
  private final FafApiProperties fafApiProperties;

  @Inject
  public MapsController(PlayerRepository playerRepository, MapService mapService, FafApiProperties fafApiProperties) {
    this.playerRepository = playerRepository;
    this.mapService = mapService;
    this.fafApiProperties = fafApiProperties;
  }

  @ApiOperation("Uploads a map")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Success", response = Void.class),
      @ApiResponse(code = 401, message = "Unauthorized"),
      @ApiResponse(code = 500, message = "Failure")})
  @RequestMapping(path = "/upload", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public void uploadMap(@RequestParam("file") MultipartFile file,
                        Authentication authentication) throws IOException {
    Player player = AuthenticationHelper.getPlayer(authentication, playerRepository);
    if (file == null) {
      throw new ApiException(new Error(ErrorCode.UPLOAD_FILE_MISSING));
    }
    String extension = Files.getNameWithoutExtension(file.getOriginalFilename());
    if (Arrays.asList(fafApiProperties.getMap().getAllowedExtensions()).stream().noneMatch(
        allowedExtension -> extension.equals(allowedExtension))) {
      throw new ApiException(new Error(ErrorCode.UPLOAD_INVALID_FILE_EXTENSION, fafApiProperties.getMap().getAllowedExtensions()));
    }
    // TODO: read metadata json and parse isranked
    mapService.uploadMap(file.getBytes(), file.getOriginalFilename(), player, true);
  }

  // Show error message as result
  @ExceptionHandler(ValidationException.class)
  public Map<String, Serializable> handleClanException(ValidationException ex, HttpServletResponse response) throws IOException {
    return new JsonApiErrorBuilder().setTitle(ex.getMessage()).build();
  }
}
