package com.faforever.api.data.domain;

import com.faforever.api.security.elide.permission.ReadTeamkillReportCheck;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Setter
@Table(name = "teamkills")
@Include(name = Teamkill.TYPE_NAME)
@Immutable
@ReadPermission(expression = ReadTeamkillReportCheck.EXPRESSION)
public class Teamkill {
  public static final String TYPE_NAME = "teamkill";

  private int id;
  private Player teamkiller;
  private Player victim;
  private Game game;
  private long gameTime;
  private OffsetDateTime reportedAt;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  public int getId() {
    return id;
  }

  @ManyToOne
  @JoinColumn(name = "teamkiller")
  public Player getTeamkiller() {
    return teamkiller;
  }

  @ManyToOne
  @JoinColumn(name = "victim")
  public Player getVictim() {
    return victim;
  }

  @ManyToOne
  @JoinColumn(name = "game_id")
  public Game getGame() {
    return game;
  }

  @Column(name = "gametime")
  public long getGameTime() {
    return gameTime;
  }

  @Column(name = "reported_at")
  public OffsetDateTime getReportedAt() {
    return reportedAt;
  }
}
