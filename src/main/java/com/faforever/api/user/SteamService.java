package com.faforever.api.user;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.config.FafApiProperties.Steam;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class SteamService {
  private final FafApiProperties properties;

  public SteamService(FafApiProperties properties) {
    this.properties = properties;
  }

  String buildLoginUrl(String redirectUrl) {
    log.trace("Building steam login url for redirect url: {}", redirectUrl);

    List<NameValuePair> steamArgs = Arrays.asList(
      new BasicNameValuePair("openid.ns", "http://specs.openid.net/auth/2.0"),
      new BasicNameValuePair("openid.mode", "checkid_setup"),
      new BasicNameValuePair("openid.return_to", redirectUrl),
      new BasicNameValuePair("openid.realm", properties.getSteam().getRealm()),
      new BasicNameValuePair("openid.identity", "http://specs.openid.net/auth/2.0/identifier_select"),
      new BasicNameValuePair("openid.claimed_id", "http://specs.openid.net/auth/2.0/identifier_select")
    );
    String queryArgs = URLEncodedUtils.format(steamArgs, StandardCharsets.UTF_8);

    return String.format(properties.getSteam().getLoginUrlFormat(), queryArgs);
  }

  String parseSteamIdFromLoginRedirect(HttpServletRequest request) {
    log.trace("Parsing steam id from request: {}", request);

    String identityUrl = request.getParameter("openid.identity");
    return identityUrl.substring(identityUrl.lastIndexOf("/") + 1, identityUrl.length());
  }

  @SneakyThrows
  boolean ownsForgedAlliance(String steamId) {
    log.debug("Checking whether steamId owns Forged Alliance: {}", steamId);

    Steam steam = properties.getSteam();

    RestTemplate restTemplate = new RestTemplate();
    List<NameValuePair> steamArgs = new ArrayList<>();
    steamArgs.add(new BasicNameValuePair("key", steam.getApiKey()));
    steamArgs.add(new BasicNameValuePair("steamid", steamId));
    steamArgs.add(new BasicNameValuePair("format", "json"));
    steamArgs.add(new BasicNameValuePair("appids_filter[0]", steam.getForgedAllianceAppId()));
    String queryArgs = URLEncodedUtils.format(steamArgs, StandardCharsets.UTF_8);

    String response = restTemplate.getForObject(String.format(steam.getGetOwnedGamesUrlFormat(), queryArgs), String.class);
    JSONObject result = new JSONObject(response);

    return result.getJSONObject("response").getInt("game_count") > 0;
  }
}
