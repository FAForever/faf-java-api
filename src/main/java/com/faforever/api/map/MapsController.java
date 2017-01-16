package com.faforever.api.map;

import com.faforever.api.data.domain.Player;
import com.faforever.api.player.PlayerRepository;
import com.faforever.api.utils.AuthenticationHelper;
import com.faforever.api.utils.JsonApiErrorBuilder;
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
import java.util.Map;

@RestController
@RequestMapping(path = "/maps")
public class MapsController {
  private final PlayerRepository playerRepository;
  private final MapService mapService;

  @Inject
  public MapsController(PlayerRepository playerRepository, MapService mapService) {
    this.playerRepository = playerRepository;
    this.mapService = mapService;
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
      throw new ValidationException("Please send the map with the 'key' file as Multipart File");
    }
    if (!file.getOriginalFilename().endsWith(".zip")) {
      throw new ValidationException("We only support zip files");
    }
    mapService.uploadMap(file.getBytes(), file.getOriginalFilename(), player);
  }

  // Show error message as result
  @ExceptionHandler(ValidationException.class)
  public Map<String, Serializable> handleClanException(ValidationException ex, HttpServletResponse response) throws IOException {
    return new JsonApiErrorBuilder().setTitle(ex.getMessage()).build();
  }
}
