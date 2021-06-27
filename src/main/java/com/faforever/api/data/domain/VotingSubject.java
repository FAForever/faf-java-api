package com.faforever.api.data.domain;

import com.faforever.api.data.checks.Prefab;
import com.faforever.api.data.listeners.VotingSubjectEnricher;
import com.faforever.api.data.validation.VotingSubjectRevealWinnerCheck;
import com.faforever.api.security.elide.permission.AdminVoteCheck;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Set;

@Entity
@Table(name = "voting_subject")
@Include(name = VotingSubject.TYPE_NAME)
@ReadPermission(expression = Prefab.ALL)
@DeletePermission(expression = AdminVoteCheck.EXPRESSION)
@UpdatePermission(expression = AdminVoteCheck.EXPRESSION)
@CreatePermission(expression = AdminVoteCheck.EXPRESSION)
@Audit(action = Action.CREATE, logStatement = "Created voting subject with id: {0}", logExpressions = {"${votingSubject.id}"})
@Audit(action = Action.DELETE, logStatement = "Deleted voting subject with id: {0}", logExpressions = {"${votingSubject.id}"})
@Audit(action = Action.UPDATE, logStatement = "Updated voting subject with id: {0}", logExpressions = {"${votingSubject.id}"})
@Data
@NoArgsConstructor
@EntityListeners(VotingSubjectEnricher.class)
@VotingSubjectRevealWinnerCheck
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class VotingSubject implements DefaultEntity {

  public static final String TYPE_NAME = "votingSubject";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  @EqualsAndHashCode.Include
  private Integer id;

  @Column(name = "create_time")
  private OffsetDateTime createTime;

  @Column(name = "update_time")
  private OffsetDateTime updateTime;


  @Column(name = "subject_key")
  @NotNull
  private String subjectKey;

  @ComputedAttribute
  @Transient
  private String subject;

  @Transient
  @ComputedAttribute
  private int numberOfVotes;

  @Column(name = "topic_url")
  private String topicUrl;

  @UpdatePermission(expression = "Prefab.Common.UpdateOnCreate")
  @NotNull
  @Column(name = "begin_of_vote_time")
  private OffsetDateTime beginOfVoteTime;

  @UpdatePermission(expression = "Prefab.Common.UpdateOnCreate")
  @NotNull
  @Column(name = "end_of_vote_time")
  private OffsetDateTime endOfVoteTime;

  @DecimalMin("0")
  @Column(name = "min_games_to_vote")
  private int minGamesToVote;

  @Column(name = "description_key")
  private String descriptionKey;

  @ComputedAttribute
  @Transient
  private String description;

  @Column(name = "reveal_winner")
  private Boolean revealWinner;

  @JsonIgnore
  @Exclude
  @OneToMany(mappedBy = "votingSubject", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Vote> votes;

  @JsonManagedReference
  @OneToMany(mappedBy = "votingSubject", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<VotingQuestion> votingQuestions;
}
