package com.faforever.api.desync;

import com.faforever.api.player.PlayerService;
import com.faforever.api.security.OAuthScope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "/game/{gameId}/desync")
@RequiredArgsConstructor
public class DesyncReportController {

  private final PlayerService playerService;
  private final DesyncReportService desyncReportService;

  @Operation(summary = "Get a presigned url to upload the desync report of the authenticated player for a game",
    description = "The report has to be uploaded with a PUT request to the returned url, with exactly 'size' bytes as body and "
      + "the returned headers. A repeated request for the same game replaces the previous report.")
  @ApiResponse(responseCode = "200", description = "Upload url created")
  @ApiResponse(responseCode = "403", description = "The player did not participate in this game")
  @ApiResponse(responseCode = "422", description = "The size is not within the allowed range")
  @GetMapping(path = "/upload", produces = APPLICATION_JSON_VALUE)
  @PreAuthorize("hasScope('" + OAuthScope._UPLOAD_DESYNC_REPORT + "')")
  public DesyncReportUploadResponse getUploadUrl(Authentication authentication,
                                                 @PathVariable("gameId") int gameId,
                                                 @RequestParam("size") long size) {
    return desyncReportService.createUploadUrl(playerService.getPlayer(authentication).getId(), gameId, size);
  }
}
