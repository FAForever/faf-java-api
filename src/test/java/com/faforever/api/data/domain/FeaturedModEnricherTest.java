package com.faforever.api.data.domain;

import com.faforever.api.config.FafApiProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class FeaturedModEnricherTest {

  private FeaturedModEnricher featuredModEnricher;

  @Before
  public void setUp() throws Exception {
    FafApiProperties fafApiProperties = new FafApiProperties();
    fafApiProperties.getFeaturedMod().setBireusUrlFormat("http://example.com/%s");

    featuredModEnricher = new FeaturedModEnricher();
    featuredModEnricher.init(fafApiProperties);

  }

  @Test
  public void enrich() throws Exception {
    FeaturedMod featuredMod = new FeaturedMod();
    featuredMod.setTechnicalName("faf");

    featuredModEnricher.enrich(featuredMod);

    assertThat(featuredMod.getBireusUrl(), is("http://example.com/faf"));
  }
}
