package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsEntityOwner;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Set;

@Entity
@Table(name = "vote")
@Include(type = Vote.TYPE_NAME, rootLevel = true)
@ReadPermission(expression = IsEntityOwner.EXPRESSION)
@UpdatePermission(expression = "Prefab.Role.None")
@EqualsAndHashCode(of = {"player", "votingSubject"}, callSuper = false)
@Getter
@Setter
public class Vote extends AbstractEntity implements OwnableEntity {
  public static final String TYPE_NAME = "vote";

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "player_id")
  private Player player;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "voting_subject_id")
  private VotingSubject votingSubject;

  @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<VotingAnswer> votingAnswers;

  @Transient
  @Override
  public Login getEntityOwner() {
    return getPlayer();
  }
}
