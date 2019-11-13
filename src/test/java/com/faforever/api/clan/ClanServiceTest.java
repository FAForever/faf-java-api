package com.faforever.api.clan;


import com.faforever.api.clan.result.InvitationResult;
import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Clan;
import com.faforever.api.data.domain.ClanMembership;
import com.faforever.api.data.domain.Player;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.player.PlayerService;
import com.faforever.api.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.jwt.Jwt;

import java.util.Optional;

import static com.faforever.api.error.ApiExceptionMatcher.hasErrorCode;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
public class ClanServiceTest {
  @Mock
  private ClanRepository clanRepository;
  @Mock
  private FafApiProperties fafApiProperties;
  @Mock
  private JwtService jwtService;
  @Mock
  private PlayerService playerService;
  @Mock
  private ClanMembershipRepository clanMembershipRepository;
  private ObjectMapper objectMapper = new ObjectMapper();

  private ClanService instance;

  @BeforeEach
  void setUp() {
    instance = new ClanService(clanRepository, fafApiProperties, jwtService, objectMapper,
      playerService, clanMembershipRepository);
  }

  @Nested
  class TestPreCreate {
    @Mock
    private Clan clan;
    @Mock
    private Player player;

    @BeforeEach
    void setUp() {
      when(clan.getId()).thenReturn(null);
      when(playerService.getCurrentPlayer()).thenReturn(player);
    }

    @Test
    void invalidClanCreate() {
      reset(clan, playerService);
      when(clan.getId()).thenReturn(1);

      assertThrows(IllegalArgumentException.class, () -> instance.preCreate(clan));
    }

    @Test
    void founderIsInAClan() {
      when(player.getClanMembership()).thenReturn(mock(ClanMembership.class));

      ApiException e = assertThrows(ApiException.class, () -> instance.preCreate(clan));
      assertThat(e, hasErrorCode(ErrorCode.CLAN_CREATE_FOUNDER_IS_IN_A_CLAN));

      verify(playerService).getCurrentPlayer();
    }

    @Test
    void invalidFounder() {
      when(clan.getFounder()).thenReturn(mock(Player.class));

      ApiException e = assertThrows(ApiException.class, () -> instance.preCreate(clan));
      assertThat(e, hasErrorCode(ErrorCode.CLAN_INVALID_FOUNDER));

      verify(playerService).getCurrentPlayer();
    }

    @Test
    void clanNameExists() {
      when(clan.getFounder()).thenReturn(player);
      when(clan.getName()).thenReturn("someName");
      when(clanRepository.findOneByName("someName")).thenReturn(Optional.of(mock(Clan.class)));

      ApiException e = assertThrows(ApiException.class, () -> instance.preCreate(clan));
      assertThat(e, hasErrorCode(ErrorCode.CLAN_NAME_EXISTS));

      verify(playerService).getCurrentPlayer();
      verify(clanRepository).findOneByName("someName");
    }

    @Test
    void clanTagExists() {
      when(clan.getFounder()).thenReturn(player);
      when(clan.getTag()).thenReturn("someTag");
      when(clanRepository.findOneByName(any())).thenReturn(Optional.empty());
      when(clanRepository.findOneByTag("someTag")).thenReturn(Optional.of(mock(Clan.class)));

      ApiException e = assertThrows(ApiException.class, () -> instance.preCreate(clan));
      assertThat(e, hasErrorCode(ErrorCode.CLAN_TAG_EXISTS));

      verify(playerService).getCurrentPlayer();
      verify(clanRepository).findOneByName(any());
      verify(clanRepository).findOneByTag("someTag");
    }

    @Test
    void success() {
      reset(clan);

      clan = new Clan();
      clan.setFounder(player);
      when(clanRepository.findOneByName(any())).thenReturn(Optional.empty());
      when(clanRepository.findOneByTag(any())).thenReturn(Optional.empty());

      instance.preCreate(clan);

      assertThat(clan.getFounder(), is(player));
      assertThat(clan.getLeader(), is(player));
      assertThat(clan.getMemberships().size(), is(1));
      assertThat(clan.getMemberships().get(0).getPlayer(), is(player));

      verify(playerService).getCurrentPlayer();
      verify(clanRepository).findOneByName(any());
      verify(clanRepository).findOneByTag(any());
    }

  }

  @Nested
  class TestGeneratePlayerInvitationToken {
    @Mock
    Player player;

    @BeforeEach
    void setUp() {
      when(player.getId()).thenReturn(1);
      when(playerService.getCurrentPlayer()).thenReturn(player);
    }

    @Test
    void invalidClan() throws Exception {
      reset(player);

      ApiException e = assertThrows(ApiException.class, () -> instance.generatePlayerInvitationToken(45, 42));

      assertThat(e, hasErrorCode(ErrorCode.CLAN_NOT_EXISTS));
      verify(jwtService, never()).sign(any());
    }

    @Test
    void generateTokenAsNonLeader() throws Exception {
      Player newMember = new Player();
      newMember.setId(2);

      Player leader = new Player();
      leader.setId(3);

      Clan clan = ClanFactory.builder().id(42).leader(leader).build();

      when(clanRepository.findById(clan.getId())).thenReturn(Optional.of(clan));

      ApiException e = assertThrows(ApiException.class, () -> instance.generatePlayerInvitationToken(45, 42));

      assertThat(e, hasErrorCode(ErrorCode.CLAN_NOT_LEADER));
      verify(jwtService, never()).sign(any());
    }

    @Test
    void invalidPlayer() throws Exception {
      Clan clan = ClanFactory.builder().id(42).leader(player).build();

      when(clanRepository.findById(clan.getId())).thenReturn(Optional.of(clan));
      when(playerService.getById(45)).thenThrow(ApiException.of(ErrorCode.PLAYER_NOT_FOUND));

      ApiException e = assertThrows(ApiException.class, () -> instance.generatePlayerInvitationToken(45, 42));

      assertThat(e, hasErrorCode(ErrorCode.PLAYER_NOT_FOUND));
      verify(jwtService, never()).sign(any());
    }

    @Test
    void success() throws Exception {
      Player newMember = new Player();
      newMember.setId(2);

      Clan clan = ClanFactory.builder().leader(player).build();

      FafApiProperties props = new FafApiProperties();

      when(clanRepository.findById(clan.getId())).thenReturn(Optional.of(clan));
      when(playerService.getById(newMember.getId())).thenReturn(newMember);
      when(fafApiProperties.getClan()).thenReturn(props.getClan());

      instance.generatePlayerInvitationToken(newMember.getId(), clan.getId());
      ArgumentCaptor<InvitationResult> captor = ArgumentCaptor.forClass(InvitationResult.class);
      verify(jwtService).sign(captor.capture());
      assertThat("expire",
        captor.getValue().getExpire(),
        greaterThan(System.currentTimeMillis()));
      assertEquals(newMember.getId(), captor.getValue().getNewMember().getId());
      assertEquals(newMember.getLogin(), captor.getValue().getNewMember().getLogin());
      assertEquals(clan.getId(), captor.getValue().getClan().getId());
      assertEquals(clan.getTag(), captor.getValue().getClan().getTag());
      assertEquals(clan.getName(), captor.getValue().getClan().getName());
    }

  }

  @Nested
  class TestAcceptPlayerInvitationToken {
    @Mock
    private Jwt jwtToken;

    private final String STRING_TOKEN = "1234";

    private long expire;

    private Clan clan;

    @BeforeEach
    void setUp() {
      expire = System.currentTimeMillis() + 1000 * 3;
      clan = ClanFactory.builder().build();
      when(jwtService.decodeAndVerify(any())).thenReturn(jwtToken);
    }

    @Test
    void expired() {
      expire = System.currentTimeMillis();

      when(jwtToken.getClaims()).thenReturn(String.format("{\"expire\":%s}", expire));

      ApiException e = assertThrows(ApiException.class, () -> instance.acceptPlayerInvitationToken(STRING_TOKEN));

      assertThat(e, hasErrorCode(ErrorCode.CLAN_ACCEPT_TOKEN_EXPIRE));
      verifyZeroInteractions(clanMembershipRepository);
    }

    @Test
    void invalidClan() {
      when(jwtToken.getClaims()).thenReturn(String.format("{\"expire\":%s,\"clan\":{\"id\":42}}", expire));

      ApiException e = assertThrows(ApiException.class, () -> instance.acceptPlayerInvitationToken(STRING_TOKEN));

      assertThat(e, hasErrorCode(ErrorCode.CLAN_NOT_EXISTS));
      verifyZeroInteractions(clanMembershipRepository);
    }

    @Test
    void invalidPlayer() {
      Player requester = new Player();
      requester.setId(1);
      when(playerService.getCurrentPlayer()).thenReturn(requester);

      when(jwtToken.getClaims()).thenReturn(
        String.format("{\"expire\":%s,\"newMember\":{\"id\":2},\"clan\":{\"id\":%s}}",
          expire, clan.getId()));
      when(clanRepository.findById(clan.getId())).thenReturn(Optional.of(clan));
      when(playerService.getById(2)).thenThrow(ApiException.of(ErrorCode.PLAYER_NOT_FOUND));

      ApiException e = assertThrows(ApiException.class, () -> instance.acceptPlayerInvitationToken(STRING_TOKEN));

      assertThat(e, hasErrorCode(ErrorCode.PLAYER_NOT_FOUND));
      verifyZeroInteractions(clanMembershipRepository);
    }

    @Test
    void playerInTokenNotLoggedInPlayer() {
      Player newMember = new Player();
      newMember.setId(2);

      Player loggedInPlayer = new Player();
      loggedInPlayer.setId(3);

      when(jwtToken.getClaims()).thenReturn(
        String.format("{\"expire\":%s,\"newMember\":{\"id\":%s},\"clan\":{\"id\":%s}}",
          expire, newMember.getId(), clan.getId()));
      when(clanRepository.findById(clan.getId())).thenReturn(Optional.of(clan));
      when(playerService.getById(newMember.getId())).thenReturn(newMember);
      when(playerService.getCurrentPlayer()).thenReturn(loggedInPlayer);

      ApiException e = assertThrows(ApiException.class, () -> instance.acceptPlayerInvitationToken(STRING_TOKEN));

      assertThat(e, hasErrorCode(ErrorCode.CLAN_ACCEPT_WRONG_PLAYER));
      verifyZeroInteractions(clanMembershipRepository);
    }

    @Test
    void playerIsAlreadyInClan() {
      Player newMember = new Player();
      newMember.setId(2);
      newMember.setClanMembership(new ClanMembership().setClan(clan).setPlayer(newMember));

      when(jwtToken.getClaims()).thenReturn(
        String.format("{\"expire\":%s,\"newMember\":{\"id\":%s},\"clan\":{\"id\":%s}}",
          expire, newMember.getId(), clan.getId()));

      when(clanRepository.findById(clan.getId())).thenReturn(Optional.of(clan));
      when(playerService.getById(newMember.getId())).thenReturn(newMember);
      when(playerService.getCurrentPlayer()).thenReturn(newMember);

      ApiException e = assertThrows(ApiException.class, () -> instance.acceptPlayerInvitationToken(STRING_TOKEN));

      assertThat(e, hasErrorCode(ErrorCode.CLAN_ACCEPT_PLAYER_IN_A_CLAN));
      verifyZeroInteractions(clanMembershipRepository);
    }

    @Test
    void success() {
      Player newMember = new Player();
      newMember.setId(2);

      when(jwtToken.getClaims()).thenReturn(
        String.format("{\"expire\":%s,\"newMember\":{\"id\":%s},\"clan\":{\"id\":%s}}",
          expire, newMember.getId(), clan.getId()));

      when(clanRepository.findById(clan.getId())).thenReturn(Optional.of(clan));
      when(playerService.getById(newMember.getId())).thenReturn(newMember);
      when(playerService.getCurrentPlayer()).thenReturn(newMember);

      instance.acceptPlayerInvitationToken(STRING_TOKEN);

      ArgumentCaptor<ClanMembership> captor = ArgumentCaptor.forClass(ClanMembership.class);
      verify(clanMembershipRepository).save(captor.capture());

      assertEquals(newMember.getId(), captor.getValue().getPlayer().getId());
      assertEquals(clan.getId(), captor.getValue().getClan().getId());
    }
  }

  @Test
  @Deprecated
  void testCreate() {
    Player player = mock(Player.class);
    when(playerService.getCurrentPlayer()).thenReturn(player);

    String clanName = "My cool Clan";
    String tag = "123";
    String description = "A cool clan for testing";

    Clan clan = instance.create(clanName, tag, description);

    assertAll(
      () -> assertThat(clan.getName(), is(clanName)),
      () -> assertThat(clan.getTag(), is(tag)),
      () -> assertThat(clan.getDescription(), is(description)),
      () -> assertThat(clan.getFounder(), is(player))
    );

    verify(clanRepository).save(eq(clan));
  }
}
