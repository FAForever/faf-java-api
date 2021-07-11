package com.faforever.api.data.domain;

import com.faforever.api.security.elide.permission.WriteMatchmakerMapCheck;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Table(name = "map_pool")
@Include(name = "mapPool")
@CreatePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
@UpdatePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
@DeletePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
public class MapPool implements DefaultEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  @EqualsAndHashCode.Include
  private Integer id;

  @Column(name = "create_time")
  private OffsetDateTime createTime;

  @Column(name = "update_time")
  private OffsetDateTime updateTime;

  @Column(name = "name")
  @NotNull
  @EqualsAndHashCode.Include
  private String name;

  @OneToOne(mappedBy = "mapPool")
  private MatchmakerQueueMapPool matchmakerQueueMapPool;

  @ManyToMany
  @JoinTable(name = "map_pool_map_version",
    joinColumns = @JoinColumn(name = "map_pool_id"),
    inverseJoinColumns = @JoinColumn(name = "map_version_id")
  )
  @NotNull
  @Deprecated
  // Scheduled for removal once Downlord's FAF Client v1.4.3 or higher is widely adopted
  private Set<MapVersion> mapVersions;

  @OneToMany(mappedBy = "mapPool", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<MapPoolAssignment> mapPoolAssignments;
}
