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

import java.util.ArrayList;

import static com.faforever.api.error.ApiExceptionWithCode.apiExceptionWithCode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

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
  public void createWithClanError() {
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
  public void createSuccessful() {
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
}
