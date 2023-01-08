package com.faforever.api.data.domain;

import com.faforever.api.data.checks.Prefab;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Entity
@Table(name = "name_history")
@Include(name = "nameRecord")
@DeletePermission(expression = Prefab.NONE)
@UpdatePermission(expression = Prefab.NONE)
@Setter
public class NameRecord {
  private int id;
  private OffsetDateTime changeTime;
  private Player player;
  private String name;

  @Id
  @Column(name = "id")
  public int getId() {
    return id;
  }

  @Column(name = "change_time")
  public OffsetDateTime getChangeTime() {
    return changeTime;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @NotNull
  public Player getPlayer() {
    return player;
  }

  @Column(name = "previous_name")
  @NotNull
  public String getName() {
    return name;
  }
}
