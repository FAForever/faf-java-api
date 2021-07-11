package com.faforever.api.data.domain;

import com.faforever.api.data.checks.Prefab;
import com.faforever.api.data.listeners.VotingChoiceEnricher;
import com.faforever.api.security.elide.permission.AdminVoteCheck;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yahoo.elide.annotation.Audit;
import com.yahoo.elide.annotation.Audit.Action;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Exclude;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Set;

@Entity
@Table(name = "voting_choice")
@ReadPermission(expression = Prefab.ALL)
@DeletePermission(expression = AdminVoteCheck.EXPRESSION)
@UpdatePermission(expression = AdminVoteCheck.EXPRESSION)
@CreatePermission(expression = AdminVoteCheck.EXPRESSION)
@Audit(action = Action.CREATE, logStatement = "Created voting choice with id: {0} ", logExpressions = {"${votingChoice.id}"})
@Audit(action = Action.DELETE, logStatement = "Deleted voting choice with id: {0} ", logExpressions = {"${votingChoice.id}"})
@Audit(action = Action.UPDATE, logStatement = "Updated voting choice with id: {0} ", logExpressions = {"${votingChoice.id}"})
@Include(name = VotingChoice.TYPE_NAME)
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@EntityListeners(VotingChoiceEnricher.class)
public class VotingChoice implements DefaultEntity {

  public static final String TYPE_NAME = "votingChoice";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  @EqualsAndHashCode.Include
  private Integer id;

  @Column(name = "create_time")
  private OffsetDateTime createTime;

  @Column(name = "update_time")
  private OffsetDateTime updateTime;


  @Column(name = "choice_text_key")
  @NotNull
  private String choiceTextKey;

  @ComputedAttribute
  @Transient
  private String choiceText;

  @Column(name = "description_key")
  private String descriptionKey;

  @ComputedAttribute
  @Transient
  private String description;

  @Transient
  @ComputedAttribute
  private Integer numberOfAnswers;

  @Column(name = "ordinal")
  @NotNull
  private Integer ordinal;

  @JsonBackReference
  @JoinColumn(name = "voting_question_id")
  @ManyToOne()
  private VotingQuestion votingQuestion;

  @JsonIgnore
  @Exclude
  @OneToMany(mappedBy = "votingChoice", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  private Set<VotingAnswer> votingAnswers;
}
