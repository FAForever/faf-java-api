package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
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
@Table(name = "ban_disable_data")
@Include(rootLevel = true, type = "banDisableData")
@Setter
public class BanRevokeData {
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
