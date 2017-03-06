package com.faforever.api.clan;

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
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ClanControllerTest {
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

  public ClanControllerTest() {
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
    assertEquals(0, clanMembershipRepository.count());
    assertEquals(0, clanRepository.count());
    assertEquals(0, userRepository.count());
    assertEquals(0, oAuthClientRepository.count());
  }

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

  private Player createPlayer(String login) throws Exception {
    User user = (User) new User()
        .setPassword("foo")
        .setLogin(login)
        .setEMail(login + "@faforever.com");
    userRepository.save(user);
    return playerRepository.findOne(user.getId());
  }


  @Test
  public void meDataWithoutClan() throws Exception {
    String accessToken = createUserAndGetAccessToken("Dragonfire", "foo");
    String expected = String.format("{\"player\":{\"id\":%s,\"login\":\"%s\"},\"clan\":null}",
        me.getId(),
        me.getLogin());

    assertEquals(1, playerRepository.count());
    this.mvc.perform(get("/clans/me/")
        .header("Authorization", accessToken))
        .andExpect(content().string(expected))
        .andExpect(status().isOk());
  }

  @Test
  public void meDataWithClan() throws Exception {
    String accessToken = createUserAndGetAccessToken("Dragonfire", "foo");

    Clan clan = new Clan().setLeader(me).setTag("123").setName("abcClanName");
    ClanMembership myMembership = new ClanMembership().setPlayer(me).setClan(clan);
    clan.setMemberships(Collections.singletonList(myMembership));
    clanRepository.save(clan);

    String expected = String.format("{\"player\":{\"id\":%s,\"login\":\"%s\"},\"clan\":{\"id\":%s,\"name\":\"%s\",\"tag\":\"%s\"}}",
        me.getId(),
        me.getLogin(),
        clan.getId(),
        clan.getName(),
        clan.getTag());

    assertEquals(1, playerRepository.count());
    assertEquals(1, clanRepository.count());
    assertEquals(1, clanMembershipRepository.count());
    this.mvc.perform(get("/clans/me/")
        .header("Authorization", accessToken))
        .andExpect(content().string(expected))
        .andExpect(status().isOk());
  }

  @Test
  public void createClan() throws Exception {
    String accessToken = createUserAndGetAccessToken("Dragonfire", "foo");
    String clanName = "My Cool ClanName";
    String tag = "123";
    String description = "spaces Must Be Encoded";

    assertEquals(1, playerRepository.count());
    assertEquals(0, clanRepository.count());
    assertEquals(0, clanMembershipRepository.count());
    ResultActions action = this.mvc.perform(post(
        String.format("/clans/create?tag=%s&name=%s&description=%s",
            tag, clanName, description))
        .header("Authorization", accessToken));

    int id = clanRepository.findAll().get(0).getId();

    action.andExpect(content().string(String.format("{\"id\":%s,\"type\":\"clan\"}", id)))
        .andExpect(status().isOk());
    assertEquals(1, playerRepository.count());
    assertEquals(1, clanRepository.count());
    assertEquals(1, clanMembershipRepository.count());
  }

  @Test
  public void createSecondClan() throws Exception {
    String accessToken = createUserAndGetAccessToken("Dragonfire", "foo");
    String clanName = "My Cool ClanName";
    String tag = "123";
    String description = "spaces Must Be Encoded";

    Clan clan = new Clan().setLeader(me).setTag("tag").setName("abcClan");
    ClanMembership membership = new ClanMembership().setPlayer(me).setClan(clan);
    clan.setMemberships(Collections.singletonList(membership));
    clanRepository.save(clan);

    assertEquals(1, playerRepository.count());
    assertEquals(1, clanRepository.count());
    assertEquals(1, clanMembershipRepository.count());
    ResultActions action = this.mvc.perform(post(
        String.format("/clans/create?tag=%s&name=%s&description=%s",
            tag, clanName, description))
        .header("Authorization", accessToken));

    action.andExpect(content().string("{\"errors\":[{\"title\":\"You are already in a clan\",\"detail\":\"Clan creator is already member of a clan\"}]}"))
        .andExpect(status().is(422));
    assertEquals(1, playerRepository.count());
    assertEquals(1, clanRepository.count());
    assertEquals(1, clanMembershipRepository.count());
  }

  @Test
  public void createClanWithSameName() throws Exception {
    Player otherLeader = createPlayer("Downloard");
    String accessToken = createUserAndGetAccessToken("Dragonfire", "foo");
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
        .header("Authorization", accessToken));

    action.andExpect(content().string("{\"errors\":[{\"title\":\"Clan Name allready in use\",\"detail\":\"The clan name 'My Cool ClanName' is allready in use. Please choose a different clan name.\"}]}"))
        .andExpect(status().is(422));
    assertEquals(2, playerRepository.count());
    assertEquals(1, clanRepository.count());
    assertEquals(1, clanMembershipRepository.count());
  }

  @Test
  public void createClanWithSameTag() throws Exception {
    Player otherLeader = createPlayer("Downloard");
    String accessToken = createUserAndGetAccessToken("Dragonfire", "foo");
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
        .header("Authorization", accessToken));

    action.andExpect(content().string("{\"errors\":[{\"title\":\"Clan Tag allready in use\",\"detail\":\"The clan tag 'My Cool ClanName' is allready in use. Please choose a different clan tag.\"}]}"))
        .andExpect(status().is(422));
    assertEquals(2, playerRepository.count());
    assertEquals(1, clanRepository.count());
    assertEquals(1, clanMembershipRepository.count());
  }
}
