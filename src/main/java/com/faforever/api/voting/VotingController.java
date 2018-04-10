package com.faforever.api.voting;

import com.faforever.api.data.domain.Vote;
import com.faforever.api.data.domain.VotingSubject;
import com.faforever.api.player.PlayerService;
import com.faforever.api.security.OAuthScope;
import com.google.common.base.Strings;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(path = VotingController.PATH)
public class VotingController {
  static final String PATH = "/voting";
  private static final String JSON_API_MEDIA_TYPE = "application/vnd.api+json";
  private final VotingService votingService;
  private final PlayerService playerService;

  @Inject
  public VotingController(VotingService votingService, PlayerService playerService) {
    this.votingService = votingService;
    this.playerService = playerService;
  }

  @ApiOperation(value = "Post a vote")
  @PreAuthorize("#oauth2.hasScope('" + OAuthScope._VOTE + "')")
  @RequestMapping(path = "/vote", method = RequestMethod.POST, produces = JSON_API_MEDIA_TYPE)
  public void post(@RequestBody Vote vote, Authentication authentication) {
    votingService.saveVote(vote, playerService.getPlayer(authentication));
  }

  @ApiOperation(value = "See if user can vote on a subject")
  @RequestMapping(path = "/votingSubjectsAbleToVote", method = RequestMethod.GET, produces = JSON_API_MEDIA_TYPE)
  public void votingSubjectsAbleTo(HttpServletResponse response, Authentication authentication, HttpServletRequest request) throws IOException {
    List<VotingSubject> votingSubjects = votingService.votingSubjectsAbleToVote(playerService.getPlayer(authentication));
    redirect(response, votingSubjects, request);
  }

  private void redirect(HttpServletResponse response, List<VotingSubject> votingSubjects, HttpServletRequest request) throws IOException {
    StringBuilder filter = new StringBuilder();
    votingSubjects.forEach(votingSubject -> {
      filter.append("id==");
      filter.append(votingSubject.getId());
      filter.append(",");
    });
    if (filter.toString().isEmpty()) {
      //FIXME: find a better way to make the redirect return an empty list
      filter.append("id==-1,");
    }
    //removes the last ,
    String filterString = filter.toString().substring(0, filter.toString().length() - 1);
    String queryString = request.getQueryString();

    if (!Strings.isNullOrEmpty(queryString)) {
      response.sendRedirect(String.format("/data/votingSubject?%s&filter=%s", queryString, filterString));
    } else {
      response.sendRedirect(String.format("/data/votingSubject?filter=%s", filterString));
    }
  }

  @ApiOperation(value = "See if user can vote on a subject")
  @RequestMapping(path = "/votingSubjectsAbleToVoteByUser", method = RequestMethod.GET, produces = JSON_API_MEDIA_TYPE)
  public void votingSubjectsAbleToVoteByUser(@RequestParam(value = "userId") int userId, HttpServletResponse response, HttpServletRequest request) throws IOException {
    List<VotingSubject> votingSubjects = votingService.votingSubjectsAbleToVote(userId);
    redirect(response, votingSubjects, request);
  }
}

