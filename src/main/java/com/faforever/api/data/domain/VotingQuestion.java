package com.faforever.api.data.domain;

import com.faforever.api.data.checks.Prefab;
import com.faforever.api.data.listeners.VotingQuestionEnricher;
import com.faforever.api.security.elide.permission.AdminVoteCheck;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.yahoo.elide.annotation.Audit;
import com.yahoo.elide.annotation.Audit.Action;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "voting_question")
@ReadPermission(expression = Prefab.ALL)
@DeletePermission(expression = AdminVoteCheck.EXPRESSION)
@UpdatePermission(expression = AdminVoteCheck.EXPRESSION)
@CreatePermission(expression = AdminVoteCheck.EXPRESSION)
@Audit(action = Action.CREATE, logStatement = "Created voting question with id:{0}", logExpressions = {"${votingQuestion.id}"})
@Audit(action = Action.DELETE, logStatement = "Deleted voting question with id:{0}", logExpressions = {"${votingQuestion.id}"})
@Audit(action = Action.UPDATE, logStatement = "Updated voting question with id:{0}", logExpressions = {"${votingQuestion.id}"})
@Include(name = VotingQuestion.TYPE_NAME)
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(VotingQuestionEnricher.class)
public class VotingQuestion implements DefaultEntity {

  public static final String TYPE_NAME = "votingQuestion";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  @EqualsAndHashCode.Include
  private Integer id;

  @Column(name = "create_time")
  private OffsetDateTime createTime;

  @Column(name = "update_time")
  private OffsetDateTime updateTime;

  @Transient
  @ComputedAttribute
  private Integer numberOfAnswers;

  @ComputedAttribute
  @Transient
  private String question;

  @ComputedAttribute
  @Transient
  private String description;

  @NotNull
  @Column(name = "question_key", nullable = false)
  private String questionKey;

  @Column(name = "description_key")
  private String descriptionKey;

  @UpdatePermission(expression = "Prefab.Common.UpdateOnCreate")
  @Column(name = "max_answers")
  private Integer maxAnswers;

  @Column(name = "ordinal")
  private Integer ordinal;

  @Column(name = "alternative_voting")
  private Boolean alternativeQuestion;

  @JsonBackReference
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "voting_subject_id")
  private VotingSubject votingSubject;

  /**
   * Is evaluated when voting ended and revealVote is set to true
   */
  @UpdatePermission(expression = Prefab.NONE)
  @JoinTable(name = "winner_for_voting_question",
    joinColumns = {@JoinColumn(name = "voting_question_id")},
    inverseJoinColumns = {@JoinColumn(name = "voting_choice_id")}
  )
  @ManyToMany
  private List<VotingChoice> winners;

  @JsonManagedReference
  @OneToMany(mappedBy = "votingQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<VotingChoice> votingChoices;
}
