package com.faforever.api.data.domain;

import com.faforever.api.security.elide.permission.WriteMatchmakerMapCheck;
import com.yahoo.elide.annotation.Audit;
import com.yahoo.elide.annotation.Audit.Action;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@CreatePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
@DeletePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
@Entity
@Setter
@Table(name = "ladder_map")
@Include(rootLevel = true, type = "ladder1v1Map")
@Immutable
@Audit(action = Action.CREATE, logStatement = "Added map `{0}` with version `{1}` to the ladder pool", logExpressions = {"${ladder1v1Map.mapVersion.map.displayName}", "${ladder1v1Map.mapVersion.version}"})
@Audit(action = Action.DELETE, logStatement = "Removed map `{0}` with version `{1}` from the ladder pool", logExpressions = {"${ladder1v1Map.mapVersion.map.displayName}", "${ladder1v1Map.mapVersion.version}"})
public class Ladder1v1Map {
  private int id;
  private MapVersion mapVersion;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  public int getId() {
    return id;
  }

  @OneToOne
  @JoinColumn(name = "idmap")
  @UpdatePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
  public MapVersion getMapVersion() {
    return mapVersion;
  }
}
