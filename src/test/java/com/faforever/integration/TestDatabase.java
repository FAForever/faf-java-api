package com.faforever.integration;

import com.faforever.api.clan.ClanMembershipRepository;
import com.faforever.api.clan.ClanRepository;
import com.faforever.api.client.OAuthClientRepository;
import com.faforever.api.player.PlayerRepository;
import com.faforever.api.user.UserRepository;
import lombok.Getter;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import javax.servlet.Filter;

import static org.junit.Assert.assertEquals;

@Component
@Getter
public class TestDatabase {
  private WebApplicationContext context;
  private Filter springSecurityFilterChain;
  private ClanRepository clanRepository;
  private UserRepository userRepository;
  private ClanMembershipRepository clanMembershipRepository;
  private PlayerRepository playerRepository;
  private OAuthClientRepository oAuthClientRepository;

  private ObjectMapper objectMapper;

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
}
