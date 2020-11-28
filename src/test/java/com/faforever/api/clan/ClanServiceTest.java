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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Optional;

import static com.faforever.api.error.ApiExceptionMatcher.hasErrorCode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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
      assertThat(clan.getMemberships().iterator().next().getPlayer(), is(player));

      verify(playerService).getCurrentPlayer();
      verify(clanRepository).findOneByName(any());
      verify(clanRepository).findOneByTag(any());
    }

  }

  @Test
  void createClanWhereLeaderIsAlreadyInAClan() {
    String clanName = "My cool Clan";
    String tag = "123";
    String description = "A cool clan for testing";
    Player creator = new Player();
    creator.setId(1);
    creator.setClanMembership(new ClanMembership());

    when(playerService.getCurrentPlayer()).thenReturn(creator);
    ApiException result = assertThrows(ApiException.class, () -> instance.create(clanName, tag, description, creator));
    assertThat(result, hasErrorCode(ErrorCode.CLAN_CREATE_FOUNDER_IS_IN_A_CLAN));

    verify(clanRepository, Mockito.never()).save(any(Clan.class));
  }

  @Test
  void createClanWithSameName() {
    String clanName = "My cool Clan";
    String tag = "123";
    String description = "A cool clan for testing";

    Player creator = new Player();
    creator.setId(1);
    when(playerService.getCurrentPlayer()).thenReturn(creator);

    when(clanRepository.findOneByName(clanName)).thenReturn(Optional.of(new Clan()));

    ApiException result = assertThrows(ApiException.class, () -> instance.create(clanName, tag, description, creator));
    assertThat(result, hasErrorCode(ErrorCode.CLAN_NAME_EXISTS));

    ArgumentCaptor<Clan> clanCaptor = ArgumentCaptor.forClass(Clan.class);
    verify(clanRepository, Mockito.times(0)).save(clanCaptor.capture());
  }

  @Test
  void createClanWithSameTag() {
    String clanName = "My cool Clan";
    String tag = "123";
    String description = "A cool clan for testing";

    Player creator = new Player();
    creator.setId(1);
    when(playerService.getCurrentPlayer()).thenReturn(creator);

    when(clanRepository.findOneByName(clanName)).thenReturn(Optional.empty());
    when(clanRepository.findOneByTag(tag)).thenReturn(Optional.of(new Clan()));

    ApiException result = assertThrows(ApiException.class, () -> instance.create(clanName, tag, description, creator));
    assertThat(result, hasErrorCode(ErrorCode.CLAN_TAG_EXISTS));

    ArgumentCaptor<Clan> clanCaptor = ArgumentCaptor.forClass(Clan.class);
    verify(clanRepository, Mockito.times(0)).save(clanCaptor.capture());
  }

  @Test
  void createClanSuccessful() {
    String clanName = "My cool Clan";
    String tag = "123";
    String description = "A cool clan for testing";

    Player creator = new Player();
    creator.setId(1);
    when(playerService.getCurrentPlayer()).thenReturn(creator);

    when(clanRepository.findOneByName(clanName)).thenReturn(Optional.empty());
    when(clanRepository.findOneByTag(tag)).thenReturn(Optional.empty());


    instance.create(clanName, tag, description);
    ArgumentCaptor<Clan> clanCaptor = ArgumentCaptor.forClass(Clan.class);
    verify(clanRepository, Mockito.times(1)).save(clanCaptor.capture());
    assertEquals(clanName, clanCaptor.getValue().getName());
    assertEquals(tag, clanCaptor.getValue().getTag());
    assertEquals(description, clanCaptor.getValue().getDescription());
    assertEquals(creator, clanCaptor.getValue().getLeader());
    assertEquals(creator, clanCaptor.getValue().getFounder());
    assertEquals(1, clanCaptor.getValue().getMemberships().size());
    assertEquals(creator, clanCaptor.getValue().getMemberships().stream().findFirst().get().getPlayer());
  }

  @Test
  void generatePlayerInvitationTokenWithInvalidClan() throws IOException {
    Player requester = new Player();
    requester.setId(1);
    when(playerService.getCurrentPlayer()).thenReturn(requester);

    ApiException result = assertThrows(ApiException.class, () -> instance.generatePlayerInvitationToken(requester, 45, 42));
    assertThat(result, hasErrorCode(ErrorCode.CLAN_NOT_EXISTS));

    verify(jwtService, Mockito.never()).sign(any());
  }

  @Test
  void generatePlayerInvitationTokenFromNonLeader() throws IOException {
    Player requester = new Player();
    requester.setId(1);
    when(playerService.getCurrentPlayer()).thenReturn(requester);

    Player newMember = new Player();
    newMember.setId(2);

    Player leader = new Player();
    leader.setId(3);

    Clan clan = ClanFactory.builder().id(42).leader(leader).build();

    when(clanRepository.findById(clan.getId())).thenReturn(Optional.of(clan));

    ApiException result = assertThrows(ApiException.class, () -> instance.generatePlayerInvitationToken(requester, newMember.getId(), clan.getId()));
    assertThat(result, hasErrorCode(ErrorCode.CLAN_NOT_LEADER));

    verify(jwtService, Mockito.never()).sign(any());
  }

  @Test
  void generatePlayerInvitationTokenInvalidPlayer() throws IOException {
    Player requester = new Player();
    requester.setId(1);
    when(playerService.getCurrentPlayer()).thenReturn(requester);

    Clan clan = ClanFactory.builder().id(42).leader(requester).build();

    when(clanRepository.findById(clan.getId())).thenReturn(Optional.of(clan));
    when(playerService.getById(45)).thenThrow(ApiException.of(ErrorCode.PLAYER_NOT_FOUND));

    ApiException result = assertThrows(ApiException.class, () -> instance.generatePlayerInvitationToken(requester, 42, clan.getId()));
    assertThat(result, hasErrorCode(ErrorCode.CLAN_GENERATE_LINK_PLAYER_NOT_FOUND));

    verify(jwtService, Mockito.never()).sign(any());
  }

  @Test
  void generatePlayerInvitationToken() throws IOException {
    Player requester = new Player();
    requester.setId(1);
    when(playerService.getCurrentPlayer()).thenReturn(requester);

    Player newMember = new Player();
    newMember.setId(2);

    Clan clan = ClanFactory.builder().leader(requester).build();

    FafApiProperties props = new FafApiProperties();

    when(clanRepository.findById(clan.getId())).thenReturn(Optional.of(clan));
    when(playerService.getById(newMember.getId())).thenReturn(newMember);
    when(fafApiProperties.getClan()).thenReturn(props.getClan());

    instance.generatePlayerInvitationToken(newMember.getId(), clan.getId());
    ArgumentCaptor<InvitationResult> captor = ArgumentCaptor.forClass(InvitationResult.class);
    verify(jwtService, Mockito.times(1)).sign(captor.capture());
    assertThat("expire",
      captor.getValue().expire(),
      greaterThan(System.currentTimeMillis()));
    assertEquals(newMember.getId(), captor.getValue().newMember().id());
    assertEquals(newMember.getLogin(), captor.getValue().newMember().login());
    assertEquals(clan.getId(), captor.getValue().clan().id());
    assertEquals(clan.getTag(), captor.getValue().clan().tag());
    assertEquals(clan.getName(), captor.getValue().clan().name());
  }

  @Test
  void acceptPlayerInvitationTokenExpire() throws IOException {
    String stringToken = "1234";
    long expire = System.currentTimeMillis();

    when(jwtService.decodeAndVerify(any())).thenReturn(
      String.format("{\"expire\":%s}", expire));


    ApiException result = assertThrows(ApiException.class, () -> instance.acceptPlayerInvitationToken(stringToken, null));
    assertThat(result, hasErrorCode(ErrorCode.CLAN_ACCEPT_TOKEN_EXPIRE));

    verify(clanMembershipRepository, Mockito.never()).save(any(ClanMembership.class));
  }

  @Test
  void acceptPlayerInvitationTokenInvalidClan() throws IOException {
    String stringToken = "1234";

    long expire = System.currentTimeMillis() + 1000 * 3;
    when(jwtService.decodeAndVerify(any())).thenReturn(String.format("{\"expire\":%s,\"clan\":{\"id\":42}}", expire));

    ApiException result = assertThrows(ApiException.class, () -> instance.acceptPlayerInvitationToken(stringToken, null));
      assertThat(result, hasErrorCode(ErrorCode.CLAN_NOT_EXISTS));

    verify(clanMembershipRepository, Mockito.never()).save(any(ClanMembership.class));
  }

  @Test
  void acceptPlayerInvitationTokenInvalidPlayer() throws IOException {
    Player requester = new Player();
    requester.setId(1);
    when(playerService.getCurrentPlayer()).thenReturn(requester);

    String stringToken = "1234";
    Clan clan = ClanFactory.builder().build();

    long expire = System.currentTimeMillis() + 1000 * 3;
    String tokenResult = String.format("{\"expire\":%s,\"newMember\":{\"id\":2},\"clan\":{\"id\":%s}}",
      expire, clan.getId());
    when(jwtService.decodeAndVerify(any())).thenReturn(tokenResult);
    when(clanRepository.findById(clan.getId())).thenReturn(Optional.of(clan));
    when(playerService.getById(2)).thenThrow(ApiException.of(ErrorCode.PLAYER_NOT_FOUND));

    ProgrammingError result = assertThrows(ProgrammingError.class, () -> instance.acceptPlayerInvitationToken(stringToken, null));
    assertThat(result.getMessage(), is("ClanMember does not exist: 2"));

    verify(clanMembershipRepository, Mockito.never()).save(any(ClanMembership.class));
  }

  @Test
  void acceptPlayerInvitationTokenWrongPlayer() throws IOException {
    String stringToken = "1234";

    Player newMember = new Player();
    newMember.setId(2);

    Clan clan = ClanFactory.builder().build();

    Player otherPlayer = new Player();
    otherPlayer.setId(3);

    long expire = System.currentTimeMillis() + 1000 * 3;
    String tokenResult = String.format("{\"expire\":%s,\"newMember\":{\"id\":%s},\"clan\":{\"id\":%s}}",
      expire, newMember.getId(), clan.getId());
    when(jwtService.decodeAndVerify(any())).thenReturn(tokenResult);
    when(clanRepository.findById(clan.getId())).thenReturn(Optional.of(clan));
    when(playerService.getById(newMember.getId())).thenReturn(newMember);
    when(playerService.getCurrentPlayer()).thenReturn(otherPlayer);

    ApiException result = assertThrows(ApiException.class, () -> instance.acceptPlayerInvitationToken(stringToken, null));
    assertThat(result, hasErrorCode(ErrorCode.CLAN_ACCEPT_WRONG_PLAYER));

    verify(clanMembershipRepository, Mockito.never()).save(any(ClanMembership.class));
  }

  @Test
  void acceptPlayerInvitationTokenPlayerIAlreadyInAClan() throws IOException {
    String stringToken = "1234";

    Clan clan = ClanFactory.builder().build();

    Player newMember = new Player();
    newMember.setId(2);
    newMember.setClanMembership(new ClanMembership().setClan(clan).setPlayer(newMember));

    long expire = System.currentTimeMillis() + 1000 * 3;
    String tokenResult = String.format("{\"expire\":%s,\"newMember\":{\"id\":%s},\"clan\":{\"id\":%s}}",
      expire, newMember.getId(), clan.getId());

    when(jwtService.decodeAndVerify(any())).thenReturn(tokenResult);
    when(clanRepository.findById(clan.getId())).thenReturn(Optional.of(clan));
    when(playerService.getById(newMember.getId())).thenReturn(newMember);
    when(playerService.getCurrentPlayer()).thenReturn(newMember);

    ApiException result = assertThrows(ApiException.class, () -> instance.acceptPlayerInvitationToken(stringToken, null));
    assertThat(result, hasErrorCode(ErrorCode.CLAN_ACCEPT_PLAYER_IN_A_CLAN));

    verify(clanMembershipRepository, Mockito.never()).save(any(ClanMembership.class));
  }

  @Test
  void acceptPlayerInvitationToken() throws IOException {
    String stringToken = "1234";
    Clan clan = ClanFactory.builder().build();
    Player newMember = new Player();
    newMember.setId(2);
    long expire = System.currentTimeMillis() + 1000 * 3;
    String tokenResult = String.format("{\"expire\":%s,\"newMember\":{\"id\":%s},\"clan\":{\"id\":%s}}",
      expire, newMember.getId(), clan.getId());

    when(jwtService.decodeAndVerify(any())).thenReturn(tokenResult);
    when(clanRepository.findById(clan.getId())).thenReturn(Optional.of(clan));
    when(playerService.getById(newMember.getId())).thenReturn(newMember);
    when(playerService.getCurrentPlayer()).thenReturn(newMember);

    instance.acceptPlayerInvitationToken(stringToken);

    ArgumentCaptor<ClanMembership> captor = ArgumentCaptor.forClass(ClanMembership.class);
    verify(clanMembershipRepository, Mockito.times(1)).save(captor.capture());
    assertEquals(newMember.getId(), captor.getValue().getPlayer().getId());
    assertEquals(clan.getId(), captor.getValue().getClan().getId());
  }
}
