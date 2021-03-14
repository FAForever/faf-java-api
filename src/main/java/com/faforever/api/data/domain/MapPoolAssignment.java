package com.faforever.api.data.domain;

import com.faforever.api.security.elide.permission.WriteMatchmakerMapCheck;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
@Setter
@Table(name = "map_pool_map_version")
@Include(rootLevel = true, type = "mapPoolAssignment")
@CreatePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
@UpdatePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
@DeletePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
public class MapPoolAssignment {
  private int id;
  private MapPool mapPool;
  private MapVersion mapVersion;
  private int weight;
  private String mapParams;

  @Id
  @Column(name = "id")
  public Integer getId() {
    return id;
  }

  @OneToOne
  @JoinColumn(name = "map_pool_id")
  public MapPool getMapPool() {
    return mapPool;
  }

  @OneToOne
  @JoinColumn(name = "map_version_id")
  public MapVersion getMapVersion() {
    return mapVersion;
  }

  @Column(name = "weight")
  public Integer getWeight() {
    return weight;
  }

  @Column(name = "map_params")
  public String getMapParams() { return mapParams; }
}
