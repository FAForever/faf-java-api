package com.faforever.api.data.listeners;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Tutorial;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.MessageSourceAccessor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
public class TutorialEnricherTest {
  private TutorialEnricherListener instance;

  @Mock
  private MessageSourceAccessor messageSourceAccessor;

  @BeforeEach
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
