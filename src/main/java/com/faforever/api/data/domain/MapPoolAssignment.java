package com.faforever.api.data.domain;

import com.faforever.api.data.converter.JsonConverter;
import com.faforever.api.security.elide.permission.WriteMatchmakerMapCheck;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;


@Entity
@Setter
@Table(name = "map_pool_map_version")
@Include(name = "mapPoolAssignment")
@CreatePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
@UpdatePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
@DeletePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
public class MapPoolAssignment extends AbstractEntity<MapPoolAssignment> {
  private MapPool mapPool;
  private MapVersion mapVersion;
  private Integer weight;
  private java.util.Map<String, Object> mapParams;

  @OneToOne
  @JoinColumn(name = "map_pool_id")
  @NotNull
  public MapPool getMapPool() {
    return mapPool;
  }

  @OneToOne
  @JoinColumn(name = "map_version_id")
  public MapVersion getMapVersion() {
    return mapVersion;
  }

  @Column(name = "weight")
  @NotNull
  public Integer getWeight() {
    return weight;
  }

  @Column(name = "map_params")
  @Convert(converter = JsonConverter.class)
  public java.util.Map<String, Object> getMapParams() { return mapParams; }
}
