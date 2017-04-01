package com.faforever.api.mod;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.user.UserService;
import com.google.common.io.Files;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

@RestController
@RequestMapping(path = "/mods")
public class ModsController {

  private final UserService userService;
  private final ModService modService;
  private final FafApiProperties fafApiProperties;

  public ModsController(UserService userService, ModService modService, FafApiProperties fafApiProperties) {
    this.userService = userService;
    this.modService = modService;
    this.fafApiProperties = fafApiProperties;
  }

  @ApiOperation("Uploads a mod")
  @RequestMapping(path = "/upload", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public void uploadMod(@RequestParam("file") MultipartFile file, Authentication authentication) throws IOException {
    if (file == null) {
      throw new ApiException(new Error(ErrorCode.UPLOAD_FILE_MISSING));
    }

    String extension = Files.getFileExtension(file.getOriginalFilename());
    if (Arrays.stream(fafApiProperties.getMod().getAllowedExtensions()).noneMatch(extension::equals)) {
      throw new ApiException(new Error(ErrorCode.UPLOAD_INVALID_FILE_EXTENSION, fafApiProperties.getMap().getAllowedExtensions()));
    }

    Path tempFile = java.nio.file.Files.createTempFile("mod", ".tmp");
    file.transferTo(tempFile.getFileName().toFile());

    modService.processUploadedMod(tempFile, userService.getPlayer(authentication));
  }
}
