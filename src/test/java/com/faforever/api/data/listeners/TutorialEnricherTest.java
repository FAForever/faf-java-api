package com.faforever.api.data.listeners;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Tutorial;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.support.MessageSourceAccessor;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class TutorialEnricherTest {
  private TutorialEnricherListener instance;

  @Mock
  private MessageSourceAccessor messageSourceAccessor;

  @Before
  public void setUp() {

    FafApiProperties fafApiProperties = new FafApiProperties();
    fafApiProperties.getTutorial().setThumbnailUrlFormat("http://example.com/%s");

    instance = new TutorialEnricherListener();
    instance.init(fafApiProperties, messageSourceAccessor);
  }

  @Test
  public void enrich() {
    Tutorial tutorial = new Tutorial();
    tutorial.setImage("abc.png");

    instance.enrich(tutorial);

    assertThat(tutorial.getImageUrl(), is("http://example.com/abc.png"));
  }

}
