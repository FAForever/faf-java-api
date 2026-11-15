package com.faforever.api.data.domain;

import com.faforever.api.data.annotation.Ephemeral;
import com.faforever.api.data.checks.Prefab;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yahoo.elide.annotation.Audit;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.ToOne;
import com.yahoo.elide.annotation.UpdatePermission;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.Setter;

@Setter
@Include(name = "gameReviewRequest", rootLevel = false)
@Ephemeral
@CreatePermission(expression = Prefab.ALL)
@UpdatePermission(expression = Prefab.NONE)
@ReadPermission(expression = Prefab.ALL)
@DeletePermission(expression = Prefab.NONE)
@Audit(action = Audit.Action.CREATE, logStatement = "Review has been requested for game ''{0}''", logExpressions = {"${gameReviewRequest.game.id}"})
public class GameReviewRequest implements OwnableEntity {

  private Game game;
  private Player player;
  private String requestDescription;

  @Id
  @GeneratedValue
  public String getId() {
      return "N/A";
  }

  @ToOne
  public Game getGame() {
    return game;
  }

  @ToOne
  public Player getPlayer() {
    return player;
  }

  public String getRequestDescription() {
    return requestDescription;
  }

  @Transient
  @Override
  @JsonIgnore
  public Login getEntityOwner() {
    return getPlayer();
  }
}
