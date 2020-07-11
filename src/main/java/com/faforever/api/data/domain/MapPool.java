package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
@Setter
@Table(name = "map_pool")
@Include(rootLevel = true, type = "mapPool")
@Immutable
public class MapPool extends AbstractEntity {
  private String name;
  private Set<MapVersion> mapVersions;

  @NotNull
  public String getName() {
    return name;
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
