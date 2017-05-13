package com.faforever.api.data.domain;

import com.faforever.api.data.checks.permission.HasBanRead;
import com.faforever.api.data.checks.permission.HasBanUpdate;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ban")
@Include(rootLevel = true, type = "banInfo")
// Bans can never be deleted, only disabled over BanDisableData
@DeletePermission(expression = "Prefab.Role.None")
@ReadPermission(expression = HasBanRead.EXPRESSION)
@CreatePermission(expression = HasBanUpdate.EXPRESSION)
@UpdatePermission(expression = HasBanUpdate.EXPRESSION)
@Setter
public class BanInfo extends AbstractEntity {
  private Player player;
  private Player author;
  private String reason;
  private OffsetDateTime expiresAt;
  private BanLevel level;
  private BanRevokeData banRevokeData;

  @ManyToOne
  @JoinColumn(name = "player_id")
  @NotNull
  public Player getPlayer() {
    return player;
  }

  @ManyToOne
  @JoinColumn(name = "author_id")
  @NotNull
  public Player getAuthor() {
    return author;
  }

  @Column(name = "reason")
  @NotNull
  public String getReason() {
    return reason;
  }

  @Column(name = "expires_at")
  public OffsetDateTime getExpiresAt() {
    return expiresAt;
  }

  @Column(name = "level")
  @Enumerated(EnumType.STRING)
  public BanLevel getLevel() {
    return level;
  }

  // Cascading is needed for Create & Delete
  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "id")
  public BanRevokeData getBanRevokeData() {
    return banRevokeData;
  }

  @Transient
  public BanDurationType getDuration() {
    return expiresAt == null ? BanDurationType.PERMANENT : BanDurationType.TEMPORARY;
  }

  @Transient
  public BanStatus getBanStatus() {
    if (banRevokeData != null) {
      return BanStatus.DISABLED;
    }
    if (getDuration() == BanDurationType.PERMANENT) {
      return BanStatus.BANNED;
    }
    return expiresAt.isBefore(OffsetDateTime.now())
      ? BanStatus.BANNED
      : BanStatus.EXPIRED;
  }
}
