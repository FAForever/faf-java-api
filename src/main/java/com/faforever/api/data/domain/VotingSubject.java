package com.faforever.api.data.domain;

import com.faforever.api.data.checks.permission.IsModerator;
import com.faforever.api.data.listeners.VotingSubjectEnricher;
import com.faforever.api.data.validation.VotingSubjectRevealWinnerCheck;
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
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Set;

@Entity
@Table(name = "voting_subject")
@Include(rootLevel = true, type = VotingSubject.TYPE_NAME)
@ReadPermission(expression = "Prefab.Role.All")
@DeletePermission(expression = IsModerator.EXPRESSION)
@UpdatePermission(expression = IsModerator.EXPRESSION)
@CreatePermission(expression = IsModerator.EXPRESSION)
@Audit(action = Action.CREATE, logStatement = "Created voting subject with id: {0}", logExpressions = {"${votingSubject.id}"})
@Audit(action = Action.DELETE, logStatement = "Deleted voting subject with id: {0}", logExpressions = {"${votingSubject.id}"})
@Audit(action = Action.UPDATE, logStatement = "Updated voting subject with id: {0}", logExpressions = {"${votingSubject.id}"})
@Getter
@Setter
@EntityListeners(VotingSubjectEnricher.class)
@VotingSubjectRevealWinnerCheck
public class VotingSubject extends AbstractEntity {
  public static final String TYPE_NAME = "votingSubject";

  @Column(name = "subject_key")
  @NotNull
  private String subjectKey;

  @ComputedAttribute
  @Transient
  private String subject;

  @Column(name = "description_key")
  private String descriptionKey;

  @Transient
  @ComputedAttribute
  private int numberOfVotes;

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

  @ComputedAttribute
  @Transient
  private String description;

  @Column(name = "topic_url")
  private String topicUrl;

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
