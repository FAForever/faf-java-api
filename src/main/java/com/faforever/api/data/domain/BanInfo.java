package com.faforever.api.data.domain;

import com.faforever.api.data.checks.permission.HasBanRead;
import com.faforever.api.data.checks.permission.HasBanUpdate;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import com.yahoo.elide.security.checks.prefab.Role;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@DeletePermission(any = {Role.NONE.class})
@ReadPermission(expression = HasBanRead.EXPRESSION)
@CreatePermission(expression = HasBanUpdate.EXPRESSION)
@UpdatePermission(expression = HasBanUpdate.EXPRESSION)
@Setter
public class BanInfo {

  private int id;
  private Player player;
  private Player author;
  private String reason;
  private OffsetDateTime expiresAt;
  private BanType type;
  private OffsetDateTime createTime;
  private OffsetDateTime updateTime;
  private BanRevokeData banRevokeData;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  public int getId() {
    return id;
  }

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

  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  public BanType getType() {
    return type;
  }

  @Column(name = "create_time")
  public OffsetDateTime getCreateTime() {
    return createTime;
  }

  @Column(name = "update_time")
  public OffsetDateTime getUpdateTime() {
    return updateTime;
  }

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  // Cascading is needed for Create & Delete
  @JoinColumn(name = "id")
  public BanRevokeData getBanRevokeData() {
    return banRevokeData;
  }

  @Transient
  public BanDurationType getDuration() {
    return (expiresAt == null) ? BanDurationType.PERMANENT : BanDurationType.TEMPORARY;
  }

  @Transient
  public BanStatusType getBanStatus() {
    if (banRevokeData != null) {
      return BanStatusType.DISABLED;
    }
    if (getDuration() == BanDurationType.PERMANENT) {
      return BanStatusType.BANNED;
    }
    return (expiresAt.isBefore(OffsetDateTime.now()))
        ? BanStatusType.BANNED
        : BanStatusType.EXPIRED;
  }
}
