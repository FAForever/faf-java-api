package com.faforever.api.data.listeners;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.i18n.RepositoryMessageSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Hands the message source dependencies to the entity listeners that need them.
 * <p>
 * The listeners must not declare injection points themselves: Hibernate creates them while the
 * EntityManagerFactory is being built and Spring autowires those instances. MessageSourceAccessor and
 * RepositoryMessageSource depend on JPA repositories, which would close a cycle back to the
 * EntityManagerFactory.
 */
@Component
@RequiredArgsConstructor
public class MessageListenerInitializer {
  private final FafApiProperties fafApiProperties;
  private final MessageSourceAccessor messageSourceAccessor;
  private final RepositoryMessageSource repositoryMessageSource;

  private final AchievementLocalizationListener achievementLocalizationListener;
  private final EventLocalizationListener eventLocalizationListener;
  private final MessageReloadListener messageReloadListener;
  private final TutorialCategoryEnricherListener tutorialCategoryEnricherListener;
  private final TutorialEnricherListener tutorialEnricherListener;
  private final VotingChoiceEnricher votingChoiceEnricher;
  private final VotingQuestionEnricher votingQuestionEnricher;
  private final VotingSubjectEnricher votingSubjectEnricher;

  @PostConstruct
  public void init() {
    achievementLocalizationListener.init(messageSourceAccessor);
    eventLocalizationListener.init(messageSourceAccessor);
    messageReloadListener.init(repositoryMessageSource);
    tutorialCategoryEnricherListener.init(messageSourceAccessor);
    tutorialEnricherListener.init(fafApiProperties, messageSourceAccessor);
    votingChoiceEnricher.init(messageSourceAccessor);
    votingQuestionEnricher.init(messageSourceAccessor);
    votingSubjectEnricher.init(messageSourceAccessor);
  }
}
