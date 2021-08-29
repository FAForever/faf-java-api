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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
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

  private static final Pattern GOG_USERNAME_PATTERN = Pattern.compile("[\\w\\d-_!?]+");
  private static final Pattern PROFILE_USER_STATUS_PATTERN = Pattern.compile("window\\.profilesData\\.profileUserPreferences\\s=\\s\\{\"bio\":\"(.*?)\"");
  private static final String GOG_FA_GAME_ID = "1444785261";

  private final UserService userService;
  private final FafApiProperties properties;
  private final Gson gson = new Gson();

  public void linkGogAccount(String gogUsername, User user) { // TODO: rate limit?
    log.debug("Verifying and attempting to link user {} to gog account {}", user.getId(), gogUsername);

    if(! verifyGogUsername(gogUsername)) {
      throw ApiException.of(ErrorCode.GOG_LINK_INVALID_USERNAME);
    }

    if(!verifyProfileToken(gogUsername, user)) {
      throw ApiException.of(ErrorCode.GOG_LINK_PROFILE_TOKEN_NOT_SET);
    }

    if(!verifyGameOwnership(gogUsername)) {
      throw ApiException.of(ErrorCode.GOG_LINK_NO_FA_GAME);
    }

    // TODO: insert username into db
  }

  private boolean verifyGogUsername(String username) {
    return username.length() <= 30 && username.length() >= 3 && GOG_USERNAME_PATTERN.matcher(username).matches();
  }

  private boolean verifyProfileToken(String gogUsername, User user) {
    String targetToken = userService.buildGogToken(user);
    String profileStatus = getProfileStatus(gogUsername);

    return profileStatus.length() < 100 && Objects.equals(targetToken, profileStatus.trim());
  }

  private String getProfileStatus(String gogUsername) {
    String profilePageUrl = String.format(properties.getGog().getProfilePageUrl(), gogUsername);
    String profilePageHtml = new RestTemplate().getForObject(profilePageUrl, String.class);

    // The returned HTML is malformed, it contains two non-ended <link> tags, therefore we remove the header to produce valid XML
    int startOfHead = profilePageHtml.indexOf("<head>");
    int endOfHead = profilePageHtml.indexOf("</head>");
    String profilePageHtmlWithoutHead = profilePageHtml.substring(0, startOfHead) + profilePageHtml.substring(endOfHead + "</head>".length());

    try {
      DocumentBuilderFactory builder = DocumentBuilderFactory.newInstance();
      builder.setValidating(false);
      Document xmlDocument = builder.newDocumentBuilder().parse(new ByteArrayInputStream(profilePageHtmlWithoutHead.getBytes(StandardCharsets.UTF_8)));
      XPath xpath = XPathFactory.newInstance().newXPath();
      NodeList nodes = (NodeList) xpath.compile("/html/body/script").evaluate(xmlDocument, XPathConstants.NODE);

      for(int i = 0;i < nodes.getLength();i++) {
        String scriptText = nodes.item(i).getTextContent();
        Matcher matcher = PROFILE_USER_STATUS_PATTERN.matcher(scriptText);

        if(matcher.find()) {
          return matcher.group(1);
        }
      }

      throw ApiException.of(ErrorCode.GOG_LINK_PROFILE_TOKEN_NOT_SET);

    } catch(ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
      log.error("Couldn't parse GOG profile page XML for " + gogUsername, e);
      throw ApiException.of(ErrorCode.GOG_LINK_INTERNAL_SERVER_ERROR);
    }
  }



  private boolean verifyGameOwnership(String gogUsername) {
    List<GogGamesListPage.GogGamesListEntry> allGames = getAllGames(gogUsername); // TODO: data not available?

    return allGames.stream().anyMatch(game -> Objects.equals(game.game.id, GOG_FA_GAME_ID));
  }

  private List<GogGamesListPage.GogGamesListEntry> getAllGames(String gogUsername) {
    GogGamesListPage firstPage = getGamesListPage(gogUsername, 1);

    List<GogGamesListPage> pages = new ArrayList<>();
    pages.add(firstPage);
    for(int i = 2;i <= firstPage.getPages();i++) {
      pages.add(getGamesListPage(gogUsername, i));
    }

    return pages.stream().flatMap(page -> page.get_embedded().getItems().stream()).collect(Collectors.toList());
  }

  private GogGamesListPage getGamesListPage(String gogUsername, int page) {
    String gamesListUrl = String.format(properties.getGog().getGamesListUrl(), gogUsername, page);
//    return new RestTemplate().getForObject(gameListUrl, GogGamesListPage.class);
    try {
      String json = new RestTemplate().getForObject(gamesListUrl, String.class);
      if(json == null || json.isBlank()) {
        throw ApiException.of(ErrorCode.GOG_LINK_GAMES_NOT_PUBLIC);
      }
      return gson.fromJson(json, GogGamesListPage.class);
    } catch(HttpClientErrorException e) {
      if(e.getRawStatusCode() == HttpStatus.FORBIDDEN.value()) {
        throw ApiException.of(ErrorCode.GOG_LINK_GAMES_NOT_PUBLIC);
      } else if(e.getRawStatusCode() == HttpStatus.NOT_FOUND.value()) {
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
