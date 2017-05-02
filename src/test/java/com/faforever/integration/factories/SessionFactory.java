package com.faforever.integration.factories;

import com.faforever.api.client.ClientType;
import com.faforever.api.client.OAuthClient;
import com.faforever.api.client.OAuthClientRepository;
import com.faforever.api.data.domain.Player;
import com.faforever.api.data.domain.User;
import com.faforever.api.player.PlayerRepository;
import com.faforever.api.user.UserRepository;
import lombok.Data;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.Base64Utils;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SessionFactory {

  private static final String OAUTH_CLIENT_ID = "1234";
  private static final String OAUTH_SECRET = "secret";

  private static ObjectMapper objectMapper = new ObjectMapper();
  private static ShaPasswordEncoder shaPasswordEncoder = new ShaPasswordEncoder(256);

  public static Session createUserAndGetAccessToken(String login,
                                                    String password,
                                                    OAuthClientRepository oAuthClientRepository,
                                                    UserRepository userRepository,
                                                    PlayerRepository playerRepository,
                                                    MockMvc mvc) throws Exception {
    OAuthClient client = new OAuthClient()
        .setId(OAUTH_CLIENT_ID)
        .setName("test")
        .setClientSecret(OAUTH_SECRET)
        .setRedirectUris("test")
        .setDefaultRedirectUri("test")
        .setDefaultScope("test")
        .setClientType(ClientType.PUBLIC);
    oAuthClientRepository.save(client);

    User user = (User) new User()
        .setPassword(shaPasswordEncoder.encodePassword(password, null))
        .setLogin(login)
        .setEmail(login + "@faforever.com");
    userRepository.save(user);

    Player player = playerRepository.findOne(user.getId());

    String authorization = "Basic "
        + new String(Base64Utils.encode((OAUTH_CLIENT_ID + ":" + OAUTH_SECRET).getBytes()));
    ResultActions auth = mvc
        .perform(
            post("/oauth/token")
                .header("Authorization", authorization)
                .param("username", login)
                .param("password", password)
                .param("grant_type", "password"));
    auth.andExpect(status().isOk());
    JsonNode node = objectMapper.readTree(auth.andReturn().getResponse().getContentAsString());
    String token = "Bearer " + node.get("access_token").asText();
    return new Session().setPlayer(player).setToken(token);
  }

  @Data
  public static class Session {
    private Player player;
    private String Token;
  }
}
