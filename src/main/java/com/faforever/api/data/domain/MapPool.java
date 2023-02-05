package com.faforever.api.data.domain;

import com.faforever.api.security.elide.permission.WriteMatchmakerMapCheck;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

@Entity
@Setter
@Table(name = "map_pool")
@Include(name = "mapPool")
@CreatePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
@UpdatePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
@DeletePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
public class MapPool extends AbstractEntity<MapPool> {
  private String name;
  private MatchmakerQueueMapPool matchmakerQueueMapPool;
  @Deprecated
  // Scheduled for removal once Downlord's FAF Client v1.4.3 or higher is widely adopted
  private Set<MapVersion> mapVersions;
  private Set<MapPoolAssignment> mapPoolAssignments;

  @NotNull
  public String getName() {
    return name;
  }

  @OneToOne(mappedBy = "mapPool")
  public MatchmakerQueueMapPool getMatchmakerQueueMapPool() {
    return matchmakerQueueMapPool;
  }

  @ManyToMany
  @JoinTable(name = "map_pool_map_version",
    joinColumns = @JoinColumn(name = "map_pool_id"),
    inverseJoinColumns = @JoinColumn(name = "map_version_id")
  )
  @NotNull
  @Deprecated
  // Scheduled for removal once Downlord's FAF Client v1.4.3 or higher is widely adopted
  public Set<MapVersion> getMapVersions() {
    return mapVersions;
  }

  @OneToMany(mappedBy = "mapPool", cascade = CascadeType.ALL, orphanRemoval = true)
  public Set<MapPoolAssignment> getMapPoolAssignments() {
    return mapPoolAssignments;
  }

}
