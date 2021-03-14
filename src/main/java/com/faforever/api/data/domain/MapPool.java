package com.faforever.api.data.domain;

import com.faforever.api.security.elide.permission.WriteMatchmakerMapCheck;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.Valid;
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
  private Set<MapPoolAssignment> mapPoolAssignments;

  @NotNull
  public String getName() {
    return name;
  }

  @OneToOne(mappedBy = "mapPool")
  public MatchmakerQueueMapPool getMatchmakerQueueMapPool() {
    return matchmakerQueueMapPool;
  }

  @OneToMany(mappedBy = "mapPool", cascade = CascadeType.ALL, orphanRemoval = true)
  @Valid
  public Set<MapPoolAssignment> getMapPoolAssignments() {
    return mapPoolAssignments;
  }
}
