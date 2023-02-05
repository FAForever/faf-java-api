package com.faforever.api.user;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.config.FafApiProperties.Steam;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SteamService {
  private final FafApiProperties properties;

  String buildLoginUrl(String redirectUrl) {
    log.debug("Building steam login url for redirect url: {}", redirectUrl);

    return UriComponentsBuilder.fromHttpUrl(properties.getSteam().getLoginUrlFormat())
      .queryParam("openid.ns", "http://specs.openid.net/auth/2.0")
      .queryParam("openid.mode", "checkid_setup")
      .queryParam("openid.return_to", redirectUrl)
      .queryParam("openid.realm", properties.getSteam().getRealm())
      .queryParam("openid.identity", "http://specs.openid.net/auth/2.0/identifier_select")
      .queryParam("openid.claimed_id", "http://specs.openid.net/auth/2.0/identifier_select")
      .toUriString();
  }

  String parseSteamIdFromLoginRedirect(HttpServletRequest request) {
    log.trace("Parsing steam id from request: {}", request);

    String identityUrl = request.getParameter("openid.identity");
    return identityUrl.substring(identityUrl.lastIndexOf("/") + 1);
  }

  @SneakyThrows
  boolean ownsForgedAlliance(String steamId) {
    log.debug("Checking whether steamId owns Forged Alliance: {}", steamId);

    Steam steam = properties.getSteam();

    String answer = new RestTemplate().getForObject(steam.getGetOwnedGamesUrlFormat(), String.class, Map.of(
      "key", steam.getApiKey(),
      "steamId", steamId,
      "format", "json",
      "faAppId", steam.getForgedAllianceAppId()
    ));
    JSONObject result = new JSONObject(answer);

    JSONObject response = result.getJSONObject("response");
    return response.has("game_count") && response.getInt("game_count") > 0;
  }

  @SneakyThrows
  void validateSteamRedirect(HttpServletRequest request) {
    log.debug("Checking valid OpenID 2.0 redirect against Steam API, query string: {}", request.getQueryString());

    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(properties.getSteam().getLoginUrlFormat());
    request.getParameterMap().forEach(builder::queryParam);
    builder.replaceQueryParam("openid.mode", "check_authentication");

    // for some reason the + character doesn't get encoded
    String recodedUri = builder.toUriString().replace("+", "%2B");
    log.debug("Verification uri: {}", recodedUri);

    // the Spring RestTemplate still struggles with the + character so we use the default Java http client
    String result = HttpClient.newHttpClient()
      .send(HttpRequest.newBuilder(new URI(recodedUri)).build(), BodyHandlers.ofString())
      .body();

    if (result == null || !result.contains("is_valid:true")) {
      throw new IllegalArgumentException("Steam redirect could not be validated! Original response:\n" + result);
    } else {
      log.debug("Steam response successfully validated.");
    }
  }
}
