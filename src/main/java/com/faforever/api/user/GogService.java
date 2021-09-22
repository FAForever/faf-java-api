package com.faforever.api.user;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.User;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.ErrorCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class GogService {
  private static final Pattern GOG_USERNAME_PATTERN = Pattern.compile("[a-zA-Z\\d-_!?.]+");
  private static final Pattern PROFILE_USER_STATUS_PATTERN = Pattern.compile("window\\.profilesData\\.profileUserPreferences\\s=\\s\\{\"bio\":\"(.*?)\"");
  private static final String GOG_FA_GAME_ID = "1444785261";

  private final FafApiProperties properties;
  private final RestTemplate restTemplate;

  boolean verifyGogUsername(String username) {
    return username.length() <= 30 && username.length() >= 3 && GOG_USERNAME_PATTERN.matcher(username).matches();
  }

  boolean verifyProfileToken(String gogUsername, User user, String targetToken) {
    String profileStatus = getProfileStatus(gogUsername);

    return profileStatus.length() < 100 && Objects.equals(targetToken, profileStatus.trim());
  }

  private String getProfileStatus(String gogUsername) {
    String profilePageUrl = String.format(properties.getGog().getProfilePageUrl(), gogUsername);
    String profilePageHtml;

    try {
      profilePageHtml = restTemplate.getForObject(profilePageUrl, String.class);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        throw ApiException.of(ErrorCode.GOG_LINK_PROFILE_NOT_PUBLIC);
      } else {
        log.error("Couldn't retrieve gog profile page for {}", profilePageUrl);
        throw ApiException.of(ErrorCode.GOG_LINK_INTERNAL_SERVER_ERROR);
      }
    }

    if (profilePageHtml == null) {
      log.error("Couldn't retrieve gog profile page for {}", profilePageUrl);
      throw ApiException.of(ErrorCode.GOG_LINK_INTERNAL_SERVER_ERROR);
    }

    Document document = Jsoup.parse(profilePageHtml);
    for (Element element : document.body().getElementsByTag("script")) {
      String scriptText = element.data();
      Matcher matcher = PROFILE_USER_STATUS_PATTERN.matcher(scriptText);

      if (matcher.find()) {
        return matcher.group(1);
      }
    }

    throw ApiException.of(ErrorCode.GOG_LINK_PROFILE_NOT_PUBLIC);
  }

  boolean verifyGameOwnership(String gogUsername) {
    int numberOfPages = 1;

    for (int i = 1; i <= numberOfPages; i++) {
      GogGamesListPage nextPage = getGamesListPage(gogUsername, i);
      boolean gameFound = nextPage.embeddedGames().items().stream()
        .anyMatch(game -> Objects.equals(game.game.id, GOG_FA_GAME_ID));

      if (gameFound) {
        return true;
      }

      numberOfPages = nextPage.pages();
    }

    return false;
  }


  private GogGamesListPage getGamesListPage(String gogUsername, int page) {
    String gamesListUrl = String.format(properties.getGog().getGamesListUrl(), gogUsername, page);

    try {
      return restTemplate.getForObject(gamesListUrl, GogGamesListPage.class);
    } catch (HttpClientErrorException e) {
      throw switch (e.getStatusCode()) {
        case FORBIDDEN -> ApiException.of(ErrorCode.GOG_LINK_GAMES_NOT_PUBLIC);
        case NOT_FOUND -> ApiException.of(ErrorCode.GOG_LINK_USER_NOT_FOUND);
        default -> e;
      };
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private static record GogGamesListPage(int pages, @JsonProperty("_embedded") GogGamesListEmbeddedList embeddedGames) {
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private static record GogGamesListEmbeddedList(List<GogGamesListEntry> items) {
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private static record GogGamesListEntry(GogGamesListEntryGameDetails game) {

  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private static record GogGamesListEntryGameDetails(String id) {
  }

}
