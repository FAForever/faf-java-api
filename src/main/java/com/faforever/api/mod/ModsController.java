package com.faforever.api.mod;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.player.PlayerService;
import com.google.common.io.Files;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@RequestMapping(path = "/mods")
public class ModsController {

  private final PlayerService playerService;
  private final ModService modService;
  private final FafApiProperties fafApiProperties;

  public ModsController(PlayerService playerService, ModService modService, FafApiProperties fafApiProperties) {
    this.playerService = playerService;
    this.modService = modService;
    this.fafApiProperties = fafApiProperties;
  }

  @ApiOperation("Upload a mod")
  @RequestMapping(path = "/upload", method = RequestMethod.POST, produces = APPLICATION_JSON_UTF8_VALUE)
  public void uploadMod(@RequestParam("file") MultipartFile file) throws IOException {
    if (file == null) {
      throw new ApiException(new Error(ErrorCode.UPLOAD_FILE_MISSING));
    }

    String extension = Files.getFileExtension(file.getOriginalFilename());
    if (!fafApiProperties.getMod().getAllowedExtensions().contains(extension)) {
      throw new ApiException(new Error(ErrorCode.UPLOAD_INVALID_FILE_EXTENSIONS, fafApiProperties.getMod().getAllowedExtensions()));
    }

    Path tempFile = java.nio.file.Files.createTempFile("mod", ".tmp");
    file.transferTo(tempFile.getFileName().toFile());

    modService.processUploadedMod(tempFile, playerService.getCurrentPlayer());
  }
}
