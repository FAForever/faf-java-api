package com.faforever.api.clan;

import com.faforever.api.client.OAuthClientRepository;
import com.faforever.api.data.domain.Clan;
import com.faforever.api.data.domain.ClanMembership;
import com.faforever.api.data.domain.Player;
import com.faforever.api.player.PlayerRepository;
import com.faforever.api.user.UserRepository;
import com.faforever.integration.factories.PlayerFactory;
import com.faforever.integration.factories.SessionFactory;
import com.faforever.integration.factories.SessionFactory.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import javax.servlet.Filter;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ClanControllerIntegrationTest {
  private MockMvc mvc;
  private WebApplicationContext context;
  private Filter springSecurityFilterChain;
  private ClanRepository clanRepository;
  private UserRepository userRepository;
  private ClanMembershipRepository clanMembershipRepository;
  private PlayerRepository playerRepository;
  private OAuthClientRepository oAuthClientRepository;

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
  }

  // Dragonfire: This duplicated code cannot be avoided, each test must cleanup all the used repositories
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


  @Test
  public void meDataWithoutClan() throws Exception {
    Session session = SessionFactory.createUserAndGetAccessToken(
        oAuthClientRepository, userRepository, playerRepository, mvc);

    String expected = String.format("{\"player\":{\"id\":%s,\"login\":\"%s\"},\"clan\":null}",
        session.getPlayer().getId(),
        session.getPlayer().getLogin());

    assertEquals(1, playerRepository.count());
    this.mvc.perform(get("/clans/me/")
        .header("Authorization", session.getToken()))
        .andExpect(content().string(expected))
        .andExpect(status().isOk());
  }

  @Test
  public void meDataWithClan() throws Exception {
    Session session = SessionFactory.createUserAndGetAccessToken(
        oAuthClientRepository, userRepository, playerRepository, mvc);

    Clan clan = new Clan().setLeader(session.getPlayer()).setTag("123").setName("abcClanName");
    ClanMembership myMembership = new ClanMembership().setPlayer(session.getPlayer()).setClan(clan);
    clan.setMemberships(Collections.singletonList(myMembership));
    clanRepository.save(clan);

    String expected = String.format("{\"player\":{\"id\":%s,\"login\":\"%s\"},\"clan\":{\"id\":%s,\"tag\":\"%s\",\"name\":\"%s\"}}",
        session.getPlayer().getId(),
        session.getPlayer().getLogin(),
        clan.getId(),
        clan.getTag(),
        clan.getName());

    assertEquals(1, playerRepository.count());
    assertEquals(1, clanRepository.count());
    assertEquals(1, clanMembershipRepository.count());
    this.mvc.perform(get("/clans/me/")
        .header("Authorization", session.getToken()))
        .andExpect(content().string(expected))
        .andExpect(status().isOk());
  }

  @Test
  public void createClan() throws Exception {
    Session session = SessionFactory.createUserAndGetAccessToken(
        oAuthClientRepository, userRepository, playerRepository, mvc);
    String clanName = "My Cool ClanName";
    String tag = "123";
    String description = "spaces Must Be Encoded";

    assertEquals(1, playerRepository.count());
    assertEquals(0, clanRepository.count());
    assertEquals(0, clanMembershipRepository.count());
    ResultActions action = this.mvc.perform(post(
        String.format("/clans/create?tag=%s&name=%s&description=%s",
            tag, clanName, description))
        .header("Authorization", session.getToken()));

    int id = clanRepository.findAll().get(0).getId();

    action.andExpect(content().string(String.format("{\"id\":%s,\"type\":\"clan\"}", id)))
        .andExpect(status().isOk());
    assertEquals(1, playerRepository.count());
    assertEquals(1, clanRepository.count());
    assertEquals(1, clanMembershipRepository.count());
  }

  @Test
  public void createSecondClan() throws Exception {
    Session session = SessionFactory.createUserAndGetAccessToken(
        oAuthClientRepository, userRepository, playerRepository, mvc);
    String clanName = "My Cool ClanName";
    String tag = "123";
    String description = "spaces Must Be Encoded";

    Clan clan = new Clan().setLeader(session.getPlayer()).setTag("tag").setName("abcClan");
    ClanMembership membership = new ClanMembership().setPlayer(session.getPlayer()).setClan(clan);
    clan.setMemberships(Collections.singletonList(membership));
    clanRepository.save(clan);

    assertEquals(1, playerRepository.count());
    assertEquals(1, clanRepository.count());
    assertEquals(1, clanMembershipRepository.count());
    ResultActions action = this.mvc.perform(post(
        String.format("/clans/create?tag=%s&name=%s&description=%s",
            tag, clanName, description))
        .header("Authorization", session.getToken()));

    action.andExpect(content().string("{\"errors\":[{\"title\":\"You are already in a clan\",\"detail\":\"Clan creator is already member of a clan\"}]}"))
        .andExpect(status().is(422));
    assertEquals(1, playerRepository.count());
    assertEquals(1, clanRepository.count());
    assertEquals(1, clanMembershipRepository.count());
  }

  @Test
  public void createClanWithSameName() throws Exception {
    Player otherLeader = PlayerFactory.createPlayer("Downloard", userRepository, playerRepository);
    Session session = SessionFactory.createUserAndGetAccessToken(
        oAuthClientRepository, userRepository, playerRepository, mvc);
    String clanName = "My Cool ClanName";
    String tag = "123";
    String description = "spaces Must Be Encoded";

    Clan clan = new Clan().setLeader(otherLeader).setTag("123").setName(clanName);
    ClanMembership membership = new ClanMembership().setPlayer(otherLeader).setClan(clan);
    clan.setMemberships(Collections.singletonList(membership));
    clanRepository.save(clan);

    assertEquals(2, playerRepository.count());
    assertEquals(1, clanRepository.count());
    assertEquals(1, clanMembershipRepository.count());
    ResultActions action = this.mvc.perform(post(
        String.format("/clans/create?tag=%s&name=%s&description=%s",
            tag, clanName, description))
        .header("Authorization", session.getToken()));

    action.andExpect(content().string("{\"errors\":[{\"title\":\"Clan Name already in use\",\"detail\":\"The clan name 'My Cool ClanName' is already in use. Please choose a different clan name.\"}]}"))
        .andExpect(status().is(422));
    assertEquals(2, playerRepository.count());
    assertEquals(1, clanRepository.count());
    assertEquals(1, clanMembershipRepository.count());
  }

  @Test
  public void createClanWithSameTag() throws Exception {
    Player otherLeader = PlayerFactory.createPlayer("Downloard", userRepository, playerRepository);
    Session session = SessionFactory.createUserAndGetAccessToken(
        oAuthClientRepository, userRepository, playerRepository, mvc);
    String clanName = "My Cool ClanName";
    String tag = "123";
    String description = "spaces Must Be Encoded";

    Clan clan = new Clan().setLeader(otherLeader).setTag(tag).setName("abcClan");
    ClanMembership membership = new ClanMembership().setPlayer(otherLeader).setClan(clan);
    clan.setMemberships(Collections.singletonList(membership));
    clanRepository.save(clan);

    assertEquals(2, playerRepository.count());
    assertEquals(1, clanRepository.count());
    assertEquals(1, clanMembershipRepository.count());
    ResultActions action = this.mvc.perform(post(
        String.format("/clans/create?tag=%s&name=%s&description=%s",
            tag, clanName, description))
        .header("Authorization", session.getToken()));

    action.andExpect(content().string("{\"errors\":[{\"title\":\"Clan Tag already in use\",\"detail\":\"The clan tag 'My Cool ClanName' is already in use. Please choose a different clan tag.\"}]}"))
        .andExpect(status().is(422));
    assertEquals(2, playerRepository.count());
    assertEquals(1, clanRepository.count());
    assertEquals(1, clanMembershipRepository.count());
  }
}
