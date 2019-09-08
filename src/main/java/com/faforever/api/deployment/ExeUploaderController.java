package com.faforever.api.deployment;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.google.common.io.Files;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@RequestMapping(path = "/exe")
@Slf4j
public class ExeUploaderController {
  private final FafApiProperties apiProperties;
  private final ExeUploaderService exeUploaderService;

  public ExeUploaderController(
    FafApiProperties apiProperties,
    ExeUploaderService exeUploaderService
  ) {
    this.apiProperties = apiProperties;
    this.exeUploaderService = exeUploaderService;
  }

  @ApiOperation("Upload an exe file")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Success"),
    @ApiResponse(code = 401, message = "Unauthorized"),
    @ApiResponse(code = 500, message = "Failure")})
  @RequestMapping(path = "/upload", method = RequestMethod.POST, produces = APPLICATION_JSON_UTF8_VALUE)
  public void upload(@RequestParam("file") MultipartFile file,
                     @RequestParam("modName") String modName,
                     @RequestParam("apiKey") String apiKey
  ) throws Exception {
    if (!apiKey.equals(apiProperties.getDeployment().getTestingExeUploadKey())) {
      throw new ApiException(new Error(ErrorCode.API_KEY_INVALID));
    }
    String extension = Files.getFileExtension(file.getOriginalFilename());
    if (!apiProperties.getDeployment().getAllowedExeExtension().equals(extension)) {
      throw new ApiException(
        new Error(ErrorCode.UPLOAD_INVALID_FILE_EXTENSIONS, apiProperties.getDeployment().getAllowedExeExtension())
      );
    }

    log.info("Uploading exe file '{}' to '{}' directory", file.getOriginalFilename(), modName);

    exeUploaderService.processUpload(file.getInputStream(), modName);
  }
}
