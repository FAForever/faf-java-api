package com.faforever.api.data.domain;

import com.faforever.api.security.elide.permission.WriteMatchmakerMapCheck;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Column;
import javax.persistence.Entity;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;

import javax.persistence.OneToOne;
import javax.persistence.Table;


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
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  public Integer getId() {
    return id;
  }

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
  public String getMapParams() { return mapParams; }
}
