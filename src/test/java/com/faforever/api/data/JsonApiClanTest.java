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
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
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
  private AuthenticationManager authenticationManager;
  private OAuthClientRepository oAuthClientRepository;
  private Filter springSecurityFilterChain;
  private ObjectMapper objectMapper;

  public JsonApiClanTest() {
    objectMapper = new ObjectMapper();
  }

  @Inject
  public void init(WebApplicationContext context,
                   ClanRepository clanRepository,
                   UserRepository userRepository,
                   PlayerRepository playerRepository,
                   AuthenticationManager authenticationManager,
                   OAuthClientRepository oAuthClientRepository,
                   Filter springSecurityFilterChain) {
    this.context = context;
    this.clanRepository = clanRepository;
    this.userRepository = userRepository;
    this.playerRepository = playerRepository;
    this.authenticationManager = authenticationManager;
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

  @Test
  public void cannotKickLeaderFromClan() throws Exception {
    User user = (User) new User()
        .setPassword("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08")
        .setLogin("Dragonfire")
        .setEMail("dragonfire@faforever.com");
    userRepository.save(user);

    OAuthClient client = new OAuthClient()
        .setId("secret")
        .setClientSecret("secret")
        .setDefaultRedirectUri("test")
        .setDefaultScope("test");
    oAuthClientRepository.save(client);

    Player player = playerRepository.findOne(user.getId());
    Clan clan = new Clan().setLeader(player).setTag("123").setName("abcClanName");
    ClanMembership membership = new ClanMembership().setPlayer(player).setClan(clan);
    clan.setMemberships(Collections.singletonList(membership));
    clanRepository.save(clan);

//    Player dbPlayer = playerRepository.findOne(1);
//    User dbUser = userRepository.findOneByLoginIgnoreCase("Dragonfire");
//    UsernamePasswordAuthenticationToken login = new UsernamePasswordAuthenticationToken("Dragonfire", "test");
//    Authentication token = authenticationManager.authenticate(login);

    String authorization = "Basic "
        + new String(Base64Utils.encode("secret:secret".getBytes()));
    ResultActions auth = mvc
        .perform(
            post("/oauth/token")
                .header("Authorization", authorization)
                .param("username", "Dragonfire")
                .param("password", "test")
                .param("grant_type", "password"));
    JsonNode node = objectMapper.readTree(auth.andReturn().getResponse().getContentAsString());
    String accessToken = node.get("access_token").asText();
    ResultActions action = this.mvc.perform(delete("/data/clan_membership/1")
        .header("Authorization", "Bearer " + accessToken));
    action
        .andExpect(content().string("{\"errors\":[\"ForbiddenAccessException\"]}"))
        .andExpect(status().is(403));
  }

}
