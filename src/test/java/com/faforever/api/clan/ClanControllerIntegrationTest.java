package com.faforever.api.clan;

import com.faforever.api.data.domain.Clan;
import com.faforever.api.data.domain.ClanMembership;
import com.faforever.api.data.domain.Player;
import com.faforever.api.error.ErrorCode;
import com.faforever.integration.TestDatabase;
import com.faforever.integration.factories.PlayerFactory;
import com.faforever.integration.factories.SessionFactory;
import com.faforever.integration.factories.SessionFactory.Session;
import com.faforever.integration.utils.MockMvcHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import javax.servlet.Filter;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@Import(TestDatabase.class)
@Transactional
public class ClanControllerIntegrationTest {
  private MockMvc mvc;
  private WebApplicationContext context;
  private Filter springSecurityFilterChain;

  private TestDatabase database;

  @Inject
  public void init(WebApplicationContext context,
                   Filter springSecurityFilterChain,
                   TestDatabase database) {
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

  @Test
  public void meDataWithoutClan() throws Exception {
    Session session = SessionFactory.createUserAndGetAccessToken(
      database, mvc);
    Player player = session.getPlayer();

    assertEquals(1, database.getPlayerRepository().count());
    MockMvcHelper.of(this.mvc).setSession(session).perform(get("/clans/me/"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.player.id", is(player.getId())))
      .andExpect(jsonPath("$.player.login", is(player.getLogin())))
      .andExpect(jsonPath("$.clan", is(nullValue())));
  }

  @Test
  public void meDataWithClan() throws Exception {
    Session session = SessionFactory.createUserAndGetAccessToken(
      database, mvc);
    Player player = session.getPlayer();

    Clan clan = new Clan().setLeader(player).setTag("123").setName("abcClanName");
    ClanMembership myMembership = new ClanMembership().setPlayer(player).setClan(clan);
    clan.setMemberships(Collections.singletonList(myMembership));
    database.getClanRepository().save(clan);

    assertEquals(1, database.getPlayerRepository().count());
    assertEquals(1, database.getClanRepository().count());
    assertEquals(1, database.getClanMembershipRepository().count());
    MockMvcHelper.of(this.mvc).setSession(session).perform(get("/clans/me/"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.player.id", is(player.getId())))
      .andExpect(jsonPath("$.player.login", is(player.getLogin())))
      .andExpect(jsonPath("$.clan.id", is(clan.getId())))
      .andExpect(jsonPath("$.clan.tag", is(clan.getTag())))
      .andExpect(jsonPath("$.clan.name", is(clan.getName())));
  }

  @Test
  public void createClan() throws Exception {
    Session session = SessionFactory.createUserAndGetAccessToken(
      database, mvc);
    String clanName = "My Cool ClanName";
    String tag = "123";
    String description = "spaces Must Be Encoded";

    assertEquals(1, database.getPlayerRepository().count());
    assertEquals(0, database.getClanRepository().count());
    assertEquals(0, database.getClanMembershipRepository().count());
    ResultActions action = MockMvcHelper.of(this.mvc).setSession(session).perform(
      post(String.format("/clans/create?tag=%s&name=%s&description=%s",
        tag, clanName, description)));

    int id = database.getClanRepository().findAll().get(0).getId();

    action.andExpect(status().isOk())
      .andExpect(jsonPath("$.id", is(id)))
      .andExpect(jsonPath("$.type", is("clan")));
    assertEquals(1, database.getPlayerRepository().count());
    assertEquals(1, database.getClanRepository().count());
    assertEquals(1, database.getClanMembershipRepository().count());
  }

  @Test
  public void createSecondClan() throws Exception {
    Session session = SessionFactory.createUserAndGetAccessToken(
      database, mvc);
    Player player = session.getPlayer();
    String clanName = "My Cool ClanName";
    String tag = "123";
    String description = "spaces Must Be Encoded";

    Clan clan = new Clan().setLeader(player).setTag("tag").setName("abcClan");
    ClanMembership membership = new ClanMembership().setPlayer(player).setClan(clan);
    clan.setMemberships(Collections.singletonList(membership));
    database.getClanRepository().save(clan);

    assertEquals(1, database.getPlayerRepository().count());
    assertEquals(1, database.getClanRepository().count());
    assertEquals(1, database.getClanMembershipRepository().count());
    ResultActions action = MockMvcHelper.of(this.mvc).setSession(session).perform(
      post(String.format("/clans/create?tag=%s&name=%s&description=%s",
        tag, clanName, description)));

    action.andExpect(status().isUnprocessableEntity())
      .andExpect(jsonPath("$.errors", hasSize(1)))
      .andExpect(jsonPath("$.errors[0].status", is(HttpStatus.UNPROCESSABLE_ENTITY.toString())))
      .andExpect(jsonPath("$.errors[0].title", is("You are already in a clan")))
      .andExpect(jsonPath("$.errors[0].detail", is("Clan creator is already member of a clan")))
      .andExpect(jsonPath("$.errors[0].code", is(ErrorCode.CLAN_CREATE_CREATOR_IS_IN_A_CLAN.codeAsString())));
    assertEquals(1, database.getPlayerRepository().count());
    assertEquals(1, database.getClanRepository().count());
    assertEquals(1, database.getClanMembershipRepository().count());
  }

  @Test
  public void createClanWithSameName() throws Exception {
    Player otherLeader = PlayerFactory.builder().database(database).build();
    Session session = SessionFactory.createUserAndGetAccessToken(
      database, mvc);
    String clanName = "My Cool ClanName";
    String tag = "123";
    String description = "spaces Must Be Encoded";

    Clan clan = new Clan().setLeader(otherLeader).setTag("123").setName(clanName);
    ClanMembership membership = new ClanMembership().setPlayer(otherLeader).setClan(clan);
    clan.setMemberships(Collections.singletonList(membership));
    database.getClanRepository().save(clan);

    assertEquals(2, database.getPlayerRepository().count());
    assertEquals(1, database.getClanRepository().count());
    assertEquals(1, database.getClanMembershipRepository().count());
    ResultActions action = MockMvcHelper.of(this.mvc).setSession(session).perform(
      post(String.format("/clans/create?tag=%s&name=%s&description=%s",
        tag, clanName, description)));

    action.andExpect(status().isUnprocessableEntity())
      .andExpect(jsonPath("$.errors", hasSize(1)))
      .andExpect(jsonPath("$.errors[0].status", is(HttpStatus.UNPROCESSABLE_ENTITY.toString())))
      .andExpect(jsonPath("$.errors[0].title", is("Clan Name already in use")))
      .andExpect(jsonPath("$.errors[0].detail", is("The clan name 'My Cool ClanName' is already in use. Please choose a different clan name.")))
      .andExpect(jsonPath("$.errors[0].code", is(ErrorCode.CLAN_NAME_EXISTS.codeAsString())))
      .andExpect(jsonPath("$.errors[0].meta.args[0]", is("My Cool ClanName")));
    assertEquals(2, database.getPlayerRepository().count());
    assertEquals(1, database.getClanRepository().count());
    assertEquals(1, database.getClanMembershipRepository().count());
  }

  @Test
  public void createClanWithSameTag() throws Exception {
    Player otherLeader = PlayerFactory.builder().database(database).build();
    Session session = SessionFactory.createUserAndGetAccessToken(
      database, mvc);
    String clanName = "My Cool ClanName";
    String tag = "123";
    String description = "spaces Must Be Encoded";

    Clan clan = new Clan().setLeader(otherLeader).setTag(tag).setName("abcClan");
    ClanMembership membership = new ClanMembership().setPlayer(otherLeader).setClan(clan);
    clan.setMemberships(Collections.singletonList(membership));
    database.getClanRepository().save(clan);

    assertEquals(2, database.getPlayerRepository().count());
    assertEquals(1, database.getClanRepository().count());
    assertEquals(1, database.getClanMembershipRepository().count());
    ResultActions action = MockMvcHelper.of(this.mvc).setSession(session).perform(
      post(String.format("/clans/create?tag=%s&name=%s&description=%s",
        tag, clanName, description)));

    action.andExpect(status().isUnprocessableEntity())
      .andExpect(jsonPath("$.errors", hasSize(1)))
      .andExpect(jsonPath("$.errors[0].status", is(HttpStatus.UNPROCESSABLE_ENTITY.toString())))
      .andExpect(jsonPath("$.errors[0].title", is("Clan Tag already in use")))
      .andExpect(jsonPath("$.errors[0].detail", is("The clan tag 'My Cool ClanName' is already in use. Please choose a different clan tag.")))
      .andExpect(jsonPath("$.errors[0].code", is(ErrorCode.CLAN_TAG_EXISTS.codeAsString())))
      .andExpect(jsonPath("$.errors[0].meta.args[0]", is("My Cool ClanName")));
    assertEquals(2, database.getPlayerRepository().count());
    assertEquals(1, database.getClanRepository().count());
    assertEquals(1, database.getClanMembershipRepository().count());
  }
}
