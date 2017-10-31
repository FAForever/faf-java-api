package com.faforever.api.data;

import com.faforever.api.data.domain.Clan;
import com.faforever.api.data.domain.ClanMembership;
import com.faforever.api.data.domain.Player;
import com.faforever.integration.TestDatabase;
import com.faforever.integration.factories.PlayerFactory;
import com.faforever.integration.factories.SessionFactory;
import com.faforever.integration.factories.SessionFactory.Session;
import com.faforever.integration.utils.MockMvcHelper;
import lombok.SneakyThrows;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(TestDatabase.class)
public class JsonApiClanIntegrationTest {

  private MockMvc mvc;
  private WebApplicationContext context;
  private Filter springSecurityFilterChain;

  private TestDatabase database;

  private final ObjectMapper objectMapper;

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
    // TODO: This is needed, because Elide has some problems with @Transactional annotation #71
    database.tearDown();
  }


  @SneakyThrows
  private String generateTransferLeadershipContent(int clanId, int newLeaderId) {
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

    Player bob = PlayerFactory.builder().login("Bob").database(database).build();
    Clan clan = new Clan().setLeader(player).setTag("123").setName("abcClanName");
    ClanMembership myMembership = new ClanMembership().setPlayer(player).setClan(clan);
    ClanMembership bobsMembership = new ClanMembership().setPlayer(bob).setClan(clan);
    clan.setMemberships(Arrays.asList(myMembership, bobsMembership));
    database.getClanRepository().save(clan);

    String dataString = generateTransferLeadershipContent(clan.getId(), bob.getId());

    clan = database.getClanRepository().findOne(clan.getId());
    assertEquals(player.getId(), clan.getLeader().getId());

    MockMvcHelper.of(this.mvc).setSession(session).perform(
      patch("/data/clan/" + clan.getId()).content(dataString))
      .andExpect(content().string(""))
      .andExpect(status().isNoContent());

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

    ResultActions action = MockMvcHelper.of(this.mvc).setSession(session).perform(
      patch("/data/clan/" + clan.getId())
        .content(dataString));
    action.andExpect(content().string(""))
      .andExpect(status().isNoContent());

    clan = database.getClanRepository().findOne(clan.getId());
    assertEquals(player.getId(), clan.getLeader().getId());
  }

  @Test
  public void transferLeadershipToNonClanMember() throws Exception {
    Session session = SessionFactory.createUserAndGetAccessToken("Leader", "foo", database, mvc);
    Player player = session.getPlayer();

    Player bob = PlayerFactory.builder().login("Bob").database(database).build();

    Clan clan = new Clan().setLeader(player).setTag("123").setName("abcClanName");
    ClanMembership myMembership = new ClanMembership().setPlayer(player).setClan(clan);
    clan.setMemberships(Collections.singletonList(myMembership));
    database.getClanRepository().save(clan);

    String dataString = generateTransferLeadershipContent(clan.getId(), bob.getId());

    clan = database.getClanRepository().findOne(clan.getId());
    assertEquals(player.getId(), clan.getLeader().getId());

    ResultActions action = MockMvcHelper.of(this.mvc).setSession(session).perform(
      patch("/data/clan/" + clan.getId()).content(dataString));
    action.andExpect(status().isUnprocessableEntity())
      .andExpect(jsonPath("$.errors", hasSize(1)))
      .andExpect(jsonPath("$.errors[0].title", is("Validation failed")));

    clan = database.getClanRepository().findOne(clan.getId());
    assertEquals(player.getId(), clan.getLeader().getId());
  }

  @Test
  public void transferLeadershipAsNonLeader() throws Exception {
    Session session = SessionFactory.createUserAndGetAccessToken("Leader", "foo", database, mvc);
    Player player = session.getPlayer();

    Player bob = PlayerFactory.builder().login("Bob").database(database).build();
    Player charlie = PlayerFactory.builder().login("Charlie").database(database).build();

    Clan clan = new Clan().setLeader(bob).setTag("123").setName("abcClanName");
    ClanMembership myMembership = new ClanMembership().setPlayer(player).setClan(clan);
    ClanMembership bobsMembership = new ClanMembership().setPlayer(bob).setClan(clan);
    ClanMembership charlieMembership = new ClanMembership().setPlayer(charlie).setClan(clan);
    clan.setMemberships(Arrays.asList(myMembership, bobsMembership, charlieMembership));
    database.getClanRepository().save(clan);

    String dataString = generateTransferLeadershipContent(clan.getId(), charlie.getId());

    clan = database.getClanRepository().findOne(clan.getId());
    assertEquals(bob.getId(), clan.getLeader().getId());

    ResultActions action = MockMvcHelper.of(this.mvc).setSession(session).perform(
      patch("/data/clan/" + clan.getId())
        .content(dataString));
    action.andExpect(status().isForbidden())
      .andExpect(jsonPath("$.errors[0]", is("ForbiddenAccessException")));

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

    ResultActions action = MockMvcHelper.of(this.mvc).setSession(session).perform(
      delete("/data/clan/" + clan.getId()));
    action.andExpect(content().string(""))
      .andExpect(status().isNoContent());

    assertEquals(0, database.getClanRepository().count());
    assertEquals(0, database.getClanMembershipRepository().count());
  }

}
