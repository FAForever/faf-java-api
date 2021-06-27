package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import static com.faforever.api.data.domain.Ladder1v1Rating.TYPE_NAME;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "ladder1v1_rating_rank_view")
@Include(name = TYPE_NAME)
public class Ladder1v1Rating extends Rating {

  public static final String TYPE_NAME = "ladder1v1Rating";

  @Column(name = "win_games", updatable = false)
  private int wonGames;
}
