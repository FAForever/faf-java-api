package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsEntityOwner;
import com.faforever.api.data.checks.Prefab;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import static com.faforever.api.data.domain.GameReview.TYPE_NAME;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Include(name = TYPE_NAME)
@Entity
@Table(name = "game_review")
@CreatePermission(expression = Prefab.ALL)
@DeletePermission(expression = IsEntityOwner.EXPRESSION)
public class GameReview extends Review {

  public static final String TYPE_NAME = "gameReview";

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "game_id")
  @UpdatePermission(expression = Prefab.ALL)
  private Game game;
}
