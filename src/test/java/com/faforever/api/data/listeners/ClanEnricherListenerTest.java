package com.faforever.api.data.listeners;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Clan;
import com.faforever.integration.factories.ClanFactory;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ClanEnricherListenerTest {
  private ClanEnricherListener instance;


  @Before
  public void setUp() throws Exception {
    instance = new ClanEnricherListener();

    FafApiProperties fafApiProperties = new FafApiProperties();
    instance.init(fafApiProperties);

    fafApiProperties.getClan().setWebsiteUrlFormat("http://example.com/%s");
  }

  @Test
  public void enrich() throws Exception {
    Clan clan = ClanFactory.builder().id(54).build();

    instance.enrich(clan);

    assertThat(clan.getWebsiteUrl(), is("http://example.com/54"));
  }
}
