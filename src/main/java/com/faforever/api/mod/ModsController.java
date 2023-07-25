package com.faforever.api.mod;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.player.PlayerService;
import com.faforever.api.security.OAuthScope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@RequestMapping(path = "/mods")
@RequiredArgsConstructor
public class ModsController {

  private final PlayerService playerService;
  private final ModService modService;
  private final FafApiProperties fafApiProperties;

  @Operation(summary = "Upload a mod")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(responseCode = "401", description = "Unauthorized"),
    @ApiResponse(responseCode = "500", description = "Failure")})
  @RequestMapping(path = "/upload", method = RequestMethod.POST, produces = APPLICATION_JSON_UTF8_VALUE)
  @PreAuthorize("hasScope('" + OAuthScope._UPLOAD_MOD + "')")
  public void uploadMod(
    @RequestParam("file") MultipartFile file,
    @RequestPart(value = "metadata", required = false) ModUploadMetadata metadata, //TODO make required when implemented by client
    Authentication authentication) throws IOException {

    Path tempFile = java.nio.file.Files.createTempFile("mod", ".tmp");
    file.transferTo(tempFile.toFile());

    modService.processUploadedMod(
      tempFile,
      file.getOriginalFilename(),
      playerService.getPlayer(authentication),
      metadata != null ? metadata.licenseId() : null,
      metadata != null ? metadata.repositoryUrl() : null
    );
  }
}
