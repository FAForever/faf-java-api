package com.faforever.api.dto;

import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Type(Vote.TYPE)
public class Vote extends AbstractEntity {
  public static final String TYPE = "vote";

  @Relationship("player")
  private Player player;
  @Relationship("votingSubject")
  private VotingSubject votingSubject;
  @Relationship("votingAnswers")
  private List<VotingAnswer> votingAnswers;
}
