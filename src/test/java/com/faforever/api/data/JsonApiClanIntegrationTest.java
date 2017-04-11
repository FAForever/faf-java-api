package com.faforever.api.data;

import com.faforever.api.clan.ClanMembershipRepository;
import com.faforever.api.clan.ClanRepository;
import com.faforever.api.client.ClientType;
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
import org.codehaus.jackson.node.ObjectNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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

import static com.github.nocatch.NoCatch.noCatch;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore("This needs to be cleaned up big time.")
public class JsonApiClanIntegrationTest {
  private static final String OAUTH_CLIENT_ID = "1234";
  private static final String OAUTH_SECRET = "secret";
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

  public JsonApiClanIntegrationTest() {
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

  // TODO @Dragonfire clean up the duplicated code
  @After
  public void tearDown() {
    clanMembershipRepository.deleteAll();
    clanRepository.deleteAll();
    userRepository.deleteAll();
    oAuthClientRepository.deleteAll();
    assertEquals(0, clanMembershipRepository.count());
    assertEquals(0, clanRepository.count());
    assertEquals(0, userRepository.count());
    assertEquals(0, oAuthClientRepository.count());
  }

  // TODO @Dragonfire clean up the duplicated code
  public String createUserAndGetAccessToken(String login, String password) throws Exception {
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

  private Player createPlayer(String login) throws Exception {
    User user = (User) new User()
        .setPassword("foo")
        .setLogin(login)
        .setEmail(login + "@faforever.com");
    userRepository.save(user);
    return playerRepository.findOne(user.getId());
  }

  @Test
  public void cannotKickLeaderFromClan() throws Exception {
    String accessToken = createUserAndGetAccessToken("Dragonfire", "foo");

    Clan clan = new Clan().setLeader(me).setTag("123").setName("abcClanName");
    ClanMembership membership = new ClanMembership().setPlayer(me).setClan(clan);
    clan.setMemberships(Collections.singletonList(membership));
    clanRepository.save(clan);

    assertEquals(1, clanMembershipRepository.count());
    this.mvc.perform(delete("/data/clanMembership/" + membership.getId())
        .header("Authorization", accessToken))
        .andExpect(content().string("{\"errors\":[\"ForbiddenAccessException\"]}"))
        .andExpect(status().is(403));
    assertEquals(1, clanMembershipRepository.count());
  }

  @Test
  public void cannotKickAsMember() throws Exception {
    String accessToken = createUserAndGetAccessToken("Dragonfire", "foo");

    Player bob = createPlayer("Bob");
    Clan clan = new Clan().setLeader(bob).setTag("123").setName("abcClanName");
    ClanMembership myMembership = new ClanMembership().setPlayer(me).setClan(clan);
    ClanMembership bobsMembership = new ClanMembership().setPlayer(bob).setClan(clan);
    clan.setMemberships(Arrays.asList(myMembership, bobsMembership));
    clanRepository.save(clan);

    assertEquals(2, clanMembershipRepository.count());
    this.mvc.perform(delete("/data/clanMembership/" + bobsMembership.getId())
        .header("Authorization", accessToken))
        .andExpect(content().string("{\"errors\":[\"ForbiddenAccessException\"]}"))
        .andExpect(status().is(403));
    assertEquals(2, clanMembershipRepository.count());
  }

  // TODO @Dragonfire clean up the duplicated code
  @Test
  public void canKickMember() throws Exception {
    String accessToken = createUserAndGetAccessToken("Dragonfire", "foo");

    Player bob = createPlayer("Bob");
    Clan clan = new Clan().setLeader(me).setTag("123").setName("abcClanName");
    ClanMembership myMembership = new ClanMembership().setPlayer(me).setClan(clan);
    ClanMembership bobsMembership = new ClanMembership().setPlayer(bob).setClan(clan);
    clan.setMemberships(Arrays.asList(myMembership, bobsMembership));
    clanRepository.save(clan);

    assertEquals(2, clanMembershipRepository.count());
    this.mvc.perform(delete("/data/clanMembership/" + bobsMembership.getId())
        .header("Authorization", accessToken))
        .andExpect(status().is(204));
    assertEquals(1, clanMembershipRepository.count());
    assertEquals(myMembership.getId(), clanMembershipRepository.findAll().get(0).getId());
  }

  // TODO @Dragonfire clean up the duplicated code
  @Test
  public void canLeaveClan() throws Exception {
    String accessToken = createUserAndGetAccessToken("Dragonfire", "foo");

    Player bob = createPlayer("Bob");
    Clan clan = new Clan().setLeader(bob).setTag("123").setName("abcClanName");
    ClanMembership myMembership = new ClanMembership().setPlayer(me).setClan(clan);
    ClanMembership bobsMembership = new ClanMembership().setPlayer(bob).setClan(clan);
    clan.setMemberships(Arrays.asList(myMembership, bobsMembership));
    clanRepository.save(clan);

    assertEquals(2, clanMembershipRepository.count());
    this.mvc.perform(delete("/data/clanMembership/" + myMembership.getId())
        .header("Authorization", accessToken))
        .andExpect(status().is(204));
    assertEquals(1, clanMembershipRepository.count());
    assertEquals(bobsMembership.getId(), clanMembershipRepository.findAll().get(0).getId());
  }

  @Test
  public void getFilteredPlayerForClanInvite() throws Exception {
    String[] players = new String[]{"Dragonfire", "DRAGON", "Fire of Dragon", "d r a g o n", "firedragon"};
    Arrays.stream(players).forEach(name -> noCatch(() -> createPlayer(name)));
    assertEquals(players.length, playerRepository.count());
    ResultActions action = this.mvc.perform(get("/data/player?filter=lowerCaseLogin==dragon*&sort=lowerCaseLogin"));

    JsonNode node = objectMapper.readTree(action.andReturn().getResponse().getContentAsString());

    assertEquals(2, node.get("data").size());
    assertEquals(players[1], node.get("data").get(0).get("attributes").get("login").asText());
    assertEquals(players[0], node.get("data").get(1).get("attributes").get("login").asText());

    action.andExpect(status().isOk());
  }

  private String generateTransferLeadershipContent(int clanId, int newLeaderId) throws Exception {
    ObjectNode node = this.objectMapper.createObjectNode();
    ObjectNode data = this.objectMapper.createObjectNode();
    ObjectNode relationships = this.objectMapper.createObjectNode();
    ObjectNode leaderData = this.objectMapper.createObjectNode();
    ObjectNode leader = this.objectMapper.createObjectNode();

    node.put("data", data);
    data.put("id", clanId);
    data.put("type", "clan");
    data.put("relationships", relationships);
    relationships.put("leader", leaderData);
    leaderData.put("data", leader);
    leader.put("id", newLeaderId);
    leader.put("type", "player");

    return node.toString();
  }

  @Test
  public void transferLeadership() throws Exception {
    String accessToken = createUserAndGetAccessToken("Leader", "foo");

    Player bob = createPlayer("Bob");
    Clan clan = new Clan().setLeader(me).setTag("123").setName("abcClanName");
    ClanMembership myMembership = new ClanMembership().setPlayer(me).setClan(clan);
    ClanMembership bobsMembership = new ClanMembership().setPlayer(bob).setClan(clan);
    clan.setMemberships(Arrays.asList(myMembership, bobsMembership));
    clanRepository.save(clan);

    String dataString = generateTransferLeadershipContent(clan.getId(), bob.getId());

    clan = clanRepository.findOne(clan.getId());
    assertEquals(me.getId(), clan.getLeader().getId());

    ResultActions action = this.mvc.perform(patch("/data/clan/" + clan.getId())
        .content(dataString)
        .header("Authorization", accessToken));
    action.andExpect(content().string(""))
        .andExpect(status().is(204));

    clan = clanRepository.findOne(clan.getId());
    assertEquals(bob.getId(), clan.getLeader().getId());
  }

  @Test
  public void transferLeadershipToOldLeader() throws Exception {
    String accessToken = createUserAndGetAccessToken("Leader", "foo");

    Clan clan = new Clan().setLeader(me).setTag("123").setName("abcClanName");
    ClanMembership myMembership = new ClanMembership().setPlayer(me).setClan(clan);
    clan.setMemberships(Collections.singletonList(myMembership));
    clanRepository.save(clan);

    String dataString = generateTransferLeadershipContent(clan.getId(), me.getId());

    clan = clanRepository.findOne(clan.getId());
    assertEquals(me.getId(), clan.getLeader().getId());

    ResultActions action = this.mvc.perform(patch("/data/clan/" + clan.getId())
        .content(dataString)
        .header("Authorization", accessToken));
    action.andExpect(content().string(""))
        .andExpect(status().is(204));

    clan = clanRepository.findOne(clan.getId());
    assertEquals(me.getId(), clan.getLeader().getId());
  }

  @Test
  public void transferLeadershipToNonClanMember() throws Exception {
    String accessToken = createUserAndGetAccessToken("Leader", "foo");

    Player bob = createPlayer("Bob");

    Clan clan = new Clan().setLeader(me).setTag("123").setName("abcClanName");
    ClanMembership myMembership = new ClanMembership().setPlayer(me).setClan(clan);
    clan.setMemberships(Collections.singletonList(myMembership));
    clanRepository.save(clan);

    String dataString = generateTransferLeadershipContent(clan.getId(), bob.getId());

    clan = clanRepository.findOne(clan.getId());
    assertEquals(me.getId(), clan.getLeader().getId());

    ResultActions action = this.mvc.perform(patch("/data/clan/" + clan.getId())
        .content(dataString)
        .header("Authorization", accessToken));
    action.andExpect(status().is(422));

    JsonNode resultNode = objectMapper.readTree(action.andReturn().getResponse().getContentAsString());
    assertEquals(1, resultNode.get("errors").size());
    assertEquals("Validation failed", resultNode.get("errors").get(0).get("title").asText());

    clan = clanRepository.findOne(clan.getId());
    assertEquals(me.getId(), clan.getLeader().getId());
  }

  @Test
  public void transferLeadershipAsNonLeader() throws Exception {
    String accessToken = createUserAndGetAccessToken("Leader", "foo");

    Player bob = createPlayer("Bob");
    Player charlie = createPlayer("Charlie");

    Clan clan = new Clan().setLeader(bob).setTag("123").setName("abcClanName");
    ClanMembership myMembership = new ClanMembership().setPlayer(me).setClan(clan);
    ClanMembership bobsMembership = new ClanMembership().setPlayer(bob).setClan(clan);
    ClanMembership charlieMembership = new ClanMembership().setPlayer(charlie).setClan(clan);
    clan.setMemberships(Arrays.asList(myMembership, bobsMembership, charlieMembership));
    clanRepository.save(clan);

    String dataString = generateTransferLeadershipContent(clan.getId(), charlie.getId());

    clan = clanRepository.findOne(clan.getId());
    assertEquals(bob.getId(), clan.getLeader().getId());

    ResultActions action = this.mvc.perform(patch("/data/clan/" + clan.getId())
        .content(dataString)
        .header("Authorization", accessToken));
    action.andExpect(content().string("{\"errors\":[\"ForbiddenAccessException\"]}"))
        .andExpect(status().is(403));

    clan = clanRepository.findOne(clan.getId());
    assertEquals(bob.getId(), clan.getLeader().getId());
  }

  @Test
  public void deleteClan() throws Exception {
    String accessToken = createUserAndGetAccessToken("Leader", "foo");

    Clan clan = new Clan().setLeader(me).setTag("123").setName("abcClanName");
    ClanMembership myMembership = new ClanMembership().setPlayer(me).setClan(clan);
    clan.setMemberships(Collections.singletonList(myMembership));
    clanRepository.save(clan);

    clan = clanRepository.findOne(clan.getId());
    assertEquals(me.getId(), clan.getLeader().getId());

    ResultActions action = this.mvc.perform(delete("/data/clan/" + clan.getId())
        .header("Authorization", accessToken));
    action.andExpect(content().string(""))
        .andExpect(status().is(204));

    assertEquals(0, clanRepository.count());
    assertEquals(0, clanMembershipRepository.count());
  }

}
