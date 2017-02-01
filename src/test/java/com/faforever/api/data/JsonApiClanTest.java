package com.faforever.api.data;

import com.faforever.api.clan.ClanMembershipRepository;
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
import org.junit.After;
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
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JsonApiClanTest {
  private MockMvc mvc;
  private WebApplicationContext context;
  private Filter springSecurityFilterChain;

  private ClanRepository clanRepository;
  private UserRepository userRepository;
  private ClanMembershipRepository clanMembershipRepository;
  private PlayerRepository playerRepository;
  private OAuthClientRepository oAuthClientRepository;


  private ObjectMapper objectMapper;
  private ShaPasswordEncoder shaPasswordEncoder;

  private Player me;

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
                   Filter springSecurityFilterChain,
                   ClanMembershipRepository clanMembershipRepository) {
    this.context = context;
    this.clanRepository = clanRepository;
    this.userRepository = userRepository;
    this.playerRepository = playerRepository;
    this.oAuthClientRepository = oAuthClientRepository;
    this.springSecurityFilterChain = springSecurityFilterChain;
    this.clanMembershipRepository = clanMembershipRepository;
  }

  @Before
  public void setUp() {
    mvc = MockMvcBuilders
        .webAppContextSetup(context)
        .addFilter(springSecurityFilterChain)
        .build();
    me = null;
  }

  @After
  public void tearDown() {
    clanMembershipRepository.deleteAll();
    clanRepository.deleteAll();
    userRepository.deleteAll();
    oAuthClientRepository.deleteAll();
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
    me = playerRepository.findOne(user.getId());

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

  private Player createPlayer(String login) {
    User user = (User) new User()
        .setPassword("foo")
        .setLogin(login)
        .setEMail(login + "@faforever.com");
    userRepository.save(user);
    return playerRepository.findOne(user.getId());
  }

  @Test
  @SneakyThrows
  public void cannotKickLeaderFromClan() {
    String accessToken = createUserAndGetAccessToken("Dragonfire", "foo");

    Clan clan = new Clan().setLeader(me).setTag("123").setName("abcClanName");
    ClanMembership membership = new ClanMembership().setPlayer(me).setClan(clan);
    clan.setMemberships(Collections.singletonList(membership));
    clanRepository.save(clan);

    assertEquals(1, clanMembershipRepository.count());
    this.mvc.perform(delete("/data/clan_membership/" + membership.getId())
        .header("Authorization", accessToken))
        .andExpect(content().string("{\"errors\":[\"ForbiddenAccessException\"]}"))
        .andExpect(status().is(403));
    assertEquals(1, clanMembershipRepository.count());
  }

  @Test
  @SneakyThrows
  public void cannotKickAsMember() {
    String accessToken = createUserAndGetAccessToken("Dragonfire", "foo");

    Player bob = createPlayer("Bob");
    Clan clan = new Clan().setLeader(bob).setTag("123").setName("abcClanName");
    ClanMembership myMembership = new ClanMembership().setPlayer(me).setClan(clan);
    ClanMembership bobsMembership = new ClanMembership().setPlayer(bob).setClan(clan);
    clan.setMemberships(Arrays.asList(myMembership, bobsMembership));
    clanRepository.save(clan);

    assertEquals(2, clanMembershipRepository.count());
    this.mvc.perform(delete("/data/clan_membership/" + bobsMembership.getId())
        .header("Authorization", accessToken))
        .andExpect(content().string("{\"errors\":[\"ForbiddenAccessException\"]}"))
        .andExpect(status().is(403));
    assertEquals(2, clanMembershipRepository.count());
  }

  @Test
  @SneakyThrows
  public void canKickMember() {
    String accessToken = createUserAndGetAccessToken("Dragonfire", "foo");

    Player bob = createPlayer("Bob");
    Clan clan = new Clan().setLeader(me).setTag("123").setName("abcClanName");
    ClanMembership myMembership = new ClanMembership().setPlayer(me).setClan(clan);
    ClanMembership bobsMembership = new ClanMembership().setPlayer(bob).setClan(clan);
    clan.setMemberships(Arrays.asList(myMembership, bobsMembership));
    clanRepository.save(clan);

    assertEquals(2, clanMembershipRepository.count());
    this.mvc.perform(delete("/data/clan_membership/" + bobsMembership.getId())
        .header("Authorization", accessToken))
        .andExpect(status().is(204));
    assertEquals(1, clanMembershipRepository.count());
  }
}
