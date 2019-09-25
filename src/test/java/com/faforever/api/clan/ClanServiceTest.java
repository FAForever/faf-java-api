package com.faforever.api.clan;


import com.faforever.api.clan.result.InvitationResult;
import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Clan;
import com.faforever.api.data.domain.ClanMembership;
import com.faforever.api.data.domain.Player;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.error.ProgrammingError;
import com.faforever.api.player.PlayerRepository;
import com.faforever.api.player.PlayerService;
import com.faforever.api.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.jwt.Jwt;

import java.io.IOException;
import java.util.Optional;

import static com.faforever.api.error.ApiExceptionMatcher.hasErrorCode;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClanServiceTest {
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
  private PlayerService playerService;
  @Mock
  private ClanMembershipRepository clanMembershipRepository;
  @Spy
  private ObjectMapper objectMapper = new ObjectMapper();
  @InjectMocks
  private ClanService instance;

  @Test
  public void createClanWhereLeaderIsAlreadyInAClan() {
    String clanName = "My cool Clan";
    String tag = "123";
    String description = "A cool clan for testing";
    Player creator = new Player();
    creator.setId(1);
    creator.setClanMembership(new ClanMembership());
    try {
      instance.create(clanName, tag, description, creator);
      fail();
    } catch (ApiException e) {
      assertThat(e, hasErrorCode(ErrorCode.CLAN_CREATE_CREATOR_IS_IN_A_CLAN));
    }
    verify(clanRepository, Mockito.never()).save(any(Clan.class));
  }

  @Test
  public void createClanWithSameName() {
    String clanName = "My cool Clan";
    String tag = "123";
    String description = "A cool clan for testing";

    Player creator = new Player();
    creator.setId(1);

    when(clanRepository.findOneByName(clanName)).thenReturn(Optional.of(new Clan()));
    try {
      instance.create(clanName, tag, description, creator);
      fail();
    } catch (ApiException e) {
      assertThat(e, hasErrorCode(ErrorCode.CLAN_NAME_EXISTS));
    }

    ArgumentCaptor<Clan> clanCaptor = ArgumentCaptor.forClass(Clan.class);
    verify(clanRepository, Mockito.times(0)).save(clanCaptor.capture());
  }

  @Test
  public void createClanWithSameTag() {
    String clanName = "My cool Clan";
    String tag = "123";
    String description = "A cool clan for testing";

    Player creator = new Player();
    creator.setId(1);

    when(clanRepository.findOneByName(clanName)).thenReturn(Optional.empty());
    when(clanRepository.findOneByTag(tag)).thenReturn(Optional.of(new Clan()));

    try {
      instance.create(clanName, tag, description, creator);
      fail();
    } catch (ApiException e) {
      assertThat(e, hasErrorCode(ErrorCode.CLAN_TAG_EXISTS));
    }

    ArgumentCaptor<Clan> clanCaptor = ArgumentCaptor.forClass(Clan.class);
    verify(clanRepository, Mockito.times(0)).save(clanCaptor.capture());
  }

  @Test
  public void createClanSuccessful() {
    String clanName = "My cool Clan";
    String tag = "123";
    String description = "A cool clan for testing";

    Player creator = new Player();
    creator.setId(1);

    when(clanRepository.findOneByName(clanName)).thenReturn(Optional.empty());
    when(clanRepository.findOneByTag(tag)).thenReturn(Optional.empty());


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
    Player requester = new Player();
    requester.setId(1);

    try {
      instance.generatePlayerInvitationToken(requester, 45, 42);
      fail();
    } catch (ApiException e) {
      assertThat(e, hasErrorCode(ErrorCode.CLAN_NOT_EXISTS));
    }
    verify(jwtService, Mockito.never()).sign(any());
  }

  @Test
  public void generatePlayerInvitationTokenFromNonLeader() throws IOException {
    Player requester = new Player();
    requester.setId(1);

    Player newMember = new Player();
    newMember.setId(2);

    Player leader = new Player();
    leader.setId(3);

    Clan clan = ClanFactory.builder().leader(leader).build();

    when(clanRepository.findById(clan.getId())).thenReturn(Optional.of(clan));

    try {
      instance.generatePlayerInvitationToken(requester, newMember.getId(), clan.getId());
      fail();
    } catch (ApiException e) {
      assertThat(e, hasErrorCode(ErrorCode.CLAN_NOT_LEADER));
    }
    verify(jwtService, Mockito.never()).sign(any());
  }

  @Test
  public void generatePlayerInvitationTokenInvalidPlayer() throws IOException {
    Player requester = new Player();
    requester.setId(1);

    Clan clan = ClanFactory.builder().leader(requester).build();

    when(clanRepository.findById(clan.getId())).thenReturn(Optional.of(clan));

    try {
      instance.generatePlayerInvitationToken(requester, 42, clan.getId());
      fail();
    } catch (ApiException e) {
      assertThat(e, hasErrorCode(ErrorCode.CLAN_GENERATE_LINK_PLAYER_NOT_FOUND));
    }
    verify(jwtService, Mockito.never()).sign(any());
  }

  @Test
  public void generatePlayerInvitationToken() throws IOException {
    Player requester = new Player();
    requester.setId(1);

    Player newMember = new Player();
    newMember.setId(2);

    Clan clan = ClanFactory.builder().leader(requester).build();

    FafApiProperties props = new FafApiProperties();

    when(clanRepository.findById(clan.getId())).thenReturn(Optional.of(clan));
    when(playerRepository.findById(newMember.getId())).thenReturn(Optional.of(newMember));
    when(fafApiProperties.getClan()).thenReturn(props.getClan());

    instance.generatePlayerInvitationToken(requester, newMember.getId(), clan.getId());
    ArgumentCaptor<InvitationResult> captor = ArgumentCaptor.forClass(InvitationResult.class);
    verify(jwtService, Mockito.times(1)).sign(captor.capture());
    assertThat("expire",
        captor.getValue().getExpire(),
        greaterThan(System.currentTimeMillis()));
    assertEquals(newMember.getId(), captor.getValue().getNewMember().getId());
    assertEquals(newMember.getLogin(), captor.getValue().getNewMember().getLogin());
    assertEquals(clan.getId(), captor.getValue().getClan().getId());
    assertEquals(clan.getTag(), captor.getValue().getClan().getTag());
    assertEquals(clan.getName(), captor.getValue().getClan().getName());
  }

  @Test
  public void acceptPlayerInvitationTokenExpire() throws IOException {
    String stringToken = "1234";
    long expire = System.currentTimeMillis();
    Jwt jwtToken = Mockito.mock(Jwt.class);

    when(jwtToken.getClaims()).thenReturn(
        String.format("{\"expire\":%s}", expire));
    when(jwtService.decodeAndVerify(any())).thenReturn(jwtToken);

    try {
      instance.acceptPlayerInvitationToken(stringToken, null);
      fail();
    } catch (ApiException e) {
      assertThat(e, hasErrorCode(ErrorCode.CLAN_ACCEPT_TOKEN_EXPIRE));
    }
    verify(clanMembershipRepository, Mockito.never()).save(any(ClanMembership.class));
  }

  @Test
  public void acceptPlayerInvitationTokenInvalidClan() throws IOException {
    String stringToken = "1234";

    long expire = System.currentTimeMillis() + 1000 * 3;
    Jwt jwtToken = Mockito.mock(Jwt.class);

    when(jwtToken.getClaims()).thenReturn(
        String.format("{\"expire\":%s,\"clan\":{\"id\":42}}", expire));
    when(jwtService.decodeAndVerify(any())).thenReturn(jwtToken);

    try {
      instance.acceptPlayerInvitationToken(stringToken, null);
      fail();
    } catch (ApiException e) {
      assertThat(e, hasErrorCode(ErrorCode.CLAN_NOT_EXISTS));
    }
    verify(clanMembershipRepository, Mockito.never()).save(any(ClanMembership.class));
  }


  @Test
  public void acceptPlayerInvitationTokenInvalidPlayer() throws IOException {
    String stringToken = "1234";
    Clan clan = ClanFactory.builder().build();

    long expire = System.currentTimeMillis() + 1000 * 3;
    Jwt jwtToken = Mockito.mock(Jwt.class);

    when(jwtToken.getClaims()).thenReturn(
        String.format("{\"expire\":%s,\"newMember\":{\"id\":2},\"clan\":{\"id\":%s}}",
            expire, clan.getId()));
    when(jwtService.decodeAndVerify(any())).thenReturn(jwtToken);
    when(clanRepository.findById(clan.getId())).thenReturn(Optional.of(clan));

    try {
      instance.acceptPlayerInvitationToken(stringToken, null);
      fail();
    } catch (ProgrammingError e) {
      assertEquals("ClanMember does not exist: 2", e.getMessage());
    }
    verify(clanMembershipRepository, Mockito.never()).save(any(ClanMembership.class));
  }

  @Test
  public void acceptPlayerInvitationTokenWrongPlayer() throws IOException {
    String stringToken = "1234";

    Player newMember = new Player();
    newMember.setId(2);

    Clan clan = ClanFactory.builder().build();

    Player otherPlayer = new Player();
    otherPlayer.setId(3);

    long expire = System.currentTimeMillis() + 1000 * 3;
    Jwt jwtToken = Mockito.mock(Jwt.class);

    when(jwtToken.getClaims()).thenReturn(
        String.format("{\"expire\":%s,\"newMember\":{\"id\":%s},\"clan\":{\"id\":%s}}",
            expire, newMember.getId(), clan.getId()));
    when(jwtService.decodeAndVerify(any())).thenReturn(jwtToken);
    when(clanRepository.findById(clan.getId())).thenReturn(Optional.of(clan));
    when(playerRepository.findById(newMember.getId())).thenReturn(Optional.of(newMember));
    when(playerService.getPlayer(any())).thenReturn(otherPlayer);

    try {
      instance.acceptPlayerInvitationToken(stringToken, null);
      fail();
    } catch (ApiException e) {
      assertThat(e, hasErrorCode(ErrorCode.CLAN_ACCEPT_WRONG_PLAYER));
    }
    verify(clanMembershipRepository, Mockito.never()).save(any(ClanMembership.class));
  }

  @Test
  public void acceptPlayerInvitationTokenPlayerIAlreadyInAClan() throws IOException {
    String stringToken = "1234";

    Clan clan = ClanFactory.builder().build();

    Player newMember = new Player();
    newMember.setId(2);
    newMember.setClanMembership(new ClanMembership().setClan(clan).setPlayer(newMember));

    long expire = System.currentTimeMillis() + 1000 * 3;
    Jwt jwtToken = Mockito.mock(Jwt.class);

    when(jwtToken.getClaims()).thenReturn(
        String.format("{\"expire\":%s,\"newMember\":{\"id\":%s},\"clan\":{\"id\":%s}}",
            expire, newMember.getId(), clan.getId()));
    when(jwtService.decodeAndVerify(any())).thenReturn(jwtToken);
    when(clanRepository.findById(clan.getId())).thenReturn(Optional.of(clan));
    when(playerRepository.findById(newMember.getId())).thenReturn(Optional.of(newMember));
    when(playerService.getPlayer(any())).thenReturn(newMember);

    try {
      instance.acceptPlayerInvitationToken(stringToken, null);
      fail();
    } catch (ApiException e) {
      assertThat(e, hasErrorCode(ErrorCode.CLAN_ACCEPT_PLAYER_IN_A_CLAN));
    }
    verify(clanMembershipRepository, Mockito.never()).save(any(ClanMembership.class));
  }

  @Test
  public void acceptPlayerInvitationToken() throws IOException {
    String stringToken = "1234";
    Clan clan = ClanFactory.builder().build();
    Player newMember = new Player();
    newMember.setId(2);
    long expire = System.currentTimeMillis() + 1000 * 3;
    Jwt jwtToken = Mockito.mock(Jwt.class);

    when(jwtToken.getClaims()).thenReturn(
        String.format("{\"expire\":%s,\"newMember\":{\"id\":%s},\"clan\":{\"id\":%s}}",
            expire, newMember.getId(), clan.getId()));
    when(jwtService.decodeAndVerify(any())).thenReturn(jwtToken);
    when(clanRepository.findById(clan.getId())).thenReturn(Optional.of(clan));
    when(playerRepository.findById(newMember.getId())).thenReturn(Optional.of(newMember));
    when(playerService.getPlayer(any())).thenReturn(newMember);

    instance.acceptPlayerInvitationToken(stringToken, null);

    ArgumentCaptor<ClanMembership> captor = ArgumentCaptor.forClass(ClanMembership.class);
    verify(clanMembershipRepository, Mockito.times(1)).save(captor.capture());
    assertEquals(newMember.getId(), captor.getValue().getPlayer().getId());
    assertEquals(clan.getId(), captor.getValue().getClan().getId());
  }
}
