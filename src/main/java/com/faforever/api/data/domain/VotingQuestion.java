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
import lombok.AccessLevel;
import lombok.Getter;
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
@ReadPermission(expression = "Prefab.Role.All")
@DeletePermission(expression = IsModerator.EXPRESSION)
@UpdatePermission(expression = IsModerator.EXPRESSION)
@CreatePermission(expression = IsModerator.EXPRESSION)
@Audit(action = Action.CREATE, logStatement = "Created voting question with id:{0}", logExpressions = {"${votingQuestion.id}"})
@Audit(action = Action.DELETE, logStatement = "Deleted voting question with id:{0}", logExpressions = {"${votingQuestion.id}"})
@Audit(action = Action.UPDATE, logStatement = "Updated voting question with id:{0}", logExpressions = {"${votingQuestion.id}"})
@Include(rootLevel = true, type = VotingQuestion.TYPE_NAME)
@Getter
@Setter
@EntityListeners(VotingQuestionEnricher.class)
public class VotingQuestion extends AbstractEntity {
  public static final String TYPE_NAME = "votingQuestion";

  @Getter(AccessLevel.NONE) // for type Boolean Lombok generates get* method, not is*
  @UpdatePermission(expression = "Prefab.Common.UpdateOnCreate")
  @Column(name = "alternative_voting")
  private Boolean alternativeQuestion;

  @Column(name = "ordinal")
  @UpdatePermission(expression = IsModerator.EXPRESSION)
  private Integer ordinal;

  // Evaluated when voting has ended and revealVote is set to true
  @UpdatePermission(expression = "Prefab.Role.None")
  @JoinTable(name = "winner_for_voting_question",
    joinColumns = {@JoinColumn(name = "voting_question_id")},
    inverseJoinColumns = {@JoinColumn(name = "voting_choice_id")}
  )
  @ManyToMany
  private List<VotingChoice> winners;

  @ComputedAttribute
  @Transient
  private String question;

  @ComputedAttribute
  @Transient
  private String description;

  @JsonBackReference
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "voting_subject_id")
  private VotingSubject votingSubject;

  @NotNull
  @Column(name = "question_key", nullable = false)
  private String questionKey;

  @Column(name = "description_key")
  private String descriptionKey;

  @UpdatePermission(expression = "Prefab.Common.UpdateOnCreate")
  @Column(name = "max_answers")
  private Integer maxAnswers;

  @Transient
  @ComputedAttribute
  private Integer numberOfAnswers;

  @JsonManagedReference
  @OneToMany(mappedBy = "votingQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<VotingChoice> votingChoices;

  public Boolean isAlternativeQuestion() {
    return alternativeQuestion;
  }
}
