package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsEntityOwner;
import com.faforever.api.data.checks.Prefab;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.time.OffsetDateTime;

@Entity
@Table(name = "voting_answer")
@Include(name = VotingAnswer.TYPE_NAME, rootLevel = false)
@ReadPermission(expression = IsEntityOwner.EXPRESSION)
@UpdatePermission(expression = Prefab.NONE)
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class VotingAnswer implements DefaultEntity, OwnableEntity {
  public static final String TYPE_NAME = "votingAnswer";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  @EqualsAndHashCode.Include
  private Integer id;

  @Column(name = "create_time")
  private OffsetDateTime createTime;

  @Column(name = "update_time")
  private OffsetDateTime updateTime;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "vote_id")
  @EqualsAndHashCode.Include
  private Vote vote;

  @Column(name = "alternative_ordinal")
  private Integer alternativeOrdinal;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "voting_choice_id")
  @EqualsAndHashCode.Include
  private VotingChoice votingChoice;

  @Transient
  @Override
  public Login getEntityOwner() {
    return vote.getEntityOwner();
  }
}
