package com.faforever.api.data.domain;

import com.faforever.api.data.annotation.Ephemeral;
import com.faforever.api.data.checks.Prefab;
import com.yahoo.elide.annotation.Audit;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ToOne;
import com.yahoo.elide.annotation.UpdatePermission;
import jakarta.persistence.Id;
import lombok.Setter;

@Setter
@Include(name = "gameReviewRequest", rootLevel = false)
@Ephemeral
@CreatePermission(expression = Prefab.ALL)
@UpdatePermission(expression = Prefab.NONE)
@DeletePermission(expression = Prefab.NONE)
@Audit(action = Audit.Action.CREATE, logStatement = "Review has been requested for game ''{0}''", logExpressions = {"${gameReviewRequest.game.id}"})
public class GameReviewRequest {

  private String id;
  private Game game;
  private String requestDescription;

  @Id
  public String getId() {
    return id;
  }

  @ToOne
  public Game getGame() {
    return game;
  }

  public String getRequestDescription() {
    return requestDescription;
  }
}
