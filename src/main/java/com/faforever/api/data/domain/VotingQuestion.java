package com.faforever.api.data.domain;

import com.faforever.api.data.checks.permission.IsModerator;
import com.faforever.api.data.listeners.VotingQuestionEnricher;
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
import lombok.EqualsAndHashCode;
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

@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
@Table(name = "voting_question")
@ReadPermission(expression = "Prefab.Role.All")
@DeletePermission(expression = IsModerator.EXPRESSION)
@UpdatePermission(expression = IsModerator.EXPRESSION)
@CreatePermission(expression = IsModerator.EXPRESSION)
@Audit(action = Action.CREATE, logStatement = "Created voting question with id:{0}", logExpressions = {"${votingQuestion.id}"})
@Audit(action = Action.DELETE, logStatement = "Deleted voting question with id:{0}", logExpressions = {"${votingQuestion.id}"})
@Audit(action = Action.UPDATE, logStatement = "Updated voting question with id:{0}", logExpressions = {"${votingQuestion.id}"})
@Include(rootLevel = true, type = VotingQuestion.TYPE_NAME)
@Setter
@EntityListeners(VotingQuestionEnricher.class)
public class VotingQuestion extends AbstractEntity {
  public static final String TYPE_NAME = "votingQuestion";

  private int numberOfAnswers;
  private String question;
  private String description;
  private String questionKey;
  private String descriptionKey;
  private int maxAnswers;
  private Boolean alternativeQuestion;
  private VotingSubject votingSubject;
  private List<VotingChoice> winners;
  private Set<VotingChoice> votingChoices;

  @UpdatePermission(expression = "Prefab.Common.UpdateOnCreate")
  @Column(name = "alternative_voting")
  public Boolean isAlternativeQuestion() {
    return alternativeQuestion;
  }

  /**
   * Is evaluated when voting ended and revealVote is set to true
   */
  @UpdatePermission(expression = "Prefab.Role.None")
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
  public int getMaxAnswers() {
    return maxAnswers;
  }

  @Transient
  @ComputedAttribute
  public int getNumberOfAnswers() {
    return numberOfAnswers;
  }

  @JsonManagedReference
  @OneToMany(mappedBy = "votingQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
  public Set<VotingChoice> getVotingChoices() {
    return votingChoices;
  }
}
