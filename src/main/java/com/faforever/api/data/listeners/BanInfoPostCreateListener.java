package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.BanDurationType;
import com.faforever.api.data.domain.BanInfo;
import com.faforever.api.data.domain.Player;
import com.faforever.api.email.EmailService;
import com.yahoo.elide.functions.LifeCycleHook;
import com.yahoo.elide.security.RequestScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
@Slf4j
public class BanInfoPostCreateListener implements LifeCycleHook<BanInfo> {

  private final EmailService emailService;

  @Inject
  public BanInfoPostCreateListener(EmailService emailService) {
    this.emailService = emailService;
  }

  @Override
  public void execute(BanInfo elideEntity, RequestScope requestScope, Optional changes) {
    try {
      @NotNull Player player = elideEntity.getPlayer();
      emailService.sendBanMail(player.getEmail(),
        player.getLogin(),
        elideEntity.getReason(),
        elideEntity.getAuthor().getLogin(),
        OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        elideEntity.getLevel().name(),
        elideEntity.getDuration() == BanDurationType.PERMANENT ? "never" : elideEntity.getExpiresAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    } catch (Exception e) {
      log.error("Sending ban email failed", e);
    }
  }
}
