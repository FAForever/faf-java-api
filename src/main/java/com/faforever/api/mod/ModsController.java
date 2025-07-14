package com.faforever.api.mod;

import com.faforever.api.data.domain.Player;
import com.faforever.api.player.PlayerService;
import com.faforever.api.security.OAuthScope;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "/mods")
@Slf4j
@RequiredArgsConstructor
public class ModsController {

  private final PlayerService playerService;
  private final ModService modService;

  @Operation(summary = "Begin process of uploading a mod (as a zip file)")
  @GetMapping(path = "/upload/start", produces = APPLICATION_JSON_VALUE)
  @PreAuthorize("hasScope('" + OAuthScope._UPLOAD_MOD + "')")
  public UploadUrlResponse startUpload(Authentication authentication) {
    UUID requestId = UUID.randomUUID();
    String presignedUrl = modService.getPresignedS3Url(playerService.getPlayer(authentication), requestId);

    return new UploadUrlResponse(presignedUrl, requestId);
  }

  @Operation(summary = "Notify about mod upload completion")
  @PostMapping(path = "/upload/complete", produces = APPLICATION_JSON_VALUE)
  @PreAuthorize("hasScope('" + OAuthScope._UPLOAD_MOD + "')")
  public void completeUpload(@RequestBody ModUploadMetadata metadata,
                             Authentication authentication) throws IOException {
    Player uploader = playerService.getPlayer(authentication);

    log.info("User {} reported completed mod upload, request id {}", uploader.getId(), metadata.requestId());
    Path tempFile = modService.getModFromS3Location(uploader, metadata.requestId());

    try {
      log.debug("Process uploaded file @ {}", tempFile);
      modService.processUploadedMod(
        tempFile,
        tempFile.getFileName().toString(),
        playerService.getPlayer(authentication),
        metadata.licenseId(),
        metadata.repositoryUrl()
      );
    } finally {
      log.debug("Delete uploaded file for request id {}", metadata.requestId());
      modService.deleteModFromS3Location(uploader, metadata.requestId());
    }
  }

  @Deprecated
  @Operation(summary = "Upload a mod (as a zip file)")
  @PostMapping(path = "/upload", produces = APPLICATION_JSON_VALUE)
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
