package com.faforever.api.data;

import com.faforever.api.clan.ClanRepository;
import com.faforever.api.client.OAuthClient;
import com.faforever.api.client.OAuthClientRepository;
import com.faforever.api.data.domain.Clan;
import com.faforever.api.data.domain.ClanMembership;
import com.faforever.api.data.domain.Player;
import com.faforever.api.data.domain.User;
import com.faforever.api.player.PlayerRepository;
import com.faforever.api.user.UserRepository;
import lombok.SneakyThrows;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Base64Utils;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import javax.servlet.Filter;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JsonApiClanTest {
  private MockMvc mvc;
  private WebApplicationContext context;
  private ClanRepository clanRepository;
  private UserRepository userRepository;
  private PlayerRepository playerRepository;
  private OAuthClientRepository oAuthClientRepository;
  private Filter springSecurityFilterChain;
  private ObjectMapper objectMapper;
  private ShaPasswordEncoder shaPasswordEncoder;

  private static final String OAUTH_CLIENT_ID = "1234";
  private static final String OAUTH_SECRET = "secret";

  public JsonApiClanTest() {
    objectMapper = new ObjectMapper();
    shaPasswordEncoder = new ShaPasswordEncoder(256);
  }

  @Inject
  public void init(WebApplicationContext context,
                   ClanRepository clanRepository,
                   UserRepository userRepository,
                   PlayerRepository playerRepository,
                   OAuthClientRepository oAuthClientRepository,
                   Filter springSecurityFilterChain) {
    this.context = context;
    this.clanRepository = clanRepository;
    this.userRepository = userRepository;
    this.playerRepository = playerRepository;
    this.oAuthClientRepository = oAuthClientRepository;
    this.springSecurityFilterChain = springSecurityFilterChain;
  }

  @Before
  public void setUp() {
    mvc = MockMvcBuilders
        .webAppContextSetup(context)
        .addFilter(springSecurityFilterChain)
        .build();
  }

  @SneakyThrows
  public String createUserAndGetAccessToken(String login, String password) {
    OAuthClient client = new OAuthClient()
        .setId(OAUTH_CLIENT_ID)
        .setClientSecret(OAUTH_SECRET)
        .setDefaultRedirectUri("test")
        .setDefaultScope("test");
    oAuthClientRepository.save(client);

    User user = (User) new User()
        .setPassword(shaPasswordEncoder.encodePassword(password, null))
        .setLogin(login)
        .setEMail(login + "@faforever.com");
    userRepository.save(user);

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
    return "Bearer " + node.get("access_token").asText();
  }

  @Test
  @SneakyThrows
  public void cannotKickLeaderFromClan() {
    String accessToken = createUserAndGetAccessToken("Dragonfire", "foo");

    Player player = playerRepository.findOne(1);
    Clan clan = new Clan().setLeader(player).setTag("123").setName("abcClanName");
    ClanMembership membership = new ClanMembership().setPlayer(player).setClan(clan);
    clan.setMemberships(Collections.singletonList(membership));
    clanRepository.save(clan);

    this.mvc.perform(delete("/data/clan_membership/1")
        .header("Authorization", accessToken))
        .andExpect(content().string("{\"errors\":[\"ForbiddenAccessException\"]}"))
        .andExpect(status().is(403));
  }

}
