package com.faforever.api.data;

import com.faforever.api.ban.BanRepository;
import com.faforever.api.client.ClientType;
import com.faforever.api.client.OAuthClient;
import com.faforever.api.client.OAuthClientRepository;
import com.faforever.api.data.checks.permission.HasBanInfoCreate;
import com.faforever.api.data.checks.permission.HasBanInfoRead;
import com.faforever.api.data.domain.BanInfo;
import com.faforever.api.data.domain.BanType;
import com.faforever.api.data.domain.Permission;
import com.faforever.api.data.domain.Player;
import com.faforever.api.data.domain.Role;
import com.faforever.api.data.domain.User;
import com.faforever.api.permission.PermissionRepository;
import com.faforever.api.permission.PermissionService;
import com.faforever.api.permission.RolePermissionsAssingmentRepository;
import com.faforever.api.permission.RoleRepository;
import com.faforever.api.permission.RoleUserAssignmentRepository;
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

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JsonApiBanIntegrationTest {
  private static final String OAUTH_CLIENT_ID = "1234";
  private static final String OAUTH_SECRET = "secret";
  private MockMvc mvc;
  private WebApplicationContext context;
  private Filter springSecurityFilterChain;

  private BanRepository banRepository;
  private PermissionService permissionService;
  private UserRepository userRepository;

  private RoleUserAssignmentRepository roleUserAssignmentRepository;
  private PermissionRepository permissionRepository;
  private RolePermissionsAssingmentRepository rolePermissionsAssingmentRepository;
  private RoleRepository roleRepository;

  private PlayerRepository playerRepository;
  private OAuthClientRepository oAuthClientRepository;
  private ObjectMapper objectMapper;
  private ShaPasswordEncoder shaPasswordEncoder;
  private Player me;

  public JsonApiBanIntegrationTest() {
    objectMapper = new ObjectMapper();
    shaPasswordEncoder = new ShaPasswordEncoder(256);
  }

  @Inject
  public void init(WebApplicationContext context,
                   UserRepository userRepository,
                   PlayerRepository playerRepository,
                   OAuthClientRepository oAuthClientRepository,
                   Filter springSecurityFilterChain,
                   BanRepository banRepository,
                   PermissionService permissionService,
                   RoleUserAssignmentRepository roleUserAssignmentRepository,
                   PermissionRepository permissionRepository,
                   RolePermissionsAssingmentRepository rolePermissionsAssingmentRepository,
                   RoleRepository roleRepository) {
    this.context = context;
    this.userRepository = userRepository;
    this.playerRepository = playerRepository;
    this.oAuthClientRepository = oAuthClientRepository;
    this.springSecurityFilterChain = springSecurityFilterChain;
    this.banRepository = banRepository;
    this.permissionService = permissionService;
    this.roleUserAssignmentRepository = roleUserAssignmentRepository;
    this.roleRepository = roleRepository;
    this.permissionRepository = permissionRepository;
    this.rolePermissionsAssingmentRepository = rolePermissionsAssingmentRepository;
  }

  @Before
  public void setUp() {
    mvc = MockMvcBuilders
        .webAppContextSetup(context)
        .addFilter(springSecurityFilterChain)
        .build();
  }

  @After
  public void tearDown() {
    banRepository.deleteAll();
    roleUserAssignmentRepository.deleteAll();
    rolePermissionsAssingmentRepository.deleteAll();
    permissionRepository.deleteAll();
    roleRepository.deleteAll();
    userRepository.deleteAll();
    oAuthClientRepository.deleteAll();
    assertEquals(0, banRepository.count());
    assertEquals(0, roleUserAssignmentRepository.count());
    assertEquals(0, rolePermissionsAssingmentRepository.count());
    assertEquals(0, permissionRepository.count());
    assertEquals(0, roleRepository.count());
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

  @Test
  @SneakyThrows
  public void getBansWithoutPermission() {
    String accessToken = createUserAndGetAccessToken("Dragonfire", "foo");

    BanInfo ban = new BanInfo().setAuthor(me).setReason("I want to ban me").setPlayer(me).setType(BanType.GLOBAL);
    banRepository.save(ban);

    assertEquals(1, banRepository.count());
    ResultActions action = this.mvc.perform(get("/data/banInfo")
        .header("Authorization", accessToken));
    action
        .andExpect(content().string("{\"data\":[]}"))
        .andExpect(status().is(200));
  }

  @Test
  @SneakyThrows
  public void getBansWithPermission() {
    String accessToken = createUserAndGetAccessToken("Dragonfire", "foo");
    Permission permission = permissionService.createPermission(HasBanInfoRead.EXPRESSION);
    Role role = permissionService.createRole("TestRole", permission);
    permissionService.assignUserToRole(userRepository.findOneByLoginIgnoreCase(me.getLogin()), role);

    BanInfo ban = new BanInfo().setAuthor(me).setReason("I want to ban me").setPlayer(me).setType(BanType.GLOBAL);
    banRepository.save(ban);

    assertEquals(1, banRepository.count());
    ResultActions action = this.mvc.perform(get("/data/banInfo")
        .header("Authorization", accessToken));
    action
        .andExpect(content().string("{\"data\":[{\"type\":\"banInfo\",\"id\":\"1\",\"attributes\":{\"createTime\":null,\"expiresAt\":null,\"reason\":\"I want to ban me\",\"type\":\"GLOBAL\",\"updateTime\":null},\"relationships\":{\"author\":{\"data\":{\"type\":\"player\",\"id\":\"1\"}},\"banRevokeData\":{\"data\":null},\"player\":{\"data\":{\"type\":\"player\",\"id\":\"1\"}}}}]}"))
        .andExpect(status().is(200));
  }

  @Test
  @SneakyThrows
  public void createBanWithPermission() {
    String accessToken = createUserAndGetAccessToken("Dragonfire", "foo");
    Permission permission = permissionService.createPermission(HasBanInfoCreate.EXPRESSION);
    Role role = permissionService.createRole("TestRole", permission);
    permissionService.assignUserToRole(userRepository.findOneByLoginIgnoreCase(me.getLogin()), role);

    String reason = "I want to ban him";
    String content = String.format("{\"data\":[{\"type\":\"banInfo\",\"attributes\":{\"reason\":\"%s\"},\"relationships\":{\"author\":{\"data\":{\"type\":\"player\",\"id\":\"%s\"}},\"player\":{\"data\":{\"type\":\"player\",\"id\":\"%s\"}}}}]}",
        reason,
        me.getId(),
        me.getId());
    assertEquals(0, banRepository.count());
    ResultActions action = this.mvc.perform(post("/data/banInfo")
        .content(content)
        .header("Authorization", accessToken));
    action
        .andExpect(content().string("{\"data\":{\"type\":\"banInfo\",\"id\":\"1\"}}"))
        .andExpect(status().is(201));
    assertEquals(1, banRepository.count());
  }
}
