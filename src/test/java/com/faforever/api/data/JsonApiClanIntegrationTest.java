package com.faforever.api.data;

import com.faforever.api.data.domain.Clan;
import com.faforever.api.data.domain.ClanMembership;
import com.faforever.api.data.domain.Player;
import com.faforever.integration.TestDatabase;
import com.faforever.integration.factories.PlayerFactory;
import com.faforever.integration.factories.SessionFactory;
import com.faforever.integration.factories.SessionFactory.Session;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(TestDatabase.class)
public class JsonApiClanIntegrationTest {

  private MockMvc mvc;
  private WebApplicationContext context;
  private Filter springSecurityFilterChain;

  private TestDatabase database;

  private ObjectMapper objectMapper;

  public JsonApiClanIntegrationTest() {
    objectMapper = new ObjectMapper();
  }

  @Inject
  public void init(TestDatabase database, WebApplicationContext context, Filter springSecurityFilterChain) {
    this.context = context;
    this.springSecurityFilterChain = springSecurityFilterChain;
    this.database = database;
  }

  @Before
  public void setUp() {
    mvc = MockMvcBuilders
        .webAppContextSetup(context)
        .addFilter(springSecurityFilterChain)
        .build();
    database.assertEmptyDatabase();
  }

  @After
  public void tearDown() {
    // TODO: This is needed, because Elide has some problems with @Transactional annotation
    database.tearDown();
  }


  @Test
  public void cannotKickLeaderFromClan() throws Exception {
    Session session = SessionFactory.createUserAndGetAccessToken(database, mvc);
    Player player = session.getPlayer();

    Clan clan = new Clan().setLeader(player).setTag("123").setName("abcClanName");
    ClanMembership membership = new ClanMembership().setPlayer(player).setClan(clan);
    clan.setMemberships(Collections.singletonList(membership));
    database.getClanRepository().save(clan);

    assertEquals(1, database.getClanMembershipRepository().count());
    this.mvc.perform(delete("/data/clanMembership/" + membership.getId())
        .header("Authorization", session.getToken()))
        .andExpect(content().string("{\"errors\":[\"ForbiddenAccessException\"]}"))
        .andExpect(status().is(403));
    assertEquals(1, database.getClanMembershipRepository().count());
  }

  @Test
  public void cannotKickAsMember() throws Exception {
    Session session = SessionFactory.createUserAndGetAccessToken(database, mvc);

    Player bob = PlayerFactory.createPlayer("Bob", database);
    Clan clan = new Clan().setLeader(bob).setTag("123").setName("abcClanName");
    ClanMembership myMembership = new ClanMembership().setPlayer(session.getPlayer()).setClan(clan);
    ClanMembership bobsMembership = new ClanMembership().setPlayer(bob).setClan(clan);
    clan.setMemberships(Arrays.asList(myMembership, bobsMembership));
    database.getClanRepository().save(clan);

    assertEquals(2, database.getClanMembershipRepository().count());
    this.mvc.perform(delete("/data/clanMembership/" + bobsMembership.getId())
        .header("Authorization", session.getToken()))
        .andExpect(content().string("{\"errors\":[\"ForbiddenAccessException\"]}"))
        .andExpect(status().is(403));
    assertEquals(2, database.getClanMembershipRepository().count());
  }

  @Test
  public void canKickMember() throws Exception {
    Session session = SessionFactory.createUserAndGetAccessToken(database, mvc);
    Player player = session.getPlayer();

    Player bob = PlayerFactory.createPlayer("Bob", database);
    Clan clan = new Clan().setLeader(player).setTag("123").setName("abcClanName");
    ClanMembership myMembership = new ClanMembership().setPlayer(player).setClan(clan);
    ClanMembership bobsMembership = new ClanMembership().setPlayer(bob).setClan(clan);
    clan.setMemberships(Arrays.asList(myMembership, bobsMembership));
    database.getClanRepository().save(clan);

    assertEquals(2, database.getClanMembershipRepository().count());
    this.mvc.perform(delete("/data/clanMembership/" + bobsMembership.getId())
        .header("Authorization", session.getToken()))
        .andExpect(status().is(204));
    assertEquals(1, database.getClanMembershipRepository().count());
    assertEquals(myMembership.getId(), database.getClanMembershipRepository().findAll().get(0).getId());
  }


  @Test
  public void canLeaveClan() throws Exception {
    Session session = SessionFactory.createUserAndGetAccessToken(database, mvc);
    Player player = session.getPlayer();

    Player bob = PlayerFactory.createPlayer("Bob", database);
    Clan clan = new Clan().setLeader(bob).setTag("123").setName("abcClanName");
    ClanMembership myMembership = new ClanMembership().setPlayer(player).setClan(clan);
    ClanMembership bobsMembership = new ClanMembership().setPlayer(bob).setClan(clan);
    clan.setMemberships(Arrays.asList(myMembership, bobsMembership));
    database.getClanRepository().save(clan);

    assertEquals(2, database.getClanMembershipRepository().count());
    this.mvc.perform(delete("/data/clanMembership/" + myMembership.getId())
        .header("Authorization", session.getToken()))
        .andExpect(status().is(204));
    assertEquals(1, database.getClanMembershipRepository().count());
    assertEquals(bobsMembership.getId(), database.getClanMembershipRepository().findAll().get(0).getId());
  }

  @Test
  public void getFilteredPlayerForClanInvite() throws Exception {
    String[] players = new String[]{"Dragonfire", "DRAGON", "Fire of Dragon", "d r a g o n", "firedragon"};
    Arrays.stream(players).forEach(name -> noCatch(() -> PlayerFactory.createPlayer(name, database)));
    assertEquals(players.length, database.getPlayerRepository().count());
    ResultActions action = this.mvc.perform(get("/data/player?filter=login==dragon*&sort=login"));

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
    Session session = SessionFactory.createUserAndGetAccessToken("Leader", "foo", database, mvc);
    Player player = session.getPlayer();

    Player bob = PlayerFactory.createPlayer("Bob", database);
    Clan clan = new Clan().setLeader(player).setTag("123").setName("abcClanName");
    ClanMembership myMembership = new ClanMembership().setPlayer(player).setClan(clan);
    ClanMembership bobsMembership = new ClanMembership().setPlayer(bob).setClan(clan);
    clan.setMemberships(Arrays.asList(myMembership, bobsMembership));
    database.getClanRepository().save(clan);

    String dataString = generateTransferLeadershipContent(clan.getId(), bob.getId());

    clan = database.getClanRepository().findOne(clan.getId());
    assertEquals(player.getId(), clan.getLeader().getId());

    ResultActions action = this.mvc.perform(patch("/data/clan/" + clan.getId())
        .content(dataString)
        .header("Authorization", session.getToken()));
    action.andExpect(content().string(""))
        .andExpect(status().is(204));

    clan = database.getClanRepository().findOne(clan.getId());
    assertEquals(bob.getId(), clan.getLeader().getId());
  }

  @Test
  public void transferLeadershipToOldLeader() throws Exception {
    Session session = SessionFactory.createUserAndGetAccessToken("Leader", "foo", database, mvc);
    Player player = session.getPlayer();

    Clan clan = new Clan().setLeader(player).setTag("123").setName("abcClanName");
    ClanMembership myMembership = new ClanMembership().setPlayer(player).setClan(clan);
    clan.setMemberships(Collections.singletonList(myMembership));
    database.getClanRepository().save(clan);

    String dataString = generateTransferLeadershipContent(clan.getId(), player.getId());

    clan = database.getClanRepository().findOne(clan.getId());
    assertEquals(player.getId(), clan.getLeader().getId());

    ResultActions action = this.mvc.perform(patch("/data/clan/" + clan.getId())
        .content(dataString)
        .header("Authorization", session.getToken()));
    action.andExpect(content().string(""))
        .andExpect(status().is(204));

    clan = database.getClanRepository().findOne(clan.getId());
    assertEquals(player.getId(), clan.getLeader().getId());
  }

  @Test
  public void transferLeadershipToNonClanMember() throws Exception {
    Session session = SessionFactory.createUserAndGetAccessToken("Leader", "foo", database, mvc);
    Player player = session.getPlayer();

    Player bob = PlayerFactory.createPlayer("Bob", database);

    Clan clan = new Clan().setLeader(player).setTag("123").setName("abcClanName");
    ClanMembership myMembership = new ClanMembership().setPlayer(player).setClan(clan);
    clan.setMemberships(Collections.singletonList(myMembership));
    database.getClanRepository().save(clan);

    String dataString = generateTransferLeadershipContent(clan.getId(), bob.getId());

    clan = database.getClanRepository().findOne(clan.getId());
    assertEquals(player.getId(), clan.getLeader().getId());

    ResultActions action = this.mvc.perform(patch("/data/clan/" + clan.getId())
        .content(dataString)
        .header("Authorization", session.getToken()));
    action.andExpect(status().is(422));

    JsonNode resultNode = objectMapper.readTree(action.andReturn().getResponse().getContentAsString());
    assertEquals(1, resultNode.get("errors").size());
    assertEquals("Validation failed", resultNode.get("errors").get(0).get("title").asText());

    clan = database.getClanRepository().findOne(clan.getId());
    assertEquals(player.getId(), clan.getLeader().getId());
  }

  @Test
  public void transferLeadershipAsNonLeader() throws Exception {
    Session session = SessionFactory.createUserAndGetAccessToken("Leader", "foo", database, mvc);
    Player player = session.getPlayer();

    Player bob = PlayerFactory.createPlayer("Bob", database);
    Player charlie = PlayerFactory.createPlayer("Charlie", database);

    Clan clan = new Clan().setLeader(bob).setTag("123").setName("abcClanName");
    ClanMembership myMembership = new ClanMembership().setPlayer(player).setClan(clan);
    ClanMembership bobsMembership = new ClanMembership().setPlayer(bob).setClan(clan);
    ClanMembership charlieMembership = new ClanMembership().setPlayer(charlie).setClan(clan);
    clan.setMemberships(Arrays.asList(myMembership, bobsMembership, charlieMembership));
    database.getClanRepository().save(clan);

    String dataString = generateTransferLeadershipContent(clan.getId(), charlie.getId());

    clan = database.getClanRepository().findOne(clan.getId());
    assertEquals(bob.getId(), clan.getLeader().getId());

    ResultActions action = this.mvc.perform(patch("/data/clan/" + clan.getId())
        .content(dataString)
        .header("Authorization", session.getToken()));
    action.andExpect(content().string("{\"errors\":[\"ForbiddenAccessException\"]}"))
        .andExpect(status().is(403));

    clan = database.getClanRepository().findOne(clan.getId());
    assertEquals(bob.getId(), clan.getLeader().getId());
  }

  @Test
  public void deleteClan() throws Exception {
    Session session = SessionFactory.createUserAndGetAccessToken("Leader", "foo", database, mvc);
    Player player = session.getPlayer();

    Clan clan = new Clan().setLeader(player).setTag("123").setName("abcClanName");
    ClanMembership myMembership = new ClanMembership().setPlayer(player).setClan(clan);
    clan.setMemberships(Collections.singletonList(myMembership));
    database.getClanRepository().save(clan);

    clan = database.getClanRepository().findOne(clan.getId());
    assertEquals(player.getId(), clan.getLeader().getId());

    ResultActions action = this.mvc.perform(delete("/data/clan/" + clan.getId())
        .header("Authorization", session.getToken()));
    action.andExpect(content().string(""))
        .andExpect(status().is(204));

    assertEquals(0, database.getClanRepository().count());
    assertEquals(0, database.getClanMembershipRepository().count());
  }

}
