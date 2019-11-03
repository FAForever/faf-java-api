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
@ReadPermission(expression = Prefab.ALL)
@DeletePermission(expression = AdminVoteCheck.EXPRESSION)
@UpdatePermission(expression = AdminVoteCheck.EXPRESSION)
@CreatePermission(expression = AdminVoteCheck.EXPRESSION)
@Audit(action = Action.CREATE, logStatement = "Created voting subject with id: {0}", logExpressions = {"${votingSubject.id}"})
@Audit(action = Action.DELETE, logStatement = "Deleted voting subject with id: {0}", logExpressions = {"${votingSubject.id}"})
@Audit(action = Action.UPDATE, logStatement = "Updated voting subject with id: {0}", logExpressions = {"${votingSubject.id}"})
@Setter
@EntityListeners(VotingSubjectEnricher.class)
@VotingSubjectRevealWinnerCheck
public class VotingSubject extends AbstractEntity {
  public static final String TYPE_NAME = "votingSubject";

  private String subjectKey;
  private String subject;
  private int numberOfVotes;
  private String topicUrl;
  private OffsetDateTime beginOfVoteTime;
  private OffsetDateTime endOfVoteTime;
  private int minGamesToVote;
  private String descriptionKey;
  private String description;
  private Boolean revealWinner;
  private Set<Vote> votes;
  private Set<VotingQuestion> votingQuestions;

  @Column(name = "subject_key")
  @NotNull
  public String getSubjectKey() {
    return subjectKey;
  }

  @ComputedAttribute
  @Transient
  public String getSubject() {
    return subject;
  }

  @Column(name = "description_key")
  public String getDescriptionKey() {
    return descriptionKey;
  }

  @Transient
  @ComputedAttribute
  public int getNumberOfVotes() {
    return numberOfVotes;
  }

  @UpdatePermission(expression = "Prefab.Common.UpdateOnCreate")
  @NotNull
  @Column(name = "begin_of_vote_time")
  public OffsetDateTime getBeginOfVoteTime() {
    return beginOfVoteTime;
  }

  @UpdatePermission(expression = "Prefab.Common.UpdateOnCreate")
  @NotNull
  @Column(name = "end_of_vote_time")
  public OffsetDateTime getEndOfVoteTime() {
    return endOfVoteTime;
  }

  @DecimalMin("0")
  @Column(name = "min_games_to_vote")
  public int getMinGamesToVote() {
    return minGamesToVote;
  }

  @ComputedAttribute
  @Transient
  public String getDescription() {
    return description;
  }

  @Column(name = "topic_url")
  public String getTopicUrl() {
    return topicUrl;
  }

  @Column(name = "reveal_winner")
  public Boolean getRevealWinner() {
    return revealWinner;
  }

  @JsonIgnore
  @Exclude
  @OneToMany(mappedBy = "votingSubject", cascade = CascadeType.ALL, orphanRemoval = true)
  public Set<Vote> getVotes() {
    return votes;
  }

  @JsonManagedReference
  @OneToMany(mappedBy = "votingSubject", cascade = CascadeType.ALL, orphanRemoval = true)
  public Set<VotingQuestion> getVotingQuestions() {
    return votingQuestions;
  }
}
