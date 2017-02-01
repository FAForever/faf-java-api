package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "login")
@Include(rootLevel = true, type = "player")
public class Player extends Login {

  private Ladder1v1Rating ladder1v1Rating;
  private GlobalRating globalRating;
  @Setter
  private List<Clan> clan;

  @OneToOne(mappedBy = "player")
  public Ladder1v1Rating getLadder1v1Rating() {
    return ladder1v1Rating;
  }

  public void setLadder1v1Rating(Ladder1v1Rating ladder1v1Rating) {
    this.ladder1v1Rating = ladder1v1Rating;
  }

  @OneToOne(mappedBy = "player")
  public GlobalRating getGlobalRating() {
    return globalRating;
  }

  public void setGlobalRating(GlobalRating globalRating) {
    this.globalRating = globalRating;
  }
  
  @ManyToMany(mappedBy="members")
  public List<Clan> getClan() { return this.clan; }
}
