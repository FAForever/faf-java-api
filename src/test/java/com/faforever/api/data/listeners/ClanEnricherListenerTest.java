package com.faforever.api.data.listeners;

import com.faforever.api.clan.ClanFactory;
import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Clan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ClanEnricherListenerTest {
  private ClanEnricherListener instance;

  @BeforeEach
  void setUp() throws Exception {
    instance = new ClanEnricherListener();

    FafApiProperties fafApiProperties = new FafApiProperties();
    instance.init(fafApiProperties);

    fafApiProperties.getClan().setWebsiteUrlFormat("http://example.com/%s");
  }

  @Test
  void enrich() throws Exception {
    Clan clan = ClanFactory.builder().id(54).build();

    instance.enrich(clan);

    assertThat(clan.getWebsiteUrl(), is("http://example.com/54"));
  }
}
