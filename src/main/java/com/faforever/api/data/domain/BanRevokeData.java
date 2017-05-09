package com.faforever.api.data.domain;

import com.faforever.api.data.checks.permission.HasBanRead;
import com.faforever.api.data.checks.permission.HasBanUpdate;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ban_revoke")
@Include(rootLevel = true, type = "banRevokeData")
@Setter
@DeletePermission(expression = "Prefab.Role.None")
@ReadPermission(expression = HasBanRead.EXPRESSION)
@CreatePermission(expression = HasBanUpdate.EXPRESSION)
@UpdatePermission(expression = HasBanUpdate.EXPRESSION)
public class BanRevokeData {
  // TODO: Use AbstractEntity class #73
  private int id;
  private OffsetDateTime createTime;
  private OffsetDateTime updateTime;
  private BanInfo ban;
  private String reason;
  private Player author;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ban_id")
  public int getId() {
    return id;
  }

  @Column(name = "create_time")
  public OffsetDateTime getCreateTime() {
    return createTime;
  }

  @Column(name = "update_time")
  public OffsetDateTime getUpdateTime() {
    return updateTime;
  }

  @OneToOne(mappedBy = "banRevokeData")
  @NotNull
  public BanInfo getBan() {
    return ban;
  }

  @Column(name = "reason")
  @NotNull
  public String getReason() {
    return reason;
  }

  @ManyToOne
  @JoinColumn(name = "author_id")
  @NotNull
  public Player getAuthor() {
    return author;
  }
}
