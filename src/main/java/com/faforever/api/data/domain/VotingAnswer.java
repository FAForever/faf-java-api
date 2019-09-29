package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsEntityOwner;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "voting_answer")
@Include(type = VotingAnswer.TYPE_NAME)
@ReadPermission(expression = IsEntityOwner.EXPRESSION)
@UpdatePermission(expression = "Prefab.Role.None")
@EqualsAndHashCode(of = {"vote", "votingChoice"}, callSuper = false)
@Getter
@Setter
public class VotingAnswer extends AbstractEntity implements OwnableEntity {
  public static final String TYPE_NAME = "votingAnswer";

  @Column(name = "alternative_ordinal")
  private Integer alternativeOrdinal;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "vote_id")
  private Vote vote;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "voting_choice_id")
  private VotingChoice votingChoice;

  @Transient
  @Override
  public Login getEntityOwner() {
    return vote.getEntityOwner();
  }
}
