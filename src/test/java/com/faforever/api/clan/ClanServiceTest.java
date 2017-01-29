package com.faforever.api.clan;


import com.faforever.api.authentication.AuthenticationService;
import com.faforever.api.authentication.JwtService;
import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Clan;
import com.faforever.api.data.domain.Player;
import com.faforever.api.player.PlayerRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ClanServiceTest {
  private ClanService instance;

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
