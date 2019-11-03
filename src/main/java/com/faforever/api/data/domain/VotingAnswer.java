package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsEntityOwner;
import com.faforever.api.data.checks.Prefab;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.EqualsAndHashCode;
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
@UpdatePermission(expression = Prefab.NONE)
@EqualsAndHashCode(of = {"vote", "votingChoice"}, callSuper = false)
@Setter
public class VotingAnswer extends AbstractEntity implements OwnableEntity {
  public static final String TYPE_NAME = "votingAnswer";

  private Vote vote;
  private Integer alternativeOrdinal;
  private VotingChoice votingChoice;

  @Column(name = "alternative_ordinal")
  public Integer getAlternativeOrdinal() {
    return alternativeOrdinal;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "vote_id")
  public Vote getVote() {
    return vote;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "voting_choice_id")
  public VotingChoice getVotingChoice() {
    return votingChoice;
  }

  @Transient
  @Override
  public Login getEntityOwner() {
    return vote.getEntityOwner();
  }
}
