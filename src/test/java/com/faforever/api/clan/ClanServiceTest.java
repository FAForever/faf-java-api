package com.faforever.api.clan;


import com.faforever.api.authentication.AuthenticationService;
import com.faforever.api.authentication.JwtService;
import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Clan;
import com.faforever.api.data.domain.ClanMembership;
import com.faforever.api.data.domain.Player;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.player.PlayerRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import static com.faforever.api.error.ApiExceptionWithCode.apiExceptionWithCode;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClanServiceTest {
  private ClanService instance;
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private ClanRepository clanRepository;
  @Mock
  private PlayerRepository playerRepository;
  @Mock
  private FafApiProperties fafApiProperties;
  @Mock
  private JwtService jwtService;
  @Mock
  private AuthenticationService authenticationService;
  @Mock
  private ClanMembershipRepository clanMembershipRepository;

  @Before
  public void setUp() throws Exception {
    instance = new ClanService(clanRepository, playerRepository, fafApiProperties, jwtService, authenticationService, clanMembershipRepository);
  }

  @Test
  public void createClanWhereLeaderIsAllreadyInAClan() {
    String clanName = "My cool Clan";
    String tag = "123";
    String description = "A cool clan for testing";
    Player creator = new Player();
    creator.setId(1);
    creator.setClanMemberships(new ArrayList<>());
    creator.getClanMemberships().add(new ClanMembership());
    try {
      instance.create(clanName, tag, description, creator);
      fail();
    } catch (ApiException e) {
      assertThat(e, apiExceptionWithCode(ErrorCode.CLAN_CREATE_CREATOR_IS_IN_A_CLAN));
    }
    verify(clanRepository, Mockito.never()).save(any(Clan.class));
  }

  @Test
  public void createClanSuccessful() {
    String clanName = "My cool Clan";
    String tag = "123";
    String description = "A cool clan for testing";
    Player creator = new Player();
    creator.setId(1);
    creator.setClanMemberships(new ArrayList<>());


    instance.create(clanName, tag, description, creator);
    ArgumentCaptor<Clan> clanCaptor = ArgumentCaptor.forClass(Clan.class);
    verify(clanRepository, Mockito.times(1)).save(clanCaptor.capture());
    assertEquals(clanName, clanCaptor.getValue().getName());
    assertEquals(tag, clanCaptor.getValue().getTag());
    assertEquals(description, clanCaptor.getValue().getDescription());
    assertEquals(creator, clanCaptor.getValue().getLeader());
    assertEquals(creator, clanCaptor.getValue().getFounder());
    assertEquals(1, clanCaptor.getValue().getMemberships().size());
    assertEquals(creator, clanCaptor.getValue().getMemberships().get(0).getPlayer());
  }

  @Test
  public void generatePlayerInvitationTokenWithInvalidClan() throws IOException {
    try {
      Player requester = new Player();
      requester.setId(1);

      instance.generatePlayerInvitationToken(requester, 45, 42);
      fail();
    } catch (ApiException e) {
      assertThat(e, apiExceptionWithCode(ErrorCode.CLAN_NOT_EXISTS));
    }
    verify(jwtService, Mockito.never()).sign(any());
  }

  @Test
  public void generatePlayerInvitationTokenFromNonLeader() throws IOException {
    try {
      Player requester = new Player();
      requester.setId(1);
      Player newMember = new Player();
      newMember.setId(2);
      Player leader = new Player();
      leader.setId(3);
      Clan clan = new Clan()
          .setId(1)
          .setTag("123")
          .setName("abc")
          .setLeader(leader);

      when(clanRepository.findOne(clan.getId())).thenReturn(clan);

      instance.generatePlayerInvitationToken(requester, newMember.getId(), clan.getId());
      fail();
    } catch (ApiException e) {
      assertThat(e, apiExceptionWithCode(ErrorCode.CLAN_NOT_LEADER));
    }
    verify(jwtService, Mockito.never()).sign(any());
  }

  @Test
  public void generatePlayerInvitationTokenInvalidPlayer() throws IOException {
    try {
      Player requester = new Player();
      requester.setId(1);
      Clan clan = new Clan()
          .setId(1)
          .setTag("123")
          .setName("abc")
          .setLeader(requester);

      when(clanRepository.findOne(clan.getId())).thenReturn(clan);

      instance.generatePlayerInvitationToken(requester, 42, clan.getId());
      fail();
    } catch (ApiException e) {
      assertThat(e, apiExceptionWithCode(ErrorCode.CLAN_GENERATE_LINK_PLAYER_NOT_FOUND));
    }
    verify(jwtService, Mockito.never()).sign(any());
  }

  @Test
  public void generatePlayerInvitationToken() throws IOException {
    Player requester = new Player();
    requester.setId(1);
    Player newMember = new Player();
    newMember.setId(2);
    Clan clan = new Clan()
        .setId(1)
        .setTag("123")
        .setName("abc");
    clan.setLeader(requester);
    FafApiProperties props = new FafApiProperties();

    when(clanRepository.findOne(clan.getId())).thenReturn(clan);
    when(playerRepository.findOne(newMember.getId())).thenReturn(newMember);
    when(fafApiProperties.getClan()).thenReturn(props.getClan());

    instance.generatePlayerInvitationToken(requester, newMember.getId(), clan.getId());
    ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
    verify(jwtService, Mockito.times(1)).sign(captor.capture());
    assertThat("expire",
        Long.parseLong(captor.getValue().get("expire").toString()),
        greaterThan(System.currentTimeMillis()));
    assertEquals(newMember.getId(), captor.getValue().get("newMemberId"));
    assertEquals(clan.getId(), ((Map) captor.getValue().get("clan")).get("id"));
    assertEquals(clan.getTag(), ((Map) captor.getValue().get("clan")).get("tag"));
    assertEquals(clan.getName(), ((Map) captor.getValue().get("clan")).get("name"));
  }
}
