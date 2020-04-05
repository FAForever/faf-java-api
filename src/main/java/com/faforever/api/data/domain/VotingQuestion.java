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
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
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
@Include(rootLevel = true, type = VotingQuestion.TYPE_NAME)
@Setter
@EntityListeners(VotingQuestionEnricher.class)
public class VotingQuestion extends AbstractEntity {
  public static final String TYPE_NAME = "votingQuestion";

  private Integer numberOfAnswers;
  private String question;
  private String description;
  private String questionKey;
  private String descriptionKey;
  private Integer maxAnswers;
  private Integer ordinal;
  private Boolean alternativeQuestion;
  private VotingSubject votingSubject;
  private List<VotingChoice> winners;
  private Set<VotingChoice> votingChoices;

  @UpdatePermission(expression = Prefab.UPDATE_ON_CREATE)
  @Column(name = "alternative_voting")
  public Boolean isAlternativeQuestion() {
    return alternativeQuestion;
  }

  @Column(name = "ordinal")
  public Integer getOrdinal() {
    return ordinal;
  }

  /**
   * Is evaluated when voting ended and revealVote is set to true
   */
  @UpdatePermission(expression = Prefab.NONE)
  @JoinTable(name = "winner_for_voting_question",
    joinColumns = {@JoinColumn(name = "voting_question_id")},
    inverseJoinColumns = {@JoinColumn(name = "voting_choice_id")}
  )
  @ManyToMany
  public List<VotingChoice> getWinners() {
    return winners;
  }

  @ComputedAttribute
  @Transient
  public String getQuestion() {
    return question;
  }

  @ComputedAttribute
  @Transient
  public String getDescription() {
    return description;
  }

  @JsonBackReference
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "voting_subject_id")
  public VotingSubject getVotingSubject() {
    return votingSubject;
  }

  @NotNull
  @Column(name = "question_key", nullable = false)
  public String getQuestionKey() {
    return questionKey;
  }

  @Column(name = "description_key")
  public String getDescriptionKey() {
    return descriptionKey;
  }

  @UpdatePermission(expression = "Prefab.Common.UpdateOnCreate")
  @Column(name = "max_answers")
  public Integer getMaxAnswers() {
    return maxAnswers;
  }

  @Transient
  @ComputedAttribute
  public Integer getNumberOfAnswers() {
    return numberOfAnswers;
  }

  @JsonManagedReference
  @OneToMany(mappedBy = "votingQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
  public Set<VotingChoice> getVotingChoices() {
    return votingChoices;
  }
}
