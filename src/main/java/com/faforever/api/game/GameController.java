package com.faforever.api.game;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(path = "/game")
@RequiredArgsConstructor
public class GameController {
  private final GameService gameService;

  @Operation(summary = "Redirects to the download location of the replay file of a game")
  @ApiResponse(responseCode = "302", description = "Redirect to the replay file")
  @GetMapping("/{id}/replay")
  public void downloadReplay(HttpServletResponse httpServletResponse,
                             @PathVariable("id") int replayId) {
    httpServletResponse.setHeader(HttpHeaders.LOCATION, gameService.getReplayDownloadUrl(replayId));
    httpServletResponse.setStatus(HttpStatus.FOUND.value());
  }
}
