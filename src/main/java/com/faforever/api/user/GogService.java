package com.faforever.api.user;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.User;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.ErrorCode;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GogService {
  // TODO: what characters to allow?
  private static final Pattern GOG_USERNAME_PATTERN = Pattern.compile("[a-z\\d-_!?.]+"); // gets lower cased by controller, gog doesn't care about capitalization, this causes db entries to be unique
  private static final Pattern PROFILE_USER_STATUS_PATTERN = Pattern.compile("window\\.profilesData\\.profileUserPreferences\\s=\\s\\{\"bio\":\"(.*?)\"");
  private static final String GOG_FA_GAME_ID = "1444785261";

  private final FafApiProperties properties;
  private final Gson gson = new Gson();

  boolean verifyGogUsername(String username) {
    return username.length() <= 100 && username.length() >= 3 && GOG_USERNAME_PATTERN.matcher(username).matches();
  }

  boolean verifyProfileToken(String gogUsername, User user, String targetToken) {
    String profileStatus = getProfileStatus(gogUsername);

    return profileStatus.length() < 100 && Objects.equals(targetToken, profileStatus.trim());
  }

  private String getProfileStatus(String gogUsername) {
    String profilePageUrl = String.format(properties.getGog().getProfilePageUrl(), gogUsername);
    String profilePageHtml = null;

    try {
      profilePageHtml = new RestTemplate().getForObject(profilePageUrl, String.class);
    } catch (HttpClientErrorException e) {
      if (e.getRawStatusCode() == HttpStatus.NOT_FOUND.value()) {
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
    List<GogGamesListPage.GogGamesListEntry> allGames = getAllGames(gogUsername);

    return allGames.stream().anyMatch(game -> Objects.equals(game.game.id, GOG_FA_GAME_ID));
  }

  private List<GogGamesListPage.GogGamesListEntry> getAllGames(String gogUsername) {
    GogGamesListPage firstPage = getGamesListPage(gogUsername, 1);

    List<GogGamesListPage> pages = new ArrayList<>();
    pages.add(firstPage);
    for (int i = 2; i <= firstPage.getPages(); i++) {
      pages.add(getGamesListPage(gogUsername, i));
    }

    return pages.stream().flatMap(page -> page.get_embedded().getItems().stream()).collect(Collectors.toList());
  }

  private GogGamesListPage getGamesListPage(String gogUsername, int page) {
    String gamesListUrl = String.format(properties.getGog().getGamesListUrl(), gogUsername, page);

    try {
      String json = new RestTemplate().getForObject(gamesListUrl, String.class);
      if (json == null || json.isBlank()) {
        throw ApiException.of(ErrorCode.GOG_LINK_GAMES_NOT_PUBLIC);
      }
      return gson.fromJson(json, GogGamesListPage.class);
    } catch (HttpClientErrorException e) {
      if (e.getRawStatusCode() == HttpStatus.FORBIDDEN.value()) {
        throw ApiException.of(ErrorCode.GOG_LINK_GAMES_NOT_PUBLIC);
      } else if (e.getRawStatusCode() == HttpStatus.NOT_FOUND.value()) {
        throw ApiException.of(ErrorCode.GOG_LINK_USER_NOT_FOUND);
      } else {
        throw e;
      }
    }
  }

  @Data
  @AllArgsConstructor
  private static class GogGamesListPage implements Serializable {
    private int page;
    private int limit;
    private int pages;
    private int total;
    private GogGamesListLinkList _links;
    private GogGamesListEmbeddedList _embedded;

    @Data
    @AllArgsConstructor
    private static class GogGamesListLinkList implements Serializable {
      private GogGamesListLink self;
      private GogGamesListLink first;
      private GogGamesListLink last;

      @Data
      @AllArgsConstructor
      private static class GogGamesListLink implements Serializable {
        private String href;
      }
    }

    @Data
    @AllArgsConstructor
    private static class GogGamesListEmbeddedList implements Serializable {
      private List<GogGamesListEntry> items;
    }

    @Data
    @AllArgsConstructor
    private static class GogGamesListEntry implements Serializable {
      private GogGamesListEntryGameDetails game;
      private Map<String, GogGamesListEntryGameStats> stats;

      @Data
      @AllArgsConstructor
      private static class GogGamesListEntryGameDetails implements Serializable {
        private String id;
        private String title;
        private String url;
        private boolean achievementSupport;
        private String image;
      }

      @Data
      @AllArgsConstructor
      private static class GogGamesListEntryGameStats implements Serializable {
        private int playtime;
        private String lastSession;
      }
    }
  }

}
