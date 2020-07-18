package com.faforever.api.clan;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.data.domain.Clan;
import com.faforever.api.data.domain.Player;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.player.PlayerRepository;
import com.faforever.api.security.FafUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.MultiValueMap;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/truncateTables.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultData.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepClanData.sql")
public class ClanControllerTest extends AbstractIntegrationTest {
  public static final String AUTH_CLAN_MEMBER = "CLAN_MEMBER";
  public static final String NEW_CLAN_NAME = "New Clan Name";
  public static final String NEW_CLAN_TAG = "new";
  public static final String NEW_CLAN_DESCRIPTION = "spaces Must Be Encoded";
  public static final String EXISTING_CLAN = "Alpha Clan";

  @Autowired
  PlayerRepository playerRepository;
  @Autowired
  ClanRepository clanRepository;
  @Autowired
  ClanMembershipRepository clanMembershipRepository;

  Player getPlayer() {
    FafUserDetails authentication = (FafUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return playerRepository.getOne(authentication.getId());
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void meDataWithoutClan() throws Exception {
    Player player = getPlayer();

    mockMvc.perform(get("/clans/me/"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.player.id", is(player.getId())))
      .andExpect(jsonPath("$.player.login", is(player.getLogin())))
      .andExpect(jsonPath("$.clan", is(nullValue())));
  }

  @Test
  @WithUserDetails(AUTH_CLAN_MEMBER)
  public void meDataWithClan() throws Exception {
    Player player = getPlayer();
    Clan clan = clanRepository.getOne(1);

    mockMvc.perform(
      get("/clans/me/"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.player.id", is(player.getId())))
      .andExpect(jsonPath("$.player.login", is(player.getLogin())))
      .andExpect(jsonPath("$.clan.id", is(clan.getId())))
      .andExpect(jsonPath("$.clan.tag", is(clan.getTag())))
      .andExpect(jsonPath("$.clan.name", is(clan.getName())));
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void createClanWithSuccess() throws Exception {
    Player player = getPlayer();

    assertNull(player.getClan());
    assertFalse(clanRepository.findOneByName(NEW_CLAN_NAME).isPresent());

    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("name", NEW_CLAN_NAME);
    params.add("tag", NEW_CLAN_TAG);
    params.add("description", NEW_CLAN_DESCRIPTION);

    ResultActions action = mockMvc.perform(
      post("/clans/create")
        .params(params));

    Clan clan = clanRepository.findOneByName(NEW_CLAN_NAME)
      .orElseThrow(() -> new IllegalStateException("Clan not found - but should be created"));

    action.andExpect(status().isOk())
      .andExpect(jsonPath("$.id", is(clan.getId())))
      .andExpect(jsonPath("$.type", is("clan")));
  }

  @Test
  public void createClanWithoutAuth() throws Exception {
    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("name", NEW_CLAN_NAME);
    params.add("tag", NEW_CLAN_TAG);
    params.add("description", NEW_CLAN_DESCRIPTION);

    mockMvc.perform(
      post("/clans/create")
        .params(params))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void createClanWithExistingName() throws Exception {
    Player player = getPlayer();

    assertNull(player.getClan());
    assertTrue(clanRepository.findOneByName(EXISTING_CLAN).isPresent());

    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("name", EXISTING_CLAN);
    params.add("tag", NEW_CLAN_TAG);
    params.add("description", NEW_CLAN_DESCRIPTION);

    MvcResult result = mockMvc.perform(
      post("/clans/create")
        .params(params))
      .andExpect(status().isUnprocessableEntity())
      .andReturn();

    assertApiError(result, ErrorCode.CLAN_NAME_EXISTS);
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void createClanWithExistingTag() throws Exception {
    Player player = getPlayer();

    assertNull(player.getClan());
    assertFalse(clanRepository.findOneByName(NEW_CLAN_NAME).isPresent());

    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("name", NEW_CLAN_NAME);
    params.add("tag", "123");
    params.add("description", NEW_CLAN_DESCRIPTION);

    MvcResult result = mockMvc.perform(
      post("/clans/create")
        .params(params))
      .andExpect(status().isUnprocessableEntity())
      .andReturn();

    assertApiError(result, ErrorCode.CLAN_TAG_EXISTS);
  }

  @Test
  @WithUserDetails(AUTH_CLAN_MEMBER)
  public void createSecondClan() throws Exception {
    Player player = getPlayer();

    assertNotNull(player.getClan());
    assertFalse(clanRepository.findOneByName(NEW_CLAN_NAME).isPresent());

    MultiValueMap<String, String> params = new HttpHeaders();
    params.add("name", NEW_CLAN_NAME);
    params.add("tag", NEW_CLAN_TAG);
    params.add("description", NEW_CLAN_DESCRIPTION);

    MvcResult result = mockMvc.perform(
      post("/clans/create")
        .params(params))
      .andExpect(status().isUnprocessableEntity())
      .andReturn();

    assertApiError(result, ErrorCode.CLAN_CREATE_FOUNDER_IS_IN_A_CLAN);
  }
}
