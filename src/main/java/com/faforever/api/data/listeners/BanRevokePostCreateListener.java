package com.faforever.api.data.listeners;

import com.faforever.api.data.domain.BanInfo;
import com.faforever.api.data.domain.BanRevokeData;
import com.faforever.api.data.domain.Player;
import com.faforever.api.email.EmailService;
import com.yahoo.elide.functions.LifeCycleHook;
import com.yahoo.elide.security.ChangeSpec;
import com.yahoo.elide.security.RequestScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Component
public class BanRevokePostCreateListener implements LifeCycleHook<BanRevokeData> {
  private final EmailService emailService;

  public BanRevokePostCreateListener(EmailService emailService) {
    this.emailService = emailService;
  }

  @Override
  public void execute(BanRevokeData banRevoke, RequestScope requestScope, Optional<ChangeSpec> changes) {
    try {
      @NotNull BanInfo ban = banRevoke.getBan();
      @NotNull Player player = ban.getPlayer();
      emailService.sendBanRevokeMail(player.getEmail(),
        player.getLogin(),
        banRevoke.getAuthor().getLogin(),
        banRevoke.getReason(),
        ban.getReason(),
        ban.getAuthor().getLogin(),
        ban.getCreateTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    } catch (Exception e) {
      log.error("Failed to send ban revoke email", e);
    }
  }
}
