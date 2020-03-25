package com.faforever.api.dto;

import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Type(VotingSubject.TYPE)
public class VotingSubject extends AbstractEntity {
  public static final String TYPE = "votingSubject";

  private String subjectKey;
  private String subject;
  private Integer numberOfVotes;
  private String topicUrl;
  private OffsetDateTime beginOfVoteTime;
  private OffsetDateTime endOfVoteTime;
  private Integer minGamesToVote;
  private String descriptionKey;
  private String description;
  private Boolean revealWinner;
  @Relationship("votes")
  private List<Vote> votes;
  @Relationship("votingQuestions")
  private List<VotingQuestion> votingQuestions;
}

