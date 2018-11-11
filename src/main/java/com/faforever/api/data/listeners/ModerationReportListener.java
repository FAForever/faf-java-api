package com.faforever.api.data.listeners;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.LegacyAccessLevel;
import com.faforever.api.data.domain.LobbyGroup;
import com.faforever.api.data.domain.Login;
import com.faforever.api.data.domain.ModerationReport;
import com.faforever.api.email.EmailService;
import com.faforever.api.user.LobbyGroupRepository;
import com.google.common.collect.Sets;
import com.yahoo.elide.functions.LifeCycleHook;
import com.yahoo.elide.security.RequestScope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ModerationReportListener implements LifeCycleHook<ModerationReport> {
  private FafApiProperties properties;
  private EmailService emailService;
  private LobbyGroupRepository lobbyGroupRepository;

  @Inject
  public ModerationReportListener(FafApiProperties properties, EmailService emailService, LobbyGroupRepository lobbyGroupRepository) {
    this.properties = properties;
    this.emailService = emailService;
    this.lobbyGroupRepository = lobbyGroupRepository;
  }

  @Override
  public void execute(ModerationReport moderationReport, RequestScope requestScope, Optional changes) {
    final HashSet<LegacyAccessLevel> moderatorLevels = Sets.newHashSet(LegacyAccessLevel.ROLE_MODERATOR, LegacyAccessLevel.ROLE_ADMINISTRATOR);
    final Set<String> moderatorEmailAddresses = lobbyGroupRepository.findAllByAccessLevelIn(moderatorLevels).stream()
      .map(LobbyGroup::getUser)
      .map(Login::getEmail)
      .collect(Collectors.toSet());
    emailService.sendMail(
      moderatorEmailAddresses,
      properties.getModerationReport().getNotificationEmailSubject(),
      MessageFormat.format(properties.getModerationReport().getNotificationEmailBodyTemplate(),
        moderationReport.getReporter().getLogin(),
        moderationReport.getReportDescription(),
        moderationReport.getGameIncidentTimecode(),
        moderationReport.getReportedUsers().stream().map(Login::getLogin).collect(Collectors.toSet())
      )
    );
  }
}
