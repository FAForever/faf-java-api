package com.faforever.api.data.domain;

import com.faforever.api.data.checks.permission.IsModerator;
import com.faforever.api.data.listeners.VotingSubjectEnricher;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Exclude;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.SharePermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

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
import java.util.List;

@Entity
@Table(name = "voting_subject")
@Include(rootLevel = true, type = VotingSubject.TYPE_NAME)
@ReadPermission(expression = "Prefab.Role.All")
@SharePermission(expression = IsModerator.EXPRESSION)
@DeletePermission(expression = IsModerator.EXPRESSION)
@UpdatePermission(expression = IsModerator.EXPRESSION)
@CreatePermission(expression = IsModerator.EXPRESSION)
@Setter
@EntityListeners(VotingSubjectEnricher.class)
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
  private List<Vote> votes;
  private List<VotingQuestion> votingQuestions;

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

  @NotNull
  @Column(name = "begin_of_vote_time")
  public OffsetDateTime getBeginOfVoteTime() {
    return beginOfVoteTime;
  }

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

  @JsonIgnore
  @Exclude
  @OneToMany(mappedBy = "votingSubject", cascade = CascadeType.ALL, orphanRemoval = true)
  public List<Vote> getVotes() {
    return votes;
  }

  @JsonManagedReference
  @NotEmpty(message = "A subject needs at least one Question")
  @OneToMany(mappedBy = "votingSubject", cascade = CascadeType.ALL, orphanRemoval = true)
  public List<VotingQuestion> getVotingQuestions() {
    return votingQuestions;
  }
}
