package com.faforever.api.data.domain;

import com.faforever.api.data.checks.permission.HasLadder1v1Update;
import com.yahoo.elide.annotation.Audit;
import com.yahoo.elide.annotation.Audit.Action;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Getter;
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

@CreatePermission(expression = HasLadder1v1Update.EXPRESSION)
@DeletePermission(expression = HasLadder1v1Update.EXPRESSION)
@Entity
@Getter
@Setter
@Table(name = "ladder_map")
@Include(rootLevel = true, type = "ladder1v1Map")
@Immutable
@Audit(action = Action.CREATE, logStatement = "Added map `{0}` with version `{1}` to the ladder pool", logExpressions = {"${ladder1v1Map.mapVersion.map.displayName}", "${ladder1v1Map.mapVersion.version}"})
@Audit(action = Action.DELETE, logStatement = "Removed map `{0}` with version `{1}` from the ladder pool", logExpressions = {"${ladder1v1Map.mapVersion.map.displayName}", "${ladder1v1Map.mapVersion.version}"})
public class Ladder1v1Map {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private int id;

  @OneToOne
  @JoinColumn(name = "idmap")
  @UpdatePermission(expression = HasLadder1v1Update.EXPRESSION)
  private MapVersion mapVersion;
}
