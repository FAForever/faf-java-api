package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Entity
@Table(name = "name_history")
@Include(rootLevel = true, type = "nameRecord")
@DeletePermission(expression = "Prefab.Role.None")
@UpdatePermission(expression = "Prefab.Role.None")
@Getter
@Setter
public class NameRecord {

  @Id
  @Column(name = "id")
  private int id;

  @Column(name = "change_time")
  private OffsetDateTime changeTime;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @NotNull
  private Player player;

  @Column(name = "previous_name")
  @NotNull
  private String name;
}
