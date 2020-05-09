package com.faforever.api.game;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(path = "/game")
@RequiredArgsConstructor
public class GameController {
  private final GameService gameService;

  @GetMapping("/{id}/replay")
  public void downloadReplay(HttpServletResponse httpServletResponse,
                             @PathVariable("id") int replayId) {
    httpServletResponse.setHeader(HttpHeaders.LOCATION, gameService.getReplayDownloadUrl(replayId));
    httpServletResponse.setStatus(HttpStatus.FOUND.value());
  }
}
