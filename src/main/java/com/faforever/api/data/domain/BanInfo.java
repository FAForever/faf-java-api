package com.faforever.api.data.domain;

import com.faforever.api.data.checks.permission.HasBanInfoRead;
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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "ban")
@Include(rootLevel = true, type = "banInfo")
// Bans can be never deleted, only disabled over BanDisableData
@DeletePermission(any = {Role.NONE.class})
@ReadPermission(expression = HasBanInfoRead.EXPRESSION)
@Setter
public class BanInfo {

  private int id;
  private Player player;
  private Player author;
  private String reason;
  private OffsetDateTime expiresAt;
  private BanType type;
  private BanDurationType duration;
  private OffsetDateTime createTime;
  private OffsetDateTime updateTime;
  private List<BanDisableData> banDisableData;

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
  @NotNull
  public BanType getType() {
    return type;
  }

  @Column(name = "duration")
  @Enumerated(EnumType.STRING)
  @NotNull
  public BanDurationType getDuration() {
    return duration;
  }

  @Column(name = "create_time")
  public OffsetDateTime getCreateTime() {
    return createTime;
  }

  @Column(name = "update_time")
  public OffsetDateTime getUpdateTime() {
    return updateTime;
  }

  @OneToMany(mappedBy = "ban", cascade = CascadeType.ALL, orphanRemoval = true)
  // Cascading is needed for Create & Delete
  @UpdatePermission(any = {Role.ALL.class}) // Permission is managed by BanDisableData class
  public List<BanDisableData> getBanDisableData() {
    return banDisableData;
  }

  @Transient
  public BanStatusType getBanStatus() {
    if (banDisableData.size() > 0) {
      return BanStatusType.DISABLED;
    }
    return (expiresAt.isBefore(OffsetDateTime.now()))
        ? BanStatusType.BANNED
        : BanStatusType.EXPIRED;
  }
}
