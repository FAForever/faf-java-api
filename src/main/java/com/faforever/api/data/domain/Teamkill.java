package com.faforever.api.data.domain;

import com.faforever.api.security.elide.permission.ReadTeamkillReportCheck;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "teamkills")
@Include(name = Teamkill.TYPE_NAME)
@Immutable
@ReadPermission(expression = ReadTeamkillReportCheck.EXPRESSION)
public class Teamkill {
  public static final String TYPE_NAME = "teamkill";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  @EqualsAndHashCode.Include
  private int id;

  @ManyToOne
  @JoinColumn(name = "teamkiller")
  private Player teamkiller;

  @ManyToOne
  @JoinColumn(name = "victim")
  private Player victim;

  @ManyToOne
  @JoinColumn(name = "game_id")
  private Game game;

  @Column(name = "gametime")
  private long gameTime;

  @Column(name = "reported_at")
  private OffsetDateTime reportedAt;
}
