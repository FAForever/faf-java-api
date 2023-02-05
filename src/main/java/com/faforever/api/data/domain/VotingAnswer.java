package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsEntityOwner;
import com.faforever.api.data.checks.Prefab;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "voting_answer")
@Include(name = VotingAnswer.TYPE_NAME, rootLevel = false)
@ReadPermission(expression = IsEntityOwner.EXPRESSION)
@UpdatePermission(expression = Prefab.NONE)
@EqualsAndHashCode(of = {"vote", "votingChoice"}, callSuper = false)
@Setter
public class VotingAnswer extends AbstractEntity<VotingAnswer> implements OwnableEntity {
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
