package com.faforever.api.data.domain;

import com.faforever.api.security.elide.permission.WriteMatchmakerMapCheck;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
@Setter
@Table(name = "map_pool")
@Include(rootLevel = true, type = "mapPool")
@CreatePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
@UpdatePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
@DeletePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
public class MapPool extends AbstractEntity {
  private String name;
  private MatchmakerQueueMapPool matchmakerQueueMapPool;
  private Set<MapVersion> mapVersions;

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
  public Set<MapVersion> getMapVersions() {
    return mapVersions;
  }
}
