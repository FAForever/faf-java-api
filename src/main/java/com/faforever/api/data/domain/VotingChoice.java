package com.faforever.api.data.domain;

import com.faforever.api.data.checks.permission.IsModerator;
import com.faforever.api.data.listeners.VotingChoiceEnricher;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Exclude;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.SharePermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(name = "voting_choice")
@ReadPermission(expression = "Prefab.Role.All")
@SharePermission(expression = IsModerator.EXPRESSION)
@DeletePermission(expression = IsModerator.EXPRESSION)
@UpdatePermission(expression = IsModerator.EXPRESSION)
@CreatePermission(expression = IsModerator.EXPRESSION)
@Include(rootLevel = true, type = VotingChoice.TYPE_NAME)
@Setter
@EntityListeners(VotingChoiceEnricher.class)
public class VotingChoice extends AbstractEntity {
  public static final String TYPE_NAME = "votingChoice";

  private String choiceTextKey;
  private String choiceText;
  private String descriptionKey;
  private String description;
  private int numberOfAnswers;
  private int ordinal;
  private VotingQuestion votingQuestion;
  private List<VotingAnswer> votingAnswers;

  @Column(name = "choice_text_key")
  @NotNull
  public String getChoiceTextKey() {
    return choiceTextKey;
  }

  @ComputedAttribute
  @Transient
  public String getChoiceText() {
    return choiceText;
  }

  @Column(name = "description_key")
  public String getDescriptionKey() {
    return descriptionKey;
  }

  @ComputedAttribute
  @Transient
  public String getDescription() {
    return description;
  }

  @Column(name = "ordinal")
  @NotNull
  public int getOrdinal() {
    return ordinal;
  }

  @Transient
  @ComputedAttribute
  public int getNumberOfAnswers() {
    return numberOfAnswers;
  }

  @JsonBackReference
  @JoinColumn(name = "voting_question_id")
  @ManyToOne()
  public VotingQuestion getVotingQuestion() {
    return votingQuestion;
  }

  @JsonIgnore
  @Exclude
  @OneToMany(mappedBy = "votingChoice", cascade = CascadeType.ALL, orphanRemoval = true)
  public List<VotingAnswer> getVotingAnswers() {
    return votingAnswers;
  }
}
