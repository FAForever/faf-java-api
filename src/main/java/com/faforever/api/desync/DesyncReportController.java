package com.faforever.api.desync;

import com.faforever.api.player.PlayerService;
import com.faforever.api.security.OAuthScope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping(path = "/game/{gameId}/desync")
@RequiredArgsConstructor
public class DesyncReportController {

  private final PlayerService playerService;
  private final DesyncReportService desyncReportService;

  @Operation(summary = "Upload the desync report (a zip file) of the authenticated player for a game",
    description = "A repeated upload for the same game replaces the previous report.")
  @ApiResponse(responseCode = "204", description = "Report stored")
  @ApiResponse(responseCode = "403", description = "The player did not participate in this game")
  @ApiResponse(responseCode = "422", description = "The file is empty or larger than the allowed size")
  @PostMapping(path = "/report", consumes = "multipart/form-data")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasScope('" + OAuthScope._UPLOAD_DESYNC_REPORT + "')")
  public void uploadReport(Authentication authentication,
                           @PathVariable("gameId") int gameId,
                           @RequestParam("file") MultipartFile file) throws IOException {
    desyncReportService.storeReport(playerService.getPlayer(authentication).getId(), gameId, file);
  }
}
