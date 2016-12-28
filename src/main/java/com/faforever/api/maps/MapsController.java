package com.faforever.api.maps;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/maps")
public class MapsController {

  @ApiOperation("Uploads a map")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Success", response = Void.class),
      @ApiResponse(code = 401, message = "Unauthorized"),
      @ApiResponse(code = 500, message = "Failure")})
  @RequestMapping(path = "/upload", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public void uploadMap() {
    // FIXME: Do magic
  }
}
