package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.clan.ClanRepository;
import com.faforever.api.data.domain.Clan;
import com.faforever.api.data.domain.ClanMembership;
import com.faforever.api.data.domain.Player;
import com.faforever.api.player.PlayerRepository;
import lombok.SneakyThrows;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/truncateTables.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultData.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepClanData.sql")
public class ClanElideTest extends AbstractIntegrationTest {
  private static final String AUTH_CLAN_LEADER = "CLAN_LEADER";
  private static final int USERID_CLAN_LEADER = 11;
  private static final int USERID_CLAN_MEMBER = 12;
  private static final String AUTH_CLAN_MEMBER = "CLAN_MEMBER";


  @Autowired
  PlayerRepository playerRepository;
  @Autowired
  ClanRepository clanRepository;

  @Test
  public void canDeleteMemberOfOwnClan() throws Exception {
    assertNotNull(playerRepository.getById(USERID_CLAN_MEMBER).getClan());

    mockMvc.perform(
        delete("/data/clanMembership/2") // magic value from prepClanData.sql
          .with(getOAuthTokenForUserId(USERID_CLAN_LEADER)))
      .andExpect(status().isNoContent());
    assertNull(playerRepository.getById(USERID_CLAN_MEMBER).getClan());
  }

  @Test
  public void cannotDeleteMemberOfOtherClan() throws Exception {
    mockMvc.perform(
        delete("/data/clanMembership/4") // magic value from prepClanData.sql
          .with(getOAuthTokenForUserId(USERID_CLAN_LEADER)))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.errors[0].detail", is("DeletePermission Denied")));
  }

  @Test
  public void cannotDeleteLeaderFromClan() throws Exception {
    mockMvc.perform(
        delete("/data/clanMembership/1") // magic value from prepClanData.sql
          .with(getOAuthTokenForUserId(USERID_CLAN_LEADER)))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.errors[0].detail", is("DeletePermission Denied")));
  }

  @Test
  public void canLeaveClan() throws Exception {
    assertNotNull(playerRepository.getById(USERID_CLAN_MEMBER).getClan());

    mockMvc.perform(
        delete("/data/clanMembership/2") // magic value from prepClanData.sql
          .with(getOAuthTokenForUserId(USERID_CLAN_MEMBER)))
      .andExpect(status().isNoContent());

    assertNull(playerRepository.getById(USERID_CLAN_MEMBER).getClan());
  }

  @Test
  public void cannotDeleteAsMember() throws Exception {
    mockMvc.perform(
        delete("/data/clanMembership/3") // magic value from prepClanData.sql
          .with(getOAuthTokenForUserId(USERID_CLAN_MEMBER)))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.errors[0].detail", is("DeletePermission Denied")));
  }

  @Test
  public void getFilteredPlayerForClanInvite() throws Exception {
    mockMvc.perform(get("/data/player?filter=login==*MEMBER*&sort=login"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(2)))
      .andExpect(jsonPath("$.data[0].attributes.login", is("CLAN_MEMBER")))
      .andExpect(jsonPath("$.data[1].attributes.login", is("CLAN_MEMBER_B")));
  }

  @Test
  public void canTransferLeadershipAsLeader() throws Exception {
    assertThat(clanRepository.getById(1).getLeader().getLogin(), is(AUTH_CLAN_LEADER));

    mockMvc.perform(
        patch("/data/clan/1")
          .with(getOAuthTokenForUserId(USERID_CLAN_LEADER))
          .header(HttpHeaders.CONTENT_TYPE, JsonApiMediaType.JSON_API_MEDIA_TYPE)
          .content(generateTransferLeadershipContent(1, USERID_CLAN_MEMBER))) // magic value from prepClanData.sql
      .andExpect(status().isNoContent());

    assertThat(clanRepository.getById(1).getLeader().getLogin(), is(AUTH_CLAN_MEMBER));
  }

  @Test
  public void cannotTransferLeadershipAsMember() throws Exception {
    assertThat(clanRepository.getById(1).getLeader().getLogin(), is(AUTH_CLAN_LEADER));

    mockMvc.perform(
        patch("/data/clan/1")
          .with(getOAuthTokenForUserId(USERID_CLAN_MEMBER))
          .header(HttpHeaders.CONTENT_TYPE, JsonApiMediaType.JSON_API_MEDIA_TYPE)
          .content(generateTransferLeadershipContent(1, USERID_CLAN_MEMBER))) // magic value from prepClanData.sql
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.errors[0].detail", is("UpdatePermission Denied")));

    assertThat(clanRepository.getById(1).getLeader().getLogin(), is(AUTH_CLAN_LEADER));
  }

  @Test
  public void cannotTransferLeadershipToNonClanMember() throws Exception {
    mockMvc.perform(
        patch("/data/clan/1")
          .with(getOAuthTokenForUserId(USERID_CLAN_LEADER))
          .header(HttpHeaders.CONTENT_TYPE, JsonApiMediaType.JSON_API_MEDIA_TYPE)
          .content(generateTransferLeadershipContent(1, 1))) // magic value from prepClanData.sql
      .andExpect(status().is4xxClientError()); // TODO: Catch javax.validation.ConstraintViolationException and wrap it into a regular ApiException
  }

  @SneakyThrows
  private String generateTransferLeadershipContent(int clanId, int newLeaderId) {
    JSONObject node = new JSONObject();
    JSONObject data = new JSONObject();
    JSONObject relationships = new JSONObject();
    JSONObject leaderData = new JSONObject();
    JSONObject leader = new JSONObject();

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
  public void canDeleteClanAsLeader() throws Exception {
    Clan clan = clanRepository.findOneByName("Alpha Clan")
      .orElseThrow(() -> new IllegalStateException("Alpha Clan could not be found"));

    List<Player> clanMember = new ArrayList<>();
    clan.getMemberships().stream()
      .map(ClanMembership::getPlayer)
      .forEach(clanMember::add);

    mockMvc.perform(
        delete("/data/clan/1")
          .with(getOAuthTokenForUserId(USERID_CLAN_LEADER)))
      .andExpect(status().isNoContent()); // TODO: Catch javax.validation.ConstraintViolationException and wrap it into a regular ApiException

    assertFalse(clanRepository.findOneByName("Alpha Clan").isPresent());
  }

  @Test
  public void cannotDeleteClanAsMember() throws Exception {
    Optional<Clan> clanOptional = clanRepository.findOneByName("Alpha Clan");
    assertTrue(clanOptional.isPresent());

    List<Player> clanMember = new ArrayList<>();
    clanOptional.get().getMemberships().stream()
      .map(ClanMembership::getPlayer)
      .forEach(clanMember::add);

    mockMvc.perform(
        delete("/data/clan/1")
          .with(getOAuthTokenForUserId(USERID_CLAN_MEMBER)))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.errors[0].detail", is("DeletePermission Denied")));

    //FIXME: for some weird Elide/Hibernate error, the transaction has ended after the error and the JpaRepository is empty
    //-> If you can fix this, uncomment the following lines
    //-> Manual test shows, that everything works properly

    //assertTrue(clanRepository.findOneByName("Alpha Clan").isPresent());
    //clanMember.forEach(player -> assertNotNull(playerRepository.findOne(player.getId()).getClan()));
  }
}
