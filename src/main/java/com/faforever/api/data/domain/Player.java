package com.faforever.api.data.domain;

import com.github.jasminb.jsonapi.annotations.Type;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.SharePermission;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "login")
@Include(rootLevel = true, type = com.faforever.api.dto.Player.TYPE)
// Needed to change leader of a clan
@SharePermission
@Setter
@Type(Player.TYPE_NAME)
public class Player extends Login {
  public static final String TYPE_NAME = "player";

  private Ladder1v1Rating ladder1v1Rating;
  private GlobalRating globalRating;

  @OneToOne(mappedBy = "player", fetch = FetchType.LAZY)
  @BatchSize(size = 1000)
  public Ladder1v1Rating getLadder1v1Rating() {
    return ladder1v1Rating;
  }

  @OneToOne(mappedBy = "player", fetch = FetchType.LAZY)
  @BatchSize(size = 1000)
  public GlobalRating getGlobalRating() {
    return globalRating;
  }

  @Override
  public String toString() {
    return "Player(" + getId() + ", " + getLogin() + ")";
  }
}
