package com.faforever.api.data.domain;

import com.faforever.api.data.checks.permission.IsModerator;
import com.faforever.api.data.listeners.VotingQuestionEnricher;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.ComputedRelationship;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.SharePermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(name = "voting_question")
@ReadPermission(expression = "Prefab.Role.All")
@SharePermission(expression = IsModerator.EXPRESSION)
@DeletePermission(expression = IsModerator.EXPRESSION)
@UpdatePermission(expression = IsModerator.EXPRESSION)
@CreatePermission(expression = IsModerator.EXPRESSION)
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
  private VotingChoice alternativeWinner;
  private List<VotingChoice> votingChoices;

  @Column(name = "alternative_voting")
  public Boolean isAlternativeQuestion() {
    return alternativeQuestion;
  }

  @Transient
  @ComputedRelationship
  @ManyToOne
  public VotingChoice getAlternativeWinner() {
    return alternativeWinner;
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
  public List<VotingChoice> getVotingChoices() {
    return votingChoices;
  }
}
